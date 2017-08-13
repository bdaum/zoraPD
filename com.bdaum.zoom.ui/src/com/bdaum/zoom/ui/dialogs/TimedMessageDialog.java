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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.dialogs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.batch.internal.Daemon;

@SuppressWarnings("restriction")
public class TimedMessageDialog extends AcousticMessageDialog {

	private IInputValidator validator;
	private int cancelIndex;

	private final long interval;
	private Daemon dialogJob;

	/**
	 * Create a timed message dialog. Note that the dialog will have no visual
	 * representation (no widgets) until it is told to open.
	 * <p>
	 * The dialogs validator is checked periodically. When it returns a value
	 * unequal <code>null</code> the dialog will cancel.
	 * </p>
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
	 * @param validator
	 *            validator
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
	 * @param cancelIndex
	 *            the index in the button label array of the cancel button
	 * @param interval
	 *            the interval for periodic validator checks
	 */
	public TimedMessageDialog(Shell parentShell, IInputValidator validator, String dialogTitle, Image dialogTitleImage,
			String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex, int cancelIndex,
			long interval) {

		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
				defaultIndex);
		Assert.isNotNull(validator);
		this.validator = validator;
		this.cancelIndex = cancelIndex;
		this.interval = interval;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.dialogs.AcousticMessageDialog#open()
	 */
	@Override
	public int open() {
		create();
		final Display display = getShell().getDisplay();
		dialogJob = new Daemon(Messages.TimedMessageDialog_update_dialog, interval) {
			@Override
			protected void doRun(IProgressMonitor monitor) {
				display.asyncExec(() -> execTimer());
			}
		};
		dialogJob.schedule(interval);
		return super.open();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	@Override
	public boolean close() {
		if (dialogJob != null)
			Job.getJobManager().cancel(dialogJob);
		return super.close();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.dialogs.IconAndMessageDialog#createButtonBar(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Composite bar = (Composite) super.createButtonBar(parent);
		bar.setTabList(bar.getChildren());
		return bar;
	}

	/**
	 * Called periodically by the timer. The default implementation checks if the
	 * validator returns null. If yes, it fires the Cancel button. Subclasses may
	 * override.
	 */
	@Override
	protected void execTimer() {
		if (validator.isValid(null) == null) {
			Shell shell = getShell();
			if (shell != null && !shell.isDisposed())
				shell.getDisplay().asyncExec(() -> {
					if (!shell.isDisposed())
						buttonPressed(cancelIndex);
				});
		}
	}

}
