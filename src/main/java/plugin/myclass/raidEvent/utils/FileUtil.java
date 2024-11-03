package plugin.myclass.raidEvent.utils;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtil extends YamlConfiguration {

    @Getter
    private final File file;
    private boolean higherVersion;

    @SneakyThrows
    public FileUtil(JavaPlugin plugin, String name) {
        this.file = new File(plugin.getDataFolder(), name + ".yml");

        if (!this.file.exists()) {
            plugin.saveResource(name + ".yml", false);
        }

        this.load(this.file);
    }

    @SneakyThrows
    public FileUtil(JavaPlugin plugin, String name, boolean ignored) {
        this.file = new File(plugin.getDataFolder(), name + ".yml");

        if (!this.file.exists()) {
            plugin.saveResource(name + ".yml", false);
        }

        this.load(this.file);
    }

    public void save() {
        try {
            this.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getInt(String path) {
        return super.getInt(path, 0);
    }

    @Override
    public double getDouble(String path) {
        return super.getDouble(path, 0.0);
    }

    @Override
    public boolean getBoolean(String path) {
        return super.getBoolean(path, false);
    }

    public String getString(String path, boolean ignored) {
        return super.getString(path, null);
    }

    public String getUnColoredString(String path) {
        return super.getString(path, null);
    }

    public String getUnColoredString(String path, String def) {
        return super.getString(path, def);
    }

    @Override
    public String getString(String path) {
        if (higherVersion) {
            return super.getString(path, path);
        }

        return super.getString(path, "&cMissing node: &f" + path);
    }

    @Override
    public List<String> getStringList(String path) {
        if (higherVersion) {
            return new ArrayList<>(super.getStringList(path));
        }

        return super.getStringList(path);
    }

    public List<String> getStringList(String path, boolean ignored) {
        if (!super.contains(path)) return null;

        if (higherVersion) {
            return super.getStringList(path).stream().map(s -> "<red>Missing node: <white>" + s).collect(Collectors.toList());
        }

        return super.getStringList(path);
    }

    public List<String> getStringList(String path, List<String> def) {
        if (!super.contains(path)) return def;

        if (higherVersion) {
            return super.getStringList(path).stream().map(s -> "<red>Missing node: <white>" + s).collect(Collectors.toList());
        }

        return super.getStringList(path);
    }

    public void reload() {
        try {
            this.load(this.file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}