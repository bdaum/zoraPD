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
 * (c) 2017-2019 Berthold Daum  
 */
package com.bdaum.zoom.gps.internal.nmea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import com.bdaum.zoom.ui.gps.IGpsParser;
import com.bdaum.zoom.ui.gps.Trackpoint;

public class NmeaParser implements IGpsParser {

	public void parse(InputStream in, List<Trackpoint> points) throws IOException, ParseException {
		boolean invalid = false, tandem = false, gaComplete = false;
		int phour = -1, pmin = -1, psec = -1, msec = 0;
		double altitude = Double.NaN;
		GregorianCalendar cal = new GregorianCalendar();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		while (true) {
			String line = reader.readLine();
			if (line == null)
				break;
			boolean gpgga = line.startsWith("$GPGGA,"); //$NON-NLS-1$
			boolean gprmc = line.startsWith("$GPRMC,"); //$NON-NLS-1$
			if (!gprmc && !gpgga)
				continue;
			if (gpgga)
				gaComplete = false;
			StringTokenizer st = new StringTokenizer(line, ","); //$NON-NLS-1$
			st.nextToken();
			if (!st.hasMoreTokens())
				continue;
			// eg1.
			// $GPRMC,081836,A,3751.65,S,14507.36,E,000.0,360.0,130998,011.3,E*62
			// eg2.
			// $GPGGA,152619.9,6023.6437,N,00518.9992,E,1,00,00.00,0.0,M,0,M,0,*45
			// $GPRMC,152619,A,6023.6437,N,00518.9992,E,000.0,000.0,170119,00,*1c

			double lat = Double.NaN;
			double lon = Double.NaN;
			double speed = Double.NaN;
			// double course = Double.NaN;
			try {
				String token = st.nextToken().trim();
				if (!st.hasMoreTokens())
					continue;
				double rawtime = Double.parseDouble(token);
				int hour = (int) (rawtime / 10000);
				rawtime -= hour * 10000;
				int minute = (int) (rawtime / 100);
				rawtime -= minute * 100;
				int second = (int) rawtime;
				if (gpgga) {
					altitude = Double.NaN;
					phour = hour;
					pmin = minute;
					psec = second;
					msec = (int) ((rawtime - second) * 1000);
				} else {
					tandem = (gaComplete && hour == phour && minute == pmin && second == psec);
					phour = pmin = psec = -1;
					if (tandem) {
						if (invalid)
							continue;
					} else
						msec = (int) ((rawtime - second) * 1000);
					token = st.nextToken().trim();
					if (!"A".equals(token) || !st.hasMoreTokens()) //$NON-NLS-1$
						continue;
				}
				token = st.nextToken().trim();
				if (!st.hasMoreTokens())
					continue;
				lat = toDecimal(parseDouble(token));
				if (Double.isNaN(lat))
					continue;
				token = st.nextToken().trim();
				if (!st.hasMoreTokens())
					continue;
				if ("S".equals(token)) //$NON-NLS-1$
					lat = -lat;
				else if (!"N".equals(token)) //$NON-NLS-1$
					continue;
				token = st.nextToken().trim();
				if (!st.hasMoreTokens())
					continue;
				lon = toDecimal(parseDouble(token));
				if (Double.isNaN(lon))
					continue;
				token = st.nextToken().trim();
				if (!st.hasMoreTokens())
					continue;
				if ("W".equals(token)) //$NON-NLS-1$
					lon = -lon;
				else if (!"E".equals(token)) //$NON-NLS-1$
					continue;
				token = st.nextToken().trim();
				if (!st.hasMoreTokens())
					continue;
				if (gprmc) {
					speed = parseDouble(token);
					token = st.nextToken().trim();
					// course = parseDouble(token);
					if (!st.hasMoreTokens())
						continue;
					token = st.nextToken().trim();
					int rawdate = Integer.parseInt(token);
					int day = rawdate / 10000;
					rawdate -= day * 10000;
					int month = rawdate / 100;
					rawdate -= month * 100;
					int year = rawdate < 70 ? rawdate + 2000 : rawdate + 1900;
					Trackpoint trkpnt = new Trackpoint(lat, lon, false);
					trkpnt.setSpeed(speed);
					cal.set(GregorianCalendar.YEAR, year);
					cal.set(GregorianCalendar.MONTH, month - 1);
					cal.set(GregorianCalendar.DAY_OF_MONTH, day);
					cal.set(GregorianCalendar.HOUR_OF_DAY, hour);
					cal.set(GregorianCalendar.MINUTE, minute);
					cal.set(GregorianCalendar.SECOND, second);
					cal.set(GregorianCalendar.MILLISECOND, msec);
					trkpnt.setTime(cal.getTimeInMillis());
					if (tandem && !Double.isNaN(altitude))
						trkpnt.setAltitude(altitude);
					points.add(trkpnt);
				} else {
					int fixquality = Integer.parseInt(token);
					invalid = (fixquality == 0);
					if (invalid)
						continue;
					token = st.nextToken().trim();
					if (!st.hasMoreTokens())
						continue;
					// noSat = Double.parseInt(token);
					token = st.nextToken().trim();
					if (!st.hasMoreTokens())
						continue;
					// accuracy = parseDouble(token);
					token = st.nextToken().trim();
					altitude = parseDouble(token);
					gaComplete = true;
				}
			} catch (NumberFormatException e) {
				continue;
			}
		}
	}
	
	private static double toDecimal(double v) {
		double degrees = Math.floor(v * 0.01d);
		return degrees + (v - degrees * 100d) / 60d;
	}

	private static double parseDouble(String s) {
		if (s.isEmpty())
			return Double.NaN;
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return Double.NaN;
		}
	}

}
