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

package com.bdaum.zoom.operations.internal.gen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.bdaum.zoom.batch.internal.IFileWatcher;
import com.bdaum.zoom.batch.internal.LoaderListener;
import com.bdaum.zoom.cat.model.Font_type;
import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameter;
import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.core.Assetbox;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IGalleryGenerator;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.image.internal.swt.ImageLoader;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.operations.internal.xmp.XMPUtilities;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;
import com.bdaum.zoom.program.HtmlEncoderDecoder;

@SuppressWarnings("restriction")
public abstract class AbstractGalleryGenerator implements IGalleryGenerator, LoaderListener {

	protected static final String IMAGE_HEIGHT = "imageHeight"; //$NON-NLS-1$
	protected static final String IMAGE_WIDTH = "imageWidth"; //$NON-NLS-1$
	protected static final String NAVPOS = "navpos"; //$NON-NLS-1$
	protected static final String RIGHTMARGIN = "rightmargin"; //$NON-NLS-1$
	protected static final String LEFTMARGIN = "leftmargin"; //$NON-NLS-1$
	protected static final String BOTTOMMARGIN = "bottommargin"; //$NON-NLS-1$
	protected static final String TOPMARGIN = "topmargin"; //$NON-NLS-1$
	public static final String[] STYLES = new String[] { "normal", "italic", //$NON-NLS-1$ //$NON-NLS-2$
			"oblique" }; //$NON-NLS-1$
	public static final String[] WEIGHT = new String[] { "normal", "bold", //$NON-NLS-1$ //$NON-NLS-2$
			"bolder ", "lighter" }; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String[] VARIANT = new String[] { "normal", "smallCaps" }; //$NON-NLS-1$ //$NON-NLS-2$
	private Storyboard selectedStoryboard;
	private MultiStatus status;
	private WebGalleryImpl gallery;
	private File thumbnailFolder;
	private File originalsFolder;
	private String prefix;
	private IProgressMonitor monitor;
	protected IAdaptable adaptable;
	private Set<String> filter;
	protected String opId = java.util.UUID.randomUUID().toString();
	protected IFileWatcher fileWatcher = CoreActivator.getDefault().getFileWatchManager();
	private File bigFolder;
	private File tTarget;
	private HtmlEncoderDecoder htmlEncoderDecoder;


	public void generate(WebGalleryImpl webGallery, IProgressMonitor aMonitor, IAdaptable info, MultiStatus mstatus)
			throws IOException {
		try {
			this.gallery = webGallery;
			this.monitor = aMonitor;
			this.adaptable = info;
			this.status = mstatus;
			jpegQuality = webGallery.getJpegQuality();
			String[] xmpFilter = webGallery.getXmpFilter();
			this.filter = xmpFilter == null ? new HashSet<String>() : new HashSet<String>(Arrays.asList(xmpFilter));
			File[] templates = getTemplates();
			String[] targetNames = getTargetNames();
			int work = templates == null ? 1 : templates.length + 1;
			if (selectedStoryboard == null)
				for (Storyboard storyboard : webGallery.getStoryboard())
					work += storyboard.getExhibit().size();
			else
				work += selectedStoryboard.getExhibit().size();
			aMonitor.beginTask(Messages.AbstractGalleryGenerator_generating_web_gallery, work);
			prefix = getGeneratorId() + '.';
			boolean isFtp = webGallery.getIsFtp();
			if (isFtp)
				targetFolder = Core.createTempDirectory("FtpTransfer"); //$NON-NLS-1$
			else {
				targetFolder = new File(webGallery.getOutputFolder());
				targetFolder.mkdirs();
			}
			File[] resources = getResourceFiles();
			if (resources != null) {
				rTarget = new File(targetFolder, "resources"); //$NON-NLS-1$
				rTarget.mkdir();
				copyResources(resources, rTarget);
				if (aMonitor.isCanceled())
					return;
			}
			File[] themes = getThemeFiles();
			if (themes != null) {
				tTarget = new File(targetFolder, "themes"); //$NON-NLS-1$
				tTarget.mkdir();
				copyResources(themes, tTarget);
				if (aMonitor.isCanceled())
					return;
			}
			String logo = webGallery.getLogo();
			if (logo != null && !logo.isEmpty()) {
				File logoFile = new File(logo);
				if (logoFile.exists()) {
					nameplate = copyImage(logoFile, new File(rTarget, logoFile.getName()));
					if (aMonitor.isCanceled())
						return;
				}
			}
			String bgimg = webGallery.getBgImage();
			if (bgimg != null && !bgimg.isEmpty()) {
				File imageFile = new File(bgimg);
				if (imageFile.exists()) {
					bgImage = copyImage(imageFile, new File(rTarget, imageFile.getName()));
					if (aMonitor.isCanceled())
						return;
				}
			}
			aMonitor.worked(1);
			imageFolder = new File(targetFolder, "images"); //$NON-NLS-1$
			imageFolder.mkdir();
			if (needsThumbnails()) {
				thumbnailFolder = new File(targetFolder, "thumbnails"); //$NON-NLS-1$
				thumbnailFolder.mkdirs();
			}
			if (getBigImageSize() != null) {
				bigFolder = new File(targetFolder, "big"); //$NON-NLS-1$
				bigFolder.mkdirs();
			}
			if (needsOriginals()) {
				originalsFolder = new File(targetFolder, "originals"); //$NON-NLS-1$
				originalsFolder.mkdirs();
			}
			Map<String, String> vars = getSubstitutions();
			if (aMonitor.isCanceled())
				return;
			Map<String, WebParameter> parameters = webGallery.getParameter();
			if (templates != null && vars != null) {
				for (int i = 0; i < templates.length; i++) {
					createArtefact(templates[i], targetFolder, targetNames[i], vars, parameters);
					if (aMonitor.isCanceled())
						return;
					aMonitor.worked(1);
				}
			}
			aMonitor.done();
		} finally {
			fileWatcher.stopIgnoring(opId);
			aMonitor = null;
		}
	}

	protected boolean needsOriginals() {
		return gallery.getDownloadText() != null && !gallery.getDownloadText().isEmpty() && !gallery.getHideDownload();
	}

	protected File[] getThemeFiles() {
		return null;
	}

	protected Rectangle getBigImageSize() {
		return null;
	}

	private static final int START = 0;
	private static final int PARM1 = 1;
	private static final int PARM2 = 2;
	private static final int COND = 3;
	private static final int CONDEND = 4;
	protected static final String HTTP = "http://"; //$NON-NLS-1$
	protected static final String HTTPS = "https://"; //$NON-NLS-1$
	private static final File[] EMPTYFILES = new File[0];
	private File imageFolder;
	private File rTarget;
	private File nameplate;
	private File bgImage;
	private File targetFolder;
	private IConfigurationElement configurationElement;
	private int jpegQuality;
	protected int maxImageWidthInSection = -1;
	protected int maxImageHeightInSection = -1;

	private void createArtefact(File template, File targetDir, String targetName, Map<String, String> vars,
			Map<String, WebParameter> parameters) {
		File targetFile = new File(targetDir, targetName);
		targetFile.delete();
		try (Writer out = new BufferedWriter(new FileWriter(targetFile));
				Reader in = new BufferedReader(new FileReader(template))) {
			targetFile.createNewFile();
			substitute(in, out, vars, parameters);
		} catch (IOException e) {
			addError(NLS.bind(Messages.AbstractGalleryGenerator_io_error_when_processing_template, template.getName()),
					e);
		}
	}

	/**
	 * @param in
	 *            - template $(var), $(booleanVar:text:}
	 * @param out
	 * @param vars
	 * @param parameters
	 * @throws IOException
	 */
	private void substitute(Reader in, Writer out, Map<String, String> vars, Map<String, WebParameter> parameters)
			throws IOException {
		int state = START;
		StringBuilder vartoken = new StringBuilder();
		boolean cond = false;
		while (true) {
			int read = in.read();
			if (read < 0)
				break;
			char c = (char) read;
			switch (state) {
			case START:
				switch (c) {
				case '$':
					state = PARM1;
					break;
				default:
					out.write(c);
					break;
				}
				break;
			case PARM1:
				switch (c) {
				case '{':
					state = PARM2;
					vartoken.setLength(0);
					break;
				default:
					out.write('$');
					out.write(c);
					state = START;
					break;
				}
				break;
			case PARM2:
				switch (c) {
				case '}':
					String varName = vartoken.toString();
					String substitute = null;
					if (parameters != null) {
						WebParameter webParameter = parameters.get(prefix + varName);
						if (webParameter != null) {
							Object value = webParameter.getValue();
							if (value instanceof Rgb_type)
								substitute = generateColor(null, (Rgb_type) value);
							else {
								substitute = value.toString();
								if (webParameter.getEncodeHtml())
									substitute = getHtmlEncoderDecoder().encodeHTML(substitute, false);
							}
						}
					}
					if (substitute == null)
						substitute = vars.get(varName);
					if (substitute != null)
						out.write(substitute.toCharArray());
					state = START;
					break;
				case ':':
					cond = false;
					varName = vartoken.toString();
					Object value = null;
					if (parameters != null) {
						WebParameter webParameter = parameters.get(prefix + varName);
						if (webParameter != null)
							value = webParameter.getValue();
					}
					if (!(value instanceof Boolean)) {
						value = vars.get(varName);
						if (value != null)
							value = Boolean.parseBoolean(value.toString());
					}
					if (value instanceof Boolean)
						cond = (Boolean) value;
					state = COND;
					break;
				case '\n':
				case '\r':
				case ' ':
					out.write('$');
					out.write('{');
					char[] buffer = new char[vartoken.length()];
					vartoken.getChars(0, vartoken.length(), buffer, 0);
					out.write(buffer);
					out.write(c);
					state = START;
					break;
				default:
					vartoken.append(c);
					break;
				}
				break;
			case COND:
				switch (c) {
				case ':':
					state = CONDEND;
					break;
				default:
					if (cond)
						out.write(c);
					break;
				}
				break;
			case CONDEND:
				switch (c) {
				case '}':
					state = START;
					break;
				default:
					if (cond) {
						out.write(':');
						out.write(c);
					}
					state = COND;
					break;
				}
				break;
			default:
				break;
			}
		}
	}

	protected void copyResources(File[] resources, File copyTarget) {
		for (File file : resources) {
			String name = file.getName();
			File out = new File(copyTarget, name);
			try {
				BatchUtilities.copyFolder(file, out, monitor);
			} catch (IOException e) {
				addError(NLS.bind(Messages.AbstractGalleryGenerator_io_error_when_copying_web_resource, name), e);
			} catch (DiskFullException e) {
				addError(NLS.bind(Messages.AbstractGalleryGenerator_disk_full, name), null);
				break;
			}
			if (monitor.isCanceled())
				break;
		}
	}

	private void addError(String msg, Exception e) {
		status.add(new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, msg, e));
	}

	protected abstract File[] getTemplates();

	protected String[] getTargetNames() {
		String pageName = getShow().getPageName();
		File[] templates = getTemplates();
		String[] names = new String[templates.length];
		for (int i = 0; i < templates.length; i++) {
			String name = templates[i].getName();
			if (name.equals(getHtmlTemplateName()) && pageName != null && !pageName.isEmpty())
				name = pageName;
			names[i] = name;
		}
		return names;
	}

	protected String getHtmlTemplateName() {
		return "index.html";//$NON-NLS-1$
	}

	protected abstract File[] getResourceFiles();

	protected File[] getResourceFiles(Bundle bundle) {
		return getFolderChildren(bundle, "/resources"); //$NON-NLS-1$
	}

	protected File getFolder(Bundle bundle, String name) {
		try {
			return FileLocator.findFile(bundle, name);
		} catch (Exception e) {
			// ignore
		}
		return null;
	}

	protected File[] getFolderChildren(Bundle bundle, String name) {
		try {
			File folder = FileLocator.findFile(bundle, name);
			return folder == null ? EMPTYFILES : folder.listFiles();
		} catch (Exception e) {
			// ignore
		}
		return EMPTYFILES;
	}

	protected File[] getTemplates(Bundle bundle) {
		return getFolderChildren(bundle, "templates"); //$NON-NLS-1$
	}

	protected Map<String, String> getSubstitutions() {
		Map<String, String> varmap = new HashMap<String, String>();
		String headHtml = getShow().getHeadHtml();
		if (headHtml != null)
			varmap.put("headhtml", headHtml); //$NON-NLS-1$
		String topHtml = getShow().getTopHtml();
		if (topHtml != null)
			varmap.put("tophtml", topHtml); //$NON-NLS-1$
		String footerHtml = getShow().getFooterHtml();
		if (footerHtml != null)
			varmap.put("footerhtml", footerHtml); //$NON-NLS-1$
		varmap.put("resources", getDeployResourceFolder().getName()); //$NON-NLS-1$
		String pageName = getShow().getPageName();
		if (pageName == null)
			pageName = getHtmlTemplateName();
		varmap.put("pagename", pageName); //$NON-NLS-1$
		File plate = getNameplate();
		if (plate != null)
			varmap.put("nameplatediv", generateNameplate(gallery, plate)); //$NON-NLS-1$
		File bgImage = getBgImage();
		if (bgImage != null)
			varmap.put("bgimage", generateBg(gallery, bgImage)); //$NON-NLS-1$
		varmap.put("keywords", getHtmlEncoderDecoder().encodeHTML( //$NON-NLS-1$
				Core.toStringList(gallery.getKeyword(), ", "), false)); //$NON-NLS-1$
		String s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()); //$NON-NLS-1$
		varmap.put("date", s.substring(0, s.length() - 2) + ':' + s.substring(s.length() - 2)); //$NON-NLS-1$
		String url = configurationElement.getAttribute("url"); //$NON-NLS-1$
		if (!gallery.getHideHeader()) {
			varmap.put("name", getHtmlEncoderDecoder().encodeHTML(gallery.getName(), false)); //$NON-NLS-1$
			String description = gallery.getDescription();
			if (description != null && !description.isEmpty()) {
				String d = gallery.getHtmlDescription() ? description : getHtmlEncoderDecoder().encodeHTML(description, true);
				varmap.put("description", d); //$NON-NLS-1$
				varmap.put("descriptiondiv", //$NON-NLS-1$
						NLS.bind("<div id=\"description\" class=\"description-container\">{0}</div>", d)); //$NON-NLS-1$
			}
		}
		varmap.put("footer", //$NON-NLS-1$
				generateFooter(gallery,
						url != null && !url.isEmpty() ? NLS.bind("<a href=\"{0}\" target=\"_blank\">{1}</a>", url, //$NON-NLS-1$
								configurationElement.getAttribute("name")) //$NON-NLS-1$
								: null));
		return varmap;
	}

	protected abstract Map<String, String> getSectionSnippetVars(StoryboardImpl storyboard, int i);

	protected abstract String getSectionHeader(int i);

	protected abstract String getSectionFooter();

	protected String generateImageList(boolean relative, boolean boost) {
		Shell shell = adaptable.getAdapter(Shell.class);
		File manifest = new File(imageFolder, "imagelist.xml"); //$NON-NLS-1$
		DialogSettings newsettings = new DialogSettings("images"); //$NON-NLS-1$
		DialogSettings settings = new DialogSettings("images"); //$NON-NLS-1$
		try {
			settings.load(manifest.getAbsolutePath());
		} catch (IOException e1) {
			// ignore
		}
		Map<String, WebParameter> parameters = gallery.getParameter();
		StringBuilder sb = new StringBuilder();
		ICore activator = CoreActivator.getDefault();
		IDbManager dbManager = activator.getDbManager();
		int thumbnailSizeInPixel = getThumbnailSizeInPixel(gallery.getThumbSize());
		int i = 0;
		boolean first = true;
		for (Storyboard storyboard : gallery.getStoryboard()) {
			if (selectedStoryboard != null && selectedStoryboard != storyboard)
				continue;
			maxImageWidthInSection = -1;
			maxImageHeightInSection = -1;
			String sectionSnippet = getSectionHeader(++i);
			Map<String, String> sectionvars = null;
			if (sectionSnippet != null) {
				sectionvars = getSectionSnippetVars((StoryboardImpl) storyboard, i);
				StringBuffer generated = generateSnippet(parameters, sectionvars, sectionSnippet);
				if (sb.length() > 0 && generated.length() > 0)
					sb.append('\n');
				sb.append(generated);
			}
			boolean enlarge = storyboard.getEnlargeSmall();
			int imageSizeInPixels = getImageSizeInPixels(storyboard.getImageSize());
			List<String> exhibits = storyboard.getExhibit();
			try (Assetbox box = new Assetbox(null, status, false)) {
				for (String id : exhibits) {
					WebExhibitImpl exhibit = dbManager.obtainById(WebExhibitImpl.class, id);
					if (exhibit != null) {
						String assetId = exhibit.getAsset();
						AssetImpl asset = dbManager.obtainAsset(assetId);
						if (asset != null) {
							int isize = imageSizeInPixels;
							if (boost) {
								int width = asset.getWidth();
								int height = asset.getHeight();
								if (width != 0 && height != 0) {
									double ratio = (double) width / height;
									if (ratio < 1d)
										ratio = 1d / ratio;
									isize *= ratio;
								}
							}
							String copyright = null;
							if (gallery.getAddWatermark()) {
								copyright = asset.getCopyright();
								if (copyright == null || copyright.isEmpty())
									copyright = gallery.getCopyright();
								if (copyright != null && copyright.isEmpty())
									copyright = null;
							}
							File originalFile = box.obtainFile(asset);
							Rectangle bounds = null;
							if (originalFile != null) {
								URI uri = box.getUri();
								boolean includeMetadata = exhibit.getIncludeMetadata();
								int rotation = asset.getRotation();
								IDialogSettings manifestSection = createManifestEntry(newsettings, uri, rotation, isize,
										thumbnailSizeInPixel, getBigImageSize(), includeMetadata, copyright, enlarge,
										gallery.getRadius(), gallery.getAmount(), gallery.getThreshold(),
										gallery.getApplySharpening());
								String imageName = Core.getFileName(uri, false) + ".jpg"; //$NON-NLS-1$
								File imageFile = new File(imageFolder, imageName);
								File bigFile = bigFolder != null ? new File(bigFolder, imageName) : null;
								File thumbnail = thumbnailFolder == null ? null : new File(thumbnailFolder, imageName);
								Rectangle imageDims = manifestEntryModified(settings, imageFile, thumbnail, uri,
										rotation, isize, thumbnailSizeInPixel, getBigImageSize(), includeMetadata,
										copyright, enlarge, gallery.getRadius(), gallery.getAmount(),
										gallery.getThreshold(), gallery.getApplySharpening());
								if (imageDims == null) {
									UnsharpMask umask = gallery.getApplySharpening()
											? ImageActivator.getDefault().computeUnsharpMask(gallery.getRadius(),
													gallery.getAmount(), gallery.getThreshold())
											: null;
									Rectangle bigImageSize = getBigImageSize();
									ZImage zimage = null;
									try {
										zimage = CoreActivator.getDefault().getHighresImageLoader().loadImage(null,
												status, originalFile, rotation, asset.getFocalLengthIn35MmFilm(),
												bigImageSize == null ? computeImageBounds(isize) : bigImageSize, 1d,
												enlarge ? Double.MAX_VALUE : 1d, true, ImageConstants.SRGB, null, umask,
												null, fileWatcher, opId, this);
									} catch (UnsupportedOperationException e) {
										// do nothing
									}
									Image image = null;
									Image bigImage = null;
									try {
										if (bigImageSize != null) {
											bigImage = zimage == null ? null
													: zimage.getSwtImage(shell.getDisplay(), true, ZImage.CROPPED,
															bigImageSize.width, bigImageSize.height);
											if (bigImage != null) {
												image = new Image(shell.getDisplay(), ImageUtilities
														.downSample(bigImage.getImageData(), isize, isize, 0));
												bigFile = decorateImage(bigImage, bigFile, asset, copyright,
														includeMetadata);
												imageFile = decorateImage(image, imageFile, asset, copyright,
														includeMetadata);
												if (thumbnail != null && thumbnailSizeInPixel > 0)
													thumbnail = generateThumbnail(thumbnail, image,
															thumbnailSizeInPixel);
											}
										} else {
											image = zimage == null ? null
													: zimage.getSwtImage(shell.getDisplay(), true, ZImage.CROPPED,
															isize, isize);
											if (image != null) {
												imageFile = decorateImage(image, imageFile, asset, copyright,
														includeMetadata);
												if (thumbnail != null && thumbnailSizeInPixel > 0)
													thumbnail = generateThumbnail(thumbnail, image,
															thumbnailSizeInPixel);
											}
										}
									} finally {
										if (bigImage != null)
											bigImage.dispose();
										if (image != null) {
											bounds = image.getBounds();
											image.dispose();
											updateSection(manifestSection, bounds);
										}
									}
								} else {
									bounds = imageDims;
									if (filter != null && !filter.isEmpty()) {
										imageFile = generateImage(asset, imageFile, null, includeMetadata, jpegQuality);
										if (bigFile != null)
											bigFile = generateImage(asset, bigFile, null, includeMetadata, jpegQuality);
									}
								}
								File copiedFile = new File(originalsFolder, originalFile.getName());
								boolean downloadable = exhibit.getDownloadable() && needsOriginals();
								if (originalModified(settings, copiedFile, downloadable, uri)) {
									if (downloadable)
										copiedFile = copyImage(originalFile, copiedFile);
									else {
										copiedFile.delete();
										copiedFile = null;
									}
								}
								Map<String, String> vars = getImageSnippetVars(exhibit, asset, storyboard,
										thumbnail == null ? null
												: (relative) ? thumbnail.getName()
														: thumbnailFolder.getName() + '/' + thumbnail.getName(),
										imageFile == null ? null
												: (relative) ? imageFile.getName()
														: imageFolder.getName() + '/' + imageFile.getName(),
										bigFile == null ? null
												: (relative) ? bigFile.getName()
														: bigFolder.getName() + '/' + bigFile.getName(),
										copiedFile == null ? null
												: (relative) ? copiedFile.getName()
														: originalsFolder == null ? null
																: originalsFolder.getName() + '/'
																		+ copiedFile.getName(),
										i);
								if (bounds != null) {
									vars.put(IMAGE_WIDTH, String.valueOf(bounds.width));
									vars.put(IMAGE_HEIGHT, String.valueOf(bounds.height));
									if (bounds.width > maxImageWidthInSection)
										maxImageWidthInSection = bounds.width;
									if (bounds.height > maxImageHeightInSection)
										maxImageHeightInSection = bounds.height;
								} else {
									vars.put(IMAGE_WIDTH, "-1"); //$NON-NLS-1$
									vars.put(IMAGE_HEIGHT, "-1"); //$NON-NLS-1$
								}
								String snippet = getImageSnippet(first);
								first = false;
								StringBuffer generated = generateSnippet(parameters, vars, snippet);
								if (sb.length() > 0 && generated.length() > 0)
									sb.append('\n');
								sb.append(generated);
							}
						}
					}
					monitor.worked(1);
					if (monitor.isCanceled())
						return ""; //$NON-NLS-1$
				}
			}
			if (sectionSnippet != null) {
				StringBuffer generated = generateSnippet(parameters, sectionvars, sectionSnippet = getSectionFooter());
				if (sb.length() > 0 && generated.length() > 0)
					sb.append('\n');
				sb.append(generated);
			}
		}
		manifest.delete();
		try {
			newsettings.save(manifest.getAbsolutePath());
		} catch (IOException e) {
			// ignore
		}
		return sb.toString();
	}

	protected Rectangle computeImageBounds(int isize) {
		return new Rectangle(0, 0, isize, isize);
	}

	private static void updateSection(IDialogSettings manifestSection, Rectangle bounds) {
		manifestSection.put("actualWidth", bounds.width); //$NON-NLS-1$
		manifestSection.put("actualHeight", bounds.height); //$NON-NLS-1$
	}

	private File decorateImage(Image image, File imageFile, AssetImpl asset, String copyright,
			boolean includeMetadata) {
		Image outputImage = image;
		outputImage = ImageUtilities.addWatermark(image, copyright);
		imageFile = generateImage(asset, imageFile, outputImage, includeMetadata, jpegQuality);
		if (outputImage != image)
			outputImage.dispose();
		return imageFile;
	}

	protected StringBuffer generateSnippet(Map<String, WebParameter> parameters, Map<String, String> vars,
			String snippet) {
		StringReader in = new StringReader(snippet);
		StringWriter out = new StringWriter();
		try {
			substitute(in, out, vars, parameters);
			in.close();
			out.close();
		} catch (IOException e) {
			// should never happen
		}
		return out.getBuffer();
	}

	private static boolean originalModified(DialogSettings settings, File copiedFile, boolean downloadable,
			URI originalFile) {
		if (copiedFile.exists() != downloadable)
			return true;
		IDialogSettings section = settings.getSection(originalFile.toString());
		if (section == null)
			return true;
		if (Constants.FILESCHEME.equals(originalFile.getScheme())) {
			long timestamp = BatchUtilities.getImageFileModificationTimestamp(new File(originalFile));
			if (section.getLong("modifiedAt") != timestamp) //$NON-NLS-1$
				return true;
		}
		return false;
	}

	private Rectangle manifestEntryModified(DialogSettings settings, File imageFile, File thumbnail, URI originalFile,
			int rotation, int imageSizeInPixel, int thumbnailSizeInPixel, Rectangle bigImageBounds,
			boolean includeMetadata, String copyright, boolean enlarge, float radius, float amount, int threshold,
			boolean applySharpening) {
		if (!imageFile.exists() || (thumbnail != null && !thumbnail.exists()))
			return null;
		IDialogSettings section = settings.getSection(originalFile.toString());
		if (section == null)
			return null;
		if (Constants.FILESCHEME.equals(originalFile.getScheme())) {
			long timestamp = BatchUtilities.getImageFileModificationTimestamp(new File(originalFile));
			if (getLongSetting(section, "modifiedAt") != timestamp) //$NON-NLS-1$
				return null;
		}
		if (getIntSetting(section, "rotation") != rotation) //$NON-NLS-1$
			return null;
		if (getIntSetting(section, "imageSize") != imageSizeInPixel) //$NON-NLS-1$
			return null;
		if (getIntSetting(section, "thumbnailSize") != thumbnailSizeInPixel) //$NON-NLS-1$
			return null;
		if (bigImageBounds != null) {
			if (getIntSetting(section, "bigImageWidth") != bigImageBounds.width) //$NON-NLS-1$
				return null;
			if (getIntSetting(section, "bigImageHeight") != bigImageBounds.height) //$NON-NLS-1$
				return null;
		}
		if (section.getBoolean("includeMeta") != includeMetadata) //$NON-NLS-1$
			return null;
		if (section.getBoolean("enlarge") != enlarge) //$NON-NLS-1$
			return null;
		if (section.getBoolean("applySharpening") != applySharpening) //$NON-NLS-1$
			return null;
		if (applySharpening) {
			if (getFloatSetting(section, "radius") != radius) //$NON-NLS-1$
				return null;
			if (getFloatSetting(section, "amount") != amount) //$NON-NLS-1$
				return null;
			if (getIntSetting(section, "threshold") != threshold) //$NON-NLS-1$
				return null;
		}
		boolean hasMetaData = (includeMetadata && filter != null && !filter.isEmpty());
		boolean hadMetaData = section.getBoolean("hasMetaData"); //$NON-NLS-1$
		if (hadMetaData != hasMetaData)
			return null;
		String s = section.get("copyright"); //$NON-NLS-1$
		if (s == null && copyright != null || s != null && !s.equals(copyright))
			return null;
		return new Rectangle(0, 0, getIntSetting(section, "actualWidth"), getIntSetting(section, "actualHeight")); //$NON-NLS-1$ //$NON-NLS-2$
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

	private IDialogSettings createManifestEntry(DialogSettings settings, URI originalFile, int rotation,
			int imageSizeInPixel, int thumbnailSizeInPixel, Rectangle bigImageBounds, boolean includeMetadata,
			String copyright, boolean enlarge, float radius, float amount, int threshold, boolean applySharpening) {
		IDialogSettings section = settings.addNewSection(originalFile.toString());
		if (Constants.FILESCHEME.equals(originalFile.getScheme())) {
			long timestamp = BatchUtilities.getImageFileModificationTimestamp(new File(originalFile));
			section.put("modifiedAt", timestamp); //$NON-NLS-1$
		}
		section.put("rotation", rotation); //$NON-NLS-1$
		section.put("imageSize", imageSizeInPixel); //$NON-NLS-1$
		section.put("thumbnailSize", thumbnailSizeInPixel); //$NON-NLS-1$
		if (bigImageBounds != null) {
			section.put("bigImageWidth", bigImageBounds.width); //$NON-NLS-1$
			section.put("bigImageHeight", bigImageBounds.height); //$NON-NLS-1$
		}
		section.put("includeMeta", includeMetadata); //$NON-NLS-1$
		section.put("enlarge", enlarge); //$NON-NLS-1$
		section.put("radius", radius); //$NON-NLS-1$
		section.put("amount", amount); //$NON-NLS-1$
		section.put("threshold", threshold); //$NON-NLS-1$
		section.put("applySharpening", applySharpening); //$NON-NLS-1$
		if (copyright != null)
			section.put("copyright", copyright); //$NON-NLS-1$
		if (includeMetadata && filter != null && !filter.isEmpty())
			section.put("hasMetaData", true); //$NON-NLS-1$
		return section;
	}

	protected int getImageSizeInPixels() {
		if (selectedStoryboard != null)
			return getImageSizeInPixels(selectedStoryboard.getImageSize());
		return getImageSizeInPixels(gallery.getStoryboard(0).getImageSize());
	}

	protected abstract int getThumbnailSizeInPixel(int thumbSize);

	protected abstract int getImageSizeInPixels(int i);

	public boolean progress(int total, int worked) {
		return monitor.isCanceled();
	}

	private File copyImage(File orginalFile, File out) {
		String name = orginalFile.getName();
		try {
			BatchUtilities.copyFile(orginalFile, out, monitor);
			return out;
		} catch (IOException e) {
			addError(NLS.bind(Messages.AbstractGalleryGenerator_io_error_copying_original, name), e);
		} catch (DiskFullException e) {
			addError(NLS.bind(Messages.AbstractGalleryGenerator_disk_full, name), null);
		}
		return null;
	}

	protected abstract String getGeneratorId();

	protected abstract String getImageSnippet(boolean first);

	protected abstract Map<String, String> getImageSnippetVars(WebExhibit exhibit, AssetImpl asset,
			Storyboard storyboard, String thumbnail, String image, String big, String original, int storyBoardNo);

	private File generateImage(AssetImpl asset, File imageFile, Image image, boolean meta, int quality) {
		ImageLoader swtLoader = null;
		if (image != null) {
			swtLoader = new ImageLoader();
			if (quality > 0)
				swtLoader.compression = quality;
			swtLoader.data = new ImageData[] { image.getImageData() };
		}
		try {
			if (meta && filter != null && !filter.isEmpty()) {
				byte[] bytes = null;
				if (swtLoader != null) {
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					swtLoader.save(bout, SWT.IMAGE_JPEG);
					bytes = bout.toByteArray();
				} else {
					int length = (int) imageFile.length();
					bytes = new byte[length];
					try (FileInputStream in = new FileInputStream(imageFile)) {
						in.read(bytes);
					} catch (IOException e) {
						addError(NLS.bind(Messages.AbstractGalleryGenerator_io_error_updating_web_gallery,
								imageFile.getName()), e);
					}
				}
				try {
					XMPUtilities.configureXMPFactory();
					XMPMeta xmpMeta = XMPMetaFactory.create();
					ByteArrayOutputStream mout = new ByteArrayOutputStream();
					XMPUtilities.writeProperties(xmpMeta, asset, null, false);
					XMPMetaFactory.serialize(xmpMeta, mout);
					byte[] metadata = mout.toByteArray();
					bytes = XMPUtilities.insertXmpIntoJPEG(bytes, metadata);
				} catch (XMPException e) {
					addError(NLS.bind(Messages.AbstractGalleryGenerator_xmp_error, imageFile.getName()), e);
				}
				try (FileOutputStream out = new FileOutputStream(imageFile)) {
					out.write(bytes);
				}
			} else if (swtLoader != null) {
				try (FileOutputStream out = new FileOutputStream(imageFile)) {
					swtLoader.save(out, SWT.IMAGE_JPEG);
				}
			}
			return imageFile;
		} catch (IOException e) {
			addError(NLS.bind(Messages.AbstractGalleryGenerator_io_error_for_image_n, imageFile.getName()), e);
			return null;
		}
	}

	private static File generateThumbnail(File out, Image image, int thumbSize) {
		ImageData thumb = ImageUtilities.downSample(image.getImageData(), thumbSize, thumbSize, 0);
		ImageUtilities.saveSwtImageAsJpg(thumb, out.getAbsolutePath());
		return out;
	}

	protected WebGalleryImpl getShow() {
		return gallery;
	}

	protected File getDeployResourceFolder() {
		return rTarget;
	}

	protected File getDeployImageFolder() {
		return imageFolder;
	}

	protected File getDeployThumbnailFolder() {
		return thumbnailFolder;
	}

	protected File getNameplate() {
		return nameplate;
	}

	protected String generateColor(String tag, Rgb_type color) {
		if (tag != null) {
			if (color == null)
				return ""; //$NON-NLS-1$
			return new StringBuilder().append(tag).append(tag.isEmpty() ? "rgb(" : ":rgb(").append(color.getR()) //$NON-NLS-1$//$NON-NLS-2$
					.append(',').append(color.getG()).append(',').append(color.getB()).append(");").toString(); //$NON-NLS-1$
		}
		if (color == null)
			return ("ffffff");//$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		appendColor(color.getR(), sb);
		appendColor(color.getG(), sb);
		appendColor(color.getB(), sb);
		return sb.toString();
	}

	private static void appendColor(int c, StringBuilder sb) {
		String hex = Integer.toHexString(c);
		if (hex.length() == 1)
			sb.append('0');
		sb.append(hex);
	}

	protected String generateFont(Font_type font) {
		StringBuilder sb = new StringBuilder();
		if (font != null) {
			String[] family = font.getFamily();
			if (family != null && family.length > 0) {
				sb.append("font-family:"); //$NON-NLS-1$
				for (int i = 0; i < family.length; i++) {
					if (i > 0)
						sb.append(","); //$NON-NLS-1$
					if (family[i].indexOf(' ') < 0)
						sb.append(family[i]);
					else
						sb.append('"').append(family[i]).append('"');
				}
				sb.append(';');
			}
			sb.append("font-size:").append(font.getSize()).append("%;"); //$NON-NLS-1$ //$NON-NLS-2$
			int style = Math.max(0, Math.min(font.getStyle(), STYLES.length - 1));
			if (style != 0)
				sb.append("font-style:").append(STYLES[style]).append(';'); //$NON-NLS-1$
			int weight = Math.max(0, Math.min(font.getWeight(), WEIGHT.length - 1));
			if (weight != 0)
				sb.append("font-weight:").append(WEIGHT[weight]).append(';'); //$NON-NLS-1$
			int variant = Math.max(0, Math.min(font.getVariant(), VARIANT.length - 1));
			if (variant != 0)
				sb.append("font-variant:").append(VARIANT[variant]).append(';'); //$NON-NLS-1$
			Rgb_type color = font.getColor();
			if (color != null)
				sb.append(generateColor("color", color)); //$NON-NLS-1$
		}
		return sb.toString();
	}

	protected File getBgImage() {
		return bgImage;
	}

	protected void generateLink(String href, StringBuilder sb) {
		sb.append("<a href=\""); //$NON-NLS-1$
		StringTokenizer st = new StringTokenizer(href, "/:", true); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.equals("/") || token.equals(":")) //$NON-NLS-1$ //$NON-NLS-2$
				sb.append(token);
			else
				sb.append(encodeURL(token));
		}
		sb.append("\">"); //$NON-NLS-1$
	}

	protected String generateColor(Rgb_type color) {
		if (color == null)
			return ""; //$NON-NLS-1$
		return Utilities.toHtmlColors(color.getR(), color.getG(), color.getB());
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

	protected String getExifDiv(Storyboard storyboard, WebExhibit exhibit, AssetImpl asset, String css) {
		String s = null;
		if (gallery.getShowMeta() && storyboard.getShowExif() && exhibit.getIncludeMetadata()) {
			String[] xmpFilter = gallery.getXmpFilter();
			if (xmpFilter != null) {
				List<QueryField> fields = new ArrayList<QueryField>();
				for (String id : xmpFilter) {
					QueryField qf = QueryField.findQueryField(id);
					if (qf != null && qf.hasLabel())
						fields.add(qf);
				}
				if (!fields.isEmpty()) {
					Collections.sort(fields, new Comparator<QueryField>() {
						public int compare(QueryField f1, QueryField f2) {
							return f1.getLabel().compareTo(f2.getLabel());
						}
					});
					boolean multi = "<multi>".equals(css); //$NON-NLS-1$
					boolean single = "<single>".equals(css); //$NON-NLS-1$
					StringBuilder sb = new StringBuilder();
					if (multi || single)
						sb.append("<b>EXIF: </b>"); //$NON-NLS-1$
					else if (css != null)
						sb.append("<div class='").append(css).append("'><ul>"); //$NON-NLS-1$ //$NON-NLS-2$
					else
						sb.append("<p align=left><b>EXIF: </b>"); //$NON-NLS-1$
					boolean first = true;
					for (QueryField qf : fields) {
						Object value = qf.obtainFieldValue(asset);
						if (value != null && value != QueryField.VALUE_NOTHING && value != QueryField.VALUE_MIXED) {
							String text = qf.value2text(value, ""); //$NON-NLS-1$
							if (qf.getUnit() != null)
								text += ' ' + qf.getUnit();
							if (text != Format.MISSINGENTRYSTRING) {
								if (multi || single) {
									sb.append(qf.getLabel()).append(": ") //$NON-NLS-1$
											.append(text);
									sb.append(multi ? "<br/>" : ", "); //$NON-NLS-1$ //$NON-NLS-2$
								} else {
									if (css != null)
										sb.append("<li>"); //$NON-NLS-1$
									else if (first)
										sb.append(", "); //$NON-NLS-1$
									sb.append(getHtmlEncoderDecoder().encodeHTML(qf.getLabel(), false)).append(": ").append( //$NON-NLS-1$
											getHtmlEncoderDecoder().encodeHTML(text, false));
									if (css != null)
										sb.append("</li>\n"); //$NON-NLS-1$
								}
								first = false;
							}
						}
					}
					if (!multi && !single)
						sb.append(css != null ? "</ul></div>" : "</p>"); //$NON-NLS-1$ //$NON-NLS-2$
					s = sb.toString();
				}
			}
		}
		return s;
	}

	protected int computeExifCount() {
		if (!gallery.getShowMeta())
			return 0;
		String[] xmpFilter = gallery.getXmpFilter();
		return (xmpFilter == null) ? 0 : xmpFilter.length;
	}

	public File getTargetFolder() {
		return targetFolder;
	}

	public File getThemeFolder() {
		return tTarget;
	}

	protected String generateFooter(WebGalleryImpl show, Object engineText) {
		StringBuilder sb = new StringBuilder();
		if (!show.getHideFooter()) {
			String web = show.getWebUrl();
			String label = web;
			web = formatWebUrl(web);
			String copyright = show.getCopyright();
			if (copyright != null && !copyright.isEmpty())
				sb.append("&copy; ").append(getHtmlEncoderDecoder().encodeHTML(copyright, false)); //$NON-NLS-1$
			String contact = show.getContactName();
			String email = show.getEmail();
			if (contact == null || contact.isEmpty())
				contact = email;
			if (contact != null && !contact.isEmpty()) {
				if (sb.length() > 0)
					sb.append("  -  "); //$NON-NLS-1$
				if (email != null && !email.isEmpty())
					sb.append("<a href=\"mailto:").append(email).append("\">") //$NON-NLS-1$ //$NON-NLS-2$
							.append(getHtmlEncoderDecoder().encodeHTML(contact, false)).append("</a>"); //$NON-NLS-1$
				else
					sb.append(getHtmlEncoderDecoder().encodeHTML(contact, false));
			}
			if (web != null && !web.isEmpty()) {
				if (sb.length() > 0)
					sb.append("  -  "); //$NON-NLS-1$
				if (label.startsWith(HTTP))
					label = label.substring(HTTP.length());
				else if (label.startsWith(HTTPS))
					label = label.substring(HTTPS.length());
				generateLink(web, sb);
				sb.append(getHtmlEncoderDecoder().encodeHTML(label, false)).append("</a>"); //$NON-NLS-1$
			}
			String poweredBy = show.getPoweredByText();
			if (poweredBy != null && !poweredBy.isEmpty() && engineText != null) {
				if (sb.length() > 0)
					sb.append("<br/>"); //$NON-NLS-1$
				if (poweredBy.indexOf("{0}") < 0) //$NON-NLS-1$
					poweredBy += " {0}"; //$NON-NLS-1$
				sb.append(NLS.bind(poweredBy, engineText));
			}
		}
		return sb.toString();
	}

	private static String formatWebUrl(String web) {
		return web != null && !web.isEmpty() && !web.startsWith(HTTP) && !web.startsWith(HTTPS) ? HTTP + web : web;
	}

	protected String generateBg(WebGalleryImpl show, File backgroundImage) {
		return new StringBuilder().append("background-image: url(") //$NON-NLS-1$
				.append(encodeURL(getDeployResourceFolder().getName())).append('/')
				.append(encodeURL(backgroundImage.getName())).append(");") //$NON-NLS-1$
				.append(show.getBgRepeat() ? "background-repeat:repeat;" : "background-repeat:no-repeat;").toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected String generateNameplate(WebGalleryImpl show, File plate) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div>"); //$NON-NLS-1$
		String web = show.getWebUrl();
		web = formatWebUrl(web);
		if (web != null && !web.isEmpty())
			generateLink(web, sb);
		sb.append("<img border=\"0\" src=\"") //$NON-NLS-1$
				.append(encodeURL(getDeployResourceFolder().getName())).append('/').append(encodeURL(plate.getName()))
				.append("\"/>"); //$NON-NLS-1$
		if (web != null && !web.isEmpty())
			sb.append("</a>"); //$NON-NLS-1$
		return sb.append("</div>").toString(); //$NON-NLS-1$
	}

	protected void setFontsAndColors(Map<String, String> varmap) {
		varmap.put("bgcolor", generateColor("background-color", gallery //$NON-NLS-1$ //$NON-NLS-2$
				.getBgColor()));
		varmap.put("backgroundcolor", generateColor(gallery.getBgColor())); //$NON-NLS-1$
		varmap.put("shadecolor", generateColor("background-color", gallery //$NON-NLS-1$ //$NON-NLS-2$
				.getShadeColor()));
		varmap.put("bordercolor", generateColor("border-color", gallery //$NON-NLS-1$ //$NON-NLS-2$
				.getBorderColor()));
		varmap.put("linkcolor", generateColor("color", gallery.getLinkColor())); //$NON-NLS-1$ //$NON-NLS-2$
		varmap.put("titlefont", generateFont(gallery.getTitleFont())); //$NON-NLS-1$
		varmap.put("sectionfont", generateFont(gallery.getSectionFont())); //$NON-NLS-1$
		varmap.put("captionfont", generateFont(gallery.getCaptionFont())); //$NON-NLS-1$
		varmap.put("bodyfont", generateFont(gallery.getDescriptionFont())); //$NON-NLS-1$
		Font_type footerFont = gallery.getFooterFont();
		Font_type navFont = gallery.getControlsFont();
		varmap.put("footerfont", generateFont(footerFont)); //$NON-NLS-1$
		if (navFont == null)
			navFont = footerFont;
		varmap.put("navfont", generateFont(navFont)); //$NON-NLS-1$
		if (footerFont != null)
			varmap.put("footercolor", generateColor("color", footerFont //$NON-NLS-1$ //$NON-NLS-2$
					.getColor()));
		if (navFont != null)
			varmap.put("navcolor", generateColor("color", navFont //$NON-NLS-1$ //$NON-NLS-2$
					.getColor()));
	}

	public void setConfigurationElement(IConfigurationElement el) {
		this.configurationElement = el;
	}

	/**
	 * @return the configurationElement
	 */
	public IConfigurationElement getConfigurationElement() {
		return configurationElement;
	}

	protected int setPaddingAndMargins(WebGalleryImpl show, Map<String, String> varmap, String imagePos) {
		int padding = show.getPadding();
		varmap.put("padding", String.valueOf(padding)); //$NON-NLS-1$
		int[] margins = show.getMargins();
		int topmargin;
		int rightmargin;
		int bottommargin;
		int leftmargin;
		if (margins != null && margins.length >= 4) {
			topmargin = margins[0];
			rightmargin = margins[1];
			bottommargin = margins[2];
			leftmargin = margins[3];
		} else {
			topmargin = padding;
			rightmargin = padding * 2;
			bottommargin = padding;
			leftmargin = 0;
			if ("left".equals(imagePos)) { //$NON-NLS-1$
				rightmargin = 0;
				leftmargin = padding * 2;
			} else if ("top".equals(imagePos)) { //$NON-NLS-1$
				topmargin = padding * 2;
				rightmargin = padding;
				bottommargin = 0;
				leftmargin = padding;
			}
		}
		varmap.put(TOPMARGIN, String.valueOf(topmargin));
		varmap.put(BOTTOMMARGIN, String.valueOf(bottommargin));
		varmap.put(LEFTMARGIN, String.valueOf(leftmargin));
		varmap.put(RIGHTMARGIN, String.valueOf(rightmargin));
		String navpos = "left"; //$NON-NLS-1$
		if (imagePos != null) {
			if ("left".equals(imagePos)) //$NON-NLS-1$
				navpos = "right"; //$NON-NLS-1$
			else if ("top".equals(imagePos)) //$NON-NLS-1$
				navpos = "bottom"; //$NON-NLS-1$
			else if ("bottom".equals(imagePos)) //$NON-NLS-1$
				navpos = "top"; //$NON-NLS-1$
		}
		varmap.put(NAVPOS, navpos);
		return 3 * (Math.abs(getThumbnailSizeInPixel(show.getThumbSize())) + leftmargin + rightmargin);
	}

	protected double getParamDouble(WebParameter param, double dflt) {
		if (param != null) {
			try {
				return Double.parseDouble(param.getValue().toString());
			} catch (NumberFormatException e) {
				// do nothing
			}
		}
		return dflt;
	}

	protected int getParamInt(WebParameter param, int dflt) {
		if (param != null)
			try {
				return Integer.parseInt(param.getValue().toString());
			} catch (NumberFormatException e) {
				// do nothing
			}
		return dflt;
	}

	protected String getParamString(WebParameter param, String dflt) {
		if (param != null) {
			Object value = param.getValue();
			if (value != null)
				return value.toString();
		}
		return dflt;
	}

	protected boolean getParamBoolean(WebParameter param) {
		if (param != null) {
			Object value = param.getValue();
			if (value != null)
				return Boolean.parseBoolean(value.toString());
		}
		return false;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IGalleryGenerator#needsThumbnails()
	 */
	public boolean needsThumbnails() {
		return true;
	}

	public void setSelectedStoryBoard(Storyboard selectedStoryboard) {
		this.selectedStoryboard = selectedStoryboard;
	}
	
	protected HtmlEncoderDecoder getHtmlEncoderDecoder() {
		if (htmlEncoderDecoder == null)
			htmlEncoderDecoder = new HtmlEncoderDecoder();
		return htmlEncoderDecoder;
	}


}
