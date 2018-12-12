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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.meta.LastDeviceImport;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.ZViewerComparator;

public class MediaDialog extends ZTitleAreaDialog {

	private static final int REMOVE = 9999;
	private static final SimpleDateFormat sdf = new SimpleDateFormat(Messages.MediaDialog_import_date_mask);
	private TableViewer viewer;
	private List<LastDeviceImport> mediaList;
	private String key;
	private boolean newMedia;

	public MediaDialog(Shell parentShell, List<LastDeviceImport> media, String key, boolean newMedia) {
		super(parentShell);
		mediaList = media;
		this.key = key;
		this.newMedia = newMedia;
	}

	@Override
	public void create() {
		super.create();
		setTitle(newMedia ? Messages.MediaDialog_register_new : Messages.MediaDialog_manage_media);
		setMessage(newMedia ? Messages.MediaDialog_specify_owner : Messages.MediaDialog_manage_media_message);
		fillValues();
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLayoutData(new GridData(780, 250));
		TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setWidth(180);
		col1.getColumn().setText(Messages.MediaDialog_volume_name);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LastDeviceImport)
					return ((LastDeviceImport) element).getVolume();
				return element.toString();
			}
		});
		TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setWidth(140);
		col2.getColumn().setText(Messages.MediaDialog_owner);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LastDeviceImport)
					return ((LastDeviceImport) element).getOwner();
				return element.toString();
			}
		});
		col2.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof LastDeviceImport && value instanceof String) {
					((LastDeviceImport) element).setOwner((String) value);
					viewer.update(element, null);
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof LastDeviceImport)
					return ((LastDeviceImport) element).getOwner();
				return ""; //$NON-NLS-1$
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getTable());
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		TableViewerColumn col3 = new TableViewerColumn(viewer, SWT.NONE);
		col3.getColumn().setWidth(140);
		col3.getColumn().setText(Messages.MediaDialog_last_import);
		col3.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LastDeviceImport) {
					long timestamp = ((LastDeviceImport) element).getTimestamp();
					return timestamp == 0L ? " - " : sdf.format(new Date(timestamp)); //$NON-NLS-1$
				}
				return element.toString();
			}
		});
		col3.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof LastDeviceImport && value instanceof String) {
					try {
						((LastDeviceImport) element).setTimestamp(sdf.parse((String) value).getTime());
						viewer.update(element, null);
					} catch (ParseException e) {
						// do nothing
					}
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof LastDeviceImport)
					return sdf.format(new Date(((LastDeviceImport) element).getTimestamp()));
				return ""; //$NON-NLS-1$
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				TextCellEditor editor = new TextCellEditor(viewer.getTable());
				editor.setValidator(new ICellEditorValidator() {
					public String isValid(Object value) {
						if (value instanceof String) {
							try {
								sdf.parse((String) value);
								setErrorMessage(null);
							} catch (ParseException e) {
								String errorMessage = NLS.bind(Messages.MediaDialog_bad_date_format,
										Messages.MediaDialog_import_date_mask);
								setErrorMessage(errorMessage);
								return errorMessage;
							}
						}
						return null;
					}
				});
				return editor;
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		TableViewerColumn col4 = new TableViewerColumn(viewer, SWT.NONE);
		col4.getColumn().setWidth(300);
		col4.getColumn().setText(Messages.MediaDialog_comments);
		col4.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LastDeviceImport)
					return ((LastDeviceImport) element).getDescription();
				return element.toString();
			}
		});
		col4.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof LastDeviceImport && value instanceof String) {
					((LastDeviceImport) element).setDescription((String) value);
					viewer.update(element, null);
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof LastDeviceImport)
					return ((LastDeviceImport) element).getDescription();
				return ""; //$NON-NLS-1$
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getTable());
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setComparator(ZViewerComparator.INSTANCE);
		viewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof LastDeviceImport) {
					String volume = ((LastDeviceImport) element).getVolume();
					return volume.indexOf('/') < 0 && volume.indexOf('\\') < 0;
				}
				return false;
			}
		} });
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		return area;
	}
	
	private void fillValues() {
		viewer.setInput(mediaList);
		if (key != null)
			for (LastDeviceImport lastDeviceImport : mediaList)
				if (key.equals(lastDeviceImport.getVolume())) {
					viewer.setSelection(new StructuredSelection(lastDeviceImport));
					break;
				}
	}

	protected void updateButtons() {
		getButton(REMOVE).setEnabled(
				viewer.getStructuredSelection().getFirstElement() instanceof LastDeviceImport);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, REMOVE, Messages.MediaDialog_remove, false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == REMOVE) {
			if (viewer.getStructuredSelection().getFirstElement() instanceof LastDeviceImport) {
				mediaList.remove(viewer.getStructuredSelection().getFirstElement());
				viewer.setInput(mediaList);
			}
		}
		super.buttonPressed(buttonId);
	}

	@Override
	protected void okPressed() {
		IDbManager dbManager = Core.getCore().getDbManager();
		Meta meta = dbManager.getMeta(true);
		Map<String, LastDeviceImport> imports = meta.getLastDeviceImport();
		List<Object> toBeDeleted = null;
		List<Object> toBeStored = new ArrayList<Object>(mediaList);
		if (imports == null) {
			meta.setLastDeviceImport(new HashMap<String, LastDeviceImport>(mediaList.size() * 3 / 2));
			imports = meta.getLastDeviceImport();
		} else if (!newMedia) {
			toBeDeleted = new ArrayList<Object>(imports.values());
			imports.clear();
		}
		toBeStored.add(meta);
		for (LastDeviceImport imp : mediaList)
			imports.put(imp.getVolume(), imp);
		dbManager.safeTransaction(toBeDeleted, toBeStored);
		super.okPressed();
	}

}
