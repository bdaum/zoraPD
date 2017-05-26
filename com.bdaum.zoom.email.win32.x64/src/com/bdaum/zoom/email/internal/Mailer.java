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
 * (c) 2011-2012 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.email.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.bdaum.zoom.program.BatchUtilities;

public class Mailer extends AbstractMailer {

	@Override
	protected boolean sendMailWithAttachments(String label, List<String> to,
			List<String> cc, List<String> bcc, String subject, String message,
			List<String> attachments, List<String> originalNames) {
		return false;
	}

	@Override
	protected void sendDesktopMail(StringBuilder mailto,
			List<String> attachments) throws URISyntaxException, IOException {
		java.awt.Desktop.getDesktop().mail(new URI(mailto.toString()));
		if (attachments != null && !attachments.isEmpty())
			BatchUtilities.showInFolder(new File(attachments.get(0)));
	}

}
