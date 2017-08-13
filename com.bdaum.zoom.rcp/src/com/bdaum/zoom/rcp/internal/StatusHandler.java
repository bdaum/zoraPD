/*******************************************************************************
 * Copyright (c) 2009-2013 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.rcp.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.AbstractStatusHandler;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.rcp.internal.perspective.AbstractPerspective;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class StatusHandler extends AbstractStatusHandler {
	protected MessageDialog dialog;
	protected int errors = 0;

	@Override
	public void handle(StatusAdapter statusAdapter, int style) {
		if (style == StatusManager.SHOW) {
			final IStatus status = statusAdapter.getStatus();
			if (status.getSeverity() == IStatus.ERROR) {
				++errors;
				UiActivator uiActivator = UiActivator.getDefault();
				uiActivator.setRepairCat(true);
				final IWorkbench workbench = PlatformUI.getWorkbench();
				final Display display = workbench.getDisplay();
				display.asyncExec(() -> {
					if (!display.isDisposed()) {
						if (dialog != null) {
							dialog.close();
							dialog = null;
						}
						Shell activeShell = display.getActiveShell();
						if (activeShell != null) {
							IStatus aStatus = status;
							if (status instanceof MultiStatus) {
								IStatus[] children = ((MultiStatus) status)
										.getChildren();
								if (children.length == 1)
									aStatus = children[0];
							}
							String title = NLS.bind(Messages.getString("StatusHandler.error_report"), Constants.APPNAME);  //$NON-NLS-1$
							if (status == aStatus) {
								dialog = new AcousticMessageDialog(
										activeShell,
										title,
										null,
										NLS.bind(
												Messages.getString("StatusHandler.multiple_error_processing"), errors, status.getMessage()), //$NON-NLS-1$
										MessageDialog.ERROR,
										new String[] {
												IDialogConstants.OK_LABEL,
												Messages.getString("StatusHandler.View_log") }, 0); //$NON-NLS-1$
							} else
								dialog = new AcousticMessageDialog(
										activeShell,
										title,
										null,
										NLS.bind(
												Messages.getString("StatusHandler.Error_processing"), errors, aStatus.getMessage()), //$NON-NLS-1$
										MessageDialog.ERROR,
										new String[] { IDialogConstants.OK_LABEL },
										0);
							int ret = dialog.open();
							if (ret == 1) {
								dialog.close();
								IWorkbenchWindow activeWorkbenchWindow = workbench
										.getActiveWorkbenchWindow();
								if (activeWorkbenchWindow != null) {
									IWorkbenchPage activePage = activeWorkbenchWindow
											.getActivePage();
									if (activePage != null) {
										try {
											activePage
													.showView(AbstractPerspective.LOG_VIEW);
											Shell shell = activeWorkbenchWindow.getShell();
											shell.setVisible(true);
											shell.setMinimized(false);
											shell.forceActive();
										} catch (PartInitException e) {
											// ignore
										}
									}
								}
							}
							dialog = null;
						}
					}
				});
			}
		}
	}

}
