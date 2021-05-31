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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.program.Program;

import com.bdaum.zoom.common.CommonUtilities;
import com.bdaum.zoom.core.Core;
import com.itextpdf.text.pdf.codec.Base64;

public abstract class AbstractMailer implements IMailer {

	private static final String ADD_ATTACHMENTS_MANUALLY = Messages.IMailer_adding_attachments;

	public Session getSession(Properties properties) {
		Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(properties.getProperty("mail.smtp.user"), //$NON-NLS-1$
						properties.getProperty("mail.smtp.password")); //$NON-NLS-1$
			}
		});
		return session;
	}

	private Properties getProperties() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp"); //$NON-NLS-1$//$NON-NLS-2$
		props.put("mail.smtp.socketFactory", "javax.net.ssl.SSLSocketFactory"); //$NON-NLS-1$//$NON-NLS-2$
		props.put("mail.smtp.socketFactory.fallback", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		String port = String.valueOf(preferenceStore.getInt(PreferenceConstants.PORT));
		props.put("mail.smtp.port", port); //$NON-NLS-1$
		props.put("mail.smtp.socketFactory.port", port); //$NON-NLS-1$
		props.put("mail.smtp.ssl.enable", String //$NON-NLS-1$
				.valueOf(PreferenceConstants.SSL.equals(preferenceStore.getString(PreferenceConstants.SECURITY))));
		props.put("mail.smtp.starttls.enable", String //$NON-NLS-1$
				.valueOf(PreferenceConstants.STARTTLS.equals(preferenceStore.getString(PreferenceConstants.SECURITY))));
		props.put("mail.smtp.host", preferenceStore.getString(PreferenceConstants.HOSTURL)); //$NON-NLS-1$
		props.put("mail.smtp.auth", "true"); //$NON-NLS-1$//$NON-NLS-2$
		props.put("mail.smtp.user", preferenceStore.getString(PreferenceConstants.USER)); //$NON-NLS-1$
		props.put("mail.smtp.password", CommonUtilities.decode(preferenceStore.getString(PreferenceConstants.PASSWORD))); //$NON-NLS-1$
		props.put("mail.smtp.from", preferenceStore.getString(PreferenceConstants.SENDER)); //$NON-NLS-1$
		return props;
	}

	public IStatus sendMail(String label, List<String> to, List<String> cc, List<String> bcc, String subject,
			String message, List<String> attachments, List<String> originalNames, String vcard) throws Exception {
		MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 0, Messages.AbstractMailer_send_mail, null);
		if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.PLATFORMCLIENT)) {
			if (attachments == null || attachments.isEmpty())
				sendMailManually(label, to, cc, bcc, subject, message, null, status);
			else {
				List<String> paths = new ArrayList<String>(attachments.size());
				for (String uri : attachments)
					try {
						paths.add(new File(new URI(uri)).getAbsolutePath());
					} catch (URISyntaxException e) {
						// should never happen
					}
				if (!sendMailWithAttachments(label, to, cc, bcc, subject, message, paths, originalNames)
						&& !exportToEml(label, to, cc, bcc, subject, message, attachments, originalNames, status))
					sendMailManually(label, to, cc, bcc, subject, message, paths, status);
			}
		} else {
			Properties properties = getProperties();
			postMail(getSession(properties), properties, to, cc, bcc, subject, message, attachments, originalNames,
					vcard, status);
		}
		return status;
	}

	public static void postMail(Session session, Properties properties, List<String> to, List<String> cc,
			List<String> bcc, String subject, String message, List<String> attachments, List<String> originalNames,
			String vcard, MultiStatus status) {
		Message msg = new MimeMessage(session);
		InternetAddress[] addressTo = new InternetAddress[to.size()];
		try {
			for (int i = 0; i < addressTo.length; i++)
				addressTo[i] = new InternetAddress(to.get(i));
			msg.setRecipients(Message.RecipientType.TO, addressTo);
			InternetAddress[] addressCc = new InternetAddress[cc.size()];
			for (int i = 0; i < addressCc.length; i++)
				addressCc[i] = new InternetAddress(cc.get(i));
			msg.setRecipients(Message.RecipientType.CC, addressTo);
			InternetAddress[] addressBcc = new InternetAddress[bcc.size()];
			for (int i = 0; i < addressBcc.length; i++)
				addressBcc[i] = new InternetAddress(bcc.get(i));
			msg.setRecipients(Message.RecipientType.BCC, addressTo);
			String from = properties.getProperty("mail.smtp.from"); //$NON-NLS-1$
			if (from != null && !from.isEmpty())
				msg.setFrom(new InternetAddress(from));
			msg.setSubject(subject);
			msg.setSentDate(new Date());
			if ((attachments == null || attachments.isEmpty()) && (vcard == null || vcard.isEmpty()))
				msg.setContent(message, "text/plain; charset=ISO-8859-1"); //$NON-NLS-1$
			else {
				Multipart multipart = new MimeMultipart();
				BodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setContent(message, "text/plain; charset=ISO-8859-1"); //$NON-NLS-1$
				multipart.addBodyPart(messageBodyPart);
				int i = 0;
				if (attachments != null)
					for (String attachment : attachments) {
						MimeBodyPart attachmentPart = new MimeBodyPart();
						try {
							URI uri = new URI(attachment);
							File file = new File(uri);
							String filename = originalNames != null && originalNames.size() > i ? originalNames.get(i)
									: file.getName();
							attachmentPart.attachFile(file);
							attachmentPart.setFileName(filename);
							multipart.addBodyPart(attachmentPart);
						} catch (IOException e) {
							status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									NLS.bind(Messages.AbstractMailer_io_error, attachment), e));
						} catch (URISyntaxException e) {
							status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									NLS.bind(Messages.AbstractMailer_bad_uri_for_mail, attachment), e));
						}
						++i;
					}
				if (vcard != null && !vcard.isEmpty()) {
					MimeBodyPart attachmentPart = new MimeBodyPart();
					try {
						attachmentPart.attachFile(new File(vcard));
						multipart.addBodyPart(attachmentPart);
					} catch (IOException e) {
						status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								Messages.AbstractMailer_io_error_vcard, e));
					}
				}
				msg.setContent(multipart);
			}
			Transport.send(msg, properties.getProperty("mail.smtp.user"), properties.getProperty("mail.smtp.password")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (AddressException e) {
			status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.AbstractMailer_address_error, e));
		} catch (MessagingException e) {
			status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.AbstractMailer_messaging_error, e));
		}
	}

	private boolean exportToEml(String label, List<String> to, List<String> cc, List<String> bcc, String subject,
			String message, List<String> attachments, List<String> originalNames, MultiStatus status) {
		try {
			File eml = Core.createTempFile("Emails", "eml"); //$NON-NLS-1$ //$NON-NLS-2$
			try (Writer writer = new BufferedWriter(new FileWriter(eml))) {
				writer.write("MIME-Version: 1.0\n"); //$NON-NLS-1$
				writeRecipients(writer, "To", to); //$NON-NLS-1$
				writeRecipients(writer, "Cc", cc); //$NON-NLS-1$
				writeRecipients(writer, "Bcc", bcc); //$NON-NLS-1$
				if (subject != null && !subject.isEmpty())
					writer.write("Subject: " + encodeAscii(subject) + '\n'); //$NON-NLS-1$
				String boundary = UUID.randomUUID().toString();
				writer.write(NLS.bind("Content-Type: multipart/mixed; boundary=\"{0}\"\n", boundary)); //$NON-NLS-1$
				writer.write("\n--" + boundary + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				writer.write("Content-Type: text/plain; charset=ISO-8859-1\n"); //$NON-NLS-1$
				writer.write("Content-Transfer-Encoding: quoted-printable\n\n"); //$NON-NLS-1$
				if (message != null)
					writer.write(wrap(message));
				int i = 0;
				for (String attachment : attachments)
					try {
						File file = new File(new URI(attachment));
						String filename = originalNames != null && originalNames.size() > i ? originalNames.get(i)
								: file.getName();
						writer.write("\n--" + boundary + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						writer.write(NLS.bind("Content-Type: {1}; name=\"{0}\"\n", //$NON-NLS-1$
								filename, filename.endsWith(".pdf") ? "application/pdf" : "image/jpeg")); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
						writer.write(NLS.bind("Content-Disposition: attachment; filename=\"{0}\"\n", //$NON-NLS-1$
								filename));
						writer.write("Content-Transfer-Encoding: base64\n"); //$NON-NLS-1$
						String attach = Base64.encodeFromFile(file.getAbsolutePath());
						if (attach != null) {
							writer.write(NLS.bind("X-Attachment-Id: f_{0}\n\n", //$NON-NLS-1$
									Core.getFileName(file.toURI().toString(), false)));
							writer.write(attach);
							writer.write("\n"); //$NON-NLS-1$
						}
					} catch (IOException e) {
						addError(status, NLS.bind(Messages.AbstractMailer_io_error_processing_attachment, attachment),
								e);
					} catch (URISyntaxException e) {
						addError(status, NLS.bind(Messages.AbstractMailer_bad_uri, attachment), e);
					}
				writer.write("\n--" + boundary + "--\n"); //$NON-NLS-1$//$NON-NLS-2$
			}
			if (Program.launch(eml.getAbsolutePath()))
				return true;
			eml.delete();
		} catch (IOException e) {
			addError(status, Messages.AbstractMailer_io_error_creating_eml, e);
		}
		return false;
	}

	private void sendMailManually(String label, List<String> to, List<String> cc, List<String> bcc, String subject,
			String message, List<String> attachments, MultiStatus status) {
		StringBuilder mailto = new StringBuilder("mailto:"); //$NON-NLS-1$
		appendUriQuerySegment("to", to, mailto); //$NON-NLS-1$
		appendUriQuerySegment("cc", cc, mailto); //$NON-NLS-1$
		appendUriQuerySegment("bcc", bcc, mailto); //$NON-NLS-1$
		appendUriQuerySegment("subject", subject, mailto); //$NON-NLS-1$
		StringBuilder body = new StringBuilder(200);
		if (message != null)
			body.append(message);
		if (attachments != null) {
			if (message != null)
				body.append("\n\n"); //$NON-NLS-1$
			body.append(ADD_ATTACHMENTS_MANUALLY).append("\n    ") //$NON-NLS-1$
					.append(Core.toStringList(attachments.toArray(), "\n    ")); //$NON-NLS-1$
		}
		appendUriQuerySegment("body", body.toString(), mailto); //$NON-NLS-1$
		try {
			sendDesktopMail(mailto, attachments);
		} catch (Exception e1) {
			addError(status, label + Messages.AbstractMailer_internal_error, e1);
		}
	}

	private void addError(MultiStatus status, String message, Throwable e) {
		status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, e));
	}

	protected abstract void sendDesktopMail(StringBuilder mailto, List<String> attachments)
			throws URISyntaxException, IOException;

	protected abstract boolean sendMailWithAttachments(String label, List<String> to, List<String> cc, List<String> bcc,
			String subject, String message, List<String> attachments, List<String> originalNames);

	private void appendUriQuerySegment(String key, List<String> list, StringBuilder mailto) {
		if (list != null)
			mailto.append(key).append('=').append(Core.encodeUrlSegment(Core.toStringList(list.toArray(), ", "))); //$NON-NLS-1$
	}

	private void appendUriQuerySegment(String key, String s, StringBuilder mailto) {
		if (s != null) {
			mailto.append(mailto.indexOf("?") >= 0 ? '&' : '?'); //$NON-NLS-1$
			mailto.append(key).append('=').append(Core.encodeUrlSegment(s));
		}
	}

	private String wrap(String message) {
		int cnt = 0;
		int space = 0;
		StringBuilder sb = new StringBuilder();
		StringTokenizer st = new StringTokenizer(message, "\n\r \t", true); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if ("\n".equals(token) || "\r".equals(token)) { //$NON-NLS-1$ //$NON-NLS-2$
				sb.append(token);
				cnt = 0;
				space = 0;
			} else if (" ".equals(token)) //$NON-NLS-1$
				space += 1;
			else if ("\t".equals(token)) //$NON-NLS-1$
				space = (space + 4) / 4 * 4;
			else {
				while (space > 0) {
					if (cnt >= 72) {
						sb.append('\n');
						cnt = 0;
					}
					sb.append(' ');
					++cnt;
					--space;
				}
				int len = token.length();
				if (cnt + len > 72) {
					if (len > 72)
						while (!token.isEmpty()) {
							int q = Math.min(token.length(), 72 - cnt);
							sb.append(token.substring(0, q));
							token = token.substring(q);
							if (token.isEmpty()) {
								cnt += q;
								continue;
							}
							sb.append('\n');
							cnt = 0;
						}
					else {
						sb.append('\n').append(token);
						cnt = len;
					}
				} else {
					sb.append(token);
					cnt += len;
				}
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	private void writeRecipients(Writer writer, String key, List<String> recipients) throws IOException {
		if (recipients != null && !recipients.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append(key).append(": "); //$NON-NLS-1$
			for (String recipient : recipients) {
				if (sb.length() > key.length() + 2)
					sb.append(',');
				sb.append(encodeAscii(recipient));
			}
			sb.append('\n');
			writer.write(sb.toString());
		}
	}

	private static final String hexChars = "0123456789ABCDEF"; //$NON-NLS-1$

	private String encodeAscii(String text) {
		StringBuilder sb = new StringBuilder();
		boolean quoted = false;
		for (int i = 0; i < text.length(); i++)
			if (text.charAt(i) > 127) {
				quoted = true;
				break;
			}
		if (quoted) {
			sb.append("=?ISO-8859-1?Q?"); //$NON-NLS-1$
			for (int i = 0; i < text.length(); i++) {
				int c = text.charAt(i);
				if (c > 127) {
					sb.append('=');
					if (c > 255)
						sb.append(hexChars.charAt((c & 0xffff) >> 12)).append(hexChars.charAt((c & 0x0fff) >> 8));
					sb.append(hexChars.charAt((c & 0x00ff) >> 4)).append(hexChars.charAt(c & 0x000f));
				} else
					sb.append((char) c);
			}
			sb.append("?="); //$NON-NLS-1$
		} else
			sb.append(text);
		return sb.toString();
	}

}
