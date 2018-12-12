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

package com.bdaum.zoom.ui.internal.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.PasteMetadataOperation;
import com.bdaum.zoom.operations.internal.xmp.XMPField;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;

@SuppressWarnings("restriction")
public class PasteMetaDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "com.bdaum.zoom.pasteMetaDialog"; //$NON-NLS-1$
	private static final String SELECTION = "selection"; //$NON-NLS-1$
	protected static final String ANNOTATION = Messages.PasteMetaDialog_anno;
	protected static final String METADATA = Messages.PasteMetaDialog_metadata;
	protected static final String VOICENOTE = Messages.PasteMetaDialog_voice_note;
	protected static final String TEXTNOTE = Messages.PasteMetaDialog_text_note;
	protected static final String DRAWING = Messages.PasteMetaDialog_drawing;
	private CheckboxTreeViewer viewer;
	private final List<XMPField> values;
	private HashSet<QueryField> filter;
	private final IAdaptable info;
	private final List<Asset> selectedAssets;
	private IDialogSettings settings;
	private Set<String> selectionToKeep = new HashSet<String>();
	private MetadataOptionGroup metadataOptionGroup;
	private File voiceFile;
	private String voiceUri;
	private String noteText;
	private String svg;

	public PasteMetaDialog(Shell parentShell, List<Asset> selectedAssets, List<XMPField> values, String note,
			File voiceFile, IAdaptable info) {
		super(parentShell, HelpContextIds.PASTEMETA_DIALOG);
		this.selectedAssets = selectedAssets;
		this.values = values;
		this.voiceFile = voiceFile;
		this.info = info;
		filter = new HashSet<QueryField>();
		for (XMPField field : values) {
			QueryField qfield = field.translateFields();
			while (qfield != null) {
				filter.add(qfield);
				qfield = qfield.getParent();
			}
		}
		if (note != null) {
			int p = note.indexOf('\f');
			if (p >= 0) {
				int q = note.indexOf('\f', p + 1);
				if (q >= 0) {
					svg = note.substring(q + 1);
					noteText = note.substring(p + 1, q);
				} else
					noteText = note.substring(p + 1);
				voiceUri = note.substring(0, p);
			} else if (note.startsWith("?")) //$NON-NLS-1$
				noteText = note.substring(1);
			else
				voiceUri = note;
		}
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.PasteMetaDialog_paste_metadata);
		setMessage(Messages.PasteMetaDialog_select_the_metadata_to_be_pasted);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		viewer = createViewerGroup(composite);
		metadataOptionGroup = new MetadataOptionGroup(composite, false);
		viewer.expandAll();
		fillValues();
		return area;
	}

	private ContainerCheckedTreeViewer createViewerGroup(Composite comp) {
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(comp, SWT.NONE);
		final ContainerCheckedTreeViewer treeViewer = new ContainerCheckedTreeViewer(comp,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		expandCollapseGroup.setViewer(treeViewer);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewer.setLabelProvider(new MetadataLabelProvider());
		treeViewer.setContentProvider(new MetadataContentProvider() {
			private Object inputElement;

			@Override
			public Object[] getElements(Object inputElement) {
				this.inputElement = inputElement;
				if (hasAnnotation())
					return new Object[] { METADATA, ANNOTATION };
				return super.getElements(inputElement);
			}

			@Override
			public boolean hasChildren(Object element) {
				if (element == METADATA && super.getElements(inputElement).length > 0 || element == ANNOTATION)
					return true;
				return super.hasChildren(element);
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement == ANNOTATION) {
					List<String> children = new ArrayList<>(3);
					if (voiceUri != null)
						children.add(VOICENOTE);
					if (noteText != null)
						children.add(TEXTNOTE);
					if (svg != null)
						children.add(DRAWING);
					return children.toArray();
				}
				if (parentElement == METADATA)
					return super.getElements(inputElement);
				return super.getChildren(parentElement);
			}

			@Override
			public Object getParent(Object element) {
				if (element == VOICENOTE || element == TEXTNOTE || element == DRAWING)
					return ANNOTATION;
				if (hasAnnotation() && element instanceof QueryField && ((QueryField) element).getParent() == null)
					return METADATA;
				return super.getParent(element);
			}

		});
		treeViewer.setComparator(ZViewerComparator.INSTANCE);
		treeViewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
			@Override
			public boolean select(Viewer aViewer, Object parentElement, Object element) {
				if (element instanceof QueryField)
					return filter.contains(element);
				return true;
			}
		} });
		treeViewer.setInput(QueryField.ALL);
		UiUtilities.installDoubleClickExpansion(treeViewer);
		return treeViewer;
	}

	private boolean hasAnnotation() {
		return noteText != null || voiceUri != null || svg != null;
	}

	@Override
	protected void okPressed() {
		Set<XMPField> selectedFields = new HashSet<XMPField>(values.size());
		for (XMPField field : values) {
			QueryField qfield = field.translateIdFields();
			if (viewer.getChecked(qfield)) {
				selectedFields.add(field);
				selectionToKeep.add(qfield.getKey());
			} else {
				QueryField parent = field.translateFields();
				if (viewer.getChecked(parent)) {
					selectedFields.add(field);
					selectionToKeep.add(parent.getKey());
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		if (voiceUri != null && viewer.getChecked(VOICENOTE))
			sb.append(voiceUri);
		sb.append('\f');
		if (noteText != null && viewer.getChecked(TEXTNOTE))
			sb.append(noteText);
		sb.append('\f');
		if (svg != null && viewer.getChecked(DRAWING))
			sb.append(svg);
		String note = sb.length() == 2 ? null : sb.toString();
		boolean pasteAnnotation = viewer.getChecked(ANNOTATION);
		PasteMetadataOperation op = new PasteMetadataOperation(selectedAssets, selectedFields,
				metadataOptionGroup.getMode(), note, viewer.getChecked(VOICENOTE) ? voiceFile : null);
		OperationJob.executeOperation(op, info);
		metadataOptionGroup.saveSettings(settings);
		if (pasteAnnotation)
			selectionToKeep.add(ANNOTATION);
		settings.put(SELECTION, Core.toStringList(selectionToKeep, '\n'));
		super.okPressed();
	}

	private void fillValues() {
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		metadataOptionGroup.fillValues(settings);
		String selection = settings.get(SELECTION);
		if (selection != null) {
			StringTokenizer st = new StringTokenizer(selection, "\n"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String id = st.nextToken();
				if (!fillAnnotation(ANNOTATION, id) && !fillAnnotation(VOICENOTE, id) && !fillAnnotation(TEXTNOTE, id)
						&& !fillAnnotation(DRAWING, id)) {
					QueryField qfield = QueryField.findQueryField(id);
					if (filter.contains(qfield))
						viewer.setChecked(qfield, true);
					else
						selectionToKeep.add(id);
				}
			}
		} else
			viewer.setCheckedElements(filter.toArray());
	}

	private boolean fillAnnotation(String anno, String id) {
		if (id.equals(anno)) {
			if (hasAnnotation())
				viewer.setChecked(anno, true);
			else
				selectionToKeep.add(anno);
			return true;
		}
		return false;
	}
}
