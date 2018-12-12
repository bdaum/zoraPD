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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.commands;

import java.io.File;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;

import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.Messages;

@SuppressWarnings("restriction")
public abstract class AbstractCatCommandHandler extends AbstractCommandHandler {

	protected File catFile;

	public void setCatFile(File catFile) {
		this.catFile = catFile;
	}

	protected void resetOperationHistory() {
		IWorkbenchOperationSupport operationSupport = getActiveWorkbenchWindow().getWorkbench().getOperationSupport();
		IOperationHistory operationHistory = operationSupport.getOperationHistory();
		IUndoContext undoContext = operationSupport.getUndoContext();
		int limit = operationHistory.getLimit(undoContext);
		operationHistory.setLimit(undoContext, 0);
		operationHistory.setLimit(undoContext, limit);
	}

	protected void resetNavigationHistory() {
		Ui.getUi().getNavigationHistory(getActiveWorkbenchWindow()).resetHistory();
	}

	protected boolean preCatClose(boolean hideShell) {
		return UiActivator.getDefault().preCatClose(CatalogListener.NORMAL, Messages.AbstractCatAction_closing_cat,
				Messages.AbstractCatAction_pending_operations, hideShell);
	}

	protected void postCatOpen(String filename, boolean newDb) {
		resetOperationHistory();
		resetNavigationHistory();
		getShell().setText(Constants.APPLICATION_NAME + " - " //$NON-NLS-1$
				+ filename);
		CoreActivator.getDefault().setCatFile(new File(filename));
		CoreActivator.getDefault().fireCatalogOpened(newDb);
		UiActivator.getDefault().postCatOpen();
	}

	protected void postCatInit(boolean startup) {
		UiActivator.getDefault().postCatInit(startup);
	}

}
