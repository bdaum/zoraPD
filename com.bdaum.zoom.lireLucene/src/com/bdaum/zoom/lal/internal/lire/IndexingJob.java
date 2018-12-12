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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.lal.internal.lire;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.security.ProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexNotFoundException;
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
import com.bdaum.zoom.job.ProfiledSchedulingRule;
import com.bdaum.zoom.lal.internal.LireActivator;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

import net.semanticmetadata.lire.builders.DocumentBuilder;

/**
 * Job for indexing images in Lucene (similarity, full text search)
 *
 */
@SuppressWarnings("restriction")
public class IndexingJob extends Job {

	private static final ProfiledSchedulingRule indexingRule = new ProfiledSchedulingRule(IndexingJob.class,
			IProfiledOperation.INDEX);
	private static final int MAXTRANS = 50;
	private static final int MAXDELAY = 5000;
	private static final int FACTOR = 4;
	private static final long MINDELAY = 50;
	private Collection<Asset> assets;
	private MultiStatus status;
	private String[] assetIds;
	private boolean reimport;
	private IDbManager dbManager;
	private ThreadMXBean threadMXBean;
	private int totalWork = -1;
	private int worked = 0;
	private boolean reset;
	private File indexBackup;
	private Date lastBackup;
	private boolean noIndex = false;
	private long startTime;
	private Set<String> postponed;

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
	 * @param postponed
	 */
	protected IndexingJob(Collection<Asset> assets, boolean reimport, int totalWork, int worked, boolean system,
			Set<String> postponed) {
		super(Messages.IndexingJob_Indexing);
		this.assets = assets;
		this.reimport = reimport;
		this.postponed = postponed;
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
	 * @param postponedIndexing
	 */
	protected IndexingJob(String[] assetIds, Set<String> postponed) {
		super(Messages.IndexingJob_Indexing);
		this.assetIds = assetIds;
		this.postponed = postponed;
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

	/**
	 * Constructor
	 *
	 * This constructor is used only for the special purpose of restoring a crashed
	 * Lucene index from a previous backup
	 *
	 * @param indexBackup
	 *            - location of old Lucene index folder (backup)
	 * @param lastBackup
	 *            - Date of last backup
	 * @param postponed
	 */
	protected IndexingJob(File indexBackup, Date lastBackup, Set<String> postponed) {
		super(Messages.IndexingJob_Indexing);
		this.indexBackup = indexBackup;
		this.lastBackup = lastBackup;
		this.postponed = postponed;
		init();
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

	@Override
	public boolean belongsTo(Object family) {
		return Constants.INDEXING == family;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		status = new MultiStatus(LireActivator.PLUGIN_ID, 0, Messages.IndexingJob_Indexing_report, null);
		if (reset && !dbManager.resetLuceneIndex())
			addErrorStatus(Messages.IndexingJob_cannot_delete_folders, null);
		if (noIndex)
			return status;
		startTime = System.currentTimeMillis();
		File indexPath = dbManager.getIndexPath();
		if (indexPath == null)
			return status;
		if (indexBackup != null && indexPath.exists()) {
			BatchUtilities.deleteFileOrFolder(indexPath);
			try {
				BatchUtilities.copyFolder(indexBackup, indexPath, monitor);
				extractAssedIds(dbManager.obtainObjects(AssetImpl.class, QueryField.LASTMOD.getKey(), lastBackup,
						QueryField.GREATER));
			} catch (IOException e) {
				return new Status(IStatus.ERROR, LireActivator.PLUGIN_ID, Messages.IndexingJob_ioerror_restoring_folder,
						e);
			} catch (DiskFullException e) {
				return new Status(IStatus.ERROR, LireActivator.PLUGIN_ID, Messages.IndexingJob_disk_full);
			}
		}
		boolean createDocs = !reimport || !indexPath.exists();
		int size = 0;
		IndexWriter iw = null;
		LireActivator activator = LireActivator.getDefault();
		int cnt = 0;
		try {
			iw = activator.getIndexWriter(indexPath);
			DocumentBuilder builder = activator.constructFullDocumentBuilder();
			int i = 0;
			List<String> done = new ArrayList<String>(MAXTRANS);
			if (assetIds != null) {
				size = assetIds.length;
				monitor.beginTask(Messages.IndexingJob_Indexing, size * 10);
				for (String assetId : assetIds) {
					monitor.subTask(NLS.bind(Messages.IndexingJob_n_of_m, cnt + 1, size));
					AssetImpl asset = dbManager.obtainAsset(assetId);
					if (asset != null) {
						if (updateDocument(monitor, createDocs, iw, builder, asset, done))
							break;
						if (++i >= MAXTRANS) {
							iw = activator.flushIndexWriter(indexPath);
							i = 0;
							postponed.removeAll(done);
							done.clear();
						}
					}
					++cnt;
				}
			} else {
				size = assets.size();
				monitor.beginTask(Messages.IndexingJob_Indexing, totalWork * 10);
				if (worked > 0)
					monitor.worked(worked * 10);
				for (Asset asset : assets) {
					monitor.subTask(NLS.bind(Messages.IndexingJob_n_of_m, cnt + 1, size));
					if (updateDocument(monitor, createDocs, iw, builder, asset, done))
						break;
					if (++i > MAXTRANS) {
						iw = activator.flushIndexWriter(indexPath);
						i = 0;
						postponed.removeAll(done);
						done.clear();
					}
					++cnt;
				}
			}
			if (i > 0) {
				iw = activator.flushIndexWriter(indexPath);
				postponed.removeAll(done);
			}
		} catch (CorruptIndexException | IndexNotFoundException e) {
			addErrorStatus(Messages.IndexingJob_index_corrupz, e);
			return status;
		} catch (IOException e) {
			addErrorStatus(Messages.IndexingJob_error_creating_lucene_index, e);
			return status;
		} catch (IllegalStateException e) {
			addErrorStatus(Messages.IndexingJob_internal_error_when_indexing, e);
			return status;
		} finally {
			addInfoStatus(NLS.bind(Messages.IndexingJob_indexed_elapsed,
					new Object[] { cnt, size, (System.currentTimeMillis() - startTime) / 1000.0f }));
			if (iw != null)
				try {
					activator.releaseIndexWriter(indexPath, false);
				} catch (IOException e) {
					addErrorStatus(Messages.IndexingJob_error_closing_lucene_index, e);
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

	private boolean updateDocument(IProgressMonitor monitor, boolean create, IndexWriter iw, DocumentBuilder builder,
			Asset asset, List<String> done) {
		if (iw != null) {
			long start = System.nanoTime();
			long cpu = threadMXBean != null ? threadMXBean.getCurrentThreadCpuTime() : 0L;
			boolean cancel = doUpdateDocument(monitor, create, iw, builder, asset, done);
			if (!cancel) {
				long dur = System.nanoTime() - start;
				long delay;
				if (threadMXBean != null) {
					cpu = threadMXBean.getCurrentThreadCpuTime() - cpu;
					double fac = Math.min(FACTOR, Math.max(0f, (86f - (cpu / (dur / 100))) / 9f));
					delay = Math.max(MINDELAY, Math.min(MAXDELAY, (long) (dur * fac / 1000000)));
				} else
					delay = Math.max(MINDELAY, Math.min(MAXDELAY, (dur * FACTOR / 2000000)));
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

	private boolean doUpdateDocument(IProgressMonitor monitor, boolean create, IndexWriter iw, DocumentBuilder builder,
			Asset asset, List<String> done) {
		String assetId = asset.getStringId();
		if (!create)
			try {
				iw.deleteDocuments(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, assetId));
			} catch (CorruptIndexException e) {
				// ignore for now
			} catch (IOException e) {
				addErrorStatus(Messages.IndexingJob_ioerror_updating_lucene_index, e);
				// Abort
				return true;
			}
		monitor.worked(1);
		try (ByteArrayInputStream in = new ByteArrayInputStream(asset.getJpegThumbnail())) {
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
				addWarningStatus(NLS.bind(Messages.IndexingJob_thumbnail_corrupt, asset.getName()));
				return false;
			}
			Document doc;
			try {
				doc = builder.createDocument(bi, assetId);
			} catch (ProviderException e) {
				addErrorStatus(NLS.bind(Messages.IndexingJob_indexing_stopped, e.getMessage()), e);
				return true;
			}
			monitor.worked(7);
			try {
				iw.addDocument(doc);
				done.add(assetId);
			} catch (CorruptIndexException e) {
				addErrorStatus(NLS.bind(Messages.IndexingJob_lucene_index_is_corrupt, dbManager.getIndexPath()), e);
				return true;
			} catch (IOException e) {
				addErrorStatus(Messages.IndexingJob_ioerror_updating_lucene_index, e);
				return true;
			}
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// do nothing
			}
		} catch (IOException e) {
			addErrorStatus(NLS.bind(Messages.IndexingJob_io_error_when_generating_index_data, asset.getName()), e);
		} catch (Exception e) {
			addErrorStatus(NLS.bind(Messages.IndexingJob_internal_error, asset.getName()), e);
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
		status.add(new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, message, t));
	}

}
