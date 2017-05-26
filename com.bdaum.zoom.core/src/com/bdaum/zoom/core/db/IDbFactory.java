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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.core.db;

import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.core.IPostProcessor2;
import com.bdaum.zoom.core.internal.lire.ILireService;
import com.bdaum.zoom.core.internal.lucene.ILuceneService;
import com.bdaum.zoom.core.internal.peer.IPeerService;

public interface IDbFactory {

	/**
	 * Creates a new database manager
	 *
	 * @param fileName
	 *            - database file name
	 * @param newDb
	 *            - true if a new database is created, false if an existing
	 *            database is opened
	 * @param readOnly
	 *            - true if the database is opened in read-only mode
	 * @param primary
	 *            - true if the database is opened as primary database
	 * @return - database manager
	 */
	IDbManager createDbManager(String fileName, boolean newDb, boolean readOnly, boolean primary);

	/**
	 * Sets the error handler to be used be the created database managers
	 *
	 * @param errorHandler
	 */
	void setErrorHandler(IDbErrorHandler errorHandler);

	/**
	 * Retrieves the current error handler
	 *
	 * @return - error handler
	 */
	IDbErrorHandler getErrorHandler();

	/**
	 * Creates a filter for rating
	 *
	 * @param newRating
	 *            - rating (-1 .. 5)
	 * @return filter
	 */
	IRatingFilter createRatingFilter(int newRating);

	/**
	 * Creates a filter for color codes
	 *
	 * @param colorCode
	 *            - colorCode
	 * @return filter
	 */
	IColorCodeFilter createColorCodeFilter(int colorCode);

	/**
	 * Creates a filter for file type and rating
	 *
	 * @param formats
	 *            - file types (@see ITypeAndRatingFilter)
	 * @return filter
	 */
	ITypeFilter createTypeFilter(int formats);

	/**
	 * Sets the tolerance values for searching the database
	 *
	 * @param prefs
	 *            - tolerance values of the syntax key1=float1\nkey2=float2\n...
	 */
	void setTolerances(String prefs);

	/**
	 * Returns the tolerance value for a given field key
	 *
	 * @param field
	 *            - field key
	 * @return - tolerance value
	 */
	float getTolerance(String field);

//	/**
//	 * Creates a text document builder for creating a free text index
//	 *
//	 * @return - the document builder
//	 */
//	DocumentBuilder getLuceneTextDocumentBuilder();
//
//	/**
//	 * Creates a Query parser for checking the correctness of free text queries
//	 *
//	 * @return - query parser
//	 */
////	QueryParser getLuceneQueryParser();
//
//	/**
//	 * Parses a Lucene query string
//	 * @param query - query string
//	 * @return - translated Lucene query
//	 * @throws ParseException in case of syntax error
//	 */
//	Query parseLuceneQuery(String query) throws ParseException;
//
//	/**
//	 * Creates a new Lucene Analyzer
//	 *
//	 * @return - Lucene analyzer
//	 */
////	StandardAnalyzer getLuceneAnalyzer();
//
//	/**
//	 * Creates a searcher for content based image retrieval
//	 *
//	 * @param method
//	 *            - algorithm index (see
//	 *            Constants.SupportedSimilarityAlgorithms)
//	 * @param maxResults
//	 *            - maximum results allowed
//	 * @return - image searcher or null
//	 */
//	ImageSearcher getContentSearcher(int method, int maxResults);
//
//	/**
//	 * Declares the CBIR algorithms used for indexing
//	 *
//	 * @param algorithms
//	 *            - algorithm IDs
//	 */
//	void configureCBIR(Set<String> algorithms);
//
//	/**
//	 * Declares the text fields used for indexing
//	 *
//	 * @param fields
//	 *            - text field (QueryField ids and Constant.INDEX_...)
//	 */
//	void configureTextIndex(Set<String> fields);
//
//	/**
//	 * Creates and returns a DocumentBuilder, which contains all available
//	 * features.
//	 *
//	 * @return a combination of all available features.
//	 */
//	DocumentBuilder constructFullDocumentBuilder();
//
//	/**
//	 * Sets the maximum size of the import list
//	 *
//	 * @param mx
//	 *            - maximum size of import list
//	 */
	void setMaxImports(int mx);

	/**
	 * Returns the maximum size of the import list
	 *
	 * @return maximum size of the import list
	 */
	public int getMaxImports();

//	/**
//	 * Returns a Lucene Index Writer Throws an IllegalStateException if an index
//	 * writer is currently in use Make sure to call a closeIndexWriter() in a
//	 * finally clause
//	 *
//	 * @param dir
//	 *            - directory of index
//	 * @param create
//	 *            - true if a new index shall be created
//	 * @return Lucene Index Writer
//	 * @throws CorruptIndexException
//	 * @throws LockObtainFailedException
//	 * @throws IOException
//	 * @throws IllegalStateException
//	 * @deprecated - use getIndexWriter(File)
//	 */
//	@Deprecated
//	IndexWriter getIndexWriter(Directory dir, boolean create)
//			throws CorruptIndexException, LockObtainFailedException,
//			IOException, IllegalStateException;
//
//	/**
//	 * Returns a Lucene Index Writer Throws an IllegalStateException if an index
//	 * writer is currently in use Make sure to call a releaseIndexWriter() in a
//	 * finally clause
//	 *
//	 * @param indexPath
//	 *            - path of index folder
//	 * @return - Lucene Index Writer
//	 * @throws CorruptIndexException
//	 * @throws LockObtainFailedException
//	 * @throws IOException
//	 */
//	IndexWriter getIndexWriter(File indexPath) throws CorruptIndexException,
//			LockObtainFailedException, IOException;
//
//	/**
//	 * Closes the index writer. Repeated invocation of this method has no
//	 * effect.
//	 *
//	 * @throws CorruptIndexException
//	 * @throws IOException
//	 * @deprecated - use releaseIndexWriter()
//	 */
//	@Deprecated
//	void closeIndexWriter() throws CorruptIndexException, IOException;
//
//	/**
//	 * Releases the index writer.
//	 *
//	 * @param indexPath
//	 *            - index path to which the writer belongs
//	 * @throws CorruptIndexException
//	 * @throws IOException
//	 */
//	void releaseIndexWriter(File indexPath) throws CorruptIndexException,
//			IOException;
//
//	/**
//	 * Commits all index writer changes
//	 *
//	 * @return current index writer
//	 * @throws CorruptIndexException
//	 * @throws IOException
//	 * @deprecated - use flushIndexWriter(File)
//	 */
//	@Deprecated
//	IndexWriter flushIndexWriter() throws CorruptIndexException, IOException;
//
//	/**
//	 * Commits all index writer changes
//	 *
//	 * @param indexPath
//	 *            - index path to which the writer belongs
//	 * @return current index writer
//	 * @throws CorruptIndexException
//	 * @throws IOException
//	 */
//	IndexWriter flushIndexWriter(File indexPath) throws CorruptIndexException,
//			IOException;
//
//	/**
//	 * Allocates and returns an index reader for the given index path Make sure
//	 * to call a corresponding releaseIndexReader() in a finally clause.
//	 *
//	 * @param indexPath
//	 * @return
//	 * @throws CorruptIndexException
//	 * @throws IOException
//	 */
//	IndexReader getIndexReader(File indexPath) throws CorruptIndexException,
//			IOException;
//
//	/**
//	 * Releases the specified index reader
//	 *
//	 * @param indexPath
//	 *            - index path to which the reader is belonging
//	 * @param reader
//	 *            - index reader to release
//	 */
//	void releaseIndexReader(File indexPath, IndexReader reader);
//
//	/**
//	 * Releases all index readers and writers
//	 */
//	void releaseAllIndexReadersAndWriters();


	/**
	 * Returns the peer service if the application runs in networked mode
	 *
	 * @return peer service or null
	 */
	IPeerService getPeerService();

	/**
	 * Returns the Lire service
	 * @param activate true to activate service if inactive
	 *
	 * @return Lire service (can be null if activate is false)
	 */
	ILireService getLireService(boolean activate);

	/**
	 * Returns the Lucene service
	 *
	 * @return Lucene service
	 */
	ILuceneService getLuceneService();

	/**
	 * Adds a listener for lifecycle events of databases
	 *
	 * @param listener
	 */
	void addDbListener(IDbListener listener);

	/**
	 * Removes a databas lifecycle listener
	 *
	 * @param listener
	 */
	void removeDbListener(IDbListener listener);

	/**
	 * Sets the Auto Coloring Post Processors
	 *
	 * @param autoColoringPostProcessors
	 */
	void setAutoColoringProcessors(IPostProcessor2[] autoColoringPostProcessors);

	/**
	 * Return  the Auto Coloring Post Processors
	 * @return Auto Coloring Post Processors
	 */
	IPostProcessor2[] getAutoColoringProcessors();

	/**
	 * Creates a query postprocessor for the given collection
	 * @param sm - collection
	 * @return query postprocessor
	 */
	IPostProcessor2 createQueryPostProcessor(SmartCollection sm);

	int getLireServiceVersion();


}
