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

package de.jsteltze.calendar;

import de.jsteltze.common.Trans;

/**
 * Frequency an event may have.
 * @author Johannes Steltzer
 *
 */
public class Frequency {
	
	/*
	 *      frequency mode
	 *    0=by date      |
	 *    1=by weekday   |
	 *    2=by interval  |
	 *    3=by monthend ---                         
	 * frequency short: xxxxxxxx-xxxxxxxx
	 *                     --------------
	 *                        |
	 *                    13 bit mode details
	 *                    
	 * for mode 0: Bit 4 = weekly bit
	 *             Bit 5 = monthly bit
	 *             Bit 6 = yearly bit
	 * for mode 1: -
	 * for mode 2: Bit 4-14 = interval (possible values: 1 to 1024)
	 *             Bit 15-16 = index of unit (possible values: 0 (day) to 3 (year))
	 * for mode 3: -
	 */
	
	public static final short OCCUR_BY_DATE = 0;		/* 000xxx00-00000000 */
	public static final short OCCUR_ONCE = 0; 			/* 00000000-00000000 */
	public static final short OCCUR_WEEKLY = 4096;		/* 00010000-00000000 */
	public static final short OCCUR_MONTHLY = 2048;		/* 00001000-00000000 */
	public static final short OCCUR_YEARLY = 1024;		/* 00000100-00000000 */
	
	public static final short OCCUR_BY_WEEKDAY = 8192;	/* 00100000-00000000 */
	
	public static final short OCCUR_BY_INTERVAL = 16384;/* 010xxxxx-xxxxxxxx */
	
	public static final short OCCUR_BY_MONTHEND = 24576;/* 01100000-00000000 */
	
	public static final int UNIT_DAYS = 0;
	public static final int UNIT_WEEKS = 1;
	public static final int UNIT_MONTHS = 2;
	public static final int UNIT_YEARS = 3;
	
	/**
	 * Generate the frequency code OCCUR_BY_DATE.
	 * @param w - Weekly occurring?
	 * @param m - Monthly occurring?
	 * @param y - Yearly occurring?
	 * @return Frequency code.
	 */
	public static short bool2short(boolean w, boolean m, boolean y) {
		short freq = OCCUR_BY_DATE;
		if (w) freq |= OCCUR_WEEKLY;
		if (m) freq |= OCCUR_MONTHLY;
		if (y) freq |= OCCUR_YEARLY;
		return freq;
	}
	
	/**
	 * 
	 * @param code - Frequency code
	 * @return True if frequency is OCCUR_BY_DATE. 
	 */
	public static boolean isByDate(short code) {
		return (code >> 13) == OCCUR_BY_DATE && code != 0;
	}
	
	/**
	 * 
	 * @param code - Frequency code
	 * @return True if frequency is OCCUR_BY_WEEKDAY. 
	 */
	public static boolean isByWeekday(short code) {
		return (code >> 13) == (OCCUR_BY_WEEKDAY >> 13);
	}
	
	/**
	 * 
	 * @param code - Frequency code
	 * @return True if frequency is OCCUR_BY_INTERVAL. 
	 */
	public static boolean isByInterval(short code) {
		return (code >> 13) == (OCCUR_BY_INTERVAL >> 13);
	}
	
	/**
	 * 
	 * @param code - Frequency code
	 * @return True if frequency is OCCUR_BY_MONTHEND. 
	 */
	public static boolean isByEndOfMonth(short code) {
		return (code >> 13) == (OCCUR_BY_MONTHEND >> 13);
	}

	/**
	 * 
	 * @param code - Frequency code
	 * @return True if weekly bit is set.
	 */
	public static boolean isW(short code) {
		return (code & OCCUR_WEEKLY) == OCCUR_WEEKLY;
	}

	/**
	 * 
	 * @param code - Frequency code
	 * @return True monthly bit is set.
	 */
	public static boolean isM(short code) {
		return (code & OCCUR_MONTHLY) == OCCUR_MONTHLY;
	}

	/**
	 * 
	 * @param code - Frequency code
	 * @return True if yearly bit is set.
	 */
	public static boolean isY(short code) {
		return (code & OCCUR_YEARLY) == OCCUR_YEARLY;
	}
	
	/**
	 * 
	 * @param code - Frequency code
	 * @return Interval in case of OCCUR_BY_INTERVAL.
	 */
	public static int getInterval(short code) {
		return (code & (short) 0x1FFC) >> 2;
	}
	
	/**
	 * 
	 * @param code - Frequency code
	 * @return Unit in case of OCCUR_BY_INTERVAL. 0=days .. 3=years.
	 */
	public static int getUnit(short code) {
		return code & (short) 0x3;
	}
	
	/**
	 * 
	 * @param weekday - Day of week (java.util.Calendar.DAY_OF_WEEK)
	 * @param index - Index of week; 0 for last week
	 * @return Generated frequency code.
	 */
	public static short genByWeekday(int weekday, int index) {
		short code = (short) Date.javaUtilOrder2germanOrder(weekday);
		code <<= 7;
		code |= OCCUR_BY_WEEKDAY;
		code |= ((short) index << 10);
		return code;
	}
	
	/**
	 * 
	 * @param interval - Interval
	 * @param unit - 0=days .. 3=years
	 * @return Generated frequency code.
	 */
	public static short genByInterval(int interval, int unit) {
		short code = (short) interval;
		code <<= 2;
		code |= (short) unit;
		code |= Frequency.OCCUR_BY_INTERVAL;
		return code;
	}

	/**
	 * 
	 * @param code - Frequency code to create label for
	 * @param baseDate - Date that code bases on (if needed)
	 * @return Short human readable string representation of
	 * 		the frequency.
	 */
	public static String getLabel(short code, Date baseDate) {
		if (isByDate(code)) {
			if (isW(code) && isM(code) && isY(code))
				return "("+Trans.t("weekly")+".,"+Trans.t("Monthly")+".,"+Trans.t("Yearly")+".)";
			else if (isW(code) && isM(code))
				return "("+Trans.t("weekly")+".,"+Trans.t("Monthly")+".)";
			else if (isW(code) && isY(code))
				return "("+Trans.t("weekly")+".,"+Trans.t("Yearly")+".)";
			else if (isW(code))
				return "("+Trans.t("weekly")+".)";
			else if (isM(code) && isY(code))
				return "("+Trans.t("monthly")+".,"+Trans.t("yearly")+")";
			else if (isM(code))
				return "("+Trans.t("monthly")+".)";
			else if (isY(code))
				return "("+Trans.t("yearly")+".)";
		}
		else if (isByWeekday(code)) {
			return "("+Trans.t("each")+" " + 
					(baseDate.getWeekdayIndex() == 0 ? Trans.t("last") : baseDate.getWeekdayIndex() + ". ") + 
					Date.dayOfWeek2String(baseDate.get(java.util.Calendar.DAY_OF_WEEK), true) + ")"; 
		}
		else if (isByInterval(code)) {
			int unit = getUnit(code);
			String unitS;
			if (unit == 0) unitS = " "+Trans.t("days")+")";
			else if (unit == 1) unitS = " "+Trans.t("week")+")";
			else if (unit == 2) unitS = " "+Trans.t("Monthly")+")";
			else unitS = " "+Trans.t("years")+")";
			return "("+Trans.t("all")+" " + getInterval(code) + unitS;
		}
		else if (isByEndOfMonth(code)) {
			int daysLeft = baseDate.getDaysToEndOfMonth();
			if (daysLeft == 0)
				return "("+Trans.t("End of month")+")";
			return "(" + daysLeft + (daysLeft == 1 ? " "+Trans.t("Day") : " "+Trans.t("Days")) + 
					" "+Trans.t("before month end")+")";
		}
		return "";
	}
}
