package com.bdaum.zoom.web.simpleviewer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.bdaum.zoom.cat.model.Font_type;
import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameter;
import com.bdaum.zoom.operations.internal.gen.AbstractGalleryGenerator;
import com.bdaum.zoom.program.BatchUtilities;

@SuppressWarnings("restriction")
public class SimpleViewerGenerator extends AbstractGalleryGenerator {

	private static final String IMAGESNIPPET = "<image imageURL=\"${image}\" thumbURL=\"${thumbnail}\">\r\n" //$NON-NLS-1$
			+ "	<caption>${image-title}</caption></image>"; //$NON-NLS-1$

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
	protected File[] getTemplates() {
		return getTemplates(Activator.getDefault().getBundle());
	}

	@Override
	protected File[] getResourceFiles() {
		return getResourceFiles(Activator.getDefault().getBundle());
	}

	@Override
	protected Map<String, String> getSectionSnippetVars(StoryboardImpl storyboard, int i) {
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
		WebParameter parm = show.getParameter().get(Activator.PLUGIN_ID + ".theme"); //$NON-NLS-1$
		varmap.put("images", //$NON-NLS-1$
				generateImageList(false, parm != null && "COMPACT".equals(parm.getValue()))); //$NON-NLS-1$
		setPaddingAndMargins(show, varmap, null);
		varmap.put("viewerHeight", varmap.get("footer").isEmpty() ? "100" : "85"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		varmap.put("simpleviewer", getDeployResourceFolder().getName() //$NON-NLS-1$
				+ "/svcore/js/simpleviewer.js"); //$NON-NLS-1$
		varmap.put("maxImageSize", //$NON-NLS-1$
				String.valueOf(getImageSizeInPixels(getShow().getStoryboard(0).getImageSize())));
		setFontsAndColors(varmap);
		varmap.put("bordercolor", generateColor(null, show //$NON-NLS-1$
				.getBorderColor()));
		varmap.put("shadecolor", generateColor(null, show //$NON-NLS-1$
				.getShadeColor()));
		varmap.put("textColor", generateColor(null, show.getTitleFont().getColor())); //$NON-NLS-1$
		if (getParamBoolean(show.getParameter().get(Activator.PLUGIN_ID + ".transparentBackground"))) //$NON-NLS-1$
			varmap.put("backgroundcolor", "transparent"); //$NON-NLS-1$ //$NON-NLS-2$
		return varmap;
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
	protected Map<String, String> getImageSnippetVars(WebExhibit exhibit, AssetImpl asset, Storyboard storyboard,
			String thumbnail, String image, String bigImage, String original, int i) {
		Map<String, String> varmap = new HashMap<String, String>();
		varmap.put("resources", getDeployResourceFolder().getName()); //$NON-NLS-1$
		varmap.put("image", encodeURL(image)); //$NON-NLS-1$
		varmap.put("thumbnail", encodeURL(thumbnail)); //$NON-NLS-1$
		String text = ""; //$NON-NLS-1$
		if (storyboard.getShowCaptions()) {
			String caption = exhibit.getCaption();
			if (caption != null && !caption.isEmpty())
				text = applyFont(caption, getShow().getCaptionFont(), 35f);
		}
		if (storyboard.getShowDescriptions()) {
			String xml = ""; //$NON-NLS-1$
			String description = exhibit.getDescription();
			String exifdiv = getExifDiv(storyboard, exhibit, asset, "<single>"); //$NON-NLS-1$
			if (description != null && !description.isEmpty())
				xml = exhibit.getHtmlDescription() ? BatchUtilities.decodeHTML(description) : description;
			if (exifdiv != null) {
				if (!xml.isEmpty())
					xml += "\n\n"; //$NON-NLS-1$
				xml += exifdiv;
			}
			if (!xml.isEmpty())
				xml = applyFont(xml, getShow().getDescriptionFont(), 35f);
			if (!text.isEmpty() && !xml.isEmpty())
				text += '\n';
			text += xml;
		}
		if (!text.isEmpty())
			varmap.put("image-title", "<![CDATA[" + text + "]]>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (exhibit.getDownloadable() && original != null && needsOriginals())
			varmap.put("downloaddiv", encodeURL(original)); //$NON-NLS-1$
		varmap.put("bigimage", //$NON-NLS-1$
				(getBigImageSize() != null) ? encodeURL(bigImage) : encodeURL(image));
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
	
	private String generateFont(Font_type font, float factor) {
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
						.append((int) Math.max(factor / 7f, Math.min(factor, size / 280f * factor))).append('"');
			Rgb_type color = font.getColor();
			if (color != null)
				sb.append(" color=\"#").append(generateColor(null, color)).append('"'); //$NON-NLS-1$
			sb.append('>');
		}
		return sb.toString();
	}


}
