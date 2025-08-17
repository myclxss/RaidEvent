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
            player.sendMessage("Usage: /raid <setup|start|stop|spawn-locations>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setup" -> {
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
                        case "all" -> {
                            player.getInventory().setItem(0, RaidEvent.getInstance().getItemManager().getBlockSetup());
                            player.getInventory().setItem(1, RaidEvent.getInstance().getItemManager().getSpawnLocationAdder());
                            player.getInventory().setItem(2, RaidEvent.getInstance().getItemManager().getSpawnLocationViewer());
                            player.sendMessage(ColorUtil.add("&aAll setup tools given!"));
                        }
                        default -> player.sendMessage(ColorUtil.add("&cUsage: /raid setup <block|spawn-add|spawn-view|all>"));
                    }
                } else {
                    player.getInventory().setItem(0, RaidEvent.getInstance().getItemManager().getBlockSetup());
                    player.sendMessage(ColorUtil.add("&aBlock setup tool given!"));
                }
                return true;
            }
            case "start" -> {
                Location location = new Location(Bukkit.getWorld(RaidEvent.getInstance().getSettings().getString("BLOCK.LOCATION.WORLD")), RaidEvent.getInstance().getSettings().getInt("BLOCK.LOCATION.X"), RaidEvent.getInstance().getSettings().getInt("BLOCK.LOCATION.Y"), RaidEvent.getInstance().getSettings().getInt("BLOCK.LOCATION.Z"));
                RaidEvent.getInstance().getEventManager().startEvent(location);
                return true;
            }
            case "stop" -> {
                RaidEvent.getInstance().getEventManager().stopEvent();
                return true;
            }
            case "spawn-locations" -> {
                if (args.length >= 2) {
                    switch (args[1].toLowerCase()) {
                        case "list" -> {
                            sendSpawnLocationsList(player);
                        }
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
                return true;
            }
            case "reload" -> {
                RaidEvent.getInstance().getSettings().reload();
                RaidEvent.getInstance().getLang().reload();
                RaidEvent.getInstance().getMobs().reload();
                player.sendMessage("Config reloaded");
                return true;
            }
            default -> {
                player.sendMessage("Usage: /raid <setup|start|stop|spawn-locations>");
                return true;
            }
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
}