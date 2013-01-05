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
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Right rolling border for calendar button panel.
 * @author Johannes Steltzer
 *
 */
public class SelectablePanel 
	extends JPanel 
	implements MouseMotionListener, MouseListener {
	
	private static final long serialVersionUID = 1L;
	
	/** True if panel is selected due to a mouse move event. */ 
	private boolean mouseHover = false;
	
	/** True if panel is permanently selected due to a mouse click event. */
	private boolean selected = false;
	
	/** Group of selectable panels this panel belongs to. */
	private SelectablePanelGroup group;
	
	/** Selection color and default color. */
	private Color selectionColor, defaultColor;
	
	/** Selection border (might be null for no border). */
	private Border selectionBorder;
		
	/**
	 * Construct a new selectable panel.
	 */
	public SelectablePanel() {
		super();
	}
	
	/**
	 * Construct a new selectable panel.
	 * @param layout - Layout to use
	 */
	public SelectablePanel(LayoutManager layout) {
		super(layout);
	}
	
	/**
	 * Assign this panel to a group of selectable panels.
	 * @param spg - Parent group
	 */
	public void setGroup(SelectablePanelGroup spg) {
		this.group = spg;
	}
	
	/**
	 * Set a selection color.
	 * @param selectionColor - Selection color to set
	 */
	public void setSelectionColor(Color selectionColor) {
		this.selectionColor = selectionColor;
	}
	
    /**
     * Set a selection color.
     * @param selectionColor - Selection color to set
     */
    public void setSelectionBorder(Border selectionBorder) {
        this.selectionBorder = selectionBorder;
    }
	
	/**
	 * Sets this panel as temporarily selected due to
	 * a mouse hover event.
	 * @param sel - True for selected (mouse hover), false
	 * 		for not selected (mouse not hover)
	 */
	public void setSelected(boolean sel) {
		if (!mouseHover && sel) {
			mouseHover = true;
			defaultColor = getBackground();
			setBackground(selectionColor);
			if (selectionBorder != null)
			    setBorder(selectionBorder);
		}
		else if (mouseHover && !sel && !selected) {
			mouseHover = false;
			setBackground(defaultColor);
			if (selectionBorder != null)
			    setBorder(new EmptyBorder(0, 0, 0, 0));
		}
	}
	
	/**
	 * Set this panel as permanently selected due to a 
	 * mouse click event.
	 * @param sel - True for selected (mouse clicked), false
	 * 		for not selected (mouse clicked somewhere else)
	 */
	public void setClicked(boolean sel) {
		selected = sel;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		if (!mouseHover)
			group.setSelected(this);
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		group.setClicked(this);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		if (arg0.getSource() instanceof SelectablePanel)
			((SelectablePanel) arg0.getSource()).setSelected(false);
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
}
