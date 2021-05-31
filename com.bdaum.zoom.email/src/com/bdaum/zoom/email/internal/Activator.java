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
 * (c) 2009-2021 Berthold Daum  
 */
package com.bdaum.zoom.email.internal;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.core.IEmailService;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.internal.ZUiPlugin;

@SuppressWarnings("restriction")
public class Activator extends ZUiPlugin implements IEmailService {

	public static final String PLUGIN_ID = "com.bdaum.zoom.email"; //$NON-NLS-1$

	private static Activator plugin;

	private File tempFolder;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public void logError(String message, Exception e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	public IStatus sendMail(List<String> to, List<String> cc, List<String> bcc, String subject, String message,
			List<String> attachments) {
		return sendMail(to, cc, bcc, subject, message, attachments, null);
	}

	public IStatus sendMail(List<String> to, List<String> cc, List<String> bcc, String subject, String message,
			List<String> attachments, List<String> originalNames) {
		try {
			IMailer mailer = (IMailer) ((Class<?>) Class.forName("com.bdaum.zoom.email.internal.Mailer")) //$NON-NLS-1$
					.newInstance();
			IPreferenceStore store = getPreferenceStore();
			String vcard = store.getBoolean(PreferenceConstants.PLATFORMCLIENT) ? null
					: store.getString(PreferenceConstants.VCARD);
			return mailer.sendMail(Messages.Activator_email, to, cc, bcc, subject, message, attachments, originalNames,
					vcard);
		} catch (Exception e1) {
			return new Status(IStatus.ERROR, PLUGIN_ID, Messages.Activator_error_sending_email, e1);
		}
	}

	public File createTempFile(String name, String suffix) throws IOException {
		File tempFile = File.createTempFile("Tmp", suffix, getTempFolder()); //$NON-NLS-1$
		int p = name.lastIndexOf('.');
		if (p >= 0)
			name = name.substring(0, p);
		File renamedFile = new File(tempFile.getParent(), name + suffix);
		if (tempFile.renameTo(renamedFile))
			tempFile = renamedFile;
		tempFile.deleteOnExit();
		return tempFile;
	}

	private File getTempFolder() {
		if (tempFolder == null || !tempFolder.exists()) {
			tempFolder = new File(new StringBuilder().append(System.getProperty("java.io.tmpdir/")) //$NON-NLS-1$
					.append(ImageConstants.APPNAME).append("_mailAttachments").toString()); //$NON-NLS-1$
			tempFolder.mkdirs();
			BatchUtilities.deleteFolderContent(tempFolder);
			tempFolder.deleteOnExit();
		}
		return tempFolder;
	}

}
