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
 * (c) 2014 Berthold Daum  
 */
package com.bdaum.zoom.core.internal.lucene;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.bdaum.zoom.cat.model.SimilarityOptions_typeImpl;
import com.bdaum.zoom.cat.model.TextSearchOptions_type;
import com.bdaum.zoom.core.internal.ScoredString;
import com.bdaum.zoom.core.internal.lire.ISearchHits;

public interface ILuceneService {

	/* IDs of text fields used for indexing */
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

	/**
	 * Invalidates all readers for the given index path
	 * @param indexPath
	 */
	void invalidateAllReaders(File indexPath);
	
	/**
	 * Releases all index readers and writers
	 */
	void releaseAllIndexReadersAndWriters();

	/**
	 * Parses a query and throws an exception in case of syntax errors
	 * @param query - the query to parse
	 * @throws ParseException
	 */
	void parseLuceneQuery(String query) throws ParseException;

	/**
	 * Configures the text search
	 * @param indexedTextFields - text field IDs (see above) used for indexing
	 */
	void configureTextIndex(Collection<String> indexedTextFields);

	/**
	 * Opens a new index writer
	 * @param indexPath - index path
	 * @return Index writer token for that path
	 * @throws IOException
	 */
	Object openIndexWriter(File indexPath) throws IOException;

	/**
	 * Deletes an index entry
	 * @param writerToken - index writer token
	 * @param assetId - asset ID of element to be deleted
	 * @throws IOException
	 */
	void deleteIndexEntry(Object writerToken, String assetId)
			throws IOException;

	/**
	 * Closes an index writer
	 * @param writerToken - index writer token
	 * @param indexPath - index path
	 * @throws IOException
	 */
	void closeIndexWriter(Object writerToken, File indexPath) throws IOException;

	/**
	 * Adds a new document to the index
	 * @param writerToken - index writer token
	 * @param image - thumbnail image
	 * @param assetid - asset ID of the new document
	 * @throws IOException
	 */
	void addDocument(Object writerToken, BufferedImage image, String assetid)
			throws IOException;

	/**
	 * List all tags for the given index
	 * @param indexPath - index path
	 * @param maxItems - maximum number of tags to be returned
	 * @return - list of scored tags
	 * @throws IOException
	 */
	List<ScoredString> listTags(File indexPath, int maxItems) throws IOException;


	/**
	 * Search for matching documents according to the specified similarity options
	 * @param indexPath - index path
	 * @param options - similarity options
	 * @return - search hits
	 * @throws IOException 
	 */
	ISearchHits search(File indexPath, SimilarityOptions_typeImpl options) throws IOException;


	/**
	 * Search for matching documents according to the specified text options
	 * @param indexPath - index path
	 * @param options - text options
	 * @return - search hits
	 * @throws IOException 
	 * @throws ParseException 
	 */
	ISearchHits search(File indexPath, TextSearchOptions_type options) throws IOException, ParseException;

}
