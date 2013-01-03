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

import java.io.File;
import java.net.URL;
import java.util.Scanner;

import javax.swing.JOptionPane;

import de.jsteltze.calendar.frames.CalendarFrame;
import de.jsteltze.common.Download;
import de.jsteltze.common.Logger;
import de.jsteltze.common.ProgressBar;

/**
 * Calendar update. Connects to the Internet and looks for new version.
 * @author Johannes Steltzer
 *
 */
public class Update 
	implements Runnable {
	
	/* different steps */
	private static final int STEP_GET_RELEASE = 0;
	private static final int STEP_GET_NEW_VERSION = 1;
	private static final int STEP_GET_UPDATER = 2;
	private static final int STEP_FINISH = 3;

	/** progressbar */
	private ProgressBar pbar;
	
	/** downloader */
	private Download down;
	
	/** download timeout: 1 minute */
	private static final int TIMEOUT_MSEC = 60000;
	
	/** update interval: half a second */
	private static int UPDATE_MSEC = 500;
	
	/** parent calendar object to update */
	private CalendarFrame caller; 
	
	/** current step (see STEP_XXX) */
	private int step;
	
	/** quite mode? */
	private boolean quiet;

	/**
	 * Construct a new update. This will connect to the Internet and
	 * look for new version.
	 * @param c - Parent calendar object
	 * @param quite - True for quiet mode (no messages), use for auto
	 * 		update
	 */
	public Update(CalendarFrame c, boolean quiet) {
		this.caller = c;
		this.quiet = quiet;
		step = STEP_GET_RELEASE;
		try {
			if (!quiet)
				pbar = new ProgressBar(caller,
						"Suche neue Version...", false);
			else
				pbar = null;
			down = new Download(new URL(Calendar.DOWNLOAD_URL
					+ Calendar.RELEASE_FILE), Calendar.RELEASE_FILE);
			new Thread(this).start();
		} catch (Exception e) {
			Logger.error(e.toString());
			JOptionPane.showMessageDialog(caller,
					"Es konnte keine Verbindung zum Internet hergestellt werden!",
					"Update...", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Parse remove version file and check if update is available.
	 * If yes, start downloading new version.
	 */
	private void getNewVersion() {
		try {
			Logger.debug("Download status=" + down.getStatus());
			
			/*
			 * Throw error if previous download was incomplete
			 */
			if (down.getStatus() != Download.COMPLETE) {
				if (!quiet)
					error("Der Download ist fehlgeschlagen!");
				return;
			}
			
			/*
			 * Try parsing remote release version
			 */
			File release = new File(Calendar.RELEASE_FILE);
			Scanner sc = new Scanner(release);
			String versionLine = sc.nextLine();
			sc.close();
			release.delete();
			Logger.debug("READ version: " + versionLine);
			boolean updateNeeded = false;
			try {
				String remoteVersion[] = versionLine.split("\\.");
				String localVersion[] = Calendar.VERSION.split("\\.");
				String remoteSubversion[] = remoteVersion[1].split("_");
				String localSubversion[] = localVersion[1].split("_");
				
				remoteSubversion[1] = remoteSubversion[1].substring(3);
				localSubversion[1] = localSubversion[1].substring(3);
				
				if (Integer.parseInt(remoteVersion[0]) > Integer.parseInt(localVersion[0])
						|| Integer.parseInt(remoteSubversion[0]) > Integer.parseInt(localSubversion[0])
						|| Integer.parseInt(remoteSubversion[1]) > Integer.parseInt(localSubversion[1]))
					updateNeeded = true;
				
			} catch(Exception e) {
				if (!quiet)
					error("Versionsdatei kann nicht gelesen werden!");
				return;
			}
			
			/*
			 * Proceed if remote version was readable and new release
			 * is available
			 */
			if (updateNeeded) {
				if (quiet) {
					caller.updateAvailable(versionLine);
					return;
				}
				
				/*
				 * Notify user and wait for OK
				 */
				if (JOptionPane.showConfirmDialog(caller, 
						"Es ist ein Update verfügbar.\nSoll die neue Version jetzt heruntergeladen und installiert werden?",
						"Update verfügbar", JOptionPane.YES_NO_OPTION)
						== JOptionPane.NO_OPTION)
					return;
				
				/*
				 * Download new version
				 */
				pbar = new ProgressBar(caller,
						"Neue Version herunterladen...", false);
				down = new Download(new URL(Calendar.DOWNLOAD_URL
						+ Calendar.FILENAME), Calendar.NEW_FILENAME);
				new Thread(this).start();
			} 
			else {
				if (quiet)
					Logger.debug("no update needed");
				else
					JOptionPane.showMessageDialog(caller,
							"Der Kalender ist auf dem aktuellen Stand!",
							"Update...", JOptionPane.PLAIN_MESSAGE);
			}
		} 
		catch (Exception e) {
			Logger.error(e.toString());
			error("Fehler: " + e.toString());
		}		
	}
	
	/**
	 * Start downloading updater program.
	 */
	private void getUpdater() {
		try {
			Logger.debug("Download status=" + down.getStatus());
			
			/*
			 * Throw error if previous download was incomplete
			 */
			if (down.getStatus() != Download.COMPLETE) {
				error("Der Download ist fehlgeschlagen!");
				return;
			}
			
			/*
			 * Download updater program
			 */
			pbar = new ProgressBar(caller,
					"Updater herunterladen...", false);
			down = new Download(new URL(Calendar.DOWNLOAD_URL
					+ Calendar.UPDATER));
			new Thread(this).start();
		} catch (Exception e) {
			e.printStackTrace();
			error("Der Download ist fehlgeschlagen!");
		}
	}
	
	/**
	 * Finalize update by calling updater program. This will
	 * exit current program, delete old version, rename new version
	 * and start new version. 
	 */
	private void finishUpdate() {
		try {
			Logger.debug("Download status=" + down.getStatus());
			if (down.getStatus() != Download.COMPLETE) {
				error("Der Download ist fehlgeschlagen!");
				return;
			}
			JOptionPane.showMessageDialog(caller,
					"Um das Update abzuschließen,\nmuss der Kalender neu gestartet werden.",
					"Update...", JOptionPane.PLAIN_MESSAGE);
			
			/*
			 * Execute updater
			 */
			String cmd = "java -jar " + Calendar.UPDATER + " \""
					+ Calendar.FILENAME + "\" \"" + Calendar.NEW_FILENAME + "\"";
			String[] args = caller.getArgs();
			for (String arg : args)
				cmd += " " + arg;
			Runtime.getRuntime().exec(cmd);
			
			/* call shutdown hook */
			System.exit(0);
		} catch (Exception e) {
			Logger.error(e.toString());
			new File(Calendar.NEW_FILENAME).delete();
			new File(Calendar.UPDATER).delete();
			error("Update fehlgeschlagen: " + e.toString());
		}
	}

	/**
	 * Proceed with a next step.
	 */
	private void next() {
		if (step == STEP_GET_NEW_VERSION)
			getNewVersion();
		else if (step == STEP_GET_UPDATER)
			getUpdater();
		else if (step == STEP_FINISH)
			finishUpdate();
	}

	@Override
	public void run() {
		int i = 0;
		
		/*
		 * While downloading, update progressbar each UPDATE_MSEC
		 * milliseconds
		 */
		while (down.getStatus() == Download.DOWNLOADING
				|| i++ > TIMEOUT_MSEC / UPDATE_MSEC) {
			if (pbar != null) {
				pbar.setValue((int) down.getProgress());
				if (pbar.isCancelled()) {
					down.cancel();
					new File(Calendar.NEW_FILENAME).delete();
					new File(Calendar.RELEASE_FILE).delete();
					new File(Calendar.UPDATER).delete();
					pbar.close();
					return;
				}
			}
			try {
				Thread.sleep(UPDATE_MSEC);
			} catch (InterruptedException e) {}
		}
		
		/* Close progressbar and proceed with next step */
		if (pbar != null)
			pbar.close();
		step++;
		next();
	}

	/**
	 * Display error message.
	 * @param msg - Error message to display
	 */
	private void error(String msg) {
		JOptionPane.showMessageDialog(caller, msg,
				"Update...", JOptionPane.ERROR_MESSAGE);
		if (pbar != null)
			pbar.close();
	}
}
