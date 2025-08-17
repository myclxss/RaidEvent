package plugin.myclass.raidEvent.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.myclass.raidEvent.RaidEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MobManager {

    private final Random random = new Random();
    private final List<LivingEntity> spawnedMobs = new ArrayList<>();
    private final Map<Integer, List<LivingEntity>> mobsByWave = new HashMap<>();

    public void spawnMobs(Location location) {
        spawnWaveMobs(location, 1);
    }

    public void spawnWaveMobs(Location location, int wave) {
        Bukkit.getLogger().info("SpawnWaveMobs method called for wave " + wave + " at location: " + location);

        if (!RaidEvent.getInstance().getSpawnLocationManager().hasSpawnLocations()) {
            Bukkit.getLogger().warning("No spawn locations configured! Please add spawn locations first.");
            return;
        }

        mobsByWave.computeIfAbsent(wave, k -> new ArrayList<>());

        String wavePath = "WAVES.WAVE-" + wave + ".MOBS";

        if (!RaidEvent.getInstance().getSettings().contains(wavePath)) {
            Bukkit.getLogger().warning("No mob configuration found for wave " + wave + " at path: " + wavePath);
            spawnLegacyMobs(wave);
            return;
        }

        if (RaidEvent.getInstance().getSettings().getConfigurationSection(wavePath) == null) {
            Bukkit.getLogger().warning("No mobs section found for wave " + wave);
            return;
        }

        for (String key : RaidEvent.getInstance().getSettings().getConfigurationSection(wavePath).getKeys(false)) {
            String mobPath = wavePath + "." + key;

            try {
                EntityType type = EntityType.valueOf(RaidEvent.getInstance().getSettings().getString(mobPath + ".TYPE"));
                String name = RaidEvent.getInstance().getSettings().getString(mobPath + ".NAME");
                int amount = RaidEvent.getInstance().getSettings().getInt(mobPath + ".AMOUNT");
                int health = RaidEvent.getInstance().getSettings().getInt(mobPath + ".HEALTH");
                int damage = RaidEvent.getInstance().getSettings().getInt(mobPath + ".DAMAGE");
                List<String> effectsMob = RaidEvent.getInstance().getSettings().getStringList(mobPath + ".EFFECTS-MOB");

                Bukkit.getLogger().info("Spawning " + amount + " mobs of type " + type + " with name " + name + " for wave " + wave);

                for (int i = 0; i < amount; i++) {
                    Location spawnLocation = getConfiguredSpawnLocation();

                    if (spawnLocation == null) {
                        Bukkit.getLogger().warning("Could not get spawn location for mob " + name);
                        continue;
                    }

                    LivingEntity mob = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, type);
                    mob.setCustomName(name);
                    mob.setCustomNameVisible(true);
                    mob.setMaxHealth(health);
                    mob.setHealth(health);
                    mob.setRemoveWhenFarAway(false);

                    configureMobTargeting(mob, mobPath);

                    for (String effect : effectsMob) {
                        try {
                            String[] parts = effect.split(":");
                            if (parts.length >= 3) {
                                PotionEffectType effectType = PotionEffectType.getByName(parts[0]);
                                if (effectType != null) {
                                    int level = Integer.parseInt(parts[1]);
                                    int duration = Integer.parseInt(parts[2]);
                                    mob.addPotionEffect(new PotionEffect(effectType, duration, level));
                                }
                            }
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("Error applying effect " + effect + " to mob: " + e.getMessage());
                        }
                    }

                    spawnedMobs.add(mob);
                    mobsByWave.get(wave).add(mob);
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error spawning mob " + key + " for wave " + wave + ": " + e.getMessage());
            }
        }
    }

    private void spawnLegacyMobs(int wave) {
        for (String key : RaidEvent.getInstance().getMobs().getConfigurationSection("MOB").getKeys(false)) {
            String path = "MOB." + key;
            EntityType type = EntityType.valueOf(RaidEvent.getInstance().getMobs().getString(path + ".TYPE"));
            String name = RaidEvent.getInstance().getMobs().getString(path + ".NAME");
            int amount = RaidEvent.getInstance().getMobs().getInt(path + ".AMOUNT");
            int health = RaidEvent.getInstance().getMobs().getInt(path + ".HEALTH");
            int damage = RaidEvent.getInstance().getMobs().getInt(path + ".DAMAGE");
            List<String> effectsMob = RaidEvent.getInstance().getMobs().getStringList(path + ".EFFECTS-MOB");

            Bukkit.getLogger().info("Spawning " + amount + " legacy mobs of type " + type + " with name " + name);

            for (int i = 0; i < amount; i++) {
                Location spawnLocation = getConfiguredSpawnLocation();

                if (spawnLocation == null) {
                    continue;
                }

                LivingEntity mob = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, type);
                mob.setCustomName(name);
                mob.setCustomNameVisible(true);
                mob.setMaxHealth(health);
                mob.setHealth(health);
                mob.setRemoveWhenFarAway(false);

                configureMobTargeting(mob, null);

                for (String effect : effectsMob) {
                    try {
                        String[] parts = effect.split(":");
                        PotionEffectType effectType = PotionEffectType.getByName(parts[0]);
                        if (effectType != null) {
                            int level = Integer.parseInt(parts[1]);
                            int duration = Integer.parseInt(parts[2]);
                            mob.addPotionEffect(new PotionEffect(effectType, duration, level));
                        }
                    } catch (Exception e) {
                        RaidEvent.getInstance().getLogger().warning("Error applying effect " + effect + " to mob: " + e.getMessage());
                    }
                }

                spawnedMobs.add(mob);
                mobsByWave.get(wave).add(mob);
            }
        }
    }

    private Location getConfiguredSpawnLocation() {
        Location baseLocation = RaidEvent.getInstance().getSpawnLocationManager().getRandomSpawnLocation();

        if (baseLocation == null) {
            return null;
        }

        double offsetX = (random.nextDouble() - 0.5) * 1.0;
        double offsetZ = (random.nextDouble() - 0.5) * 1.0;

        Location spawnLocation = baseLocation.clone();
        spawnLocation.add(offsetX, 0, offsetZ);

        return findSafeSurface(spawnLocation);
    }

    private Location findSafeSurface(Location location) {
        Location safeLoc = location.clone();

        while (safeLoc.getY() > 0 && !safeLoc.getBlock().getType().isSolid()) {
            safeLoc.add(0, -1, 0);
        }

        safeLoc.add(0, 1, 0);
        return safeLoc;
    }

    private void configureMobTargeting(LivingEntity mob, String mobPath) {
        try {
            double targetRange, followRange;

            if (mobPath != null) {
                targetRange = RaidEvent.getInstance().getSettings().getDouble(mobPath + ".TARGET-RANGE",
                        RaidEvent.getInstance().getSettings().getDouble("MOB.TARGET-RANGE", 32.0));
                followRange = RaidEvent.getInstance().getSettings().getDouble(mobPath + ".FOLLOW-RANGE",
                        RaidEvent.getInstance().getSettings().getDouble("MOB.FOLLOW-RANGE", 40.0));
            } else {
                targetRange = RaidEvent.getInstance().getSettings().getDouble("MOB.TARGET-RANGE", 32.0);
                followRange = RaidEvent.getInstance().getSettings().getDouble("MOB.FOLLOW-RANGE", 40.0);
            }

            if (mob.getAttribute(org.bukkit.attribute.Attribute.FOLLOW_RANGE) != null) {
                mob.getAttribute(org.bukkit.attribute.Attribute.FOLLOW_RANGE).setBaseValue(followRange);
            }

            org.bukkit.entity.Player target = findNearestPlayer(mob.getLocation(), targetRange);
            if (target != null && mob instanceof org.bukkit.entity.Creature creature) {
                creature.setTarget(target);
            }

            Bukkit.getLogger().info("Configured targeting for mob " + mob.getCustomName() +
                    " - Target Range: " + targetRange + ", Follow Range: " + followRange);

        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to configure mob targeting: " + e.getMessage());
        }
    }

    private org.bukkit.entity.Player findNearestPlayer(Location mobLocation, double range) {
        org.bukkit.entity.Player nearestPlayer = null;
        double nearestDistance = range;

        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(mobLocation.getWorld())) {
                double distance = player.getLocation().distance(mobLocation);
                if (distance <= range && distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestPlayer = player;
                }
            }
        }

        return nearestPlayer;
    }

    public void removeSpawnedMobs() {
        Bukkit.getLogger().info("Removing all spawned mobs (" + spawnedMobs.size() + " total)");

        for (LivingEntity mob : new ArrayList<>(spawnedMobs)) {
            if (mob != null && !mob.isDead()) {
                mob.remove();
            }
        }
        spawnedMobs.clear();

        for (List<LivingEntity> waveMobs : mobsByWave.values()) {
            waveMobs.clear();
        }
        mobsByWave.clear();
    }

    public void removeWaveMobs(int wave) {
        List<LivingEntity> waveMobs = mobsByWave.get(wave);
        if (waveMobs == null || waveMobs.isEmpty()) {
            Bukkit.getLogger().info("No mobs found for wave " + wave + " to remove");
            return;
        }

        Bukkit.getLogger().info("Removing " + waveMobs.size() + " mobs from wave " + wave);

        for (LivingEntity mob : new ArrayList<>(waveMobs)) {
            if (mob != null && !mob.isDead()) {
                mob.remove();
            }
            spawnedMobs.remove(mob);
        }

        waveMobs.clear();
        mobsByWave.remove(wave);
    }

    public int getSpawnedMobCount() {
        return spawnedMobs.size();
    }

    public List<LivingEntity> getSpawnedMobs() {
        return new ArrayList<>(spawnedMobs);
    }

    public List<LivingEntity> getWaveMobs(int wave) {
        return mobsByWave.getOrDefault(wave, new ArrayList<>());
    }

    public int getWaveMobCount(int wave) {
        List<LivingEntity> waveMobs = mobsByWave.get(wave);
        return waveMobs != null ? waveMobs.size() : 0;
    }
}