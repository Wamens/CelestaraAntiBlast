package net.celestara;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class CelestaraAntiBlast extends JavaPlugin {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private ProtectionSettings protectionSettings = ProtectionSettings.defaults();
    private Set<Material> protectedItems = Collections.emptySet();
    private List<String> protectedItemNames = List.of();
    private boolean messagesEnabled;
    private boolean debugMode;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadPluginConfig();

        getServer().getPluginManager().registerEvents(new ItemProtectionListener(this), this);

        PluginCommand command = getCommand("celestaraantiblast");
        if (command != null) {
            AntiBlastCommand executor = new AntiBlastCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        protectedItems = Collections.emptySet();
        protectedItemNames = List.of();
    }

    public void reloadPluginConfig() {
        reloadConfig();
        messagesEnabled = getConfig().getBoolean("settings.enable-messages", true);
        debugMode = getConfig().getBoolean("settings.debug-mode", false);
        protectionSettings = ProtectionSettings.fromConfig(getConfig());
        updateProtectedItemCache(loadProtectedItems());
    }

    public boolean isProtected(Material material) {
        return protectedItems.contains(material);
    }

    public Set<Material> protectedItems() {
        return protectedItems;
    }

    public List<String> protectedItemNames() {
        return protectedItemNames;
    }

    public ProtectionSettings protectionSettings() {
        return protectionSettings;
    }

    public boolean messagesEnabled() {
        return messagesEnabled;
    }

    public boolean debugMode() {
        return debugMode;
    }

    public boolean addProtectedItem(Material material) {
        if (protectedItems.contains(material)) {
            return false;
        }

        LinkedHashSet<String> items = loadConfiguredItemNames();
        items.add(material.name());
        getConfig().set("protected-items", List.copyOf(items));
        saveConfig();
        updateProtectedItemCache(loadProtectedItems());
        return true;
    }

    public boolean removeProtectedItem(Material material) {
        if (!protectedItems.contains(material)) {
            return false;
        }

        LinkedHashSet<String> items = loadConfiguredItemNames();
        items.removeIf(entry -> entry.equalsIgnoreCase(material.name()));
        getConfig().set("protected-items", List.copyOf(items));
        saveConfig();
        updateProtectedItemCache(loadProtectedItems());
        return true;
    }

    public Component message(String path) {
        return color(getConfig().getString("messages." + path, defaultMessage(path)));
    }

    public Component message(String path, Material material) {
        String raw = getConfig().getString("messages." + path, defaultMessage(path));
        return color(raw.replace("%item%", material.name()));
    }

    public Component color(String text) {
        return LEGACY_SERIALIZER.deserialize(text == null ? "" : text);
    }

    public Material matchMaterial(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Material.matchMaterial(value.trim().toUpperCase(Locale.ROOT), false);
    }

    private void updateProtectedItemCache(Set<Material> items) {
        protectedItems = Collections.unmodifiableSet(items);
        protectedItemNames = items.stream()
                .map(Enum::name)
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private Set<Material> loadProtectedItems() {
        EnumSet<Material> items = EnumSet.noneOf(Material.class);
        for (String configuredName : getConfig().getStringList("protected-items")) {
            Material material = matchMaterial(configuredName);
            if (material == null || material.isAir() || !material.isItem()) {
                debug("Ignoring unknown protected item: " + configuredName);
                continue;
            }
            items.add(material);
        }
        return items;
    }

    private LinkedHashSet<String> loadConfiguredItemNames() {
        LinkedHashSet<String> items = new LinkedHashSet<>();
        for (String configuredName : getConfig().getStringList("protected-items")) {
            Material material = matchMaterial(configuredName);
            if (material != null && !material.isAir() && material.isItem()) {
                items.add(material.name());
            }
        }
        return items;
    }

    public void debug(String message) {
        if (debugMode) {
            getLogger().info("[Debug] " + message);
        }
    }

    private String defaultMessage(String path) {
        return switch (path) {
            case "item-added" -> "&aSuccessfully added &7%item% &ato protected list.";
            case "item-removed" -> "&cSuccessfully removed &7%item% &cfrom protected list.";
            case "item-already-protected" -> "&cThis item is already protected.";
            case "item-not-protected" -> "&cThis item is not in the protected list.";
            case "no-item-in-hand" -> "&cYou must be holding an item!";
            case "invalid-item" -> "&cThat is not a valid item material.";
            case "config-reloaded" -> "&aConfiguration reloaded!";
            case "no-permission" -> "&cYou don't have permission!";
            case "list-header" -> "&7&m--------&r &aProtected Items &7&m--------";
            case "list-empty" -> "&7No items are protected.";
            case "list-footer" -> "&7&m----------------------------";
            default -> "";
        };
    }
}
