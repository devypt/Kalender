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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Logger;

import de.jsteltze.calendar.Calendar;
import de.jsteltze.calendar.Event;
import de.jsteltze.calendar.config.Configuration;
import de.jsteltze.calendar.config.Const;
import de.jsteltze.calendar.frames.EditEvent;
import de.jsteltze.calendar.frames.Notification;
import de.jsteltze.calendar.frames.TabelOfEventsMulitDay;
import de.jsteltze.calendar.frames.TableOfEventsSingleDay;
import de.jsteltze.common.GraphicUtils;
import de.jsteltze.common.calendar.Date;
import de.jsteltze.common.calendar.Moon;

/**
 * Canvas on which to paint calendar contents.
 * @author Johannes Steltzer
 *
 */
public class CalendarCanvas 
    extends JComponent 
    implements MouseListener, MouseMotionListener, KeyListener,
            PopupMenuListener, ActionListener {
    
    private static final long serialVersionUID = 1L;
    
    /** in case of dragged mouse movement: starting position */
    private Point mouseSrc;
    
    /** in case of dragged mouse movement: reverse movement */
    private boolean reverse;
    
    /** is ctrl pressed? */
    private boolean strgPressed;
    
    /** selected dates (cells) */
    private Vector<Date> markedDates; 
    
    /**
     * All cells of this current view. Each view can be
     * divided into cells, arranged as a table. Each
     * cell may represent a date. This makes it easier
     * to address a single date or a set of dates.
     */
    private Cell[][] matrix;
    
    /**
     * The cell the mouse is currently over This is just
     * to prevent the program from repainting all the
     * time, when the mouse moves but not leaves a cell
     */
    private Cell mouseHover;
    
    /** year, month, week or day (see Configuration.VIEW_XXXX) */
    private byte view;
    
    /** dimension for 'matrix' depending on selected view */
    private int rows, cols;
    
    /** clearance to upper and left window border */
    private int clear_up, clear_left;
    
    /** dimension of a single cell */
    private int width, height;
    
    /** parent calendar object */
    private Calendar calendar;
    
    /** currently selected event */
    private Event selectedEvent;
    
    /** copied event (clipboard) */
    private Event copiedEvent;
    
    /** cutted event (clipboard) */
    private Event cuttedEvent;
    
    /** currently highlighted headline */
    private int highlightedHeadline, highlightedHeadlineEnd;
    
    /** length of the two month names in px */
    private int lenMonthName[] = new int[2];
    
    /** fontsize for yearly view */
    private int fontsizeYear;
    
    /** Popup menu */
    private JPopupMenu popup;
    
    /** Popup menu items */
    private JMenuItem newItem, overviewItem, editItem, 
            deleteItem, remindItem, showAttachmentItem,
            showNotesItem, copyItem, pasteItem, cutItem;
    
    private static Logger logger = Logger.getLogger(CalendarCanvas.class);

    /**
     * 
     * @return Parent calendar object.
     */
    public Calendar getOwner() {
        return this.calendar;
    }

    /**
     * 
     * @return Current kind of view (year, month, week, day). 
     *         See Configuration.VIEW_XXXX.
     */
    public byte getView() {
        return this.view;
    }

    /**
     * Change view.
     * @param x - Year, month, week or day (see Configuration.VIEW_XXXX)
     */
    public void setView(byte x) {
        this.view = x;
        if (this.view == Configuration.VIEW_YEAR) {
            rows = Cell.Y_ROWS;
            cols = Cell.Y_COLS;
            clear_up = Cell.Y_CLEAR_UP;
            clear_left = Cell.Y_CLEAR_LEFT;
        } 
        else if (this.view == Configuration.VIEW_MONTH) {
            rows = Cell.M_ROWS;
            cols = Cell.M_COLS;
            clear_up = Cell.M_CLEAR_UP;
            clear_left = Cell.M_CLEAR_LEFT;
        } 
        else if (this.view == Configuration.VIEW_WEEK) {
            rows = Cell.W_ROWS;
            cols = Cell.W_COLS;
            clear_up = Cell.W_CLEAR_UP;
            clear_left = Cell.W_CLEAR_LEFT;
        } 
        else if (this.view == Configuration.VIEW_DAY) {
            rows = Cell.D_ROWS;
            cols = Cell.D_COLS;
            clear_up = Cell.D_CLEAR_UP;
            clear_left = Cell.D_CLEAR_LEFT;
        }
        init();
        repaint();
    }

    /**
     * Initialize values, when constructed or when view changed.
     */
    private void init() {
        this.mouseSrc = null;
        this.reverse = false;
        this.strgPressed = false;
        this.markedDates = new Vector<Date>();
        this.matrix = null;
        this.highlightedHeadline = -1;
        this.selectedEvent = null;
        this.copiedEvent = null;
    }

    /**
     * Construct new calendar canvas.
     * @param c - Parent calendar object.
     * @param view - View to start with (Year, month, week or day).
     * See Configuration.VIEW_XXX.
     */
    public CalendarCanvas(Calendar c, byte view) {
//        super();
//        this.setIgnoreRepaint(false);
//        this.setOpaque(true);
        calendar = c;
        setView(view);
        
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);
    }

    /**
     * Calculates the dimension of a single cell, pending on the 
     * current view and the window size. Sets the global variables 
     * width and height.
     * @return Dimension calculated for a single cell.
     */
    public Dimension calcDim() {
        if (this.view == Configuration.VIEW_YEAR) {
            int SPACE_FOR_KW = 29;
            if (this.getHeight() > 440)
                SPACE_FOR_KW = 39;
            else if (this.getHeight() > 420)
                SPACE_FOR_KW = 37;
            else if (this.getHeight() > 400)
                SPACE_FOR_KW = 35;
            else if (this.getHeight() > 380)
                SPACE_FOR_KW = 33;
            else if (this.getHeight() > 360)
                SPACE_FOR_KW = 31;

            width = this.getWidth() - clear_left - SPACE_FOR_KW;
            height = this.getHeight() - clear_up - 6;
        } 
        else if (this.view == Configuration.VIEW_MONTH) {
            width = this.getWidth() - clear_left - 30;
            height = this.getHeight() - clear_up - 5;
        } 
        else if (this.view == Configuration.VIEW_WEEK) {
            width = this.getWidth() - clear_left - 80;
            height = this.getHeight() - clear_up - 5;
        } 
        else if (this.view == Configuration.VIEW_DAY) {
            width = this.getWidth() - clear_left - 5;
            height = this.getHeight() - clear_up - 5;
        }
        
        width /= cols;
        height /= rows;
        return new Dimension(width, height);
    }

//    @Override
//    public void paint(Graphics g) {
//        drawCalendar(g);
//        fillCalendar(g);
//    }
    
//    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
//        g.setColor(Color.red);
//        g.fillRect(0, 0, this.getWidth(), this.getHeight());
//        g.setColor(Color.BLACK);
//        g.drawString("HALLO", 100, 100);
        logger.debug("paintComponent");
        drawCalendar(g);
        fillCalendar(g);
        
        if (popup != null)
            popup.repaint();
    }
    
//    @Override
//    public void paintComponents(Graphics g) {
//        drawCalendar(g);
//        fillCalendar(g);
//    }
//    
//    @Override
//    public void paintAll(Graphics g) {
//        drawCalendar(g);
//        fillCalendar(g);
//    }
    
    /**
     * Draw the basic structure of the calendar in yearly view.
     * @param g - Graphics to paint on
     */
    private void drawCalendarYear(Graphics g) {
        /*
         * Determine font size
         */
        if (this.getHeight() > 440)
            fontsizeYear = 14;
        else if (this.getHeight() > 420)
            fontsizeYear = 13;
        else if (this.getHeight() > 400)
            fontsizeYear = 12;
        else if (this.getHeight() > 380)
            fontsizeYear = 11;
        else if (this.getHeight() > 360)
            fontsizeYear = 10;
        else
            fontsizeYear = 9;
        
        g.setColor(Const.COLOR_CANVAS_FRAMES);
        for (int i = 0; i < cols; i++)
            for (int j = 0; j < rows; j++)
                g.drawRect(clear_left + i * width, clear_up + j * height,
                        width, height);
        g.setColor(Color.BLACK);

        /*
         * Print name of month
         */
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        int spaces_left[] = {6, 5, 5, 7, 7, 6, 10, 5, 4, 7, 4, 4};
        for (int i = 0; i < rows;)
            g.drawString(Date.month2String(i, true), spaces_left[i], 
                    clear_up - 5 + height * (++i));

        /*
         * Print week days (Mo-So)
         */
        g.setColor(Const.COLOR_DEF_FONT);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontsizeYear));
        for (int i = 0; i < cols; i++)
            g.drawString(Date.dayOfWeek2String(Date.germanOrder2javaUtilOrder(i % 7), true), 
                    clear_left + i * width, clear_up - 2);
        g.drawString("KWs", clear_left + cols * width + 1, clear_up - 2);
    }
    
    /**
     * Draw the basic structure of the calendar in monthly view.
     * @param g - Graphics to paint on
     */
    private void drawCalendarMonth(Graphics g) {
        g.setColor(Const.COLOR_DEF_FONT);
        g.setFont(Const.FONT_MONTH_HEADERS);
        g.drawString("KW", clear_left + 7 * width + 5, clear_up - 5);
        g.drawString("KW", clear_left + cols * width + 5, clear_up - 5);
        g.setColor(Color.BLACK);
        for (int j = 0; j < 2; j++)
            for (int i = 0; i < 7; i++)
                g.drawString(Date.dayOfWeek2String(Date.germanOrder2javaUtilOrder(i), true),
                        10 + i * width + j * 8 * width, clear_up - 5);
        g.setColor(Const.COLOR_CANVAS_FRAMES);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                if (j != 7)
                    g.drawRect(clear_left + j * width, clear_up + i
                            * height, width, height);
    }
    
    /**
     * Draw the basic structure of the calendar in weekly view.
     * @param g - Graphics to paint on
     */
    private void drawCalendarWeek(Graphics g) {
        g.setColor(Const.COLOR_DEF_FONT);
        for (int i = 0; i < rows; i++)
            g.drawRect(clear_left, clear_up + i * height, cols * width, Cell.W_HEADER);
        g.setColor(Color.black);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                g.drawRect(clear_left + j * width, clear_up + i * height,
                        width, height);
        g.setColor(Const.COLOR_DEF_FONT);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        for (int i = 0; i < cols; i++)
            g.drawString(Date.dayOfWeek2String(Date.germanOrder2javaUtilOrder(i), false), 
                    clear_left + 2 + i * width, clear_up - 5);
        g.drawString("Woche", clear_left + 5 + cols * width, clear_up - 5);
        for (int i = 0; i < rows; i++)
            g.fillPolygon(
                    new int[]{clear_left + 5 + cols * width, clear_left + 10 + cols * width, clear_left + 10 + cols * width},
                    new int[]{clear_up + 11 + i * height, clear_up + 6 + i * height, clear_up + 16 + i * height}, 
                    3);
    }
    
    /**
     * Draw the basic structure of the calendar in daily view.
     * @param g - Graphics to paint on
     */
    private void drawCalendarDay(Graphics g) {
        g.setColor(Const.COLOR_CANVAS_FRAMES);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                g.drawRect(clear_left + j * width, clear_up + i * height,
                        width, height);
        
        g.drawRect(clear_left, clear_up - 19, 280, 20);
        g.drawRect(clear_left + width, clear_up - 19, 280, 20);
        g.drawLine(clear_left + 279, clear_up - 19, clear_left + 298, clear_up);
        g.drawLine(clear_left + width + 279, clear_up - 19, clear_left + width + 298, clear_up);
        
        g.setColor(Const.COLOR_DEF_FONT);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        for (int i = 0; i < 24; i++)
            g.drawString(i + ":00", (i < 10) ? 9 : 3, clear_up + (i + 1)
                    * height + 10);
        
//        g.setColor(Color.white);
//        g.fillRect(clear_left + 1, clear_up - 18, 280, 20);
//        g.fillRect(clear_left + width + 1, clear_up - 18, 280, 20);
//
//        g.setColor(Const.COLOR_CANVAS_FRAMES);
//        
//        g.setColor(Color.white);
//        g.fillPolygon(
//                new int[]{clear_left + 280, clear_left + 280, clear_left + 299},
//                new int[]{clear_up - 18, clear_up + 1, clear_up + 1}, 3);
//        g.fillPolygon(
//                new int[]{clear_left + width + 280, clear_left + + width + 280, clear_left + width + 299},
//                new int[]{clear_up - 18, clear_up + 1, clear_up + 1}, 3);
    }

    /**
     * Draw the basic structure of the calendar, depending on the 
     * current view, but independent from specific day or month.
     * @param g - Graphics to paint on
     */
    private void drawCalendar(Graphics g) {
        calcDim();

        if (this.view == Configuration.VIEW_YEAR)
            drawCalendarYear(g);
        else if (this.view == Configuration.VIEW_MONTH)
            drawCalendarMonth(g);
        else if (this.view == Configuration.VIEW_WEEK)
            drawCalendarWeek(g);
        else if (this.view == Configuration.VIEW_DAY)
            drawCalendarDay(g);
    }
    
    /**
     * Reset the currently highlighted label in monthly view.
     * @param g - Graphics object to paint on
     */
    private void resetHeadlineMonth(Graphics g) {
        if (highlightedHeadline == -1)
            return;

        /* unmark day of week */
        if (highlightedHeadline < 15) {
            g.setColor(Const.COLOR_BG_MAIN);
            g.fillRect(10 + highlightedHeadline * width, clear_up - 17, 25, 17);
            g.setColor(Color.BLACK);
            g.setFont(Const.FONT_MONTH_HEADERS);
            g.drawString(Date.dayOfWeek2String(Date.germanOrder2javaUtilOrder(highlightedHeadline > 6 ? highlightedHeadline - 8 : highlightedHeadline), true),
                    10 + highlightedHeadline * width, clear_up - 5);
        }
        
        /* unmark calendar week */
        else if (highlightedHeadline < 40) {
            int row_index = highlightedHeadlineEnd == -1 ? highlightedHeadline : highlightedHeadlineEnd;
            boolean rightSite = row_index >= 30;
            row_index -= 20;
            if (rightSite)
                row_index -= 10;
            int week;
            if (matrix[rightSite ? 8 : 0][row_index] != null && matrix[rightSite ? 8 : 0][row_index].getDate() != null)
                week = matrix[rightSite ? 8 : 0][row_index].getDate().get(java.util.Calendar.WEEK_OF_YEAR);
            else if (matrix[rightSite ? 14 : 6][row_index] != null && matrix[rightSite ? 14 : 6][row_index].getDate() != null)
                week = matrix[rightSite ? 14 : 6][row_index].getDate().get(java.util.Calendar.WEEK_OF_YEAR);
            else 
                return;
            g.setColor(Const.COLOR_BG_MAIN);
            g.fillRect(clear_left + (rightSite ? 15 : 7) * width + 5, row_index * height + clear_up + 5, 20, 20);
            g.setColor(Const.COLOR_DEF_FONT);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            g.drawString("" + week, clear_left + (rightSite ? 15 : 7) * width + 5, row_index * height + clear_up + 25);
        }
        
        /* unmark month name */
        else if (highlightedHeadline == 50 || highlightedHeadline == 60) {
            g.setColor(Const.COLOR_BG_MAIN);
//            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            String line = Date.month2String(matrix[highlightedHeadline == 50 ? 3 : 10][3].getDate().get(java.util.Calendar.MONTH), false) +
            " " + matrix[highlightedHeadline == 50 ? 3 : 10][3].getDate().get(java.util.Calendar.YEAR);
//            g.drawString(line, (highlightedHeadline == 60 ? 8 * width : 0) + clear_left + 5, clear_up - 24);
            
            int start = (highlightedHeadline == 60 ? 8 * width : 0) + clear_left - 2;
            int widthHeadline = GraphicUtils.getStringWidth(g, new Font(Font.SANS_SERIF, Font.BOLD, 20), line) 
                    + start + 4;

            g.fillPolygon(
                    new int[]{start + 5, start, start},
                    new int[]{clear_up - 30, clear_up - 35, clear_up - 25}, 3);
            g.fillPolygon(
                    new int[]{widthHeadline + 5, widthHeadline + 10, widthHeadline + 10},
                    new int[]{clear_up - 30, clear_up - 35, clear_up - 25}, 3);

        }
        highlightedHeadline = -1;
    }
    
    /**
     * Reset the currently highlighted label in yealy view.
     * @param g - Graphics object to paint on
     */
    private void resetHeadlineYear(Graphics g) {
        if (highlightedHeadline == -1)
            return;
        
        /* unmark month */
        if (highlightedHeadline < 12) {
            int spaces_left[] = {6, 5, 5, 7, 7, 6, 10, 5, 4, 7, 4, 4};
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.setColor(Const.COLOR_BG_MAIN);
            g.fillRect(0, clear_up + height * highlightedHeadline, 
                    clear_left, height);
            g.setColor(Color.black);
            g.drawString(Date.month2String(highlightedHeadline, true), spaces_left[highlightedHeadline], 
                        clear_up - 5 + height * (highlightedHeadline + 1));
        }
        
        /* unmark calendar weeks */
        else if (highlightedHeadline < 24) {
            int kw_start, kw_end, i = 0;
            int index = highlightedHeadline - 12;
            while (matrix[i][index] == null || matrix[i][index].getDate() == null)
                i++;
            kw_start = matrix[i][index].getDate().get(java.util.Calendar.WEEK_OF_YEAR);
            i = cols - 1;
            while (matrix[i][index] == null || matrix[i][index].getDate() == null)
                i--;
            kw_end = matrix[i][index].getDate().get(java.util.Calendar.WEEK_OF_YEAR);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontsizeYear));
            g.setColor(Const.COLOR_BG_MAIN);
            g.fillRect(clear_left + cols * width + 1, clear_up + height * index, 
                    50, height);
            g.setColor(Const.COLOR_DEF_FONT);
            
            g.drawString("" + kw_start + "-" + kw_end,
                    cols * width + clear_left + 3, clear_up - 2
                            + (index + 1) * height);
        }
        
        /* unmark day of week */
        else if (highlightedHeadline < 30 + cols) {
            int index = highlightedHeadline - 30;
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontsizeYear));
            int base = index % 7;
            while (base < cols) {
                g.setColor(Const.COLOR_BG_MAIN);
                g.fillRect(clear_left + base * width - 1, clear_up - fontsizeYear - 2, 
                        width, fontsizeYear);
                g.setColor(Const.COLOR_DEF_FONT);
                g.drawString(Date.dayOfWeek2String(Date.germanOrder2javaUtilOrder(index % 7), true), 
                        clear_left + base * width, clear_up - 2);
                base += 7;
            }
        }
        
        /* unmark year */
        else if (highlightedHeadline == 100) {
            int widthHeadline = GraphicUtils.getStringWidth(g, new Font(Font.SANS_SERIF, Font.BOLD, 30), 
                    "" + matrix[10][3].getDate().get(java.util.Calendar.YEAR));
            g.setColor(Const.COLOR_BG_MAIN);
            g.fillPolygon(
                    new int[]{this.getWidth() / 2 - 55, this.getWidth() / 2 - 60, this.getWidth() / 2 - 60},
                    new int[]{clear_up - 30, clear_up - 35, clear_up - 25}, 3);
            g.fillPolygon(
                    new int[]{this.getWidth() / 2 - 50 + widthHeadline + 5, this.getWidth() / 2 - 50 + widthHeadline + 10, this.getWidth() / 2 - 50 + widthHeadline + 10},
                    new int[]{clear_up - 30, clear_up - 35, clear_up - 25}, 3);
        }
        
        highlightedHeadline = -1;
    }
    
    /**
     * Highlight a header in monthly view.
     * @param g - Graphics object to paint on
     * @param index - 
     *         <li>0..14: index of week day (Mo..So..) on the top
     *         <li>20..25: index of week number on the left site
     *         <li>30..35: index of week number on the right site
     *         <li>50: big month label on the left site
     *         <li>60: big month label on the right site
     * @param endindex - for calendar weeks only: where dragging
     *         of whole weeks ends (index numbers same as 'index')
     */
    private void highlightHeadlineMonth(Graphics g, int index, int endindex) {
        if (highlightedHeadlineEnd == endindex && highlightedHeadline == index)
            return;
        
        resetHeadlineMonth(g);
        highlightedHeadline = index;
        highlightedHeadlineEnd = endindex;

        /* mark column (week day) */
        if (index < 15) {
            g.setColor(Const.COLOR_BG_MAIN);
            g.fillRect(10 + highlightedHeadline * width, clear_up - 17, 25, 17);
//            g.setColor(calendar.getConfig().getColors()[ColorSet.SELECTED]);
            g.setColor(Color.BLACK);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            g.drawString(Date.dayOfWeek2String(Date.germanOrder2javaUtilOrder(index > 6 ? index - 8 : index), true),
                    10 + index * width, clear_up - 5);
        }
        
        /* mark week by number */
        else if (index < 40) {
            if (endindex != -1 && endindex < index) {
                int tmp = index;
                index = endindex;
                endindex = tmp;
            }
            logger.debug("highlight calendar week: index="+index+" endindex="+endindex);
            for (int tmpindex = index; tmpindex <= (endindex == -1 ? index : endindex); tmpindex++) {
                int i = tmpindex;
                boolean rightSite = i >= 30;
                i -= 20;
                if (rightSite)
                    i -= 10;
                int week;
                if (matrix[rightSite ? 8 : 0][i] != null && matrix[rightSite ? 8 : 0][i].getDate() != null)
                    week = matrix[rightSite ? 8 : 0][i].getDate().get(java.util.Calendar.WEEK_OF_YEAR);
                else if (matrix[rightSite ? 14 : 6][i] != null && matrix[rightSite ? 14 : 6][i].getDate() != null)
                    week = matrix[rightSite ? 14 : 6][i].getDate().get(java.util.Calendar.WEEK_OF_YEAR);
                else 
                    return;
    //            g.setColor(Const.COLOR_BG_MAIN);
    //            g.fillRect(clear_left + (rightSite ? 15 : 7) * width + 5, i * height + clear_up + 5, 20, 20);
    //            g.setColor(calendar.getConfig().getColors()[ColorSet.SELECTED]);
                g.setColor(Const.COLOR_DEF_FONT);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
                g.drawString("" + week, clear_left + (rightSite ? 15 : 7) * width + 5, i * height + clear_up + 25);
            }
        }
        
        /* mark whole month */
        else if (index == 50 || index == 60) {
//            g.setColor(calendar.getConfig().getColors()[ColorSet.SELECTED]);
//            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
//            g.setColor(Color.BLACK);
            g.setColor(Const.COLOR_DEF_FONT);
            String line = Date.month2String(matrix[index == 50 ? 3 : 10][3].getDate().get(java.util.Calendar.MONTH), false) +
                    " " + matrix[index == 50 ? 3 : 10][3].getDate().get(java.util.Calendar.YEAR);
//            g.drawString(line, (index == 60 ? 8 * width : 0) + clear_left + 5, clear_up - 24);
            
            int start = (index == 60 ? 8 * width : 0) + clear_left - 2;
            int widthHeadline = GraphicUtils.getStringWidth(g, 
                    new Font(Font.SANS_SERIF, Font.BOLD, 20), line) + start + 4;

            g.fillPolygon(
                    new int[]{start + 5, start, start},
                    new int[]{clear_up - 30, clear_up - 35, clear_up - 25}, 3);
            g.fillPolygon(
                    new int[]{widthHeadline + 5, widthHeadline + 10, widthHeadline + 10},
                    new int[]{clear_up - 30, clear_up - 35, clear_up - 25}, 3);
        }
    }
    
    /**
     * Highlight a header in yearly view.
     * @param g - Graphics object to paint on
     * @param index - <li>0..11: index of month (Jan..Dec) on the left site
     *         <li>12..23: index of calendar weeks on the right site
     *         <li>30..71: index of column (Mo..So..) on the top
     *         <li>100: big year label
     */
    private void highlightHeadlineYear(Graphics g, int index) {
        if (highlightedHeadline == index)
            return;
        resetHeadlineYear(g);
        highlightedHeadline = index;
        
        /* mark month */
        if (index < 12) {
            int spaces_left[] = {6, 5, 5, 7, 7, 6, 10, 5, 4, 7, 4, 4};
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.setColor(Const.COLOR_BG_MAIN);
            g.fillRect(0, clear_up + height * index, 
                    clear_left, height);
//            g.setColor(calendar.getConfig().getColors()[ColorSet.SELECTED]);
            g.setColor(Color.BLACK);
            g.drawString(Date.month2String(index, true), spaces_left[index], 
                        clear_up - 5 + height * (index + 1));
        }
        
        /* mark calendar weeks */
        else if (index < 24) {
            int kw_start, kw_end, i = 0;
            index -= 12;
            while (matrix[i][index] == null || matrix[i][index].getDate() == null)
                i++;
            kw_start = matrix[i][index].getDate().get(java.util.Calendar.WEEK_OF_YEAR);
            i = cols - 1;
            while (matrix[i][index] == null || matrix[i][index].getDate() == null)
                i--;
            kw_end = matrix[i][index].getDate().get(java.util.Calendar.WEEK_OF_YEAR);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontsizeYear + 2));
            g.setColor(Const.COLOR_BG_MAIN);
            g.fillRect(clear_left + cols * width + 1, clear_up + height * index, 
                    50, height);
//            g.setColor(calendar.getConfig().getColors()[ColorSet.SELECTED]);
            g.setColor(Color.BLACK);
            
            g.drawString("" + kw_start + "-" + kw_end,
                    cols * width + clear_left + 3, clear_up - 2
                            + (index + 1) * height);
        }
        
        /* mark days of week */
        else if (index < 30 + cols) {
            index -= 30;
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontsizeYear + 2));
            int base = index % 7;
            while (base < cols) {
                g.setColor(Const.COLOR_BG_MAIN);
                g.fillRect(clear_left + base * width - 1, clear_up - fontsizeYear - 2, 
                        width, fontsizeYear);
//                g.setColor(calendar.getConfig().getColors()[ColorSet.SELECTED]);
                g.setColor(Color.BLACK);
                g.drawString(Date.dayOfWeek2String(Date.germanOrder2javaUtilOrder(index % 7), true), 
                        clear_left + base * width, clear_up - 2);
                base += 7;
            }
        }
        
        /* mark year */
        else if (index == 100) {
//            g.setColor(calendar.getConfig().getColors()[ColorSet.SELECTED]);
//            g.setColor(Color.BLACK);
            g.setColor(Const.COLOR_DEF_FONT);
//            g.drawString("" + matrix[10][3].getDate().get(java.util.Calendar.YEAR), this.getWidth() / 2 - 50, clear_up - 20);
            int widthHeadline = GraphicUtils.getStringWidth(g, new Font(Font.SANS_SERIF, Font.BOLD, 30), 
                    "" + matrix[10][3].getDate().get(java.util.Calendar.YEAR));
            g.fillPolygon(
                    new int[]{this.getWidth() / 2 - 55, this.getWidth() / 2 - 60, this.getWidth() / 2 - 60},
                    new int[]{clear_up - 30, clear_up - 35, clear_up - 25}, 3);
            g.fillPolygon(
                    new int[]{this.getWidth() / 2 - 50 + widthHeadline + 5, this.getWidth() / 2 - 50 + widthHeadline + 10, this.getWidth() / 2 - 50 + widthHeadline + 10},
                    new int[]{clear_up - 30, clear_up - 35, clear_up - 25}, 3);
        }
    }
    
    /**
     * Fill the drawn calendar in yearly view with dates and events, 
     * depending on the currently viewed date.
     * @param g - Graphics to paint on
     * @param events - Events to paint
     * @param date - Currently viewed date 
     */
    private void fillCalendarYear(Graphics g, Vector<Event> events, Date date) {
        int viewedYear = date.get(java.util.Calendar.YEAR);
        
        /*
         * Print year
         */
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        g.drawString("" + viewedYear, this.getWidth() / 2 - 50, clear_up - 20);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontsizeYear));
        g.setColor(Const.COLOR_DEF_FONT);

        /*
         * For each line (=month) do...
         */
        for (int month = 0; month < rows; month++) {
            date = new Date(viewedYear, month, 1);
            int wDay = Date.javaUtilOrder2germanOrder(date.get(java.util.Calendar.DAY_OF_WEEK));
            int maxDays = date.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            int kw_start = date.get(java.util.Calendar.WEEK_OF_YEAR);
            int kw_end;
            for (int day = 0; day < maxDays; day++) {
                date.add(java.util.Calendar.DAY_OF_MONTH, day == 0 ? 0 : 1);

                matrix[wDay + day][month] = new Cell(this, wDay + day, month,
                        (Date) date.clone());

                /* Register weekends */
                if (date.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SATURDAY
                        || date.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY)
                    matrix[wDay + day][month].setWeekend(true);

                if (markedDates.contains(date)) {
                    logger.debug("selection contains: " + day + "."
                            + month + "." + viewedYear);
                    matrix[wDay + day][month].setSelected(true);
                }

                /* Print week numbers */
                if (day == maxDays - 1) {
                    kw_end = date.get(java.util.Calendar.WEEK_OF_YEAR);
                    g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontsizeYear));
                    g.setColor(Const.COLOR_DEF_FONT);
                    g.drawString("" + kw_start + "-" + kw_end,
                            cols * width + clear_left + 3, clear_up - 2
                                    + (month + 1) * height);
                }

                /* Register events */
                for (Event e : events)
                    if (e.match(date))
                        matrix[wDay + day][month].addEvent(e);

                matrix[wDay + day][month].paint(g, new Dimension(width, height));
            }
        }    
    }
    
    /**
     * Fill the drawn calendar in monthly view with dates and events, 
     * depending on the currently viewed date.
     * @param g - Graphics to paint on
     * @param events - Events to paint
     * @param date - Currently viewed date 
     */
    private void fillCalendarMonth(Graphics g, Vector<Event> events, Date date) {
        date.set(java.util.Calendar.DAY_OF_MONTH, 1);
        int col, row;

        /* for both sites do... */
        for (int j = 0; j < 2; j++) {

            /* Print name of month */
            String monthName = Date.month2String(date.get(java.util.Calendar.MONTH), false) +
                    " " + date.get(java.util.Calendar.YEAR);
            g.setColor(Color.black);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            g.drawString(monthName, 
                    clear_left + j * 8 * width + 5, clear_up - 24);
            lenMonthName[j] = GraphicUtils.getStringWidth(g, monthName);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            col = Date.javaUtilOrder2germanOrder(date.get(Date.DAY_OF_WEEK)) + j * 8;
            int maxDays = date.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            row = 0;

            /* for each day do... */
            for (int day = 1; day <= maxDays; day++) {
                date.set(java.util.Calendar.DAY_OF_MONTH, day);
                matrix[col][row] = new Cell(this, col, row, (Date) date.clone());
                if (markedDates.contains(date)) {
                    logger.debug("selection contains: " + date.dateToString(false));
                    matrix[col][row].setSelected(true);
                }

                /* Print number of week */
                int woche = date.get(java.util.Calendar.WEEK_OF_YEAR);
                g.setColor(Const.COLOR_DEF_FONT);
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
                g.drawString("" + woche, clear_left + 7 * width + 5 + j * 8
                        * width, row * height + clear_up + 25);

                /* Register events */
                for (Event e : events)
                    if (e.match(date))
                        matrix[col][row].addEvent(e);
                
                matrix[col][row].paint(g, new Dimension(width, height));
                
                if (col == 6) { // Line break on left site
                    col = 0;
                    row++;
                } else if (col == 14) { // Line break on right site
                    col = 8;
                    row++;
                } else
                    col++;
            }
            date.set(java.util.Calendar.DAY_OF_MONTH, 1);
            date.add(java.util.Calendar.MONTH, 1);
        }    
    }
    
    /**
     * Fill the drawn calendar in weekly view with dates and events, 
     * depending on the currently viewed date.
     * @param g - Graphics to paint on
     * @param events - Events to paint
     * @param date - Currently viewed date 
     */
    private void fillCalendarWeek(Graphics g, Vector<Event> events, Date date) {
        int col, row;

        /* find first day of upper week */
        int woche = date.get(java.util.Calendar.WEEK_OF_YEAR);
        int tmp = woche;
        while (tmp == woche) {
            date.add(java.util.Calendar.DAY_OF_MONTH, -1);
            tmp = date.get(java.util.Calendar.WEEK_OF_YEAR);
        }
        date.add(java.util.Calendar.DAY_OF_MONTH, 1);
        col = 0;
        row = 0;

        for (int j = 0; j < rows; j++) {
            
            /* Print number of week */
            woche = date.get(java.util.Calendar.WEEK_OF_YEAR);
            g.setColor(Const.COLOR_DEF_FONT);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 64));
            g.drawString("" + woche, clear_left + cols * width + 5, 
                    j * height + clear_up + 80);

            for (int i = 0; i < 7; i++) {

                matrix[col][row] = new Cell(this, col, row, (Date) date.clone());
                if (markedDates.contains(date)) {
                    logger.debug("selection contains: " + date.dateToString(false));
                    matrix[col][row].setSelected(true);
                }

                /* Register events */
                for (Event e : events)
                    if (e.match(date))
                        matrix[col][row].addEvent(e);
                    
                matrix[col][row].paint(g, new Dimension(width, height));

                col++;
                date.add(java.util.Calendar.DAY_OF_MONTH, 1);
            }
            row++;
            col = 0;
        }
    }
    
    /**
     * Fill the drawn calendar in daily view with dates and events, 
     * depending on the currently viewed date.
     * @param g - Graphics to paint on
     * @param events - Events to paint
     * @param date - Currently viewed date 
     */
    private void fillCalendarDay(Graphics g, Vector<Event> events, Date date) {
        /* For both days do... */
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if (j == 0)
                    date.setHasTime(false);
                else {
                    date.setHasTime(true);
                    date.set(java.util.Calendar.HOUR_OF_DAY, j - 1);
                    date.set(java.util.Calendar.MINUTE, 0);
                }
                matrix[i][j] = new Cell(this, i, j, (Date) date.clone());
                System.out.println(j + " size: " + markedDates.size());
                if (markedDates.size() > 0)
                    System.out.println(markedDates.firstElement().timeToString());

                if (markedDates.contains(date))
                    matrix[i][j].setSelected(true);

                matrix[i][j].paint(g, new Dimension(width, height));
            }

//            long dayDiff = date.dayDiff(new Date());
//
//            String header = "";
//            if (dayDiff == 0)
//                header += "Heute: ";
//            else if (dayDiff == -1)
//                header += "Gestern: ";
//            else if (dayDiff == 1)
//                header += "Morgen: ";
//            else if (dayDiff == 2)
//                header += "�bermorgen: ";
//            header += Date.dayOfWeek2String(date.get(java.util.Calendar.DAY_OF_WEEK), false);
//            header += ", " + date.dateToString(false);
//
//            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
//            g.setColor(Color.BLACK);
//            g.drawString(header, clear_left + i * width + 3, clear_up - 4);

            for (Event e : events)
                if (e.match(date)) {
                    if (e.getDate().hasTime()) {
                        matrix[i][e.getDate().get(java.util.Calendar.HOUR_OF_DAY) + 1]
                                .addEvent(e);
                        matrix[i][e.getDate().get(java.util.Calendar.HOUR_OF_DAY) + 1]
                                .paint(g, new Dimension(width, height));
                    }

                    else {
                        matrix[i][0].addEvent(e);
                        matrix[i][0].paint(g, new Dimension(width, height));
                    }
                }

            date.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
    }

    /**
     * Fill the drawn calendar with dates and events, depending on 
     * the currently viewed date.
     * @param g - Graphics to paint on
     */
    private void fillCalendar(Graphics g) {
        @SuppressWarnings("unchecked")
        Vector<Event> events = (Vector<Event>) calendar.getAllEvents().clone();
        Date date = (Date) calendar.getViewedDate().clone();
        date.setHasTime(false);

        matrix = new Cell[cols][rows];

        if (this.view == Configuration.VIEW_YEAR)
            fillCalendarYear(g, events, date);
        else if (this.view == Configuration.VIEW_MONTH)
            fillCalendarMonth(g, events, date);
        else if (this.view == Configuration.VIEW_WEEK)
            fillCalendarWeek(g, events, date);
        else if (this.view == Configuration.VIEW_DAY)
            fillCalendarDay(g, events, date);
    }
    
    /**
     * Mark a set of cells in a row to be marked as selected in
     * monthly view.
     * @param start - Index of starting cell within the matrix
     * @param end - Index of ending cell within the matrix
     */
    private void markMonth(Point start, Point end) {
        int col = start.x;
        int row = start.y;
        
        Vector<Cell> toMark = new Vector<Cell>();
        
        if (col == -1 || col == 7 || end.x == -1 || end.x == 7)
            return;
        if (end.y < row || ((end.x < col) && (row == end.y))
                || ((end.x < 7) && (col > 7)))
            reverse = true;
        else
            reverse = false;
        if (col < 7 && end.x > 7)
            reverse = false;

        while (true) {
            if (!reverse) {
                if (col == 7) {
                    /* Line break on left site (downwards) */
                    col = 0;
                    row++;
                } 
                else if (col == cols) { 
                    /* Line break on right site (downwards) */
                    col = 8;
                    row++;
                }
                if (row == rows && col == 0) { 
                    /* Change from left to right site */
                    row = 0;
                    col = 8;
                }
            } 
            else {
                if (col == -1) { 
                    /* Line break on left site (upwards) */
                    col = 6;
                    row--;
                } 
                else if (col == 7) { 
                    /* Line break on right site (upwards) */
                    col = 14;
                    row--;
                }
                if (row == -1 && col == 14) { 
                    /* Change from right to left site */
                    row = 5;
                    col = 6;
                }
            }
            
            if (matrix[col][row] != null)
                toMark.add(matrix[col][row]);
            if (col == end.x && row == end.y)
                break;
            if (!reverse)
                col++;
            else
                col--;
        }
        
        mark(toMark);
    }
    
    /**
     * Have a column (days of the same week day) to be marked as selected.
     * @param col - Column (week day) to select
     */
    private void markMonth(int col) {
        Vector<Cell> toMark = new Vector<Cell>();
        if (col != 7)
            for (int i = 0; i < 6; i++)
                if (matrix[col][i] != null)
                    toMark.add(matrix[col][i]);
        mark(toMark);
        highlightHeadlineMonth(getGraphics(), col, -1);
    }
    
    /**
     * Mark all columns of a week day in yearly view.
     * @param col - Column (0..41) where (col % 7) equals Mo..So
     */
    private void markYear(int col) {
        Vector<Cell> toMark = new Vector<Cell>();
        int base = col % 7;
        while (base < cols) {
            for (int i = 0; i < rows; i++)
                if (matrix[base][i] != null)
                    toMark.add(matrix[base][i]);
            base += 7;
        }
        mark(toMark);
        highlightHeadlineYear(getGraphics(), col + 30);
    }
    
    /**
     * Have a set of cells in a row to be marked as selected in
     * yearly or weekly view.
     * @param start - Index of starting cell within the matrix
     * @param end - Index of ending cell within the matrix
     */
    private void markWeekYear(Point start, Point end) {
        int col = start.x;
        int row = start.y;
        
        Vector<Cell> toMark = new Vector<Cell>();
        
        if (start.x == -1 || start.x == cols || end.x == -1
                || end.x == cols)
            return;
        if (end.y > start.y || (end.y == start.y && end.x > start.x))
            reverse = false;
        else
            reverse = true;

        while (true) {
            if (!reverse) {
                if (col == cols) { 
                    /* Line break (downwards) */
                    col = 0;
                    row++;
                }
            } 
            else {
                if (col == -1) { 
                    /* Line break (upwards) */
                    col = cols - 1;
                    row--;
                }
            }
            
            if (matrix[col][row] != null)
                toMark.add(matrix[col][row]);
            if (col == end.x && row == end.y)
                break;
            if (!reverse)
                col++;
            else
                col--;
        }

        mark(toMark);
    }


    /**
     * Mark a set of cells in a row to be marked as selected.
     * @param start - Index of starting cell within the matrix
     * @param end - Index of ending cell within the matrix
     */
    private void mark(Point start, Point end) {
        if (start == null || end == null)
            return;
        
        mouseHover = matrix[end.x][end.y];

        if (this.view == Configuration.VIEW_MONTH)
            markMonth(start, end);
        else if (this.view == Configuration.VIEW_WEEK
                || this.view == Configuration.VIEW_YEAR)
            markWeekYear(start, end);
        else if (this.view == Configuration.VIEW_DAY)
            /* Marking a set of cells not supported */
            ;
    }

    /**
     * Mark a set of cells as selected.
     * @param x - Set of cells to mark
     */
    private void mark(Vector<Cell> x) {
        for (int a = 0; a < cols; a++)
            for (int b = 0; b < rows; b++)
                if (matrix[a][b] != null) {
                    if (x.contains(matrix[a][b]) && !matrix[a][b].isSelected())
                        matrix[a][b].setSelected(true);
                    if (!x.contains(matrix[a][b]) && matrix[a][b].isSelected())
                        matrix[a][b].setSelected(false);
                }
        markedDates.removeAllElements();
        for (Cell c : x)
            markedDates.add(c.getDate());
    }

    /**
     * Unmark all selected cells of the current view.
     */
    public void unmarkAll() {
        logger.debug("[unmarkAll]");
        for (int a = 0; a < cols; a++)
            for (int b = 0; b < rows; b++)
                if (matrix[a][b] != null)
                    if (matrix[a][b].isSelected())
                        matrix[a][b].setSelected(false);
        markedDates.removeAllElements();
        strgPressed = false;
        if (this.view == Configuration.VIEW_MONTH)
            resetHeadlineMonth(getGraphics());
        else if (this.view == Configuration.VIEW_YEAR)
            resetHeadlineYear(getGraphics());
    }
    
    /**
     * Unmark a previously selected event.
     */
    private void markSelectedEvent(boolean mark) {
        if (selectedEvent != null) {
            selectedEvent.setSelected(mark);
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++)
                    if (matrix[j][i] != null)
                        if (matrix[j][i].containsEvent(selectedEvent))
                            matrix[j][i].paint(this.getGraphics(), new Dimension(width, height));
            if (!mark) {
                selectedEvent = null;
                setToolTipText("");
            }
            else
                setToolTipText(selectedEvent.getName());
        }
    }

    /**
     * Repaint the calendar canvas.
     */
    public void update() {
        mouseHover = null;
        repaint();
    }

    /**
     * Mark a single cell as selected.
     * @param x - Cell to select
     */
    private void mark(Cell x) {
        logger.debug("[mark] " + x);
        unmarkAll();
        if (x != null) {
            x.setSelected(true);
            markedDates.add(x.getDate());
        }
        
        mouseHover = x;
    }
    
    private void displayEventPopupMenu(MouseEvent m) {
        this.removeMouseListener(this);
        this.removeMouseMotionListener(this);
        
        popup = new JPopupMenu(selectedEvent.getName());
        
        editItem = new JMenuItem("Bearbeiten", 
                new ImageIcon(this.getClass().getClassLoader().getResource("media/event_edit20.ico")));
        deleteItem = new JMenuItem("L�schen", 
                new ImageIcon(this.getClass().getClassLoader().getResource("media/event_delete20.ico")));
        copyItem = new JMenuItem("Kopieren", 
                new ImageIcon(this.getClass().getClassLoader().getResource("media/event_copy20.ico")));
        cutItem = new JMenuItem("Ausschneiden", 
                new ImageIcon(this.getClass().getClassLoader().getResource("media/event_cut20.ico")));
        remindItem = new JMenuItem("Erinnerung aufrufen", 
                new ImageIcon(this.getClass().getClassLoader().getResource("media/bell20.ico")));
        
        editItem.addActionListener(this);
        deleteItem.addActionListener(this);
        copyItem.addActionListener(this);
        cutItem.addActionListener(this);
        remindItem.addActionListener(this);

        if (selectedEvent.getEndDate() != null) {
            copyItem.setEnabled(false);
            cutItem.setEnabled(false);
        }
        
        if (selectedEvent.isHoliday() || selectedEvent.isSpecial()) {
            editItem.setEnabled(false);
            deleteItem.setEnabled(false);
            copyItem.setEnabled(false);
            cutItem.setEnabled(false);
        }
        
        if (cuttedEvent != null) {
            copyItem.setEnabled(false);
            cutItem.setEnabled(false);
        }

        popup.add(editItem);
        popup.add(copyItem);
        popup.add(cutItem);
        popup.add(deleteItem);
        popup.addSeparator();
        popup.add(remindItem);

        if (selectedEvent.getAttachment(calendar.getWorkspace()) != null) {
            showAttachmentItem = new JMenuItem("Anhang �ffnen", 
                    new ImageIcon(this.getClass().getClassLoader().getResource("media/attachment20.ico")));
            showAttachmentItem.addActionListener(this);
            popup.add(showAttachmentItem);
        }
        if (!selectedEvent.getNotes(calendar.getWorkspace()).equals("")) {
            showNotesItem = new JMenuItem("Notizen anzeigen", 
                    new ImageIcon(this.getClass().getClassLoader().getResource("media/notes20.ico")));
            showNotesItem.addActionListener(this);
            popup.add(showNotesItem);
        }
        
        popup.addPopupMenuListener(this);
        popup.setInvoker(this);
//        popup.show(this, m.getX(), m.getY());
        popup.setLocation(m.getLocationOnScreen());
        popup.setVisible(true);
    }
    
    private void displayCellPopupMenu(MouseEvent m) {
        this.removeMouseListener(this);
        this.removeMouseMotionListener(this);
        
        popup = new JPopupMenu(markedDates.firstElement().dateToString(false));
        newItem = new JMenuItem("Neues Ereignis", 
                new ImageIcon(this.getClass().getClassLoader().getResource("media/event_new20.ico")));
        pasteItem = new JMenuItem("Einf�gen", 
                new ImageIcon(this.getClass().getClassLoader().getResource("media/event_paste20.ico")));
        overviewItem = new JMenuItem("�bersicht �ffnen", 
                new ImageIcon(this.getClass().getClassLoader().getResource("media/calendar16.ico")));
        
        newItem.addActionListener(this);
        overviewItem.addActionListener(this);
        pasteItem.addActionListener(this);
        
        if (copiedEvent == null && cuttedEvent == null)
            pasteItem.setEnabled(false);

        popup.add(newItem);
        popup.add(pasteItem);
        popup.addSeparator();
        popup.add(overviewItem);
        byte moon = Moon.getMoonPhase(markedDates.firstElement());
        if (moon != Moon.MOON_NONE) {
            popup.addSeparator();
            popup.add(new JLabel("   Mondphase: " + Moon.moonPhaseToString(moon)));
        }
        popup.addPopupMenuListener(this);
        popup.setInvoker(this);
//        popup.show(this, m.getX(), m.getY());
        popup.setLocation(m.getLocationOnScreen());
        popup.setVisible(true);
    }

    @Override
    public void mouseExited(MouseEvent m) {
    }

    @Override
    public void mouseEntered(MouseEvent m) {
    }

    /*
     * In case this is introduces mouse dragging, keep track of the cell 
     * where the dragging has started.
     */
    @Override
    public void mousePressed(MouseEvent m) {
//        markedDates.removeAllElements();
        mouseSrc = null;
        
        if (selectedEvent != null) {
            if (m.isPopupTrigger())
                displayEventPopupMenu(m);
            return;
        }
        
        if (m.isPopupTrigger()) {
            displayCellPopupMenu(m);
            return;
        }
        
        int col_index = (m.getX() - clear_left) / width;
        int row_index = (m.getY() - clear_up) / height;
        
        if (row_index < 0 || row_index >= rows)
            return;
        
        if (view == Configuration.VIEW_MONTH && (col_index == 7 || col_index == 15) && 
                m.getX() < (clear_left + col_index * width + 22)) {
            mouseSrc = new Point(col_index, row_index);
            return;
        }
        else if (view == Configuration.VIEW_WEEK && col_index == cols && 
                m.getX() >= clear_left + 5 + cols * width && m.getX() <= clear_left + 10 + cols * width &&
                m.getY() >= clear_up + 6 + row_index * height && m.getY() <= clear_up + 16 + row_index * height) {
            mouseSrc = new Point(col_index, row_index);
            return;
        }
        
        if (view == Configuration.VIEW_MONTH && col_index == 7)
            return;
        else if (m.getX() < clear_left || m.getY() < clear_up || 
                col_index >= cols || row_index >= rows)
            return;
        
        mouseSrc = new Point(col_index, row_index);
    }

    /*
     * Open the events overview based on the cells (dates) selected.
     */
    @Override
    public void mouseReleased(MouseEvent m) {
        if (popup != null)
            return;
        
        if (selectedEvent != null) {
            if (m.isPopupTrigger())
                displayEventPopupMenu(m);
            else 
                calendar.newSelection(selectedEvent);
            return;
        }
        
        for (int a = 0; a < cols; a++)
            for (int b = 0; b < rows; b++)
                if (matrix[a][b] != null)
                    if (matrix[a][b].isSelected()) {
                        if (!markedDates.contains(matrix[a][b].getDate()))
                            markedDates.add(matrix[a][b].getDate());
                        else if (strgPressed) {
                            markedDates.remove(matrix[a][b].getDate());
                            matrix[a][b].setSelected(false);
                        }
                    }
        
        if (!strgPressed && markedDates.size() > 0) {
            if (markedDates.size() == 1) {
                if (m.isPopupTrigger())
                    displayCellPopupMenu(m);
                else
                    calendar.newSelection(markedDates, true);
                return;
            }
            
            Vector<Cell> cells = new Vector<Cell>();
            for (int b = 0; b < rows; b++)
                for (int a = 0; a < cols; a++)
                    if (matrix[a][b] != null)
                        if (matrix[a][b].isSelected())
                            cells.add(matrix[a][b]);
            
            new TabelOfEventsMulitDay(cells, calendar, markedDates, 
                    highlightedHeadline != -1 && highlightedHeadline < 15);
        }
    }

    /*
     * All we need to evaluate clicks is already implemented in 
     * mousePressed() and mouseReleased().
     */
    @Override
    public void mouseClicked(MouseEvent m) {
    }

    /*
     * If the mouse key is pressed and the mouse is moved, this indicates 
     * that a set of cells, starting from the beginning of the pressing 
     * and ending at this current position is to be marked.
     */
    @Override
    public void mouseDragged(MouseEvent m) {
        
        if (this.view == Configuration.VIEW_DAY)
            return;
        
        if (m.getX() < clear_left || m.getY() < clear_up || mouseSrc == null)
            return;
        
        int col_index = (m.getX() - clear_left) / width;
        int row_index = (m.getY() - clear_up) / height;
        
        if (view == Configuration.VIEW_MONTH && mouseSrc != null && 
                (mouseSrc.x == 7 || mouseSrc.x == 15) && row_index < rows) {
            
            /* mark whole calendar week (left side) */
            if (mouseSrc.x == 7) {
                int start_x = 0, start_y = mouseSrc.y, end_x = 6, end_y = row_index;
                if (row_index < mouseSrc.y) {
                    start_y = row_index;
                    end_y = mouseSrc.y;
                }
                if (matrix[end_x][end_y] == null) {
                    end_x = 14;
                    end_y = 0;
                }
                mark(new Point(start_x, start_y), new Point(end_x, end_y));
                highlightHeadlineMonth(getGraphics(), 20 + mouseSrc.y, 20 + row_index);
                return;
            }
            
            /* mark whole calendar week (right side) */
            else if (mouseSrc.x == 15) {
                int start_x = 8, start_y = mouseSrc.y, end_x = 14, end_y = row_index;
                if (row_index < mouseSrc.y) {
                    start_y = row_index;
                    end_y = mouseSrc.y;
                }
                if (matrix[start_x][start_y] == null) {
                    start_x = 0;
                    if (matrix[0][5] == null)
                        start_y = 4;
                    else
                        start_y = 5;
                }
                mark(new Point(start_x, start_y), new Point(end_x, end_y));
                highlightHeadlineMonth(getGraphics(), 30 + mouseSrc.y, 30 + row_index);
                return;
            }
        }
        else if (view == Configuration.VIEW_WEEK && mouseSrc != null && mouseSrc.x == cols &&
                row_index < rows) {
            mark(new Point(0, mouseSrc.y), new Point(6, row_index));
            return;
        }
        else if (col_index >= cols || row_index >= rows)
            return;
        
        if (matrix[col_index][row_index] != mouseHover)
            mark(mouseSrc, new Point(col_index, row_index));
    }

    /*
     * This method gets called when the mouse is moved without a key 
     * being pressed. In this case, just mark the cell situated 
     * under the current cursor position.
     */
    @Override
    public void mouseMoved(MouseEvent m) {
        
        if (matrix == null)
            return;
        
        int col_index = (m.getX() - clear_left) / width;
        int row_index = (m.getY() - clear_up) / height;

        if (m.getX() < clear_left) {
            mouseHover = null;
            markSelectedEvent(false);
            if (view == Configuration.VIEW_YEAR && row_index >= 0 && row_index < 12) {
                /* mark whole month */
                highlightHeadlineYear(getGraphics(), row_index);
                mark(new Point(0, row_index), new Point(cols - 1, row_index));
            }
            else
                unmarkAll();
        }
        else if (m.getY() < clear_up) {
            mouseHover = null;
            if (view == Configuration.VIEW_MONTH && clear_up - m.getY() < 18) {
                if (col_index == 7 || col_index > 14) {
                    unmarkAll();
                    markSelectedEvent(false);
                }
                else {
                    /* mark a column in month */
                    markMonth(col_index);
                }
            }
            else if (view == Configuration.VIEW_MONTH && 
                    clear_up - m.getY() > 20 && clear_up - m.getY() < 40) {
                if (m.getX() - clear_left > 5 && m.getX() - clear_left < lenMonthName[0] + 5) {
                    /* mark whole left month */
                    highlightHeadlineMonth(getGraphics(), 50, -1);
                    markMonth(new Point(0, 0), new Point(6, 5));
                }
                else if (m.getX() - clear_left > 5 + 8 * width && m.getX() - clear_left < lenMonthName[1] + 5 + 8 * width) {
                    /* mark whole right month */
                    highlightHeadlineMonth(getGraphics(), 60, -1);
                    markMonth(new Point(8, 0), new Point(14, 5));
                }
                else {
                    unmarkAll();
                    markSelectedEvent(false);
                }
            }
            else if (view == Configuration.VIEW_YEAR && clear_up - m.getY() < fontsizeYear + 5 && 
                    col_index >= 0 && col_index < cols) {
                /* mark weekday in whole year */
                markYear(col_index);
            }
            else if (view == Configuration.VIEW_YEAR && m.getX() >= this.getWidth() / 2 - 50 && m.getX() <= this.getWidth() / 2 + 16 &&
                    m.getY() <= clear_up - 20 && m.getY() >= 2) {
                /* mark weekday in whole year */
                highlightHeadlineYear(getGraphics(), 100);
                mark(new Point(0, 0), new Point(cols - 1, rows - 1));
            }
            else if (view == Configuration.VIEW_DAY && m.getX() > clear_left && m.getX() < clear_left + 290 && m.getY() > 2) {
                markSelectedEvent(false);
                if (!matrix[0][0].isSelected())
                    matrix[0][0].setSelected(true);
            }
            else if (view == Configuration.VIEW_DAY && m.getX() > clear_left + width && m.getX() < clear_left + width + 290 && m.getY() > 2) {
                markSelectedEvent(false);
                if (!matrix[1][0].isSelected())
                    matrix[1][0].setSelected(true);
            }
            else {
                unmarkAll();
                markSelectedEvent(false);
            }
        }
        else if (col_index < cols && row_index < rows && matrix[col_index][row_index] != null) {
            Event newSelected = matrix[col_index][row_index].getEventUnderCursor(m.getPoint(), new Dimension(width, height));
            if (selectedEvent == null && newSelected == null)
                ;
            else if (newSelected == null) {
                markSelectedEvent(false);
//                return;
            }
            else if (!newSelected.equals(selectedEvent)) {
                markSelectedEvent(false);
                selectedEvent = newSelected;
                markSelectedEvent(true);
                mark((Cell) null);
                return;
            }
            else if (newSelected.equals(selectedEvent)) {
                mark((Cell) null);
                return;
            }

            if (matrix[col_index][row_index].equals(mouseHover))
                return;
            mark(matrix[col_index][row_index]);
//            markedDates.add(matrix[col_index][row_index].getDate());
        } 
        else if (view == Configuration.VIEW_MONTH && col_index == 7 && row_index < 6 &&
                m.getX() < (clear_left + col_index * width + 22)) {
            /* mark a week in month on left site */
            markSelectedEvent(false);
            mouseSrc = new Point(7, row_index);
            if (matrix[6][row_index] == null) {
                if (matrix[0][row_index] == null) {
                    unmarkAll();
                    markSelectedEvent(false);
                }
                else {
                    highlightHeadlineMonth(getGraphics(), 20 + row_index, -1);
                    mark(new Point(0, row_index), new Point(14, 0));
                }
            }
            else {
                highlightHeadlineMonth(getGraphics(), 20 + row_index, -1);
                mark(new Point(0, row_index), new Point(6, row_index));
            }
            mouseHover = null;
        }
        else if (view == Configuration.VIEW_MONTH && col_index == 15 && row_index < 6 &&
                m.getX() < (clear_left + col_index * width + 22)) {
            /* mark a week in month on right site */
            markSelectedEvent(false);
            mouseSrc = new Point(15, row_index);
            if (matrix[8][row_index] == null) {
                if (row_index == 0) {
                    highlightHeadlineMonth(getGraphics(), 30 + row_index, -1);
                    if (matrix[0][5] == null)
                        mark(new Point(0, 4), new Point(14, row_index));
                    else
                        mark(new Point(0, 5), new Point(14, row_index));
                }
                else {
                    unmarkAll();
                    markSelectedEvent(false);
                }
            }
            else {
                highlightHeadlineMonth(getGraphics(), 30 + row_index, -1);
                mark(new Point(8, row_index), new Point(14, row_index));
            }
            mouseHover = null;
        }
        else if (view == Configuration.VIEW_WEEK && col_index == 7 && row_index < 2) {
            if (m.getX() >= clear_left + 5 + cols * width && m.getX() <= clear_left + 10 + cols * width &&
                    m.getY() >= clear_up + 6 + row_index * height && m.getY() <= clear_up + 16 + row_index * height)
                mark(new Point(0, row_index), new Point(6, row_index));
            else
                mark((Cell) null);
            markSelectedEvent(false);
            mouseHover = null;
        }
        else if (view == Configuration.VIEW_YEAR && col_index >= cols && row_index < rows) {
            markSelectedEvent(false);
            mouseHover = null;
            int start_x = 0, start_y = row_index, end_x = cols - 1, end_y = row_index;
            if (row_index > 0) {
                int tmp_x = start_x;
                while (matrix[tmp_x][start_y] == null)
                    tmp_x++;
                if (tmp_x % 7 != 0) {
                    start_y--;
                    tmp_x = end_x;
                    while (matrix[tmp_x][start_y] == null)
                        tmp_x--;
                    start_x = tmp_x - (tmp_x % 7);
                }
            }
            if (row_index < 11) {
                int tmp_x = end_x;
                while (matrix[tmp_x][end_y] == null)
                    tmp_x--;
                if (++tmp_x % 7 != 0) {
                    end_y++;
                    end_x = 6;
                }
            }
            highlightHeadlineYear(getGraphics(), 12 + row_index);
            mark(new Point(start_x, start_y), new Point(end_x, end_y));
        }
        else {
            markSelectedEvent(false);
            mark((Cell) null);
            if (markedDates.size() != 0)
                unmarkAll();
        }
    }

    /*
     * In case STRG is released, open a new create-event-frame, based
     * on the cells (dates) selected.
     */
    @Override
    public void keyReleased(KeyEvent k) {
        if (strgPressed && k.getKeyCode() == KeyEvent.VK_CONTROL) {
            strgPressed = false;
            calendar.newSelection(markedDates, false);
        }
        else if (k.getKeyCode() == KeyEvent.VK_ESCAPE) {
            strgPressed = false;
            unmarkAll();
            markSelectedEvent(false);
        }
    }

    /*
     * In case STRG is pressed, enable selecting cells independently.
     */
    @Override
    public void keyPressed(KeyEvent k) {
        if (this.view != Configuration.VIEW_DAY
                && k.getKeyCode() == KeyEvent.VK_CONTROL)
            strgPressed = true;
    }

    @Override
    public void keyTyped(KeyEvent k) {
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
        markSelectedEvent(false);
        unmarkAll();
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        popup = null;
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(newItem))
            new EditEvent(calendar, markedDates.firstElement());
        else if (e.getSource().equals(overviewItem))
            new TableOfEventsSingleDay(markedDates.firstElement(), 
                    calendar, false);
        else if (e.getSource().equals(editItem))
            new EditEvent(calendar, selectedEvent, false);
        else if (e.getSource().equals(deleteItem))
            calendar.deleteEvent(selectedEvent);
        else if (e.getSource().equals(remindItem))
            new Notification(calendar, selectedEvent);
        else if (e.getSource().equals(showNotesItem))
            selectedEvent.openNotesDialog(calendar.getGUI().getFrame(), 
                    selectedEvent.getNotes(calendar.getWorkspace()), 
                    true);
        else if (e.getSource().equals(showAttachmentItem))
            try {
                Desktop.getDesktop().open(selectedEvent.getAttachment(calendar.getWorkspace()));
            } catch (IOException e1) {
                logger.error("cannot open attachment...", e1);
            }
        else if (e.getSource().equals(copyItem))
            copiedEvent = selectedEvent.clone();
        else if (e.getSource().equals(pasteItem)) {
            
            /* paste cutted event */
            if (cuttedEvent != null) {
                cuttedEvent.setDate((Date) markedDates.firstElement().clone());
                calendar.editEvent(cuttedEvent.getID(), cuttedEvent);
                cuttedEvent = null;
                copiedEvent = null;
            }
            
            /* paste copied event */
            else if (copiedEvent != null) {
                copiedEvent.setDate((Date) markedDates.firstElement().clone());
                calendar.copyEvent(copiedEvent);
            }
        }
        else if (e.getSource().equals(cutItem))
            cuttedEvent = selectedEvent;
    }
}