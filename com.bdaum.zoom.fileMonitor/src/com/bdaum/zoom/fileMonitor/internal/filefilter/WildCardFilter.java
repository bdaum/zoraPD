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
package com.bdaum.zoom.fileMonitor.internal.filefilter;

/*
 * Copyright (c) 1998 Kevan Stannard. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
 * without fee is hereby granted.
 *
 * Please note that this software comes with
 * NO WARRANTY
 *
 * BECAUSE THE PROGRAM IS LICENSED FREE OF CHARGE, THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED
 * BY APPLICABLE LAW. EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES
 * PROVIDE THE PROGRAM "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS
 * TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME
 * THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING WILL ANY COPYRIGHT HOLDER, OR ANY OTHER
 * PARTY WHO MAY MODIFY AND/OR REDISTRIBUTE THE PROGRAM AS PERMITTED ABOVE, BE LIABLE TO YOU FOR DAMAGES,
 * INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE
 * THE PROGRAM (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED INACCURATE OR LOSSES SUSTAINED
 * BY YOU OR THIRD PARTIES OR A FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER PROGRAMS), EVEN IF SUCH HOLDER
 * OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * modified by bdaum
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class WildCardFilter {
	private String wildPattern;

	private String[][] patterns;

	private boolean checkDir;

	private boolean rejects;

	private final static String FIND = "find"; //$NON-NLS-1$

	private final static String EXPECT = "expect"; //$NON-NLS-1$

	private final static String CONSUME = "consume"; //$NON-NLS-1$

	private final static String ANYTHING = "anything"; //$NON-NLS-1$

	private final static String NOTHING = "nothing"; //$NON-NLS-1$

	private static final String ENDSWITH = "endsWith"; //$NON-NLS-1$

	private static final String CAPTURE = "capture"; //$NON-NLS-1$

	private static final String EQUALS = "equals"; //$NON-NLS-1$

	private boolean ignoreCase = true;

	private boolean isPath;

	/**
	 * Creates a new wildcard filter
	 *
	 * @param wildString
	 *            - filter expression (*,?) wildcards
	 * @param prefixes
	 *            - first character denotes reject, second character accept. Can
	 *            be null.
	 */
	public WildCardFilter(String wildString, String prefixes) {
		this(wildString, prefixes, false);
	}

	/**
	 * Creates a new wildcard filter
	 *
	 * @param wildString
	 *            - filter expression (*,?) wildcards
	 * @param prefixes
	 *            - first character denotes reject, second character accept. Can
	 *            be null. If only one character is supplied, this character
	 *            denotes accept.
	 * @param includeDirs
	 *            - true if directory patterns (pattern/) are allowed
	 */
	public WildCardFilter(String wildString, String prefixes,
			boolean includeDirs) {
		wildPattern = wildString;
		if (prefixes != null && wildString.length() > 0) {
			char c = wildString.charAt(0);
			switch (prefixes.length()) {
			case 1:
				if (prefixes.charAt(0) == c)
					wildString = wildString.substring(1);
				else
					rejects = true;
				break;
			case 2:
				if (prefixes.charAt(0) == c) {
					wildString = wildString.substring(1);
					rejects = true;
				} else if (prefixes.charAt(1) == c)
					wildString = wildString.substring(1);
				break;
			}
		}
		if (includeDirs && wildString.endsWith("/")) { //$NON-NLS-1$
			wildString = wildString.substring(0, wildString.length() - 1);
			checkDir = true;
		}
		isPath = wildString.indexOf('/') >= 0;
		parseWildString(wildString, false);
	}

	/**
	 * Creates a new wildcard filter for capturing
	 *
	 * @param wildString
	 *            - filter expression (*,?) wildcards, / captures the next
	 *            wildcard expression
	 */
	public WildCardFilter(String wildString) {
		this(wildString, true);
	}

	/**
	 * Creates a new wildcard filter for capturing
	 *
	 * @param wildString
	 *            - filter expression (*,?) wildcards, / captures the next
	 *            wildcard expression
	 * @param ignoreCase
	 *            - true for ignoring case
	 */
	public WildCardFilter(String wildString, boolean ignoreCase) {
		wildPattern = wildString;
		this.ignoreCase = ignoreCase;
		parseWildString(wildString, true);
	}

	private void parseWildString(String wildString, boolean forCapture) {
		// ensure wildString is lowercase for all testing
		if (ignoreCase)
			wildString = wildString.toLowerCase();
		if (forCapture) {
			List<String[]> result = new ArrayList<String[]>(2);
			StringTokenizer st = new StringTokenizer(wildString, ";"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String wildPart = st.nextToken();
				// parse the input string
				List<String> commands = new ArrayList<String>();
				StringTokenizer tokens = new StringTokenizer(wildPart,
						"*?/", true); //$NON-NLS-1$
				String token = null;
				while (tokens.hasMoreTokens()) {
					token = tokens.nextToken();
					if ("/".equals(token)) //$NON-NLS-1$
						commands.add(CAPTURE);
					else if ("?".equals(token)) //$NON-NLS-1$
						commands.add(CONSUME);
					else if ("*".equals(token)) { //$NON-NLS-1$
						commands.add(FIND);
						if (tokens.hasMoreTokens()) {
							token = tokens.nextToken();
							commands.add(token);
						} else
							commands.add(ANYTHING);
					} else {
						if (!commands.isEmpty()
								&& commands.get(commands.size() - 1) == CAPTURE) {
							commands.remove(commands.size() - 1);
							token = '/' + token;
						}
						commands.add(EXPECT);
						commands.add(token);
					}
				}
				if (!"*".equals(token)) { //$NON-NLS-1$
					commands.add(EXPECT);
					commands.add(NOTHING);
				}
				result.add(commands.toArray(new String[commands.size()]));
			}
			patterns = result.toArray(new String[result.size()][]);
		} else {
			// move ? before any *
			int i = wildString.indexOf("*?"); //$NON-NLS-1$
			while (i >= 0) {
				wildString = wildString.substring(0, i) + "?*" //$NON-NLS-1$
						+ wildString.substring(i + 2);
				i = wildString.indexOf("*?"); //$NON-NLS-1$
			}
			// remove duplicate asterisks
			i = wildString.indexOf("**"); //$NON-NLS-1$
			while (i >= 0) {
				wildString = wildString.substring(0, i + 1)
						+ wildString.substring(i + 2);
				i = wildString.indexOf("**"); //$NON-NLS-1$
			}
			// parse the input string
			List<String> commands = new ArrayList<String>();
			StringTokenizer tokens = new StringTokenizer(wildString, "*?", true); //$NON-NLS-1$
			String token = null;
			while (tokens.hasMoreTokens()) {
				token = tokens.nextToken();
				if ("?".equals(token)) //$NON-NLS-1$
					commands.add(CONSUME);
				else if ("*".equals(token)) { //$NON-NLS-1$
					commands.add(FIND);
					if (tokens.hasMoreTokens()) {
						token = tokens.nextToken();
						commands.add(token);
					} else
						commands.add(ANYTHING);
				} else {
					commands.add(EXPECT);
					commands.add(token);
				}
			}
			if (!"*".equals(token)) { //$NON-NLS-1$
				commands.add(EXPECT);
				commands.add(NOTHING);
			}
			String[] pattern = commands.toArray(new String[commands.size()]);
			if (pattern.length == 4 && pattern[2] == EXPECT
					&& pattern[3] == NOTHING && pattern[0] != CONSUME) {
				if (pattern[0] == FIND)
					pattern[0] = ENDSWITH;
				else
					pattern[0] = EQUALS;
			}
			patterns = new String[][] { pattern };
		}
	}

	/**
	 * Tests if a given name matches the wildcard filter
	 *
	 * @param name
	 *            - name to be tested
	 * @return - true if successful
	 */
	public boolean accept(String name) {
		if (patterns.length == 0)
			return true;
		if (accept(patterns[0], name))
			return true;
		return false;
	}

	private boolean accept(String[] pattern, String name) {
		if (ignoreCase)
			name = name.toLowerCase();
		// shortcut for the most usual cases
		if (pattern[0] == EQUALS)
			return name.equals(pattern[1]);
		if (pattern[0] == ENDSWITH)
			return name.endsWith(pattern[1]);
		// start processing the pattern vector

		int currPos = 0;

		for (int cmdPos = 0; cmdPos < pattern.length; cmdPos++) {
			String command = pattern[cmdPos];
			if (command == FIND) {
				String param = pattern[++cmdPos];
				// if we are to find 'anything'
				// then we are done

				if (param == ANYTHING)
					return true;
				// otherwise search for the param
				// from the curr pos

				int nextPos = name.indexOf(param, currPos);
				if (nextPos < 0)
					return false;
				// found it
				currPos = nextPos + param.length();
			} else if (command == EXPECT) {
				String param = pattern[++cmdPos];
				// if we are to expect 'nothing'
				// then we MUST be at the end of the string

				if (param == NOTHING)
					// since we expect nothing else,
					// we must finish here
					return (currPos == name.length());
				// otherwise, check if the expected string
				// is at our current position

				int nextPos = name.indexOf(param, currPos);
				if (nextPos != currPos)
					return false;

				// if we've made it this far, then we've
				// found what we're looking for

				currPos += param.length();
			} else if (command == CONSUME) {
				if (currPos >= name.length())
					return false;
				++currPos;
			}
		}
		return true;
	}

	/**
	 * Extract meaningful parts from a string
	 *
	 * @param name
	 *            - name to be tested
	 * @return - meaningful string, one for each wildCard filter expression
	 */
	public String[] capture(String name) {
		String[] result = new String[patterns.length];
		lp: for (int i = 0; i < patterns.length; i++) {
			String[] pattern = patterns[i];
			String lower = ignoreCase ? name.toLowerCase() : name;
			// shortcut for the most usual cases
			if (pattern[0] == EQUALS) {
				result[i] = lower.equals(pattern[1]) ? "" : name; //$NON-NLS-1$
				continue lp;
			}
			if (pattern[0] == ENDSWITH) {
				result[i] = lower.endsWith(pattern[1]) ? name.substring(0,
						name.length() - pattern[1].length()) : name;
				continue lp;
			}
			// start processing the pattern vector
			StringBuilder sb = new StringBuilder();
			int currPos = 0;
			boolean capture = false;
			for (int cmdPos = 0; cmdPos < pattern.length; cmdPos++) {
				String command = pattern[cmdPos];
				if (command == CAPTURE)
					capture = true;
				else if (command == FIND) {
					String param = pattern[++cmdPos];
					// if we are to find 'anything'
					// then we are done

					if (param == ANYTHING) {
						if (capture) {
							if (sb.length() > 0)
								sb.append('/');
							sb.append(name.substring(currPos));
						}
						result[i] = sb.toString();
						continue lp;
					}
					// otherwise search for the param
					// from the curr pos

					int nextPos = lower.indexOf(param, currPos);
					if (nextPos < 0) {
						result[i] = name;
						continue;
					}
					// found it
					if (capture) {
						if (sb.length() > 0)
							sb.append('/');
						sb.append(name.substring(currPos, nextPos));
						capture = false;
					}
					currPos = nextPos + param.length();
				} else if (command == EXPECT) {
					String param = pattern[++cmdPos];
					// if we are to expect 'nothing'
					// then we MUST be at the end of the string

					if (param == NOTHING) {
						// since we expect nothing else,
						// we must finish here
						result[i] = (currPos == name.length()) ? sb.toString()
								: name;
						continue lp;
					}
					// otherwise, check if the expected string
					// is at our current position

					int nextPos = lower.indexOf(param, currPos);
					if (nextPos != currPos) {
						result[i] = name;
						continue lp;
					}

					// if we've made it this far, then we've
					// found what we're looking for

					currPos += param.length();
					capture = false;
				} else if (command == CONSUME) {
					if (currPos >= name.length()) {
						result[i] = name;
						continue lp;
					}
					if (capture) {
						if (sb.length() > 0)
							sb.append('/');
						sb.append(name.charAt(currPos));
						capture = false;
					}
					++currPos;
				}
			}
			result[i] = sb.toString();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File dir, String name) {
		String path = dir.getPath();
		int l = path.length();
		File tempFile;
		if (l == 0)
			tempFile = new File(name);
		else {
			char c = path.charAt(l - 1);
			if (c != '/' && c != File.separatorChar)
				path += '/';
			tempFile = new File(path, name);
		}
		if (checkDir) {
			if (tempFile.isFile())
				return false;
		} else if (tempFile.isDirectory())
			return false;
		return accept(name.toLowerCase());
	}

	/**
	 * @param file
	 *            - file to be accepted
	 * @return - true if accepted
	 */
	public boolean accept(File file) {
		if (checkDir) {
			if (file.isFile())
				return false;
		} else if (file.isDirectory())
			return false;
		return accept(file.getName().toLowerCase());
	}

	/**
	 * @param name
	 *            - filename - must be in lower case
	 * @param isDir
	 *            - true if this is a directory
	 * @return - true if accepted
	 */
	public boolean accept(String name, boolean isDir) {
		return (checkDir != isDir) ? false : accept(name);
	}

	@Override
	public String toString() {
		return wildPattern;
	}

	/**
	 * @return true if filter rejects
	 */
	public boolean isRejecting() {
		return rejects;
	}

	public static String validate(String t) {
		String errorMessage = null;
		boolean capture = false;
		boolean find = false;
		boolean something = false;
		lp: for (int i = 0; i < t.length(); i++) {
			switch (t.charAt(i)) {
			case '/':
				capture = true;
				break;
			case '*':
				if (find) {
					errorMessage = Messages
							.getString("WildCardFilter.double_asterisk"); //$NON-NLS-1$
					break lp;
				}
				find = true;
				something |= capture;
				capture = false;
				break;
			case '?':
				if (find) {
					errorMessage = Messages
							.getString("WildCardFilter.questionmark_asterisk"); //$NON-NLS-1$
					break lp;
				}
				something |= capture;
				capture = false;
				break;
			default:
				something = true;
				capture = false;
				find = false;
				break;
			}
		}
		if (errorMessage == null && !something)
			errorMessage = Messages
					.getString("WildCardFilter.no_relevant_section"); //$NON-NLS-1$
		return errorMessage;
	}

	/**
	 * @return isPath
	 */
	public boolean isPath() {
		return isPath;
	}
}
