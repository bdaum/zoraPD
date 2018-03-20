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
 * (c) 2009-2011 Berthold Daum  
 */

package com.bdaum.zoom.ui.dialogs;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.ZUiPlugin;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;

/**
 * This class is an adaption of the original Eclipse TitleAreaDialog. Other than
 * the TitleAreaDialog, the title area will adapt automatically to the message
 * size if no title image is set. If a title image is set, the title image
 * determines the size of the title area. The Eclipse default title image is not
 * used. If title images are set, they should have a transparent background to
 * fit into the different color schemes.
 *
 * Warning: The implementation of this class makes assumptions about the inner
 * workings of the TitleAreaDialog. Eclipse version changes could affect its
 * behavior.
 *
 */
public class ZTitleAreaDialog extends TitleAreaDialog implements IAdaptable {

	private static final int MESSAGEBOTTOMMARGIN = 5;
	private static final int LINESPACE = 3;
	/**
	 * Context help ID
	 */
	protected String helpId;
	/**
	 * True if catalog is read-only
	 */
	protected boolean readonly;
	/**
	 * DB manager
	 */
	protected IDbManager dbManager;

	private Composite workArea;
	private Control messageLabel;
	private int xTrim;
	private boolean hasTitleImage;
	private ZUiPlugin plugin;

	/**
	 * Constructor
	 *
	 * @param parentShell
	 *            - the parent shell or null
	 */
	public ZTitleAreaDialog(Shell parentShell) {
		this(parentShell, null);

	}

	/**
	 * Constructor
	 *
	 * @param parentShell
	 *            - the parent shell or null
	 * @param helpId
	 *            - the context help ID of the dialog
	 */
	public ZTitleAreaDialog(Shell parentShell, String helpId) {
		super(parentShell);
		this.helpId = helpId;
		setHelpAvailable(helpId != null);
		// We set a very small title image here, to make sure that the workarea
		// is attached to the title message label, not to the title image.
		super.setTitleImage(Icons.nullTitle.getImage());
		dbManager = Core.getCore().getDbManager();
		readonly = dbManager == null || dbManager.isReadOnly();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	@Override
	public void create() {
		super.create();
		CssActivator.getDefault().setColors(getShell());
		if (helpId != null)
			setHelpId(helpId);
		if (workArea != null) {
			// The workarea has been captured earlier in createDialogArea()
			Object layoutData = workArea.getLayoutData();
			if (layoutData instanceof FormData) {
				// Because the workarea is attached to the title message label,
				// we can grab the messageLabel from the FormAttachment
				messageLabel = ((FormData) layoutData).top.control;
				messageLabel.setFont(JFaceResources.getDefaultFont());
				if (messageLabel instanceof Scrollable)
					xTrim = ((Scrollable) messageLabel).computeTrim(0, 0, 100, 100).width - 100;
			}
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Constants.APPLICATION_NAME);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		// We just capture the parent container (the workarea) for further
		// analysis
		workArea = parent;
		return super.createDialogArea(parent);
	}

	@Override
	public void setMessage(String newMessage) {
		super.setMessage(newMessage);
		if (messageLabel != null && !hasTitleImage) {
			// If no title image was set, we recompute the message size
			// and update the FormData of the message label appropriately
			Object layoutData = messageLabel.getLayoutData();
			if (layoutData instanceof FormData) {
				TextLayout textLayout = new TextLayout(messageLabel.getDisplay());
				textLayout.setText(newMessage);
				textLayout.setFont(messageLabel.getFont());
				textLayout.setWidth(messageLabel.getSize().x - xTrim);
				int y = textLayout.getBounds().height;
				if (textLayout.getLineCount() > 1)
					y += LINESPACE;
				textLayout.dispose();
				((FormData) layoutData).height = y + MESSAGEBOTTOMMARGIN;
				workArea.getParent().layout(true);
				Shell shell = getShell();
				shell.setSize(shell.computeSize(shell.getSize().x, SWT.DEFAULT));
				((FormData) messageLabel.getLayoutData()).height = y + MESSAGEBOTTOMMARGIN;
				workArea.getParent().layout(true);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#setTitleImage(org.eclipse.swt
	 * .graphics.Image)
	 */
	@Override
	public void setTitleImage(Image titleImage) {
		hasTitleImage = titleImage != null;
		super.setTitleImage(titleImage);
	}

	protected void setText(Text field, String s) {
		field.setText(s == null ? "" : s); //$NON-NLS-1$
	}

	protected void setCondText(Text field, String s) {
		if (field != null && s != null)
			field.setText(s);
	}

	protected void setCondText(FileEditor field, String s) {
		if (s != null)
			field.setText(s);
	}

	/**
	 * Sets the context help ID of the dialog
	 *
	 * @param id
	 *            - help id
	 */
	public void setHelpId(String id) {
		setHelpAvailable(id != null);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(), id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter)) {
			Shell shell = getShell();
			while (shell.getParent() != null)
				shell = (Shell) shell.getParent();
			return shell;
		}
		return null;
	}

	protected IDialogSettings getDialogSettings(ZUiPlugin plugin, String id) {
		this.plugin = plugin;
		return plugin.getDialogSettings(id);
	}

	@Override
	public boolean close() {
		// We flush die dialog settings to file.
		// Otherwise they would not be saved when the native platform is shut
		// down
		if (plugin != null)
			plugin.saveDialogSettings();
		return super.close();
	}

}