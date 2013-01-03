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

import javax.swing.JLabel;

/**
 * Make a label transparent slowly.
 * @author Johannes Steltzer
 *
 */
public class MessageTransparator 
	extends Thread {
	
	/** label to make transparent */
	private JLabel messageLabel;
	
	/** set to false to stop this thread */
	private boolean running;
	
	/**
	 * Construct a new thread to make a label
	 * transparent slowly.
	 * Call interrupt to repeat this process.
	 * @param messageLabel - Label of interest
	 */
	public MessageTransparator(JLabel messageLabel) {
		this.messageLabel = messageLabel;
		this.running = true;
	}
	
	/**
	 * Stop this thread.
	 */
	public void stopit() {
		this.running = false;
		this.interrupt();
	}
	
	@Override
	public void run() {
		while (running) {
			try {
				messageLabel.setForeground(new Color(.5f, .5f, .5f, 1f));
				Thread.sleep(1000);
				for (int i = 20; i >= 0; i--) {
					Thread.sleep(100);
					messageLabel.setForeground(new Color(.5f, .5f, .5f, ((float) i) / 20f));
				}
				Thread.sleep(Integer.MAX_VALUE);
			}
			catch (InterruptedException e) {}
		}
	}
}
