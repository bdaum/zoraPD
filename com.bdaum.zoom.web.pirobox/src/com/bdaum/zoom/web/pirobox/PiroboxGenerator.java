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
package com.bdaum.zoom.web.pirobox;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameter;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.operations.internal.gen.AbstractGalleryGenerator;
import com.bdaum.zoom.program.HtmlEncoderDecoder;

@SuppressWarnings("restriction")
public class PiroboxGenerator extends AbstractGalleryGenerator {

	private static final String IMAGESNIPPET = "<div class=\"image\"><a href=\"${image}\" class=\"${sectionclass}\" title=\"${caption}\"><img src=\"${thumbnail}\" alt=\"${alt}\"/></a></div>"; //$NON-NLS-1$

	private static final int[] IMAGESIZE = new int[] { 650, // medium
			850, // large
			1050, // very large
			520, // small
			450 // very small
	};

	private static final int[] THUMBSIZE = new int[] { 96, // medium
			128, // large
			160, // very large
			64, // small
			48 // very small
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
		if (i < 0 || i >= THUMBSIZE.length * 2)
			i = 0;
		return i >= THUMBSIZE.length ? -THUMBSIZE[i - THUMBSIZE.length] : THUMBSIZE[i];
	}

	@Override
	protected String getImageSnippet(boolean first) {
		return IMAGESNIPPET;
	}

	@Override
	protected Map<String, String> getImageSnippetVars(WebExhibit exhibit, AssetImpl asset, Storyboard storyboard,
			String thumbnail, String image, String bigImage, String original, int i) {
		Map<String, String> varmap = new HashMap<String, String>();
		varmap.put("resources", getDeployResourceFolder().getName()); //$NON-NLS-1$
		varmap.put("image", encodeURL(image)); //$NON-NLS-1$
		varmap.put("sectionclass", "pirobox_gall_" + i); //$NON-NLS-1$ //$NON-NLS-2$
		varmap.put("thumbnail", encodeURL(thumbnail)); //$NON-NLS-1$
		String altText = exhibit.getAltText();
		if (altText != null && !altText.isEmpty())
			varmap.put("alt", HtmlEncoderDecoder.getInstance().encodeHTML(altText, false)); //$NON-NLS-1$
		if (storyboard.getShowCaptions()) {
			String caption = exhibit.getCaption();
			if (caption != null && !caption.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				String c = HtmlEncoderDecoder.getInstance().encodeHTML(caption, false);
				sb.append(c);
				if (storyboard.getShowDescriptions()) {
					String description = exhibit.getDescription();
					if (description != null && !description.isEmpty()) {
						if (exhibit.getHtmlDescription())
							sb.append("<br/>").append(description); //$NON-NLS-1$
						else {
							StringTokenizer st = new StringTokenizer(description, "\n\r"); //$NON-NLS-1$
							while (st.hasMoreTokens())
								sb.append("<br/><small>").append( //$NON-NLS-1$
										HtmlEncoderDecoder.getInstance().encodeHTML(st.nextToken(), false)).append("</small>"); //$NON-NLS-1$
						}
					}
				}
				WebGalleryImpl show = getShow();
				if (exhibit.getDownloadable() && original != null && needsOriginals())
					sb.append("<div class='download'><a href='").append(encodeURL(original)) //$NON-NLS-1$
							.append("'>").append(HtmlEncoderDecoder.getInstance().encodeHTML(show.getDownloadText(), false)) //$NON-NLS-1$
							.append("</a></div>"); //$NON-NLS-1$
				String exifDiv = getExifDiv(storyboard, exhibit, asset, "meta"); //$NON-NLS-1$
				if (exifDiv != null)
					sb.append(exifDiv);
				varmap.put("caption", sb.toString()); //$NON-NLS-1$
				varmap.put("image-title", c); //$NON-NLS-1$
			}
		}
		return varmap;
	}

	@Override
	protected File[] getResourceFiles() {
		File folder = getFolder("/resources"); //$NON-NLS-1$
		if (folder == null)
			return new File[0];
		File[] files = folder.listFiles();
		File neutral = new File(folder.getParent(), "neutral"); //$NON-NLS-1$
		if (!neutral.exists())
			return files;
		File[] files2 = neutral.listFiles();
		File[] all = new File[files.length + files2.length];
		System.arraycopy(files, 0, all, 0, files.length);
		System.arraycopy(files2, 0, all, files.length, files2.length);
		return all;
	}

	private File getFolder(String name) {
		WebParameter parameter = getShow().getParameter(Activator.PLUGIN_ID + ".design"); //$NON-NLS-1$
		return getFolder(Activator.getDefault().getBundle(), name + "/" + (String) parameter.getValue()); //$NON-NLS-1$
	}

	@Override
	protected File[] getTemplates() {
		return getTemplates(Activator.getDefault().getBundle());
	}

	@Override
	protected Map<String, String> getSubstitutions() {
		Map<String, String> varmap = super.getSubstitutions();
		varmap.put("images", generateImageList(false, false)); //$NON-NLS-1$
		WebGalleryImpl show = getShow();
		int navwidth = setPaddingAndMargins(show, varmap, null);
		varmap.put("next", getParamBoolean(show.getParameter(Activator.PLUGIN_ID + ".pirobox_next")) ? "piro_next_out" //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				: "piro_next"); //$NON-NLS-1$
		varmap.put("prev", //$NON-NLS-1$
				getParamBoolean(show.getParameter(Activator.PLUGIN_ID + ".pirobox_previous")) ? "piro_prev_out" //$NON-NLS-1$//$NON-NLS-2$
						: "piro_prev"); //$NON-NLS-1$
		varmap.put("close", //$NON-NLS-1$
				getParamBoolean(show.getParameter(Activator.PLUGIN_ID + ".close_all")) ? ".piro_close,.piro_overlay" //$NON-NLS-1$//$NON-NLS-2$
						: ".piro_close"); //$NON-NLS-1$
		varmap.put("sshow", getParamBoolean(show.getParameter(Activator.PLUGIN_ID + ".slideshow")) ? "slideshow" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		lp: for (Storyboard storyboard : show.getStoryboard()) {
			for (WebExhibitImpl exhibit : Core.getCore().getDbManager().obtainByIds(WebExhibitImpl.class,
					storyboard.getExhibit())) {
				if (exhibit.getDownloadable() && show.getDownloadText() != null && !show.getDownloadText().isEmpty()
						&& !show.getHideDownload()
						|| exhibit.getCaption() != null && !exhibit.getCaption().isEmpty()
								&& storyboard.getShowCaptions()
						|| exhibit.getDescription() != null && !exhibit.getDescription().isEmpty()
								&& storyboard.getShowDescriptions()) {
					varmap.put("captiondiv", //$NON-NLS-1$
							"<div id=\"caption\" class=\"embox\"></div>"); //$NON-NLS-1$
					break lp;
				}
			}
		}
		NumberFormat nf = (NumberFormat.getNumberInstance(Locale.US));
		nf.setMaximumFractionDigits(2);
		nf.setGroupingUsed(false);
		varmap.put("opacity", nf.format(show.getOpacity() / 100d)); //$NON-NLS-1$
		varmap.put("floatThumbs", getParamString(show.getParameter(Activator.PLUGIN_ID + ".floatThumbs"), "none")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		varmap.put("jquery", getDeployResourceFolder().getName() //$NON-NLS-1$
				+ "/jquery.packed.js"); //$NON-NLS-1$
		varmap.put("pirobox", getDeployResourceFolder().getName() //$NON-NLS-1$
				+ "/piroBox.1_2_min.js"); //$NON-NLS-1$
		int bodywidth = 300;
		for (Storyboard storyboard : show.getStoryboard())
			bodywidth = Math.max(bodywidth, getImageSizeInPixels(storyboard.getImageSize()));
		varmap.put("bodywidth", String.valueOf(bodywidth)); //$NON-NLS-1$
		varmap.put("navwidth", String.valueOf(navwidth)); //$NON-NLS-1$
		setFontsAndColors(varmap);
		return varmap;
	}

	@Override
	protected String getSectionFooter() {
		return ""; //$NON-NLS-1$
	}

	@Override
	protected String getSectionHeader(int i) {
		return "<h3 class=\"section_header\">${title}</h3>${descriptiondiv}"; //$NON-NLS-1$
	}

	@Override
	protected Map<String, String> getSectionSnippetVars(StoryboardImpl storyboard, int i) {
		Map<String, String> varmap = new HashMap<String, String>();
		varmap.put("resources", getDeployResourceFolder().getName()); //$NON-NLS-1$
		varmap.put("title", HtmlEncoderDecoder.getInstance().encodeHTML(storyboard.getTitle(), true)); //$NON-NLS-1$
		String description = storyboard.getDescription();
		if (description != null && !description.isEmpty()) {
			String d = storyboard.getHtmlDescription() ? description : HtmlEncoderDecoder.getInstance().encodeHTML(description, true);
			varmap.put("descriptiondiv", //$NON-NLS-1$
					"<div id=\"description\" class=\"emboxd\">" + d + "</div>"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return varmap;
	}

}
