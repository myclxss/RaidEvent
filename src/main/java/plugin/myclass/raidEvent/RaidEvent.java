package plugin.myclass.raidEvent;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.myclass.raidEvent.command.RaidCommand;
import plugin.myclass.raidEvent.listener.BreakListener;
import plugin.myclass.raidEvent.listener.ItemListener;
import plugin.myclass.raidEvent.listener.MobListener;
import plugin.myclass.raidEvent.listener.DeathListener;
import plugin.myclass.raidEvent.manager.EventManager;
import plugin.myclass.raidEvent.manager.ItemManager;
import plugin.myclass.raidEvent.manager.MobManager;
import plugin.myclass.raidEvent.manager.SpawnLocationManager;
import plugin.myclass.raidEvent.manager.CoreLocationManager;
import plugin.myclass.raidEvent.manager.RegionManager;
import plugin.myclass.raidEvent.utils.FileUtil;

public final class RaidEvent extends JavaPlugin {

    @Getter
    private static RaidEvent instance;

    @Getter
    FileUtil settings;
    @Getter
    FileUtil lang;
    @Getter
    FileUtil mobs;

    @Getter
    EventManager eventManager;
    @Getter
    ItemManager itemManager;
    @Getter
    MobManager mobManager;
    @Getter
    SpawnLocationManager spawnLocationManager;
    @Getter
    CoreLocationManager coreLocationManager;
    @Getter
    RegionManager regionManager;

    @Override
    public void onEnable() {
        instance = this;

        settings = new FileUtil(this, "settings");
        lang = new FileUtil(this, "lang");
        mobs = new FileUtil(this, "mobs");

        eventManager = new EventManager();
        itemManager = new ItemManager();
        mobManager = new MobManager();
        spawnLocationManager = new SpawnLocationManager();
        coreLocationManager = new CoreLocationManager();
        regionManager = new RegionManager();

        // Cargar spawn locations, core locations y región
        spawnLocationManager.loadSpawnLocations();
        coreLocationManager.loadCoreLocations();
        regionManager.loadRegionData();

        getCommand("raid").setExecutor(new RaidCommand());

        Bukkit.getPluginManager().registerEvents(new BreakListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemListener(), this);
        Bukkit.getPluginManager().registerEvents(new MobListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("=== RAIDEVENT PLUGIN SHUTTING DOWN ===");

        // Detener evento si está activo
        if (eventManager != null && eventManager.isEventActive()) {
            eventManager.stopEvent();
        }

        // Detener efectos de partículas al desactivar
        if (spawnLocationManager != null) {
            spawnLocationManager.stopParticleEffects();
            getLogger().info("Stopped spawn location particles");
        }

        if (coreLocationManager != null) {
            coreLocationManager.stopParticleEffects();
            getLogger().info("Stopped core location particles");
        }

        // Limpiar todos los mobs spawneados
        if (mobManager != null) {
            mobManager.removeSpawnedMobs();
            getLogger().info("Removed all spawned mobs");
        }

        // Detener monitoreo de región
        if (regionManager != null) {
            regionManager.stopParticleEffects();
            getLogger().info("Stopped region particle effects");
        }

        getLogger().info("=== RAIDEVENT PLUGIN FULLY DISABLED ===");
    }
}