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
 * (c) 2009-2013 Berthold Daum  (berthold.daum@bdaum.de)
 */

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
 * (c) 2009-2013 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.Arrays;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;

public class KeywordSearchDialog extends ZTitleAreaDialog implements VerifyListener, KeyListener {
	private static final String SETTINGSID = "com.bdaum.zoom.keywordSearchDialog"; //$NON-NLS-1$
	private static final String HISTORY = "history"; //$NON-NLS-1$

	private static final String NOT = Messages.KeywordSearchDialog_not_operator;
	private static final String AND = Messages.KeywordSearchDialog_and_operator;
	private static final String OR = Messages.KeywordSearchDialog_or_operator;
	private static final String NOTSEP = ' ' + NOT + ' ';
	private static final String ANDSEP = ' ' + AND + ' ';
	private static final String ORSEP = ' ' + OR + ' ';
	private static Point pnt = new Point(0, 0);
	private SmartCollection result;
	private Combo combo;
	private String[] keywords;
	private FindWithinGroup findWithinGroup;
	private FindInNetworkGroup findInNetworkGroup;
	private IDialogSettings settings;
	private String lastProposal;
	private String text;

	public KeywordSearchDialog(Shell parentShell, String text) {
		super(parentShell, HelpContextIds.KEYWORD_SEARCH_DIALOG);
		this.text = text;
	}

	private void setKeywords() {
		keywords = UiUtilities.getValueProposals(dbManager, QueryField.IPTC_KEYWORDS, null,
				findInNetworkGroup != null && findInNetworkGroup.getSelection());
	}

	@Override
	public void create() {
		super.create();
		fillValues();
		setKeywords();
		if (text != null)
			combo.setText(text);
		setTitle(Messages.KeywordSearchDialog_keyword_search);
		setMessage(Messages.KeywordSearchDialog_specify_keywords);
		getShell().layout();
		getShell().pack();
		updateButtons();
	}

	private void updateButtons() {
		boolean valid = validate();
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			getShell().setModified(valid);
			okButton.setEnabled(valid);
		}
	}

	private boolean validate() {
		String text = combo.getText();
		String errorMessage = text.isEmpty() ? Messages.KeywordSearchDialog_please_enter_keywords
				: validate(text, keywords);
		setErrorMessage(errorMessage);
		return errorMessage == null;
	}

	public static String validate(String s, String[] keywords) {
		boolean op = true;
		int p = 0;
		while (true) {
			while (p < s.length() && s.charAt(p) == ' ')
				++p;
			int q = getNextToken(s, p);
			String token = q < 0 ? s.substring(p).trim() : s.substring(p, q).trim();
			if (OR.equals(token) || NOT.equals(token) || AND.equals(token)) {
				if (op)
					return Messages.KeywordSearchDialog_bad_keyword;
				op = true;
			} else {
				if (!op)
					return Messages.KeywordSearchDialog_bad_keyword;
				String keyword = findMatchingKeyword(token, keywords);
				if (!token.equals(keyword))
					return NLS.bind(Messages.KeywordSearchDialog_keyword_does_not_exist, token);
				op = false;
			}
			if (q < 0)
				break;
			p += token.length();
		}
		if (op)
			return Messages.KeywordSearchDialog_bad_keyword;
		return null;
	}

	private static int getNextToken(String s, int p) {
		int q = s.indexOf(OR, p);
		if (q == p)
			return p + OR.length();
		q = s.indexOf(AND, p);
		if (q == p)
			return p + AND.length();
		q = s.indexOf(NOT, p);
		if (q == p)
			return p + NOT.length();
		q = s.indexOf(ORSEP, p);
		int q1 = s.indexOf(ANDSEP, p);
		int q2 = s.indexOf(NOTSEP, p);
		if (q < 0 || q1 >= 0 && q1 < q)
			q = q1;
		if (q < 0 || q2 >= 0 && q2 < q)
			q = q2;
		return q;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		settings = UiActivator.getDefault().getDialogSettings(SETTINGSID);
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite composite = new Composite(area, SWT.NONE);
		final GridData gd_composite = new GridData();
		gd_composite.verticalIndent = 15;
		composite.setLayoutData(gd_composite);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 10;
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);
		findWithinGroup = new FindWithinGroup(composite);
		if (Core.getCore().isNetworked()) {
			findInNetworkGroup = new FindInNetworkGroup(composite);
			findInNetworkGroup.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setKeywords();
				}
			});
		}
		new Label(composite, SWT.NONE).setText(Messages.KeywordSearchDialog_keywords);
		combo = new Combo(composite, SWT.NONE);
		String[] items = settings.getArray(HISTORY);
		if (items == null)
			items = new String[0];
		combo.setItems(items);
		if (combo.getItemCount() > 0)
			combo.setText(combo.getItem(0));
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		});
		combo.addVerifyListener(this);
		combo.addKeyListener(this);
		final GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_combo.widthHint = 350;
		combo.setLayoutData(gd_combo);
		combo.setFocus();
		return area;
	}

	private void fillValues() {
		findWithinGroup.fillValues(settings);
		if (findInNetworkGroup != null)
			findInNetworkGroup.fillValues(settings);
	}

	@Override
	protected void okPressed() {
		settings.put(HISTORY, UiUtilities.updateComboHistory(combo));
		boolean network = findInNetworkGroup == null ? false : findInNetworkGroup.getSelection();
		if (findInNetworkGroup != null)
			findInNetworkGroup.saveValues(settings);
		result = computeQuery(combo.getText(), network, findWithinGroup.getParentCollection());
		super.okPressed();
	}

	public static SmartCollection computeQuery(String searchString, boolean network, SmartCollection parent) {
		SmartCollectionImpl sm = new SmartCollectionImpl(searchString, false, false, true, network, null, 0, null, 0,
				null, Constants.INHERIT_LABEL, null, 0, null);
		boolean neg = false;
		boolean and = false;
		int p = 0;
		while (true) {
			while (p < searchString.length() && searchString.charAt(p) == ' ')
				++p;
			int q = getNextToken(searchString, p);
			String token = q < 0 ? searchString.substring(p).trim() : searchString.substring(p, q).trim();
			if (OR.equals(token)) {
				neg = false;
				and = false;
			} else if (AND.equals(token)) {
				neg = false;
				and = true;
			} else if (NOT.equals(token)) {
				neg = true;
				and = true;
			} else {
				sm.addCriterion(new CriterionImpl(QueryField.IPTC_KEYWORDS.getKey(), null, token,
						(neg) ? QueryField.NOTEQUAL : QueryField.EQUALS, and));
				neg = false;
				and = false;
			}
			if (q < 0)
				break;
			p += token.length();
		}
		sm.addSortCriterion(new SortCriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, true));
		sm.setSmartCollection_subSelection_parent(parent);
		return sm;
	}

	public SmartCollection getResult() {
		return result;
	}

	public void verifyText(VerifyEvent e) {
		if (e.character == SWT.BS || e.character == SWT.DEL)
			return;
		int start = e.start;
		int end = e.end;
		String insert = e.text;
		String text = combo.getText();
		if (start != end) {
			if (end - start > insert.length()) {
				int kwstart = getWordStart(start, text);
				String orig = text.substring(kwstart, start);
				if (!orig.isEmpty()) {
					String keyword = findMatchingKeyword(orig, keywords);
					lastProposal = text.substring(0, kwstart) + keyword;
					keyword = orig + keyword.substring(orig.length());
					int kwend = getWordEnd(start, text);
					StringBuilder sb = new StringBuilder(text);
					sb.replace(kwstart, kwend, keyword);
					combo.removeVerifyListener(this);
					combo.setText(sb.toString());
					pnt.x = pnt.y = start;
					combo.setSelection(pnt);
					combo.addVerifyListener(this);
					e.doit = false;
					return;
				}
			}
			lastProposal = null;
			return;
		}
		if (insert.equals(" ")) { //$NON-NLS-1$
			int kwstart = getWordStart(start, text);
			String orig = text.substring(kwstart, start);
			if (!orig.isEmpty()) {
				String keyword = findMatchingKeyword(orig, keywords);
				if (keyword != null && keyword.length() == orig.length())
					text = text.substring(0, kwstart) + keyword;
				else if (lastProposal != null && text.toLowerCase().startsWith(lastProposal.toLowerCase()))
					text = lastProposal + text.substring(lastProposal.length());
			}
			text += insert;
			combo.removeVerifyListener(this);
			combo.setText(text);
			pnt.x = pnt.y = text.length();
			combo.setSelection(pnt);
			combo.addVerifyListener(this);
			lastProposal = null;
			e.doit = false;
		}
		if (lastProposal != null && start < lastProposal.length() && start + insert.length() <= text.length())
			text = text.substring(0, start) + insert;
		else {
			text = text.substring(0, start) + insert + text.substring(end);
			lastProposal = null;
		}
		int kwstart = getWordStart(start + insert.length(), text);
		String orig = text.substring(kwstart, start + insert.length());
		if (!orig.isEmpty()) {
			String keyword = findMatchingKeyword(orig, keywords);
			if (keyword == null) {
				int q = text.lastIndexOf(' ', start);
				if (q > 0 && q < start) {
					String possibleOp = text.substring(q + 1, start + 1);
					if (AND.startsWith(possibleOp))
						keyword = ANDSEP;
					else if (OR.startsWith(possibleOp))
						keyword = ORSEP;
					else if (NOT.startsWith(possibleOp))
						keyword = NOTSEP;
					if (keyword != null) {
						kwstart = q;
						orig = text.substring(q, start + 1);
					}
				}
			}
			if (keyword != null) {
				e.doit = false;
				combo.removeVerifyListener(this);
				lastProposal = text.substring(0, kwstart) + keyword;
				keyword = orig + keyword.substring(orig.length());
				combo.setText(text.substring(0, kwstart) + keyword);
				int pos = start + insert.length();
				pnt.x = pnt.y = pos;
				combo.setSelection(pnt);
				combo.addVerifyListener(this);
			} else {
				lastProposal = null;
				Ui.getUi().playSound("warning", null); //$NON-NLS-1$
			}
		} else {
			if (lastProposal != null && text.toLowerCase().startsWith(lastProposal.toLowerCase()))
				text = lastProposal + text.substring(lastProposal.length());
			combo.removeVerifyListener(this);
			combo.setText(text);
			pnt.x = pnt.y = text.length();
			combo.setSelection(pnt);
			combo.addVerifyListener(this);
			e.doit = false;
		}
	}

	private static String findMatchingKeyword(String orig, String[] keywords) {
		if (AND.equals(orig))
			return AND;
		if (NOT.equals(orig))
			return NOT;
		int index = Arrays.binarySearch(keywords, orig, UiUtilities.stringComparator);
		if (index >= 0)
			return keywords[index];
		int ins = -index - 1;
		if (ins < keywords.length && keywords[ins].toLowerCase().startsWith(orig.toLowerCase()))
			return keywords[ins];
		if (ins > 0 && keywords[ins - 1].toLowerCase().startsWith(orig.toLowerCase()))
			return keywords[ins - 1];
		return null;
	}

	private static int getWordStart(int pos, String text) {
		int q = text.lastIndexOf(ANDSEP, pos);
		int q1 = text.lastIndexOf(ORSEP, pos);
		int q2 = text.lastIndexOf(NOTSEP, pos);
		if (q >= 0)
			q += ANDSEP.length();
		if (q1 >= 0)
			q1 += ORSEP.length();
		if (q2 >= 0)
			q2 += NOTSEP.length();
		if (q < 0 || q1 > q)
			q = q1;
		if (q < 0 || q2 > q)
			q = q2;
		if (q >= 0)
			return q;
		return 0;
	}

	private static int getWordEnd(int pos, String text) {
		for (int i = pos; i < text.length(); i++)
			if (text.charAt(i) == ' ')
				return i;
		return text.length();
	}

	public void keyPressed(KeyEvent e) {
		int keyCode = e.keyCode;
		if (keyCode == SWT.ARROW_UP || keyCode == SWT.ARROW_DOWN || keyCode == SWT.PAGE_DOWN
				|| keyCode == SWT.PAGE_UP) {
			Combo textField = (Combo) e.widget;
			String orig = textField.getText();
			if (!orig.contains(AND) && !orig.contains(NOT)) {
				int index = Arrays.binarySearch(keywords, orig, UiUtilities.stringComparator);
				String text = null;
				if ((keyCode == SWT.PAGE_UP || orig.isEmpty()) && keywords.length > 0)
					text = keywords[0];
				else if (keyCode == SWT.PAGE_DOWN && keywords.length > 0)
					text = keywords[keywords.length - 1];
				else if (keyCode == SWT.ARROW_UP && index > 0)
					text = keywords[index - 1];
				else if (keyCode == SWT.ARROW_DOWN && index >= 0 && index < keywords.length - 1)
					text = keywords[index + 1];
				if (text != null) {
					textField.removeVerifyListener(this);
					textField.setText(text);
					pnt.x = pnt.y = 0;
					textField.setSelection(pnt);
					textField.addVerifyListener(this);
				}
				e.doit = false;
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		Point p = combo.getSelection();
		if (p.x == p.y) {
			if (e.keyCode == SWT.ARROW_RIGHT || e.keyCode == SWT.END) {
				if (lastProposal != null && lastProposal.length() >= p.x) {
					String text = combo.getText();
					if (text.toLowerCase().startsWith(lastProposal.toLowerCase())) {
						combo.removeVerifyListener(this);
						combo.setText(text = lastProposal + text.substring(lastProposal.length()));
						p.x = p.y = text.length();
						combo.addVerifyListener(this);
					} else {
						p.x = p.y = lastProposal.length();
						lastProposal = null;
					}
					combo.setSelection(p);
				}
			}
		}
	}

}
