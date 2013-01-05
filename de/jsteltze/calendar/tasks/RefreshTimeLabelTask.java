package de.jsteltze.calendar.tasks;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

import de.jsteltze.calendar.Event;
import de.jsteltze.calendar.frames.Notification;
import de.jsteltze.common.calendar.Date;

/**
 * Task to refresh the header of a launched notification each minute.
 * The Headers shows the time left until the events starts.
 * This time has to be refreshed each minute.
 * @author Johannes Steltzer
 *
 */
public class RefreshTimeLabelTask 
    extends Thread {
    
    /** notification frame */
    private Notification noti;
    
    /** time label */
    private JLabel text;
    
    /** event of interest */
    private Event event;
    
    /** stop thread flag */
    private boolean stop;
    
    /** add a trailing colon */
    private boolean addColon;
    
    private static Logger logger = Logger.getLogger(RefreshTimeLabelTask.class);
    
    /**
     * Construct a new refresher thread. This will recalculate the
     * time difference to the event each minute and refresh the
     * timeLabel.
     * @param noti - Notification frame to refresh
     * @param addColon - add a trailing colon
     */
    public RefreshTimeLabelTask(Notification noti, boolean addColon) {
        this.noti = noti;
        this.text = null;
        this.event = noti.getEvent();
        this.stop = false;
        this.addColon = addColon;
    }
    
    /**
     * Construct a new refresher thread. This will recalculate the
     * time difference to the event each minute and refresh the
     * timeLabel.
     * @param text - JLabel which text to refresh
     * @param event - Event of interest
     */
    public RefreshTimeLabelTask(JLabel text, Event event) {
        this.text = text;
        this.noti = null;
        this.event = event;
        this.stop = false;
        this.addColon = true;
    }
    
    @Override
    public void run() {
        String upperString;
        Date date = event.getNextDate();
        
        /*
         * Initial sleep to fill the full minute
         */
        Date dateNow = new Date();
        try {
            Thread.sleep(1000 * (60 - dateNow.get(Date.SECOND)));
        } catch (InterruptedException e) {
            return;
        }
        
        long minDiff = date.minDiff(new Date());
        
        while (!stop) {
            logger.debug("Refresh timeLabel...");
            
            long hours = minDiff / 60;
            long minut = minDiff - hours * 60;

            if (minDiff < 0)
                upperString = "vor "
                        + (hours == 0 ? "" : (-hours) + "h ") + (-minut) + "min";
            else if (minDiff > 0)
                upperString = "in "
                        + (hours == 0 ? "" : (hours) + "h ") + (minut) + "min";
            else
                upperString = "JETZT";
            
            if (text == null)
                noti.refreshTimeLabel(upperString + (addColon ? ":" : ""));
            else {
                upperString += ": " + event.getName();
                text.setText(upperString);
            }
            
            /* Sleep one minute */
            try {
                Thread.sleep(1000 * 60);
            } catch (InterruptedException e) {
                return;
            }
            
            minDiff--;
        }
    }
    
    /**
     * Stop this Thread. This will stop the refreshments.
     */
    public void quit() {
        this.stop = true;
    }
}
