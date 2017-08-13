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
package com.bdaum.zoom.net.communities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.scohen.juploadr.app.Category;
import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.app.tags.Tag;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.program.BatchUtilities;

public class CommunityAccount {

	private static final String PASSWORD_LENGTH = "passwordLength"; //$NON-NLS-1$
	private static final String PASSWORD = "password"; //$NON-NLS-1$
	private static final String USED_ALBUMS = "usedAlbums"; //$NON-NLS-1$
	private static final String AVAILABLE_ALBUMS = "availableAlbums"; //$NON-NLS-1$
	private static final String PROPAGATE_CATS = "propagateCats"; //$NON-NLS-1$
	private static final String SUPPORTS_RAW = "supportsRaw"; //$NON-NLS-1$
	private static final String CAN_REPLACE = "canReplace"; //$NON-NLS-1$
	private static final String HOME = "home"; //$NON-NLS-1$
	private static final String VISIT = "visit"; //$NON-NLS-1$
	private static final String TRACK_EXPORT = "trackExport"; //$NON-NLS-1$
	private static final String TOKEN = "token"; //$NON-NLS-1$
	private static final String SECRET = "secret"; //$NON-NLS-1$
	private static final String MAX_FILESIZE = "maxFilesize"; //$NON-NLS-1$
	private static final String MAX_VIDEOSIZE = "maxVideosize"; //$NON-NLS-1$
	private static final String AUTHENTICATED = "authenticated"; //$NON-NLS-1$
	private static final String ALBUMSASSETS = "albumsAsSets"; //$NON-NLS-1$
	private static final String UNLIMITED = "unlimited"; //$NON-NLS-1$
	private static final String USED_BANDWIDTH = "usedBandwidth"; //$NON-NLS-1$
	private static final String USER_NAME = "userName"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$
	private static final String COMMUNITY = "community"; //$NON-NLS-1$
	private static final String TRAFFIC_LIMIT = "trafficLimit"; //$NON-NLS-1$
	private static final String ACCESS_TYPE_DETAILS = "accessTypeDetails"; //$NON-NLS-1$
	private static final String ACCESS_TYPE = "accessType"; //$NON-NLS-1$
	private static final String ACCOUNT_TYPE = "accountType"; //$NON-NLS-1$
	private static final char FIELDSEP = '\02';
	private static final String FIELDSEPS = new String(new char[] { FIELDSEP });
	private static final char SEP = '\01';
	private static final String SEPS = new String(new char[] { SEP });
	private static final char KWSEP = '\03';
	private static final String KWSEPS = new String(new char[] { KWSEP });
	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String KEYWORDSASTAGS = "keywordsAsTags"; //$NON-NLS-1$
	private static final String RECENTTAGS = "recentTags"; //$NON-NLS-1$
	private static final String USERTAGS = "userTags"; //$NON-NLS-1$
	private static final String DEFAULTTAGS = "defaultTags"; //$NON-NLS-1$
	private static final String LIMITEDBANDWIDTH = "bandwidthLimited"; //$NON-NLS-1$
	private static final String BANDWIDTH = "bandwidth"; //$NON-NLS-1$
	private static final String SESSIONS = "sessions"; //$NON-NLS-1$
	private static final String ALBUMPOLICY = "albumPolicy"; //$NON-NLS-1$
	private static final String DEFAULTALBUM = "defaultAlbum"; //$NON-NLS-1$

	private String name;
	private String id;
	private String communityId;
	private boolean keywordsAsTags;
	private boolean albumsAsSets;
	private IConfigurationElement config;
	private String accountType;
	private String accessType;
	private Set<String> accessDetails = new HashSet<String>();
	private long trafficLimit = -1L;
	private boolean authenticated;
	private List<Tag> userTags = new ArrayList<Tag>();
	protected Set<Tag> defaultTags = new LinkedHashSet<Tag>();
	private List<Tag> recentTags = new ArrayList<Tag>();
	private List<PhotoSet> photosets = new ArrayList<PhotoSet>();
	private long usedBandwidth = -1L;
	private String username;
	private boolean unlimited;
	private int usedAlbums;
	private int availableAlbums = -1;
	private int bandwidth = 1024;
	private boolean bandwidthLimited = false;
	private String authToken;
	private Map<String, String> properties = new HashMap<String, String>();
	private long maxFilesize;
	private long maxVideosize;
	private boolean trackExport = true;
	private boolean canReplace = false;
	private String visit;
	private boolean supportsRaw = false;
	private boolean propagateCategories;
	private String passwordHash;
	private int passwordLength;
	private boolean sessions;
	private List<? extends Category> categories;
	private List<? extends Category> subcategories;
	private int albumPolicy;
	private String defaultAlbum;
	private String secret;

	private CommunityAccount(String s, IConfigurationElement config) {
		this.config = config;
		IConfigurationElement parent = (IConfigurationElement) config
				.getParent();
		communityId = parent.getAttribute(ID);
		sessions = Boolean.parseBoolean(parent.getAttribute(SESSIONS));
		StringTokenizer st = new StringTokenizer(s, FIELDSEPS);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int p = token.indexOf('=');
			if (p < 0)
				id = token;
			else {
				String key = token.substring(0, p).intern();
				String value = token.substring(p + 1);
				if (key == NAME)
					name = value;
				else if (key == PASSWORD_LENGTH)
					passwordLength = Integer.parseInt(value);
				else if (key == PASSWORD)
					passwordHash = value;
				else if (key == COMMUNITY) {
					if (!communityId.equals(value))
						throw new IllegalArgumentException(
								Messages.CommunityAccount_account_does_not_belong_to);
				} else if (key == KEYWORDSASTAGS)
					keywordsAsTags = Boolean.parseBoolean(value);
				else if (key == AUTHENTICATED)
					authenticated = Boolean.parseBoolean(value);
				else if (key == TOKEN)
					authToken = value;
				else if (key == SECRET)
					secret = value;
				else if (key == ALBUMSASSETS)
					albumsAsSets = Boolean.parseBoolean(value);
				else if (key == PROPAGATE_CATS)
					propagateCategories = Boolean.parseBoolean(value);
				else if (key == ACCOUNT_TYPE)
					accountType = value;
				else if (key == USED_BANDWIDTH)
					usedBandwidth = Long.parseLong(value);
				else if (key == USER_NAME)
					username = value;
				else if (key == UNLIMITED)
					unlimited = Boolean.parseBoolean(value);
				else if (key == AVAILABLE_ALBUMS)
					availableAlbums = Integer.parseInt(value);
				else if (key == USED_ALBUMS)
					usedAlbums = Integer.parseInt(value);
				else if (key == LIMITEDBANDWIDTH)
					bandwidthLimited = Boolean.parseBoolean(value);
				else if (key == BANDWIDTH)
					bandwidth = Integer.parseInt(value);
				else if (key == TRAFFIC_LIMIT)
					trafficLimit = Long.parseLong(value);
				else if (key == MAX_FILESIZE)
					maxFilesize = Long.parseLong(value);
				else if (key == MAX_VIDEOSIZE)
					maxVideosize = Long.parseLong(value);
				else if (key == TRACK_EXPORT)
					trackExport = Boolean.parseBoolean(value);
				else if (key == CAN_REPLACE)
					canReplace = Boolean.parseBoolean(value);
				else if (key == SUPPORTS_RAW)
					supportsRaw = Boolean.parseBoolean(value);
				else if (key == ACCESS_TYPE)
					accessType = value;
				else if (key == VISIT)
					visit = value;
				else if (key == USERTAGS)
					userTags = parseTags(value);
				else if (key == DEFAULTTAGS)
					defaultTags = new HashSet<Tag>(parseTags(value));
				else if (key == RECENTTAGS)
					recentTags = parseTags(value);
				else if (key == ACCESS_TYPE_DETAILS) {
					StringTokenizer sta = new StringTokenizer(value, ","); //$NON-NLS-1$
					while (sta.hasMoreTokens())
						accessDetails.add(sta.nextToken());
				} else if (key == ALBUMPOLICY)
					albumPolicy = Integer.parseInt(value);
				else if (key == DEFAULTALBUM)
					defaultAlbum = value;
			}
		}
	}

	private static List<Tag> parseTags(String value) {
		List<Tag> tags = new ArrayList<Tag>();
		StringTokenizer st = new StringTokenizer(value, KWSEPS);
		while (st.hasMoreTokens())
			tags.add(new Tag(st.nextToken()));
		return tags;
	}

	public CommunityAccount(IConfigurationElement config) {
		this.config = config;
		IConfigurationElement parent = (IConfigurationElement) config
				.getParent();
		communityId = parent.getAttribute(ID);
		sessions = Boolean.parseBoolean(parent.getAttribute(SESSIONS));
		visit = parent.getAttribute(HOME);
		id = UUID.randomUUID().toString();
	}

	public String getName() {
		return name;
	}

	public static void saveAllAccounts(String id,
			List<CommunityAccount> communityAccounts) {
		IEclipsePreferences node = InstanceScope.INSTANCE
				.getNode(CommunitiesActivator.PLUGIN_ID);
		StringBuilder sb = new StringBuilder(4096);
		for (CommunityAccount acc : communityAccounts)
			if (acc.getName() != null && !acc.getName().isEmpty())
				sb.append(acc.toString()).append(SEP);
		BatchUtilities.putPreferences(node, PreferenceConstants.COMMUNITYACCOUNTS + id, sb.toString());
	}

	public static List<CommunityAccount> loadAllAccounts(String id,
			IConfigurationElement config) {
		String s = Platform.getPreferencesService().getString(
				CommunitiesActivator.PLUGIN_ID,
				PreferenceConstants.COMMUNITYACCOUNTS + id, "", null); //$NON-NLS-1$
		ArrayList<CommunityAccount> accounts = new ArrayList<CommunityAccount>();
		if (s != null) {
			StringTokenizer st = new StringTokenizer(s, SEPS);
			while (st.hasMoreTokens())
				accounts.add(new CommunityAccount(st.nextToken(), config));
		}
		return accounts;
	}

	public void save() {
		List<CommunityAccount> allAccounts = loadAllAccounts(communityId,
				config);
		int i = allAccounts.indexOf(this);
		if (i >= 0) {
			allAccounts.remove(i);
			if (i >= allAccounts.size())
				allAccounts.add(this);
			else
				allAccounts.add(i, this);
		} else
			allAccounts.add(this);
		saveAllAccounts(communityId, allAccounts);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CommunityAccount))
			return false;
		return id.equals(((CommunityAccount) obj).getId());
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append(id);
		appendString(sb, COMMUNITY, communityId);
		appendString(sb, NAME, name);
		appendString(sb, PASSWORD, passwordHash);
		appendString(sb, PASSWORD_LENGTH, String.valueOf(passwordLength));
		appendString(sb, AUTHENTICATED, String.valueOf(authenticated));
		appendString(sb, TOKEN, authToken);
		appendString(sb, SECRET, secret);
		appendString(sb, KEYWORDSASTAGS, String.valueOf(keywordsAsTags));
		appendString(sb, ALBUMSASSETS, String.valueOf(albumsAsSets));
		appendString(sb, PROPAGATE_CATS, String.valueOf(propagateCategories));
		appendString(sb, ACCOUNT_TYPE, accountType);
		appendString(sb, ACCESS_TYPE, accessType);
		appendString(sb, USER_NAME, username);
		appendString(sb, USED_BANDWIDTH, String.valueOf(usedBandwidth));
		appendString(sb, UNLIMITED, String.valueOf(unlimited));
		appendString(sb, AVAILABLE_ALBUMS, String.valueOf(availableAlbums));
		appendString(sb, USED_ALBUMS, String.valueOf(usedAlbums));
		appendString(sb, LIMITEDBANDWIDTH, String.valueOf(bandwidthLimited));
		appendString(sb, BANDWIDTH, String.valueOf(bandwidth));
		appendString(sb, TRAFFIC_LIMIT, String.valueOf(trafficLimit));
		appendString(sb, MAX_FILESIZE, String.valueOf(maxFilesize));
		appendString(sb, MAX_VIDEOSIZE, String.valueOf(maxVideosize));
		appendString(sb, TRACK_EXPORT, String.valueOf(trackExport));
		appendString(sb, SUPPORTS_RAW, String.valueOf(supportsRaw));
		appendString(sb, CAN_REPLACE, String.valueOf(canReplace));
		String details = Core.toStringList(
				accessDetails.toArray(new String[accessDetails.size()]), ","); //$NON-NLS-1$
		appendString(sb, ACCESS_TYPE_DETAILS, details);
		appendString(sb, VISIT, visit);
		appendTags(sb, DEFAULTTAGS, defaultTags);
		appendTags(sb, USERTAGS, userTags);
		appendTags(sb, RECENTTAGS, recentTags);
		appendString(sb, ALBUMPOLICY, String.valueOf(albumPolicy));
		appendString(sb, DEFAULTALBUM, defaultAlbum);
		return sb.toString();
	}

	private static void appendTags(StringBuilder sb, String key, Collection<Tag> tags) {
		sb.append(FIELDSEP).append(key).append('=');
		for (Tag tag : tags)
			sb.append(tag).append(KWSEP);
	}

	private static void appendString(StringBuilder sb, String key, String value) {
		if (value != null && !value.isEmpty())
			sb.append(FIELDSEP).append(key).append('=').append(value);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IConfigurationElement getConfiguration() {
		return config;
	}

	public String getCommunityId() {
		return communityId;
	}

	public void setUserTags(List<Tag> tags) {
		this.userTags = tags;
	}

	public List<Tag> getUserTags() {
		return userTags;
	}

	public void setDefaultTags(List<Tag> tags) {
		this.userTags = tags;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public boolean isKeywordsAsTags() {
		return keywordsAsTags;
	}

	public void setKeywordsAsTags(boolean keywordsAsTags) {
		this.keywordsAsTags = keywordsAsTags;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public Set<String> getAccessDetails() {
		return accessDetails;
	}

	public boolean hasAccessDetails(String key) {
		return accessDetails.contains(key);
	}

	public void setAccessDetails(Set<String> accessDetails) {
		this.accessDetails = accessDetails;
	}

	public long getTrafficLimit() {
		return trafficLimit;
	}

	public void setTrafficLimit(long maxBandwidth) {
		trafficLimit = maxBandwidth;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public Collection<Tag> getDefaultTags() {
		return defaultTags;
	}

	public String getToken() {
		return authToken;
	}

	public long getCurrentUploadUsed() {
		return usedBandwidth;
	}

	public void setCurrentUploadUsed(long usedBandwidth) {
		this.usedBandwidth = usedBandwidth;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public boolean isUnlimited() {
		return unlimited;
	}

	public void setUnlimited(boolean unlimited) {
		this.unlimited = unlimited;
	}

	public void addProperty(String key, String value) {
		properties.put(key, value);
	}

	public String getProperty(String key) {
		return properties.get(key);
	}

	public void setToken(String token) {
		this.authToken = token;
	}

	public boolean isNullAccount() {
		return name == null || name.trim().isEmpty();
	}

	public boolean isAlbumsAsSets() {
		return albumsAsSets;
	}

	public void setAlbumsAsSets(boolean albumsAsSets) {
		this.albumsAsSets = albumsAsSets;
	}

	public Collection<Tag> getRecentTags() {
		return recentTags;
	}

	public void setRecentTags(List<Tag> tags) {
		recentTags = tags;
	}

	public boolean hasSessions() {
		return sessions;
	}

	public List<PhotoSet> getPhotosets() {
		return photosets;
	}

	public void setPhotosets(List<PhotoSet> photosets) {
		this.photosets = photosets;
	}

	public boolean isBandwidthLimited() {
		return bandwidthLimited;
	}

	public void setBandwidthLimited(boolean bandwidthLimited) {
		this.bandwidthLimited = bandwidthLimited;
	}

	public int getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}

	public void setMaxFilesize(long max) {
		maxFilesize = max;
	}

	public void setMaxVideosize(long max) {
		maxVideosize = max;
	}

	public long getMaxFilesize() {
		return maxFilesize;
	}

	public long getMaxVideosize() {
		return maxVideosize;
	}

	public boolean isTrackExport() {
		return trackExport;
	}

	public void setTrackExport(boolean trackExport) {
		this.trackExport = trackExport;
	}

	public String getVisit() {
		return visit;
	}

	public void setVisit(String visit) {
		this.visit = visit;
	}

	public boolean isCanReplace() {
		return canReplace;
	}

	public void setCanReplace(boolean canReplace) {
		this.canReplace = canReplace;
	}

	public boolean isSupportsRaw() {
		return supportsRaw;
	}

	public void setSupportsRaw(boolean supportsRaw) {
		this.supportsRaw = supportsRaw;
	}

	/**
	 * @return
	 */
	public String testVisit() {
		String wh = getVisit();
		if (wh != null && !wh.isEmpty()) {
			wh = Core.furnishWebUrl(wh);
			try {
				IWebBrowser browser = PlatformUI.getWorkbench()
						.getBrowserSupport().getExternalBrowser();
				browser.openURL(new URL(BatchUtilities.encodeBlanks(wh)));
			} catch (PartInitException e) {
				// ignore
			} catch (MalformedURLException e) {
				return NLS.bind(Messages.CommunityAccount_bad_url, wh);
			}
		}
		return null;
	}

	public boolean isPropagateCategories() {
		return propagateCategories;
	}

	public void setPropagateCategories(boolean propagateCategories) {
		this.propagateCategories = propagateCategories;
	}

	public int getUsedAlbums() {
		return usedAlbums;
	}

	public void setUsedAlbums(int usedAlbums) {
		this.usedAlbums = usedAlbums;
	}

	public int getAvailableAlbums() {
		return availableAlbums;
	}

	public void setAvailableAlbums(int availableAlbums) {
		this.availableAlbums = availableAlbums;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public int getPasswordLength() {
		return passwordLength;
	}

	public void setPasswordLength(int passwordLength) {
		this.passwordLength = passwordLength;
	}

	public void setCategories(List<? extends Category> categories) {
		this.categories = categories;
	}

	public void setSubcategories(List<? extends Category> categories) {
		this.subcategories = categories;
	}

	public int getAlbumPolicy() {
		return albumPolicy;
	}

	public void setAlbumPolicy(int albumPolicy) {
		this.albumPolicy = albumPolicy;
	}

	public String getDefaultAlbum() {
		return defaultAlbum;
	}

	public void setDefaultAlbum(String defaultAlbum) {
		this.defaultAlbum = defaultAlbum;
	}

	public PhotoSet findPhotoset(String n) {
		if (photosets != null && n != null) {
			for (PhotoSet album : photosets) {
				if (n.equals(album.getTitle()))
					return album;
			}
		}
		return null;
	}

	public List<? extends Category> getCategories() {
		return categories;
	}

	public List<? extends Category> getSubcategories() {
		return subcategories;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * @return secret
	 */
	public String getSecret() {
		return secret;
	}
}
