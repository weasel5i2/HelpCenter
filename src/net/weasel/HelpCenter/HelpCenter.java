package net.weasel.HelpCenter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Location;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import java.net.*;
public class HelpCenter extends JavaPlugin {
    private static final Yaml yaml = new Yaml(new SafeConstructor());
    public static long rSeed = java.util.GregorianCalendar.MILLISECOND;
    public static boolean hasWelcomeFile = false;
    public static Random gen = new java.util.Random(rSeed);
    public static int item;
    public static String pluginName = "";
    public static String pluginVersion = "";
    private static String pluginHelpPathNoSlash = "plugins/HelpCenter";
    private static String pluginHelpPath = pluginHelpPathNoSlash + "/";
    private final HelpCenterListener playerListener = new HelpCenterListener(this);
    public static final String DATE_FMT = "yyyy-MM-dd HH:mm:ss";
    public static PermissionHandler Permissions;

    @Override
    public void onDisable() {
        logOutput("HelpCenter disabled.");
    }
    @Override
    public void onEnable() {
        setupPermissions();
        pluginName = this.getDescription().getName();
        pluginVersion = this.getDescription().getVersion();
        initHelpCenter();

        logOutput(pluginName + " v" + pluginVersion + " enabled.");
    }
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String pCommand = command.getName().toLowerCase();
        boolean retVal = false;
        if((pCommand.equals("help")) || (pCommand.equals("?"))) {

            String contents = "";
            if(sender instanceof Player){
                if( args.length > 0 ) {
                    if ((args[0].toLowerCase().equals("buildhelpfiles"))) {
                        buildHelpFiles((Player)sender);
                           retVal = true;
                           return (retVal);
                    } else if ((args[0].toLowerCase().equals("buildplugin"))) {
                         buildHelpFile((Player)sender, args[1]);
                           retVal = true;
                           return (retVal);
                    } else {
                        try {
                            contents = getHelpFile((Player)sender, arrayToString(args, " ").replace("/", "").replace(".", "") );
                            retVal = true;
                        } catch (IOException e) {
                            contents = "#!RUnable to load helpfile.#!w";
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        contents = getHelpFile((Player)sender, "HCdefault" );
                        retVal = true;
                    } catch (IOException e) {
                        contents = "#!RUnable to load helpfile.#!w";
                        e.printStackTrace();
                    }
                }

                try {
                    processHelp((Player)sender, contents, arrayToString(args," ") );
                } catch (IOException e) {
                    sendToPlayer((Player)sender, "#!RUnable to load helpfile.#!w", null, null );
                    e.printStackTrace();
                }
            }
        }
        return retVal;
    }
    public static void sendToPlayer(Player player, String item, String[] multi, String topic) {
        if( multi == null ) {
            try {
                player.sendMessage( cColour( parseHelpLine( player, item, topic ) ) );
            } catch (IOException e) {
                // Do nothing..
            }
        } else {
            for (int X = 0; X < multi.length; X++) {
                try {
                    player.sendMessage( cColour( parseHelpLine( player, multi[X], topic ) ) );
                } catch( IOException e ) {
                    // Do nothing..
                }
            }
        }
    }
    public static String cColour(String message) {
        CharSequence cChk = null;
        String temp = null;
        String[] Colours = { "#!k", "#!b", "#!g", "#!c", "#!r", "#!m", "#!y", "#!w",
                             "#!K", "#!B", "#!G", "#!C", "#!R", "#!M", "#!Y", "#!W" };

        ChatColor[] cCode = {ChatColor.BLACK,     ChatColor.DARK_BLUE,    ChatColor.DARK_GREEN, ChatColor.DARK_AQUA,
                             ChatColor.DARK_RED,  ChatColor.DARK_PURPLE,  ChatColor.GOLD,       ChatColor.GRAY,
                             ChatColor.DARK_GRAY, ChatColor.BLUE,         ChatColor.GREEN,      ChatColor.AQUA,
                             ChatColor.RED,       ChatColor.LIGHT_PURPLE, ChatColor.YELLOW,     ChatColor.WHITE };

        for (int x = 0; x < Colours.length; x++) {
            cChk = Colours[x];
            if (message.contains(cChk)) {
                temp = message.replace(cChk, cCode[x].toString());
                message = temp;
            }
        }
        return message;
    }
    public static class Dir implements FileFilter {
        private final String[] fileEx = new String[] {"txt"};

        static File[] getFiles(File path) {
            File files[];
            FileFilter filter = null;
            files = path.listFiles(filter);
            Arrays.sort(files);
            return(files);
        }

        @Override
        public boolean accept(File file) {
            for (String extension : fileEx) {
                if( file.getName().toLowerCase().endsWith(extension)) {
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
    public void fileLocationUpgrade() {
        logOutput( "Checking file locations for upgrade availability.." );
        File path = new File(pluginHelpPathNoSlash);
        if (path.exists() == true) {
            logOutput("Location OK.");
            return;
        }
        path = new File("HelpCenter");
        if (path.exists() == true) {
            path = new File(pluginHelpPathNoSlash);

            if (path.exists() == false) {
                logOutput("Directory '" + pluginHelpPathNoSlash + "' does not exist.");
                path = new File("HelpCenter");

                if (path.exists() == true) {
                    File newPath = new File(pluginHelpPathNoSlash);
                    path.renameTo(newPath);
                    logOutput("HelpCenter folder moved to '" + pluginHelpPathNoSlash + "'.");
                } else
                    logOutput("Unable to find any HelpCenter folder!");
            }
        }
    }
    public static String[] lineWrap(String line) {
        if(line.startsWith("[" )) return(stringToArray(line));

        String[] retVal = null;
        String retTemp = "";
        String tempVal = "";

        if(line.length() > 60) {
            String[] words = line.split(" ");
            for(int X = 0; X < words.length; X++) {
                tempVal += words[X] + " ";
                if(tempVal.length() > 60) {
                    String[] foo = tempVal.trim().split(" ");
                    foo[foo.length-1] = "";
                    retTemp += arrayToString(foo," ").trim() + "ZQX123!";
                    tempVal = "";
                    X--;
                }
            }
            retTemp += tempVal;
            retVal = retTemp.split( "ZQX123!" );
        } else
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

        File path = new File(pluginHelpPathNoSlash);
        if( path.exists() == false )
        {
            logOutput("Attempting to create new folder '" + pluginHelpPathNoSlash + "'.");
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
    public void buildHelpFiles(Player player) {
        PluginManager pluginManager = getServer().getPluginManager();
        Plugin[] plugins = pluginManager.getPlugins();
        String pluginName;
        String[] pluginNameSplit;

        try{
            // Create file
            File path = new File(pluginHelpPath + "/HCdefault.txt");
            if (path.exists() == false) {
                logOutput("Building 'HCdefault' plugin help.");
                sendToPlayer(player, "Building 'HCdefault' plugin help.", null, null );

                FileWriter fstream = new FileWriter(path);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write("Available Plugins: (type '/help <plugin>' for more info)\n-----------------\n");

                //Loop through each plugin we can find.
                for (Plugin plugin : plugins) {
                    pluginNameSplit = plugin.toString().split("@")[0].split("\\.");
                    pluginName = pluginNameSplit[pluginNameSplit.length-1].replace("plugin", "").replace("Plugin", "");
                    logOutput("Building '" + pluginName + "' plugin help.");
                    sendToPlayer(player, "Building '" + pluginName + "' plugin help.", null, null );
                    out.write(" " + pluginName);
                    buildHelpFile(player, pluginName);
                }

                //Close the output stream
                out.close();
            } else {
                sendToPlayer(player, "HCdefault already exists. Please remove before building plugin help files..", null, null );
            }
        }catch (Exception e){//Catch exception if any
          System.err.println("Error: " + e.getMessage());
        }

    }
    @SuppressWarnings("unchecked")
    public void buildHelpFile(Player player, String helpFile) {
        Map<String, Object> commands = null;
        if (!helpFile.matches("^[A-Za-z0-9 _.-]+$")) {
            logOutput("Invalid characters in plugin name.");
            sendToPlayer(player, "Invalid characters in plugin name.", null, null );
            return;
        }

        File path = new File(pluginHelpPath + helpFile.toLowerCase());
        File pathTxt = new File(pluginHelpPath + helpFile.toLowerCase() + ".txt");
        if (path.exists() == false && pathTxt.exists() == false) {
            if (path.mkdir() == true) {
                File jarFileName = new File("plugins/" + helpFile + ".jar");
                if (jarFileName.exists()) {
                    try {
                        JarFile jar = new JarFile(jarFileName);
                        JarEntry pluginYml = jar.getJarEntry("plugin.yml");
                        if (pluginYml == null) {
                            logOutput("No plugin.yml file in jar.");
                            sendToPlayer(player, "No plugin.yml in plugin jar.", null, null );
                            jar.close();
                            return;
                        }

                        InputStream stream = jar.getInputStream(pluginYml); //load the yml file to a stream
                        Map<String, Object> pluginyml = (Map<String, Object>)yaml.load(stream);

                        try{
                            //Create file
                            FileWriter fstreamPlugin = new FileWriter(path + ".txt");
                            BufferedWriter outPlugin = new BufferedWriter(fstreamPlugin);
                            outPlugin.write("Available commands:(type '/help " + helpFile + " <command>' for more info)\n-------------------\n");

                            if (pluginyml.containsKey("commands")) {
                                try {
                                    commands = (Map<String, Object>)pluginyml.get("commands");

                                    for (String command : commands.keySet()) {
                                        Map<String, Object> helpCommand = (Map<String, Object>)commands.get(command);
                                        String description = (String)helpCommand.get("description");
                                        String usage = (String)helpCommand.get("usage");
                                        logOutput("Building " + helpFile + ": " + command);
                                        sendToPlayer(player, "Building " + helpFile + ": " + command, null, null );
                                        outPlugin.write(" /" + command + " ");
                                        command = command.replace("/", "");
                                        try{
                                            // Create file
                                            FileWriter fstream = new FileWriter(path + "/" + command);
                                            BufferedWriter out = new BufferedWriter(fstream);
                                            if (helpCommand.get("description") != null)
                                                out.write("Help for " + command + "\nDescription: " + description);
                                            if (helpCommand.get("usage") != null)
                                                out.write("\nUsage: " + usage.replace("<command>", command));

                                            //Close the output stream
                                            out.close();
                                        }catch (Exception e){//Catch exception if any
                                          System.err.println("Error: " + e.getMessage());
                                        }
    
                                    }
                                } catch (ClassCastException ex) {
                                    logOutput("Plugin commands are of the wrong type.");
                                    sendToPlayer(player, "Plugin commands are of the wrong type.", null, null );
                                }
                            } 
                            //Close the output stream
                            outPlugin.close();
                        } catch (Exception e){//Catch exception if any
                              System.err.println("Error: " + e.getMessage());
                        }
                        
                        //cleanup
                        stream.close();
                        jar.close();

                    } catch (IOException ex) {
                        logOutput("Can not open '" + helpFile + ".jar'.");
                        sendToPlayer(player, "Can not open plugin jar.", null, null );
                    } catch (YAMLException ex) {
                        logOutput("Invalid plugin.yml file.");
                        sendToPlayer(player, "Invalid plugin.yml file.", null, null );
                    }
                } else {
                    logOutput("Plugin '" + helpFile + "' jar does not exist.");
                    sendToPlayer(player, "Plugin jar does not exist.", null, null );
                }
            } else {
                logOutput("Unable to create '" + helpFile + "' plugin directory. Process aborted.");
                sendToPlayer(player, "Unable to create plugin directory. Process aborted.", null, null );
            }
        } else {
            logOutput("Plugin help already defined. Remove plugin directory and file to rebuild.");
            sendToPlayer(player, "Plugin help already defined. Remove plugin help directory and file to rebuild.", null, null );
        }
    }
    public static String doPermissionsStringReplacements(Player who, String HelpString) {
        String player = who.getName().replace(" ", "").replace("/", "").replace(".", "");
        String groups = "groups";
        String group = "prigroup";
        if (HelpCenter.Permissions != null) {
            Location location = who.getLocation(); 
            groups = HelpCenter.Permissions.getGroups(location.getWorld().getName(), player)[0].toString().replace(" ", "").replace("/", "").replace(".", "");
            
            if (groups.toString().indexOf("[") == -1) {
                group = groups;
                groups = "[" + groups + "]";
            } else {
                group = groups.replace("[", "");
                group = group.substring(0, group.indexOf(","));
            }
        }
        HelpString = HelpString.replace("%user%", player)
                               .replace("%prigroup%", group)
                               .replace("%groups%", groups);

        return HelpString;
    }
    public static String getHelpFile(Player who, String which) throws IOException {
        CharSequence checkStr = "../";
        File hFile = null;
        String retVal = "";
        String player = who.getName();

        if( which == "" ) {
            return( "" );
        }

        String GroupDirectory = doPermissionsStringReplacements(who, "%prigroup%/");
        if (!new File(doPermissionsStringReplacements(who, pluginHelpPath + GroupDirectory)).exists()) {
            GroupDirectory = "";
        }

        if( which != "HCdefault" ) {
            hFile = new File(pluginHelpPath + GroupDirectory + which.toLowerCase() + ".txt");
            if (!hFile.exists() || hFile.isDirectory()) {
                hFile = new File(pluginHelpPath + GroupDirectory + which.toLowerCase());
                if (!hFile.exists() || hFile.isDirectory()) {
                    hFile = new File(pluginHelpPath + GroupDirectory + which.replace(" ", "/").toLowerCase() + ".txt");
                    if (!hFile.exists() || hFile.isDirectory()) {
                        hFile = new File(pluginHelpPath + GroupDirectory + which.replace(" ", "/").toLowerCase());
                    }
                }
            }
        } else {
            hFile = new File(doPermissionsStringReplacements(who, pluginHelpPath + "%prigroup%/" + which + ".txt"));
            if (!hFile.exists() || hFile.isDirectory()) {
                hFile = new File(doPermissionsStringReplacements(who, pluginHelpPath + "%prigroup%/" + which));
                if (!hFile.exists() || hFile.isDirectory()) {
                    hFile = new File(pluginHelpPath + which + ".txt");
                    if (!hFile.exists() || hFile.isDirectory()) {
                        hFile = new File(pluginHelpPath + which);
                    }
                }
            }
        }

        if( which.contains(checkStr) == true ) {
            logOutput( player + " tried to cheat the HelpCenter! : " + which.toString() );
            retVal = "#!RNo help found for #!Y" + which + "#!R.#!w";
        } else if(!hFile.exists() || hFile.isDirectory()) {
            logOutput( player + " tried to view a nonexistent help file: " + which );
            retVal = "#!RNo help found for #!Y" + which + "#!R.#!w";
        } else {
            byte[] buf = new byte[(int)hFile.length()];
            BufferedInputStream fr = null;

            try {
                fr = new BufferedInputStream(new FileInputStream(hFile.toString()));
                fr.read(buf);
            } finally {
                if( fr != null ) try { fr.close(); } catch (IOException ignored) { }
            }

            retVal = new String(buf);
        }
        return retVal;
    }
    public static String fetchWebHelp(Player who, String address) throws MalformedURLException
    {
        String player = who.getDisplayName();
        address = doPermissionsStringReplacements(who, address);
        logOutput( "Fetching help URL for " + player + ": " + address );
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
    public static void processHelp( Player who, String helpData, String topic ) throws IOException {
        CharSequence chkStr = "";
        String[] wrapData = null;
        helpData.replace( "\r", "" );
        chkStr = "#!HELP";
        if( helpData.contains( chkStr ) ) {
            String[] helps = helpData.split( (String) chkStr, 0 );
            int X = 0;
            while( X < 1 ) {
                X = gen.nextInt(helps.length);
            }
            String helpItem = helps[X];
            if( helpItem.length() > 2) {
                chkStr = "\n";
                if( helpItem.contains( chkStr ) ) {
                    String subHelps[] = helpItem.split( "\n" );
                    String item = "";
                    for( X = 0; X < subHelps.length; X++ ) {
                        item = subHelps[X];
                        if( item != "" ) {
                            wrapData = lineWrap( item );
                            if( wrapData.length > 1 )
                                sendToPlayer( who, null, wrapData, topic );
                            else
                                sendToPlayer( who, item, null, topic );
                        }
                    }
                }
            }
        } else {
            chkStr = "\n";
            if( helpData.contains( chkStr ) ) {
                String[] helps = helpData.split( "\n" );
                String item = "";
                for( int X = 0; X < helps.length; X++ ) {
                    item = helps[X];
                    if( item != "" ) {
                        wrapData = lineWrap( item );
                        if( wrapData.length == 1 ) {
                            sendToPlayer( who, item, null, topic );
                        } else {
                            sendToPlayer( who, null, wrapData, topic );
                        }
                    }
                }
            } else {
                wrapData = lineWrap( helpData );
                if( wrapData.length == 1 ) {
                    sendToPlayer( who, helpData, null, topic );
                } else {
                    sendToPlayer( who, null, wrapData, topic );
                }
            }
        }
    }
    public static String parseHelpLine( Player who, String helpLine, String topic ) throws IOException {
        if( helpLine.length() < 5 )
            return( helpLine );
        
        if( helpLine.substring(0,5).equals("[URL ")) {
            String sub = helpLine.substring(5);
            String[] spl = null;
            if( sub.contains(" ]")) {
                spl = sub.split(" ]");
            } else if( sub.contains("]")) {
                spl = sub.split( "]");
            } else {
                logOutput( "Error in URL string formatting for " + helpLine );
                return( "#!RThe help system encountered an error.#!w" );
            }
            return( parseHelpLine( who, fetchWebHelp(who, spl[0]), topic ) );
        }

        if( helpLine.substring(0,6).equals("[HELP ")) {
            String sub = helpLine.substring(6);
            String[] spl = null;

            if( sub.contains(" ]"))
                spl = sub.split(" ]");
            else if( sub.contains("]"))
                spl = sub.split( "]");
            else {
                logOutput( "Error in redirector string formatting for " + helpLine );
                return( "#!RThe help system encountered an error.#!w" );
            }

            if( spl[0].compareTo(topic) == 0 ) {
                logOutput( "Infinite loop detected in " + spl[0] + ".txt! Please fix it." );
                return( "#!RThe help system encountered an error.#!w" );
            } else {
                processHelp( who, getHelpFile( who, spl[0] ), topic );
                return( "" );
            }
        } else {
            return( helpLine );
        }
    }
    private void setupPermissions() {
        Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

        if (HelpCenter.Permissions == null) {
            if (test != null) {
                HelpCenter.Permissions = ((Permissions)test).getHandler();
            } else {
                logOutput("Permission system not detected. '%prigroup%' and '%groups%' will be replaced with 'prigroup' and 'groups'");
            }
        }
    }
}
