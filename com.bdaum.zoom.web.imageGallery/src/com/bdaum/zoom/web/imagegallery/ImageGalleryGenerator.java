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
package com.bdaum.zoom.web.imagegallery;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.operations.internal.gen.AbstractGalleryGenerator;
import com.bdaum.zoom.program.BatchUtilities;

@SuppressWarnings("restriction")
public class ImageGalleryGenerator extends AbstractGalleryGenerator {

	private static final String IMAGESNIPPET = "<image>\n" + "<date>${date}</date>\n" //$NON-NLS-1$ //$NON-NLS-2$
			+ "<title>${caption}</title>\n" //$NON-NLS-1$
			+ "<desc>${description}</desc>\n" //$NON-NLS-1$
			+ "<thumb>${thumbnail}</thumb>\n" + "<img>${image}</img>\n" //$NON-NLS-1$ //$NON-NLS-2$
			+ "</image>"; //$NON-NLS-1$
	private static final SimpleDateFormat sdf = new SimpleDateFormat("M-d-yy"); //$NON-NLS-1$
	private static final int CONTAINERPADDING = 20;

	private static final int[] IMAGESIZE = new int[] { 650, // medium
			850, // large
			1050, // very large
			520, // small
			450 // very small
	};

	@Override
	protected String getGeneratorId() {
		return Activator.PLUGIN_ID;
	}

	@Override
	protected int getImageSizeInPixels(int i) {
		if (i < 0 || i >= IMAGESIZE.length)
			i = 0;
		return IMAGESIZE[i];
	}

	@Override
	protected int getThumbnailSizeInPixel(int i) {
		return 128;
	}

	@Override
	protected String getImageSnippet(boolean first) {
		return IMAGESNIPPET;
	}

	@Override
	protected Map<String, String> getImageSnippetVars(WebExhibit exhibit,
			AssetImpl asset, Storyboard storyboard, String thumbnail,
			String image, String bigImage, String original, int i) {
		Map<String, String> varmap = new HashMap<String, String>();
		varmap.put("resources", getDeployResourceFolder().getName()); //$NON-NLS-1$
		varmap.put("image", encodeURL(image)); //$NON-NLS-1$
		varmap.put("thumbnail", encodeURL(thumbnail)); //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		if (storyboard.getShowCaptions() && exhibit.getCaption() != null
				&& !exhibit.getCaption().isEmpty()) {
			String c = exhibit.getCaption();
			sb.append(BatchUtilities.encodeXML(c, -1));
			varmap.put("caption", BatchUtilities.encodeXML(c, 1)); //$NON-NLS-1$
		} else
			varmap.put("caption", "&nbsp;"); //$NON-NLS-1$ //$NON-NLS-2$
		if (storyboard.getShowDescriptions()
				&& exhibit.getDescription() != null
				&& !exhibit.getDescription().isEmpty()) {
			if (sb.length() > 0)
				sb.append("&#13;"); //$NON-NLS-1$
			String des = exhibit.getDescription();
			if (exhibit.getHtmlDescription()) {
				StringBuilder hsb = new StringBuilder();
				BatchUtilities.decodeHTML(des, hsb);
				des = hsb.toString();
			}
			sb.append(BatchUtilities.encodeXML(des, -1));
		}
		if (sb.length() > 0)
			varmap.put("description", sb.toString()); //$NON-NLS-1$
		else
			varmap.put("description", "&nbsp;"); //$NON-NLS-1$ //$NON-NLS-2$
		Date date = asset.getDateCreated();
		if (date == null)
			date = asset.getDateTime();
		if (date != null)
			varmap.put("date", sdf.format(date)); //$NON-NLS-1$
		return varmap;
	}

	@Override
	protected File[] getResourceFiles() {
		return getResourceFiles(Activator.getDefault().getBundle());
	}

	@Override
	protected File[] getTemplates() {
		return getTemplates(Activator.getDefault().getBundle());
	}

	@Override
	protected String getSectionFooter() {
		return "</category>\n"; //$NON-NLS-1$
	}

	@Override
	protected String getSectionHeader(int i) {
		String snippet = "<category name=\"${title}\">\n"; //$NON-NLS-1$
		return snippet;
	}

	@Override
	protected Map<String, String> getSectionSnippetVars(
			StoryboardImpl storyboard, int i) {
		Map<String, String> varmap = new HashMap<String, String>();
		varmap.put("resources", getDeployResourceFolder().getName()); //$NON-NLS-1$
		varmap.put("title", BatchUtilities.encodeXML(storyboard.getTitle(), 1)); //$NON-NLS-1$
		return varmap;
	}

	@Override
	protected Map<String, String> getSubstitutions() {
		Map<String, String> varmap = super.getSubstitutions();
		varmap.put("thumbnailfolder", "thumbnails"); //$NON-NLS-1$ //$NON-NLS-2$
		varmap.put("imagefolder", getDeployImageFolder().getName()); //$NON-NLS-1$
		varmap.put("images", generateImageList(true, false)); //$NON-NLS-1$
		WebGalleryImpl show = getShow();
		File plate = getNameplate();
		if (plate != null)
			varmap.put("nameplatediv", generateNameplate(show, plate)); //$NON-NLS-1$
		File bgImage = getBgImage();
		if (bgImage != null)
			varmap.put("bgimage", generateBg(show, bgImage)); //$NON-NLS-1$
		String f = generateFooter(show, "ImageGallery"); //$NON-NLS-1$
		varmap.put("footer", f); //$NON-NLS-1$
		if (!show.getHideHeader()) {
			varmap.put("name", BatchUtilities.encodeHTML(show.getName(), false)); //$NON-NLS-1$
			String description = show.getDescription();
			if (description != null && !description.isEmpty()) {
				String d = show.getHtmlDescription() ? description
						: BatchUtilities.encodeHTML(description, true);
				varmap.put("description", d); //$NON-NLS-1$
				varmap.put(
						"descriptiondiv", //$NON-NLS-1$
						"<div id=\"description\" class=\"emboxd\">" + d + "</div>"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		varmap.put("keywords", BatchUtilities.encodeHTML( //$NON-NLS-1$
				Core.toStringList(show.getKeyword(), ", "), false)); //$NON-NLS-1$
		Date now = new Date();
		String s = df.format(now);
		s = s.substring(0, s.length() - 2) + ':' + s.substring(s.length() - 2);
		varmap.put("date", s); //$NON-NLS-1$
		varmap.put("id", getShow().getStringId()); //$NON-NLS-1$
		varmap.put("galleryswf", getDeployResourceFolder().getName() //$NON-NLS-1$
				+ "/gallery.swf"); //$NON-NLS-1$
		int thumbwidth = Math.abs(getThumbnailSizeInPixel(show.getThumbSize()));
		int navHeight = thumbwidth + CONTAINERPADDING;
		int bodywidth = 300;
		for (Storyboard storyboard : show.getStoryboard())
			bodywidth = Math.max(bodywidth,
					getImageSizeInPixels(storyboard.getImageSize()));
		int containerheight = bodywidth;
		containerheight += navHeight;
		varmap.put("bodywidth", String.valueOf(bodywidth)); //$NON-NLS-1$
		varmap.put("bodyheight", String.valueOf(containerheight)); //$NON-NLS-1$
		varmap.put("containerpadding", String.valueOf(CONTAINERPADDING)); //$NON-NLS-1$
		varmap.put("pagewidth", String //$NON-NLS-1$
				.valueOf(bodywidth + 2 * CONTAINERPADDING));
		varmap.put("pageheight", String.valueOf(containerheight)); //$NON-NLS-1$
		setFontsAndColors(varmap, show);
		return varmap;
	}

	@Override
	protected String[] getTargetNames() {
		String pageName = getShow().getPageName();
		File[] templates = getTemplates();
		String[] names = new String[templates.length];
		for (int i = 0; i < templates.length; i++) {
			String name = templates[i].getName();
			if (name.equals("gallery.html") && pageName != null //$NON-NLS-1$
					&& !pageName.isEmpty())
				name = pageName;
			names[i] = name;
		}
		return names;
	}

}
