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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;

/**
 * Music utilities.
 * @author Johannes Steltzer
 *
 */
public final class Music {
	
	/**
	 * Constructor not for public use.
	 */
	private Music() { }
	
	private static Logger logger = Logger.getLogger(Music.class);
	
	/**
	 * Plays an audio theme.
	 * @param path - Path within the jar-file or external path
	 * @param internal - True if paht is within the jar-file, false if
	 * 		path is external (file system)
	 * @throws LineUnavailableException 
	 * @throws IOException 
	 * @throws UnsupportedAudioFileException 
	 */
	public static void playTheme(String path, boolean internal) 
			throws LineUnavailableException, UnsupportedAudioFileException, 
			IOException {
		logger.debug("play theme: " + path);

		Clip clip = AudioSystem.getClip();
		AudioInputStream audioInputStream;
		if (internal)
			audioInputStream = AudioSystem.getAudioInputStream(
					new BufferedInputStream(Music.class.getClassLoader().
							getResourceAsStream(path)));
		else
			audioInputStream = AudioSystem.getAudioInputStream(new File(path));
		clip.open(audioInputStream);
		clip.start();
	}
}
