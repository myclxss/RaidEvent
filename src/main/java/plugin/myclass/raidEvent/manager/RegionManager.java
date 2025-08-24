package plugin.myclass.raidEvent.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.myclass.raidEvent.RaidEvent;

public class RegionManager {

    private Location pos1;
    private Location pos2;
    private BukkitRunnable particleTask;

    public void setPos1(Location location) {
        pos1 = new Location(
                location.getWorld(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
        saveRegionData();
        RaidEvent.getInstance().getLogger().info("Event Region POS1 set to: " + formatLocation(pos1));
    }

    public void setPos2(Location location) {
        pos2 = new Location(
                location.getWorld(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
        saveRegionData();
        RaidEvent.getInstance().getLogger().info("Event Region POS2 set to: " + formatLocation(pos2));
    }

    public boolean isRegionConfigured() {
        return pos1 != null && pos2 != null && pos1.getWorld().equals(pos2.getWorld());
    }

    public boolean isLocationInRegion(Location location) {
        if (!isRegionConfigured()) {
            return false;
        }

        if (!location.getWorld().equals(pos1.getWorld())) {
            return false;
        }

        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return location.getX() >= minX && location.getX() <= maxX &&
                location.getY() >= minY && location.getY() <= maxY &&
                location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    public void handlePlayerDeathInRegion(String playerName) {
        try {
            String command = "eco take " + playerName + " 100";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            RaidEvent.getInstance().getLogger().info("Player " + playerName + " died in event region - $100 deducted");
        } catch (Exception e) {
            RaidEvent.getInstance().getLogger().warning("Failed to execute economy command for " + playerName + ": " + e.getMessage());
        }
    }

    public void startParticleEffects() {
        if (!isRegionConfigured()) {
            return;
        }

        stopParticleEffects();

        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                spawnRegionBorderParticles();
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

    private void spawnRegionBorderParticles() {
        if (!isRegionConfigured()) {
            return;
        }

        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        // Dibujar esquinas verticales
        for (double y = minY; y <= maxY; y += 2.0) {
            pos1.getWorld().spawnParticle(Particle.DUST, minX, y, minZ, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(org.bukkit.Color.YELLOW, 1.5f));
            pos1.getWorld().spawnParticle(Particle.DUST, maxX, y, minZ, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(org.bukkit.Color.YELLOW, 1.5f));
            pos1.getWorld().spawnParticle(Particle.DUST, minX, y, maxZ, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(org.bukkit.Color.YELLOW, 1.5f));
            pos1.getWorld().spawnParticle(Particle.DUST, maxX, y, maxZ, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(org.bukkit.Color.YELLOW, 1.5f));
        }

        // Dibujar bordes horizontales
        for (double x = minX; x <= maxX; x += 2.0) {
            for (double z = minZ; z <= maxZ; z += 2.0) {
                if (x == minX || x == maxX || z == minZ || z == maxZ) {
                    pos1.getWorld().spawnParticle(Particle.DUST, x, minY, z, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(org.bukkit.Color.YELLOW, 1.0f));
                    pos1.getWorld().spawnParticle(Particle.DUST, x, maxY, z, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(org.bukkit.Color.YELLOW, 1.0f));
                }
            }
        }
    }

    public void loadRegionData() {
        try {
            if (RaidEvent.getInstance().getSettings().contains("EVENT-REGION.POS1")) {
                String pos1String = RaidEvent.getInstance().getSettings().getString("EVENT-REGION.POS1");
                pos1 = parseLocationString(pos1String);
            }
            if (RaidEvent.getInstance().getSettings().contains("EVENT-REGION.POS2")) {
                String pos2String = RaidEvent.getInstance().getSettings().getString("EVENT-REGION.POS2");
                pos2 = parseLocationString(pos2String);
            }

            if (isRegionConfigured()) {
                RaidEvent.getInstance().getLogger().info("Event region loaded: " + formatLocation(pos1) + " to " + formatLocation(pos2));
            }
        } catch (Exception e) {
            RaidEvent.getInstance().getLogger().warning("Error loading event region data: " + e.getMessage());
        }
    }

    private void saveRegionData() {
        if (pos1 != null) {
            RaidEvent.getInstance().getSettings().set("EVENT-REGION.POS1", formatLocationString(pos1));
        }
        if (pos2 != null) {
            RaidEvent.getInstance().getSettings().set("EVENT-REGION.POS2", formatLocationString(pos2));
        }
        RaidEvent.getInstance().getSettings().save();
    }

    private Location parseLocationString(String locationString) {
        if (locationString == null) return null;

        String[] parts = locationString.split(",");
        if (parts.length >= 4) {
            String world = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            return new Location(Bukkit.getWorld(world), x, y, z);
        }
        return null;
    }

    private String formatLocationString(Location location) {
        return location.getWorld().getName() + "," +
                location.getBlockX() + "," +
                location.getBlockY() + "," +
                location.getBlockZ();
    }

    private String formatLocation(Location location) {
        return location.getWorld().getName() + " " +
                location.getBlockX() + "," +
                location.getBlockY() + "," +
                location.getBlockZ();
    }

    public void clearRegion() {
        pos1 = null;
        pos2 = null;
        stopParticleEffects();
        RaidEvent.getInstance().getSettings().set("EVENT-REGION", null);
        RaidEvent.getInstance().getSettings().save();
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public String getRegionInfo() {
        if (!isRegionConfigured()) {
            return "No event region configured";
        }

        double volume = Math.abs((pos2.getX() - pos1.getX()) *
                (pos2.getY() - pos1.getY()) *
                (pos2.getZ() - pos1.getZ()));

        return "Event Region: " + formatLocation(pos1) + " to " + formatLocation(pos2) +
                " (Volume: " + (int)volume + " blocks)";
    }
}