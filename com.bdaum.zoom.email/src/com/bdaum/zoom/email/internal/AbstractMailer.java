package com.bdaum.zoom.email.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.program.Program;

import com.bdaum.zoom.core.Core;
import com.itextpdf.text.pdf.codec.Base64;

public abstract class AbstractMailer implements IMailer {

	private String ADD_ATTACHMENTS_MANUALLY = Messages.IMailer_adding_attachments;

	public IStatus sendMail(String label, List<String> to, List<String> cc, List<String> bcc, String subject,
			String message, List<String> attachments, List<String> originalNames) throws Exception {
		MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 0, Messages.AbstractMailer_send_mail, null);
		if (attachments == null || attachments.isEmpty())
			sendMailManually(label, to, cc, bcc, subject, message, null, status);
		else {
			List<String> paths = new ArrayList<String>(attachments.size());
			for (String uri : attachments) {
				try {
					File file = new File(new URI(uri));
					paths.add(file.getAbsolutePath());
				} catch (URISyntaxException e) {
					// should never happen
				}
			}
			if (!sendMailWithAttachments(label, to, cc, bcc, subject, message, paths, originalNames)) {
				if (!exportToEml(label, to, cc, bcc, subject, message, attachments, originalNames, status))
					sendMailManually(label, to, cc, bcc, subject, message, paths, status);
			}
		}
		return status;
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
				for (String attachment : attachments) {
					try {
						File file = new File(new URI(attachment));
						String filename = originalNames != null && originalNames.size() > i ? originalNames.get(i)
								: file.getName();
						writer.write("\n--" + boundary + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						writer.write(NLS.bind("Content-Type: {1}; name=\"{0}\"\n", //$NON-NLS-1$
								filename, filename.endsWith(".pdf") ? "application/pdf" : "image/jpeg"));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
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
				}
				writer.write("\n--" + boundary + "--\n"); //$NON-NLS-1$//$NON-NLS-2$
			}
			if (!Program.launch(eml.getAbsolutePath())) {
				eml.delete();
				return false;
			}
			return true;
		} catch (IOException e) {
			addError(status, Messages.AbstractMailer_io_error_creating_eml, e);
			return false;
		}
	}

	private void sendMailManually(String label, List<String> to, List<String> cc, List<String> bcc, String subject,
			String message, List<String> attachments, MultiStatus status) {
		StringBuilder mailto = new StringBuilder("mailto:"); //$NON-NLS-1$
		appendUriQuerySegment("to", to, mailto); //$NON-NLS-1$
		appendUriQuerySegment("cc", cc, mailto); //$NON-NLS-1$
		appendUriQuerySegment("bcc", bcc, mailto); //$NON-NLS-1$
		appendUriQuerySegment("subject", subject, mailto); //$NON-NLS-1$
		String body = message;
		if (attachments != null) {
			body = body == null ? ADD_ATTACHMENTS_MANUALLY + "\n    " : body //$NON-NLS-1$
					+ "\n\n" + ADD_ATTACHMENTS_MANUALLY + "\n    "; //$NON-NLS-1$ //$NON-NLS-2$
			body += Core.toStringList(attachments.toArray(), "\n    "); //$NON-NLS-1$
		}
		appendUriQuerySegment("body", body, mailto); //$NON-NLS-1$
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

	// {
	// java.awt.Desktop.getDesktop().mail(new URI(mailto.toString()));
	// if (attachments != null && !attachments.isEmpty())
	// BatchUtilities.showInFolder(new File(attachments.get(0)));
	// }

	protected abstract boolean sendMailWithAttachments(String label, List<String> to, List<String> cc, List<String> bcc,
			String subject, String message, List<String> attachments, List<String> originalNames);

	private void appendUriQuerySegment(String key, List<String> list, StringBuilder mailto) {
		if (list != null)
			mailto.append(key).append('=').append(Core.encodeUrlSegment(Core.toStringList(list.toArray(), ", "))); //$NON-NLS-1$
	}

	private void appendUriQuerySegment(String key, String s, StringBuilder mailto) {
		if (s != null) {
			if (mailto.indexOf("?") >= 0) //$NON-NLS-1$
				mailto.append('&');
			else
				mailto.append('?');
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
					if (len > 72) {
						while (!token.isEmpty()) {
							int q = Math.min(token.length(), 72 - cnt);
							sb.append(token.substring(0, q));
							token = token.substring(q);
							if (token.isEmpty())
								cnt += q;
							else {
								sb.append('\n');
								cnt = 0;
							}
						}
					} else {
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
