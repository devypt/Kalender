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

package de.jsteltze.calendar.frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import de.jsteltze.calendar.Calendar;
import de.jsteltze.calendar.Date;
import de.jsteltze.calendar.Event;
import de.jsteltze.calendar.config.ColorSet;
import de.jsteltze.calendar.config.Const;
import de.jsteltze.calendar.tasks.RefreshTimeLabelTask;
import de.jsteltze.common.Logger;
import de.jsteltze.common.Music;
import de.jsteltze.common.Trans;
import de.jsteltze.common.VerticalFlowPanel;

/**
 * Notification summary frame.
 * @author Johannes Steltzer
 *
 */
public class TableOfNotifications 
	extends JDialog 
	implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Component that contains all events panels
	 * with the same day difference. 
	 */
	private Vector<VerticalFlowPanel> daySummaryPanels;
	
	/**
	 * All event panels (sorted by day difference) which
	 * are added to the proper daySummaryPanel.
	 */
	private Vector<JPanel> eventPanels;
	
	/**
	 * Main component that contains all
	 * daySummaryPanels (one per day).
	 */
	private VerticalFlowPanel mainPanel;
	
	/** buttons for event options */
	private  Vector<JButton> deleteButtons;
	private  Vector<JButton> editButtons;
	private  Vector<JButton> alarmButtons;
	
	/** event to notify of */
	private Vector<Event> events;
	
	/** parent calendar object */
	private Calendar caller;

	/**
	 * Sets the icon of this window.
	 */
	private void setIcon() {
		URL url = this.getClass().getClassLoader().getResource("media/calendar32.ico");
		Image ima = Toolkit.getDefaultToolkit().createImage(url);
		setIconImage(ima);
	}

	/**
	 * Arranges all elements in this dialog window.
	 */
	private void arrangeDialog() {
		setLayout(new BorderLayout());
		setBackground(caller.getConfig().getColors()[ColorSet.NOTI]);
		
		mainPanel = new VerticalFlowPanel();
		
		editButtons = new Vector<JButton>();
		deleteButtons = new Vector<JButton>();
		alarmButtons = new Vector<JButton>();
		eventPanels = new Vector<JPanel>();
		daySummaryPanels = new Vector<VerticalFlowPanel>();
		
		boolean firstRun = true;
		long previousDayDiff = -1;
		JPanel currentPanel = null;
		
		for (Event e : events) {
			long dayDiff = e.getNextDate().dayDiff(new Date());
			if (firstRun || dayDiff != previousDayDiff) {
				VerticalFlowPanel daySummaryPanel = new VerticalFlowPanel(8);
				daySummaryPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), e.getDayDiffLabel(true),
						TitledBorder.DEFAULT_JUSTIFICATION,
						TitledBorder.DEFAULT_POSITION, Const.FONT_BORDER_TEXT));

				currentPanel = daySummaryPanel;
				mainPanel.add(daySummaryPanel);
				daySummaryPanels.add(daySummaryPanel);
				firstRun = false;
			}
			
			JPanel eventPanel = new JPanel(new BorderLayout());
			JPanel eventButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
			JButton editButton = new JButton();
			editButton.setIcon(new ImageIcon(ClassLoader
					.getSystemResource("media/edit_event20.ico")));
			editButton.setMargin(new Insets(2, 2, 2, 2));
			editButton.addActionListener(this);
			if (e.isHoliday() || e.isSpecial()) {
				editButton.setEnabled(false);
				editButton.setToolTipText(Trans.t("Holidays can not be edited"));
			}
			else
				editButton.setToolTipText("\"" + e.getName() + "\" "+Trans.t("edit"));
			editButtons.add(editButton);
			
			JButton deleteButton = new JButton();
			deleteButton.setIcon(new ImageIcon(ClassLoader
					.getSystemResource("media/delete_event20.ico")));
			deleteButton.setMargin(new Insets(2, 2, 2, 2));
			deleteButton.addActionListener(this);
			if (e.isHoliday() || e.isSpecial()) {
				deleteButton.setEnabled(false);
				deleteButton.setToolTipText(Trans.t("Holidays can be removed only on the settings"));
			}
			else
				deleteButton.setToolTipText("\"" + e.getName() + "\" "+Trans.t("Delete"));
			deleteButtons.add(deleteButton);
			
			JButton alarmButton = new JButton();
			alarmButton.setIcon(new ImageIcon(ClassLoader
					.getSystemResource("media/bell20.ico")));
			alarmButton.setMargin(new Insets(2, 2, 2, 2));
			alarmButton.addActionListener(this);
			alarmButton.setToolTipText(Trans.t("Reminder of")+" \"" + e.getName() + "\" "+Trans.t("call"));
			alarmButtons.add(alarmButton);
			
			eventButtonPanel.add(alarmButton);
			eventButtonPanel.add(editButton);
			eventButtonPanel.add(deleteButton);
			
			/*
			 * Determine the duration of this event
			 */
			long duration = -1;
			if (e.getEndDate() != null && dayDiff >= 0) {
				if (dayDiff != 0)
					duration = e.getEndDate().dayDiff(e.getDate()) + 1;
				else if (dayDiff == 0)
					duration = e.getEndDate().dayDiff(new Date());
			}
			
			JLabel nameLabel = new JLabel();
			JLabel durationLabel = new JLabel();
			JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
			String eventLabel = "";
			if (e.getDate().hasTime()) {
				if (dayDiff == 0) {
					eventLabel = e.getMinDiffLabel() + ": ";
					new RefreshTimeLabelTask(nameLabel, e).start();
				}
				else
					eventLabel = e.getDate().timeToString() + " - ";
			}
			
			eventLabel += e.getName();
			
			if (dayDiff == 0) {
				/* add duration statement */
				if (duration == 0)
					durationLabel.setText(" ("+Trans.t("last Day")+") ");
				else if (duration == 1)
					durationLabel.setText(" ("+Trans.t("goes up tomorrow")+") ");
				else if (duration == 2)
					durationLabel.setText(" ("+Trans.t("goes up tomorrow")+") ");
				else if (duration != -1)
					durationLabel.setText(" ("+Trans.t("goes")+" " + (duration + 1) + " Tage) ");
			}
			else if (dayDiff > 0 && duration != -1)
				durationLabel.setText(" ("+Trans.t("goes")+" " + duration + " "+Trans.t("Days")+") ");
			
			nameLabel.setText(eventLabel);
			nameLabel.setFont(Const.FONT_NOTI_TEXT);
			
			/*
			 * Add balloons for holidays
			 */
			if (e.isHoliday() || e.isSpecial())
				eventPanel.add(new JLabel(new ImageIcon(this.getClass().
						getClassLoader().getResource("media/balloons20.ico"))), BorderLayout.WEST);
			
			textPanel.add(nameLabel);
			textPanel.add(durationLabel);
			
			/*
			 * Add icon for attachment.
			 * Click on this icon will open attachment.
			 */
			JLabel attachmentIcon = e.getAttachmentIcon(caller.getWorkspace());
			if (attachmentIcon != null)
				textPanel.add(attachmentIcon);
			
			/*
			 * Add icon for notes.
			 * Click on this icon will open frame with text field.
			 */
			JLabel notesIcon = e.getNotesIcon(caller.getWorkspace(), caller.getGUI().getFrame());
			if (notesIcon != null)
				textPanel.add(notesIcon);
			
			/* Add some space */
			textPanel.add(new JLabel("            "));
			
			eventPanel.add(textPanel, BorderLayout.CENTER);
			eventPanel.add(eventButtonPanel, BorderLayout.EAST);
			currentPanel.add(eventPanel);
			eventPanels.add(eventPanel);
			
			previousDayDiff = dayDiff;
		}
		
		JScrollPane jsp = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(jsp, BorderLayout.CENTER);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				setVisible(false);
				dispose();
			}
		});
		
		doPack();
		setLocationRelativeTo(caller.getGUI().getFrame());
		setVisible(true);
	}
	
	/**
	 * Set the natural size of this frame. If height exceeds 500px,
	 * a scroll bar will appear.
	 */
	private void doPack() {
		pack();
		
		Dimension size = this.getSize();
		if (size.height > 500)
			this.setSize(size.width + 20, 500);
	}

	/**
	 * Launch a new notification summary.
	 * @param c - Parent calendar object
	 * @param events - Events to notify
	 */
	public TableOfNotifications(Calendar c, Vector<Event> events) {
		super(c.getGUI().getFrame(), Trans.t("upcoming events"));

		if (events == null)
			return;

		setIcon();

		this.events = Event.sortByDate(events, true);
		this.caller = c;

		arrangeDialog();
		
		if (c.getConfig().getPlayTheme())
			try {
				if (c.getConfig().getTheme() == null)
					Music.playTheme(Calendar.DEFAULT_THEME, true);
				else 
					Music.playTheme(c.getConfig().getTheme(), false);
			} catch (Exception ex) {
				Logger.error("error while trying to play music file: "+ex.toString());
			}

		this.setAlwaysOnTop(true);
	}

	@Override
	public void actionPerformed(ActionEvent a) {
		for (int i = 0; i < events.size(); i++) {
			
			/*
			 * Edit button clicked.
			 */
			if (a.getSource().equals(editButtons.get(i))) {
				this.setAlwaysOnTop(false);
				new EditEvent(caller, events.get(i));
				break;
			}
			
			/*
			 * Delete button clicked.
			 */
			else if (a.getSource().equals(deleteButtons.get(i))) {
				this.setAlwaysOnTop(false);
				if (caller.deleteEvent(events.get(i))) {
					
					/* remove the eventPanel from the right container */
					for (VerticalFlowPanel vfp : daySummaryPanels)
						if (vfp.remove2(eventPanels.get(i))) {
							if (vfp.getComponents().length == 0)
								mainPanel.remove2(vfp);
							break;
						}
					this.doPack();
				}
				this.setAlwaysOnTop(true);
				break;
			}
			
			/*
			 * Alarm button clicked.
			 */
			else if (a.getSource().equals(alarmButtons.get(i))) {
				new Notification(caller, events.get(i));
				break;
			}
		}
	}
}
