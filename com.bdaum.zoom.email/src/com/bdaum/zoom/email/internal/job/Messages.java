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

package com.bdaum.zoom.email.internal.job;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.email.internal.job.messages"; //$NON-NLS-1$
	public static String EmailJob_download_failed;
	public static String EmailJob_Email_prep_cancelled;
	public static String EmailJob_Email;
	public static String EmailJob_Email_report;
	public static String EmailJob_io_error_creating_output;
	public static String EmailJob_Preparing_email;
	public static String EmailJob_Preparing_email_mon;
	public static String HtmlJob_creating_html_page;
	public static String HtmlJob_disk_full;
	public static String HtmlJob_download_failed;
	public static String HtmlJob_error_exporting_to_ftp;
	public static String HtmlJob_export_cancelled;
	public static String HtmlJob_export_html;
	public static String HtmlJob_io_error_generating_html;
	public static String PdfJob_Create_x;
	public static String PdfJob_creating_pdf_pages;
	public static String PdfJob_download_failed;
	public static String PdfJob_internal_error_when_writing;
	public static String PdfJob_io_error_when_writing;
	public static String PdfJob_pdf_creation_cancelled;
	public static String PdfJob_pdf_report;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
