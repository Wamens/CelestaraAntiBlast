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

        if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            return isRespawnAnchorExplosion(event, settings);
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return false;
        }

        return isProtectedEntityExplosion(event, settings);
    }

    private boolean isProtectedEntityExplosion(EntityDamageEvent event, ProtectionSettings settings) {
        if (event instanceof EntityDamageByEntityEvent byEntityEvent
                && isProtectedDamager(byEntityEvent.getDamager(), settings)) {
            return true;
        }

        DamageSource damageSource = event.getDamageSource();
        return damageSource != null
                && (isProtectedDamager(damageSource.getDirectEntity(), settings)
                || isProtectedDamager(damageSource.getCausingEntity(), settings));
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
