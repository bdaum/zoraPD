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

package com.bdaum.zoom.ui;

import org.eclipse.ui.IWorkbenchActionConstants;

public interface IZoomActionConstants extends IWorkbenchActionConstants {

	// Menu

	String M_CATALOG = "catalog"; //$NON-NLS-1$
	String CATALOG_START = "catStart"; //$NON-NLS-1$
	String CATALOG_EXT1 = "catExt1"; //$NON-NLS-1$
	String CATALOG_EXT2 = "catExt2"; //$NON-NLS-1$
	String CATALOG_EXT3 = "catExt3"; //$NON-NLS-1$
	String CATALOG_END = "catEnd"; //$NON-NLS-1$
	String M_MAINTENANCE = "maintenance"; //$NON-NLS-1$
	String MAINTENANCE_START = "maintenanceStart"; //$NON-NLS-1$
	String MAINTENANCE_EXT = "maintenanceEnd"; //$NON-NLS-1$
	String M_FIND = "find"; //$NON-NLS-1$
	String FIND_START = "findStart"; //$NON-NLS-1$
	String FIND_EXT = "findExt"; //$NON-NLS-1$
	String M_IMPORT = "import"; //$NON-NLS-1$
	String IMPORT_START = "importStart"; //$NON-NLS-1$
	String IMPORT_EXT = "importExt"; //$NON-NLS-1$
	String FILE_EXT1 = "fileExt1"; //$NON-NLS-1$
	String FILE_EXT2 = "fileExt2"; //$NON-NLS-1$
	String M_VOICE = "voice"; //$NON-NLS-1$
	String VOICE_START = "voiceStart"; //$NON-NLS-1$
	String VOICE_EXT = "voiceExt"; //$NON-NLS-1$
	String NAV_EXT1 = "navExt1"; //$NON-NLS-1$
	String M_HIERARCHY = "hierarchy"; //$NON-NLS-1$
	String HIERARCHY_START = "hierarchyStart"; //$NON-NLS-1$
	String HIERARCHY_EXT = "hierarchyExt"; //$NON-NLS-1$
	String NAV_EXT2 = "navExt2"; //$NON-NLS-1$
	String M_IMAGE = "image"; //$NON-NLS-1$
	String IMAGE_START = "imageStart"; //$NON-NLS-1$
	String IMAGE_EXT1 = "imageExt1"; //$NON-NLS-1$
	String IMAGE_EXT2 = "imageExt2"; //$NON-NLS-1$
	String IMAGE_EXT3 = "imageExt3"; //$NON-NLS-1$
	String IMAGE_END = "imageEnd"; //$NON-NLS-1$
	String M_META = "meta"; //$NON-NLS-1$
	String META_START = "metaStart"; //$NON-NLS-1$
	String META_EXT1 = "metaExt1"; //$NON-NLS-1$
	String META_EXT2 = "metaExt2"; //$NON-NLS-1$
	String META_END = "metaEnd"; //$NON-NLS-1$
	String WINDOW_START = "windowStart"; //$NON-NLS-1$
	String WINDOW_EXT = "windowExt"; //$NON-NLS-1$
	String WINDOW_EXT1 = IWorkbenchActionConstants.WINDOW_EXT;
	String HELP_EXT = "helpExt"; //$NON-NLS-1$
	String HELP_UPDATE = "helpUpdate"; //$NON-NLS-1$

	// Toolbar

	String TOOLBAR_NAV = "com.bdaum.zoom.workbench.nav"; //$NON-NLS-1$
	String NAV_NAV = "nav"; //$NON-NLS-1$
	String GROUP_NAV_EXT = "nav.ext"; //$NON-NLS-1$

	String TOOLBAR_CAT = "com.bdaum.zoom.workbench.cat"; //$NON-NLS-1$
	String CAT_EXT = "cat.new"; //$NON-NLS-1$
	String CAT_IMPORT = "cat.import"; //$NON-NLS-1$
	String CAT_EXPORT = "cat.export"; //$NON-NLS-1$
	String CAT_PROPS = "cat.props"; //$NON-NLS-1$
	String GROUP_NEW_EXT = "new"; //$NON-NLS-1$
	String GROUP_IMPORT_EXT = "import.ext"; //$NON-NLS-1$
	String GROUP_EXPORT_EXT = "export.ext"; //$NON-NLS-1$
	String GROUP_PROPS_EXT = "props.ext"; //$NON-NLS-1$

	String TOOLBAR_IMAGE = "com.bdaum.zoom.workbench.image"; //$NON-NLS-1$
	String IMAGE_FILE = "image.file"; //$NON-NLS-1$
	String IMAGE_UNDO = "image.undo"; //$NON-NLS-1$
	String IMAGE_COPY = "image.copy"; //$NON-NLS-1$
	String IMAGE_MOVE = "image.move"; //$NON-NLS-1$
	String IMAGE_ROTATE = "image.rotate"; //$NON-NLS-1$
	String IMAGE_EDIT = "image.edit"; //$NON-NLS-1$
	String IMAGE_QUERY = "image.query"; //$NON-NLS-1$
	String GROUP_FILE_EXT = "file.ext"; //$NON-NLS-1$
	String GROUP_UNDO_EXT = "undo.ext"; //$NON-NLS-1$
	String GROUP_COPY_EXT = "copy.ext"; //$NON-NLS-1$
	String GROUP_MOVE_EXT = "move.ext"; //$NON-NLS-1$
	String GROUP_ROTATE_EXT = "rotate.ext"; //$NON-NLS-1$
	String GROUP_EDIT_EXT = "edit.ext"; //$NON-NLS-1$
	String GROUP_QUERY_EXT = "query.ext"; //$NON-NLS-1$

	String HELP_HELP = "help"; //$NON-NLS-1$
	String GROUP_HELP_EXT = "help.ext"; //$NON-NLS-1$
	String GROUP_SEARCH = "group.search"; //$NON-NLS-1$

	// Context menus

	String MB_SUBMENUS = "submenus"; //$NON-NLS-1$
	String MB_LINK = "link"; //$NON-NLS-1$
	String MB_HIERARCHY = "hierarchy"; //$NON-NLS-1$
	String MB_EDIT = "edit"; //$NON-NLS-1$
	String MB_SEARCH = "search"; //$NON-NLS-1$
	String MB_ROTATE = "rotate"; //$NON-NLS-1$
	String MB_VOICE = "voice"; //$NON-NLS-1$
	String MB_META = "meta"; //$NON-NLS-1$

}
