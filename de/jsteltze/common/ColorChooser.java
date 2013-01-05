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
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

/**
 * Rectangle area to choose any color by red, green
 * and blue shares.
 * @author Johannes Steltzer
 *
 */
public class ColorChooser 
	extends JPanel 
	implements MouseListener {
	
	private static final long serialVersionUID = 1L;
	
	/** Parent to notify about a color chosen. */
	private ColorChooserListener caller;
	
	/** Green and blue value of selected pixel. */
	private int green, blue;
	
	/** Holds the selected green-blue point (in pixels: 0..127). */
	private Point gbPoint = null;
	
	/** Holds the specified initial selection color (or null). */
	private Color initColor = null;
	
	private static Logger logger = Logger.getLogger(ColorChooser.class);

	/**
	 * Construct a new color chooser.
	 * @param caller - Parent to notify about a color chosen
	 */
	public ColorChooser(ColorChooserListener caller) {
		this.caller = caller;
		this.setSize(128, 145);
		this.addMouseListener(this);
		this.setOpaque(false);
		green = 128;
		blue = 128;
	}
	
	/**
	 * Construct a new color chooser.
	 * @param caller - Parent to notify about a color chosen
	 * @param init - Color that initially is selected
	 */
	public ColorChooser(ColorChooserListener caller, Color init) {
		this(caller);
		this.initColor = init;
	}
	
	/**
	 * Draw a green-blue area with fixed red=128.
	 * @param g - Graphics object to paint on
	 * @param start - Starting point where x is green and y is blue in pixels (0..127)
	 * @param end - Ending point where x is green and y is blue in pixels (0..127)
	 */
	private void drawGBArea(Graphics g, Point start, Point end) {
		int red = 128;
		for (int i = start.x < 0 ? 0 : start.x; i <= end.x && i < 128; i++)
			for (int j = start.y < 0 ? 0 : start.y; j <= end.y && j < 128; j++) {
				int green = i * 2;
				int blue = j * 2;
				g.setColor(new Color(red, green, blue));
				g.drawLine(i, j, i, j);
			}
	}
	
	/**
	 * Draw the red values band below the GB area.
	 * @param g - Graphics object to paint on
	 * @param green - Fixed green value 
	 * @param blue - Fixed blue value
	 */
	private void drawRArea(Graphics g, int green, int blue) {
		for (int i = 0; i < 128; i++) {
			g.setColor(new Color(2 * i, green, blue));
			g.drawLine(i, 129, i, 139);
		}
	}
	
	/**
	 * Draws an oval around the selected green-blue point.
	 * Selected point is held in 'gbPoint' (migth also be null). 
	 * @param g - Graphics object to paint on
	 */
	private void markSelectedGB(Graphics g) {
		if (gbPoint != null) {
			g.setColor(Color.BLACK);
			g.drawOval(gbPoint.x - 1, gbPoint.y - 1, 2, 2);
		}
	}
	
	/**
	 * Deletes the oval painted around a previously selected
	 * green-blue point.
	 * @param g - Graphics object to paint on
	 */
	private void resetSelectedGB(Graphics g) {
		if (gbPoint != null)
			drawGBArea(g, new Point(gbPoint.x - 2, gbPoint.y - 2),
					new Point(gbPoint.x + 2, gbPoint.y + 2));
	}
	
	/**
	 * Sets a color as selected. This will draw an oval around the
	 * green-blue point, draw the red values band properly and marks
	 * the red value with an arrow.
	 * @param g - Graphics object to paint on
	 * @param x - Color to set selected
	 */
	public void setColor(Graphics g, Color x) {
		logger.debug("set color: r="+x.getRed()+" g="+x.getGreen()+" b="+x.getBlue());
		if (g == null)
			g = this.getGraphics();
		resetSelectedGB(g);
		gbPoint = new Point(x.getGreen() / 2, x.getBlue() / 2);
		markSelectedGB(g);
		drawRArea(g, x.getGreen(), x.getBlue());
		clearArrow(g);
		drawArrow(g, x.getRed() / 2);
	}

	@Override
	public void paint(Graphics g) {
		/*
		 * Draw green-blue square with R=128
		 */
		drawGBArea(g, new Point(0, 0), new Point(127, 127));

		/*
		 * Draw red band with G=B=128
		 */
		drawRArea(g, green, blue);
		drawArrow(g, 64);
		
		/*
		 * Initialize with the color specified.
		 * Only once on fist start.
		 */
		if (initColor != null) {
			setColor(g, initColor);
			green = initColor.getGreen();
			blue = initColor.getBlue();
		}
	}

	/**
	 * Draw a little arrow blow the red band.
	 * @param g - Graphics to paint on
	 * @param pos - Position (in pixels: 0 to 128) to indicate the
	 * 		current R value 
	 */
	private void drawArrow(Graphics g, int pos) {
		g.setColor(Color.BLACK);
		g.fillPolygon(
				new int[]{pos - 4, pos, pos + 4},
				new int[]{145, 140, 145}, 3);
	}

	/**
	 * Remove arrow below the red band.
	 * @param g - Graphics to paint on
	 */
	private void clearArrow(Graphics g) {
		g.setColor(this.getBackground());
		g.fillRect(0, 141, 128, 6);
	}

	/**
	 * Convert a canvas into an image.
	 * @param c - Canvas to convert
	 * @return Converted image object.
	 */
	public static Image panelToImage(JPanel c) {
		BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		c.paint(image.getGraphics());
		return image;
	}

	@Override
	public void mouseClicked(MouseEvent m) {
		if (m.getY() == 128 || m.getY() >= 140)
			return;
		
		initColor = null;
		Graphics g = this.getGraphics();
		
		if (m.getX() < 128 && m.getY() < 128) {
			resetSelectedGB(g);
			gbPoint = m.getPoint();
			markSelectedGB(g);
		}
		
		/*
		 * Grab the color chosen
		 */
		int[] array = new int[1];
		PixelGrabber pg = new PixelGrabber(panelToImage(this), m.getX(),
				m.getY(), 1, 1, array, 0, 1);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			logger.error("[mouseClicked] interrupted while grabbing pixels: "+e.toString());
			return;
		}
		
		/*
		 * Extract the R,G,B values
		 */
		int red = (array[0] & 0x00ff0000) >> 16;
		green = (array[0] & 0x0000ff00) >> 8;
		blue = (array[0] & 0x000000ff);

		logger.debug("COLOR: r=" + red + "g=" + green + "b=" + blue);

		/*
		 * Notify parent
		 */
		caller.colorChosen(new Color(red, green, blue));

		/*
		 * Draw the updated red band 
		 */
		drawRArea(g, green, blue);

		if (m.getY() > 128) {
			clearArrow(g);
			drawArrow(g, red / 2);
		}
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
