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

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.Bits;

import com.bdaum.zoom.core.internal.lucene.IDocumentIterator;

public class DocumentIterator implements IDocumentIterator{

	private final Term term;
	private Iterator<AtomicReaderContext> leavesIterator;
	private DocsEnum termDocsEnum;
	private Bits liveDocs;
	private AtomicReader atomicReader;

	public DocumentIterator(IndexReader reader, Term term) {
		this.term = term;
		if (reader instanceof DirectoryReader)
			leavesIterator = ((DirectoryReader) reader).leaves().iterator();
	}

	public int next() throws IOException {
		if (leavesIterator != null) {
			while (true) {
				if (termDocsEnum != null) {
					while (true) {
						int docId = termDocsEnum.nextDoc();
						if (docId == NO_MORE_DOCS)
							break; // Continue with next reader
						if (!atomicReader.hasDeletions() || liveDocs.get(docId))
							return docId; // valid document
					}
				}
				if (!leavesIterator.hasNext())
					return NO_MORE_DOCS;
				atomicReader = leavesIterator.next().reader();
				termDocsEnum = atomicReader.termDocsEnum(term);
				liveDocs = MultiFields.getLiveDocs(atomicReader);
			}
		}
		return NO_MORE_DOCS;
	}

	public void close() throws IOException {
		atomicReader = null;
		liveDocs = null;
		termDocsEnum = null;
	}
}
