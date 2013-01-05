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

package de.jsteltze.calendar.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;

import de.jsteltze.calendar.Calendar;
import de.jsteltze.calendar.Update;
import de.jsteltze.calendar.config.ColorSet;
import de.jsteltze.calendar.config.Configuration;
import de.jsteltze.calendar.config.Const;
import de.jsteltze.calendar.config.Holidays;
import de.jsteltze.calendar.frames.Settings;
import de.jsteltze.calendar.frames.TableOfEventsSingleDay;
import de.jsteltze.calendar.tasks.RefreshDateTask;
import de.jsteltze.common.ImageButton;
import de.jsteltze.common.ImageButtonGroup;
import de.jsteltze.common.ImageButtonListener;
import de.jsteltze.common.LinkLabel;
import de.jsteltze.common.MessageTransparator;
import de.jsteltze.common.calendar.Date;

/**
 * Calendar GUI component to be added in applet or 
 * stand-alone frame.
 * @author Johannes Steltzer
 *
 */
public class CalendarPanel 
    extends JPanel 
    implements MouseListener, ActionListener, ImageButtonListener {

    private static final long serialVersionUID = 1L;
        
    /** main object that manages all data and events */
    private Calendar calendar;
    
    /** canvas where everything is painted */
    private CalendarCanvas canvas;
    
    /** control bars left rolling border */
    private LeftPanelBorder leftBorder;
    
    /** control bars rights rolling border */
    private RightPanelBorder rightBorder;

    /** buttons for the different possible views (year, month ...) */
    private ImageButton[] views;

    /** button for jumping to any date */
    private ImageButton jump;

    /** buttons for browsing within the current view */
    private JButton toPrev, toNext, toToday;
    
    /** button for settings dialog */
    private ImageButton settings;
    
    /** status bar labels */
    private JLabel dateLabel, notificationsLabel;
    
    /** label for info messages */
    private JLabel infoLabel;
    
    /** clickable labels */
    private LinkLabel eventsLabel, holidaysLabel, updateLabel;
    
    /** status bar area for date and time */
    private JPanel statusBarDate;
    
    /** status bar area for number of events & holidays */
    private JPanel statusBarInfos, statusBarNumberPanel, infoTextPanel;

    /** panels to contain the different kinds of buttons and views */
    private JPanel settingsPanel, buttonLeft, buttonPanelCoreCenter, buttonRight;

    /**
     * currently displayed view (year/month/week/day). See Configuration:
     * VIEW_XXXX
     */
    private byte view;
    
    /** current UI */
    private byte currentUI;
    
    /** is this a browser applet? */
    private boolean appletMode;
    
    /** slowly makes a label transparent */
    private MessageTransparator msgPutter;
    
    /** is an update available? */
    private boolean updateIsAvailable;
    
    /** refresh date&time every minute */
    private RefreshDateTask refreshDateTask;
    
    private static Logger logger = Logger.getLogger(CalendarPanel.class);
    
    /**
     * Construct a new calendar panel.
     * @param view - View to start with (see Configuration.VIEW_XXX)
     * @param calendar - Calendar parent object
     * @param appletMode - True for a java browser applet, false
     *         otherwise
     */
    public CalendarPanel(byte view, Calendar calendar, boolean appletMode) {
        this.view = view;
        this.calendar = calendar;
        this.currentUI = -1;
        this.appletMode = appletMode;
        this.updateIsAvailable = false;
        
        canvas = new CalendarCanvas(calendar, view);

        arrangePanel();

        msgPutter = new MessageTransparator(infoLabel);
        msgPutter.start();
        refreshDateTask = new RefreshDateTask(dateLabel);
        refreshDateTask.start();
    }

    /**
     * Arrange all components in this panel.
     */
    private void arrangePanel() {
        setLayout(new BorderLayout());
        this.setBackground(Const.COLOR_BG_MAIN);

        /* Button Panel */
        JPanel buttonPanel = new JPanel();
        JPanel buttonPanelCore = new JPanel();
        buttonPanelCoreCenter = new JPanel();
        buttonLeft = new JPanel();
        buttonRight = new JPanel();
        settingsPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanelCore.setLayout(new BorderLayout());
        settingsPanel.setLayout(new BorderLayout());
        settingsPanel.addMouseListener(this);

        toNext = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("media/arrow_r16.ico")));
        toPrev = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("media/arrow_l16.ico")));
        jump = new ImageButton("media/jump26.ico", "media/jump26.ico", false);
        if (view == Configuration.VIEW_YEAR)
            toToday = new JButton("zum aktuellen Jahr");
        else if (view == Configuration.VIEW_MONTH)
            toToday = new JButton("zum aktuellen Monat");
        else if (view == Configuration.VIEW_WEEK)
            toToday = new JButton("zur aktuellen Woche");
        else
            toToday = new JButton("zum aktuellen Tag");
        toNext.addActionListener(this);
        toPrev.addActionListener(this);
        toToday.addActionListener(this);
        jump.addButtonListener(this);
        toNext.setToolTipText("vorblättern");
        toPrev.setToolTipText("zurückblättern");
        jump.setToolTipText("zu einem bestimmten Datum springen...");

        buttonLeft.add(toPrev);
        buttonLeft.add(jump);
        buttonPanelCore.add(buttonLeft, BorderLayout.WEST);

        ImageButtonGroup group = new ImageButtonGroup();
        views = new ImageButton[4];
        views[0] = new ImageButton("media/365.PNG", "media/365_2.PNG", true);
        views[1] = new ImageButton("media/30.PNG", "media/30_2.PNG", true);
        views[2] = new ImageButton("media/7.PNG", "media/7_2.PNG", true);
        views[3] = new ImageButton("media/1.PNG", "media/1_2.PNG", true);

        views[0].setToolTipText(Configuration.VIEW_LABELS[Configuration.VIEW_YEAR]);
        views[1].setToolTipText(Configuration.VIEW_LABELS[Configuration.VIEW_MONTH]);
        views[2].setToolTipText(Configuration.VIEW_LABELS[Configuration.VIEW_WEEK]);
        views[3].setToolTipText(Configuration.VIEW_LABELS[Configuration.VIEW_DAY]);
        views[view].setPressed(true);

        for (int i = 0; i < 4; i++) {
            views[i].setButtonGroup(group);
            views[i].addButtonListener(this);
        }

        buttonPanelCoreCenter.add(views[0]);
        buttonPanelCoreCenter.add(views[1]);
        buttonPanelCoreCenter.add(toToday);
        buttonPanelCoreCenter.add(views[2]);
        buttonPanelCoreCenter.add(views[3]);

        buttonPanelCore.add(buttonPanelCoreCenter, BorderLayout.CENTER);
        buttonRight.add(toNext);
        buttonPanelCore.add(buttonRight, BorderLayout.EAST);
        buttonPanel.add(buttonPanelCore, BorderLayout.CENTER);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, 
                Const.COLOR_CONTROL_BORDER));
        /* End Button Panel */
        
        JPanel controlBar = new JPanel(new BorderLayout());
        leftBorder = new LeftPanelBorder(Toolkit.getDefaultToolkit().createImage(
                this.getClass().getClassLoader().getResource("media/controlbar_leftborder.png")),
                calendar.getConfig().getColors()[ColorSet.CONTROLPANEL]);
        controlBar.add(leftBorder, BorderLayout.WEST);
        controlBar.add(buttonPanel, BorderLayout.CENTER);
        rightBorder = new RightPanelBorder(Toolkit.getDefaultToolkit().createImage(
                this.getClass().getClassLoader().getResource("media/controlbar_rightborder.png")),
                calendar.getConfig().getColors()[ColorSet.CONTROLPANEL]);
        controlBar.add(rightBorder, BorderLayout.EAST);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlBar, BorderLayout.CENTER);
        settings = new ImageButton("media/settings.PNG", "media/settings.PNG", false);
        settings.addMouseListener(this);
        settings.setToolTipText("Einstellungen...");
        topPanel.add(settings, BorderLayout.EAST);
        topPanel.setBackground(Const.COLOR_BG_MAIN);
    
        add(topPanel, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        
        statusBarDate = new JPanel(new BorderLayout());
        Date today = new Date();
        dateLabel = new JLabel(today.dateToString(true) + " " + today.timeToString());
        dateLabel.setForeground(Color.gray);
        dateLabel.setFont(Const.FONT_STATUSBAR);
        statusBarDate.add(dateLabel, BorderLayout.CENTER);
        statusBarDate.setBackground(calendar.getConfig().getColors()[ColorSet.CONTROLPANEL]);
        statusBarDate.setBorder(new EtchedBorder());
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBarNumberPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        statusBar.add(statusBarDate, BorderLayout.WEST);
        eventsLabel = new LinkLabel((calendar.getAllEvents().size() - 
                Holidays.getNumberOfHolidays(calendar.getConfig().getHolidays()) -
                Holidays.getNumberOfHolidays(calendar.getConfig().getSpecialDays())) + " Ereignisse",
                "Alle Ereignisse anzeigen", (Window) null);
        eventsLabel.addMouseListener(this);
        holidaysLabel = new LinkLabel((Holidays.getNumberOfHolidays(calendar.getConfig().getHolidays()) +
                Holidays.getNumberOfHolidays(calendar.getConfig().getSpecialDays())) + " Feiertage",
                "Alle Feiertage anzeigen", (Window) null);
        holidaysLabel.addMouseListener(this);
        notificationsLabel = new JLabel(calendar.getAlarmTasks().size() + " Erinnerungen aktiv");
        
        //eventsLabel.setForeground(Color.blue);
        eventsLabel.setFont(Const.FONT_STATUSBAR);
        holidaysLabel.setFont(Const.FONT_STATUSBAR);
        notificationsLabel.setForeground(Color.gray);
        notificationsLabel.setFont(Const.FONT_STATUSBAR);
        JLabel separator = new JLabel(" | ");
        separator.setFont(Const.FONT_STATUSBAR);
        separator.setForeground(Color.gray);
        JLabel separator2 = new JLabel(" | ");
        separator2.setFont(Const.FONT_STATUSBAR);
        separator2.setForeground(Color.gray);
        
        statusBarNumberPanel.add(eventsLabel);
        statusBarNumberPanel.add(separator);
        statusBarNumberPanel.add(holidaysLabel);
        statusBarNumberPanel.add(separator2);
        statusBarNumberPanel.add(notificationsLabel);
        statusBarNumberPanel.setBackground(calendar.getConfig().getColors()[ColorSet.CONTROLPANEL]);
        
        infoLabel = new JLabel("");
        infoLabel.setFont(Const.FONT_STATUSBAR);
        infoTextPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        infoTextPanel.add(infoLabel);
        infoTextPanel.setBackground(calendar.getConfig().getColors()[ColorSet.CONTROLPANEL]);
        statusBarInfos = new JPanel(new BorderLayout());
        statusBarInfos.add(infoTextPanel, BorderLayout.CENTER);
        statusBarInfos.add(statusBarNumberPanel, BorderLayout.EAST);
        statusBarInfos.setBorder(new EtchedBorder());
        statusBarInfos.setBackground(calendar.getConfig().getColors()[ColorSet.CONTROLPANEL]);
        
        statusBar.add(statusBarInfos, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        
        setButtonPanelColor(calendar.getConfig().getColors()[ColorSet.CONTROLPANEL]);
    }
    
    /**
     * Sets the background color of the upper control panel which 
     * includes the view buttons.
     * @param x - Color to set
     */
    public void setButtonPanelColor(Color x) {
        logger.debug("setButtonPanelColor");
        settingsPanel.setBackground(x);
        buttonLeft.setBackground(x);
        buttonPanelCoreCenter.setBackground(x);
        buttonRight.setBackground(x);
        leftBorder.setColor(x);
        rightBorder.setColor(x);
        statusBarDate.setBackground(x);
        statusBarInfos.setBackground(x);
        statusBarNumberPanel.setBackground(x);
        infoTextPanel.setBackground(x);
    }
    
    /**
     * Unset any selection.
     */
    public void resetSelection() {
        canvas.unmarkAll();
    }
    
    /**
     * Update the number of events, holidays and pending notifications
     * in the bottom status bar.
     */
    public void updateStatusBar() {
        int num_holidays = Holidays.getNumberOfHolidays(calendar.getConfig().getHolidays()) + 
                Holidays.getNumberOfHolidays(calendar.getConfig().getSpecialDays());
        //cannot use this, since flexible holidays may occure twice
        //int num_events = calendar.getAllEvents().size() - num_holidays;
        int num_events = calendar.getNumberOfEvents();
        int num_notis = calendar.getAlarmTasks().size();
        eventsLabel.setText(num_events + " Ereignis" + (num_events == 1 ? "" : "se"));
        holidaysLabel.setText(num_holidays + " Feiertag" + (num_holidays == 1 ? "" : "e"));
        notificationsLabel.setText(num_notis + " Erinnerung" + (num_notis == 1 ? "" : "en") + " aktiv");
    }
    
    /**
     * Set the default border width for the buttons in the button
     * panel.
     */
    private void setButtonPanelDefaultButtons() {
        Border defaultBorder = new JButton().getBorder();
        toToday.setBorder(defaultBorder);
        toPrev.setBorder(defaultBorder);
        toNext.setBorder(defaultBorder);
    }
    
    /**
     * Apply a new look&feel.
     * @param style - Look&Feel to apply (see Configuration.STYLE_XXX)
     */
    public void setUI(byte style) {
        if (style == currentUI)
            return;
        else
            currentUI = style;
        
        logger.debug("set UI: " + style);
        setButtonPanelDefaultButtons();
        
        try {
            switch (style) {
            case Configuration.STYLE_SYSTEM:
                if (UIManager.getSystemLookAndFeelClassName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
                    toToday.setBorder(new EmptyBorder(5, 15, 5, 15));
                    toPrev.setBorder(new EmptyBorder(5, 15, 5, 15));
                    toNext.setBorder(new EmptyBorder(5, 15, 5, 15));    
                }
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                break;
            case Configuration.STYLE_SWING:
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                break;
            case Configuration.STYLE_MOTIF:
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                toToday.setBorder(new EtchedBorder());
                toPrev.setBorder(new EtchedBorder());
                toNext.setBorder(new EtchedBorder());
                break;
            case Configuration.STYLE_NIMBUS:
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                toToday.setBorder(new EmptyBorder(5, 15, 5, 15));
                toPrev.setBorder(new EmptyBorder(5, 15, 5, 15));
                toNext.setBorder(new EmptyBorder(5, 15, 5, 15));
                break;
            default:
                logger.error("setUI: unknown code " + style);
            }
        } catch (Exception e) {
            logger.error("cannot set UI...", e);
        }
        
        SwingUtilities.updateComponentTreeUI(this);
        this.setVisible(false);
        this.setVisible(true);    
    }
    
    /**
     * Display an info message in the status bar.
     * @param msg - Message to display
     */
    public void putInfoMessage(String msg) {
        if (updateIsAvailable)
            return;
        infoLabel.setText(msg);
        infoLabel.revalidate();
        msgPutter.interrupt();
    }
    
    /**
     * Notify about an available update.
     * (Will display a message and lock it)
     */
    public void updateAvailable() {
        updateIsAvailable = true;
        infoLabel.setText(" >> Es ist ein ");
        infoLabel.setForeground(Color.black);
        updateLabel = new LinkLabel("Update");
        updateLabel.addMouseListener(this);
        JLabel updateLabel2 = new JLabel(" verfügbar");
        updateLabel.setFont(Const.FONT_STATUSBAR);
        updateLabel2.setFont(Const.FONT_STATUSBAR);
        infoTextPanel.add(updateLabel);
        infoTextPanel.add(updateLabel2);
        infoTextPanel.validate();
    }
    
    /**
     * Update the calendar (in case of new events / removed events).
     */
    public void update() {
        canvas.update();
    }
    
    /**
     * 
     * @return current view type (year/month...) See Configuration.VIEW_XXX
     */
    public int getView() {
        return view;
    }
    
    /**
     * Gracefully shutdown the program and stop GUI related
     * tasks.
     */
    public void shutdown() {
        if (refreshDateTask != null)
            refreshDateTask.stopit();
        if (msgPutter != null)
            msgPutter.stopit();
    }
    
    @Override
    public void mouseExited(MouseEvent m) {
        m.getComponent().setCursor(
                Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseEntered(MouseEvent m) {
        m.getComponent().setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseReleased(MouseEvent m) {
    }

    @Override
    public void mousePressed(MouseEvent m) {
        if (m.getSource().equals(settings)) {
            if (appletMode)
                putInfoMessage("IN DIESEM ONLINE-APPLET KÖNNEN KEINE EINSTELLUNGEN GEÄNDERT WERDEN!");
            else
                new Settings(calendar);
        }
    }

    @Override
    public void mouseClicked(MouseEvent m) {
        
        if (m.getSource().equals(eventsLabel))
            new TableOfEventsSingleDay(null, calendar, false);
        
        else if (m.getSource().equals(holidaysLabel))
            new TableOfEventsSingleDay(null, calendar, true);
        
        else if (m.getSource().equals(updateLabel)) {
            new Update(calendar.getGUI().getFrame(), false);
            updateIsAvailable = false;
            infoLabel.setForeground(new Color(.5f, .5f, .5f, 0));
            infoTextPanel.removeAll();
            infoTextPanel.add(infoLabel);
            infoTextPanel.validate();
            infoTextPanel.repaint();
        }
    }

    @Override
    public void actionPerformed(ActionEvent a) {
        if (a.getSource().equals(toNext) || a.getSource().equals(toPrev)) {
            Date dest = (Date) calendar.getViewedDate().clone();
            int factor = a.getSource().equals(toNext) ? 1 : -1;
            if (view == Configuration.VIEW_YEAR)
                dest.add(java.util.Calendar.YEAR, factor * 1);
            else if (view == Configuration.VIEW_MONTH)
                dest.add(java.util.Calendar.MONTH, factor * 1);
            else if (view == Configuration.VIEW_WEEK)
                dest.add(java.util.Calendar.DAY_OF_MONTH, factor * 7);
            else if (view == Configuration.VIEW_DAY)
                dest.add(java.util.Calendar.DAY_OF_MONTH, factor * 1);

            jumpTo(dest);
        }

        else if (a.getSource().equals(toToday))
            jumpTo(new Date());
    }
    
    /**
     * Jump to a any date.
     * @param x - Date to jump to
     */
    private void jumpTo(Date x) {
        logger.info("jump to date "+x.dateToString(false));
        
        /*
         * Kind of easter egg
         */
        if (x.get(java.util.Calendar.YEAR) <= 1582) {
            JOptionPane.showMessageDialog(this,
                    "Der heute weltweit verwendete gregorianische Kalender\nwurde 1582 durch Papst Gregor XIII. eingeführt.\nEine Darstellung früherer Jahr in dieser Ansicht\nist nicht möglich.",
                    "Kalendergrenze...", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Date viewedDate = calendar.getViewedDate();

        /* In case of change of year: Update flexible Holidays */
        if (x.get(java.util.Calendar.YEAR) != 
                viewedDate.get(java.util.Calendar.YEAR))
            calendar.updateFlexibleHolidays(x.get(java.util.Calendar.YEAR), false, false);

        calendar.setViewedDate(x);
        canvas.repaint();
    }

    @Override
    public void buttonPressed(ImageButton x) {
        /*
         * Change of view (year, month ...)
         */
        if (x.equals(views[0]) || x.equals(views[1]) || x.equals(views[2])
                || x.equals(views[3])) {
            if (views[0].isPressed()) {
                view = Configuration.VIEW_YEAR;
                toToday.setText("zum aktuellen Jahr");
            } else if (views[1].isPressed()) {
                view = Configuration.VIEW_MONTH;
                toToday.setText("zum aktuellen Monat");
            } else if (views[2].isPressed()) {
                view = Configuration.VIEW_WEEK;
                toToday.setText("zur aktuellen Woche");
            } else if (views[3].isPressed()) {
                view = Configuration.VIEW_DAY;
                toToday.setText("zum aktuellen Tag");
            }

            if (canvas.getView() != view) {
                canvas.setView(view);
                if (!appletMode) {
                    Configuration config = calendar.getConfig();
                    config.setView(view);
                    calendar.setConfig(config);
                }
            }
        } 
        
        /*
         * Jump button clicked
         */
        else if (x.equals(jump)) {
            if (view == Configuration.VIEW_YEAR) {
                /*
                 * Jump to any year
                 */
                String input = JOptionPane.showInputDialog(this,
                        "Jahr:", "Springen nach...",
                        JOptionPane.PLAIN_MESSAGE);
                if (input == null)
                    return;
                
                int year;
                try {
                    year = Integer.parseInt(input);
                    if (year < 0 || year > 9999)
                        throw new NumberFormatException();
                    Date newDate = calendar.getViewedDate();
                    newDate.set(java.util.Calendar.YEAR, year);
                    jumpTo(newDate);
                } catch (NumberFormatException n) {
                    JOptionPane.showMessageDialog(this,
                            "Ungültiges Jahr!",
                            "Eingabefehler...", JOptionPane.ERROR_MESSAGE);
                }
            } 
            else if (view == Configuration.VIEW_MONTH) {
                /*
                 * Jump to any month
                 */
                String input = JOptionPane.showInputDialog(this,
                        "Monat/Jahr (MM/JJJJ):", "Springen nach...",
                        JOptionPane.PLAIN_MESSAGE);
                if (input == null)
                    return;
                
                int month, year;
                try {
                    String[] splitted = input.split("/");
                    if (splitted.length != 2)
                        throw new NumberFormatException();
                    month = Integer.parseInt(splitted[0]);
                    year = Integer.parseInt(splitted[1]);
                    if (month < 1 || month > 12 || year < 0 || year > 9999)
                        throw new NumberFormatException();
                    jumpTo(new Date(year, month - 1, 1));
                } catch (NumberFormatException n) {
                    JOptionPane.showMessageDialog(this,
                            "Ungültiges Datum!",
                            "Eingabefehler...", JOptionPane.ERROR_MESSAGE);
                }
            } 
            else if (view == Configuration.VIEW_WEEK) {
                /*
                 * Jump to any date or calendar week
                 */
                String input = JOptionPane.showInputDialog(this,
                        "Datum (TT.MM.JJJJ) oder Kalenderwoche/Jahr (KW/JJJJ):", "Springen nach...",
                        JOptionPane.PLAIN_MESSAGE);
                if (input == null)
                    return;
                int day, month, year, kw;
                try {
                    if (input.contains(".")) { 
                        String[] splitted = input.split("\\.");
                        if (splitted.length != 3)
                            throw new NumberFormatException();
                        day = Integer.parseInt(splitted[0]);
                        month = Integer.parseInt(splitted[1]);
                        year = Integer.parseInt(splitted[2]);
                        jumpTo(new Date(year, month - 1, day));
                    }
                    else if (input.contains("/")) {
                        String[] splitted = input.split("/");
                        if (splitted.length != 2)
                            throw new NumberFormatException();
                        kw = Integer.parseInt(splitted[0]);
                        year = Integer.parseInt(splitted[1]);
                        if (kw < 1 || kw > 52 || year < 0 || year > 9999)
                            throw new NumberFormatException();
                        Date dest = new Date(year, java.util.Calendar.DECEMBER, 31);
                        if (dest.get(java.util.Calendar.WEEK_OF_YEAR) == 1)
                            dest.add(java.util.Calendar.DAY_OF_MONTH, -7);
                        if (dest.get(java.util.Calendar.WEEK_OF_YEAR) < kw)
                            throw new NumberFormatException();
                        while (dest.get(java.util.Calendar.WEEK_OF_YEAR) != kw)
                            dest.add(java.util.Calendar.DAY_OF_MONTH, -7);
                        jumpTo(dest);
                    }
                    else
                        throw new NumberFormatException();
                } catch (NumberFormatException n) {
                    JOptionPane.showMessageDialog(this,
                            "Ungültiges Datum!",
                            "Eingabefehler...", JOptionPane.ERROR_MESSAGE);
                }
            }
            else {
                /*
                 * Jump to any date
                 */
                String input = JOptionPane.showInputDialog(this,
                        "Tag/Monat/Jahr (TT.MM.JJJJ):", "Springen nach...",
                        JOptionPane.PLAIN_MESSAGE);
                if (input == null)
                    return;
                int day, month, year;
                try {
                    String[] splitted = input.split("\\.");
                    if (splitted.length != 3)
                        throw new NumberFormatException();
                    day = Integer.parseInt(splitted[0]);
                    month = Integer.parseInt(splitted[1]);
                    year = Integer.parseInt(splitted[2]);
                    jumpTo(new Date(year, month - 1, day));
                } catch (NumberFormatException n) {
                    JOptionPane.showMessageDialog(this,
                            "Ungültiges Datum!",
                            "Eingabefehler...", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
