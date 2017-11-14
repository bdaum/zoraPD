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

package com.bdaum.zoom.ui.internal.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.core.internal.CatLocation;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.ui.internal.actions.Messages;
import com.bdaum.zoom.ui.internal.commands.AbstractCatCommandHandler;
import com.bdaum.zoom.ui.internal.commands.NewCatalogCommand;
import com.bdaum.zoom.ui.internal.commands.OpenCatalogCommand;
import com.bdaum.zoom.ui.internal.commands.RestoreCatalogCommand;
import com.bdaum.zoom.ui.internal.widgets.ZDialog;

@SuppressWarnings("restriction")
public class StartupDialog extends ZDialog {

	private IWorkbenchWindow window;
	private final File catFile;
	private int nb = 0;
	private List<CatLocation> recents = new ArrayList<>(4);

	public StartupDialog(IWorkbenchWindow window, File catFile) {
		super(window);
		this.window = window;
		this.catFile = catFile;
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Messages.StartupDialog_Catalog_selection);
		Composite composite = (Composite) super.createDialogArea(parent);
		Label noActiveCatalogLabel = new Label(composite, SWT.WRAP);
		noActiveCatalogLabel.setFont(JFaceResources.getHeaderFont());
		noActiveCatalogLabel.setText(Messages.StartupDialog_No_active_cat);
		noActiveCatalogLabel.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false));
		new Label(composite, SWT.NONE);
		return composite;
	}

	@SuppressWarnings("unused")
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, 0, Messages.StartupDialog_Create_new__cat, false);
		createButton(parent, 1, Messages.StartupDialog_Open_existing_cat, false);
		createButton(parent, 3, Messages.StartupDialog_restore, false);
		createButton(parent, 2, Messages.StartupDialog_quit, false);
		CoreActivator activator = CoreActivator.getDefault();
		File file = activator.getDbManager().getFile();
		CatLocation current = file == null ? null : new CatLocation(file);
		LinkedList<CatLocation> recentCats = activator.getRecentCats();
		GridLayout gridLayout = (GridLayout) parent.getLayout();
		int c = gridLayout.numColumns;
		nb = 3;
		for (CatLocation cat : recentCats)
			if ((current == null || !current.equals(cat)) && cat.exists()) {
				new Label(parent, SWT.NONE);
				createButton(parent, ++nb, NLS.bind(Messages.StartupDialog_open_x, cat.getFile().getName()), false);
				recents.add(cat);
				new Label(parent, SWT.NONE);
				new Label(parent, SWT.NONE);
			}
		gridLayout.numColumns = c;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		for (int i = 0; i <= nb; i++)
			getButton(i).setEnabled(false);
		AbstractCatCommandHandler command = null;
		switch (buttonId) {
		case 0:
			getShell().setText(Messages.StartupDialog_creating_new);
			command = new NewCatalogCommand();
			command.setCatFile(catFile);
			break;
		case 1:
			getShell().setText(Messages.StartupDialog_opening_existing);
			command = new OpenCatalogCommand();
			break;
		case 2:
			cancelPressed();
			BatchActivator.setFastExit(true);
			PlatformUI.getWorkbench().close();
			return;
		case 3:
			getShell().setText(Messages.StartupDialog_restoring_backup);
			command = new RestoreCatalogCommand();
			break;
		default:
			CatLocation catLocation = recents.get(buttonId - 4);
			command = new OpenCatalogCommand();
			command.setCatFile(catLocation.getFile());
			break;
		}
		getShell().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
		command.init(window);
		command.run();
		okPressed();
	}
}
