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
        if (event.getDamager() instanceof LivingEntity && event.getEntity() instanceof Player) {
            LivingEntity damager = (LivingEntity) event.getDamager();
            Player player = (Player) event.getEntity();
            RaidEvent plugin = RaidEvent.getInstance();

            for (String key : plugin.getMobs().getConfigurationSection("MOB").getKeys(false)) {
                String path = "MOB." + key;
                String mobName = plugin.getMobs().getString(path + ".NAME");

                if (damager.getCustomName() != null && damager.getCustomName().equals(mobName)) {
                    List<String> hitEffects = plugin.getMobs().getStringList(path + ".HIT-EFFECTS");

                    for (String effect : hitEffects) {
                        String[] parts = effect.split(":");
                        PotionEffectType effectType = PotionEffectType.getByName(parts[0]);
                        if (effectType != null) {
                            int level = Integer.parseInt(parts[1]);
                            int duration = Integer.parseInt(parts[2]);
                            player.addPotionEffect(new PotionEffect(effectType, duration, level));
                        } else {
                            plugin.getLogger().warning("Invalid potion effect type: " + parts[0]);
                        }
                    }
                    break;
                }
            }
        }
    }
}