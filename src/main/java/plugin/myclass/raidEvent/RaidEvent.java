package plugin.myclass.raidEvent;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.myclass.raidEvent.command.RaidCommand;
import plugin.myclass.raidEvent.listener.BreakListener;
import plugin.myclass.raidEvent.listener.ItemListener;
import plugin.myclass.raidEvent.listener.MobListener;
import plugin.myclass.raidEvent.manager.EventManager;
import plugin.myclass.raidEvent.manager.ItemManager;
import plugin.myclass.raidEvent.manager.MobManager;
import plugin.myclass.raidEvent.manager.SpawnLocationManager;
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

        // Cargar spawn locations
        spawnLocationManager.loadSpawnLocations();

        getCommand("raid").setExecutor(new RaidCommand());

        Bukkit.getPluginManager().registerEvents(new BreakListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemListener(), this);
        Bukkit.getPluginManager().registerEvents(new MobListener(), this);
    }

    @Override
    public void onDisable() {
        // Detener efectos de partículas al desactivar
        if (spawnLocationManager != null) {
            spawnLocationManager.stopParticleEffects();
        }
    }
}