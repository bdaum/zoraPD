package com.bdaum.zoom.core.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExifParser {

	private char[] characters;
	private int mxIndex;
	private char[] tokens;
	private int hw, i;

	public Object parse(String input) {
		characters = input.toCharArray();
		mxIndex = characters.length - 1;
		if (tokens == null || tokens.length <= mxIndex)
			tokens = new char[characters.length];
		i = hw = 0;
		while (i <= mxIndex) {
			char c = characters[i++];
			switch (c) {
			case '{':
				return parseStruct(hw);
			case '[':
				return parseList(hw);
			case '|':
				tokens[hw++] = i < mxIndex ? characters[++i] : i == mxIndex ? characters[i++] : c;
				break;
			default:
				tokens[hw++] = c;
			}
		}
		return new String(tokens, 0, hw);
	}

	private List<Object> parseList(int start) {
		int end = start;
		List<Object> result = new ArrayList<Object>();
		Object element = null;
		while (i <= mxIndex) {
			char c = characters[i++];
			switch (c) {
			case '{':
				element = parseStruct(end);
				break;
			case '[':
				element = parseList(end);
				break;
			case ',':
				result.add(element != null ? element : new String(tokens, start, end - start));
				end = start;
				element = null;
				break;
			case ']':
				result.add(element != null ? element : new String(tokens, start, end - start));
				return result;
			case '|':
				tokens[end++] = i <= mxIndex ? characters[i++] : c;
				break;
			default:
				tokens[end++] = c;
			}
		}
		return result;
	}

	private Map<String, Object> parseStruct(int start) {
		int end = start;
		String key = null;
		Map<String, Object> result = new HashMap<String, Object>();
		Object element = null;
		while (i <= mxIndex) {
			char c = characters[i++];
			if (key == null) {
				if (c == '=') {
					key = new String(tokens, start, end - start);
					end = start;
				} else
					tokens[end++] = c;
			} else
				switch (c) {
				case '[':
					element = parseList(end);
					break;
				case '{':
					element = parseStruct(end);
					break;
				case ',':
					if (!key.isEmpty())
						result.put(key, element != null ? element : new String(tokens, start, end - start));
					key = null;
					element = null;
					end = start;
					break;
				case '}':
					if (!key.isEmpty())
						result.put(key, element != null ? element : new String(tokens, start, end - start));
					return result;
				case '|':
					tokens[end++] = i <= mxIndex ? characters[i++] : c;
					break;
				default:
					tokens[end++] = c;
				}
		}
		return result;
	}
}
