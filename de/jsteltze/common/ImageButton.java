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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

/**
 * Images as buttons with two states: pressed and not pressed.
 * @author Johannes Steltzer
 *
 */
public class ImageButton 
	extends JLabel 
	implements MouseListener {
	
	private static final long serialVersionUID = 1L;

	/** image of this button in released state */
	private ImageIcon notPressed;

	/** image of this button in pressed state */
	private ImageIcon pressed;

	/**
	 * button group this button belongs to. Only one button of a group 
	 * can be pressed at a time. Might be null
	 */
	private ImageButtonGroup myGroup;

	/** true if this button is pressed currently */
	private boolean isPressed;

	/** true if this button shall stay in pressed state */
	private boolean stayPressed;

	/** list of listeners to notify when this button gets pressed */
	private Vector<ImageButtonListener> listeners;
	
	private static Logger logger = Logger.getLogger(ImageButton.class);

	/**
	 * Construct new ImageButton.
	 * @param notPressed - Path to image for released state
	 * @param pressed - Path to image for pressed state
	 * @param stayPressed - True if this button shall stay pressed
	 */
	public ImageButton(String notPressed, String pressed, boolean stayPressed) {
		super(new ImageIcon(ImageButton.class.getClassLoader().getResource(notPressed)));
		this.notPressed = new ImageIcon(
				this.getClass().getClassLoader().getResource(notPressed));
		this.pressed = new ImageIcon(this.getClass().getClassLoader().getResource(pressed));
		this.stayPressed = stayPressed;
		this.isPressed = false;
		this.listeners = new Vector<ImageButtonListener>();
		this.addMouseListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent m) {
	}

	@Override
	public void mouseExited(MouseEvent m) {
		m.getComponent().setCursor(
				Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void mouseEntered(MouseEvent m) {
		m.getComponent().setCursor(
				Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	@Override
	public void mousePressed(MouseEvent m) {
		logger.debug("pressed");
		if (isPressed) {
			if (myGroup == null)
				this.setPressed(false);
		}
		else {
			if (myGroup != null)
				myGroup.activate(this);
			else
				this.setPressed(true);
		}
	}

	@Override
	public void mouseReleased(MouseEvent m) {
		logger.debug("released");
		if (stayPressed)
			return;
		if (isPressed && myGroup != null)
			;
		else if (isPressed)
			this.setPressed(false);
	}

	/**
	 * Changes the state of this button to pressed or released.
	 * @param x - True for pressed state, false for released state
	 */
	public void setPressed(boolean x) {
		if (x) {
			this.setIcon(pressed);
			this.isPressed = true;
			for (ImageButtonListener i : listeners)
				i.buttonPressed(this);
		} 
		else {
			this.setIcon(notPressed);
			this.isPressed = false;
			if (myGroup == null)
				for (ImageButtonListener i : listeners)
					i.buttonPressed(this);
		}
	}

	/**
	 * 
	 * @return True if this button is currently in pressed state, false
	 * 		otherwise. 
	 */
	public boolean isPressed() {
		return this.isPressed;
	}

	/**
	 * Apply a button group for this button. In a button group only 
	 * one button can be pressed at a time.
	 * @param x - Button group to set
	 */
	public void setButtonGroup(ImageButtonGroup x) {
		myGroup = x;
		myGroup.add(this);
	}

	/**
	 * Adds a listener for this button to be notified about state
	 * changes.
	 * @param x - Object that implements ImageButtonListener
	 */
	public void addButtonListener(ImageButtonListener x) {
		listeners.add(x);
	}

	/**
	 * Stops an object from being notified about state changes of this
	 * button.
	 * @param x - Object that implements ImageButtonListener
	 */
	public void removeButtonListener(ImageButtonListener x) {
		listeners.remove(x);
	}
}
