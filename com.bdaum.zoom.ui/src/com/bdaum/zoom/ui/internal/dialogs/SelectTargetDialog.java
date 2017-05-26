package com.bdaum.zoom.ui.internal.dialogs;

import java.io.File;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;

public class SelectTargetDialog extends ZTitleAreaDialog implements
		ISelectionChangedListener {

	private final List<File> files;
	private final File folder;
	private File file;

	public SelectTargetDialog(Shell parentShell, List<File> files, File folder) {
		super(parentShell);
		this.files = files;
		this.folder = folder;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.SelectTargetDialog_retarget);
		setMessage(NLS
				.bind(Messages.SelectTargetDialog_select_from_list,
						folder));
		getButton(OK).setEnabled(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(1, false));
		ListViewer viewer = new ListViewer(comp, SWT.BORDER | SWT.SINGLE
				| SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.getControl().setLayoutData(new GridData(500, 200));
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setComparator(new ViewerComparator());
		viewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof File)
					return ((File) element).getAbsolutePath().substring(folder.getAbsolutePath().length() + 1);
				return element.toString();
			}
		});
		viewer.addSelectionChangedListener(this);
		viewer.setInput(files);
		return area;
	}

	public File getFile() {
		return file;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		file = (File) ((IStructuredSelection) event.getSelection()).getFirstElement();
		getButton(OK).setEnabled(file != null);
	}

}
