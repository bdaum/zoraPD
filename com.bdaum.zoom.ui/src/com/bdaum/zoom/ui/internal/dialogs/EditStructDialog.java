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

package com.bdaum.zoom.ui.internal.dialogs;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.common.GeoMessages;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IFormatter;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.LocationConstants;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;
import com.bdaum.zoom.ui.ILocationDisplay;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.dialogs.EditMetaDialog.LocationNode;
import com.bdaum.zoom.ui.internal.views.DateTimeCellEditor;
import com.bdaum.zoom.ui.internal.views.LargeTextCellEditor;
import com.bdaum.zoom.ui.internal.views.ListCellEditor;
import com.bdaum.zoom.ui.internal.views.TopicTextCellEditor;
import com.bdaum.zoom.ui.internal.views.ViewTextCellEditor;

@SuppressWarnings("restriction")
public class EditStructDialog extends ZTitleAreaDialog implements IAdaptable {

	private static final Object[] EMPTYOBJECTS = new Object[0];
	private static final int MAPBUTTON = 999;

	public static class ViewContentProvider implements IStructuredContentProvider {

		public void dispose() {
			// do nothing
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}

		public Object[] getElements(Object inputElement) {
			return (inputElement instanceof QueryField) ? ((QueryField) inputElement).getChildren() : EMPTYOBJECTS;
		}
	}

	public class ValueColumnLabelProvider extends ZColumnLabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof QueryField) {
				QueryField qf = (QueryField) element;
				Object value = getFieldValue(qf);
				if (value == null)
					return Format.MISSINGENTRYSTRING;
				if (value instanceof String)
					return (String) value;
				if (qf.getCard() != 1) {
					StringBuilder sb = new StringBuilder();
					if (value instanceof String[]) {
						FilterChain filter = null;
						if (qf == QueryField.IPTC_KEYWORDS)
							filter = QueryField.getKeywordFilter();
						String[] array = (String[]) value;
						for (String v : array)
							if (filter == null || filter.accept(v)) {
								if (sb.length() > 0)
									sb.append(',');
								sb.append(formatScalarValue(qf, v));
							}
					}
					return sb.toString();
				}
				return formatScalarValue(qf, value);
			}
			return null;
		}

		private String formatScalarValue(QueryField qf, Object value) {
			if (value == null)
				return Format.MISSINGENTRYSTRING;
			IFormatter formatter = qf.getFormatter();
			if (formatter != null)
				return formatter.toString(value);
			switch (qf.getType()) {
			case QueryField.T_POSITIVELONG:
				if (value instanceof Long && ((Long) value).longValue() < 0)
					return Format.MISSINGENTRYSTRING;
				break;
			case QueryField.T_POSITIVEINTEGER:
				if (value instanceof Integer && ((Integer) value).intValue() < 0)
					return Format.MISSINGENTRYSTRING;
				break;
			case QueryField.T_POSITIVEFLOAT:
			case QueryField.T_FLOAT:
			case QueryField.T_FLOATB:
				if (value instanceof Float) {
					if (((Float) value).isNaN())
						return Format.MISSINGENTRYSTRING;
				} else if (value instanceof Double) {
					if (((Double) value).isNaN())
						return Format.MISSINGENTRYSTRING;
				}
				break;
			case QueryField.T_DATE:
				if (value instanceof Date)
					return Constants.DFDT.format((Date) value);
				break;
			}
			return String.valueOf(value);
		}

	}

	// public static class ViewComparator extends ViewerComparator {
	//
	// @Override
	// public int compare(Viewer viewer, Object e1, Object e2) {
	// if (e1 instanceof QueryField && e2 instanceof QueryField)
	// return ((QueryField) e1).getLabel().compareToIgnoreCase(
	// ((QueryField) e2).getLabel());
	// return super.compare(viewer, e1, e2);
	// }
	// }

	public Map<QueryField, Object> backup = new HashMap<QueryField, Object>();

	public class ViewEditingSupport extends EditingSupport {

		public ViewEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected boolean canEdit(Object element) {
			if (!readonly && element instanceof QueryField)
				return (((QueryField) element).getEditable() != QueryField.EDIT_NEVER);
			return false;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			if (element instanceof QueryField) {
				final QueryField qf = (QueryField) element;
				Table table = viewer.getTable();
				if (qf.getType() == QueryField.T_BOOLEAN)
					return new CheckboxCellEditor(table);
				if (qf.getCard() != 1)
					return new ListCellEditor(table, qf);
				if (qf.getType() == QueryField.T_DATE)
					return new DateTimeCellEditor(table, qf);
				if (qf.getEnumeration() instanceof String[]) {
					ComboBoxViewerCellEditor editor = new ComboBoxViewerCellEditor(table);
					editor.setContentProvider(new IStructuredContentProvider() {
						public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
							// do nothing
						}

						public void dispose() {
							// do nothing
						}

						public Object[] getElements(Object inputElement) {
							return (String[]) ((QueryField) inputElement).getEnumeration();
						}
					});
					editor.setLabelProvider(new ZColumnLabelProvider() {
						@Override
						public String getText(Object el) {
							String[] enumeration = (String[]) qf.getEnumeration();
							String[] enumLabels = qf.getEnumLabels();
							for (int i = 0; i < enumeration.length; i++)
								if (enumeration[i].equals(el))
									return enumLabels[i];
							return el.toString();
						}
					});
					editor.setInput(qf);
					return editor;
				}
				if (qf.getEnumeration() instanceof Integer)
					return new TopicTextCellEditor(table, qf);
				if (qf.getMaxlength() > 60)
					return new LargeTextCellEditor(table, qf, null);
				return new ViewTextCellEditor(table, qf, null);
			}
			return null;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof QueryField) {
				QueryField qf = (QueryField) element;
				Object value = getFieldValue(qf);
				if (value != null) {
					IFormatter formatter = qf.getFormatter();
					if (formatter != null)
						return formatter.toString(value);
					if (value instanceof Date)
						return Constants.DFDT.format((Date) value);
				}
				return value;
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof QueryField) {
				QueryField qf = (QueryField) element;
				if (qf.getKey() != null) {
					if (value != null) {
						IFormatter formatter = qf.getFormatter();
						if (formatter != null)
							try {
								value = formatter.fromString((String) value);
							} catch (ParseException e) {
								return;
							}
					}
					Object oldvalue = getFieldValue(qf);
					backup.put(qf, oldvalue);
					if ((oldvalue != null && !oldvalue.equals(value)) || (oldvalue == null && value != null)) {
						setFieldValue(qf, value, oldvalue);
						viewer.update(qf, null);
						if (qf == QueryField.LOCATION_COUNTRYCODE && value instanceof String
								&& !((String) value).isEmpty()) {
							String continentCode = LocationConstants.countryToContinent.get(value);
							if (continentCode != null) {
								setFieldValue(QueryField.LOCATION_WORLDREGIONCODE, continentCode,
										getFieldValue(QueryField.LOCATION_WORLDREGIONCODE));
								backup.put(QueryField.LOCATION_WORLDREGIONCODE, continentCode);
								viewer.update(QueryField.LOCATION_WORLDREGIONCODE, null);
								String name = GeoMessages.getString(GeoMessages.PREFIX + continentCode);
								setFieldValue(QueryField.LOCATION_WORLDREGION, name,
										getFieldValue(QueryField.LOCATION_WORLDREGION));
								backup.put(QueryField.LOCATION_WORLDREGION, name);
								viewer.update(QueryField.LOCATION_WORLDREGION, null);
							}
						} else if (qf == QueryField.LOCATION_WORLDREGION) {
							String code = LocationConstants.worldRegionToContinent.get(value);
							setFieldValue(QueryField.LOCATION_WORLDREGIONCODE, code,
									getFieldValue(QueryField.LOCATION_WORLDREGIONCODE));
							backup.put(QueryField.LOCATION_WORLDREGIONCODE, code);
							viewer.update(QueryField.LOCATION_WORLDREGIONCODE, null);
						}
					}
				}
			}
		}
	}

	private IdentifiableObject struct;
	private TableViewer viewer;
	private QueryField qfield;
	private String title;
	private int type;

	private boolean updated;
	private QueryField[] fields;
	private QueryField titleField;
	private final Map<String, Map<QueryField, Object>> structMap;

	public EditStructDialog(Shell parentShell, IdentifiableObject struct, int type, int level,
			Map<String, Map<QueryField, Object>> structMap, String title) {
		super(parentShell);
		this.structMap = structMap;
		this.struct = struct;
		this.title = title;
		this.type = type;
		qfield = QueryField.getStructParent(type);
		switch (level) {
		case LocationNode.COUNTRY:
			fields = new QueryField[] { QueryField.LOCATION_WORLDREGION, QueryField.LOCATION_WORLDREGIONCODE,
					QueryField.LOCATION_COUNTRYNAME, QueryField.LOCATION_COUNTRYCODE };
			titleField = QueryField.LOCATION_COUNTRYNAME;
			break;
		case LocationNode.STATE:
			fields = new QueryField[] { QueryField.LOCATION_WORLDREGION, QueryField.LOCATION_WORLDREGIONCODE,
					QueryField.LOCATION_COUNTRYNAME, QueryField.LOCATION_COUNTRYCODE, QueryField.LOCATION_STATE };
			titleField = QueryField.LOCATION_STATE;
			break;
		case LocationNode.CITY:
			fields = new QueryField[] { QueryField.LOCATION_WORLDREGION, QueryField.LOCATION_WORLDREGIONCODE,
					QueryField.LOCATION_COUNTRYNAME, QueryField.LOCATION_COUNTRYCODE, QueryField.LOCATION_STATE,
					QueryField.LOCATION_CITY };
			titleField = QueryField.LOCATION_CITY;
			break;
		default:
			break;
		}
	}

	@Override
	public void create() {
		super.create();
		String s = NLS.bind(title, qfield.getLabel());
		if (titleField != null)
			s += ": " + getFieldValue(titleField); //$NON-NLS-1$
		setTitle(s);
		setMessage(NLS.bind(Messages.EditStructDialog_definitions_are_applied_to_all, qfield.getLabel()));
		updateButtons();
	}

	private void updateButtons() {
		getShell().setModified(!readonly);
		getButton(IDialogConstants.OK_ID).setEnabled(!readonly);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (type == QueryField.T_LOCATION)
			createButton(parent, MAPBUTTON, Messages.EditStructDialog_Map, false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == MAPBUTTON) {
			ILocationDisplay display = UiActivator.getDefault().getLocationDisplay();
			if (display != null) {
				LocationImpl newLoc = (LocationImpl) display.defineLocation((Location) struct);
				if (newLoc != null) {
					struct = newLoc;
					viewer.setInput(qfield);
				}
			}
		} else
			super.buttonPressed(buttonId);
	}

	@Override
	protected void cancelPressed() {
		for (Map.Entry<QueryField, Object> entry : backup.entrySet())
			setFieldValue(entry.getKey(), entry.getValue(), getFieldValue(entry.getKey()));
		super.cancelPressed();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout());
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		Table table = viewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setWidth(200);
		col1.getColumn().setText(Messages.EditStructDialog_name);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof QueryField) {
					QueryField qf = (QueryField) element;
					return !readonly && qf.isEditable((Asset) null) ? Format.EDITABLEINDICATOR + qf.getLabel()
							: qf.getLabel(); // $NON-NLS-1$
				}
				return null;
			}
		});
		TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setWidth(400);
		col2.getColumn().setText(Messages.EditStructDialog_value);
		col2.setLabelProvider(new ValueColumnLabelProvider());
		col2.setEditingSupport(new ViewEditingSupport(viewer));
		viewer.setContentProvider(new ViewContentProvider());
		// viewer.setComparator(new ViewComparator());
		if (fields != null)
			viewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					for (QueryField qfield : fields) {
						if (qfield == element)
							return true;
					}
					return false;
				}
			} });
		viewer.setInput(qfield);
		return area;
	}

	private static final String[] EMPTYSTRING = new String[0];

	public Object getFieldValue(QueryField qf) {
		if (struct == null) {
			if (qf.getType() == QueryField.T_BOOLEAN)
				return Boolean.FALSE;
			if (qf.getCard() != 1)
				return new String[0];
			if (qf.getType() == QueryField.T_DATE)
				return new Date();
			return ""; //$NON-NLS-1$
		}
		try {
			Map<QueryField, Object> map = structMap.get(struct.getStringId());
			if (map != null && map.containsKey(qf))
				return map.get(qf);
			return qf.obtainPlainFieldValue(struct);
		} catch (Exception e) {
			Core.getCore().logError(NLS.bind(Messages.EditStructDialog_internal_error_accessing, qf.getKey()), e);
		}
		return null;
	}

	/**
	 * @param qfield
	 * @param value
	 *            - Either value or oldvalue must be unequal null
	 * @param oldvalue
	 */
	public void setFieldValue(QueryField qfield, Object value, Object oldvalue) {
		if (value == null && oldvalue == null)
			return;
		if (struct == null) {
			switch (type) {
			case QueryField.T_LOCATION:
				struct = new LocationImpl();
				break;
			case QueryField.T_CONTACT:
				ContactImpl contactImpl = new ContactImpl();
				contactImpl.setAddress(EMPTYSTRING);
				contactImpl.setEmail(EMPTYSTRING);
				contactImpl.setWebUrl(EMPTYSTRING);
				contactImpl.setPhone(EMPTYSTRING);
				struct = contactImpl;
				break;
			case QueryField.T_OBJECT:
				ArtworkOrObjectImpl artworkOrObjectImpl = new ArtworkOrObjectImpl();
				artworkOrObjectImpl.setCreator(EMPTYSTRING);
				struct = artworkOrObjectImpl;
				break;
			}
		} else
			updated = true;
		String id = struct.getStringId();
		Map<QueryField, Object> fieldMap = structMap.get(id);
		if (fieldMap == null) {
			fieldMap = new HashMap<QueryField, Object>();
			structMap.put(id, fieldMap);
		}
		fieldMap.put(qfield, value);
	}

	public IdentifiableObject getResult() {
		return struct;
	}

	public boolean isUpdated() {
		return updated;
	}

	public String getTitle() {
		return title;
	}

}
