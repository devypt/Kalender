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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import de.jsteltze.calendar.config.Const;

/**
 * Right rolling border for calendar button panel.
 * @author Johannes Steltzer
 *
 */
public class RightPanelBorder 
    extends JPanel {
    
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
        this.setPreferredSize(new Dimension(49, 37));
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.drawImage(img, 0, 0, Const.COLOR_BG_MAIN, this);
        
        if (c == null)
            return;
        g.setColor(c);
        g.drawLine(0, 0, 45, 0);
        g.drawLine(0, 1, 42, 1);
        g.drawLine(0, 2, 40, 2);
        g.drawLine(0, 3, 39, 3);
        for (int i = 0; i < 27; i++)
            g.drawLine(0, 4 + i, 37 - i, 4 + i);
        g.drawLine(0, 31, 9, 31);
        g.drawLine(0, 32, 8, 32);
        g.drawLine(0, 33, 6, 33);
        g.drawLine(0, 34, 3, 34);
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
