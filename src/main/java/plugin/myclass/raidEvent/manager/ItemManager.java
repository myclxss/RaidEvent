package plugin.myclass.raidEvent.manager;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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
}
