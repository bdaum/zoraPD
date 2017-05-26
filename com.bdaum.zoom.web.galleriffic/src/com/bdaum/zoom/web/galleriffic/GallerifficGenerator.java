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

package com.bdaum.zoom.web.galleriffic;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameter;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.operations.internal.gen.AbstractGalleryGenerator;
import com.bdaum.zoom.program.BatchUtilities;

@SuppressWarnings("restriction")
public class GallerifficGenerator extends AbstractGalleryGenerator {

	private static final String IMAGESNIPPET = "<li> <a class=\"thumb\" href=\"${image}\" title=\"${title}\"> " //$NON-NLS-1$
			+ "<img src=\"${thumbnail}\" alt=\"${alt}\"> </a>" //$NON-NLS-1$
			+ "<div class=\"caption\">" //$NON-NLS-1$
			+ "${downloaddiv}" //$NON-NLS-1$
			+ "<div class=\"image-title\">${image-title}</div>" //$NON-NLS-1$
			+ "<div class=\"image-desc\">${description}</div>" //$NON-NLS-1$
			+ "${exifdiv}" + "</div></li>\n"; //$NON-NLS-1$//$NON-NLS-2$

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

	private static final int BODYPADDING = 40;

	private static final int CAPTIONHEIGHT = 50;

	private static final int EXIFHEIGHT = 15;

	@Override
	protected File[] getResourceFiles() {
		return getResourceFiles(Activator.getDefault().getBundle());
	}

	@Override
	protected File[] getTemplates() {
		return getTemplates(Activator.getDefault().getBundle());
	}

	@Override
	protected Map<String, String> getSubstitutions() {
		Map<String, String> varmap = super.getSubstitutions();
		String pageName = getShow().getPageName();
		if (pageName == null)
			pageName = "index.html"; //$NON-NLS-1$
		varmap.put("pagename", pageName); //$NON-NLS-1$
		varmap.put("images", generateImageList(false, false)); //$NON-NLS-1$
		WebGalleryImpl show = getShow();
		WebParameter webParameter = show.getParameter(Activator.PLUGIN_ID
				+ ".imagePosition"); //$NON-NLS-1$
		int navwidth = setPaddingAndMargins(show, varmap,
				webParameter == null ? null : webParameter.getValue()
						.toString());
		File plate = getNameplate();
		if (plate != null)
			varmap.put("nameplatediv", generateNameplate(show, plate)); //$NON-NLS-1$
		File bgImage = getBgImage();
		if (bgImage != null)
			varmap.put("bgimage", generateBg(show, bgImage)); //$NON-NLS-1$
		String f = generateFooter(
				show,
				"<a href=\"http://www.twospy.com/galleriffic/\" target=\"_blank\">Galleriffic</a>"); //$NON-NLS-1$
		varmap.put("footer", f); //$NON-NLS-1$
		if (!show.getHideHeader()) {
			varmap.put("name", BatchUtilities.encodeHTML(show.getName(), false)); //$NON-NLS-1$
			String description = show.getDescription();
			if (description != null && description.length() > 0) {
				String d = show.getHtmlDescription() ? description
						: BatchUtilities.encodeHTML(description, true);
				varmap.put("description", d); //$NON-NLS-1$
				varmap.put("descriptiondiv", //$NON-NLS-1$
						"<div id=\"description\" class=\"description-container\">" //$NON-NLS-1$
								+ d + "</div>"); //$NON-NLS-1$
			}
		}
		int captionheight = 0;
		lp: for (Storyboard storyboard : show.getStoryboard()) {
			for (WebExhibitImpl exhibit : Core.getCore().getDbManager().obtainByIds(
				WebExhibitImpl.class, storyboard.getExhibit())) {
				if (exhibit.getDownloadable()
						&& show.getDownloadText() != null
						&& show.getDownloadText().length() > 0
						&& !show.getHideDownload()
						|| exhibit.getCaption() != null
						&& exhibit.getCaption().length() > 0
						&& storyboard.getShowCaptions()
						|| exhibit.getDescription() != null
						&& exhibit.getDescription().length() > 0
						&& storyboard.getShowDescriptions()) {
					varmap.put("captiondiv", //$NON-NLS-1$
							"<div id=\"caption\" class=\"caption-container\"></div>"); //$NON-NLS-1$
					String hideCaptionText = Messages.GallerifficGenerator_hide_caption;
					WebParameter parm = show
							.getParameter(Activator.PLUGIN_ID
									+ ".hideCaptionText"); //$NON-NLS-1$
					if (parm != null) {
						Object value = parm.getValue();
						if (value != null)
							hideCaptionText = value.toString();
					}
					varmap.put(
							"captiontogglediv", //$NON-NLS-1$
							"<div id=\"captionToggle\">" //$NON-NLS-1$
									+ "<a href=\"#toggleCaption\" class=\"on\" title=\"" //$NON-NLS-1$
									+ hideCaptionText + "\">" //$NON-NLS-1$
									+ hideCaptionText + "</a>" //$NON-NLS-1$
									+ "</div>"); //$NON-NLS-1$
					captionheight += storyboard.getShowExif() ? CAPTIONHEIGHT
							+ (computeExifCount() + 1 / 2) * EXIFHEIGHT
							: CAPTIONHEIGHT;
					break lp;
				}
			}
		}

		varmap.put("keywords", BatchUtilities.encodeHTML( //$NON-NLS-1$
				Core.toStringList(show.getKeyword(), ", "), false)); //$NON-NLS-1$
		Date now = new Date();
		String s = df.format(now);
		s = s.substring(0, s.length() - 2) + ':' + s.substring(s.length() - 2);
		varmap.put("date", s); //$NON-NLS-1$
		nf.setMaximumFractionDigits(2);
		varmap.put("opacity", nf.format(show.getOpacity() / 100d)); //$NON-NLS-1$
		varmap.put("jquery", getDeployResourceFolder().getName() //$NON-NLS-1$
				+ "/jquery.packed.js"); //$NON-NLS-1$
		varmap.put("jqueryhistory", getDeployResourceFolder().getName() //$NON-NLS-1$
				+ "/jquery.history.packed.js"); //$NON-NLS-1$
		varmap.put("galleriffic", getDeployResourceFolder().getName() //$NON-NLS-1$
				+ "/jquery.galleriffic.packed.js"); //$NON-NLS-1$
		WebParameter delay = show.getParameter(Activator.PLUGIN_ID + ".delay"); //$NON-NLS-1$
		int v = 3000;
		if (delay != null) {
			try {
				v = (int) (1000 * Double.parseDouble(delay.getValue()
						.toString()));
			} catch (NumberFormatException e) {
				// do nothing
			}
		}
		varmap.put("delayms", String.valueOf(v)); //$NON-NLS-1$
		WebParameter transition = show.getParameter(Activator.PLUGIN_ID
				+ ".transition"); //$NON-NLS-1$
		v = 900;
		if (transition != null) {
			try {
				v = (int) (1000 * Double.parseDouble(transition.getValue()
						.toString()));
			} catch (NumberFormatException e) {
				// do nothing
			}
		}
		varmap.put("transitionms", String.valueOf(v)); //$NON-NLS-1$
		int bodywidth = 300;
		StringBuilder options = new StringBuilder();
		int i = 0;
		int ic = 0;
		for (Storyboard storyboard : show.getStoryboard()) {
			bodywidth = Math.max(bodywidth,
					getImageSizeInPixels(storyboard.getImageSize()));
			options.append("$('#gallery" + (++i) + "').galleriffic('#").append( //$NON-NLS-1$ //$NON-NLS-2$
					storyboard.toString())
					.append("', galleryOptions);").append('\n'); //$NON-NLS-1$
			ic += storyboard.getExhibit().size();
		}
		int tpp = 20;
		WebParameter thumbsPerPage = show.getParameter(Activator.PLUGIN_ID
				+ ".imagePosition"); //$NON-NLS-1$
		if (thumbsPerPage != null)
			try {
				tpp = Integer.parseInt(thumbsPerPage.getValue().toString());
			} catch (NumberFormatException e) {
				// do nothing
			}
		int maxPages = (ic + tpp - 1) / tpp;
		varmap.put("maxPages", String.valueOf(maxPages)); //$NON-NLS-1$
		varmap.put("setoptions", options.toString()); //$NON-NLS-1$
		int containerwidth = bodywidth + BODYPADDING;
		if (webParameter != null && "top".equals(webParameter.getValue())) //$NON-NLS-1$
			navwidth = bodywidth;
		else
			containerwidth += navwidth;
		int bodyheight = bodywidth + captionheight;
		varmap.put("bodywidth", String.valueOf(bodywidth)); //$NON-NLS-1$
		varmap.put("bodyheight", String.valueOf(bodyheight)); //$NON-NLS-1$
		varmap.put("navwidth", String.valueOf(navwidth)); //$NON-NLS-1$
		varmap.put("pagewidth", String.valueOf(containerwidth)); //$NON-NLS-1$
		setFontsAndColors(varmap, show);
		return varmap;
	}

	@Override
	protected Map<String, String> getImageSnippetVars(WebExhibit exhibit,
			AssetImpl asset, Storyboard storyboard, String thumbnail,
			String image, String bigImage, String original, int i) {
		Map<String, String> varmap = new HashMap<String, String>();
		varmap.put("resources", getDeployResourceFolder().getName()); //$NON-NLS-1$
		varmap.put("image", encodeURL(image)); //$NON-NLS-1$
		varmap.put("thumbnail", encodeURL(thumbnail)); //$NON-NLS-1$
		String altText = exhibit.getAltText();
		if (altText != null && altText.length() > 0)
			varmap.put("alt", BatchUtilities.encodeHTML(altText, false)); //$NON-NLS-1$
		if (storyboard.getShowCaptions()) {
			String caption = exhibit.getCaption();
			if (caption != null && caption.length() > 0) {
				String c = BatchUtilities.encodeHTML(caption, true);
				varmap.put("title", c); //$NON-NLS-1$
				varmap.put("image-title", c); //$NON-NLS-1$
			}
		}
		if (storyboard.getShowDescriptions()) {
			String description = exhibit.getDescription();
			if (description != null && description.length() > 0)
				varmap.put(
						"description", exhibit.getHtmlDescription() ? description : BatchUtilities.encodeHTML(description, true)); //$NON-NLS-1$
		}
		WebGalleryImpl show = getShow();
		if (exhibit.getDownloadable() && original != null
				&& !show.getHideDownload()) {
			String downloadText = show.getDownloadText();
			if (downloadText != null && downloadText.length() > 0)
				varmap.put("downloaddiv", "<div class=\"download\"> <a href=\"" //$NON-NLS-1$ //$NON-NLS-2$
						+ encodeURL(original)
						+ "\">" //$NON-NLS-1$
						+ BatchUtilities.encodeHTML(downloadText, false)
						+ "</a> </div>"); //$NON-NLS-1$
		}
		String exifdiv = getExifDiv(storyboard, exhibit, asset, "meta"); //$NON-NLS-1$
		if (exifdiv != null)
			varmap.put("exifdiv", exifdiv); //$NON-NLS-1$
		return varmap;
	}

	@Override
	protected String getImageSnippet(boolean first) {
		return IMAGESNIPPET;
	}

	@Override
	protected String getGeneratorId() {
		return Activator.PLUGIN_ID;
	}

	@Override
	protected String[] getTargetNames() {
		String pageName = getShow().getPageName();
		File[] templates = getTemplates();
		String[] names = new String[templates.length];
		for (int i = 0; i < templates.length; i++) {
			String name = templates[i].getName();
			if (name.equals("index.html") && pageName != null //$NON-NLS-1$
					&& pageName.length() > 0) {
				name = pageName;
			}
			names[i] = name;
		}
		return names;
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
		return i >= THUMBSIZE.length ? -THUMBSIZE[i - THUMBSIZE.length]
				: THUMBSIZE[i];
	}

	@Override
	protected String getSectionFooter() {
		return null;
	}

	@Override
	protected String getSectionHeader(int i) {
		return null;
	}

	@Override
	protected Map<String, String> getSectionSnippetVars(
			StoryboardImpl storyboard, int i) {
		return null;
	}

}
