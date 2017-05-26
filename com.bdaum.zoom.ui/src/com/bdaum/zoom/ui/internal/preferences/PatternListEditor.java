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


package com.bdaum.zoom.ui.internal.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

final class PatternListEditor extends ListEditor {

	public static final String ACCEPTS = "   (" + Messages.getString("PatternListEditor.accepts") +")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	public static final String REJECTS = "   (" + Messages.getString("PatternListEditor.rejects") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private int height;

	private String title;

	private String itemText;

	private String dflt;

	private String forbiddenChars;

	private boolean rule;

	private String separator;

	public PatternListEditor(String name, String text, String title,
			String itemText, String dflt, boolean rule, String separator,
			Composite parent, int height) {
		super(name, text, parent);
		this.height = height;
		this.title = title;
		this.dflt = dflt;
		this.itemText = itemText;
		this.separator = separator;
		this.forbiddenChars = separator;
		this.rule = rule;
	}

	
	@Override
	protected String[] parseString(String stringList) {
		List<String> result = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(stringList, separator); 
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.startsWith(">")) //$NON-NLS-1$
				token = token.substring(1) + ACCEPTS;
			else
				token += REJECTS;
			result.add(token);
		}
		return result.toArray(new String[result.size()]);
	}

	
	@Override
	protected String getNewInputObject() {
		PatternEditDialog dialog = new PatternEditDialog(getShell(), title,
				itemText, dflt, forbiddenChars, rule);
		if (dialog.open() == Window.OK)
			return dialog.getResult();
		return null;
	}

	
	@Override
	protected String createList(String[] items) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < items.length; i++) {
			if (sb.length() > 0)
				sb.append(separator);
			if (items[i].endsWith(REJECTS))
				sb.append(items[i].substring(0, items[i].length()
						- REJECTS.length()));
			else if (items[i].endsWith(ACCEPTS))
				sb.append('>').append(
						items[i].substring(0, items[i].length()
								- ACCEPTS.length()));
			else
				sb.append(items[i]);
		}
		return sb.toString();
	}

	
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		super.doFillIntoGrid(parent, numColumns);
		((GridData) getListControl(parent).getLayoutData()).heightHint = height;
	}

}