package net.weasel.HelpCenter;
import java.io.IOException;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import java.util.Random;

public class HelpCenterListener extends PlayerListener
{
	public static HelpCenter plugin;
	public static Random gen = new Random();
	public static int item;

	public static void sendToPlayer( Player player, String item, String[] multi, String topic ) { HelpCenter.sendToPlayer( player, item, multi, topic ); }

	public HelpCenterListener(HelpCenter instance)
	{
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		
		if( event.getType() == Type.PLAYER_JOIN )
		{
			try 
			{
				String result = HelpCenter.getHelpFile(player, "welcome");
				CharSequence chk = "No help found for ";
				
				if( result.contains(chk) != true )
				{
					sendToPlayer( player, HelpCenter.parseHelpLine( player, result, "welcome" ), null, "" );
				}
			} 
			
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
