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

        // Set block event (legacy)
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

        // Add core location (NEW)
        else if (lore.contains(ColorUtil.add("&eright click on a block to add core location"))) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                RaidEvent.getInstance().getItemManager().addCoreLocation(event.getClickedBlock().getLocation());
                event.getPlayer().sendMessage(ColorUtil.add("&6Core location added! Total: &e" +
                        RaidEvent.getInstance().getCoreLocationManager().getCoreLocationCount()));
            }
        }

        // View core locations (NEW)
        else if (lore.contains(ColorUtil.add("&eright click to show core cubes"))) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (RaidEvent.getInstance().getCoreLocationManager().hasCoreLocations()) {
                    RaidEvent.getInstance().getCoreLocationManager().startParticleEffects();
                    event.getPlayer().sendMessage(ColorUtil.add("&eCore cubes enabled! Locations: &6" +
                            RaidEvent.getInstance().getCoreLocationManager().getCoreLocationCount()));
                } else {
                    event.getPlayer().sendMessage(ColorUtil.add("&cNo core locations configured!"));
                }
            }
        }

        // Set event region POS1
        else if (lore.contains(ColorUtil.add("&eright click on a block to set position 1")) &&
                item.getItemMeta().getDisplayName().contains("EVENT REGION POS1")) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                RaidEvent.getInstance().getItemManager().setRegionPos1(event.getClickedBlock().getLocation());
                event.getPlayer().sendMessage(ColorUtil.add("&6Event region POS1 set!"));
            }
        }

        // Set event region POS2
        else if (lore.contains(ColorUtil.add("&eright click on a block to set position 2")) &&
                item.getItemMeta().getDisplayName().contains("EVENT REGION POS2")) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                RaidEvent.getInstance().getItemManager().setRegionPos2(event.getClickedBlock().getLocation());
                event.getPlayer().sendMessage(ColorUtil.add("&6Event region POS2 set!"));

                if (RaidEvent.getInstance().getRegionManager().isRegionConfigured()) {
                    event.getPlayer().sendMessage(ColorUtil.add("&a✅ Event region fully configured!"));
                    event.getPlayer().sendMessage(ColorUtil.add("&eDeaths in this zone during events = -$100"));
                }
            }
        }

        // View event region
        else if (lore.contains(ColorUtil.add("&eright click to show event zone borders"))) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (RaidEvent.getInstance().getRegionManager().isRegionConfigured()) {
                    RaidEvent.getInstance().getRegionManager().startParticleEffects();
                    event.getPlayer().sendMessage(ColorUtil.add("&6Event zone borders enabled!"));
                    event.getPlayer().sendMessage(ColorUtil.add("&eDeaths inside = -$100 during raids"));
                } else {
                    event.getPlayer().sendMessage(ColorUtil.add("&cNo event region configured!"));
                }
            }
        }
    }
}