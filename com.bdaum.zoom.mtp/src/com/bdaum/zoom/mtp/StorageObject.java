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
 * (c) 2018-2019 Berthold Daum  
 */
package com.bdaum.zoom.mtp;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.mtp.internal.MtpActivator;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

import jmtp.DeviceAlreadyOpenedException;
import jmtp.PortableDevice;
import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;

@SuppressWarnings("restriction")
public class StorageObject {

	private Object object;
	private long size = 0L;
	private long lastModified = 0L;
	private String path;
	private PortableDevice device;
	private File resolved = null;
	private boolean hidden = false;
	private PortableDeviceObject root, parent;

	public static StorageObject[] fromFile(List<File> files) {
		if (files != null) {
			StorageObject[] objects = new StorageObject[files.size()];
			for (int i = 0; i < objects.length; i++)
				objects[i] = new StorageObject(files.get(i));
			return objects;
		}
		return null;
	}

	public static void collectFilteredFiles(StorageObject[] folders, Collection<StorageObject> result,
			ObjectFilter filter, boolean skipHiddenFiles, IProgressMonitor monitor) throws IOException {
		if (folders != null)
			for (StorageObject f : folders) {
				if (monitor != null && monitor.isCanceled())
					return;
				f.collectFilteredFiles(result, filter, skipHiddenFiles, monitor);
			}
	}

	public StorageObject(File file) {
		this.object = file;
		size = file.length();
		lastModified = file.lastModified();
		hidden = file.isHidden();
	}
	
	public StorageObject(URI uri) {
		this.object = uri;
	}

	public StorageObject(PortableDeviceObject object, PortableDevice device, String parentPath) throws IOException {
		this.object = object;
		this.device = device;
		try {
			BigInteger big = object.getSize();
			if (big != null)
				size = big.longValue();
		} catch (UnsupportedOperationException e) {
			// no size
		}
		StringBuilder sb = new StringBuilder(100);
		if (parentPath != null) {
			addPathSegment(parentPath, object.getOriginalFileName(), sb);
			parent = object.getParent();
		} else {
			PortableDeviceObject po = object;
			while (true) {
				if (po instanceof PortableDeviceStorageObject) {
					root = po;
					String name = po.getName();
					if (!name.endsWith(":")) //$NON-NLS-1$
						sb.insert(0, ':');
					sb.insert(0, name);
					String friendlyName = device.getFriendlyName();
					if (!friendlyName.endsWith("::")) //$NON-NLS-1$
						sb.insert(0, friendlyName.endsWith(":") ? ":" : "::");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					sb.insert(0, friendlyName);
					break;
				}
				if (sb.length() > 0)
					sb.insert(0, '/');
				sb.insert(0, po.getOriginalFileName());
				po = po.getParent();
				if (parent == null)
					parent = po;
			}
		}
		path = sb.toString();
		try {
			Date date = object.getDateModified();
			if (date == null) {
				int p = path.lastIndexOf('/');
				if (p < 0)
					p = path.lastIndexOf(':');
				int q = path.lastIndexOf('.');
				if (q < 0 || q <= p)
					q = path.length();
				if (q > p + 5) {
					if (q > p + 20 && path.charAt(p + 20) == '_')
						q = p + 20;
					else if (q > p + 18 && path.charAt(p + 18) == '_')
						q = p + 18;
					else if (q < p + 18 && q > p + 13 && path.charAt(p + 13) == '_')
						q = p + 13;
					else if (path.charAt(q - 1) == ')') {
						int r = path.lastIndexOf('(', q - 2);
						if (r > p + 5)
							q = r;
					}
					String cand = path.substring(p + 5, q); // According to DCF spec
					String templ = null;
					switch (cand.length()) {
					case 8:
						templ = "yyyyMMdd"; //$NON-NLS-1$
						break;
					case 13:
						templ = "yyyyMMdd_HHmm"; //$NON-NLS-1$
						break;
					case 15:
						templ = "yyyyMMdd_HHmmss"; //$NON-NLS-1$
						break;
					case 18:
						templ = "yyyyMMdd_HHmmssSSS"; //$NON-NLS-1$
						break;
					}
					if (templ != null)
						try {
							date = new SimpleDateFormat(templ).parse(cand);
						} catch (ParseException e) {
							// no date
						}
				}
			}
			if (date != null)
				lastModified = date.getTime();
			hidden = object.isHidden() || getName().startsWith("."); //$NON-NLS-1$
		} catch (UnsupportedOperationException e) {
			// no mod date
		}
	}

	private static void addPathSegment(String parentPath, String name, StringBuilder sb) {
		sb.append(parentPath);
		if (!parentPath.endsWith(":")) //$NON-NLS-1$
			sb.append('/');
		sb.append(name);
	}

	public String getName() {
		if (object instanceof URI) {
			String uri = ((URI) object).toString();
			int p = uri.lastIndexOf('/');
			return p >= 0 ? uri.substring(p + 1) : ""; //$NON-NLS-1$
		}
		if (object instanceof File)
			return ((File) object).getName();
		int p = path.lastIndexOf('/');
		if (p < 0)
			p = path.lastIndexOf(':');
		return p >= 0 ? path.substring(p + 1) : ""; //$NON-NLS-1$
	}

	public String getParentName() {
		if (object instanceof URI) {
			String uri = ((URI) object).toString();
			int p = uri.lastIndexOf('/');
			if (p > 0) {
				int q = uri.lastIndexOf('/', p - 1);
				if (q >= 0)
					return uri.substring(q + 1, p);
			}
			return ""; //$NON-NLS-1$
		}
		if (object instanceof File)
			return ((File) object).getParentFile().getName();
		int q;
		int p = path.lastIndexOf('/');
		if (p < 0) {
			p = path.lastIndexOf(':');
			q = path.lastIndexOf("::", p - 1); //$NON-NLS-1$
		} else {
			q = path.lastIndexOf('/', p - 1);
			if (p < 0)
				q = path.lastIndexOf(':', p - 1);
		}
		return p >= 0 && q >= 0 ? path.substring(q + 1, p) : ""; //$NON-NLS-1$
	}

	public boolean isDirectory() {
		if (object instanceof URI)
			return false;
		if (object instanceof File)
			return ((File) object).isDirectory();
		return object instanceof PortableDeviceFolderObject;
	}

	public boolean isFile() {
		if (object instanceof URI)
			return true;
		if (object instanceof File)
			return ((File) object).isFile();
		return !(object instanceof PortableDeviceFolderObject);
	}

	public StorageObject[] listChildren() throws IOException {
		if (object instanceof File) {
			File[] children = ((File) object).listFiles();
			if (children != null) {
				StorageObject[] objects = new StorageObject[children.length];
				for (int i = 0; i < children.length; i++)
					objects[i] = new StorageObject(children[i]);
				return objects;
			}
		} else if (object instanceof PortableDeviceFolderObject || object instanceof PortableDeviceStorageObject) {
			try {
				deviceOpen();
				PortableDeviceObject[] children = object instanceof PortableDeviceFolderObject
						? ((PortableDeviceFolderObject) object).getChildObjects()
						: ((PortableDeviceStorageObject) object).getChildObjects();
				if (children != null) {
					StorageObject[] objects = new StorageObject[children.length];
					for (int i = 0; i < children.length; i++)
						objects[i] = new StorageObject(children[i], device, path);
					return objects;
				}
			} finally {
				deviceClose();
			}
		}
		return null;
	}

	public void collectFilteredFiles(Collection<StorageObject> result, ObjectFilter filter, boolean skipHiddenFiles,
			IProgressMonitor monitor) throws IOException {
		if (skipHiddenFiles && isHidden())
			return;
		if (object instanceof File) {
			if (isDirectory()) {
				File[] children = ((File) object).listFiles();
				if (children != null)
					for (File child : children) {
						if (monitor != null && monitor.isCanceled())
							return;
						internalCollectFilteredFiles(child, result, filter, skipHiddenFiles, monitor);
					}
			} else if (filter == null || filter.accept(this))
				result.add(this);
		} else if (object instanceof PortableDeviceFolderObject || object instanceof PortableDeviceStorageObject) {
			try {
				deviceOpen();
				PortableDeviceObject[] children = null;
				if (object instanceof PortableDeviceFolderObject)
					children = ((PortableDeviceFolderObject) object).getChildObjects();
				else if (object instanceof PortableDeviceStorageObject)
					children = ((PortableDeviceStorageObject) object).getChildObjects();
				else if (filter == null || filter.accept(this)) {
					result.add(this);
					return;
				}
				if (children != null)
					for (PortableDeviceObject child : children) {
						if (monitor != null && monitor.isCanceled())
							return;
						internalCollectFilteredFiles(child, getAbsolutePath(), result, filter, skipHiddenFiles,
								monitor);
					}
			} finally {
				deviceClose();
			}
		} else if (object instanceof PortableDeviceObject) {
			if (filter == null || filter.accept(this))
				result.add(this);
		}
	}

	public StorageObject findChild(String nameWithoutExtension, String[] extensions) throws IOException {
		if (object instanceof File)
			for (String ext : extensions) {
				File file = new File(((File) object), nameWithoutExtension + ext);
				if (file.exists())
					return new StorageObject(file);
			}
		else if (object instanceof PortableDeviceFolderObject || object instanceof PortableDeviceStorageObject)
			try {
				deviceOpen();
				PortableDeviceObject[] children = null;
				if (object instanceof PortableDeviceFolderObject)
					children = ((PortableDeviceFolderObject) object).getChildObjects();
				else if (object instanceof PortableDeviceStorageObject)
					children = ((PortableDeviceStorageObject) object).getChildObjects();
				if (children != null) {
					int nl = nameWithoutExtension.length();
					for (PortableDeviceObject child : children) {
						String name = child.getName();
						if (name.startsWith(nameWithoutExtension)) {
							int el = name.length() - nl;
							for (String ext : extensions)
								if (ext.length() == el && name.endsWith(ext))
									return new StorageObject(child, device, path);
						}
					}
				}
			} finally {
				deviceClose();
			}
		return null;
	}

	private void internalCollectFilteredFiles(File file, Collection<StorageObject> result, ObjectFilter filter,
			boolean skipHiddenFiles, IProgressMonitor monitor) {
		if (skipHiddenFiles && file.isHidden())
			return;
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null)
				for (File child : children) {
					if (monitor != null && monitor.isCanceled())
						return;
					internalCollectFilteredFiles(child, result, filter, skipHiddenFiles, monitor);
				}
		} else if (filter == null || filter.accept(file))
			result.add(new StorageObject(file));
	}

	public void internalCollectFilteredFiles(PortableDeviceObject o, String parentPath,
			Collection<StorageObject> result, ObjectFilter filter, boolean skipHiddenFiles, IProgressMonitor monitor)
			throws IOException {
		String name = o.getOriginalFileName();
		if (name == null || skipHiddenFiles && (o.isHidden() || name.startsWith("."))) //$NON-NLS-1$
			return;
		if (o instanceof PortableDeviceFolderObject) {
			PortableDeviceObject[] children = ((PortableDeviceFolderObject) o).getChildObjects();
			if (children != null)
				for (PortableDeviceObject child : children) {
					if (monitor != null && monitor.isCanceled())
						return;
					StringBuilder sb = new StringBuilder(100);
					addPathSegment(parentPath, name, sb);
					internalCollectFilteredFiles(child, sb.toString(), result, filter, skipHiddenFiles, monitor);
				}
		} else if (filter == null || filter.accept(name))
			result.add(new StorageObject(o, device, parentPath));
	}

	public URI toURI() {
		if (object instanceof URI)
			return (URI) object;
		if (object instanceof File)
			return ((File) object).toURI();
		try {
			return new URI("mtp:///" + path); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			try {
				return new URI("mtp:///badPath"); //$NON-NLS-1$
			} catch (URISyntaxException e1) {
				return null;
			}
		}
	}

	public String getAbsolutePath() {
		if (object instanceof URI)
			return ((URI) object).getPath();
		if (object instanceof File)
			return ((File) object).getAbsolutePath();
		return path;
	}

	public StorageObject getVoiceAttachment() throws IOException {
		String name = getName();
		StorageObject parent = getParentObject();
		if (parent != null) {
			int p = name.lastIndexOf('.');
			return parent.findChild(p >= 0 ? name.substring(0, p) : name, ImageConstants.VOICEEXT);
		}
		return null;
	}

	public String getVolume() {
		if (object instanceof URI)
			return ""; //$NON-NLS-1$
		if (object instanceof File)
			return MtpActivator.getDefault().getMtpManager().getRootManager().getVolumeForFile((File) object);
		int p = path.lastIndexOf(':');
		return path.substring(0, p);
	}

	public String getMedium() {
		if (object instanceof URI)
			return ""; //$NON-NLS-1$
		if (object instanceof File)
			return MtpActivator.getDefault().getMtpManager().getRootManager().getVolumeForFile((File) object);
		int p = path.indexOf("::"); //$NON-NLS-1$
		return path.substring(0, p);
	}

	public StorageObject getParentObject() throws IOException {
		if (object instanceof URI) {
			String uri = ((URI) object).toString();
			int p = uri.lastIndexOf('/');
			String parentUri = uri.substring(0, p);
			try {
				return new StorageObject(new URI(parentUri));
			} catch (URISyntaxException e) {
				return null;
			}
		}
		if (object instanceof File) {
			File parentFile = ((File) object).getParentFile();
			return parentFile == null ? null : new StorageObject(parentFile);
		}
		int p = path.lastIndexOf('/') - 1;
		if (p >= 0) {
			int q = path.lastIndexOf('/', p);
			if (q >= 0)
				return new StorageObject(parent, device, path.substring(0, q));
			q = path.lastIndexOf(':', p);
			if (q >= 0)
				return new StorageObject(parent, device, path.substring(0, q + 1));
		}
		return new StorageObject(parent, device, null);
	}

	public Object getNativeObject() {
		return object;
	}

	public long lastModified() {
		return lastModified;
	}

	public long size() {
		return size;
	}

	public StorageObject getRootFolder() throws IOException {
		if (object instanceof URI)
			return null;
		if (object instanceof File) {
			File rootFile = MtpActivator.getDefault().getMtpManager().getRootManager().getRootFile((File) object);
			return rootFile == null ? null : new StorageObject(rootFile);
		}
		return new StorageObject(root, device, null);
	}

	public boolean delete() {
		if (object instanceof File)
			return ((File) object).delete();
		if (object instanceof PortableDeviceObject) {
			PortableDeviceObject p = (PortableDeviceObject) object;
			try {
				deviceOpen();
				if (p.canDelete()) {
					p.delete();
					return true;
				}
			} catch (IOException e) {
				// return false
			} finally {
				deviceClose();
			}
		}
		return false;
	}

	public boolean isLocal() {
		return object instanceof File;
	}

	public boolean isMobile() {
		return object instanceof PortableDeviceObject;
	}

	public boolean isRemote() {
		return object instanceof URI;
	}

	public void copy(File targetFile, IProgressMonitor monitor) throws IOException, DiskFullException {
		if (object instanceof File) {
			BatchUtilities.copyFile((File) object, targetFile, monitor);
			if (lastModified > 0)
				targetFile.setLastModified(lastModified);
		} else if (object instanceof PortableDeviceObject) {
			String name = getName();
			String targetFolder = targetFile.getParent();
			try {
				deviceOpen();
				((PortableDeviceObject) object).copy(device, targetFolder);
				File copy = new File(targetFolder, name);
				if (!copy.exists())
					throw new IOException(NLS.bind(Messages.StorageObject_transfer_failed, name, getMedium()));
				if (size != 0 && size > copy.length())
					throw new DiskFullException(
							NLS.bind(Messages.StorageObject_file_disk_full, name, copy.length() + " < " + size)); //$NON-NLS-1$
				if (!copy.renameTo(targetFile))
					throw new IOException(NLS.bind(Messages.StorageObject_renaming_failed, name));
			} finally {
				deviceClose();
			}
		} else
			throw new UnsupportedOperationException();
	}

	public boolean move(File targetFolder, IProgressMonitor monitor) throws IOException, DiskFullException {
		String name = getName();
		File targetFile = new File(targetFolder, name);
		if (object instanceof File) {
			BatchUtilities.moveFile((File) object, targetFile, monitor);
			if (lastModified > 0)
				targetFile.setLastModified(lastModified);
		} else if (object instanceof PortableDeviceObject) {
			try {
				deviceOpen();
				PortableDeviceObject p = (PortableDeviceObject) object;
				p.copy(device, targetFolder.getAbsolutePath());
				if (targetFile.exists()) {
					if (size != 0 && size > targetFile.length())
						throw new DiskFullException(NLS.bind("File {0}: {1}", //$NON-NLS-1$
								targetFile, targetFile.length() + " < " + size)); //$NON-NLS-1$
					if (p.canDelete()) {
						p.delete();
						return true;
					}
				}
			} finally {
				deviceClose();
			}
		} else
			throw new UnsupportedOperationException();
		return false;
	}

	private void deviceClose() {
		try {
			device.close();
		} catch (Exception e) {
			// Never happens but to be sure
		}
	}

	private void deviceOpen() throws IOException {
		try {
			device.open();
		} catch (DeviceAlreadyOpenedException e) {
			// ignore this, close does not seem to work
		}
	}

	public void dispose() {
		if (resolved != null)
			resolved.delete();
	}

	public File resolve() {
		if (object instanceof File)
			return (File) object;
		if (object instanceof PortableDeviceObject) {
			dispose();
			String name = getName();
			int p = name.lastIndexOf('.');
			String suffix = p >= 0 ? name.substring(p) : ".tmp"; //$NON-NLS-1$
			try {
				copy(resolved = ImageActivator.getDefault().createTempFile("mobDev", suffix), null); //$NON-NLS-1$
				return resolved;
			} catch (IOException | DiskFullException e) {
				return null;
			}
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return getAbsolutePath();
	}

	public String getExtension() {
		if (object instanceof URI)
			return BatchUtilities.getTrueFileExtension(object.toString());
		if (object instanceof File) {
			String uri = ((File) object).toURI().toString();
			String fileExtension = BatchUtilities.getTrueFileExtension(uri);
			return fileExtension.isEmpty() ? ImageUtilities.detectImageFormat(uri) : fileExtension.toLowerCase();
		}
		return BatchUtilities.getTrueFileExtension(path);
	}

	public boolean isHidden() {
		return hidden;
	}

	public boolean isStorage() {
		return object instanceof PortableDeviceStorageObject;
	}

}
