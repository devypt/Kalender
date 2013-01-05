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

import java.awt.Color;

/**
 * Color utilities.
 * @author Johannes Steltzer
 *
 */
public final class ColorUtil {
    
    /**
     * Constructor not for public use.
     */
    private ColorUtil() {}
	
	/**
	 * Add some saturation to a given color.
	 * @param x - Color to manipulate
	 * @param sat - Saturation to add (might be negative)
	 * @return Resulting color.
	 */
	public static Color addSaturation(Color x, float sat) {
		float [] hsb = Color.RGBtoHSB(x.getRed(), x.getGreen(), x.getBlue(), null);
		return Color.getHSBColor(hsb[0], 
				hsb[1] + sat > 1f ? 1f : hsb[1] + sat, hsb[2]);
	}
}
