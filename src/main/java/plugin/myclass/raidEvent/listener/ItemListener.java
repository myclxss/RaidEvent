package plugin.myclass.raidEvent.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import plugin.myclass.raidEvent.RaidEvent;
import plugin.myclass.raidEvent.utils.ColorUtil;

import java.util.List;

public class ItemListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return;
        }

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) {
            return;
        }

        // Set block event
        if (lore.contains(ColorUtil.add("&eright click on the block to set"))) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                RaidEvent.getInstance().getItemManager().setBlockEvent(event.getClickedBlock().getLocation());
                event.getPlayer().sendMessage(ColorUtil.add("&aBlock event has been set!"));
                event.getPlayer().getInventory().remove(item);
            }
        }

        // Add spawn location
        else if (lore.contains(ColorUtil.add("&eright click on a block to add spawn location"))) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                RaidEvent.getInstance().getItemManager().addSpawnLocation(event.getClickedBlock().getLocation());
                event.getPlayer().sendMessage(ColorUtil.add("&aSpawn location added! Total: &e" +
                        RaidEvent.getInstance().getSpawnLocationManager().getSpawnLocationCount()));
            }
        }

        // View spawn locations
        else if (lore.contains(ColorUtil.add("&eright click to show soul circles"))) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (RaidEvent.getInstance().getSpawnLocationManager().hasSpawnLocations()) {
                    RaidEvent.getInstance().getSpawnLocationManager().startParticleEffects();
                    event.getPlayer().sendMessage(ColorUtil.add("&aSoul circles enabled! Locations: &e" +
                            RaidEvent.getInstance().getSpawnLocationManager().getSpawnLocationCount()));
                } else {
                    event.getPlayer().sendMessage(ColorUtil.add("&cNo spawn locations configured!"));
                }
            }
        }
    }
}