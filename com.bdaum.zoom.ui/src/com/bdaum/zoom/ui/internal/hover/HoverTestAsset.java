package com.bdaum.zoom.ui.internal.hover;

import java.util.Date;

import com.bdaum.zoom.core.QueryField;

public class HoverTestAsset extends HoverTestObject {

	public String getValueSubstitute(QueryField queryField) {
		Object value = null;
		Object enumeration = queryField.getEnumeration();
		if (queryField.getCard() == 1) {
			switch (queryField.getType()) {
			case QueryField.T_BOOLEAN:
				value = Boolean.TRUE;
				break;
			case QueryField.T_POSITIVEINTEGER:
			case QueryField.T_INTEGER:
				value = enumeration instanceof int[] ? ((int[]) enumeration)[1] : 60;
				break;
			case QueryField.T_POSITIVEFLOAT:
			case QueryField.T_FLOAT:
			case QueryField.T_CURRENCY:
			case QueryField.T_FLOATB:
				value = 60d;
				break;
			case QueryField.T_DATE:
				value = new Date();
				break;
			case QueryField.T_POSITIVELONG:
			case QueryField.T_LONG:
				value = 60L;
				break;
			default:
				value = enumeration instanceof String[] ? ((String[]) enumeration)[1] : computeExampleFromName(queryField.getLabel());
				break;
			}
		} else
			switch (queryField.getType()) {
			case QueryField.T_POSITIVEINTEGER:
			case QueryField.T_INTEGER:
				value = enumeration instanceof int[] ? new int[] { ((int[]) enumeration)[1] } : new int[] { 60 };
				break;
			case QueryField.T_POSITIVELONG:
			case QueryField.T_LONG:
				value = new long[] { 60L };
				break;
			case QueryField.T_POSITIVEFLOAT:
			case QueryField.T_FLOAT:
				value = new double[] { 60d };
				break;
			default:
				value = enumeration instanceof String[] ? new String[] { ((String[]) enumeration)[1] } : new String[] { computeExampleFromName(queryField.getLabel()) };
				break;
			}
		if (value == null)
			return null;
		if (queryField == QueryField.TRACK)
			return Messages.HoverTestAsset_export_track;
		return queryField.value2text(value, ""); //$NON-NLS-1$
	}

	private static String computeExampleFromName(String s) {
		StringBuilder sb = new StringBuilder(s);
		int i = 0;
		while (i < sb.length()) {
			if (sb.charAt(i) == ' ') {
				sb.deleteCharAt(i);
				if (i < sb.length())
					sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
			} else
				++i;
		}
		sb.append("1"); //$NON-NLS-1$
		return sb.toString();
	}

}
