package com.bdaum.zoom.ui.gps;

public class WaypointArea extends Waypoint {

	double SWlat = Double.NaN;
	double SWlon = Double.NaN;
	double NElat = Double.NaN;
	double NElon = Double.NaN;
	/**
	 * @return sWlat
	 */
	public double getSWlat() {
		return SWlat;
	}
	/**
	 * @param sWlat das zu setzende Objekt sWlat
	 */
	public void setSWlat(double sWlat) {
		SWlat = sWlat;
	}
	/**
	 * @return sWlon
	 */
	public double getSWlon() {
		return SWlon;
	}
	/**
	 * @param sWlon das zu setzende Objekt sWlon
	 */
	public void setSWlon(double sWlon) {
		SWlon = sWlon;
	}
	/**
	 * @return nElat
	 */
	public double getNElat() {
		return NElat;
	}
	/**
	 * @param nElat das zu setzende Objekt nElat
	 */
	public void setNElat(double nElat) {
		NElat = nElat;
	}
	/**
	 * @return nElon
	 */
	public double getNElon() {
		return NElon;
	}
	/**
	 * @param nElon das zu setzende Objekt nElon
	 */
	public void setNElon(double nElon) {
		NElon = nElon;
	}

}
