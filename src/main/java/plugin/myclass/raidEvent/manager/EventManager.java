package plugin.myclass.raidEvent.manager;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import plugin.myclass.raidEvent.RaidEvent;
import plugin.myclass.raidEvent.utils.ColorUtil;
import plugin.myclass.raidEvent.utils.TitleUtil;

public class EventManager {

    private Block raidBlock;
    private BlockDisplay blockDisplay;
    private TextDisplay textDisplay;
    private int blockHealth = RaidEvent.getInstance().getSettings().getInt("BLOCK.HEALTH");
    private BossBar bossBar;
    private BukkitRunnable mobSpawnerTask;

    public void startEvent(Location location) {
        raidBlock = location.getBlock();
        raidBlock.setType(Material.valueOf(RaidEvent.getInstance().getSettings().getString("BLOCK.TYPE")));
        blockHealth = RaidEvent.getInstance().getSettings().getInt("BLOCK.HEALTH");

        Location displayLocation = location.clone();
        blockDisplay = displayLocation.getWorld().spawn(displayLocation, BlockDisplay.class);
        blockDisplay.setInvisible(true);
        blockDisplay.setInvulnerable(true);
        blockDisplay.setGlowing(true);
        blockDisplay.setCustomNameVisible(false);
        blockDisplay.setBlock(Bukkit.createBlockData(Material.valueOf(RaidEvent.getInstance().getSettings().getString("BLOCK.TYPE"))));
        Transformation transformation = blockDisplay.getTransformation();
        transformation.getScale().set(0.98f);
        blockDisplay.setTransformation(transformation);

        Location textLocation = location.clone().add(0.5, 1, 0.5);
        textDisplay = textLocation.getWorld().spawn(textLocation, TextDisplay.class);
        textDisplay.setCustomNameVisible(true);
        textDisplay.setCustomName(ColorUtil.add(RaidEvent.getInstance().getSettings().getString("BLOCK.NAME")));

        bossBar = Bukkit.createBossBar(
                ColorUtil.add(RaidEvent.getInstance().getSettings().getString("BOSSBAR.TITLE").replace("%block_health%", String.valueOf(blockHealth))),
                BarColor.valueOf(RaidEvent.getInstance().getSettings().getString("BOSSBAR.COLOR")),
                BarStyle.valueOf(RaidEvent.getInstance().getSettings().getString("BOSSBAR.STYLE")));
        bossBar.setProgress(1.0);

        for (Player player : Bukkit.getOnlinePlayers()) {
            TitleUtil.sendTitle(player, RaidEvent.getInstance().getLang().getString("START-EVENT.TITLE"), RaidEvent.getInstance().getLang().getString("START-EVENT.SUBTITLE"), 20, 40, 20);
            RaidEvent.getInstance().getLang().getStringList("START-EVENT.MESSAGE").forEach(message -> {
                player.sendMessage(ColorUtil.add(message));
            });
            bossBar.addPlayer(player);
        }

        spawnMobs(location);
        startMobSpawnerTask(location);
    }

    public void stopEvent() {
        if (raidBlock != null) {
            raidBlock.setType(Material.AIR);
            raidBlock = null;
        }

        if (blockDisplay != null) {
            blockDisplay.remove();
            blockDisplay = null;
        }

        if (textDisplay != null) {
            textDisplay.remove();
            textDisplay = null;
        }

        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }

        if (mobSpawnerTask != null) {
            mobSpawnerTask.cancel();
            mobSpawnerTask = null;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            TitleUtil.sendTitle(player, RaidEvent.getInstance().getLang().getString("STOP-EVENT.TITLE"), RaidEvent.getInstance().getLang().getString("STOP-EVENT.SUBTITLE"), 20, 40, 20);
            RaidEvent.getInstance().getLang().getStringList("STOP-EVENT.MESSAGE").forEach(message -> {
                player.sendMessage(ColorUtil.add(message));
            });
        }

        RaidEvent.getInstance().getMobManager().removeSpawnedMobs();
    }

    public void damageBlock(Player player) {
        if (raidBlock != null && blockHealth > 0) {
            blockHealth--;

            String particleName = RaidEvent.getInstance().getSettings().getString("BLOCK.PARTICLE");
            Particle particle = Particle.valueOf(particleName);
            raidBlock.getWorld().spawnParticle(particle, raidBlock.getLocation().add(0.5, 0.5, 0.5), 10, 0.5, 0.5, 0.5);

            String soundName = RaidEvent.getInstance().getSettings().getString("BLOCK.SOUND");
            Sound sound = Sound.valueOf(soundName);
            raidBlock.getWorld().playSound(raidBlock.getLocation(), sound, 1.0f, 1.0f);

            if (bossBar != null) {
                bossBar.setProgress(blockHealth / (double) RaidEvent.getInstance().getSettings().getInt("BLOCK.HEALTH"));
                bossBar.setTitle(ColorUtil.add(RaidEvent.getInstance().getSettings().getString("BOSSBAR.TITLE").replace("%block_health%", String.valueOf(blockHealth))));
                bossBar.setColor(BarColor.valueOf(RaidEvent.getInstance().getSettings().getString("BOSSBAR.COLOR")));
            }
            if (blockHealth == 0) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    TitleUtil.sendTitle(players, RaidEvent.getInstance().getLang().getString("WIN-EVENT.TITLE"), RaidEvent.getInstance().getLang().getString("WIN-EVENT.SUBTITLE"), 20, 40, 20);
                    RaidEvent.getInstance().getLang().getStringList("WIN-EVENT.MESSAGE").forEach(message -> {
                        players.sendMessage(ColorUtil.add(message));
                    });
                }
                stopEvent();
            }
        }
    }

    public void spawnMobs(Location location) {
        RaidEvent.getInstance().getMobManager().spawnMobs(location);
    }

    public boolean isRaidBlock(Block block) {
        return block.equals(raidBlock);
    }

    private void startMobSpawnerTask(Location location) {
        int spawnTaskIntervalSeconds = RaidEvent.getInstance().getSettings().getInt("MOB.SPAWN-TASK");
        int spawnTaskIntervalTicks = spawnTaskIntervalSeconds * 20;

        mobSpawnerTask = new BukkitRunnable() {
            @Override
            public void run() {
                spawnMobs(location);
            }
        };
        mobSpawnerTask.runTaskTimer(RaidEvent.getInstance(), 0, spawnTaskIntervalTicks);
    }
}