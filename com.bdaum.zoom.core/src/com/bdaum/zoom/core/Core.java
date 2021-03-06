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
package com.bdaum.zoom.core;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.bdaum.zoom.common.CommonUtilities;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.image.ImageConstants;
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

	private static final double EARTHRADIUS = 6371.01;
	private static final String XMP = ".xmp"; //$NON-NLS-1$
	private static final ICore core = CoreActivator.getDefault();
	private static final File[] EMPTYFILES = new File[0];

	private static final Comparator<File> sidecarComparator1 = new Comparator<File>() {
		@Override
		public int compare(File f1, File f2) {
			long m1 = f1.lastModified();
			long m2 = f2.lastModified();
			if (m1 == m2) {
				m1 = f1.getName().length();
				m2 = f2.getName().length();
				return m1 == m2 ? 0 : m1 > m2 ? -1 : 1;
			}
			return m1 > m2 ? -1 : 1;
		}
	};
	private static final Comparator<File> sidecarComparator2 = new Comparator<File>() {
		@Override
		public int compare(File f1, File f2) {
			long m1 = f1.lastModified();
			long m2 = f2.lastModified();
			if (m1 == m2) {
				m1 = f1.getName().length();
				m2 = f2.getName().length();
				return m1 == m2 ? 0 : m1 > m2 ? -1 : 1;
			}
			return m1 < m2 ? -1 : 1;
		}
	};

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
	 * Converts an array of tokens into a string list separated by separators Null
	 * tokens are supressed
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
		for (Object t : tokens)
			condAppend(t, sb, sep);
		return sb.toString();
	}

	/**
	 * Appends token to a string builder if not null, separated by seperation string
	 * 
	 * @param token
	 *            - token
	 * @param buf
	 *            - buffer
	 * @param sep
	 *            - separator
	 */
	public static void condAppend(Object token, StringBuilder buf, String sep) {
		if (token != null) {
			if (buf.length() > 0)
				buf.append(", "); //$NON-NLS-1$
			buf.append(token);
		}
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
		return CommonUtilities.fromStringList(stringlist, seps);
	}

	/**
	 * This routine calculates the distance between two points (given the
	 * latitude/longitude of those points).
	 *
	 * South latitudes are negative, east longitudes are positive
	 *
	 * Source: https://software.intel.com/node/341473
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
	 *            - the unit for results: 'M' is statute miles, 'K' is kilometers,
	 *            'N' is nautical miles
	 * @return distance
	 */

	public static double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
		double phi1 = Math.toRadians(lat1);
		double phi2 = Math.toRadians(lat2);
		double dist = EARTHRADIUS
				* Math.acos(Math.sin(phi1) * Math.sin(phi2) + Math.cos(phi1) * Math.cos(phi2) * Math.cos(Math.toRadians(lon2-lon1)));
		return fromKm(dist, unit);
	}

	public static double fromKm(double km, char unit) {
		switch (unit) {
		case 'K':
		case 'k':
			return km;
		case 'N':
		case 'n':
			return km / 1.852;
		default:
			return km / 1.609344;
		}
	}

	public static double toKm(double value, char unit) {
		switch (unit) {
		case 'K':
		case 'k':
			return value;
		case 'N':
		case 'n':
			return value * 1.852;
		default:
			return value * 1.609344;
		}
	}

	/**
	 * This routine calculates the initial bearing between two points Source:
	 * https://software.intel.com/en-us/blogs/2012/11/30/calculating-a-bearing-between-points-in-location-aware-apps
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
		double phi1 = Math.toRadians(lat1);
		double phi2 = Math.toRadians(lat2);
		double lam1 = Math.toRadians(lon1);
		double lam2 = Math.toRadians(lon2);
		double angle = Math.atan2(Math.sin(lam2 - lam1) * Math.cos(phi2),
				Math.cos(phi1) * Math.sin(phi2) - Math.sin(phi1) * Math.cos(phi2) * Math.cos(lam2 - lam1));
		return (Math.toDegrees(angle) + 360.0) % 360;
	}

	/**
	 * This routine calculates the distance between two points (given the
	 * latitude/longitude of those points).
	 *
	 * South latitudes are negative, east longitudes are positive
	 *
	 * Source: https://software.intel.com/node/341473
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
	 *            - the unit for results: 'M' is statute miles, 'K' is kilometers,
	 *            'N' is nautical miles
	 * @return distance
	 */

	public static Double coord(double lat1, double lon1, double bearing, double distance, char unit) {
		double dist;
		switch (unit) {
		case 'K':
		case 'k':
			dist = distance;
			break;
		case 'N':
		case 'n':
			dist = distance * 1.852;
			break;
		default:
			dist = distance * 1.609344;
		}
		double brg = Math.toRadians(bearing);
		dist = dist / EARTHRADIUS;
		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1) * Math.sin(dist) * Math.cos(brg));
		double lon2 = lon1 + Math.atan2(Math.sin(brg) * Math.sin(dist) * Math.cos(lat1),
				Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
		return new Point2D.Double(Math.toDegrees(lat2), (Math.toDegrees(lon2) + 540) % 360 - 180);
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
	 * @return - the downloaded file (temp file). When file needs to be preserved it
	 *         must be copied to a different location.
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
			File tempfile = createTempFile("ImageDownLoad", fileExtension); //$NON-NLS-1$
			copyBytesToFile(in, tempfile, null);
			if (box == null)
				service.endSession(ticket);
			bundleContext.ungetService(serviceReference);
			return tempfile;
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
	 * Extract the file extension from a path specification If path does not contain
	 * a file extension, the file header is analyzed for the image type
	 *
	 * @param uri
	 *            - URI
	 * @return file extension
	 */
	public static String getFileExtension(String uri) {
		String fileExtension = BatchUtilities.getTrueFileExtension(uri);
		return fileExtension.isEmpty() ? ImageUtilities.detectImageFormat(uri) : fileExtension.toLowerCase();
	}

	/**
	 * Extracts the short filename from an uri
	 *
	 * @param uri
	 *            - the given URI
	 * @param withExtension
	 *            - true if filename shall be return including file name extension,
	 *            false otherwise
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
	 *            - true if filename shall be return including file name extension,
	 *            false otherwise
	 * @return - file name
	 */
	public static String getFileName(String uri, boolean withExtension) {
		int p = uri.lastIndexOf('/') + 1;
		if (!withExtension) {
			int q = uri.lastIndexOf('.');
			if (q > p)
				return decodeUrl(uri.substring(p, q));
		}
		return decodeUrl(uri.substring(p));
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
			return "http://" + url; //$NON-NLS-1$
		}
		return url;
	}

	/**
	 * Encodes a URL segment into the application/x-www-form-urlencoded format. In
	 * addition, '+' characters (that represent whitespace) are encoded into '%20'
	 *
	 * @param s
	 *            - input string
	 * @return - endoded string
	 */
	public static String encodeUrlSegment(String s) {
		if (s != null)
			try {
				return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} catch (UnsupportedEncodingException e) {
				// do nothing
			}
		return s;
	}

	/**
	 * Decode a URL from the application/x-www-form-urlencoded forma
	 *
	 * @param s
	 *            - URL or URL part
	 * @return the decoded string
	 */
	public static String decodeUrl(String s) {
		if (s != null)
			try {
				return URLDecoder.decode(s, "UTF-8"); //$NON-NLS-1$
			} catch (UnsupportedEncodingException e) {
				// do nothing
			}
		return s;
	}

	/**
	 * Returns the possibly associated XMP files
	 *
	 * @param uri
	 *            - image file URI
	 * @param latestFirst
	 *            - determines if the files are sorted by modification date in
	 *            ascending or descending order
	 * @return possible sidecar files. Results are ordered by modification date,
	 *         secondarily by descending file name length
	 */
	public static File[] getSidecarFiles(URI uri, boolean latestFirst) {
		String u = uri.toString();
		try {
			File[] files = new File[] { new File(new URI(u + XMP)),
					new File(new URI(removeExtensionFromUri(u) + XMP)) };
			Arrays.sort(files, latestFirst ? sidecarComparator1 : sidecarComparator2);
			return files;
		} catch (URISyntaxException e1) {
			// should not happen
		}
		return EMPTYFILES;
	}

	/**
	 * Removes the file name extension from a given URI string
	 *
	 * @param uri
	 *            - input URI
	 * @return - output URI fragment
	 */
	public static String removeExtensionFromUri(String uri) {
		int q = uri.lastIndexOf('.');
		return (q > uri.lastIndexOf('/')) ? uri.substring(0, q) : uri;
	}

	/**
	 * Returns the URI of an associated WAV file
	 *
	 * @param file
	 *            - image file
	 * @return URI of associated WAV file
	 */
	public static URI getVoicefileURI(File file) {
		String trunc = removeExtensionFromUri(file.getAbsolutePath());
		for (String ext : ImageConstants.VOICEEXT) {
			File voiceFile = new File(trunc + ext);
			if (voiceFile.exists())
				return voiceFile.toURI();
		}
		return null;
	}
//TODO 
	/**
	 * Returns a IRecipeProvider instance
	 *
	 * @return recipe provider implementation
	 */
//	public static IRecipeProvider getRecipeProvider() {
//		return BatchActivator.getDefault().getCurrentRawConverter(false);
//	}

	/**
	 * Waits until the requested cancel of the given job families was performed
	 * 
	 * @param family
	 *            - families that were canceled
	 */
	public static void waitOnJobCanceled(String... families) {
		for (String family : families)
			try {
				Job.getJobManager().join(family, null);
			} catch (OperationCanceledException | InterruptedException e) {
				// ignore
			}
	}

}
