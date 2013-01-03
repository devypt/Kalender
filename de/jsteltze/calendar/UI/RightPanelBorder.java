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
 * Right rolling border for calendar button panel.
 * @author Johannes Steltzer
 *
 */
public class RightPanelBorder 
	extends Canvas {
	
	private static final long serialVersionUID = 1L;
	
	/** button panel color */
	private Color c;
	
	/** image to paint */
	private Image img;
	
	/**
	 * Construct a new right panel border.
	 * @param img - Image to paint
	 * @param c - Button panel color
	 */
	public RightPanelBorder(Image img, Color c) {
		this.c = c;
		this.img = img;
	}
	
	@Override
	public void paint(Graphics g2) {
		g2.drawImage(img, 0, 0, Color.black, this);
		
		if (c == null)
			return;
		g2.setColor(c);
		g2.drawLine(0, 0, 45, 0);
		g2.drawLine(0, 1, 42, 1);
		g2.drawLine(0, 2, 40, 2);
		g2.drawLine(0, 3, 39, 3);
		for (int i = 0; i < 27; i++)
			g2.drawLine(0, 4 + i, 37 - i, 4 + i);
		g2.drawLine(0, 31, 9, 31);
		g2.drawLine(0, 32, 8, 32);
		g2.drawLine(0, 33, 6, 33);
		g2.drawLine(0, 34, 3, 34);
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