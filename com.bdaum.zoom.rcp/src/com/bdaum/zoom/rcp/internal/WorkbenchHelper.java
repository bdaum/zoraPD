/*******************************************************************************
 * Copyright (c) 2009-2017 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.rcp.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * This helper class can flush the workbench state without closing the
 * workbench. This is advisable when the application is sent to the tray. This
 * class replicates some code from the Workbench class and must be updated when
 * a new version of the Workbench is used
 */
@SuppressWarnings("restriction")
public final class WorkbenchHelper {

	static final String DEFAULT_WORKBENCH_STATE_FILENAME = "workbench_catState.xml"; //$NON-NLS-1$

	private WorkbenchAdvisor advisor;

	private final boolean saveAndRestore;

	public WorkbenchHelper(WorkbenchAdvisor advisor, boolean saveAndRestore) {
		this.advisor = advisor;
		this.saveAndRestore = saveAndRestore;
	}

	public void flushWorkbenchState() {
		if (saveAndRestore) {
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					XMLMemento mem = recordWorkbenchState();
					if (mem != null)
						saveMementoToFile(mem);
				}

				@Override
				public void handleException(Throwable e) {
					RcpActivator.getDefault()
							.logError(Messages.getString("WorkbenchHelper.error_flushing_workbench_state"), e); //$NON-NLS-1$
				}
			});
		}
	}

	/*
	 * Record the workbench UI in a document
	 */
	private XMLMemento recordWorkbenchState() {
		XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WORKBENCH);
		final IStatus status = saveState(memento);
		if (status.getSeverity() != IStatus.OK) {
			RcpActivator.getDefault().getLog().log(status);
			return null;
		}
		return memento;
	}

	/*
	 * Saves the current state of the workbench so it can be restored later on
	 */
	private IStatus saveState(IMemento memento) {
		MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
		result.add(advisor.saveState(memento.createChild(IWorkbenchConstants.TAG_WORKBENCH_ADVISOR)));
		return result;
	}

	/*
	 * Save the workbench UI in a persistence file.
	 */
	private static boolean saveMementoToFile(XMLMemento memento) {
		File stateFile = getWorkbenchStateFile();
		if (stateFile != null)
			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(stateFile), "utf-8")) { //$NON-NLS-1$
				memento.save(writer);
				return true;
			} catch (IOException e) {
				stateFile.delete();
				MessageDialog.openError((Shell) null, "", ""); //$NON-NLS-1$//$NON-NLS-2$
			}
		return false;
	}

	/*
	 * Answer the workbench state file.
	 */
	private static File getWorkbenchStateFile() {
		IPath path = WorkbenchPlugin.getDefault().getDataLocation();
		if (path == null)
			return null;
		path = path.append(DEFAULT_WORKBENCH_STATE_FILENAME);
		return path.toFile();
	}

	public void restoreWorkbenchState() {
		if (saveAndRestore) {
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					XMLMemento mem = readMementoFromFile();
					if (mem != null)
						advisor.restoreState(mem.getChild(IWorkbenchConstants.TAG_WORKBENCH_ADVISOR));
				}

				@Override
				public void handleException(Throwable e) {
					RcpActivator.getDefault()
							.logError(Messages.getString("WorkbenchHelper.error_flushing_workbench_state"), e); //$NON-NLS-1$
				}
			});
		}
	}

	/**
	 * Read a memento from the state directory
	 * 
	 */
	private static XMLMemento readMementoFromFile() {
		File stateFile = getWorkbenchStateFile();
		if (stateFile != null)
			try (InputStreamReader reader = new InputStreamReader(new FileInputStream(stateFile),
					StandardCharsets.UTF_8)) {
				return XMLMemento.createReadRoot(reader);
			} catch (FileNotFoundException e) {
				// do nothing
			} catch (Exception e) {
				RcpActivator.getDefault().logError(Messages.getString("WorkbenchHelper.error_reading_state"), e); //$NON-NLS-1$
			}
		return null;
	}

}
