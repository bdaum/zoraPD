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

package com.bdaum.zoom.ui.internal.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.dialogs.EditMetaDialog;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;

public class CatPreferencePage extends AbstractPreferencePage {

	@Override
	protected void createPageContents(Composite composite) {
		setHelp(HelpContextIds.CAT_PREFERENCE_PAGE);
		Label label = new Label(composite, SWT.WRAP);
		label.setText(Messages
				.getString("CatPreferencePage.catalog_settings_apply_to_specific")); //$NON-NLS-1$
		Button button = new Button(composite, SWT.PUSH);
		button.setText(Messages
				.getString("CatPreferencePage.edit_cat_settings")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkbenchWindow activeWorkbenchWindow = PlatformUI
						.getWorkbench().getActiveWorkbenchWindow();
				if (activeWorkbenchWindow != null) {
					final IWorkbenchPage activePage = activeWorkbenchWindow
							.getActivePage();
					BusyIndicator.showWhile(activeWorkbenchWindow.getShell()
							.getDisplay(), () -> {
								EditMetaDialog mdialog = new EditMetaDialog(
										getShell(), activePage, Core.getCore()
												.getDbManager(), false, null);
								mdialog.open();
							});
				}
			}
		});
		fillValues();
	}

	@Override
	protected void doFillValues() {
		// do nothing
	}

	@Override
	protected void doPerformDefaults() {
		// do nothing
	}

	@Override
	protected void doPerformOk() {
		// do nothing
	}

}
