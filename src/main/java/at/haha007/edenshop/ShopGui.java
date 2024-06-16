package at.haha007.edenshop;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ShopGui {
    private enum OwnedState {
        ALL("ALLE", (u, i) -> true), ONLY_OWNED("EIGENE", (u, i) -> i.getOwner().equals(u)), ONLY_NOT_OWNED("FREMDE", (u, i) -> !i.getOwner().equals(u));
        private final String name;
        private final BiPredicate<UUID, ShopItem> predicate;

        OwnedState(String name, BiPredicate<UUID, ShopItem> predicate) {
            this.name = name;
            this.predicate = predicate;
        }

        public String getName() {
            return name;
        }

        public boolean test(UUID uuid, ShopItem item) {
            return predicate.test(uuid, item);
        }

        public OwnedState next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private final ChestGui gui = new ChestGui(6, ComponentHolder.of(Component.text("Shop", NamedTextColor.GREEN)));
    private final PaginatedPane pages = new PaginatedPane(0, 0, 9, 6);
    private OwnedState onlyOwned = OwnedState.ALL;
    private final Predicate<Material> filter;
    private final Player player;
    private int amount = 1;

    public ShopGui(Predicate<Material> filter, Player player) {
        this.filter = filter;
        this.player = player;
        updateGui();
        gui.setOnGlobalClick(event -> event.setCancelled(true));
    }

    public void open() {
        gui.show(player);
    }

    private void updateGui() {
        List<StaticPane> panes = new ArrayList<>();
        List<ShopItem> items = getItems();
        int pageCount = (items.size() - 1) / 45 + 1;
        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            int pageNr = i / 45;
            StaticPane pane;
            if (panes.size() <= pageNr) {
                pane = createPane(pageNr, pageCount);
                panes.add(pane);
            } else {
                pane = panes.get(pageNr);
            }
            ItemStack displayItem = item.displayItem(player.getUniqueId());
            if (displayItem == null) {
                EdenShopPlugin.getPlugin().getLogger().warning("displayItem == null");
                EdenShopPlugin.getPlugin().getLogger().warning("item = %s".formatted(item));
                continue;
            }
            GuiItem guiItem = new GuiItem(item.displayItem(player.getUniqueId()), event -> {
                if (event.isRightClick() && player.getUniqueId().equals(item.getOwner())) {
                    item.setInShopSince(System.currentTimeMillis());
                    updateGui();
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Item erneut angeboten!"));
                    return;
                }
                if (!event.isLeftClick()) return;
                item.sell(player, amount);
                updateGui();
            });
            pane.addItem(guiItem, i % 9, (i % 45) / 9 + 1);
        }

        if (panes.isEmpty())
            panes.add(createPane(0, 1));
        pages.clear();
        for (int i = 0; i < panes.size(); i++) {
            pages.addPane(i, panes.get(i));
        }
        gui.getPanes().clear();
        gui.addPane(pages);
        gui.update();
    }

    private StaticPane createPane(int pageNr, int pageCount) {
        StaticPane pane = new StaticPane(0, 0, 9, 6);
        if (pageNr > 0) pane.addItem(prevPageItem(pageNr), 5, 0);
        if (pageNr < pageCount - 1) pane.addItem(nextPageItem(pageNr), 6, 0);
        pane.addItem(getInfoItem(), 0, 0);
        pane.addItem(backItem(), 8, 0);
        pane.addItem(getCountItem(), 2, 0);
        pane.addItem(getOwnedStateItem(), 1, 0);
        return pane;
    }

    private GuiItem getOwnedStateItem() {
        ItemStack stack = new ItemStack(Material.SPYGLASS);
        MiniMessage mm = MiniMessage.miniMessage();
        stack.editMeta(meta -> meta.displayName(mm.deserialize("<!italic><green>Zeige: <gold>" + onlyOwned.getName())));
        return new GuiItem(stack, event -> {
            if (!event.isLeftClick()) return;
            onlyOwned = onlyOwned.next();
            updateGui();
            pages.setPage(0);
            gui.update();
        });
    }

    private GuiItem getInfoItem() {
        ItemStack stack = new ItemStack(Material.BOOK);
        MiniMessage mm = MiniMessage.miniMessage();
        stack.editMeta(meta -> meta.displayName(mm.deserialize("<!italic><gold>- <green>Info <gold>-")));
        stack.editMeta(meta -> meta.lore(List.of(
                mm.deserialize("<!italic><aqua>Klicke auf ein Item um es zu kaufen."),
                mm.deserialize("<!italic><aqua>Linksklick auf deine eigenen Items um sie zurückzunehmen."),
                mm.deserialize("<!italic><aqua>Rechtsklick auf deine eigenen Items um sie zu erneuern."),
                mm.deserialize("<!italic><aqua>Benutze <gold>/shop <stückpreis> <aqua>um Items zu verkaufen."),
                mm.deserialize("<!italic><aqua>Items laufen nach einer Woche ab."),
                mm.deserialize("<!italic><aqua>Abgelaufene Items werden nach einem Jahr permanent gelöscht.")
        )));
        return new GuiItem(stack, event -> event.setCancelled(true));
    }

    private GuiItem backItem() {
        ItemStack stack = new ItemStack(Material.BARRIER);
        MiniMessage mm = MiniMessage.miniMessage();
        stack.editMeta(meta -> meta.displayName(mm.deserialize("<!italic><red>Zurück")));
        return new GuiItem(stack, event -> EdenShopPlugin.getPlugin().openShop(player));
    }

    private GuiItem nextPageItem(int page) {
        ItemStack stack = new ItemStack(Material.ARROW);
        MiniMessage mm = MiniMessage.miniMessage();
        stack.editMeta(meta -> meta.displayName(mm.deserialize("<!italic><green>Nächste Seite")));
        stack.editMeta(meta -> meta.lore(List.of(mm.deserialize("<!italic><aqua>Seite: <gold>" + (page + 1)))));
        return new GuiItem(stack, event -> {
            if (pages.getPage() < pages.getPages() - 1) {
                pages.setPage(pages.getPage() + 1);
                gui.update();
            }
        });
    }

    private GuiItem prevPageItem(int page) {
        ItemStack stack = new ItemStack(Material.ARROW);
        MiniMessage mm = MiniMessage.miniMessage();
        stack.editMeta(meta -> meta.displayName(mm.deserialize("<!italic><green>Vorherige Seite")));
        stack.editMeta(meta -> meta.lore(List.of(mm.deserialize("<!italic><aqua>Seite: <gold>" + (page + 1)))));
        return new GuiItem(stack, event -> {
            if (pages.getPage() > 0) {
                pages.setPage(pages.getPage() - 1);
                gui.update();
            }
        });
    }

    private GuiItem getCountItem() {
        ItemStack stack = new ItemStack(Material.PAPER);
        stack.setAmount(amount);
        MiniMessage mm = MiniMessage.miniMessage();
        stack.editMeta(meta -> meta.displayName(mm.deserialize("<!italic><green>Anzahl: <gold>" + amount)));
        return new GuiItem(stack, event -> {
            event.setCancelled(true);
            if (event.isLeftClick()) {
                if (amount < 64) amount *= 2;
                updateGui();
            }
            if (event.isRightClick()) {
                if (amount > 1) amount /= 2;
                updateGui();
            }
        });
    }


    private List<ShopItem> getItems() {
        return getAllItems().stream()
                .filter(item -> filter.test(item.getItem().getType()))
                .filter(item -> item.getOwner().equals(player.getUniqueId()) || !item.isDeprecated())
                .filter(item -> onlyOwned.test(player.getUniqueId(), item))
                .sorted(Comparator.comparingLong(shopItem -> -shopItem.getInShopSince()))
                .toList();
    }

    private static List<ShopItem> getAllItems() {
        return EdenShopPlugin.getPlugin().getItems();
    }
}
