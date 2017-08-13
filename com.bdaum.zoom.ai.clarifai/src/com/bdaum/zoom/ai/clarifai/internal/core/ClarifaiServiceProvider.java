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
 * (c) 2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ai.clarifai.internal.core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Rectangle;

import com.bdaum.zoom.ai.clarifai.internal.ClarifaiActivator;
import com.bdaum.zoom.ai.clarifai.internal.preference.PreferenceConstants;
import com.bdaum.zoom.ai.internal.AbstractAiServiceProvider;
import com.bdaum.zoom.ai.internal.AiActivator;
import com.bdaum.zoom.ai.internal.translator.TranslatorClient;
import com.bdaum.zoom.core.internal.ai.Prediction;
import com.bdaum.zoom.core.internal.ai.Prediction.Token;
import com.bdaum.zoom.core.internal.lire.AiAlgorithm;
import com.bdaum.zoom.core.internal.lire.Algorithm;

import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.ClarifaiStatus;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.input.image.Crop;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import clarifai2.dto.prediction.Embedding;

public class ClarifaiServiceProvider extends AbstractAiServiceProvider {

	private static final Object GENERAL_ID = "aaa03c23b3724a16a56b629203edc62c"; //$NON-NLS-1$
	private static final Object FOOD_ID = "bd367be194cf45149e75f01d59f77ba7"; //$NON-NLS-1$
	private static final Object TRAVEL_ID = "eee28c313d69466f836ab83287a54ed9"; //$NON-NLS-1$
	private static final Object WEDDING_ID = "c386b7a870114f4a87477c0824499348"; //$NON-NLS-1$
	private static final float[] EMPTY = new float[0];

	private Model<?> currentModel;

	@SuppressWarnings("unchecked")
	@Override
	public Prediction predict(byte[] jpeg) {
		Prediction prediction = null;
		ClarifaiActivator activator = ClarifaiActivator.getDefault();
		ClarifaiClient client = activator.getClient();
		if (client != null) {
			String modelId = activator.getModelId();
			if (modelId != null) {
				IPreferenceStore preferenceStore = activator.getPreferenceStore();
				String lang = preferenceStore.getString(PreferenceConstants.LANGUAGE);
				Model<?> model = getModel(client, modelId);
				ClarifaiInput input = ClarifaiInput.forImage(ClarifaiImage.of(jpeg));
				ClarifaiResponse<?> response = model.predict().withInputs(input).withLanguage(lang).executeSync();
				ClarifaiStatus status = response.getStatus();
				if (response.isSuccessful()) {
					int maxConcepts = preferenceStore.getInt(PreferenceConstants.MAXCONCEPTS);
					float minConfidence = preferenceStore.getInt(PreferenceConstants.MINCONFIDENCE) * 0.01f;
					List<Token> result = new ArrayList<>(20);
					List<ClarifaiOutput<Concept>> list = (List<ClarifaiOutput<Concept>>) response.get();
					lp: for (ClarifaiOutput<Concept> clarifaiOutput : list)
						for (Concept concept : clarifaiOutput.data()) {
							float value = concept.value();
							if (result.size() >= maxConcepts || value < minConfidence)
								break lp;
							result.add(new Token(concept.name(), value));
						}
					if (preferenceStore.getBoolean(PreferenceConstants.TRANSLATE)) {
						TranslatorClient translatorClient = AiActivator.getDefault().getClient();
						if (translatorClient != null) {
							StringBuilder sb = new StringBuilder();
							for (Token tok : result) {
								if (sb.length() > 0)
									sb.append(", "); //$NON-NLS-1$
								sb.append(tok.getLabel());
							}
							try {
								String translate = translatorClient.translate(sb.toString());
								StringTokenizer st = new StringTokenizer(translate, ","); //$NON-NLS-1$
								Iterator<Token> it = result.iterator();
								while (st.hasMoreTokens()) {
									if (!it.hasNext())
										break;
									Token token = it.next();
									token.setLabel(st.nextToken().trim());
								}
							} catch (Exception e) {
								// don't translate
							}
						}
					}
					prediction = new Prediction(getName(), result.toArray(new Token[result.size()]), null,
							getStatus(status));
					if (checkAdultContent()) {
						ClarifaiResponse<List<ClarifaiOutput<Concept>>> response2 = client.getDefaultModels()
								.nsfwModel().predict().withInputs(input).executeSync();
						if (response2.isSuccessful()) {
							lp2: for (ClarifaiOutput<Concept> clarifaiOutput : response2.get())
								for (Concept concept : clarifaiOutput.data())
									if ("sfw".equals(concept.name())) { //$NON-NLS-1$
										prediction.setSafeForWork(concept.value(), -1f);
										break lp2;
									}
						}
					}
					// if (checkCelebrities()) {
					// ClarifaiResponse<List<ClarifaiOutput<clarifai2.dto.prediction.Prediction>>>
					// response4 = client
					// .predict("e466caa0619f444ab97497640cefc4dc")
					// .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(jpeg))).executeSync();
					// if (response4.isSuccessful()) {
					// int height = 0;
					// int width = 0;
					// List<Rectangle> rects = null;
					// if (checkFaces()) {
					// try (InputStream in = new ByteArrayInputStream(jpeg)) {
					// BufferedImage image = ImageIO.read(in);
					// height = image.getHeight();
					// width = image.getWidth();
					// rects = new ArrayList<>();
					// } catch (IOException e) {
					// // should never happen
					// }
					// }
					// for (ClarifaiOutput<clarifai2.dto.prediction.Prediction>
					// clarifaiOutput : response4.get())
					// for (clarifai2.dto.prediction.Prediction p :
					// clarifaiOutput.data()) {
					// System.out.println(p);
					// if (rects != null) {
					// FaceDetection faceDetection = p.asFaceDetection();
					// Crop crop = faceDetection.boundingBox();
					// rects.add(new Rectangle((int) (crop.left() * width +
					// 0.5f),
					// (int) (crop.top() * height + 0.5f),
					// (int) ((crop.right() - crop.left()) * width + 0.5f),
					// (int) ((crop.bottom() - crop.top()) * height + 0.5f)));
					// }
					//
					// }
					// if (rects != null)
					// prediction.setFaces(rects);
					// }
					// } else

					if (checkFaces()) {
						Model<?> faceModel = client.getDefaultModels().faceDetectionModel();
						ClarifaiResponse<?> response3 = faceModel.predict().withInputs(input).executeSync();
						if (response3.isSuccessful()) {
							try (InputStream in = new ByteArrayInputStream(jpeg)) {
								BufferedImage image = ImageIO.read(in);
								int height = image.getHeight();
								int width = image.getWidth();
								List<Rectangle> rects = new ArrayList<>();
								for (ClarifaiOutput<clarifai2.dto.prediction.Prediction> clarifaiOutput : (List<ClarifaiOutput<clarifai2.dto.prediction.Prediction>>) response3
										.get())
									for (clarifai2.dto.prediction.Prediction p : clarifaiOutput.data()) {
										Crop crop = p.asFaceDetection().boundingBox();
										rects.add(new Rectangle((int) (crop.left() * width + 0.5f),
												(int) (crop.top() * height + 0.5f),
												(int) ((crop.right() - crop.left()) * width + 0.5f),
												(int) ((crop.bottom() - crop.top()) * height + 0.5f)));
									}
								prediction.setFaces(rects);
							} catch (IOException e) {
								// should never happen
							}

						}
					}
					return prediction;
				}
			}
		}
		return null;
	}

	protected Model<?> getModel(ClarifaiClient client, String modelId) {
		if (currentModel == null || !modelId.equals(currentModel.id())) {
			if (modelId.equals(GENERAL_ID))
				currentModel = client.getDefaultModels().generalModel();
			else if (modelId.equals(FOOD_ID))
				currentModel = client.getDefaultModels().foodModel();
			else if (modelId.equals(TRAVEL_ID))
				currentModel = client.getDefaultModels().travelModel();
			else if (modelId.equals(WEDDING_ID))
				currentModel = client.getDefaultModels().weddingModel();
			else {
				currentModel = client.getModelByID(modelId).executeSync().get();
				if (currentModel == null || !currentModel.isConceptModel())
					currentModel = client.getDefaultModels().generalModel();
			}
		}
		return currentModel;
	}

	private static Status getStatus(ClarifaiStatus status) {
		if (status == null) {
			if (ClarifaiActivator.getDefault().getClient() == null)
				return new Status(IStatus.INFO, ClarifaiActivator.PLUGIN_ID,
						Messages.ClarifaiServiceProvider_not_set_or_wrong);
			return new Status(IStatus.INFO, ClarifaiActivator.PLUGIN_ID, Messages.ClarifaiServiceProvider_pending);
		}
		if (status.networkErrorOccurred())
			return new Status(IStatus.ERROR, ClarifaiActivator.PLUGIN_ID,
					Messages.ClarifaiServiceProvider_network_error);
		return new Status(IStatus.ERROR, ClarifaiActivator.PLUGIN_ID, status.statusCode(), status.errorDetails(), null);
	}

	@Override
	public void dispose() {
		currentModel = null;
		ClarifaiActivator.getDefault().disposeClient();
	}

	@Override
	public String getTitle() {
		return NLS.bind(Messages.ClarifaiServiceProvider_proposals, ClarifaiActivator.getDefault().getTheme());
	}

	@Override
	public boolean checkAdultContent() {
		return ClarifaiActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.ADULTCONTENTS);
	}

	public boolean checkFaces() {
		return ClarifaiActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.FACES);
	}

	// @Override
	// public boolean checkCelebrities() {
	// return
	// ClarifaiActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CELEBRITIES);
	// }

	@Override
	public float getMarkAbove() {
		return ClarifaiActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.MARKABOVE);
	}

	@Override
	public boolean getMarkKnownOnly() {
		return ClarifaiActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.MARKKNOWNONLY);
	}

	@SuppressWarnings("unchecked")
	@Override
	public float[] getFeatureVector(BufferedImage image) {
		ClarifaiActivator activator = ClarifaiActivator.getDefault();
		ClarifaiClient client = activator.getClient();
		if (client != null) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				ImageIO.write(image, "jpg", out); //$NON-NLS-1$
				ClarifaiResponse<?> response = client.getDefaultModels().generalEmbeddingModel().predict()
						.withInputs(ClarifaiInput.forImage(ClarifaiImage.of(out.toByteArray()))).executeSync();
				if (response.isSuccessful())
					for (ClarifaiOutput<Embedding> clarifaiOutput : (List<ClarifaiOutput<Embedding>>) response.get())
						for (Embedding emb : clarifaiOutput.data())
							return emb.embedding();
			} catch (IOException e) {
				// should never happen
			}
		}
		return EMPTY;
	}

	@Override
	public boolean isAccountValid() {
		return ClarifaiActivator.getDefault().getClient() != null;
	}

	@Override
	public Algorithm getAlgorithm() {
		return new AiAlgorithm(getFeatureId(), "clarifai", "Clarifai", //$NON-NLS-1$ //$NON-NLS-2$
				Messages.ClarifaiServiceProvider_clarifai_expl,
				true, ClarifaiActivator.PLUGIN_ID);
	}

	@Override
	public Class<?> getFeature() {
		return ClarifaiFeature.class;
	}

}