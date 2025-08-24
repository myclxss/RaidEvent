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

    // Nuevas herramientas para núcleos
    public ItemStack getCoreLocationAdder() {
        return new ItemBuilder(Material.BREEZE_ROD)
                .setName("&6&lADD CORE LOCATION")
                .setLore("&eright click on a block to add core location")
                .build();
    }

    public ItemStack getCoreLocationViewer() {
        return new ItemBuilder(Material.BEACON)
                .setName("&e&lVIEW CORE LOCATIONS")
                .setLore("&eright click to show core cubes")
                .build();
    }

    // Herramientas para región del evento
    public ItemStack getRegionPos1Tool() {
        return new ItemBuilder(Material.GOLDEN_AXE)
                .setName("&e&lSET EVENT REGION POS1")
                .setLore("&eright click on a block to set position 1")
                .build();
    }

    public ItemStack getRegionPos2Tool() {
        return new ItemBuilder(Material.GOLDEN_AXE)
                .setName("&e&lSET EVENT REGION POS2")
                .setLore("&eright click on a block to set position 2")
                .build();
    }

    public ItemStack getRegionViewer() {
        return new ItemBuilder(Material.RECOVERY_COMPASS)
                .setName("&6&lVIEW EVENT REGION")
                .setLore("&eright click to show event zone borders")
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

    // Nuevos métodos para núcleos
    public void addCoreLocation(Location location) {
        RaidEvent.getInstance().getCoreLocationManager().addCoreLocation(location);
    }

    public boolean removeCoreLocation(Location location) {
        return RaidEvent.getInstance().getCoreLocationManager().removeNearestCoreLocation(location);
    }

    // Nuevos métodos para región
    public void setRegionPos1(Location location) {
        RaidEvent.getInstance().getRegionManager().setPos1(location);
    }

    public void setRegionPos2(Location location) {
        RaidEvent.getInstance().getRegionManager().setPos2(location);
    }
}