package com.pokerisland;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Utils
{
	/**
	 * Takes in a String and converts it to a hash using SHA 256
	 * @param base - input String
	 * @return SHA 256 hashed input
	 */
	public static String sha256(String base) 
	{
		StringBuffer hexString = null;
		try
	    {
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] hash = digest.digest(base.getBytes("UTF-8"));
	        hexString = new StringBuffer();

	        for (int i = 0; i < hash.length; i++) {
	            String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }
	    } 
	    catch (NoSuchAlgorithmException e)
		{
			System.out.println("NoSuchAlgorithmException running SHA256 Hash: " + e.getMessage());
		}
		catch(UnsupportedEncodingException e)
		{
			System.out.println("UnsupportedEncodingException running SHA256 Hash: " + e.getMessage());
		}

        return hexString.toString();
	}
	
	/**
	 * Checks the Runtime environment to see if any of the desired fonts are installed
	 */
	public static void getFontName()
	{
		if(System.getProperty("os.name").equals("Windows 7") || System.getProperty("os.name").equals("Windows 8"))
		{
			Constants.jpFontName = "Meiryo";
			Constants.knFontName = "Malgun Gothic";
			Constants.inFontName = "Aparajita";
		}
		else if(System.getProperty("os.name").equals("Mac OS X"))
		{
			Constants.jpFontName = "Osaka";
			Constants.knFontName = "AppleGothic";
			Constants.inFontName = "Kohinoor Devanagari Book";
		}
		String [] availableFonts = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for(int i = 0; i < availableFonts.length; i++)
		{
			//System.out.println(i+": "+availableFonts[i]);
			if(System.getProperty("os.name").equals("Mac OS X"))
			{
				if(availableFonts[i].equals("Helvetica"))
				{
					Constants.fontName = "Helvetica LT MM";
				}
				if(availableFonts[i].equals("Helvetica Neue"))
				{
					Constants.fontName = "HelveticaNeue";
				}
			}
			else
			{
				if(availableFonts[i].equals("Helvetica LT Std"))
				{
					Constants.fontName = "Helvetica LT Std";
				}
			}
		}
	}

	/**
	 * Takes in username and two hashed passwords and determines whether or not this is valid
	 * @param username user's username
	 * @param passwordOne the password
	 * @param passwordTwo the password (confirmed)
	 * @return whether or not the input was valid
	 */
	public static boolean checkUsernamePasswordValidity(String username, String passwordOne, String passwordTwo)
	{
		if(passwordOne.equals(passwordTwo) && !passwordOne.equals(Utils.sha256(""))) //we check the blank hash to make sure the password isn't blank
		{
			//both passwords are good
			if(username.length() < 3 || username.trim().equals(""))
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Takes in a string and converts it to titlecase
	 * @param toTitle the string to convert
	 * @return the string in titlecase
	 */
	public static String titleCase(String toTitle)
	{
		return Character.toUpperCase(toTitle.charAt(0)) + toTitle.substring(1);
	}
}
