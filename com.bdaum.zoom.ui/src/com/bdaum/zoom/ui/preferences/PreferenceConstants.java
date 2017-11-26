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

package com.bdaum.zoom.ui.preferences;

public interface PreferenceConstants {
	// Color scheme
	String BACKGROUNDCOLOR = "com.bdaum.zoom.backgroundColor"; //$NON-NLS-1$
	String BACKGROUNDCOLOR_BLACK = "b"; //$NON-NLS-1$
	String BACKGROUNDCOLOR_DARKGREY = "d"; //$NON-NLS-1$
	String BACKGROUNDCOLOR_GREY = "g"; //$NON-NLS-1$
	String BACKGROUNDCOLOR_WHITE = "w"; //$NON-NLS-1$
	String BACKGROUNDCOLOR_PLATFORM = "p"; //$NON-NLS-1$
	
	// Appearance
	String SHOWCOLORCODE = "com.bdaum.zoom.showColorCode"; //$NON-NLS-1$
	String COLORCODE_NO = "false"; //$NON-NLS-1$
	String COLORCODE_AUTO = "auto"; //$NON-NLS-1$
	String COLORCODE_MANUAL = "true"; //$NON-NLS-1$
	String AUTOCOLORCODECRIT = "com.bdaum.zoom.autoColorCodeCrit"; //$NON-NLS-1$
	String SHOWRATING = "com.bdaum.zoom.showRating"; //$NON-NLS-1$
	String SHOWRATING_NO = "false"; //$NON-NLS-1$
	String SHOWRATING_SIZE = "true"; //$NON-NLS-1$
	String SHOWRATING_COUNT = "count"; //$NON-NLS-1$
	String SHOWLOCATION =  "com.bdaum.zoom.showLocation"; //$NON-NLS-1$
	String SHOWROTATEBUTTONS = "com.bdaum.zoom.showRotateButtons"; //$NON-NLS-1$
	String SHOWVOICENOTE = "com.bdaum.zoom.showVoiceNote"; //$NON-NLS-1$
	String SHOWDONEMARK = "com.bdaum.zoom.showDoneMark"; //$NON-NLS-1$
	String SHOWEXPANDCOLLAPSE = "com.bdaum.zoom.showExpandCollapse"; //$NON-NLS-1$
	String SHOWLABEL = "com.bdaum.zoom.showLabel"; //$NON-NLS-1$
	String THUMBNAILTEMPLATE  = "com.bdaum.zoom.thumbnailTemplate"; //$NON-NLS-1$
	String LABELFONTSIZE ="com.bdaum.zoom.labelFontSize"; //$NON-NLS-1$

	// Mouse
	String MOUSE_SPEED = "com.bdaum.zoom.mouseSpeed"; //$NON-NLS-1$
	String ZOOMKEY = "com.bdaum.zoom.zoomKey"; //$NON-NLS-1$
	int ZOOMALT = 0;
	int ZOOMSHIFT = 1;
	int ZOOMRIGHT = 2;
	int NOZOOM = 3;
	String WHEELKEY = "com.bdaum.zoom.wheelKey"; //$NON-NLS-1$
	int WHEELSHIFTPANS = 0;
	int WHEELALTPANS = 1;
	int WHEELSHIFTZOOMS = 2;
	int WHEELALTZOOMS = 3;
	int WHEELZOOMONLY = 4;
	int WHEELSCROLLONLY = 5;
	String WHEELSOFTNESS = "com.bdaum.zoom.wheelSoftness"; //$NON-NLS-1$

	// Raw import
	String RAWIMPORT = "com.bdaum.zoom.rawImport"; //$NON-NLS-1$
	String DNGCONVERTERPATH = "com.bdaum.zoom.dngConverterPath"; //$NON-NLS-1$
	String DNGUNCOMPRESSED = "com.bdaum.zoom.dngUncompressed"; //$NON-NLS-1$
	String DNGLINEAR = "com.bdaum.zoom.dngLinear"; //$NON-NLS-1$
	String RECIPEDETECTORS = "com.bdaum.zoom.recipeDetectors"; //$NON-NLS-1$
	String RECIPEDETECTORCONFIGURATIONS = "com.bdaum.zoom.recipeDetectorConfigurations"; //$NON-NLS-1$
	String PROCESSRECIPES = "com.bdaum.zoom.processRecipes"; //$NON-NLS-1$
	String DNGFOLDER = "com.bdaum.zoom.dngFolder"; //$NON-NLS-1$
	
	// Import
	String KEYWORDFILTER = "com.bdaum.zoom.keywordFilter"; //$NON-NLS-1$
	String DERIVERELATIONS = "com.bdaum.zoom.deriveRelations"; //$NON-NLS-1$
	String AUTODERIVE = "com.bdaum.zoom.autoDerive"; //$NON-NLS-1$
	String APPLYXMPTODERIVATES = "com.bdaum.zoom.applyXmpToDerivates"; //$NON-NLS-1$
	String IMPORTMAKERNOTES = "com.bdaum.zoom.importMakerNotes"; //$NON-NLS-1$
	String MAXIMPORTS = "com.bdaum.zoom.maxImports"; //$NON-NLS-1$
	String DEVICEWATCH = "com.bdaum.zoom.deviceWatch"; //$NON-NLS-1$
	String ARCHIVERECIPES = "com.bdaum.zoom.archiveRecipes"; //$NON-NLS-1$
	String SHOWIMPORTED = "com.bdaum.zoom.showImported"; //$NON-NLS-1$
	String RELATIONDETECTORS = "com.bdaum.zoom.relationDetectors"; //$NON-NLS-1$
	String AUTORULES = "com.bdaum.zoom.auto_rules"; //$NON-NLS-1$

	// External programs
	String FILEASSOCIATION = "com.bdaum.zoom.fileAssociations"; //$NON-NLS-1$
	String AUTOEXPORT = "com.bdaum.zoom.autoExport"; //$NON-NLS-1$
	String EXTERNALVIEWER = "com.bdaum.zoom.externalViewer"; //$NON-NLS-1$
	String EXTERNALMEDIAVIEWER = "com.bdaum.zoom.externalMediaViewer#"; //$NON-NLS-1$
	String PREVIEW = "com.bdaum.zoom.preview"; //$NON-NLS-1$

	// Sound and Audio
	String ALARMONFINISH = "com.bdaum.zoom.alarmOnFinish"; //$NON-NLS-1$
	String ALARMONPROMPT = "com.bdaum.zoom.alarmOnPrompt"; //$NON-NLS-1$
	String AUDIOSAMPLINGRATE = "com.bdaum.zoom.audioSamplingRate"; //$NON-NLS-1$
	String AUDIOBITDEPTH = "com.bdaum.zoom.audioBitDepth"; //$NON-NLS-1$
	int AUDIO16BIT = 16;
	int AUDIO8BIT = 8;
	double AUDIO11KHZ = 11025;
	double AUDIO22KHZ = 22050;
	double AUDIO44KHZ = 44100;
	
	// Metadata
	String ESSENTIALMETADATA = "com.bdaum.zoom.essentialMetadata"; //$NON-NLS-1$
	String HOVERMETADATA = "com.bdaum.zoom.hoverMetadata"; //$NON-NLS-1$
	String METADATATOLERANCES = "com.bdaum.zoom.metaDataTolerances"; //$NON-NLS-1$
	String EXPORTMETADATA = "com.bdaum.zoom.metaDataExport"; //$NON-NLS-1$
	String JPEGMETADATA = "com.bdaum.zoom.jpegMetaExport"; //$NON-NLS-1$

	// Watched folders
	String WATCHFILTER = "com.bdaum.zoom.watchFilter"; //$NON-NLS-1$
	// Update
	String UPDATEPOLICY = "com.bdaum.zoom.updatePolicy"; //$NON-NLS-1$
	String UPDATEPOLICY_WITHBACKUP = "withBackup"; //$NON-NLS-1$
	String UPDATEPOLICY_ONSTART = "onStart"; //$NON-NLS-1$
	String UPDATEPOLICY_MANUAL = "manual"; //$NON-NLS-1$
	// General
	String INACTIVITYINTERVAL = "com.bdaum.zoom.inactivityInterval"; //$NON-NLS-1$
	String BACKUPINTERVAL = "com.bdaum.zoom.backupInterval"; //$NON-NLS-1$
	String BACKUPGENERATIONS = "com.bdaum.zoom.backupGenerations"; //$NON-NLS-1$
	String UNDOLEVELS = "com.bdaum.zoom.undoLevels"; //$NON-NLS-1$
	String LASTPLUGINSCAN = "com.bdaum.zoom.lastPluginScan"; //$NON-NLS-1$
	String NOPROGRESS = "com.bdaum.zoom.noProgress"; //$NON-NLS-1$
	String ADVANCEDGRAPHICS = "com.bdaum.zoom.advancedGraphics"; //$NON-NLS-1$
	String COLORPROFILE = "com.bdaum.zoom.colorProfile"; //$NON-NLS-1$
	String BWFILTER = "com.bdaum.zoom.bwFilter"; //$NON-NLS-1$
	String TABLECOLUMNS = "com.bdaum.zoom.tableColumns"; //$NON-NLS-1$
	String ADDNOISE = "com.bdaum.zoom.addNoise"; //$NON-NLS-1$;
	String ENLARGESMALL = "com.bdaum.zoom.enlargeSmall"; //$NON-NLS-1$
	String SECONDARYMONITOR = "com.bdaum.zoom.secondaryMonitor"; //$NON-NLS-1$

	// Faces
	String MAXREGIONS = "com.bdaum.zoom.maxRegions"; //$NON-NLS-1$
	
	// Application
	String HIDE_MENU_BAR = "hide_menu_bar"; //$NON-NLS-1$
	String HIDE_STATUS_BAR = "hide_status_bar"; //$NON-NLS-1$
	String TRAY_MODE = "tray_mode"; //$NON-NLS-1$
	String FORCEDELETETRASH = "force_delete_trash"; //$NON-NLS-1$
	String TRAY_PROMPT = "prompt"; //$NON-NLS-1$
	String TRAY_TRAY = "true"; //$NON-NLS-1$
	String TRAY_DESK = "false"; //$NON-NLS-1$

}
