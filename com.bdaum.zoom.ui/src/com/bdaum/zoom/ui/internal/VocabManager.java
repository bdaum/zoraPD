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
 * (c) 2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;

public class VocabManager {

	public class VocabReader {

		private VocabNode root = new VocabNode(null, ""); //$NON-NLS-1$

		public VocabReader(File file) throws FileNotFoundException, IOException {
			initialize(new FileReader(file));
		}

		private void initialize(Reader reader) throws IOException {
			String line;
			VocabNode currentNode = root;
			int currentLevel = -1;
			int lineNo = 0;
			try (BufferedReader r = new BufferedReader(reader)) {
				while ((line = r.readLine()) != null) {
					++lineNo;
					int len = line.length();
					int level = 0;
					while ((level < len) && line.charAt(level) == '\t')
						level++;
					if (level > 0)
						line = line.substring(level, len);
					line = line.trim();
					if (line.isEmpty())
						continue;
					if (level > currentLevel) {
						if (level - currentLevel > 1)
							throw new IOException(NLS.bind(Messages.VocabManager_cannot_change_level,
									new Object[] { currentLevel, level, lineNo }));
						if (line.startsWith("{") && line.endsWith("}")) //$NON-NLS-1$ //$NON-NLS-2$
							currentNode.addSynonym(line.substring(1, line.length() - 1));
						else {
							VocabNode child = new VocabNode(currentNode, line);
							currentNode = child;
							++currentLevel;
						}
					} else {
						VocabNode parent = currentNode.getParent();
						while (currentLevel > level) {
							parent = parent.getParent();
							--currentLevel;
						}
						VocabNode child = new VocabNode(parent, line);
						currentNode = child;
					}
				}
			}
		}

		public VocabNode read() {
			return root;
		}

	}

	public static class VocabNode {

		private static final Object[] EMPTY = new Object[0];
		private VocabNode parent;
		private String label;
		private boolean category;
		private ArrayList<VocabNode> children;
		private ArrayList<String> synonyms;

		public VocabNode(VocabNode parent, String line) {
			this.parent = parent;
			if (parent != null)
				parent.addChild(this);
			if (line.startsWith("[") && line.endsWith("]")) { //$NON-NLS-1$ //$NON-NLS-2$
				label = line.startsWith("[~") ? line.substring(2, line.length() - 1).trim() //$NON-NLS-1$
						: line.substring(1, line.length() - 1).trim();
				category = true;
			} else
				label = line;
		}

		public void addChild(VocabNode child) {
			if (children == null)
				children = new ArrayList<>();
			children.add(child);
		}

		public void addSynonym(String syn) {
			if (synonyms == null)
				synonyms = new ArrayList<>();
			synonyms.add(syn);
		}

		public VocabNode getParent() {
			return parent;
		}

		public Object[] getChildren() {
			if (children != null)
				return children.toArray();
			return EMPTY;
		}

		public boolean hasChildren() {
			return children != null;
		}

		public String getLabel() {
			return label;
		}

		public boolean getCategory() {
			return category;
		}

		@Override
		public String toString() {
			if (synonyms == null)
				return label;
			StringBuilder sb = new StringBuilder();
			sb.append(label).append(" ; "); //$NON-NLS-1$
			Iterator<String> iterator = synonyms.iterator();
			while (iterator.hasNext()) {
				sb.append(iterator.next());
				if (iterator.hasNext())
					sb.append(", "); //$NON-NLS-1$
			}
			return sb.toString();
		}

		public void collect(Map<String, String> vocabMap) {
			if (!category) {
				vocabMap.put(label, label);
				if (synonyms != null)
					for (String syn : synonyms)
						vocabMap.put(syn, label);
			}
			if (children != null)
				for (VocabNode child : children)
					child.collect(vocabMap);
		}

		public void merge(VocabNode r) {
			mergeSynonyms(r);
			if (r.children == null)
				return;
			if (children == null) {
				children = r.children;
				return;
			}
			lp: for (VocabNode rchild : r.children) {
				for (VocabNode child : children) {
					if (child.label.equals(rchild.label)) {
						child.merge(rchild);
						continue lp;
					}
					if (child.synonyms != null)
						for (String syn : child.synonyms) {
							if (syn.equals(r.label)) {
								child.merge(rchild);
								continue lp;
							}
						}
					if (rchild.synonyms != null)
						for (String syn : rchild.synonyms) {
							if (syn.equals(label)) {
								child.merge(rchild);
								continue lp;
							}
						}
				}
				children.add(rchild);
				rchild.parent = this;
			}
		}

		private void mergeSynonyms(VocabNode r) {
			Set<String> set = new HashSet<>();
			if (synonyms != null)
				set.addAll(synonyms);
			set.add(r.label);
			if (r.synonyms != null)
				set.addAll(r.synonyms);
			set.remove(label);
			if (!set.isEmpty())
				synonyms = new ArrayList<>(set);
		}

	}

	private IAdaptable adaptable;
	private HashMap<String, String> vocabMap;
	private VocabNode combinedVocabRoot;
	private boolean treeReady;
	private boolean mapReady;
	private List<String> vocabularies;

	public VocabManager(List<String> vocabularies, IAdaptable adaptable) {
		this.vocabularies = vocabularies;
		this.adaptable = adaptable;
	}

	public Map<String, String> getVocabMap() {
		if (!mapReady) {
			VocabNode root = getVocabTree();
			if (root != null) {
				vocabMap = new HashMap<>();
				root.collect(vocabMap);
			}
			mapReady = true;
		}
		return vocabMap;
	}

	public VocabNode getVocabTree() {
		if (!treeReady) {
			if (vocabularies != null)
				for (String file : vocabularies) {
					File f = new File(file);
					try {
						VocabReader reader = new VocabReader(f);
						VocabNode r = reader.read();
						if (combinedVocabRoot == null)
							combinedVocabRoot = r;
						else
							combinedVocabRoot.merge(r);
					} catch (FileNotFoundException e) {
						AcousticMessageDialog.openError(adaptable.getAdapter(Shell.class),
								NLS.bind(Messages.VocabManager_error_reading, f),
								NLS.bind(Messages.VocabManager_file_not_exist, f));
					} catch (IOException e) {
						AcousticMessageDialog.openError(adaptable.getAdapter(Shell.class),
								NLS.bind(Messages.VocabManager_error_in_vocab, f.getName()), e.getMessage());
					}
				}
			treeReady = true;
		}
		return combinedVocabRoot;
	}

	public String getVocab(String element) {
		Map<String, String> map = getVocabMap();
		if (map == null)
			return element;
		return map.get(element);
	}

	public void reset(List<String> vocabularies) {
		this.vocabularies = vocabularies;
		vocabMap = null;
		combinedVocabRoot = null;
		treeReady = false;
		mapReady = false;
	}

}
