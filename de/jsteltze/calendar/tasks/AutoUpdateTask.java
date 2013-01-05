package de.jsteltze.calendar.tasks;

import java.util.TimerTask;

import org.apache.log4j.Logger;

import de.jsteltze.calendar.Update;
import de.jsteltze.calendar.frames.CalendarFrame;

public class AutoUpdateTask    
    extends TimerTask 
    implements Runnable {
    
    private CalendarFrame c;
    
    private static Logger logger = Logger.getLogger(AutoUpdateTask.class);
    
    public AutoUpdateTask(CalendarFrame c) {
        this.c = c;
    }
    
    public void run() {
        logger.debug("start auto update");
        new Update(c, true);
    }
    
}
