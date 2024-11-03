package plugin.myclass.raidEvent.utils;

import org.bukkit.entity.Player;

public class TitleUtil {

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {

        player.sendTitle(ColorUtil.add(title), ColorUtil.add(subtitle), fadeIn, stay, fadeOut);
    }
}