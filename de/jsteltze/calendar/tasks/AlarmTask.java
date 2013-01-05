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

package de.jsteltze.calendar.tasks;

import java.util.TimerTask;

import de.jsteltze.calendar.Calendar;
import de.jsteltze.calendar.Event;
import de.jsteltze.calendar.frames.Notification;

/**
 * Task thread for a notification.
 * @author Johannes Steltzer
 *
 */
public class AlarmTask 
    extends TimerTask 
    implements Runnable {

    /** event to notify of */
    private Event event;

    /** parent calendar object */
    private Calendar caller;

    /**
     * Construct a new alarm task.
     * @param c - Parent calendar object
     * @param e - Event to notify of
     */
    public AlarmTask(Calendar c, Event e) {
        super();
        event = e;
        caller = c;
    }

    /**
     * 
     * @return Event that is subject of this AlarmTask.
     */
    public Event getEvent() {
        return this.event;
    }

    /**
     * Set a new event for this AlarmTask.
     * @param x - New event to apply
     */
    public void setEvent(Event x) {
        this.event = x;
    }

    @Override
    public void run() {
        caller.removeAlarmTask(this);
        new Notification(caller, event);
    }
}