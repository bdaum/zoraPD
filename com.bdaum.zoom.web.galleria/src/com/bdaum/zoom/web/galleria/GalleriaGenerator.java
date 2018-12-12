package com.bdaum.zoom.web.galleria;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.swt.graphics.Rectangle;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameter;
import com.bdaum.zoom.operations.internal.gen.AbstractGalleryGenerator;

@SuppressWarnings("restriction")
public class GalleriaGenerator extends AbstractGalleryGenerator {

	private static final String CLASSICTHEME = "classic"; //$NON-NLS-1$

	private static final String IMAGESNIPPET = "{image: '${image}', thumb: '${thumbnail}', big: '${bigimage}', ${downloaddiv}title: '${image-title}',description: '${description}'}"; //$NON-NLS-1$

	private static final int[] IMAGESIZE = new int[] { 600, // medium
			850, // large
			1050, // very large
			450, // small
			300 // very small
	};

	private static final int BODYPADDING = 40;
	private static final int CAPTIONHEIGHT = 50;
	private static final Rectangle MAXBIGIMAGESIZE = new Rectangle(0, 0, 1600, 1200);

	private Map<String, String> imageVarmap = new HashMap<String, String>();

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
		varmap.put("images", generateImageList(false, false)); //$NON-NLS-1$
		WebGalleryImpl show = getShow();
		WebParameter webParameter = show.getParameter(Activator.PLUGIN_ID + ".imagePosition"); //$NON-NLS-1$
		int navwidth = setPaddingAndMargins(show, varmap,
				webParameter == null ? null : webParameter.getValue().toString());
		varmap.put("interval", String //$NON-NLS-1$
				.valueOf((int) (1000d * getParamDouble(show.getParameter(Activator.PLUGIN_ID + ".autoplay"), 0d)))); //$NON-NLS-1$
		NumberFormat nf = (NumberFormat.getNumberInstance(Locale.US));
		nf.setMaximumFractionDigits(2);
		varmap.put("opacity", nf.format(show.getOpacity() / 100d)); //$NON-NLS-1$
		varmap.put("jquery", getDeployResourceFolder().getName() //$NON-NLS-1$
				+ "/jquery-2.2.4.min.js"); //$NON-NLS-1$
		varmap.put("galleria", getDeployResourceFolder().getName() //$NON-NLS-1$
				+ "/galleria-1.5.7.min.js"); //$NON-NLS-1$
		String theme = getTheme();
		varmap.put("galleriaTheme", getThemeFolder().getName() //$NON-NLS-1$
				+ '/' + theme + "/galleria." + theme + ".min.js"); //$NON-NLS-1$ //$NON-NLS-2$
		varmap.put("galleriaThemeCSS", getThemeFolder().getName() //$NON-NLS-1$
				+ '/' + theme + "/galleria." + theme + ".min.css"); //$NON-NLS-1$ //$NON-NLS-2$
		WebParameter delay = show.getParameter(Activator.PLUGIN_ID + ".delay"); //$NON-NLS-1$
		varmap.put("delayms", String.valueOf((int) (1000 * getParamDouble(delay, 3d)))); //$NON-NLS-1$
		WebParameter transition = show.getParameter(Activator.PLUGIN_ID + ".transition"); //$NON-NLS-1$
		varmap.put("transitionms", String.valueOf((int) (1000 * getParamDouble(transition, 0.9d)))); //$NON-NLS-1$
		int bodywidth = 300;
		StringBuilder options = new StringBuilder();
		int i = 0;
		int ic = 0;
		for (Storyboard storyboard : show.getStoryboard()) {
			bodywidth = Math.max(bodywidth, getImageSizeInPixels(storyboard.getImageSize()));
			options.append("$('#gallery" + (++i) + "').galleriffic('#").append( //$NON-NLS-1$ //$NON-NLS-2$
					storyboard.toString()).append("', galleryOptions);").append('\n'); //$NON-NLS-1$
			ic += storyboard.getExhibit().size();
		}
		int tpp = getParamInt(show.getParameter(Activator.PLUGIN_ID + ".imagePosition"), 20); //$NON-NLS-1$
		varmap.put("maxPages", String.valueOf((ic + tpp - 1) / tpp)); //$NON-NLS-1$
		varmap.put("setoptions", options.toString()); //$NON-NLS-1$
		int containerwidth = bodywidth + BODYPADDING;
		if (webParameter != null && "top".equals(webParameter.getValue())) //$NON-NLS-1$
			navwidth = bodywidth;
		else
			containerwidth += navwidth;
		int bodyheight = bodywidth + CAPTIONHEIGHT;//
		varmap.put("bodywidth", String.valueOf(bodywidth)); //$NON-NLS-1$
		varmap.put("bodyheight", String.valueOf(bodyheight)); //$NON-NLS-1$
		varmap.put("navwidth", String.valueOf(navwidth)); //$NON-NLS-1$
		varmap.put("pagewidth", String.valueOf(containerwidth)); //$NON-NLS-1$
		int galleriaHeight = bodywidth + Math.abs(getThumbnailSizeInPixel(show.getThumbSize()));
		varmap.put("galleriaHeight", String.valueOf(galleriaHeight)); //$NON-NLS-1$
		varmap.put("galleriaWidth", String.valueOf(galleriaHeight * 2)); //$NON-NLS-1$
		WebParameter tOptions = getShow().getParameter(Activator.PLUGIN_ID + ".themeOptions"); //$NON-NLS-1$
		StringBuilder themeOptions = new StringBuilder();
		if (tOptions != null) {
			WebParameter targetParm = getShow().getParameter(Activator.PLUGIN_ID + '.' + tOptions.getLinkTo());
			String selector = (String) targetParm.getValue() + ':';
			StringTokenizer st1 = new StringTokenizer(tOptions.getValue().toString(), "\f"); //$NON-NLS-1$
			while (st1.hasMoreTokens()) {
				String token = st1.nextToken();
				if (token.startsWith(selector)) {
					StringTokenizer st = new StringTokenizer(token.substring(selector.length()), ","); //$NON-NLS-1$
					while (st.hasMoreTokens())
						themeOptions.append(st.nextToken().trim()).append(",\n"); //$NON-NLS-1$
					break;
				}
			}
		}
		varmap.put("options", themeOptions.toString()); //$NON-NLS-1$
		setFontsAndColors(varmap);
		return varmap;
	}

	@Override
	protected File[] getThemeFiles() {
		return getFolderChildren(Activator.getDefault().getBundle(), "themes"); //$NON-NLS-1$
	}

	@Override
	protected Map<String, String> getImageSnippetVars(WebExhibit exhibit, AssetImpl asset, Storyboard storyboard,
			String thumbnail, String image, String bigImage, String original, int i) {
		imageVarmap.put("resources", getDeployResourceFolder().getName()); //$NON-NLS-1$
		imageVarmap.put("image", encodeURL(image)); //$NON-NLS-1$
		imageVarmap.put("thumbnail", encodeURL(thumbnail)); //$NON-NLS-1$
		String altText = exhibit.getAltText();
		if (altText != null && !altText.isEmpty())
			imageVarmap.put("alt", getHtmlEncoderDecoder().encodeHTML(altText, false)); //$NON-NLS-1$
		if (storyboard.getShowCaptions()) {
			String caption = exhibit.getCaption();
			if (caption != null && !caption.isEmpty()) {
				String c = getHtmlEncoderDecoder().encodeHTML(caption, true);
				imageVarmap.put("title", c); //$NON-NLS-1$
				imageVarmap.put("image-title", c); //$NON-NLS-1$
			}
		}
		if (storyboard.getShowDescriptions()) {
			String description = exhibit.getDescription();
			String exifdiv = getExifDiv(storyboard, exhibit, asset, null);
			if ((description != null && !description.isEmpty()) || exifdiv != null) {
				String html = description != null
						? exhibit.getHtmlDescription() ? description : getHtmlEncoderDecoder().encodeHTML(description, true)
						: ""; //$NON-NLS-1$
				if (exifdiv != null) {
					if (!html.isEmpty())
						html += "<br/>"; //$NON-NLS-1$
					html += exifdiv;
				}
				imageVarmap.put("description", html);//$NON-NLS-1$
			}
		}
		if (exhibit.getDownloadable() && original != null && needsOriginals())
			imageVarmap.put("downloaddiv", "link: '" //$NON-NLS-1$ //$NON-NLS-2$
					+ encodeURL(original) + "',"); //$NON-NLS-1$
		imageVarmap.put("bigimage", //$NON-NLS-1$
				(getBigImageSize() != null) ? encodeURL(bigImage) : encodeURL(image));
		return imageVarmap;
	}

	@Override
	protected String getImageSnippet(boolean first) {
		StringBuilder sb = new StringBuilder();
		if (!first)
			sb.append(",\n"); //$NON-NLS-1$
		sb.append(IMAGESNIPPET);
		return sb.toString();
	}

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
		return 60;
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
	protected Map<String, String> getSectionSnippetVars(StoryboardImpl storyboard, int i) {
		return null;
	}

	@Override
	protected Rectangle getBigImageSize() {
		return (CLASSICTHEME.equals(getTheme())) ? null : MAXBIGIMAGESIZE;
	}

	private String getTheme() {
		WebParameter theme = getShow().getParameter(Activator.PLUGIN_ID + ".theme"); //$NON-NLS-1$
		if (theme != null) {
			String value = (String) theme.getValue();
			int p = value.lastIndexOf('.');
			if (p >= 0)
				value = value.substring(p + 1);
			return value;
		}
		return CLASSICTHEME;
	}

	@Override
	protected void copyResources(File[] resources, final File rTarget) {
		if ("themes".equals(rTarget.getName())) { //$NON-NLS-1$
			String theme = getTheme();
			File[] filtered = new File[1];
			for (File file : resources)
				if (file.getName().equals(theme)) {
					filtered[0] = file;
					break;
				}
			if (filtered[0] != null)
				super.copyResources(filtered, rTarget);
		} else
			super.copyResources(resources, rTarget);
	}

}
