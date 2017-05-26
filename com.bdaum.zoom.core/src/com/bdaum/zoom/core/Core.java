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
package com.bdaum.zoom.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.image.IRecipeProvider;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.program.BatchUtilities;

/**
 * This class provides access to core functions. It also provides some basic
 * utility functions
 *
 */
@SuppressWarnings("restriction")
public class Core {

	private static final String XMP = ".xmp"; //$NON-NLS-1$
	private static final ICore core = CoreActivator.getDefault();
	private static final URI[] EMPTYURIS = new URI[0];

	/**
	 * @return singleton instance of the core root class
	 */
	public static ICore getCore() {
		return core;
	}

	/**
	 * Converts a collection into a string list separated by separators
	 *
	 * @param coll
	 *            - collection
	 * @param sep
	 *            - separator
	 * @return - resulting string list
	 */
	public static String toStringList(Collection<?> coll, char sep) {
		if (coll == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (Object object : coll) {
			if (sb.length() > 0)
				sb.append(sep);
			sb.append(object);
		}
		return sb.toString();
	}

	/**
	 * Converts an array of tokens into a string list separated by separators
	 * Null tokens are supressed
	 *
	 * @param tokens
	 *            - tokens
	 * @param sep
	 *            - separator
	 * @return - resulting string list
	 */
	public static String toStringList(Object[] tokens, String sep) {
		if (tokens == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (Object t : tokens) {
			if (t != null) {
				if (sb.length() > 0)
					sb.append(sep);
				sb.append(t);
			}
		}
		return sb.toString();
	}

	/**
	 * Converts a separator separated string list into a list of strings
	 *
	 * @param stringlist
	 *            - input string
	 * @param seps
	 *            - valid separators
	 * @return - resulting list
	 */

	public static List<String> fromStringList(String stringlist, String seps) {
		ArrayList<String> result = new ArrayList<String>();
		if (stringlist != null) {
			char[] chars = stringlist.toCharArray();
			boolean token = false;
			int offset = 0;
			int l = chars.length;
			for (int i = 0; i < l; i++) {
				char c = chars[i];
				if (token) {
					if (seps.indexOf(c) >= 0) {
						int end = i;
						while (end > offset && chars[end - 1] == ' ')
							--end;
						while (offset < end && chars[offset] == ' ')
							++offset;
						result.add(new String(chars, offset, end - offset));
						token = false;
					}
				} else if (seps.indexOf(c) < 0) {
					token = true;
					offset = i;
				}
			}
			if (token) {
				while (l > offset && chars[l - 1] == ' ')
					--l;
				while (offset < l && chars[offset] == ' ')
					++offset;
				result.add(new String(chars, offset, l - offset));
			}

			// StringTokenizer st = new StringTokenizer(stringlist, seps);
			// while (st.hasMoreTokens()) {
			// String token = st.nextToken().trim();
			// if (token.length() > 0)
			// result.add(token);
			// }
		}
		return result;
	}

	/**
	 * This routine calculates the distance between two points (given the
	 * latitude/longitude of those points).
	 *
	 * South latitudes are negative, east longitudes are positive
	 *
	 * Based on algorithm from Hexa Software Development Center. All Rights
	 * Reserved 2004
	 *
	 * @param lat1
	 *            - Latitude of point 1 (in decimal degrees)
	 * @param lon1
	 *            - Longitude of point 1 (in decimal degrees)
	 * @param lat2
	 *            - Latitude of point 2 (in decimal degrees)
	 * @param lon2
	 *            - Longitude of point 2 (in decimal degrees)
	 * @param unit
	 *            - the unit for results: 'M' is statute miles, 'K' is
	 *            kilometers, 'N' is nautical miles
	 * @return distance
	 */
	public static double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist *= 69.09; // 60 * 1.1515
		switch (unit) {
		case 'K':
		case 'k':
			return dist * 1.609344;
		case 'N':
		case 'n':
			return dist * 0.8684;
		default:
			return dist;
		}
	}

	/**
	 * This function converts decimal degrees to radians
	 *
	 * @param deg
	 *            - decimal degrees
	 * @return radians
	 */
	public static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/**
	 * This function converts radians to decimal degrees
	 *
	 * @param rad
	 *            - radians
	 * @return - decimal degrees
	 */
	public static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}

	/**
	 * This routine calculates the initial bearing between two points
	 *
	 * @param lat1
	 *            - Latitude of point 1 (in decimal degrees)
	 * @param lon1
	 *            - Longitude of point 1 (in decimal degrees)
	 * @param lat2
	 *            - Latitude of point 2 (in decimal degrees)
	 * @param lon2
	 *            - Longitude of point 2 (in decimal degrees)
	 * @return bearing in degrees
	 */
	public static double bearing(double lat1, double lon1, double lat2, double lon2) {
		double d = distance(lat1, lon1, lat2, lon2, 'N') / 60d;
		double c = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(d);
		double q = Math.sin(d) * Math.cos(deg2rad(lat1));
		if (q == 0d)
			return 90;
		c = rad2deg(Math.acos(c / q));
		while (c < 0d)
			c += 360d;
		return c;
	}

	/** File utilities **/

	/**
	 * Creates a temporary directory with the given prefix
	 *
	 * @param prefix
	 *            - dir name prefix
	 * @return - directory
	 * @throws IOException
	 */
	public static File createTempDirectory(String prefix) throws IOException {
		File temp = ImageActivator.getDefault().createTempFile(prefix, null);
		if (!(temp.delete()))
			throw new IOException(NLS.bind(Messages.Core_could_not_delete_temp_file, temp.getAbsolutePath()));
		if (!(temp.mkdir()))
			throw new IOException(NLS.bind(Messages.Core_could_not_create_temp_directory, temp.getAbsolutePath()));
		return temp;
	}

	/**
	 * Delete a file or a folder with its contents
	 *
	 * @param file
	 *            - file or directory
	 * @return true on success
	 */
	public static boolean deleteFileOrFolder(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files == null)
				return false;
			for (File member : files)
				if (!deleteFileOrFolder(member))
					return false;
		}
		return file.delete();
	}

	/**
	 * Deletes folder except the specified member
	 *
	 * @param folder
	 *            - folder
	 * @param exclude
	 *            - excluded member
	 * @return true if whole folder was deleted
	 */
	public static boolean deleteFolderExcluding(File folder, String exclude) {
		boolean delete = true;
		if (folder.isDirectory()) {
			File[] files = folder.listFiles();
			if (files == null)
				return false;
			for (File member : files)
				delete &= (exclude == null || !exclude.equals(member.getName())) && deleteFileOrFolder(member);
		}
		return delete && folder.delete();
	}

	/**
	 * Download a file from the specified URI
	 *
	 * @param uri
	 *            - file URI
	 * @param box
	 *            - a ticket box containing a session ticket
	 * @return - the downloaded file (temp file). When file needs to be
	 *         preserved it must be copied to a different location.
	 * @throws IOException
	 */
	public static File download(URI uri, Ticketbox box) throws IOException {
		URL url = uri.toURL();
		String fileExtension = BatchUtilities.getTrueFileExtension(uri.toString()).toLowerCase();
		if (IFTPService.FTPSCHEME.equals(uri.getScheme())) {
			BundleContext bundleContext = CoreActivator.getDefault().getBundle().getBundleContext();
			ServiceReference<?> serviceReference = bundleContext.getServiceReference(IFTPService.class.getName());
			if (serviceReference == null)
				throw new IOException("no FTP service"); //$NON-NLS-1$
			IFTPService service = (IFTPService) bundleContext.getService(serviceReference);
			if (service == null)
				throw new IOException("no FTP service instance"); //$NON-NLS-1$
			Object ticket;
			if (box != null) {
				if (box.ticket == null)
					box.ticket = service.startSession();
				ticket = box.ticket;
			} else
				ticket = new Object();
			InputStream in = service.retrieveFile(ticket, url);
			File file = createTempFile("ImageDownLoad", fileExtension); //$NON-NLS-1$
			copyBytesToFile(in, file, null);
			if (box == null)
				service.endSession(ticket);
			bundleContext.ungetService(serviceReference);
			return file;
		}
		File file = createTempFile("ImageDownLoad", fileExtension); //$NON-NLS-1$
		copyBytesToFile(url.openStream(), file, null);
		return file;
	}

	/**
	 * Copies the content of an input stream to a file
	 *
	 * @param fin
	 *            - input stream
	 * @param out
	 *            - output file
	 * @param monitor
	 *            - progress monitor or null
	 * @return number of copied bytes
	 * @throws IOException
	 */
	public static int copyBytesToFile(InputStream fin, File out, IProgressMonitor monitor) throws IOException {
		try (OutputStream fout = new FileOutputStream(out)) {
			return BatchUtilities.transferStreams(fin, fout, monitor);
		} finally {
			try {
				fin.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	/**
	 * Ends an FTP session
	 *
	 * @param box
	 *            - a ticket box containing a session ticket
	 */
	public static void endSession(Ticketbox box) {
		if (box != null && box.ticket != null) {
			BundleContext bundleContext = CoreActivator.getDefault().getBundle().getBundleContext();
			ServiceReference<?> serviceReference = bundleContext.getServiceReference(IFTPService.class.getName());
			if (serviceReference != null) {
				IFTPService service = (IFTPService) bundleContext.getService(serviceReference);
				if (service != null) {
					service.endSession(box.ticket);
					bundleContext.ungetService(serviceReference);
				}
			}
		}
	}

	/**
	 * Creates a temporary file with the specified prefix and file extension
	 *
	 * @param prefix
	 *            - file name prefix
	 * @param ext
	 *            - file name extension without the period
	 * @return - temp file
	 * @throws IOException
	 */
	public static File createTempFile(String prefix, String ext) throws IOException {
		return ImageActivator.getDefault().createTempFile(prefix, '.' + ext);
	}

	/**
	 * Extract the file extension from a path specification If path does not
	 * contain a file extension, the file header is analyzed for the image type
	 *
	 * @param uri
	 *            - URI
	 * @return file extension
	 */
	public static String getFileExtension(String uri) {
		String fileExtension = BatchUtilities.getTrueFileExtension(uri);
		return (fileExtension.length() > 0) ? fileExtension.toLowerCase() : ImageUtilities.detectImageFormat(uri);
	}

	/**
	 * Extracts the short filename from an uri
	 *
	 * @param uri
	 *            - the given URI
	 * @param withExtension
	 *            - true if filename shall be return including file name
	 *            extension, false otherwise
	 * @return - file name
	 */
	public static String getFileName(URI uri, boolean withExtension) {
		return getFileName(uri.getPath(), withExtension);
	}

	/**
	 * Extracts the short filename from an uri
	 *
	 * @param uri
	 *            - the given URI
	 * @param withExtension
	 *            - true if filename shall be return including file name
	 *            extension, false otherwise
	 * @return - file name
	 */
	public static String getFileName(String uri, boolean withExtension) {
		int p = uri.lastIndexOf('/');
		if (p >= 0)
			uri = uri.substring(p + 1);
		if (!withExtension) {
			p = uri.lastIndexOf('.');
			if (p >= 0)
				uri = uri.substring(0, p);
		}
		return decodeUrl(uri);
	}

	/**
	 * Completes an entered web URL with the HTTP protocol
	 *
	 * @param url
	 *            - entered URL
	 * @return - completed URL
	 */
	public static String furnishWebUrl(String url) {
		if (!url.startsWith("http:") && !url.startsWith("https:")) { //$NON-NLS-1$ //$NON-NLS-2$
			while (url.startsWith("/")) //$NON-NLS-1$
				url = url.substring(1);
			url = "http://" + url; //$NON-NLS-1$
		}
		return url;
	}

	/**
	 * Encodes a URL segment into the application/x-www-form-urlencoded format.
	 * In addition, '+' characters (that represent whitespace) are encoded into
	 * '%20'
	 *
	 * @param s
	 *            - input string
	 * @return - endoded string
	 */
	public static String encodeUrlSegment(String s) {
		try {
			String encoded = URLEncoder.encode(s, "UTF-8"); //$NON-NLS-1$
			return encoded.replaceAll("\\+", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}

	/**
	 * Decode a URL from the application/x-www-form-urlencoded forma
	 *
	 * @param s
	 *            - URL or URL part
	 * @return the decoded string
	 */
	public static String decodeUrl(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}

	/**
	 * Returns the URI of an associated XMP file
	 *
	 * @param uri
	 *            - image file
	 * @return possible URIs of associated XMP file. The most specific URI is
	 *         given first
	 */
	public static URI[] getSidecarURIs(URI uri) {
		String u = uri.toString();
		try {
			return new URI[] { new URI(u + XMP), new URI(removeExtensionFromUri(u) + XMP) };
		} catch (URISyntaxException e1) {
			// should not happen
		}
		return EMPTYURIS;
	}

	/**
	 * Removes the file name extension from a given URI string
	 *
	 * @param uri
	 *            - input URI
	 * @return - output URI fragment
	 */
	public static String removeExtensionFromUri(String uri) {
		int p = uri.lastIndexOf('/');
		int q = uri.lastIndexOf('.');
		return (q > p) ? uri.substring(0, q) : uri;
	}

	/**
	 * Returns the URI of an associated WAV file
	 *
	 * @param file
	 *            - image file
	 * @return URI of associated WAV file
	 */
	public static URI getVoicefileURI(File file) {
		try {
			return new URI(removeExtensionFromUri(file.toURI().toString()) + ".wav"); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			// should not happen
		}
		return null;
	}

	/**
	 * Returns a IRecipeProvider instance
	 *
	 * @return recipe provider implementation
	 */
	public static IRecipeProvider getRecipeProvider() {
		return BatchActivator.getDefault().getCurrentRawConverter(false);
	}

	/**
	 * Waits until the requested cancel of the given job families was performed
	 * 
	 * @param family
	 *            - families that were canceled
	 */
	public static void waitOnJobCanceled(String... families) {
		for (String family : families) {
			try {
				Job.getJobManager().join(family, null);
			} catch (OperationCanceledException e) {
				// ignore
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

}
