package at.haha007.edenshop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.*;

public class ShopItem {
    private final double price;
    private final UUID owner;
    private final String ownerName;
    private long inShopSince;
    private final ItemStack item;

    public ShopItem(ItemStack itemStack, Player owner, double price) {
        this(price, owner.getUniqueId(), owner.getName(), System.currentTimeMillis(), itemStack);
    }

    public ShopItem(double price, UUID owner, String ownerName, long inShopSince, ItemStack item) {
        this.price = price;
        this.owner = owner;
        this.ownerName = ownerName;
        this.inShopSince = inShopSince;
        this.item = item;
        if (System.currentTimeMillis() - inShopSince > (365 * 24 * 60 * 60 * 1000L)) {
            item.setAmount(0);
        }
    }

    public double getPrice() {
        return price;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public long getInShopSince() {
        return inShopSince;
    }

    public void setInShopSince(long inShopSince) {
        this.inShopSince = inShopSince;
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemStack displayItem(UUID user) {
        ItemStack item = this.item.clone();
        if (item.getAmount() == 0) return null;
        if (item.getType() == Material.AIR) return null;
        List<Component> lore = item.lore();
        if (lore == null) lore = new ArrayList<>();
        else lore = new ArrayList<>(lore);
        lore.add(0, MiniMessage.miniMessage().deserialize("<!italic><green>Preis pro Stück: <gold>%.2f".formatted(price)));
        lore.add(1, MiniMessage.miniMessage().deserialize("<!italic><green>Verkäufer: <gold>%s".formatted(ownerName)));
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        if (isDeprecated()) {
            if (!user.equals(owner)) {
                return null;
            }
            String time = formatter.format(new Date(inShopSince + (7 * 24 * 60 * 60 * 1000)));
            lore.add(2, MiniMessage.miniMessage().deserialize("<!italic><red>Ausgelaufen am: <gold>%s".formatted(time)));
        } else {
            String time = formatter.format(new Date(inShopSince));
            lore.add(2, MiniMessage.miniMessage().deserialize("<!italic><green>Eingestellt am: <gold>%s".formatted(time)));
        }
        item.lore(lore);
        return item;
    }

    public boolean isDeprecated() {
        return System.currentTimeMillis() - inShopSince > 7 * 24 * 60 * 60 * 1000;
    }

    public void sell(Player buyer, int amount) {
        amount = Math.min(amount, item.getAmount());
        if (buyer.getUniqueId().equals(owner)) {
            buyer.sendMessage(MiniMessage.miniMessage().deserialize("<green>Item zurückgenommen!"));
            amount = item.getAmount();
            ItemStack stack = this.item.clone();
            stack.setAmount(amount);
            giveItem(buyer, stack);
            item.setAmount(item.getAmount() - amount);
            return;
        }
        double price = this.price * amount;
        Economy economy = EdenShopPlugin.economy();
        if (!economy.withdrawPlayer(buyer, price).transactionSuccess()) {
            buyer.sendMessage(MiniMessage.miniMessage().deserialize("<red>Du hast nicht genug Geld!"));
            return;
        }
        buyer.sendMessage(MiniMessage.miniMessage().deserialize("<green>Item für <gold>%.02f<green> gekauft!".formatted(price)));
        Player seller = Bukkit.getPlayer(owner);
        if (seller != null) {
            seller.sendMessage(MiniMessage.miniMessage().deserialize("<green>%d <gold>%s<green> an <gold>%s<green> für <gold>%.02f<green> verkauft!".formatted(
                    amount,
                    MiniMessage.miniMessage().serialize(Component.translatable(item.getType())),
                    buyer.getName(),
                    price
            )));
        }
        economy.depositPlayer(EdenShopPlugin.getPlugin().getServer().getOfflinePlayer(owner), price * 0.9);
        ItemStack stack = this.item.clone();
        stack.setAmount(amount);
        giveItem(buyer, stack);
        item.setAmount(item.getAmount() - amount);
    }

    private void giveItem(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);
        remaining.values().forEach(i -> player.getWorld().dropItem(player.getLocation(), i).setOwner(player.getUniqueId()));
    }

    @Override
    public String toString() {
        return "ShopItem{" +
                "price=" + price +
                ", owner=" + owner +
                ", ownerName='" + ownerName + '\'' +
                ", inShopSince=" + inShopSince +
                ", item=" + item +
                '}';
    }

    public boolean isExpired() {
        if (item.getAmount() == 0) return true;
        if (item.getType() == Material.AIR) return true;
        return System.currentTimeMillis() - inShopSince > (365 * 24 * 60 * 60 * 1000L);
    }
}
