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

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.bdaum.zoom.core.internal.FileNameExtensionFilter;
import com.bdaum.zoom.program.BatchConstants;

public class Constants {

	private Constants() {
	}

	public final static String APPLICATION_NAME = BatchConstants.APPLICATION_NAME;
	public static final String APPNAME = BatchConstants.APPNAME;
	public static final String REQUIRED_JAVA_VERSIONS = "1.8.0"; //$NON-NLS-1$

	public static final boolean WIN32 = BatchConstants.WIN32;
	public static final boolean OSX = BatchConstants.OSX;
	public static final boolean LINUX = BatchConstants.LINUX;
	public final static String[] EXEEXTENSION = new String[] { WIN32 ? "*.exe;*.EXE" //$NON-NLS-1$
			: "*.*" }; //$NON-NLS-1$
	public final static String[] EXEFILTERNAMES = new String[] {
			WIN32 ? Messages.Constants_exeutables : Messages.Constants_all_files };

	public static final String FILESCHEME = "file"; //$NON-NLS-1$

	public static final String APPCOLOR_BLACK = "b"; //$NON-NLS-1$
	public static final String APPCOLOR_DARKGREY = "d"; //$NON-NLS-1$
	public static final String APPCOLOR_GREY = "g"; //$NON-NLS-1$
	public static final String APPCOLOR_WHITE = "w"; //$NON-NLS-1$
	public static final String APPCOLOR_PLATFORM = "p"; //$NON-NLS-1$
	public static final char APPCOLOR_WIDGETTEXT = '_';
	public static final char APPCOLOR_GALLERYITEMTEXT = '-';
	public static final char APPCOLOR_SELECTEDGALLERYITEMBACKGROUND = '#';
	public static final char APPCOLOR_SELECTEDGALLERYITEMTEXT = '!';

	public static final String APPCOLOR_REGION_FACE = "regionColor"; //$NON-NLS-1$

	public final static String CATALOGEXTENSION = BatchConstants.CATEXTENSION;
	public final static String KEYWORDFILEEXTENSION = ".zkf"; //$NON-NLS-1$
	public final static String CATEGORYFILEEXTENSION = ".zcf"; //$NON-NLS-1$
	public final static String INDEXEXTENSION = ".zix"; //$NON-NLS-1$
	public final static String OID = "stringId"; //$NON-NLS-1$

	public static final String PRINTLAYOUT_ID = "printLayout"; //$NON-NLS-1$
	public static final String SIMILARITYOPTIONS_ID = "similarityOptions"; //$NON-NLS-1$ ;
	public static final String TEXTSEARCHOPTIONS_ID = "textSearchOptions"; //$NON-NLS-1$ ;
	public static final int TEXTSEARCHOPTIONS_DEFAULT_MAXCOUNT = 100;
	public static final int TEXTSEARCHOPTIONS_DEFAULT_MIN_SCORE = 12;
	public static final int TEXTSEARCHOPTIONS_DEFAULT_WEIGHT = 50;

	public static final String GROUP_ID_IMPORTS = "groupImports"; //$NON-NLS-1$
	public static final String GROUP_ID_RECENTIMPORTS = "recentImports"; //$NON-NLS-1$
	public static final String GROUP_ID_FOLDERSTRUCTURE = "groupFolderStructure"; //$NON-NLS-1$
	public static final String GROUP_ID_TIMELINE = "groupTimeline"; //$NON-NLS-1$
	public static final String GROUP_ID_RATING = "groupRating"; //$NON-NLS-1$
	public static final String GROUP_ID_USER = "groupUser"; //$NON-NLS-1$
	public static final String GROUP_ID_SLIDESHOW = "groupSlideshow"; //$NON-NLS-1$
	public static final String GROUP_ID_EXHIBITION = "groupExhibition"; //$NON-NLS-1$
	public static final String GROUP_ID_WEBGALLERY = "groupWebGallery"; //$NON-NLS-1$
	public static final String GROUP_ID_IMPORTED_ALBUMS = "groupImportedAlbums"; //$NON-NLS-1$
	public static final String GROUP_ID_PERSONS = "groupPersons"; //$NON-NLS-1$
	public static final String GROUP_ID_LOCATIONS = "groupLocations"; //$NON-NLS-1$
	public static final String GROUP_ID_AUTO = "groupAuto"; //$NON-NLS-1$
	public static final String GROUP_ID_AUTOSUB = "AUTO:"; //$NON-NLS-1$
	public static final String GROUP_ID_MEDIA = "groupMedia"; //$NON-NLS-1$

	public static final String GROUP_ID_CATEGORIES = "groupCategories"; //$NON-NLS-1$
	public static final String LAST_IMPORT_ID = "lastImport"; //$NON-NLS-1$
	public static final DateFormat DFIMPORTDD = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
	public static final DateFormat DFIMPORTHH = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
	public static final DateFormat DFIMPORT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

	public final static SimpleDateFormat DFDT = new SimpleDateFormat(Messages.Constants_ExtenalDateTimeFormat);

	public static final SimpleDateFormat IPTCDF = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$

	public static final DateFormatSymbols DATEFORMATS = new DateFormatSymbols(Locale.getDefault());

	public static final long FOLDERWATCHINTERVAL = 15000L;

	public static final String DERIVE_NO = "false"; //$NON-NLS-1$
	public static final String DERIVE_FOLDER = "folder"; //$NON-NLS-1$
	public static final String DERIVE_ALL = "true"; //$NON-NLS-1$

	public static final String USERFIELD2 = "{userfield2}"; //$NON-NLS-1$
	public static final String USERFIELD1 = "{userfield1}"; //$NON-NLS-1$

	public final static int FILESOURCE_UNKNOWN = 0;
	public final static int FILESOURCE_FILMSCANNER = 1;
	public final static int FILESOURCE_REFLECTIVE_SCANNER = 2;
	public final static int FILESOURCE_DIGITAL_CAMERA = 3;
	public final static int FILESOURCE_SIGMA_DIGITAL_CAMERA = 0x03000000;

	public final static int ANALOGTYPE_UNKNOWN = -1;
	public final static int ANALOGTYPE_UNDEFINED = 0;
	public final static int ANALOGTYPE_NEGATIVE = 1;
	public final static int ANALOGTYPE_TRANSPARENCY = 2;
	public final static int ANALOGTYPE_REFLECTIVE = 3;
	public final static int ANALOGTYPE_OTHER = 255;
	public final static int STATE_UNDEFINED = -1;
	public final static int STATE_UNKNOWN = 0;
	public final static int STATE_RAW = 1;
	public final static int STATE_CONVERTED = 2;
	public final static int STATE_DEVELOPED = 3;
	public final static int STATE_CORRECTED = 4;
	public final static int STATE_RETOUCHED = 5;
	public final static int STATE_READY = 6;
	public final static int STATE_TODO = 7;
	public final static int STATE_UNDERCONSIDERATION = 8;

	public final static int COLOR_UNDEFINED = -1;
	public final static int COLOR_BLACK = 0;
	public final static int COLOR_WHITE = 1;
	public final static int COLOR_RED = 2;
	public final static int COLOR_GREEN = 3;
	public final static int COLOR_BLUE = 4;
	public final static int COLOR_CYAN = 5;
	public final static int COLOR_MAGENTA = 6;
	public final static int COLOR_YELLOW = 7;
	public final static int COLOR_ORANGE = 8;
	public final static int COLOR_PINK = 9;
	public final static int COLOR_VIOLET = 10;

	public final static int RATING_UNDEFINED = -1;
	public final static int RATING_BAD = 0;
	public final static int RATING_BELOW_AVERAGE = 1;
	public final static int RATING_AVERAGE = 2;
	public final static int RATING_ABOVE_AVERAGE = 3;
	public final static int RATING_GOOD = 4;
	public final static int RATING_EXCELLENT = 5;

	public static final int FORMAT_ORIGINAL = 0;
	public static final int FORMAT_JPEG = 1;
	public static final int FORMAT_WEBP = 2;
	public static final int SCALE_ORIGINAL = 0;
	public static final int SCALE_SCALED = 1;
	public static final int SCALE_FIXED = 2;

	public static final int SERIES_ALL = 0;
	public static final int SERIES_EXP_BRACKET = 1;
	public static final int SERIES_FOCUS_BRACKET = 2;
	public static final int SERIES_ZOOM_BRACKET = 3;
	public static final int SERIES_RAPID = 4;
	public static final int SERIES_OTHER = 5;

	public static final String PT_CATALOG = "{catalog}"; //$NON-NLS-1$
	public static final String PT_TODAY = "{today}"; //$NON-NLS-1$
	public static final String PT_COUNT = "{count}"; //$NON-NLS-1$
	public static final String PT_PAGENO = "{pageNo}"; //$NON-NLS-1$
	public static final String PT_PAGECOUNT = "{pageCount}"; //$NON-NLS-1$
	public static final String PT_COLLECTION = "{collection}"; //$NON-NLS-1$
	public static final String PT_USER = "{user}"; //$NON-NLS-1$
	public static final String PT_OWNER = "{owner}"; //$NON-NLS-1$
	public static final String[] PT_ALL = new String[] { PT_CATALOG, PT_TODAY, PT_COUNT, PT_PAGENO, PT_PAGECOUNT,
			PT_COLLECTION, PT_USER, PT_OWNER };
	public static final String PI_TITLE = "{title}"; //$NON-NLS-1$
	public static final String PI_NAME = "{name}"; //$NON-NLS-1$
	public static final String PI_DESCRIPTION = "{description}"; //$NON-NLS-1$
	public static final String PI_CREATIONDATE = "{creationDate}"; //$NON-NLS-1$
	public static final String PI_SEQUENCENO = "{sequenceNo}"; //$NON-NLS-1$
	public static final String PI_PAGEITEM = "{pageItem}"; //$NON-NLS-1$
	public static final String PI_SIZE = "{size}"; //$NON-NLS-1$
	public static final String PI_FORMAT = "{format}"; //$NON-NLS-1$

	public static final String TV_SS = "{ss}"; //$NON-NLS-1$
	public static final String TV_II = "{ii}"; //$NON-NLS-1$
	public static final String TV_HH = "{hh}"; //$NON-NLS-1$
	public static final String TV_JJJ = "{jjj}"; //$NON-NLS-1$
	public static final String TV_DD = "{dd}"; //$NON-NLS-1$
	public static final String TV_MONTH = "{month}"; //$NON-NLS-1$
	public static final String TV_WW = "{ww}"; //$NON-NLS-1$
	public static final String TV_MM = "{mm}"; //$NON-NLS-1$
	public static final String TV_YY = "{yy}"; //$NON-NLS-1$
	public static final String TV_YYYY = "{yyyy}"; //$NON-NLS-1$
	public static final String TV_SEQUENCE_NO5 = "{sequenceNo5}"; //$NON-NLS-1$
	public static final String TV_SEQUENCE_NO4 = "{sequenceNo4}"; //$NON-NLS-1$
	public static final String TV_SEQUENCE_NO3 = "{sequenceNo3}"; //$NON-NLS-1$
	public static final String TV_SEQUENCE_NO2 = "{sequenceNo2}"; //$NON-NLS-1$
	public static final String TV_SEQUENCE_NO1 = "{sequenceNo1}"; //$NON-NLS-1$
	public static final String TV_IMAGE_NO5 = "{imageNo5}"; //$NON-NLS-1$
	public static final String TV_IMAGE_NO4 = "{imageNo4}"; //$NON-NLS-1$
	public static final String TV_IMAGE_NO3 = "{imageNo3}"; //$NON-NLS-1$
	public static final String TV_IMAGE_NO2 = "{imageNo2}"; //$NON-NLS-1$
	public static final String TV_IMAGE_NO1 = "{imageNo1}"; //$NON-NLS-1$
	public static final String TV_IMPORT_NO5 = "{importNo5}"; //$NON-NLS-1$
	public static final String TV_IMPORT_NO4 = "{importNo4}"; //$NON-NLS-1$
	public static final String TV_IMPORT_NO3 = "{importNo3}"; //$NON-NLS-1$
	public static final String TV_IMPORT_NO2 = "{importNo2}"; //$NON-NLS-1$
	public static final String TV_IMPORT_NO1 = "{importNo1}"; //$NON-NLS-1$
	public static final String TV_EXTENSION = "{extension}"; //$NON-NLS-1$
	public static final String TV_FILENAME = "{filename}"; //$NON-NLS-1$
	public static final String TV_USER = "{user}"; //$NON-NLS-1$
	public static final String TV_OWNER = "{owner}"; //$NON-NLS-1$
	public static final String TV_CUE = "{cue}"; //$NON-NLS-1$
	public static final String TV_META = "{meta="; //$NON-NLS-1$
	public static final String[] TV_ALL = new String[] { TV_SS, TV_II, TV_HH, TV_JJJ, TV_DD, TV_MONTH, TV_WW, TV_MM,
			TV_YY, TV_YYYY, TV_SEQUENCE_NO5, TV_SEQUENCE_NO4, TV_SEQUENCE_NO3, TV_SEQUENCE_NO2, TV_SEQUENCE_NO1,
			TV_IMAGE_NO5, TV_IMAGE_NO4, TV_IMAGE_NO3, TV_IMAGE_NO2, TV_IMAGE_NO1, TV_IMPORT_NO5, TV_IMPORT_NO4,
			TV_IMPORT_NO3, TV_IMPORT_NO2, TV_IMPORT_NO1, TV_EXTENSION, TV_FILENAME, TV_USER, TV_OWNER, TV_CUE };
	public static final String[] TV_TRANSFER = new String[] { TV_SS, TV_II, TV_HH, TV_JJJ, TV_DD, TV_MONTH, TV_WW,
			TV_MM, TV_YY, TV_YYYY, TV_SEQUENCE_NO5, TV_SEQUENCE_NO4, TV_SEQUENCE_NO3, TV_SEQUENCE_NO2, TV_SEQUENCE_NO1,
			TV_IMAGE_NO5, TV_IMAGE_NO4, TV_IMAGE_NO3, TV_IMAGE_NO2, TV_IMAGE_NO1, TV_EXTENSION, TV_FILENAME, TV_USER,
			TV_OWNER, TV_CUE };
	public static final String[] TV_RENAME = new String[] { TV_SS, TV_II, TV_HH, TV_JJJ, TV_DD, TV_MONTH, TV_WW, TV_MM,
			TV_YY, TV_YYYY, TV_IMAGE_NO5, TV_IMAGE_NO4, TV_IMAGE_NO3, TV_IMAGE_NO2, TV_IMAGE_NO1, TV_EXTENSION,
			TV_FILENAME, TV_USER, TV_OWNER, TV_CUE };
	public static final String[] PI_ALL = new String[] { PI_TITLE, PI_NAME, PI_DESCRIPTION, PI_CREATIONDATE,
			PT_COLLECTION, PI_SEQUENCENO, PI_PAGEITEM, PI_SIZE, PI_FORMAT };
	public static final String[] TH_ALL = new String[] { Constants.PI_TITLE, Constants.PI_NAME,
			Constants.PI_FORMAT, Constants.PI_SIZE, Constants.PI_CREATIONDATE, TV_SS, TV_II, TV_HH, TV_JJJ, TV_DD, TV_MONTH, TV_WW, TV_MM,
			TV_YY, TV_YYYY };

	public final static String RAWIMPORT_ONLYRAW = "raw"; //$NON-NLS-1$
	public final static String RAWIMPORT_ONLYDNG = "dng"; //$NON-NLS-1$
	public final static String RAWIMPORT_DNGEMBEDDEDRAW = "embedded"; //$NON-NLS-1$
	public final static String RAWIMPORT_BOTH = "both"; //$NON-NLS-1$

	public final static int DERIVATIVES = 0;
	public final static int ORIGINALS = 1;
	public final static int COMPOSITES = 2;
	public final static int COMPONENTS = 3;

	public final static int SLIDE_NO_THUMBNAILS = 0;
	public final static int SLIDE_THUMBNAILS_LEFT = 1;
	public final static int SLIDE_THUMBNAILS_RIGHT = 2;
	public final static int SLIDE_THUMBNAILS_TOP = 3;
	public final static int SLIDE_THUMBNAILS_BOTTOM = 4;

	public final static int SLIDE_TRANSITION_EXPAND = -1;
	public final static int SLIDE_TRANSITION_FADE = 0;
	public final static int SLIDE_TRANSITION_MOVE_LEFT = 1;
	public final static int SLIDE_TRANSITION_MOVE_RIGHT = 2;
	public final static int SLIDE_TRANSITION_MOVE_UP = 3;
	public final static int SLIDE_TRANSITION_MOVE_DOWN = 4;
	public final static int SLIDE_TRANSITION_MOVE_TOPLEFT = 5;
	public final static int SLIDE_TRANSITION_MOVE_TOPRIGHT = 6;
	public final static int SLIDE_TRANSITION_MOVE_BOTTOMLEFT = 7;
	public final static int SLIDE_TRANSITION_MOVE_BOTTOMRIGHT = 8;
	public final static int SLIDE_TRANSITION_BLEND_LEFT = 9;
	public final static int SLIDE_TRANSITION_BLEND_RIGHT = 10;
	public final static int SLIDE_TRANSITION_BLEND_UP = 11;
	public final static int SLIDE_TRANSITION_BLEND_DOWN = 12;
	public final static int SLIDE_TRANSITION_BLEND_TOPLEFT = 13;
	public final static int SLIDE_TRANSITION_BLEND_TOPRIGHT = 14;
	public final static int SLIDE_TRANSITION_BLEND_BOTTOMLEFT = 15;
	public final static int SLIDE_TRANSITION_BLEND_BOTTOMRIGHT = 16;
	public final static int SLIDE_TRANSITION_RANDOM = 17;
	public final static int SLIDE_TRANSITION_START = SLIDE_TRANSITION_EXPAND;
	public final static int SLIDE_TRANSITION_N = SLIDE_TRANSITION_RANDOM - SLIDE_TRANSITION_START;

	public static final int SLIDE_TITLEONLY = 0;
	public static final int SLIDE_SNOONLY = 1;
	public static final int SLIDE_TITLE_SNO = 2;

	public static final FileNameExtensionFilter SOUNDFILEFILTER = new FileNameExtensionFilter(
			new String[] { "aif", "aifc", "au", "snd", "wav" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	public static final String[] SupportedSoundFileExtensions = new String[] { "*.aif;*.aifc;*.au;*.snd;*.wav", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

	public static final String[] SupportedSoundFileNames = new String[] {
			Messages.Constants_supported_sound_files + " (*.aif,*.aifc,*.au,*.snd,*.wav)", //$NON-NLS-1$
			Messages.Constants_all_files + " (*.*)" }; //$NON-NLS-1$

	// Duplicates
	public static final int DUPES_BYEXPOSUREDATA = 1;
	public static final int DUPES_BYSIMILARITY = 2;
	public static final int DUPES_COMBINED = 3;
	public static final int DUPES_BYFILENAME = 40;

	// JOBS
	public static final String DAEMONS = BatchConstants.DAEMONS;
	public static final String CATALOG = "com.bdaum.zoom.catalog"; //$NON-NLS-1$
	public static final String FOLDERWATCH = "com.bdaum.zoom.folderwatch"; //$NON-NLS-1$
	public static final String SYNCPICASA = "com.bdaum.zoom.syncPicasa"; //$NON-NLS-1$
	public static final String OPERATIONJOBFAMILY = "com.bdaum.zoom.operation"; //$NON-NLS-1$
	public static final String UPLOADJOBFAMILY = "com.bdaum.zoom.upload"; //$NON-NLS-1$
	public static final String SLIDESHOW = "com.bdaum.zoom.slideshow"; //$NON-NLS-1$
	public static final String INDEXING = "com.bdaum.zoom.indexing"; //$NON-NLS-1$
	public static final String PROPERTYPROVIDER = "com.bdaum.zoom.propertyProvider"; //$NON-NLS-1$
	public static final String SPELLING = "com.bdaum.zoom.spelling"; //$NON-NLS-1$
	public static final String UPDATING = "com.bdaum.zoom.updating"; //$NON-NLS-1$
	public static final String CRITICAL = "com.bdaum.zoom.critical"; //$NON-NLS-1$
	public static final String FILETRANSFER = "com.bdaum.zoom.fileTransfer"; //$NON-NLS-1$

	// Merge duplicates
	public static final int SKIP = 0;
	public static final int REPLACE = 1;
	public static final int MERGE = 2;
	// PDF resolution
	public static final int SCREEN_QUALITY = 0;
	public static final int PRINT_QUALITY = 1;

	// Scanners
	public static final int PICASASCANNERVERSION = 0;

	// Skipping
	public static final int SKIP_NONE = 0;
	public static final int SKIP_RAW = 1;
	public static final int SKIP_RAW_IF_JPEG = 2;
	public static final int SKIP_JPEG = 3;
	public static final int SKIP_JPEG_IF_RAW = 4;

	// Exhibition
	public static final int EXHLABEL_TIT_DES_CRED = 0;
	public static final int EXHLABEL_TIT_CRED_DES = 1;
	public static final int EXHLABEL_CRED_TIT_DES = 2;
	// Exhibition labels
	public static final int DEFAULTLABELDISTANCE = 50;
	public static final int DEFAULTLABELINDENT = 50;
	public static final int DEFAULTLABELALIGNMENT = 8;

	// Backup path
	public static final String DATEVAR = "{date}"; //$NON-NLS-1$
	public static final String LOCVAR = "{catalogLocation}"; //$NON-NLS-1$
	public static final String BACKUPEXT = "backup"; //$NON-NLS-1$

	// Export
	public static final int FTP = 1;
	public static final int FILE = 0;
	public static final String BY_NONE = "none"; //$NON-NLS-1$
	public static final String BY_TIMELINE = "timeline"; //$NON-NLS-1$
	public static final String BY_NUM_TIMELINE = "numtimeline"; //$NON-NLS-1$
	public static final String BY_RATING = "rating"; //$NON-NLS-1$
	public static final String BY_CATEGORY = "category"; //$NON-NLS-1$
	public static final String BY_STATE = "state"; //$NON-NLS-1$
	public static final String BY_DATE = "date"; //$NON-NLS-1$
	public static final String BY_TIME = "time"; //$NON-NLS-1$
	public static final String BY_JOB = "order"; //$NON-NLS-1$
	public static final String BY_EVENT = "event"; //$NON-NLS-1$

	// Stacking
	public static final String STACK = "STACK#"; //$NON-NLS-1$
	// Labels
	public static final int INHERIT_LABEL = 0;
	public static final int TITLE_LABEL = 1;
	public static final int NO_LABEL = 2;
	public static final int CUSTOM_LABEL = 3;
	// system properties
	public static final String PROP_CATACCESS = "com.bdaum.zoom.cat.access"; //$NON-NLS-1$
	public static final String PROP_CATACCESS_NONE = "none"; //$NON-NLS-1$
	public static final String PROP_CATACCESS_READ = "read"; //$NON-NLS-1$
	public static final String PROP_CATACCESS_WRITE = "write"; //$NON-NLS-1$
}
