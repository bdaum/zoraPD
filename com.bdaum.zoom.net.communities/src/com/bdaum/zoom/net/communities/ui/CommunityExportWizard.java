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
package com.bdaum.zoom.net.communities.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.scohen.juploadr.uploadapi.AuthException;
import org.scohen.juploadr.uploadapi.CommunicationException;

import com.bdaum.aoModeling.runtime.AomList;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.net.communities.CommunitiesActivator;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.ExportXmpViewerFilter;
import com.bdaum.zoom.ui.internal.wizards.MetaSelectionPage;

@SuppressWarnings("restriction")
public class CommunityExportWizard extends AbstractCommunityExportWizard {

	public static final String INCLUDEMETA = "includeMeta"; //$NON-NLS-1$
	public static final String SHOWDESCRIPTIONS = "showDescriptions"; //$NON-NLS-1$

	private AlbumDescriptor[] associatedAlbums;
	private String[] titles;
	private String[] descriptions;
	private ExportToCommunityPage mainPage;
	private MetaSelectionPage metaPage;

	public CommunityExportWizard() {
		setHelpAvailable(true);
	}

	@Override
	public void setInitializationData(IConfigurationElement cfig,
			String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);
		if (configElement != null)
			api = CommunitiesActivator.getCommunitiesApi(configElement);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setDialogSettings(Ui.getUi().getDialogSettings(settingsId));
		IWorkbenchWindow activeWorkbenchWindow = workbench
				.getActiveWorkbenchWindow();
		AssetSelection assetSelection;
		if (activeWorkbenchWindow != null)
			assetSelection = Ui.getUi()
					.getNavigationHistory(activeWorkbenchWindow)
					.getSelectedAssets();
		else
			assetSelection = (selection instanceof AssetSelection) ? ((AssetSelection) selection)
					: AssetSelection.EMPTY;
		assets = assetSelection.getAssets();
		if (assets.isEmpty()) {
			if (activeWorkbenchWindow != null) {
				IDbManager dbManager = Core.getCore().getDbManager();
				INavigationHistory navigationHistory = Ui.getUi()
						.getNavigationHistory(activeWorkbenchWindow);
				IStructuredSelection sel = navigationHistory
						.getOtherSelection();
				if (sel != null) {
					Object first = sel.getFirstElement();
					if (first instanceof SlideShowImpl) {
						SlideShowImpl show = (SlideShowImpl) first;
						String name = show.getName();
						AlbumDescriptor currentAlbum = new AlbumDescriptor(
								name, show.getDescription());
						AomList<String> entries = show.getEntry();
						assets = new ArrayList<Asset>(entries.size());
						List<AlbumDescriptor> albums = new ArrayList<AlbumDescriptor>(
								entries.size());
						List<String> titleList = new ArrayList<String>(
								entries.size());
						List<String> descriptionList = new ArrayList<String>(
								entries.size());
						for (String slideId : entries) {
							SlideImpl slide = dbManager.obtainById(
									SlideImpl.class, slideId);
							if (slide != null) {
								String assetId = slide.getAsset();
								if (assetId != null) {
									AssetImpl asset = dbManager
											.obtainAsset(assetId);
									if (asset != null) {
										assets.add(asset);
										albums.add(currentAlbum);
										titleList.add(slide.getCaption());
										descriptionList.add(slide
												.getDescription());
										currentAlbum
												.addTags(asset.getKeyword());
									} else {
										currentAlbum = new AlbumDescriptor(name
												+ ":" + slide.getCaption(), //$NON-NLS-1$
												slide.getDescription());
									}
								}
							}
						}
						associatedAlbums = albums
								.toArray(new AlbumDescriptor[albums.size()]);
						titles = titleList
								.toArray(new String[titleList.size()]);
						descriptions = descriptionList
								.toArray(new String[descriptionList.size()]);
					} else if (first instanceof ExhibitionImpl) {
						ExhibitionImpl show = (ExhibitionImpl) first;
						String name = show.getName();
						String description = show.getDescription();
						assets = new ArrayList<Asset>();
						List<AlbumDescriptor> albums = new ArrayList<AlbumDescriptor>();
						List<String> titleList = new ArrayList<String>();
						List<String> descriptionList = new ArrayList<String>();
						for (Wall wall : show.getWall()) {
							AlbumDescriptor currentAlbum = new AlbumDescriptor(
									name + ":" + wall.getLocation(), description); //$NON-NLS-1$
							AomList<String> exhibits = wall.getExhibit();
							for (String exhibitId : exhibits) {
								ExhibitImpl exhibit = dbManager.obtainById(
										ExhibitImpl.class, exhibitId);
								if (exhibit != null) {
									String assetId = exhibit.getAsset();
									if (assetId != null) {
										AssetImpl asset = dbManager
												.obtainAsset(assetId);
										if (asset != null) {
											assets.add(asset);
											albums.add(currentAlbum);
											titleList.add(exhibit.getTitle());
											descriptionList.add(exhibit
													.getDescription());
											currentAlbum.addTags(asset
													.getKeyword());
										}
									}
								}
							}
						}
						associatedAlbums = albums
								.toArray(new AlbumDescriptor[albums.size()]);
						titles = titleList
								.toArray(new String[titleList.size()]);
						descriptions = descriptionList
								.toArray(new String[descriptionList.size()]);
					} else if (first instanceof WebGalleryImpl) {
						WebGalleryImpl show = (WebGalleryImpl) first;
						String name = show.getName();
						assets = new ArrayList<Asset>();
						List<AlbumDescriptor> albums = new ArrayList<AlbumDescriptor>();
						List<String> titleList = new ArrayList<String>();
						List<String> descriptionList = new ArrayList<String>();
						for (Storyboard storyboard : show.getStoryboard()) {
							AlbumDescriptor currentAlbum = new AlbumDescriptor(
									name + ":" + storyboard.getTitle(), storyboard //$NON-NLS-1$
											.getDescription());
							currentAlbum.addTags(show.getKeyword());
							AomList<String> exhibits = storyboard.getExhibit();
							for (String exhibitId : exhibits) {
								WebExhibitImpl exhibit = dbManager.obtainById(
										WebExhibitImpl.class, exhibitId);
								if (exhibit != null) {
									String assetId = exhibit.getAsset();
									if (assetId != null) {
										AssetImpl asset = dbManager
												.obtainAsset(assetId);
										if (asset != null) {
											assets.add(asset);
											titleList.add(exhibit.getCaption());
											descriptionList.add(exhibit
													.getDescription());
											albums.add(currentAlbum);
											currentAlbum.addTags(asset
													.getKeyword());
										}
									}
								}
							}
						}
						associatedAlbums = albums
								.toArray(new AlbumDescriptor[albums.size()]);
						titles = titleList
								.toArray(new String[titleList.size()]);
						descriptions = descriptionList
								.toArray(new String[descriptionList.size()]);
					}
				}
			}
		}
		List<Asset> filteredAssets = new ArrayList<Asset>(assets.size());
		for (Asset asset : assets)
			if (CoreActivator.getDefault().getMediaSupport(asset.getFormat()) == null)
				filteredAssets.add(asset);
		assets = filteredAssets;
		int size = assets.size();
		setWindowTitle(size == 0 ? Messages.CommunityExportWizard_nothing_selected
				: size == 1 ? NLS.bind(
						Messages.CommunityExportWizard_export_one_image,
						communityName) : NLS.bind(
						Messages.CommunityExportWizard_export_n_images,
						communityName, size));
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new ExportToCommunityPage(configElement, assets,
				associatedAlbums, titles, descriptions, communityId,
				NLS.bind(Messages.CommunityExportWizard_export_to,
						communityName), imageDescriptor);
		addPage(mainPage);
		metaPage = new MetaSelectionPage(new QueryField[] {
				QueryField.EXIF_ALL, QueryField.IPTC_ALL }, false, ExportXmpViewerFilter.INSTANCE, false);
		metaPage.setImageDescriptor(imageDescriptor);
		addPage(metaPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (assets.isEmpty())
			return null;
		if (page == mainPage && !mainPage.getIncludeMeta())
			return null;
		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish() {
		if (assets.isEmpty())
			return false;
		IWizardPage currentPage = getContainer().getCurrentPage();
		if (currentPage == mainPage && mainPage.getIncludeMeta())
			return false;
		return super.canFinish();
	}

	public Set<QueryField> getFilter() {
		return metaPage.getFilter();
	}

	@Override
	protected boolean doFinish() throws CommunicationException, AuthException {
		return mainPage.finish();
	}

}
