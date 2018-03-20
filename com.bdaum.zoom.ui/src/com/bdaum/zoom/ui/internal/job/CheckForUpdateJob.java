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

package com.bdaum.zoom.ui.internal.job;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Version;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;

public class CheckForUpdateJob extends AbstractUpdateJob {

	private static final String ZORAPDVERSION = "ZORAPDVERSION"; //$NON-NLS-1$
	private final IAdaptable adaptable;
	private final boolean silent;

	public CheckForUpdateJob(IAdaptable adaptable, boolean silent) {
		super(Messages.CheckForUpdateJob_check_for_updates);
		setSystem(true);
		this.adaptable = adaptable;
		this.silent = silent;
	}

	@Override
	protected IStatus runJob(IProgressMonitor monitor) {
		try (InputStream is = new URL(System.getProperty("com.bdaum.zoom.updateLookupPage")).openStream()) { //$NON-NLS-1$
			StringBuilder sb = new StringBuilder(12000);
			byte[] buffer = new byte[4096];
			while (true) {
				int len = is.read(buffer);
				if (len <= 0)
					break;
				sb.append(new String(buffer, 0, len));
			}
			int p = sb.indexOf(ZORAPDVERSION);
			if (p >= 0) {
				StringBuilder v = new StringBuilder(16);
				for (int i = p + ZORAPDVERSION.length(); i < p + ZORAPDVERSION.length() + 30; i++) {
					char c = sb.charAt(i);
					if (Character.isDigit(c))
						v.append(c);
					else if (c == '.') {
						if (v.length() > 0)
							v.append(c);
					} else if (v.length() > 0)
						break;
				}
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				final Version publishedVersion = new Version(v.toString());
				final int result = Platform.getProduct().getDefiningBundle().getVersion().compareTo(publishedVersion);
				final Shell shell = adaptable.getAdapter(Shell.class);
				shell.getDisplay().syncExec(() -> showResult(shell, result, publishedVersion));
			}
		} catch (MalformedURLException e) {
			// should never happen
		} catch (IOException e) {
			UiActivator.getDefault().logError(Messages.CheckForUpdateJob_cannot_access, e);
		}
		return Status.OK_STATUS;
	}

	private void showResult(Shell shell, int result, Version publishedVersion) {
		if (result < 0) {
			System.setProperty("zoom.newVersion", Messages.CheckForUpdateJob_new_version_available); //$NON-NLS-1$
			if (AcousticMessageDialog.openQuestion(shell, Messages.CheckForUpdateJob_update, NLS.bind(
					Messages.CheckForUpdateJob_there_is_a_new_version, Constants.APPLICATION_NAME, publishedVersion)))
				new UpdateJob(publishedVersion, null).schedule();
		} else if (!silent)
			AcousticMessageDialog.openInformation(shell, Messages.CheckForUpdateJob_update,
					NLS.bind(Messages.CheckForUpdateJob_up_to_date, Constants.APPLICATION_NAME, publishedVersion));
	}
}
