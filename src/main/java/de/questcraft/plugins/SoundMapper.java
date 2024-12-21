package de.questcraft.plugins;

import org.bukkit.Sound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SoundMapper {
    private static final Map<String, Sound> vanillaToBukkitMap = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(SoundMapper.class);

    public static void initialize() {
        for (Sound bukkitSound : Sound.values()) {
            String vanillaSoundKey = bukkitSound.getKey().getKey();
            vanillaToBukkitMap.put(vanillaSoundKey, bukkitSound);
        }
        SoundMapper.log.info("SoundMapper initialized with {} sounds.", vanillaToBukkitMap.size());
    }

    public static Sound getSound(String soundKey) {
        try {
            Sound bukkitSound = vanillaToBukkitMap.get(soundKey);
            if (bukkitSound != null) {
                return bukkitSound;
            }
            return Sound.valueOf(soundKey.toUpperCase().replace(".", "_"));
        } catch (IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
            return Sound.ENTITY_CREEPER_HURT;
        }
    }
}