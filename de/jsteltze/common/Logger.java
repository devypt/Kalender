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
 * Very basic logging utility. Uses stderr for outputs.
 * @author Johannes Steltzer
 *
 */
public class Logger {
	
	private Logger()  {}
	
	private static void doLog(String msg) {
		System.err.println(System.currentTimeMillis() + " " + msg);
	}
	
	public static void debug(String msg) {
		doLog("DEBUG: " + msg);
	}
	
	public static void log(String msg) {
		doLog("INFO : " + msg);
	}
	
	public static void warn(String msg) {
		doLog("WARN : " + msg);
	}
	
	public static void error(String msg) {
		doLog("ERROR: " + msg);
	}
	
}
