package net.weasel.HelpCenter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

public class HelpCenterListener extends PlayerListener {
	public static HelpCenter plugin;

	public HelpCenterListener(HelpCenter instance) {
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerJoinEvent event)	{
		Player player = event.getPlayer();
		if (event.getType() == Type.PLAYER_JOIN) {
		    HelpCenter.ShowWelcomeMessage(player);
		}
	}
}
