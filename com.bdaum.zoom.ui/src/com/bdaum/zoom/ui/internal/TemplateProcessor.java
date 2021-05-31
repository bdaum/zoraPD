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

package com.bdaum.zoom.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.bdaum.zoom.cat.model.Asset_type;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.ui.internal.hover.HoverTestAsset;
import com.bdaum.zoom.ui.internal.hover.IHoverContext;
import com.bdaum.zoom.ui.internal.hover.IHoverContribution;
import com.bdaum.zoom.ui.internal.hover.IHoverItem;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class TemplateProcessor {

	private static final String EMPTYS = "\02"; //$NON-NLS-1$
	private static final char NIL = '\u0008';
	private static final char START = '\01';
	private static final char END = '\03';
	private static final char EMPTY = '\02';
	private final GregorianCalendar cal = new GregorianCalendar();
	private final String[] variables;
	// Parser
	private final int[] startpos = new int[16];
	private final int[] states = new int[16];
	private final int[] fails = new int[16];
	private final int[] successes = new int[16];
	private int lineSuccesses = 0;
	private int lineFails = 0;
	private Asset testAsset;
	private static final int EXPR = 0;
	private static final int CG1 = 1;
	private static final int CG2 = 2;
	private static final int SUCCESS = 3;
	private static final int FAIL = 4;
	private static final int CGP = 5;
	private static final int CG2P = 6;
	private static final int IGN = 7;
	private static final int CGEP = 8;
	private static final int KEEP = 9;
	private static final int DISCARD = 10;
	private static final int LINEP = 11;
	private static final int LINE2P = 12;

	public TemplateProcessor(String[] variables) {
		this.variables = variables;
	}

	public String processTemplate(String template, Asset asset) {
		return processTemplate(template, asset, "", -1, -1); //$NON-NLS-1$
	}

	public String processTemplate(String template, Asset asset, String collection, int imageNo, int sequenceNo) {
		if (template == null)
			template = UiActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.THUMBNAILTEMPLATE);
		if (template == null || template.isEmpty())
			return ""; //$NON-NLS-1$
		if (asset == null)
			asset = getTestAsset();
		if (collection == null)
			collection = Messages.TemplateProcessor_testCollection;
		Date crDate = asset.getDateTimeOriginal();
		if (crDate == null)
			crDate = asset.getDateTime();
		if (crDate != null)
			cal.setTime(crDate);
		StringBuilder sb = new StringBuilder(template);
		for (String tv : variables) {
			int u = 0;
			while (true) {
				int p = sb.indexOf(tv, u);
				if (p < 0)
					break;
				int rlen = Utilities.replaceVariables(sb, p, tv, asset, collection, null, cal, -1, imageNo, sequenceNo,
						null, false);
				if (rlen > 0) {
					sb.insert(p + rlen, END);
					sb.insert(p, START);
				} else
					sb.insert(p, EMPTY);
			}
		}
		int from = 0;
		while (true) {
			int p = sb.indexOf(Constants.TV_META, from);
			if (p < 0)
				break;
			int rlen = Utilities.replaceMeta(sb, p, asset, true, false, true);
			if (rlen < 0) {
				from = p + Constants.TV_META.length();
				continue;
			}
			if (rlen > 0) {
				sb.insert(p + rlen, END);
				sb.insert(p, START);
				from = p + rlen + 2;
			} else {
				sb.insert(p, EMPTY);
				from = p + 1;
			}
		}
		int length = sb.length();
		char[] t = new char[length];
		sb.getChars(0, length, t, 0);
		parse(t, EXPR);
		int wp = removeNils(t);
		return wp == 0 ? Format.MISSINGENTRYSTRING : new String(t, 0, wp);
	}

	public String processTemplate(IHoverContribution contrib, Object object, IHoverContext context, boolean isTitle) {
		String template = isTitle ? contrib.getTitleTemplate() : contrib.getTemplate();
		if (template == null || template.isEmpty())
			template = isTitle ? contrib.getDefaultTitleTemplate() : contrib.getDefaultTemplate();
		if (template == null || template.isEmpty())
			return ""; //$NON-NLS-1$
		return processTemplate(template, contrib, object, context,
				isTitle ? contrib.getTitleItemKeys() : contrib.getItemKeys(), isTitle);
	}

	public String processTemplate(String template, IHoverContribution contrib, String[] variables, boolean isTitle) {
		return processTemplate(template, contrib, contrib.getTestObject(), null, variables, isTitle);
	}

	private String processTemplate(String template, IHoverContribution contrib, Object object, IHoverContext context,
			String[] variables, boolean isTitle) {
		Object target = contrib.getTarget(object);
		int flags = contrib.getMediaFlags(target);
		StringBuilder sb = new StringBuilder(template);
		for (String tv : variables) {
			int u = 0;
			while (true) {
				int p = sb.indexOf(tv, u);
				if (p < 0)
					break;
				int q = p + tv.length();
				if (!isTitle && flags != 0 && "{metadata}".equals(tv) //$NON-NLS-1$
						&& (target instanceof Asset || target instanceof List<?> || target instanceof HoverTestAsset)) {
					sb.delete(p, q);
					u = createMetaBlock(sb, p, target, flags);
				} else {
					IHoverItem hoverItem = contrib.getHoverItem(tv);
					u = replaceContent(sb, p, q, hoverItem == null ? null : hoverItem.getValue(tv, object, context));
				}
			}
		}
		if ((target instanceof Asset || target instanceof List<?>) || target instanceof HoverTestAsset) {
			int u = 0;
			while (true) {
				int p = sb.indexOf(Constants.TV_META, u);
				if (p < 0)
					break;
				int q = sb.indexOf("}", p + 1); //$NON-NLS-1$
				if (q < 0)
					break;
				QueryField[] qpath = QueryField.findQuerySubField(sb.substring(p + Constants.TV_META.length(), q));
				if (qpath == null) {
					u = q + 1;
					continue;
				}
				String text = null;
				if (target instanceof HoverTestAsset)
					text = ((HoverTestAsset) target).getValueSubstitute(qpath[1]);
				else if (target instanceof Asset) {
					text = qpath[1].value2text(QueryField.obtainFieldValue((Asset) target, qpath[0], qpath[1]), ""); //$NON-NLS-1$
					if (text != null && !text.isEmpty() && Format.MISSINGENTRYSTRING != text)
						text = qpath[1].appendQuestionMark((Asset) target, qpath[1].addUnit(text, " ", "")); //$NON-NLS-1$ //$NON-NLS-2$
					else
						text = null;
				} else {
					@SuppressWarnings("unchecked")
					List<Asset> assets = (List<Asset>) target;
					for (Asset asset : assets) {
						String t = qpath[1].value2text(QueryField.obtainFieldValue(asset, qpath[0], qpath[1]), ""); //$NON-NLS-1$
						if (Format.MISSINGENTRYSTRING.equals(t))
							t = null;
						if (text == null)
							text = t;
						else if (!text.equals(t)) {
							text = QueryField.VALUE_MIXED;
							break;
						}
					}
					text = qpath[1].addUnit(text, " ", ""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				u = replaceContent(sb, p, q + 1, text);
			}
		}
		int len = sb.length();
		char[] t = new char[len];
		sb.getChars(0, len, t, 0);
		parse(t, LINEP);
		return new String(t, 0, removeNils(t));
	}

	private static int replaceContent(StringBuilder sb, int p, int q, String text) {
		if (text != null && !text.isEmpty()) {
			int epos = p + text.length();
			sb.replace(p, q, text).insert(epos, END).insert(p, START);
			return epos + 2;
		}
		sb.replace(p, q, EMPTYS);
		return p + 1;
	}

	@SuppressWarnings("unchecked")
	private static int createMetaBlock(StringBuilder sb, int pos, Object o, int flags) {
		int sbp = pos;
		IDbManager dbManager = Core.getCore().getDbManager();
		QueryField[] queryFields = UiActivator.getDefault().getHoverNodes();
		for (QueryField qfield : queryFields) {
			if (!qfield.testFlags(flags))
				continue;
			String text;
			Object value = null;
			if (o instanceof HoverTestAsset)
				text = ((HoverTestAsset) o).getValueSubstitute(qfield);
			else {
				value = (o instanceof Asset) ? qfield.obtainFieldValue((Asset) o)
						: qfield.obtainFieldValue((List<Asset>) o, null);
				if (qfield == QueryField.TRACK && value != null) {
					StringBuilder sbt = new StringBuilder();
					String[] ids = (String[]) value;
					List<TrackRecordImpl> records = new ArrayList<TrackRecordImpl>(ids.length);
					for (int i = 0; i < ids.length; i++) {
						TrackRecordImpl record = dbManager.obtainById(TrackRecordImpl.class, ids[i]);
						if (record != null)
							records.add(record);
					}
					if (records.size() > 1)
						Collections.sort(records, new Comparator<TrackRecordImpl>() {
							public int compare(TrackRecordImpl t1, TrackRecordImpl t2) {
								return t2.getExportDate().compareTo(t1.getExportDate());
							}
						});
					int j = 0;
					for (TrackRecordImpl t : records) {
						if (sbt.length() > 0)
							sbt.append('\n');
						sbt.append('\t').append(QueryField.TRACK.getFormatter().format(t));
						if (++j > 16)
							break;
					}
					text = sbt.toString();
				} else
					text = qfield.value2text(value, ""); //$NON-NLS-1$
			}
			if (text != null && !text.isEmpty() && text != Format.MISSINGENTRYSTRING) {
				if (qfield.getUnit() != null)
					text = qfield.addUnit(text, " ", ""); //$NON-NLS-1$ //$NON-NLS-2$
				else if (value instanceof String[] && ((String[]) value).length > 0)
					text = UiUtilities.addExplanation(qfield, (String[]) value, text);
				if ((o instanceof Asset))
					text = qfield.appendQuestionMark((Asset) o, text);
				sb.insert(sbp, qfield.getLabel());
				sbp += qfield.getLabel().length();
				sb.insert(sbp, ": "); //$NON-NLS-1$
				sbp += 2;
				sb.insert(sbp, text);
				sbp += text.length();
				sb.insert(sbp++, "\n"); //$NON-NLS-1$
			}
		}
		return sbp;
	}

	private static int removeNils(char[] t) {
		int wp = 0;
		int l = t.length;
		for (int i = 0; i < l; i++)
			if (t[i] > NIL)
				t[wp++] = t[i];
		return wp;
	}

	private void parse(char[] t, int start) {
		int state = start;
		int level = 0;
		int linestart = -1;
		boolean dbqm = false;
		successes[0] = fails[0] = 0;
		for (int i = 0; i < t.length; i++) {
			char c = t[i];
			switch (c) {
			case '\01':
				if (level < states.length) {
					if (state == CG2P)
						states[level - 1] = CG1;
					t[i] = NIL;
					states[level++] = state;
					state = IGN;
				}
				break;
			case '\02':
				if (state == CG2P)
					states[level - 1] = CG1;
				++fails[level];
				if (level == 0)
					++lineFails;
				t[i] = NIL;
				state = FAIL;
				break;
			case '\n':
				if (i < t.length - 1 && t[i + 1] == '\r')
					t[i++] = NIL;
				if (linestart >= 0 && level == 0) {
					lineBreak(t, linestart, i + 1, dbqm);
					linestart = -1;
				}
				state = LINEP;
				break;
			case '\r':
				if (i < t.length - 1 && t[i + 1] == '\n')
					t[i++] = NIL;
				if (linestart >= 0 && level == 0) {
					lineBreak(t, linestart, i + 1, dbqm);
					linestart = -1;
				}
				state = LINEP;
				break;
			default:
				switch (state) {
				case LINEP:
					lineSuccesses = 0;
					lineFails = 0;
					if (c == '?') {
						linestart = i;
						state = LINE2P;
					} else {
						--i;
						state = EXPR;
					}
					break;
				case LINE2P:
					state = EXPR;
					if (!(dbqm = c == '?'))
						--i;
					break;
				case SUCCESS:
				case FAIL:
				case EXPR:
					switch (c) {
					case '|':
						state = state == SUCCESS ? KEEP : state == FAIL ? DISCARD : EXPR;
						break;
					case '(':
						state = CGP;
						break;
					case '?':
						if (level > 0 && (states[level - 1] == CG1 || states[level - 1] == CG2))
							state = CGEP;
						break;
					case ')':
						if (level > 0 && (states[level - 1] == KEEP || states[level - 1] == DISCARD)) {
							int p = startpos[--level];
							switch (states[level]) {
							case KEEP:
								setNil(t, p, i, level);
								t[i] = EMPTY;
								state = SUCCESS;
								++successes[level];
								if (level == 0)
									++lineSuccesses;
								break;
							case DISCARD:
								t[p + 1] = NIL;
								t[p] = START;
								t[i] = END;
								if (state == FAIL) {
									++fails[level];
									if (level == 0)
										++lineFails;
								} else {
									state = SUCCESS;
									++successes[level];
									if (level == 0)
										++lineSuccesses;
								}
								break;
							default:
								state = states[level];
							}
						}
						break;
					default:
						state = EXPR;
						break;
					}
					break;
				case IGN:
					if (c == '\03') {
						t[i] = NIL;
						++successes[--level];
						if (level == 0)
							++lineSuccesses;
						state = SUCCESS;
					}
					break;
				case CGP:
					if (c == '?' && level < states.length) {
						states[level] = state;
						startpos[level++] = i - 1;
						state = CG2P;
						successes[level] = fails[level] = 0;
					} else
						state = EXPR;
					break;
				case CG2P:
					switch (c) {
					case '?':
						states[level - 1] = CG2;
						state = EXPR;
						break;
					case '(':
						states[level - 1] = CG1;
						state = CGP;
						break;
					default:
						states[level - 1] = CG1;
						state = EXPR;
					}
					break;
				case CGEP:
					switch (c) {
					case ')':
						int p = startpos[--level];
						switch (states[level]) {
						case CG1:
							if (successes[level + 1] == 0 && fails[level + 1] > 0) {
								setNil(t, p, i, level);
								t[i] = EMPTY;
								state = FAIL;
								++fails[level];
								if (level == 0)
									++lineFails;
							} else {
								t[p + 1] = t[i - 1] = NIL;
								t[p] = START;
								t[i] = END;
								state = SUCCESS;
								++successes[level];
								if (level == 0)
									++lineSuccesses;
							}
							break;
						case CG2:
							if (fails[level + 1] > 0) {
								setNil(t, p, i, level);
								t[i] = EMPTY;
								state = FAIL;
								++fails[level];
								if (level == 0)
									++lineFails;
							} else {
								t[p + 1] = t[p + 2] = t[i - 1] = NIL;
								t[p] = START;
								t[i] = END;
								state = SUCCESS;
								++successes[level];
								if (level == 0)
									++lineSuccesses;
							}
						}
						break;
					default:
						state = EXPR;
					}
					break;
				case KEEP:
				case DISCARD:
					if (c == '(' && level < states.length) {
						states[level] = state;
						startpos[level++] = i - 1;
					}
					state = EXPR;
				}
			}
		}
		if (linestart >= 0 && level == 0)
			lineBreak(t, linestart, t.length, dbqm);
	}

	private void lineBreak(char[] t, int linestart, int to, boolean dbqm) {
		if (lineFails > 0 && (dbqm || lineSuccesses == 0))
			setNil(t, linestart, to, 0);
		else
			t[linestart] = NIL;
	}

	private void setNil(char[] t, int from, int to, int level) {
		for (int i = from; i < to; i++) {
			switch (t[i]) {
			case EMPTY:
				--successes[level];
				break;
			case START:
				--fails[level];
				break;
			}
			t[i] = NIL;
		}
	}

	private Asset getTestAsset() {
		if (testAsset == null) {
			Date now = new Date();
			String volume = Constants.WIN32 ? "C:" : ""; //$NON-NLS-1$ //$NON-NLS-2$
			testAsset = new AssetImpl(Messages.TemplateProcessor_name, "file://filepath", volume, IVolumeManager.ONLINE, //$NON-NLS-1$
					500000l, Messages.TemplateProcessor_comments, "ORF", "image/raw", 0, 0, 4000, 3000, //$NON-NLS-1$//$NON-NLS-2$
					Asset_type.ori_l, 1024, true, "RGB", (Date) null, //$NON-NLS-1$
					"file://voicefilepath", volume, now, 0, now, //$NON-NLS-1$
					Messages.TemplateProcessor_importer, Constants.STATE_CONVERTED, 3, Messages.TemplateProcessor_rater,
					-1, Messages.TemplateProcessor_user1, Messages.TemplateProcessor_user2, 3904, 5200, 0, 2, 1, 8,
					300f, 300f, now, Messages.TemplateProcessor_description, "Olympus", "Pen-F", "Version 3.0", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					"100OLYMP/P123456.ORF", "ID34556", //$NON-NLS-1$//$NON-NLS-2$
					"2019 John Doe", 1, Messages.TemplateProcessor_icc, now, 0.002f, 5.6f, 5, //$NON-NLS-1$
					"", 200, 0.002f, 5.6f, 0f, 0f, 2f, 30f, 5, 1, false, 0, //$NON-NLS-1$
					2, false, false, 0f, 7.5f, 3f, 6759f, 6759f, 2f, 1, 3, 0, "Auto", 2f, 15, 2f, 0.015f, 3.73f, 14f, //$NON-NLS-1$
					3, 2, 1, 0, 1, 0, 3, "Laowa", "42424242", "1234567890", 49.25555f, 4.02696f, 50f, now, Double.NaN, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					0f, "", Double.NaN, //$NON-NLS-1$
					"", Double.NaN, "", Double.NaN, Double.NaN, "", Double.NaN, Double.NaN, "", now, 0, "John Doe", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					Messages.TemplateProcessor_for_test, "From the Scene", Messages.TemplateProcessor_test_image, //$NON-NLS-1$
					Messages.TemplateProcessor_ignore, "Job 33d", Messages.TemplateProcessor_test, 1, //$NON-NLS-1$
					Messages.TemplateProcessor_art_director, "John Doe", Messages.TemplateProcessor_worldwide, //$NON-NLS-1$
					Messages.TemplateProcessor_portfolio, now, Messages.TemplateProcessor_model_info, 17,
					Messages.TemplateProcessor_event, // $NON-NLS-2$
					now, 3904, 5200, "", Constants.ANALOGTYPE_UNKNOWN, -1, "", //$NON-NLS-1$ //$NON-NLS-2$
					"", 1f, 3, 300f, 800f, 2, now, "", true); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return testAsset;
	}

}
