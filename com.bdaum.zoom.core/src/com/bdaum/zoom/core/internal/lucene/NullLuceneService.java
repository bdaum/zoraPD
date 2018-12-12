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

public class NullLuceneService implements ILuceneService {

	public void invalidateAllReaders(File indexPath) {
		// do nothing
	}

	public void releaseAllIndexReadersAndWriters() {
		// do nothing
	}

	public void parseLuceneQuery(String query) throws ParseException {
		// do nothing
	}

	public void configureTextIndex(Collection<String> indexedTextFields) {
		// do nothing
	}

	public Object openIndexWriter(File indexPath) throws IOException {
		return null;
	}

	public void deleteIndexEntry(Object writerToken, String assetId) throws IOException {
		// do nothing
	}

	public void closeIndexWriter(Object writerToken, File indexPath) throws IOException {
		// do nothing
	}

	public void addDocument(Object writerToken, BufferedImage image, String assetid) throws IOException {
		// do nothing
	}

	public List<ScoredString> listTags(File indexPath, int maxItems) {
		return null;
	}

	@Override
	public ISearchHits search(File indexPath, SimilarityOptions_typeImpl options) throws IOException {
		return null;
	}

	@Override
	public ISearchHits search(File indexPath, TextSearchOptions_type options) throws IOException, ParseException {
		return null;
	}

}
