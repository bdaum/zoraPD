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

package com.bdaum.zoom.ui.internal.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.GestureEvent;
import org.eclipse.swt.events.GestureListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import com.bdaum.zoom.cat.model.Meta_type;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IRelationDetector;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.IPreferenceUpdater;
import com.bdaum.zoom.core.internal.VolumeListener;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.css.internal.IThemeListener;
import com.bdaum.zoom.image.ImageStore;
import com.bdaum.zoom.image.internal.ImageCache;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.MultiModifyAssetOperation;
import com.bdaum.zoom.operations.internal.RenameAssetOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.actions.IViewAction;
import com.bdaum.zoom.ui.internal.PreferencesUpdater;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.hover.HoveringController;
import com.bdaum.zoom.ui.internal.hover.IGalleryHover;
import com.bdaum.zoom.ui.internal.job.DecorateJob;
import com.bdaum.zoom.ui.internal.views.AbstractGalleryView.ScaleContributionItem;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public abstract class BasicView extends ViewPart
		implements ISelectionProvider, EducatedSelectionListener, IThemeListener, ImageStore, KeyListener,
		TraverseListener, IHoverSubject, UiConstants, IPartListener2, VolumeListener, IUndoHost {

	private boolean isVisible = false; // true;
	protected boolean isDirty = false;
	private INavigationHistory navigationHistory;
	protected ListenerList<ISelectionChangedListener> listeners = new ListenerList<ISelectionChangedListener>();
	protected HoveringController hoveringController;
	private IPreferenceChangeListener colorProfilePreferenceListener;
	private DecorateJob decorator;
	private List<IHandlerActivation> activations = new ArrayList<IHandlerActivation>();
	private List<IViewAction> extraViewActions;
	protected List<RetargetAction> actions = new ArrayList<>();
	private IAction undoAction;
	private IAction redoAction;
	protected IUndoContext undoContext;
	private Image titleImage;
	private Image highlightedTitleImage;
	private ImageData imageData;

	public BasicView() {
		configureCache();
		colorProfilePreferenceListener = new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				if (PreferenceConstants.COLORPROFILE.equals(event.getKey())) {
					configureCache();
					refresh();
				}
			}
		};
		Ui.getUi().addPreferenceChangeListener(colorProfilePreferenceListener);
		CssActivator.getDefault().addThemeListener(this);
	}

	protected static void cancelJobs(Object family) {
		IJobManager jobManager = Job.getJobManager();
		jobManager.cancel(family);
		try {
			jobManager.join(family, null);
		} catch (OperationCanceledException | InterruptedException e) {
			// ignore
		}
	}

	protected INavigationHistory getNavigationHistory() {
		if (navigationHistory == null)
			navigationHistory = Ui.getUi().getNavigationHistory(getSite().getWorkbenchWindow());
		return navigationHistory;
	}

	protected static Asset getVoiceNoteAsset(Asset asset) {
		if (asset != null) {
			String voiceFileURI = asset.getVoiceFileURI();
			if (voiceFileURI != null && !voiceFileURI.isEmpty()) {
				int p = voiceFileURI.indexOf('\f');
				if (p >= 0) {
					if (p > 0)
						return asset;
				} else if (!voiceFileURI.startsWith("?")) //$NON-NLS-1$
					return asset;
			}
		}
		return null;
	}

	protected static boolean hasVoiceNote(AssetSelection assetSelection) {
		if (assetSelection != null)
			for (Asset asset : assetSelection)
				if (asset.getFileState() != IVolumeManager.PEER)
					return getVoiceNoteAsset(asset) != null;
		return false;
	}

	protected static boolean isMedia(AssetSelection assetSelection, int flags, boolean local) {
		if (assetSelection != null) {
			CoreActivator activator = CoreActivator.getDefault();
			for (Asset asset : assetSelection)
				if (!local || asset.getFileState() != IVolumeManager.PEER) {
					IMediaSupport mediaSupport = activator.getMediaSupport(asset.getFormat());
					if (mediaSupport != null) {
						if (!mediaSupport.testProperty(flags))
							return false;
					} else if ((flags & QueryField.PHOTO) == 0)
						return false;
				}
		}
		return true;
	}

	public static boolean isSetEqual(Collection<String> c1, Collection<String> c2) {
		return (c1 instanceof Set ? (Set<String>) c1 : new HashSet<String>(c1))
				.equals(c2 instanceof Set ? (Set<String>) c2 : new HashSet<String>(c2));
	}

	private static void configureCache() {
		ImageCache.presetCMS(Ui.getUi().getDisplayCMS());
	}

	public static void openPerspective(String id) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			if (activePage != null) {
				IPerspectiveDescriptor perspective = workbench.getPerspectiveRegistry().findPerspectiveWithId(id);
				if (perspective != null)
					activePage.setPerspective(perspective);
			}
		}
	}

	protected static boolean dbIsReadonly() {
		return Core.getCore().getDbManager().isReadOnly();
	}

	protected void installListeners() {
		getSite().getWorkbenchWindow().getPartService().addPartListener(this);
		hookSelectionService();
		Core.getCore().getVolumeManager().addVolumeListener(this);
	}

	protected void uninstallListeners() {
		getSite().getWorkbenchWindow().getPartService().removePartListener(this);
		unhookSelectionService();
		Core.getCore().getVolumeManager().removeVolumeListener(this);
	}

	protected void hookSelectionService() {
		getNavigationHistory();
		navigationHistory.registerSelectionProvider(this);
		navigationHistory.addSelectionListener(this);
		getSite().setSelectionProvider(this);
	}

	protected void unhookSelectionService() {
		if (navigationHistory != null) {
			navigationHistory.deregisterSelectionProvicder(this);
			navigationHistory.removeSelectionListener(this);
			getSite().setSelectionProvider(null);
		}
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	protected ScaleContributionItem scaleContributionItem;

	protected void show() {
		if (isDirty) {
			refreshBusy();
			isDirty = false;
		}
		launchDecorator();
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part == this)
			return;
		if (selectionChanged()) {
			if (isVisible)
				refreshBusy();
			else
				isDirty = true;
		}
	}

	public void filterChanged() {
		// do nothing
	}

	public void sortChanged() {
		// do nothing
	}

	public void cueChanged(Object o) {
		// do nothing
	}

	public void assetsChanged(IWorkbenchPart part, AssetSelection selectedAssets) {
		if (part == this)
			return;
		if (assetsChanged()) {
			if (isVisible)
				refreshBusy();
			else
				isDirty = true;
		}
	}

	public void collectionChanged(IWorkbenchPart part, IStructuredSelection sel) {
		if (collectionChanged()) {
			if (isVisible)
				refreshBusy();
			else
				isDirty = true;
		}
	}

	private Runnable refresher = new Runnable() {
		public void run() {
			refresh();
			updateActions(false);
		}
	};

	protected void refreshBusy() {
		BusyIndicator.showWhile(getSite().getShell().getDisplay(), refresher);
	}

	public void postSelection(Object object) {
		getNavigationHistory();
		if (navigationHistory != null)
			navigationHistory.postSelection(new StructuredSelection(object));
	}

	public abstract void updateActions(boolean force);

	public void updateActions(int imageCount, int localImageCount) {
		if (extraViewActions != null)
			for (IViewAction action : extraViewActions)
				action.setEnabled(dbIsReadonly(), imageCount, localImageCount);
	}

	public abstract boolean selectionChanged();

	public abstract boolean assetsChanged();

	public abstract boolean collectionChanged();

	public abstract void refresh();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(Shell.class))
			return getSite().getShell();
		if (adapter.equals(Control.class))
			return getControl();
		if (adapter.equals(IWorkbenchWindow.class))
			return getSite().getWorkbenchWindow();
		if (adapter.equals(IWorkbenchPage.class))
			return getSite().getPage();
		if (adapter.equals(IPreferenceUpdater.class))
			return new PreferencesUpdater();
		if (adapter.equals(BasicView.class))
			return this;
		if (adapter.equals(IRelationDetector[].class)) {
			Collection<IRelationDetector> relationDetectors = UiActivator.getDefault().getRelationDetectors();
			return relationDetectors.toArray(new IRelationDetector[relationDetectors.size()]);
		}
		if (AssetSelection.class.equals(adapter)) {
			getNavigationHistory();
			if (navigationHistory != null)
				return navigationHistory.getSelectedAssets();
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void dispose() {
		uninstallListeners();
		if (undoContext != null)
			PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().dispose(undoContext, true, true,
					true);
		for (RetargetAction action : actions)
			action.dispose();
		if (colorProfilePreferenceListener != null)
			Ui.getUi().removePreferenceChangeListener(colorProfilePreferenceListener);
		CssActivator.getDefault().removeThemeListener(this);
		if (navigationHistory != null)
			navigationHistory.removeSelectionListener(this);
		getSite().getWorkbenchWindow().getPartService().removePartListener(this);
		Core.getCore().getVolumeManager().removeVolumeListener(this);
		if (titleImage != null)
			titleImage.dispose();
		if (highlightedTitleImage != null)
			highlightedTitleImage.dispose();
		super.dispose();
	}

	public boolean isVisible() {
		return isVisible;
	}

	protected void fireSelection() {
		updateActions(false);
		updateStatusLine();
		Display display = getSite().getShell().getDisplay();
		final SelectionChangedEvent event = new SelectionChangedEvent(BasicView.this, getSelection());
		for (final ISelectionChangedListener listener : listeners)
			display.syncExec(() -> listener.selectionChanged(event)); // this must be a syncExec
	}

	protected void updateStatusLine() {
		// by default do nothing
	}

	protected void setStatusMessage(String text, boolean error) {
		UiActivator.getDefault().setMesssage(getViewSite().getActionBars().getStatusLineManager(), text, error);
	}

	protected CellEditor determineCellEditor(QueryField qfield, Composite parent, int maxTextLength) {
		int type = qfield.getType();
		if (type == QueryField.T_BOOLEAN)
			return new CheckboxCellEditor(parent);
		if (type == QueryField.T_LOCATION)
			return new StructCellEditor(parent, qfield);
		if (type == QueryField.T_OBJECT)
			return new StructCellEditor(parent, qfield);
		if (type == QueryField.T_CONTACT)
			return new StructCellEditor(parent, qfield);
		if (qfield.getEnumLabels() != null)
			return new ViewComboCellEditor(parent, qfield);
		if (qfield == QueryField.IPTC_KEYWORDS)
			return new KeywordDialogCellEditor(parent);
		if (qfield == QueryField.IPTC_CATEGORY)
			return new CategoryDialogCellEditor(parent, SWT.SINGLE);
		if (qfield == QueryField.IPTC_SUPPLEMENTALCATEGORIES)
			return new SupplementalCategoryDialogCellEditor(parent);
		if (qfield == QueryField.SALES)
			return new IncrementalNumberCellEditor(parent, 0, 1);
		if (qfield == QueryField.PRICE)
			return new IncrementalNumberCellEditor(parent, Format.getCurrencyDigits(), 1);
		if (qfield == QueryField.EARNINGS)
			return new CurrencyExpressionCellEditor(parent);
		if (qfield.getCard() != 1)
			return qfield.getCard() == QueryField.CARD_MODIFIABLEBAG ? new MixedBagCellEditor(parent, qfield)
					: new ListCellEditor(parent, qfield);
		if (qfield.getType() == QueryField.T_DATE)
			return new DateTimeCellEditor(parent, qfield);
		if (qfield.getEnumeration() instanceof Integer)
			return new TopicTextCellEditor(parent, qfield);
		List<Asset> selectedAssets = getNavigationHistory().getSelectedAssets().getAssets();
		Asset asset = selectedAssets.size() == 1 ? selectedAssets.get(0) : null;
		if (qfield.getMaxlength() > maxTextLength && qfield.getType() == QueryField.T_STRING)
			return new LargeTextCellEditor(parent, qfield, asset);
		return new ViewTextCellEditor(parent, qfield, asset);
	}

	protected void updateAssets(Object value, Object oldvalue, QueryField qfield) {
		List<Asset> selectedAssets = getNavigationHistory().getSelectedAssets().getAssets();
		if (qfield == QueryField.NAME) {
			if (selectedAssets.size() == 1)
				OperationJob.executeOperation(new RenameAssetOperation(selectedAssets.get(0), (String) value, true),
						this);
		} else
			OperationJob.executeOperation(new MultiModifyAssetOperation(qfield, value, oldvalue, selectedAssets), this);
	}

	protected void updateAssetsIfNecessary(QueryField qfield, Object value, Object oldvalue) {
		if (value instanceof BagChange && !((BagChange<?>) value).hasChanges())
			return;
		if (oldvalue instanceof String[] && value instanceof String[]) {
			if (isSetEqual(Arrays.asList((String[]) oldvalue), Arrays.asList((String[]) value)))
				return;
		} else if ((oldvalue == null || oldvalue.equals(value)) && (oldvalue != null || value == null))
			return;
		updateAssets(value, oldvalue, qfield);
	}

	protected void installHoveringController() {
		hoveringController = new HoveringController(this);
		hoveringController.install();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.IHoverSubject#findObject(int, int)
	 */

	public Object findObject(MouseEvent event) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.internal.views.IHoverSubject#findRegions(int, int)
	 */
	public ImageRegion[] findAllRegions(MouseEvent event) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.internal.views.IHoverSubject#getTooltip(int, int)
	 */
	public String getTooltip(int mx, int my) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.IHoverSubject#getGalleryHover(org.eclipse.swt
	 * .events.MouseEvent)
	 */

	public IGalleryHover getGalleryHover(MouseEvent event) {
		return null;
	}

	public Image getImage(Object imageSource) {
		return Core.getCore().getImageCache().getImage(imageSource);
	}

	public void keyPressed(KeyEvent e) {
		// do nothing
	}

	public void keyReleased(KeyEvent e) {
		// do nothing
	}

	protected void addKeyListener() {
		Control control = getControl();
		if (control != null && !control.isDisposed()) {
			control.addKeyListener(this);
			control.addTraverseListener(this);
		}
	}

	protected void removeKeyListener() {
		Control control = getControl();
		if (control != null && !control.isDisposed()) {
			control.removeKeyListener(this);
			control.removeTraverseListener(this);
		}
	}

	public Control[] getControls() {
		return null;
	}

	public void keyTraversed(TraverseEvent e) {
		switch (e.detail) {
		case SWT.TRAVERSE_TAB_NEXT:
			if ((e.stateMask & SWT.CTRL) != 0)
				getNavigationHistory().traverse(getSite().getId(), 1);
			break;
		case SWT.TRAVERSE_TAB_PREVIOUS:
			if ((e.stateMask & SWT.CTRL) != 0)
				getNavigationHistory().traverse(getSite().getId(), -1);
			break;
		case SWT.TRAVERSE_NONE:
			return;
		}
		e.doit = false;
	}

	public Object getContent() {
		return null;
	}

	protected static boolean hasTimeLine() {
		return !Core.getCore().getDbManager().getMeta(true).getTimeline().equals(Meta_type.timeline_no);
	}

	protected boolean hasGeocode(AssetSelection assetSelection) {
		Asset asset = assetSelection == null ? null : assetSelection.getFirstElement();
		return asset != null && !Double.isNaN(asset.getGPSLatitude()) && !Double.isNaN(asset.getGPSLongitude());
	}

	protected void registerCommand(IAction action, String commandId) {
		if (action != null) {
			IHandlerService service = getSite().getService(IHandlerService.class);
			if (service != null) {
				action.setActionDefinitionId(commandId);
				activations.add(service.activateHandler(commandId, new ActionHandler(action)));
			}
		}
	}

	protected void addGestureListener(final Scrollable gallery) {
		gallery.addGestureListener(new GestureListener() {
			double previousMagnification = 1d;

			public void gesture(GestureEvent e) {
				if (e.detail == SWT.GESTURE_PAN) {
					ScrollBar horizontalBar = gallery.getHorizontalBar();
					if (horizontalBar != null)
						horizontalBar.setSelection(horizontalBar.getSelection() - e.xDirection);
					ScrollBar verticalBar = gallery.getVerticalBar();
					if (verticalBar != null)
						verticalBar.setSelection(verticalBar.getSelection() - e.yDirection);
				} else if (e.detail == SWT.GESTURE_BEGIN) {
					if (e.magnification == 1d)
						previousMagnification = 1d;
				} else if (e.detail == SWT.GESTURE_MAGNIFY) {
					if (scaleContributionItem != null) {
						scaleContributionItem.setSelection((int) (scaleContributionItem.getSelection()
								* Math.sqrt(e.magnification / previousMagnification) + 0.5d));
						previousMagnification = e.magnification;
					}
				}
			}
		});
	}

	public void partActivated(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false) == this) {
			deregisterCommands(); // Just to be sure
			registerCommands();
			setVisible(true);
			Display display = getSite().getShell().getDisplay();
			Image image = getTitleImage();
			if (titleImage != null)
				titleImage.dispose();
			if (imageData == null)
				imageData = image.getImageData();
			titleImage = new Image(display, imageData);
			if (highlightedTitleImage == null) {
				Rectangle bounds = image.getBounds();
				for (int y = bounds.height - 2; y < bounds.height; y++)
					for (int x = 0; x < bounds.width; x++) {
						imageData.setPixel(x, y, 0xff00);
						imageData.setAlpha(x, y, 255);
					}
				highlightedTitleImage = new Image(display, imageData);
				imageData = image.getImageData();
			}
			setTitleImage(highlightedTitleImage);
			updateActions(true);
			show();
		}
	}

	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false) == this) {
			setVisible(true);
			show();
		}
	}

	public void partClosed(IWorkbenchPartReference partRef) {
		// do nothing
	}

	public void partDeactivated(IWorkbenchPartReference partRef) {
		if (partRef != null && partRef.getPart(false) == this) {
			deregisterCommands();
			// viewActive = false;
			if (titleImage != null)
				setTitleImage(titleImage);
			updateActions(true);
		}
	}

	public void partHidden(IWorkbenchPartReference partRef) {
		if (partRef != null && partRef.getPart(false) == this)
			setVisible(false);
	}

	public void partInputChanged(IWorkbenchPartReference partRef) {
		// do nothing
	}

	public void partOpened(IWorkbenchPartReference partRef) {
		if (partRef != null && partRef.getPart(false) == this) {
			getNavigationHistory();
			if (navigationHistory != null) {
				if (navigationHistory.getSelectedCollection() != null)
					collectionChanged();
				if (!navigationHistory.getSelectedAssets().isEmpty())
					assetsChanged();
				if (!navigationHistory.getOtherSelection().isEmpty())
					selectionChanged();
			}
			refreshBusy();
		}
	}

	public void partVisible(IWorkbenchPartReference partRef) {
		if (partRef != null && partRef.getPart(false) == this) {
			setVisible(true);
			show();
		}
	}

	protected void setVisible(boolean visible) {
		isVisible = visible;
	}

	protected void registerCommands() {
		registerCommand(undoAction, IWorkbenchCommandConstants.EDIT_UNDO);
		registerCommand(redoAction, IWorkbenchCommandConstants.EDIT_REDO);
		if (extraViewActions != null)
			for (IViewAction action : extraViewActions) {
				action.setAdaptable(this);
				if (action.getActionDefinitionId() != null)
					registerCommand(action, action.getActionDefinitionId());
			}
	}

	private void deregisterCommands() {
		IHandlerService service = getSite().getService(IHandlerService.class);
		if (service != null)
			service.deactivateHandlers(activations);
		activations.clear();
	}

	/**
	 * @param control
	 *            - the control to be decorated
	 * @param decorator
	 *            - the decorator
	 */
	public void setDecorator(Control control, DecorateJob decorator) {
		this.decorator = decorator;
		control.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				launchDecorator();
			}
		});
		if (control instanceof Scrollable) {
			Scrollable scrollable = (Scrollable) control;
			ScrollBar verticalBar = scrollable.getVerticalBar();
			ScrollBar horizontalBar = scrollable.getHorizontalBar();
			if (verticalBar != null || horizontalBar != null) {
				SelectionListener scrollListener = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						launchDecorator();
					}
				};
				if (verticalBar != null)
					verticalBar.addSelectionListener(scrollListener);
				if (horizontalBar != null)
					horizontalBar.addSelectionListener(scrollListener);
			}
		}
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see
	 * com.bdaum.zoom.core.internal.VolumeListener#volumesChanged(java.io.File[] )
	 */
	public void volumesChanged(File[] roots) {
		launchDecorator();
	}

	protected void launchDecorator() {
		if (decorator != null) {
			decorator.cancel();
			decorator.schedule(300);
		}
	}

	public void themeChanged() {
		// By default do nothing
	}

	protected IAction addAction(IAction action) {
		if (action instanceof RetargetAction)
			actions.add((RetargetAction) action);
		return action;
	}

	protected void makeActions() {
		undoAction = addAction(ActionFactory.UNDO.create(getSite().getWorkbenchWindow()));
		redoAction = addAction(ActionFactory.REDO.create(getSite().getWorkbenchWindow()));
		extraViewActions = UiActivator.getDefault().getExtraViewActions(getSite().getId());
	}

	@Override
	public IUndoContext getUndoContext() {
		return undoContext;
	}

}