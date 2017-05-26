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

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.CompositeReader;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.core.internal.lire.Algorithm;
import com.bdaum.zoom.core.internal.lucene.ParseException;
import com.bdaum.zoom.lal.internal.lire.Lire;
import com.bdaum.zoom.lal.internal.lire.TextDocumentBuilder;

public class LireActivator extends Plugin {

	private class IndexWriterEntry {
		IndexWriter writer;
		int count;

		public IndexWriterEntry(IndexWriter indexWriter, int count) {
			this.writer = indexWriter;
			this.count = count;
		}

		public IndexWriter flushIndexWriter() throws CorruptIndexException,
				IOException {
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
		Map<IndexReader, Integer> readerCounts = new HashMap<IndexReader, Integer>(
				8);
		List<IndexReader> obsoleteReaders = new LinkedList<IndexReader>();

		public IndexReaderEntry(IndexReader indexReader) {
			currentReader = indexReader;
			readerCounts.put(indexReader, 1);
		}

		public IndexReader getReader(File indexPath)
				throws CorruptIndexException, IOException {
			if (currentReader == null
					|| obsoleteReaders.contains(currentReader)) {
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
					// currentReader.close();
					readerCounts.remove(currentReader);
				} else
					obsoleteReaders.add(currentReader);
				currentReader = newReader;
				return currentReader;
			}
			Integer useCount = readerCounts.get(currentReader);
			readerCounts.put(currentReader, new Integer(useCount == null ? 1
					: useCount + 1));
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
			else
				readerCounts.put(indexReader,
						new Integer(Math.max(0, useCount - 1)));
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
					indexReaderMap.remove(reader);
				}
			}
		}

		public void invalidateAllReaders() {
			obsoleteReaders.addAll(readerCounts.keySet());
		}

		public void releaseAllReaders() {
			Iterator<IndexReader> iterator = readerCounts.keySet().iterator();
			while (iterator.hasNext()) {
				IndexReader indexReader = iterator.next();
				iterator.remove();
				releaseReader(indexReader);
			}
		}

	}

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bdaum.zoom.lireAndLucene"; //$NON-NLS-1$
	public static final String FIELD_NAME_FULL_TEXT = "descriptorFullText"; //$NON-NLS-1$

	private static LireActivator plugin;

	private IndexWriter indexWriter;
	private final Map<File, IndexWriterEntry> indexWriterMap = new HashMap<File, IndexWriterEntry>(
			8);
	private final Map<File, IndexReaderEntry> indexReaderMap = new HashMap<File, IndexReaderEntry>(
			8);
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
		if (reader instanceof CompositeReader)
			for (AtomicReaderContext context : ((CompositeReader) reader)
					.leaves())
				context.reader().close();
		reader.close();
	}

	public IndexWriter createIndexWriter(Directory dir, boolean create)
			throws IOException, LockObtainFailedException {
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46,
				getLuceneAnalyzer());
		config.setOpenMode(create ? IndexWriterConfig.OpenMode.CREATE
				: IndexWriterConfig.OpenMode.APPEND);
		return new IndexWriter(dir, config);
	}

	public StandardAnalyzer getLuceneAnalyzer() {
		if (analyzer == null)
			analyzer = new StandardAnalyzer(Version.LUCENE_46);
		return analyzer;
	}

	public IndexReader createBaseIndexReader(File indexPath) throws IOException {
		return DirectoryReader.open(SimpleFSDirectory.open(indexPath));
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
		return new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER,
				identifier, Field.Store.YES);
	}

	public IndexWriter flushIndexWriter() throws CorruptIndexException,
			IOException {
		synchronized (indexWriterMap) {
			if (indexWriter != null) {
				try {
					indexWriter.commit();
				} catch (OutOfMemoryError e) {
					try {
						indexWriter.close();
					} finally {
						indexWriter = createIndexWriter(
								indexWriter.getDirectory(), false);
					}
				}
			}
		}
		return indexWriter;
	}

	public IndexWriter flushIndexWriter(File indexPath)
			throws CorruptIndexException, IOException {
		synchronized (indexWriterMap) {
			IndexWriterEntry entry = indexWriterMap.get(indexPath);
			if (entry != null)
				return entry.flushIndexWriter();
		}
		return null;
	}

	public IndexWriter getIndexWriter(Directory dir, boolean create)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, IllegalStateException {
		synchronized (indexWriterMap) {
			if (indexWriter != null)
				throw new IllegalStateException(
						Messages.LireActivator_index_writer_in_use);
			indexWriter = createIndexWriter(dir, create);
		}
		return indexWriter;
	}

	public IndexWriter getIndexWriter(File indexPath)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		synchronized (indexWriterMap) {
			IndexWriterEntry entry = indexWriterMap.get(indexPath);
			if (entry == null) {
				Directory indexWriterDir = FSDirectory.open(indexPath);
				IndexWriter writer = createIndexWriter(indexWriterDir,
						!indexPath.exists());
				indexWriterMap.put(indexPath, new IndexWriterEntry(writer, 1));
				return writer;
			}
			++entry.count;
			return entry.writer;
		}
	}

	public void releaseIndexWriter(File indexPath, boolean force)
			throws CorruptIndexException, IOException {
		synchronized (indexWriterMap) {
			IndexWriterEntry entry = indexWriterMap.get(indexPath);
			if (entry != null) {
				if (--entry.count <= 0 || force) {
					try {
						entry.writer.close();
					} finally {
						indexWriterMap.remove(indexPath);
					}
				}
			}
		}
	}

	public IndexReader getIndexReader(File indexPath)
			throws CorruptIndexException, IOException {
		synchronized (indexReaderMap) {
			IndexReaderEntry entry = indexReaderMap.get(indexPath);
			if (entry == null) {
				IndexReader indexReader = createBaseIndexReader(indexPath);
				indexReaderMap
						.put(indexPath, new IndexReaderEntry(indexReader));
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
				logError(NLS.bind(Messages.LireActivator_corrupt_index,
						indexPath), e);
			} catch (IOException e) {
				logError(NLS.bind(Messages.LireActivator_io_error_reading,
						indexPath), e);
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

	public DocumentBuilder constructFullDocumentBuilder(
			DocumentBuilder... additions) {
		ChainedDocumentBuilder cdb = new ChainedDocumentBuilder();
		boolean mpeg7 = supportsAlgorithm(Lire.MPEG7);
		if (mpeg7 || supportsAlgorithm(Lire.SCALABLECOLOR)
				|| supportsAlgorithm(Lire.COLORONLY))
			cdb.addBuilder(DocumentBuilderFactory.getScalableColorBuilder());
		if (mpeg7 || supportsAlgorithm(Lire.COLORLAYOUT))
			cdb.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
		if (mpeg7 || supportsAlgorithm(Lire.EDGEHISTOGRAM))
			cdb.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
		if (mpeg7)
			cdb.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
		if (supportsAlgorithm(Lire.FCTH))
			cdb.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
		if (supportsAlgorithm(Lire.TAMURA))
			cdb.addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
		if (supportsAlgorithm(Lire.GABOR))
			cdb.addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
		if (supportsAlgorithm(Lire.COLORHISTOGRAM))
			cdb.addBuilder(DocumentBuilderFactory
					.getColorHistogramDocumentBuilder());
		if (supportsAlgorithm(Lire.JCD))
			cdb.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
		if (supportsAlgorithm(Lire.JOINTHISTOGRAM))
			cdb.addBuilder(DocumentBuilderFactory
					.getJointHistogramDocumentBuilder());
		if (supportsAlgorithm(Lire.JPEG))
			cdb.addBuilder(DocumentBuilderFactory
					.getJpegCoefficientHistogramDocumentBuilder());
		if (supportsAlgorithm(Lire.LUMINANCE))
			cdb.addBuilder(DocumentBuilderFactory
					.getLuminanceLayoutDocumentBuilder());
		if (supportsAlgorithm(Lire.OPPONENTHISTOGRAM))
			cdb.addBuilder(DocumentBuilderFactory
					.getOpponentHistogramDocumentBuilder());
		if (supportsAlgorithm(Lire.PHOG))
			cdb.addBuilder(DocumentBuilderFactory.getPHOGDocumentBuilder());
		if (supportsAlgorithm(Lire.AUTOCOLOR))
			cdb.addBuilder(DocumentBuilderFactory
					.getAutoColorCorrelogramDocumentBuilder());
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
			queryParser = new QueryParser(Version.LUCENE_46,
					FIELD_NAME_FULL_TEXT, getLuceneAnalyzer());
		try {
			return queryParser.parse(query);
		} catch (org.apache.lucene.queryparser.classic.ParseException e) {
			throw new ParseException(e);
		}
	}

	public void deleteDocuments(IndexWriter indexWriter, String assetId)
			throws IOException {
		indexWriter.deleteDocuments(new Term(
				DocumentBuilder.FIELD_NAME_IDENTIFIER, assetId));
	}

	public ImageSearchHits search(IndexReader indexReader, int docId,
			int method, int maxResults) throws IOException {
		return getImageSearcher(indexReader, method, maxResults).search(
				indexReader.document(docId), indexReader);
	}

	private ImageSearcher getImageSearcher(IndexReader indexReader, int method,
			int maxResults) {
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
			return ImageSearcherFactory
					.createScalableColorImageSearcher(maxResults);
		case Lire.EDGEHISTOGRAM:
			return ImageSearcherFactory
					.createEdgeHistogramImageSearcher(maxResults);
		case Lire.COLORLAYOUT:
			return ImageSearcherFactory
					.createColorLayoutImageSearcher(maxResults);
		case Lire.COLORONLY:
			return ImageSearcherFactory
					.createScalableColorImageSearcher(maxResults);
		case Lire.CEDD:
			return ImageSearcherFactory.createCEDDImageSearcher(maxResults);
		case Lire.FCTH:
			return ImageSearcherFactory.createFCTHImageSearcher(maxResults);
		case Lire.TAMURA:
			return ImageSearcherFactory.createTamuraImageSearcher(maxResults);
		case Lire.GABOR:
			return ImageSearcherFactory.createGaborImageSearcher(maxResults);
		case Lire.COLORHISTOGRAM:
			return ImageSearcherFactory
					.createColorHistogramImageSearcher(maxResults);
		case Lire.JCD:
			return ImageSearcherFactory.createJCDImageSearcher(maxResults);
		case Lire.JOINTHISTOGRAM:
			return ImageSearcherFactory
					.createJointHistogramImageSearcher(maxResults);
		case Lire.JPEG:
			return ImageSearcherFactory
					.createJpegCoefficientHistogramImageSearcher(maxResults);
		case Lire.LUMINANCE:
			return ImageSearcherFactory
					.createLuminanceLayoutImageSearcher(maxResults);
		case Lire.OPPONENTHISTOGRAM:
			return ImageSearcherFactory
					.createOpponentHistogramSearcher(maxResults);
		case Lire.PHOG:
			return ImageSearcherFactory.createPHOGImageSearcher(maxResults);
		case Lire.AUTOCOLOR:
			return ImageSearcherFactory
					.createAutoColorCorrelogramImageSearcher(maxResults);
		case Lire.MPEG7:
			return ImageSearcherFactory
					.createColorLayoutImageSearcher(maxResults);
		}
		return null;
	}

	public ImageSearchHits search(IndexReader indexReader, BufferedImage image,
			int method, int maxResults) throws IOException {
		ImageSearcher imageSearcher = getImageSearcher(indexReader, method,
				maxResults);
		return imageSearcher == null ? null : imageSearcher.search(image,
				indexReader);
	}


}
