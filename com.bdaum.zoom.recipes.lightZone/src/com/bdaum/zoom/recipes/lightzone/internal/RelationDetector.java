/*******************************************************************************
 * Copyright (c) 2014 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.recipes.lightzone.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IRelationDetector;
import com.bdaum.zoom.core.LifeCycleAdapter;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class RelationDetector implements IRelationDetector {

	private static final String RELATIVE_PATH = "relativePath=\""; //$NON-NLS-1$
	private static final String PATH = "path=\""; //$NON-NLS-1$
	private static final String LIGHTZONEPOSTFIX = "_lzn.jpg"; //$NON-NLS-1$
	private static final String LIGHT_ZONE_TRANSFORM = "LightZoneTransform"; //$NON-NLS-1$

	public class LightZoneDescriptor {

		private String path;
		private String relativePath;
		private String text;
		private final File file;

		public LightZoneDescriptor(File file, String text) {
			this.file = file;
			this.text = text;
		}

		public File getOriginalFile() {
			File oFile = null;
			if (path != null)
				oFile = new File(path);
			if (oFile != null && oFile.exists())
				return oFile;
			if (relativePath != null) {
				oFile = new File(file.getParent(), relativePath);
				if (oFile.exists())
					return oFile;
			}
			return null;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public void setRelativePath(String relativePath) {
			this.relativePath = relativePath;
		}

		public String getText() {
			return text;
		}

		public boolean confirmOriginalFile(File target) {
			File oFile = null;
			if (path != null)
				oFile = new File(path);
			if (oFile != null && oFile.equals(target))
				return true;
			if (relativePath != null) {
				oFile = new File(file.getParent(), relativePath);
				if (oFile.equals(target))
					return true;
			}
			return false;
		}

		/**
		 * @return path
		 */
		public String getPath() {
			return path;
		}

		/**
		 * @return relativePath
		 */
		public String getRelativePath() {
			return relativePath;
		}

	}

	private String id;
	private String name;
	private String description;
	private int dialogReturn = 0;
	private boolean dontAskAgain = false;
	private Map<String, File> originals;
	private Map<String, List<File>> derivedFiles;

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IRelationDetector#detectRelation(java.lang.String,
	 * boolean, boolean, com.bdaum.zoom.core.internal.db.AssetEnsemble,
	 * java.util.Set, java.util.List)
	 */
	public URI detectRelation(String uri, boolean isDng, boolean isRaw,
			AssetEnsemble ensemble, Collection<Object> toBeDeleted,
			Collection<Object> toBeStored) {
		try {
			File file = new File(new URI(uri));
			String fileName = file.getName();
			if (fileName.endsWith(LIGHTZONEPOSTFIX)) {
				LightZoneDescriptor descriptor = getLightZoneDescriptor(file);
				if (descriptor != null) {
					File originalFile = descriptor.getOriginalFile();
					if (originalFile != null)
						return originalFile.toURI();
				}
			}
		} catch (URISyntaxException e) {
			// ignore wrong URI
		}
		return null;
	}

	private LightZoneDescriptor getLightZoneDescriptor(File file) {
		JpegSegmentReader reader = new JpegSegmentReader(file);
		byte[] bytes = reader.readSegment(JpegSegmentReader.SEGMENT_APP4);
		if (bytes == null)
			return null;
		final LightZoneDescriptor descriptor = new LightZoneDescriptor(file,
				new String(bytes));
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				private boolean lz = false;

				@Override
				public void startElement(String namespaceURI, String localName,
						String qName, Attributes atts) throws SAXException {
					if (LIGHT_ZONE_TRANSFORM.equals(qName))
						lz = true;
					else if (lz && "Image".equals(qName)) { //$NON-NLS-1$
						descriptor.setPath(atts.getValue("path")); //$NON-NLS-1$
						descriptor.setRelativePath(atts
								.getValue("relativePath")); //$NON-NLS-1$
					}
				}

				@Override
				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					if (LIGHT_ZONE_TRANSFORM.equals(qName))
						lz = false;
				}
			};
			saxParser.parse(in, handler);
		} catch (Exception e) {
			LightZoneActivator.getDefault().logError(
					NLS.bind(Messages.RelationDetector_cannot_parse, file), e);
		}
		return descriptor;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IRelationDetector#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IRelationDetector#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IRelationDetector#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IRelationDetector#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IRelationDetector#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IRelationDetector#getId()
	 */
	public String getId() {
		return id;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IRelationDetector#moveAsset(com.bdaum.zoom.cat.model
	 * .asset.Asset, java.io.File, java.io.File,
	 * org.eclipse.core.runtime.IAdaptable, boolean, java.lang.String)
	 */
	public boolean moveAsset(Asset asset, File file, File dest,
			IAdaptable adaptable, String opId) {
		String fileName = dest.getName();
		if (fileName.endsWith(LIGHTZONEPOSTFIX)) {
			LightZoneDescriptor descriptor = getLightZoneDescriptor(dest);
			if (descriptor == null)
				return false;
			IDbManager dbManager = Core.getCore().getDbManager();
			List<DerivedByImpl> set = dbManager.obtainObjects(
					DerivedByImpl.class,
					"derivative", asset.getStringId(), QueryField.EQUALS); //$NON-NLS-1$
			for (DerivedByImpl rel : set) {
				AssetImpl originalAsset = dbManager.obtainAsset(rel
						.getOriginal());
				if (originalAsset != null) {
					try {
						File originalFile = new File(new URI(
								originalAsset.getUri()));
						if (descriptor.confirmOriginalFile(originalFile)) {
							confirmModification(adaptable, dest);
							if (dialogReturn != 0)
								continue;
							StringBuilder sb = new StringBuilder(
									descriptor.getText());
							int p = sb.indexOf(RELATIVE_PATH);
							if (p >= 0) {
								int q = sb.indexOf(
										"\"", p + RELATIVE_PATH.length()); //$NON-NLS-1$
								if (q >= 0)
									sb.replace(
											p + RELATIVE_PATH.length(),
											q,
											dest.getParentFile()
													.toURI()
													.relativize(
															originalFile
																	.toURI())
													.getPath());
							}
							return new JpegSegmentWriter(dest).replaceSegment(
									JpegSegmentReader.SEGMENT_APP4, sb
											.toString().getBytes(), opId);
						}
					} catch (URISyntaxException e) {
						// ignore this item
					}
				}
			}
			return false;
		}
		// original file moved
		return renameAsset(asset, file, dest, adaptable, opId);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.IRelationDetector#renameAsset(com.bdaum.zoom.cat.
	 * model.asset.Asset, java.io.File, java.io.File,
	 * org.eclipse.core.runtime.IAdaptable, boolean, java.lang.String)
	 */
	public boolean renameAsset(Asset asset, File file, File dest,
			IAdaptable adaptable, String opId) {
		IDbManager dbManager = Core.getCore().getDbManager();
		List<DerivedByImpl> set = dbManager.obtainObjects(DerivedByImpl.class,
				"original", asset.getStringId(), QueryField.EQUALS); //$NON-NLS-1$
		int n = set.size();
		for (DerivedByImpl rel : set) {
			AssetImpl derivedAsset = dbManager.obtainAsset(rel.getDerivative());
			if (derivedAsset != null) {
				try {
					final File derivedFile = new File(new URI(
							derivedAsset.getUri()));
					String fileName = derivedFile.getName();
					if (fileName.endsWith(LIGHTZONEPOSTFIX)) {
						LightZoneDescriptor descriptor = getLightZoneDescriptor(derivedFile);
						if (descriptor != null
								&& descriptor.confirmOriginalFile(file)) {
							if (doRename(descriptor, derivedFile, dest,
									adaptable, opId))
								--n;
						}
					}
				} catch (URISyntaxException e) {
					// ignore this item
				}
			}
		}
		return n == 0;
	}

	private boolean doRename(LightZoneDescriptor descriptor, File derivedFile,
			File originalFile, IAdaptable adaptable, String opId) {
		confirmModification(adaptable, derivedFile);
		if (dialogReturn != 0)
			return false;
		StringBuilder sb = new StringBuilder(descriptor.getText());
		int p = sb.indexOf(PATH);
		if (p >= 0) {
			int q = sb.indexOf("\"", p + PATH.length()); //$NON-NLS-1$
			if (q >= 0) {
				sb.replace(p + PATH.length(), q, originalFile.getAbsolutePath());
			}
		}
		p = sb.indexOf(RELATIVE_PATH);
		if (p >= 0) {
			int q = sb.indexOf("\"", p + RELATIVE_PATH.length()); //$NON-NLS-1$
			if (q >= 0)
				sb.replace(
						p + RELATIVE_PATH.length(),
						q,
						derivedFile.getParentFile().toURI()
								.relativize(originalFile.toURI()).getPath());
		}
		return new JpegSegmentWriter(derivedFile).replaceSegment(
				JpegSegmentReader.SEGMENT_APP4, sb.toString().getBytes(), opId);
	}

	private void confirmModification(IAdaptable adaptable,
			final File derivedFile) {
		if (!dontAskAgain) {
			final Shell shell = (Shell) adaptable.getAdapter(Shell.class);
			if (shell.isDisposed())
				dialogReturn = 1;
			else
				shell.getDisplay().syncExec(() -> {
					UpdateDerivedDialog dialog = new UpdateDerivedDialog(
							shell, derivedFile);
					dialogReturn = dialog.open();
					dontAskAgain = dialog.getAsk();
				});
		}
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IRelationDetector#transferFile(java.io.File,
	 * java.io.File, boolean, org.eclipse.core.runtime.IAdaptable,
	 * java.lang.String)
	 */
	public void transferFile(File source, File target, boolean first,
			IAdaptable info, String opId) {
		if (originals == null) {
			originals = new HashMap<String, File>();
			derivedFiles = new HashMap<String, List<File>>();
			UiActivator.getDefault().addLifeCycleListener(
					new LifeCycleAdapter() {
						@Override
						public void sessionClosed(int mode) {
							originals.clear();
							derivedFiles.clear();
						}
					});
		}
		if (first)
			reset();
		String fileName = target.getName();
		if (fileName.endsWith(LIGHTZONEPOSTFIX)) {
			// this is a derived file
			LightZoneDescriptor descriptor = getLightZoneDescriptor(target);
			String path = descriptor.getPath(); // original
			if (path != null) {
				File originalTarget = originals.get(path);
				if (originalTarget != null) { // was already transferred
					if (originalTarget.exists())
						doRename(descriptor, target, originalTarget, info, opId);
				} else {
					List<File> list = derivedFiles.get(path); // no, store
																// derived file
																// for later
					if (list == null) {
						list = new ArrayList<File>(3);
						derivedFiles.put(path, list);
					}
					list.add(target);
				}
			}
		} else {
			String path = source.getAbsolutePath(); // this may be an original
			originals.put(path, target); // keep this for later reference by
											// derived files
			List<File> list = derivedFiles.remove(path); // look if there are
															// derived files
															// waiting for this
															// original
			if (list != null)
				for (File derived : list)
					if (derived.exists())
						doRename(getLightZoneDescriptor(derived), derived,
								target, info, opId);
		}
	}

	public void reset() {
		dontAskAgain = false;
		dialogReturn = 0;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.IRelationDetector#getCollapsePattern()
	 */
	public String getCollapsePattern() {
		return "/*" + LIGHTZONEPOSTFIX + ";/*.*"; //$NON-NLS-1$//$NON-NLS-2$
	}

}
