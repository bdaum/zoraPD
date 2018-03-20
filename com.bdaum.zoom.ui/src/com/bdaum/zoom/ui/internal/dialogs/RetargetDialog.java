package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class RetargetDialog extends ZTitleAreaDialog implements SelectionListener {

	private static final String VALUE = "value"; //$NON-NLS-1$
	private final IPath newPath;
	private final IPath oldPath;
	protected int result = -1;
	private boolean retargetVoiceNote;
	private CheckboxButton voiceNoteButton;

	public RetargetDialog(Shell parentShell, String oldPath, String newPath) {
		super(parentShell, HelpContextIds.RETARGET_DIALOG);
		this.oldPath = new Path(oldPath);
		this.newPath = new Path(newPath);
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.RetargetDialog_retarget_title);
		setMessage(Messages.RetargetDialog_retarget_message);
		updateButtons();
		getShell().pack();
	}

	private void updateButtons() {
		voiceNoteButton.setEnabled(result > 0);
		getButton(OK).setEnabled(validate());
	}

	private boolean validate() {
		setErrorMessage(result < 0 ? Messages.RetargetDialog_retarget_scope : null);
		return result >= 0;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		RadioButtonGroup retargetButtonGroup = new RadioButtonGroup(area, null, SWT.NONE, Messages.RetargetDialog_selected_image);
		retargetButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		retargetButtonGroup.setData(0, VALUE, 0);
		retargetButtonGroup.addSelectionListener(this);
		IPath path = oldPath.removeLastSegments(1);
		IPath path2 = newPath.removeLastSegments(1);
		int n = oldPath.segmentCount() - 1;
		int m = newPath.segmentCount() - 1;
		int k = 1;
		while (n > 0 && m > 0) {
			final int s = oldPath.segmentCount() - n;
			retargetButtonGroup.addButton(path.toString() + "/ -> " + path2.toString() + '/'); //$NON-NLS-1$
			retargetButtonGroup.setData(k++, VALUE, s);
			if (!oldPath.segment(--n).equals(newPath.segment(--m)))
				break;
			path = path.removeLastSegments(1);
			path2 = path2.removeLastSegments(1);
		}
		Label label = new Label(retargetButtonGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		voiceNoteButton = WidgetFactory.createCheckButton(retargetButtonGroup,
				Messages.RetargetDialog_retarget_voicenote, new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		return area;
	}

	public int getResult() {
		return result;
	}

	public void widgetSelected(SelectionEvent e) {
		result = (Integer) ((RadioButtonGroup) e.widget).getData(e.detail, VALUE);
		updateButtons();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		// do nothing
	}

	@Override
	protected void okPressed() {
		retargetVoiceNote = voiceNoteButton.getEnabled() && voiceNoteButton.getSelection();
		super.okPressed();
	}

	public boolean getRetargetVoiceNote() {
		return retargetVoiceNote;
	}

}
