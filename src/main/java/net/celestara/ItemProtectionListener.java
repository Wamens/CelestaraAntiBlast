package net.celestara;

import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public final class ItemProtectionListener implements Listener {

    private final CelestaraAntiBlast plugin;

    public ItemProtectionListener(CelestaraAntiBlast plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item)) {
            return;
        }

        if (!plugin.isProtected(item.getItemStack().getType())) {
            return;
        }

        if (shouldProtect(event)) {
            event.setCancelled(true);
            plugin.debug("Protected " + item.getItemStack().getType() + " from " + event.getCause());
        }
    }

    private boolean shouldProtect(EntityDamageEvent event) {
        ProtectionSettings settings = plugin.protectionSettings();

        if (event instanceof EntityDamageByEntityEvent byEntityEvent) {
            if (isProtectedDamager(byEntityEvent.getDamager(), settings)) {
                return true;
            }
        }

        DamageSource damageSource = event.getDamageSource();
        if (damageSource != null
                && (isProtectedDamager(damageSource.getDirectEntity(), settings)
                || isProtectedDamager(damageSource.getCausingEntity(), settings))) {
            return true;
        }

        return switch (event.getCause()) {
            case BLOCK_EXPLOSION -> isRespawnAnchorExplosion(event, settings);
            default -> false;
        };
    }

    private boolean isProtectedDamager(Entity damager, ProtectionSettings settings) {
        if (damager == null) {
            return false;
        }

        return switch (damager.getType()) {
            case TNT, TNT_MINECART -> settings.tnt();
            case END_CRYSTAL -> settings.endCrystals();
            default -> false;
        };
    }

    private boolean isRespawnAnchorExplosion(EntityDamageEvent event, ProtectionSettings settings) {
        if (!settings.respawnAnchors() || !(event instanceof EntityDamageByBlockEvent byBlockEvent)) {
            return false;
        }

        if (byBlockEvent.getDamagerBlockState() != null) {
            return byBlockEvent.getDamagerBlockState().getType() == Material.RESPAWN_ANCHOR;
        }

        return byBlockEvent.getDamager() != null && byBlockEvent.getDamager().getType() == Material.RESPAWN_ANCHOR;
    }
}
