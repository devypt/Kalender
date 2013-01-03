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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.jsteltze.calendar.config.Configuration;
import de.jsteltze.common.Logger;
import de.jsteltze.common.Trans;

/**
 * Event that takes place on a date.
 * @author Johannes Steltzer
 *
 */
public class Event {
	
	public static final int NUMBER_REMINDS = 24;

	public static final byte NO_REMIND = 0x00;
	public static final byte REMIND_NOW = 0x01;
	public static final byte REMIND_5MIN = 0x02;
	public static final byte REMIND_10MIN = 0x03;
	public static final byte REMIND_15MIN = 0x04;
	public static final byte REMIND_30MIN = 0x05;
	public static final byte REMIND_1H = 0x06;
	public static final byte REMIND_2H = 0x07;
	public static final byte REMIND_3H = 0x08;
	public static final byte REMIND_4H = 0x09;
	public static final byte REMIND_5H = 0x0A;
	public static final byte REMIND_1D = 0x0B;
	public static final byte REMIND_2D = 0x0C;
	public static final byte REMIND_3D = 0x0D;
	public static final byte REMIND_4D = 0x0E;
	public static final byte REMIND_5D = 0x0F;
	public static final byte REMIND_6D = 0x10;
	public static final byte REMIND_1W = 0x11;
	public static final byte REMIND_10D = 0x12;
	public static final byte REMIND_2W = 0x13;
	public static final byte REMIND_3W = 0x14;
	public static final byte REMIND_1M = 0x15;
	public static final byte REMIND_2M = 0x16;
	public static final byte REMIND_3M = 0x17;
	
	public static final byte HOLIDAY_NONE = 0x00;
	public static final byte HOLIDAY_LAW = 0x01;
	public static final byte HOLIDAY_OTHER = 0x02;

	/** name of this event */
	private String name;
	
	/** start date (opt. with time) */
	private Date date;
	
	/** end date (might be null) */
	private Date endDate;
	
	/** frequency */
	private short frequency;
	
	/** holiday / special day / normal event (see HOLIDAY_XXX) */
	private byte holiday;
	
	/** time prior to start to start reminding (see REMIND_XXX) */
	private byte remind;
	
	/** event ID */
	private int ID;
	
	/** is this events selected on the gui? */
	private boolean selected;

	/**
	 * Construct a new non-holiday non-frequent event without 
	 * notes/attachment.<br>
	 * <b>FOR PLAIN EVENTS</b>
	 * @param date - Date of this event
	 * @param name - Name of this event
	 * @param ID - Event ID
	 */
	public Event(Date date, String name, int ID) {
		this(date, name, Frequency.OCCUR_ONCE, ID);
	}

	/**
	 * Construct a new non-holiday multi-day event without 
	 * notes/attachment.
	 * @param startDate - Start date of this event
	 * @param endDate - End date of this event
	 * @param name - Name of this event
	 * @param ID - Event ID
	 */
	public Event(Date startDate, Date endDate, String name, int ID) {
		this(startDate, endDate, name, HOLIDAY_NONE, Frequency.OCCUR_ONCE, 
				Configuration.defaultConfig.getReminder(), ID);
	}

	/**
	 * Construct a new non-frequent event without notes/attachment 
	 * and without ID.<br>
	 * <b>FOR FLEXIBLE HOLIDAYS ONLY!</b>
	 * @param date - Date of this holiday
	 * @param name - Name of this holiday
	 * @param law - True if is this a holiday by law
	 */
	public Event(Date date, String name, boolean law) {
		this(date, name, Frequency.OCCUR_ONCE, law);
	}

	/**
	 * Construct a new holiday event without notes/attachment 
	 * and without ID.<br>
	 * <b>FOR HOLIDAYS ONLY!</b>
	 * @param date - Date of this holiday
	 * @param name - Name of this holiday
	 * @param f - Frequency of this holiday (e.g. yearly)
	 * @param law - True if this is a holiday by law
	 */
	public Event(Date date, String name, short f, boolean law) {
		this(date, null, name, law ? HOLIDAY_LAW : HOLIDAY_OTHER, f, 
				Configuration.defaultConfig.getReminder(), -1);
	}

	/**
	 * Constructs a new frequent event without notes/attachment.<br>
	 * <b>FOR PLAIN FREQUENT EVENTS (e.g. birthdays)</b>
	 * @param date - Date of this event
	 * @param name - Name of this event
	 * @param f - Frequency of this event (e.g. yearly)
	 * @param ID - Event ID
	 */
	public Event(Date date, String name, short f, int ID) {
		this(date, null, name, HOLIDAY_NONE, f, 
				Configuration.defaultConfig.getReminder(), ID);
	}

	/**
	 * Construct a new event.
	 * @param startDate - Start date of this event
	 * @param endDate - End date (might be null in case of single day events)
	 * @param name - Name of this event
	 * @param holiday - Holiday type of this event (see Event.HOLIDAY_XXX)
	 * @param f - Frequency of this event (e.g. yearly)
	 * @param remind - Reminder for this event (see Event.REMIND_XXX)
	 * @param ID - Event ID
	 */
	public Event(Date startDate, Date endDate, String name, byte holiday, 
			short f, byte remind, int ID) {
		this.date = (Date) startDate.clone();
		this.endDate = endDate == null ? null : (Date) endDate.clone();
		this.name = name;
		this.holiday = holiday;
		this.frequency = f;
		this.remind = remind;
		this.ID = ID;
		this.selected = false;
	}

	/**
	 * 
	 * @return This events name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 
	 * @return This events frequency.
	 */
	public short getFrequency() {
		return this.frequency;
	}

	/**
	 * 
	 * @return This events start date.
	 */
	public Date getDate() {
		return this.date;
	}

	/**
	 * 
	 * @return This events end date (might be null in case of single
	 * day events).
	 */
	public Date getEndDate() {
		return this.endDate;
	}

	/**
	 * 
	 * @return True if this is a holiday by law.
	 */
	public boolean isHoliday() {
		return this.holiday == HOLIDAY_LAW;
	}
	
	/**
	 * 
	 * @return True if this is a special day (such as mother day).
	 */
	public boolean isSpecial() {
		return this.holiday == HOLIDAY_OTHER;
	}
	
	/**
	 * 
	 * @return True if this event is currently selected on the gui.
	 */
	public boolean isSelected() {
		return this.selected;
	}
	
	/**
	 * Set the selected flag of this event.
	 * @param x - True for selected state, false otherwise
	 */
	public void setSelected(boolean x) {
		this.selected = x;
	}

	/**
	 * 
	 * @return Event ID.
	 */
	public int getID() {
		return this.ID;
	}

	/**
	 * 
	 * @return Reminder for this event (see Event.REMIND_XXX).
	 */
	public int getRemind() {
		return this.remind;
	}

	/**
	 * Set ID of this event.
	 * @param ID - ID to set
	 */
	public void setID(int ID) {
		this.ID = ID;
	}

	/**
	 * Set start date of this event.
	 * @param x - Date to set
	 */
	public void setDate(Date x) {
		this.date = x;
	}

	/**
	 * Set end date of this event (might be null in case of single day
	 * events).
	 * @param x - Date to set
	 */
	public void setEndDate(Date x) {
		this.endDate = x;
	}
	
	/**
	 * Creates an exact copy of this event.
	 * @return Clone.
	 */
	@Override
	public Event clone() {
		Date start = (Date) this.date.clone();
		Date end = this.endDate == null ? 
				null : (Date) this.endDate.clone();
		String s = this.name;
		byte sp = this.holiday;
		short f = this.frequency;
		byte r = this.remind;
		int ID = this.ID;
		return new Event(start, end, s, sp, f, r, ID);
	}

	/**
	 * Calculate the closest possible date when this event occurs again.<br>
	 * In case of non-frequent events the events date is returned (might
	 * be in the past).<br>In case of frequent events the next date in
	 * future is returned.
	 * @return Next possible date.
	 */
	public Date getNextDate() {
		/* If unique... */
		if (this.frequency == Frequency.OCCUR_ONCE) {
			/* If single date... */
			if (this.endDate == null)
				return this.date;
			
			/* If multi-day event... */
			else {
				Date today = new Date(0, 0);
				today.setHasTime(this.date.hasTime());
				long diff_start = this.date.dayDiff(today);
				long diff_end = this.endDate.dayDiff(today);
				/* today in the middle of event */
				if (diff_start <= 0 && diff_end >= 0) {
					today.set(java.util.Calendar.HOUR, 
							this.date.get(java.util.Calendar.HOUR));
					today.set(java.util.Calendar.MINUTE, 
							this.date.get(java.util.Calendar.MINUTE));
					return today;
				}
				/* today before event */
				else if (diff_start > 0)
					return this.date;
				/* today after event */
				else
					return this.endDate;
			}
		}

		/* else */
		else {
			Date testDate = new Date(0, 0);
			if (this.date.hasTime()) {
				testDate.set(java.util.Calendar.HOUR_OF_DAY,
						this.date.get(java.util.Calendar.HOUR_OF_DAY));
				testDate.set(java.util.Calendar.MINUTE,
						this.date.get(java.util.Calendar.MINUTE));
				testDate.setHasTime(true);
			} 
			else
				testDate.setHasTime(false);
			int daysAdded = 0;

			/*
			 * Check if next date of this event is in future. So add a maximum
			 * of 370 days (1 year) and see if matches.
			 */
			while (true) {
				if (this.match(testDate))
					return testDate;

				testDate.add(java.util.Calendar.DAY_OF_MONTH, 1);
				if (++daysAdded >= 370)
					break;
			}
			testDate.add(java.util.Calendar.DAY_OF_MONTH, -daysAdded);
			daysAdded = 0;

			/*
			 * If looking for future was not successful, last next date MUST be
			 * in the past. So reduce by a maximum of 370 days.
			 */
			while (true) {
				if (this.match(testDate)) {
					return testDate;
				}
				testDate.add(java.util.Calendar.DAY_OF_MONTH, -1);
				if (++daysAdded >= 370)
					break;
			}

			return null; // This is actually impossible
		}
	}

	/**
	 * Check if this event takes place on a specific date.
	 * @param date - Date to check
	 * @return True if this event takes place on the date.
	 */
	public boolean match(Date date) {
		if (this.frequency == Frequency.OCCUR_ONCE) {
			/* Case 1: unique event */
			if (this.getDate().sameDateAs(date))
				return true;
			else if (this.getEndDate() != null) {
				long dayDiffStart = date.dayDiff(this.getDate());
				long dayDiffEnd = date.dayDiff(this.getEndDate());
				/*
				 * dayDiffEnd has to be negative (EndDate is in future) AND
				 * dayDiffStart has to be positive (StartDate is in past)
				 */
				if (dayDiffStart >= 0 && dayDiffEnd <= 0)
					return true;
			}
		}
		else if (Frequency.isByDate(this.frequency)) {
			if (this.frequency == Frequency.OCCUR_YEARLY) {
				/* Case 2: yearly event (eg. birthday) */
				if (this.getDate().get(java.util.Calendar.MONTH) == date
						.get(java.util.Calendar.MONTH)
						&& this.getDate().get(java.util.Calendar.DAY_OF_MONTH) == date
								.get(java.util.Calendar.DAY_OF_MONTH))
					return true;
			} 
			else if (this.frequency == Frequency.OCCUR_MONTHLY) {
				/* Case 3: event each month, but only this year */
				if (this.getDate().get(java.util.Calendar.YEAR) == date
						.get(java.util.Calendar.YEAR)
						&& this.getDate().get(java.util.Calendar.DAY_OF_MONTH) == date
								.get(java.util.Calendar.DAY_OF_MONTH))
					return true;
			} 
			else if (this.frequency == (Frequency.OCCUR_MONTHLY | Frequency.OCCUR_YEARLY)) {
				/* Case 4: monthly event every year */
				if (this.getDate().get(java.util.Calendar.DAY_OF_MONTH) == date
						.get(java.util.Calendar.DAY_OF_MONTH))
					return true;
			} 
			else if (this.frequency == Frequency.OCCUR_WEEKLY) {
				/* Case 5: Weekly event but this month only */
				if (this.getDate().get(Date.DAY_OF_WEEK) == date
						.get(Date.DAY_OF_WEEK)
						&& this.getDate().get(Date.MONTH) == date.get(Date.MONTH)
						&& this.getDate().get(Date.YEAR) == date.get(Date.YEAR))
					return true;
			} 
			else if (this.frequency == (Frequency.OCCUR_MONTHLY | Frequency.OCCUR_WEEKLY)) {
				/* Case 6: Weekly event for the whole year */
				if (this.getDate().get(Date.DAY_OF_WEEK) == date
						.get(Date.DAY_OF_WEEK)
						&& this.getDate().get(Date.YEAR) == date.get(Date.YEAR))
					return true;
			} 
			else if (this.frequency == (Frequency.OCCUR_YEARLY | Frequency.OCCUR_WEEKLY)) {
				/* Case 7: Weekly event this month only but every year */
				if (this.getDate().get(Date.DAY_OF_WEEK) == date
						.get(Date.DAY_OF_WEEK)
						&& this.getDate().get(Date.MONTH) == date.get(Date.MONTH))
					return true;
			} 
			else if (this.frequency == (Frequency.OCCUR_YEARLY
					| Frequency.OCCUR_MONTHLY | Frequency.OCCUR_WEEKLY)) {
				/* Case 8: Weekly event every month every year */
				if (this.getDate().get(Date.DAY_OF_WEEK) == date
						.get(Date.DAY_OF_WEEK))
					return true;
			}
		}
		else if (Frequency.isByWeekday(this.frequency)) {
			int weekday = this.date.get(java.util.Calendar.DAY_OF_WEEK);
			int index = this.date.getWeekdayIndex();
			if (date.get(java.util.Calendar.DAY_OF_WEEK) == weekday &&
					date.getWeekdayIndex() == index)
				return true;
			else
				return false;
		}
		else if (Frequency.isByInterval(this.frequency)) {
			if (Frequency.getUnit(this.frequency) == Frequency.UNIT_DAYS ||
					Frequency.getUnit(this.frequency) == Frequency.UNIT_WEEKS) {
				long dayDiff = this.date.dayDiff(date);
				if (dayDiff < 0)
					dayDiff = -dayDiff;
				long interval = Frequency.getInterval(this.frequency);
				if (Frequency.getUnit(this.frequency) == Frequency.UNIT_WEEKS)
					interval *= 7;
				long div = dayDiff / interval;
				if (div * interval == dayDiff)
					return true;
				else
					return false;
			}
			else if (Frequency.getUnit(this.frequency) == Frequency.UNIT_MONTHS ||
					Frequency.getUnit(this.frequency) == Frequency.UNIT_YEARS) {
				if (this.date.get(java.util.Calendar.DAY_OF_MONTH) != 
						date.get(java.util.Calendar.DAY_OF_MONTH))
					return false;
				long monDiff = this.date.get(java.util.Calendar.MONTH) + 
						this.date.get(java.util.Calendar.YEAR) * 12 - (
						date.get(java.util.Calendar.MONTH) + 
						date.get(java.util.Calendar.YEAR) * 12);
				if (monDiff < 0)
					monDiff = - monDiff;
				long interval = Frequency.getInterval(this.frequency);
				if (Frequency.getUnit(this.frequency) == Frequency.UNIT_YEARS)
					interval *= 12;
				long div = monDiff / interval;
				if (div * interval == monDiff)
					return true;
				else
					return false;
			}
		}
		else if (Frequency.isByEndOfMonth(this.frequency))
			return date.getDaysToEndOfMonth() == this.date.getDaysToEndOfMonth();
		return false;
	}

	/**
	 * Returns the string representation of a reminder.
	 * @param x - Reminder (see Event.REMIND_XXX)
	 * @param Short - True for short form, false for long form
	 * @return String representation.
	 */
	public static String getReminderAsString(int x, boolean Short) {
		if (x == NO_REMIND)
			return Short ? Trans.t("No") : Trans.t("Not at all");
		else if (x == REMIND_NOW)
			return Short ? Trans.t("begin") : Trans.t("at start");
		else if (x == REMIND_5MIN)
			return Short ? Trans.t("5min") :Trans.t("5 minutes before");
		else if (x == REMIND_10MIN)
			return Short ? Trans.t("10min") : Trans.t("10 minutes before");
		else if (x == REMIND_15MIN)
			return Short ? Trans.t("15min") : Trans.t("15 minutes before");
		else if (x == REMIND_30MIN)
			return Short ? Trans.t("30min") : Trans.t("30 minutes before");
		else if (x == REMIND_1H)
			return Short ? Trans.t("1h") : Trans.t("1 hours before");
		else if (x == REMIND_2H)
			return Short ? Trans.t("2h") : Trans.t("2 hours before");
		else if (x == REMIND_3H)
			return Short ? Trans.t("3h" ):Trans.t("3 hours before");
		else if (x == REMIND_4H)
			return Short ? Trans.t("4h") : Trans.t("4 hours before");
		else if (x == REMIND_5H)
			return Short ? Trans.t("5h") : Trans.t("5 hours before");
		else if (x == REMIND_1D)
			return Short ? Trans.t("1d" ):  Trans.t("1 Days before");
		else if (x == REMIND_2D)
			return Short ? Trans.t("2d") :  Trans.t("2 Days before");
		else if (x == REMIND_3D)
			return Short ? Trans.t("3d") : Trans.t("3 Days before");
		else if (x == REMIND_4D)
			return Short ? Trans.t("4d") :  Trans.t("4 Days before");
		else if (x == REMIND_5D)
			return Short ? Trans.t("5d") :  Trans.t("5 Days before");
		else if (x == REMIND_6D)
			return Short ? Trans.t("6d") : Trans.t("6 Days before");
		else if (x == REMIND_1W)
			return Short ? Trans.t("1w") : Trans.t("1 Week before");
		else if (x == REMIND_10D)
			return Short ? Trans.t("10d") : Trans.t("10 Days before");
		else if (x == REMIND_2W)
			return Short ? Trans.t("2w") : Trans.t("2 Weeks before");
		else if (x == REMIND_3W)
			return Short ? Trans.t("3w"): Trans.t("3 Weeks before");
		else if (x == REMIND_1M)
			return Short ? Trans.t("1m") : Trans.t("1 Months before");
		else if (x == REMIND_2M)
			return Short ? Trans.t("2m") : Trans.t("2 Months before");
		else if (x == REMIND_3M)
			return Short ? Trans.t("3m") : Trans.t("3 Months before");
		else
			return "???";
	}
	
	/**
	 * Sort the events list by date (earliest first). In ambiguous
	 * cases, move holidays to front.
	 * @param events - Event list to sort
	 * @param withFrequency - True for sorting with attention to
	 * 		the events frequencies; false for sorting by base date
	 * 		only
	 * @return Sorted list.
	 */
	public static Vector<Event> sortByDate(Vector<Event> events, 
			boolean withFrequency) {
		/* Insert sort */
		Vector<Event> res = new Vector<Event>();
		for (Event e : events) {
			long time = (withFrequency ? e.getNextDate() : e.getDate()).getTimeInMillis();
			int i = 0;
			
			if (e.isHoliday() || e.isSpecial()) {
				/* 
				 * Special case for holidays:
				 * Always move them to the front
				 */
				for ( ; i < res.size(); i++)
					if (time <= (withFrequency ? res.get(i).getNextDate() : res.get(i).getDate()).getTimeInMillis())
						break;
			}
			else {
				for ( ; i < res.size(); i++)
					if (time < (withFrequency ? res.get(i).getNextDate() : res.get(i).getDate()).getTimeInMillis())
						break;
			}
			res.add(i, e);
		}
		
		return res;
	}

	/**
	 * 
	 * @return Attached file of this event. If there are more than 
	 * one file, a first one will be returned. Will NEVER return the file 
	 * "notes.txt". If there is no attached file (except for "notes.txt"), 
	 * null will be returned.
	 */
	public File getAttachment(String workspace) {
		File folder = new File(workspace + File.separator + 
				Calendar.EVENT_DIR + File.separator + this.ID);
		File files[] = null;
		if (folder.exists() && folder.isDirectory())
			files = folder.listFiles();
		if (files != null)
			for (File f : files) {
				Logger.debug("all file for this event:" + f.getName());
				if (!f.getName().equals(Calendar.NOTES_FILE)) {
					if (f.getName().equals(Calendar.LINK_FILE))
						return this.followLink(f);
					else
						return f;
				}
			}
		return null;
	}

	/**
	 * 
	 * @return True if there is a file "link.txt" for this event.
	 */
	public boolean attachmentIsLink(String workspace) {
		File res = new File(workspace + File.separator + Calendar.EVENT_DIR + 
				File.separator + this.ID + File.separator + Calendar.LINK_FILE);
		return res.exists();
	}

	/**
	 * Gets the file pointed to by a link.
	 * @param link - File which contains the link
	 * @return File pointed to by the link. If the file specified does 
	 * not exist, null will be returned.
	 */
	private File followLink(File link) {
		Logger.debug("following link:" + link);
		if (link != null && link.exists())
			try {
				String path = "";
				Scanner sc = new Scanner(link);
				if (sc.hasNextLine())
					path = sc.nextLine();
				sc.close();
				File dest = new File(path);
				if (dest.exists())
					return dest;
			} catch (IOException io) {
			}
		return null;
	}

	/**
	 * 
	 * @return Content of the file "notes.txt" for this event. 
	 * If there is no such file, "" will be returned.
	 */
	public String getNotes(String workspace) {
		File notes_txt = new File(workspace + File.separator + Calendar.EVENT_DIR + 
				File.separator + this.ID + File.separator + Calendar.NOTES_FILE);
		String res = "";
		if (notes_txt.exists() && notes_txt.canRead()) {
			Scanner in = null;
			try {
				in = new Scanner(notes_txt);
				boolean firstLine = true;
				while (in.hasNextLine())
					if (firstLine) {
						res += in.nextLine();
						firstLine = false;
					} else
						res += "\n" + in.nextLine();
			} catch (FileNotFoundException f) {
				Logger.error("There is no file " + Calendar.NOTES_FILE
						+ " for this event but there should be!");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				in.close();
			}
		}
		return res;
	}
	
	/**
	 * Write XML event tag.
	 * @param out - Stream to write
	 * @throws IOException
	 */
	public void write(BufferedWriter out) throws IOException {
		out.write("    <Event");
		out.write(" ID=\"" + ID + "\"");
		out.write(" date=\"" + date.dateToString(false) + "\"");
		if (endDate != null)
			out.write(" endDate=\"" + endDate.dateToString(false) + "\"");
		if (date.hasTime())
			out.write(" time=\"" + date.timeToString() + "\"");
		if (frequency != Frequency.OCCUR_ONCE)
			out.write(" frequency=\"" + frequency + "\"");
		if (remind != Configuration.defaultConfig.getReminder())
			out.write(" remind=\"" + 
					Event.getReminderAsString(remind, true) + "\"");
		out.write(">" + name + "</Event>\n");
	}
	
	/**
	 * Create a JLabel icon that opens this events attachment
	 * (if applicable).
	 * @param workspace - Workspace to look for an attachment
	 * @return Icon as JLabel if attachment found, null otherwise.
	 */
	public JLabel getAttachmentIcon(final String workspace) {
		if (this.getAttachment(workspace) == null)
			return null;
		
		JLabel img = new JLabel(new ImageIcon(
				this.getClass().getClassLoader().getResource("media/attachment20.ico")));
		final Event thisEvent = this;
		img.setToolTipText(Trans.t("appendix")+": " + this.getAttachment(workspace).getName());
		img.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent m) {
				try {
					Desktop.getDesktop().open(thisEvent.getAttachment(workspace));
				} catch (IOException e) {
					Logger.error("[mouseClicked] error while trying to open attached file: "+e.toString());
				}
			}

			public void mouseEntered(MouseEvent m) {
				m.getComponent().setCursor(
						Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			public void mouseExited(MouseEvent m) {
				m.getComponent().setCursor(
						Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		return img;
	}
	
	/**
	 * Create a JLabel icon that opens this events notes
	 * (if applicable) in a dialog window.
	 * @param workspace - Workspace to look for the notes
	 * @param parent - Parent frame object for the notes dialog.
	 * @return Icon as JLabel if notes found, null otherwise.
	 */
	public JLabel getNotesIcon(final String workspace, JFrame parent) {
		String notes = this.getNotes(workspace);
		if (notes.equals(""))
			return null;
		
		final JDialog notesDialog = new JDialog(parent, Trans.t("notes"));
		JTextArea notesArea = new JTextArea(notes);
		notesArea.setEditable(false);
		JScrollPane pScroll = new JScrollPane(notesArea,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		pScroll.setBounds(5, 5, 212, 80);
		notesDialog.setLayout(new BorderLayout());
		notesDialog.add(pScroll, BorderLayout.CENTER);
		notesDialog.setSize(220, 100);
		notesDialog.setUndecorated(false);
		notesDialog.setVisible(false);
		JLabel img = new JLabel(new ImageIcon(
				this.getClass().getClassLoader().getResource("media/notes20.ico")));
		img.setToolTipText(Trans.t("See notes"));
		img.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent m) {
				notesDialog.setLocation(m.getLocationOnScreen());
				notesDialog.setVisible(true);
			}

			public void mouseEntered(MouseEvent m) {
				m.getComponent().setCursor(
						Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			public void mouseExited(MouseEvent m) {
				m.getComponent().setCursor(
						Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		
		return img;
	}
	
	/**
	 * 
	 * @param capital - First letter capital?
	 * @return Label of the number of days this events next date 
	 * 		is in future or past.
	 */
	public String getDayDiffLabel(boolean capital) {
		long dayDiff = this.getNextDate().dayDiff(new Date());

		if (dayDiff == 0)
			return capital ? Trans.t("Today") : Trans.t("today");
		else if (dayDiff == 1L)
			return capital ? Trans.t("Tomorning") : Trans.t("tomorning") ;
		else if (dayDiff == 2L)
			return capital ? Trans.t("After tomorrow") : Trans.t("after tomorrow");
		else if (dayDiff == -1L)
			return capital ? Trans.t("Yesterday") : Trans.t("yesterday");
		else if (dayDiff < 0)
			return (capital ? Trans.t("Before")+" " : Trans.t("before")+" ") + 
					(-dayDiff) + " "+Trans.t("meet");
		else
			return (capital ? "In " : "in ") + 
					dayDiff + " "+Trans.t("meet");
	}
	
	/**
	 * 
	 * @return Label of the time difference this event
	 * 		is in future or past.
	 */
	public String getMinDiffLabel() {
		if (!this.date.hasTime())
			return "";
		
		long minDiff = this.getNextDate().minDiff(new Date());
		long hours = minDiff / 60L;
		long minut = minDiff - hours * 60L;

		if (minDiff < 0)
			return Trans.t("before")+" " + 
					(hours == 0 ? "" : (-hours) + Trans.t("h")+" ") + (-minut) + Trans.t("min");
		else if (minDiff > 0)
			return "in " +
					(hours == 0 ? "" : hours + Trans.t("h")+" ") + minut + Trans.t("min");
		else
			return Trans.t("NOW");
	}
}
