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

package com.bdaum.zoom.gps.internal;

import java.io.File;

import org.eclipse.core.runtime.IAdaptable;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.gps.internal.actions.GeonameAction;
import com.bdaum.zoom.ui.IDropinHandler;

public class DropinHandler implements IDropinHandler {

	public void handleDropin(File[] dropings, IAdaptable info) {
		if (!Core.getCore().getDbManager().isReadOnly())
			new GeonameAction().run(dropings, info);
	}

}
