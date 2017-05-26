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

package com.bdaum.zoom.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.bdaum.zoom.ui.internal.UiActivator;

public abstract class AbstractFieldEditorPreferencePage extends
		FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	protected IWorkbench workbench;
	private boolean hasHelp;

	/**
	 * Default constructor
	 */
	public AbstractFieldEditorPreferencePage() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param style
	 *            - layout style (GRID, FLAT)
	 */
	public AbstractFieldEditorPreferencePage(int style) {
		super(style);
	}

	/**
	 * Constructor
	 *
	 * @param title
	 *            - page title
	 * @param style
	 *            - layout style (GRID, FLAT)
	 */
	public AbstractFieldEditorPreferencePage(String title, int style) {
		super(title, style);
	}

	/**
	 * Constructor
	 *
	 * @param title
	 *            - page title
	 * @param image
	 *            - page image
	 * @param style
	 *            - layout style (GRID, FLAT)
	 */
	public AbstractFieldEditorPreferencePage(String title,
			ImageDescriptor image, int style) {
		super(title, image, style);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.preference.PreferencePage#doGetPreferenceStore()
	 */

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return UiActivator.getDefault().getPreferenceStore();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */

	@Override
	public void createControl(final Composite parent) {
		super.createControl(parent);
		applyDialogFont(parent);
		parent.layout();
	}

	@Override
	protected void contributeButtons(Composite parent) {
		if (hasHelp) {
			AbstractPreferencePage.createHelpControl(parent);
			((GridLayout) parent.getLayout()).numColumns++;
		}
		super.contributeButtons(parent);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */

	public void init(IWorkbench wb) {
		this.workbench = wb;
	}

	/**
	 * Set help id
	 *
	 * @param id
	 *            - help id
	 */
	protected void setHelp(String id) {
		workbench.getHelpSystem().setHelp(getControl(), id);
		hasHelp = id != null;
	}

}