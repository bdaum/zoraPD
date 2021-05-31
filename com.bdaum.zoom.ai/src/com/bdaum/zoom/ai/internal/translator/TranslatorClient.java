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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.ai.internal.AiActivator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TranslatorClient {

	private static final String TRANSURI = "{0}/translate?api-version=3.0&to={1}"; //$NON-NLS-1$

	private static final String APPLICATION_JSON = "application/json"; //$NON-NLS-1$

	private static final String CONTENT_TYPE = "Content-type"; //$NON-NLS-1$

	private static final String LANGURI = "{0}/languages?api-version=3.0"; //$NON-NLS-1$

	private String translatorKey;

	private Locale[] languages = new Locale[] { Locale.ENGLISH };

	private String endpoint;

	private String language;

	private OkHttpClient okhttpClient = new OkHttpClient();

	public Locale[] getLanguages() {
		try {
			if (endpoint == null || endpoint.isEmpty())
				throw new IllegalArgumentException(Messages.TranslatorClient_no_endpoint);
			List<Locale> lanlist = new ArrayList<>();
			Request request = new Request.Builder().url(NLS.bind(LANGURI, endpoint)).get()
					.addHeader(CONTENT_TYPE, APPLICATION_JSON).build();
			Response response = okhttpClient.newCall(request).execute();
			JsonElement element = JsonParser.parseString(response.body().string());
			if (element != null && element.isJsonObject()) {
				JsonElement translation = element.getAsJsonObject().get("translation"); //$NON-NLS-1$
				if (translation != null && translation.isJsonObject()) {
					Set<Entry<String, JsonElement>> entrySet = translation.getAsJsonObject().entrySet();
					if (entrySet != null)
						for (Entry<String, JsonElement> entry : entrySet)
							lanlist.add(new Locale(entry.getKey()));
				}
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
		} catch (IOException | IllegalArgumentException e) {
			AiActivator.getDefault().logError(Messages.TranslatorClient_io_exception_languages, e);
		}
		return languages;
	}

	public String translate(String text) {
		try {
			if (endpoint == null || endpoint.isEmpty())
				throw new IllegalArgumentException(Messages.TranslatorClient_no_endpoint);
			if (translatorKey == null || translatorKey.isEmpty())
				throw new IllegalArgumentException(Messages.TranslatorClient_no_key);
			if (language == null || language.isEmpty())
				language = Locale.getDefault().getLanguage();
			RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON),
					new StringBuilder().append("[{\"Text\": \"").append(text).append("\"}]\n").toString()); //$NON-NLS-1$//$NON-NLS-2$
			Request request = new Request.Builder().url(NLS.bind(TRANSURI, endpoint, language)).post(body)
					.addHeader("Ocp-Apim-Subscription-Key", translatorKey).addHeader(CONTENT_TYPE, APPLICATION_JSON) //$NON-NLS-1$
					.build();
			Response response = okhttpClient.newCall(request).execute();
			JsonElement element = JsonParser.parseString(response.body().string());
			if (element != null && element.isJsonArray()) {
				JsonArray jsonArray = element.getAsJsonArray();
				if (jsonArray.size() > 0) {
					JsonElement el = jsonArray.get(0);
					if (element != null && el.isJsonObject()) {
						JsonElement translations = el.getAsJsonObject().get("translations"); //$NON-NLS-1$
						if (translations != null && translations.isJsonArray()) {
							JsonArray array = translations.getAsJsonArray();
							if (array.size() > 0) {
								JsonElement jsonElement = array.get(0);
								if (jsonElement != null && jsonElement.isJsonObject()) {
									JsonElement textElement = jsonElement.getAsJsonObject().get("text"); //$NON-NLS-1$
									if (textElement != null && textElement.isJsonPrimitive())
										return textElement.getAsString();
								}
							}
						}
					}
				}
			}
		} catch (IOException | IllegalArgumentException e) {
			AiActivator.getDefault().logError(Messages.TranslatorClient_io_exception, e);
		}
		return null;
	}

	public TranslatorClient withEndpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}

	public TranslatorClient withLanguage(String language) {
		this.language = language;
		return this;
	}

	public TranslatorClient withKey(String key) {
		translatorKey = key;
		return this;
	}

}
