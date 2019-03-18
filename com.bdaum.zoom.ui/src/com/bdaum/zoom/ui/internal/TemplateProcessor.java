package com.bdaum.zoom.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.ui.internal.hover.HoverTestAsset;
import com.bdaum.zoom.ui.internal.hover.IHoverContext;
import com.bdaum.zoom.ui.internal.hover.IHoverContribution;
import com.bdaum.zoom.ui.internal.hover.IHoverItem;

public class TemplateProcessor {

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

	private static String processTemplate(String template, IHoverContribution contrib, Object object,
			IHoverContext context, String[] variables, boolean isTitle) {
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
		cleanup(sb);
		return sb.toString();
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
						sbt.append('\t').append(QueryField.TRACK.getFormatter().toString(t));
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

	private static int replaceContent(StringBuilder sb, int p, int q, String text) {
		if (text != null && !text.isEmpty()) {
			sb.replace(p, q, text).insert(p, '\01');
			return p + text.length();
		}
		int u = sb.lastIndexOf("\n", p); //$NON-NLS-1$
		if (u < p - 2 && sb.charAt(u + 1) == '?' && sb.charAt(u + 2) == '?') {
			int o = sb.indexOf("\n", q); //$NON-NLS-1$
			if (o < 0)
				o = sb.length() - 1;
			if (u < 1)
				u = 0;
			sb.delete(u, o + 1);
			return u;
		}
		u = sb.lastIndexOf("(??", p); //$NON-NLS-1$
		if (u >= 0) {
			int o = sb.indexOf("?)", q); //$NON-NLS-1$
			if (o >= q) {
				sb.delete(u, o + 2);
				return u;
			}
		}
		sb.delete(p, q);
		return p;
	}

	private static void cleanup(StringBuilder sb) {
		while (true) {
			int p = sb.indexOf("(??"); //$NON-NLS-1$
			if (p < 0)
				break;
			int q = sb.indexOf("?)", p + 3); //$NON-NLS-1$
			if (q < 0)
				break;
			sb.delete(q, q + 2);
			sb.delete(p, p + 3);
		}
		int u = 0;
		while (u < sb.length() - 1) {
			int o = sb.indexOf("\n", u); //$NON-NLS-1$
			if (o < 0)
				o = sb.length();
			if (sb.charAt(u) == '?' && sb.charAt(u + 1) == '?') {
				sb.delete(u, u + 2);
				u = o - 1;
			} else
				u = o + 1;
		}
		while (true) {
			int p = sb.indexOf("(?"); //$NON-NLS-1$
			if (p < 0)
				break;
			int q = sb.indexOf("?)", p + 2); //$NON-NLS-1$
			if (q < 0)
				break;
			int r = sb.indexOf("\01", p); //$NON-NLS-1$
			if (r >= 0 && r < q) {
				sb.delete(q, q + 2);
				sb.delete(p, p + 2);
			} else
				sb.delete(p, q + 2);
		}
		u = 0;
		while (u < sb.length()) {
			int o = sb.indexOf("\n", u); //$NON-NLS-1$
			if (o < 0)
				o = sb.length();
			if (sb.charAt(u) == '?') {
				int p = sb.indexOf("\01", u); //$NON-NLS-1$
				if (p >= 0 && p < o) {
					sb.deleteCharAt(u);
					u = o;
				} else
					sb.delete(u, o < sb.length() ? o + 1 : o);
			} else
				u = o + 1;
		}
		u = 0;
		while (true) {
			int p = sb.indexOf("\01", u); //$NON-NLS-1$
			if (p < 0)
				break;
			sb.deleteCharAt(u = p);
		}

	}

}
