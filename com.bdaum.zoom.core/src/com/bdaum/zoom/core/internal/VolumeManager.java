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
 * (c) 2009-2015 Berthold Daum  
 */

package com.bdaum.zoom.core.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.batch.internal.Daemon;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.mtp.DeviceInsertionListener;
import com.bdaum.zoom.mtp.MtpManager;
import com.bdaum.zoom.mtp.StorageObject;
import com.bdaum.zoom.program.BatchUtilities;

@SuppressWarnings("restriction")
public class VolumeManager implements IVolumeManager {

	private class InitJob extends Job {

		public InitJob() {
			super(Messages.VolumeManager_initialize);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			mtpManager.updateVolumes();
			errorMessage = null;
			return Status.OK_STATUS;
		}
	}

	private static final String VOLUME_PATTERNS_INI = "/volumePatterns.ini"; //$NON-NLS-1$
	private static final StorageObject[] NULLSTORAGEOBECTS = new StorageObject[0];

	private File[] roots = new File[0];
	private long[] timeStamps;
	private String[] volumes;
	private ListenerList<DeviceInsertionListener> deviceListeners = new ListenerList<DeviceInsertionListener>();
	private ListenerList<VolumeListener> volumeListeners = new ListenerList<VolumeListener>();
	private List<File> dcims;
	private boolean deviceProcessing;
	private IPeerService peerService;
	private Daemon monitorJob;
	private Pattern[] volumePatterns;
	private MtpManager mtpManager;
	private String errorMessage;
	private boolean peerRequested;

	public VolumeManager() {
		updateVolumes();
		if (Constants.WIN32) {
			errorMessage = Messages.VolumeManager_initialization_failed;
			mtpManager = new MtpManager(VolumeManager.this);
			new InitJob().schedule();
		}
		monitorJob = new Daemon(Messages.VolumeManager_monitor_volumes, 4000L) {
			@Override
			protected void doRun(IProgressMonitor monitor) {
				updateVolumes();
				if (errorMessage == null && mtpManager != null)
					mtpManager.updateVolumes();
			}
		};
		monitorJob.schedule(4000L);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IVolumeManager#findFile(com.bdaum.zoom.cat.model.
	 * asset.Asset)
	 */

	public URI findFile(Asset asset) {
		if (asset != null)
			synchronized (this) {
				return findFile(asset, asset.getUri());
			}
		return null;
	}

	private URI findFile(Asset asset, String uri) {
		return uri.startsWith(FILE) ? findFile(uri, asset.getVolume()) : null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IVolumeManager#findFile(java.lang.String,
	 * java.lang.String)
	 */

	public URI findFile(String uri, String volume) {
		if (Constants.WIN32) {
			File device = volumeToRoot(volume);
			if (device != null) {
				int q = uri.indexOf(":/", FILE.length()); //$NON-NLS-1$
				if (q > 0)
					uri = device.toURI() + uri.substring(q + 2);
			}
		}
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IVolumeManager#findExistingFile(com.bdaum.zoom.cat
	 * .model.asset.Asset, boolean)
	 */

	public URI findExistingFile(Asset asset, boolean local) {
		if (asset != null)
			synchronized (this) {
				return findExistingFile(asset, asset.getUri(), local);
			}
		return null;
	}

	private URI findExistingFile(Asset asset, String uri, boolean local) {
		if (uri.startsWith(FILE)) {
			File file = findExistingFile(uri, asset.getVolume());
			return file == null ? null : file.toURI();
		}
		if (!local)
			try {
				URI u = new URI(uri);
				u.toURL().openConnection();
				return u;
			} catch (Exception e) {
				// do nothing
			}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IVolumeManager#findExistingFile(java.lang.String,
	 * java.lang.String)
	 */

	public File findExistingFile(String uri, String volume) {
		if (volume != null && !volume.isEmpty() && Constants.WIN32) {
			File device = volumeToRoot(volume);
			if (device != null) {
				int q = uri.indexOf(":/", FILE.length()); //$NON-NLS-1$
				if (q > 0)
					uri = device.toURI() + uri.substring(q + 2);
			}
		}
		try {
			File file = new File(new URI(uri));
			File root = getRootFile(file);
			if (Constants.WIN32 && root.getPath().equals("\\\\") && file.exists()) //$NON-NLS-1$
				return file;
			for (File rootFile : roots)
				if (rootFile.equals(root) && file.exists())
					return file;
		} catch (URISyntaxException e) {
			// do nothing
		} catch (IllegalArgumentException e) {
			// do nothing
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IVolumeManager#getRootFile(java.io.File)
	 */
	public File getRootFile(File file) {
		File root = file;
		while (true) {
			File parent = root.getParentFile();
			if (parent == null || parent == root)
				break;
			root = parent;
		}
		return root;
	}

	private void updateVolumes() {
		synchronized (this) {
			File[] list = File.listRoots();
			if (list == null)
				return;
			int length = list.length;
			if (length < roots.length) {
				roots = list;
				dcims = null;
				fireDeviceEjected();
				return;
			}
			boolean inserted = false;
			if (length > roots.length) {
				inserted = true;
				roots = list;
				timeStamps = new long[length];
				volumes = new String[length];
				for (int i = 0; i < length; i++) {
					volumes[i] = obtainVolumeLabel(roots[i]);
					timeStamps[i] = roots[i].lastModified();
				}
			} else
				for (int i = 0; i < length; i++) {
					long lastModified = list[i].lastModified();
					if (timeStamps[i] != lastModified || !roots[i].equals(list[i])) {
						inserted = true;
						timeStamps[i] = lastModified;
						volumes[i] = obtainVolumeLabel(roots[i] = list[i]);
					}
				}
			if (inserted)
				for (VolumeListener listener : volumeListeners)
					listener.volumesChanged(roots);
		}
		if (!deviceProcessing && !deviceListeners.isEmpty())
			try {
				deviceProcessing = true;
				List<File> newDcims = BatchUtilities.findDCIMs();
				for (File file : newDcims)
					if (dcims == null || !dcims.contains(file)) {
						dcims = newDcims;
						fireDeviceInserted();
						break;
					}
				dcims = newDcims;
			} finally {
				deviceProcessing = false;
			}
	}

	private void fireDeviceInserted() {
		for (DeviceInsertionListener listener : deviceListeners)
			listener.deviceInserted();
	}

	private void fireDeviceEjected() {
		for (DeviceInsertionListener listener : deviceListeners)
			listener.deviceEjected();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IVolumeManager#getBaseVolumeForFile(java.io.File)
	 */
	public String getVolumeForFile(File file) {
		IPath path = new Path(file.getAbsolutePath());
		String device = path.getDevice();
		if (device != null) {
			String volumeLabel = obtainVolumeLabel(device + "/"); //$NON-NLS-1$
			return (volumeLabel == null) ? device : volumeLabel;
		}
		return obtainVolumeLabel(path.toString());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IVolumeManager#rootToVolume(java.io.File)
	 */

	public String rootToVolume(File root) {
		if (root != null)
			for (int i = 0; i < roots.length; i++)
				if (root.equals(roots[i]))
					return volumes[i];
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IVolumeManager#volumeToRoot(java.lang.String)
	 */

	public File volumeToRoot(String volume) {
		if (volume != null && volumes != null)
			for (int i = 0; i < volumes.length; i++)
				if (volume.equals(volumes[i]))
					return roots[i];
		return null;
	}

	public void dispose() {
		if (monitorJob != null)
			Job.getJobManager().cancel(monitorJob);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IVolumeManager#findVoiceFile(com.bdaum.zoom.cat.model
	 * .asset.Asset)
	 */

	public URI findVoiceFile(Asset asset) {
		if (asset != null) {
			synchronized (this) {
				String uri = asset.getVoiceFileURI();
				if (uri != null) {
					int p = uri.indexOf('\f');
					if (p >= 0)
						uri = uri.substring(0, p);
					else if (uri.startsWith("?")) //$NON-NLS-1$
						uri = null;
					if (uri != null && !uri.isEmpty()) {
						if (".".equals(uri)) { //$NON-NLS-1$
							try {
								uri = Core.getVoicefileURI(new File(new URI(asset.getUri()))).toString();
							} catch (URISyntaxException e) {
								return null;
							}
							return findExistingFile(asset, uri, true);
						}
						if (uri.startsWith(FILE)) {
							File file = findExistingFile(uri, asset.getVoiceVolume());
							return file == null ? null : file.toURI();
						}
					}
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IVolumeManager#isOffline(java.lang.String)
	 */

	public boolean isOffline(String volume) {
		if (volume == null)
			return false;
		for (String v : volumes)
			if (volume.equals(v))
				return false;
		return true;
	}

	public boolean isRemote(Asset asset) {
		if (asset == null)
			return false;
		if (!asset.getUri().startsWith(FILE))
			return true;
		IPeerService peerService = getPeerService();
		return peerService != null && peerService.isOwnedByPeer(asset.getStringId());
	}

	public String obtainVolumeLabel(String path) {
		return obtainVolumeLabel(getRootFile(new File(path)));
	}

	private String obtainVolumeLabel(File dir) {
		if (Constants.WIN32)
			try {
				String name = Files.getFileStore(dir.toPath()).name();
				if (name != null) {
					name = name.trim();
					if (!name.isEmpty()) {
						int index = name.lastIndexOf(" ("); //$NON-NLS-1$
						if (index > 0)
							return name.substring(0, index).trim();
					}
				}
				return name;
			} catch (IOException e) {
				return null;
			}
		return extractLinuxPath(dir.getAbsolutePath());
	}

	public String extractLinuxPath(String path) {
		if (volumePatterns == null) {
			URL iniUri = FileLocator.find(ImageActivator.getDefault().getBundle(), new Path(VOLUME_PATTERNS_INI), null);
			try {
				iniUri = FileLocator.toFileURL(iniUri);
				try (BufferedReader r = new BufferedReader(new FileReader(new File(iniUri.getPath())))) {
					String line;
					List<Pattern> patterns = new ArrayList<Pattern>(3);
					while ((line = r.readLine()) != null)
						if (!line.startsWith("#")) //$NON-NLS-1$
							try {
								patterns.add(Pattern.compile(line.trim()));
							} catch (PatternSyntaxException e) {
								CoreActivator.getDefault().logError(
										NLS.bind(Messages.VolumeManager_error_compiling_pattern, line, iniUri), e);
							}
					volumePatterns = patterns.toArray(new Pattern[patterns.size()]);
				} catch (Exception e) {
					volumePatterns = new Pattern[0];
				}
			} catch (IOException e) {
				volumePatterns = new Pattern[0];
			}
		}
		for (Pattern pattern : volumePatterns) {
			Matcher matcher = pattern.matcher(path);
			if (matcher.matches()) {
				StringBuffer sb = new StringBuffer();
				matcher.appendReplacement(sb, "$1"); //$NON-NLS-1$
				return sb.toString();
			}
		}
		int p = path.indexOf('/', 1);
		return (p >= 0) ? path.substring(0, p) : path;
	}

	public Icon getFileIcon(String path) {
		File f = new File(path);
		return f.exists() ? FileSystemView.getFileSystemView().getSystemIcon(f) : null;
	}

	public void addDeviceInsertionListener(DeviceInsertionListener listener) {
		dcims = BatchUtilities.findDCIMs();
		deviceListeners.add(listener);
		if (mtpManager != null)
			mtpManager.addDeviceInsertionListener(listener);
	}

	public void removeDeviceInsertionListener(DeviceInsertionListener listener) {
		deviceListeners.remove(listener);
		if (mtpManager != null)
			mtpManager.removeDeviceInsertionListener(listener);
	}

	public void addVolumeListener(VolumeListener listener) {
		volumeListeners.add(listener);
	}

	public void removeVolumeListener(VolumeListener listener) {
		volumeListeners.remove(listener);
	}

	public int determineFileState(Asset asset) {
		if (asset != null) {
			if (!asset.getUri().startsWith(FILE))
				return REMOTE;
			IPeerService peerService = getPeerService();
			if (peerService != null && peerService.isOwnedByPeer(asset.getStringId()))
				return PEER;
			return findExistingFile(asset, true) != null ? ONLINE : OFFLINE;
		}
		return OFFLINE;
	}

	private IPeerService getPeerService() {
		if (!peerRequested) {
			peerService = Core.getCore().getPeerService();
			peerRequested = true;
		}
		return peerService;
	}

	@Override
	public StorageObject[] findDCIMs() {
		return mtpManager != null ? mtpManager.findDCIMs() : NULLSTORAGEOBECTS;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

}
