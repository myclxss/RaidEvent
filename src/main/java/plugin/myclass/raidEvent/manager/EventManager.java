package plugin.myclass.raidEvent.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import plugin.myclass.raidEvent.RaidEvent;
import plugin.myclass.raidEvent.utils.ColorUtil;
import plugin.myclass.raidEvent.utils.TitleUtil;

public class EventManager {

    private Block raidBlock;
    private int blockHealth = RaidEvent.getInstance().getSettings().getInt("BLOCK.HEALTH");
    private BossBar bossBar;

    public void startEvent(Location location) {
        raidBlock = location.getBlock();
        raidBlock.setType(Material.valueOf(RaidEvent.getInstance().getSettings().getString("BLOCK.TYPE")));
        blockHealth = RaidEvent.getInstance().getSettings().getInt("BLOCK.HEALTH");

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
    }

    public void stopEvent() {
        if (raidBlock != null) {
            raidBlock.setType(Material.AIR);
            raidBlock = null;

            if (bossBar != null) {
                bossBar.removeAll();
                bossBar = null;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                TitleUtil.sendTitle(player, RaidEvent.getInstance().getLang().getString("STOP-EVENT.TITLE"), RaidEvent.getInstance().getLang().getString("STOP-EVENT.SUBTITLE"), 20, 40, 20);
                RaidEvent.getInstance().getLang().getStringList("STOP-EVENT.MESSAGE").forEach(message -> {
                    player.sendMessage(ColorUtil.add(message));
                });
            }
        }
    }

    public void damageBlock(Player player) {
        if (raidBlock != null && blockHealth > 0) {
            blockHealth--;
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

    public boolean isRaidBlock(Block block) {
        return block.equals(raidBlock);
    }
}