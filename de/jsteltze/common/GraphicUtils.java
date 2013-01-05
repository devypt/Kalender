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

import java.awt.Font;
import java.awt.Graphics;

/**
 * Graphic utilities.
 * @author Johannes Steltzer
 *
 */
public final class GraphicUtils {
	
	/**
	 * Constructor not for public use.
	 */
	private GraphicUtils() {}
	
	/**
	 * Returns the width a string would have if painted
	 * with the current font. String will not be painted!
	 * @param g - Graphics object to use with the desired font set
	 * @param x - String to get the width
	 * @return Width the painted string would have.
	 */
	public static int getStringWidth(Graphics g, String x) {
        return g.getFontMetrics().stringWidth(x);
	}
	
	/**
     * Returns the width a string would have if painted
     * with a specific font. String will not be painted!
     * @param g - Graphics object to use
     * @param font - Font of interest
     * @param x - String to get the width
     * @return Width the painted string would have with the font.
     */
    public static int getStringWidth(Graphics g, Font font, String x) {
        return g.getFontMetrics(font).stringWidth(x);
    }
}
