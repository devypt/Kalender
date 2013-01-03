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

package de.jsteltze.common;

/**
 * Math utilities.
 * @author Johannes Steltzer
 *
 */
public class Math {
	
	private Math() {}
	
	/**
	 * Divide a by b and cut the remaining.
	 * @param a - Divident
	 * @param b - Divisor
	 * @return floor(a/b).
	 */
	public static int div(int a, int b) {
		return (a - a % b) / b;
	}
	
	/**
	 * Calculate the date of easter sunday.
	 * @param year - Year to calculate easter sunday
	 * @return Days from 1st of March to easter sunday.
	 */
	public static int easterSunday(int year) {
		/*
		 * Gauss is not correct for some exceptions like 1954/1981.
		 * Use Lichterberg:
		 */
		int a = year % 19;
		int k = div(year, 100);
		int m = 15 + div(3 * k + 3, 4) - div(8 * k + 13, 25);
		int d = (19 * a + m) % 30;
		int s = 2 - div(3 * k + 3, 4);
		int r = div(d, 29) + (div(d, 28) - div(d, 29)) * div(a, 11);
		int og = 21 + d - r;
		int sz = 7 - (year + div(year, 4) + s) % 7;
		int oe = 7 - (og - sz) % 7;
		return og + oe - 1;
	}
}
