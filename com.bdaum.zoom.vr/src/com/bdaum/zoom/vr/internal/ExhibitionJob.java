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
 * (c) 2012-2014 Berthold Daum  
 */
package com.bdaum.zoom.vr.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.batch.internal.IFileWatcher;
import com.bdaum.zoom.batch.internal.LoaderListener;
import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.Rgb_typeImpl;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibit;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.Ticketbox;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.image.internal.swt.ImageLoader;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.job.CustomJob;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;
import com.bdaum.zoom.program.HtmlEncoderDecoder;

@SuppressWarnings("restriction")
public class ExhibitionJob extends CustomJob implements LoaderListener {

	private static final String TMP = ".tmp"; //$NON-NLS-1$
	private static final int DOORDEPTH = 150;
	private static final int DOORHEIGHT = 2000;
	private static final int DOORWIDTH = 1000;
	private static final int INFOLEFT = 1;
	private static final int INFONONE = -1;

	private static final int MAPMARGINS = 250;
	private static final String HTTP = "http://"; //$NON-NLS-1$
	private static final String HTTPS = "https://"; //$NON-NLS-1$

	private static final String VAR_START = "var start = '"; //$NON-NLS-1$
	private static final String ROOMPREFIX = "room_"; //$NON-NLS-1$

	private String opId = java.util.UUID.randomUUID().toString();
	private IFileWatcher fileWatcher = CoreActivator.getDefault().getFileWatchManager();
	private static final double D05 = 0.5d;
	// private static final int LABELXOFF = 60;
	private static final int LABELWIDTH = 100;
	private static final int LABELHEIGHT = 60;
	private static final int ENTRYSIZE = 240;
	private static final int INFOMARGINS = 40;
	private static final int INFOWIDTH = 950;
	private static final int INFOGAP = 50;
	private static final int INFOHEIGHT = 1333;
	private static final int MINLIGHTDIST = 150;
	private final ExhibitionImpl gallery;
	private final IAdaptable adaptable;
	private boolean wasAborted;
	private final boolean makeDefault;
	private File targetFolder;
	private MultiStatus status;
	private IProgressMonitor monitor;
	private final String exhibitionId;
	private Image entryImage;
	private GC entryGC;
	private List<Point> lights = new ArrayList<Point>();
	private int jpegQuality;
	private HtmlEncoderDecoder htmlEncoderDecoder;

	public ExhibitionJob(ExhibitionImpl gallery, String exhibitionId, IAdaptable adaptable, boolean makeDefault) {
		super(Messages.ExhibitionJob_generate_exhibition);
		this.gallery = gallery;
		this.exhibitionId = exhibitionId;
		this.adaptable = adaptable;
		this.makeDefault = makeDefault;
		this.jpegQuality = gallery.getJpegQuality();
		setPriority(Job.LONG);
		setUser(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 */

	@Override
	public boolean belongsTo(Object family) {
		return Constants.OPERATIONJOBFAMILY == family || Constants.CRITICAL == family;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	protected IStatus runJob(IProgressMonitor aMonitor) {
		this.monitor = aMonitor;
		long startTime = System.currentTimeMillis();
		status = new MultiStatus(VrActivator.PLUGIN_ID, 0, Messages.ExhibitionJob_generator, null);
		try {
			generate();
			wasAborted = aMonitor.isCanceled();
		} catch (Exception e) {
			addError(Messages.ExhibitionJob_error_generating, e);
			wasAborted = true;
		}
		if (!wasAborted)
			OperationJob.signalJobEnd(startTime);
		return status;
	}

	private void generate() throws IOException {
		int work = 0;
		for (Wall wall : gallery.getWall())
			work += wall.getExhibit().size();
		int entries = (int) Math.min(4, Math.floor(Math.sqrt(work)));
		work += 6;
		monitor.beginTask(Messages.ExhibitionJob_generating, work);
		boolean isFtp = gallery.getIsFtp();
		if (isFtp)
			targetFolder = Core.createTempDirectory("FtpTransfer"); //$NON-NLS-1$
		else {
			targetFolder = new File(gallery.getOutputFolder());
			targetFolder.mkdirs();
		}
		// Scripts
		File scripts = VrActivator.getDefault().locateResource("/resources/scripts"); //$NON-NLS-1$
		copyResources(scripts, targetFolder, null, true, monitor);
		if (monitor.isCanceled())
			return;
		monitor.worked(1);
		// Images
		File images = VrActivator.getDefault().locateResource("/resources/images"); //$NON-NLS-1$
		copyResources(images, targetFolder, null, true, monitor);
		if (monitor.isCanceled())
			return;
		monitor.worked(1);
		// Styles
		File styles = VrActivator.getDefault().locateResource("/resources/styles"); //$NON-NLS-1$
		copyResources(styles, targetFolder, null, false, monitor);
		if (monitor.isCanceled())
			return;
		monitor.worked(1);
		// HTML
		File html = VrActivator.getDefault().locateResource("/resources/html"); //$NON-NLS-1$
		copyResources(html, targetFolder, null, true, monitor);
		if (monitor.isCanceled())
			return;
		String link = gallery.getPageName();
		if (link == null || link.isEmpty())
			link = "index.html"; //$NON-NLS-1$
		File linkFile = new File(targetFolder, link);
		try (Writer writer = new BufferedWriter(new FileWriter(linkFile))) {
			StringBuilder sb = new StringBuilder();
			sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 3.2//EN\">\r\n") //$NON-NLS-1$
					.append("<HTML>\r\n\t<HEAD>\r\n\t<META http-equiv=\"Refresh\" content=\"0; URL=html/room.htm\">\r\n") //$NON-NLS-1$
					.append(NLS.bind("\t\t<META name=\"keywords\" content=\"{0}\">\r\n", //$NON-NLS-1$
							Core.toStringList(gallery.getKeyword(), ',')))
					.append(NLS.bind("\t\t<TITLE>{0}</TITLE>\r\n", //$NON-NLS-1$
							gallery.getName()))
					.append("\t</HEAD>\r\n  <BODY/>\r\n</HTML>\r\n"); //$NON-NLS-1$
			writer.write(sb.toString());
		}
		if (monitor.isCanceled())
			return;
		monitor.worked(1);
		// Resources
		File resFile = new File(targetFolder, "res"); //$NON-NLS-1$
		resFile.mkdir();
		File audioFile = new File(resFile, "audio"); //$NON-NLS-1$
		audioFile.mkdir();
		File room = VrActivator.getDefault().locateResource("/resources/res/room"); //$NON-NLS-1$
		File roomFile = copyResources(room, resFile, ROOMPREFIX + exhibitionId, false, monitor);
		String audio = gallery.getAudio();
		File audioTarget = null;
		if (audio != null && !audio.isEmpty()) {
			File audioSource = new File(audio);
			if (audioSource.exists()) {
				audioTarget = new File(audioFile, audioSource.getName());
				if (!audioTarget.exists())
					try {
						BatchUtilities.copyFile(audioSource, audioTarget, monitor);
					} catch (DiskFullException e) {
						addError(NLS.bind(Messages.ExhibitionJob_disk_full_copying, audioTarget), e);
					}
			}
		}
		String logo = gallery.getLogo();
		File logoTarget = null;
		File imageFolder = new File(targetFolder, "images"); //$NON-NLS-1$
		if (logo != null && !logo.isEmpty()) {
			File logoSource = new File(logo);
			if (logoSource.exists()) {
				logoTarget = new File(imageFolder, logoSource.getName());
				if (logoTarget.exists())
					logoTarget.delete();
				try {
					BatchUtilities.copyFile(logoSource, logoTarget, monitor);
				} catch (DiskFullException e) {
					addError(NLS.bind(Messages.ExhibitionJob_disk_full_copying, logoTarget), e);
				}
			}
		}
		if (monitor.isCanceled())
			return;
		monitor.worked(1);
		generateGeometry(resFile, roomFile, audioTarget, entries);
		int n = updateConfig(resFile);
		File howtouse = new File(imageFolder, "howtouse.png"); //$NON-NLS-1$
		howtouse.delete();
		File howtouseSingle = new File(imageFolder, "howtouseSingle.png"); //$NON-NLS-1$
		File howtouseMulti = new File(imageFolder, "howtouseMulti.png"); //$NON-NLS-1$
		if (n > 1) {
			howtouseMulti.renameTo(howtouse);
			howtouseSingle.delete();
		} else {
			howtouseSingle.renameTo(howtouse);
			howtouseMulti.delete();
		}
		monitor.worked(1);
		monitor.done();
	}

	private void generateGeometry(File resFolder, File roomFolder, File audioFile, int entries) throws IOException {
		String roomName = ROOMPREFIX + exhibitionId;
		Point sceneSize = computeSceneSize(gallery);
		Point woff = new Point(0, 0);
		if (sceneSize.x == 0) {
			sceneSize.x = sceneSize.y / 2;
			woff.x = sceneSize.x / 2;
		}
		if (sceneSize.y == 0) {
			sceneSize.y = sceneSize.x / 2;
			woff.y = sceneSize.y / 2;
		}
		int max = Math.max(sceneSize.x, sceneSize.y);
		double scale = 2000d / max;
		File geomFile = new File(roomFolder, "geometry.js"); //$NON-NLS-1$
		geomFile.delete();
		StringBuilder sb = new StringBuilder(4096);
		String web = gallery.getWebUrl();
		String label = web;
		web = formatWebUrl(web);
		String copyright = gallery.getCopyright();
		if (copyright != null && !copyright.isEmpty())
			sb.append("&copy; ").append(getHtmlEncoderDecoder().encodeHTML(copyright, false)); //$NON-NLS-1$
		String contact = gallery.getContactName();
		String email = gallery.getEmail();
		if (contact == null || contact.isEmpty())
			contact = email;
		if (contact != null && !contact.isEmpty()) {
			if (sb.length() > 0)
				sb.append("<br/>"); //$NON-NLS-1$
			if (email != null && !email.isEmpty())
				sb.append("<a href='mailto:").append(email).append("'>") //$NON-NLS-1$ //$NON-NLS-2$
						.append(getHtmlEncoderDecoder().encodeHTML(contact, false)).append("</a>"); //$NON-NLS-1$
			else
				sb.append(getHtmlEncoderDecoder().encodeHTML(contact, false));
		}
		if (web != null && !web.isEmpty()) {
			if (sb.length() > 0)
				sb.append(", "); //$NON-NLS-1$
			if (label.startsWith(HTTP))
				label = label.substring(HTTP.length());
			if (label.startsWith(HTTPS))
				label = label.substring(HTTPS.length());
			generateLink(web, sb);
			sb.append(getHtmlEncoderDecoder().encodeHTML(label, false)).append("</a>"); //$NON-NLS-1$
		}
		if (sb.length() > 0)
			sb.append("<br/>"); //$NON-NLS-1$
		sb.append(
				"Special Thanks to <a href='http://www.dhteumeuleu.com/' target='_blank'>Gerard Fernandez</a> and <a href='http://www.visitnmc.com/' target='_blank'>The National Museum of China</a> for their contribution."); //$NON-NLS-1$
		sb.append(
				"<br/>All Javascript code on this site is licensed under the <a href='http://creativecommons.org/licenses/by-nc/3.0/' target='_blank'>Creative Commons License</a>"); //$NON-NLS-1$
		String impressum = sb.toString();
		geomFile.createNewFile();
		sb.setLength(0);
		sb.append("var geometry_").append(exhibitionId).append(" = {\r\n	\"geometry\": ["); //$NON-NLS-1$ //$NON-NLS-2$
		int viewingHeight = gallery.getDefaultViewingHeight();
		int roomHeight = viewingHeight * 3 / 2;
		for (Wall wall : gallery.getWall())
			roomHeight = Math.max(roomHeight, wall.getHeight());
		int headHeight = roomHeight - viewingHeight;
		generateSpriteFragment(sb, roomName, "big_light2.png", 0, headHeight //$NON-NLS-1$
				* scale - 100, 0, 0.65);
		int cnt = generateImageList(resFolder, roomFolder, roomName, sb, sceneSize, woff, scale, headHeight, entries);
		double sx = gallery.getStartX() - sceneSize.x / 2;
		double sy = sceneSize.y / 2 - gallery.getStartY();
		double sangle = sx == 0 ? 90 : Math.toDegrees(Math.atan(sy / sx));
		if (sx < 0)
			sangle += 180;
		sangle -= 190;
		double tx = 30 * Math.cos(Math.toRadians(sangle));
		double ty = 30 * Math.sin(Math.toRadians(sangle));
		NumberFormat nf1 = NumberFormat.getInstance(Locale.US);
		nf1.setMaximumFractionDigits(8);
		nf1.setGroupingUsed(false);
		sb.append("],") //$NON-NLS-1$
				.append("\r\n\t\"params\": {\r\n\t\t\"id\" : '").append(exhibitionId) //$NON-NLS-1$
				.append("',\r\n" //$NON-NLS-1$
						+ "\t\t\"name\" : \"") //$NON-NLS-1$
				.append(Utilities.encodeJavascript(gallery.getName())).append("\",\r\n\t\t\"info\" : \"") //$NON-NLS-1$
				.append(gallery.getInfo() != null ? Utilities.encodeJavascript(normalizeWhitespace(gallery.getInfo()))
						: "") //$NON-NLS-1$
				.append("\",\r\n\t\t\"groundColor\": \"") //$NON-NLS-1$
				.append(generateColor(gallery.getGroundColor())).append("\",\r\n\t\t\"horizonColor\": \"") //$NON-NLS-1$
				.append(generateColor(gallery.getHorizonColor())).append("\",\r\n\t\t\"ceilingColor\": \"") //$NON-NLS-1$
				.append(generateColor(gallery.getCeilingColor()))
				.append("\",\r\n\t\t\"imagesPath\": \"../res/\",\r\n\t\t\"shadingLight\": 350,\r\n\t\t\"ambientLight\": 0.25,\r\n\t\t\"startX\": ") //$NON-NLS-1$
				.append(nf1.format(tx)).append(",\r\n\t\t\"startZ\": ") //$NON-NLS-1$
				.append(nf1.format(ty)).append(",\r\n		\"targetX\": 0,\r\n\t\t\"targetZ\": 0,\r\n"); //$NON-NLS-1$
		if (audioFile != null)
			sb.append("\t\t\"audio\": \"../res/audio/").append(audioFile.getName()).append("\",\r\n");//$NON-NLS-1$ //$NON-NLS-2$
		if (!impressum.isEmpty())
			sb.append("\t\t\"contact\": \"").append(impressum).append("\",\r\n");//$NON-NLS-1$ //$NON-NLS-2$

		sb.append("\t\t\"imgCount\": ").append(cnt).append(",\r\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "\t\t\"startIndex\": 1,\r\n"); //$NON-NLS-1$
		String webUrl = gallery.getWebUrl();
		if (webUrl != null && !webUrl.isEmpty())
			sb.append("\t\t\"logoUrl\": \"").append(webUrl).append("\",\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("\t\t\"map_bg\":\"../res/").append(roomName).append("/map_bg.png\",\r\n") //$NON-NLS-1$ //$NON-NLS-2$
				.append("\t\t\"map_color\": \"#75755f\",\r\n\t\t\"map_obj_color\": \"#acaa92\",\r\n\t\t\"sceneW\": ") //$NON-NLS-1$
				.append(round(sceneSize.x * scale + MAPMARGINS)).append(",\r\n\t\t\"sceneH\": ") //$NON-NLS-1$
				.append(round(sceneSize.y * scale + MAPMARGINS)).append("\r\n\t}\r\n}"); //$NON-NLS-1$
		try (Writer writer = new BufferedWriter(new FileWriter(geomFile))) {
			writer.write(sb.toString());
		}
		createMapBackground(roomFolder, sceneSize, scale, 117, 117, 95);
	}

	private static String normalizeWhitespace(String text) {
		StringBuilder sb = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			sb.append(Character.isWhitespace(c) ? " " : c); //$NON-NLS-1$
		}
		return sb.toString();
	}

	private void createMapBackground(File roomFolder, Point sceneSize, double scale, int r, int g, int b) {
		final Shell shell = adaptable.getAdapter(Shell.class);
		Image bgImage = new Image(shell.getDisplay(), (int) ((sceneSize.x * scale + MAPMARGINS) * 0.04 + 20),
				(int) ((sceneSize.y * scale + MAPMARGINS) * 0.04 + 20));
		GC gc = new GC(bgImage);
		Rectangle bounds = bgImage.getBounds();
		Color bgColor = new Color(shell.getDisplay(), r, g, b);
		gc.setBackground(bgColor);
		gc.fillRectangle(bounds);
		bgColor.dispose();
		int lr = (r + 255) / 2;
		int lg = (g + 255) / 2;
		int lb = (b + 255) / 2;
		for (int i = 0; i < 10; i++) {
			Color lColor = new Color(shell.getDisplay(), lr, lg, lb);
			gc.setForeground(lColor);
			int y2 = bounds.height - 1 - i;
			int x2 = bounds.width - 1 - i;
			gc.drawLine(i, i, x2, i);
			gc.drawLine(i, y2, x2, y2);
			gc.drawLine(i, i, i, y2);
			gc.drawLine(x2, i, x2, y2);
			lColor.dispose();
			lr = lr * 5 / 6;
			lg = lg * 5 / 6;
			lb = lb * 5 / 6;
		}
		gc.dispose();
		saveImage(new File(roomFolder, "map_bg.png"), bgImage, SWT.IMAGE_PNG, -1); //$NON-NLS-1$
		bgImage.dispose();
	}

	private static int round(double d) {
		return (int) (d < 0 ? d - D05 : d + D05);
	}

	protected int generateImageList(File resFolder, final File roomFolder, String roomName, StringBuilder sb,
			Point sceneSize, Point woff, double scale, double headHeight, int entries) {
		Set<String> artists = new HashSet<String>();
		int infoPlatePosition = gallery.getInfoPlatePosition();
		Wall doorWall = null;
		entryImage = null;
		entryGC = null;
		int tileSize = ENTRYSIZE / entries;
		int entryX = 0;
		int entryY = 0;
		int mx = sceneSize.x / 2;
		int my = sceneSize.y / 2;
		final Shell shell = adaptable.getAdapter(Shell.class);
		File manifest = new File(resFolder, "imagelist.xml"); //$NON-NLS-1$
		DialogSettings newsettings = new DialogSettings("images"); //$NON-NLS-1$
		DialogSettings settings = new DialogSettings("images"); //$NON-NLS-1$
		try {
			settings.load(manifest.getAbsolutePath());
		} catch (IOException e1) {
			// ignore
		}
		ICore activator = CoreActivator.getDefault();
		IVolumeManager volumeManager = activator.getVolumeManager();
		IDbManager dbManager = activator.getDbManager();
		double zoom = 0.24d;
		int sx = gallery.getStartX();
		int sy = gallery.getStartY();
		int tagId = 1;
		int wallId = 1;
		try (Ticketbox box = new Ticketbox()) {
			List<File> renamedFiles = new ArrayList<File>();
			for (Wall wall : gallery.getWall()) {
				Rgb_type wallColor = getWallColor(wall);
				double angle = wall.getGAngle();
				double cos = Math.cos(Math.toRadians(angle));
				double sin = Math.sin(Math.toRadians(angle));
				int wallX1 = wall.getGX();
				int wallY1 = wall.getGY();
				int wallW = wall.getWidth();
				double wallX2 = wallX1 + cos * wallW;
				double wallX3 = wallX2;
				double wallY2 = wallY1 + sin * wallW;
				double wallY3 = wallY2;
				boolean door = false;
				if (sx >= Math.min(wallX1, wallX2) - 50 && sx <= Math.max(wallX1, wallX2) + 50
						&& sy >= Math.min(wallY1, wallY2) - 50 && sy <= Math.max(wallY1, wallY2) + 50) {
					if (Math.abs(cos) > 0.01d)
						door = Math.abs(wallY1 - sy + (sx - wallX1) * sin / cos) < 50;
					else
						door = true;
				}
				double doorX1 = 0;
				double doorY1 = 0;
				if (door) {
					if (infoPlatePosition == INFOLEFT) {
						doorX1 = sx + cos * (INFOGAP + INFOWIDTH - DOORWIDTH / 2);
						doorY1 = sy + sin * (INFOGAP + INFOWIDTH - DOORWIDTH / 2);
					} else {
						doorX1 = sx - cos * DOORWIDTH / 2;
						doorY1 = sy - sin * DOORWIDTH / 2;
					}
					wallX2 = doorX1;
					wallY2 = doorY1;
				}
				double x1 = (wallX1 - mx + woff.x) * scale;
				double x2 = (wallX2 - mx + woff.x) * scale;
				double x3 = (wallX3 - mx + woff.x) * scale;
				double y1 = headHeight * scale;
				double vHeight = gallery.getDefaultViewingHeight();
				double y2 = -vHeight * scale;
				double z1 = (my - wallY1 + woff.y) * scale;
				double z2 = (my - wallY2 + woff.y) * scale;
				double z3 = (my - wallY3 + woff.y) * scale;
				// Left wall part
				generateEmptyWall(sb, x1, y1, z1, x2, y2, z2, wallColor, 2000, -1001, false,
						"Wall " + wallId + (door ? " (left part)" : "")); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				if (door) {
					doorWall = wall;
					double doorX2 = doorX1 + cos * DOORWIDTH;
					double doorY2 = doorY1 + sin * DOORWIDTH;
					double dx1 = (doorX1 - mx + woff.x + sin * DOORDEPTH) * scale;
					double dx2 = (doorX2 - mx + woff.x + sin * DOORDEPTH) * scale;
					double dy1 = (DOORHEIGHT - vHeight) * scale;
					double dy2 = -vHeight * scale;
					double dz1 = (my - doorY1 + woff.y + cos * DOORDEPTH) * scale;
					double dz2 = (my - doorY2 + woff.y + cos * DOORDEPTH) * scale;
					// Right wall part
					double sx1 = (doorX1 - mx + woff.x) * scale;
					double sx2 = (doorX2 - mx + woff.x) * scale;
					double sz1 = (my - doorY1 + woff.y) * scale;
					double sz2 = (my - doorY2 + woff.y) * scale;
					generateEmptyWall(sb, sx2, y1, sz2, x3, y2, z3, wallColor, 2000, -1001, false,
							"Wall " + wallId + " (right part)"); //$NON-NLS-1$ //$NON-NLS-2$
					// Wall above door
					generateEmptyWall(sb, sx1, y1, sz1, sx2, dy1, sz2, wallColor, 2000, -1001, false,
							"Wall " + wallId + " (above door)"); //$NON-NLS-1$ //$NON-NLS-2$
					// Top door frame
					generateEmptyWall(sb, sx1, dy1, dz1, dx2, dy1, sz2, wallColor, 3000, -200, true,
							"Door frame (top)"); //$NON-NLS-1$
					// Left door frame
					generateEmptyWall(sb, sx1, dy1, sz1, dx1, dy2, dz1, wallColor, 500, -200, false,
							"Door frame (left)"); //$NON-NLS-1$
					// Right door frame
					generateEmptyWall(sb, dx2, dy1, dz2, sx2, dy2, sz2, wallColor, 500, -200, false,
							"Door frame (right)"); //$NON-NLS-1$
					// Next room
					Rgb_type nextColor = new Rgb_typeImpl(wallColor.getR() * 2 / 3, wallColor.getG() * 2 / 3,
							wallColor.getB() * 2 / 3);
					double nx1 = x1 + sin * DOORDEPTH * scale;
					double nz1 = z1 + cos * DOORDEPTH * scale;
					double nx2 = nx1 + cos * wallW * scale;
					double nz2 = nz1 - sin * wallW * scale;
					double ex1 = x1 + sin * sceneSize.x * scale;
					double ez1 = z1 + cos * sceneSize.x * scale;
					double ex2 = ex1 + cos * wallW * scale;
					double ez2 = ez1 - sin * wallW * scale;
					double dzoom = 27 * zoom * scale;
					generateWallWithImage(sb, roomName, tagId++, "door.png", //$NON-NLS-1$
							nx1, y1, nz1, ex1, y2, ez1, nextColor, 8000, -200, 720 - angle, dzoom, 0d, "none", //$NON-NLS-1$
							"Neighbour room (left)", true); //$NON-NLS-1$
					generateWallWithImage(sb, roomName, tagId++, "door.png", //$NON-NLS-1$
							ex1, y1, ez1, ex2, y2, ez2, nextColor, 8000, -200, 630 - angle, dzoom, 0d, "none", //$NON-NLS-1$
							"Neighbour room (middle)", true); //$NON-NLS-1$
					generateWallWithImage(sb, roomName, tagId++, "door.png", //$NON-NLS-1$
							ex2, y1, ez2, nx2, y2, nz2, nextColor, 8000, -200, 540 - angle, dzoom, 0d, "none", //$NON-NLS-1$
							"Neighbour room (right)", true); //$NON-NLS-1$
				}
				List<String> exhibits = wall.getExhibit();
				for (String id : exhibits) {
					final ExhibitImpl exhibit = dbManager.obtainById(ExhibitImpl.class, id);
					if (exhibit != null) {
						AssetImpl asset = dbManager.obtainAsset(exhibit.getAsset());
						if (asset != null) {
							artists.addAll(Core.fromStringList(exhibit.getCredits(), ",")); //$NON-NLS-1$
							int isize = (int) (Math.max(exhibit.getWidth(), exhibit.getHeight()) * scale / zoom);
							String copyright = null;
							if (gallery.getAddWatermark()) {
								copyright = asset.getCopyright();
								if (copyright == null || copyright.isEmpty())
									copyright = gallery.getCopyright();
								if (copyright != null && copyright.isEmpty())
									copyright = null;
							}
							URI uri = volumeManager.findExistingFile(asset, false);
							if (uri != null) {
								File originalFile = null;
								try {
									originalFile = box.obtainFile(uri);
								} catch (IOException e) {
									status.add(new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID,
											NLS.bind(Messages.ExhibitionJob_download_image_failed, uri), e));
								}
								if (originalFile != null) {
									int rotation = asset.getRotation();
									String imageName = tagId + ".jpg"; //$NON-NLS-1$
									createManifestEntry(newsettings, exhibit.getStringId(), imageName, uri, rotation,
											isize, copyright, gallery.getRadius(), gallery.getAmount(),
											gallery.getThreshold(), gallery.getApplySharpening());
									File imageFile = new File(roomFolder, imageName);
									if (manifestEntryModified(settings, imageFile, exhibit.getStringId(), uri, rotation,
											isize, copyright, gallery.getRadius(), gallery.getAmount(),
											gallery.getThreshold(), gallery.getApplySharpening())) {
										UnsharpMask umask = gallery.getApplySharpening()
												? ImageActivator.getDefault().computeUnsharpMask(gallery.getRadius(),
														gallery.getAmount(), gallery.getThreshold())
												: null;
										ZImage zimage = null;
										try {
											zimage = CoreActivator.getDefault().getHighresImageLoader().loadImage(null,
													status, originalFile, rotation, asset.getFocalLengthIn35MmFilm(),
													new Rectangle(0, 0, isize, isize), 1d, 1d, true,
													ImageConstants.SRGB, null, umask, null, fileWatcher, opId, this);
										} catch (UnsupportedOperationException e) {
											// do nothing
										}
										Image image = null;
										image = zimage == null ? null
												: zimage.getSwtImage(shell.getDisplay(), true, ZImage.CROPPED, isize,
														isize);
										if (image != null) {
											addToEntryImage(image, entryX, entryY, tileSize);
											try {
												image = decorateImage(image, copyright);
												imageFile = saveImage(imageFile, image, SWT.IMAGE_JPEG, jpegQuality);
											} finally {
												if (image != null)
													image.dispose();
											}
										}
									} else {
										reviewSequence(settings, exhibit.getStringId(), imageName, roomFolder,
												renamedFiles);

										ImageData[] data = new ImageLoader().load(imageFile.getAbsolutePath());
										Image image = new Image(shell.getDisplay(), data[0]);
										addToEntryImage(image, entryX, entryY, tileSize);
										image.dispose();
									}
									++entryX;
									if (entryX >= entries) {
										entryX = 0;
										++entryY;
									}
									Boolean b = exhibit.getHideLabel();
									boolean hideLabel = b == null ? gallery.getHideLabel() : b.booleanValue();
									if (!hideLabel) {
										final int tid = tagId;
										shell.getDisplay().asyncExec(() -> {
											if (!shell.isDisposed())
												generateLabel(shell.getDisplay(), exhibit, roomFolder, tid);
										});
									}
									generateExhibit(sb, wall, roomName, exhibit, tagId++, sceneSize, woff, scale, zoom,
											headHeight, hideLabel);
								}
							}
						}
					}
					monitor.worked(1);
				}
				++wallId;
			}
			for (File file : renamedFiles) {
				String fileName = file.getAbsolutePath();
				if (fileName.endsWith(TMP))
					file.renameTo(new File(fileName.substring(0, fileName.length() - TMP.length())));
			}
			if (entryGC != null) {
				entryGC.dispose();
				entryGC = null;
			}
			if (entryImage != null)
				saveImage(new File(roomFolder, "entry.jpg"), entryImage, SWT.IMAGE_JPEG, jpegQuality); //$NON-NLS-1$
			if (doorWall != null && infoPlatePosition != INFONONE)
				generateInfoPlate(sb, shell, artists, roomFolder, roomName, doorWall, scale, woff, mx, my, tagId);
		} finally {
			if (entryGC != null)
				entryGC.dispose();
			if (entryImage != null)
				entryImage.dispose();
		}
		manifest.delete();
		try {
			newsettings.save(manifest.getAbsolutePath());
		} catch (IOException e) {
			// ignore
		}
		return --tagId;
	}

	private static void reviewSequence(DialogSettings settings, String exhibitId, String imageName, File roomFolder,
			List<File> renamedFiles) {
		IDialogSettings section = settings.getSection(exhibitId);
		if (section != null) {
			String oldName = section.get("name"); //$NON-NLS-1$
			if (!imageName.equals(oldName)) {
				File existingFile = new File(roomFolder, oldName);
				if (existingFile.exists()) {
					File tempFile = new File(roomFolder, imageName + TMP);
					if (existingFile.renameTo(tempFile))
						renamedFiles.add(tempFile);
				}
			}
		}
	}

	private static void generateEmptyWall(StringBuilder sb, double x1, double y1, double z1, double x2, double y2,
			double z2, Rgb_type rgb, int zIndexOffset, int zIdx, boolean horizontal, String comment) {
		if ((x1 != x2 && y1 != y2) || (y1 != y2 && z1 != z2) || (x1 != x2 && z1 != z2)) {
			generateWallFragment(sb, x1, y1, z1, x2, y2, z2, rgb, zIndexOffset, zIdx, horizontal, comment);
			sb.append('}');
		}
	}

	private static void generateWallWithImage(StringBuilder sb, String roomName, int tagId, String image, double x1,
			double y1, double z1, double x2, double y2, double z2, Rgb_type rgb, int zindexOffset, int zIdx,
			double iangle, double dzoom, double distView, String doorColor, String comment, boolean hasImage) {
		iangle = iangle % 360;
		if (iangle > 180)
			iangle -= 360;
		generateWallFragment(sb, x1, y1, z1, x2, y2, z2, rgb, zindexOffset, zIdx, false, comment);
		if (hasImage)
			generateImageFragment(sb, roomName, tagId, image, (x1 + x2) / 2, (y1 + y2) / 2, (z1 + z2) / 2, iangle,
					dzoom, distView, doorColor);
		sb.append("\r\n\t}"); //$NON-NLS-1$
	}

	private static void generateImageFragment(StringBuilder sb, String roomName, int tagId, String image, double ix,
			double iy, double iz, double iangle, double dzoom, double distView, String doorColor) {
		NumberFormat nf1 = NumberFormat.getInstance(Locale.US);
		nf1.setMaximumFractionDigits(8);
		nf1.setGroupingUsed(false);
		sb.append(",\r\n\t\t\"image\": {\r\n\t\t\t\"tagid\":") //$NON-NLS-1$
				.append(tagId).append(",\r\n\t\t\t\"src\": \"") //$NON-NLS-1$
				.append(roomName).append('/').append(image).append("\",\r\n\t\t\t\"x\":").append(nf1.format(ix)) //$NON-NLS-1$
				.append(",\r\n\t\t\t\"y\": ").append(nf1.format(iy)) //$NON-NLS-1$
				.append(",\r\n\t\t\t\"z\": ").append(nf1.format(iz)) //$NON-NLS-1$
				.append(",\r\n\t\t\t\"angle\": ").append(nf1.format(iangle)) //$NON-NLS-1$
				.append(",\r\n\t\t\t\"distView\": ").append(nf1.format(distView)); //$NON-NLS-1$
		if (doorColor != null)
			sb.append(",\r\n\t\t\t\"door_color\": \"").append(doorColor).append('"'); //$NON-NLS-1$
		sb.append(",\r\n\t\t\t\"zoom\": ").append(dzoom) //$NON-NLS-1$
				.append("\r\n\t\t}"); //$NON-NLS-1$
	}

	private static void generateWallFragment(StringBuilder sb, double x1, double y1, double z1, double x2, double y2,
			double z2, Rgb_type rgb, int zIndexOffset, int zIdx, boolean horizontal, String comment) {
		NumberFormat nf1 = NumberFormat.getInstance(Locale.US);
		nf1.setMaximumFractionDigits(8);
		nf1.setGroupingUsed(false);
		sb.append(",\r\n\t{\r\n\t// ") //$NON-NLS-1$
				.append(comment).append("\r\n\t\"type\": \"poly\",\r\n		\"fillColor\": {\"r\": ") //$NON-NLS-1$
				.append(rgb.getR()).append(", \"g\": ") //$NON-NLS-1$
				.append(rgb.getG()).append(", \"b\": ") //$NON-NLS-1$
				.append(rgb.getB()).append("},\r\n"); //$NON-NLS-1$
		if (zIndexOffset != 0)
			sb.append("\t\t\"zIndexOffset\":").append(zIndexOffset).append(",\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
		if (zIdx != -200)
			sb.append("\t\t\"zIdx\":").append(zIdx).append(",\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
		if (horizontal)
			sb.append(NLS.bind(
					"\t\t\"x\": [{0}, {1}, {1}, {0}],\r\n\t\t\"y\": [{2}, {2}, {3}, {3}],\r\n\t\t\"z\": [{5}, {5}, {4}, {4}]", //$NON-NLS-1$
					new Object[] { nf1.format(x1), nf1.format(x2), nf1.format(y1), nf1.format(y2), nf1.format(z1),
							nf1.format(z2) }));
		else
			sb.append(NLS.bind(
					"\t\t\"x\": [{0}, {1}, {1}, {0}],\r\n\t\t\"y\": [{2}, {2}, {3}, {3}],\r\n\t\t\"z\": [{4}, {5}, {5}, {4}]", //$NON-NLS-1$
					new Object[] { nf1.format(x1), nf1.format(x2), nf1.format(y1), nf1.format(y2), nf1.format(z1),
							nf1.format(z2) }));
	}

	private static Rgb_type getWallColor(Wall wall) {
		Rgb_type bgColor = wall.getColor();
		if (bgColor == null)
			bgColor = new Rgb_typeImpl(255, 255, 250);
		return bgColor;
	}

	private void generateInfoPlate(StringBuilder sb, Shell shell, Set<String> artists, File roomFolder, String roomName,
			Wall wall, double scale, Point woff, int mx, int my, int tagId) {
		Image infoPlate = new Image(shell.getDisplay(), INFOWIDTH * 3 / 2, INFOHEIGHT * 3 / 2);
		GC gc = new GC(infoPlate);
		Rgb_type bgColor = getWallColor(wall);
		Device device = gc.getDevice();
		Rectangle bounds = infoPlate.getBounds();
		String fontFamily = gallery.getLabelFontFamily();
		int fontSize = 12;
		Font titlefont = new Font(device, fontFamily, fontSize * 4, SWT.NORMAL);
		gc.setFont(titlefont);
		TextLayout textLayout = new TextLayout(device);
		textLayout.setFont(titlefont);
		textLayout.setAlignment(SWT.CENTER);
		textLayout.setWidth(bounds.width - INFOMARGINS);
		textLayout.setText(gallery.getName());
		textLayout.draw(gc, INFOMARGINS / 2, INFOMARGINS);
		Rectangle rtx = textLayout.getBounds();
		int y = 2 * INFOMARGINS + rtx.height;
		titlefont.dispose();
		textLayout.dispose();
		if (!gallery.getHideCredits() && !artists.isEmpty()) {
			Font artistfont = new Font(device, fontFamily, fontSize * 5 / 2, SWT.NORMAL);
			gc.setFont(artistfont);
			textLayout = new TextLayout(device);
			textLayout.setFont(artistfont);
			textLayout.setWrapIndent(INFOMARGINS / 2);
			textLayout.setAlignment(SWT.CENTER);
			textLayout.setWidth(bounds.width - 2 * INFOMARGINS);
			String[] artistArray = artists.toArray(new String[artists.size()]);
			Arrays.sort(artistArray);
			textLayout.setText(Core.toStringList(artistArray, ", ")); //$NON-NLS-1$
			textLayout.draw(gc, INFOMARGINS, y);
			rtx = textLayout.getBounds();
			y += rtx.height + INFOMARGINS;
			artistfont.dispose();
			textLayout.dispose();
		}
		if (gallery.getInfo() != null && !gallery.getInfo().isEmpty()) {
			Font font = new Font(device, fontFamily, fontSize * 2, SWT.NORMAL);
			gc.setFont(font);
			textLayout = new TextLayout(device);
			textLayout.setFont(font);
			textLayout.setWrapIndent(INFOMARGINS / 2);
			textLayout.setAlignment(SWT.CENTER);
			textLayout.setWidth(bounds.width - 2 * INFOMARGINS);
			textLayout.setText(gallery.getInfo());
			textLayout.draw(gc, INFOMARGINS, y);
			font.dispose();
			textLayout.dispose();
		}
		gc.dispose();
		ImageData data = infoPlate.getImageData();
		infoPlate.dispose();
		int[] pixels = new int[data.width];
		byte[] alphas = new byte[data.width];
		for (int i = 0; i < data.height; i++) {
			data.getPixels(0, i, data.width, pixels, 0);
			for (int j = 0; j < data.width; j++)
				alphas[j] = (byte) (255 - (pixels[j] >> 8 & 0xff));
			data.setAlphas(0, i, data.width, alphas, 0);
		}
		Image outImage = new Image(device, data);
		saveImage(new File(roomFolder, "info.png"), outImage, SWT.IMAGE_PNG, -1); //$NON-NLS-1$
		outImage.dispose();
		double angle = wall.getGAngle();
		double cos = Math.cos(Math.toRadians(angle));
		double sin = Math.sin(Math.toRadians(angle));
		double iiangle = (630 - angle) % 360;
		if (iiangle > 180)
			iiangle -= 360;
		int sx = gallery.getStartX();
		int sy = gallery.getStartY();
		int infoPlatePosition = gallery.getInfoPlatePosition();
		double infoX1 = sx
				+ cos * (infoPlatePosition == INFOLEFT ? DOORWIDTH / 2 - INFOGAP - INFOWIDTH : DOORWIDTH / 2 + INFOGAP);
		double infoY1 = sy
				+ sin * (infoPlatePosition == INFOLEFT ? DOORWIDTH / 2 - INFOGAP - INFOWIDTH : DOORWIDTH / 2 + INFOGAP);
		double infoX2 = infoX1 + cos * INFOWIDTH;
		double infoY2 = infoY1 + sin * INFOWIDTH;
		double ix1 = (infoX1 - mx + woff.x) * scale;
		double ix2 = (infoX2 - mx + woff.x) * scale;
		int vHeight = gallery.getDefaultViewingHeight();
		double iy1 = (DOORHEIGHT - vHeight) * scale;
		double iy2 = (DOORHEIGHT - INFOHEIGHT - vHeight) * scale;
		double iz1 = (my - infoY1 + woff.y) * scale;
		double iz2 = (my - infoY2 + woff.y) * scale;
		double zoom = 0.65 * scale;
		double iidist = Math.sqrt(INFOHEIGHT * INFOHEIGHT + INFOWIDTH * INFOWIDTH) * zoom * 2.8d;
		generateWallWithImage(sb, roomName, tagId, "info.png", ix1, iy1, iz1, //$NON-NLS-1$
				ix2, iy2, iz2, bgColor, 0, -200, iiangle, zoom, iidist, null, "Info plate", true); //$NON-NLS-1$
	}

	private void addToEntryImage(Image image, int entryX, int entryY, int tileSize) {
		if (entryImage == null) {
			entryImage = new Image(image.getDevice(), ENTRYSIZE, ENTRYSIZE);
			entryGC = new GC(entryImage);
		}
		if (entryGC != null) {
			Rectangle bounds = image.getBounds();
			int size = Math.min(bounds.height, bounds.width);
			entryGC.drawImage(image, (bounds.width - size) / 2, (bounds.height - size) / 2, size, size,
					tileSize * entryX, tileSize * entryY, tileSize, tileSize);
		}
	}

	private static String formatWebUrl(String web) {
		if (web != null && !web.isEmpty() && !web.startsWith(HTTP) && !web.startsWith(HTTPS))
			return HTTP + web;
		return web;
	}

	protected void generateLink(String href, StringBuilder sb) {
		sb.append("<a href='"); //$NON-NLS-1$
		StringTokenizer st = new StringTokenizer(href, "/:", true); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.equals("/") || token.equals(":")) //$NON-NLS-1$ //$NON-NLS-2$
				sb.append(token);
			else
				sb.append(encodeURL(token));
		}
		sb.append("'>"); //$NON-NLS-1$
	}

	protected static String encodeURL(String s) {
		Path p = new Path(s);
		StringBuilder sb = new StringBuilder();
		for (String segment : p.segments()) {
			if (sb.length() > 0)
				sb.append('/');
			sb.append(Core.encodeUrlSegment(segment));
		}
		return sb.toString();
	}

	private void generateLabel(Device device, ExhibitImpl exhibit, File roomFolder, int tagId) {
		int margins = 144 / 4;
		int soldDiameter = margins;
		Image image = new Image(device, (int) (LABELWIDTH * 144 / 25.4), (int) (LABELHEIGHT * 144 / 25.4));
		Rectangle bounds = image.getBounds();
		String fontFamily = gallery.getLabelFontFamily();
		int fontSize = gallery.getLabelFontSize();
		Font font = new Font(device, fontFamily, fontSize * 3, SWT.NORMAL);
		GC gc = new GC(image);
		gc.setFont(font);
		String title = exhibit.getTitle();
		String description = exhibit.getDescription();
		String credits = exhibit.getCredits();
		int y = margins;
		TextLayout textLayout = new TextLayout(device);
		textLayout.setFont(font);
		textLayout.setWrapIndent(margins / 2);
		textLayout.setWidth(bounds.width - 2 * margins);
		switch (gallery.getLabelSequence()) {
		case Constants.EXHLABEL_TIT_CRED_DES:
			y += createCaption(gc, margins, bounds, fontFamily, fontSize, title, y);
			y += createTextline(gc, textLayout, margins, credits, y);
			y += createTextline(gc, textLayout, margins, description, y);
			break;
		case Constants.EXHLABEL_CRED_TIT_DES:
			y += createTextline(gc, textLayout, margins, credits, y);
			y += createCaption(gc, margins, bounds, fontFamily, fontSize, title, y);
			y += createTextline(gc, textLayout, margins, description, y);
			break;
		default:
			y += createCaption(gc, margins, bounds, fontFamily, fontSize, title, y);
			y += createTextline(gc, textLayout, margins, description, y);
			y += createTextline(gc, textLayout, margins, credits, y);
			break;
		}
		String date = exhibit.getDate();
		y += createTextline(gc, textLayout, margins, date, y);
		textLayout.dispose();
		if (exhibit.getSold()) {
			gc.setBackground(device.getSystemColor(SWT.COLOR_RED));
			gc.fillOval(bounds.width - 3 * soldDiameter / 2, soldDiameter / 2, soldDiameter, soldDiameter);
		}
		gc.dispose();
		font.dispose();
		saveImage(new File(roomFolder, tagId + "lab.png"), image, SWT.IMAGE_PNG, -1); //$NON-NLS-1$
	}

	private static int createTextline(GC gc, TextLayout textLayout, int margins, String text, int y) {
		if (text != null && !text.isEmpty()) {
			textLayout.setText(text);
			textLayout.draw(gc, margins, y);
			return textLayout.getBounds().height + margins / 2;
		}
		return 0;
	}

	private static int createCaption(GC gc, int margins, Rectangle bounds, String fontFamily, int fontSize,
			String title, int y) {
		if (title != null) {
			Font titlefont = new Font(gc.getDevice(), fontFamily, fontSize * 5, SWT.BOLD);
			Font f = gc.getFont();
			gc.setFont(titlefont);
			Point textExtent = gc.textExtent(title);
			Color bgColor = gc.getBackground();
			gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_GRAY));
			gc.fillRectangle(0, 0, bounds.width, textExtent.y + 2 * margins);
			gc.drawText(title, margins, y, true);
			gc.setBackground(bgColor);
			gc.setFont(f);
			titlefont.dispose();
			return 2 * margins + textExtent.y;
		}
		return 0;
	}

	private int getMatWidth(Exhibit exhibit) {
		Integer o = exhibit.getMatWidth();
		return (o == null) ? gallery.getMatWidth() : o;
	}

	private int getFrameWidth(Exhibit exhibit) {
		Integer o = exhibit.getFrameWidth();
		return (o == null) ? gallery.getFrameWidth() : o;
	}

	private void generateExhibit(StringBuilder sb, Wall wall, String roomName, Exhibit exhibit, int tagId,
			Point sceneSize, Point woff, double scale, double zoom, double headHeight, boolean hideLabel) {
		int mx = sceneSize.x / 2;
		int my = sceneSize.y / 2;
		Rgb_type wallColor = getWallColor(wall);
		double angle = wall.getGAngle();
		int wallX = wall.getGX();
		int wallY = wall.getGY();
		double cos = Math.cos(Math.toRadians(angle));
		double sin = Math.sin(Math.toRadians(angle));
		int vHeight = gallery.getDefaultViewingHeight();
		// int variance = gallery.getVariance();
		int matWidth = getMatWidth(exhibit);
		int frameWidth = getFrameWidth(exhibit);
		double thickness = matWidth == 0 ? 2 * frameWidth / 3 + 5 : 3 * frameWidth / 2 + 5;
		int imageH = exhibit.getHeight();
		int imageW = exhibit.getWidth();
		int imageX = exhibit.getX();
		int imageY = exhibit.getY();
		int matX = imageX - matWidth;
		int matY = imageY + matWidth;
		int matW = imageW + 2 * matWidth;
		int matH = imageH + 2 * matWidth;
		int frameX = matX - frameWidth;
		int frameY = matY + frameWidth;
		int frameW = matW + 2 * frameWidth;
		int frameH = matH + 2 * frameWidth;
		int xOffset = wallX - mx + woff.x;
		int zOffset = my - wallY + woff.y;
		// Label
		int labAlign = getLabelAlignment(exhibit);
		int labDist = getLabelDistance(exhibit);
		int labIndent = getLabelIndent(exhibit);
		int tara = frameWidth + matWidth;
		double iWidth = imageW + 2 * tara;
		double iHeight = imageH + 2 * tara;
		double labelX = imageX - tara;
		double labelY = imageY - iHeight + tara;
		// Frame
		double fx1 = (xOffset + cos * frameX - sin * thickness) * scale;
		double fy1 = (frameY - vHeight) * scale;
		double fz1 = (zOffset - sin * frameX - cos * thickness) * scale;
		double fx2 = (xOffset + cos * (frameX + frameW) - sin * thickness) * scale;
		double fy2 = (frameY - vHeight - frameH) * scale;
		double fz2 = (zOffset - sin * (frameX + frameW) - cos * thickness) * scale;
		Rgb_type matColor;
		matColor = exhibit.getMatColor();
		if (matColor == null)
			matColor = gallery.getMatColor();
		if (matColor == null)
			matColor = new Rgb_typeImpl(255, 255, 252);
		Rgb_type frameColor;
		if (frameWidth == 0)
			frameColor = matWidth == 0 ? new Rgb_typeImpl(wallColor.getR(), wallColor.getG(), wallColor.getB())
					: matColor;
		else {
			frameColor = exhibit.getFrameColor();
			if (frameColor == null)
				frameColor = gallery.getFrameColor();
			if (frameColor == null)
				frameColor = new Rgb_typeImpl(8, 8, 8);
			double sx1 = fx1 + sin * thickness * scale;
			double sx2 = fx2 + sin * thickness * scale;
			double sz1 = fz1 + cos * thickness * scale;
			double sz2 = fz2 + cos * thickness * scale;
			generateEmptyWall(sb, fx2, fy1, fz2, sx1, fy1, sz1, frameColor, -100, -200, true, "Frame " + tagId //$NON-NLS-1$
					+ " (top)"); //$NON-NLS-1$
			generateEmptyWall(sb, fx2, fy2, fz2, sx1, fy2, sz1, frameColor, -100, -200, true, "Frame " + tagId //$NON-NLS-1$
					+ " (bottom)"); //$NON-NLS-1$
			generateEmptyWall(sb, sx1, fy1, sz1, fx1, fy2, fz1, frameColor, -100, -200, false, "Frame " + tagId //$NON-NLS-1$
					+ " (left)"); //$NON-NLS-1$
			generateEmptyWall(sb, fx2, fy1, fz2, sx2, fy2, sz2, frameColor, -100, -200, false, "Frame " + tagId //$NON-NLS-1$
					+ " (right)"); //$NON-NLS-1$
		}
		generateWallFragment(sb, fx1, fy1, fz1, fx2, fy2, fz2, frameColor, -100, -200, false,
				frameWidth == 0 ? "Image mount " + tagId //$NON-NLS-1$
						: "Frame " + tagId + " (front)"); //$NON-NLS-1$ //$NON-NLS-2$
		// Image
		double ix = (xOffset + cos * (imageX + exhibit.getWidth() / 2) - sin * thickness) * scale;
		double iy = (imageY - vHeight - imageH / 2) * scale;
		double iz = (zOffset - sin * (imageX + exhibit.getWidth() / 2) - cos * thickness) * scale;
		double iangle = (630 - angle) % 360;
		if (iangle > 180)
			iangle -= 360;
		double idist = Math.sqrt(imageH * imageH + imageW * imageW) * scale * zoom * 5d;
		if (matWidth > 0 && frameWidth > 0) {
			// Mat
			double mx1 = (xOffset + cos * matX - sin * thickness) * scale;
			double my1 = (matY - vHeight) * scale;
			double mz1 = (zOffset - sin * matX - cos * thickness) * scale;
			double mx2 = (xOffset + cos * (matX + matW) - sin * thickness) * scale;
			double my2 = (matY - vHeight - matH) * scale;
			double mz2 = (zOffset - sin * (matX + matW) - cos * thickness) * scale;
			sb.append('}');
			generateWallFragment(sb, mx1, my1, mz1, mx2, my2, mz2, matColor, -200, -200, false, "Mat " + tagId); //$NON-NLS-1$
		}
		// Image
		generateImageFragment(sb, roomName, tagId, tagId + ".jpg", ix, iy, iz, iangle, //$NON-NLS-1$
				zoom, idist, null);
		sb.append("\r\n\t}"); //$NON-NLS-1$
		// Label
		switch (labAlign / 9) {
		case 0:
			labelX += iWidth + labDist;
			labelY -= labIndent - align(iHeight, labAlign, LABELHEIGHT, false);
			break;
		case 1:
			labelX += align(iWidth, labAlign, LABELWIDTH, true) + labIndent;
			labelY -= labDist;
			break;
		case 2:
			labelX -= labDist + LABELWIDTH;
			labelY -= labIndent - align(iHeight, labAlign, LABELHEIGHT, false);
			break;
		default:
			labelX += align(iWidth, labAlign, LABELWIDTH, false) + labIndent;
			labelY += labDist + iHeight + LABELHEIGHT;
			break;
		}
		// int labelX = imageX + imageW + frameWidth + matWidth + LABELXOFF;
		// int labelY = imageY - imageH - matWidth + LABELHEIGHT;
		// if (labelY < vHeight - variance || labelY > vHeight + variance)
		// labelY = Math.min(Math.max(vHeight, labelY), exhibit.getY()
		// + matWidth);
		double labx1 = (xOffset + cos * labelX) * scale;
		double laby1 = (labelY - vHeight) * scale;
		double labz1 = (zOffset - sin * labelX) * scale;
		double labx2 = (xOffset + cos * (labelX + LABELWIDTH)) * scale;
		double laby2 = (labelY - vHeight - LABELHEIGHT) * scale;
		double labz2 = (zOffset - sin * (labelX + LABELWIDTH)) * scale;
		double lidist = Math.sqrt(LABELHEIGHT * LABELHEIGHT + LABELWIDTH * LABELWIDTH) * 7d * scale * zoom;
		generateWallWithImage(sb, roomName, tagId, tagId + "lab.png", //$NON-NLS-1$
				labx1, laby1, labz1, labx2, laby2, labz2, wallColor, 0, -200, iangle, zoom * scale, lidist, null,
				"Label " + tagId, !hideLabel); //$NON-NLS-1$
		// Lights
		int lightDist = 400;
		double lx = ix - sin * lightDist * scale;
		double ly = headHeight * scale - 150;
		double lz = iz - cos * lightDist * scale;
		if (!collidesWithOtherLight(lx, lz, MINLIGHTDIST)) {
			sb.append(',');
			generateSpriteFragment(sb, roomName, "light1.png", lx, ly, lz, 1d); //$NON-NLS-1$
		}
		++tagId;
	}

	private int getLabelDistance(Exhibit exhibit) {
		Integer d = exhibit.getLabelDistance();
		if (d == null)
			d = gallery.getLabelDistance();
		return d == null ? Constants.DEFAULTLABELDISTANCE : d.intValue();
	}

	private int getLabelIndent(Exhibit exhibit) {
		Integer d = exhibit.getLabelIndent();
		if (d == null)
			d = gallery.getLabelIndent();
		return d == null ? Constants.DEFAULTLABELINDENT : d.intValue();
	}

	private int getLabelAlignment(Exhibit exhibit) {
		Integer d = exhibit.getLabelAlignment();
		if (d == null)
			d = gallery.getLabelAlignment();
		return d == null ? Constants.DEFAULTLABELALIGNMENT : d.intValue();
	}

	private static double align(double iWidth, int alignment, double lWidth, boolean reverse) {
		double labelAlign;
		switch (alignment % 3) {
		case 0:
			labelAlign = 0;
			break;
		case 1:
			labelAlign = lWidth / 2;
			break;
		default:
			labelAlign = lWidth;
			break;
		}
		switch (reverse ? 2 - (alignment / 3 % 3) : alignment / 3 % 3) {
		case 0:
			return -labelAlign;
		case 1:
			return iWidth / 2 - labelAlign;
		default:
			return iWidth - labelAlign;
		}
	}

	private boolean collidesWithOtherLight(double lx, double ly, int mindist) {
		int m2 = mindist * mindist;
		for (Point light : lights) {
			double dx = light.x - lx;
			double dy = light.y - ly;
			if (dx * dx + dy * dy < m2)
				return true;
		}
		return false;
	}

	private void generateSpriteFragment(StringBuilder sb, String roomName, String sprite, double lx, double ly,
			double lz, double zoom) {
		NumberFormat nf1 = NumberFormat.getInstance(Locale.US);
		nf1.setMaximumFractionDigits(8);
		nf1.setGroupingUsed(false);
		sb.append("{\r\n\t\t\"type\": \"sprite\",\r\n\t\t\"src\": \"") //$NON-NLS-1$
				.append(roomName).append('/').append(sprite).append("\",\r\n\t\t\"x\": ") //$NON-NLS-1$
				.append(nf1.format(lx)).append(",\r\n\t\t\"y\": ") //$NON-NLS-1$
				.append(nf1.format(ly)).append(",\r\n\t\t\"z\": ") //$NON-NLS-1$
				.append(nf1.format(lz)).append(",\r\n\t\t\"zIndexOffset\":-500,\r\n\t\t\"zoom\": ") //$NON-NLS-1$
				.append(zoom).append("\r\n\t}"); //$NON-NLS-1$
		lights.add(new Point((int) lx, (int) lz));
	}

	private static void createManifestEntry(DialogSettings settings, String exhibitId, String imageName,
			URI originalFile, int rotation, int imageSizeInPixel, String copyright, float radius, float amount,
			int threshold, boolean applySharpening) {
		IDialogSettings section = settings.addNewSection(String.valueOf(exhibitId));
		if (Constants.FILESCHEME.equals(originalFile.getScheme())) {
			long timestamp = BatchUtilities.getImageFileModificationTimestamp(new File(originalFile));
			section.put("modifiedAt", timestamp); //$NON-NLS-1$
		}
		section.put("source", originalFile.toString()); //$NON-NLS-1$
		section.put("name", imageName); //$NON-NLS-1$
		section.put("rotation", rotation); //$NON-NLS-1$
		section.put("imageSize", imageSizeInPixel); //$NON-NLS-1$
		section.put("radius", radius); //$NON-NLS-1$
		section.put("amount", amount); //$NON-NLS-1$
		section.put("threshold", threshold); //$NON-NLS-1$
		section.put("applySharpening", applySharpening); //$NON-NLS-1$
		if (copyright != null)
			section.put("copyright", copyright); //$NON-NLS-1$
	}

	private static boolean manifestEntryModified(DialogSettings settings, File imageFile, String exhibitId,
			URI originalFile, int rotation, int imageSizeInPixel, String copyright, float radius, float amount,
			int threshold, boolean applySharpening) {
		if (!imageFile.exists())
			return true;
		IDialogSettings section = settings.getSection(exhibitId);
		if (section == null)
			return true;
		if (!originalFile.toString().equals(section.get("source"))) //$NON-NLS-1$
			return true;
		if (Constants.FILESCHEME.equals(originalFile.getScheme())) {
			long timestamp = BatchUtilities.getImageFileModificationTimestamp(new File(originalFile));
			if (getLongSetting(section, "modifiedAt") != timestamp) //$NON-NLS-1$
				return true;
		}
		if (getIntSetting(section, "rotation") != rotation) //$NON-NLS-1$
			return true;
		if (getIntSetting(section, "imageSize") != imageSizeInPixel) //$NON-NLS-1$
			return true;
		if (section.getBoolean("applySharpening") != applySharpening) //$NON-NLS-1$
			return true;
		if (applySharpening) {
			if (getFloatSetting(section, "radius") != radius) //$NON-NLS-1$
				return true;
			if (getFloatSetting(section, "amount") != amount) //$NON-NLS-1$
				return true;
			if (getIntSetting(section, "threshold") != threshold) //$NON-NLS-1$
				return true;
		}
		String s = section.get("copyright"); //$NON-NLS-1$
		if (s == null)
			return copyright != null;
		return !s.equals(copyright);
	}

	private static int getIntSetting(IDialogSettings section, String key) {
		try {
			return section.getInt(key);
		} catch (Exception e) {
			return -1;
		}
	}

	private static long getLongSetting(IDialogSettings section, String key) {
		try {
			return section.getLong(key);
		} catch (Exception e) {
			return -1;
		}
	}

	private static float getFloatSetting(IDialogSettings section, String key) {
		try {
			return section.getFloat(key);
		} catch (Exception e) {
			return Float.NaN;
		}
	}

	private static Image decorateImage(Image image, String copyright) {
		Image outputImage = image;
		outputImage = ImageUtilities.addWatermark(image, copyright);
		if (outputImage != image)
			image.dispose();
		return outputImage;
	}

	private File saveImage(File imageFile, Image image, int format, int quality) {
		ImageLoader swtLoader = null;
		if (image != null) {
			swtLoader = new ImageLoader();
			if (quality > 0)
				swtLoader.compression = quality;
			swtLoader.data = new ImageData[] { image.getImageData() };
		}
		if (swtLoader != null) {
			imageFile.delete();
			try {
				imageFile.createNewFile();
				try (FileOutputStream out = new FileOutputStream(imageFile)) {
					swtLoader.save(out, format);
				}
			} catch (IOException e) {
				addError(NLS.bind(Messages.ExhibitionJob_error_creating_image_file, imageFile), e);
			}
		}
		return imageFile;
	}

	private static Point computeSceneSize(ExhibitionImpl gallery) {
		int minx = Integer.MAX_VALUE;
		int miny = Integer.MAX_VALUE;
		int maxx = Integer.MIN_VALUE;
		int maxy = Integer.MIN_VALUE;
		for (Wall wall : gallery.getWall()) {
			double angle = wall.getGAngle();
			int gx = wall.getGX();
			int gy = wall.getGY();
			int width = wall.getWidth();
			int gx2 = round(gx + Math.cos(Math.toRadians(angle)) * width);
			int gy2 = round(gy + Math.sin(Math.toRadians(angle)) * width);
			minx = Math.min(minx, Math.min(gx, gx2));
			miny = Math.min(miny, Math.min(gy, gy2));
			maxx = Math.max(maxx, Math.max(gx, gx2));
			maxy = Math.max(maxy, Math.max(gy, gy2));
		}
		int sx = gallery.getStartX();
		int sy = gallery.getStartY();
		minx = Math.min(minx, sx);
		miny = Math.min(miny, sy);
		maxx = Math.max(maxx, sx);
		maxy = Math.max(maxy, sy);
		return new Point(maxx - minx, maxy - miny);
	}

	private static String generateColor(Rgb_type color) {
		return Utilities.toHtmlColors(color.getR(), color.getG(), color.getB());
	}

	private int updateConfig(File resFile) throws FileNotFoundException, IOException {
		String start = null;
		List<String> ids = new ArrayList<String>();
		File[] rooms = resFile.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().startsWith(ROOMPREFIX);
			}
		});
		for (File room : rooms)
			ids.add(room.getName().substring(ROOMPREFIX.length()));
		File configFile = new File(resFile, "config.js"); //$NON-NLS-1$
		if (configFile.exists()) {
			StringBuffer sb = new StringBuffer(4096);
			char[] buffer = new char[4096];
			try (Reader reader = new BufferedReader(new FileReader(configFile))) {
				while (true) {
					int len = reader.read(buffer);
					sb.append(new String(buffer, 0, len));
					if (len < buffer.length)
						break;
				}
			}
			StringTokenizer st = new StringTokenizer(sb.toString(), ";\n\r"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String line = st.nextToken();
				if (line.startsWith(VAR_START) && line.endsWith("'")) //$NON-NLS-1$
					start = line.substring(VAR_START.length(), line.length() - 1).trim();
			}
			configFile.delete();
		}
		if (!ids.contains(exhibitionId))
			ids.add(exhibitionId);
		if (start == null || makeDefault)
			start = exhibitionId;
		configFile.createNewFile();
		try (Writer writer = new BufferedWriter(new FileWriter(configFile))) {
			StringBuilder sb = new StringBuilder();
			sb.append("var room_ids = ["); //$NON-NLS-1$
			for (String id : ids) {
				if (sb.length() > 20)
					sb.append(',');
				sb.append('\'').append(id).append('\'');
			}
			sb.append("];\nvar start = '").append(start).append("';\n"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write(sb.toString());
		}
		return ids.size();
	}

	protected File copyResources(File file, File rTarget, String targetName, boolean overwrite,
			IProgressMonitor aMonitor) {
		String name = targetName == null ? file.getName() : targetName;
		File out = new File(rTarget, name);
		try {
			BatchUtilities.copyFolder(file, out, overwrite, aMonitor);
		} catch (IOException e) {
			addError(NLS.bind(Messages.ExhibitionJob_io_error, name), e);
		} catch (DiskFullException e) {
			addError(NLS.bind(Messages.ExhibitionJob_disk_full_web_resource, name), null);
		}
		return out;
	}

	private void addError(String msg, Exception e) {
		status.add(new Status(IStatus.ERROR, VrActivator.PLUGIN_ID, msg, e));
	}

	public File getTargetFolder() {
		return targetFolder;
	}

	/**
	 * Returns true if job was aborted
	 *
	 * @return true if job was aborted
	 */
	public boolean wasAborted() {
		return wasAborted;
	}

	public boolean progress(int total, int worked) {
		return monitor.isCanceled();
	}

	private HtmlEncoderDecoder getHtmlEncoderDecoder() {
		if (htmlEncoderDecoder == null)
			htmlEncoderDecoder = new HtmlEncoderDecoder();
		return htmlEncoderDecoder;
	}

}