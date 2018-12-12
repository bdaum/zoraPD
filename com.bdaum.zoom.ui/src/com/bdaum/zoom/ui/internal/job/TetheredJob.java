package com.bdaum.zoom.ui.internal.job;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.mtp.ObjectFilter;
import com.bdaum.zoom.mtp.StorageObject;
import com.bdaum.zoom.program.DiskFullException;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class TetheredJob extends Job {

	private WatchedFolder folder;
	private List<StorageObject> files = new ArrayList<StorageObject>(100);
	private List<StorageObject> todo = new ArrayList<StorageObject>();
	private Set<String> oldFiles = new HashSet<>();
	private File targetFolder;
	private ObjectFilter filter = CoreActivator.getDefault().getFilenameExtensionFilter();
	private int count = 0;
	private StorageObject[] dcims;

	public TetheredJob(StorageObject[] dcims, WatchedFolder folder) {
		super(Messages.TetheredJob_init);
		this.dcims = dcims;
		this.folder = folder;
		setPriority(Job.SHORT);
	}

	@Override
	public boolean belongsTo(Object family) {
		return Constants.TETHEREDJOB.equals(family) || Constants.DAEMONS.equals(family);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		MultiStatus status = new MultiStatus(UiActivator.PLUGIN_ID, IStatus.OK, Messages.TetheredJob_tethered_report,
				null);
		if (dcims != null && dcims.length > 0) {
			try {
				init(monitor);
			} catch (IOException e) {
				if (!monitor.isCanceled())
					addWarning(status, Messages.TetheredJob_io_error_init, e);
				return status;
			}
			setName(Messages.TetheredJob_tethered);
			while (!monitor.isCanceled()) {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					break;
				}
				files.addAll(todo);
				todo.clear();
				if (collect(monitor, status))
					process(monitor, status);
			}
			files.addAll(todo);
			process(monitor, status);
		}
		return status;
	}

	private static void addWarning(MultiStatus status, String msg, Throwable e) {
		status.add(new Status(IStatus.WARNING, UiActivator.PLUGIN_ID, msg, e));
	}

	private static void addError(MultiStatus status, String msg, Throwable e) {
		status.add(new Status(IStatus.ERROR, UiActivator.PLUGIN_ID, msg, e));
	}

	private void init(IProgressMonitor monitor) throws IOException {
		try {
			targetFolder = new File(new URI(folder.getUri()));
			StorageObject.collectFilteredFiles(dcims, files, filter, true, monitor);
			for (StorageObject object : files)
				oldFiles.add(object.getAbsolutePath());
		} catch (URISyntaxException e) {
			// should never happen
		} finally {
			files.clear();
		}
	}

	protected boolean collect(IProgressMonitor monitor, MultiStatus status) {
		try {
			StorageObject.collectFilteredFiles(dcims, files, filter, true, monitor);
		} catch (Exception e) {
			if (!monitor.isCanceled()) {
				addWarning(status, Messages.TetheredJob_io_error_scanning, e);
				monitor.setCanceled(true);
			}
			return false;
		}
		return true;
	}

	protected void process(IProgressMonitor monitor, MultiStatus status) {
		try {
			for (int i = 0; i < files.size(); i++) {
				if (monitor.isCanceled())
					return;
				StorageObject object = files.get(i);
				if (!oldFiles.contains(object.getAbsolutePath()))
					try {
						if (count > 0)
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// ignore
							}
						if (object.move(targetFolder, monitor))
							setName(NLS.bind(Messages.TetheredJob_tethered_n, ++count));
						else
							todo.add(object);
					} catch (IOException e) {
						if (!monitor.isCanceled())
							addError(status,
									NLS.bind(Messages.TetheredJob_io_error_transferring, object.getAbsolutePath()), e);
					} catch (DiskFullException e) {
						addError(status, Messages.TetheredJob_disk_full, e);
						monitor.setCanceled(true);
						return;
					}
			}
		} finally {
			files.clear();
		}
	}

}
