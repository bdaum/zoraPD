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
 * (c) 2015 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.widgets;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.LabelProvider;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.QueryField.Category;

public final class GroupComboLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof Category)
			return ((Category) element).toString();
		if (element instanceof QueryField) {
			QueryField qfield = ((QueryField) element);
			return MessageFormat.format(
					"{0}/{1}", qfield.getCategory(), qfield.getLabel()); //$NON-NLS-1$
		}
		return super.getText(element);
	}
}