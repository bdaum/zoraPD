package com.bdaum.zoom.gps.geonames;

import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.bdaum.zoom.ui.gps.WaypointArea;

public interface IGeonamingService {

	public Place fetchPlaceInfo(double lat, double lon)
			throws SocketTimeoutException, IOException, WebServiceException,
			SAXException, ParserConfigurationException;

	public WaypointArea[] findLocation(String address)
			throws IOException, WebServiceException, SAXException,
			ParserConfigurationException;
}
