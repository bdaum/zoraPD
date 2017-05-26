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
package com.bdaum.zoom.operations.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileWatchManager;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.program.DiskFullException;

@SuppressWarnings("restriction")
public abstract class AbstractRenamingOperation extends DbOperation {

	protected FileWatchManager fileWatchManager = CoreActivator.getDefault()
			.getFileWatchManager();

	public AbstractRenamingOperation(String label) {
		super(label);
	}

	protected void renameAsset(Asset renamedAsset, File file, File dest,
			IProgressMonitor aMonitor, FileWatchManager fwManager) {
		URI[] xmpOrigURIs = Core.getSidecarURIs(file.toURI());
		URI newURI = dest.toURI();
		URI[] xmpTargetURIs = Core.getSidecarURIs(newURI);
		URI voiceOrigURI = null;
		URI voiceTargetURI = null;
		String voiceFileURI = renamedAsset.getVoiceFileURI();
		if (".".equals(voiceFileURI)) { //$NON-NLS-1$
			voiceOrigURI = Core.getVoicefileURI(file);
			voiceTargetURI = Core.getVoicefileURI(dest);
		}
		try {
			fwManager.moveFileSilently(file, dest, opId, aMonitor);
			String nu = newURI.toString();
			renamedAsset.setUri(nu);
			renamedAsset.setName(Core.getFileName(nu, false));
			dbManager.store(renamedAsset);
			aMonitor.worked(1);
			for (int i = 0; i < xmpOrigURIs.length; i++) {
				File xmpFile = new File(xmpOrigURIs[i]);
				File xmpTarget = null;
				if (xmpFile.exists()) {
					xmpTarget = new File(xmpTargetURIs[i]);
					if (xmpTarget.exists())
						xmpTarget.delete();
					fwManager.moveFileSilently(xmpFile, xmpTarget, opId,
							aMonitor);
				}
			}
			if (voiceOrigURI != null) {
				File voiceFile = new File(voiceOrigURI);
				File voiceTarget = null;
				if (voiceFile.exists()) {
					voiceTarget = new File(voiceTargetURI);
					if (voiceTarget.exists())
						voiceTarget.delete();
					fwManager.moveFileSilently(voiceFile, voiceTarget, opId,
							aMonitor);
				}
			}
		} catch (IOException e) {
			addError(
					NLS.bind(
							Messages.getString("RenameAssetOperation.renaming_failed"), file), e); //$NON-NLS-1$
		} catch (DiskFullException e) {
			addError(NLS.bind(
					Messages.getString("RenameAssetOperation.renaming_failed"),//$NON-NLS-1$
					file), e);
		}
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT | IProfiledOperation.FILE;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT | IProfiledOperation.FILE;
	}

	@Override
	public int getPriority() {
		return Job.SHORT;
	}

}