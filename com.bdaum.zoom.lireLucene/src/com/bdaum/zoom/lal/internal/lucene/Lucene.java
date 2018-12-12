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
 * (c) 2014-2018 Berthold Daum  
 */
package com.bdaum.zoom.lal.internal.lucene;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.util.BytesRef;

import com.bdaum.zoom.cat.model.SimilarityOptions_typeImpl;
import com.bdaum.zoom.cat.model.TextSearchOptions_type;
import com.bdaum.zoom.core.internal.ScoredString;
import com.bdaum.zoom.core.internal.lire.ISearchHits;
import com.bdaum.zoom.core.internal.lucene.ILuceneService;
import com.bdaum.zoom.core.internal.lucene.ParseException;
import com.bdaum.zoom.lal.internal.LireActivator;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.searchers.ImageSearchHits;

@SuppressWarnings("restriction")
public class Lucene implements ILuceneService {

	public final class SearchHits implements ISearchHits {
		private ImageSearchHits hits;
		private TopFieldDocs topdocs;
		private IndexReader reader;

		public SearchHits(IndexReader reader, ImageSearchHits hits) {
			this.reader = reader;
			this.hits = hits;
		}

		public SearchHits(IndexReader reader, TopFieldDocs topdocs) {
			this.topdocs = topdocs;
			this.reader = reader;
		}

		public float score(int position) {
			return hits != null ? (float) hits.score(position) : topdocs.scoreDocs[position].score;
		}

		public int length() {
			return hits != null ? hits.length() : topdocs.scoreDocs.length;
		}

		public String getAssetId(int position) throws IOException {
			return reader
					.document(hits != null ? hits.documentID(position) : topdocs.scoreDocs[position].doc, fieldsToLoad)
					.get(DocumentBuilder.FIELD_NAME_IDENTIFIER);
		}

		public float getMaxScore() {
			return hits != null ? 1.0f : topdocs.getMaxScore();
		}
	}

	private static int readerCount = 0;
	private static int writerCount = 0;

	private static Set<String> fieldsToLoad = Collections.singleton(DocumentBuilder.FIELD_NAME_IDENTIFIER);

	private Hashtable<Object, IndexWriter> writerMap = new Hashtable<Object, IndexWriter>();
	private Hashtable<Object, IndexReader> readerMap = new Hashtable<Object, IndexReader>();
	private Hashtable<Object, IndexSearcher> searcherMap = new Hashtable<Object, IndexSearcher>();
	private Hashtable<Object, DocumentBuilder> builderMap = new Hashtable<Object, DocumentBuilder>();

	/**
	 * Called when service is activated.
	 */
	public void activate() {
		LireActivator.getDefault().logInfo(Messages.Lucene_lucene_service_started);
	}

	/**
	 * Called when service is deactivated.
	 */
	public void deactivate() {
		// do nothing
	}

	private static TopFieldDocs performSearch(IndexSearcher searcher, Query query, int n, Sort sort,
			boolean doDocScores, boolean doMaxScore) throws IOException {
		return searcher.search(query, n, sort, doDocScores, doMaxScore);
	}

	private static void releaseIndexWriter(File indexPath) throws CorruptIndexException, IOException {
		LireActivator.getDefault().releaseIndexWriter(indexPath, false);
	}

	public void invalidateAllReaders(File indexPath) {
		LireActivator.getDefault().invalidateAllReaders(indexPath);
	}

	public void releaseAllIndexReadersAndWriters() {
		LireActivator.getDefault().releaseAllIndexReadersAndWriters();
	}

	public void parseLuceneQuery(String query) throws ParseException {
		LireActivator.getDefault().parseQuery(query);
	}

	public void configureTextIndex(Collection<String> indexedTextFields) {
		LireActivator.getDefault().setTextFields(indexedTextFields);
	}

	public Object openIndexWriter(File indexPath) throws IOException {
		if (indexPath != null) {
			Integer key = (++writerCount);
			writerMap.put(key, LireActivator.getDefault().getIndexWriter(indexPath));
			return key;
		}
		return null;
	}

	public void deleteIndexEntry(Object writerToken, String assetId) throws IOException {
		IndexWriter indexWriter = writerMap.get(writerToken);
		if (indexWriter != null)
			LireActivator.getDefault().deleteDocuments(indexWriter, assetId);
	}

	public void closeIndexWriter(Object writerToken, File indexPath) throws IOException {
		builderMap.remove(writerToken);
		IndexWriter indexWriter = writerMap.remove(writerToken);
		if (indexWriter != null)
			releaseIndexWriter(indexPath);
	}

	public void addDocument(Object writerToken, BufferedImage image, String assetid) throws IOException {
		IndexWriter indexWriter = writerMap.get(writerToken);
		if (indexWriter != null) {
			DocumentBuilder documentBuilder = builderMap.get(writerToken);
			if (documentBuilder == null)
				builderMap.put(writerToken,
						documentBuilder = LireActivator.getDefault().constructFullDocumentBuilder());
			indexWriter.addDocument(documentBuilder.createDocument(image, assetid));
		}
	}

	private Object getIndexReaderToken(File indexPath) throws IOException {
		if (indexPath != null) {
			Integer key = ++readerCount;
			readerMap.put(key, LireActivator.getDefault().getIndexReader(indexPath));
			return key;
		}
		return null;
	}

	public List<ScoredString> listTags(File indexPath, int maxItems) throws IOException {
		Object readerToken = null;
		try {
			readerToken = indexPath == null ? null : getIndexReaderToken(indexPath);
			if (readerToken != null) {
				IndexReader indexReader = readerMap.get(readerToken);
				if (indexReader != null) {
					List<ScoredString> result = new ArrayList<ScoredString>(1000);
					Terms terms = MultiFields.getTerms(indexReader, LireActivator.FIELD_NAME_FULL_TEXT);
					if (terms == null)
						return null;
					TermsEnum termEnum = terms.iterator();
					BytesRef bytesRef;
					while ((bytesRef = termEnum.next()) != null)
						result.add(new ScoredString(bytesRef.utf8ToString(),
								indexReader.docFreq(new Term(LireActivator.FIELD_NAME_FULL_TEXT, bytesRef))));
					Collections.sort(result);
					return (result.size() > maxItems) ? result.subList(0, maxItems) : result;
				}
			}
			return null;
		} finally {
			if (readerToken != null)
				releaseIndexReader(indexPath, readerToken);
		}
	}

	private void releaseIndexReader(File indexPath, Object readerToken) {
		searcherMap.remove(readerToken);
		IndexReader indexReader = readerMap.remove(readerToken);
		if (indexReader != null)
			LireActivator.getDefault().releaseIndexReader(indexPath, indexReader);
	}

	@Override
	public ISearchHits search(File indexPath, SimilarityOptions_typeImpl options) throws IOException {
		Object readerToken = null;
		try {
			ISearchHits hits = null;
			readerToken = getIndexReaderToken(indexPath);
			IndexReader indexReader = readerToken != null ? readerMap.get(readerToken) : null;
			if (indexReader != null) {
				String assetId = options.getAssetId();
				if (assetId != null) {
					Document document = null;
					try {
						document = getDocumentById(indexReader, assetId);
					} catch (AlreadyClosedException e) {
						releaseIndexReader(indexPath, readerToken);
						readerToken = getIndexReaderToken(indexPath);
						if (readerToken != null) {
							indexReader = readerMap.get(readerToken);
							if (indexReader != null)
								document = getDocumentById(indexReader, assetId);
						}
					}
					if (document != null) {
						ImageSearchHits iHits = LireActivator.getDefault().search(indexReader, document,
								options.getMethod(), options.getMaxResults());
						if (iHits != null)
							hits = new SearchHits(indexReader, iHits);
					}
				}
				if (hits == null) {
					byte[] pngImage = options.getPngImage();
					if (pngImage != null) {
						// Search for similar images
						ImageSearchHits iHits = LireActivator.getDefault().search(indexReader,
								ImageIO.read(new ByteArrayInputStream(pngImage)), options.getMethod(),
								options.getMaxResults());
						if (iHits != null)
							hits = new SearchHits(indexReader, iHits);
					}
				}
			}
			return hits;
		} finally {
			if (readerToken != null)
				releaseIndexReader(indexPath, readerToken);
		}
	}

	private static Document getDocumentById(IndexReader indexReader, String searchString) throws IOException {
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		Analyzer analyzer = new KeywordAnalyzer();
		QueryParser queryParser = new QueryParser(DocumentBuilder.FIELD_NAME_IDENTIFIER, analyzer);
		try {
			Query query = queryParser.parse(searchString);
			TopDocs topdocs = indexSearcher.search(query, 1);
			if (topdocs.totalHits > 0)
				return indexReader.document(topdocs.scoreDocs[0].doc);
		} catch (org.apache.lucene.queryparser.classic.ParseException e) {
			// should never happen
		}
		return null;
	}

	@Override
	public ISearchHits search(File indexPath, TextSearchOptions_type options) throws IOException, ParseException {
		Object readerToken = null;
		try {
			if (indexPath != null) {
				readerToken = getIndexReaderToken(indexPath);
				if (readerToken != null) {
					IndexReader indexReader = readerMap.get(readerToken);
					if (indexReader != null) {
						IndexSearcher searcher = searcherMap.get(readerToken);
						if (searcher == null)
							searcherMap.put(readerToken, searcher = new IndexSearcher(indexReader));
						return new SearchHits(indexReader,
								performSearch(searcher, LireActivator.getDefault().parseQuery(options.getQueryString()),
										options.getMaxResults(), Sort.RELEVANCE, true, true));
					}
				}
			}
			return null;
		} finally {
			if (readerToken != null)
				releaseIndexReader(indexPath, readerToken);
		}
	}

}
