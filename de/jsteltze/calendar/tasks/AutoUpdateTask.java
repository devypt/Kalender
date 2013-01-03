package de.jsteltze.calendar.tasks;

import java.util.TimerTask;

import de.jsteltze.calendar.Update;
import de.jsteltze.calendar.frames.CalendarFrame;
import de.jsteltze.common.Logger;

public class AutoUpdateTask	
	extends TimerTask 
	implements Runnable {
	
	private CalendarFrame c;
	
	public AutoUpdateTask(CalendarFrame c) {
		this.c = c;
	}
	
	public void run() {
		Logger.debug("start auto update");
		new Update(c, true);
	}
	
}
