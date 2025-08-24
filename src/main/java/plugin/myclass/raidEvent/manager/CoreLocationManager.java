package plugin.myclass.raidEvent.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.myclass.raidEvent.RaidEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CoreLocationManager {

    private final List<Location> coreLocations = new ArrayList<>();
    private final Random random = new Random();
    private BukkitRunnable particleTask;
    private int currentLocationIndex = 0;

    public void addCoreLocation(Location location) {
        Location normalizedLocation = new Location(
                location.getWorld(),
                location.getBlockX() + 0.5,
                location.getBlockY(),
                location.getBlockZ() + 0.5,
                location.getYaw(),
                location.getPitch()
        );

        coreLocations.add(normalizedLocation);
        saveCoreLocations();
    }

    public boolean removeNearestCoreLocation(Location location) {
        if (coreLocations.isEmpty()) {
            return false;
        }

        Location nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Location coreLoc : coreLocations) {
            if (coreLoc.getWorld().equals(location.getWorld())) {
                double distance = coreLoc.distance(location);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = coreLoc;
                }
            }
        }

        if (nearest != null && minDistance <= 3.0) {
            coreLocations.remove(nearest);
            saveCoreLocations();
            return true;
        }

        return false;
    }

    public Location getLocationForWave(int wave) {
        if (coreLocations.isEmpty()) {
            // Fallback a la configuración original
            return new Location(
                    Bukkit.getWorld(RaidEvent.getInstance().getSettings().getString("BLOCK.LOCATION.WORLD")),
                    RaidEvent.getInstance().getSettings().getInt("BLOCK.LOCATION.X"),
                    RaidEvent.getInstance().getSettings().getInt("BLOCK.LOCATION.Y"),
                    RaidEvent.getInstance().getSettings().getInt("BLOCK.LOCATION.Z")
            );
        }

        // Calcular índice basado en la oleada
        int index = (wave - 1) % coreLocations.size();
        currentLocationIndex = index;
        return coreLocations.get(index).clone();
    }

    public Location getRandomCoreLocation() {
        if (coreLocations.isEmpty()) {
            return null;
        }
        return coreLocations.get(random.nextInt(coreLocations.size())).clone();
    }

    public Location getCurrentLocation() {
        if (coreLocations.isEmpty() || currentLocationIndex >= coreLocations.size()) {
            return null;
        }
        return coreLocations.get(currentLocationIndex).clone();
    }

    public void startParticleEffects() {
        if (coreLocations.isEmpty()) {
            return;
        }

        if (particleTask != null && !particleTask.isCancelled()) {
            particleTask.cancel();
        }

        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < coreLocations.size(); i++) {
                    Location location = coreLocations.get(i);
                    if (location.getWorld() != null) {
                        spawnCoreLocationEffect(location, i + 1);
                    }
                }
            }
        };

        particleTask.runTaskTimer(RaidEvent.getInstance(), 0, 20);
    }

    public void stopParticleEffects() {
        if (particleTask != null && !particleTask.isCancelled()) {
            particleTask.cancel();
            particleTask = null;
        }
    }

    private void spawnCoreLocationEffect(Location location, int number) {
        // Crear un cubo de partículas alrededor de la ubicación del núcleo
        double size = 2.0;

        for (double x = -size; x <= size; x += 0.5) {
            for (double y = 0; y <= size; y += 0.5) {
                for (double z = -size; z <= size; z += 0.5) {
                    // Solo en los bordes del cubo
                    if (Math.abs(x) == size || Math.abs(y) == size || Math.abs(z) == size) {
                        Location particleLocation = location.clone().add(x, y, z);

                        location.getWorld().spawnParticle(
                                Particle.DUST,
                                particleLocation,
                                1,
                                0, 0, 0,
                                0,
                                new Particle.DustOptions(org.bukkit.Color.YELLOW, 1.0f)
                        );
                    }
                }
            }
        }

        // Números flotantes para identificar cada ubicación
        if (random.nextInt(4) == 0) {
            location.getWorld().spawnParticle(
                    Particle.ENCHANT,
                    location.clone().add(0, 3, 0),
                    number * 2,
                    1, 1, 1,
                    0.1
            );
        }
    }

    public void loadCoreLocations() {
        coreLocations.clear();

        if (!RaidEvent.getInstance().getSettings().contains("CORE-LOCATIONS")) {
            return;
        }

        List<String> locationStrings = RaidEvent.getInstance().getSettings().getStringList("CORE-LOCATIONS");

        for (String locationString : locationStrings) {
            try {
                String[] parts = locationString.split(",");
                if (parts.length >= 4) {
                    String worldName = parts[0];
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0.0f;
                    float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0.0f;

                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                    if (location.getWorld() != null) {
                        coreLocations.add(location);
                    }
                }
            } catch (Exception e) {
                RaidEvent.getInstance().getLogger().warning("Error loading core location: " + locationString);
            }
        }

        RaidEvent.getInstance().getLogger().info("Loaded " + coreLocations.size() + " core locations");
    }

    private void saveCoreLocations() {
        List<String> locationStrings = new ArrayList<>();

        for (Location location : coreLocations) {
            String locationString = location.getWorld().getName() + "," +
                    location.getX() + "," +
                    location.getY() + "," +
                    location.getZ() + "," +
                    location.getYaw() + "," +
                    location.getPitch();
            locationStrings.add(locationString);
        }

        RaidEvent.getInstance().getSettings().set("CORE-LOCATIONS", locationStrings);
        RaidEvent.getInstance().getSettings().save();

        RaidEvent.getInstance().getLogger().info("Saved " + coreLocations.size() + " core locations");
    }

    public int getCoreLocationCount() {
        return coreLocations.size();
    }

    public boolean hasCoreLocations() {
        return !coreLocations.isEmpty();
    }

    public List<Location> getAllCoreLocations() {
        return new ArrayList<>(coreLocations);
    }

    public void clearAllCoreLocations() {
        coreLocations.clear();
        saveCoreLocations();
    }

    public int getCurrentLocationIndex() {
        return currentLocationIndex;
    }

    public Location getNextLocationForWave(int wave) {
        return getLocationForWave(wave);
    }
}