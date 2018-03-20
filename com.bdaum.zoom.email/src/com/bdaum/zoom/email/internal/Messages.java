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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.email.internal.messages"; //$NON-NLS-1$
	public static String AbstractMailer_bad_uri;
	public static String AbstractMailer_internal_error;
	public static String AbstractMailer_io_error_creating_eml;
	public static String AbstractMailer_io_error_processing_attachment;
	public static String AbstractMailer_send_mail;
	public static String Activator_email;
	public static String Activator_error_sending_email;
	public static String CreatePDFPage_Compose_n;
	public static String CreatePDFPage_compose_one;
	public static String CreatePDFPage_create_x;
	public static String CreatePDFPage_layout;
	public static String EmailPDFWizard_cannot_create_temp_PDF_file;
	public static String EmailPDFWizard_email_n_images_as_pdf;
	public static String EmailPDFWizard_email_one_image_as_pdf;
	public static String EmailPDFWizard_nothing_selected;
	public static String EmailWizard_Email_n_images;
	public static String EmailWizard_email_one_image;
	public static String EmailWizard_No_image_selected;
	public static String IMailer_adding_attachments;
	public static String no_images_pass_privacy0;
	public static String PDFTargetFilePage_all_files;
	public static String PDFTargetFilePage_file_name_empty;
	public static String PDFTargetFilePage_no_image_selected;
	public static String PDFTargetFilePage_please_specify_target_file;
	public static String PDFTargetFilePage_portable_document_format;
	public static String PDFTargetFilePage_set_target_file;
	public static String PDFTargetFilePage_target_file;
	public static String PDFTargetFilePage_weblink;
	public static String PDFWizard_cannot_create_pdf_file;
	public static String PDFWizard_create_from_n;
	public static String PDFWizard_create_from_one;
	public static String PDFWizard_nothing_selected;
	public static String SendEmailPage_Adjust_size;
	public static String SendEmailPage_attachments;
	public static String SendEmailPage_email;
	public static String SendEmailPage_Estimated_size;
	public static String SendEmailPage_Export_only;
	public static String SendEmailPage_Image_size;
	public static String SendEmailPage_Image_size_n;
	public static String SendEmailPage_include_metadata;
	public static String SendEmailPage_Mail_size;
	public static String SendEmailPage_Matadata;
	public static String SendEmailPage_Message;
	public static String SendEmailPage_mixed;
	public static String SendEmailPage_No_image_selected;
	public static String SendEmailPage_pdf_file_n_attached;
	public static String SendEmailPage_Send_n;
	public static String SendEmailPage_Send_one;
	public static String SendEmailPage_select_quality;
	public static String SendEmailPage_send_pdf_per_email;
	public static String SendEmailPage_Send_per_email;
	public static String SendEmailPage_Subject;
	public static String SendEmailPage_title;
	public static String SendEmailPage_Track_exports;
	public static String SendEmailPage_undefined;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
