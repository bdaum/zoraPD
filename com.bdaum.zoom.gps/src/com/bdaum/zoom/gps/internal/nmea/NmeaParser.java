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
 * (c) 2017 Berthold Daum  
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

	public void parse(InputStream in, List<Trackpoint> points)
			throws IOException, ParseException {
		GregorianCalendar cal = new GregorianCalendar();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		while (true) {
			String line = reader.readLine();
			if (line == null)
				break;
			if (!line.startsWith("$GPRMC,")) //$NON-NLS-1$
				continue;
			StringTokenizer st = new StringTokenizer(line, ","); //$NON-NLS-1$
			st.nextToken();
			if (!st.hasMoreTokens())
				continue;
			// eg1.
			// $GPRMC,081836,A,3751.65,S,14507.36,E,000.0,360.0,130998,011.3,E*62
			// eg2.
			// $GPRMC,225446,A,4916.45,N,12311.12,W,000.5,054.7,191194,020.3,E*68

			double lat = Double.NaN;
			double lon = Double.NaN;
			double speed = Double.NaN;
			// double course = Double.NaN;
			try {
				String token = st.nextToken().trim();
				double rawtime = Double.parseDouble(token);
				int hour = (int) (rawtime / 10000);
				rawtime -= hour * 10000;
				int minute = (int) (rawtime / 100);
				rawtime -= minute * 100;
				int second = (int) rawtime;
				if (!st.hasMoreTokens())
					continue;
				token = st.nextToken().trim();
				if (!"A".equals(token)) //$NON-NLS-1$
					continue;
				if (!st.hasMoreTokens())
					continue;
				token = st.nextToken().trim();
				lat = Double.parseDouble(token);
				if (!st.hasMoreTokens())
					continue;
				token = st.nextToken().trim();
				if ("S".equals(token)) //$NON-NLS-1$
					lat = -lat;
				else if (!"N".equals(token)) //$NON-NLS-1$
					continue;
				if (!st.hasMoreTokens())
					continue;
				token = st.nextToken().trim();
				lon = Double.parseDouble(token);
				if (!st.hasMoreTokens())
					continue;
				token = st.nextToken().trim();
				if ("W".equals(token)) //$NON-NLS-1$
					lon = -lon;
				else if (!"E".equals(token)) //$NON-NLS-1$
					continue;
				if (!st.hasMoreTokens())
					continue;
				token = st.nextToken().trim();
				speed = Double.parseDouble(token);
				if (!st.hasMoreTokens())
					continue;
				token = st.nextToken().trim();
				// course = Double.parseDouble(token);
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
				trkpnt.setTime(cal.getTimeInMillis());
				points.add(trkpnt);
			} catch (NumberFormatException e) {
				continue;
			}
		}
	}

}
