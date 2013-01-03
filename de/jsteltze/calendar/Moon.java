package de.jsteltze.calendar;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Calendar;

public class Moon {
	
	public static final byte MOON_NONE = 0x00;
	public static final byte MOON_FULL = 0x01;
	public static final byte MOON_DEC_HALF = 0x02;
	public static final byte MOON_NEW = 0x03;
	public static final byte MOON_INC_HALF = 0x04; 
	
	private static final double MOON_DIV = .2709109318f;
	
	/**
	 * 
	 * @return Moon phase of this date (see Date.MOON_XXX).
	 */
	public static byte getMoonPhase(Date date) {
		/*
	      calculates the moon phase (0-7), accurate to 1 segment.
	      0 = > new moon.
	      4 => full moon.
	      */
		int y = date.get(Calendar.YEAR);
		int m = date.get(Calendar.MONTH) + 1;
		int d = date.get(Calendar.DAY_OF_MONTH);
	    int c, e;
	    double jd;
	    //int b;

	    if (m < 3) {
	        y--;
	        m += 12;
	    }
	    ++m;
	    c = (int) (365.25f * (double) y);
	    e = (int) (30.6f * (double) m);
	    jd = c + e + d - 694039.09f;  /* jd is total days elapsed */
	    jd /= 29.53f;           /* divide by the moon cycle (29.53 days) */
	    //b = (int) Math.floor(jd);		   /* int(jd) -> b, take integer part of jd */
	    //jd -= (double) b;		   /* subtract integer part to leave fractional part of original jd */
	    jd -= Math.floor(jd);
	    jd *= 8f;
	    //b = (int) (jd * 8f + .5f);	   /* scale fraction from 0-8 and round by adding 0.5 */
	    //b = b & 7;		   /* 0 and 8 are the same so turn 8 into 0 */
	    //return b;
	    if (jd < MOON_DIV / 2f || (jd < 8f && jd > 8f - MOON_DIV / 2f))
	    	return MOON_NEW;
	    else if (Math.abs(jd - 2f) < MOON_DIV / 2f)
	    	return MOON_INC_HALF;
	    else if (Math.abs(jd - 4f) < MOON_DIV / 2f)
	    	return MOON_FULL;
	    else if (Math.abs(jd - 6f) < MOON_DIV / 2f)
	    	return MOON_DEC_HALF;
	    else
	    	return MOON_NONE;
		
		/*k
	      Calculates the moon phase (0-7), accurate to 1 segment.
	      0 = > new moon.
	      4 => Full moon.
	    */
	   
//	    int g, e;
//		int year = this.get(Calendar.YEAR);
//		int month = this.get(Calendar.MONTH) + 1;
//		int day = this.get(Calendar.DAY_OF_MONTH);
//
//	    if (month == 1) --day;
//	    else if (month == 2) day += 30;
//	    else // m >= 3
//	    {
//	        day += 28 + (month-2)*3059/100;
//
//	        // adjust for leap years
//	        if ((year & 3) == 0) ++day;
//	        if ((year%100) == 0) --day;
//	    }
//	   
//	    g = (year-1900)%19 + 1;
//	    e = (11*g + 18) % 30;
//	    if ((e == 25 && g > 11) || e == 24) e++;
//	    return ((((e + day)*6+11)%177)/22 & 7);
	}
	
	/**
	 * Paint a moon state.
	 * @param moonState - Moon state to paint (see Moon.MOON_XXX)
	 * @param g - Graphics object to paint on
	 * @param color - Color of the moon icon
	 * @param bgcolor - Background color
	 * @param x - Location where to paint
	 * @param size - Desired size (width and height of the moon icon)
	 */
	public static void paint(byte moonState, Graphics g, 
			Color color, Color bgcolor, Point x, int size) {
		if (moonState == Moon.MOON_NONE)
			return;
		g.setColor(color);
		if (moonState == Moon.MOON_FULL)
			g.fillOval(x.x, x.y, size, size);
		else if (moonState == Moon.MOON_NEW)
			g.drawOval(x.x, x.y, size, size);
		else if (moonState == Moon.MOON_DEC_HALF) {
			g.fillOval(x.x, x.y, size, size);
			g.setColor(bgcolor);
			g.fillRect(x.x + size / 2 + 1, x.y, size / 2, size);
		}
		else if (moonState == Moon.MOON_INC_HALF) {
			g.fillOval(x.x, x.y, size, size);
			g.setColor(bgcolor);
			g.fillRect(x.x, x.y, size / 2 - 1, size);
		}
	}
}