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
 * (c) 2019-2021 Berthold Daum  
 */
package com.bdaum.zoom.webserver.internals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Rectangle;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.options.PropertyOptions;
import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.SimilarityOptions_typeImpl;
import com.bdaum.zoom.cat.model.TextSearchOptions_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.common.CommonUtilities;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.core.IScoreFormatter;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.db.ITypeFilter;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileNameExtensionFilter;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.QueryOptions;
import com.bdaum.zoom.core.internal.lire.Algorithm;
import com.bdaum.zoom.core.internal.lucene.ILuceneService;
import com.bdaum.zoom.core.internal.lucene.ParseException;
import com.bdaum.zoom.gps.internal.GeoService;
import com.bdaum.zoom.image.IVideoService;
import com.bdaum.zoom.image.IVideoService.IFrameHandler;
import com.bdaum.zoom.image.IVideoService.IVideoStreamer;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.operations.internal.xmp.XMPUtilities;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.KeywordSearchDialog;
import com.bdaum.zoom.webserver.PreferenceConstants;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.MultipartIterator;
import net.freeutils.httpserver.HTTPServer.MultipartIterator.Part;
import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;

@SuppressWarnings("restriction")
final class ImagesContextHandler extends AbstractLightboxContextHandler {

	private static final String XMP = ".xmp"; //$NON-NLS-1$
	private static IVolumeManager volumeManager = Core.getCore().getVolumeManager();
	private static int labelAlignment;
	private static final String boundary = "stream"; //$NON-NLS-1$

	public synchronized int serve(Request req, Response resp) throws IOException {
		if (!preferenceStore.getBoolean(PreferenceConstants.IMAGES))
			return 403;
		IDbManager dbManager = Core.getCore().getDbManager();
		String imagesPath = '/' + preferenceStore.getString(PreferenceConstants.IMAGEPATH);
		String jpeg = imagesPath + "/jpg/"; //$NON-NLS-1$
		String path = req.getPath();
		if (path.startsWith(jpeg)) {
			String assetId = path.substring(jpeg.length());
			int q = assetId.lastIndexOf('_');
			if (q > 0) {
				int size = parseInteger(assetId.substring(q + 1), 500);
				if (size == 0 && !preferenceStore.getBoolean(PreferenceConstants.FULLSIZE))
					return 403;
				return sendImage(dbManager.obtainAsset(assetId.substring(0, q)), req, resp, size);
			}
			return sendThumbnail(resp, assetId);
		}
		String video = imagesPath + "/video/"; //$NON-NLS-1$
		if (path.startsWith(video)) {
			ZHTTPServer server = WebserverActivator.getDefault().getServer();
			IVideoService videoService = ImageActivator.getDefault().getVideoService();
			if (videoService != null && server != null) {
				AssetImpl asset = Core.getCore().getDbManager().obtainAsset(path.substring(video.length()));
				URI uri = Core.getCore().getVolumeManager().findExistingFile(asset, true);
				if (uri != null) {
					File file = new File(uri);
					OutputStream outputStream = resp.getOutputStream();
					IVideoStreamer streamer = videoService.createVideoStreamer(file.getAbsolutePath(),
							new IFrameHandler() {
								@Override
								public boolean handleFrame(BufferedImage frame) {
									try {
										server.pushImage(outputStream, frame, boundary);
									} catch (IOException e) {
										return false;
									}
									return true;
								}
							}, -1);
					ZHTTPServer.writeVideoHeader(outputStream, boundary);
					streamer.start();
					return 0;
				}
				return 404;
			}
			return 503;
		}
		String byId = imagesPath + "/byId/"; //$NON-NLS-1$
		if (path.startsWith(byId))
			return preferenceStore.getBoolean(PreferenceConstants.DOWNLOAD)
					? handleDownloadPath(path.substring(byId.length()), req, resp)
					: 403;
		Map<String, String> params = req.getParams();
		String content = null;
		String ctype = "text/html"; //$NON-NLS-1$
		String search = null;
		String targetId = null;
		int pos = 0;
		String labelTemplate = null;
		SmartCollection sm = null;
		IAssetFilter[] filter = null;
		ITypeFilter typeFilter = Core.getCore().getDbFactory()
				.createTypeFilter(preferenceStore.getInt(PreferenceConstants.FORMATS));
		if (typeFilter != null)
			filter = new IAssetFilter[] { typeFilter };
		Map<String, String> substitutions = new HashMap<String, String>();
		substitutions.put("{$appName}", Constants.APPLICATION_NAME); //$NON-NLS-1$
		substitutions.put("{$orgLink}", System.getProperty("com.bdaum.zoom.homePage")); //$NON-NLS-1$ //$NON-NLS-2$
		if ("/search-similar".equals(path)) { //$NON-NLS-1$
			MultipartIterator mi = new MultipartIterator(req);
			while (mi.hasNext()) {
				Part part = mi.next();
				String filename = part.filename;
				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					HTTPServer.transfer(part.getBody(), out, -1);
					search = NLS.bind("~{0}", filename); //$NON-NLS-1$
					sm = searchSimilar(null, search, out.toByteArray());
					content = compileLightbox(dbManager, false, search, sm, pos, targetId, search, true, labelTemplate,
							substitutions, null, filter);
					if (content == null)
						return 415;
				}
				break;
			}
		} else if ("/file-upload".equals(path)) //$NON-NLS-1$
			return upload(req, imagesPath);
		else if (path.endsWith("/login")) { //$NON-NLS-1$
			String password = CommonUtilities.decode(preferenceStore.getString(PreferenceConstants.PASSWORD));
			if (!password.isEmpty() && !password.equals(Core.decodeUrl(params.get("password")))) //$NON-NLS-1$
				return 401;
			content = createUploadPage(substitutions);
		} else {
			String startPage = preferenceStore.getString(PreferenceConstants.STARTPAGE);
			if (path.endsWith("/lightbox.html") || path.endsWith('/' + startPage) || path.endsWith("/slideshow.html")) { //$NON-NLS-1$ //$NON-NLS-2$
				IdentifiableObject obj = null;
				boolean sorted = false;
				if ("POST".equals(req.getMethod())) //$NON-NLS-1$
					search = Core.decodeUrl(params.get("text")); //$NON-NLS-1$
				else {
					pos = parseInteger(params.get("pos"), 0); //$NON-NLS-1$
					targetId = params.get("targetId"); //$NON-NLS-1$
					search = Core.decodeUrl(params.get("search")); //$NON-NLS-1$
				}
				String searchcrumb = null;
				if (search != null && !search.isEmpty()) {
					searchcrumb = Messages.ImagesContextHandler_bad_query;
					String base = req.getBaseURL().toString();
					int p = path.lastIndexOf('/');
					if (p > 0)
						base += path.substring(0, p + 1);
					if (search.startsWith(base)) {
						ILuceneService luceneService = Core.getCore().getDbFactory().getLuceneService();
						if (luceneService != null) {
							String id = null;
							p = search.indexOf("imageId=", base.length()); //$NON-NLS-1$
							if (p > base.length()) {
								int q = search.indexOf('&', p + 8);
								if (q > p + 8)
									id = search.substring(p + 8, q);
							}
							if (id == null) {
								p = search.lastIndexOf('.');
								if (p > base.length())
									id = search.substring(base.length(), p);
							}
							if (id != null) {
								AssetImpl asset = dbManager.obtainAsset(id);
								if (asset != null) {
									searchcrumb = NLS.bind("~{0}", asset.getName()); //$NON-NLS-1$
									sm = searchSimilar(id, NLS.bind("~{0}", asset.getName()), //$NON-NLS-1$
											asset.getJpegThumbnail());
								}
							}
						}
					} else {
						Set<String> keywords = dbManager.getMeta(true).getKeywords();
						if (keywords.contains(search)) {
							searchcrumb = search;
							sm = KeywordSearchDialog.computeQuery(search, false, null);
							sorted = true;
						} else {
							ILuceneService luceneService = Core.getCore().getDbFactory().getLuceneService();
							if (luceneService != null)
								try {
									luceneService.parseLuceneQuery(search);
									searchcrumb = search;
									QueryOptions queryOptions = UiActivator.getDefault().getQueryOptions();
									sm = new SmartCollectionImpl(
											search + NLS.bind(Messages.LightboxContextHandler_maxmin,
													queryOptions.getMaxHits(), queryOptions.getScore()),
											false, false, true, false, null, 0, null, 0, null, Constants.INHERIT_LABEL,
											null, 0, 1, null);
									sm.addCriterion(new CriterionImpl(ICollectionProcessor.TEXTSEARCH, null,
											new TextSearchOptions_typeImpl(search, queryOptions.getMaxHits(),
													queryOptions.getScore() / 100f),
											null, Constants.TEXTSEARCHOPTIONS_DEFAULT_MIN_SCORE, true));
								} catch (ParseException e) {
									// ignore
								}
						}
					}
					pos = 0;
				} else if (targetId != null) {
					obj = dbManager.obtainById(IdentifiableObject.class, targetId);
					if (obj instanceof SmartCollection) {
						labelTemplate = WebserverActivator.getDefault().getCaptionProcessor()
								.computeCaptionConfiguration(sm = (SmartCollection) obj).getLabelTemplate();
						sorted = true;
					}
				}
				if (path.endsWith("/slideshow.html")) //$NON-NLS-1$
					content = compileSlideshowPage(substitutions, dbManager, sm, sorted, targetId, params, pos,
							labelTemplate, filter);
				else
					content = compileLightbox(dbManager, sorted, search, sm, pos, targetId, searchcrumb, false,
							labelTemplate, substitutions, obj, filter);
			} else if (path.endsWith("/view.html")) { //$NON-NLS-1$
				String imageId = params.get("imageId"); //$NON-NLS-1$
				AssetImpl asset = Core.getCore().getDbManager().obtainAsset(imageId);
				if (asset != null) {
					String caption = WebserverActivator.getDefault().getCaptionProcessor().computeImageCaption(asset,
							null, null, null, labelTemplate, true);
					substitutions.put("{$imageLink}", computeImageLink(asset, imageId, caption)); //$NON-NLS-1$
					substitutions.put("{$imageTitle}", UiUtilities.createSlideTitle(asset)); //$NON-NLS-1$
					substitutions.put("{$fullsizeLink}", "jpg/" + asset.getStringId() + "_0"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					String geoLink = null;
					double lat = asset.getGPSLatitude();
					double lon = asset.getGPSLongitude();
					if (!Double.isNaN(lat) && !Double.isNaN(lon))
						geoLink = GeoService.obtainGeoLink(lat, lon, 12);
					if (geoLink != null)
						substitutions.put("{$geoLink}", geoLink); //$NON-NLS-1$
					substitutions.put("{$imageFile}", "byId/" + asset.getStringId()); //$NON-NLS-1$ //$NON-NLS-2$
					substitutions.put("{$$infoAction}", //$NON-NLS-1$
							preferenceStore.getBoolean(PreferenceConstants.METADATA) ? null : ""); //$NON-NLS-1$
					substitutions.put("{$$fullAction}", //$NON-NLS-1$
							preferenceStore.getBoolean(PreferenceConstants.FULLSIZE) ? null : ""); //$NON-NLS-1$
					substitutions.put("{$$downloadAction}", //$NON-NLS-1$
							preferenceStore.getBoolean(PreferenceConstants.DOWNLOAD) ? null : ""); //$NON-NLS-1$
					substitutions.put("{$$geoAction}", //$NON-NLS-1$
							preferenceStore.getBoolean(PreferenceConstants.GEO) && geoLink != null ? null : ""); //$NON-NLS-1$
					substitutions.put("{$metadata}", computeMetadata(asset)); //$NON-NLS-1$
					substitutions.put("{$caption}", caption); //$NON-NLS-1$
					content = WebserverActivator.getDefault().compilePage(PreferenceConstants.VIEW, substitutions);
				}
			} else if (path.endsWith("/video.html")) { //$NON-NLS-1$
				String imageId = params.get("imageId"); //$NON-NLS-1$
				AssetImpl asset = Core.getCore().getDbManager().obtainAsset(imageId);
				if (asset != null) {
					String caption = WebserverActivator.getDefault().getCaptionProcessor().computeImageCaption(asset,
							null, null, null, labelTemplate, true);
					String uri = asset.getUri();
					substitutions.put("{$videoLink}", uri.startsWith("http") ? uri : "video/" + asset.getStringId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					substitutions.put("{$imageTitle}", UiUtilities.createSlideTitle(asset)); //$NON-NLS-1$
					String geoLink = null;
					double lat = asset.getGPSLatitude();
					double lon = asset.getGPSLongitude();
					if (!Double.isNaN(lat) && !Double.isNaN(lon))
						geoLink = GeoService.obtainGeoLink(lat, lon, 12);
					if (geoLink != null)
						substitutions.put("{$geoLink}", geoLink); //$NON-NLS-1$
					substitutions.put("{$imageFile}", "byId/" + asset.getStringId()); //$NON-NLS-1$ //$NON-NLS-2$
					substitutions.put("{$$infoAction}", //$NON-NLS-1$
							preferenceStore.getBoolean(PreferenceConstants.METADATA) ? null : ""); //$NON-NLS-1$
					substitutions.put("{$$downloadAction}", //$NON-NLS-1$
							preferenceStore.getBoolean(PreferenceConstants.DOWNLOAD) ? null : ""); //$NON-NLS-1$
					substitutions.put("{$$geoAction}", //$NON-NLS-1$
							preferenceStore.getBoolean(PreferenceConstants.GEO) && geoLink != null ? null : ""); //$NON-NLS-1$
					substitutions.put("{$metadata}", computeMetadata(asset)); //$NON-NLS-1$
					substitutions.put("{$caption}", caption); //$NON-NLS-1$
					content = WebserverActivator.getDefault().compilePage(PreferenceConstants.VIDEO, substitutions);
				}
			} else if (path.endsWith("/uploads.html")) { //$NON-NLS-1$
				if (!preferenceStore.getBoolean(PreferenceConstants.ALLOWUPLOADS))
					return 403;
				String password = CommonUtilities.decode(preferenceStore.getString(PreferenceConstants.PASSWORD));
				if (!password.isEmpty())
					return 403;
				content = createUploadPage(substitutions);
			} else if (path.endsWith("/login.html")) //$NON-NLS-1$
				content = WebserverActivator.getDefault().compilePage(PreferenceConstants.LOGIN, substitutions);
		}
		return sendDynamicContent(resp, content, ctype);
	}

	private static String compileSlideshowPage(Map<String, String> substitutions, IDbManager dbManager,
			SmartCollection sm, boolean sorted, String targetId, Map<String, String> params, int pos, String template,
			IAssetFilter[] filter) {
		StringBuilder sb = new StringBuilder();
		StringBuilder sc = new StringBuilder();
		String ids = params.get("ids"); //$NON-NLS-1$
		String title = Core.decodeUrl(params.get("title")); //$NON-NLS-1$
		if (ids != null && !ids.isEmpty()) {
			int i = 0;
			StringTokenizer st = new StringTokenizer(ids, ","); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String id = st.nextToken();
				if (i++ >= pos) {
					AssetImpl asset = dbManager.obtainAsset(id);
					if (asset != null && volumeManager.findExistingFile(asset, true) != null)
						addSlide(sb, sc, asset, template);
				}
			}
		} else if (sm != null) {
			StringBuilder st = new StringBuilder(sm.getName());
			SmartCollection parent = sm.getSmartCollection_subSelection_parent();
			while (parent != null) {
				st.insert(0, '>').insert(0, parent.getName());
				parent = parent.getSmartCollection_subSelection_parent();
			}
			title = st.toString();
			ICollectionProcessor processor = dbManager.createCollectionProcessor(sm, filter, null);
			List<Asset> assets = processor.select(sorted);
			for (int i = pos; i < assets.size(); i++) {
				Asset asset = assets.get(i);
				if (volumeManager.findExistingFile(asset, true) != null)
					addSlide(sb, sc, asset, template);
			}
		}
		substitutions.put("{$title}", toHtml(title, false)); //$NON-NLS-1$
		substitutions.put("{$slides}", sb.toString()); //$NON-NLS-1$
		substitutions.put("{$captions}", sc.toString()); //$NON-NLS-1$
		substitutions.put("{$properties}", ""); //$NON-NLS-1$ //$NON-NLS-2$

		substitutions.put("{$captionStyle}", //$NON-NLS-1$
				NLS.bind("text-align: {0};", labelAlignment == 0 ? "left" : labelAlignment == 2 ? "right" : "center")); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
		return WebserverActivator.getDefault().compilePage(PreferenceConstants.SLIDESHOW, substitutions);
	}

	private static void addSlide(StringBuilder sb, StringBuilder sc, Asset asset, String template) {
		if (sb.length() > 0)
			sb.append(",\n\t"); //$NON-NLS-1$
		sb.append('"').append(asset.getStringId()).append('"');
		if (sc.length() > 0)
			sc.append(",\n\t"); //$NON-NLS-1$
		String caption = WebserverActivator.getDefault().getCaptionProcessor().computeImageCaption(asset, null, null,
				null, template, true);
		sc.append('"').append(toHtml(caption, false)).append('"');
	}

	private String createUploadPage(Map<String, String> substitutions) {
		substitutions.put("{$$metaMod}", preferenceStore.getBoolean(PreferenceConstants.ALLOWMETADATAMOD) ? null : ""); //$NON-NLS-1$ //$NON-NLS-2$
		return WebserverActivator.getDefault().compilePage(PreferenceConstants.UPLOADS, substitutions);
	}

	private static int handleDownloadPath(String path, Request req, Response resp)
			throws MalformedURLException, IOException {
		URI uri = volumeManager.findExistingFile(Core.getCore().getDbManager().obtainAsset(path), true);
		if (uri == null)
			return 404;
		HTTPServer.serveFileContent(new File(uri), req, resp);
		return 0;
	}

	private int upload(Request req, String imagesPath) {
		if (!preferenceStore.getBoolean(PreferenceConstants.ALLOWUPLOADS))
			return 1403;
		boolean allowMetaMod = preferenceStore.getBoolean(PreferenceConstants.ALLOWMETADATAMOD);
		try {
			WatchedFolder wf = null;
			String tid = preferenceStore.getString(PreferenceConstants.TRANSFERFOLDER);
			Meta meta = Core.getCore().getDbManager().getMeta(true);
			if (meta.getWatchedFolder() != null) {
				CoreActivator activator = CoreActivator.getDefault();
				for (String id : meta.getWatchedFolder())
					if (id.equals(tid)) {
						WatchedFolder folder = activator.getObservedFolder(id);
						if (folder != null && folder.getTransfer()) {
							wf = folder;
							break;
						}
					}
			}
			if (wf == null)
				return 1503;
			XMPMeta xmpMeta = null;
			String uri = wf.getUri();
			File targetFolder = null;
			try {
				targetFolder = new File(new URI(uri));
				targetFolder.mkdirs();
			} catch (URISyntaxException e1) {
				return 1503;
			}
			FileNameExtensionFilter filenameFilter = CoreActivator.getDefault().getFilenameExtensionFilter();
			Map<QueryField, Object> metaMap = new HashMap<QueryField, Object>(7);
			Map<String, Part> xmpMap = new HashMap<String, Part>(7);
			MultipartIterator mi = new MultipartIterator(req);
			while (mi.hasNext()) {
				Part part = mi.next();
				String filename = part.filename;
				if (filename != null) {
					if (filename.endsWith(XMP)) {
						if (!allowMetaMod)
							return 1403;
						String xmpname = filename.substring(filename.length() - 4);
						if (Constants.WIN32)
							xmpname = xmpname.toUpperCase();
						xmpMap.put(xmpname, part);
					} else if (!filenameFilter.accept(filename))
						return 1415;
				} else if (allowMetaMod)
					collectMetadata(metaMap, part);
			}
			XMPUtilities.configureXMPFactory();
			mi = new MultipartIterator(req);
			while (mi.hasNext()) {
				Part part = mi.next();
				String filename = part.filename;
				if (filename != null) {
					File file = new File(targetFolder, filename);
					File xmpFile = null;
					Part xmpPart = null;
					int i = 0;
					while (true) {
						if (file.createNewFile()) {
							String xmpName = Constants.WIN32 ? filename.toUpperCase() : filename;
							xmpPart = xmpMap.get(xmpName);
							if (xmpPart == null) {
								int p = xmpName.lastIndexOf('.');
								if (p > 0)
									xmpPart = xmpMap.get(xmpName.substring(0, p));
							}
							if (!metaMap.isEmpty() || xmpPart != null) {
								// $NON-NLS-1$
								xmpFile = new File(targetFolder, file.getName() + XMP);
							}
							break;
						}
						int p = filename.lastIndexOf('.');
						file = new File(targetFolder,
								p >= 0 ? filename.substring(0, p) + '-' + ++i + filename.substring(p)
										: filename + '-' + ++i);
					}
					if (xmpFile != null)
						try {
							if (xmpMeta == null && !metaMap.isEmpty())
								xmpMeta = updateXmpMeta(XMPMetaFactory.create(), metaMap);
							String opid = java.util.UUID.randomUUID().toString();
							xmpFile.delete();
							fileWatcher.ignore(xmpFile, opid);
							if (xmpPart != null) {
								try (FileOutputStream out = new FileOutputStream(xmpFile)) {
									HTTPServer.transfer(xmpPart.getBody(), out, -1);
								}
								if (!metaMap.isEmpty())
									try (FileInputStream in = new FileInputStream(xmpFile)) {
										XMPMeta oldMeta = updateXmpMeta(XMPMetaFactory.parse(in), metaMap);
										xmpFile.delete();
										try (FileOutputStream out = new FileOutputStream(xmpFile)) {
											XMPMetaFactory.serialize(oldMeta, out);
										}
									}
							} else if (!metaMap.isEmpty())
								try (FileOutputStream out = new FileOutputStream(xmpFile)) {
									XMPMetaFactory.serialize(xmpMeta, out);
								}
							fileWatcher.stopIgnoring(opid);
						} catch (IOException e) {
							WebserverActivator.getDefault().logError(Messages.ImagesContextHandler_xmp_io_error, e);
						} catch (XMPException e) {
							WebserverActivator.getDefault().logError(Messages.ImagesContextHandler_cmp_error, e);
						}
					try (FileOutputStream out = new FileOutputStream(file)) {
						HTTPServer.transfer(part.getBody(), out, -1);
					}
				}
			}
			return 200;
		} catch (Throwable t) {
			return 1500;
		}
	}

	private static XMPMeta updateXmpMeta(XMPMeta xmpMeta, Map<QueryField, Object> metaMap) throws XMPException {
		for (Map.Entry<QueryField, Object> entry : metaMap.entrySet()) {
			QueryField qfield = entry.getKey();
			String xmpNs = qfield.getXmpNs() == null ? null : qfield.getXmpNs().uri;
			String qpath = qfield.getPath();
			Object value = entry.getValue();
			if (qfield.getCard() != 1) {
				if (value instanceof String[]) {
					PropertyOptions optionsArray = new PropertyOptions(PropertyOptions.ARRAY);
					String[] array = (String[]) value;
					for (int j = 0; j < array.length; j++)
						if (array[j] != null && !array[j].isEmpty())
							xmpMeta.appendArrayItem(xmpNs, qpath, optionsArray, array[j], null);
				}
			} else if (value != null && !value.equals(qfield.getDefaultValue()))
				xmpMeta.setProperty(xmpNs, qpath, value);
		}
		return xmpMeta;
	}

	private static void collectMetadata(Map<QueryField, Object> metaMap, Part part)
			throws IOException, UnsupportedEncodingException {
		String value = null;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			HTTPServer.transfer(part.getBody(), out, -1);
			value = out.toString("UTF-8").trim(); //$NON-NLS-1$
		}
		if (!value.isEmpty()) {
			String name = part.getName();
			if ("artist".equals(name)) //$NON-NLS-1$
				metaMap.put(QueryField.EXIF_ARTIST, value);
			else if ("event".equals(name)) //$NON-NLS-1$
				metaMap.put(QueryField.IPTC_EVENT, value);
			else if ("keywords".equals(name)) { //$NON-NLS-1$
				List<String> kwlist = new ArrayList<String>();
				StringTokenizer st = new StringTokenizer(value, "\r\n"); //$NON-NLS-1$
				while (st.hasMoreTokens()) {
					String t = st.nextToken().trim();
					if (!t.isEmpty())
						kwlist.add(t);
				}
				if (!kwlist.isEmpty())
					metaMap.put(QueryField.IPTC_KEYWORDS, kwlist.toArray(new String[kwlist.size()]));
			} else if ("timeshift".equals(name)) //$NON-NLS-1$
				try {
					int v = Integer.parseInt(value);
					if (v != 0)
						metaMap.put(QueryField.TIMESHIFT, v);
				} catch (NumberFormatException e) {
					// ignore
				}
		}
	}

	private String compileLightbox(IDbManager dbManager, boolean sorted, String search, SmartCollection sm, int pos,
			String targetId, String searchcrumb, boolean keepIds, String labelTemplate,
			Map<String, String> substitutions, IdentifiableObject obj, IAssetFilter[] filter) {
		try {
			IScoreFormatter scoreFormatter = null;
			List<Asset> assets = null;
			if (sm != null) {
				ICollectionProcessor processor = dbManager.createCollectionProcessor(sm, filter, null);
				scoreFormatter = processor.getScoreFormatter();
				assets = processor.select(sorted);
			}
			substitutions.put("{$breadcrumbs}", computeBreadcrumbs(obj, searchcrumb)); //$NON-NLS-1$
			substitutions.put("{$members}", computeMembers(dbManager, obj)); //$NON-NLS-1$
			List<String> thumbIds = computeThumbs(assets, pos, targetId, search, labelTemplate, scoreFormatter,
					substitutions);
			substitutions.put("{$keywords}", computeDataOptions(dbManager)); //$NON-NLS-1$
			substitutions.put("{$title}", toHtml(preferenceStore.getString(PreferenceConstants.IMAGESTITLE), false)); //$NON-NLS-1$
			boolean allowUploads = preferenceStore.getBoolean(PreferenceConstants.ALLOWUPLOADS);
			substitutions.put("{$$addons}", allowUploads ? null : ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (allowUploads) {
				String password = CommonUtilities.decode(preferenceStore.getString(PreferenceConstants.PASSWORD));
				substitutions.put("{$addonLink}", //$NON-NLS-1$
						password.isEmpty() ?  "uploads.html" : "login.html"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			substitutions.put("{$$hasThumbnails}", //$NON-NLS-1$
					thumbIds != null && !thumbIds.isEmpty() ? null : ""); //$NON-NLS-1$
			substitutions.put("{$slideshowLink}", //$NON-NLS-1$
					createSlideshowLink(targetId, search, searchcrumb, thumbIds, keepIds, pos));
			boolean fullSearch = Core.getCore().getDbFactory().getLireServiceVersion() >= 0
					&& !Core.getCore().getDbManager().getMeta(true).getNoIndex();
			substitutions.put("{$$cbir}", fullSearch ? null : ""); //$NON-NLS-1$ //$NON-NLS-2$
			return WebserverActivator.getDefault().compilePage(PreferenceConstants.LIGHTBOX, substitutions);
		} catch (Exception e) {
			return null;
		}
	}

	private static String createSlideshowLink(String targetId, String search, String title, List<String> thumbIds,
			boolean keepIds, int pos) {
		StringBuilder sb = new StringBuilder(PreferenceConstants.SLIDESHOW);
		boolean first = true;
		if (pos > 0) {
			sb.append("?pos=").append(pos); //$NON-NLS-1$
			first = false;
		}
		if (targetId != null) {
			sb.append(first ? '?' : '&').append("targetId=").append(targetId); //$NON-NLS-1$
			first = false;
		}
		if (!keepIds && search != null && !search.isEmpty()) {
			sb.append(first ? '?' : '&').append("search=").append(Core.encodeUrlSegment(search)); //$NON-NLS-1$
			first = false;
		}
		if (title != null) {
			sb.append(first ? '?' : '&').append("title=").append(Core.encodeUrlSegment(title)); //$NON-NLS-1$
			first = false;
		}
		if (keepIds && thumbIds != null && !thumbIds.isEmpty()) {
			sb.append(first ? '?' : '&').append("ids="); //$NON-NLS-1$
			boolean follows = false;
			for (String id : thumbIds) {
				if (follows)
					sb.append(',');
				sb.append(id);
				follows = true;
			}
		}
		return sb.toString();
	}

	private static SmartCollection searchSimilar(String id, String name, byte[] img) {
		QueryOptions queryOptions = UiActivator.getDefault().getQueryOptions();
		int method = queryOptions.getMethod();
		int validMethod = -1;
		Set<String> cbirAlgorithms = CoreActivator.getDefault().getCbirAlgorithms();
		Algorithm algorithm = Core.getCore().getDbFactory().getLireService(true).getAlgorithmById(method);
		if (algorithm != null && cbirAlgorithms.contains(algorithm.getName()))
			validMethod = method;
		if (validMethod >= 0) {
			SimilarityOptions_typeImpl simOptions = new SimilarityOptions_typeImpl(validMethod,
					queryOptions.getMaxHits(), queryOptions.getScore() / 100f, 0, 1, 10, 30, id, 0);
			simOptions.setPngImage(img);
			SmartCollection sm = new SmartCollectionImpl(name, false, false, true, false, null, 0, null, 0, null,
					Constants.INHERIT_LABEL, null, 0, 1, null);
			sm.addCriterion(new CriterionImpl(ICollectionProcessor.SIMILARITY, null, simOptions, null,
					Constants.TEXTSEARCHOPTIONS_DEFAULT_MIN_SCORE, true));
			return sm;
		}
		return null;
	}

	private String computeMetadata(Asset asset) {
		StringBuilder sb = new StringBuilder();
		if (preferenceStore.getBoolean(PreferenceConstants.METADATA)) {
			sb.append("<dl>\n"); //$NON-NLS-1$
			int flags = getMediaFlags(asset);
			for (QueryField qfield : WebserverActivator.getDefault().getWebNodes())
				if (qfield.testFlags(flags)) {
					Object value = qfield.obtainFieldValue(asset);
					String text = qfield.value2text(value, ""); //$NON-NLS-1$
					if (text != null && !text.isEmpty() && text != Format.MISSINGENTRYSTRING) {
						if (qfield.getUnit() != null)
							text = qfield.addUnit(text, " ", ""); //$NON-NLS-1$ //$NON-NLS-2$
						else if (value instanceof String[] && ((String[]) value).length > 0)
							text = UiUtilities.addExplanation(qfield, (String[]) value, text);
						text = qfield.appendQuestionMark(asset, text);
						sb.append("<dt>").append(toHtml(qfield.getLabel(), false)) //$NON-NLS-1$
								.append("</dt><dd>").append(toHtml(text, false)).append("</dd>\n"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			sb.append("</dl>"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	private static String computeImageLink(AssetImpl asset, String imageId, String caption) {
		String uri = asset.getUri();
		StringBuilder sb = new StringBuilder(256);
		if (uri.startsWith("http")) //$NON-NLS-1$
			sb.append(" src=\"").append(uri).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		else
			sb.append(" src=\"jpg/").append(imageId).append("_500\" srcset=\"jpg/") //$NON-NLS-1$ //$NON-NLS-2$
					.append(imageId).append("_1000 1000w, jpg/").append(imageId).append("_2000 2000w, jpg/") //$NON-NLS-1$ //$NON-NLS-2$
					.append(imageId).append("_3000 3000w\" "); // $NON-NLS-1$ //$NON-NLS-1$
		return sb.append("alt=\"").append(caption).append("\" title=\"") //$NON-NLS-1$//$NON-NLS-2$
				.append(toHtml(caption, false)).toString(); // $NON-NLS-1$
	}

	private String computeDataOptions(IDbManager dbManager) {
		return keywordsToOptions(dbManager.getMeta(true).getKeywords());
	}

	private String computeMembers(IDbManager dbManager, IdentifiableObject obj) {
		List<IIdentifiableObject> children = new ArrayList<>();
		if (obj instanceof Group) {
			Group gr = (Group) obj;
			if (gr.getSubgroup() != null)
				children.addAll(gr.getSubgroup());
			List<String> ids = gr.getRootCollection();
			if (ids != null && !ids.isEmpty()) {
				String groupId = gr.getStringId();
				for (IdentifiableObject c : dbManager.obtainByIds(IdentifiableObject.class, ids))
					if (c instanceof SmartCollectionImpl
							&& groupId.equals(((SmartCollectionImpl) c).getGroup_rootCollection_parent()))
						children.add(c);
			}
		} else if (obj instanceof SmartCollection)
			children.addAll(new HashSet<>(((SmartCollection) obj).getSubSelection())); // avoid duplicates
		else
			for (GroupImpl group : dbManager.obtainObjects(GroupImpl.class))
				if (group.getGroup_subgroup_parent() == null && hasChildren(group) && isVisible(group.getStringId()))
					children.add(group);
		return membersToList(children);
	}

	private boolean isVisible(String id) {
		int groups = preferenceStore.getInt(PreferenceConstants.GROUPS);
		return (((groups & PreferenceConstants.AUTOMATIC) != 0 && Constants.GROUP_ID_AUTO.equals(id))
				|| ((groups & PreferenceConstants.CATEGERIES) != 0 && Constants.GROUP_ID_CATEGORIES.equals(id))
				|| ((groups & PreferenceConstants.DIRECTORIES) != 0 && Constants.GROUP_ID_FOLDERSTRUCTURE.equals(id))
				|| ((groups & PreferenceConstants.IMPORTS) != 0 && Constants.GROUP_ID_IMPORTS.equals(id))
				|| ((groups & PreferenceConstants.LOCATIONS) != 0 && Constants.GROUP_ID_LOCATIONS.equals(id))
				|| ((groups & PreferenceConstants.MEDIA) != 0 && Constants.GROUP_ID_MEDIA.equals(id))
				|| ((groups & PreferenceConstants.PERSONS) != 0 && Constants.GROUP_ID_PERSONS.equals(id))
				|| ((groups & PreferenceConstants.RATINGS) != 0 && Constants.GROUP_ID_RATING.equals(id))
				|| ((groups & PreferenceConstants.TIMELINE) != 0 && Constants.GROUP_ID_TIMELINE.equals(id))
				|| ((groups & PreferenceConstants.USER) != 0 && Constants.GROUP_ID_USER.equals(id)));
	}

	private static boolean hasChildren(Object parent) {
		if (parent instanceof GroupImpl) {
			GroupImpl group = (GroupImpl) parent;
			if (group.getRootCollection() != null && !group.getRootCollection().isEmpty())
				return true;
			if (group.getSubgroup() != null)
				for (Group subGroup : group.getSubgroup())
					if (hasChildren(subGroup))
						return true;
			return false;
		}
		return (parent instanceof SmartCollection) ? !((SmartCollection) parent).getSubSelection().isEmpty() : false;
	}

	private List<String> computeThumbs(List<Asset> assets, int pos, String collId, String search, String template,
			IScoreFormatter scoreFormatter, Map<String, String> substitutions) {
		List<String> assetIds = null;
		String more = ""; //$NON-NLS-1$
		int card = preferenceStore.getInt(PreferenceConstants.THUMBNAILSPERPAGE);
		StringBuilder sb = new StringBuilder(card * 200);
		if (assets != null) {
			assetIds = new ArrayList<String>(assets.size());
			boolean skipOrphans = !preferenceStore.getBoolean(PreferenceConstants.ORPHANS);
			int privacy = preferenceStore.getInt(PreferenceConstants.PRIVACY);
			if (privacy < QueryField.SAFETY_RESTRICTED || skipOrphans) {
				int upper = assets.size();
				boolean first = true;
				for (int i = pos, j = 0; i < upper; i++) {
					Asset asset = assets.get(i);
					if (isValid(asset, privacy, skipOrphans)) {
						if (j++ >= card) {
							if (collId != null || search != null)
								for (int k = i + 1; k < upper; k++)
									if (isValid(asset, privacy, skipOrphans)) {
										substitutions.put("{$moreLink}", createMoreLink(i + 1, collId, search)); //$NON-NLS-1$
										more = null;
										break;
									}
							break;
						}
						assetIds.add(createSection(sb, asset, pos, collId, search, template, scoreFormatter, first));
						first = false;
					}
				}
			} else {
				int upper = Math.min(assets.size(), pos + card);
				for (int i = pos; i < upper; i++)
					assetIds.add(
							createSection(sb, assets.get(i), pos, collId, search, template, scoreFormatter, i == pos));
				if (pos + card < assets.size() && (collId != null || search != null)) {
					substitutions.put("{$moreLink}", createMoreLink(pos + card, collId, search)); //$NON-NLS-1$
					more = null;
				}
			}
		}
		substitutions.put("{$$more}", more); //$NON-NLS-1$
		substitutions.put("{$thumbnails}", sb.toString()); //$NON-NLS-1$
		return assetIds;
	}

	private static boolean isValid(Asset asset, int privacy, boolean skipOrphans) {
		return asset.getSafety() <= privacy && (!skipOrphans || volumeManager.findExistingFile(asset, true) != null);
	}

	private String createSection(StringBuilder sb, Asset asset, int pos, String collId, String search, String template,
			IScoreFormatter scoreFormatter, boolean first) {
		String assetId = asset.getStringId();
		boolean isWeb = asset.getUri().startsWith("http"); //$NON-NLS-1$
		boolean isLocal = volumeManager.findExistingFile(asset, true) != null;
		boolean isPhoto = (isLocal || isWeb) && getMediaFlags(asset) == IMediaSupport.PHOTO;
		boolean isVideo = false;
		sb.append("<section class=\"column\">\n"); //$NON-NLS-1$
		if (isPhoto)
			sb.append("<a href=\"view.html?imageId=").append(assetId) //$NON-NLS-1$
					.append("\" target=\"_blank\">"); //$NON-NLS-1$
		// else {
		// isVideo = (isLocal || isWeb) && getMediaFlags(asset) == IMediaSupport.VIDEO
		// && ImageActivator.getDefault().getVideoService() != null;
		// if (isVideo)
		// sb.append("<a href=\"video.html?imageId=").append(assetId) //$NON-NLS-1$
		// .append("\" target=\"_blank\">"); //$NON-NLS-1$
		// }
		String caption = toHtml(WebserverActivator.getDefault().getCaptionProcessor().computeImageCaption(asset,
				scoreFormatter, null, null, template, false), false);
		sb.append("<figure class=\"thumbnail"); //$NON-NLS-1$
		if (!isPhoto && !isVideo)
			sb.append(" offline"); //$NON-NLS-1$
		if (first)
			sb.append("\" id=\"firstthumb"); //$NON-NLS-1$
		Rectangle bounds = getImageBounds(asset);
		sb.append("\"><img class=\"thumbnail\" src=\"jpg/").append(assetId) //$NON-NLS-1$
				.append("\" width=\"").append(bounds.width).append("\" height=\"").append(bounds.height) //$NON-NLS-1$ //$NON-NLS-2$
				.append("\" alt=\"").append(caption).append("\"/>\n") //$NON-NLS-1$ //$NON-NLS-2$
				.append("<figcaption class=\"thumbnail\" style=\"text-align: ") //$NON-NLS-1$
				.append(labelAlignment == 0 ? "left" : labelAlignment == 2 ? "right" : "center").append(";\">") //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				.append(caption).append("</figcaption>\n</figure>"); //$NON-NLS-1$
		if (isPhoto || isVideo)
			sb.append("</a>"); //$NON-NLS-1$
		sb.append("\n</section>\n"); //$NON-NLS-1$
		return assetId;
	}

	private static int getMediaFlags(Asset asset) {
		IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(asset.getFormat());
		return mediaSupport != null ? mediaSupport.getPropertyFlags() : QueryField.PHOTO;
	}

}