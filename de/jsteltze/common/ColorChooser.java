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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

/**
 * Rectangle area to choose any color by red, green
 * and blue shares.
 * @author Johannes Steltzer
 *
 */
public class ColorChooser 
	extends Canvas 
	implements MouseListener {
	
	private static final long serialVersionUID = 1L;
	
	/** Parent to notify about a color chosen. */
	private ColorChooserListener caller;
	
	/** Green and blue value of selected pixel. */
	private int green, blue;

	/**
	 * Construct a new color chooser.
	 * @param caller - Parent to notify about a color chosen
	 */
	public ColorChooser(ColorChooserListener caller) {
		this.caller = caller;
		this.setSize(128, 145);
		this.addMouseListener(this);
		green = 128;
		blue = 128;
	}

	@Override
	public void paint(Graphics g) {
		/*
		 * Draw green-blue square with R=128
		 */
		for (int i = 0; i < 128; i++)
			for (int j = 0; j < 128; j++) {
				int red = 128;
				int green = i * 2;
				int blue = j * 2;
				g.setColor(new Color(red, green, blue));
				g.drawLine(i, j, i, j);
			}
		/*
		 * Draw red band with G=B=128
		 */
		for (int i = 0; i < 128; i++) {
			g.setColor(new Color(2 * i, green, blue));
			g.drawLine(i, 129, i, 139);
		}
		drawArrow(g, 64);
	}

	/**
	 * Draw a little arrow blow the red band.
	 * @param g - Graphics to paint on
	 * @param pos - Position (in pixels: 0 to 128) to indicate the
	 * 		current R value 
	 */
	private void drawArrow(Graphics g, int pos) {
		g.setColor(Color.BLACK);
		g.drawLine(pos, 140, pos + 5, 145);
		g.drawLine(pos, 140, pos - 5, 145);
		g.drawLine(pos, 141, pos + 4, 145);
		g.drawLine(pos, 141, pos - 4, 145);
		g.drawLine(pos, 142, pos + 3, 145);
		g.drawLine(pos, 142, pos - 3, 145);
		g.drawLine(pos, 143, pos + 2, 145);
		g.drawLine(pos, 143, pos - 2, 145);
		g.drawLine(pos, 144, pos + 1, 145);
		g.drawLine(pos, 144, pos - 1, 145);
		g.drawLine(pos, 145, pos, 145);
	}

	/**
	 * Remove arrow below the red band.
	 * @param g - Graphics to paint on
	 */
	private void clearArrow(Graphics g) {
		g.setColor(this.getBackground());
		g.fillRect(0, 140, 128, 6);
	}

	/**
	 * Convert a canvas into an image.
	 * @param c - Canvas to convert
	 * @return Converted image object.
	 */
	public static Image canvas2Image(Canvas c) {
		BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		c.paint(image.getGraphics());
		return image;
	}

	@Override
	public void mouseClicked(MouseEvent m) {
		/*
		 * Grab the color chosen
		 */
		int[] array = new int[1];
		PixelGrabber pg = new PixelGrabber(canvas2Image(this), m.getX(),
				m.getY(), 1, 1, array, 0, 1);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			Logger.error("[mouseClicked] interrupted while grabbing pixels: "+e.toString());
			return;
		}
		
		/*
		 * Extract the R,G,B values
		 */
		int red = (array[0] & 0x00ff0000) >> 16;
		green = (array[0] & 0x0000ff00) >> 8;
		blue = (array[0] & 0x000000ff);

		Logger.debug("COLOR: r=" + red + "g=" + green + "b=" + blue);

		/*
		 * Notify parent
		 */
		caller.colorChosen(new Color(red, green, blue));

		Graphics g = this.getGraphics();

		for (int i = 0; i < 128; i++) {
			g.setColor(new Color(2 * i, green, blue));
			g.drawLine(i, 129, i, 139);
		}

		clearArrow(g);
		drawArrow(g, red / 2);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
}
