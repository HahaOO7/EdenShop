package at.haha007.edenshop;

import at.haha007.edenshop.ShopState.State;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.text.DateFormat;
import java.util.*;
import java.util.stream.Stream;


public class Shop {
    public static final Component SHOP_TITLE = Component.text(ChatColor.GREEN + "Auktionshaus");

    private static Shop instance;

    static List<ItemStack> entries = new ArrayList<>();
    static HashMap<String, ShopState> players = new HashMap<>();
    private final Plugin plugin = EdenShopPlugin.getPlugin();

    private Shop() {
        FileConfiguration config = getConfig();
        entries = (List<ItemStack>) config.getList("items");
        if (entries == null)
            entries = new ArrayList<>();
        cleanup(false);
    }

    public static Shop getInstance() {
        if (instance == null)
            instance = new Shop();
        return instance;
    }

    void refresh() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!players.containsKey(p.getName()))
                continue;
            openInv(p, players.get(p.getName()));
        }
    }

    void openInv(Player p, ShopState state) {
        if (!players.containsKey(p.getName()))
            players.put(p.getName(), state);

        Inventory inv = Bukkit.createInventory(null, 54, SHOP_TITLE);
        int invNumber = state.getPage();

        inv.setItem(0, infoBook());

        if (p.isOp())
            inv.setItem(1, opTool(state));
        if (p.isOp())
            inv.setItem(10, opExpire(state));

        inv.setItem(2, amount(state));
        inv.setItem(3, filter(State.NORMAL, state.state));
        inv.setItem(4, filter(State.EIGENE, state.state));
        inv.setItem(5, filter(State.AUSGELAUFENE, state.state));
        inv.setItem(6, filter(State.ANDERE, state.state));

        inv.setItem(7, siteChange(false, state));
        inv.setItem(8, siteChange(true, state));
        inv.setItem(9, ItemUtils.item(Material.BARRIER, 1, ChatColor.RED + "Zurück"));

        ArrayList<ItemStack> filtered = new ArrayList<>();

        MaterialFilter filter = new MaterialFilter();

        if (state.filter.equals(MaterialFilter.Filter.OTHER)) {
            for (ItemStack i : entries)
                if (filter.check(i) && matches(p, i, state))
                    filtered.add(i);
        } else
            for (ItemStack i : entries)
                if (filter.check(i, state.filter) && matches(p, i, state))
                    filtered.add(i);

        for (int i = 18; i <= 53; i++) {

            if (filtered.size() > i - 18 + invNumber * 36)
                inv.setItem(i, filtered.get(i - 18 + invNumber * 36));
        }

        p.openInventory(inv);
    }

    private ItemStack opTool(ShopState state) {

        ItemStack i = new ItemStack(Material.BARRIER, 1);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(ChatColor.RED + "Admin Tool"));
        meta.lore(Stream.of(ChatColor.AQUA + "Du siehst dies, da du OP bist.",
                ChatColor.AQUA + "Aktiviere dies um absolut alle Angebote sehen zu können.",
                ChatColor.AQUA + "Items werden beim Klick entfernt!").map(c -> (Component) Component.text(c)).toList());

        if (state.opFilter) {
            List<Component> lore = meta.lore();
            if (lore == null) lore = new ArrayList<>();
            lore.add(Component.text(ChatColor.DARK_RED + "*" + ChatColor.YELLOW +
                    " Momentan Ausgewählt " + ChatColor.DARK_RED + "*"));
            meta.lore(lore);
            meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 1, true);
        }

        i.setItemMeta(meta);

        return i;
    }

    private ItemStack opExpire(ShopState state) {
        ItemStack i = new ItemStack(Material.REDSTONE, 1);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(ChatColor.RED + "Items Auslaufen lassen"));
        meta.lore(Stream.of(ChatColor.AQUA + "Du siehst dies, da du OP bist.",
                        ChatColor.AQUA + "Aktiviere dies um angeklickte Items",
                        ChatColor.AQUA + "als abgelaufen zu markieren.")
                .map(s -> (Component) Component.text(s)).toList());

        if (state.opAuslaufen) {
            List<Component> lore = meta.lore();
            if (lore == null) lore = new ArrayList<>();
            lore.add(Component.text(ChatColor.DARK_RED + "*" + ChatColor.YELLOW +
                    " Momentan Ausgewählt " + ChatColor.DARK_RED + "*"));
            meta.lore(lore);
            meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 1, true);
        }
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack amount(ShopState state) {
        ItemStack i = new ItemStack(Material.WHITE_WOOL, state.amount);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(ChatColor.GREEN + "Anzahl: " + ChatColor.YELLOW + state.amount));
        meta.lore(Stream.of(ChatColor.AQUA + "Anzahl Items die auf einmal gekauft werden.",
                ChatColor.AQUA + "Klicken, um es zu ändern.").map(c -> (Component) Component.text(c)).toList());
        i.setItemMeta(meta);
        return i;
    }

    public UUID getOwnerUUID(ItemStack i) {
        long[] longs = i.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "owner"), PersistentDataType.LONG_ARRAY);
        if (longs == null) throw new IllegalStateException();
        return new UUID(longs[0], longs[1]);
    }

    boolean matches(Player p, ItemStack i, ShopState state) {

        UUID owner = getOwnerUUID(i);
        boolean isOwner = owner.equals(p.getUniqueId());
        boolean expired = i.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "expired"));

        // nur Besitzer sieht abgelaufene Angebote
        if (expired && !isOwner && !state.opFilter)
            return false;

        if (state.state.equals(State.NORMAL))
            return true;

        if (state.state.equals(State.EIGENE) && isOwner)
            return true;

        if (state.state.equals(State.ANDERE))
            return !isOwner;


        if (state.state.equals(State.AUSGELAUFENE)) {
            return (isOwner || state.opFilter) && expired;
        }

        return false;
    }

    ItemStack infoBook() {

        ItemStack i = new ItemStack(Material.BOOK);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(ChatColor.GREEN + "- " + ChatColor.GOLD + "Info " + ChatColor.GREEN + "-"));
        meta.lore(Stream.of(ChatColor.AQUA + "Klicke auf ein Item um es zu kaufen.",
                        ChatColor.AQUA + "Klicke auf deine eigenen Items um sie zurückzunehmen.",
                        ChatColor.AQUA + "Benutze das Papier um die Seiten zu wechseln.",
                        ChatColor.AQUA + "Mache /shop info für weitere Informationen.",
                        ChatColor.RED + "Mit einem Rechstklick kannst du Items verlängern / neu einstellen!")
                .map(s -> (Component) Component.text(s)).toList());
        i.setItemMeta(meta);

        return i;
    }

    ItemStack filter(State normal, State state) {

        ItemStack i = new ItemStack(Material.HOPPER);
        ItemMeta meta = i.getItemMeta();

        if (normal.equals(State.NORMAL)) {
            meta.displayName(Component.text(ChatColor.GREEN + "Alles"));
            meta.lore(Stream.of(ChatColor.AQUA + "Zeige alle verfügbaren Angebote.")
                    .map(s -> (Component) Component.text(s)).toList());
        }

        if (normal.equals(State.EIGENE)) {
            meta.displayName(Component.text(ChatColor.GREEN + "Eigene"));
            meta.lore(Stream.of(ChatColor.AQUA + "Zeige nur deine eigenen Angebote.")
                    .map(s -> (Component) Component.text(s)).toList());
        }

        if (normal.equals(State.AUSGELAUFENE)) {
            meta.displayName(Component.text(ChatColor.GREEN + "Ausgelaufen"));
            meta.lore(Stream.of(ChatColor.AQUA + "Zeige nur deine ausgelaufenen Angebote.")
                    .map(s -> (Component) Component.text(s)).toList());
        }

        if (normal.equals(State.ANDERE)) {
            meta.displayName(Component.text(ChatColor.GREEN + "Andere"));
            meta.lore(Stream.of(ChatColor.AQUA + "Zeige nur Angebote von anderen Spielern.")
                    .map(s -> (Component) Component.text(s)).toList());
        }

        if (normal.equals(state)) {
            List<Component> lore = meta.lore();
            if (lore == null) lore = new ArrayList<>();
            lore.add(Component.text(ChatColor.DARK_RED + "*" + ChatColor.YELLOW +
                    " Momentan Ausgewählt " + ChatColor.DARK_RED + "*"));
            meta.lore(lore);
            meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 1, true);
        }

        i.setItemMeta(meta);

        return i;
    }

    ItemStack siteChange(boolean forward, ShopState state) {

        ItemStack i = new ItemStack(Material.PAPER);
        ItemMeta meta = i.getItemMeta();
        if (forward)
            meta.displayName(Component.text(ChatColor.GREEN + "Nächste Seite"));
        else
            meta.displayName(Component.text(ChatColor.GREEN + "Vorherige Seite"));
        meta.lore(
                Stream.of(ChatColor.GOLD + "Aktuell auf Seite: " + ChatColor.DARK_RED + (state.getPage() + 1))
                        .map(s -> (Component) Component.text(s)).toList());
        i.setItemMeta(meta);

        return i;
    }

    public void save() {
        FileConfiguration config = plugin.getConfig();
        config.set("items", entries);
        plugin.saveConfig();
    }

    public void cleanup(boolean debug) { // debug zum künstlichen auslaufen lassen der Items

        for (ItemStack i : entries) {
            ItemMeta meta = i.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            List<Component> lore = meta.lore();
            if (lore == null)
                continue;
            if (container.has(new NamespacedKey(plugin, "expired")))
                continue;

            Long date = container.get(new NamespacedKey(plugin, "time"), PersistentDataType.LONG);
            if (date == null) date = 0L;

            long diff = System.currentTimeMillis() - date;
            diff = diff / 1000 / 60 / 60 / 24;

            if (debug)
                diff = 8; // künstliches Auslaufen

            if (diff > 7) {
                Date d = new Date();
                DateFormat df;
                df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
                String date2 = df.format(d);

                lore.remove(2);
                lore.add(Component.text(ChatColor.RED + "Abgelaufen am " + date2));
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "expired"), PersistentDataType.BYTE, (byte) 1);
                meta.lore(lore);
                i.setItemMeta(meta);
            }

        }

    }

    private FileConfiguration getConfig() {
        plugin.reloadConfig();
        return plugin.getConfig();
    }
}
