package plugin.myclass.raidEvent.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import plugin.myclass.raidEvent.RaidEvent;

public class SoundUtil {

    /**
     * Reproduce un sonido en una ubicación específica usando el nuevo sistema de Registry
     *
     * @param location  Ubicación donde reproducir el sonido
     * @param soundName Nombre del sonido (ej: "ENTITY_PLAYER_HURT" o "minecraft:entity.player.hurt")
     * @param volume    Volumen del sonido (0.0 - 1.0)
     * @param pitch     Pitch del sonido (0.5 - 2.0)
     */
    public static void playSound(Location location, String soundName, float volume, float pitch) {
        if (location == null || location.getWorld() == null || soundName == null || soundName.isEmpty()) {
            return;
        }

        try {
            Sound sound = getSoundFromName(soundName);
            if (sound != null) {
                location.getWorld().playSound(location, sound, SoundCategory.MASTER, volume, pitch);
            } else {
                RaidEvent.getInstance().getLogger().warning("Sound not found: " + soundName);
                // Fallback al sonido por defecto
                location.getWorld().playSound(location, Sound.ENTITY_PLAYER_HURT, SoundCategory.MASTER, volume, pitch);
            }
        } catch (Exception e) {
            RaidEvent.getInstance().getLogger().warning("Error playing sound " + soundName + ": " + e.getMessage());
            // Fallback al sonido por defecto
            location.getWorld().playSound(location, Sound.ENTITY_PLAYER_HURT, SoundCategory.MASTER, volume, pitch);
        }
    }

    /**
     * Reproduce un sonido para un jugador específico
     *
     * @param player    Jugador que escuchará el sonido
     * @param soundName Nombre del sonido
     * @param volume    Volumen del sonido
     * @param pitch     Pitch del sonido
     */
    public static void playSound(Player player, String soundName, float volume, float pitch) {
        if (player == null || !player.isOnline() || soundName == null || soundName.isEmpty()) {
            return;
        }

        try {
            Sound sound = getSoundFromName(soundName);
            if (sound != null) {
                player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch);
            } else {
                RaidEvent.getInstance().getLogger().warning("Sound not found: " + soundName);
                // Fallback al sonido por defecto
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, SoundCategory.MASTER, volume, pitch);
            }
        } catch (Exception e) {
            RaidEvent.getInstance().getLogger().warning("Error playing sound " + soundName + " for player " + player.getName() + ": " + e.getMessage());
            // Fallback al sonido por defecto
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, SoundCategory.MASTER, volume, pitch);
        }
    }

    /**
     * Reproduce un sonido para todos los jugadores online
     *
     * @param soundName Nombre del sonido
     * @param volume    Volumen del sonido
     * @param pitch     Pitch del sonido
     */
    public static void playSoundForAll(String soundName, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playSound(player, soundName, volume, pitch);
        }
    }

    /**
     * Convierte un nombre de sonido string a un objeto Sound usando el nuevo sistema
     *
     * @param soundName Nombre del sonido (puede ser enum name o namespaced key)
     * @return Sound object o null si no se encuentra
     */
    private static Sound getSoundFromName(String soundName) {
        if (soundName == null || soundName.isEmpty()) {
            return null;
        }

        try {
            // Primero intentar con el método tradicional (aún funciona para compatibilidad)
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Si falla, intentar con NamespacedKey
            try {
                NamespacedKey key;
                if (soundName.contains(":")) {
                    // Ya es un namespaced key (ej: "minecraft:entity.player.hurt")
                    String[] parts = soundName.split(":", 2);
                    key = new NamespacedKey(parts[0], parts[1]);
                } else {
                    // Convertir de enum name a namespaced key
                    String namespacedName = convertEnumToNamespaced(soundName);
                    key = NamespacedKey.minecraft(namespacedName);
                }

                return Registry.SOUNDS.get(key);
            } catch (Exception ex) {
                RaidEvent.getInstance().getLogger().warning("Failed to convert sound name: " + soundName);
                return null;
            }
        }
    }

    /**
     * Convierte nombres de enum de Sound a formato namespaced
     *
     * @param enumName Nombre del enum (ej: "ENTITY_PLAYER_HURT")
     * @return String en formato namespaced (ej: "entity.player.hurt")
     */
    private static String convertEnumToNamespaced(String enumName) {
        return enumName.toLowerCase()
                .replace("_", ".")
                .replace("ui.", "ui/")
                .replace("block.", "block/")
                .replace("entity.", "entity/")
                .replace("item.", "item/")
                .replace("music.", "music/")
                .replace("ambient.", "ambient/")
                .replace("/", ".");
    }

    /**
     * Verifica si un sonido existe
     *
     * @param soundName Nombre del sonido a verificar
     * @return true si el sonido existe, false en caso contrario
     */
    public static boolean soundExists(String soundName) {
        return getSoundFromName(soundName) != null;
    }

    /**
     * Obtiene una lista de todos los sonidos disponibles (para debugging)
     * @return Array con todos los nombres de sonidos
     */

    public static String[] getAllSoundNames() {
        return Registry.SOUNDS.stream()
                .map(sound -> Registry.SOUNDS.getKey(sound).toString())
                .toArray(String[]::new);
    }

    /**
     * Reproduce sonido de evento con configuración por defecto
     *
     * @param location  Ubicación
     * @param soundName Nombre del sonido
     */
    public static void playEventSound(Location location, String soundName) {
        playSound(location, soundName, 1.0f, 1.0f);
    }

    /**
     * Reproduce sonido de evento para un jugador con configuración por defecto
     *
     * @param player    Jugador
     * @param soundName Nombre del sonido
     */
    public static void playEventSound(Player player, String soundName) {
        playSound(player, soundName, 1.0f, 1.0f);
    }
}