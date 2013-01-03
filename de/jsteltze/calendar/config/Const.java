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
import java.awt.Font;

/**
 * Class holding the various constants.
 * @author Johannes Steltzer
 *
 */
public class Const {
	public static final Color COLOR_DEF_TODAY = new Color(166, 252, 155);
	public static final Color COLOR_DEF_WEEKEND = new Color(247, 250, 188);
	public static final Color COLOR_DEF_HOLIDAY = new Color(247, 250, 188);
	public static final Color COLOR_DEF_SELECTED = new Color(187, 219, 252);
	public static final Color COLOR_DEF_FONT = new Color(150, 150, 150);
	public static final Color COLOR_DEF_FONT_HOLIDAY = Color.BLUE;
	public static final Color COLOR_DEF_CONTROL = new Color(247, 250, 188);
	public static final Color COLOR_DEF_NOTI = new Color(236, 233, 216);
	public static final Color COLOR_SETTINGS_TABS_BG = Color.WHITE;
	public static final Color COLOR_SETTINGS_INFO_BG = new Color(200,221,242);
	public static final Color COLOR_CANVAS_FRAMES = new Color(220, 220, 220);
	public static final Color COLOR_CONTROL_BORDER = new Color(172, 168, 153);
	public static final Color COLOR_BG_MAIN = new Color(240, 237, 223);

	public static final Font FONT_BORDER_TEXT = new Font(Font.SANS_SERIF, Font.ITALIC, 12);
	public static final Font FONT_MULTIDAY_HEADER = new Font(Font.SANS_SERIF, Font.BOLD, 13);
	public static final Font FONT_NOTI_TEXT = new Font(Font.SANS_SERIF, Font.BOLD, 20);
	public static final Font FONT_STATUSBAR = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
	public static final Font FONT_MONTH_HEADERS = new Font(Font.SANS_SERIF, Font.PLAIN, 16);

}
