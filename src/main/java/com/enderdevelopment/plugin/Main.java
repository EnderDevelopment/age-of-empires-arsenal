package com.enderdevelopment.plugin;

import java.util.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Main extends JavaPlugin implements Listener, TabExecutor {
    private final Map<String, Relic> relics = new LinkedHashMap<>();
    private NamespacedKey idKey;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private record Relic(String name, Material material, int model, int damage, int armor, String family, Material ingredient) {}

    @Override public void onEnable() {
        idKey = new NamespacedKey(this, "relic_id");
        registerCatalog();
        for (Relic relic : relics.values()) registerRecipe(relic);
        Objects.requireNonNull(getCommand("arsenal")).setExecutor(this);
        Objects.requireNonNull(getCommand("arsenal")).setTabCompleter(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Loaded " + relics.size() + " historical relics.");
    }

    @Override public void onDisable() { cooldowns.clear(); }

    private void registerCatalog() {
        add("spartan_shield", "Spartan Shield", Material.SHIELD, 1, 0, "Greece", Material.IRON_INGOT);
        add("xiphos", "Xiphos", Material.IRON_SWORD, 2, 0, "Greece", Material.IRON_INGOT);
        add("dory", "Dory", Material.TRIDENT, 3, 0, "Greece", Material.BAMBOO);
        add("kopis", "Kopis", Material.IRON_SWORD, 4, 0, "Greece", Material.COPPER_INGOT);
        add("scutum", "Roman Scutum", Material.SHIELD, 5, 0, "Rome", Material.IRON_INGOT);
        add("gladius", "Gladius", Material.IRON_SWORD, 6, 0, "Rome", Material.IRON_INGOT);
        add("roman_helmet", "Roman Helmet", Material.IRON_HELMET, 7, 2, "Rome", Material.IRON_INGOT);
        add("roman_chestplate", "Roman Lorica", Material.IRON_CHESTPLATE, 8, 6, "Rome", Material.IRON_INGOT);
        add("roman_leggings", "Roman Greaves", Material.IRON_LEGGINGS, 9, 5, "Rome", Material.IRON_INGOT);
        add("roman_boots", "Roman Caligae", Material.IRON_BOOTS, 10, 2, "Rome", Material.IRON_INGOT);
        add("round_shield", "Viking Round Shield", Material.SHIELD, 11, 0, "Vikings", Material.SPRUCE_PLANKS);
        add("viking_axe", "Viking Axe", Material.IRON_AXE, 12, 0, "Vikings", Material.IRON_INGOT);
        add("seax", "Seax", Material.IRON_SWORD, 13, 0, "Vikings", Material.IRON_INGOT);
        add("longsword", "Longsword", Material.DIAMOND_SWORD, 14, 0, "Medieval", Material.IRON_INGOT);
        add("halberd", "Halberd", Material.DIAMOND_AXE, 15, 0, "Medieval", Material.IRON_INGOT);
        add("mace", "War Mace", Material.MACE, 16, 0, "Medieval", Material.IRON_INGOT);
        add("battle_axe", "Battle Axe", Material.IRON_AXE, 17, 0, "Medieval", Material.IRON_INGOT);
        add("war_crossbow", "War Crossbow", Material.CROSSBOW, 18, 0, "Medieval", Material.CROSSBOW);
        add("knight_helmet", "Knight Helm", Material.DIAMOND_HELMET, 19, 3, "Medieval", Material.IRON_INGOT);
        add("knight_chestplate", "Knight Cuirass", Material.DIAMOND_CHESTPLATE, 20, 8, "Medieval", Material.IRON_INGOT);
        add("knight_leggings", "Knight Leggings", Material.DIAMOND_LEGGINGS, 21, 6, "Medieval", Material.IRON_INGOT);
        add("knight_boots", "Knight Sabatons", Material.DIAMOND_BOOTS, 22, 3, "Medieval", Material.IRON_INGOT);
        add("katana", "Katana", Material.DIAMOND_SWORD, 23, 0, "Japan", Material.IRON_INGOT);
        add("samurai_helmet", "Kabuto", Material.IRON_HELMET, 24, 3, "Japan", Material.IRON_INGOT);
        add("samurai_chestplate", "Samurai Do", Material.IRON_CHESTPLATE, 25, 7, "Japan", Material.IRON_INGOT);
        add("samurai_leggings", "Haidate", Material.IRON_LEGGINGS, 26, 5, "Japan", Material.IRON_INGOT);
        add("samurai_boots", "Suneate", Material.IRON_BOOTS, 27, 2, "Japan", Material.IRON_INGOT);
        add("yari", "Yari", Material.TRIDENT, 28, 0, "Japan", Material.BAMBOO);
        add("composite_bow", "Composite Bow", Material.BOW, 29, 0, "Mongolia", Material.BOW);
        add("mongol_helmet", "Mongolian Helmet", Material.CHAINMAIL_HELMET, 30, 2, "Mongolia", Material.LEATHER);
        add("mongol_chestplate", "Mongolian Lamellar", Material.CHAINMAIL_CHESTPLATE, 31, 6, "Mongolia", Material.LEATHER);
        add("mongol_leggings", "Mongolian Leggings", Material.CHAINMAIL_LEGGINGS, 32, 5, "Mongolia", Material.LEATHER);
        add("mongol_boots", "Mongolian Boots", Material.CHAINMAIL_BOOTS, 33, 2, "Mongolia", Material.LEATHER);
    }

    private void add(String id, String name, Material material, int model, int damage, String family, Material ingredient) {
        relics.put(id, new Relic(name, material, model, damage, 0, family, ingredient));
    }

    private ItemStack create(Relic relic) {
        ItemStack item = new ItemStack(relic.material());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.displayName(Component.text(relic.name(), NamedTextColor.GOLD));
        meta.lore(List.of(Component.text(relic.family() + " • War Relic", NamedTextColor.GRAY), Component.text("Ability: " + abilityName(relic), NamedTextColor.AQUA)));
        meta.setCustomModelData(relic.model());
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, relicKey(relic));
        item.setItemMeta(meta);
        return item;
    }

    private String relicKey(Relic relic) { return relics.entrySet().stream().filter(e -> e.getValue() == relic).map(Map.Entry::getKey).findFirst().orElse(""); }
    private String abilityName(Relic r) {
        if (r.material() == Material.SHIELD) return "Shield Bash";
        if (r.material() == Material.BOW || r.material() == Material.CROSSBOW) return "Volley Step";
        if (r.material().name().contains("HELMET") || r.material().name().contains("CHESTPLATE") || r.material().name().contains("LEGGINGS") || r.material().name().contains("BOOTS")) return "Rally";
        return "Warrior's Lunge";
    }

    private void registerRecipe(Relic relic) {
        String key = relicKey(relic);
        NamespacedKey recipeKey = new NamespacedKey(this, key);
        if (Bukkit.getRecipe(recipeKey) != null) Bukkit.removeRecipe(recipeKey);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, create(relic));
        recipe.shape(" I ", "ISI", " S ");
        recipe.setIngredient('I', relic.ingredient());
        recipe.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(recipe);
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(Component.text("Historical Arsenal: " + String.join(", ", relics.values().stream().map(Relic::name).toList()), NamedTextColor.GOLD));
            return true;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            Player target = Bukkit.getPlayerExact(args[1]);
            Relic relic = relics.get(args[2].toLowerCase(Locale.ROOT));
            if (target == null || relic == null) { sender.sendMessage(Component.text("Player or item not found. Use /arsenal list.", NamedTextColor.RED)); return true; }
            Map<Integer, ItemStack> overflow = target.getInventory().addItem(create(relic));
            overflow.values().forEach(stack -> target.getWorld().dropItemNaturally(target.getLocation(), stack));
            sender.sendMessage(Component.text("Granted " + relic.name() + " to " + target.getName() + ".", NamedTextColor.GREEN));
            return true;
        }
        sender.sendMessage(Component.text("Usage: /arsenal list or /arsenal give <player> <item-key>", NamedTextColor.YELLOW));
        return true;
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("list", "give");
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) return relics.keySet().stream().toList();
        return List.of();
    }

    @EventHandler public void onInteract(PlayerInteractEvent event) {
        ItemStack held = event.getItem();
        if (held == null || !held.hasItemMeta()) return;
        String id = held.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        if (id == null) return;
        Player player = event.getPlayer();
        if (event.getAction().isRightClick() && player.isSneaking()) {
            long now = System.currentTimeMillis();
            if (cooldowns.getOrDefault(player.getUniqueId(), 0L) > now) return;
            cooldowns.put(player.getUniqueId(), now + 8000);
            Relic relic = relics.get(id);
            if (relic.material() == Material.SHIELD) {
                Vector direction = player.getLocation().getDirection().normalize();
                for (Entity entity : player.getNearbyEntities(3, 2, 3)) if (entity instanceof LivingEntity living && living != player && living.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().dot(direction) > 0.35) {
                    living.damage(4.0, player);
                    living.setVelocity(direction.clone().multiply(1.1).setY(0.35));
                }
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 0.7f);
            } else if (relic.material() == Material.BOW || relic.material() == Material.CROSSBOW) {
                player.setVelocity(player.getLocation().getDirection().multiply(0.9).setY(0.25));
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1.4f);
            } else if (relic.armor() > 0) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.RESISTANCE, 80, 0));
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 80, 0));
            } else {
                player.setVelocity(player.getLocation().getDirection().multiply(0.75).setY(0.18));
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 0.9f);
            }
            player.sendActionBar(Component.text("War ability used • 8s cooldown", NamedTextColor.YELLOW));
        }
    }

    @EventHandler public void onRelicDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!held.hasItemMeta()) return;
        String id = held.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        Relic relic = id == null ? null : relics.get(id);
        if (relic == null || relic.damage() <= 0) return;
        double bonus = relic.damage() * 0.35;
        event.setDamage(event.getDamage() + bonus);
        if (relic.material() == Material.TRIDENT) event.getEntity().setVelocity(player.getLocation().getDirection().multiply(0.5).setY(0.2));
        if (relic.material() == Material.MACE || relic.material() == Material.IRON_AXE || relic.material() == Material.DIAMOND_AXE) event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(0.75));
    }
}
