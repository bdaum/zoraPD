package com.bdaum.zoom.core.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExifParser {

	final private char[] characters;
	final private int mxIndex;
	private int i = 0;

	public ExifParser(String input) {
		characters = input.toCharArray();
		mxIndex = characters.length - 1;
	}

	public Object parse() {
		StringBuilder token = new StringBuilder();
		while (i <= mxIndex) {
			char c = characters[i++];
			if (c == '|' && i < mxIndex) {
				token.append(characters[++i]);
				continue;
			}
			switch (c) {
			case '{':
				return parseStruct();
			case '[':
				return parseList();
			case '|':
				if (i <= mxIndex)
					token.append(characters[i++]);
				else
					token.append(c);
				break;
			default:
				token.append(c);
			}
		}
		return token.toString();
	}

	private List<Object> parseList() {
		StringBuilder token = new StringBuilder();
		List<Object> result = new ArrayList<Object>();
		Object element = null;
		while (i <= mxIndex) {
			char c = characters[i++];
			switch (c) {
			case '{':
				element = parseStruct();
				break;
			case '[':
				element = parseList();
				break;
			case ',':
				if (element != null)
					result.add(element);
				else
					result.add(token.toString());
				token.setLength(0);
				element = null;
				break;
			case ']':
				if (element != null)
					result.add(element);
				else
					result.add(token.toString());
				return result;
			case '|':
				if (i <= mxIndex)
					token.append(characters[i++]);
				else
					token.append(c);
				break;
			default:
				token.append(c);
			}
		}
		return result;
	}

	private Map<String, Object> parseStruct() {
		String key = null;
		StringBuilder token = new StringBuilder();
		Map<String, Object> result = new HashMap<String, Object>();
		Object element = null;
		while (i <= mxIndex) {
			char c = characters[i++];
			if (key == null) {
				switch (c) {
				case '=':
					key = token.toString();
					token.setLength(0);
					break;
				default:
					token.append(c);
				}
			} else {
				switch (c) {
				case '[':
					element = parseList();
					break;
				case '{':
					element = parseStruct();
					break;
				case ',':
					if (element == null)
						element = token.toString();
					if (key.length() > 0)
						result.put(key, element);
					key = null;
					element = null;
					token.setLength(0);
					break;
				case '}':
					if (element == null)
						element = token.toString();
					if (key.length() > 0)
						result.put(key, element);
					return result;
				case '|':
					if (i <= mxIndex)
						token.append(characters[i++]);
					else
						token.append(c);
					break;
				default:
					token.append(c);
				}
			}
		}
		return result;
	}
}
