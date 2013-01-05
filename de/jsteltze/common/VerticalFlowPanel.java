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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Vector;

import javax.swing.JPanel;

/**
 * Puts elements vertically one after another 
 * from top to bottom.
 * @author Johannes Steltzer
 *
 */
public class VerticalFlowPanel 
	extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	/** subpanel below the added component */
	private Vector<JPanel> panels;
	
	/** vertical padding (gap) in pixels */
	private int vgap;
	
	/** all components added */
	private Vector<Component> components;
	
	/**
	 * Construct a new vertical flow panel to
	 * add components to it.
	 */
	public VerticalFlowPanel() {
		this(0);
	}
	
	/**
	 * Construct a new vertical flow panel to
	 * add components to it.
	 * @param valign - vertical padding in pixels
	 */
	public VerticalFlowPanel(int vgap) {
		super(new BorderLayout(0, vgap));
		this.components = new Vector<Component>();
		this.panels = new Vector<JPanel>();
		this.vgap = vgap;
	}

	@Override
	public Component add(Component comp) {
		if (components.size() == 0) {
			super.add(comp, BorderLayout.NORTH);
			panels.add(new JPanel(new BorderLayout(0, vgap)));
			super.add(panels.get(0), BorderLayout.CENTER);
		}
		else {
			panels.lastElement().add(comp, BorderLayout.NORTH);
			JPanel subpanel = new JPanel(new BorderLayout(0, vgap));
			panels.lastElement().add(subpanel, BorderLayout.CENTER);
			panels.add(subpanel);
		}
		components.add(comp);
		return comp;
	}
	
	/**
	 * Searches recursively a component for removal.
	 * @param comp - Component to search and remove
	 * @param parent - Container to start search within
	 */
	private void remove(Component comp, JPanel parent) {
		Component [] cps = parent.getComponents();
		if (cps.length > 0 && comp.equals(cps[0])) {
			parent.remove(0);
			return;
		}
		if (cps.length > 1 && comp.equals(cps[1])) {
			parent.remove(1);
			return;
		}
		if (cps.length == 1 && cps[0] instanceof JPanel)
			remove(comp, (JPanel) cps[0]);
		if (cps.length == 2 && cps[1] instanceof JPanel)
			remove(comp, (JPanel) cps[1]);
	}
	
	/**
	 * Removes a component from the panel.
	 * @param comp - Component to remove
	 * @return True if component has been found and successfully
	 * 		removed
	 */
	public boolean removeComponent(Component comp) {
		if (super.getComponent(0).equals(comp))
			super.remove(0);
		else
			remove(comp, panels.firstElement());
		return components.remove(comp);
	}
	
	@Override
	public Component [] getComponents() {
		return components.toArray(new Component[0]);
	}
}
