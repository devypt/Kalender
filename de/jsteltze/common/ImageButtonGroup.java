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

import java.util.Vector;

/**
 * Group for ImageButtons. In a group only one button can be 
 * pressed at a time. This is for buttons with the ability to
 * stay pressed.
 * @author Johannes Steltzer
 *
 */
public class ImageButtonGroup {
	
	/** all ImageButtons belonging to this group */
	private Vector<ImageButton> imageButtons;

	/**
	 * Construct an empty group.
	 */
	public ImageButtonGroup() {
		imageButtons = new Vector<ImageButton>();
	}

	/**
	 * Add an ImageButton to join this group.
	 * @param x - ImageButton to add
	 */
	public void add(ImageButton x) {
		imageButtons.add(x);
	}

	/**
	 * Activates (presses) a button of this group. All other buttons 
	 * in this group will be released.
	 * @param x - Button to press
	 */
	public void activate(ImageButton x) {
		for (ImageButton i : imageButtons)
			if (!i.equals(x))
				i.setPressed(false);
			else
				i.setPressed(true);
	}
}
