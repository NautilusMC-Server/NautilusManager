package org.nautilusmc.nautilusmanager.events;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.teleport.portals.Portals;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.Arrays;
import java.util.stream.Stream;

public class PortalHandler implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Portals.sendRecipe(e.getPlayer());
    }

    @EventHandler
    public void onRecipeClick(PlayerRecipeBookClickEvent e) {
        if (e.getRecipe().getNamespace().equals(NautilusManager.INSTANCE.getName().toLowerCase()) && e.getRecipe().getKey().startsWith("portal")) {
            ServerPlayer player = ((CraftPlayer) e.getPlayer()).getHandle();
            ShapedRecipe recipe = Util.getShapedNMSRecipe(Portals.getRecipe(e.getRecipe()));
            RecipeBookMenu<CraftingContainer> menu = (RecipeBookMenu<CraftingContainer>) ((CraftInventoryView) e.getPlayer().getOpenInventory()).getHandle();

            new ServerPlaceRecipe<>(menu).recipeClicked(player, recipe, e.isMakeAll());
//            int recipes = recipe.getIngredients().stream().map(Ingredient::getItems).flatMap(Stream::of).map(i->e.getPlayer().getInventory().all(i.getBukkitStack()).keySet().stream().mapToInt(x->x).sum() / i.getCount()).min(Integer::compareTo).orElse(0);
//            if (recipes > 0) {
//
//            } else {
//                ((CraftPlayer) e.getPlayer()).getHandle().connection.send(new ClientboundPlaceGhostRecipePacket(
//                        ((CraftInventoryView) e.getPlayer().getOpenInventory()).getHandle().containerId,
//                        recipe
//                ));
//            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {

    }
}
