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
package com.bdaum.zoom.ui;

public interface IZoomCommandIds {

	String OpenCatalogCommand = "com.bdaum.zoom.ui.OpenCatalogCommand"; //$NON-NLS-1$

	String NewCatalogCommand = "com.bdaum.zoom.ui.NewCatalogCommand"; //$NON-NLS-1$

	String PropertiesCommand = "com.bdaum.zoom.ui.PropertiesCommand"; //$NON-NLS-1$

	String SplitCatalogCommand = "com.bdaum.zoom.ui.SplitCatalogCommand"; //$NON-NLS-1$

	String MergeCatalogCommand = "com.bdaum.zoom.ui.MergeCatalogCommand"; //$NON-NLS-1$

	String BackupNowCommand = "com.bdaum.zoom.ui.BackupNowCommand"; //$NON-NLS-1$

	String ImportFromDeviceCommand = "com.bdaum.zoom.ui.ImportFromDeviceCommand"; //$NON-NLS-1$

	String ImportAnalogCommand = "com.bdaum.zoom.ui.ImportAnalogCommand"; //$NON-NLS-1$

	String ImportIntoNewFolderCommand = "com.bdaum.zoom.ui.ImportIntoNewFolderCommand"; //$NON-NLS-1$

	String ImportFolderCommand = "com.bdaum.zoom.ui.ImportFolderCommand"; //$NON-NLS-1$

	String ImportFileCommand = "com.bdaum.zoom.ui.ImportFileCommand"; //$NON-NLS-1$

	String ImportRemoteFileCommand = "com.bdaum.zoom.ui.ImportRemoteFileCommand"; //$NON-NLS-1$

	String RefreshCommand = "com.bdaum.zoom.ui.RefreshCommand"; //$NON-NLS-1$

	String MoveCommand = "com.bdaum.zoom.ui.MoveCommand"; //$NON-NLS-1$

	String EditWithCommand = "com.bdaum.zoom.ui.EditWithCommand"; //$NON-NLS-1$

	String EditCommand = "com.bdaum.zoom.ui.EditCommand"; //$NON-NLS-1$

	String RotateClockwiseCommand = "com.bdaum.zoom.ui.RotateClockwiseCommand"; //$NON-NLS-1$

	String RotateAntiClockwiseCommand = "com.bdaum.zoom.ui.RotateAntiClockwiseCommand"; //$NON-NLS-1$

	String AdhocSlideshowCommand = "com.bdaum.zoom.ui.AdhocSlideshowCommand"; //$NON-NLS-1$

	String DeleteCommand = "com.bdaum.zoom.ui.DeleteCommand"; //$NON-NLS-1$

	String AdhocQueryCommand = "com.bdaum.zoom.ui.AdhocQueryCommand"; //$NON-NLS-1$

	String KeywordQueryCommand = "com.bdaum.zoom.ui.KeywordQueryCommand"; //$NON-NLS-1$

	String AddKeywordsCommand = "com.bdaum.zoom.ui.AddKeywordsCommand"; //$NON-NLS-1$

	String ForwardCommand = "com.bdaum.zoom.ui.ForwardCommand"; //$NON-NLS-1$

	String BackCommand = "com.bdaum.zoom.ui.BackCommand"; //$NON-NLS-1$

	String LastImportCommand = "com.bdaum.zoom.ui.LastImportCommand"; //$NON-NLS-1$

	String SearchSimilarCommand = "com.bdaum.zoom.ui.SearchSimilarCommand"; //$NON-NLS-1$

	String TimeSearchCommand = "com.bdaum.zoom.ui.TimeSearchCommand"; //$NON-NLS-1$

	String CopyMetadataCommand = "com.bdaum.zoom.ui.CopyMetadataCommand"; //$NON-NLS-1$

	String PasteMetadataCommand = "com.bdaum.zoom.ui.PasteMetadataCommand"; //$NON-NLS-1$

	String FindOrphans = "com.bdaum.zoom.ui.FindOrphans"; //$NON-NLS-1$

	String FindDuplicates = "com.bdaum.zoom.ui.FindDuplicates"; //$NON-NLS-1$

	String TextQueryCommand = "com.bdaum.zoom.ui.TextQueryCommand"; //$NON-NLS-1$

	String ProximityCommand = "com.bdaum.zoom.ui.ProximityCommand"; //$NON-NLS-1$

	String FindSeries = "com.bdaum.zoom.ui.FindSeries"; //$NON-NLS-1$

	String ToggleHover = "com.bdaum.zoom.ui.command.toggleHover"; //$NON-NLS-1$

	String ShowInFolder = "com.bdaum.zoom.ui.command.showInFolder"; //$NON-NLS-1$

	String ShowInTimeline = "com.bdaum.zoom.ui.command.showInTimkeline"; //$NON-NLS-1$

	String ShowInMap = "com.bdaum.zoom.ui.command.showInMap"; //$NON-NLS-1$

	String ViewImage = "com.bdaum.zoom.ui.command.viewImage"; //$NON-NLS-1$

	String ColorCode = "com.bdaum.zoom.ui.command.colorCode"; //$NON-NLS-1$

	String Rate = "com.bdaum.zoom.ui.command.rate"; //$NON-NLS-1$

	String PlayVoiceNote = "com.bdaum.zoom.ui.command.playVoiceNote"; //$NON-NLS-1$

	String AddVoiceNote = "com.bdaum.zoom.ui.command.addVoiceNote"; //$NON-NLS-1$

	String RemoveVoiceNote = "com.bdaum.zoom.ui.command.removeVoiceNote"; //$NON-NLS-1$

	String AddToAlbum = "com.bdaum.zoom.ui.command.addToAlbum"; //$NON-NLS-1$

	String RemoveFromAlbum = "com.bdaum.zoom.ui.command.removeFromAlbum"; //$NON-NLS-1$

	String ShowDerivatives = "com.bdaum.zoom.ui.command.showDerivatives"; //$NON-NLS-1$

	String ShowOriginals = "com.bdaum.zoom.ui.command.showOriginals"; //$NON-NLS-1$

	String ShowComposites = "com.bdaum.zoom.ui.command.showComposites"; //$NON-NLS-1$

	String ShowComponents = "com.bdaum.zoom.ui.command.showComponents"; //$NON-NLS-1$

	String ShowBookmarks = "com.bdaum.zoom.ui.command.showBookmarks"; //$NON-NLS-1$

	String ShowTrashcan = "com.bdaum.zoom.ui.command.showTrashcan"; //$NON-NLS-1$

	String SheckUpdates = "com.bdaum.zoom.ui.command.checkUpdates"; //$NON-NLS-1$

	String TimeShift = "com.bdaum.zoom.ui.command.timeShift"; //$NON-NLS-1$

	String ReportProblem = "com.bdaum.zoom.ui.command.reportProblem"; //$NON-NLS-1$

	String FullscreenCommand = "org.ugosan.eclipse.fullscreen.fullscreenCommand"; //$NON-NLS-1$

//	String AddBookmarkCommand = "com.bdaum.zoom.ui.AddBookmarkCommand"; //$NON-NLS-1$ // already defined in platform

	String AddToAlbumCommand = "com.bdaum.zoom.ui.command.addToAlbum"; //$NON-NLS-1$

	String CategorizeCommand = "com.bdaum.zoom.ui.command.categorize"; //$NON-NLS-1$

	String Deselect = "com.bdaum.zoom.ui.command.deselect"; //$NON-NLS-1$

	String Revert = "com.bdaum.zoom.ui.command.revertSelection"; //$NON-NLS-1$


}
