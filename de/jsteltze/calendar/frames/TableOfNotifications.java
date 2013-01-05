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
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import de.jsteltze.calendar.Calendar;
import de.jsteltze.calendar.Event;
import de.jsteltze.calendar.config.ColorSet;
import de.jsteltze.calendar.config.Const;
import de.jsteltze.calendar.tasks.RefreshTimeLabelTask;
import de.jsteltze.common.Music;
import de.jsteltze.common.SelectablePanel;
import de.jsteltze.common.SelectablePanelGroup;
import de.jsteltze.common.SelectablePanelListener;
import de.jsteltze.common.VerticalFlowPanel;
import de.jsteltze.common.calendar.Date;

/**
 * Notification summary frame.
 * @author Johannes Steltzer
 *
 */
public class TableOfNotifications 
    extends JDialog 
    implements ActionListener, SelectablePanelListener {
    
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
    private Vector<SelectablePanel> eventPanels;
    
    /**
     * Main component that contains all
     * daySummaryPanels (one per day).
     */
    private VerticalFlowPanel mainPanel;
    
    /** buttons for event options */
    private  JButton deleteButton;
    private  JButton editButton;
    private  JButton alarmButton;
    
    /** events to notify of */
    private Vector<Event> events;
    
    /** parent calendar object */
    private Calendar caller;
    
    private SelectablePanelGroup spg;
    
    private Event selectedEvent = null;
    private SelectablePanel selectedPanel = null;
    
    private Vector<RefreshTimeLabelTask> refresherTasks;
   
    private static Logger logger = Logger.getLogger(TableOfNotifications.class);

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
        
        eventPanels = new Vector<SelectablePanel>();
        daySummaryPanels = new Vector<VerticalFlowPanel>();
        spg = new SelectablePanelGroup(this, 
                caller.getConfig().getColors()[ColorSet.SELECTED], new EtchedBorder());
        
        boolean firstRun = true;
        long previousDayDiff = -1;
        VerticalFlowPanel currentPanel = null;
        
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
            SelectablePanel textPanel = new SelectablePanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            
            String eventLabel = "";
            if (e.getDate().hasTime()) {
                if (dayDiff == 0) {
                    eventLabel = e.getMinDiffLabel() + ": ";
                    RefreshTimeLabelTask refreshTask = new RefreshTimeLabelTask(nameLabel, e);
                    refreshTask.start();
                    refresherTasks.add(refreshTask);
                }
                else
                    eventLabel = e.getDate().timeToString() + " - ";
            }
            
            eventLabel += e.getName();
            
            if (dayDiff == 0) {
                /* add duration statement */
                if (duration == 0)
                    durationLabel.setText(" (letzter Tag) ");
                else if (duration == 1)
                    durationLabel.setText(" (geht noch bis morgen) ");
                else if (duration == 2)
                    durationLabel.setText(" (geht noch bis übermorgen) ");
                else if (duration != -1)
                    durationLabel.setText(" (geht noch " + (duration + 1) + " Tage) ");
            }
            else if (dayDiff > 0 && duration != -1)
                durationLabel.setText(" (geht " + duration + " Tage) ");
            
            nameLabel.setText(eventLabel);
            nameLabel.setFont(Const.FONT_NOTI_TEXT);
            
            /*
             * Add balloons for holidays
             */
            if (e.isHoliday() || e.isSpecial()) {
                textPanel.add(new JLabel(new ImageIcon(this.getClass().
                        getClassLoader().getResource("media/balloons20.ico"))), BorderLayout.WEST);
                textPanel.add(new JLabel(" "));
            }
            
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
            textPanel.addMouseListener(textPanel);
            textPanel.addMouseMotionListener(textPanel);
            textPanel.setGroup(spg);
            spg.add(textPanel);
            
            currentPanel.add(textPanel);
            eventPanels.add(textPanel);
            
            previousDayDiff = dayDiff;
        }
        
        JScrollPane jsp = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        JPanel buttonPanel = new JPanel();
        editButton = new JButton(caller.getConfig().getButtonTexts() ? "Bearbeiten" : "");
        editButton.setIcon(new ImageIcon(ClassLoader
                .getSystemResource("media/event_edit20.ico")));
        editButton.addActionListener(this);
        editButton.setEnabled(false);
        deleteButton = new JButton(caller.getConfig().getButtonTexts() ? "Löschen" : "");
        deleteButton.setIcon(new ImageIcon(ClassLoader
                .getSystemResource("media/event_delete20.ico")));
        deleteButton.addActionListener(this);
        deleteButton.setEnabled(false);
        alarmButton = new JButton(caller.getConfig().getButtonTexts() ? "Erinnern" : "");
        alarmButton.setIcon(new ImageIcon(ClassLoader
                .getSystemResource("media/bell20.ico")));
        alarmButton.addActionListener(this);
        alarmButton.setEnabled(false);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(alarmButton);
        
        add(jsp, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
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
        else
            this.setSize(size.width, size.height + 8);
    }

    /**
     * Launch a new notification summary.
     * @param c - Parent calendar object
     * @param events - Events to notify
     */
    public TableOfNotifications(Calendar c, Vector<Event> events) {
        super(c.getGUI().getFrame(), "Anstehende Termine");

        if (events == null)
            return;

        setIcon();

        this.events = Event.sortByDate(events, true);
        this.caller = c;
        this.refresherTasks = new Vector<RefreshTimeLabelTask>();

        arrangeDialog();
        
        if (c.getConfig().getPlayTheme())
            try {
                if (c.getConfig().getTheme() == null)
                    Music.playTheme(Const.DEFAULT_THEME, true);
                else 
                    Music.playTheme(c.getConfig().getTheme(), false);
            } catch (Exception ex) {
                logger.error("error while trying to play music file...", ex);
            }
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                for (RefreshTimeLabelTask rtlt : refresherTasks)
                    rtlt.quit();
            }
        });

        this.setAlwaysOnTop(true);
    }

    @Override
    public void actionPerformed(ActionEvent a) {
        if (selectedEvent == null || selectedPanel == null)
            return;
        
        /*
         * Edit button clicked.
         */
        if (a.getSource().equals(editButton)) {
            this.setAlwaysOnTop(false);
            new EditEvent(caller, selectedEvent, false);
        }
        
        /*
         * Delete button clicked.
         */
        else if (a.getSource().equals(deleteButton)) {
            this.setAlwaysOnTop(false);
            if (caller.deleteEvent(selectedEvent)) {

                /* remove the eventPanel from the right container */
                for (VerticalFlowPanel vfp : daySummaryPanels)
                    if (vfp.removeComponent(selectedPanel)) {
                        if (vfp.getComponents().length == 0)
                            mainPanel.removeComponent(vfp);
                        disableButtons();
                        pack();
                        break;
                    }
                
                /* close if no more events to show */
                if (mainPanel.getComponents().length == 0) {
                    this.setVisible(false);
                    this.dispose();
                }
                
                this.doPack();
            }
            this.setAlwaysOnTop(true);
        }
        
        /*
         * Alarm button clicked.
         */
        else if (a.getSource().equals(alarmButton))
            new Notification(caller, selectedEvent);
    }
    
    /**
     * Disables the 3 buttons in case no event is selected.
     */
    private void disableButtons() {
        editButton.setEnabled(false);
        editButton.setToolTipText("");
        deleteButton.setEnabled(false);
        deleteButton.setToolTipText("");
        alarmButton.setEnabled(false);
        alarmButton.setToolTipText("");
    }
    
    /**
     * Returns the event displayed in a selectable panel.
     * @param sp - Selectable panel to get event from
     * @return Event
     */
    private Event getSelectedEvent(SelectablePanel sp) {
        for (int i = 0; i < eventPanels.size(); i++)
            if (eventPanels.get(i).equals(sp))
                return events.get(i);
        return null;
    }

    @Override
    public void panelSelected(SelectablePanel sp) {
        selectedEvent = getSelectedEvent(sp);
        selectedPanel = sp;
        if (selectedEvent == null) {
            disableButtons();
            return;
        }
        
        if (selectedEvent.isHoliday() || selectedEvent.isSpecial()) {
            editButton.setEnabled(false);
            editButton.setToolTipText("");
            deleteButton.setEnabled(false);
            deleteButton.setToolTipText("");
        } 
        else {
            editButton.setEnabled(true);
            editButton.setToolTipText("Ereignis \"" + selectedEvent.getName() + "\" bearbeiten");
            deleteButton.setEnabled(true);
            deleteButton.setToolTipText("Ereignis \"" + selectedEvent.getName() + "\" löschen");
        }
        
        alarmButton.setEnabled(true);
        alarmButton.setToolTipText("Erinnerung für \"" + selectedEvent.getName() + "\" aufrufen");
    }
}
