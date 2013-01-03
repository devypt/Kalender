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

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Frame with a progress bar and a cancel button.
 * @author Johannes Steltzer
 *
 */
public class ProgressBar 
	extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	/** progress bar */
	private JProgressBar jp;
	
	/** cancel button */
	private JButton cancel;
	
	/** true if cancel button has been clicked */
	private boolean cancelled = false;

	/**
	 * Construct a new progress bar.
	 * @param caller - Parent window
	 * @param title - Title of the frame
	 * @param indeterminated - True for unknown progress (continuous
	 * 		animation will be displayed), false for regular progress
	 */
	public ProgressBar(Window caller, String title, boolean indeterminated) {
		super(caller, title);
		setLayout(new GridLayout(2, 1));
		jp = new JProgressBar(0, 100);
		cancel = new JButton("Abbrechen");
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
				cancel.setEnabled(false);
			}
		});
		jp.setBorderPainted(true);
		jp.setStringPainted(true);
		jp.setValue(0);
		jp.setIndeterminate(indeterminated);
		add(jp);
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		lowerPanel.add(cancel);
		add(lowerPanel);
		setVisible(true);
		setResizable(false);
		setSize(300, 95);
		this.toFront();
		this.setAlwaysOnTop(true);
		if (caller != null)
			setLocation(caller.getLocation().x + 100, caller.getLocation().y + 100);
	}
	
	/**
	 * 
	 * @return True if cancel button was clicked.
	 */
	public boolean isCancelled() {
		return this.cancelled;
	}

	/**
	 * Add a value to the progress. Progress range is 0-100.
	 * @param value - Value (0-100) to add
	 */
	public void addValue(int value) {
		setValue(jp.getValue() + value);
	}

	/**
	 * Apply a new progress value. Possible range is 0-100.
	 * @param value - Value (0-100) to set. 
	 */
	public void setValue(int value) {
		jp.setValue(value);
		this.repaint();
	}

	/**
	 * Dispose the frame.
	 */
	public void close() {
		setVisible(false);
		dispose();
	}
}
