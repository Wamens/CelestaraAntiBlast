package net.celestara;

import org.bukkit.configuration.file.FileConfiguration;

public record ProtectionSettings(
        boolean endCrystals,
        boolean respawnAnchors,
        boolean tnt,
        boolean creepers,
        boolean witherSkulls
) {

    public static ProtectionSettings defaults() {
        return new ProtectionSettings(
                true,
                true,
                true,
                true,
                true
        );
    }

    public static ProtectionSettings fromConfig(FileConfiguration config) {
        return new ProtectionSettings(
                config.getBoolean("protection.end-crystals", true),
                config.getBoolean("protection.respawn-anchors", true),
                config.getBoolean("protection.tnt", true),
                config.getBoolean("protection.creepers", true),
                config.getBoolean("protection.wither-skulls", true)
        );
    }
}
