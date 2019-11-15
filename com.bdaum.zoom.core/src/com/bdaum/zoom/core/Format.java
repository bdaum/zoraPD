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
 * (c) 2009-2018 Berthold Daum  
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
import com.bdaum.zoom.core.internal.Utilities;

/**
 * This class provided a set of field formatters that can also parse input
 * strings and translate them into the internal field format
 *
 */
public class Format {
	
	/*** language dependent date formats ***/
	

	public static final ThreadLocal<SimpleDateFormat> TRACK_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.QueryField_track_date_format);
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> EMDY_TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_emdy_time);
		}
	};
	
	
	public static final ThreadLocal<SimpleDateFormat> MDY_TIME_SHORT_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_mdy_time_short);
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> MDY_TIME_SECS_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_mdy_time_secs);
		}
	};

	
	public static final ThreadLocal<SimpleDateFormat> EMDY_TIME_LONG_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_emdy_time_long);
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> MDY_TIME_LONG_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_mdy_time_long);
		}
	};

	public static final ThreadLocal<SimpleDateFormat> DFDT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Constants_ExtenalDateTimeFormat);
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> YMDT_SLASH = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_ymdt_slash);
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> WEEK = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_week);
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> WEEK_WY_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_week_wy);
		}
	};

	public static final ThreadLocal<SimpleDateFormat> WEEK_EWY_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_week_ewy);
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> MDYHM_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_mdy_time);
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> MDY_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_mdy);
		}
	};

	public static final ThreadLocal<SimpleDateFormat> MY_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_my);
		}
	};


	public static final ThreadLocal<SimpleDateFormat> YMD_TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_ymd_time);
		}
	};

	public static final ThreadLocal<SimpleDateFormat> YMD_SLASH_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_ymd_slash);
		}
	};

	public static final ThreadLocal<SimpleDateFormat> LDY_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_ldy);
		}
	};
	public static final ThreadLocal<SimpleDateFormat> LY_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_ly);
		}
	};
	public static final ThreadLocal<SimpleDateFormat> LRY_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_lry);
		}
	};

	public static final ThreadLocal<SimpleDateFormat> LY_SHORT_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Messages.Format_ly_short);
		}
	};

	
	/*** language invariant date formats ***/
	
	public static final ThreadLocal<SimpleDateFormat> DATE_TIME_ZONED_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy:MM:dd HH:mm:ss Z"); //$NON-NLS-1$
		}
	};

	public static final ThreadLocal<SimpleDateFormat> YEAR_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy"); //$NON-NLS-1$
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> YEAR_MONTH_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM"); //$NON-NLS-1$
		}
	};

	public static final ThreadLocal<SimpleDateFormat> YEAR_MONTH_DAY_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
		}
	};

	public static final ThreadLocal<SimpleDateFormat> YEAR_WEEK_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("YYYY-'W'ww"); //$NON-NLS-1$
		}
	};

	public static final ThreadLocal<SimpleDateFormat> YEAR_WEEK_DAY_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("YYYY-'W'ww-uu"); //$NON-NLS-1$
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> YEAR_DAY_TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-DDD HH:mm:ss"); //$NON-NLS-1$
		}
	};


	public static final ThreadLocal<SimpleDateFormat> DATE_TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy:MM:dd HH:mm:ss"); //$NON-NLS-1$
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> DATE_TIME_HYPHEN_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
		}
	};

	
	public static final ThreadLocal<SimpleDateFormat> DATE_TIME_SLASH_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"); //$NON-NLS-1$
		}
	};

	
	public static final ThreadLocal<SimpleDateFormat> XML_DATE_TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:s"); //$NON-NLS-1$
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> XML_DATE_TIME_XZONED_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"); //$NON-NLS-1$
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> XML_DATE_TIME_ZZONED_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); //$NON-NLS-1$
		}
	};



	public static final ThreadLocal<SimpleDateFormat> WEEKDAY_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("EEEEEEEEEEEEEEEEEEEE"); //$NON-NLS-1$
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat>MONTH_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("MMMMMMMMM"); //$NON-NLS-1$
		}
	};

	
	public static final ThreadLocal<SimpleDateFormat> CODES_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyyMMdd'T'HHmmssZ"); //$NON-NLS-1$
		}
	};
	
	public static final ThreadLocal<SimpleDateFormat> HMS_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
		}
	};




	private static final String AM = Messages.Format_am;
	private static final String PM = Messages.Format_pm;

	/**
	 * Representation of missing entries
	 */
	public static final String MISSINGENTRYSTRING = " - "; //$NON-NLS-1$

	public static final String EDITABLEINDICATOR = "»"; //$NON-NLS-1$

	public final static IFormatter plusMinusFormatter = new PlusMinusFormatter();

	private static final class PlusMinusFormatter implements IFormatter {

		public String toString(Object o) {
			int d = (Integer) o;
			return d > 0 ? "+" + d : String.valueOf(d); //$NON-NLS-1$
		}

		public Object fromString(String s) throws ParseException {
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				throw new ParseException(Messages.Format_bad_integer, 0);
			}
		}
	}

	public final static IFormatter orientationFormatter = new OrientationFormatter();

	private static final class OrientationFormatter implements IFormatter {

		public String toString(Object o) {
			int d = (Integer) o;
			return Utilities.orientationDegrees(d) + "°"; //$NON-NLS-1$
		}

		public Object fromString(String s) throws ParseException {
			if (s.endsWith("°")) //$NON-NLS-1$
				s = s.substring(0, s.length() - 1);
			try {
				int d = Integer.parseInt(s);
				if (d == 180)
					return 3;
				if (d == 90)
					return 6;
				if (d == 270)
					return 8;
				if (d == 0)
					return 0;
			} catch (NumberFormatException e) {
				// fal through
			}
			throw new ParseException(Messages.Format_bad_ori, 0);
		}
	}

	private static final class CurrencyExpressionFormatter implements IFormatter {

		public String toString(Object obj) {
			if (obj != null)
				try {
					double v = evaluateCurrencyExpression(obj.toString());
					return (v != 0d) ? getCurrencyNumberFormat().format(v) + "+" //$NON-NLS-1$
							: getCurrencyNumberFormat().format(v);
				} catch (ParseException e) {
					return obj.toString();
				}
			return null;
		}

		public Object fromString(String s) throws ParseException {
			return evaluateCurrencyExpression(s.trim());
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
			return sb.append(degrees).append('°').append(minutes).append('\'')
					.append(getDecimalUSFormat(2).format(seconds)).append('"').toString();
		}

		public Object fromString(String s) throws ParseException {
			s = s.trim().toUpperCase();
			NumberFormat af = getDecimalUSFormat(10);
			try {
				boolean degQ = s.indexOf('°') >= 0;
				boolean minQ = s.indexOf('"') >= 0;
				boolean secQ = s.indexOf('\'') >= 0;
				boolean dms = degQ || minQ || secQ;
				if (dms || s.indexOf(' ') >= 0) {
					double sign = 1d;
					if (s.startsWith(neg) || s.startsWith("-")) { //$NON-NLS-1$
						sign = -1d;
						s = s.substring(1);
					} else if (s.startsWith(pos) || s.startsWith("+")) //$NON-NLS-1$
						s = s.substring(1);
					else if (s.endsWith(neg)) {
						sign = -1d;
						s = s.substring(0, s.length() - 1);
					} else if (s.endsWith(pos))
						s = s.substring(0, s.length() - 1);
					StringTokenizer st = new StringTokenizer(s + "\n", "°'\" \n", //$NON-NLS-1$ //$NON-NLS-2$
							true);
					String n = null;
					double d = 0;
					int i = 0;
					boolean degHappened = false;
					boolean minHappened = false;
					boolean secHappened = false;
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if ("°".equals(token)) { //$NON-NLS-1$
							if (degHappened)
								throw new ParseException("", 0); //$NON-NLS-1$
							degHappened = true;
							if (n != null) {
								d += minQ || secQ ? Integer.parseInt(n) : af.parse(n).doubleValue();
								n = null;
								i = 1;
							}
						} else if (!dms && " ".equals(token)) { //$NON-NLS-1$
							if (n != null) {
								switch (i++) {
								case 0:
									d += st.hasMoreTokens() ? Integer.parseInt(n) : af.parse(n).doubleValue();
									degHappened = true;
									break;
								case 1:
									d += (st.hasMoreTokens() ? Integer.parseInt(n) : af.parse(n).doubleValue()) / 60d;
									minHappened = true;
									break;
								case 2:
									d += af.parse(n).doubleValue() / 3600d;
									secHappened = true;
									break;
								}
								n = null;
							}
						} else if ("'".equals(token)) { //$NON-NLS-1$
							if (minHappened)
								throw new ParseException("", 0); //$NON-NLS-1$
							minHappened = true;
							if (n != null) {
								d += (minQ || secQ ? Integer.parseInt(n) : af.parse(n).doubleValue()) / 60d;
								n = null;
								i = 2;
							}
						} else if ("\"".equals(token)) { //$NON-NLS-1$
							if (secHappened)
								throw new ParseException("", 0); //$NON-NLS-1$
							secHappened = true;
							if (n != null) {
								d += af.parse(n).doubleValue() / 3600d;
								n = null;
								i = 3;
							}
						} else if ("\n".equals(token)) { //$NON-NLS-1$
							if (n != null) {
								switch (i) {
								case 0:
									d = af.parse(n).doubleValue();
									break;
								case 1:
									d += (secQ ? (double) Integer.parseInt(n) : af.parse(n).doubleValue()) / 60d;
									break;
								case 2:
									d += af.parse(n).doubleValue() / 3600d;
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
			} catch (ParseException | NumberFormatException e) {
				throw new ParseException(Messages.Format_bad_lat_lon, 0);
			}
		}
	}

	public static NumberFormat getCurrencyNumberFormat() {
		int digits = getCurrencyDigits();
		NumberFormat nf = getDecimalFormat(digits);
		nf.setMinimumFractionDigits(digits);
		return nf;
	}

	public static String formatDecimal(double d, int digits) {
		return getDecimalFormat(digits).format(d);
	}

	public static NumberFormat getDecimalFormat(int digits) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(digits);
		return nf;
	}

	public static NumberFormat getDecimalUSFormat(int digits) {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		nf.setMaximumFractionDigits(digits);
		return nf;
	}

	public final static IFormatter exposureTimeFormatter = new IFormatter() {

		public String toString(Object o) {
			double d = ((Double) o).doubleValue();
			if (Double.isNaN(d))
				return MISSINGENTRYSTRING;
			if (d > 0.5d || d <= 0)
				return formatDecimal(d, 1);
			return "1/" + ((int) ((1 / d) + 0.5d)); //$NON-NLS-1$
		}

		public Object fromString(String s) throws ParseException {
			s = s.trim();
			try {
				NumberFormat af = getDecimalFormat(3);
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
			return formatDecimal(Math.pow(2d, d / 2), 1);
		}

		public Object fromString(String s) throws ParseException {
			s = s.trim();
			try {
				return 2d * Math.log(getDecimalFormat(10).parse(s).doubleValue()) / Math.log(2);
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
			return formatDecimal(d, 3);
		}

		public Object fromString(String s) throws ParseException {
			return getDecimalFormat(10).parse(s.trim()).doubleValue();
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
	 * Image direction
	 */
	public final static IFormatter directionFormatter = new IFormatter() {

		public String toString(Object o) {
			double d = ((Double) o).doubleValue();
			if (Double.isNaN(d))
				return MISSINGENTRYSTRING;
			while (d > 180.0)
				d -= 360.0;
			while (d < 180.0)
				d += 360.0;
			return new StringBuilder().append(formatDecimal(Math.abs(d), 1)).append('°').append(d < 0 ? "W" : "E") //$NON-NLS-1$ //$NON-NLS-2$
					.toString();
		}

		public Object fromString(String s) throws ParseException {
			s = s.trim();
			NumberFormat af = getDecimalFormat(10);
			try {
				double sign = 1d;
				if (s.endsWith("E") || s.endsWith("e")) //$NON-NLS-1$//$NON-NLS-2$
					s = s.substring(0, s.length() - 1).trim();
				else if (s.endsWith("W") || s.endsWith("w")) { //$NON-NLS-1$//$NON-NLS-2$
					s = s.substring(0, s.length() - 1).trim();
					sign = -1d;
				}
				if (s.endsWith("°")) //$NON-NLS-1$
					s = s.substring(0, s.length() - 1).trim();
				double d = sign * af.parse(s).doubleValue();
				while (d < 0)
					d += 360.0;
				while (d >= 360)
					d -= 360.0;
				return d;
			} catch (ParseException e) {
				throw new ParseException(Messages.Format_bad_bearing, 0);
			}
		}
	};

	/**
	 * Altitude
	 */
	public final static IFormatter altitudeFormatter = new IFormatter() {

		public Object fromString(String s) throws ParseException {
			return getDecimalFormat(2).parse(s);
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
					? true
					: Boolean.parseBoolean(s);
		}
	};

	/**
	 * Date values without time
	 */
	public static IFormatter dayFormatter = new IFormatter() {

		public String toString(Object d) {
			return YEAR_MONTH_DAY_FORMAT.get().format((Date) d);
		}

		public Object fromString(String s) throws ParseException {
			return YEAR_MONTH_DAY_FORMAT.get().parse(s);
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
				s = s.trim().toLowerCase();
				int offset = 0;
				if (s.endsWith(AM))
					s = s.substring(0, s.length() - AM.length()).trim();
				else if (s.endsWith(PM)) {
					s = s.substring(0, s.length() - PM.length()).trim();
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
			s = s.trim().toUpperCase();
			try {
				if (s.endsWith("TB")) //$NON-NLS-1$
					return (long) (getDecimalFormat(10).parse(s.substring(0, s.length() - 2).trim()).doubleValue()
							* 1000000000000L);
				if (s.endsWith("GB")) //$NON-NLS-1$
					return (long) (getDecimalFormat(10).parse(s.substring(0, s.length() - 2).trim()).doubleValue()
							* 1000000000);
				if (s.endsWith("MB")) //$NON-NLS-1$
					return (long) (getDecimalFormat(10).parse(s.substring(0, s.length() - 2).trim()).doubleValue()
							* 1000000);
				if (s.endsWith("kB") || s.endsWith("KB")) //$NON-NLS-1$ //$NON-NLS-2$
					return (long) (NumberFormat.getNumberInstance().parse(s.substring(0, s.length() - 2).trim())
							.doubleValue() * 1000);
				return Integer.parseInt(s);
			} catch (Exception e) {
				throw new ParseException(Messages.Format_bad_file_size, 0);
			}
		}

		public String toString(Object o) {
			long d = ((Long) o).longValue();
			if (d >= 1000000000000L)
				return formatDecimal(d / 1000000000000d, 2) + " TB"; //$NON-NLS-1$
			if (d >= 1000000000L)
				return formatDecimal(d / 1000000000d, 2) + " GB"; //$NON-NLS-1$
			if (d >= 1000000L)
				return formatDecimal(d / 1000000d, 2) + " MB"; //$NON-NLS-1$
			if (d >= 1000L)
				return formatDecimal(d / 1000d, 2) + " kB"; //$NON-NLS-1$
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
	public final static IFormatter trackFormatter = new IFormatter() {

		public Object fromString(String s) {
			return null;
		}

		public String toString(Object o) {
			if (o instanceof TrackRecord) {
				SimpleDateFormat trackdateFormat = TRACK_DATE_FORMAT.get();
				TrackRecord record = (TrackRecord) o;
				String date = trackdateFormat.format(record.getExportDate());
				String postfix = record.getReplaced() ? Messages.QueryField_replacement : ""; //$NON-NLS-1$
				String serviceName = record.getServiceName();
				String target = record.getTarget();
				if (TrackRecord_type.type_community.equals(record.getType()))
					return NLS.bind(Messages.QueryField_uploaded_to_account,
							new Object[] { serviceName, target, date, postfix });
				if (TrackRecord_type.type_ftp.equals(record.getType()))
					return NLS.bind(Messages.QueryField_uploaded_to_ftp,
							new Object[] { target, serviceName, date, postfix });
				if (TrackRecord_type.type_ftp.equals(record.getType()))
					return NLS.bind(Messages.QueryField_send_via_email, date);
				return Messages.QueryField_unknown_track_record_type;
			}
			return (String) o;
		}

	};

	public static final IFormatter currencyExpressionFormatter = new CurrencyExpressionFormatter();

	public static int getCurrencyDigits() {
		try {
			return Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits();
		} catch (Exception e) {
			return 2;
		}
	}

	// Based on an idea by Boann
	// (https://stackoverflow.com/questions/3422673/evaluating-a-math-expression-given-in-string-form)

	private static double evaluateCurrencyExpression(final String str) throws ParseException {
		return new Object() {
			int pos = -1, ch;

			void nextChar() {
				ch = (++pos < str.length()) ? str.charAt(pos) : -1;
			}

			boolean eat(int charToEat) {
				while (ch == ' ')
					nextChar();
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			double parse() throws ParseException {
				nextChar();
				double x = parseExpression();
				if (pos < str.length())
					throw new ParseException(NLS.bind(Messages.Format_unexpected, (char) ch), this.pos);
				return x;
			}

			// Grammar:
			// expression = term | expression `+` term | expression `-` term
			// term = factor | term `*` factor | term `/` factor
			// factor = `+` factor | `-` factor | `(` expression `)` | currency

			double parseExpression() throws ParseException {
				double x = parseTerm();
				for (;;) {
					if (eat('+'))
						x += parseTerm(); // addition
					else if (eat('-'))
						x -= parseTerm(); // subtraction
					else
						return x;
				}
			}

			double parseTerm() throws ParseException {
				double x = parseFactor();
				for (;;) {
					if (eat('*'))
						x *= parseFactor(); // multiplication
					else if (eat('/'))
						x /= parseFactor(); // division
					else
						return x;
				}
			}

			double parseFactor() throws ParseException {
				if (eat('+'))
					return parseFactor(); // unary plus
				if (eat('-'))
					return -parseFactor(); // unary minus
				int startPos = this.pos;
				if (eat('(')) { // parentheses
					double x = parseExpression();
					eat(')');
					return x;
				} else if (ch != '+' && ch != '-' && ch != '*' && ch != '/' && ch != '(' && ch != ')' && ch != ' '
						&& ch != -1) { // anything
					// else
					while (ch != '+' && ch != '-' && ch != '*' && ch != '/' && ch != '(' && ch != ')' && ch != ' '
							&& ch != -1)
						nextChar();
					String token = str.substring(startPos, this.pos);
					try {
						return getCurrencyNumberFormat().parse(token).doubleValue();
					} catch (ParseException e) {
						return getDecimalFormat(3).parse(token).doubleValue();
					}
				} else
					throw new ParseException(NLS.bind(Messages.Format_unexpected, (char) ch), this.pos);
			}
		}.parse();
	}

}
