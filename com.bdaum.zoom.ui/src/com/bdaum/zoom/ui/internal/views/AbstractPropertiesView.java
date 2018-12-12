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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.operations.UndoRedoActionGroup;

import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.TrackRecord;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.CatalogAdapter;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.IndexedMember;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.MultiModifyAssetOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.ILocationDisplay;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.Icons.Icon;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.dialogs.MetadataLabelProvider;
import com.bdaum.zoom.ui.internal.job.SupplyPropertyJob;
import com.bdaum.zoom.ui.internal.preferences.MetadataPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public abstract class AbstractPropertiesView extends BasicView implements ISelectionProvider, IFieldUpdater {

	private final class DetailsViewerFilter extends ViewerFilter {
		@Override
		public boolean select(Viewer aViewer, Object parentElement, Object element) {
			if (element instanceof QueryField) {
				if (((QueryField) element).isHidden() || !isApplicable((QueryField) element))
					return false;
				switch (mode) {
				case MODE_SHOW_ESSENTIAL:
					return essentials.contains(element);
				case MODE_SHOW_EDITABLE:
					return isEditable((QueryField) element);
				}
			}
			return true;
		}
	}

	private final class ContentTypeViewerFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer aViewer, Object parentElement, Object element) {
			return (element instanceof QueryField) ? ((QueryField) element).testFlags(flags) : true;
		}
	}

	public class ViewEditingSupport extends EditingSupport {

		public ViewEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected boolean canEdit(Object element) {
			if (dbIsReadonly())
				return false;
			if (element instanceof IndexedMember)
				return true;
			if (element instanceof QueryField) {
				QueryField qfield = (QueryField) element;
				if (isEditable(qfield) && isApplicable(qfield) && qfield.getChildren().length == 0) {
					if (qfield.isStruct() && qfield.getCard() != 1)
						return false;
					Object value = getFieldValue(qfield);
					return (value != QueryField.VALUE_MIXED
							|| ((qfield.getCard() == 1 || qfield.getCard() == QueryField.CARD_MODIFIABLEBAG)
									&& qfield != QueryField.NAME))
							&& value != QueryField.VALUE_NOTHING && value != FieldEntry.PENDING.value;
				}
			}
			return false;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			if (element instanceof IndexedMember)
				element = ((IndexedMember) element).getQfield();
			CellEditor cellEditor = null;
			if (element instanceof QueryField) {
				cellEditor = determineCellEditor((QueryField) element, viewer.getTree(), 60);
				if (cellEditor instanceof AbstractMixedBagDialogCellEditor) {
					List<Asset> assets = getNavigationHistory().getSelectedAssets().getAssets();
					((AbstractMixedBagDialogCellEditor) cellEditor)
							.setCommonItems(((QueryField) element).getCommonItems(assets), assets);
				}
			}
			return cellEditor;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof IndexedMember)
				return ((IndexedMember) element).getValue();
			if (element instanceof QueryField) {
				QueryField qfield = (QueryField) element;
				Object fieldValue = getFieldValue(qfield);
				if (qfield.getCard() == QueryField.CARD_MODIFIABLEBAG)
					return fieldValue;
				if (fieldValue instanceof Integer && qfield.getEnumeration() == null)
					return String.valueOf(fieldValue);
				if (fieldValue instanceof Double) {
					NumberFormat nf = NumberFormat.getInstance();
					if (qfield.getType() == QueryField.T_CURRENCY) {
						int digits = Format.getCurrencyDigits();
						nf.setMaximumFractionDigits(digits);
						nf.setMinimumFractionDigits(digits);
					} else
						nf.setMaximumFractionDigits(qfield.getMaxlength());
					return nf.format(fieldValue);
				}
				if (fieldValue == QueryField.VALUE_MIXED)
					return ""; //$NON-NLS-1$
				if (fieldValue != null)
					return fieldValue;
				if (qfield.getCard() < 0)
					return EMPTYSTRINGS;
			}
			return ""; //$NON-NLS-1$
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (value == null)
				return;
			IndexedMember indexedMember = null;
			Object oldvalue = null;
			if (element instanceof IndexedMember) {
				indexedMember = (IndexedMember) element;
				oldvalue = indexedMember.getValue();
				element = indexedMember.getQfield();
			}
			if (element instanceof QueryField) {
				QueryField qfield = (QueryField) element;
				int type = qfield.getType();
				if (qfield.getKey() != null && type != QueryField.T_NONE) {
					if (qfield.getCard() == QueryField.CARD_MODIFIABLEBAG) {
						if (value instanceof BagChange)
							updateAssets(value, oldvalue = getFieldValue(qfield), qfield);
						return;
					}
					if (indexedMember == null)
						oldvalue = getFieldValue(qfield);
					switch (type) {
					case QueryField.T_INTEGER:
					case QueryField.T_POSITIVEINTEGER:
						if (!(value instanceof Integer))
							try {
								value = Integer.valueOf(Integer.parseInt(value.toString()));
							} catch (NumberFormatException e) {
								return;
							}
						break;
					case QueryField.T_LONG:
					case QueryField.T_POSITIVELONG:
						if (!(value instanceof Long))
							try {
								value = Long.valueOf(Long.parseLong(value.toString()));
							} catch (NumberFormatException e) {
								return;
							}
						break;
					case QueryField.T_BOOLEAN:
						if (!(value instanceof Boolean))
							value = Boolean.valueOf((Boolean.parseBoolean(value.toString())));
						break;
					case QueryField.T_FLOAT:
					case QueryField.T_FLOATB:
					case QueryField.T_POSITIVEFLOAT:
					case QueryField.T_CURRENCY:
						if (!(value instanceof Double)) {
							NumberFormat nf = NumberFormat.getInstance();
							nf.setMaximumFractionDigits(8);
							try {
								value = new Double(nf.parse(value.toString()).doubleValue());
							} catch (ParseException e) {
								return;
							}
						}
						break;
					case QueryField.T_DATE:
						if (value == QueryField.EMPTYDATE)
							value = null;
						else if (!(value instanceof Date))
							return;
					}
					updateAssetsIfNecessary(qfield, value, oldvalue);
				}
			}
		}
	}

	public class ValueColumnLabelProvider extends ZColumnLabelProvider {

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return property == VALUECOL;
		}

		public String getText(Object element) {
			if (element instanceof QueryField) {
				QueryField qfield = (QueryField) element;
				if (qfield.getType() == QueryField.T_NONE)
					return ""; //$NON-NLS-1$
				Object value = getFieldValue(qfield);
				if (value == FieldEntry.PENDING.value)
					return (String) value;
				String text = qfield.value2text(value, CLICK_TO_VIEW_DETAILS);
				if (text != null) {
					if (!text.isEmpty() && value != QueryField.VALUE_MIXED && value != QueryField.VALUE_NOTHING
							&& text != Format.MISSINGENTRYSTRING) {
						String unit = qfield.getUnit();
						if (unit != null)
							return new StringBuilder(text).append(' ').append(unit).toString();
						if (value instanceof String[] && ((String[]) value).length > 0)
							return UiUtilities.addExplanation(qfield, (String[]) value, text);
					}
					return text;
				}
			} else if (element instanceof IndexedMember)
				return QueryField.serializeStruct(((IndexedMember) element).getValue(), CLICK_TO_VIEW_DETAILS);
			else if (element instanceof TrackRecord)
				return QueryField.TRACK.getFormatter().toString(element);
			else if (element instanceof String) {
				String s = (String) element;
				int p = s.indexOf(':');
				if (p > 0)
					return s.substring(p + 1).trim();
			}
			return null;
		}
	}

	public class ActionColumnLabelProvider extends ZColumnLabelProvider {

		private final Rectangle ICONSIZE = Icons.query.getImage().getBounds();

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return property == IMAGECOL;
		}

		@Override
		public String getToolTipText(Object element) {
			if (element instanceof QueryField) {
				String tooltip = null;
				QueryField qfield = (QueryField) element;
				switch (qfield.getAction()) {
				case QueryField.ACTION_QUERY:
					tooltip = Messages.getString("AbstractPropertiesView.search_similar"); //$NON-NLS-1$
					break;
				case QueryField.ACTION_MAP:
					if (UiActivator.getDefault().getLocationDisplay() != null)
						tooltip = Messages.getString("AbstractPropertiesView.show_in_map"); //$NON-NLS-1$
					break;
				case QueryField.ACTION_WWW:
					tooltip = Messages.getString("AbstractPropertiesView.open_in_web_Browser"); //$NON-NLS-1$
					break;
				case QueryField.ACTION_EMAIL:
					tooltip = Messages.getString("AbstractPropertiesView.send_an_email"); //$NON-NLS-1$
					break;
				case QueryField.ACTION_TOFOLDER:
					tooltip = Messages.getString("AbstractPropertiesView.show_in_folder"); //$NON-NLS-1$
					break;
				}
				if (tooltip != null)
					return filterEmptySlots(qfield) ? tooltip : null;
			}
			return null;
		}

		public Image getImage(Object element) {
			if (element instanceof QueryField) {
				Icon icon = null;
				QueryField qfield = (QueryField) element;
				Object value = getFieldValue(qfield);
				if (value == null || value == FieldEntry.PENDING.value || value == QueryField.VALUE_MIXED
						|| value == QueryField.VALUE_NOTHING)
					return null;
				switch (qfield.getAction()) {
				case QueryField.ACTION_QUERY:
					icon = Icons.query;
					break;
				case QueryField.ACTION_MAP:
					if (UiActivator.getDefault().getLocationDisplay() != null)
						icon = Icons.map;
					break;
				case QueryField.ACTION_WWW:
					icon = Icons.www;
					break;
				case QueryField.ACTION_EMAIL:
					icon = Icons.email;
					break;
				case QueryField.ACTION_TOFOLDER:
					icon = Icons.folder;
					break;
				}
				if (icon != null)
					return filterEmptySlots(qfield) ? icon.getImage() : null;
			} else if (element instanceof TrackRecord && ((TrackRecord) element).getVisit() != null)
				return Icons.www.getImage();
			return null;
		}

		@Override
		protected Rectangle getIconBounds() {
			return ICONSIZE;
		}

	}

	private static final Object[] EMPTYOBJECTS = new Object[0];
	private static final String[] EMPTYSTRINGS = new String[0];

	public class MetadataContentProvider implements ITreeContentProvider {

		public void dispose() {
			// do nothing
		}

		public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
			// do nothing
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof QueryField) {
				QueryField qfield = (QueryField) parentElement;
				switch (qfield.getType()) {
				case QueryField.T_LOCATION:
					if (qfield == QueryField.IPTC_LOCATIONSHOWN)
						return createIndexedGroup(qfield, collectLocationsShown());
					return EMPTYOBJECTS;
				case QueryField.T_CONTACT:
					return EMPTYOBJECTS;
				case QueryField.T_STRING:
					if (qfield == QueryField.EXIF_MAKERNOTES) {
						Object v = getFieldValue(qfield);
						if (v == QueryField.VALUE_MIXED)
							return MIXEDARRAY;
						return (v instanceof String[]) ? (String[]) v : EMPTYOBJECTS;
					}
					if (qfield == QueryField.TRACK)
						return collectTracks();
					break;
				case QueryField.T_OBJECT:
					return createIndexedGroup(qfield, collectArtwork());
				}
				List<QueryField> fields = new ArrayList<QueryField>();
				for (QueryField field : qfield.getChildren())
					if (field.isUiField())
						fields.add(field);
				return fields.toArray();
			}
			return EMPTYOBJECTS;
		}

		public Object getParent(Object element) {
			if (element instanceof QueryField)
				return getFieldParent((QueryField) element);
			if (element instanceof IndexedMember)
				return getFieldParent(((IndexedMember) element).getQfield());
			return null;

		}

		public boolean hasChildren(Object element) {
			if (element instanceof QueryField) {
				QueryField qfield = (QueryField) element;
				if (qfield == QueryField.TRACK || qfield == QueryField.EXIF_MAKERNOTES) {
					Object fieldValue = getFieldValue(qfield);
					return (fieldValue instanceof String[]) && ((String[]) fieldValue).length > 0;
				}
				return (qfield.isStruct() && qfield.getCard() != 1) ? true : qfield.getChildren().length > 0;
			}
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return (inputElement instanceof QueryField) ? ((QueryField) inputElement).getChildren() : EMPTYOBJECTS;
		}
	}

	private static final String FILTER = "filter"; //$NON-NLS-1$
	private static final String EXPANDED = "expanded"; //$NON-NLS-1$
	private static final String SELECTED = "selected"; //$NON-NLS-1$

	static final String LABELCOL = "label"; //$NON-NLS-1$
	static final String VALUECOL = "value"; //$NON-NLS-1$
	static final String IMAGECOL = "image"; //$NON-NLS-1$
	static final String[] UPDATECOLS = new String[] { LABELCOL, VALUECOL, IMAGECOL };

	private static final String CLICK_TO_VIEW_DETAILS = Messages
			.getString("AbstractPropertiesView.click_to_view_details"); //$NON-NLS-1$

	private TreeViewer viewer;
	private IPreferenceChangeListener preferenceListener;
	private String expandedElements;
	private String selectedElement;

	private Action essentialAction;
	private Action allAction;

	protected int mode = MODE_SHOW_ALL;

	protected static final int MODE_SHOW_ALL = 0;
	protected static final int MODE_SHOW_ESSENTIAL = 1;
	protected static final int MODE_SHOW_EDITABLE = 2;

	private static final int ASYNCTHRESHOLD = 20;

	private Action deleteAction;

	private Action configureAction;

	private Action editableAction;

	protected Map<QueryField, FieldEntry> valueMap = new HashMap<QueryField, FieldEntry>();

	private Action fieldAction;

	private static Set<QueryField> essentials;

	private int flags;
	private Action expandAction;
	private Action collapseAction;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			Integer filt = memento.getInteger(FILTER);
			if (filt != null)
				mode = filt;
			expandedElements = memento.getString(EXPANDED);
			selectedElement = memento.getString(SELECTED);
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null) {
			memento.putInteger(FILTER, mode);
			StringBuilder sb = new StringBuilder();
			Object[] expandedElements = viewer.getExpandedElements();
			for (Object element : expandedElements) {
				String id = ((QueryField) element).getId();
				if (sb.length() > 0)
					sb.append(' ');
				sb.append(id);
			}
			memento.putString(EXPANDED, sb.toString());
			Object element = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			if (element instanceof QueryField)
				memento.putString(SELECTED, ((QueryField) element).getId());
		}
		super.saveState(memento);
	}

	private static final Object[] MIXEDARRAY = new Object[] { QueryField.VALUE_MIXED };

	public Object[] collectTracks() {
		Object v = QueryField.TRACK.obtainFieldValue(getNavigationHistory().getSelectedAssets().getAssets(), null);
		if (v == null)
			return EMPTYOBJECTS;
		if (v == QueryField.VALUE_MIXED)
			return MIXEDARRAY;
		String[] ids = (String[]) v;
		if (ids.length == 0)
			return EMPTYOBJECTS;
		List<TrackRecordImpl> records = new ArrayList<TrackRecordImpl>(ids.length);
		IDbManager dbManager = Core.getCore().getDbManager();
		for (int i = 0; i < ids.length; i++) {
			TrackRecordImpl record = dbManager.obtainById(TrackRecordImpl.class, ids[i]);
			if (record != null)
				records.add(record);
		}
		if (records.size() > 1)
			Collections.sort(records, new Comparator<TrackRecordImpl>() {
				public int compare(TrackRecordImpl t1, TrackRecordImpl t2) {
					return t1.getExportDate().compareTo(t2.getExportDate());
				}
			});
		return records.toArray();
	}

	public Object[] collectLocationsShown() {
		IDbManager dbManager = Core.getCore().getDbManager();
		Set<LocationImpl> result = new HashSet<LocationImpl>();
		for (Asset asset : getNavigationHistory().getSelectedAssets().getAssets()) {
			String assetId = asset.getStringId();
			final List<LocationShownImpl> set = dbManager.obtainStructForAsset(LocationShownImpl.class, assetId, false);
			if (set.isEmpty())
				return null;
			String[] ids = new String[set.size()];
			int i = 0;
			for (LocationShownImpl rel : set)
				ids[i++] = rel.getLocation();
			List<LocationImpl> set2 = dbManager.obtainStructByIds(assetId, LocationImpl.class, ids);
			if (set2.isEmpty())
				return null;
			if (result.isEmpty())
				result.addAll(set2);
			else
				result.retainAll(set2);
		}
		return result.toArray();
	}

	public Object[] createIndexedGroup(QueryField qfield, Object[] members) {
		int l = members == null ? 1 : members.length + 1;
		Object[] result = new Object[l];
		if (members != null)
			for (int i = 0; i < members.length; i++)
				result[i] = new IndexedMember(qfield, members[i], i);
		result[l - 1] = new IndexedMember(qfield, null, l - 1);
		return result;
	}

	public Object[] collectArtwork() {
		IDbManager dbManager = Core.getCore().getDbManager();
		Set<ArtworkOrObjectImpl> result = new HashSet<ArtworkOrObjectImpl>();
		for (Asset asset : getNavigationHistory().getSelectedAssets().getAssets()) {
			String assetId = asset.getStringId();
			final List<ArtworkOrObjectShownImpl> set = dbManager.obtainStructForAsset(ArtworkOrObjectShownImpl.class,
					assetId, false);
			if (set.isEmpty())
				return null;
			String[] ids = new String[set.size()];
			int i = 0;
			for (ArtworkOrObjectShownImpl rel : set)
				ids[i++] = rel.getArtworkOrObject();
			List<ArtworkOrObjectImpl> set2 = dbManager.obtainStructByIds(assetId, ArtworkOrObjectImpl.class, ids);
			if (set2.isEmpty())
				return null;
			if (result.isEmpty())
				result.addAll(set2);
			else
				result.retainAll(set2);
		}
		return result.toArray();
	}

	protected abstract Object getFieldParent(QueryField element);

	public abstract QueryField getRootElement();

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */

	@SuppressWarnings("unused")
	@Override
	public void createPartControl(Composite parent) {
		computeFlags();
		if (essentials == null) {
			essentials = new HashSet<QueryField>(100);
			computeEssentials();
		}
		preferenceListener = new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				if (PreferenceConstants.ESSENTIALMETADATA.equals(event.getKey())) {
					computeEssentials();
					refreshInternal();
				}
			}
		};
		InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID).addPreferenceChangeListener(preferenceListener);
		viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		int[] colWidth = getColumnWidths();
		TreeViewerColumn col1 = new TreeViewerColumn(viewer, SWT.NONE);
		final Tree tree = viewer.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		final ViewEditingSupport editingSupport = new ViewEditingSupport(viewer);
		col1.getColumn().setWidth(colWidth[0]);
		col1.getColumn().setText(Messages.getString("AbstractPropertiesView.Name")); //$NON-NLS-1$
		col1.setLabelProvider(new MetadataLabelProvider() {
			@Override
			public String getText(Object element) {
				return (editingSupport.canEdit(element)) ? Format.EDITABLEINDICATOR + super.getText(element)
						: super.getText(element);
			}
		});
		TreeViewerColumn col2 = new TreeViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setWidth(colWidth[1]);
		col2.getColumn().setText(Messages.getString("AbstractPropertiesView.Value")); //$NON-NLS-1$
		col2.setLabelProvider(new ValueColumnLabelProvider());
		col2.setEditingSupport(editingSupport);
		TreeViewerColumn col3 = new TreeViewerColumn(viewer, SWT.NONE);
		col3.getColumn().setWidth(colWidth[2]);
		col3.setLabelProvider(new ActionColumnLabelProvider());
		viewer.setContentProvider(new MetadataContentProvider());
		viewer.setComparator(ZViewerComparator.INSTANCE);
		viewer.setFilters(new ViewerFilter[] { new DetailsViewerFilter(), new ContentTypeViewerFilter() });
		ZColumnViewerToolTipSupport.enableFor(viewer);
		viewer.setInput(getRootElement());
		parent.getDisplay().asyncExec(() -> {
			if (!parent.isDisposed()) {
				if (expandedElements != null) {
					List<QueryField> elements = new ArrayList<QueryField>();
					StringTokenizer st = new StringTokenizer(expandedElements);
					while (st.hasMoreTokens()) {
						QueryField qf1 = QueryField.findQueryField(st.nextToken());
						if (qf1 != null)
							elements.add(qf1);
					}
					viewer.setExpandedElements(elements.toArray());
					expandedElements = null;
				} else
					viewer.expandToLevel(getExpandLevel());
				if (selectedElement != null) {
					QueryField qf2 = QueryField.findQueryField(selectedElement);
					if (qf2 != null)
						viewer.setSelection(new StructuredSelection(qf2), true);
					selectedElement = null;
				}
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateActions(false);
			}
		});
		UiUtilities.installDoubleClickExpansion(viewer);
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				int col = -1;
				int x = e.x;
				for (int i = 0; i < 3; i++) {
					int w = tree.getColumn(i).getWidth();
					if (x < w) {
						col = i;
						break;
					}
					x -= w;
				}
				if (col == 2) {
					TreeItem item = tree.getItem(new Point(e.x, e.y));
					Object element = item.getData();
					if (element instanceof QueryField) {
						QueryField qfield = (QueryField) element;
						if (qfield.getAction() != QueryField.ACTION_NONE)
							processAction(qfield, (e.stateMask & SWT.SHIFT) != 0);
					} else if (element instanceof TrackRecord) {
						String visit = ((TrackRecord) element).getVisit();
						if (visit != null)
							showWebUrl(visit);
					}
				}
			}
		});
		new ColumnLayoutManager(viewer, getColumnWidths(), getColumnMaxWidths());
		addKeyListener();
		addGestureListener(tree);
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), HelpContextIds.METADATA_VIEW);
		makeActions();
		installListeners();
		hookContextMenu();
		contributeToActionBars();
		final QueryField.Visitor qfVisitor = new QueryField.Visitor() {
			@Override
			public void doVisitorWork(QueryField node) {
				viewer.refresh(node);
			}
		};
		Core.getCore().addCatalogListener(new CatalogAdapter() {
			@Override
			public void assetsModified(BagChange<Asset> changes, final QueryField node) {
				for (Asset asset : getNavigationHistory().getSelectedAssets()) {
					if (changes == null || changes.hasChanged(asset)) {
						Shell shell = getSite().getShell();
						if (shell != null && !shell.isDisposed())
							shell.getDisplay().asyncExec(() -> {
								if (!tree.isDisposed()) {
									resetCaches();
									if (node != null)
										viewer.expandToLevel(node, qfVisitor.visit(node));
									else
										refresh();
								}
							});
						break;
					}
				}
			}
		});
		updateActions(false);
	}

	protected void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				AbstractPropertiesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(getControl());
		getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, this);
	}

	protected void fillContextMenu(IMenuManager manager) {
		updateActions(true);
		Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		if (firstElement instanceof QueryField) {
			Icon icon = null;
			String text = null;
			switch (((QueryField) firstElement).getAction()) {
			case QueryField.ACTION_QUERY:
				icon = Icons.query;
				text = Messages.getString("AbstractPropertiesView.search_similar"); //$NON-NLS-1$
				break;
			case QueryField.ACTION_MAP:
				if (UiActivator.getDefault().getLocationDisplay() != null) {
					icon = Icons.map;
					text = Messages.getString("AbstractPropertiesView.show_in_map"); //$NON-NLS-1$
				}
				break;
			case QueryField.ACTION_WWW:
				icon = Icons.www;
				text = Messages.getString("AbstractPropertiesView.open_in_web_Browser"); //$NON-NLS-1$
				break;
			case QueryField.ACTION_EMAIL:
				icon = Icons.email;
				text = Messages.getString("AbstractPropertiesView.send_an_email"); //$NON-NLS-1$
				break;
			case QueryField.ACTION_TOFOLDER:
				icon = Icons.folder;
				text = Messages.getString("AbstractPropertiesView.show_in_folder"); //$NON-NLS-1$
				break;
			}
			if (text != null && icon != null && filterEmptySlots((QueryField) firstElement)) {
				fieldAction.setImageDescriptor(icon.getDescriptor());
				fieldAction.setText(text);
				manager.add(fieldAction);
			}
		}
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public Control getControl() {
		return viewer.getControl();
	}

	private void computeEssentials() {
		essentials.clear();
		String ess = Platform.getPreferencesService().getString(UiActivator.PLUGIN_ID,
				PreferenceConstants.ESSENTIALMETADATA, "", null); //$NON-NLS-1$
		StringTokenizer st = new StringTokenizer(ess, "\n"); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			QueryField qfield = QueryField.findQueryField(st.nextToken());
			if (qfield != null && qfield.testFlags(flags))
				while (qfield != null) {
					essentials.add(qfield);
					qfield = qfield.getParent();
				}
		}
	}

	private boolean filterEmptySlots(QueryField qfield) {
		Object value = getFieldValue(qfield);
		if (value == null || value == QueryField.VALUE_NOTHING || value == QueryField.VALUE_MIXED
				|| value == FieldEntry.PENDING.value)
			return false;
		if (qfield.getEnumeration() != null && Format.MISSINGENTRYSTRING == qfield.formatScalarValue(value))
			return false;
		switch (qfield.getType()) {
		case QueryField.T_STRING:
			return !(value instanceof String && ((String) value).isEmpty());
		case QueryField.T_POSITIVEINTEGER:
			return !(value instanceof Integer && ((Integer) value).intValue() < 0);
		case QueryField.T_POSITIVELONG:
			return !(value instanceof Long && ((Long) value).longValue() < 0);
		case QueryField.T_POSITIVEFLOAT:
		case QueryField.T_CURRENCY:
		case QueryField.T_FLOAT:
		case QueryField.T_FLOATB:
			if (value instanceof Float)
				return !(((Float) value).isNaN());
			if (value instanceof Double)
				return !(((Double) value).isNaN());
			break;
		}
		return true;
	}

	protected void processAction(QueryField qfield, boolean shift) {
		switch (qfield.getAction()) {
		case QueryField.ACTION_QUERY:
			Object fieldValue = getFieldValue(qfield);
			SmartCollection sm = createAdhocQuery(qfield, fieldValue, shift);
			getNavigationHistory().postSelection(new StructuredSelection(sm));
			break;
		case QueryField.ACTION_TOFOLDER:
			Asset asset = getNavigationHistory().getSelectedAssets().getFirstElement();
			sm = Utilities.obtainFolderCollection(Core.getCore().getDbManager(), asset.getUri(), asset.getVolume());
			if (sm != null) {
				try {
					((CatalogView) getSite().getPage().showView(CatalogView.ID))
							.setSelection(new StructuredSelection(sm), true);
				} catch (PartInitException e1) {
					// should never happen
				}
			}
			break;
		case QueryField.ACTION_MAP:
			ILocationDisplay locationDisplay = UiActivator.getDefault().getLocationDisplay();
			if (locationDisplay != null) {
				Object lat = getFieldValue(QueryField.EXIF_GPSLATITUDE);
				Object lon = getFieldValue(QueryField.EXIF_GPSLONGITUDE);
				if (lat instanceof Double && lon instanceof Double) {
					double latitude = ((Double) lat).doubleValue();
					double longitude = ((Double) lon).doubleValue();
					if (!Double.isNaN(latitude) && !Double.isNaN(longitude)) {
						LocationImpl loc = new LocationImpl();
						loc.setLatitude(latitude);
						loc.setLongitude(longitude);
						locationDisplay.display(loc);
					}
				}
			}
			break;
		case QueryField.ACTION_WWW:
			fieldValue = getFieldValue(qfield);
			showWebUrl((String) fieldValue);
			break;
		case QueryField.ACTION_EMAIL:
			fieldValue = getFieldValue(qfield);
			sendMail(fieldValue);
			break;
		}
	}

	private static void sendMail(Object recipients) {
		List<String> to;
		if (recipients instanceof String)
			to = Core.fromStringList((String) recipients, ";"); //$NON-NLS-1$
		else if (recipients instanceof String[])
			to = Arrays.asList((String[]) recipients);
		else
			return;
		UiActivator.getDefault().sendMail(to);
	}

	private static void showWebUrl(String fieldValue) {
		try {
			showWebPage(fieldValue);
		} catch (PartInitException e) {
			UiActivator.getDefault()
					.logError(Messages.getString("AbstractPropertiesView.cannot_instantiate_external_web_browser"), e); //$NON-NLS-1$
		}
	}

	private static void showWebPage(String fieldValue) throws PartInitException {
		IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
		try {
			if (!fieldValue.startsWith("http://")) //$NON-NLS-1$
				fieldValue = "http://" + fieldValue; //$NON-NLS-1$
			browser.openURL(new URL(fieldValue));
		} catch (MalformedURLException e) {
			try {
				browser.openURL(new URL("http://www.google.com/search?q=\"" //$NON-NLS-1$
						+ fieldValue + "\"")); //$NON-NLS-1$
			} catch (MalformedURLException e1) {
				// ignore
			}
		}
	}

	private static SmartCollection createAdhocQuery(QueryField qfield, Object fieldValue, boolean shift) {
		String text = qfield.value2text(fieldValue, CLICK_TO_VIEW_DETAILS);
		if (text == null)
			text = "?"; //$NON-NLS-1$
		String rel = null;
		SmartCollection coll = new SmartCollectionImpl("", true, false, false, true, null, 0, null, 0, null, //$NON-NLS-1$
				Constants.INHERIT_LABEL, null, 0, null);
		if (qfield.getCard() != 1) {
			rel = ":"; //$NON-NLS-1$
			if (fieldValue instanceof LocationImpl[]) {
				LocationImpl[] array = (LocationImpl[]) fieldValue;
				for (int i = 0; i < array.length; i++)
					coll.addCriterion(
							new CriterionImpl(qfield.getKey(), null, array[i].getStringId(), QueryField.EQUALS, true));
			} else if (fieldValue instanceof ArtworkOrObjectImpl[]) {
				ArtworkOrObjectImpl[] array = (ArtworkOrObjectImpl[]) fieldValue;
				for (int i = 0; i < array.length; i++)
					coll.addCriterion(
							new CriterionImpl(qfield.getKey(), null, array[i].getStringId(), QueryField.EQUALS, true));
			} else if (fieldValue instanceof ContactImpl[]) {
				ContactImpl[] array = (ContactImpl[]) fieldValue;
				for (int i = 0; i < array.length; i++)
					coll.addCriterion(
							new CriterionImpl(qfield.getKey(), null, array[i].getStringId(), QueryField.EQUALS, true));
			} else if (fieldValue instanceof int[]) {
				int[] array = (int[]) fieldValue;
				for (int i = 0; i < array.length; i++)
					coll.addCriterion(new CriterionImpl(qfield.getKey(), null, array[i], QueryField.EQUALS, true));
			} else if (fieldValue instanceof String[]) {
				String[] array = (String[]) fieldValue;
				for (int i = 0; i < array.length; i++)
					coll.addCriterion(new CriterionImpl(qfield.getKey(), null, array[i], QueryField.EQUALS, true));
			}
		} else {
			if (fieldValue instanceof LocationImpl)
				fieldValue = ((LocationImpl) fieldValue).getStringId();
			else if (fieldValue instanceof ArtworkOrObjectImpl)
				fieldValue = ((ArtworkOrObjectImpl) fieldValue).getStringId();
			else if (fieldValue instanceof ContactImpl)
				fieldValue = ((ContactImpl) fieldValue).getStringId();
			boolean equ = qfield.getTolerance() == 0f;
			coll.addCriterion(new CriterionImpl(qfield.getKey(), null, fieldValue,
					equ ? QueryField.EQUALS : QueryField.SIMILAR, false));
			rel = equ ? "=" : "=~"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		coll.setName(qfield.getLabel() + rel + text);
		if (shift) {
			IAssetProvider assetProvider = Core.getCore().getAssetProvider();
			if (assetProvider != null) {
				SmartCollectionImpl parentCollection = assetProvider.getParentCollection();
				if (parentCollection != null)
					coll.setSmartCollection_subSelection_parent(parentCollection);
			}
		}
		return coll;
	}

	@Override
	public void dispose() {
		if (preferenceListener != null)
			InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID).removePreferenceChangeListener(preferenceListener);
		resetCaches();
		super.dispose();
	}

	protected abstract int[] getColumnWidths();

	@Override
	public void updateActions(boolean force) {
		if ((isVisible() || force) && !viewer.getControl().isDisposed()) {
			if (deleteAction != null) {
				Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				deleteAction.setEnabled((firstElement instanceof IndexedMember
						|| firstElement instanceof QueryField && ((QueryField) firstElement).isStruct())
						&& !dbIsReadonly());
			}
			updateActions(-1, -1);
		}
	}

	protected abstract int getExpandLevel();

	private void contributeToActionBars() {
		undoContext = PlatformUI.getWorkbench().getOperationSupport().getUndoContext();
		IViewSite viewSite = getViewSite();
		IActionBars bars = viewSite.getActionBars();
		fillLocalToolbar(bars.getToolBarManager());
		fillLocalPullDown(bars.getMenuManager());
		new UndoRedoActionGroup(viewSite, undoContext, true).fillActionBars(bars);
	}

	private void fillLocalToolbar(IToolBarManager manager) {
		if (deleteAction != null) {
			manager.add(expandAction);
			manager.add(collapseAction);
			manager.add(deleteAction);
		}
	}

	protected void fillLocalPullDown(IMenuManager manager) {
		if (essentialAction != null && allAction != null) {
			manager.add(expandAction);
			manager.add(collapseAction);
			manager.add(new Separator());
			manager.add(editableAction);
			manager.add(essentialAction);
			manager.add(allAction);
			manager.add(new Separator());
			manager.add(configureAction);
		}
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		essentialAction = new Action(Messages.getString("AbstractPropertiesView.essential"), IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			@Override
			public void run() {
				mode = MODE_SHOW_ESSENTIAL;
				refreshInternal();
			}
		};
		essentialAction.setToolTipText(Messages.getString("AbstractPropertiesView.show_only_essential")); //$NON-NLS-1$
		allAction = new Action(Messages.getString("AbstractPropertiesView.all"), IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			@Override
			public void run() {
				mode = MODE_SHOW_ALL;
				refreshInternal();
			}
		};
		allAction.setToolTipText(Messages.getString("AbstractPropertiesView.show_all_entries")); //$NON-NLS-1$
		editableAction = new Action(Messages.getString("AbstractPropertiesView.editable"), IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			@Override
			public void run() {
				mode = MODE_SHOW_EDITABLE;
				refreshInternal();
			}
		};
		editableAction.setToolTipText(Messages.getString("AbstractPropertiesView.editable_tooltip")); //$NON-NLS-1$
		switch (mode) {
		case MODE_SHOW_ALL:
			allAction.setChecked(true);
			break;
		case MODE_SHOW_ESSENTIAL:
			essentialAction.setChecked(true);
			break;
		case MODE_SHOW_EDITABLE:
			editableAction.setChecked(true);
			break;
		}
		deleteAction = new Action(Messages.getString("AbstractPropertiesView.delete"), Icons.delete.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				deleteSubentry();
			}
		};
		deleteAction.setToolTipText(Messages.getString("AbstractPropertiesView.delete_subentry")); //$NON-NLS-1$
		configureAction = new Action(Messages.getString("AbstractPropertiesView.Configure")) { //$NON-NLS-1$
			@Override
			public void run() {
				PreferencesUtil
						.createPreferenceDialogOn(getSite().getShell(), MetadataPreferencePage.ID, new String[0],
								"essential") //$NON-NLS-1$
						.open();
			}
		};
		configureAction.setToolTipText(Messages.getString("AbstractPropertiesView.Configure_tooltip")); //$NON-NLS-1$
		fieldAction = new Action(Messages.getString("AbstractPropertiesView.find_similar")) { //$NON-NLS-1$
			@Override
			public void runWithEvent(Event event) {
				Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (firstElement instanceof QueryField)
					processAction((QueryField) firstElement, (event.stateMask & SWT.SHIFT) != 0);
			}
		};
		fieldAction.setToolTipText(Messages.getString("AbstractPropertiesView.find_similar_tooltip")); //$NON-NLS-1$
		expandAction = new Action(Messages.getString("CatalogView.expand_all"), Icons.expandAll.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				viewer.expandAll();
			}
		};
		expandAction.setToolTipText(Messages.getString("CatalogView.expand_all_tooltip")); //$NON-NLS-1$
		collapseAction = new Action(Messages.getString("CatalogView.collapse_all"), Icons.collapseAll.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				viewer.collapseAll();
			}
		};
		collapseAction.setToolTipText(Messages.getString("CatalogView.collapse_all_tree_items")); //$NON-NLS-1$
	}

	protected void deleteSubentry() {
		Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		List<Asset> assets = getNavigationHistory().getSelectedAssets().getAssets();
		if (firstElement instanceof IndexedMember) {
			IndexedMember indexedMember = (IndexedMember) firstElement;
			OperationJob.executeOperation(
					new MultiModifyAssetOperation(indexedMember.getQfield(), null, indexedMember.getValue(), assets),
					this);
		} else if (firstElement instanceof QueryField) {
			QueryField qfield = (QueryField) firstElement;
			if (qfield.isStruct()) {
				IDbManager dbManager = Core.getCore().getDbManager();
				String oldId = null;
				Object oldValue = null;
				if (qfield == QueryField.IPTC_LOCATIONCREATED) {
					for (Asset asset : assets) {
						Iterator<LocationCreatedImpl> it = dbManager
								.obtainStruct(LocationCreatedImpl.class, asset.getStringId(), true, null, null, false)
								.iterator();
						if (it.hasNext()) {
							LocationCreatedImpl rel = it.next();
							if (oldId == null)
								oldId = rel.getLocation();
							else if (!oldId.equals(rel.getLocation()))
								return;
						}
					}
					if (oldId != null)
						oldValue = dbManager.obtainById(LocationImpl.class, oldId);
				} else if (qfield == QueryField.IPTC_CONTACT) {
					for (Asset asset : assets) {
						Iterator<CreatorsContactImpl> it = dbManager
								.obtainStruct(CreatorsContactImpl.class, asset.getStringId(), true, null, null, false)
								.iterator();
						if (it.hasNext()) {
							CreatorsContactImpl rel = it.next();
							if (oldId == null)
								oldId = rel.getContact();
							else if (!oldId.equals(rel.getContact()))
								return;
						}
					}
					if (oldId != null)
						oldValue = dbManager.obtainById(ContactImpl.class, oldId);
				}
				if (oldValue != null)
					OperationJob.executeOperation(new MultiModifyAssetOperation(qfield, null, oldValue, assets), this);
			}
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	Object getFieldValue(QueryField qfield) {
		return getFieldEntry(qfield).value;
	}

	private FieldEntry getFieldEntry(QueryField qfield) {
		AssetSelection selection = getNavigationHistory().getSelectedAssets();
		int size = selection.size();
		if (size == 0)
			return FieldEntry.NOTHING;
		FieldEntry entry = valueMap.get(qfield);
		if (entry == null) {
			if (size > ASYNCTHRESHOLD) {
				valueMap.put(qfield, FieldEntry.PENDING);
				new SupplyPropertyJob(qfield, selection.getAssets(), this).schedule();
				return FieldEntry.PENDING;
			}
			valueMap.put(qfield, entry = new FieldEntry(qfield.obtainFieldValue(selection.getAssets(), null)));
		}
		return entry;
	}

	boolean isEditable(QueryField qfield) {
		AssetSelection selection = getNavigationHistory().getSelectedAssets();
		if (selection.isEmpty())
			return true;
		return qfield.isEditable(selection.getAssets());
	}

	boolean isApplicable(QueryField qfield) {
		AssetSelection selection = getNavigationHistory().getSelectedAssets();
		if (selection.isEmpty())
			return true;
		return qfield.isApplicable(selection.getAssets());
	}

	public ISelection getSelection() {
		return StructuredSelection.EMPTY;
	}

	public void setSelection(ISelection selection) {
		// do nothing
	}

	@Override
	public boolean assetsChanged() {
		resetCaches();
		return true;
	}

	private void resetCaches() {
		cancelJobs(Constants.PROPERTYPROVIDER);
		valueMap.clear();
	}

	@Override
	public boolean collectionChanged() {
		return false;
	}

	@Override
	public boolean selectionChanged() {
		return false;
	}

	@Override
	public void refresh() {
		computeFlags();
		computeEssentials();
		refreshInternal();
	}

	private void refreshInternal() {
		Object[] expandedElements = viewer.getExpandedElements();
		viewer.setInput(getRootElement());
		if (expandedElements.length == 0)
			viewer.expandToLevel(getExpandLevel());
		else
			viewer.setExpandedElements(expandedElements);
	}

	private void computeFlags() {
		flags = getNavigationHistory().getSelectedAssets().getMediaFlags();
	}

	public boolean isDisposed() {
		return viewer.getControl().isDisposed();
	}

	public Display getDisplay() {
		return viewer.getControl().getDisplay();
	}

	public void updateField(QueryField qfield, FieldEntry fieldEntry) {
		valueMap.put(qfield, fieldEntry);
		if (qfield == QueryField.EXIF_FILESOURCE)
			refresh();
		else
			viewer.update(qfield, UPDATECOLS);
	}

	protected abstract int[] getColumnMaxWidths();

}