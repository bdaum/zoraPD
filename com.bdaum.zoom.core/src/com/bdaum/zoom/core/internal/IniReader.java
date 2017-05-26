package com.bdaum.zoom.core.internal;

/*
 * Copyright (c) 2002 Stefan Matthias Aust.  All Rights Reserved.
 *
 * You are granted the right to use this code in a) GPL based projects in
 * which case this code shall be also protected by the GPL, or b) in other
 * projects as long as you make all modifications or extensions to this
 * code freely available, or c) make any other special agreement with the
 * copyright holder.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bdaum.zoom.core.Core;

/**
 * This class can read properties files in Microsoft .ini file style and
 * provides an interface to read string, integer and boolean values. The .ini
 * files has the following structure:
 *
 * <pre>
 * ; a comment
 * [section]
 * key=value
 * </pre>
 *
 * @author Stefan Matthias Aust (sma@3plus4.de)
 * @version 1
 * @contributor bdaum - added getPropertyFloat, getFloatArray, getStringArray -
 *              converted to Java5 - introduced caseSensitive option
 */
public class IniReader {
	private Map<String, Map<String, String>> sections = new HashMap<String, Map<String, String>>();
	private boolean caseSensitive;

	public IniReader(String pathname, boolean caseSensitive)
			throws FileNotFoundException, IOException {
		initialize(new FileReader(pathname), caseSensitive);
	}

	public IniReader(File metafile, boolean caseSensitive)
			throws FileNotFoundException, IOException {
		initialize(new FileReader(metafile), caseSensitive);
	}

	private void initialize(Reader reader, boolean cs) throws IOException {
		this.caseSensitive = cs;
		String section = null, line;
		try (BufferedReader r = new BufferedReader(reader)) {
			while ((line = r.readLine()) != null) {
				line = line.trim();
				if (line.equals("") || line.startsWith(";")) { //$NON-NLS-1$ //$NON-NLS-2$
					continue;
				}
				if (line.startsWith("[")) { //$NON-NLS-1$
					if (!line.endsWith("]")) { //$NON-NLS-1$
						throw new IOException("] expected in section header"); //$NON-NLS-1$
					}
					section = line.substring(1, line.length() - 1);
					if (!cs)
						section = section.toLowerCase();
				} else if (section == null) {
					throw new IOException("[section] header expected"); //$NON-NLS-1$
				} else {
					int index = line.indexOf('=');
					if (index < 0) {
						throw new IOException("key/value pair without ="); //$NON-NLS-1$
					}
					String key = line.substring(0, index).trim();
					if (!cs)
						key = key.toLowerCase();
					String value = line.substring(index + 1).trim();
					Map<String, String> map = sections.get(section);
					if (map == null) {
						sections.put(section,
								(map = new HashMap<String, String>()));
					}
					map.put(key, value);
				}
			}
		}
	}

	public String getPropertyString(String section, String key,
			String defaultValue) {
		Map<String, String> map = caseSensitive ? sections.get(section)
				: sections.get(section.toLowerCase());
		if (map != null) {
			String value = caseSensitive ? map.get(key) : map.get(key
					.toLowerCase());
			if (value != null)
				return value;
		}
		return defaultValue;
	}

	public int getPropertyInt(String section, String key, int defaultValue) {
		String s = getPropertyString(section, key, null);
		if (s != null)
			return Integer.parseInt(s);
		return defaultValue;
	}

	public boolean getPropertyBool(String section, String key,
			boolean defaultValue) {
		String s = getPropertyString(section, key, null);
		if (s != null)
			return s.equalsIgnoreCase("true"); //$NON-NLS-1$
		return defaultValue;
	}

	public float getPropertyFloat(String section, String key, float defaultValue) { // bd
		String s = getPropertyString(section, key, null);
		return (s != null) ? Float.parseFloat(s) : defaultValue;
	}

	public float[] getFloatArray(String section, String key, float[] dflt) { // bd
		String s = getPropertyString(section, key, null);
		if (s != null) {
			List<String> tokens = Core.fromStringList(s, ";"); //$NON-NLS-1$
			float[] result = new float[tokens.size()];
			int i = 0;
			for (String t : tokens)
				result[i++] = Float.parseFloat(t);
			return result;
		}
		return dflt;
	}

	public String[] getStringArray(String section, String key) { // bd
		List<String> tokens = Core.fromStringList(
				getPropertyString(section, key, null), ";"); //$NON-NLS-1$
		return tokens.toArray(new String[tokens.size()]);
	}

	public Collection<String> listSections() {
		return sections.keySet();
	}

}