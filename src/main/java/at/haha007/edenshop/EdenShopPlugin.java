package at.haha007.edenshop;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public final class EdenShopPlugin extends JavaPlugin {
    private static Economy economy;
    private static EdenShopPlugin plugin;

    private final List<ShopItem> items = new ArrayList<>();

    private ChestGui mainGui;

    Thread saveThread;


    public static EdenShopPlugin getPlugin() {
        return plugin;
    }

    public void onEnable() {
        plugin = this;
        economy = Objects.requireNonNull(getServer().getServicesManager().getRegistration(Economy.class)).getProvider();
        items.addAll(ItemLoader.loadItems());


        mainGui = new ChestGui(1, ComponentHolder.of(Component.text("EdenShop", NamedTextColor.GOLD)));
        mainGui.setOnGlobalClick(event -> event.setCancelled(true));
        StaticPane pane = new StaticPane(0, 0, 9, 1);

        GuiItem allItems = new GuiItem(Filters.ALL.displayItem(), event -> new ShopGui(Filters.ALL.filter(), (Player) event.getWhoClicked()).open());
        GuiItem buildingMaterials = new GuiItem(Filters.BUILDING_BLOCKS.displayItem(), event -> new ShopGui(Filters.BUILDING_BLOCKS.filter(), (Player) event.getWhoClicked()).open());
        GuiItem minerals = new GuiItem(Filters.MINERALS.displayItem(), event -> new ShopGui(Filters.MINERALS.filter(), (Player) event.getWhoClicked()).open());
        GuiItem gear = new GuiItem(Filters.EQUIPMENT.displayItem(), event -> new ShopGui(Filters.EQUIPMENT.filter(), (Player) event.getWhoClicked()).open());
        GuiItem food = new GuiItem(Filters.FOOD.displayItem(), event -> new ShopGui(Filters.FOOD.filter(), (Player) event.getWhoClicked()).open());
        GuiItem mobDrops = new GuiItem(Filters.MOB_DROPS.displayItem(), event -> new ShopGui(Filters.MOB_DROPS.filter(), (Player) event.getWhoClicked()).open());
        GuiItem magic = new GuiItem(Filters.MAGIC.displayItem(), event -> new ShopGui(Filters.MAGIC.filter(), (Player) event.getWhoClicked()).open());
        GuiItem misc = new GuiItem(Filters.MISC.displayItem(), event -> new ShopGui(Filters.MISC.filter(), (Player) event.getWhoClicked()).open());
        ItemStack searchItem = new ItemStack(Material.COMPASS);
        searchItem.editMeta(meta -> meta.displayName(Component.text("Suche", NamedTextColor.GREEN)));
        GuiItem search = new GuiItem(searchItem, e -> openSearch((Player) e.getWhoClicked()));

        pane.addItem(allItems, 0, 0);
        pane.addItem(search, 1, 0);
        pane.addItem(buildingMaterials, 2, 0);
        pane.addItem(minerals, 3, 0);
        pane.addItem(gear, 4, 0);
        pane.addItem(food, 5, 0);
        pane.addItem(mobDrops, 6, 0);
        pane.addItem(magic, 7, 0);
        pane.addItem(misc, 8, 0);
        mainGui.addPane(pane);

        saveThread = new Thread(() -> {
            while (true) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(1000 * 60 * 5);
                } catch (InterruptedException e) {
                    return;
                }
                ItemLoader.save(new ArrayList<>(items));
            }
        });
        saveThread.start();
    }

    @Override
    public void onDisable() {
        saveThread.interrupt();
        try {
            saveThread.join(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ItemLoader.save(items);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (args.length == 0) {
            openShop(player);
            return true;
        }
        try {
            double price = Double.parseDouble(args[0]);
            if (price < 0) {
                player.sendMessage(Component.text("Der Preis muss eine positive Zahl sein!", NamedTextColor.GOLD));
                return true;
            }
            if (price != price) {
                player.sendMessage(Component.text("Der Preis muss eine Zahl sein!", NamedTextColor.GOLD));
                return true;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().isAir()) {
                player.sendMessage(Component.text("Du musst ein Item in der Hand halten!", NamedTextColor.GOLD));
                return true;
            }
            if (item.getAmount() == 0) {
                player.sendMessage(Component.text("Du musst ein Item in der Hand halten!", NamedTextColor.GOLD));
                return true;
            }
            UUID uuid = player.getUniqueId();
            long count = items.stream().filter(i -> i.getOwner().equals(uuid)).count();
            if(count >= 200) {
                player.sendMessage(Component.text("Du kannst maximal 200 Items gleichzeitig im Shop haben!", NamedTextColor.GOLD));
                return true;
            }
            ShopItem shopItem = new ShopItem(item, player, price);
            items.add(shopItem);
            player.getInventory().setItemInMainHand(null);
            return true;
        } catch (NumberFormatException ignored) {
        }
        sendHelpText(player);
        return true;
    }

    public List<ShopItem> getItems() {
        items.removeIf(ShopItem::isExpired);
        return items;
    }

    private void sendHelpText(Player player) {
        MiniMessage mm = MiniMessage.miniMessage();
        player.sendMessage(mm.deserialize("<gold>EdenShop Hilfe:"));
        player.sendMessage(mm.deserialize("<gold>/shop <stückpreis> <green>- Stellt das Item in deiner Hand in den Shop"));
        player.sendMessage(mm.deserialize("<gold>/shop <green>- Öffnet den Shop"));
    }

    public void openShop(HumanEntity player) {
        mainGui.show(player);
    }

    private void openSearch(Player player) {
        AnvilGui gui = new AnvilGui(ComponentHolder.of(Component.text("Suche", NamedTextColor.GREEN)));
        StaticPane pane = new StaticPane(0, 0, 1, 1);
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        ItemStack item = new ItemStack(Material.PAPER);
        item.editMeta(meta -> meta.displayName(Component.text().build()));
        pane.addItem(new GuiItem(item, event -> {
            String text = gui.getRenameText();
            Predicate<Material> filter = m -> m.name().toLowerCase().contains(text.toLowerCase());
            new ShopGui(filter, player).open();
        }), 0, 0);
        gui.getFirstItemComponent().addPane(pane);

        item = new ItemStack(Material.PAPER);
        item.editMeta(meta -> meta.displayName(Component.text("Suche starten", NamedTextColor.GREEN)));
        pane = new StaticPane(0, 0, 1, 1);
        pane.addItem(new GuiItem(item, event -> {
            String text = gui.getRenameText();
            Predicate<Material> filter = m -> m.name().toLowerCase().contains(text.toLowerCase());
            new ShopGui(filter, player).open();
        }), 0, 0);
        gui.getResultComponent().addPane(pane);

        gui.show(player);
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return null;
        if (player.getInventory().getItemInMainHand().getType().isAir()) return null;
        return List.of("help", "1", "10", "100");
    }

    public static Economy economy() {
        return economy;
    }
}
