package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;

public class RetargetDialog extends ZTitleAreaDialog implements Listener {

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
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());

		CGroup group = new CGroup(composite, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);

		group.setLayoutData(layoutData);
		group.setLayout(new GridLayout());
		group.setText(Messages.RetargetDialog_scope);

		RadioButtonGroup retargetButtonGroup = new RadioButtonGroup(group, null, SWT.NONE,
				Messages.RetargetDialog_selected_image);
		retargetButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		retargetButtonGroup.setData(0, VALUE, 0);
		retargetButtonGroup.addListener(SWT.Selection, this);
		IPath path = oldPath.removeLastSegments(1);
		IPath path2 = newPath.removeLastSegments(1);
		int n = oldPath.segmentCount() - 1;
		int m = newPath.segmentCount() - 1;
		int k = 1;
		int width = 300;
		GC gc = new GC(retargetButtonGroup);
		try {
			while (n > 0 && m > 0) {
				final int s = oldPath.segmentCount() - n;
				String label = path.toString() + "/ -> " + path2.toString() + '/'; //$NON-NLS-1$
				Point textExtent = gc.textExtent(label);
				width = Math.max(width, textExtent.x);
				retargetButtonGroup.addButton(label);
				retargetButtonGroup.setData(k++, VALUE, s);
				if (!oldPath.segment(--n).equals(newPath.segment(--m)))
					break;
				path = path.removeLastSegments(1);
				path2 = path2.removeLastSegments(1);
			}
		} finally {
			gc.dispose();
		}
		layoutData.widthHint = Math.min(750, width) + 50;
		voiceNoteButton = WidgetFactory.createCheckButton(composite, Messages.RetargetDialog_retarget_voicenote,
				new GridData(SWT.FILL, SWT.CENTER, true, false));
		return area;
	}

	public int getResult() {
		return result;
	}

	public void handleEvent(Event e) {
		result = (Integer) ((RadioButtonGroup) e.widget).getData(e.detail, VALUE);
		updateButtons();
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
