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

    public ItemStack getCornerSetup() {
        return new ItemBuilder(Material.BLAZE_POWDER)
                .setName("&c&lSET REGION CORNERS")
                .setLore("&6left click to set the minimum", "&eright click to set the maximum")
                .build();
    }

    public void setBlockEvent(Location location) {
        RaidEvent.getInstance().getSettings().set("BLOCK.LOCATION.X", location.getBlockX());
        RaidEvent.getInstance().getSettings().set("BLOCK.LOCATION.Y", location.getBlockY());
        RaidEvent.getInstance().getSettings().set("BLOCK.LOCATION.Z", location.getBlockZ());
        RaidEvent.getInstance().getSettings().set("BLOCK.LOCATION.WORLD", location.getWorld().getName());
        RaidEvent.getInstance().getSettings().set("BLOCK.TYPE", location.getBlock().getType().toString());
        RaidEvent.getInstance().getSettings().save();
        RaidEvent.getInstance().getSettings().reload();
    }
}
