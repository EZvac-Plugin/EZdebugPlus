package com.github.blade.ezdebug;

import com.github.blade.hvac.api.Position;
import com.github.blade.hvac.api.event.DeviceAddedEvent;
import com.github.blade.hvac.api.event.DeviceRemovedEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Locale;

/**
 * The Debug Potion: the item, drinking it, and everything that ends it
 * (milk, death, the timer). The remaining time is kept with the player
 * across relogs. Main thread only.
 */
final class DebugPotion implements Listener {
    private static final Color TEAL = Color.fromRGB(0x2EC4B6);

    private final EzDebugPlugin plugin;
    private final NamespacedKey potionKey;
    private final NamespacedKey remainingKey;

    DebugPotion(EzDebugPlugin plugin) {
        this.plugin = plugin;
        this.potionKey = new NamespacedKey(plugin, "debug");
        this.remainingKey = new NamespacedKey(plugin, "remaining_ticks");
    }

    /** A Debug Potion. It has no water in it, so nothing mistakes it for a water bottle. */
    ItemStack create() {
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setBasePotionType(null);
        meta.setColor(TEAL);
        meta.displayName(Component.text("Debug Potion", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("See EZvac at work · " + clock(plugin.settings().durationTicks()), NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Milk ends it.", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)));
        meta.getPersistentDataContainer().set(potionKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    boolean isPotion(ItemStack item) {
        if (item == null || item.getType() != Material.POTION || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(potionKey, PersistentDataType.BYTE);
    }

    /** "8:00". */
    static String clock(long ticks) {
        long seconds = ticks / 20;
        return String.format(Locale.US, "%d:%02d", seconds / 60, seconds % 60);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrink(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            if (plugin.vision().active(player)) plugin.vision().end(player, true);
            return;
        }
        if (!isPotion(event.getItem())) return;
        long ticks = plugin.settings().durationTicks();
        plugin.vision().begin(player, ticks);
        PotionEffectType carrier = carrier();
        if (carrier != null) player.addPotionEffect(new PotionEffect(carrier, (int) ticks, 0, true, false, true));
        player.sendMessage(Component.text("Debug vision for " + clock(ticks) + ": EZvac's signs and equipment glow,"
                + " and working vents puff colour. Milk ends it.", NamedTextColor.AQUA));
    }

    /** The vanilla effect that shows the countdown, or null for none. */
    private PotionEffectType carrier() {
        String effect = plugin.settings().effect();
        if (effect.equals("none")) return null;
        NamespacedKey key = NamespacedKey.fromString(effect);
        PotionEffectType type = key == null ? null : Registry.EFFECT.get(key);
        if (type == null) plugin.getLogger().warning("effect '" + effect + "' is not a potion effect; none is shown.");
        return type;
    }

    /** Ends the countdown effect too, when debug vision is switched off by command. */
    void clearCarrier(Player player) {
        PotionEffectType carrier = carrier();
        if (carrier != null) player.removePotionEffect(carrier);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        plugin.vision().end(event.getPlayer(), false);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        long left = plugin.vision().remaining(player);
        if (left > 0) player.getPersistentDataContainer().set(remainingKey, PersistentDataType.LONG, left);
        else player.getPersistentDataContainer().remove(remainingKey);
        plugin.vision().end(player, false);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        resume(event.getPlayer());
    }

    /** Picks up a relogging player's remaining time; their countdown effect came back with them. */
    void resume(Player player) {
        Long left = player.getPersistentDataContainer().get(remainingKey, PersistentDataType.LONG);
        player.getPersistentDataContainer().remove(remainingKey);
        if (left != null && left > 0) plugin.vision().begin(player, left);
    }

    @EventHandler
    public void onDeviceRemoved(DeviceRemovedEvent event) {
        Position at = event.getDevice().position();
        plugin.vision().forget(at);
    }

    @EventHandler
    public void onDeviceAdded(DeviceAddedEvent event) {
        // Shown on the next refresh, but debug players would rather see it at once.
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.vision().refresh());
    }
}
