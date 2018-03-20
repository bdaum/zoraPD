package com.bdaum.zoom.ui.gps;

public class WaypointArea extends Waypoint {

	double SWlat = Double.NaN;
	double SWlon = Double.NaN;
	double NElat = Double.NaN;
	double NElon = Double.NaN;
	
	
	public WaypointArea() {
		super();
	}
	
	public WaypointArea(String name, double longitude, double latitude) {
		super(longitude, latitude);
		setName(name);
	}
	
	public double getSWlat() {
		return SWlat;
	}

	public void setSWlat(double sWlat) {
		SWlat = sWlat;
	}

	public double getSWlon() {
		return SWlon;
	}

	public void setSWlon(double sWlon) {
		SWlon = sWlon;
	}

	public double getNElat() {
		return NElat;
	}

	public void setNElat(double nElat) {
		NElat = nElat;
	}

	public double getNElon() {
		return NElon;
	}

	public void setNElon(double nElon) {
		NElon = nElon;
	}

}
