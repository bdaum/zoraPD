/*******************************************************************************
 * Copyright (c) 2010 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.batch.internal;

import java.util.Arrays;

public class Options {

	private Object[] options;

	public Object put(String key, Object value) {
		if (options == null)
			options = new Object[] { key, value };
		else {
			int len = options.length;
			int fnd = len;
			for (int i = 0; i < len; i += 2) {
				int r = key.compareTo((String) options[i]);
				if (r < 0) {
					fnd = i;
					break;
				}
				if (r == 0) {
					Object old = options[i + 1];
					options[i + 1] = value;
					return old;
				}
			}
			Object[] newoptions = new Object[len + 2];
			if (fnd > 0)
				System.arraycopy(options, 0, newoptions, 0, fnd);
			newoptions[fnd] = key;
			newoptions[fnd + 1] = value;
			if (fnd != len)
				System.arraycopy(options, fnd, newoptions, fnd + 2, len - fnd);
			options = newoptions;
		}
		return null;
	}

	public Object get(String key) {
		if (options == null)
			return null;
		for (int i = 0; i < options.length; i += 2) {
			int r = key.compareTo((String) options[i]);
			if (r < 0)
				return null;
			if (r == 0)
				return options[i + 1];
		}
		return null;
	}

	public boolean getBoolean(String key) {
		Object value = get(key);
		if (value instanceof Boolean)
			return ((Boolean) value).booleanValue();
		return false;
	}

	@Override
	public int hashCode() {
		int h = 0;
		if (options != null)
			for (int i = 0; i < options.length; i++)
				h = h * 31 + options[i].hashCode();
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		if (options == null)
			return obj == null;
		if (obj instanceof Options)
			return Arrays.equals(options, ((Options) obj).options);
		return false;
	}

}
