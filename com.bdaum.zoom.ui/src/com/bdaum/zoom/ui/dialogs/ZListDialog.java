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

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.css.internal.CssActivator;

/**
 * This class adapts the Eclipse ListDialog
 *
 */
public class ZListDialog extends ListDialog {

	/**
	 * true if catalog is read-only
	 */
	protected boolean readonly;

	private int style;

	/**
	 * Create a new instance of the receiver with parent shell of parent.
	 *
	 * @param parent
	 *            - parent shell or null
	 * @param style
	 *            - table style
	 */
	public ZListDialog(Shell parent, int style) {
		super(parent);
		this.style = style;
		setHelpAvailable(true);
		readonly = Core.getCore().getDbManager().isReadOnly();
	}
	
	@Override
	public void create() {
		super.create();
		getShell().setText(Constants.APPLICATION_NAME);
		CssActivator.getDefault().setColors(getShell());
	}

	/**
	 * Sets the context help ID
	 *
	 * @param id
	 *            - help ID
	 */
	public void setHelpId(String id) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(), id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.dialogs.ListDialog#getTableStyle()
	 */
	@Override
	protected int getTableStyle() {
		return style;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.dialogs.ListDialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite container) {
		Composite comp = (Composite) super.createDialogArea(container);
		createClientContent(comp);
		return comp;
	}

	/**
	 * Creates additional content. Subclasses should overwrite
	 *
	 * @param parent
	 *            - parent container
	 */
	protected void createClientContent(Composite parent) {
		// do nothing
	}

	/**
	 * Sets the list selection
	 *
	 * @param object
	 *            - object to select
	 */
	public void setSelection(Object object) {
		getTableViewer().setSelection(new StructuredSelection(object));
	}

	/**
	 * Sets the viewer comparator for sorting items
	 *
	 * @param comparator - the comparator
	 */
	public void setComparator(ViewerComparator comparator) {
		getTableViewer().setComparator(comparator);
	}

}