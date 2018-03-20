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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.operations.internal;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class ModifyRelationLegendOperation extends DbOperation {

	private IIdentifiableObject rel;
	private RelationDescription newValues;
	private String[] oldValues = new String[4];
	private byte[] oldArchivedRecipe;

	public ModifyRelationLegendOperation(IIdentifiableObject rel,
			RelationDescription newValues) {
		super(Messages.getString("ModifyRelationLegendOperation.Modify_legend")); //$NON-NLS-1$
		this.rel = rel;
		this.newValues = newValues;
	}


	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		init(aMonitor, 1);
		if (rel instanceof ComposedToImpl) {
			ComposedToImpl comp = (ComposedToImpl) rel;
			oldValues[0] = comp.getType();
			oldValues[1] = comp.getTool();
			oldValues[2] = comp.getRecipe();
			oldValues[3] = comp.getParameterFile();
			comp.setType(newValues.kind);
			comp.setTool(newValues.tool);
			comp.setRecipe(newValues.recipe);
			comp.setParameterFile(newValues.parameterFile);
			comp.setDate(newValues.createdAt);
			storeSafely(null, 1, comp);
		} else if (rel instanceof DerivedByImpl) {
			DerivedByImpl deriv = (DerivedByImpl) rel;
			oldValues[1] = deriv.getTool();
			oldValues[2] = deriv.getRecipe();
			oldValues[3] = deriv.getParameterFile();
			oldArchivedRecipe = deriv.getArchivedRecipe();
			deriv.setTool(newValues.tool);
			deriv.setRecipe(newValues.recipe);
			deriv.setParameterFile(newValues.parameterFile);
			deriv.setDate(newValues.createdAt);
			deriv.setArchivedRecipe(newValues.archivedRecipe);
			storeSafely(null, 1, deriv);
		}
		fireHierarchyModified();
		return close(info);
	}


	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		return execute(aMonitor, info);
	}


	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		initUndo(aMonitor, 1);
		if (rel instanceof ComposedToImpl) {
			ComposedToImpl comp = (ComposedToImpl) rel;
			comp.setType(oldValues[0]);
			comp.setTool(oldValues[1]);
			comp.setRecipe(oldValues[2]);
			comp.setParameterFile(oldValues[3]);
			storeSafely(null, 1, comp);
		} else if (rel instanceof DerivedByImpl) {
			DerivedByImpl deriv = (DerivedByImpl) rel;
			deriv.setTool(oldValues[1]);
			deriv.setRecipe(oldValues[2]);
			deriv.setParameterFile(oldValues[3]);
			deriv.setArchivedRecipe(oldArchivedRecipe);
			storeSafely(null, 1, deriv);
		}
		fireHierarchyModified();
		return close(info);
	}


	public int getExecuteProfile() {
		return IProfiledOperation.HIERARCHY;
	}


	public int getUndoProfile() {
		return IProfiledOperation.HIERARCHY;
	}


	@Override
	public int getPriority() {
		return Job.SHORT;
	}

}
