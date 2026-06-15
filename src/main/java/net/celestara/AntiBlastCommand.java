package net.celestara;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AntiBlastCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("add", "remove", "list", "reload");
    private static final List<String> ITEM_MATERIAL_NAMES = List.of(Material.values()).stream()
            .filter(material -> !material.isAir() && material.isItem())
            .map(Enum::name)
            .sorted()
            .toList();

    private final CelestaraAntiBlast plugin;

    public AntiBlastCommand(CelestaraAntiBlast plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "add" -> handleAdd(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            default -> sendUsage(sender, label);
        }
        return true;
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "celestaraantiblast.command.add")) {
            return;
        }

        Material material = resolveMaterial(sender, args);
        if (material == null) {
            return;
        }

        if (!plugin.addProtectedItem(material)) {
            send(sender, "item-already-protected");
            return;
        }

        send(sender, "item-added", material);
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "celestaraantiblast.command.remove")) {
            return;
        }

        Material material = resolveMaterial(sender, args);
        if (material == null) {
            return;
        }

        if (!plugin.removeProtectedItem(material)) {
            send(sender, "item-not-protected");
            return;
        }

        send(sender, "item-removed", material);
    }

    private void handleList(CommandSender sender) {
        if (!hasPermission(sender, "celestaraantiblast.command.list")) {
            return;
        }

        send(sender, "list-header");
        if (plugin.protectedItems().isEmpty()) {
            send(sender, "list-empty");
        } else {
            String format = plugin.getConfig().getString("messages.list-item", "&8- &a%item%");
            for (String materialName : plugin.protectedItemNames()) {
                sender.sendMessage(plugin.color(format.replace("%item%", materialName)));
            }
        }
        send(sender, "list-footer");
    }

    private void handleReload(CommandSender sender) {
        if (!hasPermission(sender, "celestaraantiblast.command.reload")) {
            return;
        }

        plugin.reloadPluginConfig();
        send(sender, "config-reloaded");
    }

    private Material resolveMaterial(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            Material material = plugin.matchMaterial(args[1]);
            if (material == null || material.isAir() || !material.isItem()) {
                send(sender, "invalid-item");
                return null;
            }
            return material;
        }

        if (!(sender instanceof Player player)) {
            send(sender, "no-item-in-hand");
            return null;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType().isAir()) {
            send(sender, "no-item-in-hand");
            return null;
        }

        return heldItem.getType();
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        send(sender, "no-permission");
        return false;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(plugin.color("&a/" + label + " add [material]"));
        sender.sendMessage(plugin.color("&a/" + label + " remove [material]"));
        sender.sendMessage(plugin.color("&a/" + label + " list"));
        sender.sendMessage(plugin.color("&a/" + label + " reload"));
    }

    private void send(CommandSender sender, String path) {
        if (plugin.messagesEnabled()) {
            sender.sendMessage(plugin.message(path));
        }
    }

    private void send(CommandSender sender, String path, Material material) {
        if (plugin.messagesEnabled()) {
            sender.sendMessage(plugin.message(path, material));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            return filter(args[0].equalsIgnoreCase("remove") ? plugin.protectedItemNames() : ITEM_MATERIAL_NAMES, args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String input) {
        String prefix = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                matches.add(value);
            }
        }
        return matches;
    }
}
