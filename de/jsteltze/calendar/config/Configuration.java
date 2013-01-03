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
import java.io.BufferedWriter;
import java.io.IOException;

import de.jsteltze.calendar.Event;
import de.jsteltze.common.Trans;

/**
 * Settings for calendar.
 * @author Johannes Steltzer
 *
 */
public class Configuration {
	
	/** settings properties as bytes */
	private byte view, reminder, onCloseAction, style;
	
	/** settings properties as integer */
	private int holidays, specialDays;
	
	/** look for new updates automatically */
	private boolean autoUpdate;
	
	/** Show moon phase icons */
	private boolean moon;
	
	/** ColorSet */
	private Color[] colors;
	
	/** Own theme file path or null for default */
	private String theme;
	
	/** Start in systray */
	private boolean systrayStart;
	
	/** Play theme on notification popups */
	private boolean playTheme;

	public static final byte VIEW_YEAR = 0x00;
	public static final byte VIEW_MONTH = 0x01;
	public static final byte VIEW_WEEK = 0x02;
	public static final byte VIEW_DAY = 0x03;
	public static final byte VIEW_DEFAULT = -1;
	public static final String[] VIEW_LABELS = 
		{Trans.t("year view"), Trans.t("Month"),Trans.t("Weekly"), Trans.t("Daily View")};
	
	public static final byte ON_CLOSE_EXIT = 0x00;
	public static final byte ON_CLOSE_MOVE_TO_SYSTRAY = 0x01;
	public static final String[] ON_CLOSE_LABELS =
		{Trans.t("Exit Program"), Trans.t("move into the systray")};
	
	public static final byte STYLE_SYSTEM = 0x00;
	public static final byte STYLE_SWING = 0x01;
	public static final byte STYLE_MOTIF = 0x02;
	public static final byte STYLE_NIMBUS = 0x03;
	public static final String[] STYLE_LABELS = 
		{"System", "Swing", "Motif", "Nimbus"};

	/** default configuration */
	public static final Configuration defaultConfig = new Configuration(
			VIEW_MONTH, Event.REMIND_1D, ON_CLOSE_MOVE_TO_SYSTRAY, STYLE_SYSTEM,
			ColorSet.DEFAULT.clone(), Holidays.DEFAULT_HOLIDAYS, Holidays.DEFAULT_SPECIAL, 
			true, true, null, false, true);

	/**
	 * Construct a new configuration.
	 * @param view - See Configuration.VIEW_XXX
	 * @param remind - See Event.REMIND_XXX
	 * @param onCloseAction - See Configuration.ON_CLOSE_XXX
	 * @param style - See Configuration.STYLE_XXX
	 * @param colors - ColorSet
	 * @param holidays - Holidays
	 * @param specialDays - Special days
	 * @param autUpdate - Perform auto updates
	 * @param moon - Show moon phases
	 * @param themeFile - Own theme file
	 * @param systrayStart - Start in systray
	 * @param playTheme - Play theme on notifications
	 */
	public Configuration(byte view, byte remind, byte onCloseAction,
			byte style, Color[] colors, int holidays, int specialDays, 
			boolean autoUpdate, boolean moon, String themeFile,
			boolean systrayStart, boolean playTheme) {
		this.view = view;
		this.reminder = remind;
		this.onCloseAction = onCloseAction;
		this.style = style;
		this.colors = colors;
		this.holidays = holidays;
		this.specialDays = specialDays;
		this.autoUpdate = autoUpdate;
		this.moon = moon;
		this.theme = themeFile;
		this.systrayStart = systrayStart;
		this.playTheme = playTheme;
	}

	/**
	 * Write XML configuration.
	 * @param b - Stream to write
	 * @throws IOException
	 */
	public void write(BufferedWriter b) throws IOException {
		if (this.equals(defaultConfig))
			return;
		b.write("  <Config>\n");
		if (this.view != defaultConfig.view)
			b.write("    <DefaultView>" + this.view + "</DefaultView>\n");
		if (this.reminder != defaultConfig.reminder)
			b.write("    <Remind>" + this.reminder + "</Remind>\n");
		if (this.onCloseAction != defaultConfig.onCloseAction)
			b.write("    <AtClose>" + this.onCloseAction + "</AtClose>\n");
		if (this.holidays != defaultConfig.holidays)
			b.write("    <HolidayID>" + this.holidays + "</HolidayID>\n");
		if (this.specialDays != defaultConfig.specialDays)
			b.write("    <SpecialDaysID>" + this.specialDays + "</SpecialDaysID>\n");
		if (this.style != defaultConfig.style)
			b.write("    <Style>" + this.style + "</Style>\n");
		if (this.autoUpdate != defaultConfig.autoUpdate)
			b.write("    <AutoUpdate>" + this.autoUpdate + "</AutoUpdate>\n");
		if (this.moon != defaultConfig.moon)
			b.write("    <ShowMoon>" + this.moon + "</ShowMoon>\n");
		if (this.systrayStart != defaultConfig.systrayStart)
			b.write("    <SystrayStart>" + this.systrayStart + "</SystrayStart>\n");
		if (this.playTheme != defaultConfig.playTheme)
			b.write("    <PlayTheme>" + this.playTheme + "</PlayTheme>\n");
		if (this.theme != defaultConfig.theme)
			b.write("    <Theme>" + this.theme + "</Theme>\n");
		for (byte i = 0x00; i < ColorSet.MAXCOLORS; i++)
			if (!this.colors[i].equals(ColorSet.DEFAULT[i]))
				b.write("    <Color r=\"" + this.colors[i].getRed() + "\" g=\""
						+ this.colors[i].getGreen() + "\" b=\""
						+ this.colors[i].getBlue() + "\">" + i + "</Color>\n");
		b.write("  </Config>\n");
	}

	/**
	 * 
	 * @return View (Configuration.VIEW_XXX).
	 */
	public byte getView() {
		return this.view;
	}

	/**
	 * 
	 * @return Reminder (Event.REMIND_XXX).
	 */
	public byte getReminder() {
		return this.reminder;
	}

	/**
	 * 
	 * @return Action on closing (Configuration.ON_CLOSE_XXX).
	 */
	public byte getOnCloseAction() {
		return this.onCloseAction;
	}
	
	/**
	 * 
	 * @return Style (Configuration.STYLE_XXX). 
	 */
	public byte getStyle() {
		return this.style;
	}

	/**
	 * 
	 * @return ColorSet.
	 */
	public Color[] getColors() {
		return this.colors;
	}

	/**
	 * 
	 * @return Holidays (integer encoded).
	 */
	public int getHolidays() {
		return this.holidays;
	}
	
	/**
	 * 
	 * @return Special days (integer encoded).
	 */
	public int getSpecialDays() {
		return this.specialDays;
	}
	
	/**
	 * 
	 * @return True if auto update is enabled, false otherwise.
	 */
	public boolean getAutoUpdate() {
		return this.autoUpdate;
	}
	
	/**
	 * 
	 * @return True if moon phase is enabled, false otherwise.
	 */
	public boolean getMoon() {
		return this.moon;
	}
	
	/**
	 * 
	 * @return Returns the theme file path or null for default.
	 */
	public String getTheme() {
		return this.theme;
	}
	
	/**
	 * 
	 * @return Returns whether or not to start in systray.
	 */
	public boolean getSystrayStart() {
		return this.systrayStart;
	}
	
	/**
	 * 
	 * @return Returns whether or not to play a theme on
	 * 		notifications.
	 */
	public boolean getPlayTheme() {
		return this.playTheme;
	}
	
	/**
	 * Set a new view.
	 * @param x - View to set (see Configuation.VIEW_XXX)
	 */
	public void setView(byte x) {
		this.view = x;
	}
}
