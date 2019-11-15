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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Rectangle;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibition;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.cat.model.group.slideShow.Slide;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShow;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGallery;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IGeoService;
import com.bdaum.zoom.webserver.PreferenceConstants;

import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;

@SuppressWarnings("restriction")
final class SlideshowsContextHandler extends AbstractLightboxContextHandler {

	private Comparator<SlideShow> slideshowComparator = new Comparator<SlideShow>() {
		@Override
		public int compare(SlideShow o1, SlideShow o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	public synchronized int serve(Request req, Response resp) throws IOException {
		if (!preferenceStore.getBoolean(PreferenceConstants.SLIDESHOWS))
			return 403;
		String startPage = preferenceStore.getString(PreferenceConstants.STARTPAGE);
		Map<String, String> params = req.getParams();
		String path = req.getPath();
		String content = null;
		String ctype = "text/html"; //$NON-NLS-1$
		String search = null;
		String targetId = null;
		int pos = 0;
		IDbManager dbManager = Core.getCore().getDbManager();
		if (path.endsWith(".jpg")) //$NON-NLS-1$
			return sendThumbnail(resp, path.substring(path.lastIndexOf('/') + 1, path.length() - 4));
		String slideshowsPath = '/' + preferenceStore.getString(PreferenceConstants.SLIDESHOWSPATH);
		String jpeg = slideshowsPath + "/jpg/"; //$NON-NLS-1$
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
		Map<String, String> substitutions = new HashMap<String, String>();
		substitutions.put("{$appName}", Constants.APPLICATION_NAME); //$NON-NLS-1$
		substitutions.put("{$orgLink}", System.getProperty("com.bdaum.zoom.homePage")); //$NON-NLS-1$ //$NON-NLS-2$
		if (path.endsWith("/lightbox.html") || path.endsWith('/' + startPage)) { //$NON-NLS-1$
			List<Group> allMembers = computeMembers(dbManager, null);
			List<Group> members = null;
			Set<SlideShow> slideshows = null;
			Group obj = null;
			if ("POST".equals(req.getMethod())) //$NON-NLS-1$
				search = Core.decodeUrl(params.get("text")); //$NON-NLS-1$
			else {
				pos = parseInteger(params.get("pos"), 0); //$NON-NLS-1$
				targetId = params.get("targetId"); //$NON-NLS-1$
				search = Core.decodeUrl(params.get("search")); //$NON-NLS-1$
			}
			if (search != null && !search.isEmpty()) {
				slideshows = searchByKeyword(dbManager, allMembers, search);
				members = allMembers;
				pos = 0;
			} else if (targetId != null) {
				members = computeMembers(dbManager, obj = dbManager.obtainById(Group.class, targetId));
				slideshows = extractSlideshows(dbManager, Arrays.asList(obj));
			} else
				members = allMembers;
			substitutions.put("{$breadcrumbs}", computeBreadcrumbs(obj, search)); //$NON-NLS-1$
			substitutions.put("{$members}", membersToList(members)); //$NON-NLS-1$
			computeThumbs(dbManager, slideshows, pos, targetId, search, substitutions);
			substitutions.put("{$keywords}", ""); //$NON-NLS-1$ //$NON-NLS-2$
			substitutions.put("{$title}", preferenceStore.getString(PreferenceConstants.SLIDESHOWSTITLE)); //$NON-NLS-1$
			substitutions.put("{$$cbir}", ""); //$NON-NLS-1$ //$NON-NLS-2$
			substitutions.put("{$$addons}", ""); //$NON-NLS-1$ //$NON-NLS-2$
			substitutions.put("{$$hasThumbnails}", ""); //$NON-NLS-1$ //$NON-NLS-2$
			content = WebserverActivator.getDefault().compilePage(PreferenceConstants.LIGHTBOX, substitutions);
		} else if (path.endsWith("/slideshow.html")) { //$NON-NLS-1$
			String slideshowId = params.get("slideshowId"); //$NON-NLS-1$
			compileSlideshow(slideshowId, substitutions);
			content = WebserverActivator.getDefault().compilePage(PreferenceConstants.SLIDESHOW, substitutions);
		}
		return sendDynamicContent(resp, content, ctype);
	}

	private List<Group> computeMembers(IDbManager dbManager, Group gr) {
		List<Group> children = new ArrayList<>();
		if (gr != null) {
			if (gr.getSubgroup() != null)
				for (Group group : gr.getSubgroup())
					if (hasSlideshows(group))
						children.add(group);
		} else
			for (GroupImpl group : new ArrayList<GroupImpl>(dbManager.obtainObjects(GroupImpl.class)))
				if (group.getGroup_subgroup_parent() == null && hasSlideshows(group))
					children.add(group);
		return children;
	}

	private boolean hasSlideshows(Group group) {
		if (group != null) {
			if (group.getSlideshow() != null && !group.getSlideshow().isEmpty())
				return true;
			for (Group child : group.getSubgroup())
				if (hasSlideshows(child))
					return true;
		}
		return false;
	}

	private Set<SlideShow> searchByKeyword(IDbManager dbManager, List<Group> allMembers, String search) {
		Set<SlideShow> result = new HashSet<SlideShow>();
		searchByKeyword(dbManager, result, allMembers, search);
		return result;
	}

	private void searchByKeyword(IDbManager dbManager, Collection<SlideShow> result, List<Group> members,
			String search) {
		for (Group child : members) {
			List<String> slideshows = child.getSlideshow();
			if (slideshows != null)
				for (String id : slideshows) {
					SlideShow slideshow = dbManager.obtainById(SlideShow.class, id);
					if (slideshow != null) {
						String name = slideshow.getName();
						if (name != null && name.toLowerCase().contains(search.toLowerCase()))
							result.add(slideshow);
						String des = slideshow.getDescription();
						if (des != null && des.toLowerCase().contains(search.toLowerCase()))
							result.add(slideshow);
					}
				}
			searchByKeyword(dbManager, result, child.getSubgroup(), search);
		}
	}

	private Set<SlideShow> extractSlideshows(IDbManager dbManager, List<Group> members) {
		Set<SlideShow> result = new HashSet<SlideShow>();
		extractSlideshows(dbManager, result, members);
		return result;
	}

	private void extractSlideshows(IDbManager dbManager, Set<SlideShow> result, List<Group> members) {
		for (Group child : members) {
			List<String> slideshows = child.getSlideshow();
			if (slideshows != null)
				for (String id : slideshows) {
					SlideShow slideshow = dbManager.obtainById(SlideShow.class, id);
					if (slideshow != null)
						result.add(slideshow);
				}
			extractSlideshows(dbManager, result, child.getSubgroup());
		}
	}

	private void computeThumbs(IDbManager dbManager, Set<SlideShow> ga, int pos, String collId, String search,
			Map<String, String> substitutions) {
		String more = ""; //$NON-NLS-1$
		int card = preferenceStore.getInt(PreferenceConstants.THUMBNAILSPERPAGE);
		StringBuilder sb = new StringBuilder(card * 200);
		if (ga != null) {
			List<SlideShow> galleries = new ArrayList<>(ga);
			Collections.sort(galleries, slideshowComparator);
			int upper = galleries.size();
			int j = 0;
			boolean first = true;
			for (int i = pos; i < upper; i++) {
				if (j++ >= card) {
					for (int k = i + 1; k < upper; k++)
						substitutions.put("{$moreLink}", createMoreLink(i + 1, collId, search)); //$NON-NLS-1$
					more = null;
					break;
				}
				createSection(dbManager, sb, galleries.get(i), pos, collId, search, first);
				first = false;
			}
		}
		substitutions.put("{$$more}", more); //$NON-NLS-1$
		substitutions.put("{$thumbnails}", sb.toString()); //$NON-NLS-1$
	}

	private void createSection(IDbManager dbManager, StringBuilder sb, SlideShow slideshow, int pos, String collId,
			String search, boolean first) {
		Asset firstAsset = null;
		int privacy = preferenceStore.getInt(PreferenceConstants.PRIVACY);
		l: for (String id : slideshow.getEntry()) {
			Slide slide = dbManager.obtainById(Slide.class, id);
			if (slide != null && slide.getSafety() <= privacy) {
				String assetId = slide.getAsset();
				if (assetId != null) {
					AssetImpl asset = dbManager.obtainAsset(assetId);
					if (asset != null) {
						firstAsset = asset;
						break l;
					}
				}
			}
		}
		sb.append("<section class=\"column\">\n"); //$NON-NLS-1$
		sb.append("<a target=\"_blank\"  href=\"slideshow.html?slideshowId=").append(slideshow.getStringId()) //$NON-NLS-1$
				.append("\" rel=\"noopener\">"); // $NON-NLS-1 //$NON-NLS-1$
		sb.append("<img class=\"thumbnail\" "); //$NON-NLS-1$
		if (first)
			sb.append("id=\"firstthumb\" "); //$NON-NLS-1$
		Rectangle bounds = getImageBounds(firstAsset);
		String htmlName = toHtml(slideshow.getName(), false);
		sb.append("src=\"") //$NON-NLS-1$
				.append(firstAsset == null ? "../files/nothumb.png" : firstAsset.getStringId()) //$NON-NLS-1$
				.append(".jpg\" width=\"").append(bounds.width).append("\" height=\"").append(bounds.height) //$NON-NLS-1$//$NON-NLS-2$
				.append("\" alt=\"").append(htmlName).append("\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("</a>"); //$NON-NLS-1$
		sb.append("\n<p class=\"caption\">").append(htmlName) //$NON-NLS-1$
				.append("</p>\n</section>\n"); //$NON-NLS-1$
	}

	private void compileSlideshow(String id, Map<String, String> substitutions) {
		StringBuilder sb = new StringBuilder(1024);
		StringBuilder sc = new StringBuilder(1024);
		StringBuilder sp = new StringBuilder(1024);
		IDbManager dbManager = Core.getCore().getDbManager();
		IdentifiableObject obj = dbManager.obtainById(IdentifiableObject.class, id);
		String title = null;
		boolean hasGeo = false;
		IGeoService geoService = null;
		int captionDur = 3600000;
		if (obj instanceof SlideShow) {
			geoService = CoreActivator.getDefault().getGeoService();
			SlideShow slideshow = (SlideShow) obj;
			title = slideshow.getName();
			captionDur = slideshow.getTitleDisplay();
			int privacy = preferenceStore.getInt(PreferenceConstants.PRIVACY);
			List<String> entries = slideshow.getEntry();
			List<Slide> slides = new ArrayList<>(entries.size());
			List<Asset> assets = new ArrayList<>(entries.size());
			for (String entry : entries) {
				SlideImpl slide = dbManager.obtainById(SlideImpl.class, entry);
				if (slide != null && slide.getSafety() <= privacy) {
					String assetId = slide.getAsset();
					Asset asset = dbManager.obtainAsset(assetId);
					if (assetId == null || asset != null) {
						slides.add(slide);
						assets.add(asset);
					}
				}
			}
			int layout = Constants.SLIDE_NO_THUMBNAILS;
			for (int i = 0; i < slides.size(); i++) {
				Slide slide = slides.get(i);
				if (slide.getAsset() == null) {
					layout = slide.getLayout();
					if (layout == Constants.SLIDE_MAP_LEFT || layout == Constants.SLIDE_MAP_RIGHT)
						if (geoService != null && hasGeoAssets(assets, i + 1)) {
							hasGeo = true;
						} else
							layout = Constants.SLIDE_NO_THUMBNAILS;
				}
				addSlide(slideshow, sb, sc, sp, slide, assets.get(i), layout);
			}
		} else if (obj instanceof Exhibition) {
			Exhibition exhibition = (Exhibition) obj;
			title = exhibition.getName();
			int i = 0;
			for (Wall wall : exhibition.getWall()) {
				addOfflineSlide(null, NLS.bind("Wall {0}", ++i), i == 1 ? toHtml(exhibition.getInfo(), true) : "", sb, //$NON-NLS-1$ //$NON-NLS-2$
						sc, sp);
				for (String exhibitId : wall.getExhibit()) {
					ExhibitImpl exhibit = dbManager.obtainById(ExhibitImpl.class, exhibitId);
					if (exhibit != null) {
						String assetId = exhibit.getAsset();
						if (dbManager.obtainAsset(assetId) != null)
							addOfflineSlide(assetId, toHtml(exhibit.getTitle(), false), "", sb, sc, sp); //$NON-NLS-1$
					}
				}
			}
		} else if (obj instanceof WebGallery) {
			WebGallery gallery = (WebGallery) obj;
			title = gallery.getName();
			for (Storyboard board : gallery.getStoryboard()) {
				String description = board.getDescription();
				if (!board.getHtmlDescription())
					description = toHtml(description, true);
				addOfflineSlide(null, board.getTitle(), description, sb, sc, sp); // $NON-NLS-1$
				for (String exhibitId : board.getExhibit()) {
					WebExhibitImpl exhibit = dbManager.obtainById(WebExhibitImpl.class, exhibitId);
					if (exhibit != null) {
						String assetId = exhibit.getAsset();
						if (dbManager.obtainAsset(assetId) != null)
							addOfflineSlide(assetId, exhibit.getCaption(), "", sb, sc, sp); //$NON-NLS-1$
					}
				}
			}
		}
		if (title != null) {
			substitutions.put("{$title}", toHtml(title, false)); //$NON-NLS-1$
			substitutions.put("{$slides}", sb.toString()); //$NON-NLS-1$
			substitutions.put("{$captions}", sc.toString()); //$NON-NLS-1$
			substitutions.put("{$properties}", sp.toString()); //$NON-NLS-1$
			substitutions.put("{$captionDur}", String.valueOf(captionDur)); //$NON-NLS-1$
			substitutions.put("{$mapContext}", createMapContext(hasGeo ? geoService : null)); //$NON-NLS-1$
		}
	}

	private static String createMapContext(IGeoService geoService) {
		String context = geoService != null ? geoService.getMapContext("") : null; //$NON-NLS-1$
		if (context != null && !context.isEmpty())
			return context;
		return "<script>\n\tfunction setupMap() {};\n\tfunction disposeMap() {};\n</script>\n"; //$NON-NLS-1$ /
	}

	private static boolean hasGeoAssets(List<Asset> assets, int start) {
		for (int i = start; i < assets.size(); i++) {
			Asset asset = assets.get(i);
			if (asset == null)
				break;
			if (!Double.isNaN(asset.getGPSLatitude()) && !Double.isNaN(asset.getGPSLongitude()))
				return true;
		}
		return false;
	}

	private static void addOfflineSlide(String assetId, String title, String description, StringBuilder sb,
			StringBuilder sc, StringBuilder sp) {
		if (sb.length() > 0) {
			sb.append(",\n\t"); //$NON-NLS-1$
			sc.append(",\n\t"); //$NON-NLS-1$
			sp.append(",\n\t"); //$NON-NLS-1$
		}
		if (assetId == null)
			sb.append("null"); //$NON-NLS-1$
		else
			sb.append('"').append(assetId).append('"');
		sc.append('"').append(title).append('"');
		if (description.isEmpty())
			sp.append("'{}'"); //$NON-NLS-1$
		else
			sp.append("'{\"descr\": \"").append(description).append("\"}'"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static void addSlide(SlideShow slideshow, StringBuilder sb, StringBuilder sc, StringBuilder sp, Slide slide,
			Asset asset, int lastLayout) {
		String assetId = slide.getAsset();
		if (sb.length() > 0) {
			sb.append(",\n\t"); //$NON-NLS-1$
			sc.append(",\n\t"); //$NON-NLS-1$
			sp.append(",\n\t"); //$NON-NLS-1$
		}
		if (assetId != null)
			sb.append('"').append(assetId).append('"');
		else
			sb.append("null"); //$NON-NLS-1$
		if (slideshow.getTitleDisplay() > 0 || asset == null) {
			String title;
			switch (slideshow.getTitleContent()) {
			case Constants.SLIDE_TITLE_SNO:
				title = NLS.bind("{2} ({0}/{1})", //$NON-NLS-1$
						new Object[] { slide.getSequenceNo(), slideshow.getEntry().size(), slide.getCaption() });
				break;
			case Constants.SLIDE_SNOONLY:
				title = NLS.bind("{0}/{1}", slide.getSequenceNo(), slideshow //$NON-NLS-1$
						.getEntry().size());
				break;
			case Constants.SLIDE_TITLE_NE:
				if (asset != null && slide.getCaption().equals(Core.getFileName(asset.getUri(), true))) {
					title = ""; //$NON-NLS-1$
					break;
				}
				//$FALL-THROUGH$
			default:
				title = slide.getCaption();
				break;
			}
			sc.append('"').append(toHtml(title, false)).append('"');
		}
		sp.append("'{\"dly\": ").append(slide.getDelay()).append(", \"dur\": ").append(slide.getDuration()) //$NON-NLS-1$ //$NON-NLS-2$
				.append(", \"eff\": ").append(slide.getEffect()).append(", \"in\": ").append(slide.getFadeIn()) //$NON-NLS-1$ //$NON-NLS-2$
				.append(", \"out\": ").append(slide.getFadeOut()); //$NON-NLS-1$
		if (asset != null && !Double.isNaN(asset.getGPSLatitude()) && !Double.isNaN(asset.getGPSLongitude())
				&& (lastLayout == Constants.SLIDE_MAP_LEFT || lastLayout == Constants.SLIDE_MAP_RIGHT))
			sp.append(", \"lat\": ").append(asset.getGPSLatitude()).append(", \"lon\": ") //$NON-NLS-1$//$NON-NLS-2$
					.append(asset.getGPSLongitude());
		int zoom = slide.getZoom();
		if (zoom > 0)
			sp.append(", \"zoom\": ").append(zoom).append(", \"zoomX\": ").append(slide.getZoomX()) //$NON-NLS-1$ //$NON-NLS-2$
					.append(", \"zoomY\": ").append(slide.getZoomY()); //$NON-NLS-1$
		if (assetId == null)
			sp.append(", \"lay\": ").append(lastLayout); //$NON-NLS-1$
		String description = slide.getDescription();
		if (description != null && !description.isEmpty())
			sp.append(", \"descr\": \"").append(toHtml(slide.getDescription(), false)).append('"'); //$NON-NLS-1$
		sp.append("}'"); //$NON-NLS-1$
	}
}