package org.nautilusmc.nautilusmanager.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.util.Util;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Map;
import java.util.UUID;

public class GeneralEventManager implements Listener {

    @EventHandler
    public void onAnvil(PrepareAnvilEvent e) {
        if(e.getInventory().getRenameText() != null && !e.getInventory().getRenameText().isEmpty()) {
            e.getInventory().setRepairCost(e.getInventory().getRepairCost()-1);
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
    private static final Map<UUID, ItemStack[]> DEATH_ITEMS = new java.util.HashMap<>();

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
    @EventHandler
    public void onPlayerSit(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand().equals(EquipmentSlot.HAND) &&
                e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.AIR) &&
                (Tag.STAIRS.isTagged(e.getClickedBlock().getType()))) {
            for(Entity entity : e.getClickedBlock().getLocation().getNearbyEntities(1.5, 1.5, 1.5)) {
                if(entity instanceof Egg) return;
            }
            if (e.getPlayer().isInsideVehicle()) return;

            Egg toSitOn = (Egg) e.getClickedBlock().getLocation().getWorld().spawn(
                    e.getClickedBlock().getLocation().add(0.5, 0.2, 0.5), Egg.class, (settings) -> {
                        settings.setGravity(false);
                        settings.setInvulnerable(true);
                    });
            toSitOn.addPassenger(e.getPlayer());

        }
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent e) {
        if(e.getDismounted() instanceof Egg) {
            e.getDismounted().remove();
        }
    }

    @EventHandler
    public void onPlayerTeleportOffChair(PlayerTeleportEvent e) {
        if(e.getPlayer().getVehicle() instanceof Egg) {
            e.getPlayer().getVehicle().remove();
        }
    }

    @EventHandler
    public void onPlayerDeathOnChair(PlayerDeathEvent e) {
        if(e.getPlayer().getVehicle() instanceof Egg) {
            e.getPlayer().getVehicle().remove();
        }
    }

    @EventHandler
    public void onChairBreak(BlockBreakEvent e) {
        for(Entity entity : e.getBlock().getLocation().getNearbyEntities(1.5, 1.5, 1.5)) {
            if(entity instanceof Egg) entity.remove();
        }
    }
}
