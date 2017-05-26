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
package com.bdaum.zoom.core.internal.lucene;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import com.bdaum.zoom.core.internal.ScoredString;
import com.bdaum.zoom.core.internal.lire.ISearchHits;

public interface ILuceneService {

	String INDEX_SLIDE_TITLE = "slide#title"; //$NON-NLS-1$
	String INDEX_SLIDE_DESCR = "slide#descr"; //$NON-NLS-1$
	String INDEX_EXH_TITLE = "exh#title"; //$NON-NLS-1$
	String INDEX_EXH_DESCR = "exh#descr"; //$NON-NLS-1$
	String INDEX_EXH_CREDITS = "exh#credits"; //$NON-NLS-1$
	String INDEX_WEBGAL_TITLE = "webgal#title"; //$NON-NLS-1$
	String INDEX_WEBGAL_DESCR = "webgal#descr"; //$NON-NLS-1$
	String INDEX_WEBGAL_ALT = "webgal#alt"; //$NON-NLS-1$
	String INDEX_PERSON_SHOWN = "person#shown"; //$NON-NLS-1$
	String INDEX_FILENAME = "file#name"; //$NON-NLS-1$

	void invalidateAllReaders(File indexPath);

	void releaseAllIndexReadersAndWriters();

	void parseLuceneQuery(String query) throws ParseException;

	void configureTextIndex(Collection<String> indexedTextFields);

	Object openIndexWriter(File indexPath) throws IOException;

	void deleteIndexEntry(Object writerToken, String assetId)
			throws IOException;

	void closeIndex(Object writerToken, File indexPath) throws IOException;

	void addDocument(Object writerToken, InputStream in, String assetid)
			throws IOException;

	Object getIndexReader(File indexPath) throws IOException;

	IDocumentIterator getDocumentIterator(Object readerToken, String assetId)
			throws IOException;

	ISearchHits search(Object reader, int docId, int method, int maxResults)
			throws IOException;

	ISearchHits search(Object reader, BufferedImage image, int method,
			int maxResults) throws IOException;

	void releaseIndexReader(File indexPath, Object reader);

	ISearchHits search(Object reader, String queryString, int maxResults) throws IOException, ParseException;

	List<ScoredString> listTags(File indexPath, int maxItems) throws IOException;

}
