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
package com.bdaum.zoom.core;

import java.util.Collection;

public interface ISpellCheckingService {

	/**
	 * A listener for spell checking incidents
	 */
	interface IncendentListener {
		/**
		 * Handle a spell checking incident
		 * 
		 * @param incident
		 *            - the incident to handle
		 * @return - true in case of abort
		 */
		boolean handleIncident(ISpellIncident incident);
	}

	/**
	 * Describes a spell checking incident
	 */
	interface ISpellIncident {

		/**
		 * Retrieves the invalid word
		 * 
		 * @return - invalid word
		 */
		String getWrongWord();

		/**
		 * Retrieves the offset of the invalid word
		 * 
		 * @return - offset of invalid word
		 */
		int getOffset();

		/**
		 * Retrieves possible replacements for invalid word
		 * 
		 * @return - possible replacements
		 */
		String[] getSuggestions();

		/**
		 * Tests if the given offset is inside the invalid word
		 * 
		 * @param offset
		 *            - given offset
		 * @return - true if the given offset is inside the invalid word
		 */
		boolean happensAt(int offset);

		/**
		 * Adds the invalid word to the user directory
		 */
		void addWord();
	}

	/* Options */
	/**
	 * Option for ignoring single letters
	 */
	int IGNORESINGLELETTER = 1;
	/**
	 * Option for ignoring upper case words
	 */
	int IGNORNEUPPERCASE = 2;
	/**
	 * Option for ignoring words with digits
	 */
	int IGNOREWIDTHDIGITS = 4;
	/**
	 * Option for ignoring mixed case words
	 */
	int IGNOREMIXEDCASE = 8;

	/**
	 * Option for descriptive texts
	 */
	int DESCRIPTIONOPTIONS = IGNORESINGLELETTER
	| IGNOREWIDTHDIGITS | IGNORNEUPPERCASE;
	/**
	 * Option for title texts
	 */
	int TITLEOPTIONS = IGNOREMIXEDCASE | IGNORESINGLELETTER
			| IGNOREWIDTHDIGITS | IGNORNEUPPERCASE;
	/**
	 * Option for keywords
	 */
	int KEYWORDOPTIONS = 0;

	/**
	 * Option for ignoring words containing other characters than letters
	 */
	int IGNOREWITHNONLETTERS = 16;
	
	/**
	 * Indicates that no spell checking is wanted
	 */
	int NOSPELLING = -1;

	/**
	 * Default dictionary subfolder
	 */
	String DICTFOLDER = "dictionaries"; //$NON-NLS-1$

	/**
	 * Spell checks the given text
	 * 
	 * @param text
	 *            - text to check
	 * @param listener
	 *            - listener for spell checking incidents
	 * @param options
	 *            - spell checking options ORed
	 * @param nmax
	 *            - max number of suggestions
	 */
	void checkSpelling(String text, IncendentListener listener, int options,
			int nmax);

	/**
	 * Adds the given word to the user directory.
	 * 
	 * @param word
	 *            - word to be added
	 */
	void addWord(String word);

	/**
	 * Returns the language codes (such as de_DE or en_US) of the installed
	 * dictionaries
	 * 
	 * @return - language codes
	 */
	Collection<String> getSupportedLanguages();

}
