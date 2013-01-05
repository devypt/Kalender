/*
 *  common-package - various java utilities
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

package de.jsteltze.common;

import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.lang.Math;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

/**
 * Copies a file in the background. Launches a progress bar.
 * @author Johannes Steltzer
 *
 */
public class Copy 
	implements Runnable {
	
	/** source and destination file */
	private File in, out;
	
	/** calling frame (can be null) */
	private Window caller;
	
	private static Logger logger = Logger.getLogger(Copy.class);

	/**
	 * Start a new copy process.
	 * @param caller - Calling frame for a progress bar or null (no
	 * 		progress bar)
	 * @param in - Source file
	 * @param out - Destination file or destination directory; if this
	 *     is a directory the file will be copied with the same name
	 */
	public Copy(Window caller, File in, File out) {
		this.in = in;
		this.out = out.isDirectory() ? new File(out.getAbsolutePath() + 
		        File.separator + in.getName()) : out;
		this.caller = caller;
		new Thread(this).start();
	}

	@Override
	public void run() {
		FileChannel inChannel = null, outChannel = null;
		
		/* Progress bar for files greater than 100KB only! */
		ProgressBar pbar = null;
		if (in.length() > 102400L)
		    pbar = caller == null ? null : new ProgressBar(caller, 
		            "Kopiere \"" + in.getName() + "\"...", false);
		
		try {
			inChannel = new FileInputStream(in).getChannel();
			outChannel = new FileOutputStream(out).getChannel();
            transfer(inChannel, outChannel, in.length(), 1024 * 1024 * 32, pbar);
		} 
		catch (IOException i) {
			logger.error("copy: " + i.toString());
			JOptionPane.showMessageDialog(caller,
					"Das Kopieren ist fehlgeschlagen!", "Fehler...",
					JOptionPane.ERROR_MESSAGE);
		} 
		finally {
			try {
				if (inChannel != null)
					inChannel.close();
				if (outChannel != null)
					outChannel.close();
			} catch (IOException e) {
			    logger.error("file channel cannot be closed in finally statement...", e);
			}
			if (pbar != null)
			    pbar.close();
		}
	}
	
	/**
	 * Start a file transfer.
	 * @param fileChannel - Source channel
	 * @param byteChannel - Destination channel
	 * @param lengthInBytes - File size in bytes
	 * @param bufferSize - Buffer size for copying
	 * @param pbar - Progress bar or null
	 * @throws IOException
	 */
    private static void transfer(FileChannel fileChannel, ByteChannel byteChannel, 
    		long lengthInBytes, long bufferSize, ProgressBar pbar)
            throws IOException {
 
        long overallBytesTransfered = 0L;
        while (overallBytesTransfered < lengthInBytes) {
            long bytesTransfered = 0L;
            bytesTransfered = fileChannel.transferTo(0, 
            		Math.min(bufferSize, lengthInBytes - overallBytesTransfered), 
            		byteChannel);
 
            overallBytesTransfered += bytesTransfered;
            int progress = (int) Math.round(overallBytesTransfered / 
            		((double) lengthInBytes) * 100.0);
            logger.debug("copy: bytes transferred: " + overallBytesTransfered +
                    " (" + progress + ")");
            
            if (pbar != null) {
            	pbar.setValue(progress);
            	if (pbar.isCancelled()) {
            		pbar.close();
            		return;
            	}
            }
        }
    }
    
    /**
     * Copy all files from one directory to another.
     * @param caller - Parent frame or null; if this is not null
     *      a progress bar will appear
     * @param srcFolder - Source directory
     * @param dstFolder - Destination directory
     */
    public static void copyAll(Window caller, File srcFolder, File dstFolder) {
        File files[] = null;
        if (srcFolder.exists() && srcFolder.isDirectory())
            files = srcFolder.listFiles();
        else {
            logger.error("[copyAll] cannot copy: srcFolder is not a valid directory");
            return;
        }
        
        if (!dstFolder.exists() && !dstFolder.mkdir()) {
            logger.error("[copyAll] cannot copy: dstFolder cannot be created");
            return;
        }
        
        if (files != null) {
            logger.debug("copy " + files.length + " files from " + 
                    srcFolder.getAbsolutePath() + " to " + dstFolder.getAbsolutePath());
            for (File f : files) {
                logger.debug("copy file: " + f.getName());
                new Copy(caller, f, dstFolder);
            }
        }
    }
}
