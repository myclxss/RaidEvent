package plugin.myclass.raidEvent.utils;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import plugin.myclass.raidEvent.RaidEvent;

import java.util.HashMap;
import java.util.Map;

public class BossBarUtil {

    // Mapeo de colores personalizados a colores válidos de BossBar
    private static final Map<String, BarColor> COLOR_MAPPING = new HashMap<>();
    private static final Map<String, BarStyle> STYLE_MAPPING = new HashMap<>();

    static {
        // Colores válidos en Minecraft
        COLOR_MAPPING.put("BLUE", BarColor.BLUE);
        COLOR_MAPPING.put("GREEN", BarColor.GREEN);
        COLOR_MAPPING.put("PINK", BarColor.PINK);
        COLOR_MAPPING.put("PURPLE", BarColor.PURPLE);
        COLOR_MAPPING.put("RED", BarColor.RED);
        COLOR_MAPPING.put("WHITE", BarColor.WHITE);
        COLOR_MAPPING.put("YELLOW", BarColor.YELLOW);

        // Mapeos adicionales para colores que no existen
        COLOR_MAPPING.put("ORANGE", BarColor.YELLOW);    // Orange -> Yellow
        COLOR_MAPPING.put("BLACK", BarColor.WHITE);      // Black -> White
        COLOR_MAPPING.put("GRAY", BarColor.WHITE);       // Gray -> White
        COLOR_MAPPING.put("GREY", BarColor.WHITE);       // Grey -> White
        COLOR_MAPPING.put("LIGHT_BLUE", BarColor.BLUE);  // Light Blue -> Blue
        COLOR_MAPPING.put("DARK_BLUE", BarColor.BLUE);   // Dark Blue -> Blue
        COLOR_MAPPING.put("DARK_RED", BarColor.RED);     // Dark Red -> Red
        COLOR_MAPPING.put("DARK_GREEN", BarColor.GREEN); // Dark Green -> Green

        // Estilos válidos
        STYLE_MAPPING.put("SOLID", BarStyle.SOLID);
        STYLE_MAPPING.put("SEGMENTED_6", BarStyle.SEGMENTED_6);
        STYLE_MAPPING.put("SEGMENTED_10", BarStyle.SEGMENTED_10);
        STYLE_MAPPING.put("SEGMENTED_12", BarStyle.SEGMENTED_12);
        STYLE_MAPPING.put("SEGMENTED_20", BarStyle.SEGMENTED_20);

        // Mapeos adicionales para estilos
        STYLE_MAPPING.put("PROGRESS", BarStyle.SOLID);
        STYLE_MAPPING.put("NOTCHED_6", BarStyle.SEGMENTED_6);
        STYLE_MAPPING.put("NOTCHED_10", BarStyle.SEGMENTED_10);
        STYLE_MAPPING.put("NOTCHED_12", BarStyle.SEGMENTED_12);
        STYLE_MAPPING.put("NOTCHED_20", BarStyle.SEGMENTED_20);
    }

    /**
     * Obtiene un BarColor válido desde un string
     * @param colorName Nombre del color
     * @return BarColor válido o RED por defecto
     */
    public static BarColor getBarColor(String colorName) {
        if (colorName == null || colorName.isEmpty()) {
            return BarColor.RED;
        }

        String upperColor = colorName.toUpperCase().trim();

        // Intentar obtener del mapeo personalizado primero
        BarColor mappedColor = COLOR_MAPPING.get(upperColor);
        if (mappedColor != null) {
            return mappedColor;
        }

        // Intentar con el enum directamente
        try {
            return BarColor.valueOf(upperColor);
        } catch (IllegalArgumentException e) {
            RaidEvent.getInstance().getLogger().warning("Invalid BossBar color: " + colorName + ", using RED as fallback");
            return BarColor.RED;
        }
    }

    /**
     * Obtiene un BarStyle válido desde un string
     * @param styleName Nombre del estilo
     * @return BarStyle válido o SOLID por defecto
     */
    public static BarStyle getBarStyle(String styleName) {
        if (styleName == null || styleName.isEmpty()) {
            return BarStyle.SOLID;
        }

        String upperStyle = styleName.toUpperCase().trim();

        // Intentar obtener del mapeo personalizado primero
        BarStyle mappedStyle = STYLE_MAPPING.get(upperStyle);
        if (mappedStyle != null) {
            return mappedStyle;
        }

        // Intentar con el enum directamente
        try {
            return BarStyle.valueOf(upperStyle);
        } catch (IllegalArgumentException e) {
            RaidEvent.getInstance().getLogger().warning("Invalid BossBar style: " + styleName + ", using SOLID as fallback");
            return BarStyle.SOLID;
        }
    }

    /**
     * Verifica si un color es válido
     * @param colorName Nombre del color
     * @return true si es válido, false en caso contrario
     */
    public static boolean isValidColor(String colorName) {
        if (colorName == null || colorName.isEmpty()) {
            return false;
        }

        String upperColor = colorName.toUpperCase().trim();
        return COLOR_MAPPING.containsKey(upperColor);
    }

    /**
     * Verifica si un estilo es válido
     * @param styleName Nombre del estilo
     * @return true si es válido, false en caso contrario
     */
    public static boolean isValidStyle(String styleName) {
        if (styleName == null || styleName.isEmpty()) {
            return false;
        }

        String upperStyle = styleName.toUpperCase().trim();
        return STYLE_MAPPING.containsKey(upperStyle);
    }

    /**
     * Obtiene todos los colores válidos
     * @return Array con nombres de colores válidos
     */
    public static String[] getValidColors() {
        return COLOR_MAPPING.keySet().toArray(new String[0]);
    }

    /**
     * Obtiene todos los estilos válidos
     * @return Array con nombres de estilos válidos
     */
    public static String[] getValidStyles() {
        return STYLE_MAPPING.keySet().toArray(new String[0]);
    }

    /**
     * Obtiene el color más apropiado para una oleada específica
     * @param wave Número de oleada (1-10)
     * @return BarColor apropiado para la oleada
     */
    public static BarColor getWaveColor(int wave) {
        return switch (wave) {
            case 1 -> BarColor.GREEN;      // Fácil
            case 2, 3 -> BarColor.YELLOW;  // Medio/Difícil
            case 4, 5 -> BarColor.PURPLE;  // Experto/Maestro
            case 6, 7 -> BarColor.BLUE;    // Legendario/Épico
            case 8, 9, 10 -> BarColor.RED; // Mítico/Abismal/Final
            default -> BarColor.WHITE;
        };
    }

    /**
     * Obtiene el estilo más apropiado para una oleada específica
     * @param wave Número de oleada (1-10)
     * @return BarStyle apropiado para la oleada
     */
    public static BarStyle getWaveStyle(int wave) {
        return switch (wave) {
            case 1, 2 -> BarStyle.SOLID;
            case 3, 4 -> BarStyle.SEGMENTED_6;
            case 5, 6 -> BarStyle.SEGMENTED_10;
            case 7, 8 -> BarStyle.SEGMENTED_12;
            case 9, 10 -> BarStyle.SEGMENTED_20;
            default -> BarStyle.SOLID;
        };
    }
}