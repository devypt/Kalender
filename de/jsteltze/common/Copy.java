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

	/**
	 * Start a new copy process.
	 * @param caller - Calling frame for a progress bar or null (no
	 * 		progress bar)
	 * @param in - Source file
	 * @param out - Destination file
	 */
	public Copy(Window caller, File in, File out) {
		this.in = in;
		this.out = out;
		this.caller = caller;
		new Thread(this).start();
	}

	@Override
	public void run() {
		FileChannel inChannel = null, outChannel = null;
		ProgressBar pbar = caller == null ? null : new ProgressBar(caller, 
				Trans.t("Copy")+" \"" + in.getName() + "\"...", false);
		try {
			inChannel = new FileInputStream(in).getChannel();
			outChannel = new FileOutputStream(out).getChannel();
            transfer(inChannel, outChannel, in.length(), 1024 * 1024 * 32, pbar);
		} 
		catch (IOException i) {
			Logger.error("copy: " + i.toString());
			JOptionPane.showMessageDialog(caller,
					Trans.t("Copying failed")+"!", Trans.t("error")+"...",
					JOptionPane.ERROR_MESSAGE);
		} 
		finally {
			try {
				if (inChannel != null)
					inChannel.close();
				if (outChannel != null)
					outChannel.close();
			} catch (IOException e) {
			}
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
    public static void transfer(FileChannel fileChannel, ByteChannel byteChannel, 
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
            Logger.debug("copy: bytes transferred: " + overallBytesTransfered +
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
}
