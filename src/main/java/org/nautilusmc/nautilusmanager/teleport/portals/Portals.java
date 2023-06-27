package org.nautilusmc.nautilusmanager.teleport.portals;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftShapedRecipe;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.sql.SQLHandler;
import org.nautilusmc.nautilusmanager.sql.SQLSyncedPerPlayerList;
import org.nautilusmc.nautilusmanager.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Portals {

    public static final int MAX_LINKED_PORTALS = 99;
    public static final int MAX_NAME_LENGTH = 16;

    private static final List<ShapedRecipe> RECIPES = new ArrayList<>();
//    protected static final Map<NamespacedKey, Map<Integer, Integer>> COUNTS = new HashMap<>();

    private static final Map<UUID, Portal> portals = new HashMap<>();

    private static final SQLSyncedPerPlayerList<UUID, String> linkedPortalsSQL = new SQLSyncedPerPlayerList<>(String.class, UUID::toString, UUID::fromString, "portal_id", MAX_LINKED_PORTALS);
    private static SQLHandler portalSQL;


    @SuppressWarnings("unchecked")
    public static void init() {
        portalSQL = new SQLHandler("portals") {
            @Override
            public void update(ResultSet results) throws SQLException {
                while (results.next()) {
                    UUID id = UUID.fromString(results.getString("uuid"));
                    String name = results.getString("name");
                    Location loc = sqlToLocation(results);

                    Portal existing = portals.getOrDefault(id, new Portal(null, null, null, new ArrayList<>()));
                    portals.put(id, new Portal(id, name, loc, existing.linked));
                }
            }
        };
        linkedPortalsSQL.initSQL("linked_portals");

        try {
            Configuration config = NautilusManager.INSTANCE.getConfig();
            int i = 0;

            for (LinkedHashMap<String, Object> configRecipe : (List<LinkedHashMap<String, Object>>) config.getList("portals.recipes")) {
                ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(NautilusManager.INSTANCE, "portal" + i++), getPortalItem());
                recipe.setCategory(CraftingBookCategory.MISC);

                recipe.shape(((List<String>) configRecipe.get("shape")).toArray(String[]::new));

                for (Map.Entry<String, LinkedHashMap<String, Object>> entry : ((LinkedHashMap<String, LinkedHashMap<String, Object>>) configRecipe.get("ingredients")).entrySet()) {
                    recipe.setIngredient(entry.getKey().charAt(0), new ItemStack(
                            Material.matchMaterial(entry.getValue().get("id").toString()),
                            (int) entry.getValue().getOrDefault("count", 1)));
                }

//                COUNTS.put(recipe.getKey(), configRecipe.containsKey("counts") ?
//                        ((LinkedHashMap<Integer, Integer>) configRecipe.get("counts")) :
//                        new HashMap<>());
                RECIPES.add(recipe);
            }
        } catch (Exception e) {
            NautilusManager.INSTANCE.getLogger().warning("Failed to load portal recipes: " + e.getMessage());
        }
    }

    public static ItemStack getPortalItem() {
        return new ItemStack(Material.BEACON);
    }

    public static ShapedRecipe getRecipe(NamespacedKey key) {
        if (!key.getNamespace().equals(NautilusManager.INSTANCE.getName().toLowerCase()) || !key.getKey().matches("^portal\\d+$")) return null;
        return RECIPES.get(Integer.parseInt(key.getKey().substring("portal".length())));
    }

    public static void sendRecipe(Player player) {
        // ServerPlaceRecipe#recipeClicked: ClientboundPlaceGhostRecipePacket
        ServerPlayer nms = ((CraftPlayer) player).getHandle();

        Collection<Recipe<?>> recipes = ((CraftServer) Bukkit.getServer()).getHandle().getServer().getRecipeManager().getRecipes();
        recipes.addAll(RECIPES.stream().map(Util::getShapedNMSRecipe).toList());
        nms.connection.send(new ClientboundUpdateRecipesPacket(recipes));

        nms.connection.send(new ClientboundRecipePacket(
                ClientboundRecipePacket.State.INIT,
                List.of(CraftNamespacedKey.toMinecraft(RECIPES.get(0).getKey())),
                List.of(),
                nms.getRecipeBook().getBookSettings()));
        nms.getRecipeBook().add(Util.getShapedNMSRecipe(RECIPES.get(0)));
    }

    private static Portal getPortal(Location loc) {
        return portals.values().stream()
                .filter(portal -> portal.loc.equals(loc))
                .findFirst()
                .orElse(null);
    }

    public static void placePortal(String name, Location loc) {
        Portal portal = new Portal(UUID.randomUUID(), name, loc, new ArrayList<>());

        portals.put(portal.id, portal);

        Map<String, Object> sql = new HashMap<>(SQLHandler.locationToSql(loc));
        sql.put("name", portal.name.substring(0, MAX_NAME_LENGTH));
        portalSQL.setValues(portal.id, sql);
    }

    public static void breakPortal(Location loc) {
        Portal portal = getPortal(loc);

        portals.remove(portal.id);
        portalSQL.deleteEntry(portal.id);

        portals.values().forEach(p -> p.linked.remove(portal.id));
        linkedPortalsSQL.remove(portal.id);

        // TODO: deal with linkers currently bound to the broken portal
    }

    public static void linkPortals(Location loc1, Location loc2) {
        if (loc1.equals(loc2)) return;

        Portal portal1 = getPortal(loc1);
        Portal portal2 = getPortal(loc2);

        portal1.linked.add(portal2.id);
        portal2.linked.add(portal1.id);

        linkedPortalsSQL.add(portal1.id, portal2.id);
        linkedPortalsSQL.add(portal2.id, portal1.id);
    }

    private record Portal(UUID id, String name, Location loc, List<UUID> linked) {}
}
