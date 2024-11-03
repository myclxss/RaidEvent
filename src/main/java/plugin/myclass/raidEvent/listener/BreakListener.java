package plugin.myclass.raidEvent.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import plugin.myclass.raidEvent.RaidEvent;

public class BreakListener implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (RaidEvent.getInstance().getEventManager().isRaidBlock(event.getBlock())) {
            event.setCancelled(true);
            RaidEvent.getInstance().getEventManager().damageBlock(player);
        }
    }
}