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
 * (c) 2009-2017 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.bdaum.zoom.cat.model.BookmarkImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.peer.AssetOrigin;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ColorCodeOperation;
import com.bdaum.zoom.operations.internal.RateOperation;
import com.bdaum.zoom.operations.internal.RemoveAlbumOperation;
import com.bdaum.zoom.operations.internal.xmp.XMPUtilities;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.ILocationDisplay;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.ColorCodeDialog;
import com.bdaum.zoom.ui.internal.dialogs.PasteMetaDialog;
import com.bdaum.zoom.ui.internal.dialogs.RatingDialog;
import com.bdaum.zoom.ui.internal.dialogs.AddBookmarkDialog;
import com.bdaum.zoom.ui.internal.views.BasicView;
import com.bdaum.zoom.ui.internal.views.BookmarkView;
import com.bdaum.zoom.ui.internal.views.CatalogView;
import com.bdaum.zoom.ui.internal.views.LightboxView;
import com.bdaum.zoom.ui.internal.views.TableView;
import com.bdaum.zoom.ui.internal.views.ZuiView;

@SuppressWarnings("restriction")
public abstract class ZoomActionFactory {

	private final String label;
	private final Icons.Icon icon;
	private final String tooltip;
	private final String id;

	protected ZoomActionFactory(String label, String tooltip, String id, Icons.Icon icon) {
		this.label = label;
		this.icon = icon;
		this.id = id;
		this.tooltip = tooltip;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public ImageDescriptor getImage() {
		return icon == null ? null : icon.getDescriptor();
	}

	public String getTooltip() {
		return tooltip;
	}

	public abstract IAction create(IActionBars bars, IAdaptable adaptable);

	public static final ZoomActionFactory PROXIMITY = new ZoomActionFactory(Messages.ZoomActionFactory_proximity_search,
			Messages.ZoomActionFactory_search_images_in_vincinity, "com.bdaum.zoom.gps.actions.ProximityAction", //$NON-NLS-1$
			Icons.proximity) {

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			ProximityAction action = new ProximityAction(getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory DELETE = new ZoomActionFactory(Messages.ZoomActionFactory_delete,
			Messages.ZoomActionFactory_delete_selected_images, "com.bdaum.zoom.ui.actions.DeleteAction", Icons.delete) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			DeleteAction action = new DeleteAction(getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory REFRESH = new ZoomActionFactory(Messages.ZoomActionFactory_refresh,
			Messages.ZoomActionFactory_refresh_tooltip, "com.bdaum.zoom.ui.actions.RefreshAction", Icons.refresh) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			RefreshAction action = new RefreshAction(getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory SHOWINFOLDER = new ZoomActionFactory(
			Messages.ZoomActionFactory_show_in_folder, Messages.ZoomActionFactory_show_in_folder_tooltip,
			"com.bdaum.zoom.ui.actions.ShowInFolderAction", Icons.folder_find) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			Action action = new Action(getLabel(), getImage()) {
				@Override
				public void run() {
					final Asset asset = getFirstLocalAsset(adaptable);
					if (asset != null) {
						showCollection(adaptable, asset);
						showHostFolder(adaptable, asset);
					}
				}

				private void showCollection(final IAdaptable info, final Asset asset) {
					showInGallery(info, asset, Utilities.obtainFolderCollection(Core.getCore().getDbManager(),
							asset.getUri(), asset.getVolume()));
				}

				private void showHostFolder(final IAdaptable info, final Asset asset) {
					URI uri = Core.getCore().getVolumeManager().findExistingFile(asset, true);
					if (uri != null)
						BatchUtilities.showInFolder(new File(uri));
					else
						AcousticMessageDialog.openInformation(info.getAdapter(Shell.class),
								Messages.ZoomActionFactory_show_in_folder,
								Messages.ZoomActionFactory_selected_file_is_offline);
				}
			};
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	private static void showInGallery(final IAdaptable adaptable, final Asset asset, SmartCollection sm) {
		if (sm != null) {
			try {
				BasicView currentView = adaptable.getAdapter(BasicView.class);
				IWorkbenchPage page = adaptable.getAdapter(IWorkbenchPage.class);
				String viewId = null;
				for (IViewReference ref : page.getViewReferences()) {
					String id = ref.getId();
					if (id.equals(LightboxView.ID) || id.equals(ZuiView.ID) || id.equals(TableView.ID)
							|| id.equals(LightboxView.VSTRIPVIEW) || id.equals(LightboxView.HSTRIPVIEW)) {
						viewId = id;
						break;
					}
				}
				if (viewId == null)
					viewId = LightboxView.ID;
				IViewPart gallery = null;
				if (currentView == null || !viewId.equals(currentView.getViewSite().getId()))
					gallery = page.showView(viewId);
				((CatalogView) page.showView(CatalogView.ID)).setSelection(new StructuredSelection(sm), true);
				INavigationHistory navigationHistory = UiActivator.getDefault()
						.getNavigationHistory(page.getWorkbenchWindow());
				navigationHistory.postSelection(AssetSelection.EMPTY);
				navigationHistory.postSelection(new AssetSelection(asset));
				if (gallery != null)
					gallery.setFocus();
			} catch (PartInitException e) {
				// should never happen
			}
		}
	}

	public static final ZoomActionFactory SHOWINTIMELINE = new ZoomActionFactory(
			Messages.ZoomActionFactory_show_in_timeline, Messages.ZoomActionFactory_show_in_timeline_tooltip,
			"com.bdaum.zoom.ui.actions.ShowInTimeLineAction", Icons.timeline_find) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			Action action = new Action(getLabel(), getImage()) {

				@Override
				public void run() {
					final Asset asset = getFirstLocalAsset(adaptable);
					if (asset != null) {
						showInGallery(adaptable, asset, Utilities
								.obtainTimelineCollection(Core.getCore().getDbManager(), asset.getDateTimeOriginal()));
					}
				}

			};
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory SHOWINMAP = new ZoomActionFactory(Messages.ZoomActionFactory_show_in_map,
			Messages.ZoomActionFactory_show_in_map_tooltip, "com.bdaum.zoom.ui.actions.ShowInMapAction", //$NON-NLS-1$
			Icons.map_find) {

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			ILocationDisplay display = UiActivator.getDefault().getLocationDisplay();
			if (display == null)
				return null;
			Action action = new Action(getLabel(), getImage()) {
				@Override
				public void run() {
					AssetSelection assetSelection = adaptable.getAdapter(AssetSelection.class);
					if (!assetSelection.isEmpty()) {
						ILocationDisplay display = UiActivator.getDefault().getLocationDisplay();
						if (display != null)
							display.display(assetSelection);
					}
				}
			};
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory EDIT = new ZoomActionFactory(Messages.ZoomActionFactory_edit,
			Messages.ZoomActionFactory_edit_images_with_default, "com.bdaum.zoom.ui.actions.EditImageAction", //$NON-NLS-1$
			Icons.image_edit) {

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			EditImageAction action = new EditImageAction(getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory EDITWITH = new ZoomActionFactory(Messages.ZoomActionFactory_edit_with,
			Messages.ZoomActionFactory_edit_images_with_selected_editor, "com.bdaum.zoom.ui.actions.EditWithAction", //$NON-NLS-1$
			Icons.image_edit_with) {

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			EditWithAction action = new EditWithAction(getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory ROTATELEFT = new ZoomActionFactory(
			Messages.ZoomActionFactory_rotate_anti_clockwise, Messages.ZoomActionFactory_rotate_anti_clockwise_90,
			"com.bdaum.zoom.ui.actions.RotateLeftAction", Icons.rotate270s) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			RotateAction action = new RotateAction(adaptable.getAdapter(IWorkbenchWindow.class), getLabel(),
					getTooltip(), getImage(), adaptable, 270);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory ROTATERIGHT = new ZoomActionFactory(
			Messages.ZoomActionFactory_rotate_clockwise, Messages.ZoomActionFactory_rotate_clockwise_90,
			"com.bdaum.zoom.ui.actions.RotateRightAction", Icons.rotate90s) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			RotateAction action = new RotateAction(adaptable.getAdapter(IWorkbenchWindow.class), getLabel(),
					getTooltip(), getImage(), adaptable, 90);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory RATING = new ZoomActionFactory(Messages.ZoomActionFactory_rating,
			Messages.ZoomActionFactory_rate_this_image, "com.bdaum.zoom.ui.actions.RatingAction", Icons.rating) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			IAction action = new Action(getLabel(), getImage()) {
				@Override
				public void run() {
					List<Asset> localAssets = adaptable.getAdapter(AssetSelection.class).getLocalAssets();
					if (localAssets != null && !localAssets.isEmpty()) {
						RatingDialog dialog = new RatingDialog(adaptable.getAdapter(Shell.class),
								localAssets.get(0).getRating(), 0.7d, true);
						dialog.create();
						dialog.getShell().setLocation(adaptable.getAdapter(Control.class).toDisplay(0, 0));
						rate(localAssets, adaptable, dialog.open());
					}
				}
			};
			action.setToolTipText(getTooltip());
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static void rate(final List<Asset> assets, final IAdaptable adaptable, int rate) {
		if (rate == RatingDialog.DELETE) {
			DeleteAction action = new DeleteAction(DELETE.getLabel(), DELETE.getTooltip(), DELETE.getImage(),
					new IAdaptable() {
						@SuppressWarnings({ "rawtypes", "unchecked" })
						public Object getAdapter(Class adapter) {
							if (AssetSelection.class.equals(adapter))
								return new AssetSelection(assets);
							return adaptable.getAdapter(adapter);
						}
					});
			action.run();
		} else if (rate >= 0)
			OperationJob.executeOperation(new RateOperation(assets, rate), adaptable);
	}

	public static final ZoomActionFactory COLORCODE = new ZoomActionFactory(Messages.ZoomActionFactory_color_code,
			Messages.ZoomActionFactory_color_code_this_image, "com.bdaum.zoom.ui.actions.ColorCodeAction", //$NON-NLS-1$
			Icons.colorCode) {

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			IAction action = new Action(getLabel(), getImage()) {

				@Override
				public void run() {
					List<Asset> localAssets = adaptable.getAdapter(AssetSelection.class).getLocalAssets();
					if (localAssets != null && !localAssets.isEmpty()) {
						ColorCodeDialog dialog = new ColorCodeDialog(adaptable.getAdapter(Shell.class),
								localAssets.get(0).getColorCode());
						dialog.create();
						dialog.getShell().setLocation(adaptable.getAdapter(Control.class).toDisplay(0, 0));
						int code = dialog.open();
						if (code >= Constants.COLOR_UNDEFINED)
							OperationJob.executeOperation(new ColorCodeOperation(localAssets, code), adaptable);
					}
				}
			};
			action.setToolTipText(getTooltip());
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory MOVE = new ZoomActionFactory(Messages.ZoomActionFactory_move, Messages.ZoomActionFactory_move_tooltip,
			"com.bdaum.zoom.ui.MoveAction", Icons.move) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			MoveAction action = new MoveAction(adaptable.getAdapter(IWorkbenchWindow.class), getLabel(), getTooltip(),
					getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory PLAYVOICENOTE = new ZoomActionFactory(
			Messages.ZoomActionFactory_play_voicenote, Messages.ZoomActionFactory_play_tooltip,
			"com.bdaum.zoom.ui.PlayVoiceNoteAction", Icons.sound) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			PlayVoiceNoteAction action = new PlayVoiceNoteAction(adaptable.getAdapter(IWorkbenchWindow.class),
					getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory ADDVOICENOTE = new ZoomActionFactory(
			Messages.ZoomActionFactory_attach_remove_voicenote,
			Messages.ZoomActionFactory_attach_remove_voicenote_tooltip, "com.bdaum.zoom.ui.AddVoiceNoteAction", //$NON-NLS-1$
			Icons.sound_add) {

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			AddVoiceNoteAction action = new AddVoiceNoteAction(adaptable.getAdapter(IWorkbenchWindow.class), getLabel(),
					getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory UPDATEKEYWORDS = new ZoomActionFactory(
			Messages.ZoomActionFactory_update_keywords, Messages.ZoomActionFactory_update_keywords_tooltip,
			"com.bdaum.zoom.ui.actions.UpdateKeywordAction", Icons.keydef) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			UpdateKeywordsAction action = new UpdateKeywordsAction(adaptable.getAdapter(IWorkbenchWindow.class),
					getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory SEARCHSIMILAR = new ZoomActionFactory(
			Messages.ZoomActionFactory_search_similar, Messages.ZoomActionFactory_search_similar_tooltip,
			"com.bdaum.zoom.ui.actions.SearchSimilarAction", Icons.similar) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			SearchSimilarAction action = new SearchSimilarAction(getId(), getLabel(), getTooltip(), getImage(),
					adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory TIMESEARCH = new ZoomActionFactory(Messages.ZoomActionFactory_timer_search,
			Messages.ZoomActionFactory_time_search_tooltip, "com.bdaum.zoom.ui.actions.TimeSearch", Icons.time) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			TimeSearchAction action = new TimeSearchAction(getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory BULKRENAME = new ZoomActionFactory(Messages.ZoomActionFactory_rename,
			Messages.ZoomActionFactory_rename_tooltip, "com.bdaum.zoom.ui.actions.BulkRename", Icons.rename) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			BulkRenameAction action = new BulkRenameAction(getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory SLIDESHOW = new ZoomActionFactory(Messages.ZoomActionFactory_slideshow,
			Messages.ZoomActionFactory_slideshow_tooltip,
			"com.bdaum.zoom.ui.actions.Slideshow", Icons.slideshow) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			SlideshowAction action = new SlideshowAction(adaptable.getAdapter(IWorkbenchWindow.class), getLabel(),
					getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};
	
	public static final ZoomActionFactory SPLITCATALOG = new ZoomActionFactory(Messages.ZoomActionFactory_split_cat,
			Messages.ZoomActionFactory_split_cat_tooltip, "com.bdaum.zoom.ui.actions.SplitCatalog", Icons.splitcat) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			SplitCatAction action = new SplitCatAction(adaptable.getAdapter(IWorkbenchWindow.class), getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};


	public static final ZoomActionFactory STACK = new ZoomActionFactory(Messages.ZoomActionFactory_named_stack,
			Messages.ZoomActionFactory_named_stack_tooltip, "com.bdaum.zoom.ui.actions.Stack", Icons.stack) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			StackAction action = new StackAction(getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory TIMESHIFT = new ZoomActionFactory(Messages.ZoomActionFactory_apply_time_shift,
			Messages.ZoomActionFactory_correct_time_stamps, "com.bdaum.zoom.ui.actions.TimeShiftAction", //$NON-NLS-1$
			Icons.timeShift) {

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			TimeShiftAction action = new TimeShiftAction(getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory VIEWIMAGE = new ZoomActionFactory(Messages.ZoomActionFactory_full_screen,
			Messages.ZoomActionFactory_open_image_full_screen, "com.bdaum.zoom.ui.actions.ViewImageAction", //$NON-NLS-1$
			Icons.square) {

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			ViewImageAction action = new ViewImageAction(getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory ADDTOALBUM = new ZoomActionFactory(Messages.ZoomActionFactory_add_to_albums,
			Messages.ZoomActionFactory_add_to_albums_tooltip, "com.bdaum.zoom.ui.actions.AddToAlbumAction", //$NON-NLS-1$
			Icons.addAlbum) {

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {

			IAction action = new AddToAlbumAction(getLabel(), getImage(), adaptable);
			action.setToolTipText(getTooltip());
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory REMOVEFROMALBUM = new ZoomActionFactory(
			Messages.ZoomActionFactory_remove_from_album, Messages.ZoomActionFactory_remove_from_album_tooltip,
			"com.bdaum.zoom.ui.actions.RemoveFromAlbumAction", Icons.albumRemove) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			IAction action = new Action(getLabel(), getImage()) {

				@Override
				public void run() {
					List<Asset> localAssets = adaptable.getAdapter(AssetSelection.class).getLocalAssets();
					if (localAssets != null && !localAssets.isEmpty()) {
						SmartCollectionImpl coll = Core.getCore().getAssetProvider().getCurrentCollection();
						if (coll.getAlbum()) {
							boolean remove = true;
							if (subAlbumContainsAssets(coll, localAssets))
								remove = AcousticMessageDialog.openQuestion(adaptable.getAdapter(Shell.class),
										Messages.ZoomActionFactory_remove_from_album,
										Messages.ZoomActionFactory_contained_in_subalbums);
							if (remove)
								OperationJob.executeOperation(new RemoveAlbumOperation(coll, localAssets), adaptable);
						}
					}
				}

				private boolean subAlbumContainsAssets(SmartCollection coll, List<Asset> localAssets) {
					for (SmartCollection sub : coll.getSubSelection()) {
						String name = sub.getName();
						if (name != null) {
							for (Asset asset : localAssets) {
								String[] album = asset.getAlbum();
								if (album != null)
									for (String a : album)
										if (name.equals(a))
											return true;
							}
						}
						if (subAlbumContainsAssets(sub, localAssets))
							return true;
					}
					return false;
				}
			};
			action.setToolTipText(getTooltip());
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory SHOWDERIVATIVES = new ZoomActionFactory(
			Messages.ZoomActionFactory_show_derivatives, Messages.ZoomActionFactory_show_all_images_derived,
			"com.bdaum.zoom.ui.actions.ShowDerivatives", Icons.derivative) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			IAction action = new ShowAssetAction(Constants.DERIVATIVES, getLabel(), getImage(), getTooltip(),
					adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory SHOWORIGINALS = new ZoomActionFactory(
			Messages.ZoomActionFactory_show_originals, Messages.ZoomActionFactory_show_images_from_which_derived,
			"com.bdaum.zoom.ui.actions.ShowOriginals", Icons.original) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			IAction action = new ShowAssetAction(Constants.ORIGINALS, getLabel(), getImage(), getTooltip(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory SHOWCOMPOSITES = new ZoomActionFactory(
			Messages.ZoomActionFactory_show_composites, Messages.ZoomActionFactory_images_composed_from,
			"com.bdaum.zoom.ui.actions.ShowComposites", Icons.composite) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			IAction action = new ShowAssetAction(Constants.COMPOSITES, getLabel(), getImage(), getTooltip(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory SHOWCOMPONENTS = new ZoomActionFactory(
			Messages.ZoomActionFactory_show_components, Messages.ZoomActionFactory_show_images_contributing,
			"com.bdaum.zoom.ui.actions.ShowComponents", Icons.component) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			IAction action = new ShowAssetAction(Constants.COMPONENTS, getLabel(), getImage(), getTooltip(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	public static final ZoomActionFactory ADDBOOKMARK = new ZoomActionFactory(Messages.ZoomActionFactory_add_bookmark,
			Messages.ZoomActionFactory_add_bookmark_tooltip, "com.bdaum.zoom.ui.actions.AddBookmark", //$NON-NLS-1$
			Icons.addBookmark) {

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			IAction action = new Action(getLabel(), getImage()) {

				@Override
				public void run() {
					AssetSelection assetSelection = adaptable.getAdapter(AssetSelection.class);

					if (!assetSelection.isEmpty()) {
						CoreActivator activator = CoreActivator.getDefault();
						final IDbManager dbManager = activator.getDbManager();
						boolean readOnly = dbManager.isReadOnly();
						dbManager.setReadOnly(false);
						try {
							Iterator<Asset> iterator = assetSelection.iterator();
							while (iterator.hasNext()) {
								Asset asset = iterator.next();
								String title = UiUtilities.createSlideTitle(asset);
								AddBookmarkDialog dialog = assetSelection.size() > 1 ? null
										: new AddBookmarkDialog(adaptable.getAdapter(Shell.class),
												Messages.ZoomActionFactory_add_bookmark_dialog,
												Messages.ZoomActionFactory_add_bookmark_description, title, null, true);
								if (dialog == null || dialog.open() == Window.OK) {
									SmartCollectionImpl coll = Core.getCore().getAssetProvider().getCurrentCollection();
									String assetId = asset.getStringId();
									String catFile = null;
									String peer = null;
									IPeerService peerService = activator.getPeerService();
									if (peerService != null) {
										AssetOrigin assetOrigin = peerService.getAssetOrigin(asset.getStringId());
										if (assetOrigin != null) {
											peer = peerService.isLocal(assetOrigin.getLocation()) ? null
													: assetOrigin.getLocation();
											catFile = assetOrigin.getCatFile().toString();
										}
									}
									BookmarkImpl bookmark = new BookmarkImpl(dialog == null ? title : dialog.getValue(),
											assetId, coll.getAdhoc() ? null : coll.getStringId(), new Date(), peer,
											catFile);
									bookmark.setJpegImage(asset.getJpegThumbnail());
									dbManager.safeTransaction(null, bookmark);
									activator.fireBookmarksModified();
									IWorkbenchPage page = adaptable.getAdapter(IWorkbenchPage.class);
									try {
										((BookmarkView) page.showView(BookmarkView.ID)).selectBookmark(bookmark);
									} catch (PartInitException e) {
										// do nothing
									}
								}
							}
						} finally {
							dbManager.setReadOnly(readOnly);
						}
					}
				}
			};
			action.setToolTipText(getTooltip());
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};
	public static final ZoomActionFactory PASTEMETADATA = new ZoomActionFactory(
			Messages.ZoomActionFactory_paste_metadata, Messages.ZoomActionFactory_paste_metadata_tooltip,
			"com.bdaum.zoom.ui.actions.PasteMetadataAction", Icons.pasteMetadata) { //$NON-NLS-1$

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			IAction action = new Action(getLabel(), getImage()) {

				@Override
				public void run() {
					List<Asset> localAssets = adaptable.getAdapter(AssetSelection.class).getLocalAssets();
					if (localAssets != null && !localAssets.isEmpty()) {
						Shell shell = adaptable.getAdapter(Shell.class);
						Object contents = UiActivator.getDefault().getClipboard(shell.getDisplay())
								.getContents(TextTransfer.getInstance());
						if (contents instanceof String) {
							String text = (String) contents;
							try {
								new PasteMetaDialog(shell, localAssets,
										XMPUtilities.readXMP(new ByteArrayInputStream(text.getBytes("UTF-8"))), //$NON-NLS-1$
										adaptable).open();
							} catch (UnsupportedEncodingException e) {
								// should never happen
							} catch (XMPException e) {
								UiActivator.getDefault()
										.logError(Messages.PasteMetadataAction_xmp_error_reading_from_clipboard, e);
							}
						}
					}

				}
			};
			action.setToolTipText(getTooltip());
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};
	public static final ZoomActionFactory COPYMETADATA = new ZoomActionFactory(Messages.ZoomActionFactory_copy_metadata,
			Messages.ZoomActionFactory_copy_metadata_tooltip, "com.bdaum.zoom.ui.actions.CopyMetadataAction", //$NON-NLS-1$
			Icons.copyMetadata) {

		@Override
		public IAction create(IActionBars bars, final IAdaptable adaptable) {
			IAction action = new Action(getLabel(), getImage()) {

				@Override
				public void run() {
					Asset asset = getSingleAsset(adaptable);
					if (asset != null) {
						Set<QueryField> filter = new HashSet<QueryField>(100);
						for (String id : QueryField.getQueryFieldKeys()) {
							QueryField qfield = QueryField.findQueryField(id);
							if (qfield.isEditable(asset))
								filter.add(qfield);
						}
						Shell shell = adaptable.getAdapter(Shell.class);
						UiActivator activator = UiActivator.getDefault();
						try {
							XMPUtilities.configureXMPFactory();
							XMPMeta xmpMeta = XMPMetaFactory.create();
							XMPUtilities.writeProperties(xmpMeta, asset, filter, true);
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							XMPMetaFactory.serialize(xmpMeta, out);
							activator.getClipboard(shell.getDisplay()).setContents(
									new Object[] { new String(out.toByteArray(), "UTF-8") }, //$NON-NLS-1$
									new Transfer[] { TextTransfer.getInstance() });

						} catch (SWTError ex) {
							if (ex.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
								activator.logError(Messages.CopyMetadataAction_Error_when_copying_metadata, ex);
							else {
								AcousticMessageDialog.openWarning(shell,
										Messages.CopyMetadataAction_Problem_when_copying,
										Messages.CopyMetadataAction_There_was_a_problem_accessing);
							}
						} catch (UnsupportedEncodingException e) {
							// should never happen
						} catch (XMPException e) {
							activator.logError(Messages.CopyMetadataAction_xmp_error_creating_clipboard, e);
						}
					} else
						AcousticMessageDialog.openWarning(adaptable.getAdapter(Shell.class),
								Messages.CopyMetadataAction_Copy_metadata,
								Messages.CopyMetadataAction_Please_select_single_image);
				}
			};
			action.setToolTipText(getTooltip());
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};
	public static final ZoomActionFactory CATEGORIZE = new ZoomActionFactory(Messages.ZoomActionFactory_categorize,
			Messages.ZoomActionFactory_add_images_to_categories, "com.bdaum.zoom.ui.actions.CategorizeAction", //$NON-NLS-1$
			Icons.categorize) {

		@Override
		public IAction create(IActionBars bars, IAdaptable adaptable) {
			CategorizeAction action = new CategorizeAction(getLabel(), getTooltip(), getImage(), adaptable);
			if (bars != null)
				bars.setGlobalActionHandler(getId(), action);
			return action;
		}
	};

	private static Asset getSingleAsset(IAdaptable adaptable) {
		return adaptable.getAdapter(AssetSelection.class).getFirstElement();
	}

	private static Asset getFirstLocalAsset(IAdaptable adaptable) {
		return adaptable.getAdapter(AssetSelection.class).getFirstLocalAsset();
	}

}
