package de.questcraft.plugins;

import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Map;

public class SoundMapper {
    private static final Map<String, Sound> vs2bsMap = new HashMap<>();

    static {
        for (Sound bukkitSound : Sound.values()) {
            String vanillaSoundKey = bukkitSound.getKey().getKey();
            vs2bsMap.put(vanillaSoundKey, bukkitSound);
        }
    }

    public static Sound getSound(String soundKey) throws IllegalArgumentException {
        try {
            Sound bukkitSound = vs2bsMap.get(soundKey);
            return bukkitSound != null ? bukkitSound : Sound.valueOf(soundKey);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}