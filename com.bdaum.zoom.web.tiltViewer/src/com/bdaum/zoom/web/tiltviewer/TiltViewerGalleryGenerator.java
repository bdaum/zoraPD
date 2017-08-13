package com.bdaum.zoom.web.tiltviewer;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bdaum.zoom.cat.model.Font_type;
import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.operations.internal.gen.AbstractGalleryGenerator;
import com.bdaum.zoom.program.BatchUtilities;

@SuppressWarnings("restriction")
public class TiltViewerGalleryGenerator extends AbstractGalleryGenerator {

	private static final String IMAGESNIPPET = "<photo imageurl=\"${image}\"  showFlipButton=\"${flipButton}\">\r\n" //$NON-NLS-1$
			+ "			<title><![CDATA[${image-title}]]></title>\r\n" //$NON-NLS-1$
			+ "			<description><![CDATA[${image-description}]]></description>\r\n" //$NON-NLS-1$
			+ "		</photo>"; //$NON-NLS-1$

	private static final int[] IMAGESIZE = new int[] { 600, // medium
			850, // large
			1050, // very large
			450, // small
			300 // very small
	};

	@Override
	protected String getGeneratorId() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public boolean needsThumbnails() {
		return false;
	}

	@Override
	protected File[] getTemplates() {
		return getTemplates(Activator.getDefault().getBundle());
	}

	@Override
	protected String[] getTargetNames() {
		String pageName = getShow().getPageName();
		File[] templates = getTemplates();
		String[] names = new String[templates.length];
		for (int i = 0; i < templates.length; i++) {
			String name = templates[i].getName();
			if (name.equals("index.html") && pageName != null //$NON-NLS-1$
					&& !pageName.isEmpty()) {
				name = pageName;
			}
			names[i] = name;
		}
		return names;
	}

	@Override
	protected File[] getResourceFiles() {
		return getResourceFiles(Activator.getDefault().getBundle());
	}

	@Override
	protected Map<String, String> getSectionSnippetVars(
			StoryboardImpl storyboard, int i) {
		return null;
	}

	@Override
	protected String getSectionHeader(int i) {
		return null;
	}

	@Override
	protected String getSectionFooter() {
		return null;
	}

	@Override
	protected int getThumbnailSizeInPixel(int thumbSize) {
		return 320;
	}

	@Override
	protected int getImageSizeInPixels(int i) {
		if (i < 0 || i >= IMAGESIZE.length)
			i = 0;
		return IMAGESIZE[i];
	}

	@Override
	protected Map<String, String> getSubstitutions() {
		WebGalleryImpl show = getShow();
		Map<String, String> varmap = super.getSubstitutions();
		String pageName = show.getPageName();
		if (pageName == null)
			pageName = "index.html"; //$NON-NLS-1$
		varmap.put("pagename", pageName); //$NON-NLS-1$
		varmap.put("images", generateImageList(false, false)); //$NON-NLS-1$
		setPaddingAndMargins(show, varmap, null);
		File plate = getNameplate();
		if (plate != null)
			varmap.put("nameplatediv", generateNameplate(show, plate)); //$NON-NLS-1$
		File bgImage = getBgImage();
		if (bgImage != null)
			varmap.put("bgimage", "style=\"" + generateBg(show, bgImage) + '"'); //$NON-NLS-1$ //$NON-NLS-2$
		String f = generateFooter(
				show,
				"<a href=\"http://http://www.simpleviewer.net/\" target=\"_blank\">TiltViewer</a>"); //$NON-NLS-1$
		if (!f.isEmpty()) {
			f = applyFont(f, getShow().getFooterFont(), 7f);
			varmap.put("viewerHeight", "95"); //$NON-NLS-1$ //$NON-NLS-2$
		} else
			varmap.put("viewerHeight", "100"); //$NON-NLS-1$ //$NON-NLS-2$
		varmap.put("footer", f); //$NON-NLS-1$
		if (!show.getHideHeader()) {
			varmap.put("name", BatchUtilities.encodeHTML(show.getName(), false)); //$NON-NLS-1$
			String description = show.getDescription();
			if (description != null && !description.isEmpty())
				varmap.put(
						"description", //$NON-NLS-1$
						show.getHtmlDescription() ? description : BatchUtilities
								.encodeHTML(description, true));
		}
		varmap.put("keywords", BatchUtilities.encodeHTML( //$NON-NLS-1$
				Core.toStringList(show.getKeyword(), ", "), false)); //$NON-NLS-1$
		Date now = new Date();
		String s = df.format(now);
		s = s.substring(0, s.length() - 2) + ':' + s.substring(s.length() - 2);
		varmap.put("date", s); //$NON-NLS-1$
		nf.setMaximumFractionDigits(2);
		varmap.put("viewer", getDeployResourceFolder().getName() //$NON-NLS-1$
				+ "/swfobject.js"); //$NON-NLS-1$
		varmap.put("swf", getDeployResourceFolder().getName() //$NON-NLS-1$
				+ "/TiltViewer.swf"); //$NON-NLS-1$
		Storyboard storyboard = getShow().getStoryboard(0);
		varmap.put("maxImageSize", //$NON-NLS-1$
				String.valueOf(getImageSizeInPixels(storyboard.getImageSize())));
		setFontsAndColors(varmap, show);
		return varmap;
	}

	@Override
	protected String generateColor(String tag, Rgb_type c) {
		if (c == null)
			return ("ffffff");//$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		appendColor(c.getR(), sb);
		appendColor(c.getG(), sb);
		appendColor(c.getB(), sb);
		return sb.toString();
	}

	protected String generateFont(Font_type font, float factor) {
		StringBuilder sb = new StringBuilder();
		if (font != null) {
			if (font.getWeight() == 1 || font.getWeight() == 2)
				sb.append("<b>"); //$NON-NLS-1$
			if (font.getStyle() > 0)
				sb.append("<i>"); //$NON-NLS-1$
			sb.append("<font"); //$NON-NLS-1$
			String[] family = font.getFamily();
			if (family != null && family.length > 0) {
				sb.append(" face=\""); //$NON-NLS-1$
				for (int i = 0; i < family.length; i++) {
					if (i > 0)
						sb.append(","); //$NON-NLS-1$
					sb.append(family[i]);
				}
				sb.append('"');
			}
			int size = font.getSize();
			if (size != 100)
				sb.append(" size=\"") //$NON-NLS-1$
						.append((int) Math.max(factor / 7f,
								Math.min(factor, size / 280f * factor)))
						.append('"');
			Rgb_type color = font.getColor();
			if (color != null)
				sb.append(" color=\"#").append(generateColor(null, color)).append('"'); //$NON-NLS-1$
			sb.append('>');
		}
		return sb.toString();
	}

	private static void appendColor(int c, StringBuilder sb) {
		String hex = Integer.toHexString(c);
		if (hex.length() == 1)
			sb.append('0');
		sb.append(hex);
	}

	@Override
	protected String getImageSnippet(boolean first) {
		StringBuilder sb = new StringBuilder();
		if (!first)
			sb.append("\n"); //$NON-NLS-1$
		sb.append(IMAGESNIPPET);
		return sb.toString();
	}

	@Override
	protected Map<String, String> getImageSnippetVars(WebExhibit exhibit,
			AssetImpl asset, Storyboard storyboard, String thumbnail,
			String image, String bigImage, String original, int i) {
		Map<String, String> varmap = new HashMap<String, String>();
		varmap.put("resources", getDeployResourceFolder().getName()); //$NON-NLS-1$
		varmap.put("image", encodeURL(image)); //$NON-NLS-1$
		String titleText = ""; //$NON-NLS-1$
		if (storyboard.getShowCaptions()) {
			String caption = exhibit.getCaption();
			if (caption != null && !caption.isEmpty()) {
				titleText = BatchUtilities.encodeHTML(caption, true);
				if (!titleText.isEmpty())
					titleText = applyFont(titleText,
							getShow().getCaptionFont(), 70f);
			}
		}
		if (!titleText.isEmpty())
			varmap.put("image-title", titleText); //$NON-NLS-1$
		String html = ""; //$NON-NLS-1$
		if (storyboard.getShowDescriptions()) {
			String description = exhibit.getDescription();
			String exifdiv = getExifDiv(storyboard, exhibit, asset, "<multi>"); //$NON-NLS-1$
			if (description != null && !description.isEmpty())
				html = exhibit.getHtmlDescription() ? description : BatchUtilities
						.encodeHTML(description, true);
			if (exifdiv != null) {
				if (!html.isEmpty())
					html += "<br/><br/>"; //$NON-NLS-1$
				html += exifdiv;
			}
		}
		if (!html.isEmpty()) {
			html = applyFont(html, getShow().getDescriptionFont(), 70f);
			varmap.put("image-description", html);//$NON-NLS-1$
		}
		if (!titleText.isEmpty() || !html.isEmpty())
			varmap.put("flipButton", "true");//$NON-NLS-1$ //$NON-NLS-2$
		else
			varmap.put("flipButton", "false");//$NON-NLS-1$ //$NON-NLS-2$
		WebGalleryImpl show = getShow();
		if (exhibit.getDownloadable() && original != null
				&& !show.getHideDownload()) {
			String downloadText = show.getDownloadText();
			if (downloadText != null && !downloadText.isEmpty())
				varmap.put("downloaddiv", encodeURL(original)); //$NON-NLS-1$
		}
		varmap.put("bigimage", //$NON-NLS-1$
				(getBigImageSize() != null) ? encodeURL(bigImage)
						: encodeURL(image));
		return varmap;
	}

	private String applyFont(String text, Font_type font, float factor) {
		String fontString = generateFont(font, factor);
		if (!fontString.isEmpty()) {
			text = fontString + text + "</font>"; //$NON-NLS-1$
			if (fontString.indexOf("<i>") >= 0) //$NON-NLS-1$
				text += "</i>"; //$NON-NLS-1$
			if (fontString.indexOf("<b>") >= 0) //$NON-NLS-1$
				text += "</b>"; //$NON-NLS-1$
		}
		return text;
	}

}
