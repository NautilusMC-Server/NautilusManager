package org.nautilusmc.nautilusmanager.events;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.util.Util;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class GeneralEventManager implements Listener {

    @EventHandler
    public void onAnvil(PrepareAnvilEvent e) {
        ItemStack firstItem = e.getInventory().getFirstItem();
        if (firstItem == null) return;
        String currentName = firstItem.hasItemMeta() && firstItem.getItemMeta().displayName() != null ? Util.getTextContent(firstItem.getItemMeta().displayName()) : "";

        if (!currentName.equals(e.getInventory().getRenameText())) {
            int baseRepairCost = CraftItemStack.asNMSCopy(firstItem).getBaseRepairCost();
            if (e.getInventory().getSecondItem() != null) baseRepairCost = 0;

            e.getInventory().setRepairCost(e.getInventory().getRepairCost()-1-baseRepairCost);
        }
    }

    @EventHandler
    public void onPlayerMessage(AsyncChatEvent e) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            String message = Util.getTextContent(e.message()).toLowerCase();
            if(message.contains(Util.getTextContent(p.displayName()).toLowerCase()) || message.contains(p.getName().toLowerCase())) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 2);
                Bukkit.getScheduler().scheduleSyncDelayedTask(NautilusManager.INSTANCE, () -> {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 4F);
                }, 2);
            }
        }
    }

    private static final String OWNER_KEY = "owner";
    private static final String SLOT_KEY = "slot";

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Inventory inv = e.getPlayer().getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            meta.getPersistentDataContainer().set(new NamespacedKey(NautilusManager.INSTANCE, SLOT_KEY), PersistentDataType.INTEGER, i);
            meta.getPersistentDataContainer().set(new NamespacedKey(NautilusManager.INSTANCE, OWNER_KEY), PersistentDataType.STRING, e.getPlayer().getUniqueId().toString());
            item.setItemMeta(meta);
        }
    }

    @EventHandler
    public void itemSpawnEvent(ItemSpawnEvent e) {
        if (e.getEntity().getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(NautilusManager.INSTANCE, OWNER_KEY))) {
            ItemEntity nms = (ItemEntity) ((CraftItem) e.getEntity()).getHandle();
            // copied from ItemEntity#setItem
            int defaultDespawnTime = nms.level.paperConfig().entities.spawning.altItemDespawnRate.enabled ? nms.level.paperConfig().entities.spawning.altItemDespawnRate.items.getOrDefault(nms.getItem().getItem(), nms.level.spigotConfig.itemDespawnRate) : nms.level.spigotConfig.itemDespawnRate;
            int despawnTime = NautilusManager.INSTANCE.getConfig().getInt("death.itemDespawnSeconds") * 20;
            int age = defaultDespawnTime - despawnTime;

            if (age <= Short.MIN_VALUE) {
                NautilusManager.INSTANCE.getLogger().warning("Despawn time for death items is too low (" + despawnTime + "), and would be infinite. Defaulting to " + ((defaultDespawnTime - (Short.MIN_VALUE + 1))/20) + ". For infinite values, use -1.");

                age = Short.MIN_VALUE+1;
            }

            nms.age = age;
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!container.has(new NamespacedKey(NautilusManager.INSTANCE, SLOT_KEY), PersistentDataType.INTEGER)) return;
        String owner = container.get(new NamespacedKey(NautilusManager.INSTANCE, OWNER_KEY), PersistentDataType.STRING);
        int slot = container.get(new NamespacedKey(NautilusManager.INSTANCE, SLOT_KEY), PersistentDataType.INTEGER);

        container.remove(new NamespacedKey(NautilusManager.INSTANCE, OWNER_KEY));
        container.remove(new NamespacedKey(NautilusManager.INSTANCE, SLOT_KEY));

        item.setItemMeta(meta);
        e.getItem().setItemStack(item);

        if (e.getEntity() instanceof Player player && player.getUniqueId().equals(UUID.fromString(owner))) {
            Inventory inv = player.getInventory();
            ItemStack invItem = inv.getItem(slot);

            if (invItem == null) {
                inv.setItem(slot, item);
                ((CraftPlayer) player).getHandle().take(((CraftItem) e.getItem()).getHandle(), item.getAmount());
                e.getItem().remove();
                e.setCancelled(true);
            }
        }
    }

    // chairs cause why not

    // yuck. boolean flag
    // I couldn't find a better way to prevent the dismount event from being called in calls to e.g. seat.remove()
    private boolean enableChairDismountEvent = true;

    private boolean isValidChair(@NotNull Block block) {
        // not sure if checking the material AND block data type is redundant but might as well
        return Tag.STAIRS.isTagged(block.getType()) && block.getBlockData() instanceof Stairs stairData && stairData.getHalf().equals(Bisected.Half.BOTTOM);
    }

    @EventHandler
    public void onPlayerInteractWithChair(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && Objects.equals(e.getHand(), EquipmentSlot.HAND) &&
                e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.AIR) && isValidChair(e.getClickedBlock()) &&
                !(e.getPlayer().getVehicle() instanceof Egg)) {
            Location seatLocation = e.getClickedBlock().getLocation().add(0.5, 0.1, 0.5);
            if (!seatLocation.getNearbyEntitiesByType(Egg.class, 0.2, 0.2, 0.2).isEmpty()) {
                return;
            }

            Egg seat = e.getClickedBlock().getLocation().getWorld().spawn(
                    seatLocation, Egg.class, (egg) -> {
                        egg.setGravity(false);
                        egg.setInvulnerable(true);
                    });
            seat.addPassenger(e.getPlayer());
        }
    }

    @EventHandler
    public void onChairDismount(EntityDismountEvent e) {
        if (enableChairDismountEvent && e.getDismounted() instanceof Egg) {
            Location center = e.getDismounted().getLocation().toBlockLocation().add(0.5, 0.0, 0.5);
            center.setDirection(e.getEntity().getLocation().getDirection());
            e.getDismounted().remove();

            Block chair = center.getBlock();
            if (isValidChair(chair)) {
                // is there a better way to do this? probably
                Vector frontOffset = ((Stairs) chair.getBlockData()).getFacing().getOppositeFace().getDirection();
                Vector leftOffset = frontOffset.clone().rotateAroundY(Math.PI * 0.5);
                Location front = center.clone().add(frontOffset);
                Location frontLeft = front.clone().add(leftOffset);
                Location frontRight = front.clone().subtract(leftOffset);
                Location left = center.clone().add(leftOffset);
                Location right = center.clone().subtract(leftOffset);
                Location back = center.clone().subtract(frontOffset);
                Location backLeft = back.clone().add(leftOffset);
                Location backRight = back.clone().subtract(leftOffset);
                Location top = center.clone().add(0, 1, 0);

                if (!e.getEntity().collidesAt(front)) {
                    e.getEntity().teleport(front);
                } else if (!e.getEntity().collidesAt(frontLeft)) {
                    e.getEntity().teleport(frontLeft);
                } else if (!e.getEntity().collidesAt(frontRight)) {
                    e.getEntity().teleport(frontRight);
                } else if (!e.getEntity().collidesAt(left)) {
                    e.getEntity().teleport(left);
                } else if (!e.getEntity().collidesAt(right)) {
                    e.getEntity().teleport(right);
                } else if (!e.getEntity().collidesAt(backLeft)) {
                    e.getEntity().teleport(backLeft);
                } else if (!e.getEntity().collidesAt(backRight)) {
                    e.getEntity().teleport(backRight);
                } else if (!e.getEntity().collidesAt(back)) {
                    e.getEntity().teleport(back);
                } else if (!e.getEntity().collidesAt(top)) {
                    e.getEntity().teleport(top);
                } else {
                    // they really got themselves in a pickle
                    e.getEntity().teleport(center);
                }
            } else {
                e.getEntity().teleport(center);
            }
        }
    }

    @EventHandler
    public void onPlayerTeleportOffChair(PlayerTeleportEvent e) {
        if (e.getPlayer().getVehicle() instanceof Egg seat) {
            enableChairDismountEvent = false;
            seat.remove();
            enableChairDismountEvent = true;
        }
    }

    @EventHandler
    public void onPlayerDeathOnChair(PlayerDeathEvent e) {
        if (e.getPlayer().getVehicle() instanceof Egg seat) {
            enableChairDismountEvent = false;
            seat.remove();
            enableChairDismountEvent = true;
        }
    }

    @EventHandler
    public void onChairBreak(BlockBreakEvent e) {
        if (isValidChair(e.getBlock())) {
            Location seatLocation = e.getBlock().getLocation().add(0.5, 0.1, 0.5);
            enableChairDismountEvent = false;
            for (Entity entity : seatLocation.getNearbyEntitiesByType(Egg.class, 0.2, 0.2, 0.2)) {
                for (Entity passenger : entity.getPassengers()) {
                    passenger.teleport(entity.getLocation().setDirection(passenger.getLocation().getDirection()));
                }
                entity.remove();
            }
            enableChairDismountEvent = true;
        }
    }
}
