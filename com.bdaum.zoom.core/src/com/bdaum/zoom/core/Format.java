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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.core;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.TrackRecord_type;
import com.bdaum.zoom.cat.model.asset.TrackRecord;

/**
 * This class provided a set of field formatters that can also parse input
 * strings and translate them into the internal field format
 *
 */
public class Format {

	private static final String AM = Messages.Format_am;
	private static final String PM = Messages.Format_pm;

	/**
	 * Representation of missing entries
	 */
	public static final String MISSINGENTRYSTRING = " - "; //$NON-NLS-1$
	
	public static final String EDITABLEINDICATOR = "»"; //$NON-NLS-1$

	private static final class CurrencyExpressionFormatter implements IFormatter {

		public String toString(Object obj) {
			if (obj != null) {
				try {
					double v = evaluateCurrencyExpression(obj.toString());
					return (v != 0d) ? getCurrencyNumberFormat().format(v) + "+" //$NON-NLS-1$
							: getCurrencyNumberFormat().format(v);
				} catch (ParseException e) {
					// do nothing
				}
				return obj.toString();
			}
			return null;
		}

		public Object fromString(String s) throws ParseException {
			return evaluateCurrencyExpression(s);
		}

	}

	private static final class GpsFormatter implements IFormatter {

		private String neg;
		private String pos;

		GpsFormatter(String neg, String pos) {
			this.neg = neg;
			this.pos = pos;
		}

		public String toString(Object o) {
			double d = ((Double) o).doubleValue();
			if (Double.isNaN(d))
				return MISSINGENTRYSTRING;
			StringBuilder sb = new StringBuilder();
			if (d < 0) {
				sb.append(neg);
				d = -d;
			} else
				sb.append(pos);
			int degrees = (int) d;
			int minutes = (int) ((d - degrees) * 60);
			double seconds = ((d - degrees) * 60 - minutes) * 60;
			NumberFormat af = NumberFormat.getNumberInstance();
			af.setMaximumFractionDigits(2);
			sb.append(degrees).append('°').append(minutes).append('\'').append(af.format(seconds)).append('"');
			return sb.toString();
		}

		public Object fromString(String s) throws ParseException {
			s = s.trim();
			NumberFormat af = NumberFormat.getNumberInstance();
			af.setMaximumFractionDigits(10);
			try {
				if (s.indexOf('°') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\'') >= 0 || s.indexOf(' ') >= 0) {
					double sign = 1d;
					if (s.startsWith(neg)) {
						sign = -1d;
						s = s.substring(1);
					} else if (s.startsWith(pos)) {
						s = s.substring(1);
					}
					StringTokenizer st = new StringTokenizer(s + " ", "°'\" ", //$NON-NLS-1$ //$NON-NLS-2$
							true);
					String n = null;
					double d = 0;
					int i = 0;
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if ("°".equals(token)) { //$NON-NLS-1$
							if (n != null) {
								d += Integer.parseInt(n);
								n = null;
								i = 1;
							}
						} else if ("'".equals(token)) { //$NON-NLS-1$
							if (n != null) {
								d += Integer.parseInt(n) / 60d;
								n = null;
								i = 2;
							}
						} else if ("\"".equals(token)) { //$NON-NLS-1$
							if (n != null) {
								d += af.parse(n).doubleValue() / 3600d;
								n = null;
								i = 3;
							}
						} else if (" ".equals(token)) { //$NON-NLS-1$
							if (n != null) {
								switch (i) {
								case 0:
									d += Integer.parseInt(n);
									break;
								case 1:
									d += Integer.parseInt(n) / 60d;
									break;
								case 2:
									d += af.parse(s).doubleValue() / 3600d;
									break;
								}
								n = null;
								i++;
							}
						} else
							n = token;
					}
					return d * sign;
				}
				return af.parse(s).doubleValue();
			} catch (ParseException e) {
				throw new ParseException(Messages.Format_bad_lat_lon, 0);
			}
		}
	}

	/**
	 * Exposure time
	 */
	public final static IFormatter exposureTimeFormatter = new IFormatter() {

		public String toString(Object o) {
			double d = ((Double) o).doubleValue();
			if (Double.isNaN(d))
				return MISSINGENTRYSTRING;
			if (d > 0.5d || d <= 0) {
				NumberFormat af = NumberFormat.getNumberInstance();
				af.setMaximumFractionDigits(1);
				return af.format(d);
			}
			return "1/" + ((int) (1 / d)); //$NON-NLS-1$
		}

		public Object fromString(String s) throws ParseException {
			s = s.trim();
			try {
				NumberFormat af = NumberFormat.getNumberInstance();
				af.setMaximumFractionDigits(3);
				return (s.startsWith("1/")) ? 1.0 / af.parse(s.substring(2)) //$NON-NLS-1$
						.doubleValue() : af.parse(s).doubleValue();
			} catch (ParseException e) {
				throw new ParseException(Messages.Format_bad_exp_time, 0);
			}
		}
	};
	/**
	 * Aperture
	 */
	public final static IFormatter apertureFormatter = new IFormatter() {

		public String toString(Object o) {
			double d = ((Double) o).doubleValue();
			if (Double.isNaN(d) || Double.isInfinite(d))
				return MISSINGENTRYSTRING;
			NumberFormat af = NumberFormat.getNumberInstance();
			af.setMaximumFractionDigits(1);
			return af.format(Math.pow(2d, d / 2));
		}

		public Object fromString(String s) throws ParseException {
			s = s.trim();
			try {
				NumberFormat af = NumberFormat.getNumberInstance();
				af.setMaximumFractionDigits(10);
				return 2d * Math.log(af.parse(s).doubleValue()) / Math.log(2);
			} catch (Exception e) {
				throw new ParseException(Messages.Format_bad_aperture, 0);
			}
		}
	};

	/**
	 * Geographic distance
	 */
	public final static IFormatter distanceFormatter = new IFormatter() {

		public String toString(Object o) {
			double d = ((Double) o).doubleValue();
			if (Double.isNaN(d) || Double.isInfinite(d))
				return MISSINGENTRYSTRING;
			NumberFormat af = NumberFormat.getNumberInstance();
			af.setMaximumFractionDigits(3);
			return af.format(d);
		}

		public Object fromString(String s) throws ParseException {
			NumberFormat af = NumberFormat.getNumberInstance();
			af.setMaximumFractionDigits(10);
			return af.parse(s.trim()).doubleValue();
		}
	};
	/**
	 * Latitude
	 */
	public final static IFormatter latitudeFormatter = new GpsFormatter("S", "N"); //$NON-NLS-1$ //$NON-NLS-2$
	/**
	 * Longitude
	 */
	public final static IFormatter longitudeFormatter = new GpsFormatter("W", "E"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Altitude
	 */
	public final static IFormatter altitudeFormatter = new IFormatter() {

		public Object fromString(String s) throws ParseException {
			NumberFormat af = NumberFormat.getNumberInstance();
			af.setMaximumFractionDigits(2);
			return af.parse(s);
		}

		public String toString(Object o) {
			double d = ((Double) o).doubleValue();
			return (Double.isNaN(d) || Double.isInfinite(d)) ? MISSINGENTRYSTRING : String.valueOf((int) (d + 0.5d));
		}

	};

	/**
	 * Boolean values
	 */
	public final static IFormatter booleanFormatter = new IFormatter() {

		public String toString(Object d) {
			return ((Boolean) d).booleanValue() ? Messages.QueryField_yes : Messages.QueryField_no;
		}

		public Object fromString(String s) {
			return (s.equalsIgnoreCase(Messages.QueryField_yes) || s.equals("1")) //$NON-NLS-1$
					? true : Boolean.parseBoolean(s);
		}
	};

	/**
	 * Date values without time
	 */
	public static IFormatter dayFormatter = new IFormatter() {

		public String toString(Object d) {
			return Constants.IPTCDF.format((Date) d);
		}

		public Object fromString(String s) throws ParseException {
			return Constants.IPTCDF.parse(s);
		}
	};

	/**
	 * Date values without time
	 */
	public static IFormatter timeFormatter = new IFormatter() {

		StringBuilder sb = new StringBuilder();

		public String toString(Object t) {
			int time = (Integer) t;
			if (time < 0)
				return Messages.Format_undefined;
			String ampm = null;
			int hour = time / 60;
			int minute = time % 60;
			sb.setLength(0);
			if (AM.length() > 1) {
				ampm = hour > 12 || hour == 12 && minute > 0 ? PM : AM;
				if (hour > 12)
					hour -= 12;
			}
			if (hour < 10)
				sb.append('0');
			sb.append(hour).append(':');
			if (minute < 10)
				sb.append('0');
			sb.append(minute);
			if (ampm != null)
				sb.append(' ').append(ampm);
			return sb.toString();
		}

		public Object fromString(String s) throws ParseException {
			try {
				s = s.trim();
				int offset = 0;
				if (s.endsWith(AM))
					s = s.substring(0, s.length()-AM.length()).trim();
				else if (s.endsWith(PM)) {
					s = s.substring(0, s.length()-PM.length()).trim();
					offset = 720;
				}
				int p = s.indexOf(':');
				if (p < 0)
					return Integer.parseInt(s) * 60;
				return Integer.parseInt(s.substring(0, p)) * 60 + Integer.parseInt(s.substring(p + 1) + offset);
			} catch (NumberFormatException e) {
				throw new ParseException(Messages.Format_time_invalid, 0);
			}
		}
	};

	/**
	 * File size
	 */
	public final static IFormatter sizeFormatter = new IFormatter() {

		public Object fromString(String s) throws ParseException {
			s = s.trim();
			try {
				if (s.endsWith("GB")) { //$NON-NLS-1$
					NumberFormat af = NumberFormat.getNumberInstance();
					af.setMaximumFractionDigits(10);
					return (long) (af.parse(s.substring(0, s.length() - 2).trim()).doubleValue() * 1000000000);
				}
				if (s.endsWith("MB")) { //$NON-NLS-1$
					NumberFormat af = NumberFormat.getNumberInstance();
					af.setMaximumFractionDigits(10);
					return (long) (af.parse(s.substring(0, s.length() - 2).trim()).doubleValue() * 1000000);
				}
				if (s.endsWith("kB") || s.endsWith("KB")) //$NON-NLS-1$ //$NON-NLS-2$
					return (long) (NumberFormat.getNumberInstance().parse(s.substring(0, s.length() - 2).trim())
							.doubleValue() * 1000);
				return Integer.parseInt(s);
			} catch (Exception e) {
				throw new ParseException(Messages.Format_bad_file_size, 0);
			}
		}

		public String toString(Object o) {
			NumberFormat af = NumberFormat.getNumberInstance();
			af.setMaximumFractionDigits(2);
			long d = ((Long) o).longValue();
			if (d >= 1000000000)
				return af.format(d / 1000000000d) + " GB"; //$NON-NLS-1$
			if (d >= 1000000)
				return af.format(d / 1000000d) + " MB"; //$NON-NLS-1$
			if (d >= 1000)
				return af.format(d / 1000d) + " kB"; //$NON-NLS-1$
			return String.valueOf(d);
		}

	};

	/**
	 * URIs
	 */
	public final static IFormatter uriFormatter = new IFormatter() {

		public Object fromString(String s) {
			return Core.encodeUrlSegment(s.trim());
		}

		public String toString(Object o) {
			return Core.decodeUrl((String) o);
		}

	};

	/**
	 * Track entries
	 */
	public static final SimpleDateFormat trackdateFormat = new SimpleDateFormat(Messages.QueryField_track_date_format);
	public final static IFormatter trackFormatter = new IFormatter() {

		public Object fromString(String s) {
			return null;
		}

		public String toString(Object o) {
			if (o instanceof TrackRecord) {
				TrackRecord record = (TrackRecord) o;
				String date = trackdateFormat.format(record.getExportDate());
				String postfix = record.getReplaced() ? Messages.QueryField_replacement : ""; //$NON-NLS-1$
				String serviceName = record.getServiceName();
				String target = record.getTarget();
				if (TrackRecord_type.type_community.equals(record.getType())) {
					return NLS.bind(Messages.QueryField_uploaded_to_account,
							new Object[] { serviceName, target, date, postfix });
				} else if (TrackRecord_type.type_ftp.equals(record.getType())) {
					return NLS.bind(Messages.QueryField_uploaded_to_ftp,
							new Object[] { target, serviceName, date, postfix });
				} else if (TrackRecord_type.type_ftp.equals(record.getType())) {
					return NLS.bind(Messages.QueryField_send_via_email, date);
				} else
					return Messages.QueryField_unknown_track_record_type;
			}
			return (String) o;
		}

	};

	// /**
	// * Regions
	// */
	// public static final IFormatter rectangleFormatter = new IFormatter() {
	//
	// public String toString(Object obj) {
	// NumberFormat af = NumberFormat.getNumberInstance(Locale.US);
	// af.setMaximumFractionDigits(6);
	// String regionId = (String) obj;
	// try {
	// int leadingZeros = Math.max(0, 16 - regionId.length());
	// double x1 = Utilities.parseHex(regionId, 0 - leadingZeros,
	// 4 - leadingZeros) / 65535d;
	// double y1 = Utilities.parseHex(regionId, 4 - leadingZeros,
	// 8 - leadingZeros) / 65535d;
	// double x2 = Utilities.parseHex(regionId, 8 - leadingZeros,
	// 12 - leadingZeros) / 65535d;
	// double y2 = Utilities.parseHex(regionId, 12 - leadingZeros,
	// 16 - leadingZeros) / 65535d;
	// return MessageFormat.format(
	// "{0}, {1}, {2}, {3}", af.format(x1), af.format(y1), //$NON-NLS-1$
	// af.format(x2 - x1), af.format(y2 - y1));
	// } catch (NumberFormatException e) {
	// return null;
	// }
	// }
	//
	// public Object fromString(String s) throws ParseException {
	// StringBuilder sb = new StringBuilder(16);
	// StringTokenizer st = new StringTokenizer(s, ", "); //$NON-NLS-1$
	// try {
	// double x = Double.parseDouble(st.nextToken());
	// double y = Double.parseDouble(st.nextToken());
	// double w = Double.parseDouble(st.nextToken());
	// double h = Double.parseDouble(st.nextToken());
	// Utilities.toHex(sb, (int) (x * 65535 + 0.5d));
	// Utilities.toHex(sb, (int) (y * 65535 + 0.5d));
	// Utilities.toHex(sb, (int) ((x + w) * 65535 + 0.5d));
	// Utilities.toHex(sb, (int) ((y + h) * 65535 + 0.5d));
	// return sb.toString();
	// } catch (NoSuchElementException e) {
	// return null;
	// } catch (NumberFormatException e) {
	// return null;
	// }
	// }
	// };

	public static final IFormatter currencyExpressionFormatter = new CurrencyExpressionFormatter();

	public static int getCurrencyDigits() {
		Currency curr = null;
		try {
			curr = Currency.getInstance(Locale.getDefault());
		} catch (Exception e) {
			// do nothing
		}
		return curr == null ? 2 : curr.getDefaultFractionDigits();
	}

	private static NumberFormat nf;

	public static NumberFormat getCurrencyNumberFormat() {
		if (nf == null) {
			int digits = getCurrencyDigits();
			nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(digits);
			nf.setMinimumFractionDigits(digits);
		}
		return nf;
	}

	public static double evaluateCurrencyExpression(String expression) throws ParseException {
		double total = 0d;
		double sign = 1d;
		String ops = "-+"; //$NON-NLS-1$
		StringTokenizer st = new StringTokenizer(expression, ops, true);
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			int p = ops.indexOf(token);
			if (p >= 0)
				sign = 2 * p - 1;
			else
				total += sign * evaluateProduct(token);
		}
		return total;

	}

	private static double evaluateProduct(String s) throws ParseException {
		double total = 1d;
		boolean divide = false;
		String ops = "/*"; //$NON-NLS-1$
		StringTokenizer st = new StringTokenizer(s, ops, true);
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			int p = ops.indexOf(token);
			if (p >= 0)
				divide = p == 0;
			else if (divide)
				total /= parseCurreny(token);
			else
				total *= parseCurreny(token);
		}
		return total;
	}

	private static double parseCurreny(String token) throws ParseException {
		return getCurrencyNumberFormat().parse(token).doubleValue();
	}

}
