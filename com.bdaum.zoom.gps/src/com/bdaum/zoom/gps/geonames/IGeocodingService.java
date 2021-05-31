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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.gps.geonames;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.widgets.Control;
import org.xml.sax.SAXException;

import com.bdaum.zoom.ui.gps.WaypointArea;
import com.bdaum.zoom.ui.widgets.CGroup;

public interface IGeocodingService {
	
	public class Parameter {
		private String id;
		private String label;
		private String reqMsg;
		private String hint;
		private String tooltip;
		private String explanation;
		
		public Parameter(String id, String label, String reqMsg, String hint, String tooltip, String explanation) {
			super();
			this.id = id;
			this.label = label;
			this.reqMsg = reqMsg;
			this.hint = hint;
			this.tooltip = tooltip;
			this.explanation = explanation;
		}
		
		public String getId() {
			return id;
		}

		public String getLabel() {
			return label;
		}

		public String getReqMsg() {
			return reqMsg;
		}

		public String getHint() {
			return hint;
		}

		public String getTooltip() {
			return tooltip;
		}
		
		public String getExplanation() {
			return explanation;
		}
	}

	String SEARCHPARMS = "searchParameters"; //$NON-NLS-1$
	
	Place fetchPlaceInfo(double lat, double lon)
			throws SocketTimeoutException, IOException, WebServiceException,
			SAXException, ParserConfigurationException;

	WaypointArea[] findLocation(String address)
			throws IOException, WebServiceException, SAXException,
			ParserConfigurationException;

	void setName(String name);
	
	String getName();

	void setDefault(boolean dflt);
	
	boolean isDefault();

	Control createParameterGroup(CGroup parmGroup);

	String getId();
	
	void setId(String id);
	
	String getLink();
	
	void setLink(String link);
	
	void addParameter(Parameter parameter);
	
	List<Parameter> getParameters();

	String getDescription();

	void setDescription(String description);

	double getElevation(double lat, double lon) throws UnknownHostException, IOException;

	void saveSearchParameters();

}
