package plugin.myclass.raidEvent.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import plugin.myclass.raidEvent.RaidEvent;

public class BreakListener implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Proteger el núcleo de ser roto accidentalmente
        if (RaidEvent.getInstance().getEventManager().isRaidBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Solo procesar click izquierdo en bloques (golpe de ataque)
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        // Verificar si es el núcleo del raid
        if (RaidEvent.getInstance().getEventManager().isRaidBlock(event.getClickedBlock())) {
            event.setCancelled(true); // Cancelar rotura del bloque
            RaidEvent.getInstance().getEventManager().damageBlock(player);
        }
    }
}