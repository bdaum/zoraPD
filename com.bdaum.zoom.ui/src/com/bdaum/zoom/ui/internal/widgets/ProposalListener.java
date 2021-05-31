package com.bdaum.zoom.ui.internal.widgets;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.UiUtilities;

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
public class ProposalListener implements Listener {
	private String lastProposal;
	private String[] proposals;
	private Point pnt = new Point(0, 0);

	public ProposalListener(String[] proposals) {
		this.proposals = proposals;
	}

	private void verifyText(Event e) {
		if (e.character == SWT.BS || e.character == SWT.DEL)
			return;
		Text textField = (Text) e.widget;
		int start = e.start;
		int end = e.end;
		String insert = e.text;
		String text = textField.getText();
		if (start != end) {
			if (end - start > insert.length()) {
				int kwstart = getWordStart(start, text);
				String orig = text.substring(kwstart, start);
				if (!orig.isEmpty()) {
					String keyword = findMatchingKeyword(orig);
					lastProposal = text.substring(0, kwstart) + keyword;
					keyword = orig + keyword.substring(orig.length());
					int kwend = getWordEnd(start, text);
					StringBuilder sb = new StringBuilder(text);
					sb.replace(kwstart, kwend, keyword);
					textField.removeListener(SWT.Verify, this);
					textField.setText(sb.toString());
					pnt.x = pnt.y = start;
					textField.setSelection(pnt);
					textField.addListener(SWT.Verify, this);
					e.doit = false;
					return;
				}
			}
			lastProposal = null;
			return;
		}
		StringBuilder sb = new StringBuilder(text);
		sb.setLength(start);
		sb.append(insert);
		if (lastProposal == null || start >= lastProposal.length() || start + insert.length() > text.length()) {
			sb.append(text.substring(end));
			lastProposal = null;
		}
		text = sb.toString();
		int kwstart = getWordStart(start + insert.length(), text);
		String orig = text.substring(kwstart, start + insert.length());
		if (!orig.isEmpty()) {
			String keyword = findMatchingKeyword(orig);
			e.doit = false;
			if (keyword != null) {
				textField.removeListener(SWT.Verify, this);
				lastProposal = text.substring(0, kwstart) + keyword;
				keyword = orig + keyword.substring(orig.length());
				textField.setText(text.substring(0, kwstart) + keyword);
				pnt.x = pnt.y = start + insert.length();
				textField.setSelection(pnt);
				textField.addListener(SWT.Verify, this);
			} else {
				lastProposal = null;
				Ui.getUi().playSound("warning", null); //$NON-NLS-1$
			}
		} else {
			if (lastProposal != null && text.toLowerCase().startsWith(lastProposal.toLowerCase()))
				text = lastProposal + text.substring(lastProposal.length());
			textField.removeListener(SWT.Verify, this);
			textField.setText(text);
			pnt.x = pnt.y = text.length();
			textField.setSelection(pnt);
			textField.addListener(SWT.Verify, this);
			e.doit = false;
		}
	}

	private String findMatchingKeyword(String orig) {
		int index = Arrays.binarySearch(proposals, orig, UiUtilities.stringComparator);
		if (index >= 0)
			return proposals[index];
		int ins = -index - 1;
		if (ins < proposals.length && proposals[ins].toLowerCase().startsWith(orig.toLowerCase()))
			return proposals[ins];
		if (ins > 0 && proposals[ins - 1].toLowerCase().startsWith(orig.toLowerCase()))
			return proposals[ins - 1];
		return null;
	}

	private static int getWordStart(int pos, String text) {
		for (int i = pos - 1; i >= 0; i--) {
			char c = text.charAt(i);
			if (c == ' ' || (i > 0 && (c == '-' || c == '+') && text.charAt(i - 1) == ' '))
				return i + 1;
		}
		return 0;
	}

	private static int getWordEnd(int pos, String text) {
		for (int i = pos; i < text.length(); i++)
			if (text.charAt(i) == ' ')
				return i;
		return text.length();
	}

	private void keyPressed(Event e) {
		int keyCode = e.keyCode;
		if (keyCode == SWT.ARROW_UP || keyCode == SWT.ARROW_DOWN || keyCode == SWT.PAGE_DOWN
				|| keyCode == SWT.PAGE_UP) {
			Text textField = (Text) e.widget;
			String orig = textField.getText();
			int index = Arrays.binarySearch(proposals, orig);
			String text = null;
			if ((keyCode == SWT.PAGE_UP || orig.isEmpty()) && proposals.length > 0)
				text = proposals[0];
			else if (keyCode == SWT.PAGE_DOWN && proposals.length > 0)
				text = proposals[proposals.length - 1];
			else if (keyCode == SWT.ARROW_UP && index > 0)
				text = proposals[index - 1];
			else if (keyCode == SWT.ARROW_DOWN && index >= 0 && index < proposals.length - 1)
				text = proposals[index + 1];
			if (text != null) {
				textField.removeListener(SWT.Verify, this);
				textField.setText(text);
				pnt.x = pnt.y = 0;
				textField.setSelection(pnt);
				textField.addListener(SWT.Verify, this);
			}
			e.doit = false;
		}
	}

	public void addTo(Text textField) {
		textField.addListener(SWT.Verify, this);
		textField.addListener(SWT.KeyDown, this);
	}

	public void removeFrom(Text textField) {
		textField.removeListener(SWT.Verify,this);
		textField.removeListener(SWT.KeyDown, this);
	}

	public String validate(String text) {
		return text.equalsIgnoreCase(findMatchingKeyword(text)) ? null : Messages.ProposalListener_not_valid;
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Verify:
			verifyText(e);
			return;
		case SWT.KeyDown:
			keyPressed(e);
		}
		
	}
}