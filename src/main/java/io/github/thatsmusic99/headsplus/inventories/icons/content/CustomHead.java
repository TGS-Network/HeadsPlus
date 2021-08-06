package io.github.thatsmusic99.headsplus.inventories.icons.content;

import io.github.thatsmusic99.headsplus.HeadsPlus;
import io.github.thatsmusic99.headsplus.api.HPPlayer;
import io.github.thatsmusic99.headsplus.api.events.HeadPurchaseEvent;
import io.github.thatsmusic99.headsplus.config.ConfigHeadsSelector;
import io.github.thatsmusic99.headsplus.config.ConfigInventories;
import io.github.thatsmusic99.headsplus.config.MessagesManager;
import io.github.thatsmusic99.headsplus.config.MainConfig;
import io.github.thatsmusic99.headsplus.inventories.InventoryManager;
import io.github.thatsmusic99.headsplus.inventories.icons.Content;
import io.github.thatsmusic99.headsplus.managers.PersistenceManager;
import io.github.thatsmusic99.headsplus.util.HPUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CustomHead extends Content {

    private double price = 0;
    private String id;

    public CustomHead(String id) {
        super(ConfigHeadsSelector.get().getBuyableHead(id).forceBuildHead());
        this.price = ConfigHeadsSelector.get().getBuyableHead(id).getPrice(); // TODO - set price
        this.id = id;
    }

    public CustomHead() {}

    @Override
    public boolean onClick(Player player, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem().clone();
        if (event.isLeftClick()) { // Check if we're giving the head
            if (player.getInventory().firstEmpty() == -1) { // Check if there is a free space
                MessagesManager.get().sendMessage("commands.head.full-inv", player);
                return false;
            }
            double price = player.hasPermission("headsplus.bypass.cost") ? 0 : this.price; // Set price to 0 or not
            Economy ef = null;
            if (price > 0.0
                    && hp.isVaultEnabled()
                    && (ef = hp.getEconomy()) != null
                    && price > ef.getBalance(player)) { // If Vault is enabled, price is above 0, and the player can't afford the head
                MessagesManager.get().sendMessage("commands.heads.not-enough-money", player); // K.O
                return false;
            }
            HeadPurchaseEvent purchaseEvent = new HeadPurchaseEvent(player, item);
            Bukkit.getServer().getPluginManager().callEvent(purchaseEvent);
            if (!purchaseEvent.isCancelled()) {
                if(price > 0.0 && ef != null) {
                    EconomyResponse er = ef.withdrawPlayer(player, price);
                    if(er.transactionSuccess()) {
                        MessagesManager.get().sendMessage("commands.heads.buy-success", player,
                                "{price}", MainConfig.get().fixBalanceStr(price),
                                "{balance}", MainConfig.get().fixBalanceStr(ef.getBalance(player)));
                    } else {
                        MessagesManager.get().sendMessage(
                                MessagesManager.get().getString("commands.errors.cmd-fail", player) + ": " + er.errorMessage, player, false);
                        return false;
                    }
                }
                if (player.getInventory().firstEmpty() == 8) {
                    InventoryManager im = InventoryManager.getManager(player);
                    im.setGlitchSlot(true);
                }
                ItemMeta meta = item.getItemMeta();
                meta.setLore(new ArrayList<>());
                item.setItemMeta(meta);
                PersistenceManager.get().removeIcon(item);
                player.getInventory().addItem(item);
            }
        } else {
            HPPlayer hpp = HPPlayer.getHPPlayer(player);
            if (hpp.hasHeadFavourited(id)) {
                hpp.removeFavourite(id).thenAcceptAsync(what -> {
                    initNameAndLore(id, player);
                    event.getInventory().setItem(event.getSlot(), this.item);
                }, HeadsPlus.sync);
            } else {
                hpp.addFavourite(id).thenAcceptAsync(why -> {
                    initNameAndLore(id, player);
                    event.getInventory().setItem(event.getSlot(), this.item);
                }, HeadsPlus.sync);
            }
        }
        return false;
    }

    @Override
    public String getId() {
        return "head";
    }

    @Override
    public void initNameAndLore(String id, Player player) {
        // We only really need to add the lore here
        List<String> lore = new ArrayList<>();
        for (String str : ConfigInventories.get().getStringList("icons.head.lore")) {
            HPUtils.parseLorePlaceholders(lore, MessagesManager.get().formatMsg(str, player),
                    new HPUtils.PlaceholderInfo("{favourite}",
                            MessagesManager.get().getString("inventory.icon.head.favourite", player),
                            HPPlayer.getHPPlayer(player).hasHeadFavourited(id)),
                    new HPUtils.PlaceholderInfo("{msg_inventory.icon.head.favourite}",
                            MessagesManager.get().getString("inventory.icon.head.favourite", player),
                            HPPlayer.getHPPlayer(player).hasHeadFavourited(id)),
                    new HPUtils.PlaceholderInfo("{price}", determinePrice(player.getWorld()), true));
        }
        ItemMeta im = item.getItemMeta();
        im.setLore(lore);
        item.setItemMeta(im);
    }

    private double determinePrice(World world) {
        if (price != -1) return price;
        return MainConfig.get().getHeadsSelector().PER_WORLD_PRICES.getDouble(world.getName(),
                MainConfig.get().getHeadsSelector().DEFAULT_PRICE);
    }

}
