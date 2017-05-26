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
package com.bdaum.zoom.lal.internal.lucene;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.util.BytesRef;

import com.bdaum.zoom.core.internal.ScoredString;
import com.bdaum.zoom.core.internal.lire.ISearchHits;
import com.bdaum.zoom.core.internal.lucene.IDocumentIterator;
import com.bdaum.zoom.core.internal.lucene.ILuceneService;
import com.bdaum.zoom.core.internal.lucene.ParseException;
import com.bdaum.zoom.lal.internal.LireActivator;

@SuppressWarnings("restriction")
public class Lucene implements ILuceneService {

	public final class SearchHits implements ISearchHits {
		private final ImageSearchHits hits;

		public SearchHits(ImageSearchHits hits) {
			this.hits = hits;
		}

		public float score(int position) {
			return hits.score(position);
		}

		public int length() {
			return hits.length();
		}

		public String getAssetId(int position) {
			return getFieldValue(hits.doc(position),
					DocumentBuilder.FIELD_NAME_IDENTIFIER);
		}

		public float getMaxScore() {
			// not used
			return 1.0f;
		}
	}

	private static int readerCount = 0;
	private static int writerCount = 0;

	private Hashtable<Object, IndexWriter> writerMap = new Hashtable<Object, IndexWriter>();
	private Hashtable<Object, IndexReader> readerMap = new Hashtable<Object, IndexReader>();
	private Hashtable<Object, IndexSearcher> searcherMap = new Hashtable<Object, IndexSearcher>();
	private Hashtable<Object, DocumentBuilder> builderMap = new Hashtable<Object, DocumentBuilder>();

	/**
	 * Called when service is activated.
	 */
	public void activate() {
		LireActivator.getDefault().logInfo(
				Messages.Lucene_lucene_service_started);
	}

	/**
	 * Called when service is deactivated.
	 */
	public void deactivate() {
		// do nothing
	}

	public IDocumentIterator getDocumentIterator(Object readerToken,
			String assetId) throws IOException {
		IndexReader indexReader = readerMap.get(readerToken);
		if (indexReader == null)
			return null;
		return new DocumentIterator(indexReader, new Term(
				DocumentBuilder.FIELD_NAME_IDENTIFIER, assetId));
	}

	public String getFieldValue(Document doc, String name) {
		return doc.get(name);
	}

	private static TopFieldDocs performSearch(IndexSearcher searcher, Query query,
			Filter filter, int n, Sort sort, boolean doDocScores,
			boolean doMaxScore) throws IOException {
		return searcher.search(query, filter, n, sort, doDocScores, doMaxScore);
	}

	public void releaseIndexWriter(File indexPath)
			throws CorruptIndexException, IOException {
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
			writerMap.put(key,
					LireActivator.getDefault().getIndexWriter(indexPath));
			return key;
		}
		return null;
	}

	public void deleteIndexEntry(Object writerToken, String assetId)
			throws IOException {
		IndexWriter indexWriter = writerMap.get(writerToken);
		if (indexWriter != null)
			LireActivator.getDefault().deleteDocuments(indexWriter, assetId);
	}

	public void closeIndex(Object writerToken, File indexPath)
			throws IOException {
		builderMap.remove(writerToken);
		IndexWriter indexWriter = writerMap.remove(writerToken);
		if (indexWriter != null)
			releaseIndexWriter(indexPath);
	}

	public void addDocument(Object writerToken, InputStream in, String assetid)
			throws IOException {
		try {
			IndexWriter indexWriter = writerMap.get(writerToken);
			if (indexWriter != null) {
				DocumentBuilder documentBuilder = builderMap.get(writerToken);
				if (documentBuilder == null) {
					documentBuilder = LireActivator.getDefault()
							.constructFullDocumentBuilder();
					builderMap.put(writerToken, documentBuilder);
				}
				indexWriter.addDocument(documentBuilder.createDocument(in,
						assetid));
			}
		} finally {
			in.close();
		}
	}

	public Object getIndexReader(File indexPath) throws IOException {
		if (indexPath != null) {
			Integer key = (++readerCount);
			readerMap.put(key,
					LireActivator.getDefault().getIndexReader(indexPath));
			return key;
		}
		return null;
	}

	public List<ScoredString> listTags(File indexPath, int maxItems)
			throws IOException {
		if (indexPath == null)
			return null;
		IndexReader indexReader = readerMap.get(getIndexReader(indexPath));
		List<ScoredString> result = new ArrayList<ScoredString>(1000);
		Terms terms = MultiFields.getTerms(indexReader,
				LireActivator.FIELD_NAME_FULL_TEXT);
		if (terms == null)
			return null;
		TermsEnum termEnum = terms.iterator(null);
		BytesRef bytesRef;
		while ((bytesRef = termEnum.next()) != null) {
			int freq = indexReader.docFreq(new Term(
					LireActivator.FIELD_NAME_FULL_TEXT, bytesRef));
			result.add(new ScoredString(bytesRef.utf8ToString(), freq));
		}
		Collections.sort(result);
		return (result.size() > maxItems) ? result.subList(0, maxItems)
				: result;
	}

	public ISearchHits search(Object readerToken, int docId, int method,
			int maxResults) throws IOException {
		IndexReader indexReader = readerMap.get(readerToken);
		if (indexReader != null) {
			ImageSearchHits hits = LireActivator.getDefault().search(
					indexReader, docId, method, maxResults);
			if (hits != null)
				return new SearchHits(hits);
		}
		return null;
	}

	public ISearchHits search(Object readerToken, BufferedImage image,
			int method, int maxResults) throws IOException {
		IndexReader indexReader = readerMap.get(readerToken);
		if (indexReader != null) {
			ImageSearchHits hits = LireActivator.getDefault().search(
					indexReader, image, method, maxResults);
			if (hits != null)
				return new SearchHits(hits);
		}
		return null;
	}

	public void releaseIndexReader(File indexPath, Object readerToken) {
		searcherMap.remove(readerToken);
		IndexReader indexReader = readerMap.remove(readerToken);
		if (indexReader != null)
			LireActivator.getDefault().releaseIndexReader(indexPath,
					indexReader);
	}

	public ISearchHits search(Object readerToken, String queryString,
			int maxResults) throws IOException, ParseException {
		final IndexReader indexReader = readerMap.get(readerToken);
		if (indexReader != null) {
			IndexSearcher searcher = searcherMap.get(readerToken);
			if (searcher == null) {
				searcher = new IndexSearcher(indexReader);
				searcherMap.put(readerToken, searcher);
			}
			final TopFieldDocs hits = performSearch(searcher, LireActivator
					.getDefault().parseQuery(queryString), null, maxResults,
					Sort.RELEVANCE, true, true);
			return new ISearchHits() {

				public float score(int position) {
					return hits.scoreDocs[position].score;
				}

				public int length() {
					return hits.scoreDocs.length;
				}

				public float getMaxScore() {
					return hits.getMaxScore();
				}

				public String getAssetId(int position) throws IOException {
					return getFieldValue(
							indexReader.document(hits.scoreDocs[position].doc),
							DocumentBuilder.FIELD_NAME_IDENTIFIER);
				}
			};
		}
		return null;
	}

}
