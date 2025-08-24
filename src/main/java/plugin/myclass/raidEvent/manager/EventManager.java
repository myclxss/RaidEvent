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
import plugin.myclass.raidEvent.utils.SoundUtil;
import plugin.myclass.raidEvent.utils.BossBarUtil;

import java.util.*;

public class EventManager {

    private Block raidBlock;
    private BlockDisplay blockDisplay;
    private TextDisplay textDisplay;
    private int blockHealth;
    private int maxBlockHealth;
    private BossBar bossBar;
    private BukkitRunnable mobSpawnerTask;
    private BukkitRunnable waveTransitionTask;

    // Sistema de oleadas
    private int currentWave = 1;
    private int maxWaves = 10;
    private boolean eventActive = false;
    private boolean waveTransition = false;

    // Sistema de puntuación de jugadores
    private final Map<UUID, Integer> playerDamage = new HashMap<>();

    public void startEvent(Location location) {
        if (eventActive) {
            return;
        }

        eventActive = true;
        currentWave = 1;
        playerDamage.clear();

        // Obtener configuración de la primera oleada
        maxWaves = getMaxWavesFromConfig();

        // Usar la ubicación específica para la primera oleada
        Location waveLocation = RaidEvent.getInstance().getCoreLocationManager().getLocationForWave(1);
        startWave(waveLocation != null ? waveLocation : location);

        // Mensaje de inicio del evento
        for (Player player : Bukkit.getOnlinePlayers()) {
            TitleUtil.sendTitle(player,
                    RaidEvent.getInstance().getLang().getString("START-EVENT.TITLE"),
                    RaidEvent.getInstance().getLang().getString("START-EVENT.SUBTITLE"),
                    20, 40, 20);
            RaidEvent.getInstance().getLang().getStringList("START-EVENT.MESSAGE").forEach(message -> {
                player.sendMessage(ColorUtil.add(message.replace("%wave%", String.valueOf(currentWave))));
            });
            if (bossBar != null) {
                bossBar.addPlayer(player);
            }
        }
    }

    private void startWave(Location location) {
        if (!eventActive) return;

        RaidEvent.getInstance().getLogger().info("=== STARTING WAVE " + currentWave + " ===");

        // Obtener la ubicación específica para esta oleada
        Location waveLocation = RaidEvent.getInstance().getCoreLocationManager().getLocationForWave(currentWave);
        if (waveLocation != null) {
            location = waveLocation;
            RaidEvent.getInstance().getLogger().info("Using core location " +
                    (RaidEvent.getInstance().getCoreLocationManager().getCurrentLocationIndex() + 1) +
                    " for wave " + currentWave);
        } else {
            RaidEvent.getInstance().getLogger().info("No core locations configured, using provided location");
        }

        String wavePath = "WAVES.WAVE-" + currentWave;

        // Verificar que existe la configuración de la oleada
        if (!RaidEvent.getInstance().getSettings().contains(wavePath)) {
            RaidEvent.getInstance().getLogger().severe("Wave configuration not found for wave " + currentWave + " at path: " + wavePath);
            return;
        }

        // Configurar el bloque de la oleada en la nueva ubicación
        setupRaidBlock(location, wavePath);
        setupBlockDisplay(location, wavePath);
        setupTextDisplay(location, wavePath);

        // Transición de BossBar (limpia la anterior y crea nueva)
        transitionBossBar(wavePath);

        // Agregar jugadores al bossbar
        if (bossBar != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                bossBar.addPlayer(player);
                RaidEvent.getInstance().getLogger().info("Added player " + player.getName() + " to NEW bossbar for wave " + currentWave);
            }
        } else {
            RaidEvent.getInstance().getLogger().severe("BossBar is null! Cannot add players to bossbar for wave " + currentWave);
        }

        // Spawn inicial de mobs
        spawnWaveMobs(location);

        // Iniciar efectos de partículas específicos de la oleada
        RaidEvent.getInstance().getSpawnLocationManager().startWaveParticleEffects(currentWave);

        // Iniciar task de spawn de mobs cada 10 segundos
        startMobSpawnerTask(location);

        // Anunciar inicio de oleada con información de ubicación
        announceWaveStart(location);

        RaidEvent.getInstance().getLogger().info("=== WAVE " + currentWave + " STARTED SUCCESSFULLY at " +
                location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + " ===");
    }

    private void completeWave() {
        if (!eventActive) return;

        // Detener spawn de mobs
        if (mobSpawnerTask != null) {
            mobSpawnerTask.cancel();
            mobSpawnerTask = null;
        }

        // Remover mobs específicos de la oleada actual
        RaidEvent.getInstance().getMobManager().removeWaveMobs(currentWave);

        // Detener efectos de partículas
        RaidEvent.getInstance().getSpawnLocationManager().stopParticleEffects();

        // Anunciar completación de oleada
        announceWaveComplete();

        if (currentWave >= maxWaves) {
            // Evento completado
            completeEvent();
        } else {
            // Iniciar transición a siguiente oleada
            startWaveTransition();
        }
    }

    private void startWaveTransition() {
        RaidEvent.getInstance().getLogger().info("=== STARTING WAVE TRANSITION FROM " + (currentWave) + " TO " + (currentWave + 1) + " ===");

        waveTransition = true;
        currentWave++;

        // Limpiar displays anteriores
        cleanupDisplays();

        // Obtener la próxima ubicación del núcleo
        final Location nextLocation = RaidEvent.getInstance().getCoreLocationManager().getLocationForWave(currentWave);
        final String locationInfo = nextLocation != null ?
                " at " + nextLocation.getBlockX() + "," + nextLocation.getBlockY() + "," + nextLocation.getBlockZ() : "";

        // Anunciar próxima oleada con información de ubicación
        for (Player player : Bukkit.getOnlinePlayers()) {
            TitleUtil.sendTitle(player,
                    ColorUtil.add(RaidEvent.getInstance().getLang().getString("WAVE-TRANSITION.TITLE")
                            .replace("%next_wave%", String.valueOf(currentWave))),
                    ColorUtil.add(RaidEvent.getInstance().getLang().getString("WAVE-TRANSITION.SUBTITLE")),
                    10, 60, 10);

            RaidEvent.getInstance().getLang().getStringList("WAVE-TRANSITION.MESSAGE").forEach(message -> {
                player.sendMessage(ColorUtil.add(message
                        .replace("%current_wave%", String.valueOf(currentWave - 1))
                        .replace("%next_wave%", String.valueOf(currentWave))));
            });

            // Mensaje adicional con ubicación del núcleo
            if (nextLocation != null) {
                player.sendMessage(ColorUtil.add("&6⚡ El núcleo aparecerá en: &e" +
                        nextLocation.getBlockX() + ", " + nextLocation.getBlockY() + ", " + nextLocation.getBlockZ()));
            }
        }

        // Task de transición de 5 segundos
        waveTransitionTask = new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (countdown <= 0) {
                    RaidEvent.getInstance().getLogger().info("Transition countdown finished, starting wave " + currentWave + locationInfo);
                    waveTransition = false;

                    // Usar la ubicación específica para la oleada o fallback
                    Location location = nextLocation;
                    if (location == null) {
                        location = new Location(
                                Bukkit.getWorld(RaidEvent.getInstance().getSettings().getString("BLOCK.LOCATION.WORLD")),
                                RaidEvent.getInstance().getSettings().getInt("BLOCK.LOCATION.X"),
                                RaidEvent.getInstance().getSettings().getInt("BLOCK.LOCATION.Y"),
                                RaidEvent.getInstance().getSettings().getInt("BLOCK.LOCATION.Z")
                        );
                    }
                    startWave(location);
                    this.cancel();
                } else {
                    RaidEvent.getInstance().getLogger().info("Wave transition countdown: " + countdown);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(ColorUtil.add(RaidEvent.getInstance().getLang().getString("WAVE-COUNTDOWN.MESSAGE")
                                .replace("%countdown%", String.valueOf(countdown))
                                .replace("%next_wave%", String.valueOf(currentWave))));
                    }
                    countdown--;
                }
            }
        };
        waveTransitionTask.runTaskTimer(RaidEvent.getInstance(), 0, 20);
    }

    private void completeEvent() {
        RaidEvent.getInstance().getLogger().info("=== EVENT COMPLETED - SHOWING VICTORY ===");

        eventActive = false;

        // Mostrar top 3 jugadores
        showTopPlayers();

        // Mensaje de victoria
        for (Player player : Bukkit.getOnlinePlayers()) {
            TitleUtil.sendTitle(player,
                    RaidEvent.getInstance().getLang().getString("WIN-EVENT.TITLE"),
                    RaidEvent.getInstance().getLang().getString("WIN-EVENT.SUBTITLE"),
                    20, 40, 20);
            RaidEvent.getInstance().getLang().getStringList("WIN-EVENT.MESSAGE").forEach(message -> {
                player.sendMessage(ColorUtil.add(message));
            });
        }

        // Usar stopEvent para limpieza completa
        stopEvent();
    }

    public void stopEvent() {
        RaidEvent.getInstance().getLogger().info("=== STOPPING RAID EVENT - FULL CLEANUP ===");

        eventActive = false;
        waveTransition = false;
        currentWave = 1;

        // 1. LIMPIAR DISPLAYS Y BLOQUES
        cleanupDisplays();

        // 2. LIMPIAR BOSSBAR COMPLETAMENTE
        if (bossBar != null) {
            RaidEvent.getInstance().getLogger().info("Removing BossBar from all players");
            bossBar.removeAll();
            bossBar = null;
        }

        // 3. CANCELAR TODAS LAS TAREAS
        if (mobSpawnerTask != null && !mobSpawnerTask.isCancelled()) {
            mobSpawnerTask.cancel();
            mobSpawnerTask = null;
            RaidEvent.getInstance().getLogger().info("Cancelled mob spawner task");
        }

        if (waveTransitionTask != null && !waveTransitionTask.isCancelled()) {
            waveTransitionTask.cancel();
            waveTransitionTask = null;
            RaidEvent.getInstance().getLogger().info("Cancelled wave transition task");
        }

        // 4. REMOVER TODOS LOS MOBS
        RaidEvent.getInstance().getMobManager().removeSpawnedMobs();

        // 5. DETENER TODAS LAS PARTÍCULAS
        RaidEvent.getInstance().getSpawnLocationManager().stopParticleEffects();
        RaidEvent.getInstance().getCoreLocationManager().stopParticleEffects();
        RaidEvent.getInstance().getRegionManager().stopParticleEffects();
        RaidEvent.getInstance().getLogger().info("Stopped all particle effects");

        // 6. LIMPIAR DATOS DE JUGADORES
        playerDamage.clear();

        // 7. MENSAJE A JUGADORES (solo si no es victoria)
        if (eventActive || waveTransition) { // Si se detuvo manualmente
            for (Player player : Bukkit.getOnlinePlayers()) {
                TitleUtil.sendTitle(player,
                        RaidEvent.getInstance().getLang().getString("STOP-EVENT.TITLE"),
                        RaidEvent.getInstance().getLang().getString("STOP-EVENT.SUBTITLE"),
                        20, 40, 20);
                RaidEvent.getInstance().getLang().getStringList("STOP-EVENT.MESSAGE").forEach(message -> {
                    player.sendMessage(ColorUtil.add(message));
                });
            }
        }

        RaidEvent.getInstance().getLogger().info("=== RAID EVENT FULLY STOPPED AND CLEANED ===");
    }

    public void damageBlock(Player player) {
        if (!eventActive || waveTransition || raidBlock == null || blockHealth <= 0) {
            return;
        }

        blockHealth--;

        // Registrar daño del jugador
        playerDamage.put(player.getUniqueId(), playerDamage.getOrDefault(player.getUniqueId(), 0) + 1);

        String wavePath = "WAVES.WAVE-" + currentWave;
        String particleName = RaidEvent.getInstance().getSettings().getString(wavePath + ".PARTICLE", "HEART");
        Particle particle = Particle.valueOf(particleName);
        raidBlock.getWorld().spawnParticle(particle, raidBlock.getLocation().add(0.5, 0.5, 0.5), 10, 0.5, 0.5, 0.5);

        String soundName = RaidEvent.getInstance().getSettings().getString(wavePath + ".SOUND", "ENTITY_PLAYER_HURT");
        SoundUtil.playEventSound(raidBlock.getLocation(), soundName);

        // Actualizar bossbar
        if (bossBar != null) {
            double progress = (double) blockHealth / maxBlockHealth;
            bossBar.setProgress(Math.max(0, progress));
            bossBar.setTitle(ColorUtil.add(RaidEvent.getInstance().getSettings().getString(wavePath + ".BOSSBAR.TITLE", "&6Wave %wave% &7- &c%block_health%")
                    .replace("%block_health%", String.valueOf(blockHealth))
                    .replace("%wave%", String.valueOf(currentWave))));

            // Actualizar color de bossbar dinámicamente
            String colorName = RaidEvent.getInstance().getSettings().getString(wavePath + ".BOSSBAR.COLOR", "RED");
            bossBar.setColor(BossBarUtil.getBarColor(colorName));
        }

        if (blockHealth <= 0) {
            completeWave();
        }
    }

    public void spawnWaveMobs(Location location) {
        RaidEvent.getInstance().getMobManager().spawnWaveMobs(location, currentWave);
    }

    public boolean isRaidBlock(Block block) {
        return block != null && block.equals(raidBlock);
    }

    private void startMobSpawnerTask(Location location) {
        // Cancelar task anterior si existe
        if (mobSpawnerTask != null && !mobSpawnerTask.isCancelled()) {
            mobSpawnerTask.cancel();
            RaidEvent.getInstance().getLogger().info("Cancelled previous mob spawner task");
        }

        mobSpawnerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!eventActive || waveTransition) {
                    RaidEvent.getInstance().getLogger().info("Mob spawner task cancelled - Event: " + eventActive + ", Transition: " + waveTransition);
                    this.cancel();
                    return;
                }
                RaidEvent.getInstance().getLogger().info("Spawning mobs for wave " + currentWave);
                spawnWaveMobs(location);
            }
        };
        mobSpawnerTask.runTaskTimer(RaidEvent.getInstance(), 200, 200); // 10 segundos
        RaidEvent.getInstance().getLogger().info("Started mob spawner task for wave " + currentWave);
    }

    private void setupRaidBlock(Location location, String wavePath) {
        raidBlock = location.getBlock();
        String blockType = RaidEvent.getInstance().getSettings().getString(wavePath + ".TYPE", "GOLD_BLOCK");
        raidBlock.setType(Material.valueOf(blockType));
        maxBlockHealth = RaidEvent.getInstance().getSettings().getInt(wavePath + ".HEALTH", 20);
        blockHealth = maxBlockHealth;
    }

    private void setupBlockDisplay(Location location, String wavePath) {
        // Centrar exactamente en el bloque físico
        Location displayLocation = location.clone().add(0, 0.0, 0);
        blockDisplay = displayLocation.getWorld().spawn(displayLocation, BlockDisplay.class);

        blockDisplay.setInvisible(true);
        blockDisplay.setInvulnerable(true);
        blockDisplay.setGlowing(true);

        try {
            String glowColorName = RaidEvent.getInstance().getSettings().getString(wavePath + ".GLOW-COLOR", "RED");
            Color glowColor = (Color) Color.class.getField(glowColorName.toUpperCase()).get(null);
            blockDisplay.setGlowColorOverride(glowColor);
        } catch (Exception e) {
            blockDisplay.setGlowColorOverride(Color.RED);
        }

        blockDisplay.setCustomNameVisible(false);
        String blockType = RaidEvent.getInstance().getSettings().getString(wavePath + ".TYPE", "GOLD_BLOCK");
        blockDisplay.setBlock(Bukkit.createBlockData(Material.valueOf(blockType)));

        // Configurar transformación para que coincida exactamente con el bloque
        Transformation transformation = blockDisplay.getTransformation();
        transformation.getScale().set(1.0f); // Tamaño completo, no 0.98f
        transformation.getTranslation().set(0f, 0f, 0f); // Sin desplazamiento
        blockDisplay.setTransformation(transformation);

        RaidEvent.getInstance().getLogger().info("BlockDisplay positioned at: " +
                displayLocation.getX() + ", " + displayLocation.getY() + ", " + displayLocation.getZ());
    }

    private void setupTextDisplay(Location location, String wavePath) {
        Location textLocation = location.clone().add(0.5, 1, 0.5);
        textDisplay = textLocation.getWorld().spawn(textLocation, TextDisplay.class);
        textDisplay.setCustomNameVisible(true);
        String blockName = RaidEvent.getInstance().getSettings().getString(wavePath + ".NAME", "&6&lWAVE " + currentWave);
        textDisplay.setCustomName(ColorUtil.add(blockName.replace("%wave%", String.valueOf(currentWave))));
    }

    private void setupBossBar(String wavePath) {
        // Asegurarse de que no hay BossBar anterior
        if (bossBar != null) {
            RaidEvent.getInstance().getLogger().info("Removing existing BossBar before creating new one");
            bossBar.removeAll();
            bossBar = null;
        }

        String title = RaidEvent.getInstance().getSettings().getString(wavePath + ".BOSSBAR.TITLE", "&6Wave %wave% &7- &c%block_health%")
                .replace("%block_health%", String.valueOf(blockHealth))
                .replace("%wave%", String.valueOf(currentWave));
        String colorName = RaidEvent.getInstance().getSettings().getString(wavePath + ".BOSSBAR.COLOR", "RED");
        String styleName = RaidEvent.getInstance().getSettings().getString(wavePath + ".BOSSBAR.STYLE", "SOLID");

        // Usar la utilidad para obtener colores y estilos válidos
        BarColor color = BossBarUtil.getBarColor(colorName);
        BarStyle style = BossBarUtil.getBarStyle(styleName);

        bossBar = Bukkit.createBossBar(
                ColorUtil.add(title),
                color,
                style
        );
        bossBar.setProgress(1.0);

        RaidEvent.getInstance().getLogger().info("Created NEW BossBar for wave " + currentWave +
                " with color " + color + " and style " + style);
    }

    private void transitionBossBar(String newWavePath) {
        RaidEvent.getInstance().getLogger().info("Transitioning BossBar from wave " + (currentWave - 1) + " to wave " + currentWave);

        // Guardar lista de jugadores de la BossBar anterior
        Set<Player> previousPlayers = new HashSet<>();
        if (bossBar != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (bossBar.getPlayers().contains(player)) {
                    previousPlayers.add(player);
                }
            }

            // Remover BossBar anterior completamente
            bossBar.removeAll();
            bossBar = null;
            RaidEvent.getInstance().getLogger().info("Removed previous BossBar from " + previousPlayers.size() + " players");
        }

        // Crear nueva BossBar
        setupBossBar(newWavePath);

        // Agregar jugadores a la nueva BossBar
        if (bossBar != null) {
            for (Player player : previousPlayers.isEmpty() ? Bukkit.getOnlinePlayers() : previousPlayers) {
                bossBar.addPlayer(player);
            }
            RaidEvent.getInstance().getLogger().info("Added players to new BossBar for wave " + currentWave);
        }
    }

    private void cleanupDisplays() {
        RaidEvent.getInstance().getLogger().info("=== CLEANING UP ALL DISPLAYS AND ENTITIES ===");

        // Limpiar bloque del núcleo
        if (raidBlock != null) {
            raidBlock.setType(Material.AIR);
            raidBlock = null;
            RaidEvent.getInstance().getLogger().info("Removed raid block");
        }

        // Remover display del bloque
        if (blockDisplay != null) {
            blockDisplay.remove();
            blockDisplay = null;
            RaidEvent.getInstance().getLogger().info("Removed block display");
        }

        // Remover display de texto
        if (textDisplay != null) {
            textDisplay.remove();
            textDisplay = null;
            RaidEvent.getInstance().getLogger().info("Removed text display");
        }

        // Limpiar BossBar (doble verificación)
        if (bossBar != null) {
            RaidEvent.getInstance().getLogger().info("Emergency cleanup: Removing BossBar from cleanupDisplays");
            bossBar.removeAll();
            bossBar = null;
        }

        RaidEvent.getInstance().getLogger().info("=== ALL DISPLAYS CLEANED ===");
    }

    private void announceWaveStart() {
        // Obtener ubicación actual del núcleo para mostrar en el mensaje
        Location coreLocation = RaidEvent.getInstance().getCoreLocationManager().getCurrentLocation();

        for (Player player : Bukkit.getOnlinePlayers()) {
            RaidEvent.getInstance().getLang().getStringList("WAVE-START.MESSAGE").forEach(message -> {
                player.sendMessage(ColorUtil.add(message
                        .replace("%wave%", String.valueOf(currentWave))
                        .replace("%max_waves%", String.valueOf(maxWaves))));
            });

            // Mensaje adicional con ubicación del núcleo
            if (coreLocation != null) {
                player.sendMessage(ColorUtil.add("&6⚡ Núcleo ubicado en: &e" +
                        coreLocation.getBlockX() + ", " + coreLocation.getBlockY() + ", " + coreLocation.getBlockZ()));
            }
        }
    }

    private void announceWaveStart(Location location) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            RaidEvent.getInstance().getLang().getStringList("WAVE-START.MESSAGE").forEach(message -> {
                player.sendMessage(ColorUtil.add(message
                        .replace("%wave%", String.valueOf(currentWave))
                        .replace("%max_waves%", String.valueOf(maxWaves))));
            });

            // Mensaje con ubicación específica del núcleo
            player.sendMessage(ColorUtil.add("&6⚡ Núcleo ubicado en: &e" +
                    location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ()));
        }
    }

    private void announceWaveComplete() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            RaidEvent.getInstance().getLang().getStringList("WAVE-COMPLETE.MESSAGE").forEach(message -> {
                player.sendMessage(ColorUtil.add(message
                        .replace("%wave%", String.valueOf(currentWave))
                        .replace("%max_waves%", String.valueOf(maxWaves))));
            });
        }
    }

    private void showTopPlayers() {
        List<Map.Entry<UUID, Integer>> sortedPlayers = playerDamage.entrySet()
                .stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(3)
                .toList();

        for (Player player : Bukkit.getOnlinePlayers()) {
            RaidEvent.getInstance().getLang().getStringList("TOP-PLAYERS.MESSAGE").forEach(message -> {
                player.sendMessage(ColorUtil.add(message));
            });

            for (int i = 0; i < sortedPlayers.size(); i++) {
                Map.Entry<UUID, Integer> entry = sortedPlayers.get(i);
                Player topPlayer = Bukkit.getPlayer(entry.getKey());
                if (topPlayer != null) {
                    String topMessage = RaidEvent.getInstance().getLang().getString("TOP-PLAYERS.POSITION")
                            .replace("%position%", String.valueOf(i + 1))
                            .replace("%player%", topPlayer.getName())
                            .replace("%damage%", String.valueOf(entry.getValue()));
                    player.sendMessage(ColorUtil.add(topMessage));
                }
            }
        }
    }

    private int getMaxWavesFromConfig() {
        int waves = 1;
        while (RaidEvent.getInstance().getSettings().contains("WAVES.WAVE-" + waves)) {
            waves++;
        }
        return waves - 1;
    }

    // Getters
    public int getCurrentWave() {
        return currentWave;
    }

    public boolean isEventActive() {
        return eventActive;
    }

    public boolean isWaveTransition() {
        return waveTransition;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public int getCurrentWaveMobCount() {
        if (!eventActive) return 0;
        return RaidEvent.getInstance().getMobManager().getWaveMobCount(currentWave);
    }

    public int getTotalMobCount() {
        return RaidEvent.getInstance().getMobManager().getSpawnedMobCount();
    }

    public void forceCompleteWave() {
        if (eventActive && !waveTransition) {
            blockHealth = 0;
            completeWave();
        }
    }

    public void forceWave(int wave) {
        if (wave < 1 || wave > maxWaves) return;

        if (eventActive) {
            // Limpiar oleada actual
            if (mobSpawnerTask != null) {
                mobSpawnerTask.cancel();
                mobSpawnerTask = null;
            }
            RaidEvent.getInstance().getMobManager().removeWaveMobs(currentWave);
            cleanupDisplays();
        }

        currentWave = wave;
        eventActive = true;
        waveTransition = false;

        // Usar la ubicación específica para la oleada forzada
        Location location = RaidEvent.getInstance().getCoreLocationManager().getLocationForWave(wave);
        if (location == null) {
            location = new Location(
                    Bukkit.getWorld(RaidEvent.getInstance().getSettings().getString("BLOCK.LOCATION.WORLD")),
                    RaidEvent.getInstance().getSettings().getInt("BLOCK.LOCATION.X"),
                    RaidEvent.getInstance().getSettings().getInt("BLOCK.LOCATION.Y"),
                    RaidEvent.getInstance().getSettings().getInt("BLOCK.LOCATION.Z")
            );
        }

        startWave(location);
    }
}