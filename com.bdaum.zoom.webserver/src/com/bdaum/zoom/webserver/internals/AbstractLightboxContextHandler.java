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
 * (c) 2019 Berthold Daum  
 */
package com.bdaum.zoom.webserver.internals;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.batch.internal.ExifTool;
import com.bdaum.zoom.batch.internal.IFileWatcher;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.core.Assetbox;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.internal.views.CatalogView;
import com.bdaum.zoom.ui.internal.views.CatalogView.CatalogComparator;
import com.bdaum.zoom.webserver.PreferenceConstants;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;

@SuppressWarnings("restriction")
public abstract class AbstractLightboxContextHandler extends AbstractContextHandler {

	private static CatalogComparator catComp = new CatalogView.CatalogComparator();

	protected static Comparator<Object> comp = new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			return catComp.compare(null, o1, o2);
		}
	};

	protected static IFileWatcher fileWatcher = CoreActivator.getDefault().getFileWatchManager();

	protected static String membersToList(List<?> members) {
		StringBuilder sb = new StringBuilder();
		if (members != null && !members.isEmpty()) {
			Collections.sort(members, comp);
			sb.append("<ul>\n"); //$NON-NLS-1$
			for (Object c : members)
				if (c instanceof IdentifiableObject) {
					sb.append("<li><a href=\"lightbox.html"); //$NON-NLS-1$
					sb.append("?targetId=") //$NON-NLS-1$
							.append(((IIdentifiableObject) c).getStringId());
					sb.append("\">"); //$NON-NLS-1$
					String name = "?"; //$NON-NLS-1$
					if (c instanceof Group)
						name = ((Group) c).getName();
					else if (c instanceof SmartCollection)
						name = ((SmartCollection) c).getName();
					sb.append(name).append("</a></li>\n"); //$NON-NLS-1$
				}
			sb.append("</ul>"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	protected String computeBreadcrumbs(IIdentifiableObject obj, String search) {
		StringBuilder sb = new StringBuilder();
		if (obj != null) {
			while (obj instanceof SmartCollection) {
				SmartCollection sm = (SmartCollection) obj;
				sb.insert(0, "</a>") //$NON-NLS-1$
						.insert(0, toHtml(sm.getName(), false)).insert(0, "\">") //$NON-NLS-1$
						.insert(0, sm.getStringId()).insert(0, " &gt; <a href=\"lightbox.html?targetId="); //$NON-NLS-1$
				obj = sm.getSmartCollection_subSelection_parent();
				if (obj == null)
					obj = Core.getCore().getDbManager().obtainById(GroupImpl.class,
							sm.getGroup_rootCollection_parent());
			}
			while (obj instanceof Group) {
				Group gr = (Group) obj;
				sb.insert(0, "</a>") //$NON-NLS-1$
						.insert(0, toHtml(gr.getName(), false)).insert(0, "\">") //$NON-NLS-1$
						.insert(0, gr.getStringId()).insert(0, " &gt; <a href=\"lightbox.html?targetId="); //$NON-NLS-1$
				obj = gr.getGroup_subgroup_parent();
			}
		} else if (search != null && !search.isEmpty()) {
			sb.insert(0, "&quot;") //$NON-NLS-1$
					.insert(0, search).insert(0, " &gt; &quot;"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	protected String keywordsToOptions(Collection<String> keywords) {
		List<String> kw = new ArrayList<String>(keywords);
		Collections.sort(kw);
		StringBuilder sb = new StringBuilder(kw.size() * 30);
		for (String w : kw)
			sb.append("<option value=\"").append(toHtml(w, false)).append("\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return sb.toString();
	}

	protected int sendThumbnail(Response resp, String assetId) throws IOException {
		AssetImpl asset = Core.getCore().getDbManager().obtainAsset(assetId);
		if (asset != null) {
			byte[] jpegThumbnail = asset.getJpegThumbnail();
			if (jpegThumbnail != null) {
				if (!ImageUtilities.testOnJpeg(jpegThumbnail)) {
					ZImage zimage = new ZImage(ImageUtilities.loadThumbnail(Display.getDefault(), jpegThumbnail,
							ImageConstants.SRGB, ImageConstants.IMAGE_WEBP, true), null);
					try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
						zimage.saveToStream(null, true, ZImage.UNCROPPED, SWT.DEFAULT, SWT.DEFAULT, out, SWT.IMAGE_JPEG,
								75);
						jpegThumbnail = out.toByteArray();
					} catch (IOException e) {
						return 500;
					} finally {
						zimage.dispose();
					}
				}
				sendBytes(resp, asset, jpegThumbnail);
				return 0;
			}
		}
		return 404;
	}

	protected Rectangle getImageBounds(Asset asset) {
		return Core.getCore().getImageCache().getImage(asset).getBounds();
	}

	protected static String sendBytes(Response resp, Asset asset, byte[] bytes) throws IOException {
		long len = bytes.length;
		long lastModified = asset.getLastModification().getTime();
		String etag = "W/\"" + lastModified + "\""; // a weak tag based on date //$NON-NLS-1$//$NON-NLS-2$
		resp.sendHeaders(200, len, lastModified, etag, "image/jpeg", null); //$NON-NLS-1$
		try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
			resp.sendBody(in, len, null);
		}
		return "image/jpeg"; //$NON-NLS-1$
	}

	protected static String createMoreLink(int pos, String collId, String search) {
		StringBuilder sb = new StringBuilder();
		sb.append("lightbox.html?pos=").append(pos); //$NON-NLS-1$
		if (collId != null)
			sb.append("&targetId=").append(collId); //$NON-NLS-1$
		if (search != null)
			sb.append("&search=").append(Core.encodeUrlSegment(search)); //$NON-NLS-1$
		return sb.toString();
	}

	protected void createPresentationSection(StringBuilder sb, Asset asset, String url, String caption, boolean first,
			boolean online) {
		sb.append("<section class=\"column"); //$NON-NLS-1$
		if (!online)
			sb.append(" offline"); //$NON-NLS-1$
		sb.append("\">\n"); //$NON-NLS-1$
		if (url != null)
			sb.append("<a target=\"_blank\" href=\"").append(url) //$NON-NLS-1$
					.append("\" rel=\"noopener\">"); //$NON-NLS-1$
		sb.append("<img class=\"thumbnail\" "); //$NON-NLS-1$
		if (first)
			sb.append("id=\"firstthumb\" "); //$NON-NLS-1$
		Rectangle bounds = getImageBounds(asset);
		sb.append("src=\"") //$NON-NLS-1$
				.append(asset == null ? "../files/nothumb.png" : asset.getStringId()) //$NON-NLS-1$
				.append(".jpg\" width=\"").append(bounds.width).append("\" height=\"").append(bounds.height) //$NON-NLS-1$//$NON-NLS-2$
				.append("\" alt=\"").append(toHtml(caption, false)).append("\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
		if (url != null)
			sb.append("</a>"); //$NON-NLS-1$
		sb.append("\n<p class=\"caption\">").append(toHtml(caption, false)); //$NON-NLS-1$
		if (!online)
			sb.append(" (offline)"); //$NON-NLS-1$
		sb.append("</p>\n</section>\n"); //$NON-NLS-1$
	}

	protected int sendImage(Asset a, Request req, Response resp, int size) {
		if (a == null)
			return 404;
		String opId = null;
		MultiStatus status = new MultiStatus(WebserverActivator.PLUGIN_ID, 0, Messages.LightboxContextHandler_report,
				null);
		List<Asset> assets = Collections.singletonList(a);
		try (Assetbox box = new Assetbox(assets, status, false)) {
			for (File file : box) {
				if (!status.isOK())
					return 500;
				Asset asset = box.getAsset();
				if (testColorProfile(asset, file) && asset.getWidth() < size * 6 / 5)
					HTTPServer.serveFileContent(file, req, resp);
				else
					sendBytes(resp, asset,
							loadImage(status, asset, file, size, opId = java.util.UUID.randomUUID().toString()));
			}
		} catch (IOException e) {
			return 500;
		} finally {
			if (opId != null)
				fileWatcher.stopIgnoring(opId);
		}
		return 0;
	}

	private byte[] loadImage(MultiStatus status, Asset asset, File file, int w, String opId) {
		ZImage zimage = null;
		if (file != null)
			try {
				Rectangle bounds = null;
				UnsharpMask umask = null;
				if (w > 0 && w != asset.getWidth()) {
					bounds = new Rectangle(0, 0, w, (int) ((double) w / asset.getWidth() * asset.getHeight()));
					umask = ImageActivator.getDefault().computeUnsharpMask(
							preferenceStore.getFloat(PreferenceConstants.RADIUS),
							preferenceStore.getFloat(PreferenceConstants.AMOUNT),
							preferenceStore.getInt(PreferenceConstants.THRESHOLD));
				}
				zimage = CoreActivator.getDefault().getHighresImageLoader().loadImage(null, status, file,
						asset.getRotation(), asset.getFocalLengthIn35MmFilm(), bounds, 1f, 1f, true,
						ImageConstants.SRGB, null, umask, null, fileWatcher, opId, null);
			} catch (UnsupportedOperationException e) {
				// do nothing
			}
		if (zimage != null) {
			int s = Math.min(w, asset.getWidth());
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				zimage.saveToStream(null, true, ZImage.CROPPED, SWT.DEFAULT, SWT.DEFAULT, out, SWT.IMAGE_JPEG,
						s > 2000 ? 35 : s > 1000 ? 55 : 75);
				return out.toByteArray();
			} catch (IOException e) {
				// should not happen
			} finally {
				zimage.dispose();
			}
		}
		return null;
	}

	protected static int parseInteger(String s, int dflt) {
		if (s != null)
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				// fall through
			}
		return dflt;
	}

	private static boolean testColorProfile(Asset asset, File workfile) {
		String ext = BatchUtilities.getTrueFileExtension(asset.getUri());
		if (ImageConstants.isJpeg(ext) || ext.equalsIgnoreCase("png")) //$NON-NLS-1$
			try (ExifTool exifTool = new ExifTool(workfile, true)) {
				ICC_Profile iccProfile = exifTool.getICCProfile();
				return iccProfile == null || iccProfile.getColorSpaceType() == ColorSpace.CS_sRGB;
			}
		return false;
	}

}