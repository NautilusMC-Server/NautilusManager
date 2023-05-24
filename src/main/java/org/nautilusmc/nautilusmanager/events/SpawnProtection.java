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
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.util.BoundingBox;
import org.nautilusmc.nautilusmanager.NautilusManager;

public class SpawnProtection implements Listener {

    private void alert(Player p) {
        p.sendActionBar(Component.text("You can't do that here").color(TextColor.color(255, 149, 146)));
        Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, () -> {
            p.sendActionBar(Component.text("You can't do that here").color(NamedTextColor.RED));
        }, 7);
    }

    private boolean isProtected(Location loc) {
        Location loc1 = NautilusManager.INSTANCE.getConfig().getLocation("spawnProtection.loc1");
        Location loc2 = NautilusManager.INSTANCE.getConfig().getLocation("spawnProtection.loc2");

        if (loc1 == null || loc2 == null) {
            return false;
        }

        return new BoundingBox(loc1.getX(), loc1.getWorld().getMinHeight(), loc1.getZ(), loc2.getX(), loc2.getWorld().getMaxHeight(), loc2.getZ()).contains(loc.toVector());
    }

    private boolean isAllowed(Player player) {
        return player.hasPermission("nautilusmanager.edit_spawn");
    }

    @EventHandler
    public void onSpawnEvent(EntitySpawnEvent e) {
        if (e.getEntity() instanceof LivingEntity && isProtected(e.getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if(isProtected(e.getBlock().getLocation()) && !isAllowed(e.getPlayer())) {
            alert(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if(isProtected(e.getBlock().getLocation()) && !isAllowed(e.getPlayer())) {
            alert(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucket(PlayerBucketFillEvent e) {
        if(isProtected(e.getBlock().getLocation()) && !isAllowed(e.getPlayer())) {
            alert(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if(!e.hasBlock() || (e.getPlayer().isSneaking() && e.hasItem())) return;

        Block block = e.getClickedBlock();
        if(isProtected(block.getLocation()) && !isAllowed(e.getPlayer())) {
            Material type = block.getType();

            if (type != Material.CHEST && type != Material.BARREL && type != Material.ENDER_CHEST && !Tag.PRESSURE_PLATES.isTagged(type) && !Tag.WOODEN_DOORS.isTagged(type) && !Tag.BUTTONS.isTagged(type) && !Tag.FENCE_GATES.isTagged(type)) {
                alert(e.getPlayer());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if(isProtected(e.getRightClicked().getLocation()) && !isAllowed(e.getPlayer())) {
            alert(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if(isProtected(e.getRightClicked().getLocation()) && !isAllowed(e.getPlayer())) {
            alert(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCropTrample(PlayerInteractEvent e) {
        if(e.getAction().equals(Action.PHYSICAL) && e.getInteractionPoint() != null && e.getInteractionPoint().getBlock().getType().equals(Material.FARMLAND)) {
            if(isProtected(e.getInteractionPoint()) && !isAllowed(e.getPlayer())) {
                alert(e.getPlayer());
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

        if(isProtected(e.getEntity().getLocation()) && !isAllowed(player)) {
            alert(player);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent e) {
        if (!(e.getAttacker() instanceof Player player)) {
            e.setCancelled(true);
            return;
        }

        if(isProtected(e.getVehicle().getLocation()) && isAllowed(player)) {
            alert(player);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerAttackEvent(EntityDamageByEntityEvent e) {
        if(isProtected(e.getEntity().getLocation())) {
            if (!(e.getDamager() instanceof Player player)) {
                e.setCancelled(true);
                return;
            }

            if (!isAllowed(player)) {
                alert(player);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if(e.getEntity() instanceof Creeper) {
            e.blockList().clear();
            return;
        }

        e.blockList().removeIf(b -> isProtected(b.getLocation()) && (e.getEntity() instanceof Creeper || !b.getType().equals(Material.TNT)));
    }

    @EventHandler
    public void onChangeBlock(EntityChangeBlockEvent e) {
        if(e.getEntity() instanceof Enderman) {
            e.setCancelled(true);
        }

        if(!isProtected(e.getBlock().getLocation())) return;
        if(!(e.getEntity() instanceof Player) && e.getBlock().getType().equals(Material.FARMLAND)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onChangeBlock(EntityBreakDoorEvent e) {
        if(e.getEntity() instanceof Monster) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplosion(BlockExplodeEvent e) {
        e.blockList().removeIf(b -> isProtected(e.getBlock().getLocation()));
    }

    @EventHandler
    public void onHungerEvent(FoodLevelChangeEvent e) {
        if(!(e.getEntity() instanceof Player)) return;

        if(e.getFoodLevel() < e.getEntity().getFoodLevel() && isProtected(e.getEntity().getLocation())) {
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
        // allow pistons if they're inside; just not outside going in
        if (isProtected(e.getBlock().getLocation())) return;

        for (Block block : e.getBlocks()) {
            if (isProtected(block.getLocation()) || isProtected(block.getLocation().add(e.getDirection().getDirection()))) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonRetractEvent e) {
        // allow pistons if they're inside; just not outside pulling out
        if (isProtected(e.getBlock().getLocation())) return;

        for (Block block : e.getBlocks()) {
            if (isProtected(block.getLocation()) || isProtected(block.getLocation().add(e.getDirection().getDirection()))) {
                e.setCancelled(true);
                return;
            }
        }
    }
}
