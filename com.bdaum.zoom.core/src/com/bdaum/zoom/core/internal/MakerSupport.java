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
 * (c) 2015 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.core.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.core.internal.db.AssetEnsemble.MWGRegion;
import com.bdaum.zoom.image.IExifLoader;

@SuppressWarnings("unused")
public class MakerSupport {

	private static final Map<String, MakerSupport> makerMap = new HashMap<String, MakerSupport>();

	static {
		new MakerSupport("FUJIFILM") { //$NON-NLS-1$
			@Override
			public void processFaceData(AssetEnsemble ensemble,
					IExifLoader exifTool) throws NumberFormatException {
				Asset asset = ensemble.getAsset();
				Map<String, String> metadata = exifTool.getMetadata();
				String facePositions = metadata.get("FacePositions"); //$NON-NLS-1$
				String width = metadata.get(QueryField.EXIF_IMAGEWIDTH
						.getExifToolKey());
				String height = metadata.get(QueryField.EXIF_IMAGEHEIGHT
						.getExifToolKey());
				if (facePositions != null && !facePositions.isEmpty()
						&& width != null && height != null) {
					int imageWidth = Integer.parseInt(width);
					int imageHeight = Integer.parseInt(height);
					int i = 0;
					int j = 0;
					double x1 = 0, y1 = 0, x2 = 0, y2 = 0;
					StringTokenizer st = new StringTokenizer(facePositions);
					while (st.hasMoreTokens()) {
						double d = Double.parseDouble(st.nextToken());
						switch (i % 4) {
						case 0:
							x1 = d / imageWidth;
							break;
						case 1:
							y1 = d / imageHeight;
							break;
						case 2:
							x2 = d / imageWidth;
							break;
						default:
							y2 = d / imageHeight;
							++j;
							MWGRegion reg = ensemble.getRegion(
									AssetEnsemble.CAMERA, j);
							reg.setRect(x1, y1, x2 - x1, y2 - y1);
							String faceName = metadata.get(NLS.bind(
									"Face{0}Name", (i / 4 + 1))); //$NON-NLS-1$
							if (faceName != null)
								reg.setName(faceName);
							break;
						}
						++i;
					}
					asset.setNoPersons(j);
				}
			}
		};

		new MakerSupport("Panasonic") { //$NON-NLS-1$
			@Override
			public void processFaceData(AssetEnsemble ensemble,
					IExifLoader exifTool) throws NumberFormatException {
				Asset asset = ensemble.getAsset();
				Map<String, String> metadata = exifTool.getMetadata();
				String numFaces = metadata.get("FacesRecognized"); //$NON-NLS-1$
				Map<String, String> faceMap = null;
				if (numFaces != null) {
					faceMap = new HashMap<String, String>();
					int fn = Integer.parseInt(numFaces);
					for (int j = 1; j <= fn; j++) {
						String name = metadata.get(NLS.bind(
								"RecognizedFace{0}Name", j)); //$NON-NLS-1$
						String region = metadata.get(NLS.bind(
								"RecognizedFace{0}Position", j)); //$NON-NLS-1$
						String age = metadata.get(NLS.bind(
								"RecognizedFace{0}Age", j)); //$NON-NLS-1$
						if (name != null && !name.isEmpty() && region != null
								&& age != null && !age.startsWith("9999:")) //$NON-NLS-1$
							faceMap.put(region, name);
					}
				}
				String numPositions = metadata.get("NumFacePositions"); //$NON-NLS-1$
				if (numPositions != null) {
					int n = Integer.parseInt(numPositions);
					for (int i = 1; i <= n; i++) {
						String facePosition = metadata.get(NLS.bind(
								"Face{0}Position", i)); //$NON-NLS-1$
						if (facePosition != null)
							parseRegion(i, ensemble, facePosition, faceMap);
					}
					if (faceMap != null) {
						int i = n + 1;
						for (String region : new ArrayList<String>(
								faceMap.keySet()))
							parseRegion(i++, ensemble, region, faceMap);
					}
					asset.setNoPersons(n);
				}
			}

			private void parseRegion(int index, AssetEnsemble ensemble,
					String facePosition, Map<String, String> faceMap) {
				StringTokenizer st = new StringTokenizer(facePosition);
				int j = 0;
				double x1 = 0, y1 = 0, w = 0, h = 0;
				while (st.hasMoreTokens() && j <= 3) {
					double d = Integer.parseInt(st.nextToken()) / 320d;
					switch (j++) {
					case 0:
						x1 = d;
						break;
					case 1:
						y1 = d;
						break;
					case 2:
						w = d;
						break;
					default:
						h = d;
						MWGRegion region = ensemble.getRegion(
								AssetEnsemble.CAMERA, index);
						region.setRect(x1 - w / 2, y1 - h / 2, w, h);
						String faceName = faceMap == null ? null : faceMap
								.remove(facePosition);
						if (faceName != null)
							region.setName(faceName);
						break;
					}
				}
			}
		};

		new MakerSupport("OLYMPUS IMAGING CORP.") { //$NON-NLS-1$
			@Override
			public void processFaceData(AssetEnsemble ensemble,
					IExifLoader exifTool) throws NumberFormatException {
				Asset asset = ensemble.getAsset();
				Map<String, String> metadata = exifTool.getMetadata();
				String numPositions = metadata.get("FacesDetected"); //$NON-NLS-1$
				if (numPositions != null) {
					int n = 0;
					List<String> facesDetected = Core.fromStringList(
							numPositions, " "); //$NON-NLS-1$
					boolean newModels = facesDetected.size() > 2;
					if (!facesDetected.isEmpty())
						try {
							n = Integer.parseInt(facesDetected.get(0));
						} catch (NumberFormatException e) {
							// ignore
						}
					if (n > 0) {
						double frameWidth = 640d;
						double frameHeight = 480d;
						String frameSize = metadata.get("FaceDetectFrameSize"); //$NON-NLS-1$
						int p = frameSize.indexOf(' ');
						if (p >= 0) {
							int q = frameSize.indexOf(' ', p + 1);
							if (q < 0)
								q = frameSize.length();
							frameWidth = Integer.parseInt(frameSize.substring(
									0, p));
							frameHeight = Integer.parseInt(frameSize.substring(
									p + 1, q));
						}
						byte[] binaryData = exifTool.getBinaryData(
								"FaceDetectArea", false); //$NON-NLS-1$
						if (binaryData != null) {
							String faceDetectArea = new String(binaryData);
							StringTokenizer st = new StringTokenizer(
									faceDetectArea);
							int i = 0, j = 0, x = 0, y = 0, w = 0, h = 0;
							while (st.hasMoreTokens()) {
								if (i >= n)
									break;
								String token = st.nextToken();
								int v = Integer.parseInt(token);
								switch (j++) {
								case 0:
									x = v;
									break;
								case 1:
									y = v;
									break;
								case 2:
									w = v;
									break;
								default:
									h = v;
									j = 0;
									++i;
									MWGRegion region = ensemble.getRegion(
											AssetEnsemble.CAMERA, i);
									if (newModels)
										region.setRect(
												(x - w / 2) / frameWidth,
												(y - w / 2) / frameHeight, w
														/ frameWidth, w
														/ frameWidth);
									else
										region.setRect(
												(x - w / 2) / frameWidth,
												(y - h / 2) / frameHeight, w
														/ frameWidth, h
														/ frameWidth);
									break;
								}
							}
						}
					}
					asset.setNoPersons(n);
				}
			}
		};

		new MakerSupport("Canon") { //$NON-NLS-1$
			@Override
			public void processFaceData(AssetEnsemble ensemble,
					IExifLoader exifTool) throws NumberFormatException {
				Asset asset = ensemble.getAsset();
				Map<String, String> metadata = exifTool.getMetadata();
				String numPositions = metadata.get("FacesDetected"); //$NON-NLS-1$
				String frameSize = metadata.get("FaceDetectFrameSize"); //$NON-NLS-1$
				int imageWidth = asset.getImageWidth();
				int imageHeigth = asset.getImageLength();
				int frameWidth = imageWidth / 10;
				int frameHeight = imageHeigth / 10;
				if (numPositions != null && imageHeigth > 0 && imageWidth > 0) {
					int n = Integer.parseInt(numPositions);
					if (frameSize != null) {
						int p = frameSize.indexOf(' ');
						frameWidth = Integer
								.parseInt(frameSize.substring(0, p));
						frameHeight = Integer.parseInt(frameSize
								.substring(p + 1));
					}
					for (int i = 1; i <= n; i++) {
						String facePosition = metadata.get(NLS.bind(
								"Face{0}Position", i)); //$NON-NLS-1$
						if (facePosition == null)
							continue;
						int p = facePosition.indexOf(' ');
						int x = Integer.parseInt(facePosition.substring(0, p));
						int y = Integer.parseInt(facePosition.substring(p + 1));
						double x1 = imageWidth / 2d + x;
						double x2 = x1 + frameWidth;
						double y1 = imageHeigth / 2d + y;
						double y2 = y1 + frameHeight;
						MWGRegion region = ensemble.getRegion(
								AssetEnsemble.CAMERA, i);
						region.setRect(x1 / imageWidth, y1 / imageHeigth,
								(x2 - x1) / imageWidth, (y2 - y1) / imageHeigth);
						break;
					}
					asset.setNoPersons(n);
				}
			}
		};

		new MakerSupport("PENTAX") { //$NON-NLS-1$ /
			@Override
			public void processFaceData(AssetEnsemble ensemble,
					IExifLoader exifTool) throws NumberFormatException {
				Asset asset = ensemble.getAsset();
				Map<String, String> metadata = exifTool.getMetadata();
				String numPositions = metadata.get("FacesDetected"); //$NON-NLS-1$
				if (numPositions != null) {
					int n = Integer.parseInt(numPositions);
					for (int i = 1; i <= n; i++) {
						String facePosition = metadata.get(NLS.bind(
								"Face{0}Position", i)); //$NON-NLS-1$
						String faceSize = metadata.get(NLS.bind(
								"Face{0}Size", i)); //$NON-NLS-1$
						if (facePosition == null || faceSize == null)
							continue;
						int p = facePosition.indexOf(' ');
						int x = Integer.parseInt(facePosition.substring(0, p));
						int y = Integer.parseInt(facePosition.substring(p + 1));
						p = faceSize.indexOf(' ');
						int w = Integer.parseInt(faceSize.substring(0, p));
						int h = Integer.parseInt(faceSize.substring(p + 1));
						double x1 = 50d + x;
						double x2 = x1 + w;
						double y1 = 50d + y;
						double y2 = y1 + h;
						MWGRegion region = ensemble.getRegion(
								AssetEnsemble.CAMERA, i);
						region.setRect(x1 / 100, y1 / 100, (x2 - x1) / 100,
								(y2 - y1) / 100);
						break;
					}
					asset.setNoPersons(n);
				}
			}
		};

		new MakerSupport("RICOH IMAGING COMPANY, LTD.") { //$NON-NLS-1$
			@Override
			public void processFaceData(AssetEnsemble ensemble,
					IExifLoader exifTool) throws NumberFormatException {
				Asset asset = ensemble.getAsset();
				Map<String, String> metadata = exifTool.getMetadata();
				String numPositions = metadata.get("FacesDetected"); //$NON-NLS-1$
				String frameSize = metadata.get("FaceDetectFrameSize"); //$NON-NLS-1$
				if (numPositions != null && frameSize != null) {
					int n = Integer.parseInt(numPositions);
					int p = frameSize.indexOf(' ');
					int w = Integer.parseInt(frameSize.substring(0, p));
					int h = Integer.parseInt(frameSize.substring(p + 1));
					for (int i = 1; i <= n; i++) {
						String facePosition = metadata.get(NLS.bind(
								"Face{0}Position", i)); //$NON-NLS-1$
						if (facePosition == null)
							continue;
						StringTokenizer st = new StringTokenizer(facePosition);
						int j = 0;
						double x1 = 0, y1 = 0, x2 = 0, y2 = 0;
						while (st.hasMoreTokens() && j <= 3) {
							Double d = Double.parseDouble(st.nextToken());
							switch (j++) {
							case 0:
								x1 = d / w;
								break;
							case 1:
								y1 = d / h;
								break;
							case 2:
								x2 = d / w;
								break;
							default:
								y2 = d / h;
								MWGRegion region = ensemble.getRegion(
										AssetEnsemble.CAMERA, i);
								region.setRect(x1, y1, x2 - x1, y2 - y1);
								break;
							}
						}
					}
					asset.setNoPersons(n);
				}
			}
		};

		new MakerSupport("SONY", "NIKON CORPORATION") { //$NON-NLS-1$ //$NON-NLS-2$
			@Override
			public void processFaceData(AssetEnsemble ensemble,
					IExifLoader exifTool) throws NumberFormatException {
				Asset asset = ensemble.getAsset();
				Map<String, String> metadata = exifTool.getMetadata();
				int imageWidth = asset.getImageWidth();
				int imageHeigth = asset.getImageLength();
				if (imageWidth > 0 && imageHeigth > 0) {
					int i = 0;
					while (true) {
						String facePosition = metadata.get(NLS.bind(
								"Face{0}Position", (++i))); //$NON-NLS-1$
						if (facePosition == null)
							break;
						StringTokenizer st = new StringTokenizer(facePosition);
						int j = 0;
						double x1 = 0, y1 = 0, w = 0, h = 0;
						while (st.hasMoreTokens() && j <= 3) {
							double d = Double.parseDouble(st.nextToken());
							switch (j++) {
							case 0:
								y1 = d / imageHeigth;
								break;
							case 1:
								x1 = d / imageWidth;
								break;
							case 2:
								h = d / imageHeigth;
								break;
							default:
								w = d * 65536 / imageWidth;
								MWGRegion region = ensemble.getRegion(
										AssetEnsemble.CAMERA, i);
								region.setRect(x1, y1, w, h);
								break;
							}
						}
					}
					asset.setNoPersons(i - 1);
				}
			}
		};
	}

	public static MakerSupport getMakerSupport(String maker) {
		return makerMap.get(maker);
	}

	private String[] makers;

	private MakerSupport(String... makers) {
		this.makers = makers;
		for (String maker : makers)
			makerMap.put(maker, this);
	}

	public void processFaceData(AssetEnsemble ensemble, IExifLoader exifTool)
			throws NumberFormatException {
		// by default do nothing
	}

	@Override
	public String toString() {
		return this.getClass().getName() + ": " //$NON-NLS-1$
				+ Core.toStringList(makers, ", "); //$NON-NLS-1$
	}

}
