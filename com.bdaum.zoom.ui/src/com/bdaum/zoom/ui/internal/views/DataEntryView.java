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

package com.bdaum.zoom.ui.internal.views;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.operations.UndoRedoActionGroup;

import com.bdaum.aoModeling.runtime.AomObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.CatalogAdapter;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IFormatter;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.IndexedMember;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.MultiModifyAssetOperation;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.dialogs.AbstractListCellEditorDialog;
import com.bdaum.zoom.ui.internal.dialogs.CategoryDialog;
import com.bdaum.zoom.ui.internal.dialogs.CodeCellEditorDialog;
import com.bdaum.zoom.ui.internal.dialogs.CodesDialog;
import com.bdaum.zoom.ui.internal.dialogs.IndexedMemberDialog;
import com.bdaum.zoom.ui.internal.dialogs.KeywordDialog;
import com.bdaum.zoom.ui.internal.dialogs.LargeTextCellEditorDialog;
import com.bdaum.zoom.ui.internal.dialogs.ListCellEditorDialog;
import com.bdaum.zoom.ui.internal.dialogs.MixedBagCellEditorDialog;
import com.bdaum.zoom.ui.internal.dialogs.StructEditDialog;
import com.bdaum.zoom.ui.internal.dialogs.SupplementalCategoryDialog;
import com.bdaum.zoom.ui.internal.job.SupplyPropertyJob;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.DateComponent;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class DataEntryView extends BasicView implements IFieldUpdater, IMenuListener{

	public class SpinnerComponent extends Composite {

		private StackLayout stackLayout;
		private Spinner spinner;
		private Label label;

		public SpinnerComponent(Composite parent, int style) {
			super(parent, style);
			stackLayout = new StackLayout();
			setLayout(stackLayout);
			spinner = new Spinner(this, style);
			label = new Label(this, SWT.BORDER);
			label.setAlignment(SWT.CENTER);
			stackLayout.topControl = spinner;
		}

		/**
		 * @return
		 * @see com.bdaum.zoom.ui.widgets.DateInput#getDate()
		 */
		public Integer getSelection() {
			return spinner == stackLayout.topControl ? spinner.getSelection() : null;
		}

		/**
		 * @param value
		 * @see com.bdaum.zoom.ui.widgets.DateInput#setDate(java.util.Date)
		 */
		public void setSelection(Object value) {
			if (value instanceof Integer) {
				spinner.setSelection((Integer) value);
				stackLayout.topControl = spinner;
			} else {
				label.setText(value.toString());
				stackLayout.topControl = label;
			}
			layout(true, true);
		}

		/**
		 * @param listener
		 * @see com.bdaum.zoom.ui.widgets.DateInput#addSelectionListener(org.eclipse.swt.events.SelectionListener)
		 */
		public void addSelectionListener(SelectionListener listener) {
			spinner.addSelectionListener(listener);
		}

		/**
		 * @param enabled
		 * @see com.bdaum.zoom.ui.widgets.DateInput#setEnabled(boolean)
		 */
		@Override
		public void setEnabled(boolean enabled) {
			spinner.setEnabled(enabled);
			super.setEnabled(enabled);
		}

		/**
		 * @param listener
		 * @see org.eclipse.swt.widgets.Control#addKeyListener(org.eclipse.swt.events.KeyListener)
		 */
		@Override
		public void addKeyListener(KeyListener listener) {
			spinner.addKeyListener(listener);
		}

		/**
		 * @param listener
		 * @see org.eclipse.swt.widgets.Spinner#removeSelectionListener(org.eclipse.swt.events.SelectionListener)
		 */
		public void removeSelectionListener(SelectionListener listener) {
			spinner.removeSelectionListener(listener);
		}

		/**
		 * @param value
		 * @see org.eclipse.swt.widgets.Spinner#setDigits(int)
		 */
		public void setDigits(int value) {
			spinner.setDigits(value);
		}

		/**
		 * @param value
		 * @see org.eclipse.swt.widgets.Spinner#setIncrement(int)
		 */
		public void setIncrement(int value) {
			spinner.setIncrement(value);
		}

		/**
		 * @param value
		 * @see org.eclipse.swt.widgets.Spinner#setMaximum(int)
		 */
		public void setMaximum(int value) {
			spinner.setMaximum(value);
		}

		/**
		 * @param value
		 * @see org.eclipse.swt.widgets.Spinner#setMinimum(int)
		 */
		public void setMinimum(int value) {
			spinner.setMinimum(value);
		}

		/**
		 * @param value
		 * @see org.eclipse.swt.widgets.Spinner#setPageIncrement(int)
		 */
		public void setPageIncrement(int value) {
			spinner.setPageIncrement(value);
		}

		/**
		 * @param listener
		 * @see org.eclipse.swt.widgets.Control#removeKeyListener(org.eclipse.swt.events.KeyListener)
		 */
		@Override
		public void removeKeyListener(KeyListener listener) {
			spinner.removeKeyListener(listener);
		}
	}

	public static final String ID = "com.bdaum.zoom.ui.DataEntryView"; //$NON-NLS-1$
	private static final String EXIFPREFIX = "EXIF "; //$NON-NLS-1$
	private static final int ASYNCTHRESHOLD = 2;
	private static final String FIELDDECO = "fieldDecoration"; //$NON-NLS-1$ ;
	private static final String EDITBUTTON = "editButton"; //$NON-NLS-1$ ;
	private static final String DEC_PENDING = "pending"; //$NON-NLS-1$
	private static final String DEC_MIXED = "mixed"; //$NON-NLS-1$
	private static final String CLICK_TO_VIEW_DETAILS = Messages.getString("DataEntryView.press_button_details"); //$NON-NLS-1$

	public class IntArrayLabelProvider extends ZColumnLabelProvider {

		private final QueryField qfield;

		public IntArrayLabelProvider(QueryField qfield) {
			this.qfield = qfield;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof Integer) {
				int v = ((Integer) element);
				int[] enumeration = (int[]) qfield.getEnumeration();
				String[] labels = qfield.getEnumLabels();
				for (int i = 0; i < enumeration.length; i++)
					if (v == enumeration[i])
						return labels[i];
			}
			return element.toString();
		}
	}

	public class StringArrayLabelProvider extends ZColumnLabelProvider {

		private final QueryField qfield;

		public StringArrayLabelProvider(QueryField qfield) {
			this.qfield = qfield;
		}

		@Override
		public String getText(Object element) {
			String v = String.valueOf(element);
			String[] enumeration = (String[]) qfield.getEnumeration();
			String[] labels = qfield.getEnumLabels();
			for (int i = 0; i < enumeration.length; i++)
				if (v.equals(enumeration[i]))
					return labels[i];
			return element.toString();
		}
	}

	public class IntArrayContentProvider implements IStructuredContentProvider {

		private final QueryField qfield;

		public IntArrayContentProvider(QueryField qfield) {
			this.qfield = qfield;
		}

		public void dispose() {
			// do nothing
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}

		public Object[] getElements(Object inputElement) {
			int[] enumeration = (int[]) qfield.getEnumeration();
			Object[] elements = new Object[enumeration.length];
			for (int i = 0; i < elements.length; i++)
				elements[i] = enumeration[i];
			return elements;
		}

	}

	private Map<QueryField, Object> widgetMap = new HashMap<QueryField, Object>(150);
	private Map<QueryField, Object> valueMap = new HashMap<QueryField, Object>(150);
	private Map<QueryField, Object> oldValueMap = new HashMap<QueryField, Object>(150);
	private Composite container;
	private Action saveAction;
	private Action restoreAction;
	// protected boolean dirty;
	private Set<QueryField> updateSet = ConcurrentHashMap.newKeySet(150);
	private TabFolder tabFolder;
	private List<Asset> dirtyAssets;

	@Override
	public void createPartControl(Composite parent) {
		FieldDecorationRegistry.getDefault().registerFieldDecoration(DEC_MIXED,
				Messages.getString("DataEntryView.mixed_values"), Icons.MIXED_OVERLAY.getImage()); //$NON-NLS-1$
		FieldDecorationRegistry.getDefault().registerFieldDecoration(DEC_PENDING,
				Messages.getString("DataEntryView.pending"), Icons.PENDING_OVERLAY.getImage()); //$NON-NLS-1$
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		createTabFolder(container);
		fillValues();
		addKeyListener();
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), HelpContextIds.DATAENTRY_VIEW);
		makeActions();
		installListeners();
		hookContextMenu();
		contributeToActionBars();
		final QueryField.Visitor qfvisitor = new QueryField.Visitor() {
			@Override
			public void doVisitorWork(QueryField node) {
				updateValue(node);
			}
		};
		Core.getCore().addCatalogListener(new CatalogAdapter() {

			@Override
			public void assetsModified(BagChange<Asset> changes, final QueryField node) {
				for (Asset asset : getNavigationHistory().getSelectedAssets().getAssets()) {
					if (changes == null || changes.hasChanged(asset)) {
						Shell shell = getSite().getShell();
						if (!shell.isDisposed())
							shell.getDisplay().asyncExec(() -> {
								if (!shell.isDisposed()) {
									resetCaches();
									if (node != null)
										qfvisitor.visit(node);
									else
										refresh();
								}
							});
						break;
					}
				}
			}
		});
		getSite().getWorkbenchWindow().getPartService().addPartListener(new IPartListener2() {

			public void partVisible(IWorkbenchPartReference partRef) {
				// do nothing
			}

			public void partOpened(IWorkbenchPartReference partRef) {
				// do nothing
			}

			public void partInputChanged(IWorkbenchPartReference partRef) {
				// do nothing
			}

			public void partHidden(IWorkbenchPartReference partRef) {
				resetCaches();
			}

			public void partDeactivated(IWorkbenchPartReference partRef) {
				// do nothing
			}

			public void partClosed(IWorkbenchPartReference partRef) {
				resetCaches();
			}

			public void partBroughtToTop(IWorkbenchPartReference partRef) {
				// do nothing
			}

			public void partActivated(IWorkbenchPartReference partRef) {
				// do nothing
			}
		});
		updateActions(true);

	}

	private void contributeToActionBars() {
		undoContext = PlatformUI.getWorkbench().getOperationSupport().getUndoContext();
		IViewSite viewSite = getViewSite();
		IActionBars bars = viewSite.getActionBars();
		fillLocalToolbar(bars.getToolBarManager());
		fillLocalPullDown(bars.getMenuManager());
		UndoRedoActionGroup undoRedoGroup = new UndoRedoActionGroup(viewSite, undoContext, true);
		undoRedoGroup.fillActionBars(bars);
	}

	private void fillLocalToolbar(IToolBarManager manager) {
		manager.add(saveAction);
		manager.add(restoreAction);
	}

	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(saveAction);
		manager.add(restoreAction);
	}

	protected void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		getControl().setMenu(menuMgr.createContextMenu(getControl()));
		getSite().registerContextMenu(menuMgr, this);
	}
	
	public void menuAboutToShow(IMenuManager manager) {
		fillContextMenu(manager);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		saveAction = new Action(Messages.getString("DataEntryView.save"), Icons.save.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				if (dirtyAssets != null) {
					OperationJob.executeOperation(new MultiModifyAssetOperation(valueMap, oldValueMap, dirtyAssets),
							DataEntryView.this);
					dirtyAssets = null;
					updateActions(false);
					refresh();
				}
			}
		};
		saveAction.setToolTipText(Messages.getString("DataEntryView.apply_changes")); //$NON-NLS-1$
		restoreAction = new Action(Messages.getString("DataEntryView.restore"), Icons.refresh.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				valueMap.clear();
				fillValues();
				dirtyAssets = null;
				updateActions(false);
			}
		};
		restoreAction.setToolTipText(Messages.getString("DataEntryView.restore_all_fields")); //$NON-NLS-1$

	}

	private void fillValues() {
		List<Asset> selectedAssets = getNavigationHistory().getSelectedAssets().getAssets();
		boolean analog = true;
		Object mediaSupport = null;
		if (!selectedAssets.isEmpty()) {
			for (Asset asset : selectedAssets) {
				analog &= asset.getFileSource() != Constants.FILESOURCE_DIGITAL_CAMERA
						&& asset.getFileSource() != Constants.FILESOURCE_SIGMA_DIGITAL_CAMERA;
				IMediaSupport ms = CoreActivator.getDefault().getMediaSupport(asset.getFormat());
				if (ms != mediaSupport)
					mediaSupport = mediaSupport == null ? ms : QueryField.VALUE_MIXED;
			}
		}
		TabItem analogItem = tabFolder.getItem(2);
		if (mediaSupport instanceof IMediaSupport)
			analogItem.setText(((IMediaSupport) mediaSupport).getName());
		else
			analogItem.setText(
					analog ? Messages.getString("DataEntryView.analog") : Messages.getString("DataEntryView.n_a")); //$NON-NLS-1$ //$NON-NLS-2$
		for (QueryField qfield : widgetMap.keySet())
			updateValue(qfield);
	}

	private void updateValue(QueryField qfield) {
		updateField(qfield, getFieldEntry(qfield));
	}

	@SuppressWarnings("unused")
	private void createTabFolder(Composite parent) {
		tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite composite_1 = createTabItem(tabFolder, QueryField.IMAGE_ALL.getLabel(),
				Messages.getString("DataEntryView.general"), //$NON-NLS-1$
				Messages.getString("DataEntryView.general_description")); //$NON-NLS-1$

		FormData fd_fileGroup = new FormData();
		fd_fileGroup.top = new FormAttachment(0, 10);
		fd_fileGroup.left = new FormAttachment(0, 10);
		Composite fileGroup = createGroup(composite_1, 2, QueryField.IMAGE_FILE.getLabel(), fd_fileGroup);

		createField(fileGroup, QueryField.NAME, SWT.NONE, 1, 600);

		FormData fd_grpFinance = new FormData();
		fd_grpFinance.top = new FormAttachment(fileGroup, 10);
		fd_grpFinance.left = new FormAttachment(fileGroup, 0, SWT.LEFT);
		Composite grpFinance = createGroup(composite_1, 2, QueryField.IMAGE_FINANCE.getLabel(), fd_grpFinance);

		createField(grpFinance, QueryField.PRICE, SWT.NONE, 1, SWT.DEFAULT);

		createField(grpFinance, QueryField.SALES, SWT.NONE, 1, 80);

		createField(grpFinance, QueryField.EARNINGS, SWT.NONE, 1, 180);

		FormData fd_zoraGroup = new FormData();
		fd_zoraGroup.left = new FormAttachment(fileGroup, 0, SWT.LEFT);
		fd_zoraGroup.top = new FormAttachment(grpFinance, 10);
		Composite zoraGroup = createGroup(composite_1, 4, QueryField.IMAGE_ZOOM.getLabel(), fd_zoraGroup);

		createField(zoraGroup, QueryField.CONTENTTYPE, SWT.NONE, 3, SWT.DEFAULT);
		createField(zoraGroup, QueryField.STATUS, SWT.NONE, 3, SWT.DEFAULT);
		ComboViewer ratingField = (ComboViewer) createField(zoraGroup, QueryField.RATING, SWT.NONE, 1, SWT.DEFAULT);
		final Text ratedByField = (Text) createField(zoraGroup, QueryField.RATEDBY, SWT.NONE, 1, 100);
		ratingField.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (!ratedByField.isDisposed() && !updateSet.contains(QueryField.RATING)
						&& !updateSet.contains(QueryField.RATEDBY)) {
					Integer rating = (Integer) ((IStructuredSelection) event.getSelection()).getFirstElement();
					if (rating != null && rating >= 0) {
						String text = ratedByField.getText().trim();
						if (text.isEmpty() || "-".equals(text)) //$NON-NLS-1$
							ratedByField.setText(System.getProperty("user.name")); //$NON-NLS-1$
					}
				}
			}
		});

		createField(zoraGroup, QueryField.COLORCODE, SWT.NONE, 3, SWT.DEFAULT);
		createField(zoraGroup, QueryField.SAFETY, SWT.NONE, 3, SWT.DEFAULT);
		createField(zoraGroup, QueryField.USERFIELD1, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP, 3, SWT.DEFAULT);
		createField(zoraGroup, QueryField.USERFIELD2, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP, 3, SWT.DEFAULT);

		Composite composite_2 = createTabItem(tabFolder, QueryField.EXIF_ALL.getLabel(),
				Messages.getString("DataEntryView.exif_data"), //$NON-NLS-1$
				Messages.getString("DataEntryView.exif_description")); //$NON-NLS-1$

		FormData fd_imageDataGroup = new FormData();
		fd_imageDataGroup.top = new FormAttachment(0, 10);
		fd_imageDataGroup.left = new FormAttachment(0, 10);
		fd_imageDataGroup.right = new FormAttachment(80, -10);
		Composite imageDataGroup = createGroup(composite_2, 2, QueryField.EXIF_IMAGE.getLabel(), fd_imageDataGroup);

		createField(imageDataGroup, QueryField.EXIF_IMAGEDESCRIPTION, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI, 1,
				SWT.DEFAULT);
		createField(imageDataGroup, QueryField.EXIF_COPYRIGHT, SWT.NONE, 1, SWT.DEFAULT);
		createField(imageDataGroup, QueryField.EXIF_FILESOURCE, SWT.NONE, 1, SWT.DEFAULT);

		FormData fd_grpGps = new FormData();
		fd_grpGps.top = new FormAttachment(imageDataGroup, 10);
		fd_grpGps.left = new FormAttachment(imageDataGroup, 0, SWT.LEFT);
		fd_grpGps.right = new FormAttachment(imageDataGroup, 0, SWT.RIGHT);
		Composite grpGps = createGroup(composite_2, 2, QueryField.EXIF_GPS.getLabel(), fd_grpGps);

		createField(grpGps, QueryField.EXIF_GPSLONGITUDE, SWT.NONE, 1, 80);
		createField(grpGps, QueryField.EXIF_GPSLATITUDE, SWT.NONE, 1, 80);
		createField(grpGps, QueryField.EXIF_GPSALTITUDE, SWT.NONE, 1, 60);
		createField(grpGps, QueryField.EXIF_GPSSPEED, SWT.NONE, 1, 60);

		Composite composite_4 = createTabItem(tabFolder, Messages.getString("DataEntryView.analog"), //$NON-NLS-1$
				Messages.getString("DataEntryView.editable_metadata"), //$NON-NLS-1$
				Messages.getString("DataEntryView.analog_description")); //$NON-NLS-1$
		FormData fd_analogImageGroup = new FormData();
		fd_analogImageGroup.right = new FormAttachment(80, -10);
		fd_analogImageGroup.top = new FormAttachment(0, 10);
		fd_analogImageGroup.left = new FormAttachment(0, 10);
		Composite analogImageGroup = createGroup(composite_4, 2, QueryField.IMAGE_IMAGE.getLabel(),
				fd_analogImageGroup);

		createField(analogImageGroup, QueryField.ANALOGTYPE, SWT.NONE, 1, SWT.DEFAULT);
		createField(analogImageGroup, QueryField.ANALOGFORMAT, SWT.NONE, 1, SWT.DEFAULT);
		createField(analogImageGroup, QueryField.ANALOGPROCESSING, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP, 1, SWT.DEFAULT);
		createField(analogImageGroup, QueryField.COLORTYPE, SWT.NONE, 1, SWT.DEFAULT);

		createField(analogImageGroup, QueryField.EMULSION, SWT.NONE, 1, SWT.DEFAULT);

		FormData fd_exifImageGroup = new FormData();
		fd_exifImageGroup.top = new FormAttachment(analogImageGroup, 10);
		fd_exifImageGroup.left = new FormAttachment(analogImageGroup, 0, SWT.LEFT);
		Composite exifImageGroup = createGroup(composite_4, 2, EXIFPREFIX + QueryField.EXIF_IMAGE.getLabel(),
				fd_exifImageGroup);

		createField(exifImageGroup, QueryField.EXIF_DATETIMEORIGINAL, SWT.NONE, 1, SWT.DEFAULT);
		createField(exifImageGroup, QueryField.EXIF_MAKE, SWT.NONE, 1, SWT.DEFAULT);
		createField(exifImageGroup, QueryField.EXIF_MODEL, SWT.NONE, 1, SWT.DEFAULT);
		createField(exifImageGroup, QueryField.EXIF_SOFTWARE, SWT.NONE, 1, SWT.DEFAULT);

		FormData fd_auxGroup = new FormData();
		fd_auxGroup.top = new FormAttachment(exifImageGroup, 10);
		fd_auxGroup.left = new FormAttachment(exifImageGroup, 0, SWT.LEFT);
		fd_auxGroup.right = new FormAttachment(exifImageGroup, 0, SWT.RIGHT);
		Composite auxGroup = createGroup(composite_4, 2, QueryField.EXIF_AUX.getLabel(), fd_auxGroup);

		createField(auxGroup, QueryField.EXIF_SERIAL, SWT.NONE, 1, SWT.DEFAULT);
		createField(auxGroup, QueryField.EXIF_LENS, SWT.NONE, 1, SWT.DEFAULT);
		createField(auxGroup, QueryField.EXIF_LENSSERIAL, SWT.NONE, 1, SWT.DEFAULT);

		FormData fd_exifCameraGroup = new FormData();
		fd_exifCameraGroup.top = new FormAttachment(exifImageGroup, 0, SWT.TOP);
		fd_exifCameraGroup.left = new FormAttachment(exifImageGroup, 10);
		fd_exifCameraGroup.right = new FormAttachment(analogImageGroup, 0, SWT.RIGHT);
		Composite exifCameraGroup = createGroup(composite_4, 6, EXIFPREFIX + QueryField.EXIF_CAMERA.getLabel(),
				fd_exifCameraGroup);

		createField(exifCameraGroup, QueryField.EXIF_FOCALLENGTH, SWT.NONE, 1, 80);
		createField(exifCameraGroup, QueryField.EXIF_FOCALLENGTHIN35MMFILM, SWT.NONE, 1, SWT.DEFAULT);
		createField(exifCameraGroup, QueryField.EXIF_FOCALLENGTHFACTOR, SWT.NONE, 1, 50);
		createField(exifCameraGroup, QueryField.EXIF_MAXLENSAPERTURE, SWT.NONE, 1, 50);
		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);

		createField(exifCameraGroup, QueryField.EXIF_FNUMBER, SWT.NONE, 1, 50);
		createField(exifCameraGroup, QueryField.EXIF_EXPOSURETIME, SWT.NONE, 1, 80);
		createField(exifCameraGroup, QueryField.EXIF_LV, SWT.NONE, 1, 50);
		createField(exifCameraGroup, QueryField.EXIF_EXPOSUREINDEX, SWT.NONE, 1, 50);
		createField(exifCameraGroup, QueryField.EXIF_EXPOSUREBIAS, SWT.NONE, 1, 50);
		createField(exifCameraGroup, QueryField.EXIF_EXPOSUREMODE, SWT.NONE, 1, SWT.DEFAULT);
		createField(exifCameraGroup, QueryField.EXIF_EXPOSUREPROGRAM, SWT.NONE, 1, SWT.DEFAULT);
		createField(exifCameraGroup, QueryField.EXIF_METERINGMODE, SWT.NONE, 1, SWT.DEFAULT);
		createField(exifCameraGroup, QueryField.EXIF_LIGHTSOURCE, SWT.NONE, 1, SWT.DEFAULT);
		createField(exifCameraGroup, QueryField.EXIF_WHITEBALANCE, SWT.NONE, 1, SWT.DEFAULT);
		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);

		SpinnerComponent scalarSpeedField = (SpinnerComponent) createField(exifCameraGroup,
				QueryField.SCALAR_ISOSPEEDRATINGS, SWT.NONE, 1, SWT.DEFAULT);
		scalarSpeedField.setMaximum(512000);
		scalarSpeedField.setMinimum(1);
		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);

		Composite flashGroup = createGroup(exifCameraGroup, 6, QueryField.EXIF_FLASH.getLabel(),
				new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));
		createField(flashGroup, QueryField.EXIF_FLASHFUNCTION, SWT.NONE, 1, SWT.DEFAULT);
		new Label(flashGroup, SWT.NONE);
		new Label(flashGroup, SWT.NONE);
		new Label(flashGroup, SWT.NONE);
		new Label(flashGroup, SWT.NONE);
		createField(flashGroup, QueryField.EXIF_FLASHFIRED, SWT.NONE, 1, SWT.DEFAULT);
		createField(flashGroup, QueryField.EXIF_FLASHAUTO, SWT.NONE, 1, SWT.DEFAULT);
		createField(flashGroup, QueryField.EXIF_FLASHEXPOSURECOMP, SWT.NONE, 1, SWT.DEFAULT);
		createField(flashGroup, QueryField.EXIF_REDEYEREDUCTION, SWT.NONE, 1, SWT.DEFAULT);
		createField(flashGroup, QueryField.EXIF_RETURNLIGHTDETECTED, SWT.NONE, 1, SWT.DEFAULT);
		createField(flashGroup, QueryField.EXIF_FLASHENERGY, SWT.NONE, 1, SWT.DEFAULT);
		new Label(flashGroup, SWT.NONE);
		new Label(flashGroup, SWT.NONE);

		createField(exifCameraGroup, QueryField.EXIF_SUBJECTDISTANCE, SWT.NONE, 1, SWT.DEFAULT);
		createField(exifCameraGroup, QueryField.EXIF_FOV, SWT.NONE, 1, SWT.DEFAULT);
		createField(exifCameraGroup, QueryField.EXIF_SUBJECTDISTANCERANGE, SWT.NONE, 1, SWT.DEFAULT);
		createField(exifCameraGroup, QueryField.EXIF_CIRCLEOFCONFUSION, SWT.NONE, 1, SWT.DEFAULT);
		createField(exifCameraGroup, QueryField.EXIF_HYPERFOCALDISTANCE, SWT.NONE, 1, SWT.DEFAULT);
		createField(exifCameraGroup, QueryField.EXIF_DOF, SWT.NONE, 1, SWT.DEFAULT);

		createField(exifCameraGroup, QueryField.EXIF_SCENECAPTURETYPE, SWT.NONE, 1, SWT.DEFAULT);

		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);
		new Label(exifCameraGroup, SWT.NONE);

		Composite composite_3 = createTabItem(tabFolder, QueryField.IPTC_ALL.getLabel(),
				Messages.getString("DataEntryView.iptc"), //$NON-NLS-1$
				Messages.getString("DataEntryView.iptc_description")); //$NON-NLS-1$

		FormData fd_descriptionGroup = new FormData();
		fd_descriptionGroup.right = new FormAttachment(50);
		fd_descriptionGroup.top = new FormAttachment(0, 10);
		fd_descriptionGroup.left = new FormAttachment(0, 10);
		Composite descriptionGroup = createGroup(composite_3, 3, QueryField.IPTC_DESCRIPTION.getLabel(),
				fd_descriptionGroup);

		createField(descriptionGroup, QueryField.IPTC_TITLE, SWT.NONE, 2, SWT.DEFAULT);
		createField(descriptionGroup, QueryField.IPTC_HEADLINE, SWT.NONE, 2, SWT.DEFAULT);
		createField(descriptionGroup, QueryField.IPTC_JOBID, SWT.NONE, 2, SWT.DEFAULT);
		// createField(descriptionGroup, QueryField.IPTC_INTELLECTUAL_GENRE, SWT.NONE,
		// 2, SWT.DEFAULT);
		createField(descriptionGroup, QueryField.IPTC_INTELLECTUAL_GENRE, SWT.NONE, -1, SWT.DEFAULT);
		createField(descriptionGroup, QueryField.IPTC_WRITEREDITOR, SWT.NONE, 2, SWT.DEFAULT);
		createField(descriptionGroup, QueryField.IPTC_SPECIALINSTRUCTIONS, SWT.NONE, 2, SWT.DEFAULT);

		Label label = new Label(descriptionGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		Label label_3 = new Label(descriptionGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		createField(descriptionGroup, QueryField.IPTC_PERSONSHOWN, SWT.NONE, -1, SWT.DEFAULT);
		createField(descriptionGroup, QueryField.IPTC_MODELAGE, SWT.NONE, 2, SWT.DEFAULT);
		createField(descriptionGroup, QueryField.IPTC_MODELINFO, SWT.NONE, 2, SWT.DEFAULT);

		Label label_1 = new Label(descriptionGroup, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.RIGHT);
		label_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		Label label_4 = new Label(descriptionGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_4.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		createField(descriptionGroup, QueryField.IPTC_NAMEOFORG, SWT.NONE, 2, SWT.DEFAULT);
		createField(descriptionGroup, QueryField.IPTC_CODEOFORG, SWT.NONE, 2, SWT.DEFAULT);

		Label label_2 = new Label(descriptionGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		Label label_5 = new Label(descriptionGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_5.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		createField(descriptionGroup, QueryField.IPTC_ARTWORK, SWT.NONE, -1, SWT.DEFAULT);

		FormData fd_classificationGroup = new FormData();
		fd_classificationGroup.right = new FormAttachment(descriptionGroup, 0, SWT.RIGHT);
		fd_classificationGroup.top = new FormAttachment(descriptionGroup, 10);
		fd_classificationGroup.left = new FormAttachment(descriptionGroup, 0, SWT.LEFT);
		Composite classificationGroup = createGroup(composite_3, 3, QueryField.IPTC_CLASSIFICATION.getLabel(),
				fd_classificationGroup);

		createField(classificationGroup, QueryField.IPTC_URGENCY, SWT.NONE, 2, SWT.DEFAULT);
		createField(classificationGroup, QueryField.IPTC_CATEGORY, SWT.NONE, -1, SWT.DEFAULT);
		createField(classificationGroup, QueryField.IPTC_SUPPLEMENTALCATEGORIES, SWT.NONE, -1, SWT.DEFAULT);
		createField(classificationGroup, QueryField.IPTC_KEYWORDS, SWT.NONE, -1, SWT.DEFAULT);

		FormData fd_adminGroup = new FormData();
		fd_adminGroup.right = new FormAttachment(100, -10);
		fd_adminGroup.left = new FormAttachment(descriptionGroup, 10);
		fd_adminGroup.top = new FormAttachment(descriptionGroup, 0, SWT.TOP);
		Composite adminGroup = createGroup(composite_3, 3, QueryField.IPTC_ADMIN.getLabel(), fd_adminGroup);

		createField(adminGroup, QueryField.IPTC_EVENT, SWT.NONE, 2, SWT.DEFAULT);
		createField(adminGroup, QueryField.IPTC_LOCATIONCREATED, SWT.NONE, -1, SWT.DEFAULT);

		SpinnerComponent maximumWidthField = (SpinnerComponent) createField(adminGroup, QueryField.IPTC_MAXAVAILWIDTH,
				SWT.NONE, 2, SWT.DEFAULT);
		maximumWidthField.setPageIncrement(100);
		maximumWidthField.setIncrement(10);
		maximumWidthField.setMinimum(0);
		maximumWidthField.setMaximum(Integer.MAX_VALUE);
		SpinnerComponent maximumHeightField = (SpinnerComponent) createField(adminGroup, QueryField.IPTC_MAXAVAILHEIGHT,
				SWT.NONE, 2, SWT.DEFAULT);
		maximumHeightField.setPageIncrement(100);
		maximumHeightField.setIncrement(10);
		maximumHeightField.setMinimum(0);
		maximumHeightField.setMaximum(Integer.MAX_VALUE);

		FormData fd_originGroup = new FormData();
		fd_originGroup.right = new FormAttachment(adminGroup, 10, SWT.RIGHT);
		fd_originGroup.top = new FormAttachment(adminGroup, 10);
		fd_originGroup.left = new FormAttachment(adminGroup, 0, SWT.LEFT);
		Composite originGroup = createGroup(composite_3, 3, QueryField.IPTC_ORIGIN.getLabel(), fd_originGroup);
		createField(originGroup, QueryField.IPTC_DATECREATED, SWT.NONE, 2, SWT.DEFAULT);
		createField(originGroup, QueryField.IPTC_SCENECODE, SWT.NONE, -1, SWT.DEFAULT);
		createField(originGroup, QueryField.IPTC_SUBJECTCODE, SWT.NONE, -1, SWT.DEFAULT);
		createField(originGroup, QueryField.IPTC_LOCATIONSHOWN, SWT.NONE, -1, SWT.DEFAULT);

		FormData fd_rightsGroup = new FormData();
		fd_rightsGroup.right = new FormAttachment(originGroup, 0, SWT.RIGHT);
		fd_rightsGroup.top = new FormAttachment(originGroup, 10);
		fd_rightsGroup.left = new FormAttachment(originGroup, 0, SWT.LEFT);
		Composite rightsGroup = createGroup(composite_3, 3, QueryField.IPTC_RIGHTS.getLabel(), fd_rightsGroup);
		createField(rightsGroup, QueryField.IPTC_OWNER, SWT.NONE, -1, SWT.DEFAULT);
		createField(rightsGroup, QueryField.IPTC_BYLINE, SWT.NONE, -1, SWT.DEFAULT);
		createField(rightsGroup, QueryField.IPTC_BYLINETITLE, SWT.NONE, 2, SWT.DEFAULT);
		createField(rightsGroup, QueryField.IPTC_CONTACT, SWT.NONE, -1, SWT.DEFAULT);
		createField(rightsGroup, QueryField.IPTC_SOURCE, SWT.NONE, 2, SWT.DEFAULT);
		createField(rightsGroup, QueryField.IPTC_CREDITS, SWT.NONE, 2, SWT.DEFAULT);
		createField(rightsGroup, QueryField.IPTC_USAGE, SWT.NONE, 2, SWT.DEFAULT);
	}

	private static Composite createGroup(Composite parent, int columns, String label, Object layoutData) {
		CGroup group = new CGroup(parent, SWT.NONE);
		group.setText(label);
		if (layoutData != null)
			group.setLayoutData(layoutData);
		group.setLayout(new GridLayout(columns, false));
		return group;
	}

	private Composite createTabItem(TabFolder tabFolder, String label, String tooltip, String msg) {
		TabItem item = new TabItem(tabFolder, SWT.NONE);
		item.setToolTipText(tooltip);
		item.setText(label);
		Composite comp = new Composite(tabFolder, SWT.NONE);
		item.setControl(comp);
		comp.setLayout(new GridLayout());
		Label description = new Label(comp, SWT.WRAP);
		description.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		description.setText(msg);
		final ScrolledComposite sc = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final Composite innerComp = new Composite(sc, SWT.NONE);
		sc.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event e) {
				sc.removeListener(SWT.Resize,this);
				try {
					sc.setMinSize(innerComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				} finally {
					sc.addListener(SWT.Resize,this);
				}
			}
		});
		innerComp.setLayout(new FormLayout());
		sc.setContent(innerComp);
		addGestureListener(sc);
		return innerComp;
	}

	private Object createField(final Composite parent, final QueryField qfield, int style, int horSpan, int widthHint) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(qfield.getLabelWithUnit());
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, true, false, Math.abs(horSpan), 1);
		layoutData.horizontalIndent = 12;
		final int qtype = qfield.getType();
		switch (qtype) {
		case QueryField.T_BOOLEAN:
			final Button checkButton = new Button(parent, SWT.CHECK);
			checkButton.setData(UiConstants.LABEL, label);
			layoutData.grabExcessHorizontalSpace = false;
			checkButton.setLayoutData(layoutData);
			widgetMap.put(qfield, checkButton);
			checkButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					if (!updateSet.contains(qfield))
						putValue(qfield, checkButton.getSelection());
				}
			});
			return checkButton;
		case QueryField.T_DATE: {
			final DateComponent dateField = new DateComponent(parent, SWT.NONE);
			dateField.setData(UiConstants.LABEL, label);
			layoutData.widthHint = widthHint;
			dateField.setLayoutData(layoutData);
			dateField.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					if (!updateSet.contains(qfield))
						putValue(qfield, dateField.getSelection());
				}
			});
			widgetMap.put(qfield, dateField);
			return dateField;
		}
		case QueryField.T_CURRENCY: {
			IFormatter formatter = qfield.getFormatter();
			if (formatter != null) {
				final Text textField = new Text(parent, SWT.BORDER | style);
				textField.setData(UiConstants.LABEL, label);
				layoutData.widthHint = widthHint;
				textField.setLayoutData(layoutData);
				widgetMap.put(qfield, textField);
				textField.addListener(SWT.Modify, new Listener() {
					public void handleEvent(Event event) {
						if (!updateSet.contains(qfield)) {
							Object text = textField.getText();
							if (!QueryField.VALUE_MIXED.equals(text)) {
								try {
									putValue(qfield, text = qfield.getFormatter().parse((String) text));
									hideFieldDeco(textField);
								} catch (ParseException e) {
									showError(textField, e.getMessage());
								}
							}
						}
					}
				});
				return textField;
			}
			final SpinnerComponent spinner = new SpinnerComponent(parent, SWT.BORDER);
			spinner.setData(UiConstants.LABEL, label);
			spinner.setDigits(Format.getCurrencyDigits());
			spinner.setIncrement(1);
			spinner.setPageIncrement((int) Math.pow(10, Format.getCurrencyDigits()));
			spinner.setMinimum(0);
			spinner.setMaximum(Integer.MAX_VALUE);
			spinner.setLayoutData(layoutData);
			spinner.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					putCurrencyValue(qfield, spinner);
				}
			});
			spinner.addListener(SWT.KeyUp, new Listener() {
				@Override
				public void handleEvent(Event keyEvent) {
					switch (keyEvent.character) {
					case '+':
						spinner.setSelection(spinner.getSelection() + 1);
						putCurrencyValue(qfield, spinner);
						break;
					case '-':
						spinner.setSelection(spinner.getSelection() - 1);
						putCurrencyValue(qfield, spinner);
						break;
					}
				}
			});
			widgetMap.put(qfield, spinner);
			return spinner;
		}
		case QueryField.T_FLOAT:
		case QueryField.T_FLOATB:
		case QueryField.T_POSITIVEFLOAT: {
			final Text textField = new Text(parent, SWT.BORDER | style);
			textField.setData(UiConstants.LABEL, label);
			layoutData.horizontalAlignment = widthHint == SWT.DEFAULT ? SWT.FILL : SWT.LEFT;
			layoutData.widthHint = widthHint;
			textField.setLayoutData(layoutData);
			textField.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					if (!updateSet.contains(qfield)) {
						String text = textField.getText().trim();
						if (text.isEmpty()) {
							putValue(qfield, Double.NaN);
							hideFieldDeco(textField);
						} else {
							NumberFormat nf = NumberFormat.getInstance();
							nf.setMaximumFractionDigits(8);
							try {
								double value = nf.parse(text).doubleValue();
								if (qtype == QueryField.T_POSITIVEFLOAT && value < 0)
									showError(textField, Messages.getString("DataEntryView.must_be_positive")); //$NON-NLS-1$
								else {
									putValue(qfield, value);
									hideFieldDeco(textField);
								}
							} catch (ParseException e) {
								showError(textField, Messages.getString("DataEntryView.bad_fp")); //$NON-NLS-1$
							}
						}
					}
				}
			});
			widgetMap.put(qfield, textField);
			return textField;
		}
		case QueryField.T_INTEGER:
		case QueryField.T_POSITIVEINTEGER:
		case QueryField.T_LONG:
		case QueryField.T_POSITIVELONG:
			if (qfield.getEnumeration() instanceof int[]) {
				final ComboViewer comboViewer = new ComboViewer(parent, SWT.NONE);
				comboViewer.getControl().setData(UiConstants.LABEL, label);
				layoutData.horizontalAlignment = widthHint == SWT.DEFAULT ? SWT.FILL : SWT.LEFT;
				comboViewer.getCombo().setLayoutData(layoutData);
				comboViewer.setLabelProvider(new IntArrayLabelProvider(qfield));
				comboViewer.setContentProvider(new IntArrayContentProvider(qfield));
				comboViewer.setInput(this);
				comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						if (!comboViewer.getControl().isDisposed() && !updateSet.contains(qfield)
								&& !QueryField.VALUE_MIXED.equals(comboViewer.getCombo().getText()))
							putValue(qfield, comboViewer.getStructuredSelection().getFirstElement());
					}
				});
				widgetMap.put(qfield, comboViewer);
				return comboViewer;
			}
			final SpinnerComponent spinner = new SpinnerComponent(parent, SWT.BORDER);
			spinner.setData(UiConstants.LABEL, label);
			spinner.setLayoutData(layoutData);
			spinner.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					putIntegerValue(qfield, spinner);
				}
			});
			spinner.addListener(SWT.KeyUp, new Listener() {
				@Override
				public void handleEvent(Event keyEvent) {
					switch (keyEvent.character) {
					case '+':
						spinner.setSelection(spinner.getSelection() + 1);
						putIntegerValue(qfield, spinner);
						break;
					case '-':
						spinner.setSelection(spinner.getSelection() - 1);
						putIntegerValue(qfield, spinner);
						break;
					}
				}
			});
			widgetMap.put(qfield, spinner);
			return spinner;
		case QueryField.T_OBJECT:
		case QueryField.T_LOCATION:
		case QueryField.T_CONTACT:
		case QueryField.T_STRING: {
			layoutData.horizontalAlignment = widthHint == SWT.DEFAULT ? SWT.FILL : SWT.LEFT;
			if (qfield.getEnumeration() instanceof String[]) {
				final ComboViewer comboViewer = new ComboViewer(parent, SWT.NONE);
				comboViewer.getControl().setData(UiConstants.LABEL, label);
				comboViewer.getCombo().setLayoutData(layoutData);
				widgetMap.put(qfield, comboViewer);
				comboViewer.setContentProvider(ArrayContentProvider.getInstance());
				comboViewer.setLabelProvider(new StringArrayLabelProvider(qfield));
				comboViewer.setInput(qfield.getEnumeration());
				comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						if (!updateSet.contains(qfield)
								&& !QueryField.VALUE_MIXED.equals(comboViewer.getCombo().getText()))
							putValue(qfield, ((IStructuredSelection) comboViewer.getSelection()).getFirstElement());
					}
				});
				return comboViewer;
			}
			if (qfield.getSpellingOptions() == ISpellCheckingService.NOSPELLING || horSpan < 0) {
				final Text textField = new Text(parent, SWT.BORDER | style | (horSpan < 0 ? SWT.READ_ONLY : SWT.NONE));
				textField.setData(UiConstants.LABEL, label);
				layoutData.widthHint = widthHint;
				if ((style & SWT.MULTI) != 0)
					layoutData.heightHint = 50;
				textField.setLayoutData(layoutData);
				widgetMap.put(qfield, textField);
				if (horSpan < 0) {
					Button editButton = new Button(parent, SWT.PUSH);
					textField.setData(EDITBUTTON, editButton);
					editButton.setToolTipText(NLS.bind(Messages.getString("DataEntryView.edit"), //$NON-NLS-1$
							qfield.getLabel()));
					editButton.setText("..."); //$NON-NLS-1$
					editButton.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event e) {
							Object result = handleEditButton(parent, qfield, valueMap.get(qfield));
							if (result != null) {
								if (result instanceof BagChange)
									result = ((BagChange<?>) result).getDisplay();
								putValue(qfield, result);
								String text = qfield.value2text(result, CLICK_TO_VIEW_DETAILS);
								textField.setText(text);
								updateActions(false);
							}
						}

					});
				} else {
					textField.addListener(SWT.Modify, new Listener() {
						public void handleEvent(Event event) {
							if (!updateSet.contains(qfield)) {
								Object text = textField.getText();
								if (!QueryField.VALUE_MIXED.equals(text)) {
									if (qfield.getFormatter() != null)
										try {
											text = qfield.getFormatter().parse((String) text);
											putValue(qfield, text);
											hideFieldDeco(textField);
										} catch (ParseException e) {
											showError(textField, e.getMessage());
										}
									else {
										putValue(qfield, text);
										hideFieldDeco(textField);
									}
								}
							}
						}
					});
				}
				return textField;
			}
			final CheckedText checkedTextField = new CheckedText(parent, SWT.BORDER | style);
			checkedTextField.setData(UiConstants.LABEL, label);
			checkedTextField.setSpellingOptions(10, qfield.getSpellingOptions());
			layoutData.horizontalAlignment = widthHint == SWT.DEFAULT ? SWT.FILL : SWT.LEFT;
			layoutData.widthHint = widthHint;
			if ((style & SWT.MULTI) != 0)
				layoutData.heightHint = 50;
			checkedTextField.setLayoutData(layoutData);
			checkedTextField.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event e) {
					if (!updateSet.contains(qfield)) {
						String text = checkedTextField.getText();
						if (!QueryField.VALUE_MIXED.equals(text)) {
							putValue(qfield, text);
							hideFieldDeco(checkedTextField);
						}
					}
				}
			});
			widgetMap.put(qfield, checkedTextField);
			return checkedTextField;
		}
		}
		return null;
	}

	private Object handleEditButton(final Composite parent, final QueryField qfield, Object override) {
		final int qtype = qfield.getType();
		if (qtype == QueryField.T_LOCATION || qtype == QueryField.T_OBJECT || qtype == QueryField.T_CONTACT) {
			Object value = override != null ? override : getFieldValue(qfield);
			if (value instanceof IndexedMember[]) {
				IndexedMemberDialog dialog = new IndexedMemberDialog(parent.getShell(), (IndexedMember[]) value,
						qfield);
				setDialogLocation(dialog);
				return (dialog.open() == Window.OK) ? dialog.getResult() : null;
			}
			StructEditDialog dialog = new StructEditDialog(parent.getShell(), (AomObject) value, qfield);
			setDialogLocation(dialog);
			return (dialog.open() == Window.OK) ? dialog.getResult() : null;
		}
		List<Asset> assets = getNavigationHistory().getSelectedAssets().getAssets();
		boolean multiple = assets.size() > 1;
		if (qfield == QueryField.IPTC_KEYWORDS) {
			Meta meta = Core.getCore().getDbManager().getMeta(true);
			Set<String> selectableKeywords = meta.getKeywords();
			KeywordDialog dialog = new KeywordDialog(parent.getShell(), Messages.getString("DataEntryView.keywords"), //$NON-NLS-1$
					(override instanceof String[] ? (String[]) override : getCommonItems(qfield)), selectableKeywords,
					assets);
			return (dialog.open() == Window.OK) ? dialog.getResult() : null;
		}
		if (qfield == QueryField.IPTC_CATEGORY) {
			CategoryDialog dialog = new CategoryDialog(parent.getShell(),
					override != null ? override : getFieldValue(qfield), SWT.SINGLE);
			return (dialog.open() == Window.OK) ? dialog.getResult() : null;
		}
		if (qfield == QueryField.IPTC_SUPPLEMENTALCATEGORIES) {
			SupplementalCategoryDialog dialog = new SupplementalCategoryDialog(parent.getShell(),
					override != null ? override : getCommonItems(qfield), multiple);
			return (dialog.open() == Window.OK) ? dialog.getResult() : null;
		}
		Object enumeration = qfield.getEnumeration();
		if (qfield.getCard() != 1) {
			AbstractListCellEditorDialog dialog = enumeration instanceof Integer
					? new CodeCellEditorDialog(parent.getShell(), override != null ? override : getFieldValue(qfield),
							qfield)
					: qfield.getCard() == QueryField.CARD_MODIFIABLEBAG
							? new MixedBagCellEditorDialog(parent.getShell(),
									override != null ? override : getCommonItems(qfield), qfield, multiple)
							: new ListCellEditorDialog(parent.getShell(),
									override != null ? override : getFieldValue(qfield), qfield);
			setDialogLocation(dialog);
			return (dialog.open() == Window.OK) ? dialog.getResult() : null;
		} else if (enumeration instanceof Integer) {
			CodesDialog dialog = new CodesDialog(parent.getShell(), qfield,
					(String) (override != null ? override : getFieldValue(qfield)), null);
			return (dialog.open() == Window.OK) ? dialog.getResult() : null;
		}
		List<Asset> selectedAssets = getNavigationHistory().getSelectedAssets().getAssets();
		Asset asset = selectedAssets.size() == 1 ? selectedAssets.get(0) : null;
		LargeTextCellEditorDialog dialog = new LargeTextCellEditorDialog(parent.getShell(),
				override != null ? override : getFieldValue(qfield), qfield, asset);
		return (dialog.open() == Window.OK) ? dialog.getResult() : null;
	}

	protected void setDialogLocation(Dialog dialog) {
		dialog.create();
		Point location = dialog.getShell().getLocation();
		location.x -= 25;
		location.y += 30;
		dialog.getShell().setLocation(location);
	}

	private void putCurrencyValue(final QueryField qfield, final SpinnerComponent spinner) {
		if (!updateSet.contains(qfield))
			putValue(qfield, spinner.getSelection() / (Math.pow(10, Format.getCurrencyDigits())));
	}

	private void putIntegerValue(final QueryField qfield, final SpinnerComponent spinner) {
		if (!updateSet.contains(qfield))
			putValue(qfield, spinner.getSelection());
	}

	protected void putValue(QueryField qfield, Object value) {
		if (!oldValueMap.containsKey(qfield))
			oldValueMap.put(qfield, getFieldValue(qfield));
		valueMap.put(qfield, value);
		dirtyAssets = getNavigationHistory().getSelectedAssets().getAssets();
		updateActions(false);
	}

	private String[] getCommonItems(QueryField qfield) {
		return qfield.getCommonItems(getNavigationHistory().getSelectedAssets().getAssets());
	}

	protected Object getFieldValue(QueryField qfield) {
		Object value = valueMap.get(qfield);
		if (value == null) {
			List<Asset> selectedAssets = getNavigationHistory().getSelectedAssets().getAssets();
			if (!selectedAssets.isEmpty())
				value = qfield.obtainFieldValue(selectedAssets, null);
			if (value != QueryField.VALUE_MIXED && value != QueryField.VALUE_NOTHING)
				return value;
		}
		return value;
	}

	protected void hideFieldDeco(Control control) {
		ControlDecoration fieldDec = (ControlDecoration) control.getData(FIELDDECO);
		if (fieldDec != null)
			fieldDec.hide();
	}

	protected void showError(Control control, String msg) {
		ControlDecoration fieldDec = getFieldDecoration(control);
		FieldDecoration deco = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		fieldDec.setImage(deco.getImage());
		fieldDec.setDescriptionText(msg);
		fieldDec.show();
	}

	private FieldEntry getFieldEntry(QueryField qfield) {
		List<Asset> selectedAssets = getNavigationHistory().getSelectedAssets().getAssets();
		if (selectedAssets.isEmpty())
			return FieldEntry.NOTHING;
		if (selectedAssets.size() > ASYNCTHRESHOLD) {
			new SupplyPropertyJob(qfield, selectedAssets, this).schedule();
			return FieldEntry.PENDING;
		}
		return new FieldEntry(qfield.obtainFieldValue(selectedAssets, null), qfield.isEditable(selectedAssets),
				qfield.isApplicable(selectedAssets));
	}

	@Override
	public void setFocus() {
		// do nothing
	}

	public ISelection getSelection() {
		return StructuredSelection.EMPTY;
	}

	public void setSelection(ISelection selection) {
		// do nothing
	}

	public Control getControl() {
		return container;
	}

	@Override
	public void updateActions(boolean force) {
		if (isVisible() || force) {
			saveAction.setEnabled(dirtyAssets != null);
			restoreAction.setEnabled(dirtyAssets != null);
			updateActions(-1, -1);
		}
	}

	@Override
	public boolean selectionChanged() {
		return false;
	}

	@Override
	public boolean assetsChanged() {
		resetCaches();
		return true;
	}

	@Override
	public void dispose() {
		resetCaches();
		super.dispose();
	}

	private void resetCaches() {
		cancelJobs(Constants.PROPERTYPROVIDER);
		checkDirty();
		valueMap.clear();
		oldValueMap.clear();
		updateSet.clear();
	}

	private void checkDirty() {
		if (dirtyAssets != null) {
			Shell shell = getSite().getShell();
			shell.setVisible(true);
			shell.setMinimized(false);
			shell.setActive();
			if (AcousticMessageDialog.openQuestion(shell, Messages.getString("DataEntryView.save_changes"), //$NON-NLS-1$
					Messages.getString("DataEntryView.uncommitted_changes"))) { //$NON-NLS-1$
				saveAction.run();
			}
			dirtyAssets = null;
		}
	}

	@Override
	public boolean collectionChanged() {
		return false;
	}

	@Override
	public void refresh() {
		fillValues();
	}

	@Override
	protected void updateStatusLine() {
		// do nothing
	}

	public boolean isDisposed() {
		return getControl().isDisposed();
	}

	public Display getDisplay() {
		return getControl().getDisplay();
	}

	public void updateField(QueryField qfield, FieldEntry fieldEntry) {
		Object widget = widgetMap.get(qfield);
		Control control = (widget instanceof ComboViewer ? ((ComboViewer) widget).getControl() : (Control) widget);
		if (!fieldEntry.applicable || (!fieldEntry.editable && fieldEntry != FieldEntry.PENDING)) {
			setControlVisible(control, false);
			return;
		}
		setControlVisible(control, true);
		updateSet.add(qfield);
		try {
			if (fieldEntry == FieldEntry.PENDING) {
				setControlEnabled(control, false);
				ControlDecoration fieldDec = getFieldDecoration(control);
				FieldDecoration deco = FieldDecorationRegistry.getDefault().getFieldDecoration(DEC_PENDING);
				fieldDec.setImage(deco.getImage());
				fieldDec.setDescriptionText(deco.getDescription());
				fieldDec.show();
			} else {
				Object value = fieldEntry.value;
				if (value == QueryField.VALUE_MIXED) {
					setControlEnabled(control,
							qfield.getCard() == 1 || qfield.getCard() == QueryField.CARD_MODIFIABLEBAG);
					ControlDecoration fieldDec = getFieldDecoration(control);
					FieldDecoration deco = FieldDecorationRegistry.getDefault().getFieldDecoration(DEC_MIXED);
					fieldDec.setImage(deco.getImage());
					fieldDec.setDescriptionText(deco.getDescription());
					fieldDec.show();
					if (control instanceof Combo)
						((Combo) control).setText(QueryField.VALUE_MIXED);
					else if (control instanceof Text)
						((Text) control).setText(QueryField.VALUE_MIXED);
					else if (control instanceof CheckedText)
						((CheckedText) control).setText(QueryField.VALUE_MIXED);
					else if (control instanceof DateComponent)
						((DateComponent) control).setSelection(QueryField.VALUE_MIXED);
					else if (control instanceof SpinnerComponent)
						((SpinnerComponent) control).setSelection(QueryField.VALUE_MIXED);
				} else {
					setControlEnabled(control, value != QueryField.VALUE_NOTHING);
					String text = qfield.value2text(value, CLICK_TO_VIEW_DETAILS);
					if (text != null && value != QueryField.VALUE_MIXED) {
						if (!text.isEmpty() && value != QueryField.VALUE_NOTHING && text != Format.MISSINGENTRYSTRING) {
							if (widget instanceof ComboViewer)
								((ComboViewer) widget).setSelection(new StructuredSelection(value));
							else if (control instanceof Text)
								((Text) control).setText(text);
							else if (control instanceof CheckedText)
								((CheckedText) control).setText(text);
							else if (control instanceof DateComponent)
								((DateComponent) control).setSelection(value);
							else if (control instanceof SpinnerComponent) {
								if (qfield.getType() == QueryField.T_CURRENCY)
									((SpinnerComponent) control).setSelection(
											(int) ((Double) value * Math.pow(10, Format.getCurrencyDigits()) + 0.5));
								else
									((SpinnerComponent) control).setSelection(value);
							}
						} else {
							if (widget instanceof ComboViewer)
								((ComboViewer) widget).setSelection(StructuredSelection.EMPTY);
							else if (control instanceof Text)
								((Text) control).setText(text);
							else if (control instanceof CheckedText)
								((CheckedText) control).setText(text);
							else if (control instanceof DateComponent)
								((DateComponent) control).setSelection(text);
							else if (control instanceof SpinnerComponent)
								((SpinnerComponent) control).setSelection(text);
						}
					}
					hideFieldDeco(control);
				}
			}
		} finally {
			updateSet.remove(qfield);
		}

	}

	private static void setControlEnabled(Control control, boolean enabled) {
		control.setEnabled(enabled);
		Button editButton = (Button) control.getData(EDITBUTTON);
		if (editButton != null)
			editButton.setEnabled(enabled);
	}

	private void setControlVisible(Control control, boolean visible) {
		control.setVisible(visible);
		Button editButton = (Button) control.getData(EDITBUTTON);
		if (editButton != null)
			editButton.setVisible(visible);
		Label label = (Label) control.getData(UiConstants.LABEL);
		if (label != null)
			label.setVisible(visible);
		if (!visible)
			hideFieldDeco(control);
	}

	private static ControlDecoration getFieldDecoration(final Control control) {
		ControlDecoration fieldDec = (ControlDecoration) control.getData(FIELDDECO);
		if (fieldDec == null) {
			fieldDec = new ControlDecoration(control, SWT.TOP | SWT.LEFT);
			fieldDec.setShowHover(true);
			control.setData(FIELDDECO, fieldDec);
		}
		return fieldDec;
	}

}
