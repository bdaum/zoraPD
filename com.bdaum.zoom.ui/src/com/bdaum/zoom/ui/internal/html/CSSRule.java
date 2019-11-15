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
 * (c) 2019 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.html;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class CSSRule implements IPredicateRule {

	private IToken propToken;
	private IToken valueToken;

	public CSSRule(final IToken propToken, final IToken valueToken) {
		this.propToken = propToken;
		this.valueToken = valueToken;
	}

	private IToken getToken(final ICharacterScanner scanner) {
		int c;
		while ((c = scanner.read()) != ICharacterScanner.EOF) {
			if (c == ':') {
				int k = 0;
				while ((c = scanner.read()) != ICharacterScanner.EOF) {
					++k;
					if (c == '{' || c == '\n')
						break;
					if (c == ';') {
						while (k-- > 0)
							scanner.unread();
						return propToken;
					}
				}
				while (k-- > 0)
					scanner.unread();
				return null;
			} else if (c == ';')
				return valueToken;
			else if (c == '\n')
				return null;
		}
		scanner.unread();
		return null;
	}

	public IToken evaluate(final ICharacterScanner scanner, final boolean resume) {
		return doEvaluate(scanner, resume);
	}

	private IToken doEvaluate(final ICharacterScanner scanner, final boolean resume) {
		IToken token = null;
		if (resume)
			token = getToken(scanner);
		else {
			int c = scanner.read();
			if (c != ' ' && c != '\t' && c != '\r' && c != '\n')
				token = getToken(scanner);
		}
		if (token != null)
			return token;
		scanner.unread();
		return Token.UNDEFINED;
	}

	public IToken getSuccessToken() {
		return null;
	}

	public IToken evaluate(final ICharacterScanner scanner) {
		return evaluate(scanner, false);
	}
}
