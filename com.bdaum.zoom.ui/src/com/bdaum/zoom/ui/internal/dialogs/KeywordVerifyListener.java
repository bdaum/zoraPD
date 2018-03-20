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
 * (c) 2009-2013 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.Arrays;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

import com.bdaum.zoom.core.internal.Utilities;

@SuppressWarnings("restriction")
public class KeywordVerifyListener implements VerifyListener {

	private int lastOrigLength = -1;
	private String lastProposal;
	private Object reveal = null;
	private StyledText field;

	private String[] sortedKeywords;

	private final CheckboxTreeViewer viewer;

	public KeywordVerifyListener(CheckboxTreeViewer viewer) {
		this.viewer = viewer;
	}

	public KeywordVerifyListener() {
		this(null);
	}

	public void setKeywords(String[] keywords) {
		sortedKeywords = keywords;
		Arrays.sort(keywords, Utilities.KEYWORDCOMPARATOR);
	}

	public void verifyText(VerifyEvent e) {
		reveal = null;
		String insert = e.text;
		field = (StyledText) e.widget;
		if (insert.indexOf("\n") >= 0 && lastProposal != null && lastProposal.length() >= e.start) { //$NON-NLS-1$
			field.removeVerifyListener(this);
			field.setText(lastProposal);
			field.setSelection(lastProposal.length());
			field.addVerifyListener(this);
			lastProposal = null;
			e.doit = false;
			return;
		}
		if (insert.isEmpty() & e.end - e.start == 1 && lastProposal != null && lastProposal.length() > e.start) {
			String text = field.getText();
			text = (e.end == lastOrigLength) ? text.substring(0, e.start)
					: text.substring(0, e.start) + text.substring(lastProposal.length());
			field.removeVerifyListener(this);
			field.setText(text);
			field.setSelection(e.start);
			field.addVerifyListener(this);
			lastProposal = null;
			lastOrigLength = -1;
			e.doit = false;
			return;
		}
		if (insert.indexOf(',') >= 0 || insert.indexOf(';') >= 0)
			e.doit = false;
		else if (!insert.isEmpty())
			computeProposal(e);
		if (reveal != null && viewer != null)
			viewer.reveal(reveal);
		if (e.doit) {
			field.removeVerifyListener(this);
			StringBuilder sb = new StringBuilder(field.getText());
			sb.replace(e.start, e.end, e.text);
			field.setText(sb.toString());
			field.setSelection(e.start + e.text.length());
			field.addVerifyListener(this);
			e.doit = false;
		}
	}

	private void computeProposal(VerifyEvent e) {
		int start = e.start;
		int end = e.end;
		if (start != end) {
			lastProposal = null;
			return;
		}
		String insert = e.text;
		String text = field.getText();
		if (lastProposal != null && start < lastProposal.length() && start + insert.length() <= text.length())
			text = text.substring(0, start) + insert + text.substring(start + insert.length());
		else {
			text = text.substring(0, start) + insert + text.substring(end);
			lastProposal = null;
		}
		int kwstart = findWordStart(start + insert.length(), text);
		String orig = text.substring(kwstart, start + insert.length());
		if (!orig.isEmpty()) {
			Object keyword = findMatchingKeyword(orig);
			reveal = keyword;
			if (keyword == null) {
				keyword = orig;
				lastProposal = text.substring(0, kwstart) + keyword;
			} else {
				lastProposal = text.substring(0, kwstart) + keyword;
				keyword = orig + keyword.toString().substring(orig.length());
			}
			field.removeVerifyListener(this);
			field.setText(text.substring(0, kwstart) + keyword);
			field.setSelection(start + insert.length());
			field.addVerifyListener(this);
			lastOrigLength = orig.length();
			e.doit = false;
			return;
		}
	}

	private static int findWordStart(int pos, String text) {
		for (int i = pos - 1; i >= 0; i--) {
			char c = text.charAt(i);
			if (c == '\n' || c == '\r')
				return i + 1;
		}
		return 0;
	}

	private Object findMatchingKeyword(String orig) {
		int index = Arrays.binarySearch(sortedKeywords, orig, Utilities.KEYWORDCOMPARATOR);
		if (index >= 0)
			return sortedKeywords[index];
		int ins = -index - 1;
		if (ins < sortedKeywords.length && sortedKeywords[ins].toString().toLowerCase().startsWith(orig.toLowerCase()))
			return sortedKeywords[ins];
		else if (ins > 0 && sortedKeywords[ins - 1].toString().toLowerCase().startsWith(orig.toLowerCase()))
			return sortedKeywords[ins - 1];
		return null;
	}

}
