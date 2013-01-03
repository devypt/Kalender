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

package de.jsteltze.calendar.UI;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

/**
 * Left rolling border for calendar button panel.
 * @author Johannes Steltzer
 *
 */
public class LeftPanelBorder 
	extends Canvas {
	
	private static final long serialVersionUID = 1L;
	
	/** button panel color */
	private Color c;
	
	/** image to paint */
	private Image img;
	
	/**
	 * Construct a new left panel border.
	 * @param img - Image to paint
	 * @param c - Button panel color
	 */
	public LeftPanelBorder(Image img, Color c) {
		this.c = c;
		this.img = img;
	}
	
	@Override
	public void paint(Graphics g1) {
		g1.drawImage(img, 0, 0, Color.black, this);
		
		if (c == null)
			return;
		g1.setColor(c);
		g1.drawLine(2, 0, 2, 0);
		g1.drawLine(3, 0, 3, 1);
		g1.drawLine(4, 0, 4, 2);
		g1.drawLine(5, 0, 5, 4);
		g1.fillRect(6, 0, 5, 31);
		g1.fillRect(7, 31, 4, 2);
		g1.drawLine(8, 33, 10, 33);
		g1.drawLine(9, 34, 10, 34);
	}
	
	/**
	 * 
	 * @param x - New color to set
	 */
	public void setColor(Color x) {
		this.c = x;
		repaint();
	}
}

