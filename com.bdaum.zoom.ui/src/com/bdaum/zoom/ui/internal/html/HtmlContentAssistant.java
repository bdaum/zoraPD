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
 * (c) 2009-2011 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Point;

public class HtmlContentAssistant implements IContentAssistProcessor {
	private static final String BR = "<br/>"; //$NON-NLS-1$
	private static final String HTMLSTYLE = "HtmlContentAssistant.style."; //$NON-NLS-1$
	private static final String HTMLSTYLEEXTEND = HTMLSTYLE + "nlx"; //$NON-NLS-1$
	private static final String HTMLBRACKETS = "HtmlContentAssistant.bracket."; //$NON-NLS-1$;

	private static String[] STRUCTTAGS = new String[] { "<a href=\"*\"/></a>", //$NON-NLS-1$
			BR, "<caption>*</caption>", //$NON-NLS-1$
			"<col width=\"*\"/>", //$NON-NLS-1$
			"<colgroup>*</colgroup>", //$NON-NLS-1$
			"<dd>*</dd><dt></dt>", //$NON-NLS-1$
			"<div>*\n</div>", //$NON-NLS-1$
			"<dl><dd>*</dd><dt></dt>\n</dl>", //$NON-NLS-1$
			"<li>*</li>", //$NON-NLS-1$
			"<ol><li>*</li>\n</ol>", //$NON-NLS-1$
			"<p>*\n</p>", //$NON-NLS-1$
			"<p align=\"center\">*</p>", //$NON-NLS-1$
			"<table>*\n</table>", //$NON-NLS-1$
			"<tbody>*</tbody>", //$NON-NLS-1$
			"<td>*</td>", //$NON-NLS-1$
			"<th>*</th>", //$NON-NLS-1$
			"<thead>*</thead>", //$NON-NLS-1$
			"<tfoot>*</tfoot>", //$NON-NLS-1$
			"<tr>*</tr>", //$NON-NLS-1$
			"<ul><li>*</li>\n</ul>", //$NON-NLS-1$

			"&amp;", //$NON-NLS-1$
			"&apos;", //$NON-NLS-1$
			"&circ;", //$NON-NLS-1$
			"&copy;", //$NON-NLS-1$
			"&gt;", //$NON-NLS-1$
			"&lt;", //$NON-NLS-1$
			"&nbsp;", //$NON-NLS-1$
			"&quot;", //$NON-NLS-1$
			"&tilde;", //$NON-NLS-1$

			"&bdquo;*&rdquo;", //$NON-NLS-1$
			"&ldquo;*&rdquo;", //$NON-NLS-1$
			"&laquo;*&raquo;", //$NON-NLS-1$
			"&lsaquo;*&rsaquo;", //$NON-NLS-1$
			"&lsquo;*&rsquo;" //$NON-NLS-1$
	};
	private final static String[] LISTTAGS = new String[] { "ol", //$NON-NLS-1$
			"ul" }; //$NON-NLS-1$

	private final static String[][] STRUCTPARENTS = new String[][] {
			new String[] { "address", //$NON-NLS-1$
					"blockquote", //$NON-NLS-1$
					"dl", //$NON-NLS-1$
					"div", //$NON-NLS-1$
					"fieldset", //$NON-NLS-1$
					"form", //$NON-NLS-1$
					"h1", //$NON-NLS-1$
					"h2", //$NON-NLS-1$
					"h3", //$NON-NLS-1$
					"h4", //$NON-NLS-1$
					"h5", //$NON-NLS-1$
					"h6", //$NON-NLS-1$
					"hr", //$NON-NLS-1$
					"ol", //$NON-NLS-1$
					"p", //$NON-NLS-1$
					"pre", //$NON-NLS-1$
					"table", //$NON-NLS-1$
					"ul", //$NON-NLS-1$
					// block elements
					"a", //$NON-NLS-1$
					"abbr", //$NON-NLS-1$
					"acronym", //$NON-NLS-1$
					"b", //$NON-NLS-1$
					"basefont", //$NON-NLS-1$
					"bdo", //$NON-NLS-1$
					"big", //$NON-NLS-1$
					"br", //$NON-NLS-1$
					"button", //$NON-NLS-1$
					"cite", //$NON-NLS-1$
					"code", //$NON-NLS-1$
					"dfn", //$NON-NLS-1$
					"em", //$NON-NLS-1$
					"font", //$NON-NLS-1$
					"i", //$NON-NLS-1$
					"img", //$NON-NLS-1$
					"input", //$NON-NLS-1$
					"kbd", //$NON-NLS-1$
					"label", //$NON-NLS-1$
					"map", //$NON-NLS-1$
					"q", //$NON-NLS-1$
					"samp", //$NON-NLS-1$
					"select", //$NON-NLS-1$
					"small", //$NON-NLS-1$
					"span", //$NON-NLS-1$
					"strong", //$NON-NLS-1$
					"sub", //$NON-NLS-1$
					"sup", //$NON-NLS-1$
					"textarea", //$NON-NLS-1$
					"tt", //$NON-NLS-1$
					"var", //$NON-NLS-1$
					// inline elements
					"body" }, // br //$NON-NLS-1$
			new String[] { "table" }, // caption //$NON-NLS-1$
			new String[] { "table", "colgroup" }, // col //$NON-NLS-1$ //$NON-NLS-2$
			new String[] { "table" }, // colgroup //$NON-NLS-1$
			new String[] { "dl" }, // dd //$NON-NLS-1$
			new String[] { "blockquote", //$NON-NLS-1$
					"body", //$NON-NLS-1$
					"button", //$NON-NLS-1$
					"center", //$NON-NLS-1$
					"dd", //$NON-NLS-1$
					"del", //$NON-NLS-1$
					"div", //$NON-NLS-1$
					"ins", //$NON-NLS-1$
					"li", //$NON-NLS-1$
					"map", //$NON-NLS-1$
					"td", //$NON-NLS-1$
					"th" }, //$NON-NLS-1$
			// div
			new String[] { "blockquote", //$NON-NLS-1$
					"body", //$NON-NLS-1$
					"button", //$NON-NLS-1$
					"center", //$NON-NLS-1$
					"dd", //$NON-NLS-1$
					"del", //$NON-NLS-1$
					"div", //$NON-NLS-1$
					"ins", //$NON-NLS-1$
					"li", //$NON-NLS-1$
					"map", //$NON-NLS-1$
					"td", //$NON-NLS-1$
					"th" }, //$NON-NLS-1$
			// dl
			new String[] { "dir", "menu", "ol", "ul" }, // li //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			new String[] { "blockquote", //$NON-NLS-1$
					"body", //$NON-NLS-1$
					"button", //$NON-NLS-1$
					"center", //$NON-NLS-1$
					"dd", //$NON-NLS-1$
					"del", //$NON-NLS-1$
					"div", //$NON-NLS-1$
					"ins", //$NON-NLS-1$
					"li", //$NON-NLS-1$
					"map", //$NON-NLS-1$
					"td", //$NON-NLS-1$
					"th" }, //$NON-NLS-1$
			// ol
			new String[] { "address", //$NON-NLS-1$
					"blockquote", //$NON-NLS-1$
					"body", //$NON-NLS-1$
					"button", //$NON-NLS-1$
					"center", //$NON-NLS-1$
					"dd", //$NON-NLS-1$
					"del", //$NON-NLS-1$
					"div", //$NON-NLS-1$
					"ins", //$NON-NLS-1$
					"li", //$NON-NLS-1$
					"map", //$NON-NLS-1$
					"td", //$NON-NLS-1$
					"th" }, //$NON-NLS-1$
			// p
			new String[] { "blockquote", //$NON-NLS-1$
					"body", //$NON-NLS-1$
					"button", //$NON-NLS-1$
					"center", //$NON-NLS-1$
					"dd", //$NON-NLS-1$
					"del", //$NON-NLS-1$
					"div", //$NON-NLS-1$
					"ins", //$NON-NLS-1$
					"li", //$NON-NLS-1$
					"map", //$NON-NLS-1$
					"td", //$NON-NLS-1$
					"th" }, //$NON-NLS-1$
			// table
			new String[] { "table" }, // tbody //$NON-NLS-1$
			new String[] { "tr" }, // td //$NON-NLS-1$
			new String[] { "tr" }, // th //$NON-NLS-1$
			new String[] { "tr" }, // thead //$NON-NLS-1$
			new String[] { "tr" }, // tfoot //$NON-NLS-1$
			new String[] { "table", "tbody", "tfoot", "thead" }, // tr //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			new String[] { "blockquote", //$NON-NLS-1$
					"body", //$NON-NLS-1$
					"button", //$NON-NLS-1$
					"center", //$NON-NLS-1$
					"dd", //$NON-NLS-1$
					"del", //$NON-NLS-1$
					"div", //$NON-NLS-1$
					"ins", //$NON-NLS-1$
					"li", //$NON-NLS-1$
					"map", //$NON-NLS-1$
					"td", //$NON-NLS-1$
					"th" }, //$NON-NLS-1$
	};
	private final static String[] STYLEPARENTS = new String[] { "address", //$NON-NLS-1$
			"blockquote", //$NON-NLS-1$
			"dl", //$NON-NLS-1$
			"div", //$NON-NLS-1$
			"fieldset", //$NON-NLS-1$
			"form", //$NON-NLS-1$
			"h1", //$NON-NLS-1$
			"h2", //$NON-NLS-1$
			"h3", //$NON-NLS-1$
			"h4", //$NON-NLS-1$
			"h5", //$NON-NLS-1$
			"h6", //$NON-NLS-1$
			"hr", //$NON-NLS-1$
			"ol", //$NON-NLS-1$
			"p", //$NON-NLS-1$
			"table", //$NON-NLS-1$
			"ul", //$NON-NLS-1$
			// block elements
			"a", //$NON-NLS-1$
			"abbr", //$NON-NLS-1$
			"acronym", //$NON-NLS-1$
			"b", //$NON-NLS-1$
			"basefont", //$NON-NLS-1$
			"bdo", //$NON-NLS-1$
			"big", //$NON-NLS-1$
			"br", //$NON-NLS-1$
			"button", //$NON-NLS-1$
			"cite", //$NON-NLS-1$
			"code", //$NON-NLS-1$
			"dfn", //$NON-NLS-1$
			"em", //$NON-NLS-1$
			"font", //$NON-NLS-1$
			"i", //$NON-NLS-1$
			"img", //$NON-NLS-1$
			"input", //$NON-NLS-1$
			"kbd", //$NON-NLS-1$
			"label", //$NON-NLS-1$
			"map", //$NON-NLS-1$
			"q", //$NON-NLS-1$
			"samp", //$NON-NLS-1$
			"select", //$NON-NLS-1$
			"small", //$NON-NLS-1$
			"span", //$NON-NLS-1$
			"strong", //$NON-NLS-1$
			"sub", //$NON-NLS-1$
			"sup", //$NON-NLS-1$
			"textarea", //$NON-NLS-1$
			"tt", //$NON-NLS-1$
			"var", //$NON-NLS-1$
			// inline elements
			"caption", "dd", "dt", "li", "td", "th", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			// subblock elements
			"abbr", //$NON-NLS-1$
			"acronym", //$NON-NLS-1$
			"b", //$NON-NLS-1$
			"cite", //$NON-NLS-1$
			"code", //$NON-NLS-1$
			"dfn", //$NON-NLS-1$
			"em", //$NON-NLS-1$
			"samp", //$NON-NLS-1$
			"kbd", //$NON-NLS-1$
			"i", //$NON-NLS-1$
			"q", //$NON-NLS-1$
			"strong", //$NON-NLS-1$
			"strike", //$NON-NLS-1$
			"sub", //$NON-NLS-1$
			"super", //$NON-NLS-1$
			"u", //$NON-NLS-1$
			"var", //$NON-NLS-1$
			// phrase elements
			"body" }; //$NON-NLS-1$
	private final static String[] HTMLTAGS = new String[] { "abbr", //$NON-NLS-1$
			"acronym", //$NON-NLS-1$
			"b", //$NON-NLS-1$
			"cite", //$NON-NLS-1$
			"code", //$NON-NLS-1$
			"dfn", //$NON-NLS-1$
			"em", //$NON-NLS-1$
			"samp", //$NON-NLS-1$
			"kbd", //$NON-NLS-1$
			"i", //$NON-NLS-1$
			"q cite=\"\"", //$NON-NLS-1$
			"strong", //$NON-NLS-1$
			"strike", //$NON-NLS-1$
			"sub", //$NON-NLS-1$
			"super", //$NON-NLS-1$
			"u", //$NON-NLS-1$
			"var" }; //$NON-NLS-1$

	private static final String[] BRACKETS = new String[] { "quot", "apos", //$NON-NLS-1$//$NON-NLS-2$
			"bdquo rdquo", "ldquo rdquo", "laquo raquo", "lsaquo rsaquo", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
			"lsquo rsquo", "lt gt" }; //$NON-NLS-1$ //$NON-NLS-2$

	private final boolean full;

	public HtmlContentAssistant() {
		this(true);
	}

	public HtmlContentAssistant(boolean full) {
		this.full = full;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '<', '&' };
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int documentOffset) {
		IDocument doc = viewer.getDocument();
		Point selectedRange = viewer.getSelectedRange();
		try {
			List<CompletionProposal> propList = (selectedRange.y == 0) ? computeStructProposals(
					getParentTag(doc, documentOffset),
					getQualifier(doc, documentOffset), documentOffset)
					: computeFormatProposals(
							getParentTag(doc, selectedRange.x),
							selectedRange.x,
							doc.get(selectedRange.x, selectedRange.y));
			return propList.toArray(new CompletionProposal[propList.size()]);
		} catch (BadLocationException e) {
			return new CompletionProposal[0];
		}
	}

	private static final int TEXT = 0;
	private static final int TAG = 1;
	private static final int ENT = 2;
	private static final int STARTTAG = 3;
	private static final int ENDTAG = 4;
	private static final int EMPTYTAG = 5;

	static {
		String nl = Messages.getString(HTMLSTYLEEXTEND);
		if (nl != null) {
			List<String> allStyles = new ArrayList<String>(
					Arrays.asList(STRUCTTAGS));
			StringTokenizer st = new StringTokenizer(nl);
			while (st.hasMoreTokens())
				allStyles.add('&' + st.nextToken() + ';');
			STRUCTTAGS = allStyles.toArray(new String[allStyles.size()]);
		}
	}

	/**
	 * @param doc
	 * @param documentOffset
	 * @return the parent tag
	 */

	private static String getParentTag(IDocument doc, int documentOffset) {
		int state = TEXT;
		StringBuffer tag = new StringBuffer();
		Stack<String> stack = new Stack<String>();
		for (int i = 0; i < documentOffset; i++) {
			try {
				char c = doc.getChar(i);
				switch (state) {
				case TEXT:
					switch (c) {
					case '<':
						tag.setLength(0);
						state = TAG;
						break;
					case '&':
						state = ENT;
						break;
					}
					break;
				case ENT:
					switch (c) {
					case ';':
						state = TEXT;
						break;
					}
					break;
				case TAG:
					switch (c) {
					case '/':
						state = ENDTAG;
						break;
					default:
						tag.append(c);
						state = STARTTAG;
						break;
					}
					break;
				case STARTTAG:
					switch (c) {
					case '/':
						state = EMPTYTAG;
						break;
					case '>':
						stack.push(tag.toString());
						state = TEXT;
						break;
					default:
						tag.append(c);
						break;
					}
					break;
				case ENDTAG:
					switch (c) {
					case '>':
						String endTag = tag.toString();
						if (endTag.equals(stack.peek()))
							stack.pop();
						state = TEXT;
						break;
					default:
						tag.append(c);
						break;
					}
					break;
				case EMPTYTAG:
					switch (c) {
					case '>':
						state = TEXT;
						break;
					}
					break;
				}
			} catch (BadLocationException e) {
				break;
			}
		}
		if (!stack.isEmpty())
			return stack.pop();
		return "body"; //$NON-NLS-1$
	}

	private List<CompletionProposal> computeFormatProposals(String parent,
			int documentOffset, String selectedText) {

		List<CompletionProposal> propList = new ArrayList<CompletionProposal>();
		String trimmedText = selectedText.trim();
		if (trimmedText.indexOf('<') == 0
				&& trimmedText.lastIndexOf('>') == trimmedText.length() - 1) {
			int e1 = trimmedText.indexOf('>');
			if (e1 >= 0) {
				String tag = trimmedText.substring(1, e1);
				String endTag = "</" + tag + ">"; //$NON-NLS-1$ //$NON-NLS-2$
				if (trimmedText.indexOf(endTag) == trimmedText.length()
						- endTag.length()) {
					int a1 = selectedText.indexOf('<');
					e1 += a1 + 1;
					int a2 = selectedText.lastIndexOf(endTag);
					int e2 = a2 + endTag.length();
					String replacement = selectedText.substring(0, a1)
							+ selectedText.substring(e1, a2)
							+ selectedText.substring(e2);
					propList.add(new CompletionProposal(
							replacement,
							documentOffset,
							selectedText.length(),
							a1,
							null,
							NLS.bind(
									Messages.getString("HtmlContentAssistant.remove_formatting"), tag), //$NON-NLS-1$
							null, replacement));
				}
			}
		}
		if (full && trimmedText.indexOf('\n') > 0) {
			for (int i = 0; i < LISTTAGS.length; i++) {
				String tag = LISTTAGS[i];
				StringBuffer sb = new StringBuffer('<');
				sb.append(tag).append('>');
				StringTokenizer st = new StringTokenizer(selectedText, "\n\r"); //$NON-NLS-1$
				while (st.hasMoreElements())
					sb.append("<li>").append(st.nextToken()).append("</li>\n"); //$NON-NLS-1$ //$NON-NLS-2$
				sb.append("</").append(tag).append('>'); //$NON-NLS-1$
				int cursor = sb.length();
				String insert = sb.toString();
				String displayString = new StringBuilder().append('<')
						.append(LISTTAGS[i]).append(">      ") //$NON-NLS-1$
						.append(Messages.getString(HTMLSTYLE + tag)).toString();
				propList.add(new CompletionProposal(insert, documentOffset,
						selectedText.length(), cursor, null, displayString,
						null, insert));
			}
		}
		if (parent != null) {
			parent = parent.intern();
			boolean found = false;
			for (int j = 0; j < STYLEPARENTS.length; j++)
				if (parent == STYLEPARENTS[j]) {
					found = true;
					break;
				}
			if (!found)
				return propList;
		}
		for (int i = 0; i < HTMLTAGS.length; i++) {
			int p = HTMLTAGS[i].indexOf(' ');
			String endTag = (p < 0) ? HTMLTAGS[i] : HTMLTAGS[i].substring(0, p);
			String insert = new StringBuilder().append('<').append(HTMLTAGS[i])
					.append('>').append(selectedText).append("</") //$NON-NLS-1$
					.append(endTag).append('>').toString();
			String displayString = new StringBuilder().append('<')
					.append(HTMLTAGS[i]).append(">      ") //$NON-NLS-1$
					.append(Messages.getString(HTMLSTYLE + endTag)).toString();
			propList.add(new CompletionProposal(insert, documentOffset,
					selectedText.length(), insert.length(), null,
					displayString, null, insert));
		}
		for (int i = 0; i < BRACKETS.length; i++) {
			String left, right;
			left = right = BRACKETS[i];
			int p = left.indexOf(' ');
			if (p >= 0) {
				right = left.substring(p + 1);
				left = left.substring(0, p);
			}
			String insert = new StringBuilder().append('&').append(left)
					.append(';').append(selectedText).append('&').append(right)
					.append(';').toString();
			String displayString = Messages.getString(HTMLBRACKETS + left);
			propList.add(new CompletionProposal(insert, documentOffset,
					selectedText.length(), insert.length(), null,
					displayString, null, insert));
		}
		return propList;
	}

	private static String getQualifier(IDocument doc, int documentOffset) {
		StringBuffer buf = new StringBuffer();
		while (true)
			try {
				char c = doc.getChar(--documentOffset);
				if (c == '>' || Character.isWhitespace(c))
					break;
				buf.append(c);
				if (c == '<' || c == '&')
					return buf.reverse().toString();
			} catch (BadLocationException e) {
				break;
			}
		return ""; //$NON-NLS-1$
	}

	private List<CompletionProposal> computeStructProposals(String parent,
			String qualifier, int documentOffset) {
		List<CompletionProposal> propList = new ArrayList<CompletionProposal>();
		for (int i = 0; i < STRUCTTAGS.length; i++) {
			String insert = STRUCTTAGS[i];
			if (full || insert == BR || insert.startsWith("&")) { //$NON-NLS-1$
				if (insert.startsWith(qualifier)) {
					if (parent != null) {
						String[] parents = i < STRUCTPARENTS.length ? STRUCTPARENTS[i]
								: null;
						if (parents != null) {
							parent = parent.intern();
							boolean found = false;
							for (int j = 0; j < parents.length; j++) {
								if (parent == parents[j]) {
									found = true;
									break;
								}
							}
							if (!found)
								continue;
						}
					}
					int p = insert.indexOf('>');
					if (p < 0)
						p = insert.indexOf(';');
					else if (insert.charAt(p - 1) == '/')
						p--;
					String tag = insert.substring(1, p);
					p = tag.indexOf(' ');
					if (p >= 0)
						tag = tag.substring(0, p);
					int cursor = insert.indexOf("*"); //$NON-NLS-1$
					String markup;
					if (cursor < 0) {
						cursor = insert.length();
						markup = insert;
					} else {
						markup = insert.substring(0, cursor);
						insert = markup + insert.substring(cursor + 1);
					}
					propList.add(new CompletionProposal(insert, documentOffset
							- qualifier.length(), qualifier.length(), cursor,
							null, markup + "      " //$NON-NLS-1$
									+ Messages.getString(HTMLSTYLE + tag),
							null, insert));
				}
			}
		}
		if (parent != null) {
			parent = parent.intern();
			boolean found = false;
			for (int j = 0; j < STYLEPARENTS.length; j++)
				if (parent == STYLEPARENTS[j]) {
					found = true;
					break;
				}
			if (!found)
				return propList;
		}
		for (int i = 0; i < HTMLTAGS.length; i++) {
			int p = HTMLTAGS[i].indexOf(' ');
			String endTag = (p < 0) ? HTMLTAGS[i] : HTMLTAGS[i].substring(0, p);
			String insert = new StringBuilder().append('<').append(HTMLTAGS[i])
					.append("></").append(endTag).append('>').toString(); //$NON-NLS-1$
			if (insert.startsWith(qualifier))
				propList.add(new CompletionProposal(insert, documentOffset
						- qualifier.length(), qualifier.length(), HTMLTAGS[i]
						.length() + 2, null, insert + "      " //$NON-NLS-1$
						+ Messages.getString(HTMLSTYLE + endTag), null, insert));
		}
		return propList;
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int documentOffset) {
		return null;
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	public String getErrorMessage() {
		return null;
	}
}