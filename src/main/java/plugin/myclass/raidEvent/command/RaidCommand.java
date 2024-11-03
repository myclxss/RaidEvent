package plugin.myclass.raidEvent.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plugin.myclass.raidEvent.RaidEvent;

public class RaidCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("Usage: /raid <start|stop>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "test" -> {
                player.sendMessage("Test command");
                return true;
            }
            case "start" -> {
                Location location = new Location(Bukkit.getWorld("world"), 0, 100, 0);
                RaidEvent.getInstance().getEventManager().startEvent(location);
                return true;
            }
            case "stop" -> {
                RaidEvent.getInstance().getEventManager().stopEvent();
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
                player.sendMessage("Usage: /raid <start|stop>");
                return true;
            }
        }
    }
}
