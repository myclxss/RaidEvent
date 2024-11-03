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

        if (lore.contains(ColorUtil.add("&eright click on the block to set"))) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.getPlayer().sendMessage("hola");
            }
        } else if (lore.contains(ColorUtil.add("&6left click to set the minimum")) && lore.contains(ColorUtil.add("&eright click to set the maximum"))) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.getPlayer().sendMessage("pe");
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                event.getPlayer().sendMessage("sad");
            }
        }
    }
}