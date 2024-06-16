package at.haha007.edenshop;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ItemLoader {

    private ItemLoader() {
        throw new IllegalStateException("Utility class");
    }

    public static List<ShopItem> loadItems() {
        EdenShopPlugin.getPlugin().reloadConfig();
        FileConfiguration cfg = EdenShopPlugin.getPlugin().getConfig();
        if (cfg.contains("items")) {
            List<ShopItem> shopItems = loadLegacy();
            save(shopItems);
            return shopItems;
        }
        List<ShopItem> shopItems = new ArrayList<>();
        for (String key : cfg.getKeys(false)) {
            try {
                ItemStack stack = null;
                if (cfg.contains(key + ".stack"))
                    stack = cfg.getItemStack(key + ".stack");
                if (stack == null) {
                    byte[] bytes = Base64.getDecoder().decode(cfg.getString(key + ".base64stack"));
                    stack = ItemStack.deserializeBytes(bytes);
                }
                String name = cfg.getString(key + ".name");
                double price = cfg.getDouble(key + ".price");
                long time = cfg.getLong(key + ".time");
                UUID uuid = UUID.fromString(Objects.requireNonNull(cfg.getString(key + ".uuid")));
                if (stack == null || name == null) {
                    EdenShopPlugin.getPlugin().getLogger().severe("Error while loading item " + key);
                    continue;
                }
                ShopItem item = new ShopItem(price, uuid, name, time, stack);
                shopItems.add(item);
            } catch (Exception e) {
                System.err.println("Error while loading item " + key);
                e.printStackTrace();
            }
        }
        return shopItems;
    }

    public static void save(List<ShopItem> items) {
        FileConfiguration cfg = EdenShopPlugin.getPlugin().getConfig();
        cfg.getKeys(false).stream().toList().forEach(s -> cfg.set(s, null));
        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            cfg.set(i + ".stack", item.getItem());
            cfg.set(i + ".base64stack", Base64.getEncoder().encodeToString(item.getItem().serializeAsBytes()));
            cfg.set(i + ".name", item.getOwnerName());
            cfg.set(i + ".price", item.getPrice());
            cfg.set(i + ".time", item.getInShopSince());
            cfg.set(i + ".uuid", item.getOwner().toString());
        }
        EdenShopPlugin.getPlugin().saveConfig();
    }

    private static List<ShopItem> loadLegacy() {
        FileConfiguration cfg = EdenShopPlugin.getPlugin().getConfig();
        List<ShopItem> shopItems = new ArrayList<>();
        List<?> list = cfg.getList("items");
        if (list == null) throw new IllegalStateException();
        for (Object o : list) {
            if (!(o instanceof ItemStack displayStack)) {
                System.err.println("Error while loading item " + o);
                continue;
            }
            ItemStack stack = displayStack.clone();
            ItemMeta meta = stack.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.getKeys().stream()
                    .filter(k -> k.getNamespace().equalsIgnoreCase(EdenShopPlugin.getPlugin().getName()))
                    .forEach(pdc::remove);
            List<Component> lore = meta.lore();
            if (lore == null) lore = new ArrayList<>();
            else {
                lore = new ArrayList<>(lore);
                lore.remove(0);
                lore.remove(0);
                lore.remove(0);
            }
            meta.lore(lore);
            stack.setItemMeta(meta);

            pdc = displayStack.getItemMeta().getPersistentDataContainer();
            Double price = pdc.get(new NamespacedKey(EdenShopPlugin.getPlugin(), "price"), PersistentDataType.DOUBLE);
            Long time = pdc.get(new NamespacedKey(EdenShopPlugin.getPlugin(), "time"), PersistentDataType.LONG);
            UUID uuid = getOwnerUUID(displayStack);
            String ownerName = Bukkit.getOfflinePlayer(uuid).getName();
            if (price == null) {
                System.err.println("Error while loading item " + o);
                continue;
            }
            if (time == null) time = System.currentTimeMillis();
            ShopItem item = new ShopItem(price, uuid, ownerName, time, stack);
            shopItems.add(item);
        }
        return shopItems;
    }

    private static UUID getOwnerUUID(ItemStack i) {
        long[] longs = i.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(EdenShopPlugin.getPlugin(), "owner"), PersistentDataType.LONG_ARRAY);
        if (longs == null) throw new IllegalStateException();
        return new UUID(longs[0], longs[1]);
    }
}
