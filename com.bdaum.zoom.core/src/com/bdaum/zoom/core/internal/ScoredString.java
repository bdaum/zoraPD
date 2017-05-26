package com.bdaum.zoom.core.internal;

import org.eclipse.osgi.util.NLS;

public class ScoredString implements Comparable<ScoredString>{
	String string;
	int score;

	public ScoredString(String s, int score) {
		this.string = s;
		this.score = score;
	}

	public String getString() {
		return string;
	}

	public int getScore() {
		return score;
	}

	@Override
	public String toString() {
		return NLS.bind("{0} ({1}%)", string, score); //$NON-NLS-1$
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int compareTo(ScoredString s) {
		return s == null || s.getScore() == score ? 0 : s.getScore() > score ? 1 : -1;
	}

}