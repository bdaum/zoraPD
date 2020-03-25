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

package com.bdaum.zoom.ui.internal.preferences;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.dialogs.AllNoneGroup;
import com.bdaum.zoom.ui.internal.dialogs.MetadataContentProvider;
import com.bdaum.zoom.ui.internal.dialogs.MetadataLabelProvider;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.internal.widgets.JpegMetaGroup;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

public class MetadataPreferencePage extends AbstractPreferencePage {

	private static final Object[] EMPTY = new Object[0];
	private final static NumberFormat af = (NumberFormat.getNumberInstance());
	private static final String SEC = Messages.getString("MetadataPreferencePage.sec"); //$NON-NLS-1$

	public class ToleranceLabelProvider extends ZColumnLabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof QueryField) {
				QueryField qfield = (QueryField) element;
				Float tolerance = toleranceMap.get(qfield.getKey());
				if (tolerance != null) {
					if (tolerance > 0)
						return (int) tolerance.floatValue() + " %"; //$NON-NLS-1$
					if (tolerance < 0) {
						tolerance = -tolerance;
						switch (qfield.getType()) {
						case QueryField.T_INTEGER:
						case QueryField.T_POSITIVEINTEGER:
						case QueryField.T_POSITIVELONG:
						case QueryField.T_LONG:
							return String.valueOf((int) tolerance.floatValue());
						case QueryField.T_FLOAT:
						case QueryField.T_FLOATB:
						case QueryField.T_CURRENCY:
						case QueryField.T_POSITIVEFLOAT:
							af.setMaximumFractionDigits(qfield.getMaxlength());
							return af.format(tolerance);
						case QueryField.T_DATE:
							return String.valueOf((int) tolerance.floatValue() / 1000) + SEC;
						}
					}
				}
			}
			return null;
		}
	}

	private static final Object[] EMPTYOBJECTS = new Object[0];
	public static final String ID = "com.bdaum.zoom.ui.MetadataPreferencePage"; //$NON-NLS-1$
	private static final Object ESSENTIAL = "essential"; //$NON-NLS-1$
	private static final Object HOVER = "hover"; //$NON-NLS-1$

	private CheckboxTreeViewer essentialsViewer;
	private CheckboxTreeViewer hoverViewer;
	private TreeViewer tolerancesViewer;
	private Map<String, Float> toleranceMap = new HashMap<String, Float>(50);
	private TreeViewer exportViewer;
	private JpegMetaGroup jpegGroup;
	private ContainerCheckedTreeViewer tuningViewer;
	private Set<QueryField> alwaysIndexed = new HashSet<>();

	public MetadataPreferencePage() {
		setDescription(Messages.getString("MetadataPreferencePage.what_to_do")); //$NON-NLS-1$
		StringTokenizer st = new StringTokenizer(
				getPreferenceStore().getDefaultString(PreferenceConstants.METADATATUNING), "\n"); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			QueryField qfield = QueryField.findQueryField(st.nextToken());
			if (qfield != null)
				alwaysIndexed.add(qfield);
		}
	}

	@Override
	public void applyData(Object data) {
		if (ESSENTIAL.equals(data)) {
			tabFolder.setSelection(0);
			fillValues();
		} else if (HOVER.equals(data)) {
			tabFolder.setSelection(1);
			fillValues();
		}
	}

	@Override
	protected void createPageContents(Composite comp) {
		setHelp(HelpContextIds.METADATA_PREFERENCE_PAGE);
		createTabFolder(comp, "Meta"); //$NON-NLS-1$
		final Composite essentialsGroup = UiUtilities.createTabPage(tabFolder,
				Messages.getString("MetadataPreferencePage.essential_metadata"), //$NON-NLS-1$
				Messages.getString("MetadataPreferencePage.essential_tooltip")); //$NON-NLS-1$
		essentialsGroup.setLayout(new GridLayout(2, false));
		essentialsViewer = createViewerGroup(essentialsGroup, null, new MetadataLabelProvider(), null);
		final Composite hoverGroup = UiUtilities.createTabPage(tabFolder,
				Messages.getString("MetadataPreferencePage.hover_metadata"), //$NON-NLS-1$
				Messages.getString("MetadataPreferencePage.hover_tooltip")); //$NON-NLS-1$
		hoverGroup.setLayout(new GridLayout(2, false));
		hoverViewer = createViewerGroup(hoverGroup, null, new MetadataLabelProvider(), null);
		final Composite tolerancesGroup = UiUtilities.createTabPage(tabFolder,
				Messages.getString("MetadataPreferencePage.tolerances"), //$NON-NLS-1$
				Messages.getString("MetadataPreferencePage.tolerance_tooltip")); //$NON-NLS-1$
		tolerancesGroup.setLayout(new GridLayout());
		tolerancesViewer = createTolerancesViewer(tolerancesGroup);
		final Composite exportGroup = UiUtilities.createTabPage(tabFolder,
				Messages.getString("MetadataPreferencePage.export"), //$NON-NLS-1$
				Messages.getString("MetadataPreferencePage.export_tooltip")); //$NON-NLS-1$
		exportGroup.setLayout(new GridLayout());
		Composite viewerGroup = new Composite(exportGroup, SWT.NONE);
		viewerGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewerGroup.setLayout(new GridLayout(2, false));
		exportViewer = createViewerGroup(viewerGroup, null, new MetadataLabelProvider(), null);
		jpegGroup = new JpegMetaGroup(exportGroup, SWT.NONE);
		final Composite tuningGroup = UiUtilities.createTabPage(tabFolder,
				Messages.getString("MetadataPreferencePage.tuning"), //$NON-NLS-1$
				Messages.getString("MetadataPreferencePage.tuning_tooltip")); //$NON-NLS-1$
		tuningGroup.setLayout(new GridLayout(2, false));
		Label label = new Label(tuningGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		label.setText(Messages.getString("MetadataPreferencePage.tuning_msg")); //$NON-NLS-1$
		tuningViewer = createViewerGroup(tuningGroup, new ViewerFilter() {
			@Override
			public boolean select(Viewer aViewer, Object parentElement, Object element) {
				if (element instanceof QueryField) {
					QueryField qfield = (QueryField) element;
					return qfield.hasChildren() || qfield.isUiField() && !qfield.isStruct() && qfield.isQuery();
				}
				return false;
			}
		}, new MetadataLabelProvider() {
			@Override
			protected Color getForeground(Object element) {
				if (element instanceof QueryField && alwaysIndexed.contains(element))
					return super.getDisabledForeground(element);
				return super.getForeground(element);
			}
		}, alwaysIndexed);
		tuningViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (!event.getChecked()) {
					Object element = event.getElement();
					if (element instanceof QueryField && alwaysIndexed.contains(element))
						tuningViewer.setChecked(element, true);
				}
			}
		});
		jpegGroup.setSelection(getPreferenceStore().getBoolean(PreferenceConstants.JPEGMETADATA));
		initTabFolder(0);
		createExtensions(tabFolder, ID);
		fillValues();
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fillValues();
			}
		});
	}

	protected void doFillValues() {
		switch (tabFolder.getSelectionIndex()) {
		case 0:
			fillViewer(essentialsViewer, getPreferenceStore().getString(PreferenceConstants.ESSENTIALMETADATA), null, 2,
					false);
			break;
		case 1:
			fillViewer(hoverViewer, getPreferenceStore().getString(PreferenceConstants.HOVERMETADATA), null, 2, false);
			break;
		case 2:
			fillViewer(tolerancesViewer, getPreferenceStore().getString(PreferenceConstants.METADATATOLERANCES),
					toleranceMap, 2, false);
			break;
		case 3:
			fillViewer(exportViewer, getPreferenceStore().getString(PreferenceConstants.EXPORTMETADATA), null, 2,
					false);
			break;
		case 4:
			fillViewer(tuningViewer,
					getPreferenceStore().getString(PreferenceConstants.METADATATUNING) + '\n'
							+ getPreferenceStore().getDefaultString(PreferenceConstants.METADATATUNING),
					null, 2, false);
			break;
		}
	}

	private TreeViewer createTolerancesViewer(Composite comp) {
		ExpandCollapseGroup toleranceExpandCollapseGroup = new ExpandCollapseGroup(comp, SWT.NONE);
		final TreeViewer viewer = new TreeViewer(comp,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		toleranceExpandCollapseGroup.setViewer(viewer);
		viewer.getTree().setLinesVisible(true);
		TreeViewerColumn col1 = new TreeViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setWidth(250);
		col1.setLabelProvider(new MetadataLabelProvider());
		TreeViewerColumn col2 = new TreeViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setWidth(80);
		final ToleranceLabelProvider labelProvider = new ToleranceLabelProvider();
		col2.setLabelProvider(labelProvider);
		col2.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof QueryField) {
					QueryField qfield = (QueryField) element;
					String key = qfield.getKey();
					String s = String.valueOf(value);
					float v;
					if (s.endsWith("%")) //$NON-NLS-1$
						v = Integer.parseInt(s.substring(0, s.length() - 1).trim());
					else
						try {
							af.setMaximumFractionDigits(8);
							v = -af.parse(s.trim()).floatValue();
							if (qfield.getType() == QueryField.T_DATE)
								v = 1000 * v;
						} catch (ParseException e) {
							v = 0f;
						}
					toleranceMap.put(key, v);
					viewer.update(element, null);
				}
			}

			@Override
			protected Object getValue(Object element) {
				String text = labelProvider.getText(element);
				if (text.endsWith(SEC))
					return text.substring(0, text.length() - SEC.length());
				return text;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				if (element instanceof QueryField) {
					final int type = ((QueryField) element).getType();
					TextCellEditor editor = new TextCellEditor(viewer.getTree());
					editor.setValidator(new ICellEditorValidator() {
						public String isValid(Object value) {
							String s = String.valueOf(value);
							if (s.endsWith("%")) { //$NON-NLS-1$
								try {
									if (Integer.parseInt(s.substring(0, s.length() - 1).trim()) < 0)
										return Messages.getString("MetadataPreferencePage.must_not_be_negative"); //$NON-NLS-1$
									if (type == QueryField.T_DATE)
										return Messages
												.getString("MetadataPreferencePage.percent_not_allowed_for_date"); //$NON-NLS-1$
									return null;
								} catch (NumberFormatException e) {
									return Messages.getString("MetadataPreferencePage.bad_integer"); //$NON-NLS-1$
								}
							}
							if (type == QueryField.T_POSITIVEINTEGER || type == QueryField.T_INTEGER) {
								try {
									if (Integer.parseInt(s.substring(0, s.length() - 1).trim()) < 0)
										return Messages.getString("MetadataPreferencePage.must_not_be_negative"); //$NON-NLS-1$
									return null;
								} catch (NumberFormatException e) {
									return Messages.getString("MetadataPreferencePage.bad_integer"); //$NON-NLS-1$
								}
							} else if (type == QueryField.T_POSITIVELONG || type == QueryField.T_LONG) {
								try {
									if (Long.parseLong(s.substring(0, s.length() - 1).trim()) < 0)
										return Messages.getString("MetadataPreferencePage.must_not_be_negative"); //$NON-NLS-1$
									return null;
								} catch (NumberFormatException e) {
									return Messages.getString("MetadataPreferencePage.bad_integer"); //$NON-NLS-1$
								}
							}
							try {
								af.setMaximumFractionDigits(8);
								if (af.parse(s.trim()).floatValue() < 0)
									return Messages.getString("MetadataPreferencePage.must_not_be_negative"); //$NON-NLS-1$
								return null;
							} catch (ParseException e) {
								return Messages.getString("MetadataPreferencePage.bad_floating_point"); //$NON-NLS-1$
							}
						}
					});
					return editor;
				}
				return null;
			}

			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof QueryField)
					return ((QueryField) element).getTolerance() != 0f;
				return false;
			}
		});
		viewer.setContentProvider(new MetadataContentProvider());
		viewer.setComparator(ZViewerComparator.INSTANCE);
		UiUtilities.installDoubleClickExpansion(viewer);
		viewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
			@Override
			public boolean select(Viewer aViewer, Object parentElement, Object element) {
				if (element instanceof QueryField) {
					QueryField qfield = (QueryField) element;
					return (qfield.hasChildren() || qfield.isUiField()) && hostsToleranceValue((QueryField) element);
				}
				return false;
			}

			private boolean hostsToleranceValue(QueryField qfield) {
				if (qfield.getTolerance() != 0f)
					return true;
				for (QueryField child : qfield.getChildren())
					if (hostsToleranceValue(child))
						return true;
				return false;
			}
		} });
		return viewer;
	}

	@SuppressWarnings("unused")
	public static ContainerCheckedTreeViewer createViewerGroup(Composite comp, ViewerFilter filter,
			MetadataLabelProvider labelProvider, Set<QueryField> alwaysSelected) {
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(comp, SWT.NONE);
		new Label(comp, SWT.NONE);
		final ContainerCheckedTreeViewer viewer = new ContainerCheckedTreeViewer(comp,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		expandCollapseGroup.setViewer(viewer);
		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(new MetadataContentProvider());
		viewer.setFilters(new ViewerFilter[] { filter != null ? filter : new ViewerFilter() {
			@Override
			public boolean select(Viewer aViewer, Object parentElement, Object element) {
				if (element instanceof QueryField) {
					QueryField qfield = (QueryField) element;
					return qfield.hasChildren() || qfield.isUiField();
				}
				return false;
			}
		} });
		viewer.setComparator(ZViewerComparator.INSTANCE);
		UiUtilities.installDoubleClickExpansion(viewer);
		new AllNoneGroup(comp, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.widget.getData() == AllNoneGroup.ALL) {
					viewer.setGrayedElements(EMPTY);
					viewer.setCheckedElements(QueryField.getQueryFields().toArray());
				} else if (alwaysSelected != null)
					viewer.setCheckedElements(alwaysSelected.toArray());
				else
					viewer.setCheckedElements(EMPTY);
			}
		});
		return viewer;
	}

	public static void fillViewer(final TreeViewer viewer, final String ess, Map<String, Float> toleranceMap,
			final int level, boolean force) {
		if (viewer.getInput() == null || force)
			BusyIndicator.showWhile(viewer.getControl().getDisplay(), () -> {
				if (viewer.getInput() == null) {
					viewer.setInput(QueryField.ALL);
					if (level >= 0)
						viewer.expandToLevel(level);
				}
				StringTokenizer st = new StringTokenizer(ess, "\n"); //$NON-NLS-1$
				if (viewer instanceof CheckboxTreeViewer) {
					Set<QueryField> fields = new HashSet<QueryField>();
					while (st.hasMoreTokens()) {
						QueryField qfield = QueryField.findQueryField(st.nextToken());
						if (qfield != null)
							fields.add(qfield);
					}
					((CheckboxTreeViewer) viewer).setCheckedElements(fields.toArray());
				} else
					while (st.hasMoreTokens()) {
						String id = st.nextToken();
						int p = id.lastIndexOf("="); //$NON-NLS-1$
						if (p > 0) {
							try {
								toleranceMap.put(id.substring(0, p), Float.parseFloat(id.substring(p + 1)));
							} catch (NumberFormatException e) {
								// ignore
							}
						}
					}
			});
	}

	@Override
	public void init(IWorkbench wb) {
		super.init(wb);
		setTitle(Messages.getString("MetadataPreferencePage.metadata_configuration")); //$NON-NLS-1$
		setMessage(Messages.getString("MetadataPreferencePage.select_essential_and_hover")); //$NON-NLS-1$
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return UiActivator.getDefault().getPreferenceStore();
	}

	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		setDefaults(preferenceStore, essentialsViewer, PreferenceConstants.ESSENTIALMETADATA);
		setDefaults(preferenceStore, hoverViewer, PreferenceConstants.HOVERMETADATA);
		setDefaults(preferenceStore, tolerancesViewer, PreferenceConstants.METADATATOLERANCES);
		setDefaults(preferenceStore, exportViewer, PreferenceConstants.EXPORTMETADATA);
		setDefaults(preferenceStore, tuningViewer, PreferenceConstants.METADATATUNING);
		tolerancesViewer.setInput(QueryField.ALL);
		preferenceStore.setValue(PreferenceConstants.JPEGMETADATA,
				preferenceStore.getDefaultBoolean(PreferenceConstants.JPEGMETADATA));
		jpegGroup.setSelection(getPreferenceStore().getBoolean(PreferenceConstants.JPEGMETADATA));
	}

	public static void setDefaults(IPreferenceStore preferenceStore, TreeViewer viewer, String key) {
		if (viewer instanceof CheckboxTreeViewer)
			((CheckboxTreeViewer) viewer).setCheckedElements(EMPTYOBJECTS);
		String ess = preferenceStore.getDefaultString(key);
		preferenceStore.setValue(key, ess);
		fillViewer(viewer, ess, null, -1, true);
	}

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		saveValues(preferenceStore, essentialsViewer, PreferenceConstants.ESSENTIALMETADATA, null);
		saveValues(preferenceStore, hoverViewer, PreferenceConstants.HOVERMETADATA, null);
		saveValues(preferenceStore, exportViewer, PreferenceConstants.EXPORTMETADATA, null);
		saveValues(preferenceStore, tolerancesViewer, PreferenceConstants.METADATATOLERANCES, toleranceMap);
		saveValues(preferenceStore, tuningViewer, PreferenceConstants.METADATATUNING, null);
		getPreferenceStore().setValue(PreferenceConstants.JPEGMETADATA, jpegGroup.getSelection());
	}

	public static void saveValues(IPreferenceStore preferenceStore, TreeViewer viewer, String pkey,
			Map<String, Float> toleranceMap) {
		if (viewer.getInput() != null) {
			StringBuilder sb = new StringBuilder();
			if (viewer instanceof CheckboxTreeViewer) {
				for (Object object : ((CheckboxTreeViewer) viewer).getCheckedElements())
					if (object instanceof QueryField) {
						QueryField queryField = (QueryField) object;
						String id = queryField.getId();
						if (id != null && queryField.getChildren().length == 0) {
							if (sb.length() > 0)
								sb.append('\n');
							sb.append(id);
						}
					}
			} else
				for (String key : toleranceMap.keySet()) {
					if (sb.length() > 0)
						sb.append('\n');
					sb.append(key).append("=").append(toleranceMap.get(key)); //$NON-NLS-1$
				}
			preferenceStore.setValue(pkey, sb.toString());
		}
	}

}
