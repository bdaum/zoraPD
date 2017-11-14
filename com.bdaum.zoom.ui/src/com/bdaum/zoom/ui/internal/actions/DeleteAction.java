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

package com.bdaum.zoom.ui.internal.actions;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibition;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.WallImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGallery;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.DeleteOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class DeleteAction extends Action {

	private final IAdaptable adaptable;
	private Shell shell;

	public DeleteAction(String label, String tooltip, ImageDescriptor image, IAdaptable adaptable) {
		super(label, image);
		this.adaptable = adaptable;
		shell = adaptable.getAdapter(Shell.class);
		if (shell == null) {
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (activeWorkbenchWindow != null)
				shell = activeWorkbenchWindow.getShell();
		}
		setToolTipText(tooltip);
	}

	@Override
	public void run() {
		INavigationHistory navigationHistory = UiActivator.getDefault()
				.getNavigationHistory(adaptable.getAdapter(IWorkbenchWindow.class));
		SmartCollectionImpl selectedCollection = navigationHistory.getSelectedCollection();
		boolean inAlbum = selectedCollection.getAlbum() && !selectedCollection.getSystem();
		AssetSelection selection = adaptable.getAdapter(AssetSelection.class);
		List<SlideImpl> slides = null;
		List<ExhibitImpl> exhibits = null;
		List<WebExhibitImpl> webexhibits = null;
		if (selection != null) {
			List<Asset> localAssets = selection.getLocalAssets();
			if (localAssets != null && !localAssets.isEmpty()) {
				ICore core = Core.getCore();
				IVolumeManager volumeManager = core.getVolumeManager();
				IDbManager dbManager = core.getDbManager();
				if (localAssets.size() == 1) {
					Asset asset = localAssets.get(0);
					String assetId = asset.getStringId();
					// Check XRef
					slides = dbManager.obtainObjects(SlideImpl.class, "asset", assetId, QueryField.EQUALS); //$NON-NLS-1$
					exhibits = dbManager.obtainObjects(ExhibitImpl.class, "asset", assetId, QueryField.EQUALS); //$NON-NLS-1$
					webexhibits = dbManager.obtainObjects(WebExhibitImpl.class, "asset", assetId, QueryField.EQUALS); //$NON-NLS-1$
					if (!slides.isEmpty() || !exhibits.isEmpty() || !webexhibits.isEmpty()) {
						Set<String> presentationIds = new HashSet<String>(7);
						for (SlideImpl slide : slides) {
							presentationIds.add(slide.getSlideShow_entry_parent());
							if (presentationIds.size() > 4)
								break;
						}
						for (ExhibitImpl exhibit : exhibits) {
							if (presentationIds.size() > 4)
								break;
							WallImpl wall = dbManager.obtainById(WallImpl.class, exhibit.getWall_exhibit_parent());
							if (wall != null) {
								Exhibition exhibition = wall.getExhibition_wall_parent();
								if (exhibition != null)
									presentationIds.add(exhibition.toString());
							}
						}
						for (WebExhibitImpl webexhibit : webexhibits) {
							if (presentationIds.size() > 4)
								break;
							StoryboardImpl story = dbManager.obtainById(StoryboardImpl.class,
									webexhibit.getStoryboard_exhibit_parent());
							if (story != null) {
								WebGallery gallery = story.getWebGallery_storyboard_parent();
								if (gallery != null)
									presentationIds.add(gallery.toString());
							}
						}
						StringBuilder sb = new StringBuilder();
						int i = 0;
						for (String id : presentationIds) {
							if (++i > 3) {
								sb.append(",... "); //$NON-NLS-1$
								break;
							}
							IdentifiableObject obj = dbManager.obtainById(IdentifiableObject.class, id);
							if (obj instanceof SlideShowImpl) {
								if (i > 1)
									sb.append(", "); //$NON-NLS-1$
								sb.append(NLS.bind(Messages.DeleteAction_Slide_show, ((SlideShowImpl) obj).getName()));
							} else if (obj instanceof ExhibitionImpl) {
								if (i > 1)
									sb.append(", "); //$NON-NLS-1$
								sb.append(NLS.bind(Messages.DeleteAction_Exhibition, ((ExhibitionImpl) obj).getName()));
							} else if (obj instanceof WebGalleryImpl) {
								if (i > 1)
									sb.append(", "); //$NON-NLS-1$
								sb.append(
										NLS.bind(Messages.DeleteAction_Web_gallery, ((WebGalleryImpl) obj).getName()));
							}
						}
						StringBuilder message = new StringBuilder();
						message.append(NLS.bind(Messages.DeleteAction_image_used_in, sb));
						if (i == 1)
							message.append(Messages.DeleteAction_remove_image_singular);
						else
							message.append(Messages.DeleteAction_remove_image_plural);
						message.append(Messages.DeleteAction_operation_cannot_be_undome);
						if (!AcousticMessageDialog.openConfirm(shell, Messages.DeleteAction_used_in_artifacts,
								message.toString()))
							return;
					}
					URI fileUri = volumeManager.findExistingFile(asset, true);
					if (fileUri == null) {
						// Image file does not exist
						if (!volumeManager.isOffline(asset.getVolume())) {
							if (inAlbum) {
								MessageDialog dialog = new AcousticMessageDialog(shell,
										Messages.DeleteAction_deleting_images, null,
										Messages.DeleteAction_delete_remote_entries, MessageDialog.QUESTION,
										new String[] { Messages.DeleteAction_remove_from_album,
												Messages.DeleteAction_delete_only_cat, IDialogConstants.CANCEL_LABEL },
										0);
								int ret = dialog.open();
								if (ret != 2)
									launchOperation(selectedCollection, false, Collections.singletonList(asset), slides,
											exhibits, webexhibits);
							} else {
								if (AcousticMessageDialog.openConfirm(shell, Messages.DeleteAction_deleting_images,
										NLS.bind(Messages.DeleteAction_delete_cat_entry, asset.getName())))
									launchOperation(null, false, Collections.singletonList(asset), slides, exhibits,
											webexhibits);
							}
							return;
						}
					} else {
						File file = new File(fileUri);
						if (!file.canWrite()) {
							if (inAlbum) {
								MessageDialog dialog = new AcousticMessageDialog(shell,
										Messages.DeleteAction_deleting_images, null,
										Messages.DeleteAction_delete_write_protected, MessageDialog.QUESTION,
										new String[] { Messages.DeleteAction_remove_from_album,
												Messages.DeleteAction_delete_only_cat, IDialogConstants.CANCEL_LABEL },
										0);
								int ret = dialog.open();
								if (ret != 2)
									launchOperation(selectedCollection, false, Collections.singletonList(asset), slides,
											exhibits, webexhibits);
							} else {
								if (AcousticMessageDialog.openConfirm(shell, Messages.DeleteAction_deleting_images, NLS
										.bind(Messages.DeleteAction_delete_cat_entry_write_protected, asset.getName())))
									launchOperation(null, false, Collections.singletonList(asset), slides, exhibits,
											webexhibits);
							}
							return;
						}
					}
				} else {
					String[] assetIds = selection.getLocalAssetIds();
					slides = dbManager.obtainObjects(SlideImpl.class, "asset", assetIds); //$NON-NLS-1$
					exhibits = dbManager.obtainObjects(ExhibitImpl.class, "asset", assetIds); //$NON-NLS-1$
					webexhibits = dbManager.obtainObjects(WebExhibitImpl.class, "asset", assetIds); //$NON-NLS-1$
					if ((!slides.isEmpty() || !exhibits.isEmpty() || !webexhibits.isEmpty())
							&& !AcousticMessageDialog.openConfirm(shell, Messages.DeleteAction_used_in_artifacts,
									Messages.DeleteAction_images_used_in_presentations))
						return;
				}
				boolean fileOnDisc = false;
				for (Asset asset : localAssets) {
					URI uri = volumeManager.findExistingFile(asset, true);
					if (uri != null && !volumeManager.isOffline(asset.getVolume())) {
						File file = new File(uri);
						if (file.canWrite()) {
							fileOnDisc = true;
							break;
						}
					}
				}
				int esc = 1;
				List<String> buttons = new ArrayList<String>();
				if (inAlbum) {
					buttons.add(Messages.DeleteAction_remove_from_album);
					++esc;
				}
				buttons.add(Messages.DeleteAction_delete_only_cat);
				if (fileOnDisc) {
					buttons.add(Messages.DeleteAction_delete_on_disc_too);
					++esc;
				}
				buttons.add(IDialogConstants.CANCEL_LABEL);
				MessageDialog dialog = new AcousticMessageDialog(shell, Messages.DeleteAction_deleting_images, null,
						(fileOnDisc) ? Messages.DeleteAction_delete_images + Messages.DeleteAction_delete_on_disk
								: Messages.DeleteAction_delete_images,
						MessageDialog.QUESTION, buttons.toArray(new String[buttons.size()]), 0);
				int ret = dialog.open();
				if (ret < esc)
					launchOperation(ret == 0 && inAlbum ? selectedCollection : null, inAlbum ? (ret > 1) : (ret > 0),
							localAssets, slides, exhibits, webexhibits);
			}
		}
	}

	private void launchOperation(SmartCollectionImpl album, boolean ondisc, List<Asset> assets, List<SlideImpl> slides,
			List<ExhibitImpl> exhibits, List<WebExhibitImpl> webexhibits) {
		if (album != null)
			ZoomActionFactory.REMOVEFROMALBUM.create(null, adaptable).run();
		else
			OperationJob.executeOperation(new DeleteOperation(assets, ondisc, slides, exhibits, webexhibits,
					UiActivator.getDefault().createImportConfiguration(adaptable)), adaptable);
	}

}
