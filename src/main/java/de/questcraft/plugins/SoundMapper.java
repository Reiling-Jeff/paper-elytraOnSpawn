package de.questcraft.plugins;

import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Map;

public class SoundMapper {
    private static final Map<String, Sound> vs2bsMap = new HashMap<>();

    public static void initialize() {
        for (Sound bukkitSound : Sound.values()) {
            String vanillaSoundKey = bukkitSound.getKey().getKey();
            vs2bsMap.put(vanillaSoundKey, bukkitSound);
        }
        System.out.println("SoundMapper initialized with " + vs2bsMap.size() + " sounds.");
    }

    public static Sound getSound(String soundKey) {
        try {
            Sound bukkitSound = vs2bsMap.get(soundKey);
            if (bukkitSound != null) {
                return bukkitSound;
            }
            return Sound.valueOf(soundKey.toUpperCase().replace(".", "_"));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return Sound.ENTITY_CREEPER_HURT;
        }
    }
}