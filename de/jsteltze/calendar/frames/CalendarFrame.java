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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Timer;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import de.jsteltze.calendar.Calendar;
import de.jsteltze.calendar.Event;
import de.jsteltze.calendar.UI.CalendarGUI;
import de.jsteltze.calendar.UI.CalendarPanel;
import de.jsteltze.calendar.applet.CalendarApplet;
import de.jsteltze.calendar.config.Configuration;
import de.jsteltze.calendar.config.Holidays;
import de.jsteltze.calendar.tasks.AlarmTask;
import de.jsteltze.calendar.tasks.ChangeIconTask;
import de.jsteltze.common.calendar.Date;

/**
 * Main frame of the calendar.
 * @author Johannes Steltzer
 *
 */
public class CalendarFrame 
    extends JFrame 
    implements WindowListener, MouseListener, CalendarGUI {
    
    private static final long serialVersionUID = 1L;

    private static final Image cal16 = Toolkit.getDefaultToolkit().createImage(CalendarFrame.class.getClassLoader().getResource("media/calendar16.ico"));
    private static final Image cal32 = Toolkit.getDefaultToolkit().createImage(CalendarFrame.class.getClassLoader().getResource("media/calendar32.ico"));
    private static final Image bell16 = Toolkit.getDefaultToolkit().createImage(CalendarFrame.class.getClassLoader().getResource("media/bell16.ico"));
    private static final Image bell30 = Toolkit.getDefaultToolkit().createImage(CalendarFrame.class.getClassLoader().getResource("media/bell30.ico"));
    public static final Dimension defaultSize = new Dimension(695, 437);

    /** main object that manages all data and events */
    private Calendar calendar;
    
    /** main calendar panel */
    private CalendarPanel calendarPanel;

    /** is the frame currently invisible and at systray? */
    private boolean atSystray;

    /** systray object of the host operating system */
    private SystemTray systray;

    /** icon object at systray */
    private TrayIcon trayIcon;

    /** task for changing the icon in case of notifications */
    private Timer changeIcon;
    
    private static Logger logger = Logger.getLogger(CalendarFrame.class);

    /**
     * Construct a new calendar main frame.
     * @param size - Dimension to start with. If width equals 0, frame 
     *         will start maximized. If width equals -1, frame will start 
     *         with default size.
     * @param view - Specify view to start with (pass -1 to use default),
     *         see Configuration.VIEW_XXX
     * @param calendar - Parent calendar object
     */
    public CalendarFrame(Dimension size, byte view, Calendar calendar) {
        super("Kalender");
        setIconImage(cal32);

        this.atSystray = false;
        this.calendar = calendar;

        calendarPanel = new CalendarPanel(view, calendar, false);
        add(calendarPanel);
        
        setLocationRelativeTo(null);
        setResizable(true);
        
        /* set visible when UI is set */
        setVisible(false);
        
        addWindowListener(this);
        
        if (size.width == 0)
            this.setExtendedState(Frame.MAXIMIZED_BOTH);
        else if (size.width == -1)
            setSize(defaultSize);
        else
            setSize(size);
        setLocationRelativeTo(null);
    }
    
    @Override
    public void updateStatusBar() {
        calendarPanel.updateStatusBar();
    }
    
    /**
     * Apply a new look&feel.
     * @param style - See Configuration.STYLE_XXX
     * @param visible - Change visibility to visible
     */
    public void setUI(byte style, boolean visible) {
        calendarPanel.setUI(style);
        if (visible) {
            this.setVisible(false);
            this.setVisible(true);
        }
    }

    @Override
    public void update() {
        calendarPanel.update();
    }

    /**
     * Maximize this main windows (set visible again and remove 
     * tray icon from systray). DOES NOT CHANGE FRAME SIZE.
     */
    public void maximize() {
        logger.debug("maximize");
        setVisible(true);
        this.toFront();
        if (atSystray)
            try {
                systray.remove(trayIcon);
                atSystray = false;
            } catch (Exception e) {
                logger.error("error while trying to remove tray icon...", e);
            }
    }
    
    @Override
    public void putMessage(String msg) {
        calendarPanel.putInfoMessage(msg);
    }
    
    /**
     * Set the update available flag.
     */
    public void updateAvailable(String version) {
        if (atSystray)
            trayIcon.displayMessage("Kalender-Update verfügbar", "Version " + version + " wurde veröffentlicht.", TrayIcon.MessageType.INFO);
        calendarPanel.updateAvailable();
    }
    
    /**
     * Apply a new color for the control bar.
     * @param x - Color to set
     */
    public void setButtonPanelColor(Color x) {
        calendarPanel.setButtonPanelColor(x);
    }

    /**
     * Tells if the window is currently at systray.
     * @return True if minimized in systray. False otherwise.
     */
    public boolean isInSystray() {
        return atSystray;
    }

    /**
     * Makes the entire program invisible and adds an icon to 
     * the systray. DOES NOT EXIT!
     */
    public void toSystray() {
        logger.debug("minimize");
        try {
            if (!SystemTray.isSupported())
                throw new Exception();
            atSystray = true;
            setVisible(false);
            //calendar.save();

            /* Tray icon title */
            String title = "Kalender - " + new Date().dateToString(false) + "\n* 1 Klick: Statusinformation abrufen\n* Doppelklick: Kalender hervorholen\n* rechte Maustaste: Menü";
            
            /*
             * Create tray icon and popup menu
             */
            trayIcon = new TrayIcon(cal16, title, new TrayIconMenu(this));
            trayIcon.addMouseListener(this);
            trayIcon.setImageAutoSize(true);
            boolean firstSystrayMove = false;
            if (systray == null) {
                systray = SystemTray.getSystemTray();
                firstSystrayMove = true;
            }

            systray.add(trayIcon);
            if (calendar.isFirstStartup() && firstSystrayMove)
                trayIcon.displayMessage("Kalender:",
                        "Der Kalender befindet sich jetzt hier.\nDoppelklicken zum Hervorholen oder rechte Maustaste zum Beenden.",
                        TrayIcon.MessageType.INFO);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Das Betriebssystem unterstützt keinen Systray!\nDer Kalender wird nun beendet...");
            /* call shutdown hook */
            System.exit(0);
        }
    }
    
    /**
     * Show basic information on the calendar in a tray icon
     * message box.
     */
    private void showTrayIconInfo() {
        if (!isInSystray())
            return;
        int num_holidays = Holidays.getNumberOfHolidays(calendar.getConfig().getHolidays()) +
                Holidays.getNumberOfHolidays(calendar.getConfig().getSpecialDays());
        Vector<Event> events = calendar.getAllEvents();
        Vector<AlarmTask> alarms = calendar.getAlarmTasks();
        String message = "Insgesamt: " + (events.size() - num_holidays) + " Ereignisse, " +
                num_holidays + " Feiertage\n\n";
        message += "Heute:";
        Date now = new Date();
        boolean haveMatches = false;
        for (Event e : events) {
            if (e.match(now)) {
                message += "\n    - " + e.getName();
                if (e.getDate().hasTime())
                    message += " (" + e.getDate().timeToString() + ")";
                haveMatches = true;
            }
        }
        if (!haveMatches)
            message += " keine Ereignisse";
        
        if (alarms.size() > 0) {
            message += "\n\nLaufende Erinnerungen:";
            for (AlarmTask alarm : alarms) {
                Event event = alarm.getEvent();
                message += "\n    - " + event.getDayDiffLabel(true) +
                        ": " + event.getName();
                if (event.getDate().hasTime())
                    message += " (" + event.getDate().timeToString() + " Uhr)";
            }
        }
        trayIcon.displayMessage("Kalender - " + now.dateToString(false) + " " + now.timeToString(), message, TrayIcon.MessageType.INFO);
    }

    /**
     * 
     * @return Current view type (year/month...), see Configuration.VIEW_XXX.
     */
    public int getView() {
        return calendarPanel.getView();
    }
    
    /**
     * Returns the path of a file or directory with the current
     * working directory.
     * @param file - File or directory to get
     * @return Path of the file within the working directory.
     */
    public String getPath(String file) {
        return calendar.getPath(file);
    }
    
    /**
     * 
     * @return Command line arguments the calendar was started with.
     */
    public String[] getArgs() {
        return calendar.getArgs();
    }

    /**
     * Starts a task to switch the icon each second. Change between
     * default icon and notification icon.
     */
    public void startChangeIcon() {
        if (changeIcon != null)
            return;
        changeIcon = new Timer(true);
        long dauer = 1000, delay = 0;
        changeIcon.schedule(new ChangeIconTask(this), delay, dauer);
    }

    /**
     * Change the icon of this frame. Change from default icon to
     * notification icon or other way.
     */
    public void changeIcon() {
        /* Stop changing if no more notifications */
        logger.debug("changeIcon: notis size=" + calendar.getNotificationSize());
        if (calendar.getNotificationSize() == 0) {
            stopChangeIcon();
            return;
        }
        
        if (!atSystray) {
            if (this.getIconImage().equals(cal32))
                this.setIconImage(bell30);
            else
                this.setIconImage(cal32);
        } else {
            if (trayIcon.getImage().equals(cal16))
                trayIcon.setImage(bell16);
            else
                trayIcon.setImage(cal16);
        }
    }

    /**
     * Stop changing the icon and set the default icon again.
     */
    public void stopChangeIcon() {
        if (changeIcon != null) {
            changeIcon.cancel();
            changeIcon = null;
        }
        if (!atSystray)
            setIconImage(cal32);
        else
            trayIcon.setImage(cal16);
    }

    @Override
    public void windowDeactivated(WindowEvent w) {
    }

    @Override
    public void windowActivated(WindowEvent w) {
    }

    @Override
    public void windowDeiconified(WindowEvent w) {
    }

    @Override
    public void windowIconified(WindowEvent w) {
    }

    @Override
    public void windowClosed(WindowEvent w) {
    }

    @Override
    public void windowClosing(WindowEvent w) {
        logger.debug("windowClosing");
        if (calendar.getConfig().getOnCloseAction() == Configuration.ON_CLOSE_EXIT)
            /* call shutdown hook */
            System.exit(0);
        else if (calendar.getConfig().getOnCloseAction() == Configuration.ON_CLOSE_MOVE_TO_SYSTRAY
                && !atSystray)
            toSystray();
    }

    @Override
    public void windowOpened(WindowEvent w) {
    }

    @Override
    public void mouseExited(MouseEvent m) {
    }

    @Override
    public void mouseEntered(MouseEvent m) {
    }

    @Override
    public void mouseReleased(MouseEvent m) {
    }

    @Override
    public void mousePressed(MouseEvent m) {
    }

    @Override
    public void mouseClicked(MouseEvent m) {
        if (m.getSource().equals(trayIcon)) {
            if (m.getClickCount() == 2 && !m.isMetaDown())
                maximize();
            else if (m.getClickCount() == 1 && !m.isMetaDown())
                showTrayIconInfo();
        }
    }
    
    @Override
    public CalendarFrame getFrame() {
        return this;
    }
    
    @Override
    public CalendarApplet getApplet() {
        return null;
    }
    
    @Override
    public void shutdown() {
        if (changeIcon != null)
            changeIcon.cancel();
        calendarPanel.shutdown();
//        this.setVisible(false);
//        this.dispose();
    }
}

/**
 * Calendar systray popup menu.
 * @author Johannes Steltzer
 *
 */
class TrayIconMenu 
    extends PopupMenu 
    implements ActionListener {
    
    private static final long serialVersionUID = 1L;

    /** calendar frame which hides behind systray icon */
    private CalendarFrame cf;
    
    /** menu item for maximizing */
    private MenuItem maxItem;
    
    /** menu item for quitting */
    private MenuItem exitItem;

    /**
     * Construct new systray menu.
     * @param cf - Calendar frame to refer
     */
    public TrayIconMenu(CalendarFrame cf) {
        super("Kalender Optionen:");
        maxItem = new MenuItem("Kalender öffnen");
        exitItem = new MenuItem("Kalender beenden");
        maxItem.addActionListener(this);
        exitItem.addActionListener(this);
        this.add(maxItem);
        this.addSeparator();
        this.add(exitItem);

        this.cf = cf;
    }

    @Override
    public void actionPerformed(ActionEvent a) {
        if (a.getSource().equals(maxItem))
            cf.maximize();

        else if (a.getSource().equals(exitItem))
            /* call shutdown hook */
            System.exit(0);
    }
}