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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import de.jsteltze.calendar.Calendar;
import de.jsteltze.calendar.Date;
import de.jsteltze.calendar.Event;
import de.jsteltze.calendar.Frequency;
import de.jsteltze.calendar.Moon;
import de.jsteltze.calendar.UI.Cell;
import de.jsteltze.calendar.config.Configuration;
import de.jsteltze.calendar.config.Const;
import de.jsteltze.common.ImageButton;
import de.jsteltze.common.ImageButtonListener;
import de.jsteltze.common.Trans;

/**
 * Frame for analyzing a set of selected dates.
 * @author Johannes Steltzer
 *
 */
public class MultiDayAnalyzer 
	extends JDialog 
	implements ImageButtonListener, ActionListener {

	private static final long serialVersionUID = 1L;
	
	/** +/- buttons for extending/reducing events */
	private ImageButton extendHolidaysButton, extendSpecialDaysButton, extendEventsButton;
	
	/** true if events are extended, false if hidden */
	private boolean extendHolidays, extendSpecialDays, extendEvents;
	
	/** content panel, contains all events and listings */
	private JPanel content;
	
	/** button for creating a new event on the selected dates */
	private JButton newButton;
	
	/** selected dates to pass to calendar object */
	private Vector<Date> dates;
	
	/** parent calendar object */
	private Calendar cal;
	
	/** true for weekly selection */
	private boolean weekly;
	
	/** events, holidays and special days within the selection */
	private Vector<Event> events, holidays, specialDays;
	
	/** number of labor days/weekends/holidays ... */
	private int num_labordays, num_weekends, num_holidays, 
			num_moon_full, num_moon_half1, num_moon_half2, 
			num_moon_new, num_total;
	
	/**
	 * Construct a multi-day analyzer frame.
	 * @param cells - selected cells
	 * @param cal - Parent calendar object
	 * @param dates - selected dates
	 * @param weekly - true for weekly selection
	 */
	public MultiDayAnalyzer(Vector<Cell> cells, Calendar cal, 
			Vector<Date> dates, boolean weekly) {
		super(cal.getGUI().getFrame(), "Markierter Zeitraum", true);
		
		this.cal = cal;
		this.dates = dates;
		this.weekly = weekly;
		events = new Vector<Event>();
		holidays = new Vector<Event>();
		specialDays = new Vector<Event>();
		extendHolidays = extendSpecialDays = extendEvents = false;
		num_labordays = 0;
		num_weekends = 0;
		num_holidays = 0;
		num_moon_full = 0;
		num_moon_half1 = 0;
		num_moon_half2 = 0;
		num_moon_new = 0;
		num_total = 0;
		
		/*
		 * Analyze selected cells
		 */
		for (Cell c : cells) {
			if (c == null || c.getDate() == null)
				continue;
			
			if (c.isHoliday()) num_holidays++;
			else if (c.isWeekend()) num_weekends++;
			else num_labordays++;
			byte moon = Moon.getMoonPhase(c.getDate());
			if (moon == Moon.MOON_DEC_HALF) num_moon_half1++;
			else if (moon == Moon.MOON_INC_HALF) num_moon_half2++;
			else if (moon == Moon.MOON_FULL) num_moon_full++;
			else if (moon == Moon.MOON_NEW) num_moon_new++;
			
			/*
			 * Collect events and holidays
			 */
			Vector<Event> tmpEvents = c.getEvents();
			for (Event e : tmpEvents)
				if (e.isHoliday()) {
					Event tmp = e.clone();
					tmp.setDate(c.getDate());
					holidays.add(tmp);
				}
				else if (e.isSpecial()) {
					Event tmp = e.clone();
					tmp.setDate(c.getDate());
					specialDays.add(tmp);
				}
				else {
					boolean doAdd = true;
					for (Event etmp : events)
						if (etmp.getID() == e.getID() && 
								e.getFrequency() == Frequency.OCCUR_ONCE) {
							doAdd = false;
							break;
						}
					if (!doAdd)
						continue;
					Event tmp = e.clone();
					tmp.setDate(c.getDate());
					if (weekly)
						tmp.setEndDate(null);
					events.add(tmp);
				}

			num_total++;
		}
		
		events = Event.sortByDate(events, false);

		setLayout(new BorderLayout());
		
		/* buttons for extending/hiding events */
		extendHolidaysButton = new ImageButton("media/+.PNG", "media/-.PNG", true);
		extendSpecialDaysButton = new ImageButton("media/+.PNG", "media/-.PNG", true);
		extendEventsButton = new ImageButton("media/+.PNG", "media/-.PNG", true);
		extendHolidaysButton.addButtonListener(this);
		extendSpecialDaysButton.addButtonListener(this);
		extendEventsButton.addButtonListener(this);
		
		content = createPanel();
		
		newButton = new JButton(Trans.t("New Event"));
		newButton.setToolTipText(Trans.t("New event for the selected period of time to create"));
		newButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("media/new_event20.ico")));
		newButton.addActionListener(this);
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(newButton);
		
		/* construct header */
		JPanel headerPanel = new JPanel(new FlowLayout());
		JLabel headerLabel;
		
		/*
		 * Get first and last date
		 */
		Date first = dates.lastElement();
		Date last = dates.lastElement();
		for (Date d : dates)
			if (d.getTimeInMillis() < first.getTimeInMillis())
				first = d;
			else if (d.getTimeInMillis() > last.getTimeInMillis())
				last = d;
		if (weekly)
			headerLabel = new JLabel(Trans.t("all")+" " + Date.dayOfWeek2String(dates.firstElement().get(java.util.Calendar.DAY_OF_WEEK), false) + 
					"e " + 
					(cal.getConfig().getView() == Configuration.VIEW_MONTH ? "im " + Date.month2String(dates.firstElement().get(java.util.Calendar.MONTH), false) + " " : "") + 
					dates.firstElement().get(java.util.Calendar.YEAR));
		else
			headerLabel = new JLabel(first.dateToString(false) + " - " + last.dateToString(false));
		headerLabel.setFont(Const.FONT_MULTIDAY_HEADER);
		headerPanel.add(headerLabel);
		
		add(headerPanel, BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		doPack();
		this.setVisible(true);
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
		
		setLocationRelativeTo(cal.getGUI().getFrame());
	}
	
	/**
	 * Construct the main content panel, which contains the
	 * listings and events.
	 * @return content panel.
	 */
	private JPanel createPanel() {
		int rows = 5, index = 0;
		boolean somethingExtended = extendHolidays || 
				extendSpecialDays || extendEvents;
		if (extendHolidays)
			rows += num_holidays;
		if (extendSpecialDays)
			rows += specialDays.size();
		if (extendEvents)
			rows += events.size();
		JPanel properties = new JPanel(new BorderLayout());
		JPanel leftProps = new JPanel(new GridLayout(rows, 1));
		JPanel rightProps = new JPanel(new GridLayout(rows, 1));
		
		JPanel leftPanels[] = new JPanel[rows];
		JPanel rightPanels[] = new JPanel[rows];
		for (int i = 0; i < rows; i++) {
			leftPanels[i] = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, somethingExtended ? 0 : 2));
			rightPanels[i] = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, somethingExtended ? 0 : 2));
			leftProps.add(leftPanels[i]);
			rightProps.add(rightPanels[i]);
		}
		
		/* Headlines on the left site */
		leftPanels[index++].add(new JLabel(Trans.t("Weekend days")+": "));
		leftPanels[index++].add(new JLabel(Trans.t("National Holidays")+": "));
		if (extendHolidays)
			index += num_holidays;
		leftPanels[index++].add(new JLabel(Trans.t("Other Holidays")+": "));
		if (extendSpecialDays)
			index += specialDays.size();
		leftPanels[index++].add(new JLabel(Trans.t("working days")+": "));
		leftPanels[index++].add(new JLabel(Trans.t("events")+": "));
		if (extendEvents)
			index += events.size();

		/* Number of events on the right site */
		index = 0;
		rightPanels[index++].add(new JLabel("" + num_weekends + "                                   "));
		rightPanels[index++].add(new JLabel("" + num_holidays + " "));
		if (num_holidays > 0)
			rightPanels[index - 1].add(extendHolidaysButton);
		
		/* Get holiday events */
		if (extendHolidays) {
			for (Event e : holidays) {
				JLabel dateLabel = new JLabel();
				String text = e.getDate().dateToString(true);
				if (e.getEndDate() != null)
					text += "-" + e.getEndDate().dateToString(true);
				text += ": ";
				dateLabel.setText(text);
				dateLabel.setForeground(Color.gray);
				rightPanels[index].add(dateLabel);
				rightPanels[index].add(new JLabel(e.getName()));
				rightPanels[index].setBorder(new EtchedBorder());
				rightPanels[index++].setBackground(Color.white);
			}
		}
		
		rightPanels[index++].add(new JLabel("" + specialDays.size() + " "));
		if (specialDays.size() > 0)
			rightPanels[index - 1].add(extendSpecialDaysButton);
		
		/* Get special day events */
		if (extendSpecialDays) {
			for (Event e : specialDays) {
				JLabel dateLabel = new JLabel();
				String text = e.getDate().dateToString(true);
				if (e.getEndDate() != null)
					text += "-" + e.getEndDate().dateToString(true);
				text += ": ";
				dateLabel.setText(text);
				dateLabel.setForeground(Color.gray);
				rightPanels[index].add(dateLabel);
				rightPanels[index].add(new JLabel(e.getName()));
				rightPanels[index].setBorder(new EtchedBorder());
				rightPanels[index++].setBackground(Color.white);
			}
		}
		
		rightPanels[index++].add(new JLabel("" + num_labordays));
		rightPanels[index++].add(new JLabel("" + events.size() + " "));
		if (events.size() > 0)
			rightPanels[index - 1].add(extendEventsButton);
		
		/* Get user events */
		if (extendEvents) {
			for (Event e : events) {
				JLabel dateLabel = new JLabel();
				String text = e.getDate().dateToString(true);
				if (e.getEndDate() != null)
					text += "-" + e.getEndDate().dateToString(true);
				text += ": ";
				dateLabel.setText(text);
				dateLabel.setForeground(Color.gray);
				rightPanels[index].add(dateLabel);
				rightPanels[index].add(new JLabel(e.getName()));
				rightPanels[index].setBorder(new EtchedBorder());
				rightPanels[index++].setBackground(Color.white);
			}
		}
		
		properties.add(leftProps, BorderLayout.WEST);
		properties.add(rightProps, BorderLayout.CENTER);
		
		JScrollPane jsp = new JScrollPane(properties, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jsp.setBorder(new EmptyBorder(0, 0, 0, 0));
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(jsp);
		mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				num_total + (weekly ? 
						(" " + Date.dayOfWeek2String(dates.firstElement().get(java.util.Calendar.DAY_OF_WEEK), false) + "e") : 
						" "+Trans.t("consecutive days")),
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				Const.FONT_BORDER_TEXT));
		
		return mainPanel;
	}

	@Override
	public void buttonPressed(ImageButton x) {
		if (x.equals(extendHolidaysButton))
			extendHolidays = extendHolidaysButton.isPressed();
		else if (x.equals(extendSpecialDaysButton))
			extendSpecialDays = extendSpecialDaysButton.isPressed();
		else if (x.equals(extendEventsButton))
			extendEvents = extendEventsButton.isPressed();
		
		remove(content);
		content = null;
		content = createPanel();
		add(content, BorderLayout.CENTER);
		doPack();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (weekly)
			cal.newWeeklySelection(dates.firstElement(), dates.size() > 5);
		else
			cal.newSelection(dates, true);
		
		this.setVisible(false);
		this.dispose();
	}
}
