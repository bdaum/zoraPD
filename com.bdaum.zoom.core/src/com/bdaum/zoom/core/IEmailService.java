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
package com.bdaum.zoom.core;

import java.util.List;

import org.eclipse.core.runtime.IStatus;

public interface IEmailService {
	/**
	 * Sends an Email via the hosts Email client
	 * @param to - receivers of originals
	 * @param cc - receivers of copies
	 * @param bcc - receivers of blind copies
	 * @param subject - subject 
	 * @param message - message body
	 * @param attachments - attachments
	 * @return resulting status of operation
	 */
	IStatus sendMail(List<String> to, List<String> cc, List<String> bcc,
			String subject, String message, List<String> attachments);
}
