package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.css.CSSProperties;

public class JpegMetaGroup extends Composite {

	private CheckboxButton jpegButton;
	private Label jpegRemark;

	public JpegMetaGroup(Composite parent, int style) {
		super(parent, style);
		setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		setLayout(new GridLayout());
		jpegButton = WidgetFactory.createCheckButton(parent, Messages.JpegMetaGroup_insert_into_jpeg,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		jpegRemark = new Label(parent, SWT.NONE);
		jpegRemark.setText(Messages.JpegMetaGroup_xmp_warning);
		jpegRemark.setData(CSSProperties.ID, CSSProperties.ERRORS);
		jpegButton.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateLabel();
			}
		});
	}

	private void updateLabel() {
		jpegRemark.setVisible(jpegButton.getSelection());
	}

	public void setSelection(boolean selected) {
		jpegButton.setSelection(selected);
		updateLabel();
	}

	public boolean getSelection() {
		return jpegButton.getSelection();
	}

}
