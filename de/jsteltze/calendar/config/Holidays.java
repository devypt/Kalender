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

/**
 * Class holding the different holidays supported by
 * the calendar.
 * @author Johannes Steltzer
 *
 */
public class Holidays {
	
	/* supported holidays by law */
	public static final int TOTAL_HOLIDAY_LAW = 16;
	
	/** Neujahr */
	public static final int NEUJAHR = 0x00000001;
	
	/** Heilige drei Könige */
	public static final int HL3K = 0x00000002;
	
	/** Gründonnerstag */
	public static final int GRDO = 0x00000004;
	
	/** Karfreitag */
	public static final int KARFR = 0x00000008;
	
	/** Ostermontag */
	public static final int OSTERMO = 0x00000010;
	
	/** Tag der Arbeit */
	public static final int TDA = 0x00000020;
	
	/** Christihimmelfahrt */
	public static final int CHRHIMMELF = 0x00000040;
	
	/** Pfingstmontag */
	public static final int PFINGSTMO = 0x00000080;
	
	/** Fronleichnahm */
	public static final int FRONLEICH = 0x00000100;
	
	/** Mariä Himmelfahrt */
	public static final int MHIMMELF = 0x00000200;
	
	/** Tag der deutschen Einheit */ 
	public static final int TDDE = 0x00000400;
	
	/** Reformationstag */
	public static final int REFORM = 0x00000800;
	
	/** Allerheiligen */
	public static final int ALLERH = 0x00001000;
	
	/** Buß- und Bettag */
	public static final int BUBT = 0x00002000;
	
	/** 1. Weihnachtsfeiertag */
	public static final int WEIH1 = 0x00004000;
	
	/** 2. Weihnachtsfeiertag */
	public static final int WEIH2 = 0x00008000;
	
	
	/* supported special days */ 
	public static final int TOTAL_HOLIDAY_SPECIAL = 19;
	
	/** Valentinstag */
	public static final int VALENTIN = 0x00000001;
	
	/** Rosenmontag */
	public static final int ROSENM = 0x00000002;
	
	/** Faschingsdienstag */
	public static final int FASCHING = 0x00000004;
	
	/** Aschermittwoch */
	public static final int ASCHERM = 0x00000008;
	
	/** Frauentag */
	public static final int FRAUEN = 0x00000010;
	
	/** Palmsonntag */
	public static final int PALMS = 0x00000020;
	
	/** Muttertag */
	public static final int MUTTER = 0x00000040;
	
	/** Kindertag */
	public static final int KINDER = 0x00000080;
	
	/** Halloween */
	public static final int HALLOWEEN = 0x00000100;
	
	/** Martinstag */
	public static final int MARTIN = 0x00000200;
	
	/** Volkstrauertag */
	public static final int VOLKSTRAUER = 0x00000400;
	
	/** Totensonntag */
	public static final int TOTENS = 0x00000800;
	
	/** 1. Advent */
	public static final int ADV1 = 0x00001000;
	
	/** Nikolaus */
	public static final int NIKO = 0x00002000;
	
	/** 2. Advent */
	public static final int ADV2 = 0x00004000;
	
	/** 3. Advent */
	public static final int ADV3 = 0x00008000;
	
	/** 4. Advent */
	public static final int ADV4 = 0x00010000;
	
	/** Heiligabend */
	public static final int HEILIGA = 0x00020000;
	
	/** Silvester */
	public static final int SILVESTER = 0x00040000;

	/** default set of holidays by law */
	public static final int DEFAULT_HOLIDAYS = NEUJAHR | KARFR | OSTERMO
			| TDA | CHRHIMMELF | PFINGSTMO | TDDE | WEIH1 | WEIH2;
	
	/** default set of special days */
	public static final int DEFAULT_SPECIAL = VALENTIN | MUTTER | KINDER
			| HALLOWEEN | HEILIGA | SILVESTER;
	
	/**
	 * Get the number of holidays enabled.
	 * @param code - Holidays encoded as integer
	 * @return Number of bits set.
	 */
	public static int getNumberOfHolidays(int code) {
		int num = 0;
		for (int i = 0; i < 32; i++)
			if ((code & (1 << i)) != 0)
				num++;
		return num;
	}
}
