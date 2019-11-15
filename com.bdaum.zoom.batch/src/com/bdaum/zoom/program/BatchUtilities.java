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
 * (c) 2009-2018 Berthold Daum  
 */
package com.bdaum.zoom.program;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceFilter;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.osgi.service.prefs.BackingStoreException;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.batch.internal.StreamCapture;
import com.bdaum.zoom.common.CommonUtilities;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageUtilities;

public class BatchUtilities {

	/**
	 * Encodes the blanks of a URL
	 *
	 * @param s
	 *            - URL or URL part/
	 * @return the encoded string
	 */
	public static String encodeBlanks(String s) {
		return CommonUtilities.encodeBlanks(s);
	}

	/**
	 * Replaces problematic characters with underscore
	 *
	 * @param s
	 *            input string
	 * @return result string
	 */
	public static String toValidFilename(String s) {
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (!Character.isLetterOrDigit(c) && c != ' ' && c != '.' && c != '_' && c != '-')
				chars[i] = '_';
		}
		return new String(chars);
	}

	/**
	 * Extract the file extension from a path specification
	 *
	 * @param path
	 *            - path (URI syntax only)
	 * @return file extension
	 */
	public static String getTrueFileExtension(String path) {
		int p = path.lastIndexOf('.');
		return (p > path.lastIndexOf('/')) ? path.substring(p + 1) : ""; //$NON-NLS-1$
	}

	/**
	 * Returns the filename from a path with or without extension
	 * 
	 * @param path
	 * @param withExtension
	 * @return
	 */
	public static String getFileName(String path, boolean withExtension) {
		int p = path.lastIndexOf('/') + 1;
		if (!withExtension) {
			int q = path.lastIndexOf('.');
			if (q > p)
				return path.substring(p, q);
		}
		return path.substring(p);
	}

	/**
	 * Determines the last relevant modification of an image
	 *
	 * @param file
	 *            - input file
	 * @param recipeProvider
	 *            - A raw recipe provider or null
	 * @return - timestamp
	 */
	public static long getImageFileModificationTimestamp(File file) {
		long ts = file.lastModified();
		String uri = file.toURI().toString();
		String extension = getTrueFileExtension(uri);
		String xmpUri = uri;
		if (extension != null)
			xmpUri = uri.substring(0, uri.length() - (extension.length() + 1));
		xmpUri += ".xmp"; //$NON-NLS-1$
		try {
			ts = Math.max(ts, new File(new URI(xmpUri)).lastModified());
		} catch (URISyntaxException e) {
			// should never happen
		}

		if (ImageConstants.isRaw(uri, true)) {
			IRawConverter currentRawConverter = BatchActivator.getDefault().getCurrentRawConverter(false);
			if (currentRawConverter != null)
				return currentRawConverter.getLastRecipeModification(uri, ts, null);
		}
		return ts;
	}

	/**
	 * Moves a file from one location to another. Works across file systems
	 *
	 * @param source
	 *            - source file
	 * @param target
	 *            - target file
	 * @param monitor
	 *            - progress monitor or null
	 * @throws IOException
	 * @throws DiskFullException
	 */
	public static void moveFile(File source, File target, IProgressMonitor monitor)
			throws IOException, DiskFullException {
		target.delete();
		if (source != null) {
			if (source.renameTo(target))
				return;
			copyFile(source, target, monitor);
			if (target.exists())
				source.delete();
		}
	}

	/**
	 * Copies a folder with its contents
	 *
	 * @param in
	 *            - input folder
	 * @param out
	 *            - output destination
	 * @param monitor
	 *            - progress monitor or null
	 * @throws IOException
	 * @throws DiskFullException
	 */
	public synchronized static void copyFolder(File in, File out, IProgressMonitor monitor)
			throws IOException, DiskFullException {
		copyFolder(in, out, true, monitor);
	}

	/**
	 * Copies a folder with its contents
	 *
	 * @param in
	 *            - input folder
	 * @param out
	 *            - output destination
	 * @param overwrite
	 *            - true if destination is to be overwritten
	 * @param monitor
	 *            - progress monitor or null
	 * @throws IOException
	 * @throws DiskFullException
	 */
	public synchronized static void copyFolder(File in, File out, boolean overwrite, IProgressMonitor monitor)
			throws IOException, DiskFullException {
		if (in.isFile())
			copyFile(in, out, overwrite, monitor);
		else {
			out.mkdirs();
			File[] files = in.listFiles();
			if (files == null)
				throw new IOException(NLS.bind(Messages.getString("BatchUtilities.directory_cannot_be_listed"), in)); //$NON-NLS-1$
			for (File inFile : files)
				copyFolder(inFile, new File(out, inFile.getName()), overwrite, monitor);
		}
	}

	/**
	 * Deletes a file or folder with its contents
	 *
	 * @param in
	 *            - input file or folder
	 * @return true if file or folder was deleted
	 */
	public synchronized static boolean deleteFileOrFolder(File in) {
		if (in.isDirectory() && !deleteFolderContent(in))
			return false;
		return in.delete();
	}

	/**
	 * Deletes the content of the specified folder
	 *
	 * @param in
	 *            - input folder
	 * @return true if all content was deleted
	 */
	public synchronized static boolean deleteFolderContent(File in) {
		File[] files = in.listFiles();
		if (files == null)
			return false;
		for (File inFile : files)
			if (!deleteFileOrFolder(inFile))
				return false;
		return true;
	}

	private static final long MAXTRANSFERSIZE = (64 * 1024 - 32) * 1024L;

	/**
	 * Copies a file
	 *
	 * @param in
	 *            - input file
	 * @param out
	 *            - output destination
	 * @param monitor
	 *            - progress monitor or null
	 * @throws IOException
	 * @throws DiskFullException
	 */
	public synchronized static void copyFile(File in, File out, IProgressMonitor monitor)
			throws IOException, DiskFullException {
		copyFile(in, out, true, monitor);
	}

	/**
	 * Copies a file
	 *
	 * @param in
	 *            - input file
	 * @param out
	 *            - output destination
	 * @param overwrite
	 *            - true if destination is to be overwritten
	 * @param monitor
	 *            - progress monitor or null
	 * @throws IOException
	 * @throws DiskFullException
	 */
	public static void copyFile(File in, File out, boolean overwrite, IProgressMonitor monitor)
			throws IOException, DiskFullException {
		if (in.equals(out) || !overwrite && out.exists())
			return;
		ImageUtilities.waitUntilFileIsReady(in);
		long size = in.length();
		synchronized (out) {
			try (FileInputStream instream = new FileInputStream(in);
					FileOutputStream outstream = new FileOutputStream(out);) {
				try (FileChannel sourceChannel = instream.getChannel();
						FileChannel destinationChannel = outstream.getChannel()) {
					if (size < MAXTRANSFERSIZE)
						sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
					else {
						long position = 0;
						while (position < size && (monitor == null || !monitor.isCanceled()))
							position += sourceChannel.transferTo(position, MAXTRANSFERSIZE, destinationChannel);
					}
				}
			} finally {
				if (size > 0 && out.exists()) {
					long outsize = out.length();
					if (outsize < size) {
						if (!in.exists())
							throw new FileNotFoundException(
									NLS.bind(Messages.getString("BatchUtilities.Fike_not_found_while_copying"), //$NON-NLS-1$
											in.getPath()));
						copyBytes(in, out, monitor);
					}
				}
			}
		}
	}

	private static void copyBytes(File in, File out, IProgressMonitor monitor) throws IOException, DiskFullException {
		FileLock fl = null;
		try (FileInputStream fin = new FileInputStream(in); FileOutputStream fout = new FileOutputStream(out)) {
			try {
				fl = fin.getChannel().tryLock();
				if (fl == null)
					throw new IOException(NLS.bind(Messages.getString("BatchUtilities.File_in_use"), in.getPath())); //$NON-NLS-1$
			} catch (Exception e) {
				// work without lock
			}
			try (BufferedInputStream bin = new BufferedInputStream(fin);
					BufferedOutputStream bout = new BufferedOutputStream(fout)) {
				try {
					transferStreams(bin, bout, monitor);
				} finally {
					if (fl != null)
						fl.release();
					long size = in.length();
					long outsize = out.length();
					if (outsize < size)
						throw new DiskFullException(NLS.bind(Messages.getString("BatchUtilities.File_n_m"), //$NON-NLS-1$
								out, outsize + " < " + size)); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Transfers bytes from an input stream to an output stream
	 *
	 * @param source
	 *            - input stream
	 * @param destination
	 *            - output stream
	 * @param monitor
	 *            -progress monitor or null
	 * @return number of transmitted bytes
	 * @throws IOException
	 */
	public static final int transferStreams(InputStream source, OutputStream destination, IProgressMonitor monitor)
			throws IOException {
		byte[] buffer = new byte[8192];
		int r = 0;
		while (true) {
			int bytesRead = source.read(buffer);
			if (bytesRead == -1)
				break;
			destination.write(buffer, 0, bytesRead);
			r += bytesRead;
			if (monitor != null && monitor.isCanceled())
				return -1;
		}
		return r;
	}

	private static final String MEDIA = "/media/"; //$NON-NLS-1$
	private static final String VOLUMES = "/Volumes/"; //$NON-NLS-1$

	/**
	 * Ejects removable media
	 *
	 * @param file
	 *            - file stored on the media to be removed
	 * @throws IOException
	 */
	public static void ejectMedia(String file) throws IOException {
		String[] parms = null;
		if (BatchConstants.WIN32) {
			String ejectMedia = BatchActivator.getDefault().locate("/EjectMedia.exe"); //$NON-NLS-1$
			if (ejectMedia != null)
				parms = new String[] { ejectMedia, new Path(file).getDevice(), "-w:300" }; //$NON-NLS-1$
		} else if (BatchConstants.OSX) {
			if (file.startsWith(VOLUMES)) {
				int p = file.indexOf('/', VOLUMES.length());
				parms = new String[] { "diskutil", "unmount", "force", p >= 0 ? file.substring(0, p) : file }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} else if (file.startsWith(MEDIA)) {
			int p = file.indexOf('/', MEDIA.length());
			parms = new String[] { "pumount", p >= 0 ? file.substring(0, p) : file }; //$NON-NLS-1$
		}
		if (parms != null)
			Runtime.getRuntime().exec(parms);
	}

	/**
	 * Sbows the folder where the specified file is located Both the folder in the
	 * catalog and the host file system are shown If possible the file is selected
	 * in the host folder
	 *
	 * @param file
	 *            - the file to be shown
	 * @param select
	 *            - true if specifed file is to be selected, false if file is to be
	 *            opened
	 */
	public static void showInFolder(final File file, boolean select) {
		BusyIndicator.showWhile(null, () -> {
			try {
				if (BatchConstants.WIN32)
					BatchActivator.getDefault().runScript(new String[] { select ? "/select.bat" : "root.bat", //$NON-NLS-1$ //$NON-NLS-2$
							file.getAbsolutePath() }, false);
				else
					BatchUtilities.executeCommand(new String[] { BatchConstants.OSX ? "open" : "xdg-open", //$NON-NLS-1$ //$NON-NLS-2$
							select ? file.getParentFile().getAbsolutePath() : file.getAbsolutePath() }, null,
							Messages.getString("BatchUtilities.run_script"), IStatus.OK, IStatus.ERROR, 0, 1000L, //$NON-NLS-1$
							"UTF-8"); //$NON-NLS-1$
			} catch (IOException e1) {
				BatchActivator.getDefault().logError(Messages.getString("BatchUtilities.io_error_showFile"), //$NON-NLS-1$
						e1);
			} catch (ExecutionException e) {
				BatchActivator.getDefault().logError(Messages.getString("BatchUtilities.script_execution_failed"), //$NON-NLS-1$
						e);
			}
		});

	}

	/**
	 * Finds DCIM folders in the mounted volumes
	 *
	 * @return DCIM folders
	 */
	public static List<File> findDCIMs() {
		List<File> dcims = new ArrayList<File>(3);
		if (BatchConstants.OSX) {
			File volumes = new File("/Volumes"); //$NON-NLS-1$
			File[] files = volumes.listFiles();
			if (files != null)
				for (File volume : files) {
					File file = new File(volume, "DCIM"); //$NON-NLS-1$
					if (file.isDirectory())
						dcims.add(file);
				}
		} else {
			File[] roots = File.listRoots();
			for (File root : roots) {
				File file = new File(root, "DCIM"); //$NON-NLS-1$
				if (file.isDirectory())
					dcims.add(file);
			}
			if (BatchConstants.LINUX) {
				File media = new File("/media"); //$NON-NLS-1$
				File[] files = media.listFiles();
				if (files != null) {
					roots = files;
					for (File root : roots) {
						File file = new File(root, "DCIM"); //$NON-NLS-1$
						if (file.isDirectory())
							dcims.add(file);
					}
				}
			}
		}
		return dcims;
	}

	/**
	 * Execute an operating system command
	 *
	 * @param parms
	 *            - command parameters
	 * @param workingDir
	 *            - working directory or null
	 * @param label
	 *            - job label
	 * @param logLevel
	 *            - status level (as defined in IStatus) for output data. IStatus.OK
	 *            turns logging off
	 * @param errorLevel
	 *            - status level (as defined in IStatus) for error data. IStatus.OK
	 *            turns logging off
	 * @param errorHandling
	 *            determines when an error message is generated if no error data is
	 *            supplied by the error stream: > 0 returned condition code must
	 *            match this number; -1 returned condition code must be unequal 0; 0
	 *            no error message
	 * @param timeout
	 *            - timeout value in msec for waiting on output. 0 waits forever.
	 * @param charsetName
	 *            - character set used such as "UTF-8"
	 * @return standard output data
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static String executeCommand(String[] parms, File workingDir, String label, int logLevel, int errorLevel,
			int errorHandling, long timeout, String charsetName) throws IOException, ExecutionException {
		StreamCapture inputGrabber = null;
		StreamCapture errorGrabber = null;
		try {
			Process process = Runtime.getRuntime().exec(parms, null, workingDir);
			inputGrabber = new StreamCapture(process.getInputStream(), charsetName, label,
					Messages.getString("BatchUtilities.output"), logLevel); //$NON-NLS-1$
			errorGrabber = new StreamCapture(process.getErrorStream(), charsetName, label,
					Messages.getString("BatchUtilities.errors"), errorLevel); //$NON-NLS-1$
			errorGrabber.start();
			inputGrabber.start();
			try {
				int ret = process.waitFor();
				if (ret == 0) {
					inputGrabber.join(timeout);
					return inputGrabber.getData();
				}
				String errorData = errorGrabber.getData().trim();
				if (errorData.isEmpty() && (ret != 0 && errorHandling < 0 || errorHandling == ret && errorHandling > 0))
					errorData = Arrays.toString(parms);
				throw new ExecutionException(
						NLS.bind(Messages.getString("BatchUtilities.command_execution_failed"), ret, errorData), //$NON-NLS-1$
						null);
			} catch (InterruptedException e1) {
				String errorData = errorGrabber.getData().trim();
				throw new ExecutionException(NLS.bind(Messages.getString("BatchUtilities.time_limit_exceeded"), //$NON-NLS-1$
						new Object[] { label, errorData, timeout / 1000 }), e1);
			}
		} finally {
			if (inputGrabber != null)
				inputGrabber.abort();
			if (errorGrabber != null)
				errorGrabber.abort();
		}
	}

	/**
	 * Converts string to valid XML text
	 *
	 * @param s
	 *            - text to encode
	 * @param ascii
	 *            -1 if non-Ascii characters are to be converted into '?', 1 if
	 *            non-Ascii characters are to be converted to XML entities 0 for
	 *            Unicode
	 *
	 * @return xml
	 */
	public static String encodeXML(String s, int ascii) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '"')
				out.append("&quot;"); //$NON-NLS-1$
			else if (c == '\'')
				out.append("&apos;"); //$NON-NLS-1$
			else if (c == '&')
				out.append("&amp;"); //$NON-NLS-1$
			else if (c == '<')
				out.append("&lt;"); //$NON-NLS-1$
			else if (c == '>')
				out.append("&gt;"); //$NON-NLS-1$
			else if (ascii != 0 && c > 127) {
				if (ascii > 0)
					out.append("&#").append((int) c).append(';'); //$NON-NLS-1$
				else
					out.append('?');
			} else
				out.append(c);
		}
		return out.toString();
	}

	public static int parseInt(String s) throws NumberFormatException {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return (int) (0.5d + Double.parseDouble(s));
		}
	}

	public static int readInt(byte[] b, int i) {
		return (b[i] & 0xff) * 256 + (b[i + 1] & 0xff);
	}

	public static void yield() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	public static File makeUniqueFile(File subFolder, String filename, String ext) {
		filename = toValidFilename(filename);
		int tara = subFolder.getAbsolutePath().length() + 1 + filename.length() + ext.length();
		File target;
		int i = 0;
		while (true) {
			String uniqueFileName = filename;
			if (i > 0) {
				String suffix = "-" + i; //$NON-NLS-1$
				int diff = tara + suffix.length() - BatchConstants.MAXPATHLENGTH;
				if (diff > 0)
					uniqueFileName = uniqueFileName.substring(0, uniqueFileName.length() - diff) + suffix;
				else
					uniqueFileName += suffix;
			}
			++i;
			target = new File(subFolder, uniqueFileName + ext);
			if (!target.exists())
				break;
		}
		return target;
	}

	private static final String FORBIDDENCHARS = "/\\<>:%?*|\"."; //$NON-NLS-1$

	/**
	 * Check filename for forbidden characters
	 * 
	 * @param s
	 *            - string to check
	 * @return - invalid character or 0
	 */
	public static char checkFilename(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (FORBIDDENCHARS.indexOf(c) >= 0)
				return c;
		}
		return 0;
	}

	public static void putPreferences(String key, String value) {
		putPreferences(InstanceScope.INSTANCE.getNode(BatchActivator.PLUGIN_ID), key, value);
	}

	public static void putPreferences(IEclipsePreferences node, String key, String value) {
		node.put(key, value);
		try {
			node.flush();
		} catch (BackingStoreException e1) {
			// should never happen
		}
	}

	public static void exportPreferences(File path) {
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(path))) {
			IPreferencesService preferencesService = Platform.getPreferencesService();
			preferencesService.exportPreferences(preferencesService.getRootNode(),
					new IPreferenceFilter[] { new IPreferenceFilter() {
						public String[] getScopes() {
							return new String[] { InstanceScope.SCOPE };
						}

						@SuppressWarnings({ "rawtypes", "unchecked" })
						public Map getMapping(String scope) {
							return null;
						}
					} }, out);
		} catch (CoreException e) {
			BatchActivator.getDefault().logError(Messages.getString("BatchUtilities.internal_error"), e); //$NON-NLS-1$
		} catch (IOException e1) {
			// do nothing
		}
	}

}
