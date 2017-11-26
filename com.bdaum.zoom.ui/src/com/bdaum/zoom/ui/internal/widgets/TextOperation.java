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
 * (c) 2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Point;

class TextOperation extends AbstractOperation {

	private final IAugmentedTextField textField;
	private String text;
	private String replacement;
	private Point oldSelection = new Point(-1, -1);
	private Point newSelection = new Point(-1, -1);
	private boolean added;

	public TextOperation(IAugmentedTextField textField, IUndoContext context, String text, Point selection) {
		super(Messages.TextOperation_edit_text);
		this.textField = textField;
		this.text = text;
		oldSelection.x = selection.x;
		oldSelection.y = selection.y;
		addContext(context);
	}

	public void setReplacement(String replacement, Point selection) {
		this.replacement = replacement;
		newSelection.x = selection.x;
		newSelection.y = selection.y;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		this.textField.setText(replacement);
		textField.setSelection(newSelection);
		return Status.OK_STATUS;
	}

	@Override
	public boolean canUndo() {
		return !isEmpty();
	}

	@Override
	public boolean canRedo() {
		return !isEmpty();
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		this.textField.setText(text);
		textField.setSelection(oldSelection);
		return Status.OK_STATUS;
	}

	public boolean isEmpty() {
		return replacement == null;
	}

	public void addToHistory(IOperationHistory hist) {
		if (!added && !isEmpty()) {
			hist.add(this);
			added = true;
		}
	}

}