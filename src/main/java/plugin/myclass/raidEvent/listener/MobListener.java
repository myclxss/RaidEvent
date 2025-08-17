package plugin.myclass.raidEvent.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.myclass.raidEvent.RaidEvent;

import java.util.List;

public class MobListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity damager) || !(event.getEntity() instanceof Player player)) {
            return;
        }

        // Solo procesar si el evento está activo
        if (!RaidEvent.getInstance().getEventManager().isEventActive()) {
            return;
        }

        RaidEvent plugin = RaidEvent.getInstance();
        int currentWave = plugin.getEventManager().getCurrentWave();

        // Buscar efectos de hit para la oleada actual
        String wavePath = "WAVES.WAVE-" + currentWave + ".MOBS";

        if (!plugin.getSettings().contains(wavePath)) {
            // Fallback al sistema anterior si no hay configuración de oleadas
            processLegacyMobEffects(damager, player);
            return;
        }

        // Procesar efectos de mobs de la oleada actual
        processWaveMobEffects(damager, player, wavePath);
    }

    private void processWaveMobEffects(LivingEntity damager, Player player, String wavePath) {
        RaidEvent plugin = RaidEvent.getInstance();

        if (plugin.getSettings().getConfigurationSection(wavePath) == null) {
            return;
        }

        for (String key : plugin.getSettings().getConfigurationSection(wavePath).getKeys(false)) {
            String mobPath = wavePath + "." + key;
            String mobName = plugin.getSettings().getString(mobPath + ".NAME");

            if (damager.getCustomName() != null && damager.getCustomName().equals(mobName)) {
                List<String> hitEffects = plugin.getSettings().getStringList(mobPath + ".HIT-EFFECTS");

                for (String effect : hitEffects) {
                    applyPotionEffect(player, effect, mobName);
                }
                break; // Salir del bucle una vez encontrado el mob correcto
            }
        }
    }

    private void processLegacyMobEffects(LivingEntity damager, Player player) {
        RaidEvent plugin = RaidEvent.getInstance();

        if (plugin.getMobs().getConfigurationSection("MOB") == null) {
            return;
        }

        for (String key : plugin.getMobs().getConfigurationSection("MOB").getKeys(false)) {
            String path = "MOB." + key;
            String mobName = plugin.getMobs().getString(path + ".NAME");

            if (damager.getCustomName() != null && damager.getCustomName().equals(mobName)) {
                List<String> hitEffects = plugin.getMobs().getStringList(path + ".HIT-EFFECTS");

                for (String effect : hitEffects) {
                    applyPotionEffect(player, effect, mobName);
                }
                break;
            }
        }
    }

    private void applyPotionEffect(Player player, String effect, String mobName) {
        try {
            String[] parts = effect.split(":");
            if (parts.length < 3) {
                RaidEvent.getInstance().getLogger().warning("Invalid effect format: " + effect + " for mob: " + mobName);
                return;
            }

            PotionEffectType effectType = PotionEffectType.getByName(parts[0]);
            if (effectType != null) {
                int level = Integer.parseInt(parts[1]);
                int duration = Integer.parseInt(parts[2]);

                // Validar valores
                level = Math.max(0, Math.min(level, 255));
                duration = Math.max(1, Math.min(duration, 1000000));

                player.addPotionEffect(new PotionEffect(effectType, duration, level));
            } else {
                RaidEvent.getInstance().getLogger().warning("Invalid potion effect type: " + parts[0] + " for mob: " + mobName);
            }
        } catch (NumberFormatException e) {
            RaidEvent.getInstance().getLogger().warning("Invalid number in effect: " + effect + " for mob: " + mobName);
        } catch (Exception e) {
            RaidEvent.getInstance().getLogger().warning("Error applying effect " + effect + " for mob " + mobName + ": " + e.getMessage());
        }
    }
}