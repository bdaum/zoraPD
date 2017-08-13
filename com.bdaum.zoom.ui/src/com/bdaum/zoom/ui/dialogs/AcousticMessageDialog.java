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
 * (c) 2009-2015 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.dialogs;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.db.IValidator;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

public class AcousticMessageDialog extends MessageDialog {

	private int type;
	private String dialogTitle;
	private IValidator validator;
	private Timer timer;
	private int y = -1;

	/**
	 * Convenience method to open a simple confirm (OK/Cancel) dialog.
	 *
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 * @return <code>true</code> if the user presses the OK button,
	 *         <code>false</code> otherwise
	 */
	public static boolean openConfirm(Shell parent, String title, String message) {
		MessageDialog dialog = new AcousticMessageDialog(parent, title, null, // accept
				// the
				// default
				// window
				// icon
				message, QUESTION, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0); // OK
																													// is
																													// the
		// default
		return dialog.open() == 0;
	}

	/**
	 * Convenience method to open a standard error dialog.
	 *
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 */
	public static void openError(Shell parent, String title, String message) {
		MessageDialog dialog = new AcousticMessageDialog(parent, title, null, // accept
				// the
				// default
				// window
				// icon
				message, ERROR, new String[] { IDialogConstants.OK_LABEL }, 0); // ok
		// is
		// the
		// default
		dialog.open();
	}

	/**
	 * Convenience method to open a standard information dialog.
	 *
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 */
	public static void openInformation(Shell parent, String title, String message) {
		openInformation(parent, title, message, null);
	}

	/**
	 * Convenience method to open a standard information dialog.
	 *
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 * @param validator
	 *            when the validator is executed and returns true the dialog closes
	 */
	public static void openInformation(Shell parent, String title, String message, IValidator validator) {
		AcousticMessageDialog dialog = new AcousticMessageDialog(parent, title, null, // accept
				// the
				// default
				// window
				// icon
				message, INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0);
		// ok is the default
		dialog.setValidator(validator);
		dialog.open();
	}

	/**
	 * Convenience method to open a simple Yes/No question dialog.
	 *
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 * @return <code>true</code> if the user presses the OK button,
	 *         <code>false</code> otherwise
	 */
	public static boolean openQuestion(Shell parent, String title, String message) {
		MessageDialog dialog = new AcousticMessageDialog(parent, title, null, // accept
				// the
				// default
				// window
				// icon
				message, QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0); // yes
																												// is
																												// the
																												// default
		dialog.create();
		return dialog.open() == 0;
	}

	/**
	 * Convenience method to open a standard warning dialog.
	 *
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 */
	public static void openWarning(Shell parent, String title, String message) {
		MessageDialog dialog = new AcousticMessageDialog(parent, title, null, // accept
				// the
				// default
				// window
				// icon
				message, WARNING, new String[] { IDialogConstants.OK_LABEL }, 0); // ok
		// is
		// the
		// default
		dialog.open();
		return;
	}

	/**
	 * Create a message dialog. Note that the dialog will have no visual
	 * representation (no widgets) until it is told to open.
	 * <p>
	 * The labels of the buttons to appear in the button bar are supplied in this
	 * constructor as an array. The <code>open</code> method will return the index
	 * of the label in this array corresponding to the button that was pressed to
	 * close the dialog.
	 * </p>
	 * <p>
	 * <strong>Note:</strong> If the dialog was dismissed without pressing a button
	 * (ESC key, close box, etc.) then {@link SWT#DEFAULT} is returned. Note that
	 * the <code>open</code> method blocks.
	 * </p>
	 *
	 * @param parentShell
	 *            the parent shell
	 * @param dialogTitle
	 *            the dialog title, or <code>null</code> if none
	 * @param dialogTitleImage
	 *            the dialog title image, or <code>null</code> if none
	 * @param dialogMessage
	 *            the dialog message
	 * @param dialogImageType
	 *            one of the following values:
	 *            <ul>
	 *            <li><code>MessageDialog.NONE</code> for a dialog with no image
	 *            </li>
	 *            <li><code>MessageDialog.ERROR</code> for a dialog with an error
	 *            image</li>
	 *            <li><code>MessageDialog.INFORMATION</code> for a dialog with an
	 *            information image</li>
	 *            <li><code>MessageDialog.QUESTION </code> for a dialog with a
	 *            question image</li>
	 *            <li><code>MessageDialog.WARNING</code> for a dialog with a warning
	 *            image</li>
	 *            </ul>
	 * @param dialogButtonLabels
	 *            an array of labels for the buttons in the button bar
	 * @param defaultIndex
	 *            the index in the button label array of the default button
	 */

	public AcousticMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
			int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, Constants.OSX ? dialogTitle + "\n\n" + dialogMessage //$NON-NLS-1$
				: dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
		setShellStyle(Constants.OSX ? getShellStyle() | SWT.SHEET : SWT.APPLICATION_MODAL);
		this.type = dialogImageType;
		this.dialogTitle = dialogTitle;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public void create() {
		super.create();
		Shell shell = getShell();
		CssActivator.getDefault().setColors(shell);
		Rectangle bounds = shell.getBounds();
		shell.setText(Constants.APPLICATION_NAME);
		if (!Constants.OSX) {
			Rectangle rootBounds = getRootShell(shell).getBounds();
			int oHeight = (messageLabel != null) ? messageLabel.getBounds().height : 0;
			int mHeight = (messageLabel != null) ? messageLabel.computeSize(rootBounds.width, SWT.DEFAULT, true).y : 0;
			shell.setBounds(rootBounds.x, y >= 0 ? y : bounds.y, rootBounds.width, bounds.height + mHeight - oHeight);
		} else if (y >= 0)
			shell.setLocation(bounds.x, y);
		shell.forceActive();
	}

	@Override
	public void setReturnCode(int code) {
		super.setReturnCode(code);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contents = (Composite) super.createContents(parent);
		if (!Constants.OSX) {
			Control buttonBar = getButtonBar();
			GridLayout gridLayout = (GridLayout) buttonBar.getParent().getLayout();
			gridLayout.marginHeight += 10;
			GridData layoutData = new GridData(SWT.CENTER, SWT.BEGINNING, true, true, gridLayout.numColumns, 1);
			layoutData.minimumHeight = 30;
			buttonBar.setLayoutData(layoutData);
		}
		return contents;
	}

	private static Shell getRootShell(Shell shell) {
		Shell parent = shell;
		while (parent.getParent() instanceof Shell)
			parent = (Shell) parent.getParent();
		return parent;
	}

	@Override
	protected Control createMessageArea(Composite parent) {
		if (Constants.OSX)
			return super.createMessageArea(parent);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1));
		composite.setLayout(new GridLayout(2, false));
		Image image = getImage();
		imageLabel = new Label(composite, SWT.NULL);
		if (image != null) {
			image.setBackground(imageLabel.getBackground());
			imageLabel.setImage(image);
		}
		Label titleLabel = new Label(composite, SWT.NONE);
		titleLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		if (dialogTitle != null) {
			titleLabel.setFont(JFaceResources.getFont(UiConstants.MESSAGETITLEFONT));
			titleLabel.setText(dialogTitle);
		}
		// create message
		if (message != null) {
			composite = new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1));
			GridLayout layout = new GridLayout();
			layout.marginWidth = 15;
			composite.setLayout(layout);
			messageLabel = new Label(composite, getMessageLabelStyle());
			messageLabel.setFont(JFaceResources.getFont(UiConstants.MESSAGEFONT));
			messageLabel.setAlignment(message.indexOf('\t') >= 0 ? SWT.LEFT : SWT.CENTER);
			messageLabel.setText(message);
			messageLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		}
		return parent;
	}

	private void setValidator(IValidator validator) {
		this.validator = validator;
	}

	@Override
	public int open() {
		String sound;
		switch (type) {
		case ERROR:
			sound = "error"; //$NON-NLS-1$
			break;
		case WARNING:
			sound = "warning"; //$NON-NLS-1$
			break;
		case INFORMATION:
			sound = "ding"; //$NON-NLS-1$
			break;
		case QUESTION:
			sound = "question"; //$NON-NLS-1$
			break;
		default:
			sound = "ding"; //$NON-NLS-1$
			break;
		}
		Ui.getUi().playSound(sound, PreferenceConstants.ALARMONPROMPT);
		create();
		if (validator != null) {
			final Display display = getShell().getDisplay();
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					display.asyncExec(() -> execTimer());
				}
			}, 300L, 300L);
		}
		return super.open();
	}

	@Override
	public boolean close() {
		if (timer != null)
			timer.cancel();
		return super.close();
	}

	protected void execTimer() {
		if (validator != null && validator.validate()) {
			Shell shell = getShell();
			if (shell != null && !shell.isDisposed())
				shell.getDisplay().asyncExec(() -> {
					if (!shell.isDisposed())
						buttonPressed(getDefaultButtonIndex());
				});
		}
	}

}
