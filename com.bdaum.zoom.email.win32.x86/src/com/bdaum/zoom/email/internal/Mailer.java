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
 * (c) 2009-2011 Berthold Daum  
 */
package com.bdaum.zoom.email.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.Message;

import com.bdaum.zoom.program.BatchUtilities;

public class Mailer extends AbstractMailer {

	@Override
	protected boolean sendMailWithAttachments(String label, List<String> to,
			List<String> cc, List<String> bcc, String subject, String message,
			List<String> attachments, List<String> originalNames) {
		try {
			if (attachments != null) {
				Message msg = new Message();
				if (to != null)
					msg.setToAddrs(to);
				if (cc != null)
					msg.setCcAddrs(cc);
				if (bcc != null)
					msg.setBccAddrs(bcc);
				if (subject != null)
					msg.setSubject(subject);
				if (message != null)
					msg.setBody(message);
				msg.setAttachments(attachments);
				Desktop.mail(msg);
			}
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	@Override
	protected void sendDesktopMail(StringBuilder mailto,
			List<String> attachments) throws URISyntaxException, IOException {
		java.awt.Desktop.getDesktop().mail(new URI(mailto.toString()));
		if (attachments != null && !attachments.isEmpty())
			BatchUtilities.showInFolder(new File(attachments.get(0)), true);
	}

}
