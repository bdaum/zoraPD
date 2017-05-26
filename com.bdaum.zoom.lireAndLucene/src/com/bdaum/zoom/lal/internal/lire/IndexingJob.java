/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.lal.internal.lire;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import net.semanticmetadata.lire.DocumentBuilder;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.job.Messages;
import com.bdaum.zoom.job.ProfiledSchedulingRule;
import com.bdaum.zoom.lal.internal.LireActivator;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

/**
 * Job for indexing images in Lucene (similarity, full text search)
 *
 */
@SuppressWarnings("restriction")
public class IndexingJob extends Job {

	private static final ProfiledSchedulingRule indexingRule = new ProfiledSchedulingRule(
			IndexingJob.class, IProfiledOperation.INDEX);
	private static final int MAXTRANS = 50;
	private static final int MAXDELAY = 5000;
	private static final int FACTOR = 4;
	private static final long MINDELAY = 50;
	private Collection<Asset> assets;
	private MultiStatus status;
	private String[] assetIds;
	private boolean reimport;
	private IDbManager dbManager;
	private Set<String> postponed = new HashSet<String>(511);
	private ThreadMXBean threadMXBean;
	private int totalWork = -1;
	private int worked = 0;
	private boolean reset;
	private File indexBackup;
	private Date lastBackup;
	private boolean noIndex = false;
	private long startTime;

	/**
	 * Constructor
	 *
	 * @param assets
	 *            - images to index
	 * @param reimport
	 *            - true if images are re-imported
	 * @param totalWork
	 *            - total work (for progress monitor)
	 * @param worked
	 *            - already worked (for progress monitor)
	 * @param system
	 *            - true if job should run as system job (unused, runs always as
	 *            system job)
	 */
	public IndexingJob(Collection<Asset> assets, boolean reimport,
			int totalWork, int worked, boolean system) {
		super(Messages.getString("IndexingJob.Indexing")); //$NON-NLS-1$
		this.assets = assets;
		this.reimport = reimport;
		this.totalWork = totalWork >= 0 ? totalWork : assets.size();
		this.worked = worked;
		setSystem(true);
		init();
		for (Asset asset : assets)
			postponed.add(asset.getStringId());
	}

	/**
	 * Constructor
	 *
	 * @param assetIds
	 *            - assets to be index or null. If null is supplied the index is
	 *            reset and then all assets in the catalog will be indexed
	 */
	public IndexingJob(String[] assetIds) {
		super(Messages.getString("IndexingJob.Indexing")); //$NON-NLS-1$
		this.assetIds = assetIds;
		init();
		if (assetIds == null) {
			reset = true;
			if (!noIndex)
				extractAssedIds(dbManager.obtainAssets());
		} else {
			reimport = true;
			postponed.addAll(Arrays.asList(assetIds));
		}
	}

	private void init() {
		setPriority(Job.DECORATE);
		setRule(indexingRule);
		dbManager = CoreActivator.getDefault().getDbManager();
		noIndex = dbManager.getMeta(true).getNoIndex();
		try {
			threadMXBean = ManagementFactory.getThreadMXBean();
			if (!threadMXBean.isCurrentThreadCpuTimeSupported())
				threadMXBean = null;
		} catch (Exception e) {
			// do nothing
		}
	}

	/**
	 * Constructor
	 *
	 * This constructor is used only for the special purpose of restoring a
	 * crashed Lucene index from a previous backup
	 *
	 * @param indexBackup
	 *            - location of old Lucene index folder (backup)
	 * @param lastBackup
	 *            - Date of last backup
	 */
	public IndexingJob(File indexBackup, Date lastBackup) {
		super(Messages.getString("IndexingJob.Indexing")); //$NON-NLS-1$
		this.indexBackup = indexBackup;
		this.lastBackup = lastBackup;
		init();
	}

	@Override
	public boolean belongsTo(Object family) {
		return Constants.INDEXING == family;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		status = new MultiStatus(LireActivator.PLUGIN_ID, 0,
				Messages.getString("IndexingJob.Indexing_report"), //$NON-NLS-1$
				null);
		startTime = System.currentTimeMillis();
		if (reset && !dbManager.resetLuceneIndex())
			addErrorStatus(
					Messages.getString("IndexingJob.cannot_delete_folders"), null); //$NON-NLS-1$
		if (noIndex)
			return status;
		File indexPath = dbManager.getIndexPath();
		if (indexPath == null)
			return status;
		if (indexBackup != null && indexPath.exists()) {
			BatchUtilities.deleteFileOrFolder(indexPath);
			try {
				BatchUtilities.copyFolder(indexBackup, indexPath, monitor);
				extractAssedIds(dbManager.obtainObjects(AssetImpl.class,
						QueryField.LASTMOD.getKey(), lastBackup,
						QueryField.GREATER));
			} catch (IOException e) {
				return new Status(
						IStatus.ERROR,
						LireActivator.PLUGIN_ID,
						Messages.getString("IndexingJob.ioerror_restoring_folder"), e); //$NON-NLS-1$
			} catch (DiskFullException e) {
				return new Status(IStatus.ERROR, LireActivator.PLUGIN_ID,
						Messages.getString("IndexingJob.disk_full")); //$NON-NLS-1$
			}
		}
		boolean createDocs = !reimport || !indexPath.exists();
		int size = 0;
		IndexWriter iw = null;
		LireActivator activator = LireActivator.getDefault();
		try {
			iw = activator.getIndexWriter(indexPath);
			DocumentBuilder builder = activator
					.constructFullDocumentBuilder();
			int i = 0;
			List<String> done = new ArrayList<String>(MAXTRANS);
			if (assetIds != null) {
				size = assetIds.length;
				monitor.beginTask(
						Messages.getString("IndexingJob.Indexing"), size * 10); //$NON-NLS-1$
				for (String assetId : assetIds) {
					AssetImpl asset = dbManager.obtainAsset(assetId);
					if (asset != null) {
						if (updateDocument(monitor, createDocs, iw, builder,
								asset, done))
							break;
						if (++i >= MAXTRANS) {
							iw = activator.flushIndexWriter(indexPath);
							i = 0;
							postponed.removeAll(done);
							done.clear();
						}
					}
				}
			} else {
				size = assets.size();
				monitor.beginTask(
						Messages.getString("IndexingJob.Indexing"), totalWork * 10); //$NON-NLS-1$
				if (worked > 0)
					monitor.worked(worked * 10);
				for (Asset asset : assets) {
					if (updateDocument(monitor, createDocs, iw, builder, asset,
							done))
						break;
					if (++i > MAXTRANS) {
						iw = activator.flushIndexWriter(indexPath);
						i = 0;
						postponed.removeAll(done);
						done.clear();
					}
				}
			}
			if (i > 0) {
				iw = activator.flushIndexWriter(indexPath);
				postponed.removeAll(done);
			}
		} catch (IOException e) {
			addErrorStatus(
					Messages.getString("IndexingJob.error_creating_lucene_index"), e);//$NON-NLS-1$
			return status;
		} catch (IllegalStateException e) {
			addErrorStatus(
					Messages.getString("IndexingJob.internal_error_when_indexing"), e); //$NON-NLS-1$
			return status;
		} finally {
			addInfoStatus(NLS.bind(
					Messages.getString("IndexingJob.indexed_elapsed"), //$NON-NLS-1$
					size, (System.currentTimeMillis() - startTime) / 1000.0f));
			if (iw != null)
				try {
					activator.releaseIndexWriter(indexPath, false);
				} catch (IOException e) {
					addErrorStatus(
							Messages.getString("IndexingJob.error_closing_lucene_index"), e);//$NON-NLS-1$
					return status;
				}
			System.gc();
			monitor.done();
		}
		return status;
	}

	private void extractAssedIds(List<AssetImpl> set) {
		List<String> ids = new ArrayList<String>(set.size());
		for (AssetImpl asset : set)
			ids.add(asset.getStringId());
		this.assetIds = ids.toArray(new String[ids.size()]);
		postponed.addAll(ids);
	}

	private boolean updateDocument(IProgressMonitor monitor, boolean create,
			IndexWriter iw, DocumentBuilder builder, Asset asset,
			List<String> done) {
		if (iw != null) {
			long start = System.nanoTime();
			long cpu = threadMXBean != null ? threadMXBean
					.getCurrentThreadCpuTime() : 0L;
			boolean cancel = doUpdateDocument(monitor, create, iw, builder,
					asset, done);
			if (!cancel) {
				long dur = System.nanoTime() - start;
				long delay;
				if (threadMXBean != null) {
					cpu = threadMXBean.getCurrentThreadCpuTime() - cpu;
					long u = cpu / (dur / 100);
					double fac = Math.min(FACTOR, Math.max(0f, (86f - u) / 9f));
					delay = Math.max(MINDELAY,
							Math.min(MAXDELAY, (long) (dur * fac / 1000000)));
				} else
					delay = Math.max(MINDELAY,
							Math.min(MAXDELAY, (dur * FACTOR / 2000000)));
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			return cancel;
		}
		return true;
	}

	private boolean doUpdateDocument(IProgressMonitor monitor, boolean create,
			IndexWriter iw, DocumentBuilder builder, Asset asset,
			List<String> done) {
		String assetId = asset.getStringId();
		if (!create)
			try {
				iw.deleteDocuments(new Term(
						DocumentBuilder.FIELD_NAME_IDENTIFIER, assetId));
			} catch (CorruptIndexException e) {
				// ignore for now
			} catch (IOException e) {
				addErrorStatus(
						Messages.getString("IndexingJob.ioerror_updating_lucene_index"), //$NON-NLS-1$
						e);
				// Abort
				return true;
			}
		monitor.worked(1);
		ByteArrayInputStream in = new ByteArrayInputStream(
				asset.getJpegThumbnail());
		try {
			BufferedImage bi = null;
			for (int i = 0; i < 15; i++) {
				bi = ImageIO.read(in);
				if (bi != null)
					break;
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					break;
				}
			}
			monitor.worked(1);
			if (bi == null) {
				addWarningStatus(NLS.bind(
						Messages.getString("IndexingJob.thumbnail_corrupt"), //$NON-NLS-1$
						asset.getName()));
				return false;
			}
			Document doc = builder.createDocument(bi, assetId);
			monitor.worked(7);
			try {
				iw.addDocument(doc);
				done.add(assetId);
			} catch (CorruptIndexException e) {
				addErrorStatus(NLS.bind(Messages
						.getString("IndexingJob.lucene_index_is_corrupt"), //$NON-NLS-1$
						dbManager.getIndexPath()), e);
				return true;
			} catch (IOException e) {
				addErrorStatus(
						Messages.getString("IndexingJob.ioerror_updating_lucene_index"), //$NON-NLS-1$
						e);
				return true;
			}
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// do nothing
			}
		} catch (IOException e) {
			addErrorStatus(
					NLS.bind(
							Messages.getString("IndexingJob.io_error_when_generating_index_data"), asset //$NON-NLS-1$
									.getName()), e);
		} catch (Exception e) {
			addErrorStatus(
					NLS.bind(Messages.getString("IndexingJob.internal_error"), //$NON-NLS-1$
							asset.getName()), e);
			return true;
		}
		monitor.worked(1);
		return monitor.isCanceled();
	}

	private void addWarningStatus(String message) {
		status.add(new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, message));
	}

	private void addInfoStatus(String message) {
		status.add(new Status(IStatus.INFO, CoreActivator.PLUGIN_ID, message));
	}

	protected void addErrorStatus(String message, Throwable t) {
		status.add(new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, message,
				t));
	}

	public Set<String> getPostponed() {
		return postponed;
	}

}
