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
 * (c) 2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.VocabManager.VocabNode;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;

public class ViewVocabDialog extends ZTitleAreaDialog {

	private VocabNode root;
	private File file;
	private TreeViewer viewer;
	private ExpandCollapseGroup expandCollapseGroup;
	private String[] result;
	private boolean select;

	public ViewVocabDialog(Shell parentShell, VocabNode root, File file, boolean select) {
		super(parentShell);
		this.root = root;
		this.file = file;
		this.select = select;
	}

	@Override
	public void create() {
		super.create();
		setTitle(file == null ? Messages.ViewVocabDialog_combined
				: NLS.bind(Messages.ViewVocabDialog_controlled_vocab, file.getName()));
		setMessage(Messages.ViewVocabDialog_vocab_message);
		viewer.setInput(root);
		viewer.expandToLevel(2);
		if (select)
			updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		expandCollapseGroup = new ExpandCollapseGroup(composite, SWT.NONE,
				new GridData(SWT.END, SWT.BEGINNING, true, false));
		viewer = new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		expandCollapseGroup.setViewer(viewer);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 440;
		layoutData.widthHint = 550;
		viewer.getTree().setLayoutData(layoutData);
		viewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof VocabNode)
					return element.toString();
				return null;
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof VocabNode && ((VocabNode) element).getCategory())
					return Icons.folder.getImage();
				return null;
			}
		});
		viewer.setContentProvider(new ITreeContentProvider() {
			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof VocabNode)
					return ((VocabNode) element).hasChildren();
				return false;
			}

			@Override
			public Object getParent(Object element) {
				if (element instanceof VocabNode)
					return ((VocabNode) element).getParent();
				return null;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof VocabNode)
					return ((VocabNode) parentElement).getChildren();
				return null;
			}
		});
		viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof VocabNode && e2 instanceof VocabNode)
					((VocabNode) e1).getLabel().compareTo(((VocabNode) e2).getLabel());
				return super.compare(viewer, e1, e2);
			}
		});
		if (select) {
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					updateButtons();
				}
			});
			viewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					if (!viewer.getSelection().isEmpty())
						okPressed();
				}
			});
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (select)
			super.createButtonsForButtonBar(parent);
		else
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected void okPressed() {
		List<String> list = new ArrayList<>();
		Iterator<?> iterator = ((IStructuredSelection) viewer.getSelection()).iterator();
		while (iterator.hasNext())
			list.add(((VocabNode) iterator.next()).getLabel());
		result = list.toArray(new String[list.size()]);
		super.okPressed();
	}

	private void updateButtons() {
		boolean enabled = !viewer.getSelection().isEmpty();
		getButton(OK).setEnabled(enabled);
	}

	public String[] getResult() {
		return result;
	}

}
