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
 * (c) 2009-2017 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.views;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.RenameAssetOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.ZoomActionFactory;
import com.bdaum.zoom.ui.internal.dialogs.RatingDialog;
import com.bdaum.zoom.ui.internal.job.DecorateJob;
import com.bdaum.zoom.ui.internal.widgets.AnimatedGallery;
import com.bdaum.zoom.ui.internal.widgets.AnimatedGallery.PGalleryItem;
import com.bdaum.zoom.ui.internal.widgets.ZPSWTCanvas;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class ZuiView extends AbstractGalleryView implements Listener {

	private static class ZuiGalleryDecorateJob extends DecorateJob {

		private final AnimatedGallery gallery;
		private PGalleryItem[] items;
		private final AbstractGalleryView view;

		private Runnable itemRunnable = new Runnable() {
			public void run() {
				if (!gallery.isDisposed())
					items = gallery.getItems();
			}
		};
		private Runnable assetRunnable = new Runnable() {
			public void run() {
				for (PGalleryItem item : items) {
					if (item != null && !gallery.isDisposed()) {
						Asset asset = item.getAsset();
						if (asset != null)
							asset.setFileState(volumeManager.determineFileState(asset));
					}
				}
				items = null;
			}
		};

		public ZuiGalleryDecorateJob(AbstractGalleryView view, AnimatedGallery gallery) {
			super(Messages.getString("ZuiView.decorate_gallery")); //$NON-NLS-1$
			this.view = view;
			this.gallery = gallery;
		}

		@Override
		protected boolean mayRun() {
			return view.isVisible() && view.getRefreshing() <= 0;
		}

		@Override
		protected void doRun(IProgressMonitor monitor) {
			if (!gallery.isDisposed()) {
				Display display = gallery.getDisplay();
				display.syncExec(itemRunnable);
				if (items != null && items.length > 0 && items[0] != null && mayRun())
					display.asyncExec(assetRunnable);
			}
		}

	}

	public class ColumnAction extends Action {

		private int cnt;

		public ColumnAction(int cnt) {
			super(NLS.bind(Messages.getString("ZuiView.n_columns"), cnt), IAction.AS_RADIO_BUTTON); //$NON-NLS-1$
			this.cnt = cnt;
		}

		@Override
		public void run() {
			setNumberOfColums(cnt);
		}
	}

	private static final String COLUMNS = "com.bdaum.zoom.viewerColumns"; //$NON-NLS-1$

	private static final int DIST = 150;
	private static final int THUMB = 320;

	public static final String ID = "com.bdaum.zoom.ui.views.ZuiView"; //$NON-NLS-1$

	private Action action4;
	private Action action6;
	private Action action8;
	private Action action10;
	private Action action12;
	private Action action15;
	private Action action18;
	private Action action21;
	private Action action24;
	private int columns = 8;
	private AnimatedGallery animatedGallery;
	private boolean forceExternalSelection;
	protected boolean mouseDown;
	protected int lastMouseX;
	protected int mouseButton;
	private int currentSystemCursor = SWT.CURSOR_ARROW;
	private String currentCustomCursor;
	private SmartCollectionImpl currentCollection;
	private int showLabelDflt;
	private String labelTemplateDflt;
	private int labelFontsizeDflt;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			Integer integer = memento.getInteger(COLUMNS);
			if (integer != null)
				columns = integer;
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null)
			memento.putInteger(COLUMNS, columns);
		super.saveState(memento);
	}

	public void setNumberOfColums(int cnt) {
		animatedGallery.setColumns(columns = cnt);
	}

	@Override
	public void createPartControl(final Composite parent) {
		setPreferences();
		animatedGallery = new AnimatedGallery(new ZPSWTCanvas(parent, SWT.NONE), DIST, THUMB, this);
		setCanvasCursor(null, SWT.CURSOR_APPSTARTING);
		animatedGallery.setMinScale(0.2d);
		animatedGallery.setMaxScale(3d);
		animatedGallery.setColumns(columns);
		animatedGallery.addListener(this);
		animatedGallery.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button != 2 && ((e.stateMask & SWT.ALT) == 0) && cursorOverImage(e.x, e.y))
					setCanvasCursor(null, SWT.CURSOR_ARROW);
				else {
					mouseDown = true;
					lastMouseX = e.x;
					mouseButton = e.button;
					int zoomKey = Platform.getPreferencesService().getInt(UiActivator.PLUGIN_ID,
							PreferenceConstants.ZOOMKEY, PreferenceConstants.ZOOMALT, null);
					boolean zoomin = false;
					if (mouseButton != 0) {
						if (mouseButton == 2)
							zoomin = zoomKey == PreferenceConstants.ZOOMRIGHT;
						else if (mouseButton == 1) {
							switch (zoomKey) {
							case PreferenceConstants.ZOOMALT:
								zoomin = (e.stateMask & SWT.ALT) != 0;
								break;
							case PreferenceConstants.ZOOMSHIFT:
								zoomin = (e.stateMask & SWT.SHIFT) != 0;
								break;
							}
						}
						setCanvasCursor(zoomin ? CURSOR_MPLUS : CURSOR_GRABBING, -1);
					}
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				mouseDown = false;
				if (e.button != 2 && ((e.stateMask & SWT.ALT) == 0) && cursorOverImage(e.x, e.y))
					setCanvasCursor(null, SWT.CURSOR_ARROW);
				else
					setCanvasCursor(CURSOR_OPEN_HAND, -1);
			}
		});
		animatedGallery.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (mouseButton != 2 && ((e.stateMask & SWT.ALT) == 0) && cursorOverImage(e.x, e.y))
					setCanvasCursor(null, SWT.CURSOR_ARROW);
				else {
					if (mouseDown) {
						int zoomKey = Platform.getPreferencesService().getInt(UiActivator.PLUGIN_ID,
								PreferenceConstants.ZOOMKEY, PreferenceConstants.ZOOMALT, null);
						boolean zoomin = false;
						if (mouseButton == 2)
							zoomin = zoomKey == PreferenceConstants.ZOOMRIGHT;
						else if (mouseButton == 1) {
							int modMask = (zoomKey == PreferenceConstants.ZOOMALT) ? SWT.ALT : SWT.SHIFT;
							zoomin = (e.stateMask & modMask) != modMask;
						}
						if (zoomin)
							setCanvasCursor(CURSOR_GRABBING, -1);
						else {
							setCanvasCursor(e.x >= lastMouseX ? CURSOR_MPLUS : CURSOR_MMINUS, -1);
							if (Math.abs(lastMouseX - e.x) > 3)
								unhookContextMenu();
							lastMouseX = e.x;
						}
					} else {
						setCanvasCursor(CURSOR_OPEN_HAND, -1);
						hookContextMenu();
					}
				}
			}
		});
		addKeyListener();
		addExplanationListener(false);
		setFocus();
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(animatedGallery.getControl(), HelpContextIds.SLEEVES_VIEW);
		// Drop-Unterstützung
		addDragDropSupport();
		// Hover
		installHoveringController();
		addCueListener();
		// Actions
		makeActions(getViewSite().getActionBars());
		installListeners(parent);
		hookContextMenu();
		contributeToActionBars();
		setDecorator(animatedGallery.getControl(), new ZuiGalleryDecorateJob(this, animatedGallery));
		updateActions(false);
	}

	protected void setCanvasCursor(String customCursor, int systemCursor) {
		if (customCursor == null) {
			if (currentSystemCursor != systemCursor || currentCustomCursor != null) {
				animatedGallery.setCursor(animatedGallery.getDisplay().getSystemCursor(systemCursor));
				currentSystemCursor = systemCursor;
				currentCustomCursor = null;
			}
		} else if (currentCustomCursor != customCursor || currentSystemCursor >= 0) {
			animatedGallery
					.setCursor(UiActivator.getDefault().getCursor(animatedGallery.getDisplay(), customCursor));
			currentSystemCursor = -1;
			currentCustomCursor = customCursor;
		}
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(action4);
		manager.add(action6);
		manager.add(action8);
		manager.add(action10);
		manager.add(action12);
		manager.add(action15);
		manager.add(action18);
		manager.add(action21);
		manager.add(action24);
		manager.add(new Separator());
		super.fillLocalPullDown(manager);
	}

	@Override
	protected void makeActions(IActionBars bars) {
		super.makeActions(bars);
		action4 = new ColumnAction(4);
		action6 = new ColumnAction(6);
		action8 = new ColumnAction(8);
		action10 = new ColumnAction(10);
		action12 = new ColumnAction(12);
		action15 = new ColumnAction(15);
		action18 = new ColumnAction(18);
		action21 = new ColumnAction(21);
		action24 = new ColumnAction(24);
		if (columns <= 4)
			action4.setChecked(true);
		else if (columns <= 6)
			action6.setChecked(true);
		else if (columns <= 8)
			action8.setChecked(true);
		else if (columns <= 10)
			action10.setChecked(true);
		else if (columns <= 12)
			action12.setChecked(true);
		else if (columns <= 15)
			action15.setChecked(true);
		else if (columns <= 18)
			action18.setChecked(true);
		else if (columns <= 21)
			action21.setChecked(true);
		else
			action24.setChecked(true);
	}

	@Override
	public void setFocus() {
		animatedGallery.setFocus();
	}

	@Override
	protected boolean doRedrawCollection(Collection<? extends Asset> assets, QueryField node) {
		if (animatedGallery == null || animatedGallery.isDisposed())
			return false;
		IAssetProvider assetProvider = getAssetProvider();
		if (assetProvider != null) {
			int showLabel = Constants.INHERIT_LABEL;
			String labelTemplate = null;
			SmartCollection coll = assetProvider.getCurrentCollection();
			int labelFontsize = 0;
			if (coll != null) {
				while (true) {
					showLabel = coll.getShowLabel();
					if (showLabel != Constants.INHERIT_LABEL) {
						labelTemplate = coll.getLabelTemplate();
						labelFontsize = coll.getFontSize();
						break;
					}
					if (coll.getSmartCollection_subSelection_parent() == null)
						break;
					coll = coll.getSmartCollection_subSelection_parent();
				}
				if (showLabel == Constants.INHERIT_LABEL) {
					String groupId = coll.getGroup_rootCollection_parent();
					Group group = Core.getCore().getDbManager().obtainById(GroupImpl.class, groupId);
					while (group != null) {
						showLabel = group.getShowLabel();
						if (showLabel != Constants.INHERIT_LABEL) {
							labelTemplate = group.getLabelTemplate();
							labelFontsize = group.getFontSize();
							break;
						}
						group = group.getGroup_subgroup_parent();
					}
				}
			}
			if (showLabel == Constants.INHERIT_LABEL) {
				showLabel = showLabelDflt;
				labelTemplate = labelTemplateDflt;
				labelFontsize = labelFontsizeDflt;
			}
			animatedGallery.setAppearance(showLabel, labelTemplate, labelFontsize);
		}
		if (assets == null) {
			if (assetProvider != null) {
				if (fetchAssets()) {
					SmartCollectionImpl selectedCollection = getNavigationHistory().getSelectedCollection();
					boolean reset = currentCollection != selectedCollection;
					currentCollection = selectedCollection;
					animatedGallery.setPersonFilter(selectedCollection == null ? null
							: selectedCollection.getSystem() && selectedCollection.getAlbum()
									? selectedCollection.getStringId()
									: null);
					scoreFormatter = assetProvider.getScoreFormatter();
					animatedGallery.setScoreFormatter(scoreFormatter);
					if (forceExternalSelection && selection instanceof AssetSelection) {
						forceExternalSelection = false;
						animatedGallery.setSelection((AssetSelection) selection, assetProvider);
					} else
						animatedGallery.setCollection(assetProvider.getAssets(), reset);
					if (currentSystemCursor == SWT.CURSOR_APPSTARTING)
						setCanvasCursor(null, SWT.CURSOR_ARROW);
				}
			}
			setFocus();
		} else
			animatedGallery.update(assets);
		return true;
	}

	public void setSelection(ISelection sel) {
		assetsChanged();
	}

	@Override
	public boolean assetsChanged() {
		if (isVisible()) {
			AssetSelection assetSelection = getNavigationHistory().getSelectedAssets();
			animatedGallery.setSelection(assetSelection, getAssetProvider());
			selection = assetSelection;
		}
		forceExternalSelection = true;
		return true;
	}

	@Override
	protected void addKeyListener() {
		Control control = getControl();
		if (control != null && !control.isDisposed())
			control.addTraverseListener(this);
	}

	public void handleEvent(Event event) {
		switch (event.type) {
		case SWT.KeyUp:
			switch (event.keyCode) {
			case 13:
				if ((event.stateMask & SWT.SHIFT) != 0)
					editWithAction.run();
				else
					editAction.run();
				break;
			case SWT.TAB:
				viewImageAction.runWithEvent(event);
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
				ZoomActionFactory.rate(animatedGallery.getSelection().getAssets(), this, event.keyCode - '0');
				break;
			case SWT.DEL:
				ZoomActionFactory.rate(animatedGallery.getSelection().getAssets(), this, RatingDialog.DELETE);
				break;
			}
			break;
		case SWT.MouseDoubleClick:
			viewImageAction.runWithEvent(event);
			break;
		case SWT.Selection:
			if (refreshing <= 0) {
				stopAudio();
				selection = animatedGallery.getSelection();
				fireSelection();
			}
			break;
		case SWT.SetData:
			int value = event.keyCode;
			if (event.detail == AnimatedGallery.REGION)
				editRegionName((ImageRegion) event.data);
			else {
				Asset asset = (Asset) event.data;
				if (asset != null)
					switch (event.detail) {
					case AnimatedGallery.RATE:
						ZoomActionFactory.rate(Collections.singletonList(asset), this, value);
						break;
					case AnimatedGallery.CODE:
						colorCode(asset, value);
						break;
					case AnimatedGallery.SHOWLOCATION:
						showLocation(asset, (event.stateMask & SWT.SHIFT) == SWT.SHIFT);
						break;
					case AnimatedGallery.ROTATE:
						rotate(asset, value);
						break;
					case AnimatedGallery.VOICENOTE:
						String voiceFileURI = asset.getVoiceFileURI();
						if (voiceFileURI != null && !voiceFileURI.startsWith("?")) //$NON-NLS-1$
							UiActivator.getDefault().playVoicenote(asset);
						break;
					case AnimatedGallery.STATUS:
						setStatus(asset, value);
						break;
					}
			}
			break;
		case SWT.Modify:
			OperationJob.executeOperation(new RenameAssetOperation((Asset) event.data, event.text, true), this);
			break;
		case SWT.Verify:
			setStatusMessage(event.text, true);
			break;
		}
	}

	@Override
	public void dispose() {
		animatedGallery.dispose();
		super.dispose();
	}

	public boolean cursorOverImage(int x, int y) {
		return findObject(x, y) != null;
	}

	@Override
	protected void setAssetSelection(AssetSelection assetSelection) {
		animatedGallery.setSelection(assetSelection, getAssetProvider());
		selection = assetSelection;
		fireSelection();
	}

	public Control getControl() {
		return animatedGallery.getControl();
	}

	@Override
	public String getTooltip(int mx, int my) {
		return animatedGallery.getTooltip(mx, my);
	}

	@Override
	public Asset findObject(MouseEvent event) {
		return findObject(event.x, event.y);
	}

	public Asset findObject(int x, int y) {
		return animatedGallery.getItem(x, y);
	}

	@Override
	public ImageRegion[] findAllRegions(MouseEvent event) {
		return animatedGallery.findAllRegions(event);
	}

	@Override
	public ImageRegion findBestFaceRegion(int x, int y, boolean all) {
		return animatedGallery.getBestFaceRegion(x, y, all, null);
	}

	public AssetSelection getAssetSelection() {
		return animatedGallery.getSelection();
	}

	@Override
	protected int getSelectionCount(boolean local) {
		if (local) {
			int i = 0;
			for (Asset a : getAssetSelection())
				if (a.getFileState() != IVolumeManager.PEER && ++i >= 2)
					return i;
			return i;
		}
		return animatedGallery.getSelectionCount();
	}

	@Override
	public void themeChanged() {
		animatedGallery.themeChanged();
	}

	@Override
	protected void setDefaultPartName() {
		setPartName(Messages.getString("ZuiView.sleeves")); //$NON-NLS-1$
	}

	protected void setPreferences() {
		IPreferenceStore preferenceStore = applyPreferences();
		preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				if (PreferenceConstants.SHOWLABEL.equals(property)
						|| PreferenceConstants.THUMBNAILTEMPLATE.equals(property)
						|| PreferenceConstants.LABELFONTSIZE.equals(property))
					applyPreferences();
			}
		});
	}

	protected IPreferenceStore applyPreferences() {
		final IPreferenceStore preferenceStore = UiActivator.getDefault().getPreferenceStore();
		showLabelDflt = preferenceStore.getInt(PreferenceConstants.SHOWLABEL);
		labelTemplateDflt = preferenceStore.getString(PreferenceConstants.THUMBNAILTEMPLATE);
		labelFontsizeDflt = preferenceStore.getInt(PreferenceConstants.LABELFONTSIZE);
		return preferenceStore;
	}

}