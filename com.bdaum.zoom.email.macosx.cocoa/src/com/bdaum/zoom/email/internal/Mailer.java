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
package com.bdaum.zoom.email.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.program.BatchUtilities;

@SuppressWarnings("restriction")
public class Mailer extends AbstractMailer {
	private static final String TOVAR = "$to$"; //$NON-NLS-1$
	private static final String CCVAR = "$cc$"; //$NON-NLS-1$
	private static final String BCCVAR = "$bcc$"; //$NON-NLS-1$
	private static final String SUBJECTVAR = "$subj$"; //$NON-NLS-1$
	private static final String BODYVAR = "$body$"; //$NON-NLS-1$
	private static final String ATTACHVAR = "$attach$"; //$NON-NLS-1$


	@Override
	protected boolean sendMailWithAttachments(String label, List<String> to,
			List<String> cc, List<String> bcc, String subject, String message,
			List<String> attachments, List<String> originalNames) {
		/* Create script from template */
		try {
			URL url = FileLocator.find(Activator.getDefault().getBundle(),
					new Path("scripts/mail.txt"), null); //$NON-NLS-1$
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					url.openStream()));
			StringBuilder sb = new StringBuilder();
			try {
				while (true) {
					String line = reader.readLine();
					if (line == null)
						break;
					if (sb.length() > 0)
						sb.append('\n');
					sb.append(line);
				}
			} finally {
				reader.close();
			}
			replaceText(sb, TOVAR, Core.toStringList(quote(to), ','));
			replaceText(sb, CCVAR, Core.toStringList(quote(cc), ','));
			replaceText(sb, BCCVAR, Core.toStringList(quote(bcc), ','));
			replaceText(sb, SUBJECTVAR, subject);
			replaceText(sb, BODYVAR, message);
			replaceText(sb, ATTACHVAR, Core.toStringList(quote(attachments), ','));

			String[] parms = new String[] { "osascript", "-e", sb.toString() }; //$NON-NLS-1$ //$NON-NLS-2$
			BatchActivator.executeCommand(parms, null, label, IStatus.OK,
					IStatus.WARNING, 3000L, "UTF-8"); //$NON-NLS-1$
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	private static void replaceText(StringBuilder sb, String var, String text) {
		int p = sb.indexOf(var);
		if (p >= 0) {
			if (text == null || text.length() == 0)
				sb.delete(p, p + var.length());
			else
				sb.replace(p, p + var.length(), text);
		}
	}

	private static Collection<?> quote(List<String> list) {
		if (list != null) {
			List<String> result = new ArrayList<String>(list.size());
			for (String string : list)
				result.add('"' + string + '"');
			return result;
		}
		return null;
	}

	@Override
	protected void sendDesktopMail(StringBuilder mailto,
			List<String> attachments) throws URISyntaxException, IOException {
		java.awt.Desktop.getDesktop().mail(new URI(mailto.toString()));
		if (attachments != null && !attachments.isEmpty())
			BatchUtilities.showInFolder(new File(attachments.get(0)));
	}


}
