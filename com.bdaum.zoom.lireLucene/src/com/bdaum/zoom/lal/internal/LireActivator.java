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
 * (c) 2014 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.lal.internal;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CompositeReader;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ai.IAiService;
import com.bdaum.zoom.core.internal.lire.Algorithm;
import com.bdaum.zoom.core.internal.lucene.ParseException;
import com.bdaum.zoom.lal.internal.lire.ChainedDocumentBuilder;
import com.bdaum.zoom.lal.internal.lire.Lire;
import com.bdaum.zoom.lal.internal.lire.TextDocumentBuilder;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.ACCID;
import net.semanticmetadata.lire.imageanalysis.features.global.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.features.global.BinaryPatternsPyramid;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.features.global.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.imageanalysis.features.global.FuzzyColorHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.FuzzyOpponentHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.Gabor;
import net.semanticmetadata.lire.imageanalysis.features.global.JCD;
import net.semanticmetadata.lire.imageanalysis.features.global.JpegCoefficientHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.LocalBinaryPatterns;
import net.semanticmetadata.lire.imageanalysis.features.global.LuminanceLayout;
import net.semanticmetadata.lire.imageanalysis.features.global.OpponentHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.PHOG;
import net.semanticmetadata.lire.imageanalysis.features.global.RotationInvariantLocalBinaryPatterns;
import net.semanticmetadata.lire.imageanalysis.features.global.ScalableColor;
import net.semanticmetadata.lire.imageanalysis.features.global.SimpleColorHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.Tamura;
import net.semanticmetadata.lire.imageanalysis.features.global.centrist.SpatialPyramidCentrist;
import net.semanticmetadata.lire.imageanalysis.features.global.joint.JointHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.joint.LocalBinaryPatternsAndOpponent;
import net.semanticmetadata.lire.imageanalysis.features.global.joint.RankAndOpponent;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPACC;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPCEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPFCTH;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPJCD;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPLBP;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;

@SuppressWarnings("restriction")
public class LireActivator extends Plugin {

	private class IndexWriterEntry {
		IndexWriter writer;
		int count;

		public IndexWriterEntry(IndexWriter indexWriter, int count) {
			this.writer = indexWriter;
			this.count = count;
		}

		public IndexWriter flushIndexWriter() throws CorruptIndexException, IOException {
			Directory directory = writer.getDirectory();
			try {
				writer.commit();
			} catch (OutOfMemoryError e) {
				try {
					writer.close();
				} finally {
					writer = createIndexWriter(directory, false);
				}
			}
			return writer;
		}
	}

	private class IndexReaderEntry {
		IndexReader currentReader;
		Map<IndexReader, Integer> readerCounts = new HashMap<IndexReader, Integer>(8);
		List<IndexReader> obsoleteReaders = new LinkedList<IndexReader>();

		public IndexReaderEntry(IndexReader indexReader) {
			currentReader = indexReader;
			readerCounts.put(indexReader, 1);
		}

		public IndexReader getReader(File indexPath) throws CorruptIndexException, IOException {
			if (currentReader == null || obsoleteReaders.contains(currentReader)) {
				IndexReader indexReader = createBaseIndexReader(indexPath);
				currentReader = indexReader;
				readerCounts.put(indexReader, 1);
				return indexReader;
			}
			IndexReader newReader = openIfChanged(currentReader);
			if (newReader != null) {
				Integer useCount = readerCounts.get(currentReader);
				if (useCount == null || useCount.intValue() == 0) {
					closeIndexReader(currentReader);
					readerCounts.remove(currentReader);
				} else
					obsoleteReaders.add(currentReader);
				currentReader = newReader;
				return currentReader;
			}
			Integer useCount = readerCounts.get(currentReader);
			readerCounts.put(currentReader, new Integer(useCount == null ? 1 : useCount + 1));
			return currentReader;
		}

		public void releaseReader(IndexReader indexReader) {
			Integer useCount = readerCounts.get(indexReader);
			if (useCount == null)
				try {
					closeIndexReader(indexReader);
					if (currentReader == indexReader)
						currentReader = null;
				} catch (IOException e) {
					// ignore
				}
			else if (useCount > 1)
				readerCounts.put(indexReader, new Integer(Math.max(0, useCount - 1)));
			else
				readerCounts.remove(indexReader);
			Iterator<IndexReader> it = obsoleteReaders.iterator();
			while (it.hasNext()) {
				IndexReader reader = it.next();
				Integer u = readerCounts.get(reader);
				if (u == null || u.intValue() == 0) {
					try {
						closeIndexReader(reader);
						if (currentReader == reader)
							currentReader = null;
					} catch (IOException e) {
						// ignore
					}
					it.remove();
				}
			}
		}

		public void invalidateAllReaders() {
			obsoleteReaders.addAll(readerCounts.keySet());
		}

		public void releaseAllReaders() {
			Iterator<IndexReader> iterator = readerCounts.keySet().iterator();
			while (iterator.hasNext())
				releaseReader(iterator.next());
			readerCounts.clear();
		}

	}

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bdaum.zoom.lireAndLucene"; //$NON-NLS-1$
	public static final String FIELD_NAME_FULL_TEXT = "descriptorFullText"; //$NON-NLS-1$

	private static LireActivator plugin;

	private IndexWriter indexWriter;
	private final Map<File, IndexWriterEntry> indexWriterMap = new HashMap<File, IndexWriterEntry>(8);
	private final Map<File, IndexReaderEntry> indexReaderMap = new HashMap<File, IndexReaderEntry>(8);
	private Thread shutdownHook;

	private StandardAnalyzer analyzer;
	private Collection<String> textFields;
	private Collection<String> algorithms;
	private QueryParser queryParser;
	private Hashtable<IndexReader, ImageSearcher> searcherMap = new Hashtable<IndexReader, ImageSearcher>();

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		shutdownHook = new Thread(new Runnable() {
			public void run() {
				try {
					closeIndexWriter();
				} catch (IOException e) {
					// do nothing
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	public void closeIndexWriter() throws CorruptIndexException, IOException {
		synchronized (indexWriterMap) {
			try {
				if (indexWriter != null)
					indexWriter.close();
			} finally {
				indexWriter = null;
			}
		}
	}

	public void closeIndexReader(IndexReader reader) throws IOException {
		searcherMap.remove(reader);
		try {
			if (reader instanceof CompositeReader)
				for (LeafReaderContext context : ((CompositeReader) reader).leaves())
					context.reader().close();
			reader.close();
		} catch (AlreadyClosedException e) {
			// do nothing
		}
	}

	public IndexWriter createIndexWriter(Directory dir, boolean create) throws IOException, LockObtainFailedException {
		IndexWriterConfig config = new IndexWriterConfig(getLuceneAnalyzer());
		config.setOpenMode(create ? IndexWriterConfig.OpenMode.CREATE : IndexWriterConfig.OpenMode.APPEND);
		return new IndexWriter(dir, config);
	}

	public StandardAnalyzer getLuceneAnalyzer() {
		if (analyzer == null)
			analyzer = new StandardAnalyzer();
		return analyzer;
	}

	public IndexReader createBaseIndexReader(File indexPath) throws IOException {
		return DirectoryReader.open(SimpleFSDirectory.open(indexPath.toPath()));
	}

	public IndexReader openIfChanged(IndexReader oldReader) throws IOException {
		if (oldReader instanceof DirectoryReader)
			return DirectoryReader.openIfChanged((DirectoryReader) oldReader);
		return null;
	}

	public Field createTextSearchField(String fieldName, String value) {
		return new TextField(fieldName, value, Store.NO);
	}

	public Field createDocumentIdentifierField(String identifier) {
		return new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES);
	}

	public IndexWriter flushIndexWriter() throws CorruptIndexException, IOException {
		synchronized (indexWriterMap) {
			if (indexWriter != null) {
				try {
					indexWriter.commit();
				} catch (OutOfMemoryError e) {
					try {
						indexWriter.close();
					} finally {
						indexWriter = createIndexWriter(indexWriter.getDirectory(), false);
					}
				}
			}
		}
		return indexWriter;
	}

	public IndexWriter flushIndexWriter(File indexPath) throws CorruptIndexException, IOException {
		synchronized (indexWriterMap) {
			IndexWriterEntry entry = indexWriterMap.get(indexPath);
			if (entry != null)
				return entry.flushIndexWriter();
		}
		return null;
	}

	public IndexWriter getIndexWriter(Directory dir, boolean create)
			throws CorruptIndexException, LockObtainFailedException, IOException, IllegalStateException {
		synchronized (indexWriterMap) {
			if (indexWriter != null)
				throw new IllegalStateException(Messages.LireActivator_index_writer_in_use);
			indexWriter = createIndexWriter(dir, create);
		}
		return indexWriter;
	}

	public IndexWriter getIndexWriter(File indexPath)
			throws CorruptIndexException, LockObtainFailedException, IOException {
		synchronized (indexWriterMap) {
			IndexWriterEntry entry = indexWriterMap.get(indexPath);
			if (entry == null) {
				boolean create = !indexPath.exists();
				Directory indexWriterDir = FSDirectory.open(indexPath.toPath());
				IndexWriter writer = createIndexWriter(indexWriterDir, create);
				indexWriterMap.put(indexPath, new IndexWriterEntry(writer, 1));
				return writer;
			}
			++entry.count;
			return entry.writer;
		}
	}

	public void releaseIndexWriter(File indexPath, boolean force) throws CorruptIndexException, IOException {
		synchronized (indexWriterMap) {
			IndexWriterEntry entry = indexWriterMap.get(indexPath);
			if (entry != null) {
				if (--entry.count <= 0 || force) {
					try {
						IndexWriter writer = entry.writer;
						@SuppressWarnings("resource")
						Directory directory = writer.getDirectory();
						writer.close();
						directory.deleteFile("write.lock"); //$NON-NLS-1$
						directory.close();
					} finally {
						indexWriterMap.remove(indexPath);
					}
				}
			}
		}
	}

	public IndexReader getIndexReader(File indexPath) throws CorruptIndexException, IOException {
		synchronized (indexReaderMap) {
			IndexReaderEntry entry = indexReaderMap.get(indexPath);
			if (entry == null) {
				IndexReader indexReader = createBaseIndexReader(indexPath);
				indexReaderMap.put(indexPath, new IndexReaderEntry(indexReader));
				return indexReader;
			}
			return entry.getReader(indexPath);
		}
	}

	public void releaseIndexReader(File indexPath, IndexReader indexReader) {
		synchronized (indexReaderMap) {
			searcherMap.remove(indexReader);
			IndexReaderEntry entry = indexReaderMap.get(indexPath);
			if (entry != null)
				entry.releaseReader(indexReader);
			else
				try {
					closeIndexReader(indexReader);
				} catch (IOException e) {
					// ignore
				}
		}
	}

	public void releaseAllIndexReadersAndWriters() {
		for (IndexReaderEntry entry : indexReaderMap.values())
			entry.releaseAllReaders();
		for (File indexPath : indexWriterMap.keySet())
			try {
				releaseIndexWriter(indexPath, true);
			} catch (CorruptIndexException e) {
				logError(NLS.bind(Messages.LireActivator_corrupt_index, indexPath), e);
			} catch (IOException e) {
				logError(NLS.bind(Messages.LireActivator_io_error_reading, indexPath), e);
			}

	}

	public void invalidateAllReaders(File indexPath) {
		synchronized (indexReaderMap) {
			IndexReaderEntry entry = indexReaderMap.get(indexPath);
			if (entry != null)
				entry.invalidateAllReaders();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	public static LireActivator getDefault() {
		return plugin;
	}

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	public void logInfo(String message) {
		getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
	}

	public DocumentBuilder constructFullDocumentBuilder() {
		return constructFullDocumentBuilder(getLuceneTextDocumentBuilder());
	}

	public DocumentBuilder getLuceneTextDocumentBuilder() {
		return new TextDocumentBuilder(FIELD_NAME_FULL_TEXT, textFields);
	}

	@SuppressWarnings("unchecked")
	public DocumentBuilder constructFullDocumentBuilder(DocumentBuilder... additions) {
		ChainedDocumentBuilder cdb = new ChainedDocumentBuilder();
		boolean mpeg7 = supportsAlgorithm(Lire.MPEG7);
		if (mpeg7 || supportsAlgorithm(Lire.SCALABLECOLOR) || supportsAlgorithm(Lire.COLORONLY))
			cdb.addBuilder(new GlobalDocumentBuilder(ScalableColor.class));
		if (mpeg7 || supportsAlgorithm(Lire.COLORLAYOUT))
			cdb.addBuilder(new GlobalDocumentBuilder(ColorLayout.class));
		if (mpeg7 || supportsAlgorithm(Lire.EDGEHISTOGRAM))
			cdb.addBuilder(new GlobalDocumentBuilder(EdgeHistogram.class));
		if (mpeg7 || supportsAlgorithm(Lire.CEDD))
			cdb.addBuilder(new GlobalDocumentBuilder(CEDD.class));
		if (supportsAlgorithm(Lire.FCTH))
			cdb.addBuilder(new GlobalDocumentBuilder(FCTH.class));
		if (supportsAlgorithm(Lire.TAMURA))
			cdb.addBuilder(new GlobalDocumentBuilder(Tamura.class));
		if (supportsAlgorithm(Lire.GABOR))
			cdb.addBuilder(new GlobalDocumentBuilder(Gabor.class));
		if (supportsAlgorithm(Lire.COLORHISTOGRAM))
			cdb.addBuilder(new GlobalDocumentBuilder(SimpleColorHistogram.class));
		if (supportsAlgorithm(Lire.JCD))
			cdb.addBuilder(new GlobalDocumentBuilder(JCD.class));
		if (supportsAlgorithm(Lire.JOINTHISTOGRAM))
			cdb.addBuilder(new GlobalDocumentBuilder(JointHistogram.class));
		if (supportsAlgorithm(Lire.JPEG))
			cdb.addBuilder(new GlobalDocumentBuilder(JpegCoefficientHistogram.class));
		if (supportsAlgorithm(Lire.LUMINANCE))
			cdb.addBuilder(new GlobalDocumentBuilder(LuminanceLayout.class));
		if (supportsAlgorithm(Lire.OPPONENTHISTOGRAM))
			cdb.addBuilder(new GlobalDocumentBuilder(OpponentHistogram.class));
		if (supportsAlgorithm(Lire.PHOG))
			cdb.addBuilder(new GlobalDocumentBuilder(PHOG.class));
		if (supportsAlgorithm(Lire.AUTOCOLOR))
			cdb.addBuilder(new GlobalDocumentBuilder(AutoColorCorrelogram.class));
		if (supportsAlgorithm(Lire.ACCID))
			cdb.addBuilder(new GlobalDocumentBuilder(ACCID.class));
		if (supportsAlgorithm(Lire.BINPATTERNSPYRAMID))
			cdb.addBuilder(new GlobalDocumentBuilder(BinaryPatternsPyramid.class));
		if (supportsAlgorithm(Lire.FUZZYOPPOHISTOGRAM))
			cdb.addBuilder(new GlobalDocumentBuilder(FuzzyOpponentHistogram.class));
		if (supportsAlgorithm(Lire.LOCALBINARYPATTERN))
			cdb.addBuilder(new GlobalDocumentBuilder(LocalBinaryPatterns.class));
		if (supportsAlgorithm(Lire.LOCALBINARYPATTERNOPPO))
			cdb.addBuilder(new GlobalDocumentBuilder(LocalBinaryPatternsAndOpponent.class));
		if (supportsAlgorithm(Lire.FUZZYCOLORHISTOGRAM))
			cdb.addBuilder(new GlobalDocumentBuilder(FuzzyColorHistogram.class));
		if (supportsAlgorithm(Lire.RANKOPPONENT))
			cdb.addBuilder(new GlobalDocumentBuilder(RankAndOpponent.class));
		if (supportsAlgorithm(Lire.ROTINVBINPATTERN))
			cdb.addBuilder(new GlobalDocumentBuilder(RotationInvariantLocalBinaryPatterns.class));
		if (supportsAlgorithm(Lire.SPACC))
			cdb.addBuilder(new GlobalDocumentBuilder(SPACC.class));
		if (supportsAlgorithm(Lire.SPATIALPYRAMIDCENTRIST))
			cdb.addBuilder(new GlobalDocumentBuilder(SpatialPyramidCentrist.class));
		if (supportsAlgorithm(Lire.SPCEDD))
			cdb.addBuilder(new GlobalDocumentBuilder(SPCEDD.class));
		if (supportsAlgorithm(Lire.SPFCTH))
			cdb.addBuilder(new GlobalDocumentBuilder(SPFCTH.class));
		if (supportsAlgorithm(Lire.SPJCD))
			cdb.addBuilder(new GlobalDocumentBuilder(SPJCD.class));
		if (supportsAlgorithm(Lire.SPLBP))
			cdb.addBuilder(new GlobalDocumentBuilder(SPLBP.class));
		IAiService service = CoreActivator.getDefault().getAiService();
		if (service != null)
			for (Algorithm aiAlgorithm : service.getLireAlgorithms())
				if (supportsAlgorithm(aiAlgorithm.getId())) {
					Class<? extends GlobalFeature> feature = (Class<? extends GlobalFeature>) service
							.getFeature(aiAlgorithm.getProviderId());
					if (feature != null)
						cdb.addBuilder(new GlobalDocumentBuilder(feature));
				}
		for (DocumentBuilder documentBuilder : additions)
			cdb.addBuilder(documentBuilder);
		return cdb;
	}

	public boolean supportsAlgorithm(int id) {
		Algorithm algo = getAlgorithmById(id);
		return algo == null ? false : algorithms.contains(algo.getName());
	}

	public Algorithm getAlgorithmById(int id) {
		for (Algorithm algo : getSupportedSimilarityAlgorithms())
			if (algo.getId() == id)
				return algo;
		return null;
	}

	public Algorithm[] getSupportedSimilarityAlgorithms() {
		if (Lire.SupportedSimilarityAlgorithms == null)
			Lire.setUpAlgorithms();
		return Lire.SupportedSimilarityAlgorithms;
	}

	public void setTextFields(Collection<String> textFields) {
		this.textFields = textFields;
	}

	public void setAlgorithms(Collection<String> algo) {
		algorithms = algo;
	}

	public Query parseQuery(String query) throws ParseException {
		if (queryParser == null)
			queryParser = new QueryParser(FIELD_NAME_FULL_TEXT, getLuceneAnalyzer());
		try {
			return queryParser.parse(query);
		} catch (org.apache.lucene.queryparser.classic.ParseException e) {
			throw new ParseException(e);
		}
	}

	public void deleteDocuments(IndexWriter indexWriter, String assetId) throws IOException {
		indexWriter.deleteDocuments(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, assetId));
	}

	public ImageSearchHits search(IndexReader indexReader, int docId, int method, int maxResults) throws IOException {
		return getImageSearcher(indexReader, method, maxResults).search(indexReader.document(docId), indexReader);
	}

	private ImageSearcher getImageSearcher(IndexReader indexReader, int method, int maxResults) {
		ImageSearcher searcher = searcherMap.get(indexReader);
		if (searcher == null) {
			searcher = getContentSearcher(method, maxResults);
			searcherMap.put(indexReader, searcher);
		}
		return searcher;
	}

	public ImageSearcher getContentSearcher(int method, int maxResults) {
		switch (method) {
		case Lire.SCALABLECOLOR:
			return new GenericFastImageSearcher(maxResults, ScalableColor.class, false, null, true);
		case Lire.EDGEHISTOGRAM:
			return new GenericFastImageSearcher(maxResults, EdgeHistogram.class, false, null, true);
		case Lire.COLORLAYOUT:
			return new GenericFastImageSearcher(maxResults, ColorLayout.class, false, null, true);
		case Lire.COLORONLY:
			return new GenericFastImageSearcher(maxResults, ScalableColor.class, false, null, true);
		case Lire.CEDD:
			return new GenericFastImageSearcher(maxResults, CEDD.class, false, null, true);
		case Lire.FCTH:
			return new GenericFastImageSearcher(maxResults, FCTH.class, false, null, true);
		case Lire.TAMURA:
			return new GenericFastImageSearcher(maxResults, Tamura.class, false, null, true);
		case Lire.GABOR:
			return new GenericFastImageSearcher(maxResults, Gabor.class, false, null, true);
		case Lire.COLORHISTOGRAM:
			return new GenericFastImageSearcher(maxResults, SimpleColorHistogram.class, false, null, true);
		case Lire.JCD:
			return new GenericFastImageSearcher(maxResults, JCD.class, false, null, true);
		case Lire.JOINTHISTOGRAM:
			return new GenericFastImageSearcher(maxResults, JointHistogram.class, false, null, true);
		case Lire.JPEG:
			return new GenericFastImageSearcher(maxResults, JpegCoefficientHistogram.class, false, null, true);
		case Lire.LUMINANCE:
			return new GenericFastImageSearcher(maxResults, LuminanceLayout.class, false, null, true);
		case Lire.OPPONENTHISTOGRAM:
			return new GenericFastImageSearcher(maxResults, OpponentHistogram.class, false, null, true);
		case Lire.PHOG:
			return new GenericFastImageSearcher(maxResults, PHOG.class, false, null, true);
		case Lire.AUTOCOLOR:
			return new GenericFastImageSearcher(maxResults, AutoColorCorrelogram.class, false, null, true);
		case Lire.MPEG7:
			return new GenericFastImageSearcher(maxResults, ColorLayout.class, false, null, true);
		case Lire.ACCID:
			return new GenericFastImageSearcher(maxResults, ACCID.class, false, null, true);
		case Lire.BINPATTERNSPYRAMID:
			return new GenericFastImageSearcher(maxResults, BinaryPatternsPyramid.class, false, null, true);
		case Lire.FUZZYOPPOHISTOGRAM:
			return new GenericFastImageSearcher(maxResults, FuzzyOpponentHistogram.class, false, null, true);
		case Lire.LOCALBINARYPATTERN:
			return new GenericFastImageSearcher(maxResults, LocalBinaryPatterns.class, false, null, true);
		case Lire.FUZZYCOLORHISTOGRAM:
			return new GenericFastImageSearcher(maxResults, FuzzyColorHistogram.class, false, null, true);
		case Lire.RANKOPPONENT:
			return new GenericFastImageSearcher(maxResults, RankAndOpponent.class, false, null, true);
		case Lire.ROTINVBINPATTERN:
			return new GenericFastImageSearcher(maxResults, RotationInvariantLocalBinaryPatterns.class, false, null,
					true);
		case Lire.SPACC:
			return new GenericFastImageSearcher(maxResults, SPACC.class, false, null, true);
		case Lire.SPATIALPYRAMIDCENTRIST:
			return new GenericFastImageSearcher(maxResults, SpatialPyramidCentrist.class, false, null, true);
		case Lire.SPCEDD:
			return new GenericFastImageSearcher(maxResults, SPCEDD.class, false, null, true);
		case Lire.SPFCTH:
			return new GenericFastImageSearcher(maxResults, SPFCTH.class, false, null, true);
		case Lire.SPJCD:
			return new GenericFastImageSearcher(maxResults, SPJCD.class, false, null, true);
		case Lire.SPLBP:
			return new GenericFastImageSearcher(maxResults, SPLBP.class, false, null, true);
		default:
			IAiService service = CoreActivator.getDefault().getAiService();
			if (service != null)
				for (Algorithm aiAlgorithm : service.getLireAlgorithms())
					if (method == aiAlgorithm.getId()) {
						@SuppressWarnings("unchecked")
						Class<? extends GlobalFeature> feature = (Class<? extends GlobalFeature>) service
								.getFeature(aiAlgorithm.getProviderId());
						if (feature != null)
							return new GenericFastImageSearcher(maxResults, feature, false, null, true);
					}
		}
		return null;
	}

	public ImageSearchHits search(IndexReader indexReader, BufferedImage image, int method, int maxResults)
			throws IOException {
		ImageSearcher imageSearcher = getImageSearcher(indexReader, method, maxResults);
		return imageSearcher == null ? null : imageSearcher.search(image, indexReader);
	}

}
