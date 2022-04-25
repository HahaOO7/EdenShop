package at.haha007.edenshop;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.*;
import java.util.stream.Stream;

public class CE_shop implements CommandExecutor {

    public static final Component MENU_TITLE = Component.text(ChatColor.YELLOW + "Wähle die sichtbaren Items:");
    private final Plugin plugin = EdenShopPlugin.getPlugin();

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player))
            return true;

        if (args.length > 1) {
            player.sendMessage(ChatColor.RED + "Du hast den Befehl falsch genutzt. " + ChatColor.YELLOW + "/shop info");
            return true;
        }

        if (args.length == 0) {
            openInventory(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            info(player);
            return true;
        }

        if (player.getInventory().getItemInMainHand().getAmount() == 0) {
            player.sendMessage(ChatColor.RED + "Du hast kein Item in der Hand.");
            return true;
        }

        double price;
        try {
            String parse = args[0].replace(",", ".");
            price = Double.parseDouble(parse);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Das Argument muss eine Zahl sein.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        long entries = Shop.entries.stream().map(Shop.getInstance()::getOwnerUUID).filter(uuid::equals).count();
        if(entries > 216){
            player.sendMessage(ChatColor.RED + "Du kannst maximal 216 Items gleichzeitig im Shop haben.");
            return true;
        }

        add(player, price);

        return true;
    }

    static void openInventory(Player p) {
        Inventory inv = Bukkit.createInventory(null, 9, MENU_TITLE);

        inv.setItem(0, ItemUtils.item(Material.CHEST, 1, ChatColor.GREEN + "Alle Items"));
        inv.setItem(2, ItemUtils.item(Material.GRASS_BLOCK, 1, ChatColor.GREEN + "Baumaterial"));
        inv.setItem(3, ItemUtils.item(Material.DIAMOND, 1, ChatColor.GREEN + "Mineralien"));
        inv.setItem(4, ItemUtils.item(Material.IRON_PICKAXE, 1, ChatColor.GREEN + "Ausrüstung & Werkzeuge"));
        inv.setItem(5, ItemUtils.item(Material.APPLE, 1, ChatColor.GREEN + "Nahrung"));
        inv.setItem(6, ItemUtils.item(Material.BONE, 1, ChatColor.GREEN + "Monster- & Tierdrops"));
        inv.setItem(7, ItemUtils.item(Material.ENCHANTING_TABLE, 1, ChatColor.GREEN + "Magisches & Tränke"));
        inv.setItem(8, ItemUtils.item(Material.COMPASS, 1, ChatColor.GREEN + "Sonstiges"));

        p.openInventory(inv);
    }

    void info(Player p) {

        p.sendMessage("");
        p.sendMessage("");
        p.sendMessage("");
        p.sendMessage(ChatColor.YELLOW + "-" + ChatColor.GOLD + " Auktionshaus Info " + ChatColor.YELLOW + "-");
        p.sendMessage(ChatColor.YELLOW + "*" + ChatColor.AQUA
                + "Im Auktionshaus kannst du beliebig viele Items zum Verkauf anbieten.");
        p.sendMessage(ChatColor.YELLOW + "*" + ChatColor.AQUA + "Benutze " + ChatColor.GREEN + "/shop" + ChatColor.AQUA
                + " um das Auktionshaus zu öffnen.");
        p.sendMessage(ChatColor.YELLOW + "*" + ChatColor.AQUA + "Mit " + ChatColor.GREEN + "/shop Preis "
                + ChatColor.AQUA + "bietest du die Items in deiner Hand zum Verkauf an. Zum Beispiel: "
                + ChatColor.GREEN + "/shop 0.5");
        p.sendMessage(ChatColor.YELLOW + "*" + ChatColor.AQUA + "Jedes deiner Items ist bis zu " + ChatColor.GREEN
                + "7 Tage" + ChatColor.AQUA
                + " im Auktionshaus verfügbar. Danach kann es nur noch von dir selbst gesehen werden. Klicke es an um es aus dem Shop rauszunehmen.");
        p.sendMessage(ChatColor.YELLOW + "*" + ChatColor.AQUA + "Klicke auf ein " + ChatColor.GREEN + "Item"
                + ChatColor.AQUA + " um es zu kaufen. Klicke auf die " + ChatColor.GREEN + "Blätter" + ChatColor.AQUA
                + " oben rechts um die angezeigte Seite im Shop zu ändern.");
        p.sendMessage(ChatColor.YELLOW + "*" + ChatColor.AQUA + "Mit den " + ChatColor.GREEN + "Trichtern"
                + ChatColor.AQUA
                + " kannst du einstellen welche Angebote du sehen willst - alle, deine eigenen, abgelaufene Items oder nur Items von anderen Spielern.");
        p.sendMessage(ChatColor.YELLOW + "*" + ChatColor.AQUA + "Klicke auf den " + ChatColor.GREEN + "Wollblock"
                + ChatColor.AQUA + " um die Anzahl Items zu ändern welche auf einmal gekauft werden.");
        p.sendMessage(ChatColor.YELLOW + "*" + ChatColor.RED + "Achtung: " + ChatColor.AQUA
                + "Von dem Gewinn den du erhältst werden " + ChatColor.GREEN + "10 %" + ChatColor.AQUA
                + " Verkaufsgebühr abgezogen.");

    }

    void add(Player player, double price) {
        ItemStack i = player.getInventory().getItemInMainHand();
        ItemMeta meta = i.getItemMeta();

        Date d = new Date();
        DateFormat df;
        df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
        String date = df.format(d);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(new NamespacedKey(plugin, "time"), PersistentDataType.LONG, System.currentTimeMillis());
        UUID uuid = player.getUniqueId();
        long[] uuidLongs = new long[]{uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()};
        container.set(new NamespacedKey(plugin, "owner"), PersistentDataType.LONG_ARRAY, uuidLongs);
        container.set(new NamespacedKey(plugin, "price"), PersistentDataType.DOUBLE, price);

        if (meta.hasLore()) {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(ChatColor.GOLD + "Preis pro Stück: " + ChatColor.GREEN + price));
            lore.add(Component.text(ChatColor.GOLD + "Verkäufer: " + ChatColor.AQUA + player.getName()));
            lore.add(Component.text(ChatColor.GOLD + "eingestellt am: " + ChatColor.AQUA + date));
            lore.addAll(Objects.requireNonNull(meta.lore()));
            meta.lore(lore);
        } else {
            meta.lore(Stream.of(ChatColor.GOLD + "Preis pro Stück: " + ChatColor.GREEN + price,
                            ChatColor.GOLD + "Verkäufer: " + ChatColor.AQUA + player.getName(),
                            ChatColor.GOLD + "eingestellt am: " + ChatColor.AQUA + date)
                    .map(c -> (Component) Component.text(c)).toList());
        }

        i.setItemMeta(meta);
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(ChatColor.GOLD + "Items erfolgreich zum Verkauf angeboten.");
        Shop.entries.add(i);
        Shop.getInstance().refresh();
    }
}
