package com.bdaum.zoom.web.skitter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Rectangle;

import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.Rgb_typeImpl;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameter;
import com.bdaum.zoom.operations.internal.gen.AbstractGalleryGenerator;
import com.bdaum.zoom.program.HtmlEncoderDecoder;

@SuppressWarnings("restriction")
public class SkitterGalleryGenerator extends AbstractGalleryGenerator {

	private static final String IMAGESNIPPET = "<li><a href=\"#${href}\"><img src=\"${image}\" class=\"${class}\"/></a>${caption}</li>"; //$NON-NLS-1$

	private static final int[] MEDIUMIMAGESIZE = new int[] { 640, // medium
			832, // large
			1024, // very large
			512, // small
			420 // very small
	};

	private Map<String, String> imageVarmap = new HashMap<String, String>();

	private boolean fullScreen;

	@Override
	protected File[] getTemplates() {
		return getTemplates(Activator.getDefault().getBundle());
	}

	@Override
	protected File[] getResourceFiles() {
		return getResourceFiles(Activator.getDefault().getBundle());
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
		return 0;
	}

	@Override
	protected int getImageSizeInPixels(int i) {
		if (fullScreen)
			return 1920;
		if (i < 0 || i >= MEDIUMIMAGESIZE.length)
			i = 0;
		return MEDIUMIMAGESIZE[i];
	}

	@Override
	protected String getGeneratorId() {
		return Activator.PLUGIN_ID;
	}

	@Override
	protected String getImageSnippet(boolean first) {
		return IMAGESNIPPET;
	}

	@Override
	protected Map<String, String> getSectionSnippetVars(StoryboardImpl storyboard, int storyBoardNo) {
		return null;
	}

	@Override
	protected Map<String, String> getImageSnippetVars(WebExhibit exhibit, AssetImpl asset, Storyboard storyboard,
			String thumbnail, String image, String big, String original, int storyBoardNo) {
		WebGalleryImpl show = getShow();
		imageVarmap.put("resources", getDeployResourceFolder().getName()); //$NON-NLS-1$
		imageVarmap.put("image", encodeURL(image)); //$NON-NLS-1$
		boolean downloadable = original != null && exhibit.getDownloadable() && needsOriginals();
		boolean hasCaption = storyboard.getShowCaptions() && exhibit.getCaption() != null
				&& !exhibit.getCaption().isEmpty();
		StringBuilder sb = new StringBuilder();
		if ((hasCaption || downloadable) && !"none".equals(getParamString(show, "labels", "slideUp"))) {//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
			sb.append("<div class=\"label_text\"><p>"); //$NON-NLS-1$
			if (hasCaption)
				sb.append(HtmlEncoderDecoder.getInstance().encodeHTML(exhibit.getCaption(), true));
			if (downloadable) {
				if (hasCaption)
					sb.append("&nbsp;&nbsp;&nbsp;"); //$NON-NLS-1$
				sb.append("<a href=\"").append(encodeURL(original)).append("\" class=\"btn btn-small btn-download\">") //$NON-NLS-1$ //$NON-NLS-2$
						.append(HtmlEncoderDecoder.getInstance().encodeHTML(show.getDownloadText(), true)).append("</a>"); //$NON-NLS-1$
			}
			sb.append("</p></div>"); //$NON-NLS-1$
		}
		imageVarmap.put("caption", sb.toString()); //$NON-NLS-1$
		String animation = getParamString(show, "animation", "fade"); //$NON-NLS-1$//$NON-NLS-2$
		imageVarmap.put("href", animation); //$NON-NLS-1$
		imageVarmap.put("class", animation); //$NON-NLS-1$
		return imageVarmap;
	}

	@Override
	protected Map<String, String> getSubstitutions() {
		WebGalleryImpl show = getShow();
		Map<String, String> varmap = super.getSubstitutions();
		setPaddingAndMargins(show, varmap, null);
		fullScreen = Boolean.parseBoolean(getParamString(show, "fullscreen", "false")); //$NON-NLS-1$ //$NON-NLS-2$
		if (fullScreen) {
			WebParameter parameter = show.getParameter(Activator.PLUGIN_ID + ".navControls"); //$NON-NLS-1$
			if (parameter != null)
				parameter.setValue("false"); //$NON-NLS-1$
			parameter = show.getParameter(Activator.PLUGIN_ID + ".focus"); //$NON-NLS-1$
			if (parameter != null)
				parameter.setValue("false"); //$NON-NLS-1$
		}
		varmap.put("images", generateImageList(false, false)); //$NON-NLS-1$
		if (maxImageWidthInSection > 0)
			varmap.put("maxWidth", String.valueOf(maxImageWidthInSection)); //$NON-NLS-1$
		int msec = (int) (1000d * getParamDouble(show.getParameter(Activator.PLUGIN_ID + ".autoplay"), 5d)); //$NON-NLS-1$
		varmap.put("interval", String.valueOf(msec)); //$NON-NLS-1$
		varmap.put("autoPlay", String.valueOf(msec > 0)); //$NON-NLS-1$
		varmap.put("hideNavTools", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		String nav = getParamString(show, "navigation", "dots"); //$NON-NLS-1$ //$NON-NLS-2$
		if ("numbers".equals(nav)) { //$NON-NLS-1$
			varmap.put("navStyle", "numbers: true,\n\t\tthumbs: false,\n\t\tdots: false"); //$NON-NLS-1$ //$NON-NLS-2$
			varmap.put("hideNavTools", getParamString(show, "hideTools", "false")); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		} else if ("thumbs".equals(nav)) { //$NON-NLS-1$
			varmap.put("navStyle", "thumbs: true,\n\t\tnumbers: false,\n\t\tdots: false"); //$NON-NLS-1$ //$NON-NLS-2$
			varmap.put("navClass", "with-thumbs"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			varmap.put("navStyle", //$NON-NLS-1$
					"dotsPreview".equals(nav) //$NON-NLS-1$
							? "dots: true,\n\t\tnumbers: false,\n\t\tthumbs: false,\n\t\tpreview: true" //$NON-NLS-1$
							: "dots: true,\nnumbers: false,\nthumbs: false,\npreview: false"); //$NON-NLS-1$
			varmap.put("navClass", "with-dots"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		String labels = getParamString(show, "labels", "slideUp"); //$NON-NLS-1$ //$NON-NLS-2$
		if ("none".equals(labels)) //$NON-NLS-1$
			varmap.put("labelStyle", "label: false"); //$NON-NLS-1$ //$NON-NLS-2$
		else
			varmap.put("labelStyle", NLS.bind("label: true,\n\t\t\tlabel_animation: '{0}'", labels)); //$NON-NLS-1$ //$NON-NLS-2$
		String theme = getParamString(show, "theme", "default"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!"default".equals(theme)) //$NON-NLS-1$
			varmap.put("themeEntry", NLS.bind("theme: '{0}',", theme)); //$NON-NLS-1$ //$NON-NLS-2$
		setFontsAndColors(varmap);
		Rgb_type linkColor = show.getLinkColor();
		varmap.put("downloadColor", generateColor(linkColor)); //$NON-NLS-1$
		Rgb_type hoverColor = new Rgb_typeImpl((linkColor.getR() + 255) / 2, (linkColor.getG() + 255) / 2,
				(linkColor.getB() + 255) / 2);
		varmap.put("hoverColor", generateColor(hoverColor)); //$NON-NLS-1$
		return varmap;
	}

	private String getParamString(WebGalleryImpl show, String key, String dflt) {
		return getParamString(show.getParameter(Activator.PLUGIN_ID + '.' + key), dflt);
	}

	protected Rectangle computeImageBounds(int isize) {
		return new Rectangle(0, 0, isize, -isize);
	}

	public boolean needsThumbnails() {
		return false;
	}

}
