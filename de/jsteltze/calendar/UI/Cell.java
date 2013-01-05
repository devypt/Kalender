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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Calendar;
import java.util.Vector;

import de.jsteltze.calendar.Event;
import de.jsteltze.calendar.config.ColorSet;
import de.jsteltze.calendar.config.Configuration;
import de.jsteltze.calendar.config.Const;
import de.jsteltze.common.ColorUtil;
import de.jsteltze.common.GraphicUtils;
import de.jsteltze.common.Math;
import de.jsteltze.common.calendar.Date;
import de.jsteltze.common.calendar.Moon;

/**
 * Single cell (date) within calendar canvas.
 * @author Johannes Steltzer
 *
 */
public class Cell {

    /* some width and height constants */
    public static final int Y_CLEAR_LEFT = 28;
    public static final int Y_CLEAR_UP = 44;
    public static final int Y_COLS = 37;
    public static final int Y_ROWS = 12;
    public static final int M_CLEAR_LEFT = 5;
    public static final int M_CLEAR_UP = 44;
    public static final int M_COLS = 15;
    public static final int M_ROWS = 6;
    public static final int W_CLEAR_LEFT = 5;
    public static final int W_CLEAR_UP = 24;
    public static final int W_COLS = 7;
    public static final int W_ROWS = 2;
    public static final int D_CLEAR_LEFT = 30;
    public static final int D_CLEAR_UP = 20;
    public static final int D_COLS = 2;
    public static final int D_ROWS = 25;
    
    public static final int M_HEADER = 25;
    public static final int W_HEADER = 40;
    private static final Font W_HEADER_FONT = new Font(Font.SANS_SERIF, Font.ROMAN_BASELINE, 14);
    
    private static final float _SATURATION = .1f;
    private static Image BALLOONS_9 = Toolkit.getDefaultToolkit().createImage(Cell.class.getClassLoader().getResource("media/balloons9.ico"));
    private static Image BALLOONS_20 = Toolkit.getDefaultToolkit().createImage(Cell.class.getClassLoader().getResource("media/balloons20.ico"));
    
    /** frame color for events */
    private static final Color frameColor = Const.COLOR_DEF_FONT;
    
    /** colors according to current config */
    private Color weekendColor, holidayBgColor, 
            holidayFtColor, ftColor, todayColor, 
            selectedColor, selectedColor2;

    /** column of this cell within the matrix */
    private int col;
    
    /** row of this cell within the matrix */
    private int row;
    
    /** date of the cell */
    private Date date;
    
    /** is the date of this cell todays date? */
    private boolean today;
    
    /** is one event of this cell a holiday? */
    private boolean holiday;
    
    /** does the date belong to a weekend? */
    private boolean weekend;
    
    /** is this cell selected? */
    private boolean selected;
    
    /** all events on this cells date */
    private Vector<Event> events;
    
    /** the canvas where to paint */
    private CalendarCanvas canvas;
    
    /** font size for events text */
    private int fontsize;
    
    /** maximum number of event lines that fit the cell */
    private int max_lines;
    
    /** moon phase */
    private byte moonPhase;

    /**
     * Construct a new cell.
     * @param owner - CalendarFrame object where to paint
     * @param col - Column index within matrix 
     * @param row - Row index within matrix
     * @param date - Date of this cell
     */
    public Cell(CalendarCanvas owner, int col, int row, Date date) {
        this.col = col;
        this.row = row;
        this.date = date;
        this.canvas = owner;
        this.today = false;
        this.holiday = false;
        this.weekend = false;
        this.selected = false;
        this.moonPhase = Moon.MOON_NONE;
        
        if (this.date != null) {
            if (this.date.sameDateAs(new Date()))
                today = true;
            if (canvas.getOwner().getConfig().getMoon())
                moonPhase = Moon.getMoonPhase(this.date);
        }

        if (owner.getView() == Configuration.VIEW_MONTH
                || owner.getView() == Configuration.VIEW_WEEK) {
            if (col == 5 || col == 6 || col == 13 || col == 14)
                weekend = true;
        }
        
        /*
         * In case of yearly view: Weekends will be set in CalendarCanvas 
         * since it is too complicated to calculate here...
         */

        events = new Vector<Event>();
    }

    /**
     * 
     * @return Column index of this cell.
     */
    public int getCol() {
        return col;
    }

    /**
     * 
     * @return Row index of this cell.
     */
    public int getRow() {
        return row;
    }

    /**
     * 
     * @return This cells date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * 
     * @return True if this cells date is today.
     */
    public boolean isToday() {
        return today;
    }

    /**
     * 
     * @return True if one of this cells events is a holiday.
     */
    public boolean isHoliday() {
        return holiday;
    }

    /**
     * 
     * @return True if the cells date is a weekend.
     */
    public boolean isWeekend() {
        return weekend;
    }

    /**
     * 
     * @return True if this cell is currently selected.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * 
     * @return This cells events.
     */
    public Vector<Event> getEvents() {
        return events;
    }

    /**
     * Alter column index.
     * @param x - Column index to set
     */
    public void setCol(int x) {
        col = x;
    }
    
    /**
     * Get the event under the cursor.
     * @param y_coord - Mouse y-position
     * @param height - Current height of all cells
     * @return The event under the current mouse position (if exists) or
     *         null.
     */
    public Event getEventUnderCursor(Point cursor, Dimension cellSpace) {
        int y_coord = cursor.y;
        
        if (canvas.getView() == Configuration.VIEW_MONTH) {
            y_coord -= (M_CLEAR_UP + 27 + row * cellSpace.height);
            if (y_coord < 0)
                return null;
            int index = Math.div(y_coord, fontsize);
            if (index < events.size()) {
                if (max_lines < events.size() && index + 1 >= max_lines)
                    return null;
                return events.elementAt(index);
            }
        }
        
        else if (canvas.getView() == Configuration.VIEW_WEEK) {
            y_coord -= (W_CLEAR_UP + W_HEADER + 3 + row * cellSpace.height);
            if (y_coord < 0)
                return null;
            int index = Math.div(y_coord, fontsize);
            int size = events.size();
            if (size > 0 && (events.lastElement().isSpecial() || 
                    events.lastElement().isHoliday()))
                size--;
            if (index < size) {
                if (max_lines < size && index + 1 >= max_lines)
                    return null;
                return events.elementAt(index);
            }
        }
        
        else if (canvas.getView() == Configuration.VIEW_DAY) {
            int start_x = D_CLEAR_LEFT + col * cellSpace.width + 2;
            for (Event event : events) {
                if (cursor.x >= start_x && 
                        cursor.x <= start_x + GraphicUtils.getStringWidth(canvas.getGraphics(), new Font(Font.SANS_SERIF, Font.PLAIN, fontsize), event.getName()))
                    return event;
                start_x += GraphicUtils.getStringWidth(canvas.getGraphics(), new Font(Font.SANS_SERIF, Font.PLAIN, fontsize), event.getName()) + 4;
            }
        }
        
        return null;
    }
    
    /**
     * Set weekend flag.
     * @param x - True if the cells date is a weekend
     */
    public void setWeekend(boolean x) {
        weekend = x;
    }

    /**
     * Set selected flag.
     * @param x - True if this cell is currently selected
     */
    public void setSelected(boolean x) {
        selected = x;
        paint(canvas.getGraphics(), canvas.calcDim());
    }

    /**
     * Add an event to this cell.
     * @param x - Event to add
     */
    public void addEvent(Event x) {
        if (x.isHoliday()) {
            this.holiday = true;
            events.add(0, x);
        }
        else
            events.add(x);
        paint(canvas.getGraphics(), canvas.calcDim());
    }

    /**
     * Remove an event from this cell.
     * @param x - Event to remove
     */
    public void removeEvent(Event x) {
        /* Was this the last holiday? */
        events.remove(x);

        holiday = false;
        for (Event e : events)
            if (e.isHoliday()) {
                holiday = true;
                break;
            }

        paint(canvas.getGraphics(), canvas.calcDim());
    }
    
    /**
     * Check if this cell contains a specific event.
     * @param e - Event to check
     * @return True if this cell contains the event, false otherwise.
     */
    public boolean containsEvent(Event e) {
        return events.contains(e);
    }
    
    /**
     * Print the name of an event over affected cells for monthly 
     * and weekly view.
     * @param event - Event to label
     * @param x - Graphics to paint on
     * @param space - Available space per cell
     * @param index - Index (row) of this event 
     */
    private void printEventNameMonthWeek(Event event, Graphics x, Dimension space, int index) {
        int relevantCols = 1;
        int start_col = col;
        int startText = 1;
        if (event.getEndDate() != null) {
            int dayDiffBefore = (int) this.date.dayDiff(event.getDate());
            int dayDiffAfter = (int) event.getEndDate().dayDiff(this.date);
            int tmpCol = col > 7 ? col - 8 : col;
            int relevantColsBefore;
            if (tmpCol > dayDiffBefore) {
                relevantColsBefore = dayDiffBefore;
                startText += 2;
            }
            else
                relevantColsBefore = tmpCol;
            start_col -= relevantColsBefore;
            int relevantColsAfter = tmpCol + dayDiffAfter > 6 ? 6 - tmpCol : dayDiffAfter;
            
            /* Check for end of month (line break) */
            Date testLineBreak = (Date) this.date.clone();
            testLineBreak.add(java.util.Calendar.DAY_OF_MONTH, relevantColsAfter);
            if (testLineBreak.get(java.util.Calendar.MONTH) != this.date.get(java.util.Calendar.MONTH))
                relevantColsAfter -= testLineBreak.get(java.util.Calendar.DAY_OF_MONTH);
            
            /* Check for beginning of month (line break) */
            if (relevantColsBefore >= this.date.get(java.util.Calendar.DAY_OF_MONTH)) {
                start_col += relevantColsBefore - (this.date.get(java.util.Calendar.DAY_OF_MONTH) - 1);
                relevantColsBefore = this.date.get(java.util.Calendar.DAY_OF_MONTH) - 1;
            }
            
            relevantCols += relevantColsBefore + relevantColsAfter;
        }
        
        int clear_left = canvas.getView() == Configuration.VIEW_MONTH ? M_CLEAR_LEFT : W_CLEAR_LEFT;
        int clear_up = canvas.getView() == Configuration.VIEW_MONTH ? M_CLEAR_UP : W_CLEAR_UP;
        int header = canvas.getView() == Configuration.VIEW_MONTH ? M_HEADER : W_HEADER;
        
        int textSpace = space.width * relevantCols - startText - 1;
        startText += clear_left + space.width * start_col;
        int start_y = clear_up + row * space.height;
        
        if (event.isSpecial() || event.isHoliday())
            textSpace -= 9;
        
        String cuttedName = cut(event.getName(), textSpace, x);
        if (cuttedName.equals(event.getName())) {
            int neededSpace = GraphicUtils.getStringWidth(x, cuttedName);
            startText += ((textSpace - neededSpace) / 2);
        }
        else
            startText += 2;
        
        if (event.isSpecial() || event.isHoliday()) {
            x.setColor(holidayFtColor);
            x.drawImage(BALLOONS_9, ++startText, start_y + header + 3 + index * fontsize, 
                    event.isSelected() ? selectedColor2 :
                        today ? todayColor : Color.WHITE, canvas);
            startText += 9;
        }
        
        x.drawString(cuttedName,
                startText, start_y + header + (index + 1) * fontsize + 1);
    }
    
    /**
     * Paint an selectible event on the this cell in monthly view.
     * @param x - Graphics to paint on
     * @param i - Index of event to paint
     * @param space - Available space of this cell
     */
    private void paintEventMonth(Graphics x, int i, Dimension space) {
        int start_x = M_CLEAR_LEFT + col * space.width;
        int start_y = M_CLEAR_UP + row * space.height;

        Color c1, c2;
        Event event = events.elementAt(i);
        
        /* group multi-day events */
        if (event.getEndDate() != null) {
            if (event.getDate().sameDateAs(this.date)) {
                /* mark as selected */
                if (event.isSelected()) {
                    x.setColor(selectedColor);
                    x.fillRect(start_x + 3, start_y + M_HEADER + 3 + i * fontsize, 
                            space.width - 3, fontsize / 2);
                    x.setColor(selectedColor2);
                    x.fillRect(start_x + 3, start_y + M_HEADER + 3 + i * fontsize + fontsize / 2, 
                            space.width - 3, fontsize / 2);
                    x.setColor(Color.black);
                }
                else
                    x.setColor(frameColor);
                x.drawRoundRect(start_x + 2, start_y + M_HEADER + 2 + i * fontsize, 
                        space.width - 2, fontsize, 2, 2);
                
                if (col != 6 && col != 14) {
                    /* remove right border of rectangle */
                    if (event.isSelected()) {
                        c1 = selectedColor;
                        c2 = selectedColor2;
                    }
                    else
                        c1 = c2 = Color.white;
                    x.setColor(c1);
                    x.drawLine(start_x + space.width, start_y + M_HEADER + 3 + i * fontsize, 
                            start_x + space.width, start_y + M_HEADER + 3 + i * fontsize + fontsize / 2);
                    x.setColor(c2);
                    x.drawLine(start_x + space.width, start_y + M_HEADER + 3 + i * fontsize + fontsize / 2, 
                            start_x + space.width, start_y + M_HEADER + 1 + (i + 1) * fontsize);
                }
            }
            else if (event.getEndDate().sameDateAs(this.date)) {
                if (event.isSelected()) {
                    x.setColor(selectedColor);
                    x.fillRect(start_x, start_y + M_HEADER + 3 + i * fontsize, 
                            space.width - 2, fontsize / 2);
                    x.setColor(selectedColor2);
                    x.fillRect(start_x, start_y + M_HEADER + 3 + i * fontsize + fontsize / 2, 
                            space.width - 2, fontsize / 2);
                    x.setColor(Color.black);
                    x.drawLine(start_x, start_y + M_HEADER + 2 + i * fontsize, 
                            start_x, start_y + M_HEADER + 2 + i * fontsize);
                    x.drawLine(start_x, start_y + M_HEADER + 2 + (i + 1) * fontsize, 
                            start_x, start_y + M_HEADER + 2 + (i + 1) * fontsize);
                    x.setColor(Color.black);
                }
                else
                    x.setColor(frameColor);
                x.drawRoundRect(start_x, start_y + M_HEADER + 2 + i * fontsize, 
                        space.width - 2, fontsize, 2, 2);
                                
                if (col != 0 && col != 8) {
                    /* remove left border of rectangle */
                    if (event.isSelected()) {
                        c1 = selectedColor;
                        c2 = selectedColor2;
                    }
                    else
                        c1 = c2 = Color.white;
                    x.setColor(c1);
                    x.drawLine(start_x, start_y + M_HEADER + 3 + i * fontsize, 
                            start_x, start_y + M_HEADER + 3 + i * fontsize + fontsize / 2);
                    x.setColor(c2);
                    x.drawLine(start_x, start_y + M_HEADER + 3 + i * fontsize + fontsize / 2, 
                            start_x, start_y + M_HEADER + 1 + (i + 1) * fontsize);
                }
            }
            else {
                if (event.isSelected()) {
                    x.setColor(selectedColor);
                    x.fillRect(start_x + 1, start_y + M_HEADER + 3 + i * fontsize, 
                            space.width - 1, fontsize / 2);
                    x.setColor(selectedColor2);
                    x.fillRect(start_x + 1, start_y + M_HEADER + 3 + i * fontsize + fontsize / 2, 
                            space.width - 1, fontsize / 2);
                    x.setColor(Color.black);
                }
                else
                    x.setColor(frameColor);
                x.drawLine(start_x,    start_y + M_HEADER + 2 + i * fontsize,
                        start_x + space.width, start_y + M_HEADER + 2 + i * fontsize);
                x.drawLine(start_x, start_y + M_HEADER + 2 + (i + 1) * fontsize, 
                        start_x + space.width, start_y + M_HEADER + 2 + (i + 1) * fontsize);
                
                if (col != 6 && col != 14) {
                    /* remove right border of rectangle */
                    if (event.isSelected()) {
                        c1 = selectedColor;
                        c2 = selectedColor2;
                    }
                    else
                        c1 = c2 = Color.white;
                    x.setColor(c1);
                    x.drawLine(start_x + space.width, start_y + M_HEADER + 3 + i * fontsize, 
                            start_x + space.width, start_y + M_HEADER + 3 + i * fontsize + fontsize / 2);
                    x.setColor(c2);
                    x.drawLine(start_x + space.width, start_y + M_HEADER + 3 + i * fontsize + fontsize / 2, 
                            start_x + space.width, start_y + M_HEADER + 1 + (i + 1) * fontsize);
                }
                if (col != 0 && col != 8) {
                    /* remove left border of rectangle */
                    if (event.isSelected()) {
                        c1 = selectedColor;
                        c2 = selectedColor2;
                    }
                    else
                        c1 = c2 = Color.white;
                    x.setColor(c1);
                    x.drawLine(start_x, start_y + M_HEADER + 3 + i * fontsize, 
                            start_x, start_y + M_HEADER + 3 + i * fontsize + fontsize / 2);
                    x.setColor(c2);
                    x.drawLine(start_x, start_y + M_HEADER + 3 + i * fontsize + fontsize / 2, 
                            start_x, start_y + M_HEADER + 1 + (i + 1) * fontsize);
                }
            }
        }
        
        /* single day events */
        else {
            /* mark as selected */
            if (event.isSelected()) {
                x.setColor(selectedColor);
                x.fillRect(start_x + 3, start_y + M_HEADER + 3 + i * fontsize, 
                        space.width - 5, fontsize / 2);
                x.setColor(selectedColor2);
                x.fillRect(start_x + 3, start_y + M_HEADER + 3 + i * fontsize + fontsize / 2, 
                        space.width - 5, fontsize / 2);
                x.setColor(Color.black);
            }
            else
                x.setColor(frameColor);
            x.drawRoundRect(start_x + 2, start_y + M_HEADER + 2 + i * fontsize, 
                    space.width - 4, fontsize, 2, 2);
        }
        
        if (event.isSelected())
            x.setColor(Color.black);
        else if (event.isSpecial() || event.isHoliday())
            x.setColor(holidayFtColor);
        else 
            x.setColor(ftColor);
        
        printEventNameMonthWeek(event, x, space, i);
    }
    
    /**
     * Paint an selectible event on the this cell in weekly view.
     * @param x - Graphics to paint on
     * @param i - Index of event to paint
     * @param space - Available space of this cell
     */
    private void paintEventWeek(Graphics x, int i, Dimension space) {
        int start_x = W_CLEAR_LEFT + col * space.width;
        int start_y = W_CLEAR_UP + row * space.height;
        Color c1, c2;
        boolean rem_left_border = false, rem_right_border = false;
        Event event = events.elementAt(i);
        
        /* group multi-day events */
        if (event.getEndDate() != null) {
            if (event.getDate().sameDateAs(this.date)) {
                /* mark as selected */
                if (event.isSelected()) {
                    x.setColor(selectedColor);
                    x.fillRect(start_x + 3, start_y + 3 + i * fontsize + W_HEADER, 
                            space.width - 3, fontsize / 2);
                    x.setColor(selectedColor2);
                    x.fillRect(start_x + 3, start_y + 3 + i * fontsize + fontsize / 2 + W_HEADER, 
                            space.width - 3, fontsize / 2);
                }
                
                if (event.isSelected())
                    x.setColor(Color.black);
                else
                    x.setColor(frameColor);
                x.drawRoundRect(start_x + 2, start_y + 2 + W_HEADER + i * fontsize, 
                        space.width - 2, fontsize, 2, 2);
                
                if (col != 6)
                    rem_right_border = true;
            }
            else if (event.getEndDate().sameDateAs(this.date)) {
                if (event.isSelected()) {
                    x.setColor(selectedColor);
                    x.fillRect(start_x + 1, start_y + 3 + W_HEADER + i * fontsize, 
                            space.width - 2, fontsize / 2);
                    x.setColor(selectedColor2);
                    x.fillRect(start_x + 1, start_y + 3 + W_HEADER + i * fontsize + fontsize / 2, 
                            space.width - 2, fontsize / 2);
                }
                
                if (event.isSelected())
                    x.setColor(Color.black);
                else
                    x.setColor(frameColor);
                x.drawRoundRect(start_x, start_y + 2 + W_HEADER + i * fontsize, 
                        space.width - 2, fontsize, 2, 2);
                
                if (col != 0)
                    rem_left_border = true;
            }
            else {
                if (event.isSelected()) {
                    x.setColor(selectedColor);
                    x.fillRect(start_x + 1, start_y + 3 + W_HEADER + i * fontsize, 
                            space.width - 1, fontsize / 2);
                    x.setColor(selectedColor2);
                    x.fillRect(start_x + 1, start_y + 3 + W_HEADER + i * fontsize + fontsize / 2, 
                            space.width - 1, fontsize / 2);
                }
                
                if (event.isSelected())
                    x.setColor(Color.black);
                else
                    x.setColor(frameColor);
                x.drawLine(start_x, start_y + 2 + W_HEADER + i * fontsize, 
                        start_x + space.width, start_y + 2 + W_HEADER + i * fontsize);
                x.drawLine(start_x, start_y + 2 + W_HEADER + (i + 1) * fontsize, 
                        start_x + space.width, start_y + 2 + W_HEADER + (i + 1) * fontsize);
                
                if (col != 6)
                    rem_right_border = true;
                if (col != 0)
                    rem_left_border = true;
            }
            
            if (rem_right_border) {
                /* remove right border of rectangle */
                if (event.isSelected()) {
                    c1 = selectedColor;
                    c2 = selectedColor2;
                }
                else
                    c1 = c2 = Color.white;
                x.setColor(c1);
                x.drawLine(start_x + space.width, start_y + 3 + W_HEADER + i * fontsize, 
                        start_x + space.width, start_y + W_HEADER + i * fontsize + fontsize / 2 + 2);
                x.setColor(c2);
                x.drawLine(start_x + space.width, start_y + 3 + W_HEADER + i * fontsize + fontsize / 2, 
                        start_x + space.width, start_y + 1 + W_HEADER + (i + 1) * fontsize);
            }
            if (rem_left_border) {
                /* remove left border of rectangle */
                if (event.isSelected()) {
                    c1 = selectedColor;
                    c2 = selectedColor2;
                }
                else
                    c1 = c2 = Color.white;
                x.setColor(c1);
                x.drawLine(start_x, start_y + 3 + W_HEADER + i * fontsize, 
                        start_x, start_y + 1 + W_HEADER + i * fontsize + fontsize / 2 + 1);
                x.setColor(c2);
                x.drawLine(start_x, start_y + 3 + W_HEADER + i * fontsize + fontsize / 2, 
                        start_x, start_y + 1 + W_HEADER + (i + 1) * fontsize);
            }
        }
        
        /* single day events */
        else {
            /* mark as selected */
            if (event.isSelected()) {
                x.setColor(selectedColor);
                x.fillRect(start_x + 3, start_y + 3 + W_HEADER + i * fontsize, 
                        space.width - 5, fontsize / 2);
                x.setColor(selectedColor2);
                x.fillRect(start_x + 3, start_y + 3 + W_HEADER + i * fontsize + fontsize / 2,
                        space.width - 5, fontsize / 2);
            }
            
            if (event.isSelected())
                x.setColor(Color.black);
            else
                x.setColor(frameColor);
            x.drawRoundRect(start_x + 2, start_y + 2 + W_HEADER + i * fontsize, 
                    space.width - 4, fontsize, /*fontsize /*/ 2, /*fontsize /*/ 2);
        }
        
        if (event.isSelected())
            x.setColor(Color.black);
        else if (event.isSpecial() || event.isHoliday())
            x.setColor(holidayFtColor);
        else        
            x.setColor(ftColor);
        
        printEventNameMonthWeek(event, x, space, i);
    }
    
    private void paintEventDay(Graphics x, int i, Dimension space) {
        Event event = events.get(i);
        int start_x = D_CLEAR_LEFT + col * space.width;
        int start_y = D_CLEAR_UP + row * space.height;
        Color c1, c2;
        
        x.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontsize));
        
        for (int j = 0; j < i; j++)
            start_x += GraphicUtils.getStringWidth(x, events.get(j).getName()) + 4;
        
        int width = GraphicUtils.getStringWidth(x, event.getName());
        if (start_x + width > space.width);
            //TODO
        
        if (event.isSelected()) {
            c1 = selectedColor;
            c2 = selectedColor2;
        }
        else
            c1 = c2 = Color.white;
        
        x.setColor(c1);
        x.fillRect(start_x + 2, start_y + 2, width + 1, space.height / 2);
        x.setColor(c2);
        x.fillRect(start_x + 2, start_y + space.height / 2, width + 1, space.height / 2);
        
        if (event.isSelected())
            x.setColor(Color.black);
        else 
            x.setColor(frameColor);
        x.drawRoundRect(start_x + 2, start_y + 1, width + 1, space.height - 2, 3, 3);
        x.drawString(event.getName(), start_x + 3, start_y + space.height - 2);
    }
    
    /**
     * Paint this cell in yearly view on a canvas.
     * @param x - Graphics object to paint on
     * @param space - Available space
     */
    private void paintYear(Graphics x, Dimension space) {
        if (date != null) {
            
            /* adapt font size */
            fontsize = 9;
            if (space.width >= 14 && space.height >= 22)
                fontsize = 10;
            if (space.width >= 14 && space.height >= 24)
                fontsize = 11;
            if (space.width >= 16 && space.height >= 24)
                fontsize = 12;
            if (space.width >= 18 && space.height >= 25)
                fontsize = 13;
            if (space.width >= 18 && space.height >= 27)
                fontsize = 14;
            if (space.width >= 18 && space.height >= 29)
                fontsize = 15;
            
            Color c1, c2;
            final int HEADER_HEIGHT = space.height - fontsize;

            /* paint holiday over weekend */
            if (holiday) {
                c1 = holidayBgColor;
                c2 = ColorUtil.addSaturation(c1, _SATURATION);
            }
            else if (weekend) {
                c1 = weekendColor;
                c2 = ColorUtil.addSaturation(c1, _SATURATION);
            }
            else
                c1 = c2 = Color.white;
            
            x.setColor(c1);
            x.fillRect(Y_CLEAR_LEFT + 1 + col * space.width, Y_CLEAR_UP
                    + 1 + row * space.height, space.width - 1, space.height / 2);
            x.setColor(c2);
            x.fillRect(Y_CLEAR_LEFT + 1 + col * space.width, Y_CLEAR_UP
                    + 1 + row * space.height + space.height / 2, space.width - 1, space.height / 2);

            
            /* paint selection over today */
            if (selected) {
                c1 = selectedColor;
                c2 = selectedColor2;
            }
            else if (today) {
                c1 = todayColor;
                c2 = ColorUtil.addSaturation(c1, _SATURATION);
            }
            
            if (selected || today) {
                x.setColor(c1);
                x.fillRect(Y_CLEAR_LEFT + 1 + col * space.width, Y_CLEAR_UP
                        + HEADER_HEIGHT + row * space.height, space.width - 1, (space.height - HEADER_HEIGHT) / 2);
                x.setColor(c2);
                x.fillRect(Y_CLEAR_LEFT + 1 + col * space.width, Y_CLEAR_UP
                        + HEADER_HEIGHT + (space.height - HEADER_HEIGHT) / 2 + row * space.height, space.width - 1, (space.height - HEADER_HEIGHT) / 2);

            }

            /* print number of day */
            x.setColor(Color.black);
            x.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontsize));
            x.drawString("" + date.get(Calendar.DAY_OF_MONTH), Y_CLEAR_LEFT
                    + col * space.width, Y_CLEAR_UP + (row + 1) * space.height);

            /* print number of events */
            if (events.size() > 0) {
                x.setColor(ftColor);
                x.drawString("(" + events.size() + ")", Y_CLEAR_LEFT + col * space.width, 
                        Y_CLEAR_UP + (row + 1) * space.height - space.height / 2);
            }
        }
    }
    
    /**
     * Paint this cell in monthly view on a canvas.
     * @param x - Graphics object to paint on
     * @param space - Available space
     */
    private void paintMonth(Graphics x, Dimension space) {
        if (date == null)
            return;

        Color c1, c2;
        int start_x = M_CLEAR_LEFT + col * space.width;
        int start_y = M_CLEAR_UP + row * space.height;
            
        /* paint cell frame */
        x.setColor(Const.COLOR_DEF_FONT);
        x.drawRect(start_x, start_y, space.width, space.height);
        
        /* paint selected or white ground */
        if (selected) {
            c1 = selectedColor;
            c2 = selectedColor2;
        }
        else
            c1 = c2 = Color.white;
        x.setColor(c1);
        x.fillRect(start_x + 1, start_y + 1, space.width - 1, 13);
        x.setColor(c2);
        x.fillRect(start_x + 1, start_y + 14, space.width - 1, 12);

        /* paint holiday over weekend */
        if (holiday) {
            c1 = holidayBgColor;
            c2 = ColorUtil.addSaturation(c1, _SATURATION);
        }
        else if (weekend) {
            c1 = weekendColor;
            c2 = ColorUtil.addSaturation(c1, _SATURATION);
        }
        else
            c1 = c2 = Color.white;
        x.setColor(c1);
        x.fillRect(start_x + 6, start_y + 6, space.width - 11, 8);
        x.setColor(c2);
        x.fillRect(start_x + 6, start_y + 14, space.width - 11, 7);

        if (today)
            c1 = todayColor;
        else
            c1 = Color.white;
        x.setColor(c1);
        x.fillRect(start_x + 1, start_y + M_HEADER + 1, 
                space.width - 1, space.height - (M_HEADER + 1));

        /* print number of day */
        x.setColor(Color.black);
        x.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        x.drawString("" + date.get(Calendar.DAY_OF_MONTH), start_x + 7, 
                start_y + 20);
        
        Color moonBg = holiday ? holidayBgColor : weekend ? weekendColor : Color.white;
        Moon.paint(moonPhase, x, //moonSelected ? canvas.getOwner().getConfig().getColors()[ColorSet.SELECTED] : 
            Color.gray, moonBg, new Point(start_x + space.width - 14, start_y + 12), 8);

        /* print events */
        if (space.height > 72)
            fontsize = 13;
        else if (space.height > 62)
            fontsize = 12;
        else if (space.height > 52)
            fontsize = 11;
        else
            fontsize = 10;
        
        max_lines = (space.height - 27) / fontsize;
        if (max_lines < 1)
            return;

        sortEventsByLength();
        x.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontsize));
        x.setColor(ftColor);
        int selectedIndex = -1;
        for (int i = 0; i < events.size(); i++) {
            
            /* print +<mun> if not enough space for the remaining events */
            if (i == max_lines - 1 && i != events.size() - 1) {
                x.drawString("+" + (events.size() - i), start_x + 1, 
                        start_y + 26 + (i + 1) * fontsize);
                break;
            }
            
            if (events.elementAt(i).isSelected()) {
                selectedIndex = i;
                continue;
            }
            
            paintEventMonth(x, i, space);
        }
        
        if (selectedIndex != -1)
            paintEventMonth(x, selectedIndex, space);
    }
    
    /**
     * Paint this cell in weekly view on a canvas.
     * @param x - Graphics object to paint on
     * @param space - Available space
     */
    private void paintWeek(Graphics x, Dimension space) {
        int start_x = W_CLEAR_LEFT + col * space.width;
        int start_y = W_CLEAR_UP + row * space.height;
        Color bgcolor;
        
        x.setColor(Color.white);
        x.fillRect(start_x + 1, start_y + W_HEADER + 1, space.width - 1,
                space.height - W_HEADER - 1);

        /* print holiday over weekend */
        if (selected)
            x.setColor(selectedColor);
        else if (holiday)
            x.setColor(holidayBgColor);
        else if (weekend)
            x.setColor(weekendColor);
        else
            x.setColor(Color.white);
        bgcolor = x.getColor();
        
        x.fillRect(start_x + 1, start_y + 1, space.width - 1, W_HEADER - 1);

        if (today) {
            x.setColor(todayColor);
            x.fillRect(start_x + 1, start_y + 1 + W_HEADER,
                    space.width - 1, space.height - W_HEADER - 1);
        }
        
        Moon.paint(moonPhase, x, Color.gray, today ? todayColor : Color.white, 
                new Point(start_x + space.width - 12, start_y + space.height - 12), 10);

        /* print date */
        x.setColor(Color.black);
        x.setFont(W_HEADER_FONT);
        x.drawString(date.dateToString(false), start_x + 2,
                start_y + (W_HEADER / 2) - 4);

        /* print events */
        fontsize = space.height / 10;
        if (fontsize > 17)
            fontsize = 17;
        else if (fontsize < 10)
            fontsize = 10;
        max_lines = (space.height - W_HEADER) / fontsize;
        if (moonPhase != Moon.MOON_NONE)
            max_lines--;

        sortEventsByLength();
        x.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontsize));
        x.setColor(ftColor);
        int selectedIndex = -1;
        for (int i = 0; i < events.size(); i++) {
            if (i == max_lines - 1 && i != events.size() - 1) {
                x.drawString("+" + (events.size() - i), start_x + 1, 
                        start_y + W_HEADER + (i + 1) * fontsize);
                break;
            }
            
            if (events.elementAt(i).isHoliday() || events.elementAt(i).isSpecial()) {
                x.drawImage(BALLOONS_20, start_x + 1, start_y + W_HEADER / 2 - 2, 
                        bgcolor, canvas);
                x.setFont(W_HEADER_FONT);
                x.setColor(holidayFtColor);
                x.drawString(cut(events.elementAt(i).getName(), space.width - 20, x), 
                        start_x + 21, start_y + W_HEADER - 5);
                x.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontsize));
            }
            else if (events.elementAt(i).isSelected())
                selectedIndex = i;
            else
                paintEventWeek(x, i, space);
        }
        
        if (selectedIndex != -1)
            paintEventWeek(x, selectedIndex, space);
    }
    
    private void paintDayHeader(Graphics x, int width) {
        if (selected)
            x.setColor(selectedColor);
        else
            x.setColor(Color.white);
        
        x.fillRect(D_CLEAR_LEFT + 1 + col * width, D_CLEAR_UP - 18, 279, 19);
        x.fillPolygon(
                new int[]{D_CLEAR_LEFT + 280 + col * width, D_CLEAR_LEFT + 280 + col * width, D_CLEAR_LEFT + 299 + col * width},
                new int[]{D_CLEAR_UP - 18, D_CLEAR_UP + 1, D_CLEAR_UP + 1}, 3);
        
        String header = "";
        long dayDiff = this.date.dayDiff(new Date());
        if (dayDiff == 0)
            header += "Heute: ";
        else if (dayDiff == -1)
            header += "Gestern: ";
        else if (dayDiff == 1)
            header += "Morgen: ";
        else if (dayDiff == 2)
            header += "Übermorgen: ";
        header += Date.dayOfWeek2String(date.get(java.util.Calendar.DAY_OF_WEEK), false);
        header += ", " + date.dateToString(false);
        
        x.setColor(Color.black);
        x.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        x.drawString(header, D_CLEAR_LEFT + col * width + 3, D_CLEAR_UP - 4);
    }
    
    /**
     * Paint this cell in daily view on a canvas.
     * @param x - Graphics object to paint on
     * @param space - Available space
     */
    private void paintDay(Graphics x, Dimension space) {
        Color bgColor;
        if (selected)
            bgColor = selectedColor;
        else
            bgColor = Color.white;
        x.setColor(bgColor);
        x.fillRect(D_CLEAR_LEFT + 1 + col * space.width, D_CLEAR_UP + 1
                + row * space.height, space.width - 1, space.height - 1);
        
        if (row == 0)
            paintDayHeader(x, space.width);

        fontsize = space.height - 1;
        x.setColor(ftColor);
        x.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontsize));
//        String desc = "";
        for (int i = 0; i < events.size(); i++)
            paintEventDay(x, i, space);
//            desc += e.getName() + " ::: ";
//        if (!desc.equals("")) {
//            desc = desc.substring(0, desc.length() - 5);
//            x.drawString(cut(desc, space.width, x), D_CLEAR_LEFT + 3 + col
//                    * space.width, D_CLEAR_UP + (row + 1) * space.height - 1);
//        }
    }
    
    /**
     * Initialize colors according to the current config.
     */
    private void initColors() {
        weekendColor = canvas.getOwner().getConfig().getColors()[ColorSet.WEEKEND];
        holidayBgColor = canvas.getOwner().getConfig().getColors()[ColorSet.HOLIDAY];
        holidayFtColor = canvas.getOwner().getConfig().getColors()[ColorSet.FONT_HOLIDAY];
        todayColor = canvas.getOwner().getConfig().getColors()[ColorSet.TODAY];
        ftColor = canvas.getOwner().getConfig().getColors()[ColorSet.FONT];
        selectedColor = canvas.getOwner().getConfig().getColors()[ColorSet.SELECTED];
        selectedColor2 = ColorUtil.addSaturation(selectedColor, _SATURATION);
    }

    /**
     * Paint this cell on a canvas.
     * @param x - Graphics object to paint on
     * @param space - Available space
     */
    public void paint(Graphics x, Dimension space) {
        initColors();
        
        if (canvas.getView() == Configuration.VIEW_YEAR)
            paintYear(x, space);
        else if (canvas.getView() == Configuration.VIEW_MONTH)
            paintMonth(x, space);
        else if (canvas.getView() == Configuration.VIEW_WEEK)
            paintWeek(x, space);
        else if (canvas.getView() == Configuration.VIEW_DAY)
            paintDay(x, space);
    }

    /**
     * Cuts a string to fit a specific width. If the string needs to be cut,
     * '...' will be appended.
     * @param orig - Original string
     * @param width - Available width
     * @param g - Graphics object (contains the font to apply)
     * @return Original string if enough space available, cutted
     * string otherwise.
     */
    public static String cut(String orig, int width, Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        String tmp = orig;
        int len = orig.length();
        while (fm.stringWidth(tmp) > width - 2 && len > 0)
            tmp = orig.substring(0, --len) + "...";
        return tmp.equals("...") ? orig.substring(0, 1) : tmp;
    }
    
    /**
     * Swap two events.
     * @param x - Index of first events
     * @param y - Index of second events
     */
    private void swap(int x, int y) {
        Event tmp = events.elementAt(x);
        events.set(x, events.elementAt(y));
        events.set(y, tmp);
    }
    
    /**
     * Sort the events list so that longest events (in terms of
     * distance between start- and enddate) occur first.
     * Also move holidays to the end.
     */
    private void sortEventsByLength() {
        long max_length = 0;
        for (int i = 0; i < events.size(); i++) {
            if (events.elementAt(i).getEndDate() != null) {
                long length = events.elementAt(i).getEndDate().getTimeInMillis() - 
                        events.elementAt(i).getDate().getTimeInMillis();
                if (length > max_length) {
                    swap(0, i);
                    max_length = length;
                }
            }
            else if (events.elementAt(i).isHoliday() || events.elementAt(i).isSpecial())
                swap(i, events.size() - 1);
        }
    }
}