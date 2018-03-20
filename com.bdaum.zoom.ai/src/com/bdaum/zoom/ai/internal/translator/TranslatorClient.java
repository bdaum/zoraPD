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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ai.internal.translator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.ai.internal.AiActivator;
import com.bdaum.zoom.ai.internal.preference.PreferenceConstants;
import com.bdaum.zoom.common.CommonUtilities;
import com.bdaum.zoom.core.Core;

public class TranslatorClient {

	private static final String STRING = "string"; //$NON-NLS-1$

	private static final class LocaleHandler extends DefaultHandler {
		private StringBuilder text = new StringBuilder();
		private List<Locale> resultList;

		public LocaleHandler(List<Locale> resultList) {
			this.resultList = resultList;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			text.setLength(0);
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			text.append(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (STRING.equals(qName)) {
				String lan = text.toString();
				if (!lan.startsWith("tlh")) { //$NON-NLS-1$
					Locale locale = Locale.forLanguageTag(lan);
					String displayLanguage = locale.getDisplayLanguage();
					if (!displayLanguage.equals(lan))
						resultList.add(locale);
				}
			}
		}
	}

	private static final class StringHandler extends DefaultHandler {
		private StringBuilder result;

		public StringHandler(StringBuilder sb) {
			this.result = sb;
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			result.append(ch, start, length);
		}

	}

	private static final long EIGHTMINUTES = 8 * 60 * 1000L;

	String BASEURI = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken"; //$NON-NLS-1$

	private String translatorKey;

	private String accessToken;

	private long accessTokenBirth = -1L;

	private Locale[] languages = new Locale[] { Locale.ENGLISH };

	public void setKey(String key) {
		translatorKey = key;
	}

	public String getAccessToken() {
		long currentTime = System.currentTimeMillis();
		if (accessToken == null || currentTime - accessTokenBirth > EIGHTMINUTES) {
			accessTokenBirth = currentTime;
			try {
				URL u = new URL(BASEURI);
				URLConnection c = u.openConnection();
				c.setRequestProperty("CONTENT-TYPE", "application/json"); //$NON-NLS-1$//$NON-NLS-2$
				c.setRequestProperty("Accept", "application/jwt"); //$NON-NLS-1$//$NON-NLS-2$
				c.setRequestProperty("Ocp-Apim-Subscription-Key", translatorKey); //$NON-NLS-1$
				c.setDoOutput(true);
				if (c instanceof HttpURLConnection)
					((HttpURLConnection) c).setRequestMethod("POST"); //$NON-NLS-1$
				try (OutputStreamWriter out = new OutputStreamWriter(c.getOutputStream())) {
					try (BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"))) { //$NON-NLS-1$
						accessToken = readLines(in);
					}
				} 
			} catch (UnknownHostException e) {
				AiActivator.getDefault().logError(Messages.TranslatorClient_unknown_host, e);
				accessToken = null;
			} catch (MalformedURLException e) {
				// should not happen
			} catch (ProtocolException e) {
				accessToken = null;
				AiActivator.getDefault().logError(Messages.TranslatorClient_protocol_exception, e);
			} catch (IOException e) {
				accessToken = null;
				AiActivator.getDefault().logError(Messages.TranslatorClient_io_exception_access_token, e);
			}
		}
		return accessToken;
	}

	protected String readLines(BufferedReader in) throws IOException {
		StringBuilder sb = new StringBuilder();
		String s = null;
		while ((s = in.readLine()) != null) {
			if (sb.length() > 0)
				sb.append('\n');
			sb.append(s);
		}
		return trim(sb.toString());
	}

	public Locale[] getLanguages() {
		String accessToken = getAccessToken();
		if (languages.length == 1 && accessToken != null) {
			try {
				List<Locale> lanlist = new ArrayList<>();
				try (InputStream in = new URL(
						"https://api.microsofttranslator.com/v2/http.svc/GetLanguagesForTranslate?appid=Bearer%20" //$NON-NLS-1$
								+ accessToken).openStream()) {
					SAXParserFactory.newInstance().newSAXParser().parse(in, new LocaleHandler(lanlist));
				}
				if (!lanlist.isEmpty()) {
					languages = lanlist.toArray(new Locale[lanlist.size()]);
					Arrays.sort(languages, new Comparator<Locale>() {
						@Override
						public int compare(Locale l1, Locale l2) {
							return l1.getDisplayLanguage().compareTo(l2.getDisplayLanguage());
						}
					});
				}
			} catch (UnknownHostException e) {
				AiActivator.getDefault().logError(Messages.TranslatorClient_unknown_host, e);
			} catch (MalformedURLException e) {
				// should not happen
			} catch (IOException e) {
				AiActivator.getDefault().logError(Messages.TranslatorClient_io_exception_languages, e);
			} catch (ParserConfigurationException | SAXException e) {
				AiActivator.getDefault().logError(Messages.TranslatorClient_configuration_error, e);
			}
		}
		return languages;
	}

	public String translate(String text) {
		String accessToken = getAccessToken();
		if (accessToken != null) {
			String lan = AiActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.LANGUAGE);
			if (!Locale.ENGLISH.getLanguage().equals(lan)) {
				String segment = Core.encodeUrlSegment(text);
				segment = CommonUtilities.encodeBlanks(segment);
				StringBuilder sb = new StringBuilder();
				sb.append("https://api.microsofttranslator.com/v2/http.svc/Translate?appid=Bearer%20") //$NON-NLS-1$
						.append(accessToken).append("&text=").append(segment).append("&from=en&to=").append(lan) //$NON-NLS-1$ //$NON-NLS-2$
						.append("&contentType=text%2Fplain"); //$NON-NLS-1$
				try (InputStream in = new URL(sb.toString()).openStream()) {
					sb.setLength(0);
					SAXParserFactory.newInstance().newSAXParser().parse(in, new StringHandler(sb));
					return sb.toString();
				} catch (UnknownHostException e) {
					AiActivator.getDefault().logError(Messages.TranslatorClient_unknown_host, e);
				} catch (MalformedURLException e) {
					// should never happen
				} catch (IOException e) {
					AiActivator.getDefault().logError(Messages.TranslatorClient_io_exception_translating, e);
				} catch (ParserConfigurationException | SAXException e) {
					AiActivator.getDefault().logError(Messages.TranslatorClient_configuration_error, e);
				}
			}
		}
		return text;
	}

	public String trim(String s) {
		int len = s.length();
		int st = 0;
		char[] val = s.toCharArray();
		while ((st < len) && (val[st] == '\ufeff' || Character.isWhitespace(val[st])))
			st++;
		while ((st < len) && Character.isWhitespace(val[st]))
			len--;
		return ((st > 0) || (len < s.length())) ? s.substring(st, len) : s;
	}

}
