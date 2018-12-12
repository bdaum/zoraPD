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

package com.bdaum.zoom.ui.internal;

public interface HelpContextIds extends IHelpContexts {
	//	public static final String PREFIX = UiActivator.PLUGIN_ID + "."; //$NON-NLS-1$
	public static final String PREFIX = "com.bdaum.zoom.doc.";//$NON-NLS-1$

	/* Pages */
	public static final String FILE_ASSOCIATIONS_PREFERENCE_PAGE = PREFIX
			+ "fileAssociations" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String APPEARANCE_PREFERENCE_PAGE = PREFIX
			+ "appearance" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String AUDIO_PREFERENCE_PAGE = PREFIX
			+ "audio" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String GENERAL_PREFERENCE_PAGE = PREFIX
			+ "general" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String IMPORT_PREFERENCE_PAGE = PREFIX
			+ "import" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String KEYWORD_PREFERENCE_PAGE = PREFIX
			+ "keyword" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String METADATA_PREFERENCE_PAGE = PREFIX
			+ "metadata" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String PERSPECTIVE_PREFERENCE_PAGE = PREFIX
			+ "perspectives" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String SIMILARITY_PREFERENCE_PAGE = PREFIX
			+ "similarity" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String KEY_PREFERENCE_PAGE = PREFIX
			+ "keys" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String MOUSE_PREFERENCE_PAGE = PREFIX
			+ "mouse" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String FACE_PREFERENCE_PAGE = PREFIX
			+ "face" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String CAT_PREFERENCE_PAGE =  PREFIX
			+ "catalog" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String AUTO_PREFERENCE_PAGE = PREFIX
			+ "auto" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String VOCAB_PREFERENCE_PAGE = PREFIX
			+ "vocab" + PAGE_POSTFIX; //$NON-NLS-1$


	/* Dialogs */
	public static final String CAT_PROPERTIES_DIALOG = PREFIX
			+ "catProperties" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String NEWCAT_PROPERTIES_DIALOG = PREFIX
			+ "newcatProperties" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String CATEGORY_DIALOG = PREFIX
			+ "category" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String SUPPLEMENTALCATEGORY_DIALOG = PREFIX
			+ "supplementalCategories" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String COLLECTION_EDIT_DIALOG = PREFIX
			+ "collectionEdit" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String DESCRIPTION_DIALOG = PREFIX
			+ "description" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String EDITWALL_DIALOG = PREFIX
			+ "editWall" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String WEBGALLERY_DIALOG = PREFIX
			+ "editWebGallery" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String EXHIBITION_DIALOG = PREFIX
			+ "exhibition" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String IMPORT_FROM_DEVICE_DIALOG = PREFIX
			+ "importFromDevice" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String IMPORT_NEW_STRUCTURE_DIALOG = PREFIX
			+ "importNewStructure" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String KEYWORD_DIALOG = PREFIX
			+ "keyword" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String PRINTLAYOUT_DIALOG = PREFIX
			+ "printLayout" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String SLIDESHOW_DIALOG = PREFIX
			+ "slideshow" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String SLIDE_DIALOG = PREFIX + "slide" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String SECTIONBREAK_DIALOG = PREFIX
			+ "sectionbreak" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String REFRESH_DIALOG = PREFIX
			+ "refresh" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String TEMPLATE_DIALOG = PREFIX
			+ "template" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String PASTEMETA_DIALOG = PREFIX
			+ "pasteMetadata" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String EDITWEBEXHIBIT_DIALOG = PREFIX
			+ "webExhibit" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String EDITSTORYBOARD_DIALOG = PREFIX
			+ "storyboard" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String EDIT_FTP_DIALOG = PREFIX
			+ "ftpAccount" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String KEYWORD_SEARCH_DIALOG = PREFIX
			+ "keywordSearch" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String TIMESEARCH_DIALOG = PREFIX
			+ "timeSearch" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String DUPLICATES_DIALOG = PREFIX
			+ "duplicatesSearch" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String SERIES_DIALOG = PREFIX
			+ "seriesSearch" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String SPLITCAT = PREFIX + "splitCat" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String COLUMNS_DIALOG = PREFIX
			+ "configureColumns" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String TIMEHSHIFT_DIALOG = PREFIX
			+ "timeShift" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String FILTERDIALOG = PREFIX
			+ "catFilter" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String RETARGET_DIALOG = PREFIX
			+ "retarget" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String CONFLICT_DIALOG = PREFIX
			+ "conflict" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String COLLAPSEPATTERN_DIALOG = PREFIX
			+ "collapsePattern" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String EXHIBITLAYOUT_DIALOG = PREFIX
			+ "exhibitLayout" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String CLUSTER_DIALOG = PREFIX
			+ "cluster" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String RENAME_DIALOG = PREFIX
			+ "rename" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String ARCHIVE_DIALOG = PREFIX
			+ "archive" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String MIGRATE_DIALOG = PREFIX
			+ "migrate" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String REGEX_DIALOG = PREFIX
			+ "regex" + DIALOG_POSTFIX; //$NON-NLS-1$


	public static final String CATEGORIZE_DIALOG = PREFIX
			+ "categorize" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String GROUP_DIALOG = PREFIX
			+ "group" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String AUTORATING_DIALOG = PREFIX
			+ "autoRating" + DIALOG_POSTFIX; //$NON-NLS-1$
	

	public static final String IMPORTMODE_DIALOG  = PREFIX
			+ "importMode" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String TETHERED_DIALOG  = PREFIX
			+ "tethered" + DIALOG_POSTFIX; //$NON-NLS-1$

	/* Views */
	public static final String LIGHTBOX_VIEW = PREFIX
			+ "lightbox" + VIEW_POSTFIX; //$NON-NLS-1$

	public static final String TABLE_VIEW = PREFIX + "table" + VIEW_POSTFIX; //$NON-NLS-1$

	public static final String SLEEVES_VIEW = PREFIX + "sleeves" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String HSTRIP_VIEW = PREFIX + "hstrip" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String VSTRIP_VIEW = PREFIX + "vstrip" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String CATALOG_VIEW = PREFIX + "catalog" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String HISTORY_VIEW = PREFIX + "history" + VIEW_POSTFIX; //$NON-NLS-1$

	public static final String DATAENTRY_VIEW = PREFIX + "dataEntry" + VIEW_POSTFIX; //$NON-NLS-1$

	public static final String METADATA_VIEW = PREFIX
			+ "metadata" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String EXHIBITION_VIEW = PREFIX
			+ "exhibition" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String SLIDESHOW_VIEW = PREFIX
			+ "slideshow" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String WEBGALLERY_VIEW = PREFIX
			+ "webGallery" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String HIERARCHY_VIEW = PREFIX
			+ "hierarchy" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String PREVIEW_VIEW = PREFIX + "preview" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String HISTOGRAM_VIEW = PREFIX
			+ "histogram" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String TRASHCAN_VIEW = PREFIX
			+ "trashcan" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String DUPLICATES_VIEW = PREFIX
			+ "duplicates" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String BOOKMARK_VIEW = PREFIX
			+ "bookmark" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String TAGCLOUD_VIEW = PREFIX
			+ "tagCloud" + VIEW_POSTFIX; //$NON-NLS-1$
	public static final String CALENDAR_VIEW = PREFIX
			+ "calendar" + VIEW_POSTFIX; //$NON-NLS-1$


	// Wizards
	public static final String EXPORTFOLDER_WIZARD = PREFIX
			+ "exportFolder" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String MERGECAT = PREFIX + "mergeCat" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String IMPORTREMOTE = PREFIX
			+ "importRemote" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String IMPORT_FROM_DEVICE_WIZARD_FILE_SELECTION = PREFIX
			+ "importDeviceFileSelection" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String IMPORT_FROM_DEVICE_WIZARD_TARGET_SELECTION = PREFIX
			+ "importDeviceTargetSelection" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String IMPORT_FROM_DEVICE_WIZARD_RENAMING = PREFIX
			+ "importDeviceRenaming" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String IMPORT_FROM_DEVICE_WIZARD_METADATA = PREFIX
			+ "importDeviceMetadata" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String IMPORT_FROM_DEVICE_WIZARD_ANALOG = PREFIX
			+ "analogProperties" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String IMPORT_NEW_STRUCTURE_WIZARD_FILE_SELECTION = PREFIX
			+ "importNewStructureFileSelection" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String IMPORT_NEW_STRUCTURE_WIZARD_TARGET_SELECTION = PREFIX
			+ "importNewStructureTargetSelection" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String IMPORT_NEW_STRUCTURE_WIZARD_RENAMING = PREFIX
			+ "importNewStructureRenaming" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String IMPORT_NEW_STRUCTURE_WIZARD_METADATA =  PREFIX
			+ "importNewStructureMetadata" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String IMPORT_FOLDER_WIZARD_METADATA =  PREFIX
			+ "importFolderMetadata" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String WATCHED_FOLDER_FILE_SELECTION = PREFIX
			+ "watchedFolderFileSelection" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String WATCHED_FOLDER_METADATA = PREFIX
			+ "watchedFolderMetadata" + WIZARD_POSTFIX; //$NON-NLS-1$
	public static final String WATCHED_FOLDER_FILTER = PREFIX
			+ "watchedFolderFilter" + WIZARD_POSTFIX; //$NON-NLS-1$


}
