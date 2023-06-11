package org.nautilusmc.nautilusmanager.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.util.BoundingBox;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.Command.Permission;

public class SpawnProtection implements Listener {

    public static final String PROTECTED_AREA_ALERT = "You can't do that here";
    public static final long ALERT_FLASH_TICKS = 7;
    public static final TextColor ALERT_FLASH_COLOR = TextColor.color(255, 149, 146);
    public static final TextColor ALERT_COLOR = NamedTextColor.RED;

    public SpawnProtection() {
        Location loc1 = NautilusManager.INSTANCE.getConfig().getLocation("spawnProtection.loc1");
        Location loc2 = NautilusManager.INSTANCE.getConfig().getLocation("spawnProtection.loc2");

        if (loc1 == null || loc2 == null) {
            NautilusManager.INSTANCE.getLogger().warning("Spawn protection disabled. To enable, provide values for loc1 and loc2.");
        } else if (!loc1.getWorld().equals(loc2.getWorld())) {
            NautilusManager.INSTANCE.getLogger().warning("Spawn protection is configured improperly! loc1 and loc2 are not in the same world (loc2's world will be ignored).");
        }
    }

    private void sendAlert(Player player) {
        player.sendActionBar(Component.text(PROTECTED_AREA_ALERT).color(ALERT_FLASH_COLOR));
        Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, () -> {
            player.sendActionBar(Component.text(PROTECTED_AREA_ALERT).color(ALERT_COLOR));
        }, ALERT_FLASH_TICKS);
    }

    public boolean isProtected(Location location) {
        Location loc1 = NautilusManager.INSTANCE.getConfig().getLocation("spawnProtection.loc1");
        Location loc2 = NautilusManager.INSTANCE.getConfig().getLocation("spawnProtection.loc2");

        if (loc1 == null || loc2 == null) {
            return false;
        }

        // sufficient to check if location is in the same world as loc1
        // if loc2 is not in the same world as loc1, server administration is to blame...
        return location.getWorld().equals(loc1.getWorld()) && new BoundingBox(
                loc1.getX(), loc1.getWorld().getMinHeight(), loc1.getZ(),
                loc2.getX(), loc2.getWorld().getMaxHeight(), loc2.getZ()
        ).contains(location.toVector());
    }

    public boolean allowsInteraction(Block block) {
        Material type = block.getType();
        return type == Material.CHEST
                || type == Material.BARREL
                || type == Material.ENDER_CHEST
                || Tag.PRESSURE_PLATES.isTagged(type)
                || Tag.WOODEN_DOORS.isTagged(type)
                || Tag.BUTTONS.isTagged(type)
                || Tag.FENCE_GATES.isTagged(type);
    }

    public boolean canEditSpawn(Player player) {
        return player.hasPermission(Permission.EDIT_SPAWN);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof LivingEntity && isProtected(e.getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (isProtected(e.getBlock().getLocation()) && !canEditSpawn(e.getPlayer())) {
            sendAlert(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (isProtected(e.getBlock().getLocation()) && !canEditSpawn(e.getPlayer())) {
            sendAlert(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent e) {
        if (isProtected(e.getBlock().getLocation()) && !canEditSpawn(e.getPlayer())) {
            sendAlert(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
        if (isProtected(e.getBlock().getLocation()) && !canEditSpawn(e.getPlayer())) {
            sendAlert(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!e.hasBlock() || (e.getPlayer().isSneaking() && e.hasItem())) return;
        if (e.hasItem() && e.getItem().getType().isEdible()) return;

        Block block = e.getClickedBlock();
        if (isProtected(block.getLocation()) && !canEditSpawn(e.getPlayer()) && !allowsInteraction(block)) {
            sendAlert(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (isProtected(e.getRightClicked().getLocation()) && !canEditSpawn(e.getPlayer())) {
            sendAlert(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (isProtected(e.getRightClicked().getLocation()) && !canEditSpawn(e.getPlayer())) {
            sendAlert(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCropTrample(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.PHYSICAL) && e.getInteractionPoint() != null && e.getInteractionPoint().getBlock().getType().equals(Material.FARMLAND)) {
            if (isProtected(e.getInteractionPoint()) && !canEditSpawn(e.getPlayer())) {
                sendAlert(e.getPlayer());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        if (!(e.getRemover() instanceof Player player)) {
            e.setCancelled(true);
            return;
        }

        if (isProtected(e.getEntity().getLocation()) && !canEditSpawn(player)) {
            sendAlert(player);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent e) {
        if (isProtected(e.getVehicle().getLocation())) {
            if (!(e.getAttacker() instanceof Player player)) {
                e.setCancelled(true);
                return;
            }

            if (!canEditSpawn(player)) {
                sendAlert(player);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (isProtected(e.getEntity().getLocation())) {
            if (!(e.getDamager() instanceof Player player)) {
                e.setCancelled(true);
                return;
            }

            if (!canEditSpawn(player)) {
                sendAlert(player);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.getEntity() instanceof Creeper) {
            e.blockList().clear();
            return;
        }

        e.blockList().removeIf(b -> isProtected(b.getLocation()) && (e.getEntity() instanceof Creeper || !b.getType().equals(Material.TNT)));
    }

    @EventHandler
    public void onChangeBlock(EntityChangeBlockEvent e) {
        if (e.getEntity() instanceof Enderman) {
            e.setCancelled(true);
        }

        if (!isProtected(e.getBlock().getLocation())) return;
        if (!(e.getEntity() instanceof Player) && e.getBlock().getType().equals(Material.FARMLAND)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreakDoor(EntityBreakDoorEvent e) {
        if (e.getEntity() instanceof Monster) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        e.blockList().removeIf(b -> isProtected(e.getBlock().getLocation()));
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        if (e.getFoodLevel() < e.getEntity().getFoodLevel() && isProtected(e.getEntity().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onLiquidFlow(BlockFromToEvent e) {
        if (isProtected(e.getToBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        // allow pistons if they're inside; just not outside pushing in
        if (isProtected(e.getBlock().getLocation())) return;

        for (Block block : e.getBlocks()) {
            if (isProtected(block.getLocation()) || isProtected(block.getLocation().add(e.getDirection().getDirection()))) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        // allow pistons if they're inside; just not outside pulling out
        if (isProtected(e.getBlock().getLocation())) return;

        for (Block block : e.getBlocks()) {
            if (isProtected(block.getLocation()) || isProtected(block.getLocation().add(e.getDirection().getDirection()))) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onRaidTrigger(RaidTriggerEvent e) {
        if (isProtected(e.getRaid().getLocation())) {
            e.setCancelled(true);
        }
    }
}
