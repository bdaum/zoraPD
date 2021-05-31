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
 * (c) 2009-2018 Berthold Daum  
 */

package com.bdaum.zoom.ui.preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.bdaum.zoom.common.PreferenceRegistry;
import com.bdaum.zoom.common.PreferenceRegistry.IPreferenceConstants;
import com.bdaum.zoom.ui.internal.UiActivator;

public class PreferenceConstants implements IPreferenceConstants {

	// Color scheme
	public static final String BACKGROUNDCOLOR = "com.bdaum.zoom.backgroundColor"; //$NON-NLS-1$
	public static final String BACKGROUNDCOLOR_BLACK = "b"; //$NON-NLS-1$
	public static final String BACKGROUNDCOLOR_DARKGREY = "d"; //$NON-NLS-1$
	public static final String BACKGROUNDCOLOR_GREY = "g"; //$NON-NLS-1$
	public static final String BACKGROUNDCOLOR_WHITE = "w"; //$NON-NLS-1$
	public static final String BACKGROUNDCOLOR_PLATFORM = "p"; //$NON-NLS-1$

	// Appearance
	public static final String SHOWCOLORCODE = "com.bdaum.zoom.showColorCode"; //$NON-NLS-1$
	public static final String COLORCODE_NO = "false"; //$NON-NLS-1$
	public static final String COLORCODE_AUTO = "auto"; //$NON-NLS-1$
	public static final String COLORCODE_MANUAL = "true"; //$NON-NLS-1$
	public static final String AUTOCOLORCODECRIT = "com.bdaum.zoom.autoColorCodeCrit"; //$NON-NLS-1$
	public static final String SHOWRATING = "com.bdaum.zoom.showRating"; //$NON-NLS-1$
	public static final String SHOWRATING_NO = "false"; //$NON-NLS-1$
	public static final String SHOWRATING_SIZE = "true"; //$NON-NLS-1$
	public static final String SHOWRATING_COUNT = "count"; //$NON-NLS-1$
	public static final String SHOWLOCATION = "com.bdaum.zoom.showLocation"; //$NON-NLS-1$
	public static final String SHOWROTATEBUTTONS = "com.bdaum.zoom.showRotateButtons"; //$NON-NLS-1$
	public static final String SHOWVOICENOTE = "com.bdaum.zoom.showVoiceNote"; //$NON-NLS-1$
	public static final String SHOWDONEMARK = "com.bdaum.zoom.showDoneMark"; //$NON-NLS-1$
	public static final String SHOWEXPANDCOLLAPSE = "com.bdaum.zoom.showExpandCollapse"; //$NON-NLS-1$
	public static final String SHOWLABEL = "com.bdaum.zoom.showLabel"; //$NON-NLS-1$
	public static final String THUMBNAILTEMPLATE = "com.bdaum.zoom.thumbnailTemplate"; //$NON-NLS-1$
	public static final String LABELFONTSIZE = "com.bdaum.zoom.labelFontSize"; //$NON-NLS-1$
	public static final String LABELALIGNMENT = "com.bdaum.zoom.labelAlignment"; //$NON-NLS-1$
	public static final String DISTANCEUNIT = "com.bdaum.zoom.distanceUnit"; //$NON-NLS-1$
	public static final String DIMUNIT = "com.bdaum.zoom.dimUnit"; //$NON-NLS-1$
	public static final String HOVERBASETIME = "com.bdaum.zoom.hoverBaseTime"; //$NON-NLS-1$
	public static final String HOVERCHARTIME = "com.bdaum.zoom.hoverCharTime"; //$NON-NLS-1$
	public static final String HOVERDELAY = "com.bdaum.zoom.hoverDelay"; //$NON-NLS-1$

	// Mouse
	public static final String MOUSE_SPEED = "com.bdaum.zoom.mouseSpeed"; //$NON-NLS-1$
	public static final String ZOOMKEY = "com.bdaum.zoom.zoomKey"; //$NON-NLS-1$
	public static final int ZOOMALT = 0;
	public static final int ZOOMSHIFT = 1;
	public static final int ZOOMRIGHT = 2;
	public static final int NOZOOM = 3;
	public static final String WHEELSOFTNESS = "com.bdaum.zoom.wheelSoftness"; //$NON-NLS-1$

	// Raw import
	public static final String RAWIMPORT = "com.bdaum.zoom.rawImport"; //$NON-NLS-1$
	public static final String DNGCONVERTERPATH = "com.bdaum.zoom.dngConverterPath"; //$NON-NLS-1$
	public static final String DNGUNCOMPRESSED = "com.bdaum.zoom.dngUncompressed"; //$NON-NLS-1$
	public static final String DNGLINEAR = "com.bdaum.zoom.dngLinear"; //$NON-NLS-1$
	public static final String RECIPEDETECTORS = "com.bdaum.zoom.recipeDetectors"; //$NON-NLS-1$
	public static final String RECIPEDETECTORCONFIGURATIONS = "com.bdaum.zoom.recipeDetectorConfigurations"; //$NON-NLS-1$
	public static final String PROCESSRECIPES = "com.bdaum.zoom.processRecipes"; //$NON-NLS-1$
	public static final String DNGFOLDER = "com.bdaum.zoom.dngFolder"; //$NON-NLS-1$

	// Import
	public static final String KEYWORDFILTER = "com.bdaum.zoom.keywordFilter"; //$NON-NLS-1$
	public static final String DERIVERELATIONS = "com.bdaum.zoom.deriveRelations"; //$NON-NLS-1$
	public static final String AUTODERIVE = "com.bdaum.zoom.autoDerive"; //$NON-NLS-1$
	public static final String APPLYXMPTODERIVATES = "com.bdaum.zoom.applyXmpToDerivates"; //$NON-NLS-1$
	public static final String IMPORTMAKERNOTES = "com.bdaum.zoom.importMakerNotes"; //$NON-NLS-1$
	public static final String IMPORTFACEDATA = "com.bdaum.zoom.importFaceData"; //$NON-NLS-1$
	public static final String MAXIMPORTS = "com.bdaum.zoom.maxImports"; //$NON-NLS-1$
	public static final String DEVICEWATCH = "com.bdaum.zoom.deviceWatch"; //$NON-NLS-1$
	public static final String TETHEREDSHOW = "com.bdaum.zoom.tetheredShow"; //$NON-NLS-1$
	public static final int TETHEREDSHOW_NO = 0;
	public static final int TETHEREDSHOW_INTERN = 1;
	public static final int TETHEREDSHOW_EXTERN = 2;
	public static final String TETHERED = "tethered"; //$NON-NLS-1$
	public static final String ARCHIVERECIPES = "com.bdaum.zoom.archiveRecipes"; //$NON-NLS-1$
	public static final String SHOWIMPORTED = "com.bdaum.zoom.showImported"; //$NON-NLS-1$
	public static final String RELATIONDETECTORS = "com.bdaum.zoom.relationDetectors"; //$NON-NLS-1$
	public static final String AUTORULES = "com.bdaum.zoom.auto_rules"; //$NON-NLS-1$

	// External programs
	public static final String FILEASSOCIATION = "com.bdaum.zoom.fileAssociations"; //$NON-NLS-1$
	public static final String AUTOEXPORT = "com.bdaum.zoom.autoExport"; //$NON-NLS-1$
	public static final String EXTERNALVIEWER = "com.bdaum.zoom.externalViewer"; //$NON-NLS-1$
	public static final String EXTERNALMEDIAVIEWER = "com.bdaum.zoom.externalMediaViewer#"; //$NON-NLS-1$
	public static final String PREVIEW = "com.bdaum.zoom.preview"; //$NON-NLS-1$

	// Sound and Audio
	public static final String ALARMONFINISH = "com.bdaum.zoom.alarmOnFinish"; //$NON-NLS-1$
	public static final String ALARMONPROMPT = "com.bdaum.zoom.alarmOnPrompt"; //$NON-NLS-1$
	public static final String AUDIOSAMPLINGRATE = "com.bdaum.zoom.audioSamplingRate"; //$NON-NLS-1$
	public static final String AUDIOBITDEPTH = "com.bdaum.zoom.audioBitDepth"; //$NON-NLS-1$
	public static final int AUDIO16BIT = 16;
	public static final int AUDIO8BIT = 8;
	public static final double AUDIO11KHZ = 11025;
	public static final double AUDIO22KHZ = 22050;
	public static final double AUDIO44KHZ = 44100;

	// Metadata
	public static final String ESSENTIALMETADATA = "com.bdaum.zoom.essentialMetadata"; //$NON-NLS-1$
	public static final String HOVERMETADATA = "com.bdaum.zoom.hoverMetadata"; //$NON-NLS-1$
	public static final String METADATATUNING = "com.bdaum.zoom.metaDataTuning"; //$NON-NLS-1$
	public static final String METADATATOLERANCES = "com.bdaum.zoom.metaDataTolerances"; //$NON-NLS-1$
	public static final String EXPORTMETADATA = "com.bdaum.zoom.metaDataExport"; //$NON-NLS-1$
	public static final String JPEGMETADATA = "com.bdaum.zoom.jpegMetaExport"; //$NON-NLS-1$

	// Watched folders
	public static final String WATCHFILTER = "com.bdaum.zoom.watchFilter"; //$NON-NLS-1$

	// Update
	public static final String UPDATEPOLICY = "com.bdaum.zoom.updatePolicy"; //$NON-NLS-1$
	public static final String UPDATEPOLICY_WITHBACKUP = "withBackup"; //$NON-NLS-1$
	public static final String UPDATEPOLICY_ONSTART = "onStart"; //$NON-NLS-1$
	public static final String UPDATEPOLICY_MANUAL = "manual"; //$NON-NLS-1$

	// General
	public static final String INACTIVITYINTERVAL = "com.bdaum.zoom.inactivityInterval"; //$NON-NLS-1$
	public static final String BACKUPINTERVAL = "com.bdaum.zoom.backupInterval"; //$NON-NLS-1$
	public static final String NOBACKUP = "com.bdaum.zoom.noBackup"; //$NON-NLS-1$
	public static final String BACKUPGENERATIONS = "com.bdaum.zoom.backupGenerations"; //$NON-NLS-1$
	public static final String UNDOLEVELS = "com.bdaum.zoom.undoLevels"; //$NON-NLS-1$
	public static final String LASTPLUGINSCAN = "com.bdaum.zoom.lastPluginScan"; //$NON-NLS-1$
	public static final String NOPROGRESS = "com.bdaum.zoom.noProgress"; //$NON-NLS-1$
	public static final String ADVANCEDGRAPHICS = "com.bdaum.zoom.advancedGraphics"; //$NON-NLS-1$
	public static final String ACCELERATION = "com.bdaum.zoom.acceleration"; //$NON-NLS-1$
	public static final String ALWAYSHIGHRES = "com.bdaum.zoom.alwaysHighRes"; //$NON-NLS-1$
	public static final String COLORPROFILE = "com.bdaum.zoom.colorProfile"; //$NON-NLS-1$
	public static final String CUSTOMPROFILE = "com.bdaum.zoom.customProfile"; //$NON-NLS-1$
	public static final String BWFILTER = "com.bdaum.zoom.bwFilter"; //$NON-NLS-1$
	public static final String TABLECOLUMNS = "com.bdaum.zoom.tableColumns"; //$NON-NLS-1$

	public static final String ENLARGESMALL = "com.bdaum.zoom.enlargeSmall"; //$NON-NLS-1$
	public static final String SECONDARYMONITOR = "com.bdaum.zoom.secondaryMonitor"; //$NON-NLS-1$
	public static final String MON_ALTERNATE = "alternate"; //$NON-NLS-1$
	public static final String NOPROCESSORS = "com.bdaum.zoom.noProcessors"; //$NON-NLS-1$

	// Faces
	public static final String MAXREGIONS = "com.bdaum.zoom.maxRegions"; //$NON-NLS-1$

	// Application
	public static final String HIDE_MENU_BAR = "hide_menu_bar"; //$NON-NLS-1$
	public static final String HIDE_STATUS_BAR = "hide_status_bar"; //$NON-NLS-1$
	public static final String TRAY_MODE = "tray_mode"; //$NON-NLS-1$
	public static final String FORCEDELETETRASH = "force_delete_trash"; //$NON-NLS-1$
	public static final String TRAY_PROMPT = "prompt"; //$NON-NLS-1$
	public static final String TRAY_TRAY = "true"; //$NON-NLS-1$
	public static final String TRAY_DESK = "false"; //$NON-NLS-1$
	// Vocab
	public static final String V_GENRE = "com.bdaum.zoom.v_genre"; //$NON-NLS-1$
	public static final String V_SCENE = "com.bdaum.zoom.v_scene"; //$NON-NLS-1$
	public static final String V_SUBJECT = "com.bdaum.zoom.v_subject"; //$NON-NLS-1$

	public static final String APPLICATION_GROUP = Messages.PreferenceConstants_application;
	public static final String APPEARANCE_GROUP = Messages.PreferenceConstants_appearance;
	public static final String IMPORT_GROUP = Messages.PreferenceConstants_import;
	
	public static final String AUDIO_GROUP = Messages.PreferenceConstants_audio;
	public static final String METADATA_GROUP = Messages.PreferenceConstants_metadata;

	@Override
	public List<Object> getRootElements() {
		return Arrays.asList(PreferenceRegistry.UI, PreferenceRegistry.PROCESSING);
	}

	@SuppressWarnings("restriction")
	@Override
	public List<Object> getChildren(Object group) {
		if (group == PreferenceRegistry.UI)
			return Arrays.asList(APPEARANCE_GROUP, APPLICATION_GROUP, AUDIO_GROUP);
		if (group == PreferenceRegistry.PROCESSING)
			return Arrays.asList(IMPORT_GROUP, PreferenceRegistry.EXTERNAL_GROUP, METADATA_GROUP);
		if (group == APPEARANCE_GROUP)
			return Arrays.asList(BACKGROUNDCOLOR, SHOWCOLORCODE, AUTOCOLORCODECRIT, SHOWRATING, SHOWLOCATION,
					SHOWROTATEBUTTONS, SHOWVOICENOTE, SHOWDONEMARK, SHOWEXPANDCOLLAPSE, SHOWLABEL, THUMBNAILTEMPLATE,
					LABELFONTSIZE, LABELALIGNMENT, DISTANCEUNIT, DIMUNIT, HOVERBASETIME, HOVERCHARTIME, HOVERDELAY,
					MAXREGIONS, NOPROGRESS, ALWAYSHIGHRES, COLORPROFILE, CUSTOMPROFILE,
					BWFILTER, TABLECOLUMNS, ENLARGESMALL, SECONDARYMONITOR, MON_ALTERNATE);
		if (group == APPLICATION_GROUP)
			return Arrays.asList(HIDE_MENU_BAR, HIDE_STATUS_BAR, TRAY_MODE, FORCEDELETETRASH, UPDATEPOLICY, MOUSE_SPEED,
					ZOOMKEY, WHEELSOFTNESS, INACTIVITYINTERVAL, BACKUPINTERVAL, NOBACKUP, BACKUPGENERATIONS,
					UNDOLEVELS, ADVANCEDGRAPHICS, ACCELERATION, NOPROCESSORS);
		if (group == AUDIO_GROUP)
			return Arrays.asList(ALARMONFINISH, ALARMONPROMPT, AUDIOSAMPLINGRATE, AUDIOBITDEPTH);
		if (group == IMPORT_GROUP)
			return Arrays.asList(RAWIMPORT, DNGUNCOMPRESSED, DNGLINEAR, 
					DNGFOLDER, KEYWORDFILTER, DERIVERELATIONS, AUTODERIVE, APPLYXMPTODERIVATES,
					IMPORTMAKERNOTES, IMPORTFACEDATA, MAXIMPORTS, DEVICEWATCH, TETHEREDSHOW, ARCHIVERECIPES,
					SHOWIMPORTED, RELATIONDETECTORS, AUTORULES, WATCHFILTER);
		if (group == com.bdaum.zoom.batch.internal.PreferenceConstants.RAW)
			return Arrays.asList(PROCESSRECIPES,RECIPEDETECTORS, RECIPEDETECTORCONFIGURATIONS);
		if (group == PreferenceRegistry.EXTERNAL_GROUP)
			return Arrays.asList(DNGCONVERTERPATH, FILEASSOCIATION, AUTOEXPORT, EXTERNALVIEWER, EXTERNALMEDIAVIEWER,
					PREVIEW);
		if (group == METADATA_GROUP)
			return Arrays.asList(ESSENTIALMETADATA, HOVERMETADATA, METADATATUNING, METADATATOLERANCES, EXPORTMETADATA,
					JPEGMETADATA, V_GENRE, V_SCENE, V_SUBJECT);
		return Collections.emptyList();
	}

	@Override
	public IEclipsePreferences getNode() {
		return InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID);
	}

}
