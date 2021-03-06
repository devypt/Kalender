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

package de.jsteltze.calendar.exceptions;

/**
 * Exception to throw in case of invalid dates.
 * @author Johannes Steltzer
 *
 */
public class InvalidDateException 
    extends Exception {
    
    private static final long serialVersionUID = 1L;

    /**
     * General invalid date exception.
     */
    public InvalidDateException() {
        super();
    }

    /**
     * 
     * @param s - Error description (e.g. exception.toString())
     */
    public InvalidDateException(String s) {
        super(s);
    }
}
