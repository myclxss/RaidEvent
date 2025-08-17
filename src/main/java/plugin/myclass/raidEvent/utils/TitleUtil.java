package plugin.myclass.raidEvent.utils;

import org.bukkit.entity.Player;

public class TitleUtil {

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(ColorUtil.add(title), ColorUtil.add(subtitle), fadeIn, stay, fadeOut);
    }

    public static void sendTitleWithSound(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut, String soundName) {
        player.sendTitle(ColorUtil.add(title), ColorUtil.add(subtitle), fadeIn, stay, fadeOut);
        if (soundName != null && !soundName.isEmpty()) {
            SoundUtil.playEventSound(player, soundName);
        }
    }

    public static void sendTitleWithSound(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut, String soundName, float volume, float pitch) {
        player.sendTitle(ColorUtil.add(title), ColorUtil.add(subtitle), fadeIn, stay, fadeOut);
        if (soundName != null && !soundName.isEmpty()) {
            SoundUtil.playSound(player, soundName, volume, pitch);
        }
    }
}