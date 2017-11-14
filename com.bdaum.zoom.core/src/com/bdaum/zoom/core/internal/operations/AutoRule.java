package com.bdaum.zoom.core.internal.operations;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;

public class AutoRule {
	public static final String VALUE_UNDEFINED = "-"; //$NON-NLS-1$
	private QueryField qfield;
	private double[] intervals;
	private Object[] enumeration;
	private List<String> values;
	private String name;

	public AutoRule(String name, QueryField qfield, double[] intervals, Object[] enumeration, List<String> values) {
		this.name = name;
		this.qfield = qfield;
		this.intervals = intervals;
		this.enumeration = enumeration;
		this.values = values;
	}

	public AutoRule() {
	}

	public static List<AutoRule> constructRules(String text) {
		int n = 0;
		List<AutoRule> autoRules = new ArrayList<AutoRule>();
		if (text != null) {
			StringTokenizer st = new StringTokenizer(text, "\n"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				try {
					String token = st.nextToken();
					QueryField qfield;
					String name = null;
					double[] parms = null;
					Object[] enumeration = null;
					List<String> values = null;
					int p = token.indexOf("::"); //$NON-NLS-1$
					if (p > 0) {
						name = token.substring(0, p);
						token = token.substring(p + 2);
					} else
						name = "Autorule" + (++n); //$NON-NLS-1$
					p = token.indexOf(':');
					if (p > 0) {
						qfield = QueryField.findQueryField(token.substring(0, p));
						String para = token.substring(p + 1);
						List<String> list = Core.fromStringList(para, ";"); //$NON-NLS-1$
						int i = 0;
						switch (qfield.getAutoPolicy()) {
						case QueryField.AUTO_CONTAINS:
							values = list;
							break;
						case QueryField.AUTO_SELECT:
							values = new ArrayList<String>(list.size());
							for (String s : list)
								values.add("¬".equals(s) ? "-" : s); //$NON-NLS-1$//$NON-NLS-2$
							break;
						case QueryField.AUTO_DISCRETE:
							if (qfield.getEnumeration() != null) {
								enumeration = new Object[list.size()];
								int type = qfield.getType();
								for (String s : list) {
									if (type == QueryField.T_STRING)
										enumeration[i++] = s;
									else {
										try {
											enumeration[i] = Integer.parseInt(s);
										} catch (NumberFormatException e) {
											enumeration[i] = -1;
										}
										++i;
									}
								}
								break;
							}
							if (qfield.getType() == QueryField.T_BOOLEAN) {
								enumeration = new Object[list.size()];
								for (String s : list) {
									enumeration[i] = Boolean.parseBoolean(s);
									++i;
								}
								break;
							}
							//$FALL-THROUGH$
						default:
							parms = new double[list.size()];
							for (String s : list)
								parms[i++] = Double.parseDouble(s);
							break;
						}
					} else
						qfield = QueryField.findQueryField(token);
					autoRules.add(new AutoRule(name, qfield, parms, enumeration, values));
				} catch (NumberFormatException e) {
					// ignore invalid values
				}
			}
		}
		return autoRules;
	}

	public double[] getIntervals() {
		return intervals;
	}

	public boolean hasCustomIntervals() {
		return intervals != null && intervals.length > 1;
	}

	public String getIntervalSpec() {
		if (intervals == null)
			return ""; //$NON-NLS-1$
		NumberFormat af = NumberFormat.getNumberInstance();
		af.setMaximumFractionDigits(3);
		af.setMinimumFractionDigits(0);
		StringBuilder sb = new StringBuilder();
		for (double d : intervals) {
			if (sb.length() > 0)
				sb.append("; "); //$NON-NLS-1$
			sb.append(af.format(d));
		}
		return sb.toString();
	}

	public String getEnumerationSpec() {
		if (enumeration == null)
			return ""; //$NON-NLS-1$
		String[] labels = new String[enumeration.length];
		for (int i = 0; i < enumeration.length; i++)
			labels[i] = qfield.formatScalarValue(enumeration[i]);
		return Core.toStringList(labels, "; "); //$NON-NLS-1$
	}

	/**
	 * @return qfield
	 */
	public QueryField getQfield() {
		return qfield;
	}

	/**
	 * @param qfield
	 *            das zu setzende Objekt qfield
	 */
	public void setQfield(QueryField qfield) {
		this.qfield = qfield;
	}

	/**
	 * @param intervals
	 *            das zu setzende Objekt intervals
	 */
	public void setIntervals(double[] intervals) {
		this.intervals = intervals;
	}

	/**
	 * @return enumeration
	 */
	public Object[] getEnumeration() {
		return enumeration;
	}

	/**
	 * @param enumeration
	 *            das zu setzende Objekt enumeration
	 */
	public void setEnumeration(Object[] enumeration) {
		this.enumeration = enumeration;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	/**
	 * @return values
	 */
	public List<String> getValues() {
		return values;
	}

	public String getValueSpec() {
		return values == null ? "" : Core.toStringList(values, ';'); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (name != null && !name.isEmpty())
			sb.append(name).append("::"); //$NON-NLS-1$
		sb.append(qfield.getId());
		switch (qfield.getAutoPolicy()) {
		case QueryField.AUTO_CONTAINS:
			sb.append(':');
			for (String d : values) {
				if (sb.charAt(sb.length() - 1) != ':')
					sb.append(';');
				sb.append(d);
			}
			break;
		case QueryField.AUTO_SELECT:
			sb.append(':');
			for (String d : values) {
				if (sb.charAt(sb.length() - 1) != ':')
					sb.append(';');
				sb.append("-".equals(d) ? "¬" : d); //$NON-NLS-1$ //$NON-NLS-2$
			}
			break;
		case QueryField.AUTO_DISCRETE:
			if (qfield.getEnumeration() != null) {
				sb.append(':');
				for (Object d : enumeration) {
					if (sb.charAt(sb.length() - 1) != ':')
						sb.append(';');
					sb.append(d);
				}
				break;
			}
			if (qfield.getType() == QueryField.T_BOOLEAN) {
				sb.append(':');
				for (Object d : enumeration) {
					if (sb.charAt(sb.length() - 1) != ':')
						sb.append(';');
					sb.append(d);
				}
				break;
			}
			//$FALL-THROUGH$
		default:
			sb.append(':');
			for (double d : intervals) {
				if (sb.charAt(sb.length() - 1) != ':')
					sb.append(';');
				sb.append(d);
			}
		}
		return sb.toString();
	}

	public String getBooleanSpec() {
		if (enumeration == null)
			return ""; //$NON-NLS-1$
		String[] labels = new String[enumeration.length];
		for (int i = 0; i < enumeration.length; i++)
			labels[i] = String.valueOf(enumeration[i]);
		return Core.toStringList(labels, "; "); //$NON-NLS-1$
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}