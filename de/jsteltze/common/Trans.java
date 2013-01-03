package de.jsteltze.common;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 *
 * @author www.javadb.com
 */
public class Trans {
    

 
    public static void main(String[] args) {
        
      
    }
    public static String t(String key)
    {
    	 ResourceBundle rb = ResourceBundle.getBundle("translations/local", Locale.US);
    	try{
 		 String value = rb.getString(key);
 		return value;
    	}catch(Exception e){
    		return key;
    	 }
 		 
    }
    

 
 }
