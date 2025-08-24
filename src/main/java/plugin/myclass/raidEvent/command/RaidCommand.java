package plugin.myclass.raidEvent.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plugin.myclass.raidEvent.RaidEvent;
import plugin.myclass.raidEvent.utils.ColorUtil;

import java.util.List;

public class RaidCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setup" -> {
                handleSetupCommand(player, args);
                return true;
            }
            case "start" -> {
                Location location = RaidEvent.getInstance().getCoreLocationManager().getLocationForWave(1);
                RaidEvent.getInstance().getEventManager().startEvent(location);
                return true;
            }
            case "stop" -> {
                RaidEvent.getInstance().getEventManager().stopEvent();
                return true;
            }
            case "spawn-locations" -> {
                handleSpawnLocationsCommand(player, args);
                return true;
            }
            case "caore-locations" -> {
                handleCoreLocationsCommand(player, args);
                return true;
            }
            case "reload" -> {
                RaidEvent.getInstance().getSettings().reload();
                RaidEvent.getInstance().getLang().reload();
                RaidEvent.getInstance().getMobs().reload();
                // Recargar ubicaciones
                RaidEvent.getInstance().getSpawnLocationManager().loadSpawnLocations();
                RaidEvent.getInstance().getCoreLocationManager().loadCoreLocations();
                player.sendMessage(ColorUtil.add("&aConfig reloaded"));
                return true;
            }
            default -> {
                sendHelpMessage(player);
                return true;
            }
        }
    }

    private void handleSetupCommand(Player player, String[] args) {
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "block" -> {
                    player.getInventory().setItem(0, RaidEvent.getInstance().getItemManager().getBlockSetup());
                    player.sendMessage(ColorUtil.add("&aBlock setup tool given!"));
                }
                case "spawn-add" -> {
                    player.getInventory().setItem(1, RaidEvent.getInstance().getItemManager().getSpawnLocationAdder());
                    player.sendMessage(ColorUtil.add("&aSpawn location adder given!"));
                }
                case "spawn-view" -> {
                    player.getInventory().setItem(2, RaidEvent.getInstance().getItemManager().getSpawnLocationViewer());
                    player.sendMessage(ColorUtil.add("&aSpawn location viewer given!"));
                }
                case "core-add" -> {
                    player.getInventory().setItem(3, RaidEvent.getInstance().getItemManager().getCoreLocationAdder());
                    player.sendMessage(ColorUtil.add("&6Core location adder given!"));
                }
                case "core-view" -> {
                    player.getInventory().setItem(4, RaidEvent.getInstance().getItemManager().getCoreLocationViewer());
                    player.sendMessage(ColorUtil.add("&eCore location viewer given!"));
                }
                case "all" -> {
                    player.getInventory().setItem(1, RaidEvent.getInstance().getItemManager().getSpawnLocationAdder());
                    player.getInventory().setItem(2, RaidEvent.getInstance().getItemManager().getSpawnLocationViewer());
                    player.getInventory().setItem(3, RaidEvent.getInstance().getItemManager().getCoreLocationAdder());
                    player.getInventory().setItem(4, RaidEvent.getInstance().getItemManager().getCoreLocationViewer());
                    player.getInventory().setItem(5, RaidEvent.getInstance().getItemManager().getRegionPos1Tool());
                    player.getInventory().setItem(6, RaidEvent.getInstance().getItemManager().getRegionPos2Tool());
                    player.getInventory().setItem(7, RaidEvent.getInstance().getItemManager().getRegionViewer());
                    player.sendMessage(ColorUtil.add("&aAll setup tools given!"));
                }
                default -> player.sendMessage(ColorUtil.add("&cUsage: /raid setup <block|spawn-add|spawn-view|core-add|core-view|all>"));
            }
        } else {
            player.getInventory().setItem(0, RaidEvent.getInstance().getItemManager().getBlockSetup());
            player.sendMessage(ColorUtil.add("&aBlock setup tool given!"));
        }
    }

    private void handleSpawnLocationsCommand(Player player, String[] args) {
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "list" -> sendSpawnLocationsList(player);
                case "clear" -> {
                    RaidEvent.getInstance().getSpawnLocationManager().clearAllSpawnLocations();
                    player.sendMessage(ColorUtil.add("&cAll spawn locations cleared!"));
                }
                case "show" -> {
                    if (RaidEvent.getInstance().getSpawnLocationManager().hasSpawnLocations()) {
                        RaidEvent.getInstance().getSpawnLocationManager().startParticleEffects();
                        player.sendMessage(ColorUtil.add("&aSoul circles enabled!"));
                    } else {
                        player.sendMessage(ColorUtil.add("&cNo spawn locations configured!"));
                    }
                }
                case "hide" -> {
                    RaidEvent.getInstance().getSpawnLocationManager().stopParticleEffects();
                    player.sendMessage(ColorUtil.add("&cSoul circles disabled!"));
                }
                default -> player.sendMessage(ColorUtil.add("&cUsage: /raid spawn-locations <list|clear|show|hide>"));
            }
        } else {
            sendSpawnLocationsList(player);
        }
    }

    private void handleCoreLocationsCommand(Player player, String[] args) {
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "list" -> sendCoreLocationsList(player);
                case "clear" -> {
                    RaidEvent.getInstance().getCoreLocationManager().clearAllCoreLocations();
                    player.sendMessage(ColorUtil.add("&cAll core locations cleared!"));
                }
                case "show" -> {
                    if (RaidEvent.getInstance().getCoreLocationManager().hasCoreLocations()) {
                        RaidEvent.getInstance().getCoreLocationManager().startParticleEffects();
                        player.sendMessage(ColorUtil.add("&eCore cubes enabled!"));
                    } else {
                        player.sendMessage(ColorUtil.add("&cNo core locations configured!"));
                    }
                }
                case "hide" -> {
                    RaidEvent.getInstance().getCoreLocationManager().stopParticleEffects();
                    player.sendMessage(ColorUtil.add("&cCore cubes disabled!"));
                }
                default -> player.sendMessage(ColorUtil.add("&cUsage: /raid core-locations <list|clear|show|hide>"));
            }
        } else {
            sendCoreLocationsList(player);
        }
    }

    private void sendSpawnLocationsList(Player player) {
        int count = RaidEvent.getInstance().getSpawnLocationManager().getSpawnLocationCount();

        player.sendMessage(ColorUtil.add("&6&lSPAWN LOCATIONS"));
        player.sendMessage(ColorUtil.add("&7Total: &c" + count));

        if (count == 0) {
            player.sendMessage(ColorUtil.add("&cNo spawn locations configured"));
            player.sendMessage(ColorUtil.add("&7Use /raid setup spawn-add to add locations"));
        } else {
            List<Location> locations = RaidEvent.getInstance().getSpawnLocationManager().getAllSpawnLocations();
            for (int i = 0; i < locations.size(); i++) {
                Location loc = locations.get(i);
                player.sendMessage(ColorUtil.add("&e" + (i + 1) + ". &7" + loc.getWorld().getName() +
                        " X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ()));
            }
        }
    }

    private void sendCoreLocationsList(Player player) {
        int count = RaidEvent.getInstance().getCoreLocationManager().getCoreLocationCount();

        player.sendMessage(ColorUtil.add("&6&lCORE LOCATIONS"));
        player.sendMessage(ColorUtil.add("&7Total: &e" + count));

        if (count == 0) {
            player.sendMessage(ColorUtil.add("&cNo core locations configured"));
            player.sendMessage(ColorUtil.add("&7Use /raid setup core-add to add locations"));
        } else {
            List<Location> locations = RaidEvent.getInstance().getCoreLocationManager().getAllCoreLocations();
            for (int i = 0; i < locations.size(); i++) {
                Location loc = locations.get(i);
                player.sendMessage(ColorUtil.add("&6" + (i + 1) + ". &7" + loc.getWorld().getName() +
                        " X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ()));
            }
            player.sendMessage(ColorUtil.add("&7Núcleos rotarán entre estas ubicaciones por oleada"));
        }
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ColorUtil.add("&6&l=== RAID EVENT COMMANDS ==="));
        player.sendMessage(ColorUtil.add("&e/raid setup &7- Get setup tools"));
        player.sendMessage(ColorUtil.add("&e/raid start &7- Start the event"));
        player.sendMessage(ColorUtil.add("&e/raid stop &7- Stop the event"));
        player.sendMessage(ColorUtil.add("&e/raid spawn-locations &7- Manage spawn locations"));
        player.sendMessage(ColorUtil.add("&e/raid core-locations &7- Manage core locations"));
        player.sendMessage(ColorUtil.add("&e/raid reload &7- Reload config"));
        player.sendMessage(ColorUtil.add("&6&l=== SETUP TOOLS ==="));
        player.sendMessage(ColorUtil.add("&e/raid setup all &7- Get all tools"));
        player.sendMessage(ColorUtil.add("&e/raid setup core-add &7- Add core locations"));
        player.sendMessage(ColorUtil.add("&e/raid setup core-view &7- View core locations"));
    }
}