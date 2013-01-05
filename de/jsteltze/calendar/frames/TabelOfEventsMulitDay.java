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

import org.apache.log4j.Logger;

import de.jsteltze.calendar.Calendar;
import de.jsteltze.calendar.Event;
import de.jsteltze.calendar.Frequency;
import de.jsteltze.calendar.UI.Cell;
import de.jsteltze.calendar.config.ColorSet;
import de.jsteltze.calendar.config.Configuration;
import de.jsteltze.calendar.config.Const;
import de.jsteltze.common.ImageButton;
import de.jsteltze.common.ImageButtonListener;
import de.jsteltze.common.SelectablePanel;
import de.jsteltze.common.SelectablePanelGroup;
import de.jsteltze.common.SelectablePanelListener;
import de.jsteltze.common.calendar.Date;
import de.jsteltze.common.calendar.Moon;

/**
 * Frame for analyzing a set of selected dates.
 * @author Johannes Steltzer
 *
 */
public class TabelOfEventsMulitDay 
    extends JDialog 
    implements ImageButtonListener, ActionListener, SelectablePanelListener {

    private static final long serialVersionUID = 1L;
    
    /** +/- buttons for extending/reducing events */
    private ImageButton extendHolidaysButton, extendSpecialDaysButton, extendEventsButton;
    
    /** true if events are extended, false if hidden */
    private boolean extendHolidays, extendSpecialDays, extendEvents;
    
    /** content panel, contains all events and listings */
    private JPanel content;
    
    /** buttons for event options */
    private JButton newButton, editButton, deleteButton, 
            remindButton, duplicateButton;
    
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
    
    /** Group of selectable panels. */
    private SelectablePanelGroup spg;
    
    /** All selectable panels. */
    private SelectablePanel eventPanels[];
    
    /** Currently selected event. */
    private Event selectedEvent;
    
    private static Logger logger = Logger.getLogger(TabelOfEventsMulitDay.class);
    
    /**
     * Construct a multi-day analyzer frame.
     * @param cells - selected cells
     * @param cal - Parent calendar object
     * @param dates - selected dates
     * @param weekly - true for weekly selection
     */
    public TabelOfEventsMulitDay(Vector<Cell> cells, Calendar cal, 
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
            
            if (c.isHoliday()) 
                num_holidays++;
            else if (c.isWeekend()) 
                num_weekends++;
            else 
                num_labordays++;
            
            byte moon = Moon.getMoonPhase(c.getDate());
            if (moon == Moon.MOON_DEC_HALF) 
                num_moon_half1++;
            else if (moon == Moon.MOON_INC_HALF) 
                num_moon_half2++;
            else if (moon == Moon.MOON_FULL) 
                num_moon_full++;
            else if (moon == Moon.MOON_NEW) 
                num_moon_new++;
            
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
        
        newButton = new JButton(cal.getConfig().getButtonTexts() ? "Neu" : "");
        newButton.setToolTipText("Neues Ereignis für den markierten Zeitraum erstellen");
        newButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("media/event_new20.ico")));
        newButton.addActionListener(this);
        editButton = new JButton(cal.getConfig().getButtonTexts() ? "Bearbeiten" : "");
        editButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("media/event_edit20.ico")));
        editButton.addActionListener(this);
        editButton.setEnabled(false);
        duplicateButton = new JButton(cal.getConfig().getButtonTexts() ? "Duplizieren" : "");
        duplicateButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("media/event_copy20.ico")));
        duplicateButton.addActionListener(this);
        duplicateButton.setEnabled(false);
        deleteButton = new JButton(cal.getConfig().getButtonTexts() ? "Löschen" : "");
        deleteButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("media/event_delete20.ico")));
        deleteButton.addActionListener(this);
        deleteButton.setEnabled(false);
        remindButton = new JButton(cal.getConfig().getButtonTexts() ? "Erinnern" : "");
        remindButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("media/bell20.ico")));
        remindButton.addActionListener(this);
        remindButton.setEnabled(false);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(newButton);
        buttonPanel.add(editButton);
        buttonPanel.add(duplicateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(remindButton);
        
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
            headerLabel = new JLabel("Alle " + Date.dayOfWeek2String(dates.firstElement().get(java.util.Calendar.DAY_OF_WEEK), false) + 
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
            this.setSize(size.width + 20, 501);
        
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
        eventPanels = new SelectablePanel[rows];
        spg = new SelectablePanelGroup(this, cal.getConfig().getColors()[ColorSet.SELECTED]);
        
        for (int i = 0; i < rows; i++) {
            leftPanels[i] = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, somethingExtended ? 0 : 2));
            eventPanels[i] = new SelectablePanel(new FlowLayout(FlowLayout.LEFT, 2, somethingExtended ? 0 : 2));
            leftProps.add(leftPanels[i]);
            rightProps.add(eventPanels[i]);
        }
        
        /* Headlines on the left site */
        leftPanels[index++].add(new JLabel("Wochenendtage: "));
        leftPanels[index++].add(new JLabel("gesetzliche Feiertage: "));
        if (extendHolidays)
            index += num_holidays;
        leftPanels[index++].add(new JLabel("sonstige Feiertage: "));
        if (extendSpecialDays)
            index += specialDays.size();
        leftPanels[index++].add(new JLabel("Arbeitstage: "));
        leftPanels[index++].add(new JLabel("Ereignisse: "));
        if (extendEvents)
            index += events.size();

        /* Number of events on the right site */
        index = 0;
        eventPanels[index++].add(new JLabel("" + num_weekends + "                                   "));
        eventPanels[index++].add(new JLabel("" + num_holidays + " "));
        if (num_holidays > 0)
            eventPanels[index - 1].add(extendHolidaysButton);
        
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
                spg.add(eventPanels[index]);
                eventPanels[index].add(dateLabel);
                eventPanels[index].add(new JLabel(e.getName()));
                eventPanels[index].setBorder(new EtchedBorder());
                eventPanels[index].addMouseMotionListener(eventPanels[index]);
                eventPanels[index].addMouseListener(eventPanels[index]);
                eventPanels[index].setGroup(spg);
                eventPanels[index++].setBackground(Color.white);
            }
        }
        
        eventPanels[index++].add(new JLabel("" + specialDays.size() + " "));
        if (specialDays.size() > 0)
            eventPanels[index - 1].add(extendSpecialDaysButton);
        
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
                spg.add(eventPanels[index]);
                eventPanels[index].add(dateLabel);
                eventPanels[index].add(new JLabel(e.getName()));
                eventPanels[index].setBorder(new EtchedBorder());
                eventPanels[index].addMouseMotionListener(eventPanels[index]);
                eventPanels[index].addMouseListener(eventPanels[index]);
                eventPanels[index].setGroup(spg);
                eventPanels[index++].setBackground(Color.white);
            }
        }
        
        eventPanels[index++].add(new JLabel("" + num_labordays));
        eventPanels[index++].add(new JLabel("" + events.size() + " "));
        if (events.size() > 0)
            eventPanels[index - 1].add(extendEventsButton);
        
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
                spg.add(eventPanels[index]);
                eventPanels[index].add(dateLabel);
                eventPanels[index].add(new JLabel(e.getName()));
                eventPanels[index].setBorder(new EtchedBorder());
                eventPanels[index].addMouseMotionListener(eventPanels[index]);
                eventPanels[index].addMouseListener(eventPanels[index]);
                eventPanels[index].setGroup(spg);
                eventPanels[index++].setBackground(Color.white);
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
                        " zusammenhängende Tage"),
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
        
        /*
         * New event button
         */
        if (arg0.getSource().equals(newButton)) {
            if (weekly)
                cal.newWeeklySelection(dates.firstElement(), dates.size() > 5);
            else
                cal.newSelection(dates, true);
        }
        
        /*
         * Edit event button
         */
        else if (arg0.getSource().equals(editButton))
            new EditEvent(cal, selectedEvent, false);
        
        /*
         * Duplicate event button
         */
        else if (arg0.getSource().equals(duplicateButton))
            new EditEvent(cal, selectedEvent, true);
        
        /*
         * Delete event button
         */
        else if (arg0.getSource().equals(deleteButton)) {
            if (selectedEvent.isHoliday() || selectedEvent.isSpecial())
                new Settings(cal, Settings.TAB_HOLIDAYS);
            else
                cal.deleteEvent(selectedEvent);
        }
        
        /*
         * Remind button
         */
        else if (arg0.getSource().equals(remindButton))
            new Notification(cal, selectedEvent);
        
        this.setVisible(false);
        this.dispose();
    }
    
    /**
     * Returns the event displayed in a selectable panel.
     * @param sp - Selectable panel to get event from
     * @return Event
     */
    private Event getSelectedEvent(SelectablePanel sp) {
        int plain_index = eventPanels.length;
        for (int i = 0; i < eventPanels.length; i++)
            if (sp.equals(eventPanels[i])) {
                plain_index = i;
                break;
            }
        logger.debug("getSelectedEvent: plain_index=" + plain_index);
        if (plain_index == eventPanels.length)
            return null;
        
        Event selected;
        int start_index_holidays = 2;
        int start_index_special = start_index_holidays + 1;
        if (extendHolidays)
            start_index_special += holidays.size();
        int start_index_events = start_index_special + 2;
        if (extendSpecialDays)
            start_index_events += specialDays.size();
        
        if (plain_index < start_index_special)
            selected = holidays.get(plain_index - start_index_holidays);
        else if (plain_index < start_index_events)
            selected = specialDays.get(plain_index - start_index_special);
        else
            selected = events.get(plain_index - start_index_events);
        
        return selected.getID() == -1 ? selected : 
            cal.getEventByID(selected.getID());
    }
    
    @Override
    public void panelSelected(SelectablePanel source) {
        selectedEvent = getSelectedEvent(source);
        if (selectedEvent.isHoliday() || selectedEvent.isSpecial()) {
            editButton.setEnabled(false);
            duplicateButton.setEnabled(false);
        }
        else {
            editButton.setEnabled(true);
            duplicateButton.setEnabled(true);
        }
        editButton.setToolTipText("Ereignis \"" + selectedEvent.getName() + "\" bearbeiten");
        duplicateButton.setToolTipText("Ereignis \"" + selectedEvent.getName() + "\" duplizieren");
        deleteButton.setEnabled(true);
        deleteButton.setToolTipText("Ereignis \"" + selectedEvent.getName() + "\" löschen");
        remindButton.setEnabled(true);
        remindButton.setToolTipText("Erinnerung für \"" + selectedEvent.getName() + "\" aufrufen");
    }
}
