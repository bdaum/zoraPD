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

package com.bdaum.zoom.core;

import java.io.File;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPUtils;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.Asset_type;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObject;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.MediaExtension;
import com.bdaum.zoom.cat.model.creatorsContact.Contact;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.LocationConstants;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.db.AssetEnsemble.MWGRegion;
import com.bdaum.zoom.core.internal.peer.AssetOrigin;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;
import com.bdaum.zoom.program.BatchUtilities;

public class QueryField {

	private static final String[] REFLABELS = new String[] { Messages.QueryField_true_dir,
			Messages.QueryField_mag_dir };
	private static final String[] REFVALUES = new String[] { "T", "M" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static FilterChain keywordFilter;
	private static IMediaSupport[] mediaSupport;

	private static IMediaSupport[] getMediaSupport() {
		if (mediaSupport == null)
			mediaSupport = CoreActivator.getDefault().getMediaSupport();
		return mediaSupport;
	}

	public static Object obtainFieldValue(Asset asset, QueryField qfield, QueryField qsubfield) {
		if (asset != null && qfield != null) {
			if (qsubfield == null || qsubfield == qfield)
				return qfield.obtainFieldValue(asset);
			if (qsubfield.getType() != QueryField.T_NONE) {
				Object collected = qfield.getStruct(asset);
				if (collected != null) {
					IDbManager dbManager = CoreActivator.getDefault().getDbManager();
					if (collected instanceof String[]) {
						String[] array = (String[]) collected;
						List<Object> list = new ArrayList<>(array.length + 1);
						for (int i = 0; i < array.length; i++) {
							IdentifiableObject obj = dbManager.obtainById(IdentifiableObject.class, array[i]);
							if (obj != null) {
								Object value = qsubfield.obtainPlainFieldValue(obj);
								if (value != null)
									list.add(value);
							}
						}
						return list.toArray();
					}
					IdentifiableObject obj = dbManager.obtainById(IdentifiableObject.class, (String) collected);
					if (obj != null)
						return qsubfield.obtainPlainFieldValue(obj);
				}
			}
		}
		return null;
	}

	public static final String[] COLORCODELABELS = new String[] { Messages.QueryField_Undefined,
			Messages.QueryField_Black, Messages.QueryField_White, Messages.QueryField_Red, Messages.QueryField_Green,
			Messages.QueryField_Blue, Messages.QueryField_Cyan, Messages.QueryField_Magenta, Messages.QueryField_Yellow,
			Messages.QueryField_Orange, Messages.QueryField_pink, Messages.QueryField_violet };

	public static final String[][] XMPCOLORCODES = new String[][] { new String[] { "Black", Messages.QueryField_Black }, //$NON-NLS-1$
			new String[] { "White", Messages.QueryField_White }, //$NON-NLS-1$
			new String[] { "Red", Messages.QueryField_Red }, //$NON-NLS-1$
			new String[] { "Green", Messages.QueryField_Green }, //$NON-NLS-1$
			new String[] { "Blue", Messages.QueryField_Blue }, //$NON-NLS-1$
			new String[] { "Cyan", Messages.QueryField_Cyan }, //$NON-NLS-1$
			new String[] { "Magenta", Messages.QueryField_Magenta, }, //$NON-NLS-1$
			new String[] { "Yellow", Messages.QueryField_Yellow }, //$NON-NLS-1$
			new String[] { "Orange", Messages.QueryField_Orange }, //$NON-NLS-1$
			new String[] { "Pink", Messages.QueryField_pink }, //$NON-NLS-1$
			new String[] { "Purple", "Violet", Messages.QueryField_violet, Messages.QueryField_Lila } }; //$NON-NLS-1$ //$NON-NLS-2$

	private static final String[] CONTINENTCODES = new String[] { "AF", "AN", //$NON-NLS-1$ //$NON-NLS-2$
			"AS", "EU", "NA", "OC", "SA" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private static List<Category> categories = new ArrayList<QueryField.Category>();
	private static List<QueryField> subgroups = new ArrayList<QueryField>(20);
	private static List<Object> categoriesAndSubgroups;

	public static class Namespace {
		public String defaultPrefix;
		public String uri;

		public Namespace(String defaultPrefix, String uri) {
			this.defaultPrefix = defaultPrefix;
			this.uri = uri;
		}

		@Override
		public String toString() {
			return uri;
		}
	}

	public static class Category {
		public String label;
		public boolean query;
		public final boolean extension;

		public Category(String label, boolean query, boolean extension) {
			this.label = label;
			this.query = query;
			this.extension = extension;
			categories.add(this);
		}

		@Override
		public String toString() {
			return label;
		}
	}

	public abstract static class Visitor {

		public int visit(QueryField root) {
			doVisitorWork(root);
			int level = 0;
			for (QueryField child : root.getChildren())
				Math.max(level, visit(child));
			return level + 1;
		}

		public abstract void doVisitorWork(QueryField root);
	}

	// Constants
	public static final String VALUE_MIXED = Messages.QueryField_mixed;
	public static final String VALUE_NOTHING = new String();

	// Actions
	public static final int ACTION_NONE = -1;
	public static final int ACTION_QUERY = 0;
	public static final int ACTION_MAP = 1;
	public static final int ACTION_WWW = 2;
	public static final int ACTION_EMAIL = 3;
	public static final int ACTION_TOFOLDER = 4;
	public static final int ACTION_TRACK = 5;

	// Namespaces
	private final static Namespace NS_DC = new Namespace("dc", "http://purl.org/dc/elements/1.1/"); //$NON-NLS-1$ //$NON-NLS-2$
	private final static Namespace NS_IPTC4XMPCORE = new Namespace("Iptc4xmpCore", //$NON-NLS-1$
			"http://iptc.org/std/Iptc4xmpCore/1.0/xmlns/"); //$NON-NLS-1$
	public static final Namespace NS_IPTC4XMPEXT = new Namespace("Iptc4xmpExt", //$NON-NLS-1$
			"http://iptc.org/std/Iptc4xmpExt/2008-02-29/"); //$NON-NLS-1$
	public static final Namespace NS_MSPHOTO_10 = new Namespace("MicrosoftPhoto", //$NON-NLS-1$
			"http://ns.microsoft.com/photo/1.0"); //$NON-NLS-1$
	public static final Namespace NS_MSPHOTO_12 = new Namespace("MP", "http://ns.microsoft.com/photo/1.2/"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final Namespace NS_MSPHOTO_MPRI = new Namespace("MPRI", NS_MSPHOTO_12 //$NON-NLS-1$
			+ "t/RegionInfo#"); //$NON-NLS-1$
	public static final Namespace NS_MSPHOTO_MPREG = new Namespace("MPReg", NS_MSPHOTO_12 + "t/Region#"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final Namespace NS_MWG_REGIONS = new Namespace("mwg-rs", //$NON-NLS-1$
			"http://www.metadataworkinggroup.com/schemas/regions/"); //$NON-NLS-1$
	public static final Namespace NS_MWG_DIM = new Namespace("stDim", //$NON-NLS-1$
			"http://ns.adobe.com/xap/1.0/sType/Dimensions#"); //$NON-NLS-1$
	public static final Namespace NS_MWG_AREA = new Namespace("stArea", //$NON-NLS-1$
			"http://ns.adobe.com/xmp/sType/Area#"); //$NON-NLS-1$
	public final static Namespace NS_EXIF = new Namespace("exif", "http://ns.adobe.com/exif/1.0/"); //$NON-NLS-1$ //$NON-NLS-2$
	public final static Namespace NS_TIFF = new Namespace("tiff", "http://ns.adobe.com/tiff/1.0/"); //$NON-NLS-1$ //$NON-NLS-2$
	public final static Namespace NS_XMP = new Namespace("xmp", "http://ns.adobe.com/xap/1.0/"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final Namespace NS_XMPRIGHTS = new Namespace("xmpRights", "http://ns.adobe.com/xap/1.0/rights/"); //$NON-NLS-1$ //$NON-NLS-2$
	private final static Namespace NS_PHOTOSHOP = new Namespace("photoshop", "http://ns.adobe.com/photoshop/1.0/"); //$NON-NLS-1$ //$NON-NLS-2$
	private final static Namespace NS_AUX = new Namespace("aux", "http://ns.adobe.com/exif/1.0/aux/"); //$NON-NLS-1$ //$NON-NLS-2$
	public final static Namespace NS_LIGHTROOM = new Namespace("lr", "http://ns.adobe.com/lightroom/1.0/"); //$NON-NLS-1$//$NON-NLS-2$
	public final static Namespace NS_ZORA = new Namespace("photoZoRa", "http://ns.photozora.org/director/1.0/image/"); //$NON-NLS-1$ //$NON-NLS-2$
	public final static Namespace NS_ACR = new Namespace("crs", "http://ns.adobe.com/camera-raw-settings/1.0/"); //$NON-NLS-1$ //$NON-NLS-2$
	public final static Namespace NS_BIBBLE = new Namespace("bopt", "http://www.bibblelabs.com/BibbleOpt/5.0/"); //$NON-NLS-1$ //$NON-NLS-2$

	public final static Namespace[] ALLNAMESPACES = new Namespace[] { NS_DC, NS_IPTC4XMPCORE, NS_IPTC4XMPEXT,
			NS_MSPHOTO_10, NS_MSPHOTO_12, NS_MSPHOTO_MPREG, NS_MSPHOTO_MPRI, NS_MWG_REGIONS, NS_MWG_DIM, NS_MWG_AREA,
			NS_EXIF, NS_TIFF, NS_XMP, NS_XMPRIGHTS, NS_PHOTOSHOP, NS_AUX, NS_LIGHTROOM, NS_ZORA, NS_ACR, NS_BIBBLE };
	/**** Flags ***/
	// Editable
	public static final int EDIT_NEVER = 0;
	public static final int EDIT_DIGITAL = 1;
	public static final int EDIT_ANALOG = 2;
	public static final int EDIT_ALWAYS = 3;
	public static final int EDIT_HIDDEN = 4;
	public static final int EDIT_MASK = 15;
	// Auto creation
	public static final int AUTO_DISCRETE = 1 << 21;
	public static final int AUTO_LINEAR = 1 << 22;
	public static final int AUTO_LOG = AUTO_DISCRETE | AUTO_LINEAR;
	public static final int AUTO_CONTAINS = 1 << 23;
	public static final int AUTO_SELECT = AUTO_CONTAINS | AUTO_DISCRETE;
	public static final int AUTO_MASK = AUTO_LOG | AUTO_CONTAINS;
	// Other Flags
	public static final int QUERY = 1 << 4;
	public static final int ESSENTIAL = 1 << 5;
	public static final int HOVER = 1 << 6;
	public static final int TEXT = 1 << 7;
	public static final int REPORT = 1 << 8;
	public static final int PHOTO = IMediaSupport.PHOTO;

	// Relations
	public static final int EQUALS = 1 << 0;
	public static final int NOTEQUAL = 1 << 1;
	public static final int GREATER = 1 << 2;
	public static final int NOTGREATER = 1 << 3;
	public static final int SMALLER = 1 << 4;
	public static final int NOTSMALLER = 1 << 5;
	public static final int STARTSWITH = 1 << 6;
	public static final int DOESNOTSTARTWITH = 1 << 7;
	public static final int ENDSWITH = 1 << 8;
	public static final int DOESNOTENDWITH = 1 << 9;
	public static final int CONTAINS = 1 << 10;
	public static final int DOESNOTCONTAIN = 1 << 11;
	public static final int BETWEEN = 1 << 12;
	public static final int NOTBETWEEN = 1 << 13;
	public static final int SIMILAR = 1 << 14;
	public static final int DATEEQUALS = 1 << 15;
	public static final int DATENOTEQUAL = 1 << 16;
	public static final int UNDEFINED = 1 << 17;
	public static final int XREF = 1 << 18;
	public static final int WILDCARDS = 1 << 19;
	public static final int NOTWILDCARDS = 1 << 20;

	public static final int[] ALLRELATIONS = new int[] { EQUALS, NOTEQUAL, GREATER, NOTGREATER, SMALLER, NOTSMALLER,
			STARTSWITH, DOESNOTSTARTWITH, ENDSWITH, DOESNOTENDWITH, CONTAINS, DOESNOTCONTAIN, BETWEEN, NOTBETWEEN,
			SIMILAR, DATEEQUALS, DATENOTEQUAL, UNDEFINED, WILDCARDS, NOTWILDCARDS };
	public static final String[] ALLRELATIONLABELS = new String[] { Messages.QueryField_equals,
			Messages.QueryField_unequal, Messages.QueryField_greater, Messages.QueryField_not_greater,
			Messages.QueryField_less, Messages.QueryField_not_less, Messages.QueryField_starts_with,
			Messages.QueryField_does_not_start_with, Messages.QueryField_ends_with,
			Messages.QueryField_does_not_end_with, Messages.QueryField_contains, Messages.QueryField_does_not_contain,
			Messages.QueryField_between, Messages.QueryField_not_between, Messages.QueryField_similar,
			Messages.QueryField_equals, Messages.QueryField_unequal, Messages.QueryField_undefined,
			Messages.QueryField_wildcards, Messages.QueryField_wildcard_exclude };
	public static final int STRINGRELATIONS = EQUALS | NOTEQUAL | STARTSWITH | DOESNOTSTARTWITH | ENDSWITH
			| DOESNOTENDWITH | CONTAINS | DOESNOTCONTAIN | WILDCARDS | NOTWILDCARDS;
	public static final int STRINGARRAYRELATIONS = EQUALS | NOTEQUAL | STARTSWITH | DOESNOTSTARTWITH | ENDSWITH
			| DOESNOTENDWITH | CONTAINS | DOESNOTCONTAIN | WILDCARDS | NOTWILDCARDS | UNDEFINED;
	public static final int SIZERELATIONS = EQUALS | NOTEQUAL | GREATER | NOTGREATER | SMALLER | NOTSMALLER;
	public static final int NUMERICRELATIONS = SIZERELATIONS | BETWEEN | NOTBETWEEN;
	public static final int APPROXRELATIONS = NUMERICRELATIONS | SIMILAR;
	public static final int DATERELATIONS = DATEEQUALS | DATENOTEQUAL | GREATER | NOTGREATER | SMALLER | NOTSMALLER
			| BETWEEN | NOTBETWEEN | UNDEFINED;
	public static final int APPROXDATERELATIONS = DATERELATIONS | SIMILAR;
	public static final int NORELATIONS = 0;
	// Field Cardinalities
	public static final int CARD_LIST = -1;
	public static final int CARD_BAG = -2; // only with type STRING
	public static final int CARD_MODIFIABLEBAG = -3; // only with type STRING
	// Field Categories
	public static final Category CATEGORY_ALL = new Category(Messages.QueryField_cat_all, true, false);
	public static final Category CATEGORY_ASSET = new Category(Messages.QueryField_cat_prop, true, false);
	public static final Category CATEGORY_EXIF = new Category("EXIF", true, false); //$NON-NLS-1$
	public static final Category CATEGORY_IPTC = new Category("IPTC", true, false); //$NON-NLS-1$
	public static final Category CATEGORY_FOREIGN = new Category(Messages.QueryField_cat_foreign, false, false);
	public static final Category CATEGORY_LOCAL = new Category(Messages.QueryField_cat_local, false, false);
	public static final Category CATEGORY_NONE = new Category(Messages.QueryField_cat_none, false, false);
	// Types
	public static final int T_NONE = 63;
	public static final int T_STRING = 0;
	public static final int T_INTEGER = 1;
	public static final int T_FLOAT = 2;
	public static final int T_DATE = 3;
	public static final int T_BOOLEAN = 4;
	public static final int T_POSITIVEINTEGER = 5;
	public static final int T_POSITIVEFLOAT = 6;
	public static final int T_FLOATB = 7;
	public static final int T_CURRENCY = 8;
	public static final int T_LONG = 9;
	public static final int T_POSITIVELONG = 10;
	public static final int T_LOCATION = 65;
	public static final int T_CONTACT = 66;
	public static final int T_OBJECT = 67;
	public static final int T_REGION = 68;
	public static final int T_STRUCT = 64;
	// Literals
	public static final Object EMPTYDATE = new Object();

	// Codes
	public static final int SUBJECTCODES = 0;
	public static final int SCENECODES = 1;
	// Maps

	private static Map<String, QueryField> fieldMap = new HashMap<String, QueryField>(100);
	private static Map<String, QueryField> pathMap = new HashMap<String, QueryField>(100);
	private static Map<String, QueryField> exifToolMap = new HashMap<String, QueryField>(100);
	private static Map<String, QueryField> regionPropertyMap = new HashMap<String, QueryField>(8);

	// TYPES
	public static final QueryField CONTACT_TYPE = new QueryField(null, null, null, null, null,
			Messages.QueryField_Contact, ACTION_NONE, EDIT_NEVER, CATEGORY_IPTC, T_NONE, 1, T_NONE, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField ARTWORKOROBJECT_TYPE = new QueryField(null, null, null, null, null,
			Messages.QueryField_ArtworkObject, ACTION_NONE, EDIT_NEVER, CATEGORY_IPTC, T_NONE, 1, T_NONE, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField LOCATION_TYPE = new QueryField(null, null, null, null, null,
			Messages.QueryField_Location, ACTION_NONE, EDIT_NEVER, CATEGORY_IPTC, T_NONE, 1, T_NONE, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField REGION_TYPE = new QueryField(null, null, null, null, null,
			Messages.QueryField_region, ACTION_NONE, EDIT_NEVER, CATEGORY_ASSET, T_NONE, 1, T_NONE, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField MWG_REGION_TYPE = new QueryField(null, null, null, null, null,
			Messages.QueryField_region, ACTION_NONE, EDIT_NEVER, CATEGORY_ASSET, T_NONE, 1, T_NONE, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);

	// All fields
	public static final QueryField ALL = new QueryField(null, null, null, null, null, Messages.QueryField_All,
			ACTION_NONE, EDIT_NEVER | ESSENTIAL, CATEGORY_ALL, T_NONE, 1, T_NONE, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);

	// Image fields
	public static final QueryField IMAGE_ALL = new QueryField(ALL, null, null, null, null,
			Messages.QueryField_Properties, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_ASSET, T_NONE, 1, T_NONE, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);

	// Image File fields
	public static final QueryField IMAGE_FILE = new QueryField(IMAGE_ALL, "image_file", null, null, null, //$NON-NLS-1$
			Messages.QueryField_File, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_ASSET, T_NONE, 1, T_NONE, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField NAME = new QueryField(IMAGE_FILE, "name", //$NON-NLS-1$
			null, null, null, Messages.QueryField_Name, ACTION_TOFOLDER, PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY | TEXT,
			CATEGORY_ASSET, T_STRING, 1, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getName();
		}

		@Override
		public boolean isEditable(Asset asset) {
			return asset.getFileState() == IVolumeManager.ONLINE;
		}

		@Override
		public boolean isEditable(java.util.List<? extends Asset> assets) {
			if (assets.size() != 1)
				return false;
			return isEditable(assets.get(0));
		}

		@Override
		public String isValid(Object value, Asset asset) {
			if (value instanceof String) {
				String f = BatchUtilities.toValidFilename((String) value);
				if (!f.equals(value))
					return Messages.QueryField_bad_characters_image_name;
				if (asset != null) {
					String captionText = Core.getFileName(asset.getUri(), true);
					int q = captionText.lastIndexOf('.');
					String ext = q >= 0 ? captionText.substring(q + 1) : null;
					int p = f.lastIndexOf('.');
					if (p >= 0) {
						if (!f.endsWith(ext) || p + 1 + ext.length() != f.length())
//							 if (!f.substring(p + 1).equals(ext))
							return Messages.QueryField_dont_modify_file_extensions;
					} else if (ext != null)
						f += '.' + ext;
					if (!f.equals(captionText)) {
						String u = asset.getUri();
						try {
							File file = new File(new java.net.URI(u));
							File folder = file.getParentFile();
							File targetFile = new File(folder, f);
							if (targetFile.exists())
								return NLS.bind(Messages.QueryField_target_file_already_exists, targetFile);
						} catch (URISyntaxException e) {
							return NLS.bind(Messages.QueryField_bad_uri, u);
						}
					}
				}
			}
			return null;
		}
	};
	public static final QueryField URI = new QueryField(IMAGE_FILE, "uri", //$NON-NLS-1$
			null, null, null, Messages.QueryField_URI, ACTION_TOFOLDER, PHOTO | EDIT_NEVER | ESSENTIAL | QUERY | QUERY,
			CATEGORY_ASSET, T_STRING, 1, T_NONE, Format.uriFormatter, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			if (asset.getFileState() == IVolumeManager.PEER) {
				String u = asset.getUri();
				if (u != null) {
					IPeerService peerService = Core.getCore().getPeerService();
					if (peerService != null) {
						AssetOrigin assetOrigin = peerService.getAssetOrigin(asset.getStringId());
						if (assetOrigin != null)
							return Utilities.getPeerUri(u, assetOrigin.getLocation());
					}
				}
			}
			return asset.getUri();
		}
	};
	public static final QueryField VOLUME = new QueryField(IMAGE_FILE, "volume", //$NON-NLS-1$
			null, null, null, Messages.QueryField_Volume, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | TEXT | REPORT,
			CATEGORY_ASSET, T_STRING, 1, T_NONE, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getVolume();
		}
	};
	public static final QueryField FILESIZE = new QueryField(IMAGE_FILE, "fileSize", //$NON-NLS-1$
			null, null, null, Messages.QueryField_File_size, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | AUTO_LOG,
			CATEGORY_ASSET, T_POSITIVELONG, 1, 12, Format.sizeFormatter, 0f, 9.999e11f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return (int) asset.getFileSize();
		}

		@Override
		protected long getLong(Asset asset) {
			return asset.getFileSize();
		}
	};
	public static final QueryField FORMAT = new QueryField(IMAGE_FILE, "format", //$NON-NLS-1$
			"FileType", //$NON-NLS-1$
			null, null, Messages.QueryField_File_format, ACTION_QUERY,
			PHOTO | EDIT_NEVER | ESSENTIAL | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_ASSET, T_STRING, 1, T_NONE,
			0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getFormat();
		}
	};
	public static final QueryField MIMETYPE = new QueryField(IMAGE_FILE, "mimeType", //$NON-NLS-1$
			"MIMEType", //$NON-NLS-1$
			null, null, Messages.QueryField_MIME_type, ACTION_QUERY,
			PHOTO | EDIT_NEVER | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_ASSET, T_STRING, 1, T_NONE, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getMimeType();
		}
	};
	public static final QueryField LASTMOD = new QueryField(IMAGE_FILE, "lastModification", //$NON-NLS-1$
			null, null, null, Messages.QueryField_Last_modification, ACTION_NONE,
			PHOTO | EDIT_NEVER | ESSENTIAL | HOVER, CATEGORY_ASSET, T_DATE, 1, T_NONE, -900000f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getLastModification();
		}
	};
	public static final QueryField MODIFIED_SINCE = new QueryField(IMAGE_FILE, "$modifiedSince", //$NON-NLS-1$
			null, null, null, Messages.QueryField_days_since_last_mod, ACTION_QUERY,
			PHOTO | EDIT_HIDDEN | QUERY | AUTO_LINEAR, CATEGORY_ASSET, T_POSITIVEINTEGER, 1, 5, 0f, 99999f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getLastModification();
		}
	};
	// Image Image fields
	public static final QueryField IMAGE_IMAGE = new QueryField(IMAGE_ALL, "image_image", null, null, null, //$NON-NLS-1$
			Messages.QueryField_Image, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_ASSET, T_NONE, 1, T_NONE, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);

	public static final QueryField SAMPLESPERPIXEL = new QueryField(IMAGE_IMAGE, "samplesPerPixel", //$NON-NLS-1$
			"SamplesPerPixel", //$NON-NLS-1$
			null, null, Messages.QueryField_Samples_pe_Pixel, ACTION_QUERY,
			PHOTO | EDIT_NEVER | ESSENTIAL | QUERY | AUTO_DISCRETE | REPORT, CATEGORY_ASSET, T_POSITIVEINTEGER, 1, 2,
			0f, 96, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getSamplesPerPixel();
		}
	};
	public static final QueryField PHYSICALHEIGHT = new QueryField(IMAGE_IMAGE, "$physicalHeight", //$NON-NLS-1$
			null, null, null, Messages.QueryField_Physical_height, ACTION_QUERY,
			PHOTO | EDIT_NEVER | QUERY | AUTO_LOG | REPORT, CATEGORY_ASSET, T_POSITIVEFLOAT, 1, 1, -1f, 10000f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			int h = asset.getHeight();
			return (h <= 0) ? -1 : h * 2.54d / 300d;
		}
	};
	public static final QueryField PHYSICALWIDTH = new QueryField(IMAGE_IMAGE, "$physicalWidth", //$NON-NLS-1$
			null, null, null, Messages.QueryField_Physical_width, ACTION_QUERY,
			PHOTO | EDIT_NEVER | QUERY | AUTO_LOG | REPORT, CATEGORY_ASSET, T_POSITIVEFLOAT, 1, 1, -1f, 10000f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			int w = asset.getWidth();
			return (w <= 0) ? -1 : w * 2.54d / 300d;
		}
	};

	public static final QueryField ROTATION = new QueryField(IMAGE_IMAGE, "rotation", //$NON-NLS-1$
			null, null, null, Messages.QueryField_rotation, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | AUTO_LINEAR,
			CATEGORY_ASSET, T_POSITIVEINTEGER, 1, 1, new int[] { 0, 90, 180, 270 }, new String[] { "0°", "90°", //$NON-NLS-1$//$NON-NLS-2$
					"180°", "270°" }, //$NON-NLS-1$//$NON-NLS-2$
			null, 5f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getRotation();
		}
	};

	public static final QueryField ASPECTRATIO = new QueryField(IMAGE_IMAGE, "$aspectRatio", //$NON-NLS-1$
			null, null, null, Messages.QueryField_apect_ratio, ACTION_QUERY,
			PHOTO | EDIT_NEVER | QUERY | AUTO_LINEAR | REPORT, CATEGORY_ASSET, T_POSITIVEFLOAT, 1, 1, 5f, 20f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			double h = asset.getHeight();
			double w = asset.getWidth();
			if (asset.getRotation() % 180 == 0)
				return (h == 0) ? Double.NaN : w / h;
			return (w == 0) ? Double.NaN : h / w;
		}
	};

	public static final QueryField HEIGHT = new QueryField(IMAGE_IMAGE, "height", //$NON-NLS-1$
			"ImageHeight", //$NON-NLS-1$
			null, null, Messages.QueryField_Height, ACTION_QUERY,
			PHOTO | EDIT_NEVER | ESSENTIAL | HOVER | QUERY | AUTO_LOG | REPORT, CATEGORY_ASSET, T_POSITIVEINTEGER, 1, 6,
			0f, 999999, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getHeight();
		}
	};
	public static final QueryField WIDTH = new QueryField(IMAGE_IMAGE, "width", //$NON-NLS-1$
			"ImageWidth", //$NON-NLS-1$
			null, null, Messages.QueryField_Width, ACTION_QUERY,
			PHOTO | EDIT_NEVER | ESSENTIAL | HOVER | QUERY | AUTO_LOG | REPORT, CATEGORY_ASSET, T_POSITIVEINTEGER, 1, 6,
			0f, 999999, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getWidth();
		}
	};
	public static final QueryField ORIENTATION = new QueryField(IMAGE_IMAGE, "ori", null, null, null, //$NON-NLS-1$
			Messages.QueryField_Orientation, ACTION_QUERY,
			PHOTO | EDIT_NEVER | ESSENTIAL | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_ASSET, T_STRING, 1, 8,
			Asset_type.oriALLVALUES, new String[] { Messages.QueryField_Undefined, Messages.QueryField_Portrait,
					Messages.QueryField_Square, Messages.QueryField_Landscape },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getOri();
		}
	};

	public static final QueryField COLORTYPE = new QueryField(IMAGE_IMAGE, "colorType", null, null, null, //$NON-NLS-1$
			Messages.QueryField_Color_model, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_ASSET, T_STRING, 1, 15, Asset_type.colorTypeALLVALUES,
			new String[] { Messages.QueryField_Black_and_white, Messages.QueryField_Greyscale, Messages.QueryField_RGB,
					Messages.QueryField_CMYK, Messages.QueryField_Other, Messages.QueryField_Unknown },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getColorType();
		}
	};

	public static final QueryField EMULSION = new QueryField(IMAGE_IMAGE, "emulsion", //$NON-NLS-1$
			null, NS_ZORA, "Emulsion", //$NON-NLS-1$
			Messages.QueryField_Emulsion, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_ASSET, T_STRING, 1, 64, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getEmulsion();
		}
	};
	public static final QueryField ANALOGTYPE = new QueryField(IMAGE_IMAGE, "analogType", null, NS_ZORA, "AnalogType", //$NON-NLS-1$ //$NON-NLS-2$
			Messages.QueryField_analog_type, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_ASSET, T_INTEGER, 1, 1,
			new int[] { Constants.ANALOGTYPE_UNKNOWN, Constants.ANALOGTYPE_UNDEFINED, Constants.ANALOGTYPE_NEGATIVE,
					Constants.ANALOGTYPE_TRANSPARENCY, Constants.ANALOGTYPE_REFLECTIVE, Constants.ANALOGTYPE_OTHER },
			new String[] { Messages.QueryField_Unknown, Messages.QueryField_Undefined, Messages.QueryField_negative,
					Messages.QueryField_transparency, Messages.QueryField_reflective, Messages.QueryField_Other },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getAnalogType();
		}
	};
	public static final QueryField ANALOGFORMAT = new QueryField(IMAGE_IMAGE, "analogFormat", null, NS_ZORA, //$NON-NLS-1$
			"AnalogFormat", //$NON-NLS-1$
			Messages.QueryField_analog_size, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_ASSET, T_INTEGER, 1, 1,
			new int[] { -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
					25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 255 },
			new String[] { Messages.QueryField_Unknown, Messages.QueryField_Undefined, Messages.QueryField_kodak_disc,
					"8x11mm", //$NON-NLS-1$
					Messages.QueryField_pocket, "18x24mm", //$NON-NLS-1$
					"24x24mm", //$NON-NLS-1$
					Messages.QueryField_APS_classic, Messages.QueryField_APS_panorama, Messages.QueryField_APS_full,
					Messages.QueryField_Instamatic, "24x36mm", //$NON-NLS-1$
					Messages.QueryField_35_panorama, "3x4cm", //$NON-NLS-1$
					"4x4cm", //$NON-NLS-1$
					Messages.QueryField_4x6_5, Messages.QueryField_4_5x6, Messages.QueryField_5X7_5, "6x6cm", //$NON-NLS-1$
					"5x7cm", //$NON-NLS-1$
					"5x8cm", //$NON-NLS-1$
					"6x9cm", //$NON-NLS-1$
					"6x11cm", //$NON-NLS-1$
					"4x5\"", //$NON-NLS-1$
					"4x10\"", //$NON-NLS-1$
					"5x7\"", //$NON-NLS-1$
					"8x10\"", //$NON-NLS-1$
					"8x20\"", //$NON-NLS-1$
					"11x14\"", //$NON-NLS-1$
					"16x20\"", //$NON-NLS-1$
					"20x24\"", //$NON-NLS-1$
					Messages.QueryField_ninth_plate, Messages.QueryField_sixth_plate, Messages.QueryField_quarter_plate,
					Messages.QueryField_half_plate, Messages.QueryField_full_plate, Messages.QueryField_Other },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getAnalogFormat();
		}
	};
	public static final QueryField ANALOGPROCESSING = new QueryField(IMAGE_IMAGE, "analogProcessing", //$NON-NLS-1$
			null, NS_ZORA, "AnalogProcessing", //$NON-NLS-1$
			Messages.QueryField_Analog_Processing_Notes, ACTION_QUERY,
			PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_CONTAINS, CATEGORY_ASSET, T_STRING, 1, 255, 0f, Float.NaN,
			ISpellCheckingService.DESCRIPTIONOPTIONS) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getAnalogProcessing();
		}
	};
	public static final QueryField TIMEOFDAY = new QueryField(IMAGE_IMAGE, "$timeOfDay", //$NON-NLS-1$
			null, null, null, Messages.QueryField_time_of_day, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | AUTO_LINEAR,
			CATEGORY_ASSET, T_POSITIVEINTEGER, 1, 5, Format.timeFormatter, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {

		private Calendar cal = new GregorianCalendar();

		@Override
		protected int getInt(Asset asset) {
			Date dateCreated = asset.getDateCreated();
			if (dateCreated == null)
				dateCreated = asset.getDateTimeOriginal();
			if (dateCreated == null)
				return -1;
			cal.setTime(dateCreated);
			return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
		}
	};
	public static final QueryField DATE = new QueryField(IMAGE_IMAGE, "$date", //$NON-NLS-1$
			null, null, null, Messages.QueryField_date, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | AUTO_LINEAR,
			CATEGORY_ASSET, T_DATE, 1, 16, -60000f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			Date dateCreated = asset.getDateCreated();
			if (dateCreated != null)
				return dateCreated;
			dateCreated = asset.getDateTimeOriginal();
			if (dateCreated != null)
				return dateCreated;
			File file = Core.getCore().getVolumeManager().findExistingFile(asset.getUri(), asset.getVolume());
			return file == null ? null : new Date(file.lastModified());
		}
	};

	// Image Financial fields
	public static final QueryField IMAGE_FINANCE = new QueryField(IMAGE_ALL, "image_finance", null, null, null, //$NON-NLS-1$
			Messages.QueryField_financial, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_ASSET, T_NONE, 1, T_NONE, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField SALES = new QueryField(IMAGE_FINANCE, "sales", //$NON-NLS-1$
			null, null, null, Messages.QueryField_sales, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | AUTO_LOG,
			CATEGORY_ASSET, T_POSITIVEINTEGER, 1, 4, 0f, 9999f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getSales();
		}
	};
	public static final QueryField PRICE = new QueryField(IMAGE_FINANCE, "price", //$NON-NLS-1$
			null, null, null, Messages.QueryField_price, ACTION_QUERY,
			PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_LOG | REPORT, CATEGORY_ASSET, T_CURRENCY, 1, 1, 0f, 999999999f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getPrice();
		}
	};
	public static final QueryField EARNINGS = new QueryField(IMAGE_FINANCE, "earnings", //$NON-NLS-1$
			null, null, null, Messages.QueryField_earnings, ACTION_QUERY,
			PHOTO | EDIT_ALWAYS | QUERY | AUTO_LOG | REPORT, CATEGORY_ASSET, T_CURRENCY, 1, 1,
			Format.currencyExpressionFormatter, 0f, 999999999f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getEarnings();
		}
	};

	// Image ZoRa specific fields
	public static final QueryField IMAGE_ZOOM = new QueryField(IMAGE_ALL, "image_zoom", null, null, null, //$NON-NLS-1$
			Constants.APPLICATION_NAME, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_ASSET, T_NONE, 1, T_NONE, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField IMPORTDATE = new QueryField(IMAGE_ZOOM, "importDate", //$NON-NLS-1$
			null, NS_ZORA, "ImportDate", //$NON-NLS-1$
			Messages.QueryField_Import_date, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | REPORT, CATEGORY_ASSET, T_DATE,
			1, 16, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getImportDate();
		}
	};
	public static final QueryField IMPORTEDBY = new QueryField(IMAGE_ZOOM, "importedBy", //$NON-NLS-1$
			null, NS_ZORA, "ImportedBy", //$NON-NLS-1$
			Messages.QueryField_Imported_by, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_ASSET, T_STRING, 1, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getImportedBy();
		}
	};
	public static final QueryField STATUS = new QueryField(IMAGE_ZOOM, "status", null, NS_ZORA, "Status", //$NON-NLS-1$ //$NON-NLS-2$
			Messages.QueryField_Status, ACTION_QUERY,
			PHOTO | EDIT_ALWAYS | ESSENTIAL | HOVER | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_ASSET, T_INTEGER,
			1, 1,
			new int[] { Constants.STATE_UNDEFINED, Constants.STATE_UNKNOWN, Constants.STATE_RAW,
					Constants.STATE_CONVERTED, Constants.STATE_DEVELOPED, Constants.STATE_CORRECTED,
					Constants.STATE_RETOUCHED, Constants.STATE_READY, Constants.STATE_TODO,
					Constants.STATE_UNDERCONSIDERATION },
			new String[] { Messages.QueryField_Undefined, Messages.QueryField_Unknown, Messages.QueryField_Raw,
					Messages.QueryField_Converted, Messages.QueryField_Developed, Messages.QueryField_Corrected,
					Messages.QueryField_Retouched, Messages.QueryField_Ready_for_publishing, Messages.QueryField_Todo,
					Messages.QueryField_Under_consideration },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getStatus();
		}
	};

	public static final QueryField RATING = new QueryField(IMAGE_ZOOM, "rating", "Rating", NS_XMP, "Rating", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Messages.QueryField_Rating, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | REPORT, CATEGORY_ASSET,
			T_INTEGER, 1, 1, new int[] { -1, 0, 1, 2, 3, 4, 5 },
			new String[] { Messages.QueryField_Undefined, Messages.QueryField_Bad, Messages.QueryField_Below_average,
					Messages.QueryField_Average, Messages.QueryField_Above_average, Messages.QueryField_Good,
					Messages.QueryField_Excellent },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getRating();
		}
	};
	public static final int SELECTALL = -2;
	public static final int SELECTUNDEF = -1;

	public static final QueryField RATEDBY = new QueryField(IMAGE_ZOOM, "ratedBy", //$NON-NLS-1$
			"ratedBy", //$NON-NLS-1$
			NS_ZORA, "RatedBy", //$NON-NLS-1$
			Messages.QueryField_Rated_by, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_ASSET, T_STRING, 1, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getRatedBy();
		}
	};
	public static final QueryField TITLEORNAME = new QueryField(IMAGE_ZOOM, "$titleOrName", //$NON-NLS-1$
			null, null, null, Messages.QueryField_title_name, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_ASSET, T_STRING,
			1, 255, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			String title = asset.getTitle();
			return (title == null || title.trim().isEmpty()) ? asset.getName() : title;
		}
	};

	public static final int SAFETY_LOCAL = -1;
	public static final int SAFETY_SAFE = 0;
	public static final int SAFETY_MODERATE = 3;
	public static final int SAFETY_RESTRICTED = 6;

	public static final QueryField SAFETY = new QueryField(IMAGE_ZOOM, "safety", null, NS_XMP, "Safety", //$NON-NLS-1$ //$NON-NLS-2$
			Messages.QueryField_safety, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_ASSET, T_INTEGER, 1, 1, new int[] { SAFETY_SAFE, SAFETY_MODERATE, SAFETY_RESTRICTED },
			new String[] { Messages.QueryField_safe, Messages.QueryField_moderate, Messages.QueryField_restricted },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getSafety();
		}
	};
	public static final int CONTENTTYPE_PHOTO = 0;
	public static final int CONTENTTYPE_SCREENSHOT = 1;
	public static final int CONTENTTYPE_ARTWORK = 2;

	public static final QueryField CONTENTTYPE = new QueryField(IMAGE_ZOOM, "contentType", null, NS_XMP, "ContentType", //$NON-NLS-1$ //$NON-NLS-2$
			Messages.QueryField_content_type, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_ASSET, T_INTEGER, 1, 1,
			new int[] { CONTENTTYPE_PHOTO, CONTENTTYPE_SCREENSHOT, CONTENTTYPE_ARTWORK },
			new String[] { Messages.QueryField_photo, Messages.QueryField_screenshot, Messages.QueryField_artwork },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getContentType();
		}
	};
	public static final QueryField COLORCODE = new QueryField(IMAGE_ZOOM, "colorCode", "Label", NS_ZORA, "ColorCode", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Messages.QueryField_Color_code, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE,
			CATEGORY_ASSET, T_INTEGER, 1, 1,
			new int[] { Constants.COLOR_UNDEFINED, Constants.COLOR_BLACK, Constants.COLOR_WHITE, Constants.COLOR_RED,
					Constants.COLOR_GREEN, Constants.COLOR_BLUE, Constants.COLOR_CYAN, Constants.COLOR_MAGENTA,
					Constants.COLOR_YELLOW, Constants.COLOR_ORANGE, Constants.COLOR_PINK, Constants.COLOR_VIOLET },
			COLORCODELABELS, null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getColorCode();
		}
	};
	public static final QueryField ALBUM = new QueryField(IMAGE_ZOOM, "album", //$NON-NLS-1$
			null, NS_ZORA, "Album", //$NON-NLS-1$
			Messages.QueryField_Album, ACTION_NONE, PHOTO | EDIT_NEVER | QUERY | TEXT, CATEGORY_ASSET, T_STRING,
			CARD_BAG, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getAlbum();
		}
	};
	public static final QueryField NOOFPERSONS = new QueryField(IMAGE_IMAGE, "noPersons", //$NON-NLS-1$
			null, NS_ZORA, "NoOfPersons", //$NON-NLS-1$
			Messages.QueryField_no_persons, ACTION_NONE, PHOTO | EDIT_NEVER | QUERY | AUTO_LOG | REPORT, CATEGORY_ASSET,
			T_POSITIVEINTEGER, 1, 6, 0f, 999999f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getNoPersons();
		}
	};

	public static final QueryField DATEREGIONSVALID = new QueryField(IMAGE_IMAGE, "dateRegionsValid", //$NON-NLS-1$
			"DateRegionsValid", //$NON-NLS-1$
			NS_MSPHOTO_MPRI, "RegionInfo/" + NS_MSPHOTO_MPRI.defaultPrefix + ":DateRegionsValid", //$NON-NLS-1$ //$NON-NLS-2$
			Messages.QueryField_last_region_update, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_ASSET, T_DATE, 1, 16, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getNoPersons();
		}
	};

	public static final QueryField FACESSHOWN = new QueryField(IMAGE_IMAGE, "$mp_region", //$NON-NLS-1$
			"Regions", //$NON-NLS-1$
			NS_MSPHOTO_MPRI, "RegionInfo/" + NS_MSPHOTO_MPRI.defaultPrefix + ":Regions", //$NON-NLS-1$ //$NON-NLS-2$
			Messages.QueryField_face_regions, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_ASSET, T_REGION, CARD_LIST, 128,
			16, Float.NaN, ISpellCheckingService.NOSPELLING) {
		@Override
		public Object getStruct(Asset asset) {
			String[] regionIds = asset.getPerson();
			return regionIds == null || regionIds.length == 0 ? null : regionIds;
		}
	};

	public static final QueryField MWG_FACESSHOWN = new QueryField(IMAGE_IMAGE, "$mwg_region", //$NON-NLS-1$
			"RegionList", //$NON-NLS-1$
			NS_MWG_REGIONS, "Regions/" + NS_MWG_REGIONS.defaultPrefix + ":RegionList", //$NON-NLS-1$ //$NON-NLS-2$
			Messages.QueryField_exif_face_regions, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_ASSET, T_REGION, CARD_LIST,
			128, 16, Float.NaN, ISpellCheckingService.NOSPELLING) {
		@Override
		public Object getStruct(Asset asset) {
			String[] regionIds = asset.getPerson();
			return regionIds == null || regionIds.length == 0 ? null : regionIds;
		}
	};

	public static final QueryField USERFIELD1 = new QueryField(IMAGE_ZOOM, "userfield1", //$NON-NLS-1$
			null, NS_ZORA, "Userfield1", //$NON-NLS-1$
			Constants.USERFIELD1, ACTION_QUERY, PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY | TEXT | AUTO_CONTAINS,
			CATEGORY_ASSET, T_STRING, 1, 32, 0f, Float.NaN, ISpellCheckingService.DESCRIPTIONOPTIONS) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getUserfield1();
		}
	};
	public static final QueryField USERFIELD2 = new QueryField(IMAGE_ZOOM, "userfield2", //$NON-NLS-1$
			null, NS_ZORA, "Userfield2", //$NON-NLS-1$
			Constants.USERFIELD2, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_CONTAINS, CATEGORY_ASSET,
			T_STRING, 1, 1024, 0f, Float.NaN, ISpellCheckingService.DESCRIPTIONOPTIONS) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getUserfield2();
		}
	};
	public static final QueryField TRACK = new QueryField(IMAGE_ZOOM, "track", //$NON-NLS-1$
			null, null, null, Messages.QueryField_track, ACTION_TRACK, PHOTO | EDIT_NEVER, CATEGORY_ASSET, T_STRING,
			CARD_LIST, T_NONE, Format.trackFormatter, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getTrack();
		}

		@Override
		public void mergeValues(Object source, Object target) {
			String[] newValue = (String[]) obtainPlainFieldValue(source);
			if (newValue.length > 0) {
				String[] oldValue = (String[]) obtainPlainFieldValue(target);
				if (oldValue.length == 0)
					setPlainFieldValue(target, newValue);
				else {
					Set<String> set = new HashSet<String>(Arrays.asList(oldValue));
					set.addAll(Arrays.asList(newValue));
					setPlainFieldValue(target, set.toArray(new String[set.size()]));
				}
			}
		}

		@Override
		protected String doValueToText(Object value, boolean useEnums, boolean useFormatter, Locale inLocale,
				boolean subField) {
			String[] array = (String[]) value;
			if (array.length == 1)
				return Messages.QueryField_one_record;
			if (array.length > 0)
				return NLS.bind(Messages.QueryField_n_records, array.length);
			return Format.MISSINGENTRYSTRING;
		}
	};

	// Exif fields
	public static final QueryField EXIF_ALL = new QueryField(ALL, null, null, null, null, Messages.QueryField_Exif,
			ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_EXIF, T_NONE, 1, T_NONE, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);

	// Exif Image fields
	public static final QueryField EXIF_IMAGE = new QueryField(EXIF_ALL, "exif_image", null, null, null, //$NON-NLS-1$
			Messages.QueryField_image_data, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_EXIF, T_NONE, 1, T_NONE, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);

	public static final QueryField EXIF_IMAGEHEIGHT = new QueryField(EXIF_IMAGE, "imageLength", //$NON-NLS-1$
			"ImageHeight", //$NON-NLS-1$
			NS_TIFF, "ImageLength", //$NON-NLS-1$
			Messages.QueryField_Original_height, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | AUTO_LOG | REPORT,
			CATEGORY_EXIF, T_POSITIVEINTEGER, 1, 5, 0f, 99999f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getImageLength();
		}
	};
	public static final QueryField EXIF_IMAGEWIDTH = new QueryField(EXIF_IMAGE, "imageWidth", //$NON-NLS-1$
			"ImageWidth", //$NON-NLS-1$
			NS_TIFF, "ImageWidth", //$NON-NLS-1$
			Messages.QueryField_Original_width, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | AUTO_LOG | REPORT,
			CATEGORY_EXIF, T_POSITIVEINTEGER, 1, 5, 0f, 99999f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getImageWidth();
		}
	};
	public static final QueryField BITSPERSAMPLE = new QueryField(EXIF_IMAGE, "bitsPerSample", //$NON-NLS-1$
			"BitsPerSample", //$NON-NLS-1$
			NS_TIFF, "BitsPerSample", //$NON-NLS-1$
			Messages.QueryField_Bits_per_sample, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_ASSET, T_INTEGER, 3, 3, 0f,
			192f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getBitsPerSample();
		}
	};
	public static final QueryField PHOTOMETRICINTERPRETATION = new QueryField(EXIF_IMAGE, "photometricInterpretation", //$NON-NLS-1$
			"PhotometricInterpretation", NS_TIFF, //$NON-NLS-1$
			"PhotometricInterpretation", Messages.QueryField_Photometric_Interpretation, ACTION_NONE, //$NON-NLS-1$
			PHOTO | EDIT_NEVER | TEXT, CATEGORY_ASSET, T_INTEGER, 1, 1,
			new int[] { -1, 0, 1, 2, 3, 4, 5, 6, 8, 9, 10, 32803, 32844, 32845, 34892 },
			new String[] { Format.MISSINGENTRYSTRING, Messages.QueryField_WhiteIsZero, Messages.QueryField_BlackIsZero,
					"RGB", //$NON-NLS-1$
					Messages.QueryField_RGB_Palette, Messages.QueryField_Transparency_Mask, "CMYK", "YCbCr", //$NON-NLS-1$ //$NON-NLS-2$
					"CIELab", //$NON-NLS-1$
					"ICCLab", //$NON-NLS-1$
					"ITULab", Messages.QueryField_Color_Filter_Array, //$NON-NLS-1$
					"Pixar LogL", //$NON-NLS-1$
					"Pixar LogLuv", Messages.QueryField_Linear_Raw }, //$NON-NLS-1$
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getPhotometricInterpretation();
		}
	};
	public static final QueryField EXIF_XRES = new QueryField(EXIF_IMAGE, "XResolution", //$NON-NLS-1$
			"XResolution", //$NON-NLS-1$
			NS_TIFF, "XResolution", //$NON-NLS-1$
			Messages.QueryField_Horizontal_Resolution, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_EXIF, T_POSITIVEFLOAT,
			1, 1, 0f, 2000f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getXResolution();
		}
	};
	public static final QueryField EXIF_YRES = new QueryField(EXIF_IMAGE, "YResolution", //$NON-NLS-1$
			"YResolution", //$NON-NLS-1$
			NS_TIFF, "YResolution", //$NON-NLS-1$
			Messages.QueryField_Vertical_Resolution, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_EXIF, T_POSITIVEFLOAT, 1,
			1, 0f, 2000f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getYResolution();
		}
	};
	public static final QueryField EXIF_DATETIME = new QueryField(EXIF_IMAGE, "dateTime", //$NON-NLS-1$
			"ModifyDate", //$NON-NLS-1$
			NS_XMP, "ModifyDate", //$NON-NLS-1$
			Messages.QueryField_File_creation_date, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY, CATEGORY_EXIF, T_DATE, 1,
			16, -60000, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getDateTime();
		}
	};
	public static final QueryField EXIF_IMAGEDESCRIPTION = new QueryField(EXIF_IMAGE, "imageDescription", //$NON-NLS-1$
			new String[] { "ImageDescription", "Caption-Abstract" }, //$NON-NLS-1$ //$NON-NLS-2$
			NS_DC, "description", //$NON-NLS-1$
			Messages.QueryField_Image_Description, ACTION_NONE,
			PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY | TEXT | AUTO_CONTAINS, CATEGORY_EXIF, T_STRING, 1, 2000, 0f,
			Float.NaN, ISpellCheckingService.DESCRIPTIONOPTIONS) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getImageDescription();
		}
	};
	public static final QueryField EXIF_MAKE = new QueryField(EXIF_IMAGE, "make", //$NON-NLS-1$
			"Make", //$NON-NLS-1$
			NS_TIFF, "Make", //$NON-NLS-1$
			Messages.QueryField_Camera_maker, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_EXIF, T_STRING, 1, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getMake();
		}
	};
	public static final QueryField EXIF_MODEL = new QueryField(EXIF_IMAGE, "model", //$NON-NLS-1$
			"Model", //$NON-NLS-1$
			NS_TIFF, "Model", //$NON-NLS-1$
			Messages.QueryField_Camera_model, ACTION_QUERY,
			PHOTO | EDIT_ANALOG | ESSENTIAL | HOVER | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_STRING, 1,
			32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getModel();
		}
	};
	public static final QueryField EXIF_SOFTWARE = new QueryField(EXIF_IMAGE, "software", //$NON-NLS-1$
			"Software", //$NON-NLS-1$
			NS_TIFF, "Software", //$NON-NLS-1$
			Messages.QueryField_Software, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_EXIF, T_STRING, 1, 64, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getSoftware();
		}
	};
	public static final QueryField EXIF_ORIENTATION = new QueryField(EXIF_IMAGE, "orientation", //$NON-NLS-1$
			"Orientation", //$NON-NLS-1$
			NS_TIFF, "Orientation", //$NON-NLS-1$
			Messages.QueryField_Orientation_exif, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_EXIF, T_POSITIVEINTEGER, 1,
			3, 0f, 360, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getOrientation();
		}

		@Override
		public int handleIntErrand(String v) throws UnsupportedOperationException {
			if (v != null) {
				if (v.startsWith("hor")) //$NON-NLS-1$
					return 1;
				if (v.startsWith("mirror ver")) //$NON-NLS-1$
					return 4;
				if (v.startsWith("mirror hor")) //$NON-NLS-1$
					return v.indexOf("270") > 0 ? 5 //$NON-NLS-1$
							: v.indexOf("90") > 0 ? 7 //$NON-NLS-1$
									: 2;
				if (v.indexOf("180") > 0) //$NON-NLS-1$
					return 3;
				if (v.indexOf("90") > 6) //$NON-NLS-1$
					return 3;
				if (v.indexOf("270") > 8) //$NON-NLS-1$
					return 3;
			}
			return 0;
		}

	};
	public static final QueryField EXIF_ORIGINALFILENAME = new QueryField(EXIF_IMAGE, "originalFileName", //$NON-NLS-1$
			"OriginalRawFileName", //$NON-NLS-1$
			null, null, Messages.QueryField_Original_file_name, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | TEXT,
			CATEGORY_EXIF, T_STRING, 1, 50, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getOriginalFileName();
		}
	};
	public static final QueryField EXIF_ARTIST = new QueryField(EXIF_IMAGE, "artist", //$NON-NLS-1$
			"Artist", //$NON-NLS-1$
			NS_DC, "creator", //$NON-NLS-1$
			Messages.QueryField_artist, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_EXIF, T_STRING, CARD_MODIFIABLEBAG, 50, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getArtist();
		}
	};
	public static final QueryField EXIF_COPYRIGHT = new QueryField(EXIF_IMAGE, "copyright", //$NON-NLS-1$
			new String[] { "Copyright", "CopyrightNotice" }, //$NON-NLS-1$ //$NON-NLS-2$
			NS_DC, "rights", //$NON-NLS-1$
			Messages.QueryField_Copyright, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_CONTAINS,
			CATEGORY_EXIF, T_STRING, 1, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getCopyright();
		}
	};
	public static final QueryField EXIF_COLORSPACE = new QueryField(EXIF_IMAGE, "colorSpace", "ColorSpace", NS_EXIF, //$NON-NLS-1$ //$NON-NLS-2$
			"ColorSpace", Messages.QueryField_Color_space, ACTION_QUERY, //$NON-NLS-1$
			PHOTO | EDIT_NEVER | ESSENTIAL | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_INTEGER, 1, 1,
			new int[] { -1, 1, 2, 65535 }, new String[] { Messages.QueryField_Undefined, "sRGB", //$NON-NLS-1$
					"AdobeRGB", Messages.QueryField_Uncalibrated }, //$NON-NLS-1$
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getColorSpace();
		}
	};
	public static final QueryField EXIF_PROFILEDESCRIPTION = new QueryField(EXIF_IMAGE, "profileDescription", //$NON-NLS-1$
			"ProfileDescription", //$NON-NLS-1$
			NS_EXIF, "ProfileDescription", //$NON-NLS-1$
			Messages.QueryField_Color_profile, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | TEXT, CATEGORY_EXIF, T_STRING,
			1, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getProfileDescription();
		}
	};
	public static final QueryField EXIF_DATETIMEORIGINAL = new QueryField(EXIF_IMAGE, "dateTimeOriginal", //$NON-NLS-1$
			"DateTimeOriginal", //$NON-NLS-1$
			NS_EXIF, "DateTimeOriginal", //$NON-NLS-1$
			Messages.QueryField_Image_creation_date, ACTION_QUERY, PHOTO | EDIT_ANALOG | ESSENTIAL | HOVER | QUERY,
			CATEGORY_EXIF, T_DATE, 1, 16, -60000f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getDateTimeOriginal();
		}
	};
	// Exif Camera fields
	public static final QueryField EXIF_CAMERA = new QueryField(EXIF_ALL, "exif_camera", null, null, null, //$NON-NLS-1$
			Messages.QueryField_Camera, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_EXIF, T_NONE, 1, T_NONE, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);

	public static final QueryField EXIF_EXPOSURETIME = new QueryField(EXIF_CAMERA, "exposureTime", //$NON-NLS-1$
			"ExposureTime", //$NON-NLS-1$
			NS_EXIF, "ExposureTime", //$NON-NLS-1$
			Messages.QueryField_Exposure_time, ACTION_QUERY,
			PHOTO | EDIT_ANALOG | ESSENTIAL | HOVER | QUERY | AUTO_LOG | REPORT, CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 4,
			Format.exposureTimeFormatter, 0f, 999f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getExposureTime();
		}
	};
	public static final QueryField EXIF_FNUMBER = new QueryField(EXIF_CAMERA, "fNumber", //$NON-NLS-1$
			"FNumber", //$NON-NLS-1$
			NS_EXIF, "FNumber", //$NON-NLS-1$
			Messages.QueryField_fNumber, ACTION_QUERY,
			PHOTO | EDIT_ANALOG | ESSENTIAL | HOVER | QUERY | AUTO_LOG | REPORT, CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 1,
			0f, 128f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getFNumber();
		}
	};
	public static final QueryField EXIF_EXPOSUREPROGRAM = new QueryField(EXIF_CAMERA, "exposureProgram", //$NON-NLS-1$
			"ExposureProgram", //$NON-NLS-1$
			NS_EXIF, "ExposureProgram", Messages.QueryField_Exposure_program, //$NON-NLS-1$
			ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_INTEGER, 1, 1,
			new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 },
			new String[] { Messages.QueryField_Undefined, Messages.QueryField_Manual,
					Messages.QueryField_Normal_program, Messages.QueryField_Aperture_priority,
					Messages.QueryField_Shutter_priority, Messages.QueryField_Creative_program,
					Messages.QueryField_Action_program, Messages.QueryField_Portrait_mode,
					Messages.QueryField_Landscape_mode },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getExposureProgram();
		}

		@Override
		public int handleIntErrand(String v) throws UnsupportedOperationException {
			if (v.startsWith("man")) //$NON-NLS-1$
				return 1;
			if (v.startsWith("pro")) //$NON-NLS-1$
				return 2;
			if (v.startsWith("aper")) //$NON-NLS-1$
				return 3;
			if (v.startsWith("shut")) //$NON-NLS-1$
				return 4;
			if (v.startsWith("crea")) //$NON-NLS-1$
				return 5;
			if (v.startsWith("act")) //$NON-NLS-1$
				return 6;
			if (v.startsWith("port")) //$NON-NLS-1$
				return 7;
			if (v.startsWith("land")) //$NON-NLS-1$
				return 8;
			if (v.startsWith("bulb")) //$NON-NLS-1$
				return 9;
			return 0;
		}
	};

	public static final QueryField EXIF_ISOSPEEDRATINGS = new QueryField(EXIF_CAMERA, "isoSpeedRatings", //$NON-NLS-1$
			"ISO", //$NON-NLS-1$
			NS_EXIF, "ISOSpeedRatings", //$NON-NLS-1$
			null, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_EXIF, T_POSITIVEINTEGER, CARD_LIST, 6, 0f, 52428800,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getIsoSpeedRatings();
		}
	};

	public static final QueryField SCALAR_ISOSPEEDRATINGS = new QueryField(EXIF_CAMERA, "scalarSpeedRatings", //$NON-NLS-1$
			null, NS_EXIF, "ISOSpeedRating", //$NON-NLS-1$
			Messages.QueryField_ISO_speed, ACTION_QUERY,
			PHOTO | EDIT_ANALOG | ESSENTIAL | HOVER | QUERY | AUTO_LOG | REPORT, CATEGORY_EXIF, T_POSITIVEINTEGER, 1, 8,
			0f, 52428800, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getScalarSpeedRatings();
		}
	};
	public static final QueryField EXIF_EXPOSUREBIAS = new QueryField(EXIF_CAMERA, "exposureBias", //$NON-NLS-1$
			"ExposureCompensation", //$NON-NLS-1$
			NS_EXIF, "ExposureBiasValue", //$NON-NLS-1$
			Messages.QueryField_Exposure_bias, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | AUTO_LOG | REPORT,
			CATEGORY_EXIF, T_FLOAT, 1, 2, 10f, 16f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getExposureBias();
		}
	};
	public static final QueryField EXIF_MAXLENSAPERTURE = new QueryField(EXIF_CAMERA, "maxLensAperture", //$NON-NLS-1$
			"MaxApertureValue", //$NON-NLS-1$
			NS_EXIF, "MaxApertureValue", //$NON-NLS-1$
			Messages.QueryField_Max_lens_aperture, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | REPORT, CATEGORY_EXIF,
			T_POSITIVEFLOAT, 1, 1, Format.apertureFormatter, 0f, 16f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getMaxLensAperture();
		}
	};
	public static final QueryField EXIF_SUBJECTDISTANCE = new QueryField(EXIF_CAMERA, "subjectDistance", //$NON-NLS-1$
			"SubjectDistance", //$NON-NLS-1$
			NS_EXIF, "SubjectDistance", //$NON-NLS-1$
			Messages.QueryField_Subject_distance, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | AUTO_LOG | REPORT,
			CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 2, 10f, 10000f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getSubjectDistance();
		}
	};
	public static final QueryField EXIF_METERINGMODE = new QueryField(EXIF_CAMERA, "meteringMode", "MeteringMode", //$NON-NLS-1$ //$NON-NLS-2$
			NS_EXIF, "MeteringMode", Messages.QueryField_Metering_Mode, ACTION_QUERY, //$NON-NLS-1$
			PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_INTEGER, 1, 3,
			new int[] { 0, 1, 2, 3, 4, 5, 6, 255 },
			new String[] { Messages.QueryField_Unknown, Messages.QueryField_Average,
					Messages.QueryField_CenterWeightedAverage, Messages.QueryField_Spot, Messages.QueryField_MultiSpot,
					Messages.QueryField_Pattern, Messages.QueryField_Partial, Messages.QueryField_Other },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getMeteringMode();
		}

		@Override
		public int handleIntErrand(String v) throws UnsupportedOperationException {
			if (v.startsWith("aver")) //$NON-NLS-1$
				return 1;
			if (v.startsWith("cent")) //$NON-NLS-1$
				return 2;
			if (v.startsWith("spot")) //$NON-NLS-1$
				return 3;
			if (v.startsWith("multi-spot")) //$NON-NLS-1$
				return 4;
			if (v.startsWith("multi-seg")) //$NON-NLS-1$
				return 5;
			if (v.startsWith("part")) //$NON-NLS-1$
				return 6;
			if (v.startsWith("oth")) //$NON-NLS-1$
				return 255;
			return 0;
		}
	};

	public static final QueryField EXIF_LIGHTSOURCE = new QueryField(EXIF_CAMERA, "lightSource", //$NON-NLS-1$
			"LightSource", //$NON-NLS-1$
			NS_EXIF, "LightSource", //$NON-NLS-1$
			Messages.QueryField_Light_source, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_EXIF, T_INTEGER, 1, 3,
			new int[] { 0, 1, 2, 3, 4, 9, 10, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 255 },
			new String[] { Messages.QueryField_Unknown, Messages.QueryField_Daylight, Messages.QueryField_Fluorescent,
					Messages.QueryField_Tungsten, Messages.QueryField_Flash, Messages.QueryField_Fin_weather,
					Messages.QueryField_Cloudy_weather, Messages.QueryField_Shade,
					Messages.QueryField_Daylight_fluorescent, Messages.QueryField_Day_white_fluorescent,
					Messages.QueryField_Cool_white_fluorescent, Messages.QueryField_White_fluorescent,
					Messages.QueryField_Standard_light_A, Messages.QueryField_Standard_light_B,
					Messages.QueryField_Standard_light_C, "D55", "D65", "D75", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"D50", Messages.QueryField_ISO_studio_tungsten, //$NON-NLS-1$
					Messages.QueryField_Other_light_source },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getLightSource();
		}

		@Override
		public int handleIntErrand(String v) throws UnsupportedOperationException {
			if (v.indexOf("fluo") >= 0) { //$NON-NLS-1$
				if (v.startsWith("daylight")) //$NON-NLS-1$
					return 12;
				if (v.startsWith("day")) //$NON-NLS-1$
					return 13;
				if (v.startsWith("cool")) //$NON-NLS-1$
					return 14;
				if (v.startsWith("white")) //$NON-NLS-1$
					return 15;
				if (v.startsWith("warm")) //$NON-NLS-1$
					return 16;
				return 2;
			}
			if (v.startsWith("daylight") || v.startsWith("natur")) //$NON-NLS-1$ //$NON-NLS-2$
				return 1;
			if (v.startsWith("tung")) //$NON-NLS-1$
				return 3;
			if (v.startsWith("flash")) //$NON-NLS-1$
				return 4;
			if (v.startsWith("iso")) //$NON-NLS-1$
				return 24;
			if (v.startsWith("fine")) //$NON-NLS-1$
				return 9;
			if (v.startsWith("cloud")) //$NON-NLS-1$
				return 10;
			if (v.startsWith("hade")) //$NON-NLS-1$
				return 11;
			if (v.startsWith("standard")) //$NON-NLS-1$
				return v.endsWith("a") //$NON-NLS-1$
						? 17
						: v.endsWith("b") //$NON-NLS-1$
								? 18
								: 19;
			if (v.equals("d55")) //$NON-NLS-1$
				return 20;
			if (v.equals("d65")) //$NON-NLS-1$
				return 21;
			if (v.equals("d75")) //$NON-NLS-1$
				return 22;
			if (v.equals("d50")) //$NON-NLS-1$
				return 23;
			if (v.startsWith("oth")) //$NON-NLS-1$
				return 255;
			return 0;
		}
	};

	public static final QueryField EXIF_FLASH = new QueryField(EXIF_CAMERA, "exif_flash", null, NS_EXIF, "Flash", //$NON-NLS-1$ //$NON-NLS-2$
			Messages.QueryField_Flash_group, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_EXIF, T_NONE, 1, T_NONE, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField EXIF_FLASHFIRED = new QueryField(EXIF_FLASH, "flashFired", //$NON-NLS-1$
			"FlashFired", //$NON-NLS-1$
			NS_EXIF, "Flash/Fired", //$NON-NLS-1$
			Messages.QueryField_Flash_fired, ACTION_QUERY,
			PHOTO | EDIT_ANALOG | ESSENTIAL | QUERY | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_BOOLEAN, 1, 5,
			Format.booleanFormatter, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected boolean getBoolean(Asset asset) {
			return asset.getFlashFired();
		}
	};
	public static final QueryField EXIF_RETURNLIGHTDETECTED = new QueryField(EXIF_FLASH, "returnLightDetected", //$NON-NLS-1$
			"FlashReturn", //$NON-NLS-1$
			NS_EXIF, "Flash/Return", Messages.QueryField_Return_light, //$NON-NLS-1$
			ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_INTEGER, 1, 5,
			new int[] { 0, 2, 3 },
			new String[] { Messages.QueryField_No_strobe_return_detection,
					Messages.QueryField_Strobe_return_light_not_detected,
					Messages.QueryField_Strobe_return_light_detected },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getReturnLightDetected();
		}
	};
	public static final QueryField EXIF_FLASHAUTO = new QueryField(EXIF_FLASH, "flashAuto", "FlashAuto", NS_EXIF, //$NON-NLS-1$ //$NON-NLS-2$
			"Flash/Mode", Messages.QueryField_Flash_auto, ACTION_QUERY, //$NON-NLS-1$
			PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_INTEGER, 1, 5,
			new int[] { 0, 1, 2, 3 },
			new String[] { Messages.QueryField_Unknown, Messages.QueryField_Compulsory_flash_firing,
					Messages.QueryField_Compulsory_flash_suppression, Messages.QueryField_Auto_mode },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getFlashAuto();
		}
	};
	public static final QueryField EXIF_FLASHFUNCTION = new QueryField(EXIF_FLASH, "flashFunction", //$NON-NLS-1$
			"FlashFunction", //$NON-NLS-1$
			NS_EXIF, "Flash/Function", //$NON-NLS-1$
			Messages.QueryField_Flash_function, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | AUTO_DISCRETE | REPORT,
			CATEGORY_EXIF, T_BOOLEAN, 1, 5, Format.booleanFormatter, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected boolean getBoolean(Asset asset) {
			return asset.getFlashFunction();
		}
	};
	public static final QueryField EXIF_REDEYEREDUCTION = new QueryField(EXIF_FLASH, "redEyeReduction", //$NON-NLS-1$
			"FlashRedEyeMode", //$NON-NLS-1$
			NS_EXIF, "Flash/RedEyeMode", //$NON-NLS-1$
			Messages.QueryField_Red_eye, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | AUTO_DISCRETE | REPORT,
			CATEGORY_EXIF, T_BOOLEAN, 1, 5, Format.booleanFormatter, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected boolean getBoolean(Asset asset) {
			return asset.getRedEyeReduction();
		}
	};
	public static final QueryField EXIF_FOCALLENGTH = new QueryField(EXIF_CAMERA, "focalLength", //$NON-NLS-1$
			"FocalLength", //$NON-NLS-1$
			NS_EXIF, "FocalLength", //$NON-NLS-1$
			Messages.QueryField_Focal_length, ACTION_QUERY, PHOTO | EDIT_ANALOG | ESSENTIAL | QUERY | AUTO_LOG | REPORT,
			CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 1, 5f, 20000f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getFocalLength();
		}
	};
	public static final QueryField EXIF_FLASHENERGY = new QueryField(EXIF_FLASH, "flashEnergy", //$NON-NLS-1$
			"FlashEnergy", //$NON-NLS-1$
			NS_EXIF, "FlashEnergy", //$NON-NLS-1$
			Messages.QueryField_Flash_energy, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | AUTO_LOG | REPORT,
			CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 3, 10f, 99999f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getFlashEnergy();
		}
	};
	public static final QueryField EXIF_FLASHEXPOSURECOMP = new QueryField(EXIF_FLASH, "flashExposureComp", //$NON-NLS-1$
			"FlashCompensation", //$NON-NLS-1$
			NS_EXIF, "FlashExposureComp", //$NON-NLS-1$
			Messages.QueryField_Flash_exposure_compensation, ACTION_QUERY,
			PHOTO | EDIT_ANALOG | QUERY | AUTO_LOG | REPORT, CATEGORY_EXIF, T_FLOAT, 1, 3, 10f, 16f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getFlashExposureComp();
		}
	};
	public static final QueryField EXIF_FOCALPLANEXRESOLUTION = new QueryField(EXIF_CAMERA, "focalPlaneXResolution", //$NON-NLS-1$
			"FocalPlaneXResolution", //$NON-NLS-1$
			NS_EXIF, "FocalPlaneXResolution", //$NON-NLS-1$
			Messages.QueryField_Focal_plane_Xres, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | AUTO_LINEAR | REPORT,
			CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 1, 0f, 99999f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getFocalPlaneXResolution();
		}
	};
	public static final QueryField EXIF_FOCALPLANEYRESOLUTION = new QueryField(EXIF_CAMERA, "focalPlaneYResolution", //$NON-NLS-1$
			"FocalPlaneYResolution", //$NON-NLS-1$
			NS_EXIF, "FocalPlaneYResolution", //$NON-NLS-1$
			Messages.QueryField_Focal_plane_Yres, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | AUTO_LINEAR | REPORT,
			CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 1, 0f, 99999f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getFocalPlaneYResolution();
		}
	};
	public static final QueryField EXIF_EXPOSUREINDEX = new QueryField(EXIF_CAMERA, "exposureIndex", //$NON-NLS-1$
			"ExposureIndex", //$NON-NLS-1$
			NS_EXIF, "ExposureIndex", //$NON-NLS-1$
			Messages.QueryField_Exposure_index, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | AUTO_LINEAR | REPORT,
			CATEGORY_EXIF, T_FLOAT, 1, 2, -0.5f, 99f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getExposureIndex();
		}
	};
	public static final QueryField EXIF_SENSINGMETHOD = new QueryField(EXIF_CAMERA, "sensingMethod", "SensingMethod", //$NON-NLS-1$ //$NON-NLS-2$
			NS_EXIF, "SensingMethod", Messages.QueryField_Sensing_method, ACTION_QUERY, //$NON-NLS-1$
			PHOTO | EDIT_NEVER | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_INTEGER, 1, 1,
			new int[] { 0, 1, 2, 3, 4, 5, 7, 8 },
			new String[] { Format.MISSINGENTRYSTRING, Messages.QueryField_Undefined, Messages.QueryField_OneChip,
					Messages.QueryField_TwoChip, Messages.QueryField_ThreeChip, Messages.QueryField_Color_sequential,
					Messages.QueryField_Trilinear, Messages.QueryField_ColorSequential },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getSensingMethod();
		}
	};
	public static final QueryField EXIF_FILESOURCE = new QueryField(EXIF_CAMERA, "fileSource", "FileSource", //$NON-NLS-1$ //$NON-NLS-2$
			NS_EXIF, "FileSource", Messages.QueryField_File_source, ACTION_QUERY, //$NON-NLS-1$
			PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_INTEGER, 1, 4,
			new int[] { Constants.FILESOURCE_UNKNOWN, Constants.FILESOURCE_FILMSCANNER,
					Constants.FILESOURCE_REFLECTIVE_SCANNER, Constants.FILESOURCE_DIGITAL_CAMERA,
					Constants.FILESOURCE_SIGMA_DIGITAL_CAMERA },
			new String[] { Format.MISSINGENTRYSTRING, Messages.QueryField_Film_scanner,
					Messages.QueryField_Reflection_Scanner, Messages.QueryField_Digital_Camera,
					Messages.QueryField_Sigma_Digital_Camera },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getFileSource();
		}
	};
	public static final QueryField EXIF_EXPOSUREMODE = new QueryField(EXIF_CAMERA, "exposureMode", "ExposureMode", //$NON-NLS-1$ //$NON-NLS-2$
			NS_EXIF, "ExposureMode", Messages.QueryField_Exposure_mode, ACTION_QUERY, //$NON-NLS-1$
			PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_INTEGER, 1, 1,
			new int[] { 0, 1, 2 }, new String[] { Messages.QueryField_Auto_exposure,
					Messages.QueryField_Manual_exposure, Messages.QueryField_Auto_bracket },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getExposureMode();
		}

		@Override
		public int handleIntErrand(String v) throws UnsupportedOperationException {
			if (v.indexOf("brack") >= 0) //$NON-NLS-1$
				return 2;
			if (v.startsWith("man")) //$NON-NLS-1$
				return 1;
			return 0;
		}
	};
	public static final QueryField EXIF_WHITEBALANCE = new QueryField(EXIF_CAMERA, "whiteBalance", //$NON-NLS-1$
			"WhiteBalance", //$NON-NLS-1$
			NS_EXIF, "WhiteBalance", //$NON-NLS-1$
			Messages.QueryField_White_balance, ACTION_QUERY,
			PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_STRING, 1, 16, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getWhiteBalance();
		}
	};
	public static final QueryField EXIF_DIGITALZOOMRATIO = new QueryField(EXIF_CAMERA, "digitalZoomRatio", //$NON-NLS-1$
			"DigitalZoomRatio", //$NON-NLS-1$
			NS_EXIF, "DigitalZoomRatio", //$NON-NLS-1$
			Messages.QueryField_Digital_zoom_ratio, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | AUTO_LOG | REPORT,
			CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 2, 5f, 256f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getDigitalZoomRatio();
		}
	};
	public static final QueryField EXIF_FOCALLENGTHIN35MMFILM = new QueryField(EXIF_CAMERA, "focalLengthIn35MmFilm", //$NON-NLS-1$
			"FocalLength35efl", //$NON-NLS-1$
			NS_EXIF, "FocalLengthIn35mmFilm", //$NON-NLS-1$
			Messages.QueryField_Focal_length_35, ACTION_QUERY, PHOTO | EDIT_ANALOG | HOVER | QUERY | AUTO_LOG | REPORT,
			CATEGORY_EXIF, T_POSITIVEINTEGER, 1, 4, 5f, 20000f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getFocalLengthIn35MmFilm();
		}
	};
	public static final QueryField EXIF_FOCALLENGTHFACTOR = new QueryField(EXIF_CAMERA, "focalLengthFactor", //$NON-NLS-1$
			"ScaleFactor35efl", //$NON-NLS-1$
			null, null, Messages.QueryField_Focal_length_factor, ACTION_QUERY,
			PHOTO | EDIT_ANALOG | QUERY | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 2, 1f, 200f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getFocalLengthFactor();
		}
	};
	public static final QueryField EXIF_CIRCLEOFCONFUSION = new QueryField(EXIF_CAMERA, "circleOfConfusion", //$NON-NLS-1$
			"CircleOfConfusion", //$NON-NLS-1$
			null, null, Messages.QueryField_Circle_of_confusion, ACTION_QUERY,
			PHOTO | EDIT_ANALOG | QUERY | AUTO_LOG | REPORT, CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 4, 0.01f, 1f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getCircleOfConfusion();
		}
	};
	public static final QueryField EXIF_HYPERFOCALDISTANCE = new QueryField(EXIF_CAMERA, "hyperfocalDistance", //$NON-NLS-1$
			"HyperfocalDistance", //$NON-NLS-1$
			null, null, Messages.QueryField_Hyperfocal_distance, ACTION_QUERY,
			PHOTO | EDIT_ANALOG | QUERY | AUTO_LOG | REPORT, CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 2, 10f, 1000f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getHyperfocalDistance();
		}
	};
	public static final QueryField EXIF_DOF = new QueryField(EXIF_CAMERA, "dof", //$NON-NLS-1$
			"DOF", //$NON-NLS-1$
			null, null, Messages.QueryField_Depth_of_field, ACTION_QUERY, QUERY | PHOTO | EDIT_ANALOG | REPORT,
			CATEGORY_EXIF, T_POSITIVEFLOAT, 2, 2, 10f, Float.POSITIVE_INFINITY, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getDof();
		}
	};
	public static final QueryField EXIF_LV = new QueryField(EXIF_CAMERA, "lv", //$NON-NLS-1$
			"LightValue", //$NON-NLS-1$
			null, null, "LV", //$NON-NLS-1$
			ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | AUTO_LINEAR | REPORT, CATEGORY_EXIF, T_FLOAT, 1, 1, -0.5f,
			Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getLv();
		}
	};
	public static final QueryField EXIF_FOV = new QueryField(EXIF_CAMERA, "fov", //$NON-NLS-1$
			"FOV", //$NON-NLS-1$
			null, null, Messages.QueryField_Field_of_View, ACTION_QUERY, QUERY | PHOTO | EDIT_ANALOG | REPORT,
			CATEGORY_EXIF, T_FLOAT, 2, 2, 5f, Float.POSITIVE_INFINITY, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getFov();
		}
	};
	public static final QueryField EXIF_SCENECAPTURETYPE = new QueryField(EXIF_CAMERA, "sceneCaptureType", //$NON-NLS-1$
			"SceneCaptureType", //$NON-NLS-1$
			NS_EXIF, "SceneCaptureType", Messages.QueryField_Scene_capture_type, //$NON-NLS-1$
			ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_INTEGER, 1, 1,
			new int[] { 0, 1, 2, 3 }, new String[] { Messages.QueryField_Standard, Messages.QueryField_Landscape,
					Messages.QueryField_Portrait, Messages.QueryField_Night_scene },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getSceneCaptureType();
		}

		@Override
		public int handleIntErrand(String v) throws UnsupportedOperationException {
			if (v.startsWith("land")) //$NON-NLS-1$
				return 1;
			if (v.startsWith("port")) //$NON-NLS-1$
				return 2;
			if (v.startsWith("night")) //$NON-NLS-1$
				return 3;
			return 0;
		}
	};
	public static final QueryField EXIF_GAINCONTROL = new QueryField(EXIF_CAMERA, "gainControl", "GainControl", //$NON-NLS-1$ //$NON-NLS-2$
			NS_EXIF, "GainControl", Messages.QueryField_Gain_control, ACTION_QUERY, //$NON-NLS-1$
			PHOTO | EDIT_NEVER | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_INTEGER, 1, 1,
			new int[] { 0, 1, 2, 3, 4 },
			new String[] { Messages.QueryField_None, Messages.QueryField_Low_gain_up, Messages.QueryField_High_gain_up,
					Messages.QueryField_Low_gain_down, Messages.QueryField_High_gain_down },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getGainControl();
		}

		@Override
		public int handleIntErrand(String v) throws UnsupportedOperationException {
			if (v.startsWith("lo")) //$NON-NLS-1$
				return v.indexOf("up") >= 0 ? 1 : 3; //$NON-NLS-1$
			if (v.startsWith("hi")) //$NON-NLS-1$
				return v.indexOf("up") >= 0 ? 2 : 4; //$NON-NLS-1$
			return 0;
		}
	};
	public static final QueryField EXIF_CONTRAST = new QueryField(EXIF_CAMERA, "contrast", "Contrast", NS_EXIF, //$NON-NLS-1$ //$NON-NLS-2$
			"Contrast", //$NON-NLS-1$
			Messages.QueryField_Contrast, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_EXIF, T_INTEGER, 1, 1, new int[] { 0, 1, 2 }, new String[] { Messages.QueryField_Normal_contrast,
					Messages.QueryField_Soft_contrast, Messages.QueryField_Hard_contrast },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getContrast();
		}

		@Override
		public int handleIntErrand(String v) throws UnsupportedOperationException {
			if (v.startsWith("lo")) //$NON-NLS-1$
				return 1;
			if (v.startsWith("hi")) //$NON-NLS-1$
				return 2;
			return 0;
		}
	};
	public static final QueryField EXIF_SATURATION = new QueryField(EXIF_CAMERA, "saturation", "Saturation", //$NON-NLS-1$ //$NON-NLS-2$
			NS_EXIF, "Saturation", Messages.QueryField_Saturation, ACTION_QUERY, //$NON-NLS-1$
			PHOTO | EDIT_NEVER | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_INTEGER, 1, 1,
			new int[] { 0, 1, 2 },
			new String[] { Messages.QueryField_Normal_saturation, Messages.QueryField_Low, Messages.QueryField_High },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getSaturation();
		}

		@Override
		public int handleIntErrand(String v) throws UnsupportedOperationException {
			if (v.startsWith("lo")) //$NON-NLS-1$
				return 1;
			if (v.startsWith("hi")) //$NON-NLS-1$
				return 2;
			return 0;
		}

	};
	public static final QueryField EXIF_SHARPNESS = new QueryField(EXIF_CAMERA, "sharpness", "Sharpness", NS_EXIF, //$NON-NLS-1$ //$NON-NLS-2$
			"Sharpness", //$NON-NLS-1$
			Messages.QueryField_Sharpness, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_EXIF, T_INTEGER, 1, 1, new int[] { 0, 1, 2 }, new String[] { Messages.QueryField_Normal_sharpness,
					Messages.QueryField_Soft_sharpness, Messages.QueryField_Hard_sharpness },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getSharpness();
		}

		@Override
		public int handleIntErrand(String v) throws UnsupportedOperationException {
			if (v.startsWith("soft")) //$NON-NLS-1$
				return 1;
			if (v.startsWith("hard")) //$NON-NLS-1$
				return 2;
			return 0;
		}

	};
	public static final QueryField EXIF_VIBRANCE = new QueryField(EXIF_CAMERA, "vibrance", //$NON-NLS-1$
			"Vibrance", //$NON-NLS-1$
			NS_EXIF, "Vibrance", //$NON-NLS-1$
			Messages.QueryField_Vibrance, ACTION_QUERY, PHOTO | EDIT_NEVER | QUERY | AUTO_LINEAR | REPORT,
			CATEGORY_EXIF, T_INTEGER, 1, 1, 0f, 9f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getVibrance();
		}
	};
	public static final QueryField EXIF_SUBJECTDISTANCERANGE = new QueryField(EXIF_CAMERA, "subjectDistanceRange", //$NON-NLS-1$
			"SubjectDistanceRange", NS_EXIF, "SubjectDistanceRange", Messages.QueryField_Subject_distance_range, //$NON-NLS-1$ //$NON-NLS-2$
			ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_EXIF, T_INTEGER, 1, 1,
			new int[] { 0, 1, 2, 3 }, new String[] { Messages.QueryField_Unknown, Messages.QueryField_Macro,
					Messages.QueryField_Close_view, Messages.QueryField_Distant },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getSubjectDistanceRange();
		}

		@Override
		public int handleIntErrand(String v) throws UnsupportedOperationException {
			if (v.startsWith("macr")) //$NON-NLS-1$
				return 1;
			if (v.startsWith("clos")) //$NON-NLS-1$
				return 2;
			if (v.startsWith("dist")) //$NON-NLS-1$
				return 3;
			return 0;
		}

	};
	public static final QueryField EXIF_IMAGEUNIQUEID = new QueryField(EXIF_CAMERA, "originalImageId", //$NON-NLS-1$
			"ImageUniqueID", //$NON-NLS-1$
			NS_EXIF, "ImageUniqueID", //$NON-NLS-1$
			null, ACTION_NONE, PHOTO | EDIT_NEVER | QUERY, CATEGORY_EXIF, T_STRING, 1, 32, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getOriginalImageId();
		}
	};
	// Exif Aux fields
	public static final QueryField EXIF_AUX = new QueryField(EXIF_ALL, "exif_aux", null, null, null, //$NON-NLS-1$
			Messages.QueryField_Auxiliary, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_EXIF, T_NONE, 1, T_NONE, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);

	public static final QueryField EXIF_LENS = new QueryField(EXIF_AUX, "lens", //$NON-NLS-1$
			"Lens", //$NON-NLS-1$
			NS_AUX, "Lens", //$NON-NLS-1$
			Messages.QueryField_Lens, ACTION_QUERY, PHOTO | EDIT_ANALOG | HOVER | QUERY | AUTO_DISCRETE | REPORT,
			CATEGORY_EXIF, T_STRING, 1, 50, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getLens();
		}
	};
	public static final QueryField EXIF_LENSSERIAL = new QueryField(EXIF_AUX, "lensSerial", //$NON-NLS-1$
			"LensSerialNumber", //$NON-NLS-1$
			NS_AUX, "LensSerialNumber", //$NON-NLS-1$
			Messages.QueryField_Lens_Serial_Number, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | AUTO_DISCRETE,
			CATEGORY_EXIF, T_STRING, 1, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getLensSerial();
		}
	};
	public static final QueryField EXIF_SERIAL = new QueryField(EXIF_AUX, "serial", //$NON-NLS-1$
			"SerialNumber", //$NON-NLS-1$
			NS_AUX, "SerialNumber", //$NON-NLS-1$
			Messages.QueryField_Serial_Number, ACTION_QUERY, PHOTO | EDIT_ANALOG | QUERY | TEXT | AUTO_DISCRETE,
			CATEGORY_EXIF, T_STRING, 1, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getSerial();
		}
	};

	public static final QueryField EXIF_MAKERNOTES = new QueryField(EXIF_AUX, "makerNotes", //$NON-NLS-1$
			null, null, null, Messages.QueryField_maker_notes, ACTION_NONE, PHOTO | EDIT_NEVER | AUTO_CONTAINS,
			CATEGORY_EXIF, T_STRING, CARD_LIST, 255, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getMakerNotes();
		}

		@Override
		public void mergeValues(Object source, Object target) {
			String[] newValue = (String[]) obtainPlainFieldValue(source);
			if (newValue.length > 0) {
				String[] oldValue = (String[]) obtainPlainFieldValue(target);
				if (oldValue.length == 0)
					setPlainFieldValue(target, newValue);
				else {
					Map<String, String> notes = new HashMap<String, String>(oldValue.length + newValue.length);
					for (String note : oldValue) {
						int p = note.indexOf(": "); //$NON-NLS-1$
						if (p >= 0)
							notes.put(note.substring(0, p), note);
					}
					for (String note : newValue) {
						int p = note.indexOf(": "); //$NON-NLS-1$
						if (p >= 0) {
							String key = note.substring(0, p);
							if (!notes.containsKey(key))
								notes.put(key, note);
						}
					}
					setPlainFieldValue(target, notes.values().toArray(new String[notes.size()]));
				}
			}
		}

		@Override
		protected String doValueToText(Object value, boolean useEnums, boolean useFormatter, Locale inLocale,
				boolean subField) {
			String[] array = (String[]) value;
			if (array.length > 0)
				return NLS.bind(Messages.QueryField_n_makernotes, array.length);
			return Format.MISSINGENTRYSTRING;
		}
	};
	// Exif GPS fields
	public static final QueryField EXIF_GPS = new QueryField(EXIF_ALL, "exif_gps", null, null, null, "GPS", //$NON-NLS-1$ //$NON-NLS-2$
			ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_EXIF, T_NONE, 1, T_NONE, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);

	public static final QueryField EXIF_GPSLATITUDE = new QueryField(EXIF_GPS, "gPSLatitude", //$NON-NLS-1$
			"GPSLatitude", //$NON-NLS-1$
			NS_EXIF, "GPSLatitude", //$NON-NLS-1$
			Messages.QueryField_GPS_Latitude, ACTION_MAP, PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY, CATEGORY_EXIF,
			T_FLOAT, 1, 5, Format.latitudeFormatter, -0.0005f, 90f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getGPSLatitude();
		}

	};
	public static final QueryField EXIF_GPSLONGITUDE = new QueryField(EXIF_GPS, "gPSLongitude", //$NON-NLS-1$
			"GPSLongitude", //$NON-NLS-1$
			NS_EXIF, "GPSLongitude", //$NON-NLS-1$
			Messages.QueryField_GPS_Longitude, ACTION_MAP, PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY, CATEGORY_EXIF,
			T_FLOAT, 1, 5, Format.longitudeFormatter, -0.0005f, 180f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getGPSLongitude();
		}

	};
	public static final QueryField EXIF_GPSALTITUDE = new QueryField(EXIF_GPS, "gPSAltitude", //$NON-NLS-1$
			"GPSAltitude", //$NON-NLS-1$
			NS_EXIF, "GPSAltitude", //$NON-NLS-1$
			Messages.QueryField_GPS_Altitude, ACTION_NONE, PHOTO | EDIT_ALWAYS | QUERY | AUTO_LOG | REPORT,
			CATEGORY_EXIF, T_FLOAT, 1, 2, Format.altitudeFormatter, -50f, 20000f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getGPSAltitude();
		}
	};
	public static final QueryField EXIF_GPSSPEED = new QueryField(EXIF_GPS, "gPSSpeed", //$NON-NLS-1$
			"GPSSpeed", //$NON-NLS-1$
			NS_EXIF, "GPSSpeed", //$NON-NLS-1$
			Messages.QueryField_GPS_Speed, ACTION_NONE, PHOTO | EDIT_ALWAYS | QUERY | AUTO_LOG | REPORT, CATEGORY_EXIF,
			T_POSITIVEFLOAT, 1, 3, 3f, 2500f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getGPSSpeed();
		}
	};
	public static final QueryField EXIF_GPSTRACK = new QueryField(EXIF_GPS, "gPSTrack", //$NON-NLS-1$
			"GPSTrack", //$NON-NLS-1$
			NS_EXIF, "GPSTrack", //$NON-NLS-1$
			Messages.QueryField_move_dir, ACTION_NONE, PHOTO | EDIT_ALWAYS | QUERY | AUTO_LINEAR | REPORT,
			CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 3, -3f, 360f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getGPSTrack();
		}
	};
	public static final QueryField EXIF_GPSTRACKREF = new QueryField(EXIF_GPS, "gPSTrackRef", //$NON-NLS-1$
			"GPSTrackRef", //$NON-NLS-1$
			NS_EXIF, "GPSTrackRef", //$NON-NLS-1$
			Messages.QueryField_move_dir_ref, ACTION_NONE, PHOTO | EDIT_ALWAYS | QUERY | AUTO_DISCRETE | REPORT,
			CATEGORY_EXIF, T_STRING, 1, 1, REFVALUES, REFLABELS, null, 0, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getGPSTrackRef();
		}
	};

	public static final QueryField EXIF_GPSIMAGEDIR = new QueryField(EXIF_GPS, "gPSImgDirection", //$NON-NLS-1$
			"GPSImgDirection", //$NON-NLS-1$
			NS_EXIF, "GPSImgDirection", //$NON-NLS-1$
			Messages.QueryField_img_dir, ACTION_QUERY, PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY | AUTO_LINEAR | REPORT,
			CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 3, Format.directionFormatter, -1f, 360f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getGPSImgDirection();
		}
	};
	public static final QueryField EXIF_GPSIMAGEDIRREF = new QueryField(EXIF_GPS, "gPSImgDirectionRef", //$NON-NLS-1$
			"GPSImgDirectionRef", //$NON-NLS-1$
			NS_EXIF, "GPSImgDirectionRef", //$NON-NLS-1$
			Messages.QueryField_img_dir_ref, ACTION_NONE, PHOTO | EDIT_ALWAYS | QUERY | AUTO_DISCRETE | REPORT,
			CATEGORY_EXIF, T_STRING, 1, 1, REFVALUES, REFLABELS, null, 0, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getGPSImgDirectionRef();
		}
	};
	public static final QueryField EXIF_GPSLOCATIONDISTANCE = new QueryField(EXIF_GPS, "$gpsLocationDistance", //$NON-NLS-1$
			null, null, null, Messages.QueryField_Distance_from_location, ACTION_NONE, PHOTO | EDIT_NEVER,
			CATEGORY_EXIF, T_POSITIVEFLOAT, 1, 3, null, null, Format.distanceFormatter, 3f,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			// TODO kann das weg?
			double gpsLatitude = asset.getGPSLatitude();
			if (Double.isNaN(gpsLatitude))
				return Double.NaN;
			double gpsLongitude = asset.getGPSLongitude();
			if (Double.isNaN(gpsLongitude))
				return Double.NaN;
			Object loc = IPTC_LOCATIONCREATED.obtainFieldValue(asset);
			if (loc == null)
				return Double.NaN;
			LocationImpl location = (LocationImpl) loc;
			if (location.getLatitude() == null || Double.isNaN(location.getLatitude())
					|| location.getLongitude() == null || Double.isNaN(location.getLongitude()))
				return Double.NaN;
			return Core.distance(gpsLatitude, gpsLongitude, location.getLatitude(), location.getLongitude(), 'K');
		}
	};

	// IPTC fields
	public static final QueryField IPTC_ALL = new QueryField(ALL, null, null, null, null, "IPTC", ACTION_NONE, //$NON-NLS-1$
			PHOTO | EDIT_NEVER, CATEGORY_IPTC, T_NONE, 1, T_NONE, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);

	// IPTC description fields
	public static final QueryField IPTC_DESCRIPTION = new QueryField(IPTC_ALL, "iptc_description", null, null, null, //$NON-NLS-1$
			Messages.QueryField_Description, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_IPTC, T_NONE, 1, T_NONE, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);

	public static final QueryField IPTC_HEADLINE = new QueryField(IPTC_DESCRIPTION, "headline", //$NON-NLS-1$
			"Headline", //$NON-NLS-1$
			NS_PHOTOSHOP, "Headline", //$NON-NLS-1$
			Messages.QueryField_Headline, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_CONTAINS,
			CATEGORY_IPTC, T_STRING, 1, 255, 0f, Float.NaN, ISpellCheckingService.TITLEOPTIONS) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getHeadline();
		}
	};
	public static final QueryField IPTC_TITLE = new QueryField(IPTC_DESCRIPTION, "title", //$NON-NLS-1$
			"Title", //$NON-NLS-1$
			NS_DC, "title", //$NON-NLS-1$
			Messages.QueryField_Title, ACTION_QUERY, PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY | TEXT | AUTO_CONTAINS,
			CATEGORY_IPTC, T_STRING, 1, 255, 0f, Float.NaN, ISpellCheckingService.TITLEOPTIONS) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getTitle();
		}
	};

	public static final QueryField IPTC_INTELLECTUAL_GENRE = new QueryField(IPTC_DESCRIPTION, "intellectualGenre", //$NON-NLS-1$
			null, NS_IPTC4XMPCORE, "IntellectualGenre", //$NON-NLS-1$
			Messages.QueryField_Intellectual_genre, ACTION_QUERY,
			PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_CONTAINS | REPORT, CATEGORY_IPTC, T_STRING, 1, 64, 0f, Float.NaN,
			ISpellCheckingService.DESCRIPTIONOPTIONS) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getIntellectualGenre();
		}
	};
	public static final QueryField IPTC_WRITEREDITOR = new QueryField(IPTC_DESCRIPTION, "writerEditor", //$NON-NLS-1$
			"Writer-Editor", //$NON-NLS-1$
			NS_PHOTOSHOP, "CaptionWriter", //$NON-NLS-1$
			Messages.QueryField_Description_writer, ACTION_QUERY,
			PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_IPTC, T_STRING, 1, 32, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getWriterEditor();
		}
	};
	public static final QueryField IPTC_SPECIALINSTRUCTIONS = new QueryField(IPTC_DESCRIPTION, "specialInstructions", //$NON-NLS-1$
			"SpecialInstructions", //$NON-NLS-1$
			NS_PHOTOSHOP, "Instructions", //$NON-NLS-1$
			Messages.QueryField_Special_Instructions, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_CONTAINS,
			CATEGORY_IPTC, T_STRING, 1, 255, 0f, Float.NaN, ISpellCheckingService.DESCRIPTIONOPTIONS) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getSpecialInstructions();
		}
	};
	public static final QueryField IPTC_JOBID = new QueryField(IPTC_DESCRIPTION, "jobId", //$NON-NLS-1$
			"JobID", //$NON-NLS-1$
			NS_PHOTOSHOP, "TransmissionReference", //$NON-NLS-1$
			Messages.QueryField_Job_ID, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | REPORT, CATEGORY_IPTC,
			T_STRING, 1, 64, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getJobId();
		}
	};
	public static final QueryField IPTC_MODELINFO = new QueryField(IPTC_DESCRIPTION, "modelInformation", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "AddlModelInfo", //$NON-NLS-1$
			Messages.QueryField_Additional_model_information, ACTION_QUERY,
			PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_CONTAINS, CATEGORY_IPTC, T_STRING, 1, 255, 0f, Float.NaN,
			ISpellCheckingService.DESCRIPTIONOPTIONS) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getModelInformation();
		}
	};
	public static final QueryField IPTC_MODELAGE = new QueryField(IPTC_DESCRIPTION, "modelAge", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "ModelAge", //$NON-NLS-1$
			Messages.QueryField_Model_age, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | AUTO_LINEAR | REPORT,
			CATEGORY_IPTC, T_POSITIVEINTEGER, 1, 3, 0f, 125f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getModelAge();
		}
	};
	public static final QueryField IPTC_ARTWORK = new QueryField(IPTC_DESCRIPTION, "iptc_artwork_shown", //$NON-NLS-1$
			"ObjectName", //$NON-NLS-1$
			NS_IPTC4XMPEXT, "ArtworkOrObject", //$NON-NLS-1$
			Messages.QueryField_ArtworkObject, ACTION_NONE, PHOTO | EDIT_ALWAYS | QUERY | TEXT, CATEGORY_IPTC, T_OBJECT,
			CARD_LIST, 64, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {
		@Override
		public Object getStruct(Asset asset) {
			List<ArtworkOrObjectShownImpl> set = CoreActivator.getDefault().getDbManager()
					.obtainStructForAsset(ArtworkOrObjectShownImpl.class, asset.getStringId(), false);
			int i = 0;
			String[] group = new String[set.size()];
			for (ArtworkOrObjectShownImpl rel : set)
				group[i++] = rel.getArtworkOrObject();
			return group;
		}

		@Override
		public Object getStructRelationIds(Asset asset) {
			List<String> rels = asset.getArtworkOrObjectShown_parent();
			return rels.toArray(new String[rels.size()]);
		}
	};
	public static final QueryField IPTC_CODEOFORG = new QueryField(IPTC_DESCRIPTION, "codeOfOrg", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "OrganisationInImageCode", //$NON-NLS-1$
			Messages.QueryField_Code_of_featured_org, ACTION_QUERY,
			PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_IPTC, T_STRING, CARD_MODIFIABLEBAG,
			32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getCodeOfOrg();
		}
	};
	public static final QueryField IPTC_NAMEOFORG = new QueryField(IPTC_DESCRIPTION, "nameOfOrg", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "OrganisationInImageName", //$NON-NLS-1$
			Messages.QueryField_Name_of_featured_org, ACTION_QUERY,
			PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_IPTC, T_STRING, CARD_MODIFIABLEBAG,
			32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getNameOfOrg();
		}
	};
	public static final QueryField IPTC_PERSONSHOWN = new QueryField(IPTC_DESCRIPTION, "personShown", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "PersonInImage", //$NON-NLS-1$
			Messages.QueryField_Persons_shown, ACTION_QUERY,
			PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY | TEXT | AUTO_CONTAINS | REPORT, CATEGORY_IPTC, T_STRING,
			CARD_MODIFIABLEBAG, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getPersonShown();
		}
	};

	// IPTC admin fields
	public static final QueryField IPTC_ADMIN = new QueryField(IPTC_ALL, "iptc_administration", null, null, null, //$NON-NLS-1$
			Messages.QueryField_Administration, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_IPTC, T_NONE, 1, T_NONE, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);

	public static final QueryField IPTC_EVENT = new QueryField(IPTC_ADMIN, "event", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "Event", //$NON-NLS-1$
			Messages.QueryField_Event, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_CONTAINS | REPORT,
			CATEGORY_IPTC, T_STRING, 1, 255, 0f, Float.NaN, ISpellCheckingService.KEYWORDOPTIONS) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getEvent();
		}
	};
	public static final QueryField IPTC_LASTEDITED = new QueryField(IPTC_ADMIN, "lastEdited", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "IptcLastEdited", //$NON-NLS-1$
			Messages.QueryField_Date_IPTC_last_edited, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | AUTO_LINEAR,
			CATEGORY_IPTC, T_DATE, 1, 255, -3600000f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getLastEdited();
		}
	};
	public static final QueryField IPTC_LOCATIONCREATED = new QueryField(IPTC_ADMIN, "iptc_location_created", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "LocationCreated", //$NON-NLS-1$
			Messages.QueryField_Location_created, ACTION_QUERY, PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY | TEXT,
			CATEGORY_IPTC, T_LOCATION, 1, 128, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		public Object getStruct(Asset asset) {
			Iterator<LocationCreatedImpl> it = CoreActivator.getDefault().getDbManager()
					.obtainStructForAsset(LocationCreatedImpl.class, asset.getStringId(), true).iterator();
			return it.hasNext() ? it.next().getLocation() : null;
		}

		@Override
		public Object getStructRelationIds(Asset asset) {
			return asset.getLocationCreated_parent();
		}

	};
	public static final QueryField IPTC_MAXAVAILHEIGHT = new QueryField(IPTC_ADMIN, "maxAvailHeight", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "MaxAvailHeight", //$NON-NLS-1$
			Messages.QueryField_Maximum_height, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | AUTO_LOG | REPORT,
			CATEGORY_IPTC, T_POSITIVEINTEGER, 1, 6, 0f, 999999f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getMaxAvailHeight();
		}
	};
	public static final QueryField IPTC_MAXAVAILWIDTH = new QueryField(IPTC_ADMIN, "maxAvailWidth", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "MaxAvailWidth", //$NON-NLS-1$
			Messages.QueryField_Maximum_width, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | AUTO_LOG | REPORT,
			CATEGORY_IPTC, T_POSITIVEINTEGER, 1, 6, 0f, 999999f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getMaxAvailWidth();
		}
	};

	// IPTC classification fields
	public static final QueryField IPTC_CLASSIFICATION = new QueryField(IPTC_ALL, "iptc_classification", null, null, //$NON-NLS-1$
			null, Messages.QueryField_Classification, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_IPTC, T_NONE, 1, T_NONE,
			0f, Float.NaN, ISpellCheckingService.NOSPELLING);

	public static final QueryField IPTC_KEYWORDS = new QueryField(IPTC_CLASSIFICATION, "keyword", //$NON-NLS-1$
			"Keywords", //$NON-NLS-1$
			NS_DC, "subject", //$NON-NLS-1$
			Messages.QueryField_Keywords, ACTION_QUERY, PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY | TEXT | AUTO_SELECT,
			CATEGORY_IPTC, T_STRING, CARD_MODIFIABLEBAG, 60, 0f, Float.NaN, ISpellCheckingService.KEYWORDOPTIONS) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getKeyword();
		}

		@Override
		public Object obtainPlainFieldValue(Object obj) {
			return ((Asset) obj).getKeyword();
		}

		@Override
		public void setFieldValue(Object target, Object newValue) {
			((Asset) target).setKeyword((String[]) mergeVectoredValue(target, newValue));
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.bdaum.zoom.core.QueryField#doValueToText(java.lang.Object)
		 */

		@Override
		protected String doValueToText(Object value, boolean useEnums, boolean useFormatter, Locale inLocale,
				boolean subField) {
			StringBuilder sb = new StringBuilder();
			String[] array = (String[]) value;
			FilterChain filter = getKeywordFilter();
			for (String v : array) {
				if (filter.accept(v)) {
					String s = formatScalarValue(v, useEnums, useFormatter, inLocale);
					if (s == null)
						return Format.MISSINGENTRYSTRING;
					if (sb.length() > 0)
						sb.append(';');
					sb.append(s);
				}
			}
			return sb.toString();
		}
	};
	public static final QueryField IPTC_CATEGORY = new QueryField(IPTC_CLASSIFICATION, "category", //$NON-NLS-1$
			"Category", //$NON-NLS-1$
			NS_PHOTOSHOP, "Category", //$NON-NLS-1$
			Messages.QueryField_Category, ACTION_QUERY, PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY | TEXT | REPORT,
			CATEGORY_IPTC, T_STRING, 1, 60, 0f, Float.NaN, ISpellCheckingService.KEYWORDOPTIONS) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getCategory();
		}
	};
	public static final QueryField IPTC_SUPPLEMENTALCATEGORIES = new QueryField(IPTC_CLASSIFICATION, "supplementalCats", //$NON-NLS-1$
			"SupplementalCategories", //$NON-NLS-1$
			NS_PHOTOSHOP, "SupplementalCategories", //$NON-NLS-1$
			Messages.QueryField_Supplemental_Categories, ACTION_QUERY,
			PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY | TEXT | AUTO_SELECT, CATEGORY_IPTC, T_STRING, CARD_MODIFIABLEBAG,
			1000, 0f, Float.NaN, ISpellCheckingService.KEYWORDOPTIONS) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getSupplementalCats();
		}
	};

	public static final QueryField MP_LASTKEYWORDXMP = new QueryField(IPTC_CLASSIFICATION, "supplementalCats", //$NON-NLS-1$
			null, NS_MSPHOTO_10, "LastKeywordXMP", //$NON-NLS-1$
			Messages.QueryField_ms_hierarchical_keywords, ACTION_NONE, PHOTO | EDIT_HIDDEN, CATEGORY_IPTC, T_STRING,
			CARD_BAG, 1023, 0, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getSupplementalCats();
		}

		@Override
		public Object obtainPlainFieldValue(Object obj) {
			return ((Asset) obj).getSupplementalCats();
		}

		@Override
		public void setFieldValue(Object target, Object newValue) {
			((Asset) target).setSupplementalCats(
					(String[]) mergeVectoredValue(target, normalizeHierarchy((String[]) newValue)));
		}

		/**
		 * @return unique id
		 */
		@Override
		public String getId() {
			return "msHierarchicalKeywords"; //$NON-NLS-1$
		}

	};

	public static final QueryField LR_HIERARCHICALSUBJECT = new QueryField(IPTC_CLASSIFICATION, "supplementalCats", //$NON-NLS-1$
			null, NS_LIGHTROOM, "hierarchicalSubject", //$NON-NLS-1$
			Messages.QueryField_lr_hierarchical_subjects, ACTION_NONE, PHOTO | EDIT_HIDDEN, CATEGORY_IPTC, T_STRING,
			CARD_BAG, 1023, 0, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			String[] supplementalCats = asset.getSupplementalCats();
			String[] newCats = new String[supplementalCats.length];
			for (int i = 0; i < supplementalCats.length; i++)
				newCats[i] = supplementalCats[i].replace('/', '|');
			return newCats;
		}

		@Override
		public Object obtainPlainFieldValue(Object obj) {
			return ((Asset) obj).getSupplementalCats();
		}

		@Override
		public void setFieldValue(Object target, Object newValue) {
			((Asset) target).setSupplementalCats(
					(String[]) mergeVectoredValue(target, normalizeHierarchy((String[]) newValue)));
		}

		/**
		 * @return unique id
		 */
		@Override
		public String getId() {
			return "lrHierarchicalSubjects"; //$NON-NLS-1$
		}

	};

	public static final QueryField IPTC_URGENCY = new QueryField(IPTC_CLASSIFICATION, "urgency", "Urgency", //$NON-NLS-1$ //$NON-NLS-2$
			NS_PHOTOSHOP, "Urgency", Messages.QueryField_Urgency, ACTION_QUERY, //$NON-NLS-1$
			PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE | REPORT, CATEGORY_IPTC, T_INTEGER, 1, T_NONE,
			new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 },
			new String[] { Format.MISSINGENTRYSTRING, Messages.QueryField_Very_high, Messages.QueryField_High_urgency,
					Messages.QueryField_NormalPlus, Messages.QueryField_Normal_urgency, Messages.QueryField_NormalMinus,
					Messages.QueryField_Low_urgency, Messages.QueryField_Lower_urgency, Messages.QueryField_Very_low },
			null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected int getInt(Asset asset) {
			return asset.getUrgency();
		}
	};

	// IPTC rights fields
	public static final QueryField IPTC_RIGHTS = new QueryField(IPTC_ALL, "iptc_rights", null, null, null, //$NON-NLS-1$
			Messages.QueryField_Rights, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_IPTC, T_NONE, 1, T_NONE, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);

	public static final QueryField IPTC_BYLINETITLE = new QueryField(IPTC_RIGHTS, "authorsPosition", //$NON-NLS-1$
			"By-lineTitle", //$NON-NLS-1$
			NS_PHOTOSHOP, "AuthorsPosition", //$NON-NLS-1$
			Messages.QueryField_Creators_job_title, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_CONTAINS,
			CATEGORY_IPTC, T_STRING, 1, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getAuthorsPosition();
		}
	};
	public static final QueryField IPTC_BYLINE = new QueryField(IPTC_RIGHTS, "artist", //$NON-NLS-1$
			"By-line", //$NON-NLS-1$
			NS_PHOTOSHOP, "Artist", //$NON-NLS-1$
			Messages.QueryField_Artis, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE, CATEGORY_IPTC,
			T_STRING, CARD_MODIFIABLEBAG, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getArtist();
		}
	};
	public static final QueryField IPTC_CREDITS = new QueryField(IPTC_RIGHTS, "credit", //$NON-NLS-1$
			"Credit", //$NON-NLS-1$
			NS_PHOTOSHOP, "Credit", //$NON-NLS-1$
			Messages.QueryField_Credits, ACTION_QUERY, PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY | TEXT | AUTO_CONTAINS,
			CATEGORY_IPTC, T_STRING, 1, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getCredit();
		}
	};
	public static final QueryField IPTC_SOURCE = new QueryField(IPTC_RIGHTS, "source", //$NON-NLS-1$
			"Source", //$NON-NLS-1$
			NS_PHOTOSHOP, "Source", //$NON-NLS-1$
			Messages.QueryField_Source, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_CONTAINS, CATEGORY_IPTC,
			T_STRING, 1, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getSource();
		}
	};
	public static final QueryField IPTC_CONTACT = new QueryField(IPTC_RIGHTS, "iptc_creator_contact", //$NON-NLS-1$
			"Contact", //$NON-NLS-1$
			NS_IPTC4XMPCORE, "CreatorContactInfo", //$NON-NLS-1$
			Messages.QueryField_Creators_Contact, ACTION_QUERY,
			PHOTO | EDIT_ALWAYS | ESSENTIAL | QUERY | TEXT | AUTO_CONTAINS, CATEGORY_IPTC, T_CONTACT, 1, 255, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING) {
		@Override
		public Object getStruct(Asset asset) {
			for (CreatorsContactImpl creatorsContact : CoreActivator.getDefault().getDbManager()
					.obtainStructForAsset(CreatorsContactImpl.class, asset.getStringId(), true))
				return creatorsContact.getContact();
			return null;
		}

		@Override
		public Object getStructRelationIds(Asset asset) {
			return asset.getCreatorsContact_parent();
		}

	};
	public static final QueryField IPTC_USAGE = new QueryField(IPTC_RIGHTS, "usageTerms", //$NON-NLS-1$
			null, NS_XMPRIGHTS, "UsageTerms", //$NON-NLS-1$
			Messages.QueryField_Usage_terms, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_CONTAINS,
			CATEGORY_IPTC, T_STRING, 1, 2048, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getUsageTerms();
		}
	};
	public static final QueryField IPTC_OWNER = new QueryField(IPTC_RIGHTS, "owner", //$NON-NLS-1$
			Messages.QueryField_owner_id, NS_XMPRIGHTS, "Owner", //$NON-NLS-1$
			Messages.QueryField_Owner, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_IPTC, T_STRING, CARD_MODIFIABLEBAG, 64, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getOwner();
		}
	};

	// IPTC origin fields
	public static final QueryField IPTC_ORIGIN = new QueryField(IPTC_ALL, "iptc_origin", null, null, null, //$NON-NLS-1$
			Messages.QueryField_Origin, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_IPTC, T_NONE, 1, T_NONE, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);

	public static final QueryField IPTC_DATECREATED = new QueryField(IPTC_ORIGIN, "dateCreated", //$NON-NLS-1$
			"DateCreated", //$NON-NLS-1$
			NS_PHOTOSHOP, "DateCreated", //$NON-NLS-1$
			Messages.QueryField_Date_created, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY, CATEGORY_IPTC, T_DATE, 1, 16,
			Format.dayFormatter, -900000f, Float.NaN, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getDateCreated();
		}
	};
	public static final QueryField IPTC_LOCATIONSHOWN = new QueryField(IPTC_ORIGIN, "iptc_location_shown", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "LocationShown", //$NON-NLS-1$
			Messages.QueryField_Location_shown, ACTION_NONE, PHOTO | EDIT_ALWAYS | QUERY | TEXT, CATEGORY_IPTC,
			T_LOCATION, CARD_LIST, 128, 0f, Float.NaN, ISpellCheckingService.NOSPELLING) {
		@Override
		public Object getStruct(Asset asset) {
			List<LocationShownImpl> set = CoreActivator.getDefault().getDbManager()
					.obtainStructForAsset(LocationShownImpl.class, asset.getStringId(), false);
			int i = 0;
			String[] group = new String[set.size()];
			for (LocationShownImpl rel : set)
				group[i++] = rel.getLocation();
			return group;
		}

		@Override
		public Object getStructRelationIds(Asset asset) {
			List<String> rels = asset.getLocationShown_parent();
			return rels.toArray(new String[rels.size()]);
		}

	};
	// Legacy IPTC
	public static final QueryField IPTC_CITY = new QueryField(null, "city", "City", NS_PHOTOSHOP, "City", null, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_IPTC, T_LOCATION, 1, 32, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField IPTC_SUBLOCATION = new QueryField(null, "sublocation", "Sub-location", //$NON-NLS-1$ //$NON-NLS-2$
			NS_IPTC4XMPCORE, "Location", null, ACTION_NONE, //$NON-NLS-1$
			PHOTO | EDIT_NEVER, CATEGORY_IPTC, T_LOCATION, 1, 128, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField IPTC_STATE = new QueryField(null, "provinceOrState", "Province-State", NS_PHOTOSHOP, //$NON-NLS-1$ //$NON-NLS-2$
			"State", //$NON-NLS-1$
			null, ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_IPTC, T_LOCATION, 1, 32, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField IPTC_COUNTRY = new QueryField(null, "countryName", "Country-PrimaryLocationName", //$NON-NLS-1$ //$NON-NLS-2$
			NS_PHOTOSHOP, "Country", null, //$NON-NLS-1$
			ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_IPTC, T_LOCATION, 1, 64, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField IPTC_COUNTRYCODE = new QueryField(null, "countryISOCode", //$NON-NLS-1$
			"Country-PrimaryLocationCode", NS_IPTC4XMPCORE, "CountryCode", null, //$NON-NLS-1$ //$NON-NLS-2$
			ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_IPTC, T_LOCATION, 1, 3, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField IPTC_WORLDREGION = new QueryField(null, "worldRegion", "LocationCreatedWorldRegion", //$NON-NLS-1$ //$NON-NLS-2$
			NS_IPTC4XMPEXT, "LocationCreatedWorldRegion", null, //$NON-NLS-1$
			ACTION_NONE, PHOTO | EDIT_NEVER, CATEGORY_IPTC, T_LOCATION, 1, 32, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);

	// IPTC Codes
	public static final QueryField IPTC_SCENECODE = new QueryField(IPTC_ORIGIN, "sceneCode", //$NON-NLS-1$
			null, NS_IPTC4XMPCORE, "Scene", //$NON-NLS-1$
			Messages.QueryField_Scene_code, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_IPTC, T_STRING, CARD_BAG, 6, SCENECODES, null, null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getSceneCode();
		}
	};
	public static final QueryField IPTC_SUBJECTCODE = new QueryField(IPTC_ORIGIN, "subjectCode", //$NON-NLS-1$
			"SubjectReference", //$NON-NLS-1$
			NS_IPTC4XMPCORE, "SubjectCode", //$NON-NLS-1$
			Messages.QueryField_Subject_code, ACTION_QUERY, PHOTO | EDIT_ALWAYS | QUERY | TEXT | AUTO_DISCRETE | REPORT,
			CATEGORY_IPTC, T_STRING, CARD_BAG, 8, SUBJECTCODES, null, null, 0f, ISpellCheckingService.NOSPELLING) {

		@Override
		protected Object getValue(Asset asset) {
			return asset.getSubjectCode();
		}
	};

	private static final QueryField[] NOCHILDREN = new QueryField[0];

	// Location
	public static final QueryField LOCATION_CITY = new QueryField(LOCATION_TYPE, "city", null, NS_IPTC4XMPEXT, "City", //$NON-NLS-1$ //$NON-NLS-2$
			Messages.QueryField_City, ACTION_QUERY, EDIT_ALWAYS | TEXT | REPORT, CATEGORY_FOREIGN, T_STRING, 1, 32, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField LOCATION_COUNTRYCODE = new QueryField(LOCATION_TYPE, "countryISOCode", null, //$NON-NLS-1$
			NS_IPTC4XMPEXT, "CountryCode", Messages.QueryField_CountryISOCode, ACTION_QUERY, //$NON-NLS-1$
			EDIT_ALWAYS | TEXT | REPORT, CATEGORY_FOREIGN, T_STRING, 1, 3, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField LOCATION_COUNTRYNAME = new QueryField(LOCATION_TYPE, "countryName", null, //$NON-NLS-1$
			NS_IPTC4XMPEXT, "CountryName", Messages.QueryField_Country_name, ACTION_QUERY, //$NON-NLS-1$
			EDIT_ALWAYS | TEXT | REPORT, CATEGORY_FOREIGN, T_STRING, 1, 32, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField LOCATION_DETAILS = new QueryField(LOCATION_TYPE, "details", null, //$NON-NLS-1$
			NS_IPTC4XMPEXT, "LocationDetails", Messages.QueryField_Location_details, //$NON-NLS-1$
			ACTION_QUERY, EDIT_ALWAYS | TEXT, CATEGORY_FOREIGN, T_STRING, 1, 255, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField LOCATION_STATE = new QueryField(LOCATION_TYPE, "provinceOrState", null, //$NON-NLS-1$
			NS_IPTC4XMPEXT, "ProvinceState", Messages.QueryField_ProvinceState, ACTION_QUERY, //$NON-NLS-1$
			EDIT_ALWAYS | TEXT | REPORT, CATEGORY_FOREIGN, T_STRING, 1, 32, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField LOCATION_SUBLOCATION = new QueryField(LOCATION_TYPE, "sublocation", null, //$NON-NLS-1$
			NS_IPTC4XMPEXT, "Sublocation", Messages.QueryField_Sublocation_loc, ACTION_QUERY, //$NON-NLS-1$
			EDIT_ALWAYS | TEXT, CATEGORY_FOREIGN, T_STRING, 1, 255, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField LOCATION_WORLDREGION = new QueryField(LOCATION_TYPE, "worldRegion", null, //$NON-NLS-1$
			NS_IPTC4XMPEXT, "WorldRegion", Messages.QueryField_World_region, ACTION_QUERY, //$NON-NLS-1$
			EDIT_ALWAYS | TEXT | REPORT, CATEGORY_FOREIGN, T_STRING, 1, 32, LocationConstants.continentNames,
			LocationConstants.continentNames, null, 0f, ISpellCheckingService.NOSPELLING);
	public static final QueryField LOCATION_WORLDREGIONCODE = new QueryField(LOCATION_TYPE, "worldRegionCode", null, //$NON-NLS-1$
			null, null, Messages.QueryField_continent_code, ACTION_QUERY, EDIT_NEVER | TEXT | REPORT, CATEGORY_FOREIGN,
			T_STRING, 1, 2, CONTINENTCODES, CONTINENTCODES, null, 0f, ISpellCheckingService.NOSPELLING);
	public static final QueryField LOCATION_LONGITUDE = new QueryField(LOCATION_TYPE, "longitude", null, NS_ZORA, //$NON-NLS-1$
			"LocationLongitude", Messages.QueryField_GPS_longitude_loc, //$NON-NLS-1$
			ACTION_MAP, EDIT_ALWAYS, CATEGORY_FOREIGN, T_FLOATB, 1, 5, Format.longitudeFormatter, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField LOCATION_LATITUDE = new QueryField(LOCATION_TYPE, "latitude", null, NS_ZORA, //$NON-NLS-1$
			"LocationLatitude", //$NON-NLS-1$
			Messages.QueryField_GPS_latitude_loc, ACTION_MAP, EDIT_ALWAYS, CATEGORY_FOREIGN, T_FLOATB, 1, 5,
			Format.latitudeFormatter, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField LOCATION_ALTITUDE = new QueryField(LOCATION_TYPE, "altitude", null, NS_ZORA, //$NON-NLS-1$
			"LocationAltitude", Messages.QueryField_GPS_altitude_loc, //$NON-NLS-1$
			ACTION_NONE, EDIT_ALWAYS, CATEGORY_FOREIGN, T_FLOATB, 1, 2, Format.altitudeFormatter, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);

	// Artwork
	public static final QueryField ARTWORKOROBJECT_TITLE = new QueryField(ARTWORKOROBJECT_TYPE, "title", null, //$NON-NLS-1$
			NS_IPTC4XMPEXT, "AOTitle", Messages.QueryField_Title_art, ACTION_QUERY, EDIT_ALWAYS | TEXT | REPORT, //$NON-NLS-1$
			CATEGORY_FOREIGN, T_STRING, 1, 64, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField ARTWORKOROBJECT_COPYRIGHT = new QueryField(ARTWORKOROBJECT_TYPE, "copyrightNotice", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "AOCopyrightNotice", Messages.QueryField_Copyright_notice, //$NON-NLS-1$
			ACTION_QUERY, EDIT_ALWAYS | TEXT, CATEGORY_FOREIGN, T_STRING, 1, 512, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField ARTWORKOROBJECT_CREATOR = new QueryField(ARTWORKOROBJECT_TYPE, "creator", null, //$NON-NLS-1$
			NS_IPTC4XMPEXT, "AOCreator", Messages.QueryField_Creator, ACTION_QUERY, EDIT_ALWAYS | TEXT | REPORT, //$NON-NLS-1$
			CATEGORY_FOREIGN, T_STRING, CARD_LIST, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField ARTWORKOROBJECT_DATECREATED = new QueryField(ARTWORKOROBJECT_TYPE, "dateCreated", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "AODateCreated", Messages.QueryField_Date_art_created, ACTION_QUERY, //$NON-NLS-1$
			EDIT_ALWAYS, CATEGORY_FOREIGN, T_DATE, 1, 16, Format.dayFormatter, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField ARTWORKOROBJECT_SOURCE = new QueryField(ARTWORKOROBJECT_TYPE, "source", null, //$NON-NLS-1$
			NS_IPTC4XMPEXT, "AOSource", Messages.QueryField_Source_art, ACTION_QUERY, EDIT_ALWAYS | TEXT, //$NON-NLS-1$
			CATEGORY_FOREIGN, T_STRING, 1, 512, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField ARTWORKOROBJECT_INVENTORYNUMBER = new QueryField(ARTWORKOROBJECT_TYPE,
			"sourceInventoryNumber", //$NON-NLS-1$
			null, NS_IPTC4XMPEXT, "AOSourceInvNo", Messages.QueryField_Source_Inventory_Number, //$NON-NLS-1$
			ACTION_QUERY, EDIT_ALWAYS | TEXT | REPORT, CATEGORY_FOREIGN, T_STRING, 1, 64, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);

	// Contact
	public static final QueryField CONTACT_ADDRESS = new QueryField(CONTACT_TYPE, "address", null, //$NON-NLS-1$
			NS_IPTC4XMPCORE, "CiAdrExtadr", Messages.QueryField_Address_contact, ACTION_QUERY, EDIT_ALWAYS | TEXT, //$NON-NLS-1$
			CATEGORY_FOREIGN, T_STRING, CARD_LIST, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField CONTACT_CITY = new QueryField(CONTACT_TYPE, "city", null, NS_IPTC4XMPCORE, //$NON-NLS-1$
			"CiAdrCity", Messages.QueryField_City_contract, //$NON-NLS-1$
			ACTION_QUERY, EDIT_ALWAYS | TEXT, CATEGORY_FOREIGN, T_STRING, 1, 32, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField CONTACT_COUNTRY = new QueryField(CONTACT_TYPE, "country", null, NS_IPTC4XMPCORE, //$NON-NLS-1$
			"CiAdrCtry", Messages.QueryField_Country_contact, ACTION_QUERY, EDIT_ALWAYS | TEXT, CATEGORY_FOREIGN, //$NON-NLS-1$
			T_STRING, 1, 32, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField CONTACT_EMAIL = new QueryField(CONTACT_TYPE, "email", null, NS_IPTC4XMPCORE, //$NON-NLS-1$
			"CiEmailWork", //$NON-NLS-1$
			Messages.QueryField_Email_contact, ACTION_EMAIL, EDIT_ALWAYS | TEXT, CATEGORY_FOREIGN, T_STRING, CARD_LIST,
			64, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField CONTACT_PHONE = new QueryField(CONTACT_TYPE, "phone", null, NS_IPTC4XMPCORE, //$NON-NLS-1$
			"CiTelWork", Messages.QueryField_Phone, //$NON-NLS-1$
			ACTION_QUERY, EDIT_ALWAYS | TEXT | REPORT, CATEGORY_FOREIGN, T_STRING, CARD_LIST, 32, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField CONTACT_POSTALCODE = new QueryField(CONTACT_TYPE, "postalCode", null, //$NON-NLS-1$
			NS_IPTC4XMPCORE, "CiAdrPcode", Messages.QueryField_Postal_code_contact, ACTION_QUERY, //$NON-NLS-1$
			EDIT_ALWAYS | TEXT, CATEGORY_FOREIGN, T_STRING, 1, 10, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField CONTACT_STATE = new QueryField(CONTACT_TYPE, "state", null, NS_IPTC4XMPCORE, //$NON-NLS-1$
			"CiAdrRegion", //$NON-NLS-1$
			Messages.QueryField_State_contact, ACTION_QUERY, EDIT_ALWAYS | TEXT, CATEGORY_FOREIGN, T_STRING, 1, 16, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField CONTACT_WEBURL = new QueryField(CONTACT_TYPE, "webUrl", null, NS_IPTC4XMPCORE, //$NON-NLS-1$
			"CiUrlWork", Messages.QueryField_Web_URL_contact, ACTION_WWW, EDIT_ALWAYS | TEXT, CATEGORY_FOREIGN, //$NON-NLS-1$
			T_STRING, CARD_LIST, 255, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);

	// Region Microsoft
	public static final QueryField REGION_MP = new QueryField(null, null, "RegionInfoMP", //$NON-NLS-1$
			null, null, null, ACTION_NONE, EDIT_NEVER, CATEGORY_LOCAL, T_STRUCT, 1, 16, null, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField REGION_MP_LIST = new QueryField(null, null, "Regions", //$NON-NLS-1$
			null, null, null, ACTION_NONE, EDIT_NEVER, CATEGORY_LOCAL, T_STRUCT, 1, 16, null, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField REGION_RECTANGLE = new QueryField(
			// REGION_TYPE, Constants.OID,
			REGION_TYPE, "$rectangle", //$NON-NLS-1$
			"Rectangle", //$NON-NLS-1$
			NS_MSPHOTO_MPREG, "Rectangle", //$NON-NLS-1$
			Messages.QueryField_rectangle, ACTION_NONE, EDIT_NEVER, CATEGORY_LOCAL, T_STRING, 1, 16, null, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING) {
		@Override
		public void setFieldValue(Object obj, Object newValue) {
			if (obj instanceof MWGRegion && newValue instanceof String)
				try {
					((MWGRegion) obj).setArea((String) newValue);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
		}

		@Override
		public Object obtainPlainFieldValue(Object obj) {
			return (obj instanceof MWGRegion) ? ((MWGRegion) obj).getRect64() : null;
		}
	};
	public static final QueryField REGION_PERSONEMAILDIGEST = new QueryField(REGION_TYPE, "personEmailDigest", //$NON-NLS-1$
			"PersonEmailDigest", //$NON-NLS-1$
			NS_MSPHOTO_MPREG, "PersonEmailDigest", Messages.QueryField_live_email, ACTION_NONE, EDIT_NEVER, //$NON-NLS-1$
			CATEGORY_LOCAL, T_STRING, 1, 64, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField REGION_PERSONLIVECIDE = new QueryField(REGION_TYPE, "personLiveCID", //$NON-NLS-1$
			"PersonLiveIdCID", //$NON-NLS-1$
			NS_MSPHOTO_MPREG, "PersonLiveCID", Messages.QueryField_live_cid, ACTION_NONE, EDIT_NEVER, //$NON-NLS-1$
			CATEGORY_LOCAL, T_LONG, 1, 16, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField REGION_ALBUM = new QueryField(REGION_TYPE, "name", "PersonDisplayName", //$NON-NLS-1$ //$NON-NLS-2$
			NS_MSPHOTO_MPREG, "PersonDisplayName", Messages.QueryField_person_display_name, ACTION_NONE, EDIT_NEVER, //$NON-NLS-1$
			CATEGORY_FOREIGN, T_STRING, 1, 255, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);

	// Region MWG
	public static final QueryField MWG_REGION_INFO = new QueryField(null, null, "RegionInfo", //$NON-NLS-1$
			null, null, null, ACTION_NONE, EDIT_NEVER, CATEGORY_LOCAL, T_STRUCT, 1, 16, null, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING);
	public static final QueryField MWG_APPLIEDTODIM = new QueryField(null, null, "AppliedToDimensions", //$NON-NLS-1$
			NS_MWG_REGIONS, "AppliedToDimensions", null, ACTION_NONE, EDIT_NEVER, CATEGORY_LOCAL, //$NON-NLS-1$
			T_STRUCT, 1, 16, null, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField MWG_REGION_LIST = new QueryField(null, null, "RegionList", //$NON-NLS-1$
			NS_MWG_REGIONS, "RegionList", null, ACTION_NONE, EDIT_NEVER, CATEGORY_LOCAL, //$NON-NLS-1$
			T_STRUCT, 1, 16, null, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField MWG_REGION_RECTANGLE = new QueryField(MWG_REGION_TYPE,
			// Constants.OID,
			"$area", //$NON-NLS-1$
			"Area", //$NON-NLS-1$
			NS_MWG_REGIONS, "Area", //$NON-NLS-1$
			Messages.QueryField_rectangle, ACTION_NONE, EDIT_NEVER, CATEGORY_LOCAL, T_REGION, 1, 16, null, 0f,
			Float.NaN, ISpellCheckingService.NOSPELLING) {
		@Override
		public void setFieldValue(Object obj, Object newValue) {
			if (obj instanceof MWGRegion)
				try {
					if (newValue instanceof String)
						((MWGRegion) obj).setArea((String) newValue);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
		}

		@Override
		public Object obtainPlainFieldValue(Object obj) {
			return (obj instanceof MWGRegion) ? obj : null;
		}
	};
	public static final QueryField MWG_AREA_X = new QueryField(MWG_REGION_RECTANGLE, "x", //$NON-NLS-1$
			null, NS_MWG_AREA, "Regions/RegionList/Area/x", //$NON-NLS-1$
			null, ACTION_NONE, EDIT_NEVER, CATEGORY_LOCAL, T_FLOAT, 1, 16, null, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {
		@Override
		public void setFieldValue(Object obj, Object newValue) {
			if (obj instanceof MWGRegion)
				((MWGRegion) obj).x = (Double) newValue;
		}
	};
	public static final QueryField MWG_AREA_Y = new QueryField(MWG_REGION_RECTANGLE, "y", //$NON-NLS-1$
			null, NS_MWG_AREA, "Regions/RegionList/Area/y", //$NON-NLS-1$
			null, ACTION_NONE, EDIT_NEVER, CATEGORY_LOCAL, T_FLOAT, 1, 16, null, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {
		@Override
		public void setFieldValue(Object obj, Object newValue) {
			if (obj instanceof MWGRegion)
				((MWGRegion) obj).y = (Double) newValue;
		}
	};
	public static final QueryField MWG_AREA_W = new QueryField(MWG_REGION_RECTANGLE, "w", //$NON-NLS-1$
			null, NS_MWG_AREA, "Regions/RegionList/Area/w", //$NON-NLS-1$
			null, ACTION_NONE, EDIT_NEVER, CATEGORY_LOCAL, T_FLOAT, 1, 16, null, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {
		@Override
		public void setFieldValue(Object obj, Object newValue) {
			if (obj instanceof MWGRegion)
				((MWGRegion) obj).width = (Double) newValue;
		}
	};
	public static final QueryField MWG_AREA_H = new QueryField(MWG_REGION_RECTANGLE, "h", //$NON-NLS-1$
			null, NS_MWG_AREA, "Regions/RegionList/Area/h", //$NON-NLS-1$
			null, ACTION_NONE, EDIT_NEVER, CATEGORY_LOCAL, T_FLOAT, 1, 16, null, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {
		@Override
		public void setFieldValue(Object obj, Object newValue) {
			if (obj instanceof MWGRegion)
				((MWGRegion) obj).height = (Double) newValue;
		}
	};

	public static final QueryField MWG_REGION_ALBUM = new QueryField(MWG_REGION_TYPE, "name", "Name", //$NON-NLS-1$ //$NON-NLS-2$
			NS_MWG_REGIONS, "Name", Messages.QueryField_person_display_name, ACTION_NONE, EDIT_NEVER, //$NON-NLS-1$
			CATEGORY_FOREIGN, T_STRING, 1, 255, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField MWG_REGION_VALUE = new QueryField(MWG_REGION_TYPE, "value", "BarCodeValue", //$NON-NLS-1$ //$NON-NLS-2$
			NS_MWG_REGIONS, "BarCodeValue", Messages.QueryField_bar_code_value, ACTION_NONE, EDIT_NEVER, //$NON-NLS-1$
			CATEGORY_FOREIGN, T_STRING, 1, 255, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField MWG_REGION_DESCRIPTION = new QueryField(MWG_REGION_TYPE, "description", //$NON-NLS-1$
			"Description", //$NON-NLS-1$
			NS_MWG_REGIONS, "Description", Messages.QueryField_region_description, ACTION_NONE, EDIT_NEVER, //$NON-NLS-1$
			CATEGORY_FOREIGN, T_STRING, 1, 255, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField MWG_REGION_KIND = new QueryField(MWG_REGION_TYPE, "type", //$NON-NLS-1$
			"Type", //$NON-NLS-1$
			NS_MWG_REGIONS, "Type", Messages.QueryField_region_type, ACTION_NONE, EDIT_NEVER, //$NON-NLS-1$
			CATEGORY_LOCAL, T_STRING, 1, 64, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);

	// Internal fields
	public static final QueryField LOCATIONCREATED_ID = new QueryField(null, "", null, //$NON-NLS-1$
			IPTC_LOCATIONCREATED.getXmpNs(), IPTC_LOCATIONCREATED.getPath() + "/ZoRaId", null, ACTION_QUERY, //$NON-NLS-1$
			EDIT_NEVER, CATEGORY_NONE, T_STRING, 1, 255, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField LOCATIONSHOWN_ID = new QueryField(null, "", null, //$NON-NLS-1$
			IPTC_LOCATIONSHOWN.getXmpNs(), IPTC_LOCATIONSHOWN.getPath() + "/ZoRaId", null, ACTION_QUERY, //$NON-NLS-1$
			EDIT_NEVER, CATEGORY_NONE, T_STRING, 1, 255, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField CONTACT_ID = new QueryField(null, "", null, //$NON-NLS-1$
			IPTC_CONTACT.getXmpNs(), IPTC_CONTACT.getPath() + "/ZoRaId", null, ACTION_QUERY, //$NON-NLS-1$
			EDIT_NEVER, CATEGORY_NONE, T_STRING, 1, 255, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);
	public static final QueryField ARTWORK_ID = new QueryField(null, "", null, //$NON-NLS-1$
			IPTC_ARTWORK.getXmpNs(), IPTC_ARTWORK.getPath() + "/ZoRaId", null, ACTION_QUERY, //$NON-NLS-1$
			EDIT_NEVER, CATEGORY_NONE, T_STRING, 1, 255, 0f, Float.NaN, ISpellCheckingService.NOSPELLING);

	public static final QueryField SCORE = new QueryField(null, "score", //$NON-NLS-1$
			null, null, null, null, ACTION_NONE, EDIT_NEVER, CATEGORY_NONE, T_FLOAT, 1, 2, 0f, Float.NaN,
			ISpellCheckingService.NOSPELLING) {

		@Override
		protected double getDouble(Asset asset) {
			return asset.getScore();
		}
	};

	private static final Object EMPTYBOOL = new boolean[0];
	private static final Object EMPTYINT = new int[0];
	private static final Object EMPTYDOUBLE = new double[0];
	private static final Object EMPTYSTRING = new String[0];
	private static final Object EMPTYOBJECT = new Object[0];
	private static final Object ZERO = Integer.valueOf(0);
	private static final Object LONGZERO = Long.valueOf(0);

	/**
	 * Finds the QueryField instance belonging to an object field name
	 *
	 * @param id
	 *            - field ID
	 * @return - QueryField instance
	 */
	public static QueryField findQueryField(String id) {
		return id == null ? null : fieldMap.get(id);
	}

	// protected static void updateDirDist(Asset asset) {
	// double lat = asset.getGPSLatitude();
	// double lon = asset.getGPSLongitude();
	// if (Double.isNaN(lat) || Double.isNaN(lon)) {
	// asset.setGPSImgDirection(Double.NaN);
	// asset.setGPSDestDistance(Double.NaN);
	// } else {
	// double destLat = asset.getGPSDestLatitude();
	// double destLon = asset.getGPSDestLongitude();
	// if (Double.isNaN(destLat) || Double.isNaN(destLon)) {
	// asset.setGPSDestDistance(Double.NaN);
	// } else {
	// asset.setGPSDestDistance(Core.distance(lat, lon, destLat, destLon, 'k'));
	// asset.setGPSImgDirection(Core.bearing(lat, lon, destLat, destLon));
	// asset.setGPSImgDirectionRef("T"); //$NON-NLS-1$
	// }
	// }
	// }

	/**
	 * Finds the QueryField instance belonging to an object sub field name
	 *
	 * @param id
	 *            - main field ID
	 * @param subId
	 *            - subfield ID
	 * @return - QueryField instance
	 */
	public static QueryField findQuerySubField(String id, String subId) {
		if (id == null)
			return null;
		if (subId == null)
			return findQueryField(id);
		return findQueryField(new StringBuilder().append(id).append(':').append(subId).toString());
	}

	/**
	 * Finds the QueryField instance belonging to an object sub field name
	 *
	 * @param path
	 *            - main field ID & subfield ID
	 * @return - Array of parent/child QueryField instances or null
	 */
	public static QueryField[] findQuerySubField(String path) {
		path = path.replace('&', ':');
		int p = path.indexOf(':');
		QueryField subfield = findQueryField(path);
		if (subfield == null) {
			if (p <= 0)
				return null;
			QueryField qfield = findQueryField(path.substring(p + 1));
			return qfield == null ? null : new QueryField[] { qfield, qfield };
		}
		if (p <= 0)
			return new QueryField[] { subfield, subfield };
		QueryField qfield = findQueryField(path.substring(0, p));
		return qfield == null ? new QueryField[] { subfield, subfield } : new QueryField[] { qfield, subfield };
	}

	protected static Object normalizeHierarchy(String[] cats) {
		String[] newCats = new String[cats.length];
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cats.length; i++) {
			StringTokenizer st = new StringTokenizer(cats[i], "/|.\\>"); //$NON-NLS-1$
			sb.setLength(0);
			while (st.hasMoreTokens()) {
				if (sb.length() > 0)
					sb.append('/');
				sb.append(st.nextToken());
			}
			newCats[i] = sb.toString();
		}
		return newCats;
	}

	/**
	 * Returns a collection of all field IDs
	 *
	 * @return field names
	 */
	public static Collection<String> getQueryFieldKeys() {
		return fieldMap.keySet();
	}

	/**
	 * Returns a collection of all QueryField constants
	 *
	 * @return QueryField constants
	 */
	public static Collection<QueryField> getQueryFields() {
		return fieldMap.values();
	}

	/**
	 * Returns the structure parent associated with the field type
	 *
	 * @param type
	 *            - field type
	 * @return - structure parent
	 */
	public static QueryField getStructParent(int type) {
		switch (type) {
		case QueryField.T_LOCATION:
			return QueryField.LOCATION_TYPE;
		case QueryField.T_CONTACT:
			return QueryField.CONTACT_TYPE;
		case QueryField.T_OBJECT:
			return QueryField.ARTWORKOROBJECT_TYPE;
		}
		return null;
	}

	/**
	 * Tests if a query field is a descendand of another query field
	 *
	 * @param offspring
	 *            - candidate
	 * @param ancestor
	 *            - possible ancestor
	 * @return
	 */
	public static boolean belongsTo(QueryField offspring, QueryField ancestor) {
		while (offspring != null) {
			if (offspring == ancestor)
				return true;
			offspring = offspring.getParent();
		}
		return false;
	}

	private String key;
	private String label;
	private String unit;
	private Category category;
	private int type;
	private Object enumeration;
	private String[] enumLabels;
	private IFormatter formatter;
	private int flags;
	private QueryField parent;
	private int maxlength;
	private Object exifToolKey; // String or Array of Strings
	private Namespace xmpNs;
	private String path;
	private List<QueryField> children;
	private int card;
	private int action;
	private final float tolerance;
	private final int spellingOptions;
	private float maxValue = Float.NaN;
	private boolean isSubfield;

	/**
	 * @param parent
	 *            - field category
	 * @param key
	 *            - object field name (or pseudo name starting with '$')
	 * @param exifToolKey
	 *            - key used by EXIFTOOL (String or array of strings)
	 * @param xmpNs
	 *            - XMP namespace
	 * @param path
	 *            - XMP path
	 * @param label
	 *            - label shown as shown in views
	 * @param action
	 *            - action belonging to this field
	 * @param flags
	 *            - edit policy (EDIT_ALWAYS, EDIT_DIGITAL, EDIT_ANALOG, EDIT_NEVER,
	 *            EDIT_HIDDEN) - other flags (QUERY, ESSENTIAL, HOVER, TEXT, VIDEO)
	 * @param category
	 *            - category to which this field belongs
	 * @param type
	 *            - data type of the field
	 * @param card
	 *            - cardinality (-1 for multiple unlimited)
	 * @param maxlength
	 *            - maximum length for strings, maximum fraction digits for float
	 * @param tolerance
	 *            - default tolerance for queries (negative: absolute value,
	 *            positive: percent)
	 * @param maxValue
	 *            - largest positive and negative number possible
	 * @param spellingOptions
	 *            - determines how the field is spell checked
	 */
	public QueryField(QueryField parent, String key, Object exifToolKey, Namespace xmpNs, String path, String label,
			int action, int flags, Category category, int type, int card, int maxlength, float tolerance,
			float maxValue, int spellingOptions) {
		this(parent, key, exifToolKey, xmpNs, path, label, action, flags, category, type, card, maxlength, null, null,
				null, tolerance, spellingOptions);
		this.maxValue = maxValue;
	}

	/**
	 * @param parent
	 *            - field category
	 * @param key
	 *            - object field name (or pseudo name starting with '$')
	 * @param exifToolKey
	 *            - key used by EXIFTOOL (String or array of strings)
	 * @param xmpNs
	 *            - XMP namespace
	 * @param path
	 *            - XMP path
	 * @param label
	 *            - label shown as shown in views
	 * @param action
	 *            - action belonging to this field
	 * @param flags
	 *            - edit policy (EDIT_ALWAYS, EDIT_DIGITAL, EDIT_ANALOG, EDIT_NEVER,
	 *            EDIT_HIDDEN) - other flags (QUERY, ESSENTIAL, HOVER, TEXT, VIDEO,
	 *            PHOTO)
	 * @param category
	 *            - category to which this field belongs
	 * @param type
	 *            - data type of the field
	 * @param card
	 *            - cardinality (-1 for multiple unlimited)
	 * @param maxlength
	 *            - maximum length for strings, maximum fraction digits for float
	 * @param formatter
	 *            - formatter used for formatting and parsing the field value
	 * @param tolerance
	 *            - default tolerance for queries (negative: absolute value,
	 *            positive: percent)
	 * @param maxValue
	 *            - largest positive and negative number possible
	 * @param spellingOptions
	 *            - determines how the field is spell checked
	 */
	public QueryField(QueryField parent, String key, Object exifToolKey, Namespace xmpNs, String path, String label,
			int action, int flags, Category category, int type, int card, int maxlength, IFormatter formatter,
			float tolerance, float maxValue, int spellingOptions) {
		this(parent, key, exifToolKey, xmpNs, path, label, action, flags, category, type, card, maxlength, null, null,
				formatter, tolerance, spellingOptions);
		this.maxValue = maxValue;
	}

	/**
	 * @param parent
	 *            - field category
	 * @param key
	 *            - object field name (or pseudo name starting with '$')
	 * @param exifToolKey
	 *            - key used by EXIFTOOL (String or array of strings)
	 * @param xmpNs
	 *            - XMP namespace
	 * @param path
	 *            - XMP path
	 * @param label
	 *            - label shown as shown in views
	 * @param action
	 *            - action belonging to this field
	 * @param flags
	 *            - edit policy (EDIT_ALWAYS, EDIT_DIGITAL, EDIT_ANALOG, EDIT_NEVER,
	 *            EDIT_HIDDEN) - other flags (QUERY, ESSENTIAL, HOVER, TEXT, VIDEO)
	 * @param category
	 *            - category to which this field belongs
	 * @param type
	 *            - data type of the field
	 * @param card
	 *            - cardinality (-1 for multiple unlimited)
	 * @param maxlength
	 *            - maximum length for strings, maximum fraction digits for float
	 * @param enumeration
	 *            - enumeration values (field values)
	 * @param enumLabels
	 *            - enumeration labels as shown in views
	 * @param formatter
	 *            - formatter used for formatting and parsing the field value
	 * @param tolerance
	 *            - default tolerance for queries (negative: absolute value,
	 *            positive: percent)
	 * @param spellingOptions
	 *            - determines how the field is spell checked
	 */
	public QueryField(QueryField parent, String key, Object exifToolKey, Namespace xmpNs, String path, String label,
			int action, int flags, Category category, int type, int card, int maxLength, Object enumeration,
			String[] enumLabels, IFormatter formatter, float tolerance, int spellingOptions) {
		this.parent = parent;
		this.tolerance = tolerance;
		this.spellingOptions = spellingOptions;
		if (parent != null) {
			if (parent.children == null)
				parent.children = new ArrayList<QueryField>();
			parent.children.add(this);
			this.flags = flags;
			QueryField p = parent;
			while (p != null) {
				p.flags |= (flags & EDIT_ALWAYS);
				p = p.getParent();
			}
			if (parent.parent == ALL)
				subgroups.add(this);
		}
		this.key = key;
		this.exifToolKey = exifToolKey;
		this.path = path;
		this.xmpNs = xmpNs;
		this.action = action;
		this.category = category;
		this.type = type;
		this.card = card;
		this.enumeration = enumeration;
		this.enumLabels = enumLabels;
		this.formatter = formatter;
		this.maxlength = maxLength;
		if (label != null) {
			int p = label.lastIndexOf('(');
			int q = label.lastIndexOf(')');
			if (p > 0 && q > p) {
				this.unit = label.substring(p + 1, q).trim();
				this.label = label.substring(0, p).trim();
			} else
				this.label = label;
		}
		if (exifToolKey instanceof String) {
			if (xmpNs == NS_MWG_REGIONS)
				regionPropertyMap.put((String) exifToolKey, this);
			else
				exifToolMap.put((String) exifToolKey, this);
		} else if (exifToolKey instanceof String[])
			for (String exifKey : (String[]) exifToolKey)
				exifToolMap.put(exifKey, this);
		if (path != null && type != T_NONE) {
			if (xmpNs == NS_EXIF || xmpNs == NS_PHOTOSHOP || xmpNs == NS_IPTC4XMPCORE || xmpNs == NS_XMP)
				exifToolMap.put(path, this);
			else if ((xmpNs == NS_DC || xmpNs == NS_LIGHTROOM) && !path.isEmpty())
				exifToolMap.put(Character.toUpperCase(path.charAt(0)) + path.substring(1), this);
		}
		if (parent == LOCATION_TYPE) {
			fieldMap.put("iptc_location_created:" + getId(), this); //$NON-NLS-1$
			fieldMap.put("iptc_location_shown:" + getId(), this); //$NON-NLS-1$
			if (path != null) {
				pathMap.put(IPTC_LOCATIONCREATED.getXmpNs().uri + ':' + IPTC_LOCATIONCREATED.getPath() + '/' + path,
						this);
				pathMap.put(IPTC_LOCATIONSHOWN.getXmpNs().uri + ':' + IPTC_LOCATIONSHOWN.getPath() + '/' + path, this);
			}
			isSubfield = true;
		} else if (parent == ARTWORKOROBJECT_TYPE) {
			fieldMap.put("iptc_artwork_shown:" + getId(), this); //$NON-NLS-1$
			if (path != null)
				pathMap.put(IPTC_ARTWORK.getXmpNs().uri + ':' + IPTC_ARTWORK.getPath() + '/' + path, this);
			isSubfield = true;
		} else if (parent == CONTACT_TYPE) {
			fieldMap.put("iptc_creator_contact:" + getId(), this); //$NON-NLS-1$
			if (path != null)
				pathMap.put(IPTC_CONTACT.getXmpNs().uri + ':' + IPTC_CONTACT.getPath() + '/' + path, this);
			isSubfield = true;
		} else if (parent == REGION_TYPE) {
			if (path != null) {
				pathMap.put(FACESSHOWN.getXmpNs().uri + ":RegionInfo/Regions/" + path, this); //$NON-NLS-1$
				fieldMap.put("region:" + getId(), this); //$NON-NLS-1$
				isSubfield = true;
			}
		} else if (parent == MWG_REGION_TYPE) {
			if (path != null) {
				pathMap.put(MWG_FACESSHOWN.getXmpNs().uri + ":Regions/RegionList/" + path, this); //$NON-NLS-1$
				fieldMap.put("region:" + getId(), this); //$NON-NLS-1$
				isSubfield = true;
			}
		} else {
			fieldMap.put(getId(), this);
			if (path != null)
				pathMap.put(xmpNs + ":" + path, this); //$NON-NLS-1$
		}
	}

	/**
	 * @return object field name (pseudo names start with '$')
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return unique id
	 */
	public String getId() {
		String k = getKey();
		return k == null ? super.toString() : k;
	}

	/**
	 * Tests if the field has a label. If not it is only for internal use and not
	 * shown at the user interface.
	 *
	 * @return true if field has a label
	 */
	public boolean hasLabel() {
		return label != null && !label.isEmpty();
	}

	/**
	 * Tests if the field has a label and a key. If not it is only for internal use
	 * or is a category node
	 *
	 * @return true if field has a label and a key
	 */
	public boolean isUiField() {
		return hasLabel() && key != null;
	}

	/**
	 * Returns the field label. User field labels are replaced by their user defined
	 * strings.
	 *
	 * @return field label
	 */
	public String getLabel() {
		if (hasLabel() && label.charAt(0) == '{') {
			ICore core = Core.getCore();
			if (core != null) {
				Meta meta = core.getDbManager().getMeta(true);
				if (Constants.USERFIELD1.equals(label))
					return meta.getUserFieldLabel1();
				if (Constants.USERFIELD2.equals(label))
					return meta.getUserFieldLabel2();
			}
		}
		return label;
	}

	/**
	 * Returns the field label with the append unit of measurement
	 *
	 * @return label suffixed with unit
	 */
	public String getLabelWithUnit() {
		return hasLabel() ? ((unit == null) ? getLabel() : label + " (" + unit + ')') : null; //$NON-NLS-1$
	}

	/**
	 * Returns the field category
	 *
	 * @return category
	 */
	public Category getCategory() {
		return category;
	}

	/**
	 * Returns the field data type
	 *
	 * @return data type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns a bit pattern indicating the possible relation operators for the
	 * field
	 *
	 * @return bit combination of possible relation operators
	 */
	@SuppressWarnings("fallthrough")
	public int getRelations() {
		if (isStruct())
			return EQUALS;
		if (card != 1)
			return type == T_STRING ? STRINGARRAYRELATIONS : EQUALS | NOTEQUAL;
		switch (type) {
		case T_BOOLEAN:
			return EQUALS | NOTEQUAL;
		case T_POSITIVEFLOAT:
			if (this == QueryField.EXIF_GPSLOCATIONDISTANCE)
				return SIZERELATIONS;
		case T_POSITIVELONG:
		case T_POSITIVEINTEGER:
		case T_FLOAT:
		case T_CURRENCY:
		case T_FLOATB:
			return (tolerance == 0f ? NUMERICRELATIONS : APPROXRELATIONS) | UNDEFINED;
		case T_INTEGER:
			return tolerance == 0f ? NUMERICRELATIONS : APPROXRELATIONS;
		case T_DATE:
			return tolerance == 0f ? DATERELATIONS : APPROXDATERELATIONS;
		case T_STRING:
			return STRINGRELATIONS;
		default:
			return NORELATIONS;
		}
	}

	/**
	 * Returns the enumeration values
	 *
	 * @return enumeration values or null
	 */
	public Object getEnumeration() {
		return enumeration;
	}

	/**
	 * Returns the enumeration labels
	 *
	 * @return enumeration labels or null
	 */
	public String[] getEnumLabels() {
		return enumLabels;
	}

	/**
	 * Tests if the field can take part in a query
	 *
	 * @return - true if the field can take part in a query
	 */
	public boolean isQuery() {
		return (flags & QUERY) != 0;
	}

	/**
	 * Returns the field formatter
	 *
	 * @return field formatter or null
	 */
	public IFormatter getFormatter() {
		return formatter;
	}

	/**
	 * Returns the max string length or max fraction digits, -1 for unlimited
	 *
	 * @return max string length or max fraction digits
	 */
	public int getMaxlength() {
		return maxlength;
	}

	/**
	 * Returns the max negative or positive value Can be Float.NaN for undefined max
	 * value or Float.POSITVE_INFINITY for unlimited values
	 *
	 * @return max string length or max fraction digits
	 */
	public float getMaxValue() {
		return maxValue;
	}

	/**
	 * Returns the editing policy
	 *
	 * @return editing policy
	 */
	public int getEditable() {
		return flags & EDIT_MASK;
	}

	/**
	 * Returns the auto collection creation policy
	 *
	 * @return auto collection creation policy
	 */
	public int getAutoPolicy() {
		return flags & AUTO_MASK;
	}

	/**
	 * Tests if the field is applicable to the the media types of all assets
	 *
	 * @param assets
	 *            - assets affected
	 * @return true if the field is applicable
	 */
	public boolean isApplicable(List<? extends Asset> assets) {
		for (Asset asset : assets)
			if (!isApplicable(asset))
				return false;
		return true;
	}

	/**
	 * Tests if the field can be edited when all of the specified assets are
	 * affected
	 *
	 * @param assets
	 *            - assets affected
	 * @return true if field can be edited
	 */
	public boolean isEditable(List<? extends Asset> assets) {
		for (Asset asset : assets)
			if (!isEditable(asset))
				return false;
		return true;
	}

	/**
	 * Tests if the field is applicable to the assets media type
	 *
	 * @param asset
	 *            - asset affected
	 * @return true if field can be applied
	 */
	public boolean isApplicable(Asset asset) {
		IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(asset.getFormat());
		return mediaSupport != null ? testFlags(mediaSupport.getPropertyFlags()) : testFlags(PHOTO);
	}

	/**
	 * Tests if the field can be edited when the specified asset is affected
	 *
	 * @param asset
	 *            - asset affected
	 * @return true if field can be edited
	 */
	public boolean isEditable(Asset asset) {
		if (asset == null || asset.getFileState() == IVolumeManager.PEER)
			return false;
		switch (getEditable()) {
		case EDIT_ALWAYS:
			return true;
		case EDIT_ANALOG:
			return asset.getFileSource() != Constants.FILESOURCE_DIGITAL_CAMERA
					&& asset.getFileSource() != Constants.FILESOURCE_SIGMA_DIGITAL_CAMERA;
		case EDIT_DIGITAL:
			return asset.getFileSource() != Constants.FILESOURCE_FILMSCANNER
					&& asset.getFileSource() != Constants.FILESOURCE_REFLECTIVE_SCANNER;
		}
		return false;
	}

	public boolean isReportField() {
		return (flags & REPORT) != 0;
	}

	/**
	 * Returns the field parent
	 *
	 * @return field parent
	 */
	public QueryField getParent() {
		return parent;
	}

	/**
	 * Returns the XMP namespace
	 *
	 * @return XMP namespace
	 */
	public Namespace getXmpNs() {
		return xmpNs;
	}

	/**
	 * Returns the XMP path
	 *
	 * @return XMP path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the children of a field or an empty array
	 *
	 * @return child fields
	 */
	public QueryField[] getChildren() {
		return (children != null) ? children.toArray(new QueryField[children.size()]) : NOCHILDREN;
	}

	/**
	 * @return true if the field has children
	 */
	public boolean hasChildren() {
		return children != null;
	}

	/**
	 * Returns the cardinality of the field (-1 for unlimited list, -2 for unlimited
	 * set)
	 *
	 * @return cardinality
	 */
	public int getCard() {
		return card;
	}

	/**
	 * Returns a substitute for a missing value
	 *
	 * @return - value indicating the the value is missing
	 */
	public Object getMissingValue() {
		if (isStruct() || card != 1)
			return null;
		switch (type) {
		case T_BOOLEAN:
			return Boolean.FALSE;
		case T_POSITIVEFLOAT:
		case T_FLOAT:
			return Double.NaN;
		case T_POSITIVEINTEGER:
			return -1;
		case T_POSITIVELONG:
			return -1L;
		case T_INTEGER:
			return (enumeration instanceof int[]) ? ((int[]) enumeration)[0] : ZERO;
		default:
			return null;
		}
	}

	/**
	 * Returns the default value of the field
	 *
	 * @return default value
	 */
	public Object getDefaultValue() {
		if (isStruct())
			return null;
		if (card != 1) {
			switch (type) {
			case T_BOOLEAN:
				return EMPTYBOOL;
			case T_INTEGER:
				return EMPTYINT;
			case T_POSITIVEFLOAT:
			case T_FLOAT:
			case T_CURRENCY:
				return EMPTYDOUBLE;
			case T_STRING:
				return EMPTYSTRING;
			default:
				return EMPTYOBJECT;
			}
		}
		switch (type) {
		case T_BOOLEAN:
			return Boolean.FALSE;
		case T_POSITIVEFLOAT:
		case T_FLOAT:
		case T_FLOATB:
			return Double.NaN;
		case T_POSITIVEINTEGER:
			return ZERO;
		case T_POSITIVELONG:
			return LONGZERO;
		case T_INTEGER:
			return (enumeration instanceof int[]) ? ((int[]) enumeration)[0] : ZERO;
		case T_STRING:
			return ""; //$NON-NLS-1$
		default:
			return null;
		}
	}

	/**
	 * Finds a field by its XMP path
	 *
	 * @param path
	 *            - XMP path
	 * @return QueryField instance or null
	 */
	public static QueryField findXmpProperty(String path) {
		return pathMap.get(path);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */

	@Override
	public String toString() {
		return hasLabel() ? getLabel() : super.toString();
	}

	public String debugInfo() {
		return hasLabel() ? getLabel() : key != null ? key : super.toString();
	}

	/**
	 * Tests if field has a structured value
	 *
	 * @return true if field has a structured value
	 */
	public boolean isStruct() {
		return (type & T_STRUCT) != 0;
	}

	/**
	 * Test if the field is shown by default in the 'essential' mode
	 *
	 * @return true if the field is shown by default in the 'essential' mode
	 */
	public boolean isEssential() {
		return (flags & ESSENTIAL) != 0;
	}

	/**
	 * Test if the field contains the specified flags
	 *
	 * @param flags
	 *            - flags to test
	 * @return true if the field contains the specified flags
	 */
	public boolean testFlags(int flags) {
		return (this.flags & flags) != 0;
	}

	/**
	 * Returns the field action
	 *
	 * @return field action
	 */
	public int getAction() {
		return action;
	}

	/**
	 * Retrieves a field by its EXIFTOOL key
	 *
	 * @param key
	 *            - EXIFTOOL key
	 * @return QueryField instance
	 */
	public static QueryField findExifProperty(String key) {
		return exifToolMap.get(key);
	}

	/**
	 * Retrieves a region field by its EXIFTOOL key
	 *
	 * @param key
	 *            - EXIFTOOL key
	 * @return QueryField instance
	 */
	public static QueryField findRegionProperty(String key) {
		return regionPropertyMap.get(key);
	}

	/**
	 * Returns the EXIFTOOL key
	 *
	 * @return EXIFTOOL key
	 */
	public String getExifToolKey() {
		return exifToolKey instanceof String ? (String) exifToolKey
				: exifToolKey instanceof String[] ? ((String[]) exifToolKey)[0] : null;
	}

	/**
	 * Test if the field is shown by default in hover
	 *
	 * @return true if the field is shown by default in hover
	 */
	public boolean isHover() {
		return (flags & HOVER) != 0;
	}

	/**
	 * Returns the query tolerance (negative: absolute value, positive: percent)
	 *
	 * @return query tolerance
	 */
	public float getTolerance() {
		return tolerance;
	}

	/**
	 * Tests if an integer value represents an undefined entry
	 *
	 * @param value
	 * @return true if the value represents an undefined entry
	 */
	public boolean isUndefined(int value) {
		return (value < 0) ? type == T_POSITIVEINTEGER
				: (enumeration == null || value >= enumLabels.length) ? false
						: enumLabels[value].equals(Messages.QueryField_Undefined)
								|| enumLabels[value].equals(Messages.QueryField_Unknown);
	}

	/**
	 * Tests if a string value represents an undefined entry
	 *
	 * @param value
	 * @return true if the value represents an undefined entry
	 */
	public boolean isUndefined(String s) {
		if (!(enumeration instanceof String[]))
			return s.isEmpty();
		String[] ids = (String[]) enumeration;
		for (int j = 0; j < ids.length; j++)
			if (ids[j].equals(s))
				return enumLabels[j].equals(Messages.QueryField_Undefined)
						|| enumLabels[j].equals(Messages.QueryField_Unknown);
		return false;
	}

	/**
	 * Tests if a double value represents an undefined entry
	 *
	 * @param value
	 * @return true if the value represents an undefined entry
	 */
	public boolean isUndefined(double d) {
		return d < 0 ? type == T_POSITIVEFLOAT || type == T_CURRENCY : Double.isNaN(d);
	}

	private static final Class<?>[] NOPARMS = new Class[0];

	private static final Object[] NOARGS = new Object[0];

	/**
	 * Retrieves the field value from a given object The default implementation uses
	 * reflection to do so, individual fields may override
	 *
	 * @param obj
	 *            - Object containing the field value
	 * @return - the boxed field value
	 */
	public Object obtainPlainFieldValue(Object obj) {
		if (!isStruct()) {
			if (key == null || key.startsWith("$")) //$NON-NLS-1$
				return null;
			try {
				if (obj instanceof Asset)
					for (IMediaSupport support : getMediaSupport())
						if (support.handles(key)) {
							MediaExtension ext = support.getMediaExtension((Asset) obj);
							return (ext == null) ? getMissingValue() : support.getFieldValue(this, ext);
						}
				return obj.getClass().getMethod(getGetAccessor(), NOPARMS).invoke(obj, NOARGS);
			} catch (Exception e) {
				CoreActivator.getDefault().logError(NLS.bind(Messages.QueryField_internal_error_accessing_field, key),
						e);
			}
		}
		return null;
	}

	/**
	 * Sets the field value into a given object if the field is not a structure
	 *
	 * @param obj
	 *            - object to be modified
	 * @param newValue
	 *            - new field value
	 */
	public void setPlainFieldValue(Object obj, Object newValue) {
		if (!isStruct())
			setFieldValue(obj, newValue);
	}

	/**
	 * Sets the field value into a given object !! For BAG fields the field values
	 * are merged into the old values !!
	 *
	 * @param obj
	 *            - object to be modified
	 * @param newValue
	 *            - new field value
	 */
	public void setFieldValue(Object obj, Object newValue) {
		if (key != null && !key.startsWith("$")) //$NON-NLS-1$
			try {
				if (card == CARD_BAG || card == CARD_MODIFIABLEBAG)
					newValue = mergeVectoredValue(obj, newValue);
				if (obj instanceof Asset)
					for (IMediaSupport support : getMediaSupport())
						if (support.setFieldValue(this, (Asset) obj, newValue))
							return;
				obj.getClass().getMethod(getSetAccessor(), getJavaType()).invoke(obj, newValue);
			} catch (Exception e) {
				CoreActivator.getDefault().logError(NLS.bind(Messages.QueryField_internal_error_accessing_field, key),
						e);
			}
	}

	/**
	 * Resets a bag field to null
	 *
	 * @param obj
	 *            - object to be modified
	 */
	public void resetBag(Object obj) {
		if (key == null || key.startsWith("$")) //$NON-NLS-1$
			return;
		if (card != CARD_BAG && card != CARD_MODIFIABLEBAG)
			return;
		try {
			if (obj instanceof Asset) {
				for (IMediaSupport support : getMediaSupport())
					if (support.resetBag(this, (Asset) obj))
						return;
			}
			obj.getClass().getMethod(getSetAccessor(), getJavaType()).invoke(obj, new Object[] { null });
		} catch (Exception e) {
			CoreActivator.getDefault().logError(NLS.bind(Messages.QueryField_internal_error_accessing_field, key), e);
		}
	}

	public String getSetAccessor(String fname) {
		char[] chars = new char[fname.length() + 3];
		fname.getChars(0, fname.length(), chars, 3);
		chars[0] = 's';
		chars[1] = 'e';
		chars[2] = 't';
		chars[3] = Character.toUpperCase(chars[3]);
		return new String(chars);
	}

	public String getGetAccessor(String fname) {
		char[] chars = new char[fname.length() + 3];
		fname.getChars(0, fname.length(), chars, 3);
		chars[0] = 'g';
		chars[1] = 'e';
		chars[2] = 't';
		chars[3] = Character.toUpperCase(chars[3]);
		return new String(chars);
	}

	/**
	 * Construct SET accessor
	 *
	 * @return SET accessor
	 */
	public String getSetAccessor() {
		return getSetAccessor(key);
	}

	/**
	 * Construct get accessor
	 *
	 * @return get accessor
	 */
	public String getGetAccessor() {
		return getGetAccessor(key);
	}

	/**
	 * Determine the Java type of the field
	 *
	 * @return Java type
	 */
	public Class<?> getJavaType() {
		if (getCard() == 1) {
			switch (type) {
			case QueryField.T_BOOLEAN:
				return Boolean.TYPE;
			case QueryField.T_POSITIVEINTEGER:
			case QueryField.T_INTEGER:
				return Integer.TYPE;
			case QueryField.T_POSITIVELONG:
			case QueryField.T_LONG:
				return Long.TYPE;
			case QueryField.T_POSITIVEFLOAT:
			case QueryField.T_FLOAT:
			case QueryField.T_CURRENCY:
				return Double.TYPE;
			case QueryField.T_FLOATB:
				return Double.class;
			case QueryField.T_DATE:
				return Date.class;
			default:
				return String.class;
			}
		}
		switch (type) {
		case QueryField.T_POSITIVEINTEGER:
		case QueryField.T_INTEGER:
			return int[].class;
		case QueryField.T_POSITIVEFLOAT:
		case QueryField.T_FLOAT:
			return double[].class;
		default:
			return String[].class;
		}
	}

	/**
	 * Merges the field value of a source object into a target object
	 *
	 * @param source
	 *            - source object
	 * @param target
	 *            - target object
	 */
	public void mergeValues(Object source, Object target) {
		if (!isStruct()) {
			if (card == 1) {
				if (isNeutralValue(obtainPlainFieldValue(target)))
					setPlainFieldValue(target, obtainPlainFieldValue(source));
			} else
				setPlainFieldValue(target, mergeVectoredValue(target, obtainPlainFieldValue(source)));
		}
	}

	Object mergeVectoredValue(Object target, Object newValue) {
		Object oldValue = obtainPlainFieldValue(target);
		if (!isNeutralValue(newValue)) {
			if (isNeutralValue(oldValue))
				return newValue instanceof BagChange ? ((BagChange<?>) newValue).getDisplay() : newValue;
			else if (type == T_STRING) {
				Set<Object> set = new HashSet<Object>(((String[]) oldValue).length * 3 / 2);
				for (String v : ((String[]) oldValue))
					if (v != null)
						set.add(v);
				if (newValue instanceof BagChange)
					((BagChange<?>) newValue).update(set);
				else
					for (String v : ((String[]) newValue))
						if (v != null)
							set.add(v);
				String[] merged = set.toArray(new String[set.size()]);
				Arrays.sort(merged);
				return merged;
			}
		}
		return oldValue;
	}

	/**
	 * Retrieve the field value for a given asset. This should not be used on
	 * subfields. Use QueryField.obtainValue(asset, qfield,qsubfield) instead.
	 *
	 * @param asset
	 *            - asset
	 * @return - field value
	 */
	public Object obtainFieldValue(Asset asset) {
		if (asset == null || getType() == QueryField.T_NONE)
			return null;
		if (!isStruct()) {
			if (key == null)
				return ""; //$NON-NLS-1$
			if (card == 1) {
				switch (type) {
				case T_INTEGER:
					return getInt(asset);
				case T_LONG:
					return getLong(asset);
				case T_POSITIVELONG:
					long l = getLong(asset);
					return (l < 0) ? null : l;
				case T_POSITIVEINTEGER:
					int i = getInt(asset);
					return (i < 0) ? null : i;
				case T_FLOAT:
					double d = getDouble(asset);
					return (Double.isNaN(d)) ? null : d;
				case T_POSITIVEFLOAT:
				case T_CURRENCY:
					d = getDouble(asset);
					return (Double.isNaN(d) || d < 0) ? null : d;
				case T_BOOLEAN:
					return getBoolean(asset);
				}
			}
			return getValue(asset);
		}
		Object collected = getStruct(asset);
		return (collected != null) ? resolveStruct(collected, true) : null;
	}

	/**
	 * Retrieve the field value for a list of assets This method checks if the
	 * values from individual assets match and returns VALUE_MIXED if not
	 *
	 * @param assets
	 *            - list of assets
	 * @param monitor
	 *            - progress monitor
	 * @return - folded and boxed field value
	 */
	public Object obtainFieldValue(List<Asset> assets, IProgressMonitor monitor) {
		if (assets == null || getType() == QueryField.T_NONE)
			return null;
		Object collected = null;
		if (assets.isEmpty())
			return VALUE_NOTHING;
		if (!isStruct()) {
			if (key == null)
				return ""; //$NON-NLS-1$
			try {
				Iterator<Asset> it = assets.iterator();
				if (card == 1) {
					switch (type) {
					case T_INTEGER:
						int collInt = getInt(it.next());
						while (it.hasNext() && (monitor == null || !monitor.isCanceled()))
							if (collInt != getInt(it.next()))
								return VALUE_MIXED;
						return collInt;
					case T_LONG:
						long collLong = getLong(it.next());
						while (it.hasNext() && (monitor == null || !monitor.isCanceled()))
							if (collLong != getLong(it.next()))
								return VALUE_MIXED;
						return collLong;
					case T_POSITIVEINTEGER:
						collInt = getInt(it.next());
						while (it.hasNext() && (monitor == null || !monitor.isCanceled()))
							if (collInt != getInt(it.next()))
								return VALUE_MIXED;
						return (collInt < 0) ? null : collInt;
					case T_POSITIVELONG:
						collLong = getLong(it.next());
						while (it.hasNext() && (monitor == null || !monitor.isCanceled()))
							if (collLong != getLong(it.next()))
								return VALUE_MIXED;
						return (collLong < 0) ? null : collLong;
					case T_FLOAT:
						double collDouble = getDouble(it.next());
						while (it.hasNext() && (monitor == null || !monitor.isCanceled()))
							if (collDouble != getDouble(it.next()))
								return VALUE_MIXED;
						return (Double.isNaN(collDouble)) ? null : collDouble;
					case T_POSITIVEFLOAT:
					case T_CURRENCY:
						collDouble = getDouble(it.next());
						while (it.hasNext() && (monitor == null || !monitor.isCanceled()))
							if (collDouble != getDouble(it.next()))
								return VALUE_MIXED;
						return (Double.isNaN(collDouble) || collDouble < 0d) ? null : collDouble;
					case T_BOOLEAN:
						boolean collBool = getBoolean(it.next());
						while (it.hasNext() && (monitor == null || !monitor.isCanceled()))
							if (collBool != getBoolean(it.next()))
								return VALUE_MIXED;
						return collBool;
					default:
						collected = getValue(it.next());
						while (it.hasNext() && (monitor == null || !monitor.isCanceled())) {
							Object v = getValue(it.next());
							if (collected == v)
								continue;
							if (collected == null || v == null || !collected.equals(v))
								return VALUE_MIXED;
						}
						return collected;
					}
				}
				collected = getValue(it.next());
				if (!it.hasNext())
					return collected;
				switch (type) {
				case T_INTEGER:
				case T_POSITIVEINTEGER:
					while (it.hasNext() && (monitor == null || !monitor.isCanceled()))
						if (!Arrays.equals((int[]) collected, (int[]) getValue(it.next())))
							return VALUE_MIXED;
					return collected;
				case T_POSITIVEFLOAT:
				case T_FLOAT:
					while (it.hasNext() && (monitor == null || !monitor.isCanceled()))
						if (!Arrays.equals((double[]) collected, (double[]) getValue(it.next())))
							return VALUE_MIXED;
					return collected;
				default:
					HashSet<String> set1 = null;
					int l = collected == null ? -1 : ((String[]) collected).length;
					if (l > 0)
						set1 = new HashSet<String>(Arrays.asList((String[]) collected));
					while (it.hasNext() && (monitor == null || !monitor.isCanceled())) {
						Object result = getValue(it.next());
						if (collected == result)
							continue;
						if (collected == null || result == null || l != ((String[]) result).length)
							return VALUE_MIXED;
						for (String string : (String[]) result)
							if (!set1.contains(string))
								return VALUE_MIXED;
					}
					return collected;
				}
			} catch (Exception e) {
				CoreActivator.getDefault().logError(NLS.bind(Messages.QueryField_internal_error_accessing_field, key),
						e);
			}
		} else {
			Iterator<Asset> it = assets.iterator();
			collected = getStruct(it.next());
			HashSet<String> set1 = null;
			int l = (collected instanceof String[]) ? ((String[]) collected).length : -1;
			if (l > 0)
				set1 = new HashSet<String>(Arrays.asList((String[]) collected));
			while (it.hasNext() && (monitor == null || !monitor.isCanceled())) {
				Object result = getStruct(it.next());
				if (l >= 0 && result instanceof String[]) {
					if (l != ((String[]) result).length)
						return VALUE_MIXED;
					for (String string : (String[]) result)
						if (!set1.contains(string))
							return VALUE_MIXED;
				} else if ((collected == null && result != null) || (collected != null && !collected.equals(result)))
					return VALUE_MIXED;
			}
			if (collected != null && collected != VALUE_MIXED)
				return resolveStruct(collected, true);
		}
		return collected;
	}

	protected int getInt(Asset asset) {
		return -1;
	}

	protected long getLong(Asset asset) {
		return -1l;
	}

	protected double getDouble(Asset asset) {
		return Double.NaN;
	}

	protected boolean getBoolean(Asset asset) {
		return false;
	}

	protected Object getValue(Asset asset) {
		return null;
	}

	private Object resolveStruct(Object struct, boolean extend) {
		IDbManager dbManager = CoreActivator.getDefault().getDbManager();
		if (struct instanceof String[]) {
			String[] array = (String[]) struct;
			List<IndexedMember> list = new ArrayList<IndexedMember>(array.length + 1);
			for (int i = 0; i < array.length; i++) {
				IdentifiableObject obj = dbManager.obtainById(IdentifiableObject.class, array[i]);
				if (obj != null)
					list.add(new IndexedMember(this, obj, i));
			}
			if (extend)
				list.add(new IndexedMember(this, null, list.size()));
			return list.toArray(new IndexedMember[list.size()]);
		}
		return dbManager.obtainById(IdentifiableObject.class, (String) struct);
	}

	/**
	 * Retrieves a structured field value for a given asset
	 *
	 * @param asset
	 *            - given asset
	 * @return - structured field value
	 */
	public Object getStruct(Asset asset) {
		return null;
	}

	/**
	 * Retrieves structured field relation ids for a given asset
	 *
	 * @param asset
	 *            - given asset
	 * @return - structured field relation IDs
	 */
	public Object getStructRelationIds(Asset asset) {
		return null;
	}

	public Object getStructRelationIds(Collection<Asset> asset) {
		boolean first = true;
		Object result = null;
		for (Asset a : asset) {
			Object o = getStructRelationIds(a);
			if (first) {
				result = o instanceof String[] ? new HashSet<String>(Arrays.asList((String[]) o)) : o;
				first = false;
			} else {
				if (result == null) {
					if (o != null)
						return VALUE_MIXED;
				} else if (result instanceof String) {
					if (!result.equals(o))
						return VALUE_MIXED;
				} else if (result instanceof Set) {
					if (!(o instanceof String[]) || !result.equals(new HashSet<String>(Arrays.asList((String[]) o))))
						return VALUE_MIXED;
				}
			}
		}
		return result;
	}

	/**
	 * Converts a field value into its text representation
	 *
	 * @param value
	 *            - boxed field value
	 * @param structDefault
	 *            - default value for structured field values
	 * @return - text representation
	 */
	public String value2text(Object value, String structDefault) {
		return value2text(value, structDefault, true, true, null);
	}

	/**
	 * Converts a field value into its text representation
	 *
	 * @param value
	 *            - boxed field value
	 * @param structDefault
	 *            - default value for structured field values
	 * @param useEnums
	 *            - true if values are to be translated to enumeration strings
	 * @param useFormatter
	 *            - true if defined formatter is to be used
	 * @param inLocale
	 *            - Locale for formatting or null
	 * @return - text representation
	 */
	public String value2text(Object value, String structDefault, boolean useEnums, boolean useFormatter,
			Locale inLocale) {
		if (value == QueryField.VALUE_MIXED || value == QueryField.VALUE_NOTHING)
			return (String) value;
		if (value == null)
			return Format.MISSINGENTRYSTRING;
		if (isStruct())
			return value instanceof IndexedMember[] ? QueryField.VALUE_NOTHING : serializeStruct(value, structDefault);
		return doValueToText(value, useEnums, useFormatter, inLocale, false);
	}

	String doValueToText(Object value, boolean useEnums, boolean useFormatter, Locale inLocale, boolean recursion) {
		if (isSubfield && !recursion && value instanceof Object[]) {
			StringBuilder sb = new StringBuilder();
			for (Object v : (Object[]) value) {
				String s = doValueToText(v, useEnums, useFormatter, inLocale, true);
				if (s != null && !s.isEmpty() && !Format.MISSINGENTRYSTRING.equals(s)) {
					if (sb.length() > 0)
						sb.append(';');
					sb.append(s);
				}
			}
			return sb.toString();
		}
		if (card != 1) {
			StringBuilder sb = new StringBuilder();
			if (value instanceof int[]) {
				int[] array = (int[]) value;
				for (int v : array) {
					if ((type == QueryField.T_POSITIVEINTEGER || type == QueryField.T_POSITIVELONG) && v < 0)
						return Format.MISSINGENTRYSTRING;
					String s = formatScalarValue(v, useEnums, useFormatter, inLocale);
					if (s == null)
						return null;
					if (sb.length() > 0)
						sb.append(';');
					sb.append(s);
				}
			} else if (value instanceof boolean[]) {
				boolean[] array = (boolean[]) value;
				for (boolean v : array) {
					String s = formatScalarValue(v, useEnums, useFormatter, inLocale);
					if (s == null)
						return null;
					if (sb.length() > 0)
						sb.append(';');
					sb.append(s);
				}
			} else if (value instanceof double[]) {
				double[] array = (double[]) value;
				for (double v : array) {
					if (Double.isNaN(v)
							|| ((type == QueryField.T_POSITIVEFLOAT || type == QueryField.T_CURRENCY) && v < 0))
						return Format.MISSINGENTRYSTRING;
					String s = formatScalarValue(v, useEnums, useFormatter, inLocale);
					if (s == null)
						return null;
					if (sb.length() > 0) {
						if (this == QueryField.EXIF_DOF)
							sb.append(Format.MISSINGENTRYSTRING);
						else
							sb.append(';');
					}
					sb.append(s);
				}
			} else if (value instanceof String[]) {
				String[] array = (String[]) value;
				for (String v : array) {
					String s = formatScalarValue(v, useEnums, useFormatter, inLocale);
					if (s != null) {
						if (sb.length() > 0)
							sb.append(';');
						sb.append(s);
					}
				}
			}
			return sb.toString();
		}
		return formatScalarValue(value, useEnums, useFormatter, inLocale);
	}

	/**
	 * Converts a structured field value into its text representation
	 *
	 * @param value
	 *            - the field value
	 * @param dflt
	 *            - default value
	 * @return text representation
	 */
	public static String serializeStruct(Object value, String dflt) {
		StringBuilder sb = new StringBuilder();
		if (value instanceof Location) {
			Location loc = (Location) value;
			append(sb, loc.getDetails());
			append(sb, loc.getSublocation());
			append(sb, loc.getCity());
			append(sb, loc.getCountryISOCode());
			append(sb, loc.getWorldRegion());
			if (sb.length() == 0)
				sb.append(dflt);
			return sb.toString();
		} else if (value instanceof Contact) {
			Contact contact = (Contact) value;
			append(sb, contact.getAddress());
			append(sb, contact.getCity());
			append(sb, contact.getCountry());
			append(sb, contact.getEmail());
			append(sb, contact.getPhone());
			if (sb.length() == 0)
				sb.append(dflt);
			return sb.toString();
		} else if (value instanceof ArtworkOrObject) {
			ArtworkOrObject art = (ArtworkOrObject) value;
			append(sb, art.getTitle());
			append(sb, art.getCreator());
			append(sb, art.getSource());
			append(sb, art.getSourceInventoryNumber());
			if (sb.length() == 0)
				sb.append(dflt);
			return sb.toString();
		}
		return null;
	}

	private static void append(StringBuilder sb, String[] s) {
		if (s != null && s.length > 0) {
			for (int i = 0; i < s.length; i++) {
				if (sb.length() > 0)
					sb.append((i == 0) ? ';' : ',');
				sb.append(s[i]);
			}
		}
	}

	private static void append(StringBuilder sb, String s) {
		if (s != null && s.length() > 0) {
			if (sb.length() > 0)
				sb.append(';');
			sb.append(s);
		}
	}

	/**
	 * Format scalar field value
	 *
	 * @param value
	 *            - field value
	 * @return text representation
	 */
	public String formatScalarValue(Object value) {
		return formatScalarValue(value, true, true, null);
	}

	/**
	 * Format scalar field value
	 *
	 * @param value
	 *            - field value
	 * @param useEnums
	 *            - true if values are to be translated to enumeration strings
	 * @param useFormatter
	 *            - true if defined formatter is to be used
	 * @param inLocale
	 *            - Locale for formatting or null
	 * @return text representation
	 */
	public String formatScalarValue(Object value, boolean useEnums, boolean useFormatter, Locale inLocale) {
		if (value == null)
			return null;
		if (enumeration != null) {
			String[] enumLabs = getEnumLabels();
			if (useEnums && enumeration instanceof int[]) {
				int v = ((Integer) value).intValue();
				int[] a = (int[]) enumeration;
				for (int i = 0; i < a.length; i++)
					if (a[i] == v)
						return enumLabs[i];
				return NLS.bind(Messages.QueryField_out_of_range, v);
			}
			if (enumeration instanceof String[]) {
				String[] sa = (String[]) enumeration;
				for (int i = 0; i < sa.length; i++)
					if (sa[i].equals(value))
						return enumLabs[i];
				return NLS.bind(Messages.QueryField_out_of_range, value);
			}
		}
		if (useFormatter) {
			IFormatter formatter1 = getFormatter();
			if (formatter1 != null)
				return formatter1.toString(value);
		}
		switch (getType()) {
		case QueryField.T_BOOLEAN:
			return (Boolean) value ? "1" //$NON-NLS-1$
					: "0"; //$NON-NLS-1$
		case QueryField.T_POSITIVEINTEGER:
			if (value instanceof Integer && ((Integer) value).intValue() < 0)
				return Format.MISSINGENTRYSTRING;
			break;
		case QueryField.T_POSITIVELONG:
			if (value instanceof Long && ((Long) value).longValue() < 0)
				return Format.MISSINGENTRYSTRING;
			break;
		case QueryField.T_POSITIVEFLOAT:
		case QueryField.T_FLOAT:
		case QueryField.T_FLOATB:
			if (value instanceof Double) {
				Double d = (Double) value;
				if (d.isInfinite())
					return Messages.QueryField_infinite;
				if (d.isNaN())
					return Format.MISSINGENTRYSTRING;
				NumberFormat nf = inLocale != null ? NumberFormat.getNumberInstance(inLocale)
						: NumberFormat.getNumberInstance();
				nf.setMaximumFractionDigits(getMaxlength());
				return nf.format(value);
			}
			break;
		case QueryField.T_CURRENCY:
			if (value instanceof Double) {
				if (((Double) value).isNaN())
					return Format.MISSINGENTRYSTRING;
				NumberFormat cf = inLocale != null ? NumberFormat.getCurrencyInstance(inLocale)
						: NumberFormat.getCurrencyInstance();
				int digits = cf.getCurrency().getDefaultFractionDigits();
				cf.setMaximumFractionDigits(digits);
				cf.setMinimumFractionDigits(digits);
				return cf.format(value);
			}
			break;
		case QueryField.T_DATE:
			if (value instanceof Date)
				return (inLocale != null) ? DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, inLocale)
						.format((Date) value) : Constants.DFDT.format((Date) value);
			break;
		}
		return String.valueOf(value);
	}

	/**
	 * Sets a keyword filter for creating presentations of the keyword field
	 *
	 * @param filter
	 *            - keyword filter
	 */
	public static void setKeywordFilter(FilterChain filter) {
		keywordFilter = filter;
	}

	/**
	 * Retrieves the current keyword filter
	 *
	 * @return keyword filter
	 */
	public static FilterChain getKeywordFilter() {
		return keywordFilter;
	}

	/**
	 * Test if the field contributes to the Lucene index
	 *
	 * @return - true if the field contributes to the Lucene index
	 */
	public boolean isFullTextSearch() {
		return (flags & TEXT) != 0;
	}

	/**
	 * Test if the field is a selectable text field
	 *
	 * @return - true if the field is a selectable text field
	 */
	public boolean isFullTextBase() {
		return isFullTextSearch() && category.query;
	}

	/**
	 * Tests if the specified value is a neutral value
	 *
	 * @param value
	 *            - value to be tested
	 * @return true if the specified value is a neutral value
	 */
	public boolean isNeutralValue(Object value) {
		if (value == null)
			return true;
		if (value instanceof BagChange)
			return !((BagChange<?>) value).hasChanges();
		if (getCard() == 1) {
			switch (getType()) {
			case T_BOOLEAN:
				return false;
			case T_POSITIVEINTEGER:
			case T_INTEGER:
				return isUndefined((Integer) value);
			case T_POSITIVEFLOAT:
			case T_FLOAT:
			case T_CURRENCY:
			case T_FLOATB:
				return isUndefined((Double) value);
			case T_DATE:
				return false;
			case T_POSITIVELONG:
			case T_LONG:
				return isUndefined((Long) value);
			default:
				return isUndefined(value.toString());
			}
		}
		switch (getType()) {
		case T_POSITIVEINTEGER:
		case T_INTEGER:
			return ((int[]) value).length == 0;
		case T_POSITIVELONG:
		case T_LONG:
			return ((long[]) value).length == 0;
		case T_POSITIVEFLOAT:
		case T_FLOAT:
			return ((double[]) value).length == 0;
		default:
			return ((String[]) value).length == 0;
		}
	}

	/**
	 * Returns the unit of measurement or null
	 *
	 * @return unit of measurement or null
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * Returns the spelling options for the field (see ISpellCheckingService)
	 *
	 * @return spellingOptions
	 */
	public int getSpellingOptions() {
		return spellingOptions;
	}

	/**
	 * Tests if the specified value is valid
	 *
	 * @param asset
	 *            - asset in whose context the value shall be checked or null
	 *
	 * @return - error message or null of valid
	 */
	public String isValid(Object value, Asset asset) {
		return null;
	}

	public boolean isHidden() {
		return (getEditable() & EDIT_HIDDEN) != 0;
	}

	/**
	 * Parses and EXIF date string
	 *
	 * @param v
	 *            - input String
	 * @return - Date object
	 * @throws XMPException
	 */
	@SuppressWarnings("fallthrough")
	public static Date parseDate(String v) throws XMPException {
		try {
			return new SimpleDateFormat("yyyy:MM:dd HH:mm:ss Z").parse(v); //$NON-NLS-1$
		} catch (ParseException e) {
			try {
				return new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(v); //$NON-NLS-1$
			} catch (Exception e1) {
				try {
					return XMPUtils.convertToDate(v).getCalendar().getTime();
				} catch (Exception e2) {
					// At last try brute force
					char[] chars = v.toCharArray();
					boolean isTime = false;
					int cnt = 2;
					for (int i = 0; i < chars.length; i++) {
						switch (chars[i]) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							break;
						case ' ':
							chars[i] = 'T';
							/* FALL-THROUGH */
						case 'T':
							isTime = true;
							cnt = 2;
							break;
						case 'Z':
						case '+':
						case '-':
							break;
						default:
							if (cnt-- > 0)
								chars[i] = isTime ? ':' : '-';
							break;
						}
					}
					return XMPUtils.convertToDate(new String(chars)).getCalendar().getTime();
				}
			}
		}
	}

	/**
	 * Parses an EXIF floating point string
	 *
	 * @param v
	 *            - input string
	 * @return - floating point number
	 */
	public static double parseDouble(String v) throws NumberFormatException {
		if (v == null)
			return Double.NaN;
		v = v.trim();
		try {
			return Double.parseDouble(v.startsWith("+") ? v.substring(1) : v); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			v = v.toLowerCase();
			if (v.startsWith("inf")) //$NON-NLS-1$
				return Double.POSITIVE_INFINITY;
			if (v.startsWith("undef") || "none".equals(v) || v.startsWith("--")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return Double.NaN;
			throw e;
		}
	}

	/**
	 * Parses an EXIF integer string
	 *
	 * @param v
	 *            - input string
	 * @param dflt
	 *            - default value for undefined slots
	 * @return - integer number
	 */
	public int parseInt(String v, int dflt) throws NumberFormatException {
		if (v == null)
			return dflt;
		v = v.trim();
		try {
			return (int) (Double.parseDouble(v.startsWith("+") ? v.substring(1) : v) + 0.5d); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			v = v.toLowerCase();
			if (v.startsWith("inf")) //$NON-NLS-1$
				return Integer.MAX_VALUE;
			if (v.startsWith("undef") || "none".equals(v) || v.startsWith("--")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return dflt;
			try {
				return handleIntErrand(v);
			} catch (UnsupportedOperationException e1) {
				throw e;
			}
		}
	}

	public int handleIntErrand(String v) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Parses an EXIF long string
	 *
	 * @param v
	 *            - input string
	 * @param dflt
	 *            - default value for undefined slots
	 * @return - integer number
	 */
	public static long parseLong(String v, long dflt) throws NumberFormatException {
		if (v == null)
			return dflt;
		v = v.trim();
		try {
			return Long.parseLong(v.startsWith("+") ? v.substring(1) : v); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			v = v.toLowerCase();
			if (v.startsWith("inf")) //$NON-NLS-1$
				return Long.MAX_VALUE;
			if (v.startsWith("undef") || "none".equals(v) || v.startsWith("--")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return dflt;
			throw e;
		}
	}

	/**
	 * Returns the list of registered field categories
	 *
	 * @return - list of categories
	 */
	public static List<Category> getCategories() {
		return categories;
	}

	/**
	 * Returns the list of registered field categories and associated subgroups
	 *
	 * @return - list of categories and subgroups containing Category and QueryField
	 *         objects
	 */
	public static List<Object> getCategoriesAndSubgroups() {
		if (categoriesAndSubgroups == null) {
			categoriesAndSubgroups = new ArrayList<Object>(30);
			for (Category cat : categories) {
				categoriesAndSubgroups.add(cat);
				for (QueryField subgroup : subgroups)
					if (subgroup.getCategory() == cat)
						categoriesAndSubgroups.add(subgroup);
			}
		}
		return categoriesAndSubgroups;
	}

	/**
	 * Returns the category or subgroup to which the receiver belongs
	 *
	 * @return Category or QueryField object
	 */
	public Object getCategoryOrSubgroup() {
		getCategoriesAndSubgroups();
		QueryField p = getParent();
		while (p != ALL) {
			if (categoriesAndSubgroups.contains(p))
				return p;
			p = p.getParent();
		}
		return getCategory();
	}

	/**
	 * Flags the field, its children and its parents with the given property flag
	 *
	 * @param flag
	 *            - flag to be set
	 */
	public void setFlag(int flag) {
		flags |= flag;
		if (children != null)
			for (QueryField child : children)
				child.setFlag(flag);
		QueryField ancestor = parent;
		while (ancestor != null && !ancestor.testFlags(flag)) {
			ancestor.flags |= flag;
			ancestor = ancestor.parent;
		}
	}

	/**
	 * Fetches common string items from a set of assets
	 *
	 * @param assets
	 * @return array of common string items or null
	 */
	public String[] getCommonItems(Collection<Asset> assets) {
		Set<String> common = null;
		for (Asset asset : assets) {
			Object v = getValue(asset);
			if (v instanceof String[]) {
				String[] items = (String[]) v;
				List<String> list = new ArrayList<String>(items.length);
				for (String item : items)
					if (item != null)
						list.add(item);
				if (common == null)
					common = new HashSet<String>(list);
				else
					common.retainAll(list);
			}
		}
		return common == null ? null : common.toArray(new String[common.size()]);
	}

	/**
	 * Tests if the receiver belongs to the specified category or group
	 *
	 * @param g
	 *            - Category or QueryField object
	 * @return true if the receiver belongs to the specified category or group
	 */
	public boolean belongsTo(Object g) {
		if (g instanceof Category)
			return ((g == QueryField.CATEGORY_ALL || getCategory() == g));
		QueryField p = getParent();
		while (p != null) {
			if (p == g)
				return true;
			p = p.getParent();
		}
		return false;
	}

}
