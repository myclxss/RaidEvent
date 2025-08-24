package plugin.myclass.raidEvent.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import plugin.myclass.raidEvent.RaidEvent;
import plugin.myclass.raidEvent.utils.ColorUtil;

public class DeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Solo procesar si el evento está activo
        if (!RaidEvent.getInstance().getEventManager().isEventActive()) {
            return;
        }

        // Solo procesar si hay una región configurada
        if (!RaidEvent.getInstance().getRegionManager().isRegionConfigured()) {
            return;
        }

        // Verificar si el jugador murió dentro de la región del evento
        if (RaidEvent.getInstance().getRegionManager().isLocationInRegion(player.getLocation())) {
            // Ejecutar comando de economía
            RaidEvent.getInstance().getRegionManager().handlePlayerDeathInRegion(player.getName());

            // Mensaje al jugador (se mostrará cuando respawnee)
            player.sendMessage(ColorUtil.add("&6⚡ Muerte en zona del evento"));
            player.sendMessage(ColorUtil.add("&e-$100 por morir durante el raid"));
        }
    }
}