package plugin.myclass.raidEvent.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import plugin.myclass.raidEvent.RaidEvent;
import plugin.myclass.raidEvent.utils.ItemBuilder;

public class ItemManager {

    public ItemStack getBlockSetup() {
        return new ItemBuilder(Material.STICK)
                .setName("&c&lSET BLOCK EVENT")
                .setLore("&eright click on the block to set")
                .build();
    }

    public ItemStack getSpawnLocationAdder() {
        return new ItemBuilder(Material.BLAZE_ROD)
                .setName("&a&lADD SPAWN LOCATION")
                .setLore("&eright click on a block to add spawn location")
                .build();
    }

    public ItemStack getSpawnLocationViewer() {
        return new ItemBuilder(Material.SOUL_LANTERN)
                .setName("&b&lVIEW SPAWN LOCATIONS")
                .setLore("&eright click to show soul circles")
                .build();
    }

    public void setBlockEvent(Location location) {
        RaidEvent.getInstance().getSettings().set("BLOCK.LOCATION.X", location.getBlockX());
        RaidEvent.getInstance().getSettings().set("BLOCK.LOCATION.Y", location.getBlockY());
        RaidEvent.getInstance().getSettings().set("BLOCK.LOCATION.Z", location.getBlockZ());
        RaidEvent.getInstance().getSettings().set("BLOCK.LOCATION.WORLD", location.getWorld().getName());
        RaidEvent.getInstance().getSettings().set("BLOCK.LOCATION.YAW", location.getYaw());
        RaidEvent.getInstance().getSettings().set("BLOCK.LOCATION.PITCH", location.getPitch());
        RaidEvent.getInstance().getSettings().set("BLOCK.TYPE", location.getBlock().getType().toString());
        RaidEvent.getInstance().getSettings().save();
        RaidEvent.getInstance().getSettings().reload();
    }

    public void addSpawnLocation(Location location) {
        RaidEvent.getInstance().getSpawnLocationManager().addSpawnLocation(location);
    }

    public boolean removeSpawnLocation(Location location) {
        return RaidEvent.getInstance().getSpawnLocationManager().removeNearestSpawnLocation(location);
    }
}