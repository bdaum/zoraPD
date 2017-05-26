/*******************************************************************************
 * Copyright (c) 2009-2012 Berthold Daum.
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
import java.io.IOException;

import javax.swing.JOptionPane;

import com.bdaum.zoom.batch.internal.BatchActivator;

@SuppressWarnings("restriction")
public class Updater extends Thread {

	private final String[] updaterCommand;
	private final File cp;
	private final File installLocation;

	public Updater(File cp, String[] updaterCommand, File installLocation) {
		this.cp = cp;
		this.updaterCommand = updaterCommand;
		this.installLocation = installLocation;
	}

	@Override
	public void run() {
		if (!BatchActivator.isFastExit())
			try {
				String[] cmdarray = new String[updaterCommand == null ? 5
						: updaterCommand.length + 5];
				cmdarray[0] = System.getProperty("java.home") + "/bin/java"; //$NON-NLS-1$//$NON-NLS-2$
				cmdarray[1] = "-cp"; //$NON-NLS-1$
				cmdarray[2] = cp.getPath();
				cmdarray[3] = getClass().getName();
				cmdarray[4] = installLocation.getAbsolutePath();
				if (updaterCommand != null)
					System.arraycopy(updaterCommand, 0, cmdarray, 5,
							updaterCommand.length);
				Runtime.getRuntime().exec(cmdarray);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public static void main(String[] args) {
		if (args.length > 1)
			try {
				String[] cmdarray = new String[args.length + 1];
				cmdarray[0] = System.getProperty("java.home") + "/bin/java"; //$NON-NLS-1$//$NON-NLS-2$
				cmdarray[1] = "-jar"; //$NON-NLS-1$
				System.arraycopy(args, 1, cmdarray, 2, args.length - 1);
				Runtime.getRuntime().exec(cmdarray).waitFor();
				if (args.length >= 4)
					new File(args[3]).delete();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null,
						Messages.getString("Updater.installation_failed", e), //$NON-NLS-1$
						Messages.getString("Updater.update"), //$NON-NLS-1$
						JOptionPane.ERROR_MESSAGE);
				return;
			} catch (InterruptedException e) {
				// do nothing
			}
	}

}
