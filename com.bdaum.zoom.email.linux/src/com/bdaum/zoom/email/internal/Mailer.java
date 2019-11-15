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
package com.bdaum.zoom.email.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.program.BatchUtilities;

@SuppressWarnings("restriction")
public class Mailer extends AbstractMailer {

	@Override
	protected boolean sendMailWithAttachments(String label, List<String> to,
			List<String> cc, List<String> bcc, String subject, String message,
			List<String> attachments, List<String> originalNames) {
		StringBuilder mailto = new StringBuilder();
		mailto.append("--utf8"); //$NON-NLS-1$
		addXdgParameter(mailto, "cc", Core.toStringList(quote(cc), ' ')); //$NON-NLS-1$
		addXdgParameter(mailto, "bcc", Core.toStringList(quote(bcc), ' ')); //$NON-NLS-1$
		addXdgParameter(mailto, "subject", //$NON-NLS-1$
				quote(subject));
		addXdgParameter(mailto, "body", //$NON-NLS-1$
				quote(message));
		if (attachments != null)
			for (String att : attachments)
				mailto.append(" --attach ").append(att); //$NON-NLS-1$
		if (to != null)
			mailto.append(Core.toStringList(quote(to), ' '));
		String[] parms = new String[] { "xdg-email", mailto.toString() }; //$NON-NLS-1$
		try {
			BatchActivator.executeCommand(parms, null, label, IStatus.OK,
					IStatus.WARNING, 3000L, "UTF-8"); //$NON-NLS-1$
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	private static String quote(String s) {
		if (s != null && !s.isEmpty()) {
			StringTokenizer st = new StringTokenizer(s, "'"); //$NON-NLS-1$
			StringBuilder sb = new StringBuilder();
			if (s.startsWith("'")) //$NON-NLS-1$
				sb.append("\"'\""); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				sb.append('\'').append(st.nextToken()).append('\'');
				if (st.hasMoreTokens())
					sb.append("\"'\""); //$NON-NLS-1$
			}
			if (s.endsWith("'")) //$NON-NLS-1$
				sb.append("\"'\""); //$NON-NLS-1$
			return sb.toString();
		}
		return null;
	}

	private static void addXdgParameter(StringBuilder mailto, String parm, String value) {
		if (value != null && !value.trim().isEmpty())
			mailto.append(" --").append(parm).append(' ').append(value.trim()); //$NON-NLS-1$
	}

	private static Collection<?> quote(List<String> list) {
		if (list != null) {
			List<String> result = new ArrayList<String>(list.size());
			for (String string : list)
				result.add('\'' + string + '\'');
			return result;
		}
		return null;
	}

	@Override
	protected void sendDesktopMail(StringBuilder mailto,
			List<String> attachments) throws URISyntaxException, IOException {
		java.awt.Desktop.getDesktop().mail(new URI(mailto.toString()));
		if (attachments != null && !attachments.isEmpty())
			BatchUtilities.showInFolder(new File(attachments.get(0)), true);
	}

}
