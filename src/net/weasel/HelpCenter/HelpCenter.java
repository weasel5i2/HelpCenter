package net.weasel.HelpCenter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.CoalType;
import org.bukkit.DyeColor;
import org.bukkit.TreeSpecies;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Location;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import java.net.*;

public class HelpCenter extends JavaPlugin {
    private final HelpCenterListener playerListener = new HelpCenterListener(this);

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
    private static String TokenLineSplit = "ZQX123!";                       //A unique string used to combine and later split combined lines 
    private static PermissionHandler Permissions;                           //PermissionsHandler object for access to permissions
    private static String PermissionsVersion;                               //Holds the Permissions version number for later use
    private static long rSeed = java.util.GregorianCalendar.MILLISECOND;    //used for random number generation
    private static Random gen = new java.util.Random(rSeed);                //used to help pick a random line number in a help file

    public static String pluginName = "";
    public static String pluginVersion = "";

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
                else if (pCommand.equals("helpitem"))
                    if (args.length >= 1)
                        contents = getHelpDirectItem(args[0].toLowerCase());
                    else
                        contents = getHelpDirectItem("");
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
     * Will try to display the welcome message to the user on login
     * 
     * @param who 
     *             Player who is requesting help information
     */
    public static void ShowWelcomeMessage(Player who) {
        try {
            String result = getHelpFile(who, "welcome");
            CharSequence chk = "No help found for ";
            
            if (result.contains(chk) != true) {
                processHelp(who, result, "");
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
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
     * This will parse the requested plugin for its plugin.yml file and get the commands out of it to build a helpfile structure.<br/>
     * A lot more description needs to go here.
     * 
     * @param who
     *            Player who is requesting help information
     * @param pluginName
     *            name of the plugin to build 
     */
    @SuppressWarnings("unchecked")
    private String buildHelpFile(Player who, String pluginName) {
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
                                String description = (String) helpCommand.get("description");
                                //get the usage
                                String usage = (String) helpCommand.get("usage");
                                //get the aliases
                                ArrayList<String> aliases = (ArrayList<String>)helpCommand.get("aliases");

                                //log that we got something
                                logOutput("Building " + pluginName + ": " + command);
                                sendToPlayer(who, "Building " + pluginName + ": " + command, null, null);
                                
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
                        sendToPlayer(who, "Unable to create '" + pluginName + "' help directory. Process aborted.", null, null);
                    }                
                } else {
                    logOutput("Plugin help already defined. Remove plugin directory and file to rebuild.");
                    sendToPlayer(who, "Plugin help already defined. Remove plugin help directory and file to rebuild.", null, null);
                }                
            } else {
                logOutput("Plugin '" + pluginName + "' does not have defined commands.");
                sendToPlayer(who, "Plugin '" + pluginName + "' does not have defined commands.", null, null);
            }
        } else {
            logOutput("Plugin '" + pluginName + "' does not exist.");
            sendToPlayer(who, "Plugin '" + pluginName + "' does not exist.", null, null);
        }
        return ("");
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
     * Replace key tokens in the string with looked up values.<br/>
     * <br/>
     * This will replace the following tokens with the associated values.<br/> 
     * %user% - Bukkit - player - The players name<br/>
     * %world% - Bukkit - player - The world the player is in<br/>
     * %health% - Bukkit - player - The players health value (0 = dead, 20 = full health)<br/>
     * %locx% - Bukkit - player - The players location X<br/>
     * %locy% - Bukkit - player - The players location Y<br/>
     * %locz% - Bukkit - player - The players location Z<br/>
     * %iteminhandid% - Bukkit - player - The ID of the item the player has in hand<br/>
     * %iteminhand% - Bukkit - player - The item the player has in hand<br/>
     * %isop% - Bukkit - player - Is the player an OP - "YES" or "NO"<br/>
     * %serveronlinecount% - bukkit - Number of players currently on server<br/>
     * %serverver% - bukkit - version of the server<br/>
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
        //declare replaceable variables
        String player = "user";
            String playerWorld = "world";
            String playerHealth = "health";
            String playerArmor = "armor";
            String playerX = "locx";
            String playerY = "locy";
            String playerZ = "locz";
            String playerItemInHandId = "iteminhandid";
            String playerItemInHand = "iteminhand";
            String playerIsOp = "isop";
            String playerGroups = "groups";
            String playerGroup = "prigroup";
        String serverOnlineCount = "serveronlinecount";
        String serverVersion = "serverver";
        
        //get server variables
        serverOnlineCount = "" + Bukkit.getServer().getOnlinePlayers().length;
        serverVersion = "" + Bukkit.getServer().getVersion().replace(" ", "");
            //extract the build number
            if (serverVersion.charAt(serverVersion.indexOf("(MC:")-9) == '-')
                //the build is 3 digits long
                serverVersion = serverVersion.substring(serverVersion.indexOf("(MC:")-7, serverVersion.indexOf("(MC:")-4);
            else
                //the build is 4 digits long (hopefully not more)
                serverVersion = serverVersion.substring(serverVersion.indexOf("(MC:")-8, serverVersion.indexOf("(MC:")-5);
                
        //get player specific variables
        if (who != null) {
            Location location = who.getLocation();
            player = who.getName().replace(" ", "").replace("/", "").replace(".", "");
            playerHealth = Integer.toString(who.getHealth());
            playerArmor = Integer.toString(getArmorPoints(who));

            //who.getItemInHand()
            playerWorld = location.getWorld().getName();
            playerX = "" + Math.floor(location.getX()); 
            playerY = "" + Math.floor(location.getY()); 
            playerZ = "" + Math.floor(location.getZ()); 
            playerItemInHand = who.getItemInHand().getType().name();
            playerItemInHandId = "" + who.getItemInHand().getTypeId();
            playerIsOp = "" + (who.isOp() ? "YES" : "NO");

            //get player specific permission variables
            if (HelpCenter.Permissions != null) {
                try {
                    if (PermissionsVersion.substring(0,1) == "2") {
                        //Handle the Permissions 2.x style permission groups
                        playerGroups = HelpCenter.Permissions.getGroups(location.getWorld().getName(), player)[0].toString();
                        playerGroups.replace(" ", "").replace("/", "").replace(".", "");                        
                    } else {
                        //Handle the Permissions 3.x style permission groups
                        playerGroups = "";
                        Map<String, Set<String>> allGroups = HelpCenter.Permissions.getAllGroups(location.getWorld().getName(), player);
                        //loop through each command available
                        for (String allGroupsKey : allGroups.keySet()) {
                            Set<String> groups = allGroups.get(allGroupsKey);
                            for (String group : groups) {
                                playerGroups += "," + group;
                                //playerGroups += "," + allGroupsKey + "." + group;
                            }
                        }
                        //strip off leading comma
                        if (playerGroups.indexOf(",") != -1) {
                            playerGroups = playerGroups.substring(1);
                            //if there is more than one group, then surround in brackets
                            if (playerGroups.indexOf(",") != -1) {
                                playerGroups = "[" + playerGroups + "]";
                            }
                        }
                    }
                    if (playerGroups.toString().indexOf("[") == -1) {
                        playerGroup = playerGroups;
                        playerGroups = "[" + playerGroups + "]";
                    } else {
                        playerGroup = playerGroups.replace("[", "");
                        playerGroup = playerGroup.substring(0, playerGroup.indexOf(","));
                    }
                } catch (Exception e) {
                    playerGroups = "";
                    playerGroup = "";
                }
            }
        }
        HelpString = HelpString.replace("%user%", player)
                               .replace("%world%", playerWorld)
                               .replace("%health%", playerHealth)
                               .replace("%armor%", playerArmor)
                               .replace("%locx%", playerX)
                               .replace("%locy%", playerY)
                               .replace("%locz%", playerZ)
                               .replace("%iteminhandid%", playerItemInHandId)
                               .replace("%iteminhand%", playerItemInHand)
                               .replace("%isop%", playerIsOp)
                               .replace("%prigroup%", playerGroup)
                               .replace("%groups%", playerGroups)
                               .replace("%serveronlinecount%", serverOnlineCount)
                               .replace("%serverver%", serverVersion)
                               .replace("%helpver%", pluginVersion);

        return HelpString;
    }
    /**
     * Attempt to fetch a help file from the web.
     * 
     * @param who
     *          Player who is requesting help information
     * @param address
     *          URL to attempt to retrieve
     * @return
     *          String containing the pulled text
     * @throws MalformedURLException
     */
    private static String fetchWebHelp(Player who, String address) throws MalformedURLException {
        String player = who.getDisplayName();
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
     * This will return the number of armor points a player has on a scale of 0 - 20
     * 
     * This is calculated on the formula of baseArmorPoints * currentDurability / baseDurability which is on a scale of 0 to 10.
     * Multiply this by 2 to get a scale of 0 to 20.  After the calculation, floor it to get a whole number. 
     * http://www.minecraftwiki.net/wiki/Item_Durability#Armor_durability
     * Thanks to Dynmap plugin from which I copied most of this function.
     * 
     * @param who
     *              Player who we are looking at.
     * @return int value from 0 to 20 indicating no armor to full armor.
     * 
     */
    private static int getArmorPoints(Player who) {
        //This assumes that the inventory array will be in the order of boots, pants, chest, helmet
        double armorPoints[] = {1.5,    //Boots
                                3.0,    //Leggings
                                4.0,    //Chest plate
                                1.5};   //Helmet
        int currentDurability = 0;
        int baseDurability = 0;
        double baseArmorPoints = 0;
        ItemStack inventory[] = who.getInventory().getArmorContents();
        
        for(int i=0;i<inventory.length;i++) {
            final short maxDurability = inventory[i].getType().getMaxDurability();
            if(maxDurability < 0) continue;                         //Since we do not have any durability, there is no armor in this slot and we should just go to the next slot
            baseDurability    += maxDurability;                                 //Get the base durability of the item.
            currentDurability += maxDurability - inventory[i].getDurability();  //Get the current durability of the item. This is calculated by taking the max damage and subtracting the current durability damage.
            baseArmorPoints   += armorPoints[i];                                //Get the base armor points for the slot.
        }
        if (baseDurability == 0) {  //prevent a divide by zero error.
            return 0;
        } else {
            return (int)(2*baseArmorPoints*currentDurability/baseDurability);   //calculate the value and floor it before sending back.
        }
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
    private String getHelpDirect(String pluginName, String pluginCommand) {
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
                        String version = plugin.getDescription().getVersion();
                        retValue += "&e" + pluginName + " " + version + "\n"
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
     * This will return a list of items that are recognized by bukkit.  
     *   This list can be filtered by the string itemName. 
     *   If the itemName is in any part of the item name or ID, it will be shown.
     *    
     * @param itemName
     *            String containing the characters to filter the list  
     * @return String containing the list of items that match. 
     */
    private String getHelpDirectItem(String itemName) {
        String partialRetValue = "";
        String retValue = "";
        boolean itemShowFlag = false;
        Material[] items = Material.values();
        for (Material item : items) {
            itemShowFlag = (Integer.toString(item.getId()).contains(itemName) || item.toString().toLowerCase().contains(itemName) ? true : false);
            partialRetValue = "";
            if (item.getData() == null)
                if (itemShowFlag)
                    partialRetValue = item.getId() + " - " + item.toString() + "\n";
            else {
                switch (item) {
                    case SAPLING:
                    case LOG:
                    case LEAVES:
                        TreeSpecies[] species = TreeSpecies.values();
                        partialRetValue = "";
                        for (TreeSpecies specie : species) 
                            if (itemShowFlag || Integer.toString(specie.getData()).contains(itemName) || specie.toString().toLowerCase().contains(itemName))
                                partialRetValue += item.getId() + ":" + specie.getData() + " - " + item.toString() + " (" + specie.toString() + ")\n";
                        break;
                    case WOOL:
                    case INK_SACK:
                        DyeColor[] colors = DyeColor.values();
                        partialRetValue = "";
                        if (item == Material.WOOL) {
                            for (DyeColor color : colors)
                                if (itemShowFlag || Integer.toString(color.getData()).contains(itemName) || color.toString().toLowerCase().contains(itemName))
                                    partialRetValue += item.getId() + ":" + color.getData() + " - " + item.toString() + " (" + color.toString() + ")\n";
                        } else {
                            String tempRetValue = "";
                            for (DyeColor color : colors) 
                                if (itemShowFlag || Integer.toString(color.getData()).contains(itemName) || color.toString().toLowerCase().contains(itemName))
                                    tempRetValue = item.getId() + ":" + (15 - color.getData()) + " - " + item.toString() + " (" + color.toString() + ")\n" + tempRetValue;
                            partialRetValue += tempRetValue;
                        }
                        break;
                    case DOUBLE_STEP:
                    case STEP:
                        partialRetValue = (itemShowFlag || "0".contains(itemName) || "STONE".contains(itemName)       ? item.getId() + ":0 - " + item.toString() + " (STONE)\n" : "") +
                                          (itemShowFlag || "1".contains(itemName) || "SANDSTONE".contains(itemName)   ? item.getId() + ":1 - " + item.toString() + " (SANDSTONE)\n" : "") +
                                          (itemShowFlag || "2".contains(itemName) || "WOOD".contains(itemName)        ? item.getId() + ":2 - " + item.toString() + " (WOOD)\n" : "") +
                                          (itemShowFlag || "3".contains(itemName) || "COBBLESTONE".contains(itemName) ? item.getId() + ":3 - " + item.toString() + " (COBBLESTONE)\n" : "") ;
                        break;
                    case COAL:
                        CoalType[] coaltypes = CoalType.values();
                        partialRetValue = "";
                        for (CoalType coaltype : coaltypes) 
                            if (itemShowFlag || Integer.toString(coaltype.getData()).contains(itemName) || coaltype.toString().toLowerCase().contains(itemName))
                                partialRetValue += item.getId() + ":" + coaltype.getData() + " - " + item.toString() + " (" + coaltype.toString() + ")\n";
                        break;
                    //Uncomment for future release when LONG_GRASS exists
                    //case LONG_GRASS:
                    //    GrassSpecies[] species = GrassSpecies.values();
                    //    for (GrassSpecies specie : species) 
                    //        if (specie.getData() == 0)
                    //            partialRetValue += item.getId() + ":0 - " + item.toString() + "\n";
                    //        else
                    //            partialRetValue += item.getId() + ":" + specie.getData() + " - " + specie.toString() + "_" + item.toString() + "\n";
                    //    break;
                    default:
                        if (itemShowFlag)
                            partialRetValue = item.getId() + " - " + item.toString() + "\n";
                }
            }
            retValue += partialRetValue;
        }
        return (retValue);
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
    private static String getHelpFile(Player who, String which) throws IOException {
        CharSequence checkStr = "../";
        File hFile = null;
        String retVal = "";
        String player = null;
        if (who != null) player = who.getName();
        which = which.replace("/","").replace("\\","").replace(".", "");

        if (which == "")
            return ("");

        String GroupDirectory = doStringTokenReplacements(who, "%prigroup%/");
        if (!new File(pluginHelpPath + GroupDirectory).exists())
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
     * Called from onEnable<br/>
     * - Hook into the proper events for this plugin<br/>
     * - Make sure we have a proper HelpCenter directory in the Plugins folder<br/>
     * - Initialize the Permissions hook for later use
     * 
     * @return
     */
    private void initHelpCenter() {
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
                HelpCenter.PermissionsVersion = test.getDescription().getVersion();
            } else {
                logOutput("Permission system not detected. '%prigroup%' and '%groups%' will be replaced with 'prigroup' and 'groups'");
            }
        }
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

        String retVal = "";
        String tempVal = "";
        String tempStrippedVal = "";

        if (cColourRemove(messageLine).length() > 60) {
            String[] words = messageLine.split(" ", -1);
            String[] wordsStripped = cColourRemove(messageLine).split(" ", -1);
            for (int X = 0; X < words.length; X++) {
                if ((tempStrippedVal + wordsStripped[X]).length() > 60) {
                    //The added word would push the length to be greater than 60
                    if(tempStrippedVal.trim().length() != 0) {
                        retVal += TokenLineSplit + tempVal.trim();    //save the current line without the new part
                        tempVal = "";
                        tempStrippedVal = "";
                    }
                    //queue up the new part of the line
                    tempVal += words[X] + " ";
                    tempStrippedVal += wordsStripped[X] + " ";
                } else {
                    //add the current part to the previous part of the line
                    tempVal += words[X] + " ";
                    tempStrippedVal += wordsStripped[X] + " "; 
                }
            }
            if (tempStrippedVal.length() > 0) {
              //save the current line
                retVal += TokenLineSplit + tempVal.trim();
            }
        } else
            retVal = messageLine;
        
        if (retVal.length() >= TokenLineSplit.length())
            if (retVal.substring(0,TokenLineSplit.length()).equals(TokenLineSplit))
                retVal = retVal.substring(TokenLineSplit.length());

        return (retVal.split(TokenLineSplit));
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
    private static String parseHelpLine(Player who, String helpLine, String topic) throws IOException {
        //Replace tokens in line if present
        helpLine = doStringTokenReplacements(who, helpLine);
        
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
                            if (wrapData.length == 1 || who == null)
                                sendToPlayer(who, item, null, topic);
                            else
                                sendToPlayer(who, null, wrapData, topic);
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
                        if (wrapData.length == 1 || who == null)
                            sendToPlayer(who, item, null, topic);
                        else
                            sendToPlayer(who, null, wrapData, topic);
                    }
                }
            } else {
                wrapData = lineWrap(helpData);
                if (wrapData.length == 1 || who == null)
                    sendToPlayer(who, helpData, null, topic);
                else
                    sendToPlayer(who, null, wrapData, topic);
            }
        }
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
    private static void sendToPlayer(Player player, String item, String[] multi, String topic) {
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
     * Convert a String to a String[].
     * 
     * @param line
     *            String that needs to be split into the array
     * @return String[] array containing the single string split on the TokenLineSplit indicator.
     */
    private static String[] stringToArray(String line) {
        return (line.concat(TokenLineSplit).split(TokenLineSplit));
    }
}
