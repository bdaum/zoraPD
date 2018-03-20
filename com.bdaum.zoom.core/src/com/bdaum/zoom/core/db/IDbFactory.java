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
 * (c) 2009-2018 Berthold Daum  
 */

package com.bdaum.zoom.core.db;

import java.util.List;

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


	/**
	 * Sets the maximum size of the import list
	 *
	 * @param mx
	 *            - maximum size of import list
	 */
	void setMaxImports(int mx);

	/**
	 * Returns the maximum size of the import list
	 *
	 * @return maximum size of the import list
	 */
	public int getMaxImports();

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

	/**
	 * Returns the version of the Lire service
	 * @return - Lire service version
	 */
	int getLireServiceVersion();

	/**
	 * Sets the indexed fields to be used by the database manager
	 * @param fields - A string containing the keys of the indexed QueryFields separated by newline character
	 */
	void setIndexedFields(String fields);

	/**
	 * Returns the set of indexed fields
	 * @return set of indexed field keys
	 */
	List<String> getIndexedFields();

	/**
	 * returns the unit for distances
	 * @return 'k' for kilometers, 'm' for miles, 'n' for nautical miles
	 */
	char getDistanceUnit();

	/**
	 * sets the unit for distances
	 * @param unit - "k" for kilometers, "m" for miles, "n" for nautical miles
	 */
	void setDistanceUnit(String unit);
	
	/**
	 * returns the unit for dimensions
	 * @return 'c' for centimeters, 'i' for inches
	 */
	char getDimUnit();

	/**
	 * sets the unit for dimensions
	 * @param unit - "c" for centimeters, "i" for inches
	 */

	void setDimUnit(String unit);

}
