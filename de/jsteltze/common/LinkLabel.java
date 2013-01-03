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
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JLabel;

/**
 * Clickable label that is associated with a link.
 * @author Johannes Steltzer
 *
 */
public class LinkLabel 
	extends JLabel {
	
	private static final long serialVersionUID = 1L;
	
	/** linked url */
	private String url;
	
	/** linked window */
	private Window win;

	/** listener that handles clicks on this link */
	private MouseListener linker = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			LinkLabel self = (LinkLabel) e.getSource();
			if (self.url == null || self.url.equals("")) {
				if (self.win == null)
					return;
				else
					win.setVisible(true);
			} else
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e2) {
					e2.printStackTrace();
				}
		}

		public void mouseEntered(MouseEvent e) {
			e.getComponent().setCursor(
					Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	};

	/**
	 * Construct a new label with no link.
	 * @param label - Label
	 */
	public LinkLabel(String label) {
		super(label);
		url = "";
		win = null;
		setForeground(Color.BLUE);
		addMouseListener(linker);
	}

	/**
	 * Construct a new label which is linked with an url.
	 * @param label - Label
	 * @param url - URL
	 */
	public LinkLabel(String label, String url) {
		this(label);
		this.url = url;
		this.win = null;
	}

	/**
	 * Construct a new label which is linked with an url.
	 * @param label - Label
	 * @param tip - URL
	 * @param url - Tool tip
	 */
	public LinkLabel(String label, String tip, String url) {
		this(label, url);
		setToolTipText(tip);
	}

	/**
	 * Construct a new label which is linked with a window.
	 * @param label - Label
	 * @param window - Window to set visible on click
	 */
	public LinkLabel(String label, Window window) {
		this(label);
		this.win = window;
		this.url = "";
	}

	/**
	 * Construct a new label which is linked with a window.
	 * @param label - Label
	 * @param tip - Tool tip
	 * @param window - Window to set visible on click
	 */
	public LinkLabel(String label, String tip, Window window) {
		this(label, window);
		setToolTipText(tip);
	}
}
