package plugin.myclass.raidEvent.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.myclass.raidEvent.RaidEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobManager {

    private final Random random = new Random();
    private final List<LivingEntity> spawnedMobs = new ArrayList<>();

    public void spawnMobs(Location location) {
        Bukkit.getLogger().info("SpawnMobs method called with location: " + location);

        double x = RaidEvent.getInstance().getSettings().getDouble("BLOCK.LOCATION.X");
        double y = RaidEvent.getInstance().getSettings().getDouble("BLOCK.LOCATION.Y");
        double z = RaidEvent.getInstance().getSettings().getDouble("BLOCK.LOCATION.Z");
        String worldName = RaidEvent.getInstance().getSettings().getString("BLOCK.LOCATION.WORLD");
        Location centerLocation = new Location(Bukkit.getWorld(worldName), x, y, z);

        for (String key : RaidEvent.getInstance().getMobs().getConfigurationSection("MOB").getKeys(false)) {
            String path = "MOB." + key;
            EntityType type = EntityType.valueOf(RaidEvent.getInstance().getMobs().getString(path + ".TYPE"));
            String name = RaidEvent.getInstance().getMobs().getString(path + ".NAME");
            int amount = RaidEvent.getInstance().getMobs().getInt(path + ".AMOUNT");
            int health = RaidEvent.getInstance().getMobs().getInt(path + ".HEALTH");
            int damage = RaidEvent.getInstance().getMobs().getInt(path + ".DAMAGE");
            List<String> effectsMob = RaidEvent.getInstance().getMobs().getStringList(path + ".EFFECTS-MOB");

            Bukkit.getLogger().info("Spawning " + amount + " mobs of type " + type + " with name " + name);

            for (int i = 0; i < amount; i++) {
                Location spawnLocation = getRandomLocationAround(centerLocation, 4);
                LivingEntity mob = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, type);
                mob.setCustomName(name);
                mob.setMaxHealth(health);
                mob.setHealth(health);

                for (String effect : effectsMob) {
                    String[] parts = effect.split(":");
                    PotionEffectType effectType = PotionEffectType.getByName(parts[0]);
                    int level = Integer.parseInt(parts[1]);
                    int duration = Integer.parseInt(parts[2]);
                    mob.addPotionEffect(new PotionEffect(effectType, duration, level));
                }
                spawnedMobs.add(mob);
            }
        }
    }

    public void removeSpawnedMobs() {
        for (LivingEntity mob : spawnedMobs) {
            if (mob != null && !mob.isDead()) {
                mob.remove();
            }
        }
        spawnedMobs.clear();
    }

    private Location getRandomLocationAround(Location center, int radius) {
        double x = center.getX() + (random.nextDouble() * radius * 2 - radius);
        double z = center.getZ() + (random.nextDouble() * radius * 2 - radius);
        double y = center.getY();
        return new Location(center.getWorld(), x, y, z);
    }
}