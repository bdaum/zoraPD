package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.jface.text.Document;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.program.HtmlEncoderDecoder;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.html.HtmlContentAssistant;
import com.bdaum.zoom.ui.internal.html.HtmlSourceViewer;
import com.bdaum.zoom.ui.internal.html.XMLCodeScanner;

public class DescriptionGroup {

	private Label descriptionHelpLabel;
	private Composite descriptionComposite;
	private StackLayout descriptionStack;
	private CheckedText descriptionField;
	private HtmlSourceViewer descriptionHtmlViewer;
	private RadioButtonGroup formatButtonGroup;

	@SuppressWarnings("unused")
	public DescriptionGroup(final Composite parent, int style) {
		new Label(parent, SWT.NONE).setText(Messages.DescriptionGroup_description);
		int numColumns = ((GridLayout) parent.getLayout()).numColumns;
		Composite formatGroup = new Composite(parent, SWT.NONE);
		formatGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, numColumns - 1, 1));
		formatGroup.setLayout(new GridLayout(2, false));
		formatButtonGroup = new RadioButtonGroup(formatGroup, null, SWT.HORIZONTAL,
				Messages.DescriptionGroup_plain_text, "HTML"); //$NON-NLS-1$
		formatButtonGroup.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateDescriptionStack(parent);
			}
		});
		descriptionHelpLabel = new Label(formatGroup, SWT.NONE);
		GridData gd_descriptionHelp = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_descriptionHelp.horizontalIndent = 20;
		descriptionHelpLabel.setLayoutData(gd_descriptionHelp);
		descriptionHelpLabel.setText(Messages.DescriptionGroup_html_assist);
		new Label(parent, SWT.NONE);
		GridData gd_descriptionField = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_descriptionField.widthHint = 300;
		gd_descriptionField.heightHint = 100;
		descriptionComposite = new Composite(parent, SWT.NONE);
		descriptionComposite.setLayoutData(gd_descriptionField);
		descriptionStack = new StackLayout();
		descriptionComposite.setLayout(descriptionStack);
		descriptionField = new CheckedText(descriptionComposite, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		descriptionHtmlViewer = new HtmlSourceViewer(descriptionComposite, null,
				SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL, new XMLCodeScanner(),
				new HtmlContentAssistant(false));
		descriptionHtmlViewer.setDocument(new Document());
		while (numColumns-- > 2)
			new Label(parent, SWT.NONE);
		setText("", false); //$NON-NLS-1$
	}

	private void updateDescriptionStack(final Composite parent) {
		if (formatButtonGroup.getSelection() == 1) {
			descriptionHtmlViewer.getDocument().set(HtmlEncoderDecoder.getInstance().encodeHTML(descriptionField.getText(), true));
			descriptionStack.topControl = descriptionHtmlViewer.getControl();
			descriptionHtmlViewer.setFocus();
		} else {
			StringBuilder sb = new StringBuilder();
			if (HtmlEncoderDecoder.getInstance().decodeHTML(descriptionHtmlViewer.getDocument().get(), sb)
					&& !AcousticMessageDialog.openConfirm(parent.getShell(), Messages.DescriptionGroup_html_to_plain,
							Messages.DescriptionGroup_existing_markup_will_be_deleted)) {
				formatButtonGroup.setSelection(1);
				return;
			}
			descriptionField.setText(sb.toString());
			descriptionStack.topControl = descriptionField;
			descriptionField.setFocus();
		}
		updateHelpLabel();
		descriptionComposite.layout();
	}

	public void setText(String text, boolean html) {
		formatButtonGroup.setSelection(html ? 1 : 0);
		if (html) {
			descriptionHtmlViewer.getDocument().set(text);
			descriptionStack.topControl = descriptionHtmlViewer.getControl();
		} else {
			descriptionField.setText(text);
			descriptionStack.topControl = descriptionField;
		}
		updateHelpLabel();
		descriptionComposite.layout();
	}

	public String getText() {
		return isHtml() ? descriptionHtmlViewer.getDocument().get().trim() : descriptionField.getText().trim();
	}

	public boolean isHtml() {
		return formatButtonGroup.getSelection() == 1;
	}

	public void setEnabled(boolean enabled) {
		descriptionField.setEnabled(enabled);
		descriptionHtmlViewer.getControl().setEnabled(enabled);
		formatButtonGroup.setEnabled(enabled);
		updateHelpLabel();
	}

	private void updateHelpLabel() {
		descriptionHelpLabel.setVisible(formatButtonGroup.isEnabled(1) && formatButtonGroup.getSelection() == 1);
	}

}
