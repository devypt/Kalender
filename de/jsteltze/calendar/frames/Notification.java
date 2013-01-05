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
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import de.jsteltze.calendar.Calendar;
import de.jsteltze.calendar.Event;
import de.jsteltze.calendar.config.ColorSet;
import de.jsteltze.calendar.config.Const;
import de.jsteltze.calendar.tasks.AlarmTask;
import de.jsteltze.calendar.tasks.RefreshTimeLabelTask;
import de.jsteltze.common.Music;
import de.jsteltze.common.calendar.Date;

/**
 * Notification frame.
 * @author Johannes Steltzer
 *
 */
public class Notification 
    extends Dialog 
    implements ActionListener, ItemListener {
    
    private static final long serialVersionUID = 1L;
    
    /** buttons for alarm options */
    private JButton buttonAgain, buttonMute; 
    
    /** buttons for event options */
    private JButton deleteButton, editButton;
    
    /** time boxes */
    private JComboBox hourBox, minuteBox;
    
    /** label showing the time difference to the event */
    private JLabel timeLabel, label1, label2;
    
    /** the event is today and has a time before now */
    private boolean beforeBegin;
    
    /** event to notify of */
    private Event event;
    
    /** parent calendar object */
    private Calendar caller;
    
    /** thread for refreshing timeLabel each minute */
    private RefreshTimeLabelTask refresher = null;
    
    private static Logger logger = Logger.getLogger(Notification.class);

    /**
     * Sets the icon of this window.
     */
    private void setIcon() {
        URL url = this.getClass().getClassLoader().getResource("media/bell30.ico");
        Image ima = Toolkit.getDefaultToolkit().createImage(url);
        setIconImage(ima);
    }

    /**
     * Arranges all elements in this dialog window.
     */
    private void arrangeDialog() {
        setLayout(new BorderLayout());
        setBackground(caller.getConfig().getColors()[ColorSet.NOTI]);

        JPanel main = new JPanel();
        JPanel eventOptions = new JPanel();
        JPanel alarmOptions = new JPanel(new BorderLayout());
        JPanel alarmOptionsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JPanel alarmOptionsRight = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        /*
         * Main area: constructing timeLabel and eventLabel
         */
        String upperString = "", lowerString = "", durationString = "";
        Date thisDate = event.getNextDate();
        logger.info("NOTIFICATION for event: "
                + thisDate.dateToString(false) + "  "
                + thisDate.timeToString());
        long dayDiff = thisDate.dayDiff(new Date());
        long duration = -1;
        
        /*
         * Determine the duration of this event
         */
        if (event.getEndDate() != null && dayDiff >= 0) {
            if (dayDiff != 0)
                duration = event.getEndDate().dayDiff(event.getDate()) + 1;
            else if (dayDiff == 0)
                duration = event.getEndDate().dayDiff(new Date());
        }
        
        if (dayDiff == 0) {
            if (!thisDate.hasTime())
                upperString += "Heute";
            else {
                upperString += event.getMinDiffLabel();
                
                refresher = new RefreshTimeLabelTask(this, duration == -1);
                refresher.start();
            }
            
            /* add duration statement */
            if (duration == 0)
                durationString += " (letzter Tag):";
            else if (duration == 1)
                durationString += " (geht noch bis morgen):";
            else if (duration == 2)
                durationString += " (geht noch bis �bermorgen):";
            else if (duration != -1)
                durationString += " (geht noch " + (duration + 1) + " Tage):";

        } 
        else 
            upperString += event.getDayDiffLabel(true);
        
        /* add duration statement */
        if (dayDiff > 0 && duration != -1)
            durationString += " (geht " + duration + " Tage):";
        else if (duration == -1)
            upperString += ":";
        
        lowerString += event.getName();
        if (thisDate.hasTime())
            lowerString += " (" + thisDate.timeToString() + " Uhr)";
        timeLabel = new JLabel(upperString);
        JLabel duratioLabel = new JLabel(durationString);
        JLabel eventLabel = new JLabel(lowerString);
        timeLabel.setFont(Const.FONT_NOTI_TEXT);
        duratioLabel.setFont(Const.FONT_NOTI_TEXT);
        eventLabel.setFont(Const.FONT_NOTI_TEXT);

        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        upperPanel.setBackground(caller.getConfig().getColors()[ColorSet.NOTI]);
        upperPanel.add(timeLabel);
        upperPanel.add(duratioLabel);

        JPanel lowerPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        lowerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        lowerPanel.setBackground(caller.getConfig().getColors()[ColorSet.NOTI]);
        
        /*
         * Add balloons for holidays
         */
        if (event.isHoliday() || event.isSpecial())
            lowerPanel.add(new JLabel(new ImageIcon(
                    this.getClass().getClassLoader().getResource("media/balloons20.ico"))));

        lowerPanel.add(eventLabel);
        
        /*
         * Add icon for attachment.
         * Click on this icon will open attachment.
         */
        JLabel attachmentIcon = event.getAttachmentIcon(caller.getWorkspace());
        if (attachmentIcon != null)
            lowerPanel.add(attachmentIcon);
        
        /*
         * Add icon for notes.
         * Click on this icon will open frame with text field.
         */
        JLabel notesIcon = event.getNotesIcon(caller.getWorkspace(), caller.getGUI().getFrame());
        if (notesIcon != null)
            lowerPanel.add(notesIcon);

        main.setLayout(new BorderLayout());
        main.setBackground(caller.getConfig().getColors()[ColorSet.NOTI]);
        main.add(upperPanel, BorderLayout.NORTH);
        main.add(lowerPanel, BorderLayout.CENTER);

        /*
         * Event options
         */
        eventOptions.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), caller.getConfig().getButtonTexts() ? "Ereignis-Optionen" : "",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, Const.FONT_BORDER_TEXT));
        eventOptions.setLayout(new BorderLayout());
        eventOptions.setBackground(caller.getConfig().getColors()[ColorSet.NOTI]);
        editButton = new JButton(caller.getConfig().getButtonTexts() ? "Bearbeiten" : "");
        deleteButton = new JButton(caller.getConfig().getButtonTexts() ? "L�schen" : "");
        editButton.setIcon(new ImageIcon(ClassLoader
                .getSystemResource("media/event_edit20.ico")));
        editButton.setToolTipText("Ereignis bearbeiten");
        deleteButton.setIcon(new ImageIcon(ClassLoader
                .getSystemResource("media/event_delete20.ico")));
        deleteButton.setToolTipText("Ereignis l�schen");
        editButton.addActionListener(this);
        //editButton.setBackground(caller.getConfig().getColors()[ColorSet.NOTI]);
        deleteButton.addActionListener(this);
        //deleteButton.setBackground(caller.getConfig().getColors()[ColorSet.NOTI]);
        if (event.isHoliday() || event.isSpecial())
            editButton.setEnabled(false);
        eventOptions.add(editButton, BorderLayout.NORTH);
        eventOptions.add(deleteButton, BorderLayout.SOUTH);

        /*
         * Alarm options
         */
        alarmOptions.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Wecker-Optionen",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, Const.FONT_BORDER_TEXT));
        buttonMute = new JButton(caller.getConfig().getButtonTexts() ? "Ich denke dran" : "");
        buttonAgain = new JButton(caller.getConfig().getButtonTexts() ? "Erneut klingeln" : "");
        buttonMute.setIcon(new ImageIcon(ClassLoader
                .getSystemResource("media/mute20.ico")));
        buttonAgain.setIcon(new ImageIcon(ClassLoader
                .getSystemResource("media/bell20.ico")));
        buttonMute.addActionListener(this);
        buttonMute.setToolTipText("Dieses Fenster schlie�en");
        //buttonMute.setBackground(caller.getConfig().getColors()[ColorSet.NOTI]);
        buttonAgain.addActionListener(this);
        buttonAgain.setToolTipText("Dieses Fenster in der angegebenen Zeit noch einmal �ffnen");
        //buttonAgain.setBackground(caller.getConfig().getColors()[ColorSet.NOTI]);
        hourBox = new JComboBox();
        for (int i = 0; i < 24; i++)
            hourBox.addItem(i);
        minuteBox = new JComboBox();
        for (int i = 0; i < 60; i++)
            minuteBox.addItem(i);
        hourBox.setSelectedIndex(0);
        hourBox.addItemListener(this);
        minuteBox.setSelectedIndex(30);
        minuteBox.addItemListener(this);
        
        beforeBegin = event.getDate().hasTime() && dayDiff == 0;
        logger.debug("event has a time and is today");
        if (beforeBegin) {
            long minDiff = event.getDate().minDiff(new Date());
            if (minDiff <= 0)
                beforeBegin = false;
        }
        logger.debug("beforeBegin=" + beforeBegin);

        if (beforeBegin)
            label1 = new JLabel("");
        else
            label1 = new JLabel("in ");
        alarmOptionsLeft.add(label1);
        alarmOptionsLeft.add(hourBox);
        alarmOptionsLeft.add(new JLabel("h und "));
        alarmOptionsLeft.add(minuteBox);
        if (beforeBegin)
            label2 = new JLabel("min vor Beginn ");
        else
            label2 = new JLabel("min ");
        alarmOptionsLeft.add(label2);
        alarmOptionsLeft.add(buttonAgain);
        alarmOptionsLeft.setBackground(caller.getConfig().getColors()[ColorSet.NOTI]);
        alarmOptionsRight.add(buttonMute);
        alarmOptionsRight.setBackground(caller.getConfig().getColors()[ColorSet.NOTI]);
        alarmOptions.add(alarmOptionsLeft, BorderLayout.WEST);
        alarmOptions.add(alarmOptionsRight, BorderLayout.EAST);
        alarmOptions.setBackground(caller.getConfig().getColors()[ColorSet.NOTI]);

        add(main, BorderLayout.CENTER);
        add(eventOptions, BorderLayout.EAST);
        add(alarmOptions, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(caller.getGUI().getFrame());
        setVisible(true);
        setResizable(false);
    }

    /**
     * Launch a new notification.
     * @param c - Parent calendar object
     * @param e - Event to notify of
     */
    public Notification(Calendar c, Event e) {
        super(c.getGUI().getFrame(), "Denk dran...");

        if (e == null)
            return;
        
        c.addCurrentNoti(this);

        setIcon();

        event = e;
        caller = c;

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

        this.setAlwaysOnTop(true);
    }

    @Override
    public void actionPerformed(ActionEvent a) {
        boolean editEvent = false;
        
        /*
         * Mute button clicked: do nothing
         */
        if (a.getSource().equals(buttonMute))
            ;
        
        /*
         * Notify again button clicked: start new timer
         */
        else if (a.getSource().equals(buttonAgain)) {
            long minDiff = event.getDate().minDiff(new Date());
            logger.debug("minDiff=" + minDiff);
            if (beforeBegin && minDiff <= 0) {
                logger.debug("relaunch notification");
                new Notification(caller, event);
            }
            else {
                java.util.Timer timer = new java.util.Timer(true);
                int stunden = hourBox.getSelectedIndex();
                int minuten = minuteBox.getSelectedIndex();
                long minDuration = 60 * stunden + minuten;
                logger.debug("selected minutes: " + minDuration);
                if (beforeBegin) {
                    minDuration = minDiff - minDuration;
                    logger.debug("beforeBegin -> minDuration=" + minDuration);
                    if (minDuration < 0)
                        minDuration = 0;
                }
                AlarmTask at = new AlarmTask(caller, event);
                caller.addAlarmTask(at);
                timer.schedule(at, minDuration * 60 * 1000);
            }
        } 
        
        /*
         * Delete event button clicked: call parent calendar for
         * deleting
         */
        else if (a.getSource().equals(deleteButton)) {
            setVisible(false);
            if (event.isHoliday() || event.isSpecial())
                new Settings(caller, Settings.TAB_HOLIDAYS);
            else
                caller.deleteEvent(event);
        } 
        
        /*
         * Edit event button clicked: open edit event frame
         */
        else if (a.getSource().equals(editButton))
            editEvent = true;

        /*
         * Any case: close this frame
         */
        if (refresher != null)
            refresher.quit();
        caller.removeCurrentNoti(this);
        setVisible(false);
        
        if (editEvent)
            new EditEvent(caller, event, false);
        
        dispose();
    }
    
    /**
     * 
     * @return Event to notify of.
     */
    public Event getEvent() {
        return this.event;
    }
    
    /**
     * Set a time label in this notification frame.
     * @param timeLabel - New label
     */
    public void refreshTimeLabel(String timeLabel) {
        logger.debug("refresh Label");
        this.timeLabel.setText(timeLabel);
        this.timeLabel.invalidate();
        this.pack();
    }

    @Override
    public void itemStateChanged(ItemEvent i) {
        int a = hourBox.getSelectedIndex();
        int b = minuteBox.getSelectedIndex();
        if (a == 0 && b == 0)
            buttonAgain.setEnabled(false);
        else
            buttonAgain.setEnabled(true);
    }
}
