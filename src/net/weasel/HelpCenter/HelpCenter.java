package net.weasel.HelpCenter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
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
import org.bukkit.Location;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import java.net.*;

public class HelpCenter extends JavaPlugin {
    private static String pluginHelpPathNoSlash = "plugins/HelpCenter";
    private static String pluginHelpPath = pluginHelpPathNoSlash + "/";
    private static String[] Colours = { "&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", 
                                        "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f",
                                        "#!k", "#!b", "#!g", "#!c", "#!r", "#!m", "#!y", "#!w",
                                        "#!K", "#!B", "#!G", "#!C", "#!R", "#!M", "#!Y", "#!W"                                        
                                      };
    private static ChatColor[] cCode = {ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY, 
                                        ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE,
                                        ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY,
                                        ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE
                                       };
    
    private final HelpCenterListener playerListener = new HelpCenterListener(this);
    private static String TokenLineSplit = "ZQX123!";
    private static PermissionHandler Permissions;

    public static long rSeed = java.util.GregorianCalendar.MILLISECOND;
    public static boolean hasWelcomeFile = false;
    public static Random gen = new java.util.Random(rSeed);
    public static int item;
    public static String pluginName = "";
    public static String pluginVersion = "";
    public static final String DATE_FMT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Called when this plugin is enabled
     */
    @Override
    public void onEnable() {
        // Setup the different hooks into the plugin manager.
        initHelpCenter();

        // display the enabled line in the log file.
        pluginName = this.getDescription().getName();
        pluginVersion = this.getDescription().getVersion();
        logOutput(pluginName + " v" + pluginVersion + " enabled.");
    }
    /**
     * Called from onEnable<br/>
     * - Hook into the proper events for this plugin<br/>
     * - Make sure we have a proper HelpCenter directory in the Plugins folder<br/>
     * - Initialize the Permissions hook for later use
     * 
     * @return
     */
    public void initHelpCenter() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Priority.Normal, this);
        Plugin plugin = pm.getPlugin("HelpCenter");

        // Make sure we have our HelpCenter directory available
        File path = new File(pluginHelpPathNoSlash);
        if (!path.exists()) {
            // See if we have an old directory structure
            File oldPath = new File("HelpCenter");
            if (oldPath.exists()) {
                // HelpCenter directory exists outside of the plugin directory.
                // We need to fix that by moving it.
                oldPath.renameTo(path);
                logOutput("HelpCenter folder moved to '" + pluginHelpPathNoSlash + "'.");
            } else {
                // HelpCenter directory does not exist anywhere. Create it.
                logOutput("Attempting to create new folder '" + pluginHelpPathNoSlash + "'.");
                if (path.mkdir()) {
                    logOutput("Created successfully.");
                } else {
                    logOutput("Unable to create directory! Do you have write permission?");
                    pm.disablePlugin(plugin);
                }
            }
        }
        // Initialize the Permissions hook for later use
        Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
        if (HelpCenter.Permissions == null) {
            if (test != null) {
                HelpCenter.Permissions = ((Permissions) test).getHandler();
            } else {
                logOutput("Permission system not detected. '%prigroup%' and '%groups%' will be replaced with 'prigroup' and 'groups'");
            }
        }
    }
    /**
     * Called when this plugin is disabled
     */
    @Override
    public void onDisable() {
        logOutput("HelpCenter disabled.");
    }
    /**
     * handles help, ?, and helpp commands
     * 
     * @return true if this handled the command, false otherwise.
     */
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String pCommand = command.getName().toLowerCase();
        boolean retVal = false;
        String contents = "";
        try {
            if (sender instanceof Player) {
                //from within game
                if (pCommand.equals("helpp"))
                    if (args.length >= 1)
                        if (args.length >= 2)
                            contents = getHelpDirect(args[0], args[1]);
                        else
                            contents = getHelpDirect(args[0], null);
                    else
                        contents = getHelpDirect(null, null);
                else if (args.length > 0)
                    if ((args[0].toLowerCase().equals("buildhelpfiles")))
                        contents = buildHelpFiles((Player) sender);
                    else if ((args[0].toLowerCase().equals("buildplugin")))
                        contents = buildHelpFile((Player) sender, args[1]);
                    else
                        contents = getHelpFile((Player) sender, arrayToString(args, " ").replace("/", "").replace(".", ""));
                else
                    contents = getHelpFile((Player) sender, "HCdefault");
                processHelp((Player) sender, contents, arrayToString(args, " "));
            } else {
                //from console
                if (pCommand.equals("helpp"))
                    if (args.length >= 1)
                        if (args.length >= 2)
                            contents = getHelpDirect(args[0], args[1]);
                        else
                            contents = getHelpDirect(args[0], null);
                    else
                        contents = getHelpDirect(null, null);
                else if (args.length > 0)
                    if ((args[0].toLowerCase().equals("buildhelpfiles")))
                        contents = buildHelpFiles(null);
                    else if ((args[0].toLowerCase().equals("buildplugin")))
                        contents = buildHelpFile(null, args[1]);
                    else
                        contents = getHelpFile(null, arrayToString(args, " ").replace("/", "").replace(".", ""));
                else
                    contents = getHelpFile(null, "HCdefault");
                processHelp(null, contents, arrayToString(args, " "));
            }
        } catch (IOException e) {
            contents = "&cUnable to load helpfile.&f";
            e.printStackTrace();
        }
        retVal = true;
        return retVal;
    }
    /**
     * Sends a message to the indicated player
     * 
     * @param player
     *            Player who will receive the message
     * @param item
     *            String a single line statement to send to the player
     * @param multi
     *            String[] an array of message lines to send to the player
     * @param topic
     *            String used to help prevent infinite loops. On the initial call this should be null
     */
    public static void sendToPlayer(Player player, String item, String[] multi, String topic) {
        // convert a single statement into a multi-line statment with only one
        // line so we don't duplicate code below.
        if (multi == null)
            multi = stringToArray(item);
        // Output the help lines
        for (int X = 0; X < multi.length; X++) {
            try {
                if (player != null) {
                    player.sendMessage(cColourFinalize(parseHelpLine(player, multi[X], topic)));
                } else {
                    logOutput(parseHelpLine(player, multi[X], topic));
                }
            } catch (IOException e) {
                // Do nothing..
            }
        }
    }
    /**
     * This will attempt to get the help file from the plugins/HelpCenter directory.<br/>
     * <br/>
     * Help file search pattern. If the step works, the subsequent steps are not taken.<br/>
     * <br/>
     * Prep) See if there is a directory named the same as Permissions %prigroup% Token. If it exists, it will look
     * there first for help files.<br/>
     * 1) Append a ".txt" to the end of the command<br/>
     * 2) search for file directly<br/>
     * 3) replace " " with a "/" and append a ".txt" to the end of the command<br/>
     * 4) replace " " with a "/"<br/>
     * 
     * @param who
     *            Player who requested this help file.
     * @param which
     *            String the command to try to get a file for
     * @return String containing the contents of the help file
     * @throws IOException
     *             This is thrown if a file related issue arises.
     */
    public static String getHelpFile(Player who, String which) throws IOException {
        CharSequence checkStr = "../";
        File hFile = null;
        String retVal = "";
        String player = null;
        if (who != null) player = who.getName();
        which = which.replace("/","").replace("\\","").replace(".", "");

        if (which == "")
            return ("");

        String GroupDirectory = doStringTokenReplacements(who, "%prigroup%/");
        if (!new File(doStringTokenReplacements(who, pluginHelpPath + GroupDirectory)).exists())
            GroupDirectory = "";

        hFile = new File(pluginHelpPath + GroupDirectory + which.toLowerCase() + ".txt");
        if (!hFile.exists() || hFile.isDirectory()) {
            hFile = new File(pluginHelpPath + GroupDirectory + which.toLowerCase());
            if (!hFile.exists() || hFile.isDirectory()) {
                hFile = new File(pluginHelpPath + GroupDirectory + which.replace(" ", "/").toLowerCase() + ".txt");
                if (!hFile.exists() || hFile.isDirectory()) {
                    hFile = new File(pluginHelpPath + GroupDirectory + which.replace(" ", "/").toLowerCase());
                    if (!hFile.exists() && !GroupDirectory.isEmpty()) {
                        // Try the whole thing again but without the GroupDirectory
                        hFile = new File(pluginHelpPath + which.toLowerCase() + ".txt");
                        if (!hFile.exists() || hFile.isDirectory()) {
                            hFile = new File(pluginHelpPath + which.toLowerCase());
                            if (!hFile.exists() || hFile.isDirectory()) {
                                hFile = new File(pluginHelpPath + which.replace(" ", "/").toLowerCase() + ".txt");
                                if (!hFile.exists() || hFile.isDirectory()) {
                                    hFile = new File(pluginHelpPath + which.replace(" ", "/").toLowerCase());
                                }
                            }
                        }
                    }
                }
            }
        }

        if (which.contains(checkStr)) {
            logOutput(player + " tried to cheat the HelpCenter! : " + which.toString());
            retVal = "&cNo help found for &e" + which + "&c.&f";
        } else if (!hFile.exists() || hFile.isDirectory()) {
            logOutput(player + " tried to view a nonexistent help file: " + which);
            retVal = "&cNo help found for &e" + which + "&c.&f";
        } else {
            byte[] buf = new byte[(int) hFile.length()];
            BufferedInputStream fr = null;
            try {
                fr = new BufferedInputStream(new FileInputStream(hFile.toString()));
                fr.read(buf);
            } finally {
                if (fr != null)
                    try {
                        fr.close();
                    } catch (IOException ignored) {
                    }
            }
            retVal = new String(buf);
        }
        return retVal;
    }
    /**
     * This will attempt to get the help file from the installed plugins.<br/>
     * <br/>
     * Help file search pattern. If the step works, the subsequent steps are not taken.<br/>
     * <br/>
     * 1) See if plugin is available.<br/>
     * 2) If pluginCommand is specified, look for the specific command.<br/>
     * 3) If pluginCommand is not specified, it will return a list of plugin commands.<br/>
     * 
     * @param who
     *            Player who requested this help file.
     * @param pluginName
     *            String the plugin query
     * @param pluginCommand
     *            String the plugin command to query
     * @return String containing the commands of the plugin or a specific plugin command
     */
    @SuppressWarnings("unchecked")
    public String getHelpDirect(String pluginName, String pluginCommand) {
        PluginManager pluginManager = getServer().getPluginManager();
        Plugin[] plugins = pluginManager.getPlugins();
        String retValue = "";
        if (pluginName != null) {
            Plugin plugin = null;
            for (Plugin tempPlugin : plugins) {
                if (tempPlugin.getDescription().getName().toLowerCase().equals(pluginName.toLowerCase())) { 
                    plugin = tempPlugin;
                    pluginName = plugin.getDescription().getName();
                }
            }
            if (plugin != null) {
                Map<String, Object> commands = (Map<String, Object>)plugin.getDescription().getCommands();
                if(commands != null) {
                    if (pluginCommand == null) {
                        String description = plugin.getDescription().getDescription();
                        retValue += "&e" + pluginName + "\n"
                        + "&e--------------------\n";
                        if (description != null) retValue += "&eDescription: &f" + description + "\n";                          // Add the description
                        retValue += "&eAvailable commands:\n"
                                  + "&e---------------------\n"
                                  + "&e(type '&f/helpp " + pluginName + " <command>&e' for more info)\n"
                                  + "&f";
                        //loop through each command available
                        for (String command : commands.keySet()) {
                            retValue += command + " ";
                        }
                    } else {
                        Map<String, Object> helpCommand = (Map<String, Object>) commands.get(pluginCommand);    //get the commands tree
                        if (helpCommand != null) {
                            String description = (String) helpCommand.get("description");                           //get the description
                            String usage = (String) helpCommand.get("usage");                                       //get the usage
                            ArrayList<String> aliases = (ArrayList<String>)helpCommand.get("aliases");              //get the aliases
        
                            retValue += "&e" + pluginCommand + "\n"
                                      + "&e--------------------\n";
                            
                            if (description != null) retValue += "&eDescription: &f" + description + "\n";                          // Add the description
                            if (usage != null)       retValue += "&eUsage: &f" + usage.replace("<command>", pluginCommand) + "\n";  // Add the usage
                            if (aliases != null) {                                                                                  // Add the aliases
                                retValue += "&eAliases: &f";
                                for(String alias: aliases) {
                                    retValue += alias.replace("<command>", pluginCommand) + " ";
                                }
                            }
                        } else {
                            retValue = "Command '&c" + pluginCommand + "&f' for '" + pluginName + "' plugin, not found.";
                        }
                    }
                }
            } else {
                retValue = "Plugin '" + pluginName + "' is not available.";
            }
        } else {
            //create the header
            retValue = "&eAvailable Plugins:\n"
                     + "&e---------------------\n"
                     + "&e(type '&f/helpp <plugin>&e' for more info)\n"
                     + "&e";
            // Loop through each plugin we can find.
            for (Plugin plugin : plugins) {
                pluginName = plugin.getDescription().getName();
                retValue += pluginName + " ";
            }
        }
        return (retValue);
    }
    /**
     * This will go through the help Text string and try to handle redirections. <br/>
     * Handled redirections are in the form of [URL address] and [help alternateFile]
     * 
     * @param who
     *            Player that will be receiving the help information
     * @param helpLine
     *            String of text that will be parsed for key words
     * @param topic
     *            String matching the helpfile name as to prevent recursion loops
     * @return String of the redirected to help file
     * @throws IOException
     */
    public static String parseHelpLine(Player who, String helpLine, String topic) throws IOException {
        if (helpLine.length() < 6)
            return (helpLine);

        if (helpLine.substring(0, 5).equals("[URL ")) {
            String sub = helpLine.substring(5);
            String[] spl = null;
            if (sub.contains(" ]")) {
                spl = sub.split(" ]");
            } else if (sub.contains("]")) {
                spl = sub.split("]");
            } else {
                logOutput("Error in URL string formatting for " + helpLine);
                return ("&cThe help system encountered an error.&f");
            }
            return (parseHelpLine(who, fetchWebHelp(who, spl[0]), topic));
        }

        if (helpLine.substring(0, 6).equals("[HELP ")) {
            String sub = helpLine.substring(6);
            String[] spl = null;

            if (sub.contains(" ]"))
                spl = sub.split(" ]");
            else if (sub.contains("]"))
                spl = sub.split("]");
            else {
                logOutput("Error in redirector string formatting for " + helpLine);
                return ("&cThe help system encountered an error.&f");
            }

            if (spl[0].compareTo(topic) == 0) {
                logOutput("Infinite loop detected in " + spl[0] + ".txt! Please fix it.");
                return ("&cThe help system encountered an error.&f");
            } else {
                processHelp(who, getHelpFile(who, spl[0]), topic);
                return ("");
            }
        } else {
            return (helpLine);
        }
    }
    

    /**
     * Convert the color keywords of chat colors to the chat codes used by the chat window.
     * 
     * @param message
     *            String to be colorized
     * 
     * @return String the colorized message
     */
    private static String cColourFinalize(String message) {
        CharSequence cChk = null;
        String temp = null;

        for (int x = 0; x < Colours.length; x++) {
            cChk = Colours[x];
            if (message.contains(cChk)) {
                temp = message.replace(cChk, cCode[x].toString());
                message = temp;
            }
        }
        return message;
    }
    /**
     * Strip the color keywords of chat colors from the message.
     * 
     * @param message
     *            String to be stripped of color keywords
     * 
     * @return String the message stripped of color keywords
     */
    private static String cColourRemove(String message) {
        // make sure we have a copy of the string so we do not modify the string itself
        String returnValue = message.toString();

        // remove the chat colors
        for (int x = 0; x < Colours.length; x++) {
            returnValue = returnValue.replace(Colours[x], "");
        }
        return returnValue;
    }
    /**
     * Output the message to the console with the plugin name prepended to it.
     * 
     * @param output
     *            String of the message to be output.
     */
    private static void logOutput(String output) {
        System.out.println("[HelpCenter] " + output);
    }
    /**
     * Break a message line into multiple lines if the line exceeds 60 characters.
     * 
     * @param messageLine
     *            String message to be split if necessary
     * @return String[] array of one or more lines of text.
     */
    private static String[] lineWrap(String messageLine) {
        if (messageLine.startsWith("["))
            return (stringToArray(messageLine));

        String[] retVal = null;
        String retTemp = "";
        String tempVal = "";

        if (cColourRemove(messageLine).length() > 60) {
            String[] words = messageLine.split(" ");
            for (int X = 0; X < words.length; X++) {
                tempVal += words[X] + " ";
                if (cColourRemove(tempVal).length() > 60) {
                    String[] foo = tempVal.trim().split(" ");
                    foo[foo.length - 1] = "";
                    retTemp += arrayToString(foo, " ").trim() + TokenLineSplit;
                    tempVal = "";
                    X--;
                }
            }
            retTemp += tempVal;
            retVal = retTemp.split(TokenLineSplit);
        } else
            retVal = stringToArray(messageLine);
        return retVal;
    }
    /**
     * Convert a String to a String[].
     * 
     * @param line
     *            String that needs to be split into the array
     * @return String[] array containing the single string split on the TokenLineSplit indicator.
     */
    private static String[] stringToArray(String line) {
        return (line.concat(TokenLineSplit).split(TokenLineSplit));
    }
    /**
     * Convert a string[] array to a single string
     * 
     * @param stringArray
     *            String[] array containing one or more strings that need to be combined.
     * @param separator
     *            String containing what needs to be placed between each of the combined strings.
     * @return String containing the combined strings.
     */
    private static String arrayToString(String[] stringArray, String separator) {
        String result = "";

        if (stringArray.length > 0) {
            result = stringArray[0]; // start with the first element
            for (int i = 1; i < stringArray.length; i++) {
                result = result + separator + stringArray[i];
            }
        }
        return result;
    }
    /**
     * Replace key tokens in the string with looked up values.<br/>
     * <br/>
     * This will replace the following tokens with the associated values.<br/> 
     * %user% - Bukkit - The players name<br/>
     * %groups% - Plugin - Permissions - The list of groups to which the player belongs.<br/>
     * %prigroup% - Plugin - Permissions - The first group returned by Permissions to which the player belongs.<br/>
     * %helpver% - Plugin - HelpCenter - Version of the plugin<br/>
     * <br/>
     * If Permissions does not exist, the associated Tokens will be replaced with the same token but have the % stripped off.<br/>
     * %groups% -> groups<br/>
     * %prigroup% -> prigroup
     * 
     * @param who
     *            Player that we are getting information about.
     * @param HelpString
     *            String containing the Tokens to be replaced
     * @return String with the tokens replaced.
     */
    private static String doStringTokenReplacements(Player who, String HelpString) {
        String player = "user";
        String groups = "groups";
        String group = "prigroup";
        if (who != null) {
            player = who.getName().replace(" ", "").replace("/", "").replace(".", "");
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
        }
        HelpString = HelpString.replace("%user%", player).replace("%prigroup%", group).replace("%groups%", groups).replace("%helpver%", pluginVersion);

        return HelpString;
    }
    /**
     * Attempt to fetch a help file from the web.
     * 
     * @param who
     *            Player who is requesting help information
     * @param address
     * @return
     * @throws MalformedURLException
     */
    private static String fetchWebHelp(Player who, String address) throws MalformedURLException {
        String player = who.getDisplayName();
        address = doStringTokenReplacements(who, address);
        logOutput("Fetching help URL for " + player + ": " + address);
        String result = null;
        URLConnection fConn = null;

        try {
            fConn = new URL(address).openConnection();
            Scanner fSc = new Scanner(fConn.getInputStream());
            fSc.useDelimiter("\\Z");
            result = fSc.next();
        } catch (Exception e) {
            logOutput("Unable to fetch web help URL: " + address);
            return ("Unable to fetch web help.");
        }
        return (result);
    }
    /**
     * This will parse all installed plugins for their plugin.yml file and get the commands out of it to build a helpfile structure.<br/>
     * A lot more description needs to go here.
     * 
     * @param who
     *            Player who is requesting help information
     */
    private String buildHelpFiles(Player who) {
        PluginManager pluginManager = getServer().getPluginManager();
        Plugin[] plugins = pluginManager.getPlugins();
        String pluginName;
        String retVal = "";
        try {
            // Create file
            File path = new File(pluginHelpPath + "/HCdefault.txt");
            if (path.exists() == false) {
                logOutput("Building 'HCdefault' plugin help.");
                sendToPlayer(who, "Building 'HCdefault' plugin help.", null, null);

                FileWriter fstream = new FileWriter(path);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write("&eAvailable Plugins:\n"
                        + "&e---------------------\n"
                        + "&e(type '&f/help <plugin>&e' for more info)\n"
                        );
                // Loop through each plugin we can find.
                for (Plugin plugin : plugins) {
                    pluginName = plugin.getDescription().getName();
                    logOutput("Building '" + pluginName + "' plugin help.");
                    sendToPlayer(who, "Building '" + pluginName + "' plugin help.", null, null);
                    out.write(" " + pluginName.replace(" ", "").replace("/", "").replace(".", "")); //strip out spaces, dots, and slashes
                    buildHelpFile(who, pluginName);
                }

                // Close the output stream
                out.close();
            } else {
                sendToPlayer(who, "HCdefault already exists. Please remove before building plugin help files..", null, null);
            }
            retVal = "buildHelpFiles complete.";
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return (retVal);
    }
    /**
     * This will parse the requested plugin for its plugin.yml file and get the commands out of it to build a helpfile structure.<br/>
     * A lot more description needs to go here.
     * 
     * @param player
     *            Player who is requesting help information
     * @param pluginName
     *            name of the plugin to build 
     */
    @SuppressWarnings("unchecked")
    private String buildHelpFile(Player player, String pluginName) {
        Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);

        if (plugin != null) {
            Map<String, Object> commands = (Map<String, Object>)plugin.getDescription().getCommands();
            if(commands != null) {
                File path = new File(pluginHelpPath + pluginName.toLowerCase());
                File pathTxt = new File(pluginHelpPath + pluginName.toLowerCase() + ".txt");
                if (path.exists() == false && pathTxt.exists() == false) {
                    if (path.mkdir() == true) {
                        try {
                            // Create the plugin index file
                            FileWriter fstreamPlugin = new FileWriter(path + ".txt");
                            BufferedWriter outPlugin = new BufferedWriter(fstreamPlugin);
                            outPlugin.write("&eAvailable commands:\n"
                                          + "&e---------------------\n"
                                          + "&e(type '&f/help " + pluginName + " <command>&e' for more info)\n"
                                          );
                            //loop through each command available
                            for (String command : commands.keySet()) {
                                //get the command
                                Map<String, Object> helpCommand = (Map<String, Object>) commands.get(command);
                                //get the description
logOutput("getting Description");
                                String description = (String) helpCommand.get("description");
                                //get the usage
logOutput("getting Usage");
                                String usage = (String) helpCommand.get("usage");
                                //get the aliases
logOutput("getting Aliases");
                                ArrayList<String> aliases = (ArrayList<String>)helpCommand.get("aliases");
logOutput("after Aliases");

                                //log that we got something
                                logOutput("Building " + pluginName + ": " + command);
                                sendToPlayer(player, "Building " + pluginName + ": " + command, null, null);
                                
                                //make sure we have a valid file name
                                String commandFile = command.replace("/", "").replace(".", "");
                                //if we stripped the entire command, then just have a file with the .txt extension so we can find it later
                                if (commandFile.equals("")) commandFile = ".txt";
                                //write the command into the index 
                                outPlugin.write(" /" + commandFile + " ");
                                
                                try {
                                    // Create file for command
                                    FileWriter fstream = new FileWriter(path + "/" + commandFile);
                                    BufferedWriter out = new BufferedWriter(fstream);
                                    // Add the command header
                                    out.write("&e" + command + "\n"
                                            + "&e--------------------\n");

                                    // Add the description
                                    if (description != null) out.write("&eDescription: &f" + description + "\n");
                                    // Add the usage
                                    if (usage != null)       out.write("&eUsage: &f" + usage.replace("<command>", command) + "\n");
                                    // Add the aliases
                                    if (aliases != null)
                                        for(String alias: aliases) {
                                            out.write("&eAliases: &f" + alias.replace("<command>", command));
                                        }
                                    // Close the output stream
                                    out.close();
                                    fstream.close();
                                } catch (Exception e) {// Catch exception if any
                                    System.err.println("Error: " + e.getMessage());
                                }
                            }
                            outPlugin.close();
                            fstreamPlugin.close();
                        } catch (Exception e) {// Catch exception if any
                            System.err.println("Error: " + e.getMessage());
                        }
                    } else {
                        logOutput("Unable to create '" + pluginName + "' help directory. Process aborted.");
                        sendToPlayer(player, "Unable to create '" + pluginName + "' help directory. Process aborted.", null, null);
                    }                
                } else {
                    logOutput("Plugin help already defined. Remove plugin directory and file to rebuild.");
                    sendToPlayer(player, "Plugin help already defined. Remove plugin help directory and file to rebuild.", null, null);
                }                
            } else {
                logOutput("Plugin '" + pluginName + "' does not have defined commands.");
                sendToPlayer(player, "Plugin '" + pluginName + "' does not have defined commands.", null, null);
            }
        } else {
            logOutput("Plugin '" + pluginName + "' does not exist.");
            sendToPlayer(player, "Plugin '" + pluginName + "' does not exist.", null, null);
        }
        return ("");
    }
    /**
     * Goes through each line of the help file and outputs it to the player.<br/>
     * It will break lines as needed
     * 
     * @param who
     *            Player who is requesting help information
     * @param helpData
     *            The string of data to be sent to the player
     * @param topic
     *            A string to prevent recursion loops
     * @throws IOException
     */
    private static void processHelp(Player who, String helpData, String topic) {
        CharSequence chkStr = "";
        String[] wrapData = null;
        helpData.replace("\r", "");
        chkStr = "#!HELP";
        if (helpData.contains(chkStr)) {
            String[] helps = helpData.split((String) chkStr, 0);
            int X = 0;
            while (X < 1) {
                X = gen.nextInt(helps.length);
            }
            String helpItem = helps[X];
            if (helpItem.length() > 2) {
                chkStr = "\n";
                if (helpItem.contains(chkStr)) {
                    String subHelps[] = helpItem.split("\n");
                    String item = "";
                    for (X = 0; X < subHelps.length; X++) {
                        item = subHelps[X];
                        if (item != "") {
                            wrapData = lineWrap(item);
                            if (wrapData.length > 1)
                                sendToPlayer(who, null, wrapData, topic);
                            else
                                sendToPlayer(who, item, null, topic);
                        }
                    }
                }
            }
        } else {
            chkStr = "\n";
            if (helpData.contains(chkStr)) {
                String[] helps = helpData.split("\n");
                String item = "";
                for (int X = 0; X < helps.length; X++) {
                    item = helps[X];
                    if (item != "") {
                        wrapData = lineWrap(item);
                        if (wrapData.length == 1) {
                            sendToPlayer(who, item, null, topic);
                        } else {
                            sendToPlayer(who, null, wrapData, topic);
                        }
                    }
                }
            } else {
                wrapData = lineWrap(helpData);
                if (wrapData.length == 1) {
                    sendToPlayer(who, helpData, null, topic);
                } else {
                    sendToPlayer(who, null, wrapData, topic);
                }
            }
        }
    }

    /******************************************
     * Possibly unused functions
     ******************************************/
    public static class Dir implements FileFilter {
        private final String[] fileEx = new String[] { "txt" };

        static File[] getFiles(File path) {
            File files[];
            FileFilter filter = null;
            files = path.listFiles(filter);
            Arrays.sort(files);
            return (files);
        }

        @Override
        public boolean accept(File file) {
            for (String extension : fileEx) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }
}
