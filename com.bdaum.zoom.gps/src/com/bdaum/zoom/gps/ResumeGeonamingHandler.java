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
 * (c) 2012 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.gps;

import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.internal.IResumeHandler;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.operations.GeotagOperation;
import com.bdaum.zoom.job.OperationJob;

@SuppressWarnings("restriction")
public class ResumeGeonamingHandler implements IResumeHandler, IAdaptable {

	private static final long ONEMINUTE = 60000L;

	public void resumeWork(Meta meta) {
		Set<String> postponedNaming = meta.getPostponedNaming();
		if (postponedNaming != null && postponedNaming.size() > 0)
			OperationJob.executeOperation(new GeotagOperation(GpsActivator
					.getDefault().createGpsConfiguration()), this, false,
					ONEMINUTE);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter)) {
			IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench()
					.getWorkbenchWindows();
			if (workbenchWindows.length > 0)
				return workbenchWindows[0].getShell();
		}
		return null;
	}

}
