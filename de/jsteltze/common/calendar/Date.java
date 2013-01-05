/*
 *  common-package - various java utilities
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

package de.jsteltze.common.calendar;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Date class.
 * @author Johannes Steltzer
 *
 */
public class Date 
    extends GregorianCalendar {
    
    private static final long serialVersionUID = 1L;
    
    /** Is there a time specified for this date? */ 
    private boolean hasTime;

    /**
     * Construct a new date with the current time and date.
     */
    public Date() {
        super();
//        this.set(Calendar.SECOND, 0);
//        this.set(Calendar.MILLISECOND, 0);
        this.hasTime = true;
    }

    /**
     * Construct a new date with todays date and specific time.
     * @param hour - Hour to set (0-23) 
     * @param min - Minute to set (0-59)
     */
    public Date(int hour, int min) {
        super();
        this.set(Calendar.HOUR_OF_DAY, hour);
        this.set(Calendar.MINUTE, min);
        this.set(Calendar.SECOND, 0);
        this.set(Calendar.MILLISECOND, 0);
        this.hasTime = true;
    }

    /**
     * Construct a new specific date without time. 
     * @param year - Year to set (e.g. 2012)
     * @param month - Month to set (see java.util.Calendar.XX)
     * @param day - Day to set (1-31)
     */
    public Date(int year, int month, int day) {
        super(year, month, day, 0, 0, 0);
        this.set(Calendar.MILLISECOND, 0);
        this.hasTime = false;
    }

    /**
     * Construct a new specific date with a specific time.
     * @param year - Year to set  
     * @param month - Month to set (see java.util.Calendar.XX)
     * @param day - Day to set (1-31)
     * @param hour - Hour to set (0-23)
     * @param min - Minute to set (0-59)
     */
    public Date(int year, int month, int day, int hour, int min) {
        super(year, month, day, hour, min);
        this.set(Calendar.SECOND, 0);
        this.set(Calendar.MILLISECOND, 0);
        this.hasTime = true;
    }
    
    /**
     * Construct a new date by a string of the form "DD.MM.YYYY"
     * (may also be: "D.M.YYYY").
     * @param date - String
     */
    public Date(String date) {
        this(Integer.parseInt(date.split("\\.")[2].split("-")[0]), 
                Integer.parseInt(date.split("\\.")[1]) - 1,
                Integer.parseInt(date.split("\\.")[0]));
    }

    /**
     * 
     * @return True if time is enabled for this date.
     */
    public boolean hasTime() {
        return this.hasTime;
    }

    /**
     * Set time flag.
     * @param x - True for enabling time, false otherwise
     */
    public void setHasTime(boolean x) {
        this.hasTime = x;
    }

    /**
     * Returns the string representation of this date.
     * Format will be DD.MM.YYYY but may also be D.M.YYYY (in case
     * day or month are less than 10).
     * @param fillWithZeros - True for adding a zero to numbers 
     *         smaller than 10
     * @return E.g. '25.5.2012' vs '25.05.2012' 
     */
    public String dateToString(boolean fillWithZeros) {
        int day = this.get(Calendar.DAY_OF_MONTH);
        int month = this.get(Calendar.MONTH) + 1;
        return (fillWithZeros && day < 10 ? "0" : "") + day + "." +
                (fillWithZeros && month < 10 ? "0" : "") + month + "."
                + this.get(Calendar.YEAR);
    }

    /**
     * Returns the string representation of this dates time.
     * Format will be HH:MM (with HH 0-23).
     * @return E.g. 18:09
     */
    public String timeToString() {
        return "" + this.get(Calendar.HOUR_OF_DAY) + ":"
                + (this.get(Calendar.MINUTE) < 10 ? "0" : "")
                + this.get(Calendar.MINUTE);
    }

    /**
     * Compares day, month and year of this date with a second one.
     * @param d - Date to compare with
     * @return True if days, months and years are equal. False otherwise.
     */
    public boolean sameDateAs(Date d) {
        return d.get(Calendar.DAY_OF_MONTH) == this.get(Calendar.DAY_OF_MONTH)
                && d.get(Calendar.MONTH) == this.get(Calendar.MONTH)
                && d.get(Calendar.YEAR) == this.get(Calendar.YEAR);
    }

    /**
     * Compares this dates time with the time of a second date. 
     * @param d - Date with time to compare with
     * @return True if hours and minutes are equal. False otherwise.
     */
    public boolean sameTimeAs(Date d) {
        return d.get(Calendar.HOUR_OF_DAY) == this.get(Calendar.HOUR_OF_DAY)
                && d.get(Calendar.MINUTE) == this.get(Calendar.MINUTE);
    }

    /**
     * Convert day of week order from java.util.Calendar.XXX 
     * (Sunday=1...Saturday=7) to German order (Monday=0...Sunday=6)
     * @param x - Day of week from java.util.Calendar (Sunday=1...Saturday=7)
     */
    public static int javaUtilOrder2germanOrder(int x) {
        return x == 1 ? 6 : x - 2;
    }
    
    /**
     * Convert day of week order from German order 
     * (Monday=0...Sunday=6) to java.util.Calendar.XXX (Sunday=1...Saturday=7)
     * @param x - Day of week in German order (Monday=0...Sunday=6)
     */
    public static int germanOrder2javaUtilOrder(int x) {
        return x == 6 ? Calendar.SUNDAY : x + 2;
    }

    /**
     * Returns the string representation of a month.
     * @param month - Month from java.util.Calendar.XX
     * @param Short - True for short form (Jan, Feb ...),
     *         False for long form (Januar, Februar ...)
     * @return String representation.
     */
    public static String month2String(int month, boolean Short) {
        if (month == Calendar.JANUARY)
            return (Short) ? "Jan" : "Januar";
        else if (month == Calendar.FEBRUARY)
            return (Short) ? "Feb" : "Februar";
        else if (month == Calendar.MARCH)
            return (Short) ? "Mär" : "März";
        else if (month == Calendar.APRIL)
            return (Short) ? "Apr" : "April";
        else if (month == Calendar.MAY)
            return "Mai";
        else if (month == Calendar.JUNE)
            return (Short) ? "Jun" : "Juni";
        else if (month == Calendar.JULY)
            return (Short) ? "Jul" : "Juli";
        else if (month == Calendar.AUGUST)
            return (Short) ? "Aug" : "August";
        else if (month == Calendar.SEPTEMBER)
            return (Short) ? "Sep" : "September";
        else if (month == Calendar.OCTOBER)
            return (Short) ? "Okt" : "Oktober";
        else if (month == Calendar.NOVEMBER)
            return (Short) ? "Nov" : "November";
        else if (month == Calendar.DECEMBER)
            return (Short) ? "Dez" : "Dezember";
        else
            return "???";
    }

    /**
     * Returns the string representation of a weekday.
     * @param day - Day of week from java.util.Calendar.XX
     * @param Short - True for short form (Mo, Di ...),
     *         False for long form (Montag, Dienstag ...)
     * @return String representation. 
     */
    public static String dayOfWeek2String(int day, boolean Short) {
        if (day == Calendar.SUNDAY)
            return (Short) ? "So" : "Sonntag";
        else if (day == Calendar.MONDAY)
            return (Short) ? "Mo" : "Montag";
        else if (day == Calendar.TUESDAY)
            return (Short) ? "Di" : "Dienstag";
        else if (day == Calendar.WEDNESDAY)
            return (Short) ? "Mi" : "Mittwoch";
        else if (day == Calendar.THURSDAY)
            return (Short) ? "Do" : "Donnerstag";
        else if (day == Calendar.FRIDAY)
            return (Short) ? "Fr" : "Freitag";
        else if (day == Calendar.SATURDAY)
            return (Short) ? "Sa" : "Samstag";
        else
            return "???";
    }

    /**
     * Calculate the difference of two dates, measured in days.
     * If passed date is in future, return value will be negative.
     * If passed date is same date (no matter what time), return
     * value will be zero.
     * @param d - Date to calculate difference
     * @return Difference of days.
     */
    public long dayDiff(Date d) {
        Date d1 = (Date) this.clone();
        Date d2 = (Date) d.clone();
        d1.set(Calendar.HOUR_OF_DAY, 0);
        d2.set(Calendar.HOUR_OF_DAY, 0);
        d1.set(Calendar.MINUTE, 0);
        d2.set(Calendar.MINUTE, 0);
        d1.set(Calendar.SECOND, 0);
        d2.set(Calendar.SECOND, 0);
        d1.set(Calendar.MILLISECOND, 0);
        d2.set(Calendar.MILLISECOND, 0);

        long hours = (d1.getTimeInMillis() - d2.getTimeInMillis()) / 1000 / 60 / 60;
        
        /*
         * Daylight saving might produce an error of -1 or 1.
         */
        long dst = hours % 24;
        
        return (hours - (dst == -23 ? 1 : dst)) / 24;
    }

    /**
     * Calculate the difference of two dates, measured in minutes.
     * If passed date is in future, return value will be negative.
     * If passed date is same date and time, return value will be zero.
     * @param d - Date to calculate difference
     * @return Difference of minutes.
     */
    public long minDiff(Date d) {
        Calendar d1 = (Calendar) this.clone();
        Calendar d2 = (Calendar) d.clone();
        d1.set(Calendar.SECOND, 0);
        d1.set(Calendar.MILLISECOND, 0);
        d2.set(Calendar.SECOND, 0);
        d2.set(Calendar.MILLISECOND, 0);
        if (!this.hasTime) {
            d1.set(Calendar.HOUR_OF_DAY, 0);
            d1.set(Calendar.MINUTE, 0);
        }
        if (!d.hasTime()) {
            d2.set(Calendar.HOUR_OF_DAY, 0);
            d2.set(Calendar.MINUTE, 0);
        }

        return (d1.getTimeInMillis() - d2.getTimeInMillis()) / 1000 / 60;
    }
    
    /**
     * Compares day, month and year with todays date.
     * @return True if the date equals today.
     */
    public boolean isToday() {
        Date today = new Date();
        return today.get(Calendar.YEAR) == this.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == this.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == this.get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * 
     * @return Index of this dates weekday within this month. E.g. if
     *         this is the first Monday in this month, return will be 1. If
     *         this is the third Friday in this month, return will be 3.
     *         In case of the last weekday, return will be 0. 
     */
    public int getWeekdayIndex() {
        int weekday = this.get(java.util.Calendar.DAY_OF_WEEK);
        int day = this.get(java.util.Calendar.DAY_OF_MONTH);
        int weekday_cnt = 0;
        Date test = (Date) this.clone();
        test.set(java.util.Calendar.DAY_OF_MONTH, 1);
        while (test.get(java.util.Calendar.DAY_OF_MONTH) != day) {
            if (test.get(java.util.Calendar.DAY_OF_WEEK) == weekday)
                weekday_cnt++;
            test.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        test.set(java.util.Calendar.DAY_OF_MONTH, test.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        if (day > test.get(java.util.Calendar.DAY_OF_MONTH) - 7)
            weekday_cnt = -1;
        return weekday_cnt + 1;
    }
    
    /**
     * 
     * @return Number of days remaining in this month.
     */
    public int getDaysToEndOfMonth() {
        return this.getActualMaximum(java.util.Calendar.DAY_OF_MONTH) -
                this.get(java.util.Calendar.DAY_OF_MONTH);
    }
}
