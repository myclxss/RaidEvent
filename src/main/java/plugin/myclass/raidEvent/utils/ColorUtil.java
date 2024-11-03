package plugin.myclass.raidEvent.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String add(String text) {
        text = translateHexColorCodes(text);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> add(List<String> textList) {
        List<String> translatedList = new ArrayList<>();
        for (String text : textList) {
            translatedList.add(add(text));
        }
        return translatedList;
    }

    public static String translateHexColorCodes(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(buffer, "\u00A7x");

            for (char c : color.toCharArray()) {
                buffer.append("\u00A7").append(c);
            }
        }

        return matcher.appendTail(buffer).toString();
    }
}