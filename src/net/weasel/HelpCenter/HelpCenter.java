package net.weasel.HelpCenter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import java.net.*;

public class HelpCenter extends JavaPlugin 
{
	public static long rSeed = java.util.GregorianCalendar.MILLISECOND;
	public static boolean hasWelcomeFile = false;
	public static Random gen = new java.util.Random(rSeed);
	public static int item;
	public static String pluginName = "";
	public static String pluginVersion = "";
	
	private final HelpCenterListener playerListener = new HelpCenterListener(this);
	public static final String DATE_FMT = "yyyy-MM-dd HH:mm:ss";
	
	@Override
	public void onDisable() 
	{
		logOutput("HelpCenter disabled.");
	}

	@Override
	public void onEnable() 
	{
		pluginName = this.getDescription().getName();
		pluginVersion = this.getDescription().getVersion();
		initHelpCenter();
		
		logOutput( pluginName + " v" + pluginVersion + " enabled." );
	}

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	String pCommand = command.getName().toLowerCase();
    	String contents = "";
    	boolean retVal = false;
    	
    	if( sender instanceof Player )
    	{
    		
	    	if((pCommand.equals("help")) || (pCommand.equals("?")))
			{			
	    		if( sender instanceof Player )
	    		{
		    		if( args.length > 0 )
		    		{
		    			try 
		    			{
		    				contents = getHelpFile( player, arrayToString(args, " ") );
		    				retVal = true;
		     			} 
		    			catch (IOException e) 
		    			{
							contents = "#!RUnable to load helpfile.#!w";
		    				e.printStackTrace();
		    			}
		    		}
		    		else    			
					{
						try 
						{
							contents = getHelpFile( player, "HCdefault" );
							retVal = true;
						} 
						catch (IOException e) 
						{
							contents = "#!RUnable to load helpfile.#!w";
							e.printStackTrace();
						}
					}
				}
		    	
		    	try 
		    	{
					processHelp( player, contents, arrayToString(args," ") );
				}
		    	catch (IOException e) 
		    	{
					sendToPlayer( player, "#!RUnable to load helpfile.#!w", null, null );
					e.printStackTrace();
				}
			}
	    	else
	    	{
	    		return false;
	    	}
		}
    	
		return retVal;
    }

	public static void sendToPlayer( Player player, String item, String[] multi, String topic )
	{
		if( multi == null )
		{
			try 
			{
				player.sendMessage( cColour( parseHelpLine( player, item, topic ) ) );
			} 
			catch (IOException e) 
			{
				// Do nothing..
			}
		}
		else
		{
			for( int X = 0; X < multi.length; X++ )
			{
				try 
				{
					player.sendMessage( cColour( parseHelpLine( player, multi[X], topic ) ) );
				} 
				catch( IOException e )
				{
					// Do nothing..
				}
			}
		}
	}
	
	public static String cColour( String message )
	{
		CharSequence cChk = null;
		String temp = null;
		String[] Colours = { "#!k", "#!b", "#!g", "#!c", "#!r", "#!m", "#!y", "#!w", 
							 "#!K", "#!B", "#!G", "#!C", "#!R", "#!M", "#!Y", "#!W" };
		
		ChatColor[] cCode = { ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN,
				           ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE,
				           ChatColor.GOLD, ChatColor.GRAY, ChatColor.DARK_GRAY,
				           ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED,
				           ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE };
		
		for( int x = 0; x < Colours.length; x++ )
		{
			cChk = Colours[x];
			
			if( message.contains(cChk))
			{
				temp = message.replace(cChk, cCode[x].toString() );
				message = temp;
			}
		}
		
		return message;
		
	}
	
	public static class Dir implements FileFilter
	{
		private final String[] fileEx = new String[] { "txt" };
		
		static File[] getFiles(File path)
		{
			File files[];
			FileFilter filter = null;
			
			files = path.listFiles(filter);
		
			Arrays.sort(files);
			
			return( files );
		}

		@Override
		public boolean accept(File file) 
		{
			for( String extension : fileEx )
			{
				if( file.getName().toLowerCase().endsWith(extension))
				{
					return true;
				}
			}
			
			return false;
		}
	}
	
	public static void logOutput(String output)
	{
		System.out.println( "[HelpCenter] " + output );
	}

	public void fileLocationUpgrade()
	{
		logOutput( "Checking file locations for upgrade availability.." );
		
		File path = new File("plugins/HelpCenter");
		
		if( path.exists() == true ) 
		{
			logOutput( "Location OK." );
			return;
		}
		
		path = new File("HelpCenter");
			
		if( path.exists() == true )
		{
			path = new File("plugins/HelpCenter");
				
			if( path.exists() == false )
			{
				logOutput("Directory 'plugins/HelpCenter' does not exist.");
				
				path = new File( "HelpCenter" );
					
				if( path.exists() == true )
				{
					File newPath = new File("plugins/HelpCenter");
					path.renameTo(newPath);
					logOutput( "HelpCenter folder moved to plugins/HelpCenter." );
				}
				else
					logOutput( "Unable to find any HelpCenter folder!" );
			}
			
		}
	}
	
	public static String[] lineWrap( String line )
	{
		if( line.startsWith("[" ) ) return( stringToArray( line ) );
		
		String[] retVal = null;
		String retTemp = "";
		String tempVal = "";
		
		if( line.length() > 60 )
		{
			String[] words = line.split(" ");
			
			for( int X = 0; X < words.length; X++ )
			{
				tempVal += words[X] + " ";
				
				if( tempVal.length() > 60 )
				{
					String[] foo = tempVal.trim().split(" ");
					foo[foo.length-1] = "";
					retTemp += arrayToString(foo," ").trim() + "ZQX123!";
					tempVal = "";
					X--;
				}
			}
			
			retTemp += tempVal;
			
			retVal = retTemp.split( "ZQX123!" );
		}
		else
			retVal = stringToArray(line);

		return retVal;
	}
	
	public static String[] stringToArray( String line )
	{
		return( line.concat("ZQX123!").split( "ZQX123!" ) );
	}
	
	public void initHelpCenter()
	{
		fileLocationUpgrade();
		
		PluginManager pm = getServer().getPluginManager();		
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Priority.Normal,this );
		Plugin plugin = pm.getPlugin("HelpCenter");
		
		File path = new File("plugins/HelpCenter");
		
		if( path.exists() == false )
		{
			logOutput("Attempting to create new folder plugins/HelpCenter..");
			
			if( path.mkdir() == true )
			{
				logOutput("Created successfully.");
			}
			else
			{
				logOutput("Unable to create directory! Do you have write permission?");
				pm.disablePlugin(plugin);
			}
		}
	}
	
	public static String getHelpFile(Player who, String which) throws IOException
	{
		CharSequence checkStr = "../";
		File hFile = null;
		String retVal = "";
		String player = who.getName();
		
		if( which == "" )
		{
			return( "" );
		}
		
		if( which != "HCdefault" )
		{
			hFile = new File("plugins/HelpCenter/" + which.toLowerCase() + ".txt");
		}
		else
		{
			hFile = new File("plugins/HelpCenter/" + which + ".txt");
		}
		
		if( which.contains(checkStr) == true )
		{
			logOutput( player + " tried to cheat the HelpCenter! : " + which.toString() );
			retVal = "#!RNo help found for #!Y" + which + "#!R.#!w";
		}
		
		if( hFile.exists() == false )
		{
			logOutput( player + " tried to view a nonexistent help file: " + which + ".txt" );
			retVal = "#!RNo help found for #!Y" + which + "#!R.#!w";
		}
		else
		{
			byte[] buf = new byte[(int)hFile.length()];
			BufferedInputStream fr = null;
			
			try
			{
				fr = new BufferedInputStream(new FileInputStream(hFile.toString()));
				fr.read(buf);
			}
			finally
			{
				if( fr != null ) try { fr.close(); } catch (IOException ignored) { }
			}
			
			retVal = new String(buf);
			
		}
		
		return retVal;
	}
	
	public static String fetchWebHelp(String who, String address) throws MalformedURLException
	{
		logOutput( "Fetching help URL for " + who + ": " + address );
		String result = null;
		URLConnection fConn = null;
		
		try
		{
			fConn = new URL(address).openConnection();
			Scanner fSc = new Scanner(fConn.getInputStream());
			fSc.useDelimiter("\\Z");
			result = fSc.next();
		}
		catch (Exception e)
		{
			logOutput( "Unable to fetch web help URL: " + address );
			return( "Unable to fetch web help." );
		}
		
		return( result );
	}

    public static String arrayToString(String[] a, String separator) 
    {
        String result = "";
        
        if (a.length > 0) 
        {
            result = a[0];    // start with the first element
            for (int i=1; i<a.length; i++) {
                result = result + separator + a[i];
            }
        }
        
        return result;
    }
        
    public static void processHelp( Player who, String helpData, String topic ) throws IOException
    {
    	CharSequence chkStr = "";
		String[] wrapData = null;
    	
    	helpData.replace( "\r", "" );
    	
    	chkStr = "#!HELP";
		
    	if( helpData.contains( chkStr ) )
    	{
    		String[] helps = helpData.split( (String) chkStr, 0 );
    		
    		int X = 0;
    		
    		while( X < 1 ) { X = gen.nextInt(helps.length); }
    	
    		String helpItem = helps[X];
    		
    		if( helpItem.length() > 2)
    		{
        		chkStr = "\n";
            	
        		if( helpItem.contains( chkStr ) )
        		{
        			String subHelps[] = helpItem.split( "\n" );
        			String item = "";
        		
        			for( X = 0; X < subHelps.length; X++ )
        			{
        				item = subHelps[X];
        				
        				if( item != "" )
        				{
        					wrapData = lineWrap( item );
        					
        					if( wrapData.length > 1 )
        						sendToPlayer( who, null, wrapData, topic );
        					else
        						sendToPlayer( who, item, null, topic );
        				}
        			}
        		}
    		}
    	}
    	
    	else
    	{
    		chkStr = "\n";
    	
    		if( helpData.contains( chkStr ) )
    		{
    			String[] helps = helpData.split( "\n" );
    			String item = "";
    		
    			for( int X = 0; X < helps.length; X++ )
    			{
    				item = helps[X];
    				
    				if( item != "" )
    				{
    					wrapData = lineWrap( item );
    					
    					if( wrapData.length == 1 )
    					{
    						sendToPlayer( who, item, null, topic );
    					}
    					else
    					{
	    					sendToPlayer( who, null, wrapData, topic );
    					}
    				}
    			}
    		}
    		else
    		{
    			wrapData = lineWrap( helpData );

    			if( wrapData.length == 1 )
				{
					sendToPlayer( who, helpData, null, topic );
				}
				else
				{
					sendToPlayer( who, null, wrapData, topic );
				}
    		}
    	}
    }

    public static String parseHelpLine( Player who, String helpLine, String topic ) throws IOException
    {
    	if( helpLine.length() < 5 )
    		return( helpLine );
    	
    	if( helpLine.substring(0,5).equals("[URL "))
		{
			String sub = helpLine.substring(5);
			String[] spl = null;
			
			if( sub.contains(" ]"))
			{
				spl = sub.split(" ]");
			}
			else if( sub.contains("]"))
			{
				spl = sub.split( "]");
			}
			else
			{	
				logOutput( "Error in URL string formatting for " + helpLine );
				return( "#!RThe help system encountered an error.#!w" );
			}
				
			return( parseHelpLine( who, fetchWebHelp(who.getDisplayName(), spl[0]), topic ) );
		}

		if( helpLine.substring(0,6).equals("[HELP "))
		{
			String sub = helpLine.substring(6);
			String[] spl = null;
			
			if( sub.contains(" ]"))
				spl = sub.split(" ]");
			else if( sub.contains("]"))
				spl = sub.split( "]");
			else
			{
				logOutput( "Error in redirector string formatting for " + helpLine );
				return( "#!RThe help system encountered an error.#!w" );
			}
			
			if( spl[0].compareTo(topic) == 0 )
			{
				logOutput( "Infinite loop detected in " + spl[0] + ".txt! Please fix it." );
				return( "#!RThe help system encountered an error.#!w" );
			}
			else
			{
				processHelp( who, getHelpFile( who, spl[0] ), topic );
				return( "" );
			}
		}		
		else
		{
			return( helpLine );
		}
    }
}
