/*
 *  java-calendar - a java calendar for Germany
 *  Copyright (C) 2012  Johannes Steltzer
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.jsteltze.calendar.config;

import java.awt.Color;

import de.jsteltze.common.Trans;

/**
 * Class holding the different default colors
 * of the calendar UI.
 * @author Johannes Steltzer
 *
 */
public class ColorSet {
	public static final byte MAXCOLORS = 0x08;

	public static final byte TODAY = 0x00;
	public static final byte WEEKEND = 0x01;
	public static final byte HOLIDAY = 0x02;
	public static final byte SELECTED = 0x03;
	public static final byte FONT = 0x04;
	public static final byte FONT_HOLIDAY = 0x05;
	public static final byte CONTROLPANEL = 0x06;
	public static final byte NOTI = 0x07;

	public static final Color[] DEFAULT = {
		Const.COLOR_DEF_TODAY, Const.COLOR_DEF_WEEKEND, 
		Const.COLOR_DEF_HOLIDAY, Const.COLOR_DEF_SELECTED,
		Const.COLOR_DEF_FONT, Const.COLOR_DEF_FONT_HOLIDAY,
		Const.COLOR_DEF_CONTROL, Const.COLOR_DEF_NOTI
	};
	
	public static final String[] NAMES = {Trans.t( "today"), Trans.t("weekend"), 
		Trans.t("holiday"),	Trans.t("mark"),Trans.t( "Font color for events"), 
		Trans.t("Font color for holidays"), Trans.t("control bar"),  
		Trans.t("reminder window") };
}
