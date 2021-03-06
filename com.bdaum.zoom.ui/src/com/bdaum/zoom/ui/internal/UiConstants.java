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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal;

import com.bdaum.zoom.core.Constants;

public interface UiConstants {

	// Cursor
	String CURSOR_MMINUS = "mminus"; //$NON-NLS-1$
	String CURSOR_OPEN_HAND = "openHand"; //$NON-NLS-1$
	String CURSOR_MPLUS = "mplus"; //$NON-NLS-1$
	String CURSOR_GRABBING = "grabbing"; //$NON-NLS-1$
	
	// Widget properties
	String ASSET = "asset"; //$NON-NLS-1$
	String TRASH = "trash"; //$NON-NLS-1$
	String CARD = "card"; //$NON-NLS-1$
	String REGIONS = "regions"; //$NON-NLS-1$
	String HOTSPOTS = "hotSpots"; //$NON-NLS-1$
	String LABEL = "label"; //$NON-NLS-1$
	String KEY = "key"; //$NON-NLS-1$
	String DATA = "data"; //$NON-NLS-1$
	String ID = "id"; //$NON-NLS-1$
	String RGB = "rgb"; //$NON-NLS-1$
	String CONTROL = "control"; //$NON-NLS-1$
	String BUTTON = "button"; //$NON-NLS-1$
	String MAP = "map"; //$NON-NLS-1$
	String SELECTOR = "selector"; //$NON-NLS-1$
	
	//Fonts
	String MESSAGEFONT = "com.bdaum.zoom.messageFont"; //$NON-NLS-1$
	String MESSAGETITLEFONT = "com.bdaum.zoom.messageTitleFont"; //$NON-NLS-1$
	String SELECTIONFONT = "com.bdaum.zoom.selectionFont"; //$NON-NLS-1$
	String ITALICFONT = "com.bdaum.zoom.italicFont"; //$NON-NLS-1$
	String VIEWERFONT = "com.bdaum.zoom.viewerFont"; //$NON-NLS-1$
	String VIEWERTITLEFONT = "com.bdaum.zoom.viewerTitleFont"; //$NON-NLS-1$
	String VIEWERBANNERFONT = "com.bdaum.zoom.viewerBannerFont"; //$NON-NLS-1$
	
	//Animation
	int DRAG_ANIMATION_ALPHA = 160;
	int ANIMATION_YOFFSET = 10;
	int ANIMATION_XOFFSET = 10;
	double ANIMATION_SIZE = 150;
	
	//Templates
	String STACKPATTERN = Constants.STACK + "/*!*"; //$NON-NLS-1$
	String DEFAULTTEMPLATE = "ISO{meta=scalarSpeedRatings}        {meta=focalLengthIn35MmFilm}         f{meta=fNumber}        {meta=exposureTime}"; //$NON-NLS-1$
	String TEMPLATE = "template"; //$NON-NLS-1$
	String ALIGNMENT = "alignment"; //$NON-NLS-1$
	String[] BOOLEANLABELS = new String[] { Messages.UiConstants_true, Messages.UiConstants_false };
	String INFINITE = Messages.UiConstants_infinite;
	
	// Slideshow color schemes
	int WHITEONBLACK = 1;
	int BLACKONWHITE = 0;
	
	int BG_DARK_GRAY = 0;
	int BG_GRAY = 1;
	int BG_BLACK = 2;
	int BG_WHITE = 3;


}
