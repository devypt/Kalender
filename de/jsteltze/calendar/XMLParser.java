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

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import de.jsteltze.calendar.config.ColorSet;
import de.jsteltze.calendar.config.Configuration;
import de.jsteltze.calendar.config.Const;
import de.jsteltze.calendar.config.Holidays;
import de.jsteltze.calendar.exceptions.CannotParseException;
import de.jsteltze.common.calendar.Date;

/**
 * XML parser for calendar XML file.<br> 
 * This is actually a poor implementation of a XML parser that will
 * only work for a very special XML syntax of the calendar XML file.
 * Since external XML parser would extend this simple java project
 * by some factors and would throw questions of licensing, I leave
 * it that way.<br><br>
 * 
 * <b>NOTE: You must not change the generated XML file in terms
 * of adding spaces, blank lines, moving spaces into tabs etc.
 * because this will cause this XML parser to fail!</b><br><br>
 * 
 * Since I already admitted that this implementation is no example
 * for good programming, I don't see a need for augmented commenting
 * or formatting.  
 * 
 * @author Johannes Steltzer
 *
 */
public class XMLParser {
    
    /** Parsed settings */
    private Configuration config;
    
    /** Parsed events */
    private Vector<Event> events;
    
    /** File to parse */
    private String file;
    
    private static Logger logger = Logger.getLogger(XMLParser.class);

    /**
     * Construct a new XML parser.
     * Call .parse to start parsing.
     */
    public XMLParser() {
        this.events = new Vector<Event>();
        this.config = Configuration.defaultConfig;
    }

    /**
     * Parses the specified calendar XML file for settings and events. 
     * @param file - File path to parse
     * @throws CannotParseException
     * @throws FileNotFoundException
     */
    public void parse(String file) throws 
        CannotParseException, FileNotFoundException {

        Scanner in = null;
        this.file = file;
        File datei = new File(file);
        try {
            in = new Scanner(datei, Const.ENCODING);
        } catch (NoClassDefFoundError e) {
            JOptionPane.showMessageDialog(null,
                    "Zur Ausführung wird Java mit Version 1.6 oder aktueller benötigt.",
                    "Java zu alt...", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        String zeile = "";
        if (in.hasNextLine()) {
            zeile = in.nextLine();
            
            /*
             * UTF-8 BOM if exists
             */
            if ((int) zeile.charAt(0) == 0xfeff)
                zeile = zeile.substring(1);

            if (!zeile.startsWith("<?xml version="))
                throw new CannotParseException("XML-Kopfzeile passt nicht ins Schema.");
            if (!zeile.substring(30).startsWith(Const.ENCODING))
                throw new CannotParseException("Unbekanntes XML-Encoding.");
        }

        if (in.hasNextLine()) {
            zeile = in.nextLine();
            if (!zeile.matches("<Calendar version=\"\\d\\.\\d_svn\\d+\">"))
                throw new CannotParseException("Versions-Zeile nicht wie erwartet.");
        }

        if (in.hasNextLine())
            zeile = in.nextLine();

        if (in.hasNextLine() && zeile.equals("  <Config>")) {
            Vector<String> configLines = new Vector<String>();
            while (!zeile.equals("  </Config>") && in.hasNextLine()) {
                zeile = in.nextLine();
                configLines.add(zeile);
            }

            readConfigLines(configLines);

            zeile = in.nextLine();
        } else
            logger.debug("No config section found. Default settings are already loaded.");

        if (in.hasNextLine() && zeile.equals("  <Events>")) {
            while (in.hasNextLine()
                    && !(zeile = in.nextLine()).equals("  </Events>")) {
                try {
                    Event e = parseEventLine(zeile);
                    if (e != null)
                        events.add(e);
                } catch (CannotParseException e) {
                    showErrorMessage(zeile);
                }
            }
        }

        in.close();
    }
    
    /**
     * Shows an error massage that there was a problem with a
     * specific line and that this line will be skipped.
     * @param line - Line that could not be parsed
     */
    private void showErrorMessage(String line) {
        JOptionPane.showMessageDialog(null,
                "Die Zeile:\n\"" + line + "\"\naus der Datei \"" + file 
                + "\" passt nicht ins Schema.\nDie Zeile wird übersprungen.",
                "Fehler beim Lesen...", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Parse config section of calendar XML file.
     * @param configLines - Config section
     */
    private void readConfigLines(Vector<String> configLines) {
        byte defaultView = Configuration.defaultConfig.getView();
        byte reminder = Configuration.defaultConfig.getReminder();
        byte atClose = Configuration.defaultConfig.getOnCloseAction();
        byte atClickDay = Configuration.defaultConfig.getOnClickDayAction();
        byte atClickEvent = Configuration.defaultConfig.getOnClickEventAction();
        byte style = Configuration.defaultConfig.getStyle();
        Color[] colors = ColorSet.DEFAULT.clone();
        int holidays = Holidays.DEFAULT_HOLIDAYS;
        int specialDays = Holidays.DEFAULT_SPECIAL;
        boolean autoUpdate = Configuration.defaultConfig.getAutoUpdate();
        boolean moon = Configuration.defaultConfig.getMoon();
        String themeFile = Configuration.defaultConfig.getTheme();
        boolean startSystray = Configuration.defaultConfig.getSystrayStart();
        boolean playTheme = Configuration.defaultConfig.getPlayTheme();
        boolean buttonTexts = Configuration.defaultConfig.getButtonTexts();

        for (String s : configLines) {
            if (s.equals("  <Config>") || s.equals("  </Config>") || s.equals(""))
                continue;
            if (s.matches("    <DefaultView>\\d</DefaultView>")) {
                try {
                    defaultView = (byte) Integer.parseInt(s.substring(17, 18));
                } catch (NumberFormatException ex) {
                    showErrorMessage(s);
                }
            } else if (s.matches("    <Remind>\\d+</Remind>")) {
                try {
                    String tmp = s.substring(12, s.length());
                    tmp = tmp.substring(0, tmp.indexOf("<"));
                    reminder = (byte) Integer.parseInt(tmp);
                } catch (NumberFormatException ex) {
                    showErrorMessage(s);
                }
            } else if (s.matches("    <AtClose>\\d</AtClose>")) {
                try {
                    atClose = (byte) Integer.parseInt(s.substring(13, 14));
                } catch (NumberFormatException ex) {
                    showErrorMessage(s);
                }
            } else if (s.matches("    <AtClickDay>\\d</AtClickDay>")) {
                try {
                    atClickDay = (byte) Integer.parseInt(s.substring(16, 17));
                } catch (NumberFormatException ex) {
                    showErrorMessage(s);
                }
            } else if (s.matches("    <AtClickEvent>\\d</AtClickEvent>")) {
                try {
                    atClickEvent = (byte) Integer.parseInt(s.substring(18, 19));
                } catch (NumberFormatException ex) {
                    showErrorMessage(s);
                }
            } else if (s.matches("    <Style>\\d</Style>")) {
                try {
                    style = (byte) Integer.parseInt(s.substring(11, 12));
                } catch (NumberFormatException ex) {
                    showErrorMessage(s);
                }
            } else if (s.matches("    <HolidayID>\\d+</HolidayID>")) {
                try {
                    holidays = Integer.parseInt(s.substring(15).replace(
                            "</HolidayID>", ""));
                } catch (NumberFormatException ex) {
                    showErrorMessage(s);
                }
            } else if (s.matches("    <SpecialDaysID>\\d+</SpecialDaysID>")) {
                try {
                    specialDays = Integer.parseInt(s.substring(19).replace(
                            "</SpecialDaysID>", ""));
                } catch (NumberFormatException ex) {
                    showErrorMessage(s);
                }
            } else if (s.matches("    <AutoUpdate>\\w+</AutoUpdate>")) {
                try {
                    autoUpdate = stringToBoolean(s.substring(16).replace(
                            "</AutoUpdate>", ""));
                } catch (CannotParseException ex) {
                    showErrorMessage(s);
                }
            } else if (s.matches("    <ShowMoon>\\w+</ShowMoon>")) {
                try {
                    moon = stringToBoolean(s.substring(14).replace(
                            "</ShowMoon>", ""));
                } catch (CannotParseException ex) {
                    showErrorMessage(s);
                }
            } else if (s.matches("    <ButtonTexts>\\w+</ButtonTexts>")) {
                try {
                    buttonTexts = stringToBoolean(s.substring(17).replace(
                            "</ButtonTexts>", ""));
                } catch (CannotParseException ex) {
                    showErrorMessage(s);
                }
            } else if (s.matches("    <SystrayStart>\\w+</SystrayStart>")) {
                try {
                    startSystray = stringToBoolean(s.substring(18).replace(
                            "</SystrayStart>", ""));
                } catch (CannotParseException ex) {
                    showErrorMessage(s);
                }
            } else if (s.matches("    <PlayTheme>\\w+</PlayTheme>")) {
                try {
                    playTheme = stringToBoolean(s.substring(15).replace(
                            "</PlayTheme>", ""));
                } catch (CannotParseException ex) {
                    showErrorMessage(s);
                }
            } else if (s.matches("    <Theme>.+</Theme>")) {
                themeFile = s.substring(11);
                themeFile = themeFile.substring(0, themeFile.length() - "</Theme>".length());
            } else if (s.matches("    <Color r=\"\\d+\" g=\"\\d+\" b=\"\\d+\">\\d</Color>")) {
                try {
                    String[] array = s.split("\"");
                    int r = Integer.parseInt(array[1]);
                    int g = Integer.parseInt(array[3]);
                    int b = Integer.parseInt(array[5]);
                    int index = Integer.parseInt(array[6].substring(1, 2));
                    colors[index] = new Color(r, g, b);
                } catch (Exception ex) {
                    showErrorMessage(s);
                }
            } else {
                showErrorMessage(s);
            }
        }

        this.config = new Configuration(defaultView, reminder, atClose,
                atClickDay, atClickEvent, style, colors, holidays, 
                specialDays, autoUpdate, moon, themeFile, startSystray, 
                playTheme, buttonTexts);
    }

    /**
     * Convert a string to a date. 
     * @param s - String of the form DD.MM.YYYY (also D.M.YYYY)
     * @return Date object.
     * @throws CannotParseException
     */
    private Date stringToCalendar(String s) 
        throws CannotParseException {
        
        String[] array = s.split("\\.");

        int tag, monat, jahr;
        try {
            tag = Integer.parseInt(array[0]);
            monat = Integer.parseInt(array[1]);
            jahr = Integer.parseInt(array[2]);
        } catch (Exception e) {
            throw new CannotParseException(s);
        }
        return new Date(jahr, monat - 1, tag);
    }

    /**
     * Convert a string to a boolean.
     * @param s - String either "true", "1", "false" or "0"
     * @return Boolean.
     * @throws CannotParseException
     */
    private boolean stringToBoolean(String s) throws CannotParseException {
        if (s.equalsIgnoreCase("true") || s.equals("1"))
            return true;
        else if (s.equalsIgnoreCase("false") || s.equals("0"))
            return false;
        else
            throw new CannotParseException(s);
    }

    /**
     * Converts a string to a time.
     * @param s - String of the form HH:MM
     * @return Date object that contains the time.
     * @throws CannotParseException
     */
    private Date stringToTime(String s) throws CannotParseException {
        String[] array = s.split(":");
        int stunde, minute;
        try {
            stunde = Integer.parseInt(array[0]);
            minute = Integer.parseInt(array[1]);
        } catch (Exception e) {
            throw new CannotParseException(s);
        }
        return new Date(stunde, minute);
    }

    /**
     * Parse event ID.
     * @param s - Event line
     * @return ID.
     * @throws CannotParseException
     */
    private int parseID(String s) throws CannotParseException {
        /* Extrahiere ID */
        Pattern p;
        Matcher m;

        p = Pattern.compile("ID=\"\\d+\"");
        m = p.matcher(s);
        if (m.find()) {
            String id = s.substring(m.start(), m.end());
            id = id.substring(4, id.length() - 1);
            try {
                return Integer.parseInt(id);
            } catch (NumberFormatException n) {
                throw new CannotParseException(id);
            }
        } else
            return -1;
    }

    /**
     * Parse event date and time.
     * @param s - Event line
     * @return Date object.
     * @throws CannotParseException
     */
    private Date parseDate(String s) throws CannotParseException {
        /* Extrahiere Datum */
        Pattern p;
        Matcher m;
        Date greg;

        p = Pattern.compile("date=\"\\d+\\.\\d+\\.\\d+\"");
        m = p.matcher(s);
        if (m.find()) {
            String date = s.substring(m.start(), m.end());
            date = date.substring(6, date.length() - 1);
            greg = stringToCalendar(date);
        } else
            throw new CannotParseException("Fehlender Datums-EIntrag.");

        /* Extrahiere Uhrzeit */
        p = Pattern.compile("time=\"\\d+:\\d+\"");
        m = p.matcher(s);
        if (m.find()) {
            String time = s.substring(m.start(), m.end());
            time = time.substring(6, time.length() - 1);
            Date uhr = stringToTime(time);
            greg.set(Calendar.HOUR_OF_DAY, uhr.get(Calendar.HOUR_OF_DAY));
            greg.set(Calendar.MINUTE, uhr.get(Calendar.MINUTE));
            greg.setHasTime(true);
        } else {
            greg.setHasTime(false);
        }

        return greg;
    }

    /**
     * Parse event end date (if exists).
     * @param s - Event line
     * @return If exists end date, null otherwise. 
     * @throws CannotParseException
     */
    private Date parseEndDate(String s) throws CannotParseException {
        /* Extrahiere Datum */
        Pattern p;
        Matcher m;

        p = Pattern.compile("endDate=\"\\d+\\.\\d+\\.\\d+\"");
        m = p.matcher(s);
        if (m.find()) {
            String date = s.substring(m.start(), m.end());
            date = date.substring(9, date.length() - 1);
            return stringToCalendar(date);
        } else
            return null;
    }

    /**
     * Parse reminder.
     * @param s - Event line
     * @return Reminde (see Event.REMIND_XXX).
     * @throws CannotParseException
     */
    private byte parseRemind(String s) throws CannotParseException {
        /* Extrahiere Datum */
        Pattern p;
        Matcher m;
        byte remind = Configuration.defaultConfig.getReminder();

        p = Pattern.compile("remind=\".+\"");
        m = p.matcher(s);
        if (m.find()) {
            String rem = s.substring(m.start(), m.end());
            rem = rem.substring(8, rem.length() - 1);
            for (byte i = 0; i < Event.NUMBER_REMINDS; i++)
                if (rem.equals(Event.getReminderAsString(i, true))) {
                    remind = i;
                    break;
                }
            if (remind == Configuration.defaultConfig.getReminder())
                throw new CannotParseException(s);
        }
        return remind;
    }

    /**
     * Parse event frequency.
     * @param s - Event line
     * @return Frequency object.
     * @throws CannotParseException
     */
    private short parseFrequency(String s) throws CannotParseException {
        /* Extrahiere Jaehrlich */
        Pattern p;
        Matcher m;
        short frequency = Frequency.OCCUR_ONCE;

        p = Pattern.compile("yearly=\"\\w*\"");
        m = p.matcher(s);
        if (m.find()) {
            String yearly = s.substring(m.start(), m.end());
            yearly = yearly.substring(8, yearly.length() - 1);
            if (stringToBoolean(yearly))
                frequency |= Frequency.OCCUR_YEARLY;
        }

        /* Extrahiere Monatlich */
        p = Pattern.compile("monthly=\"\\w*\"");
        m = p.matcher(s);
        if (m.find()) {
            String monthly = s.substring(m.start(), m.end());
            monthly = monthly.substring(9, monthly.length() - 1);
            if (stringToBoolean(monthly))
                frequency |= Frequency.OCCUR_MONTHLY;
        }

        /* Extrahiere Taeglich */
        p = Pattern.compile("weekly=\"\\w*\"");
        m = p.matcher(s);
        if (m.find()) {
            String weekly = s.substring(m.start(), m.end());
            weekly = weekly.substring(8, weekly.length() - 1);
            if (stringToBoolean(weekly))
                frequency |= Frequency.OCCUR_WEEKLY;
        }
        
        p = Pattern.compile("frequency=\"\\d*\"");
        m = p.matcher(s);
        if (m.find()) {
            String freqS = s.substring(m.start(), m.end());
            try {
                return Short.parseShort(freqS.substring(11, freqS.length() - 1));
            } catch (NumberFormatException n) {
                throw new CannotParseException(freqS);
            }
        }

        return frequency;
    }

    /**
     * Parse event name.
     * @param s - Event line
     * @return Event name.
     * @throws CannotParseException
     */
    private String parseName(String s) throws CannotParseException {
        /* Extrahiere Beschreibung */
        Pattern p = Pattern.compile(">.*</Event>");
        Matcher m = p.matcher(s);
        String beschreibung;
        if (m.find()) {
            beschreibung = s.substring(m.start(), m.end());
            beschreibung = beschreibung.substring(1, beschreibung.length() - 8);
            if (beschreibung.equals(""))
                throw new CannotParseException("Beschreibung ist leer.");
        } else
            throw new CannotParseException(s);

        return beschreibung;
    }

    /**
     * Parse event.
     * @param s - Event line
     * @return Event object.
     * @throws CannotParseException
     */
    private Event parseEventLine(String s) throws CannotParseException {
        if (s.equals("  </Events>") || s.equals("  <Events>") || s.equals(""))
            return null;

        if (!s.startsWith("    <Event "))
            throw new CannotParseException(s);

        Event newEvent = new Event(parseDate(s), /* start date */
        parseEndDate(s), /* end date (might be null) */
        parseName(s), /* name */
        Event.HOLIDAY_NONE, /* no holiday */
        parseFrequency(s), /* frequency */
        parseRemind(s), /* time before reminding */
        parseID(s)); /* ID (might be -1) */
        return newEvent;
    }

    /**
     * 
     * @return List of events parsed.
     */
    public Vector<Event> getEvents() {
        return this.events;
    }

    /**
     * 
     * @return Configuration parsed.
     */
    public Configuration getConfig() {
        return this.config;
    }
}
