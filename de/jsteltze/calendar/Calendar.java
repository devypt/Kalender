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

package de.jsteltze.calendar;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JOptionPane;

import de.jsteltze.calendar.UI.CalendarGUI;
import de.jsteltze.calendar.applet.CalendarApplet;
import de.jsteltze.calendar.config.ColorSet;
import de.jsteltze.calendar.config.Configuration;
import de.jsteltze.calendar.config.Holidays;
import de.jsteltze.calendar.exceptions.CannotParseException;
import de.jsteltze.calendar.frames.CalendarFrame;
import de.jsteltze.calendar.frames.EditEvent;
import de.jsteltze.calendar.frames.Notification;
import de.jsteltze.calendar.frames.TableOfEvents;
import de.jsteltze.calendar.frames.TableOfNotifications;
import de.jsteltze.calendar.tasks.AlarmTask;
import de.jsteltze.calendar.tasks.AutoUpdateTask;
import de.jsteltze.calendar.tasks.SingletonTask;
import de.jsteltze.common.Logger;
import de.jsteltze.common.Math;

/**
 * Main class.
 * @author Johannes Steltzer
 *
 */
public class Calendar {
	
	/* to be updated frequently */
	public static final String VERSION = "1.7_svn826";
	public static final String LAST_EDIT_DATE = "11.11.2012";
	
	/* file names */
	public static final String FILENAME = "Kalender.jar";
	public static final String NEW_FILENAME = "KalenderNEU.jar";
	public static final String UPDATER = "KalenderUpdater.jar";
	public static final String XMLFILE = "Kalender.xml";
	public static final String LOCKFILE = "Kalender.lock";
	public static final String MAXIMIZEFILE = "Kalender.maximize";
	public static final String RELEASE_FILE = "Kalender.release";
	public static final String EVENT_DIR = "Kalender.Events";
	public static final String NOTES_FILE = "notes.txt";
	public static final String LINK_FILE = "link.txt";
	public static final String DEFAULT_THEME = "media/notify.wav";
	
	/* URLs */
	public static final String HOME_URL = "http://java-kalender.sourceforge.net";
	public static final String DOWNLOAD_URL = "http://java-kalender.sourceforge.net/";
	
	public static final String ENCODING = "UTF-8";
	public static final String AUTHOR = "Johannes Steltzer";
	public static final String COMPILER = "javac 1.6.0_11";

	/** frame which displays information managed here */
	private CalendarGUI gui;

	/** current date the user is viewing. On startup this is always today */
	private Date viewedDate;

	/**
	 * list of AlarmTasks that will pop up soon for notifying (timer thread is
	 * started already).
	 */
	private Vector<AlarmTask> pendingAlarms;

	/** list of open notifications (actual frames waiting for user input) */
	private Vector<Notification> notis;

	/** all events (including holidays) */
	private Vector<Event> events;

	/** current configuration (settings) */
	private Configuration config;

	/** tells if this is the very first launch of this program */
	private boolean firstStartup;
	
	/** tells if application has fully launched */
	private boolean fullyLaunched;

	/** refresh date and notifications at midnight */
	private Timer refreshAtMidnight; 
	
	/**
	 * make sure this application is only launched once in order
	 * to prevent inconsistency
	 */
	private SingletonTask singletonThread;
	
	/** auto update timer */
	private Timer autoUpdateTimer;
	
	/** browser applet instead of jframe */
	private boolean appletMode;
	
	/** working directory */
	private String workspace;
	
	/** command line arguments */
	private static String[] cmdArgs;

	/**
	 * Construct a new calendar.
	 * @param size - Dimension (in pixels) to start with. Pass -1,-1 to use
	 * default dimension. Pass 0,0 to start with full screen.
	 * @param view - Specify view to start with. See Configuration.VIEW_XXXX.
	 * @param asApplet - Start calendar as java applet.
	 * @param workspace - Use a specific working directory (default is ".").
	 */
	public Calendar(Dimension size, byte view, boolean asApplet, String workspace) {
		this.viewedDate = new Date();
		this.firstStartup = false;
		this.fullyLaunched = false;
		this.pendingAlarms = new Vector<AlarmTask>();
		this.events = new Vector<Event>();
		this.notis = new Vector<Notification>();
		this.appletMode = asApplet;
		this.gui = null;
		this.workspace = workspace;

		CalendarFrame mainFrame = null;
		
		if (!asApplet) {
			/*
			 * Read events from xml file
			 */
			
			XMLParser parser = new XMLParser();
	
			try {
				parser.parse(getPath(XMLFILE));
				events = parser.getEvents();
			} catch (CannotParseException e) {
				JOptionPane.showMessageDialog(gui.getFrame(), 
						"Der Inhalt der Datei \"" + getPath(XMLFILE) + "\" kann nicht gelesen werden.\n> " + e.getMessage() + "\nDer Kalender wird nun leer gestartet.",
						"Fehler beim Lesen...", JOptionPane.ERROR_MESSAGE);
			} catch (FileNotFoundException e) {
				Logger.debug("XML file \"" + getPath(XMLFILE) + "\" not found, assuming first startup");
				firstStartup = true;
			}

			this.config = parser.getConfig();
	
			mainFrame = new CalendarFrame(size, view == Configuration.VIEW_DEFAULT ? 
					this.config.getView() : view, this);
			if (this.config.getSystrayStart()) {
				mainFrame.setUI(this.config.getStyle(), false);
				mainFrame.toSystray();
			}
			else
				mainFrame.setUI(this.config.getStyle(), true);
			gui = mainFrame;
	
			/*
			 * Test for write rights
			 */
			File test = new File(getPath(XMLFILE));
			if (!test.exists())
				try {
					test.createNewFile();
				} catch (IOException e) {}
			if (!test.canWrite())
				JOptionPane.showMessageDialog(mainFrame,
						"Der Kalender hat hier keine Schreibrechte.\nDaher können keine neuen Daten oder Änderungen gespeichert werden.",
						"Keine Schreibrechte...", JOptionPane.ERROR_MESSAGE);
		} 
		else
			config = Configuration.defaultConfig;

		updateFlexibleHolidays(viewedDate.get(java.util.Calendar.YEAR), true, false);
		updateStaticHolidays(false);

		if (!asApplet) {
			/* collect events to notify */
			Vector<Event> events2notify = new Vector<Event>();
			
			/*
			 * Remove Updater program if exists
			 */
			new File(UPDATER).delete();
	
			/*
			 * Register parsed events
			 */
			for (Event e : events) {
				long notifyTimer = checkNotification(e);
				if (notifyTimer == 0)
					events2notify.add(e);
				else if (notifyTimer != -1) {
					Timer timer = new Timer(true);
					timer.schedule(new AlarmTask(this, e), notifyTimer);
				}
			}
			gui.updateStatusBar();
			gui.update();
	
			/*
			 * Refresh at midnight (task)
			 */
			Date midnight = new Date(0, 0);
			midnight.add(java.util.Calendar.DAY_OF_MONTH, 1);
			refreshAtMidnight = new Timer(true);
			refreshAtMidnight.schedule(new RefreshTask(gui),
					midnight.minDiff(viewedDate) * 60 * 1000 + 10 * 1000);
			
			singletonThread = new SingletonTask(mainFrame);
			singletonThread.start();
			
			/*
			 * Start auto update after 5 minutes
			 */
			if (config.getAutoUpdate()) {
				autoUpdateTimer = new Timer(true);
				autoUpdateTimer.schedule(new AutoUpdateTask(mainFrame), 5 * 60 * 1000);
			}
			
			/*
			 * Add shutdown hook
			 */
			Runtime.getRuntime().addShutdownHook(new Thread() {
				final File lockFile = new File(getPath(LOCKFILE));
				@Override
				public void run() {
					lockFile.delete();
					gui.shutdown();
					if (singletonThread != null)
						singletonThread.stopit();
					for (AlarmTask at : pendingAlarms)
						at.cancel();
					if (autoUpdateTimer != null);
						autoUpdateTimer.cancel();
				}
			});
			
			/* show table of notifications */
			if (events2notify.size() > 1)
				new TableOfNotifications(this, events2notify);
			else if (events2notify.size() == 1)
				new Notification(this, events2notify.firstElement());
		}
		
		fullyLaunched = true;
	}

	/**
	 * Updates the flexible holidays (such as easter). They always pend on the
	 * year.
	 * @param year - Year of interest
	 * @param force <li>false for browsing in different years (no settings changes, 
	 * 		no new notifications)<li>true for complete update (settings changes,
	 * 		running notifications get lost, new notifications may occur)
	 * @param notify - true if notifications to be launched (if applicable), false
	 * 		for silent adding
	 */
	public void updateFlexibleHolidays(int year, boolean force, boolean notify) {
		Vector<Event> holidays = new Vector<Event>();
		int thisYear = new Date().get(java.util.Calendar.YEAR);

		/*
		 * Get all flexible holidays
		 */
		for (Event e : events)
			if ((e.isHoliday() || e.isSpecial()) && e.getFrequency() == Frequency.OCCUR_ONCE)
				holidays.add(e);

		if (force)
			/*
			 * Remove all flexible holidays
			 */
			for (Event e : holidays)
				events.remove(e);
		
		else {
			/*
			 * Remove all flexible holidays of years other than @year
			 */
			for (Event e : holidays) 
				if (e.getDate().get(java.util.Calendar.YEAR) != thisYear)
					events.remove(e);
			
			if (year == thisYear)
				return;
		}
		
		/* collect holiday events to add */
		holidays.removeAllElements();

		/*
		 * Recalculate, based on easter Sunday and 4th advent
		 */
		Date ostersonntag = Ostersonntag(year);
		Date advent4 = new Date(year, java.util.Calendar.DECEMBER, 24);
		while (advent4.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.SUNDAY)
			advent4.add(java.util.Calendar.DAY_OF_MONTH, -1);

		if ((config.getHolidays() & Holidays.GRDO) == Holidays.GRDO) {
			Date gruendonnerstag = (Date) ostersonntag.clone();
			gruendonnerstag.add(java.util.Calendar.DAY_OF_MONTH, -3);
			holidays.add(new Event(gruendonnerstag, "Gründonnerstag", true));
		}
		if ((config.getHolidays() & Holidays.KARFR) == Holidays.KARFR) {
			Date karfreitag = (Date) ostersonntag.clone();
			karfreitag.add(java.util.Calendar.DAY_OF_MONTH, -2);
			holidays.add(new Event(karfreitag, "Karfreitag", true));
		}
		if ((config.getHolidays() & Holidays.OSTERMO) == Holidays.OSTERMO) {
			Date ostermontag = (Date) ostersonntag.clone();
			ostermontag.add(java.util.Calendar.DAY_OF_MONTH, 1);
			holidays.add(new Event(ostermontag, "Ostermontag", true));
		}
		if ((config.getHolidays() & Holidays.CHRHIMMELF) == Holidays.CHRHIMMELF) {
			Date christihimmelfahrt = (Date) ostersonntag.clone();
			christihimmelfahrt.add(java.util.Calendar.DAY_OF_MONTH, 39);
			holidays.add(new Event(christihimmelfahrt, "Christihimmelfahrt", true));
		}
		if ((config.getHolidays() & Holidays.PFINGSTMO) == Holidays.PFINGSTMO) {
			Date pfingstmontag = (Date) ostersonntag.clone();
			pfingstmontag.add(java.util.Calendar.DAY_OF_MONTH, 50);
			holidays.add(new Event(pfingstmontag, "Pfingstmontag", true));
		}
		if ((config.getHolidays() & Holidays.FRONLEICH) == Holidays.FRONLEICH) {
			Date fronleichnam = (Date) ostersonntag.clone();
			fronleichnam.add(java.util.Calendar.DAY_OF_MONTH, 60);
			holidays.add(new Event(fronleichnam, "Fronleichnam", true));
		}
		if ((config.getHolidays() & Holidays.BUBT) == Holidays.BUBT) {
			Date bussundbettag = new Date(year, java.util.Calendar.NOVEMBER, 22);
			while (bussundbettag.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.WEDNESDAY)
				bussundbettag.add(java.util.Calendar.DAY_OF_MONTH, -1);
			holidays.add(new Event(bussundbettag, "Buß- und Bettag", true));
		}
		
		if ((config.getSpecialDays() & Holidays.ROSENM) == Holidays.ROSENM) {
			Date rosenmontag = (Date) ostersonntag.clone();
			rosenmontag.add(java.util.Calendar.DAY_OF_MONTH, -48);
			holidays.add(new Event(rosenmontag, "Rosenmontag", false));
		}
		if ((config.getSpecialDays() & Holidays.FASCHING) == Holidays.FASCHING) {
			Date faschingsdienstag = (Date) ostersonntag.clone();
			faschingsdienstag.add(java.util.Calendar.DAY_OF_MONTH, -47);
			holidays.add(new Event(faschingsdienstag, "Faschingsdienstag", false));
		}
		if ((config.getSpecialDays() & Holidays.ASCHERM) == Holidays.ASCHERM) {
			Date aschermittwoch = (Date) ostersonntag.clone();
			aschermittwoch.add(java.util.Calendar.DAY_OF_MONTH, -46);
			holidays.add(new Event(aschermittwoch, "Aschermittwoch", false));
		}
		if ((config.getSpecialDays() & Holidays.PALMS) == Holidays.PALMS) {
			Date palmsonntag = (Date) ostersonntag.clone();
			palmsonntag.add(java.util.Calendar.DAY_OF_MONTH, -7);
			holidays.add(new Event(palmsonntag, "Palmsonntag", false));
		}
		if ((config.getSpecialDays() & Holidays.MUTTER) == Holidays.MUTTER) {
			Date muttertag = new Date(year, java.util.Calendar.MAY, 1);
			while (muttertag.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.SUNDAY)
				muttertag.add(java.util.Calendar.DAY_OF_MONTH, 1);
			muttertag.add(java.util.Calendar.DAY_OF_MONTH, 7);
			holidays.add(new Event(muttertag, "Muttertag", false));
		}
		if ((config.getSpecialDays() & Holidays.VOLKSTRAUER) == Holidays.VOLKSTRAUER) {
			Date volkstrauertag = (Date) advent4.clone();
			volkstrauertag.add(java.util.Calendar.DAY_OF_MONTH, -35);
			holidays.add(new Event(volkstrauertag, "Volkstrauertag", false));
		}
		if ((config.getSpecialDays() & Holidays.TOTENS) == Holidays.TOTENS) {
			Date totensonntag = (Date) advent4.clone();
			totensonntag.add(java.util.Calendar.DAY_OF_MONTH, -28);
			holidays.add(new Event(totensonntag, "Totensonntag", false));
		}
		if ((config.getSpecialDays() & Holidays.ADV1) == Holidays.ADV1) {
			Date advent1 = (Date) advent4.clone();
			advent1.add(java.util.Calendar.DAY_OF_MONTH, -21);
			holidays.add(new Event(advent1, "1. Advent", false));
		}
		if ((config.getSpecialDays() & Holidays.ADV2) == Holidays.ADV2) {
			Date advent2 = (Date) advent4.clone();
			advent2.add(java.util.Calendar.DAY_OF_MONTH, -14);
			holidays.add(new Event(advent2, "2. Advent", false));
		}
		if ((config.getSpecialDays() & Holidays.ADV3) == Holidays.ADV3) {
			Date advent3 = (Date) advent4.clone();
			advent3.add(java.util.Calendar.DAY_OF_MONTH, -7);
			holidays.add(new Event(advent3, "3. Advent", false));
		}
		if ((config.getSpecialDays() & Holidays.ADV4) == Holidays.ADV4)
			holidays.add(new Event(advent4, "4. Advent", false));
		
		for (Event e : holidays)
			if (notify)
				newEvent(e, false);
			else
				events.add(e);
		
		if (gui != null)
			gui.updateStatusBar();
	}

	/**
	 * Updates the static holidays (such as Christmas...).
	 * To be called in case of settings changes only.
	 * @param notify - true if notifications to be launched (if applicable), false
	 * 		for silent adding
	 */
	public void updateStaticHolidays(boolean notify) {
		Vector<Event> holidays = new Vector<Event>();
		int year = viewedDate.get(java.util.Calendar.YEAR);

		/*
		 * Get all static holidays
		 */
		for (Event e : events)
			if ((e.isHoliday() || e.isSpecial()) && Frequency.isY(e.getFrequency()))
				holidays.add(e);

		/*
		 * Remove all static holidays
		 */
		for (Event e : holidays)
			events.remove(e);
		
		/* collect holiday events to add */
		holidays.removeAllElements();

		if ((config.getHolidays() & Holidays.NEUJAHR) == Holidays.NEUJAHR)
			holidays.add(new Event(new Date(year, java.util.Calendar.JANUARY, 1),
					"Neujahr", Frequency.OCCUR_YEARLY, true));
		if ((config.getHolidays() & Holidays.HL3K) == Holidays.HL3K)
			holidays.add(new Event(new Date(year, java.util.Calendar.JANUARY, 6),
					"Heilige 3 Könige", Frequency.OCCUR_YEARLY, true));
		if ((config.getHolidays() & Holidays.TDA) == Holidays.TDA)
			holidays.add(new Event(new Date(year, java.util.Calendar.MAY, 1),
					"Tag der Arbeit", Frequency.OCCUR_YEARLY, true));
		if ((config.getHolidays() & Holidays.MHIMMELF) == Holidays.MHIMMELF)
			holidays.add(new Event(new Date(year, java.util.Calendar.AUGUST, 15),
					"Mariä Himmelfahrt", Frequency.OCCUR_YEARLY, true));
		if ((config.getHolidays() & Holidays.TDDE) == Holidays.TDDE)
			holidays.add(new Event(new Date(year, java.util.Calendar.OCTOBER, 3),
					"Tag der deutschen Einheit", Frequency.OCCUR_YEARLY, true));
		if ((config.getHolidays() & Holidays.REFORM) == Holidays.REFORM)
			holidays.add(new Event(new Date(year, java.util.Calendar.OCTOBER, 31),
					"Reformationstag", Frequency.OCCUR_YEARLY, true));
		if ((config.getHolidays() & Holidays.ALLERH) == Holidays.ALLERH)
			holidays.add(new Event(new Date(year, java.util.Calendar.NOVEMBER, 1),
					"Allerheiligen", Frequency.OCCUR_YEARLY, true));
		if ((config.getHolidays() & Holidays.WEIH1) == Holidays.WEIH1)
			holidays.add(new Event(new Date(year, java.util.Calendar.DECEMBER, 25),
					"1. Weihnachtsfeiertag", Frequency.OCCUR_YEARLY, true));
		if ((config.getHolidays() & Holidays.WEIH2) == Holidays.WEIH2)
			holidays.add(new Event(new Date(year, java.util.Calendar.DECEMBER, 26),
					"2. Weihnachtsfeiertag", Frequency.OCCUR_YEARLY, true));
		
		if ((config.getSpecialDays() & Holidays.VALENTIN) == Holidays.VALENTIN)
			holidays.add(new Event(new Date(year, java.util.Calendar.FEBRUARY, 14),
					"Valentinstag", Frequency.OCCUR_YEARLY, false));
		if ((config.getSpecialDays() & Holidays.FRAUEN) == Holidays.FRAUEN)
			holidays.add(new Event(new Date(year, java.util.Calendar.MARCH, 8),
					"Frauentag", Frequency.OCCUR_YEARLY, false));
		if ((config.getSpecialDays() & Holidays.KINDER) == Holidays.KINDER)
			holidays.add(new Event(new Date(year, java.util.Calendar.JUNE, 1),
					"Kindertag", Frequency.OCCUR_YEARLY, false));
		if ((config.getSpecialDays() & Holidays.HALLOWEEN) == Holidays.HALLOWEEN)
			holidays.add(new Event(new Date(year, java.util.Calendar.OCTOBER, 31),
					"Halloween", Frequency.OCCUR_YEARLY, false));
		if ((config.getSpecialDays() & Holidays.MARTIN) == Holidays.MARTIN)
			holidays.add(new Event(new Date(year, java.util.Calendar.NOVEMBER, 11),
					"Martinstag", Frequency.OCCUR_YEARLY, false));
		if ((config.getSpecialDays() & Holidays.NIKO) == Holidays.NIKO)
			holidays.add(new Event(new Date(year, java.util.Calendar.DECEMBER, 6),
					"Nikolaus", Frequency.OCCUR_YEARLY, false));
		if ((config.getSpecialDays() & Holidays.HEILIGA) == Holidays.HEILIGA)
			holidays.add(new Event(new Date(year, java.util.Calendar.DECEMBER, 24),
					"Heilig Abend", Frequency.OCCUR_YEARLY, false));
		if ((config.getSpecialDays() & Holidays.SILVESTER) == Holidays.SILVESTER)
			holidays.add(new Event(new Date(year, java.util.Calendar.DECEMBER, 31),
					"Silvester", Frequency.OCCUR_YEARLY, false));
		
		for (Event e : holidays)
			if (notify)
				newEvent(e, false);
			else
				events.add(e);
		
		if (gui != null)
			gui.updateStatusBar();
	}

	/**
	 * Calculate easter Sunday. Use Lichtenberg. Do not use Gauss.
	 * @param year - Year of interest
	 * @return Date of easter Sunday.
	 */
	public static Date Ostersonntag(int year) {
		Date easter = new Date(year, java.util.Calendar.MARCH, 1);
		easter.add(java.util.Calendar.DAY_OF_MONTH, Math.easterSunday(year));
		return easter;
	}

	/**
	 * 
	 * @return List of all events.
	 */
	public Vector<Event> getAllEvents() {
		return events;
	}

	/**
	 * 
	 * @return Currently viewed date.
	 */
	public Date getViewedDate() {
		return viewedDate;
	}

	/**
	 * Set the currently viewed date.
	 * @param x - Date to jump to
	 */
	public void setViewedDate(Date x) {
		this.viewedDate = x;
	}

	/**
	 * 
	 * @return List of running alarm tasks.
	 */
	public Vector<AlarmTask> getAlarmTasks() {
		return pendingAlarms;
	}

	/**
	 * Add an alarm task to the list of running alarm tasks.
	 * If there is already a task with for the same event, this
	 * task will be replaced.
	 * @param x - AlarmTask to add
	 */
	public void addAlarmTask(AlarmTask x) {
		Logger.debug("[addPendingEvent] " + x.getEvent().getName());
		if (pendingAlarms.contains(x))
			return;
		
		/* remove duplicate alarm task */
		int i, size = pendingAlarms.size();
		for (i = 0; i < size; i++)
			if (pendingAlarms.get(i).getEvent().equals(x.getEvent())) {
				pendingAlarms.get(i).cancel();
				pendingAlarms.remove(i);
				gui.putMessage("Wecker für \"" + x.getEvent().getName() + "\" wurde geändert.");
				break;
			}
		
		if (i == size)
			gui.putMessage("Wecker für \"" + x.getEvent().getName() + "\" wurde gestellt.");
		
		pendingAlarms.add(x);
		gui.updateStatusBar();
	}

	/**
	 * Removes an alarm task from the list of running alarm tasks.
	 * DOES NOT CANCEL THE TASK.
	 * @param x - AlarmTask to remove
	 */
	public void removeAlarmTask(AlarmTask x) {
		pendingAlarms.remove(x);
		gui.updateStatusBar();
	}

	public static void main(String[] args) {
		/* use default dimension */
		int width = -1, height = -1; 
			
		/* use default view */
		byte view = Configuration.VIEW_DEFAULT;
		
		/* use a specific working directory */
		String workspaceArg = new File("").getAbsolutePath();
		
		/*
		 * Parse command line parameters
		 */
		for (String s : args)
			if (s.equals("--maximized")) {
				width = 0;
				height = 0;
			} 
			else if (s.equals("--view=YEAR"))
				view = Configuration.VIEW_YEAR;
			else if (s.equals("--view=MONTH"))
				view = Configuration.VIEW_MONTH;
			else if (s.equals("--view=WEEK"))
				view = Configuration.VIEW_WEEK;
			else if (s.equals("--view=DAY"))
				view = Configuration.VIEW_DAY;
			else if (s.startsWith("--workspace=")) {
				workspaceArg = s.substring(12);
				File test = new File(workspaceArg);
				if (!test.exists() || !test.isDirectory()) {
					System.err.println("workspace \"" + workspaceArg + "\" is no valid directory!");
					System.exit(1);
				}
			}
			else if (s.equals("--version")) {
				versioninfo();
				System.exit(0);
			} else if (s.startsWith("--size=")) {
				String w = s.substring(7, s.lastIndexOf("x"));
				String h = s.substring(s.lastIndexOf("x") + 1, s.length());
				try {
					width = Integer.parseInt(w);
					height = Integer.parseInt(h);
				} catch (NumberFormatException n) {
					System.err.println("Cannot parse \"" + s + "\"");
					System.err.println("Use interges only!");
					System.exit(1);
				}
			} else {
				System.err.println("Unsupported option \"" + s + "\"");
				usage();
				System.exit(1);
			}
		cmdArgs = args;
		
		/*
		 * Check if lock file exists
		 */
		if (new File(workspaceArg + File.separator + LOCKFILE).exists()) {
			/*
			 * Call running calendar to appear
			 */
			Logger.log("Lockfile exists. Already started?");
			File maxfile = new File(workspaceArg + File.separator + MAXIMIZEFILE);
			try {
				Logger.debug("create file to call running calendar");
				maxfile.createNewFile();
				Thread.sleep(2000);
				
				if (!maxfile.exists()) {
					/*
					 * Exit if running calendar answers
					 */
					Logger.debug("calendar was is already running. Exit.");
					return;
				}
				else {
					Logger.debug("calendar is not responding. Launching new.");
					maxfile.delete();
				}
			} catch (IOException e) {
				Logger.error("problems with the lock file: "+e.toString());
			} catch (InterruptedException e) {
				Logger.warn("sleep interrupted: "+e.toString());
			}
		}

		/*
		 * Case: no lock file found or running calendar does not answer
		 */
		new Calendar(new Dimension(width, height), view, false, workspaceArg);
	}

	/**
	 * Print version information on stdout.
	 */
	private static void versioninfo() {
		System.out.println(FILENAME);
		System.out.println("Version " + VERSION);
		System.out.println();
	}

	/**
	 * Print usage information on stdout.
	 */
	private static void usage() {
		System.out.println("Calendar command line options:");
		System.out.println();
		System.out.println("--maximized                   Start calendar maximized");
		System.out.println("--workspace=[path]            Start calendar with a certain working directory");
		System.out.println("--view=[YEAR|MONTH|WEEK|DAY]  Start calendar with a specified view");
		System.out.println("--size=WIDTHxHEIGHT           Start calendar with size WIDTH and HEIGHT");
		System.out.println("--version                     Print version on stdout and exit");
		System.out.println();
		System.out.println("Call \"java -jar " + FILENAME + " [options]\" to start calendar with options.");
		System.out.println("Or simply doubleclick to start without options.");
	}

	/**
	 * User did a selection of one or more dates.<br>
	 * 2 cases:
	 * <br>(1) dates are connected to each other without gaps 
	 * <br>(2) randomly chosen dates TODO
	 * @param dates - List of selected dates
	 * @param connected - True if selected dates are in a row, false
	 * 		if randomly chosen dates (with gaps) 
	 */
	public void newSelection(Vector<Date> dates, boolean connected) {
		Logger.debug("New selection: " + dates.size() + " dates");
		
		for (Date d : dates)
			Logger.debug("selected date: "+d.dateToString(true));
		
		/*
		 * Short cut if one date only
		 */
		if (dates.size() == 1) {
			new TableOfEvents(dates.firstElement(), this, false);
			return;
		}
		
		if (appletMode) {
			gui.getApplet().newSelection();
			return;
		}

		if (connected) {
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
			new EditEvent(this, first, last);
		}
		else
			new EditEvent(this, dates);
	}
	
	/**
	 * User did a selection of an event. This will launch a new
	 * notification.
	 * @param e - Selected event
	 */
	public void newSelection(Event e) {
		if (appletMode)
			gui.getApplet().newSelection();
		else
			new Notification(this, e);
	}
	
	/**
	 * User did a selection of a column in monthly view. This
	 * will launch the EditEvent frame with a new weekly event.
	 * @param start - Start date for weekly event
	 */
	public void newWeeklySelection(Date start, boolean monthly) {
		new EditEvent(this, new Event(start, "", monthly ? 
				Frequency.OCCUR_WEEKLY | Frequency.OCCUR_MONTHLY : Frequency.OCCUR_WEEKLY, -1));
	}

	/**
	 * Alters an existing event, references by its ID.
	 * @param oldID - ID of the event to edit
	 * @param newEvent - New event information to save
	 */
	public void editEvent(int oldID, Event newEvent) {
		Logger.debug("EDIT EVENT: ID=" + oldID + " new name="
				+ newEvent.getName() + " remind=" + newEvent.getRemind());
		
		/* 
		 * Avoid corruption if not yet fully launched 
		 */
		if (!fullyLaunched) {
			Logger.warn("edit requested BUT application NOT FULLY LAUNCHED!!!");
			JOptionPane.showMessageDialog(gui.getFrame(), 
					"Änderungen können jetzt nicht vorgenommen werden, da der Kalender noch nicht komplett gestartet ist.", 
					"Änderung noch nicht möglich", JOptionPane.WARNING_MESSAGE);
			return;
		}

		/*
		 * Simply add new event if ID equals -1
		 */
		if (oldID == -1) {
			newEvent(newEvent, true);
			return;
		}

		/*
		 * Remove old event, copy the ID, add new event
		 */
		Event oldEvent = getEventByID(oldID);
		events.remove(oldEvent);
		newEvent.setID(oldID);
		events.add(newEvent);

		/*
		 * Update alarm task if exists
		 */
		for (AlarmTask a : pendingAlarms)
			if (a.getEvent().equals(oldEvent))
				a.setEvent(newEvent);

		Logger.debug("old event was: " + oldEvent.getName());
		gui.update();
		gui.putMessage("Ereignis \"" + oldEvent.getName() + "\" wurde bearbeitet");
		Logger.debug("new date=" + newEvent.getDate().dateToString(true));

		save();
	}

	/**
	 * Register a new event.
	 * @param event - New event to add. Automatically checks if this event is
	 * 		close enough to launch a notification
	 * @param saveAfter <li>true if changes to be written
	 * 		<li>false if no writing (will be used on startup)
	 */
	public void newEvent(Event event, boolean saveAfter) {
		if (event == null)
			return;
		
		if (appletMode) {
			events.add(event);
			return;
		}

		Logger.debug("NEW EVENT: " + event.getDate().dateToString(true)
				+ " -> " + event.getName() + " (" + event.isHoliday() + ")");

		/*
		 * Does this event already exist?
		 */
		for (Event e : events)
			if (e.match(event.getDate()) && e.getName().equals(event.getName()))
				if (JOptionPane.showConfirmDialog(gui.getFrame(),
						"Es gibt bereits ein Ereignis \"" + e.getName() + "\" am " + e.getDate().dateToString(false) + ".\nTrotzdem hinzufügen?", 
						"Doppeltes Ereignis...", JOptionPane.YES_NO_OPTION, 
						JOptionPane.PLAIN_MESSAGE, null) 
						== JOptionPane.YES_OPTION)
					return;

		if (!event.isHoliday() && !event.isSpecial()) {
			/*
			 * All non-holiday events get an ID
			 */
			if (event.getID() == -1)
				event.setID(genID());
			Logger.debug(" ID=" + event.getID());
		}

		events.add(event);
		gui.update();
		gui.updateStatusBar();
		if (!event.isHoliday() && !event.isSpecial())
			gui.putMessage("Ereignis \"" + event.getName() + "\" wurde hinzugefügt");

		/*
		 * If time to wait matches the config, launch a new notification
		 */
		long notifyTimer = checkNotification(event);
		if (notifyTimer == 0)
			new Notification(this, event);
		else if (notifyTimer != -1) {
			Timer timer = new Timer(true);
			timer.schedule(new AlarmTask(this, event), notifyTimer);
		}

		if (saveAfter)
			save();
	}

	/**
	 * Unregister a specified event. 
	 * @param e - Event to remove
	 * @return True if the event really was deleted.
	 */
	public boolean deleteEvent(Event e) {
		Logger.debug("REMOVE EVENT: " + e.getDate().dateToString(true)
				+ " -> " + e.getName());

		if (!events.contains(e)) {
			Logger.error("NO SUCH EVENT TO REMOVE: " + e.getName());
			return false;
		}
		
		if (appletMode) {
			events.remove(e);
			return true;
		}
		
		/* 
		 * Avoid corruption if not yet fully launched 
		 */
		if (!fullyLaunched) {
			Logger.warn("deletion requested BUT application NOT FULLY LAUNCHED!!!");
			JOptionPane.showMessageDialog(gui.getFrame(), 
					"Änderungen können jetzt nicht vorgenommen werden, da der Kalender noch nicht komplett gestartet ist.", 
					"Änderung noch nicht möglich", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		/*
		 * for regular events: also delete notes and attachments
		 */
		if (!e.isHoliday() && !e.isSpecial()) {
			
			/*
			 * Warn in case of frequent events.
			 */
			if (e.getFrequency() != Frequency.OCCUR_ONCE)
				if (JOptionPane.showConfirmDialog(gui.getFrame(), 
						"Es soll das regelmäßige Ereignis \"" + e.getName() + "\" gelöscht werden.\nDas Ereignis wird dann nicht nur an diesem Tag, sondern überall gelöscht.\nFortfahren?",
						"Löschen von \"" + e.getName() + "\"...", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
					return false;
			
			/*
			 * Warn in case of multi-day events.
			 */
			if (e.getEndDate() != null) {
				if (JOptionPane.showConfirmDialog(gui.getFrame(),
						"Das Ereignis \"" + e.getName()
						+ "\" erstreckt sich über mehrere Tage.\nSoll wirklich das komplette Ereignis gelöscht werden?",
						"Löschen von \"" + e.getName() + "\"", 
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return false;						
			}

			/*
			 * Warn if there are notes to delete.
			 */
			if (!e.getNotes(workspace).equals("")) {
				if (JOptionPane.showConfirmDialog(gui.getFrame(),
						"Sollen auch die Notizen zu diesem Ereignis geloscht werden?",
						"\"" + e.getName() + "\": Löschen der Notizen", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
					return false;
				new File(getPath(EVENT_DIR) + File.separator + e.getID()
						+ File.separator + NOTES_FILE).delete();
			}

			/*
			 * Warn if there is a real attachment to delete.
			 */
			if (e.getAttachment(workspace) != null && !e.attachmentIsLink(workspace)) {
				if (JOptionPane.showConfirmDialog(gui.getFrame(),
						"Dem Ereignis wurde eine Datei als Kopie angehangen.\nSoll diese Kopie gelöscht werden?",
						"\"" + e.getName() + "\": Löschen des Anhangs", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
					return false;
				e.getAttachment(workspace).delete();
			}

			/*
			 * Delete folder if all warnings have been confirmed.
			 */
			File folder = new File(getPath(EVENT_DIR) + File.separator + e.getID());
			if (folder.exists() && folder.isDirectory()) {
				for (File f : folder.listFiles())
					if (!f.delete())
						Logger.warn("WARNING: cannot delete file " + f.getName());
				if (!folder.delete())
					Logger.debug("WARNING: folder for event " + e.getID() + "still exists!");
			}
		}

		/*
		 * Remove this event from the list of events to notify.
		 */
		events.remove(e);
		for (AlarmTask a : pendingAlarms)
			if (a.getEvent().equals(e)) {
				a.cancel();
				pendingAlarms.remove(a);
				break;
			}

		gui.update();
		gui.updateStatusBar();
		gui.putMessage("Ereignis \"" + e.getName() + "\" wurde gelöscht");

		save();
		return true;
	}

	/**
	 * Saves all settings and events to the default xml-file.
	 */
	public void save() {
		save(this.events, this.config, getPath(XMLFILE));
	}

	/**
	 * Writes events and settings to the file specified.
	 * @param v - List of events to save
	 * @param c - Configuration (settings) to save
	 * @param filename - File to write
	 */
	public void save(Vector<Event> v, Configuration c, String filename) {
		Logger.log("SAVE");
		
		/* 
		 * Avoid corruption if not yet fully launched 
		 */
		if (!fullyLaunched) {
			Logger.warn("save requested BUT application NOT FULLY LAUNCHED!!!");
			JOptionPane.showMessageDialog(gui.getFrame(), 
					"Änderungen können jetzt nicht abgespeichert werden, da der Kalender noch nicht komplett gestartet ist.", 
					"Speichern wird verhindert", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(filename)), ENCODING));

			/*
			 * Write XML header
			 */
			out.write("<?xml version=\"1.0\" encoding=\"" + ENCODING
					+ "\" standalone=\"yes\"?>\n<Calendar version=\"" + VERSION
					+ "\">\n");

			/*
			 * Write config section
			 */
			if (!c.equals(Configuration.defaultConfig))
				c.write(out);

			/*
			 * Write events section
			 */
			out.write("  <Events>\n");
			for (Event event : v)
				if (event.getID() != -1)
					event.write(out);

			/*
			 * Write XML trailer
			 */
			out.write("  </Events>\n</Calendar>");
			out.close();
		} catch (Exception e) {
			Logger.error("cannot save: "+e.toString());
		}
	}

	/**
	 * 
	 * @return Current configuration
	 */
	public Configuration getConfig() {
		return this.config;
	}
	
	/**
	 * Apply a new configuration.
	 * @param x - Settings to activate
	 */
	public void setConfig(Configuration x) {
		Configuration old = this.config;
		this.config = x;
		if (old.getHolidays() != x.getHolidays() || old.getSpecialDays() != x.getSpecialDays()) {
			updateFlexibleHolidays(this.viewedDate.get(java.util.Calendar.YEAR), true, true);
			updateStaticHolidays(true);
		}
		if (old.getAutoUpdate() && !x.getAutoUpdate())
			autoUpdateTimer.cancel();
		else if (!old.getAutoUpdate() && x.getAutoUpdate()) {
			autoUpdateTimer = new Timer(true);
			autoUpdateTimer.schedule(new AutoUpdateTask(gui.getFrame()), 60 * 1000);
		}
		gui.getFrame().setButtonPanelColor(x.getColors()[ColorSet.CONTROLPANEL]);
		gui.update();
		gui.putMessage("Einstellungen wurden übernommen");
		save();
	}
	
	/**
	 * 
	 * @param x - Applet to set
	 */
	public void setApplet(CalendarApplet x) {
		gui = x;
	}

	/**
	 * 
	 * @return True if this is the very first launch
	 */
	public boolean isFirstStartup() {
		return this.firstStartup;
	}

	/**
	 * Find an empty ID.
	 * @return unused ID
	 */
	public int genID() {
		int ID = 0;
		for (int i = 0; i < events.size(); i++)
			if (ID == events.elementAt(i).getID()) {
				ID++;
				i = 0;
			}
		return ID;
	}

	/**
	 * Returns the event with a specific ID.
	 * @param ID - ID
	 * @return Event
	 */
	public Event getEventByID(int ID) {
		for (Event e : events)
			if (e.getID() == ID)
				return e;
		return null;
	}
	
	/**
	 * Returns the event with a specific ID and name.
	 * @param ID - ID
	 * @param name - Name
	 * @return Event
	 */
	public Event getEventByIDAndName(int ID, String name) {
		for (Event e : events)
			if (e.getID() == ID && e.getName().equals(name))
				return e;
		return null;
	}

	/**
	 * Returns the Calendar GUI object of this calendar.
	 * Can be implemented by either a CalendarFrame or a CalendarApplet.
	 * @return CalendarGUI object
	 */
	public CalendarGUI getGUI() {
		return gui;
	}
	
	/**
	 * 
	 * @return Number of non-holiday events.
	 */
	public int getNumberOfEvents() {
		int num = 0;
		for (Event e : events)
			if (!e.isHoliday() && !e.isSpecial())
				num++;
		return num;
	}
	
	/**
	 * Returns the path of a file or directory with the current
	 * working directory.
	 * @param file - File or directory to get
	 * @return Path of the file within the working directory.
	 */
	public String getPath(String file) {
		return workspace + File.separator + file;
	}
	
	/**
	 * 
	 * @return The current working directory.
	 */
	public String getWorkspace() {
		return workspace;
	}
	
	/**
	 * 
	 * @return Command line arguments the calendar was started with.
	 */
	public String[] getArgs() {
		return cmdArgs;
	}

	/**
	 * Adds a notification to the list of notifications currently displayed.
	 * @param x - Notification to add
	 */
	public void addCurrentNoti(Notification x) {
		if (notis.size() == 0)
			gui.getFrame().startChangeIcon();

		if (!notis.contains(x))
			notis.add(x);
	}

	/**
	 * Removes a notification from the list of notifications currently
	 * displayed.
	 * @param x - Notification to unregister
	 */
	public void removeCurrentNoti(Notification x) {
		notis.remove(x);

		if (notis.size() == 0)
			gui.getFrame().stopChangeIcon();
	}
	
	/**
	 * 
	 * @return Number of currently displayed notification frames.
	 */
	public int getNotificationSize() {
		return notis.size();
	}

	/**
	 * Checks if an event is close enough for notification.
	 * @param event - Event of interest
	 * @return Time in milliseconds to wait before a notification
	 * 		is to be launched. 0 for immediate notification. -1
	 * 		for no notification at all.
	 */
	private long checkNotification(Event event) {
		int dayDiff = (int) event.getNextDate().dayDiff(new Date());
		int minDiff = -1;
		if (dayDiff == 0)
			minDiff = (int) event.getNextDate().minDiff(new Date());

		int remind = event.getRemind();
		if (remind == Configuration.defaultConfig.getReminder()) {
			Logger.debug("remind=DEFAULT");
			remind = this.getConfig().getReminder();
		}

		Logger.debug("remind=" + remind);

		/*
		 * If no holiday: always notify if event is in past
		 */
		if (dayDiff < 0) {
			if (!event.isHoliday() && !event.isSpecial())
				return 0;
		}
		else if (remind >= Event.REMIND_NOW && remind <= Event.REMIND_15MIN) {
			if (minDiff <= 5 * (remind - Event.REMIND_NOW))
				return 0;
			else if (dayDiff <= 1)
				return 1000 * 60 * minDiff - 5 * (remind - Event.REMIND_NOW);
		} 
		else if (remind == Event.REMIND_30MIN) {
			if (minDiff <= 30)
				return 0;
			else if (dayDiff <= 1)
				return 1000 * 60 * minDiff - 30;
		} 
		else if (remind >= Event.REMIND_1H && remind <= Event.REMIND_5H) {
			if (minDiff <= 60 * (remind - Event.REMIND_1H + 1))
				return 0;
			else if (dayDiff <= 1)
				return 1000 * 60 * minDiff - 60 * (remind - Event.REMIND_1H + 1);
		} 
		else if (remind >= Event.REMIND_1D && remind <= Event.REMIND_1W) {
			if (dayDiff <= remind - Event.REMIND_1D + 1)
				return 0;
			else if (dayDiff == remind - Event.REMIND_1D + 2) {
				Date test = new Date(0, 0);
				test.add(java.util.Calendar.DAY_OF_MONTH, remind - Event.REMIND_1D + 1);
				return 1000 * 60 * test.minDiff(new Date());
			}
		} 
		else if (remind == Event.REMIND_10D) {
			if (dayDiff <= 10)
				return 0;
			else if (dayDiff == 11) {
				Date test = new Date(0, 0);
				test.add(java.util.Calendar.DAY_OF_MONTH, 10);
				return 1000 * 60 * test.minDiff(new Date());
			}
		} 
		else if (remind == Event.REMIND_2W || remind == Event.REMIND_3W) {
			if (dayDiff <= 7 * (remind - Event.REMIND_2W + 2))
				return 0;
			else if (dayDiff == 7 * (remind - Event.REMIND_2W + 2) + 1) {
				Date test = new Date(0, 0);
				test.add(java.util.Calendar.DAY_OF_MONTH, 7 * (remind - Event.REMIND_2W + 2));
				return 1000 * 60 * test.minDiff(new Date());
			}
		} 
		else if (remind >= Event.REMIND_1M && remind <= Event.REMIND_3M) {
			Date test = (Date) event.getDate().clone();
			test.add(java.util.Calendar.MONTH, -(remind - Event.REMIND_1M + 1));
			test.set(java.util.Calendar.HOUR_OF_DAY, 0);
			test.set(java.util.Calendar.MINUTE, 0);
			long dayDiff2 = test.dayDiff(new Date());
			if (dayDiff2 <= 0)
				return 0;
			else if (dayDiff2 == remind - Event.REMIND_1M + 1)
				return 1000 * 60 * test.minDiff(new Date());
		}
		
		return -1;
	}

	/**
	 * Hides all notifications currently displayed.
	 * @param visible - True for visible. False for hiding.
	 */
	public void setNotisVisible(boolean visible) {
		for (Notification n : notis)
			n.setVisible(visible);
	}
}

/**
 * Refresh calendar canvas.
 * @author Johannes Steltzer
 *
 */
class RefreshTask 
	extends TimerTask 
	implements Runnable {
	
	/** Calendar to refresh */
	private CalendarGUI c;

	/**
	 * Construct a new refresher.
	 * @param c - Calendar to refresh
	 */
	public RefreshTask(CalendarGUI c) {
		super();
		this.c = c;
	}

	@Override
	public void run() {
		c.update();
	}
}
