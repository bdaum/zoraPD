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
 * (c) 2016-2021 Berthold Daum  
 */
package com.bdaum.zoom.ai.msvision.internal.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Rectangle;

import com.bdaum.zoom.ai.internal.AbstractAiServiceProvider;
import com.bdaum.zoom.ai.internal.AiActivator;
import com.bdaum.zoom.ai.internal.TokenComparator;
import com.bdaum.zoom.ai.internal.translator.TranslatorClient;
import com.bdaum.zoom.ai.msvision.internal.MsVisionActivator;
import com.bdaum.zoom.ai.msvision.internal.preference.PreferenceConstants;
import com.bdaum.zoom.core.internal.ai.Prediction;
import com.bdaum.zoom.core.internal.ai.Prediction.Token;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVision;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.AdultInfo;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.AnalyzeImageInStreamOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.Category;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ComputerVisionErrorResponseException;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.Details;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.FaceDescription;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.FaceRectangle;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageAnalysis;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageCaption;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageDescriptionDetails;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageTag;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.VisualFeatureTypes;

public class MsVisionServiceProvider extends AbstractAiServiceProvider {

	private static final String SERVICENAME = "Microsoft Computer Vision API"; //$NON-NLS-1$

	public Prediction predict(byte[] jpeg) {
		Prediction prediction = null;
		MsVisionActivator activator = MsVisionActivator.getDefault();
		ComputerVisionClient client = activator.getClient();
		try {
			ComputerVision computerVision = client.computerVision();
			AnalyzeImageInStreamOptionalParameter parameter = new AnalyzeImageInStreamOptionalParameter();
			List<VisualFeatureTypes> visualFeatures = new ArrayList<VisualFeatureTypes>();
			visualFeatures.add(VisualFeatureTypes.CATEGORIES);
			visualFeatures.add(VisualFeatureTypes.TAGS);
			if (generateDescription())
				visualFeatures.add(VisualFeatureTypes.DESCRIPTION);
			if (checkFaces())
				visualFeatures.add(VisualFeatureTypes.FACES);
			if (checkAdultContent())
				visualFeatures.add(VisualFeatureTypes.ADULT);
			parameter.withVisualFeatures(visualFeatures);
			if (checkCelebrities()) {
				List<Details> details = new ArrayList<Details>();
				details.add(Details.CELEBRITIES);
				details.add(Details.LANDMARKS);
				parameter.withDetails(details);
			}
			ImageAnalysis analysis = computerVision.analyzeImageInStream(jpeg, parameter);
			IPreferenceStore preferenceStore = activator.getPreferenceStore();
			int maxConcepts = preferenceStore.getInt(PreferenceConstants.MAXCONCEPTS);
			float minConfidence = preferenceStore.getInt(PreferenceConstants.MINCONFIDENCE) * 0.01f;
			List<Token> cats = new ArrayList<>();
			List<Category> categories = analysis.categories();
			if (categories != null)
				for (Category category : categories)
					cats.add(new Token(CategoryMessages.getString(category.name()), (float) category.score()));
			if (cats.size() >= maxConcepts)
				cats = cats.subList(0, maxConcepts);
			Collections.sort(cats, TokenComparator.INSTANCE);
			List<Token> keywords = new ArrayList<>();
			List<ImageTag> tags = analysis.tags();
			if (tags != null)
				for (ImageTag tag : tags) {
					double score = tag.confidence();
					if (score >= minConfidence)
						keywords.add(new Token(tag.name(), (float) score));
				}
			if (keywords.size() >= maxConcepts)
				keywords = keywords.subList(0, maxConcepts);
			Collections.sort(keywords, TokenComparator.INSTANCE);
			boolean trCats = preferenceStore.getBoolean(PreferenceConstants.TRANSLATE_CATEGORIES) && !cats.isEmpty();
			boolean trTags = preferenceStore.getBoolean(PreferenceConstants.TRANSLATE_TAGS) && !keywords.isEmpty();
			if (trCats || trTags) {
				TranslatorClient translatorClient = AiActivator.getDefault().getClient();
				if (translatorClient != null) {
					StringBuilder sb = new StringBuilder();
					if (trCats) {
						for (Token tok : cats) {
							if (sb.length() > 0)
								sb.append(", "); //$NON-NLS-1$
							sb.append(tok.getLabel());
						}
					}
					if (trTags && trCats)
						sb.append(" : "); //$NON-NLS-1$
					int l = sb.length();
					if (trTags) {
						for (Token tok : keywords) {
							if (sb.length() > l)
								sb.append(", "); //$NON-NLS-1$
							sb.append(tok.getLabel());
						}
					}
					String translate = translatorClient.translate(sb.toString());
					if (translate == null)
						translate = sb.toString();
					String translatedCats = null;
					String translatedTags = null;
					if (trTags) {
						if (trCats) {
							int p = translate.indexOf(':');
							if (p >= 0) {
								translatedCats = translate.substring(0, p);
								translatedTags = translate.substring(p + 1);
							} else
								translatedCats = translate;
						} else
							translatedTags = translate;
					} else if (trCats)
						translatedCats = translate;
					applyTranslation(translatedCats, cats);
					applyTranslation(translatedTags, keywords);
				}
			}

			prediction = new Prediction(SERVICENAME, cats.toArray(new Token[cats.size()]),
					keywords.toArray(new Token[keywords.size()]), Status.OK_STATUS);
			if (generateDescription()) {
				ImageDescriptionDetails description = analysis.description();
				if (description != null) {
					List<ImageCaption> captions = description.captions();
					if (!captions.isEmpty()) {
						List<Token> descriptions = new ArrayList<>(captions.size());
						for (ImageCaption caption : captions)
							descriptions.add(new Token(caption.text(), (float) caption.confidence()));
						Collections.sort(descriptions, TokenComparator.INSTANCE);
						String descr = descriptions.get(0).getLabel();
						if (preferenceStore.getBoolean(PreferenceConstants.TRANSLATE_DESCRIPTION)) {
							TranslatorClient translatorClient = AiActivator.getDefault().getClient();
							if (translatorClient != null)
								descr = translatorClient.translate(descr);
						}
						if (descr.length() > 1)
							descr = Character.toUpperCase(descr.charAt(0)) + descr.substring(1);
						prediction.setDescription(descr);
					}
				}
			}
			if (checkFaces()) {
				List<Rectangle> rects = new ArrayList<>();
				List<FaceDescription> faces = analysis.faces();
				if (faces != null)
					for (FaceDescription face : faces) {
						FaceRectangle faceRectangle = face.faceRectangle();
						rects.add(new Rectangle(faceRectangle.left(), faceRectangle.top(), faceRectangle.width(),
								faceRectangle.height()));
					}
				prediction.setFaces(rects);
			}
			if (checkAdultContent()) {
				AdultInfo adult = analysis.adult();
				boolean isAdultContent = adult != null && adult.isAdultContent();
				boolean isRacyContent = adult != null && adult.isRacyContent();
				prediction.setSafeForWork(isAdultContent ? 0f : 1f, isRacyContent ? 0f : 1f);
			}
			return prediction;
		} catch (ComputerVisionErrorResponseException e1) {
			String message = e1.getMessage();
			int p = message.indexOf("message\":\""); //$NON-NLS-1$
			if (p >= 0) {
				message = message.substring(p + 10);
				if (message.endsWith("\"}}")) //$NON-NLS-1$
					message = message.substring(0, message.length() - 3);
			}
			return new Prediction(SERVICENAME, null, null,
					new Status(IStatus.ERROR, MsVisionActivator.PLUGIN_ID, message, e1));
		} catch (Throwable e) {
			return new Prediction(SERVICENAME, null, null, new Status(IStatus.ERROR, MsVisionActivator.PLUGIN_ID,
					Messages.MsVisionServiceProvider_ms_vision_io_error, e));
		}
	}

	private static void applyTranslation(String translated, List<Token> list) {
		if (translated != null) {
			StringTokenizer st = new StringTokenizer(translated, ","); //$NON-NLS-1$
			Iterator<Token> it = list.iterator();
			while (st.hasMoreTokens()) {
				if (!it.hasNext())
					break;
				it.next().setLabel(st.nextToken().trim());
			}
		}
	}

	@Override
	public boolean checkCelebrities() {
		return MsVisionActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CELEBRITIES);
	}

	public void dispose() {
		// do nothing
	}

	public boolean checkAdultContent() {
		return MsVisionActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.ADULTCONTENTS);
	}

	public boolean generateDescription() {
		return MsVisionActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.DESCRIPTION);
	}

	public boolean checkFaces() {
		return MsVisionActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.FACES);
	}

	public String getTitle() {
		return Messages.MsVisionServiceProvider_ms_vision_proposals;
	}

	@Override
	public float getMarkAbove() {
		return MsVisionActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.MARKABOVE);
	}

	@Override
	public boolean getMarkKnownOnly() {
		return MsVisionActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.MARKKNOWNONLY);
	}

	@Override
	public boolean isAccountValid() {
		return MsVisionActivator.getDefault().getClient() != null;
	}

}
