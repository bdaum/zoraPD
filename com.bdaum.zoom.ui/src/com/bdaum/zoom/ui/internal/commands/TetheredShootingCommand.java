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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.commands;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.mtp.StorageObject;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;

public class TetheredShootingCommand extends AbstractCommandHandler {

	private static final String ID = "com.bdaum.zoom.ui.command.tethered"; //$NON-NLS-1$
	private Command command;

	@Override
	protected void init(ExecutionEvent event) {
		super.init(event);
		if (event != null)
			command = event.getCommand();
		else {
			ICommandService commandService = PlatformUI.getWorkbench().getAdapter(ICommandService.class);
			if (commandService != null)
				command = commandService.getCommand(ID);
		}
	}

	@Override
	public void run() {
		if (command != null)
			try {
				boolean wasChecked = HandlerUtil.toggleCommandState(command);
				UiActivator activator = UiActivator.getDefault();
				if (!wasChecked) {
					if (activator.isTetheredShootingActive()) {
						AcousticMessageDialog.openWarning(getShell(), Messages.TetheredShootingCommand_tethered,
								Messages.TetheredShootingCommand_tethered_active);
						HandlerUtil.toggleCommandState(command);
						return;
					}
					StorageObject[] dcims = Core.getCore().getVolumeManager().findDCIMs();
					if (dcims.length == 0) {
						AcousticMessageDialog.openWarning(getShell(), Messages.TetheredShootingCommand_tethered,
								Messages.TetheredShootingCommand_nothing_connected);
						HandlerUtil.toggleCommandState(command);
						return;
					}
					IJobChangeListener jobListener = new JobChangeAdapter() {
						@Override
						public void done(IJobChangeEvent event) {
							getShell().getDisplay().asyncExec(() -> {
								try {
									if (!HandlerUtil.toggleCommandState(command))
										HandlerUtil.toggleCommandState(command);
								} catch (ExecutionException e) {
									// do nothing
								}
							});
						}
					};
					if (!activator.startTetheredShooting(dcims, jobListener))
						HandlerUtil.toggleCommandState(command);
				} else
					activator.endTetheredShooting();
			} catch (ExecutionException e) {
				// ignore
			}
	}

}
