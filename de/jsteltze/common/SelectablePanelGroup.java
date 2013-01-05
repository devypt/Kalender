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

package de.jsteltze.common;

import java.awt.Color;
import java.util.Vector;

import javax.swing.border.Border;

import org.apache.log4j.Logger;

/**
 * Right rolling border for calendar button panel.
 * @author Johannes Steltzer
 *
 */
public class SelectablePanelGroup {
	
	/** All panels belonging to this group. */
	private Vector<SelectablePanel> panels;
	
	/** Selection color. */
	private Color selectionColor;
	
	/** Selection border. */
	private Border selectionBorder;
	
	/** Panel currently selected. */
	private SelectablePanel selected = null;
	
	/** Listener to notify if a panel has been selected. */
	private SelectablePanelListener listener;
	
	private static Logger logger = Logger.getLogger(SelectablePanelGroup.class);

	/**
	 * Construct a new panel group for selectable panels.
	 * @param listener - Listener to notify if a panel has been selected
	 * @param selectionColor - Selection color
	 */
	public SelectablePanelGroup(SelectablePanelListener listener, Color selectionColor) {
		panels = new Vector<SelectablePanel>();
		this.selectionColor = selectionColor;
		this.listener = listener;
		this.selectionBorder = null;
	}
	
	   /**
     * Construct a new panel group for selectable panels.
     * @param listener - Listener to notify if a panel has been selected
     * @param selectionColor - Selection color
     * @param selectionBorder - Selection border
     */
    public SelectablePanelGroup(SelectablePanelListener listener, 
            Color selectionColor, Border selectionBorder) {
        panels = new Vector<SelectablePanel>();
        this.selectionColor = selectionColor;
        this.listener = listener;
        this.selectionBorder = selectionBorder;
    }
	
	/**
	 * Add a panel to this group
	 * @param sp - Selectable panel to add
	 */
	public void add(SelectablePanel sp) {
		panels.add(sp);
		sp.setSelectionColor(selectionColor);
		sp.setSelectionBorder(selectionBorder);
	}
	
	/**
	 * Set a panel of this group as temporarily selected (due
	 * to a mouse hover event). This will unset all other
	 * temporarily selected panels.
	 * @param sp - Panel of this group to select
	 */
	public void setSelected(SelectablePanel sp) {
	    logger.debug("setSelected: " + sp.hashCode());
		for (SelectablePanel panel : panels)
			panel.setSelected(false);
		sp.setSelected(true);
	}
	
	/**
	 * Set a panel of this group as permanently selected (due
	 * to a mouse click event). This will unset all other
	 * permanently selected panels.
	 * @param sp - Panel of this group to select
	 */
	public void setClicked(SelectablePanel sp) {
	    logger.debug("setClicked: " + sp.hashCode());
		if (selected != null) {
			selected.setClicked(false);
			selected.setSelected(false);
		}
		selected = sp;
		sp.setSelected(true);
		sp.setClicked(true);
		
		listener.panelSelected(sp);
	}
}
