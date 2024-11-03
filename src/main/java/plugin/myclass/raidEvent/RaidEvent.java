package plugin.myclass.raidEvent;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.myclass.raidEvent.command.RaidCommand;
import plugin.myclass.raidEvent.listener.BreakListener;
import plugin.myclass.raidEvent.manager.EventManager;
import plugin.myclass.raidEvent.utils.FileUtil;

public final class RaidEvent extends JavaPlugin {

    @Getter
    private static RaidEvent instance;

    @Getter
    EventManager eventManager;
    @Getter
    FileUtil settings;
    @Getter
    FileUtil lang;
    @Getter
    FileUtil mobs;

    @Override
    public void onEnable() {
        instance = this;

        settings = new FileUtil(this, "settings");
        lang = new FileUtil(this, "lang");
        mobs = new FileUtil(this, "mobs");

        eventManager = new EventManager();

        getCommand("raid").setExecutor(new RaidCommand());

        Bukkit.getPluginManager().registerEvents(new BreakListener(), this);
    }

    @Override
    public void onDisable() {
    }
}