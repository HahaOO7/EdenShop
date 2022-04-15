package at.haha007.edenshop;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand;

public class ItemUtils {
    public static ItemStack item(Material material, int amount, String name) {
        ItemStack item = new ItemStack(material, amount);
        item.editMeta(meta -> meta.displayName(legacyAmpersand().deserialize(name)));
        return item;
    }

    public static void give(Player p, ItemStack item) {
        Inventory inv = p.getInventory();
        HashMap<Integer, ItemStack> remainding = inv.addItem(item);
        World world = p.getWorld();
        for (ItemStack stack : remainding.values()) {
            Item e = world.dropItem(p.getLocation(), stack);
            e.setOwner(p.getUniqueId());
        }
    }
}
