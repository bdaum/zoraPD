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

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.dialogs.ZInputDialog;

public class GroupDialog extends ZInputDialog {

	public GroupDialog(Shell parentShell, final GroupImpl current, Group parent) {
		super(parentShell, parent == null ? Messages.GroupDialog_Group : NLS
				.bind(Messages.GroupDialog_subgroup_of, parent.getName()),
				Messages.GroupDialog_enter_group_name,
				current == null ? "" : current.getName(), //$NON-NLS-1$
				new IInputValidator() {

					public String isValid(String newText) {
						if (newText.length() == 0)
							return Messages.GroupDialog_specify_name;
						List<GroupImpl> set = Core
								.getCore()
								.getDbManager()
								.obtainObjects(GroupImpl.class,
										"name", newText, QueryField.EQUALS); //$NON-NLS-1$
						for (GroupImpl obj : set) {
							if (obj != current)
								return Messages.GroupDialog_name_already_exists;
						}
						return null;
					}
				}, false);
	}

}
