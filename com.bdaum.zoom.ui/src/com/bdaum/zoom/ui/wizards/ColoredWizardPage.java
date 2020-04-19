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
package com.bdaum.zoom.ui.wizards;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.ui.internal.Icons;

public abstract class ColoredWizardPage extends WizardPage implements
		IAdaptable {

	/**
	 * Constructor
	 *
	 * @param pageName
	 *            - page id
	 */
	public ColoredWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * Constructor
	 *
	 * @param pageName
	 *            - page id
	 * @param title
	 *            - page title
	 * @param titleImage
	 *            - title image
	 */
	public ColoredWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/**
	 * Apply coloring scheme to wizard and set default image
	 */
	public void setColors() {
		CssActivator.getDefault().setColors(getShell());
		if (getImage() == null)
			setImageDescriptor(Icons.emptyTitle.getDescriptor());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter)) {
			Shell shell = getShell();
			if (shell != null) {
				while (shell.getParent() != null)
					shell = (Shell) shell.getParent();
				return shell;
			}
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (windows.length > 0)
				return windows[0].getShell();
		}
		return null;
	}

	/**
	 * Sets the help context ID for the page
	 *
	 * @param helpId
	 *            - help context ID
	 */
	protected void setHelp(String helpId) {
		if (helpId != null)
			PlatformUI.getWorkbench().getHelpSystem()
					.setHelp(getControl(), helpId);
	}

	@Override
	public void performHelp() {
		getControl().notifyListeners(SWT.Help, new Event());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(Composite parent) {
		setColors();
		validatePage();
	}

	/**
	 * Validates the page
	 */
	protected void validatePage() {
		String errorMsg = validate();
		setPageComplete(errorMsg == null);
		setErrorMessage(errorMsg);
	}
	
	/**
	 * Validates the page
	 */
	protected abstract String validate();


	/**
	 * Creates a composite formatted with a GridLayout with the specified number
	 * of columns
	 *
	 * @param parent
	 *            - parent container
	 * @param ncols
	 *            - number of columns
	 * @return - composite
	 */
	protected Composite createComposite(Composite parent, int ncols) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(ncols, false));
		return composite;
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		getControl().getParent().layout(true, true); // because of Linux rendering bug
	}

}
