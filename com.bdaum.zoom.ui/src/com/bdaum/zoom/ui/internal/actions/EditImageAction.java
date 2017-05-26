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

package com.bdaum.zoom.ui.internal.actions;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;

import com.bdaum.zoom.ui.internal.preferences.EditorDescriptor;
import com.bdaum.zoom.ui.internal.preferences.FileEditorMapping;

public class EditImageAction extends EditWithAction {

	public EditImageAction(String label, String tooltip, ImageDescriptor image,
			IAdaptable adaptable) {
		super(label, tooltip, image, adaptable);
	}

	protected Set<EditorDescriptor> computeDefaultEditors(
			Set<EditorDescriptor> defaultEditors, FileEditorMapping mapping) {
		if (defaultEditors == null) 
			defaultEditors = getValidEditors(mapping.getDeclaredDefaultEditors());
		else 
			defaultEditors.retainAll(Arrays.asList(mapping
					.getDeclaredDefaultEditors()));
		return defaultEditors;
	}

	protected Set<EditorDescriptor> computeEditors(
			Set<EditorDescriptor> editors,
			Set<EditorDescriptor> allowedEditors, FileEditorMapping mapping) {
		if (editors == null) 
			editors = getValidEditors(mapping.getEditors());
		else {
			List<EditorDescriptor> defaultEditors = Arrays.asList(mapping
					.getDeclaredDefaultEditors());
			defaultEditors.retainAll(allowedEditors);
			editors.addAll(defaultEditors);
		}
		return editors;
	}

	
	@Override
	protected boolean isDefault() {
		return true;
	}
}
