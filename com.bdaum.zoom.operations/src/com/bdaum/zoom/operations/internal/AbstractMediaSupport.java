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
 * (c) 2017 Berthold Daum  
 */
package com.bdaum.zoom.operations.internal;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.mtp.StorageObject;

@SuppressWarnings("restriction")
public abstract class AbstractMediaSupport implements IMediaSupport {

	private String id;

	protected boolean isOutDated(List<AssetEnsemble> existing, long lastModified) {
		if (existing == null || existing.isEmpty())
			return true;
		Asset asset = existing.get(0).getAsset();
		Date importDate = asset.getImportDate();
		long imported = importDate == null ? 0L : importDate.getTime();
		if (imported < lastModified)
			return true;
		File[] sidecars = ImportState.getXmpURIs(asset);
		return sidecars.length > 0 && imported < sidecars[sidecars.length - 1].lastModified();
	}

	protected GroupImpl getMediaGroup(IDbManager dbManager) {
		GroupImpl group = dbManager.obtainById(GroupImpl.class, Constants.GROUP_ID_MEDIA);
		if (group == null) {
			group = new GroupImpl(Messages.getString("AbstractMediaSupport.media"), true, Constants.INHERIT_LABEL, //$NON-NLS-1$
					null, 0, 1, null);
			group.setStringId(Constants.GROUP_ID_MEDIA);
		}
		return group;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	protected void removeFromTransferfolder(ImportState importState, StorageObject originalFile, File[] files)
			throws IOException {
		if (originalFile != null && importState.fromTransferFolder()
				&& !files[0].equals(originalFile.getNativeObject())) {
			originalFile.delete(); // the file in the transfer folder must be deleted
			String parentPath = originalFile.getParentObject().getAbsolutePath();
			String name = originalFile.getName();
			int p = name.lastIndexOf('.');
			if (p >= 0)
				new File(parentPath, name.substring(0, p) + ".xmp").delete(); //$NON-NLS-1$
			new File(parentPath, name + ".xmp").delete(); //$NON-NLS-1$
		}
	}

}