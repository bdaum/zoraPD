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
 * (c) 2016 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ai.internal;

import java.util.Comparator;

import com.bdaum.zoom.core.internal.ai.Prediction.Token;

final public class TokenComparator implements Comparator<Token> {
	
	public final static TokenComparator INSTANCE = new TokenComparator();
	
	@Override
	public int compare(Token t1, Token t2) {
		float f1 = t1.getScore();
		float f2 = t2.getScore();
		return f1 == f2 ? 0 : f1 < f2 ? 1 : -1;
	}
}