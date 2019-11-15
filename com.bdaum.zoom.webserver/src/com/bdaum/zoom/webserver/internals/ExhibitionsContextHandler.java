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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibit;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibition;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.webserver.PreferenceConstants;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;

final class ExhibitionsContextHandler extends AbstractLightboxContextHandler {

	private Comparator<Exhibition> exhibitionComparator = new Comparator<Exhibition>() {
		@Override
		public int compare(Exhibition o1, Exhibition o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	public synchronized int serve(Request req, Response resp) throws IOException {
		if (!preferenceStore.getBoolean(PreferenceConstants.EXHIBITIONS))
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
		String byId = '/' + preferenceStore.getString(PreferenceConstants.EXHIBITIONPATH) + "/byId/"; //$NON-NLS-1$
		if (path.startsWith(byId))
			return handleExternalPath(path.substring(byId.length()), req, resp);
		if (path.endsWith(".jpg")) //$NON-NLS-1$
			return sendThumbnail(resp, path.substring(path.lastIndexOf('/') + 1, path.length() - 4));
		if (path.endsWith("/lightbox.html") || path.endsWith('/' + startPage)) { //$NON-NLS-1$ /
			Map<String, String> substitutions = new HashMap<String, String>();
			substitutions.put("{$appName}", Constants.APPLICATION_NAME); //$NON-NLS-1$
			substitutions.put("{$orgLink}", System.getProperty("com.bdaum.zoom.homePage")); //$NON-NLS-1$ //$NON-NLS-2$
			List<Group> allMembers = computeMembers(dbManager, null);
			List<Group> members = null;
			Set<Exhibition> exhibitions = null;
			Group obj = null;
			if ("POST".equals(req.getMethod())) //$NON-NLS-1$
				search = Core.decodeUrl(params.get("text")); //$NON-NLS-1$
			else {
				pos = parseInteger(params.get("pos"), 0); //$NON-NLS-1$
				targetId = params.get("targetId"); //$NON-NLS-1$
				search = Core.decodeUrl(params.get("search")); //$NON-NLS-1$
			}
			if (search != null && !search.isEmpty()) {
				exhibitions = searchByKeyword(dbManager, allMembers, search);
				members = allMembers;
				pos = 0;
			} else if (targetId != null) {
				members = computeMembers(dbManager, obj = dbManager.obtainById(Group.class, targetId));
				exhibitions = extractExhibitions(dbManager, Arrays.asList(obj));
			} else
				members = allMembers;
			substitutions.put("{$breadcrumbs}", computeBreadcrumbs(obj, search)); //$NON-NLS-1$
			substitutions.put("{$members}", membersToList(members)); //$NON-NLS-1$
			computeThumbs(dbManager, exhibitions, pos, targetId, search, substitutions);
			substitutions.put("{$keywords}", computeDataOptions(exhibitions)); //$NON-NLS-1$
			substitutions.put("{$title}", preferenceStore.getString(PreferenceConstants.EXHIBITIONSTITLE)); //$NON-NLS-1$
			substitutions.put("{$$cbir}", ""); //$NON-NLS-1$ //$NON-NLS-2$
			substitutions.put("{$$addons}", ""); //$NON-NLS-1$ //$NON-NLS-2$
			substitutions.put("{$$hasThumbnails}", ""); //$NON-NLS-1$ //$NON-NLS-2$
			content = WebserverActivator.getDefault().compilePage(PreferenceConstants.LIGHTBOX, substitutions);
		}
		return sendDynamicContent(resp, content, ctype);
	}

	private String computeDataOptions(Set<Exhibition> exhibitions) {
		Set<String> results = new HashSet<String>();
		if (exhibitions != null)
			for (Exhibition exhibition : exhibitions)
				results.addAll(exhibition.getKeyword());
		return keywordsToOptions(results);
	}

	private List<Group> computeMembers(IDbManager dbManager, Group gr) {
		List<Group> children = new ArrayList<>();
		if (gr != null) {
			if (gr.getSubgroup() != null)
				for (Group group : gr.getSubgroup())
					if (hasExhibition(group))
						children.add(group);
		} else
			for (GroupImpl group : new ArrayList<GroupImpl>(dbManager.obtainObjects(GroupImpl.class)))
				if (group.getGroup_subgroup_parent() == null && hasExhibition(group))
					children.add(group);
		return children;
	}

	private boolean hasExhibition(Group group) {
		if (group != null) {
			if (group.getExhibition() != null && !group.getExhibition().isEmpty())
				return true;
			for (Group child : group.getSubgroup())
				if (hasExhibition(child))
					return true;
		}
		return false;
	}

	private Set<Exhibition> searchByKeyword(IDbManager dbManager, List<Group> allMembers, String search) {
		Set<Exhibition> result = new HashSet<Exhibition>();
		searchByKeyword(dbManager, result, allMembers, search);
		return result;
	}

	private void searchByKeyword(IDbManager dbManager, Collection<Exhibition> result, List<Group> members,
			String search) {
		for (Group child : members) {
			List<String> exhibitions = child.getExhibition();
			if (exhibitions != null)
				for (String id : exhibitions) {
					Exhibition exhibition = dbManager.obtainById(Exhibition.class, id);
					if (exhibition != null)
						for (String kw : exhibition.getKeyword())
							if (search.equalsIgnoreCase(kw))
								result.add(exhibition);
				}
			searchByKeyword(dbManager, result, child.getSubgroup(), search);
		}
	}

	private Set<Exhibition> extractExhibitions(IDbManager dbManager, List<Group> members) {
		Set<Exhibition> result = new HashSet<Exhibition>();
		extractExhibitions(dbManager, result, members);
		return result;
	}

	private void extractExhibitions(IDbManager dbManager, Set<Exhibition> result, List<Group> members) {
		for (Group child : members) {
			List<String> exhibitions = child.getExhibition();
			if (exhibitions != null)
				for (String id : exhibitions) {
					Exhibition exhibition = dbManager.obtainById(Exhibition.class, id);
					if (exhibition != null && isValid(exhibition))
						result.add(exhibition);
				}
			extractExhibitions(dbManager, result, child.getSubgroup());
		}
	}

	private boolean isValid(Exhibition exhibition) {
		return exhibition.getSafety() <= preferenceStore.getInt(PreferenceConstants.PRIVACY);
	}

	private boolean isOnline(Exhibition exhibition) {
		if (!exhibition.getIsFtp())
			return (new File(exhibition.getOutputFolder(), exhibition.getPageName()).exists());
		String ftpDir = exhibition.getFtpDir();
		FtpAccount account = ftpDir == null ? null : FtpAccount.findAccount(ftpDir);
		return account != null && account.getWebUrl() != null;
	}

	private void computeThumbs(IDbManager dbManager, Set<Exhibition> ex, int pos, String collId, String search,
			Map<String, String> substitutions) {
		String more = ""; //$NON-NLS-1$
		int card = preferenceStore.getInt(PreferenceConstants.THUMBNAILSPERPAGE);
		StringBuilder sb = new StringBuilder(card * 200);
		if (ex != null) {
			List<Exhibition> exhibitions = new ArrayList<Exhibition>(ex);
			Collections.sort(exhibitions, exhibitionComparator);
			boolean skipOrphans = !preferenceStore.getBoolean(PreferenceConstants.ORPHANS);
			int privacy = preferenceStore.getInt(PreferenceConstants.PRIVACY);
			int upper = exhibitions.size();
			int j = 0;
			boolean first = true;
			for (int i = pos; i < upper; i++) {
				Exhibition exhibition = exhibitions.get(i);
				if (isValid(exhibition, privacy, skipOrphans)) {
					if (j++ >= card) {
						for (int k = i + 1; k < upper; k++)
							if (isValid(exhibition, privacy, skipOrphans)) {
								substitutions.put("{$moreLink}", createMoreLink(i + 1, collId, search)); //$NON-NLS-1$
								more = null;
								break;
							}
						break;
					}
					createSection(dbManager, sb, exhibitions.get(i), pos, collId, search, first);
					first = false;
				}
			}
		}
		substitutions.put("{$$more}", more); //$NON-NLS-1$
		substitutions.put("{$thumbnails}", sb.toString()); //$NON-NLS-1$
	}

	private static boolean isValid(Exhibition exhibition, int privacy, boolean skipOrphans) {
		return exhibition.getSafety() <= privacy && (!skipOrphans || getExhibitionUrl(exhibition) != null);
	}

	private void createSection(IDbManager dbManager, StringBuilder sb, Exhibition exhibition, int pos, String collId,
			String search, boolean first) {
		Asset firstAsset = null;
		l: for (Wall wall : exhibition.getWall()) {
			for (String id : wall.getExhibit()) {
				Exhibit exhibit = dbManager.obtainById(Exhibit.class, id);
				if (exhibit != null) {
					AssetImpl asset = dbManager.obtainAsset(exhibit.getAsset());
					if (asset != null) {
						firstAsset = asset;
						break l;
					}
				}
			}
		}
		boolean online = isOnline(exhibition);
		String url = online ? getExhibitionUrl(exhibition)
				: "../" + preferenceStore.getString(PreferenceConstants.SLIDESHOWSPATH) + "/slideshow.html?slideshowId=" //$NON-NLS-1$//$NON-NLS-2$
						+ exhibition.getStringId();
		createPresentationSection(sb, firstAsset, url, exhibition.getName(), first, online);
	}

	private static String getExhibitionUrl(Exhibition exhibition) {
		return exhibition.getIsFtp() ? FtpAccount.findAccount(exhibition.getFtpDir()).getWebUrl()
				: "byId/" + exhibition.getStringId() + '/' + exhibition.getPageName(); //$NON-NLS-1$
	}

	private static int handleExternalPath(String path, Request req, Response resp)
			throws MalformedURLException, IOException {
		int p = path.indexOf('/');
		Exhibition exhibition = Core.getCore().getDbManager().obtainById(ExhibitionImpl.class, path.substring(0, p));
		if (exhibition == null || exhibition.getIsFtp())
			return 404;
		File file = new File(exhibition.getOutputFolder(), path.substring(p));
		if (!file.exists())
			return 404;
		HTTPServer.serveFileContent(file, req, resp);
		return 0;
	}

}