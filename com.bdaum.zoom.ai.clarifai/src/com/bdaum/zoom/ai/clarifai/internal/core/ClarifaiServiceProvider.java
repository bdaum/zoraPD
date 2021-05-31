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
 * (c) 2017 Berthold Daum  
 */
package com.bdaum.zoom.ai.clarifai.internal.core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import com.bdaum.zoom.core.internal.lire.Algorithm;

import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.ClarifaiStatus;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.Crop;
import clarifai2.dto.model.DefaultModels;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import clarifai2.dto.prediction.Detection;
import clarifai2.dto.prediction.Embedding;
import clarifai2.dto.prediction.FaceEmbedding;

public class ClarifaiServiceProvider extends AbstractAiServiceProvider {
	// Models
	private static final String GENERAL_ID = "aaa03c23b3724a16a56b629203edc62c"; //$NON-NLS-1$
	private static final String FOOD_ID = "bd367be194cf45149e75f01d59f77ba7"; //$NON-NLS-1$
	private static final String TRAVEL_ID = "eee28c313d69466f836ab83287a54ed9"; //$NON-NLS-1$
	private static final String WEDDING_ID = "c386b7a870114f4a87477c0824499348"; //$NON-NLS-1$
	private static final String CELEBRITIES_ID = "e466caa0619f444ab97497640cefc4dc"; //$NON-NLS-1$
	// Concepts
	private static final String SAFE_FOR_WORK = "sfw"; //$NON-NLS-1$

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
				int maxConcepts = preferenceStore.getInt(PreferenceConstants.MAXCONCEPTS);
				double minConfidence = preferenceStore.getInt(PreferenceConstants.MINCONFIDENCE) * 0.01f;
				Model<?> model = getModel(client, modelId);
				ClarifaiInput input = ClarifaiInput.forInputValue(ClarifaiImage.of(jpeg));
				ClarifaiResponse<?> response = model.predict().withInputs(input).withLanguage(lang)
						.withMaxConcepts(maxConcepts).withMinValue(minConfidence).executeSync();
				ClarifaiStatus status = response.getStatus();
				if (response.isSuccessful()) {
					List<Token> result = new ArrayList<>(20);
					for (ClarifaiOutput<Concept> clarifaiOutput : (List<ClarifaiOutput<Concept>>) response.get())
						for (Concept concept : clarifaiOutput.data())
							result.add(new Token(concept.name(), concept.value()));
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
								if (translate == null)
									translate = sb.toString();
								StringTokenizer st = new StringTokenizer(translate, ","); //$NON-NLS-1$
								int i = 0;
								while (st.hasMoreTokens() && i < result.size())
									result.get(i++).setLabel(st.nextToken().trim());
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
						if (response2.isSuccessful())
							lp: for (ClarifaiOutput<Concept> clarifaiOutput : response2.get())
								for (Concept concept : clarifaiOutput.data())
									if (SAFE_FOR_WORK.equals(concept.name())) {
										prediction.setSafeForWork(concept.value(), -1f);
										break lp;
									}
					}
					List<Rectangle> rects = null;
					if (checkCelebrities()) {
						ClarifaiResponse<List<ClarifaiOutput<clarifai2.dto.prediction.Prediction>>> response4 = client
								.predict(CELEBRITIES_ID).withInputs(ClarifaiInput.forInputValue(ClarifaiImage.of(jpeg)))
								.withMaxConcepts(maxConcepts).withMinValue(minConfidence).executeSync();
						if (response4.isSuccessful()) {
							int height = 0;
							int width = 0;
							if (checkFaces()) {
								try (InputStream in = new ByteArrayInputStream(jpeg)) {
									BufferedImage image = ImageIO.read(in);
									height = image.getHeight();
									width = image.getWidth();
									rects = new ArrayList<>();
								} catch (IOException e) {
									// should never happen
								}
							}
							for (ClarifaiOutput<clarifai2.dto.prediction.Prediction> clarifaiOutput : response4.get())
								for (clarifai2.dto.prediction.Prediction p : clarifaiOutput.data()) {
									if (p instanceof Detection) {
										Detection fc = p.asDetection();
										if (rects != null) {
											Crop crop = fc.crop();
											rects.add(new Rectangle((int) (crop.left() * width + 0.5f),
													(int) (crop.top() * height + 0.5f),
													(int) ((crop.right() - crop.left()) * width + 0.5f),
													(int) ((crop.bottom() - crop.top()) * height + 0.5f)));

										}
										for (Concept concept : fc.concepts())
											result.add(new Token(concept.name(), concept.value()));
									}
								}
							if (rects != null)
								prediction.setFaces(rects);
						}
					}
					if (rects == null && checkFaces()) {
						Model<?> faceModel = client.getDefaultModels().faceDetectionModel();
						ClarifaiResponse<?> response3 = faceModel.predict().withInputs(input).executeSync();
						if (response3.isSuccessful()) {
							try (InputStream in = new ByteArrayInputStream(jpeg)) {
								BufferedImage image = ImageIO.read(in);
								int height = image.getHeight();
								int width = image.getWidth();
								rects = new ArrayList<>();
								for (ClarifaiOutput<clarifai2.dto.prediction.Prediction> clarifaiOutput : (List<ClarifaiOutput<clarifai2.dto.prediction.Prediction>>) response3
										.get())
									for (clarifai2.dto.prediction.Prediction p : clarifaiOutput.data()) {
										Crop crop = p.asFaceEmbedding().crop();
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
				} else if (status.networkErrorOccurred())
					return new Prediction(getName(), null, null, getStatus(status));
			}
		}
		return null;
	}

//	@Override
//	public int rate(Asset asset, String opId, int maxRating, String modelId) {
//		ClarifaiClient client = ClarifaiActivator.getDefault().getClient();
//		if (client != null) {
//			Model<?> model = currentModel = client.getModelByID(modelId).executeSync().get();
//			URI uri = Core.getCore().getVolumeManager().findExistingFile(asset, false);
//			if (uri != null) {
//				String ext = Core.getFileExtension(uri.toString());
//				if (ImageConstants.isJpeg(ext)) {
//					if (Constants.FILESCHEME.equals(uri.getScheme()))
//						return rateImage(maxRating, model, ClarifaiImage.of(new File(uri)));
//					try {
//						return rateImage(maxRating, model, ClarifaiImage.of(uri.toURL()));
//					} catch (MalformedURLException e) {
//						return -1;
//					}
//				}
//				IFileWatcher fileWatcher = CoreActivator.getDefault().getFileWatchManager();
//				MultiStatus status = new MultiStatus(ClarifaiActivator.PLUGIN_ID, 0,
//						Messages.ClarifaiServiceProvider_image_rating, null);
//				File file = null;
//				ZImage hzimage = null;
//				try (Ticketbox box = new Ticketbox()) {
//					try {
//						file = box.obtainFile(uri);
//					} catch (IOException e) {
//						status.add(new Status(IStatus.ERROR, UiActivator.PLUGIN_ID,
//								NLS.bind(Messages.ClarifaiServiceProvider_download_failed, uri), e));
//					}
//					if (file != null) {
//						hzimage = CoreActivator.getDefault().getHighresImageLoader().loadImage(null, status, file,
//								asset.getRotation(), asset.getFocalLengthIn35MmFilm(), null, 1d, Double.MAX_VALUE, true,
//								ImageConstants.SRGB, null, null, null, fileWatcher, opId, null);
//						try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//							hzimage.saveToStream(null, true, ZImage.CROPPED, SWT.DEFAULT, SWT.DEFAULT, out,
//									SWT.IMAGE_JPEG, 75);
//							return rateImage(maxRating, model, ClarifaiImage.of(out.toByteArray()));
//						} catch (UnsupportedOperationException e) {
//							// ignore file
//						} catch (IOException e) {
//							status.add(new Status(IStatus.ERROR, UiActivator.PLUGIN_ID,
//									NLS.bind(Messages.ClarifaiServiceProvider_loading_failed, uri), e));
//						}
//					}
//				} finally {
//					if (hzimage != null)
//						hzimage.dispose();
//					fileWatcher.stopIgnoring(opId);
//				}
//			}
//		}
//		return -1;
//	}

//	@SuppressWarnings("unchecked")
//	protected int rateImage(int maxRating, Model<?> model, ClarifaiImage cImage) {
//		ClarifaiResponse<?> response = model.predict().withInputs(ClarifaiInput.forInputValue(cImage)).executeSync();
//		if (response.isSuccessful())
//			for (ClarifaiOutput<clarifai2.dto.prediction.Prediction> clarifaiOutput : (List<ClarifaiOutput<clarifai2.dto.prediction.Prediction>>) response
//					.get())
//				for (clarifai2.dto.prediction.Prediction prediction : clarifaiOutput.data())
//					if (prediction.isBlur())
//						return (int) (maxRating * prediction.asBlur().value() + 0.5f);
//					else if (HIGH_QUALITY.equals(((Concept) prediction).id()))
//						return (int) (maxRating * ((Concept) prediction).value() + 0.5f);
//		return -1;
//	}

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

	@Override
	public boolean checkCelebrities() {
		return ClarifaiActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CELEBRITIES);
	}

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
	public float[] getFeatureVector(BufferedImage image, int featureId) {
		ClarifaiActivator activator = ClarifaiActivator.getDefault();
		ClarifaiClient client = activator.getClient();
		if (client != null)
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				ImageIO.write(image, "jpg", out); //$NON-NLS-1$
				DefaultModels defaultModels = client.getDefaultModels();
				ClarifaiInput inputValue = ClarifaiInput.forInputValue(ClarifaiImage.of(out.toByteArray()));
				if (featureId == 1002) {
					ClarifaiResponse<?> response = defaultModels.faceEmbeddingModel().predict().withInputs(inputValue)
							.executeSync();
					if (response.isSuccessful())
						for (ClarifaiOutput<FaceEmbedding> clarifaiOutput : (List<ClarifaiOutput<FaceEmbedding>>) response
								.get())
							for (FaceEmbedding emb : clarifaiOutput.data()) {
								List<Embedding> embeddings = emb.embeddings();
								int size = Math.min(20, embeddings.size());
								float[] vv = null;
								for (int i = 0; i < size; i++) {
									float[] embedding = embeddings.get(i).embedding();
									int length = embedding.length;
									if (vv == null) {
										vv = new float[1 + size * length];
										vv[0] = size;
									}
									System.arraycopy(embedding, 0, vv, 1 + i * length, length);
								}
								return vv;
							}
				}
				ClarifaiResponse<?> response = defaultModels.generalEmbeddingModel().predict().withInputs(inputValue)
						.executeSync();
				if (response.isSuccessful())
					for (ClarifaiOutput<Embedding> clarifaiOutput : (List<ClarifaiOutput<Embedding>>) response.get())
						for (Embedding emb : clarifaiOutput.data())
							return emb.embedding();
			} catch (IOException e) {
				// should never happen
			}
		return EMPTY;
	}

	@Override
	public boolean isAccountValid() {
		return ClarifaiActivator.getDefault().getClient() != null;
	}

	@Override
	public Class<?> getFeature(Algorithm algorithm) {
		try {
			return algorithm.getId() == 1002 ? ClarifaiFaceFeature.class : ClarifaiFeature.class;
		} catch (NoClassDefFoundError e) {
			// not installed
			return null;
		}
	}

}