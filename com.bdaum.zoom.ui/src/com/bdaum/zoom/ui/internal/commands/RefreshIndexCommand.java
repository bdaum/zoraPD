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
 * (c) 2016 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.commands;

import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.actions.Messages;

@SuppressWarnings("restriction")
public class RefreshIndexCommand extends AbstractCommandHandler {

	@Override
	public void run() {
		if (AcousticMessageDialog.openQuestion(getShell(),
				Messages.RefreshIndexActionDelegate_refresh_index,
				Messages.RefreshIndexActionDelegate_refresh_index_message))
			CoreActivator.getDefault().recreateIndex();	}

}
