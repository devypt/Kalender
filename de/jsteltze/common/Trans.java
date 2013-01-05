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

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Get texts/labels depending on the current locale.
 * @author www.javadb.com
 */
public final class Trans {
    
    private static final ResourceBundle rb = 
            ResourceBundle.getBundle("translations/local", Locale.GERMAN); 
    
    /**
     * Returns the text/label with the specified key.
     * @param key - Key
     * @return the text/label value of the key.
     */
    public static String getMessage(String key) {
        try {
            return rb.getString(key);
        } catch (Exception e) {
            return key;
        }
    }
    
    /**
     * Returns the text/label with the specified key.
     * @param key - Key
     * @param params - Parameters list for the replacement
     *      of {1}, {2}, ...
     * @return the text/label value of the key.
     */
    public static String getMessage(String key, String[] params) {
        String message = getMessage(key);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                message = message.replaceAll("\\{" + (i + 1) + "\\}", 
                        params[i]);
            }
        }
        return message;
    }
}
