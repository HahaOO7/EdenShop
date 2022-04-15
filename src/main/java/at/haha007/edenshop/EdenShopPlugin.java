package at.haha007.edenshop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class EdenShopPlugin extends JavaPlugin {
    private static Economy economy;
    private static EdenShopPlugin plugin;

    public static Plugin getPlugin() {
        return plugin;
    }

    public void onEnable() {
        plugin = this;
        economy = Objects.requireNonNull(getServer().getServicesManager().getRegistration(Economy.class)).getProvider();
        Shop.getInstance();
        Objects.requireNonNull(getCommand("shop")).setExecutor(new CE_shop());
        Bukkit.getPluginManager().registerEvents(new ShopListener(), this);
    }

    public void onDisable() {
        Shop.getInstance().save();
    }

    public static Economy economy() {
        return economy;
    }
}
