package com.bdaum.zoom.gps.widgets;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.internal.widgets.ZDialog;

@SuppressWarnings("restriction")
public class LocationSelectionDialog extends ZDialog {

	private final String[] items;
	protected String result;

	public LocationSelectionDialog(Shell parent, String[] items) {
		super(parent);
		setShellStyle(SWT.NO_TRIM);
		this.items = items;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		area.setLayout(new GridLayout(1, false));
		ListViewer viewer = new ListViewer(area, SWT.V_SCROLL| SWT.SINGLE);
		viewer.add(items);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				result = (String) ((IStructuredSelection) event.getSelection()).getFirstElement();
				close();
			}
		});
		viewer.getControl().addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				result = null;
				close();
			}

			public void focusGained(FocusEvent e) {
				//do nothing
			}
		});
		Shell shell = getShell();
		shell.pack();
		shell.layout();
		viewer.getControl().setFocus();
		return area;
	}

	public String getResult() {
		return result;
	}
}
