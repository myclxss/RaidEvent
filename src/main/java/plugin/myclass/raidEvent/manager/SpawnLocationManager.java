package plugin.myclass.raidEvent.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.myclass.raidEvent.RaidEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnLocationManager {

    private final List<Location> spawnLocations = new ArrayList<>();
    private final Random random = new Random();
    private BukkitRunnable particleTask;

    public void addSpawnLocation(Location location) {
        Location normalizedLocation = new Location(
                location.getWorld(),
                location.getBlockX() + 0.5,
                location.getBlockY() + 0.1,
                location.getBlockZ() + 0.5
        );

        spawnLocations.add(normalizedLocation);
        saveSpawnLocations();
    }

    public boolean removeNearestSpawnLocation(Location location) {
        if (spawnLocations.isEmpty()) {
            return false;
        }

        Location nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Location spawnLoc : spawnLocations) {
            if (spawnLoc.getWorld().equals(location.getWorld())) {
                double distance = spawnLoc.distance(location);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = spawnLoc;
                }
            }
        }

        if (nearest != null && minDistance <= 3.0) {
            spawnLocations.remove(nearest);
            saveSpawnLocations();
            return true;
        }

        return false;
    }

    public Location getRandomSpawnLocation() {
        if (spawnLocations.isEmpty()) {
            return null;
        }
        return spawnLocations.get(random.nextInt(spawnLocations.size())).clone();
    }

    public void startParticleEffects() {
        if (spawnLocations.isEmpty()) {
            return;
        }

        if (particleTask != null && !particleTask.isCancelled()) {
            particleTask.cancel();
        }

        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Location location : spawnLocations) {
                    if (location.getWorld() != null) {
                        spawnSoulCircleEffect(location);
                    }
                }
            }
        };

        particleTask.runTaskTimer(RaidEvent.getInstance(), 0, 10);
    }

    public void startWaveParticleEffects(int wave) {
        // Por simplicidad, usamos el mismo efecto para todas las oleadas
        // Si quieres efectos diferentes por oleada, se puede expandir aquí
        startParticleEffects();
    }

    public void stopParticleEffects() {
        if (particleTask != null && !particleTask.isCancelled()) {
            particleTask.cancel();
            particleTask = null;
        }
    }

    private void spawnSoulCircleEffect(Location location) {
        double radius = 1.5;
        int points = 16;

        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = location.getX() + radius * Math.cos(angle);
            double z = location.getZ() + radius * Math.sin(angle);

            Location particleLocation = new Location(location.getWorld(), x, location.getY(), z);

            location.getWorld().spawnParticle(
                    Particle.SOUL,
                    particleLocation,
                    2,
                    0.1, 0.1, 0.1,
                    0.02
            );
        }

        if (random.nextInt(5) == 0) {
            location.getWorld().spawnParticle(
                    Particle.SOUL,
                    location,
                    5,
                    0.3, 0.1, 0.3,
                    0.01
            );
        }
    }

    public void loadSpawnLocations() {
        spawnLocations.clear();

        if (!RaidEvent.getInstance().getSettings().contains("SPAWN-LOCATIONS")) {
            return;
        }

        List<String> locationStrings = RaidEvent.getInstance().getSettings().getStringList("SPAWN-LOCATIONS");

        for (String locationString : locationStrings) {
            try {
                String[] parts = locationString.split(",");
                if (parts.length >= 4) {
                    String worldName = parts[0];
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);

                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                    if (location.getWorld() != null) {
                        spawnLocations.add(location);
                    }
                }
            } catch (Exception e) {
                RaidEvent.getInstance().getLogger().warning("Error loading spawn location: " + locationString);
            }
        }
    }

    private void saveSpawnLocations() {
        List<String> locationStrings = new ArrayList<>();

        for (Location location : spawnLocations) {
            String locationString = location.getWorld().getName() + "," +
                    location.getX() + "," +
                    location.getY() + "," +
                    location.getZ();
            locationStrings.add(locationString);
        }

        RaidEvent.getInstance().getSettings().set("SPAWN-LOCATIONS", locationStrings);
        RaidEvent.getInstance().getSettings().save();
    }

    public int getSpawnLocationCount() {
        return spawnLocations.size();
    }

    public boolean hasSpawnLocations() {
        return !spawnLocations.isEmpty();
    }

    public List<Location> getAllSpawnLocations() {
        return new ArrayList<>(spawnLocations);
    }

    public void clearAllSpawnLocations() {
        spawnLocations.clear();
        saveSpawnLocations();
    }
}