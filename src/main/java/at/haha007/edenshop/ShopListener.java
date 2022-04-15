package at.haha007.edenshop;

import at.haha007.edenshop.MaterialFilter.Filter;
import at.haha007.edenshop.ShopState.State;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.text.DateFormat;
import java.util.*;


public class ShopListener implements Listener {


    private final Plugin plugin = EdenShopPlugin.getPlugin();

    void clickSelection(InventoryClickEvent e) {

        e.setCancelled(true);
        if (e.getSlotType().equals(SlotType.OUTSIDE))
            return;
        if (e.getCurrentItem() == null)
            return;

        Player p = (Player) e.getWhoClicked();

        if (e.getCurrentItem().getType().equals(Material.CHEST))
            Shop.getInstance().openInv(p, new ShopState(State.NORMAL, 0, Filter.ALL));

        if (e.getCurrentItem().getType().equals(Material.GRASS_BLOCK))
            Shop.getInstance().openInv(p, new ShopState(State.NORMAL, 0, Filter.BUILDING));

        if (e.getCurrentItem().getType().equals(Material.DIAMOND))
            Shop.getInstance().openInv(p, new ShopState(State.NORMAL, 0, Filter.ORES));

        if (e.getCurrentItem().getType().equals(Material.IRON_PICKAXE))
            Shop.getInstance().openInv(p, new ShopState(State.NORMAL, 0, Filter.GEAR));

        if (e.getCurrentItem().getType().equals(Material.APPLE))
            Shop.getInstance().openInv(p, new ShopState(State.NORMAL, 0, Filter.FOOD));

        if (e.getCurrentItem().getType().equals(Material.BONE))
            Shop.getInstance().openInv(p, new ShopState(State.NORMAL, 0, Filter.LOOT));

        if (e.getCurrentItem().getType().equals(Material.ENCHANTING_TABLE))
            Shop.getInstance().openInv(p, new ShopState(State.NORMAL, 0, Filter.MAGIC));

        if (e.getCurrentItem().getType().equals(Material.COMPASS))
            Shop.getInstance().openInv(p, new ShopState(State.NORMAL, 0, Filter.OTHER));

    }

    @EventHandler
    void klick(InventoryClickEvent e) {

        if (e.getView().title().equals(CE_shop.MENU_TITLE)) {
            clickSelection(e);
            return;
        }

        if (!e.getView().title().equals(Shop.SHOP_TITLE))
            return;
        e.setCancelled(true);
        if (e.getSlotType().equals(SlotType.OUTSIDE))
            return;
        if (e.getCurrentItem() == null)
            return;

        ItemStack i = e.getCurrentItem();
        Player p = (Player) e.getWhoClicked();

        Component displayName = i.getItemMeta().displayName();
        if (displayName == null) displayName = Component.text("");

        if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()
                && displayName.equals(Component.text(ChatColor.RED + "Zurück"))) {

            CE_shop.openInventory(p);
            Shop.players.remove(p.getName());
            return;
        }

        if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()
                && displayName.equals(Component.text(ChatColor.GREEN + "Nächste Seite"))) {

            ShopState state = Shop.players.get(p.getName());
            state.next();
            Shop.getInstance().openInv(p, state);
            return;
        }

        if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()
                && displayName.equals(Component.text(ChatColor.GREEN + "Vorherige Seite"))) {

            ShopState state = Shop.players.get(p.getName());
            state.prev();
            Shop.getInstance().openInv(p, state);
            return;
        }

        if (p.isOp() && e.getSlot() == 1) {
            ShopState state = Shop.players.get(p.getName());
            state.opFilter = !state.opFilter;
            Shop.getInstance().openInv(p, state);
            return;
        }

        if (p.isOp() && e.getSlot() == 10) {
            ShopState state = Shop.players.get(p.getName());
            state.opAuslaufen = !state.opAuslaufen;
            Shop.getInstance().openInv(p, state);
            return;
        }

        if (e.getSlot() == 2) {
            ShopState state = Shop.players.get(p.getName());
            state.changeAmount();
            Shop.getInstance().openInv(p, state);
            return;
        }

        if (e.getSlot() == 3) {
            ShopState state = Shop.players.get(p.getName());
            state.state = State.NORMAL;
            Shop.getInstance().openInv(p, state);
            return;
        }

        if (e.getSlot() == 4) {
            ShopState state = Shop.players.get(p.getName());
            state.state = State.EIGENE;
            Shop.getInstance().openInv(p, state);
            return;
        }

        if (e.getSlot() == 5) {
            ShopState state = Shop.players.get(p.getName());
            state.state = State.AUSGELAUFENE;
            Shop.getInstance().openInv(p, state);
            return;
        }

        if (e.getSlot() == 6) {
            ShopState state = Shop.players.get(p.getName());
            state.state = State.ANDERE;
            Shop.getInstance().openInv(p, state);
            return;
        }

        if (!i.hasItemMeta() || !i.getItemMeta().hasLore())
            return;

        if (e.getSlot() < 18)
            return;
        if (e.getClickedInventory() != e.getView().getTopInventory())
            return;
        UUID owner;
        try {
            owner = Shop.getInstance().getOwnerUUID(i);
        } catch (IllegalStateException exception) {
            return;
        }

        // Eigenes Item angeklickt
        if (owner.equals(p.getUniqueId())) {

            if (e.getClick().equals(ClickType.RIGHT)) {
                ItemMeta meta = i.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                List<Component> lore = Objects.requireNonNull(meta.lore());

                Date d = new Date();
                DateFormat df;
                df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
                String date = df.format(d);
                String iDate = container.get(new NamespacedKey(plugin, "date"), PersistentDataType.STRING);

                if (Objects.equals(iDate, date) && !container.has(new NamespacedKey(plugin, "expired"))) {
                    p.sendMessage(ChatColor.RED + "Der Gegenstand ist von heute!");
                    return;
                }

                Shop.entries.remove(i);
                lore.remove(lore.size() - 1);
                lore.add(2, Component.text(ChatColor.GOLD + "eingestellt am: " + ChatColor.AQUA + date));
                meta.lore(lore);
                i.setItemMeta(meta);

                p.sendMessage(ChatColor.GOLD + "Item erfolgreich wieder eingestellt!");

                Shop.entries.add(i);
                Shop.getInstance().refresh();
                return;
            }

            p.sendMessage(ChatColor.GOLD + "Angebot aus dem Shop genommen.");

            Shop.entries.remove(i);
            Shop.getInstance().refresh();
            ItemUtils.give(p, removeShopLore(i));
            return;
        }

        if (Shop.players.get(p.getName()).opAuslaufen) {

            Shop.entries.remove(i);

            ItemMeta meta = i.getItemMeta();
            List<Component> lore = Objects.requireNonNull(meta.lore());
            lore.remove(2);
            lore.add(Component.text(ChatColor.RED + "von Admin als Abgelaufen markiert"));
            meta.lore(lore);
            i.setItemMeta(meta);

            Shop.entries.add(i);
            Shop.getInstance().refresh();

            return;
        }

        if (Shop.players.get(p.getName()).opFilter) {
            Shop.entries.remove(i);
            Shop.getInstance().refresh();
            return;
        }

        buyItem(p, i);

    }

    void buyItem(Player p, ItemStack i) {
        PersistentDataContainer container = i.getItemMeta().getPersistentDataContainer();
        Double price = container.get(new NamespacedKey(plugin, "price"), PersistentDataType.DOUBLE);
        long[] uuidLongs = container.get(new NamespacedKey(plugin, "owner"), PersistentDataType.LONG_ARRAY);
        UUID ownerUUID = new UUID(Objects.requireNonNull(uuidLongs)[0], uuidLongs[1]);

        int amount = Shop.players.get(p.getName()).amount;

        if (i.getAmount() < amount)
            amount = i.getAmount();

        price *= amount;

        EconomyResponse response = EdenShopPlugin.economy().withdrawPlayer(p, price);
        if (!response.transactionSuccess()) {
            p.sendMessage(ChatColor.RED + "Du kannst dir diesen Gegenstand nicht leisten.");
            return;
        }
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUID);
        EdenShopPlugin.economy().depositPlayer(owner, price - (price / 10));

        p.sendMessage(ChatColor.GOLD + "Gegenstand für " + ChatColor.GREEN + price + " $" + ChatColor.GOLD + " gekauft.");
        int index = Shop.entries.indexOf(i);
        if (i.getAmount() > amount) {
            Shop.entries.get(Shop.entries.indexOf(i)).setAmount(i.getAmount() - amount);
            i.setAmount(i.getAmount() - amount);
            ItemStack i2 = new ItemStack(i);
            i2.setAmount(amount);
            ItemUtils.give(p, removeShopLore(i2));
        } else {
            ItemUtils.give(p, removeShopLore(i));
            Shop.entries.remove(index);
        }
        Shop.getInstance().refresh();


        if (owner.isOnline())
            ((Player) owner).sendMessage(ChatColor.GOLD + "Der Spieler " + ChatColor.GREEN + p.getName()
                    + ChatColor.GOLD + " hat " + ChatColor.GREEN + amount + " x " + ChatColor.GOLD
                    + "den Gegenstand " + ChatColor.GREEN + i.getType().toString().toLowerCase() + ChatColor.GOLD
                    + " von dir für " + ChatColor.GREEN + price + " $" + ChatColor.GOLD + " gekauft.");

    }

    ItemStack removeShopLore(ItemStack i) {
        ItemMeta meta = i.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        List<Component> lore = meta.lore();
        if (lore == null)
            throw new IllegalStateException();

        if (container.has(new NamespacedKey(plugin, "expired"))) {
            for (int i2 = 0; i2 <= 1; i2++) {
                lore.remove(0);
            }
            lore.remove(lore.size() - 1);
        } else {
            for (int i2 = 0; i2 <= 2; i2++) {
                lore.remove(0);
            }
        }
        meta.lore(lore.isEmpty() ? null : lore);
        container.remove(new NamespacedKey(plugin, "price"));
        container.remove(new NamespacedKey(plugin, "time"));
        container.remove(new NamespacedKey(plugin, "owner"));
        container.remove(new NamespacedKey(plugin, "expired"));
        i.setItemMeta(meta);
        return i;
    }

    @EventHandler
    void close(final InventoryCloseEvent e) {
        if (!e.getView().title().equals(Shop.SHOP_TITLE))
            return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(EdenShopPlugin.getPlugin(), () -> {
            if (!e.getPlayer().getOpenInventory().getType().equals(InventoryType.CHEST))
                Shop.players.remove(e.getPlayer().getName());
        }, 1);
    }

}
