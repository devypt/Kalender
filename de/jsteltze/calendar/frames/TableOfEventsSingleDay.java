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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import de.jsteltze.calendar.Calendar;
import de.jsteltze.calendar.Event;
import de.jsteltze.calendar.Frequency;
import de.jsteltze.calendar.config.Const;
import de.jsteltze.common.ImageButton;
import de.jsteltze.common.ImageButtonListener;
import de.jsteltze.common.calendar.Date;
import de.jsteltze.common.calendar.Moon;

/**
 * Frame for controlling events on a specific date (or
 * all events).
 * @author Johannes Steltzer
 *
 */
public class TableOfEventsSingleDay 
    extends JDialog 
    implements ActionListener,
        KeyListener, ImageButtonListener, FocusListener, MouseListener {

    private static final long serialVersionUID = 1L;
    
    /** maximum number of events to be displayed without scroll bar */
    private static final int MAX_EVENTS_IN_FRAME = 25;
    
    /** column names for event table */
    private static final String COL_NAME = "Name", COL_DATE = "Datum",
            COL_FREQ = "Regelm‰ﬂigkeit", COL_TIME = "Uhrzeit",
            COL_HOLIDAY = "Feiertag", COL_ID = "ID";
    
    /** default text shown in the search text field */
    private static final String DEFAULT_FILTER_TEXT = "Name, Datum oder Uhrzeit";
    
    /** matched events to display */
    private Vector<Event> events, filteredEvents, holidays;
    
    /** parent object */
    private Calendar caller;
    
    /** date of interest */
    private Date date;
    
    /** control buttons */
    private JButton editButton, deleteButton, newButton, 
            duplicateButton, remindButton;
    
    /** currently selected event */
    private Event selectedEvent;
    
    /** search field for filtering events */
    private JTextField filterField;
    
    /** area to contain all matched events */
    private JPanel eventPanel;
    
    /** columns of the event table */
    private String [] columnNames;
    
    /** table model */
    private DefaultTableModel tableModel;
    
    /** event table */
    private JTable eventTable;
    
    /** show holidays or events? */
    private boolean holidaysOnly;
    
    private static Logger logger = Logger.getLogger(TableOfEventsSingleDay.class);

    /**
     * Arranges all elements in this dialog window.
     */
    private void arrangeDialog() {
        setLayout(new BorderLayout());
        
        /*
         * Add buttons
         */
        editButton = new JButton(caller.getConfig().getButtonTexts() ? "Bearbeiten" : "");
        duplicateButton = new JButton(caller.getConfig().getButtonTexts() ? "Duplizieren" : "");
        deleteButton = new JButton(caller.getConfig().getButtonTexts() ? "Lˆschen" : "");
        remindButton = new JButton(caller.getConfig().getButtonTexts() ? "Erinnern" : "");
        newButton = new JButton(caller.getConfig().getButtonTexts() ? "Neu" : "");
        editButton.setToolTipText("Das ausgew‰hlte Ereignis bearbeiten");
        duplicateButton.setToolTipText("Das ausgew‰hlte Ereignis duplizieren");
        deleteButton.setToolTipText("Das ausgew‰hlte Ereignis lˆschen");
        remindButton.setToolTipText("Den Wecker f¸r dieses Ereignis stellen");
        newButton.setToolTipText("Ein neues Ereignis f¸r diesen Tag erstellen");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(newButton);
        buttonPanel.add(editButton);
        buttonPanel.add(duplicateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(remindButton);

        editButton.addActionListener(this);
        duplicateButton.addActionListener(this);
        remindButton.addActionListener(this);
        deleteButton.addActionListener(this);
        newButton.addActionListener(this);
        deleteButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("media/event_delete20.ico")));
        editButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("media/event_edit20.ico")));
        duplicateButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("media/event_copy20.ico")));
        newButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("media/event_new20.ico")));
        remindButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("media/bell20.ico")));

        eventPanel = new JPanel(new BorderLayout());
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel northPanel = new JPanel(new BorderLayout());
        centerPanel.add(northPanel, BorderLayout.NORTH);
        eventPanel.add(buttonPanel, BorderLayout.SOUTH);
        centerPanel.add(eventPanel, BorderLayout.CENTER);
        
        if (date == null) {
            if ((holidays.size() > 0 && holidaysOnly) ||
                    (!holidaysOnly && events.size() > 0)) {
                /*
                 * Add filter text field and put event list into
                 * a scroll pane 
                 */
                JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JLabel filterLabel = new JLabel("Suche: ");
                JPanel filtercore = new JPanel(new BorderLayout());
                filterField = new JTextField(18);
                filterField.setForeground(Color.LIGHT_GRAY);
                filterField.setBorder(new EmptyBorder(0, 0, 0, 0));
                filterField.addKeyListener(this);
                filterField.addFocusListener(this);
                filterField.setText(DEFAULT_FILTER_TEXT);
                filterLabel.setForeground(Color.gray);
                filtercore.add(filterField, BorderLayout.CENTER);
                ImageButton searchButton = new ImageButton("media/lupe.png", "media/lupe.png", false);
                searchButton.addButtonListener(this);
                filtercore.add(searchButton, BorderLayout.EAST);
                filtercore.setBackground(Color.white);
                filtercore.setBorder(new EtchedBorder());
                filterPanel.add(filterLabel);
                filterPanel.add(filtercore);
                add(filterPanel, BorderLayout.NORTH);
            }
        }
        else {
            /*
             * No filter / scroll pane necessary
             */
            eventPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
                    date != null ? "Ereignisse" : "‹bersicht", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
                    Const.FONT_BORDER_TEXT));

            byte moon;
            if (caller.getConfig().getMoon() && 
                    (moon = Moon.getMoonPhase(date)) != Moon.MOON_NONE) {
                JPanel moonPanel = new JPanel(new GridLayout(1, 1));
                JLabel moonImage = null;
                String moonText = Moon.moonPhaseToString(moon);
                if (moon == Moon.MOON_DEC_HALF)
                    moonImage = new JLabel(new ImageIcon(this.getClass().getClassLoader().getResource("media/moon_half-desc.png")));
                else if (moon == Moon.MOON_FULL)
                    moonImage = new JLabel(new ImageIcon(this.getClass().getClassLoader().getResource("media/moon_full.png")));
                else if (moon == Moon.MOON_INC_HALF)
                    moonImage = new JLabel(new ImageIcon(this.getClass().getClassLoader().getResource("media/moon_half-asc.png")));
                else if (moon == Moon.MOON_NEW)
                    moonImage = new JLabel(new ImageIcon(this.getClass().getClassLoader().getResource("media/moon_new.png")));
                moonText += " in der Nacht des " + date.dateToString(false) + ".";
                moonImage.setToolTipText(moonText);
                moonPanel.add(moonImage);
                moonPanel.add(new JLabel("            "));
                moonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
                        "Mondphase", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
                        Const.FONT_BORDER_TEXT));
                
                northPanel.add(moonPanel, BorderLayout.EAST);
            }
            
            if (holidays.size() > 0) {
                JPanel holidayPanel = new JPanel(new GridLayout(holidays.size(), 1));
                for (Event e : holidays) {
                    JPanel holiday = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    holiday.add(new JLabel(new ImageIcon(this.getClass().getClassLoader().getResource("media/balloons20.ico"))));
                    holiday.add(new JLabel(e.getName()));
                    holidayPanel.add(holiday);
                }
                holidayPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
                        "Feiertage", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
                        Const.FONT_BORDER_TEXT));
                northPanel.add(holidayPanel, BorderLayout.CENTER);
            }
        }
        
        if (events.size() == 0) {
            JPanel noEvents = new JPanel();
            noEvents.add(new JLabel(holidaysOnly ? "keine Feiertage registriert" :
                    "noch keine Ereignisse geplant..."));
            eventPanel.add(noEvents, BorderLayout.NORTH);
            selectedEvent = null;
        } 
        else {
            /*
             * Add events
             */
            if (date == null && holidaysOnly)
                /* All holidays */
                columnNames = new String [] {COL_ID, COL_NAME, COL_DATE, COL_FREQ, COL_HOLIDAY};
            
            else if (date == null)
                /* All events */
                columnNames = new String [] {COL_ID, COL_NAME, COL_DATE, COL_FREQ, COL_TIME};
            
            else
                /* Events on a date */
                columnNames = new String [] {COL_ID, COL_NAME, COL_FREQ, COL_TIME};
            
            tableModel = new DefaultTableModel(getTableContent(events), columnNames);
            eventTable = new JTable(tableModel) {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isCellEditable(int row,int column){  
                    return false;  
                }
                
                @Override
                public Dimension getPreferredScrollableViewportSize() {
                    if (events.size() > MAX_EVENTS_IN_FRAME)
                        return new Dimension(500, 400);
                    else
                        return getPreferredSize();
                }
            };
            
            int colDate = getColumnByName(COL_DATE);
            int colID = getColumnByName(COL_ID);
            int colName = getColumnByName(COL_NAME);
            int colTime = getColumnByName(COL_TIME);
            if (colDate != -1) {
                TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>();
                eventTable.setRowSorter(rowSorter);
                rowSorter.setModel(tableModel);
                rowSorter.setComparator(colDate, new DateComparator());
            }
            
//            eventTable.setAutoCreateRowSorter(true);

            
            eventTable.setUpdateSelectionOnSort(true);
            eventTable.setAutoscrolls(true);
            eventTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
            eventTable.setDragEnabled(false);
            eventTable.setColumnSelectionAllowed(false);
            eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            eventTable.getColumnModel().getColumn(colID).setMinWidth(0);
            eventTable.getColumnModel().getColumn(colID).setMaxWidth(0);
            eventTable.getColumnModel().getColumn(colName).setPreferredWidth(200);
            if (colDate != -1)
                eventTable.getColumnModel().getColumn(colDate).setPreferredWidth(100);
            if (colTime != -1)
                eventTable.getColumnModel().getColumn(colTime).setPreferredWidth(30);
            eventTable.getTableHeader().setReorderingAllowed(false);
            eventTable.addMouseListener(this);
            
            eventPanel.add(new JScrollPane(eventTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        }
        
        add(centerPanel, BorderLayout.CENTER);

        if (selectedEvent == null) {
            editButton.setEnabled(false);
            duplicateButton.setEnabled(false);
            deleteButton.setEnabled(false);
            remindButton.setEnabled(false);
        } 
        else if (selectedEvent.isHoliday() || selectedEvent.isSpecial()) {
            editButton.setEnabled(false);
            duplicateButton.setEnabled(false);
        }
        
        addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                newButton.requestFocusInWindow();
            }
        });
        
        pack();
        
        setLocationRelativeTo(caller.getGUI().getFrame());
        setVisible(true);
    }
    
    /**
     * Construct a new table of events frame.
     * @param d - Date to survey (will show all events on this date)
     *         or null (will show all events)
     * @param c - Parent calendar object
     * @param holidays <li>If true and if d is null, all holidays are shown
     *         <li>If false and if d is null, all events are shown
     *         <li>Ignored if d is not null
     */
    public TableOfEventsSingleDay(Date d, Calendar c, boolean holidaysOnly) {
        super(c.getGUI().getFrame(), true);
        
        this.caller = c;
        this.date = d;
        this.holidaysOnly = holidaysOnly;

        /*
         * Query events of interest (pending on date)
         */
        events = new Vector<Event>();
        holidays = new Vector<Event>();
        filteredEvents = new Vector<Event>();
        Vector<Event> all = caller.getAllEvents();
        
        if (d == null) {
            if (holidaysOnly) {
                /*
                 * Get all holiday events
                 */
                for (Event e : all)
                    if (e.isHoliday() || e.isSpecial())
                        events.add(e);
            }
            else {
                /*
                 * Get all non-holiday events
                 */
                for (Event e : all)
                    if (!e.isHoliday() && !e.isSpecial())
                        events.add(e);
            }
        }
        else {
            if (!d.hasTime()) {
                /*
                 * Get all events on the date specified
                 */
                for (Event e : all)
                    if (e.match(d)) {
                        if (e.isHoliday() || e.isSpecial())
                            holidays.add(e);
                        else
                            events.add(e);
                    }
            } 
            else
                /*
                 * Get all events on the date and time specified
                 */
                for (Event e : all)
                    if (e.getDate().hasTime() && e.match(d)
                            && e.getDate().get(java.util.Calendar.HOUR_OF_DAY) == 
                            d.get(java.util.Calendar.HOUR_OF_DAY))
                        events.add(e);
        }
        
        /*
         * Build frame title
         */
        String title;
        if (d == null)
            title = "Alle " + (holidaysOnly ? "gesetzlichen und sonstigen Feiertage" : 
                "Ereignisse") + " (" + events.size() + ")";
        else {
            long dayDiff = d.dayDiff(new Date());
            if (dayDiff == 0)
                title = "Heute (" + d.dateToString(false) + ")";
            else if (dayDiff == 1)
                title = "Morgen (" + d.dateToString(false) + ")";
            else if (dayDiff == 2)
                title = "‹bermorgen (" + d.dateToString(false) + ")";
            else if (dayDiff == -1)
                title = "Gestern (" + d.dateToString(false) + ")";
            else
                title = d.dateToString(false);
            
            if (d.hasTime())
                title += ", " + d.get(java.util.Calendar.HOUR_OF_DAY)
                    + " bis " + (d.get(java.util.Calendar.HOUR_OF_DAY) + 1) + " Uhr";
        }
        this.setTitle(title);

        arrangeDialog();
    }
    
    /**
     * Returns the index of a column by the columns name.
     * @param colName - Name of the column
     * @return Index or -1 if column name not found.
     */
    private int getColumnByName(String colName) {
        for (int col = 0; col < columnNames.length; col++)
            if (columnNames[col].equals(colName))
                return col;
        return -1;
    }
    
    /**
     * Build the table data (rows and columns to be shown).
     * @param ev - Events to show
     * @return Table matrix.
     */
    private String [] [] getTableContent(Vector<Event> ev) {
        String [][] rows = new String[ev.size()][columnNames.length];
        int colDate = getColumnByName(COL_DATE);
        int colName = getColumnByName(COL_NAME);
        int colID = getColumnByName(COL_ID);
        int colTime = getColumnByName(COL_TIME);
        int colFreq = getColumnByName(COL_FREQ);
        int colHoliday = getColumnByName(COL_HOLIDAY);
        for (int i = 0; i < ev.size(); i++) {
            if (colDate != -1) {
                rows[i][colDate] = ev.elementAt(i).getDate().dateToString(true);
                if (ev.elementAt(i).getEndDate() != null)
                    rows[i][colDate] += "-" + ev.elementAt(i).getEndDate().dateToString(true);
            }
            if (colID != -1)
                rows[i][colID] = "" + ev.elementAt(i).getID();
            if (colName != -1)
                rows[i][colName] = ev.elementAt(i).getName();
            if (ev.elementAt(i).getDate().hasTime() &&
                    colTime != -1)
                rows[i][colTime] = ev.elementAt(i).getDate().timeToString();
            if (colFreq != -1)
                rows[i][colFreq] = Frequency.getLabel(ev.elementAt(i).getFrequency(), ev.elementAt(i).getDate());
            if (colHoliday != -1)
                rows[i][colHoliday] = ev.elementAt(i).isHoliday() ? "gesetzl." : "";
        }

        return rows;
    }
    
    /**
     * Filters the list of events shown with the pattern
     * in filterField.
     */
    private void filter() {
        String text = filterField.getText();
        logger.debug("filter: " + text);
        filteredEvents.removeAllElements();
        
        int count = tableModel.getRowCount();
        logger.debug("row count before filter: " + count);
        for (int i = 0; i < count; i++)
            tableModel.removeRow(0);
        
        /*
         * If text field is empty, reset filter (query all events)
         */
        if (text.equals("")) {
            for (Event e : events)
                filteredEvents.add(e);
            
            setTitle("Alle " + (holidaysOnly ? "gesetzlichen und sonstigen Feiertage" : 
                    "Ereignisse") + " (" + events.size() + ")");
        }
        
        /*
         * Query events which contain the filter pattern
         */
        else {
            for (Event e : events)
                if (e.getName().contains(text) || 
                        e.getDate().dateToString(false).contains(text) || 
                        e.getDate().timeToString().contains(text))
                    filteredEvents.add(e);

            setTitle((holidaysOnly ? "Feiertage" : "Ereignisse") + " mit \"" +
                    text + "\" (" + filteredEvents.size() + ")");
        }
        
        String [][] content = getTableContent(filteredEvents);
        for (String [] row : content)
            tableModel.addRow(row);
    }
    
    @Override
    public void actionPerformed(ActionEvent a) {
        
        if (caller.getGUI().getApplet() != null) {
            caller.getGUI().getApplet().newSelection();
            this.setVisible(false);
            this.dispose();
            return;
        }
        
        /*
         * Edit event button 
         */
        if (a.getSource().equals(editButton))
            new EditEvent(caller, selectedEvent, false);
        
        /*
         * Duplicate event button 
         */
        if (a.getSource().equals(duplicateButton))
            new EditEvent(caller, selectedEvent, true);

        /* 
         * New event button 
         */
        else if (a.getSource().equals(newButton)) {
            if (holidaysOnly)
                new Settings(caller, Settings.TAB_HOLIDAYS);
            else
                new EditEvent(caller, date == null ? new Date() : date);
        }

        /* 
         * Delete event button 
         */
        else if (a.getSource().equals(deleteButton)) {
            if (selectedEvent.isHoliday() || selectedEvent.isSpecial()) {
                new Settings(caller, Settings.TAB_HOLIDAYS);
                this.setVisible(false);
                this.dispose();
            }

            if (caller.deleteEvent(selectedEvent)) {
                tableModel.removeRow(eventTable.getSelectedRow());
                editButton.setEnabled(false);
                duplicateButton.setEnabled(false);
                deleteButton.setEnabled(false);
                remindButton.setEnabled(false);
            }
            
            return;
        }

        /* 
         * Notify button 
         */
        else if (a.getSource().equals(remindButton))
            new Notification(caller, selectedEvent);

        /*
         * Any case: close this frame
         */
        setVisible(false);
        dispose();
    }

    @Override
    public void keyPressed(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_ENTER)
             filter();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void buttonPressed(ImageButton x) {
        filter();
    }

    @Override
    public void focusGained(FocusEvent arg0) {
        if (arg0.getSource().equals(filterField) && 
                filterField.getText().equals(DEFAULT_FILTER_TEXT)) {
            filterField.setText("");
            filterField.setForeground(Color.black);
        }
    }

    @Override
    public void focusLost(FocusEvent arg0) {
        if (arg0.getSource().equals(filterField) && 
                filterField.getText().equals("")) {
            filterField.setText(DEFAULT_FILTER_TEXT);
            filterField.setForeground(Color.LIGHT_GRAY);
        }
    }
    
    /**
     * Determines the selected event of the table and
     * stores it in 'selectedEvent'.
     * Configures buttons accordingly (disable edit button
     * for holidays ...)
     */
    private void getSelectedEvent() {
        int idIndex = 0, nameIndex = 0;
        for (int i = 0; i < columnNames.length; i++)
            if (columnNames[i].equals(COL_ID))
                idIndex = i;
            else if (columnNames[i].equals(COL_NAME))
                nameIndex = i;
        
        String id = (String) eventTable.getValueAt(eventTable.getSelectedRow(), idIndex);
        String name = (String) eventTable.getValueAt(eventTable.getSelectedRow(), nameIndex);
        logger.debug("selected event: id="+id+ " name="+name);
        
        selectedEvent = caller.getEventByIDAndName(Integer.parseInt(id), name);
        if (selectedEvent == null) {
            editButton.setEnabled(false);
            duplicateButton.setEnabled(false);
            deleteButton.setEnabled(false);
            remindButton.setEnabled(false);
            return;
        }
        
        if (selectedEvent.isHoliday() || selectedEvent.isSpecial()) {
            editButton.setEnabled(false);
            duplicateButton.setEnabled(false);
        }
        else {
            editButton.setEnabled(true);
            duplicateButton.setEnabled(true);
        }
        
        deleteButton.setEnabled(true);
        remindButton.setEnabled(true);
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        if (arg0.getSource().equals(eventTable)) {
            getSelectedEvent();
            if (selectedEvent != null && arg0.getClickCount() == 2) {
                new Notification(caller, selectedEvent);
                this.setVisible(false);
                this.dispose();
                return;
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        getSelectedEvent();
    }
}

/**
 * Class for comparing two dates (logically and not by abc).
 * @author Johannes Steltzer
 *
 */
class DateComparator 
    implements Comparator<String>
{
    @Override
    public int compare(String d1, String d2) {
        long date1 = new Date(d1).getTimeInMillis();
        long date2 = new Date(d2).getTimeInMillis();
        
        if (date1 < date2)
            return -1;
        else if (date1 > date2)
            return 1;
        else
            return 0;
    }
}