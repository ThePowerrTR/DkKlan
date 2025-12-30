package com.dkprojects.dkklan.managers;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.utils.Serializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionType;
import org.bukkit.potion.PotionData;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KitManager {
    private final DkKlan plugin;
    private final File kitsFolder;

    public KitManager(DkKlan plugin) {
        this.plugin = plugin;
        this.kitsFolder = new File(plugin.getDataFolder(), "kits");
        if (!kitsFolder.exists()) {
            kitsFolder.mkdirs();
        }
        createDefaultKits();
    }

    public void saveKitFromGUI(String name, Inventory inv) {
        File file = new File(kitsFolder, name + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        // Inventory (0-35)
        ItemStack[] contents = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            contents[i] = inv.getItem(i);
        }
        config.set("inventory-base64", Serializer.itemStackArrayToBase64(contents));
        config.set("inventory", null);
        
        // Armor (36-39) [Boots, Leggings, Chest, Helmet]
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            ItemStack item = inv.getItem(36 + i);
            boolean isPlaceholder = false;
            
            if (item != null && item.getType() == Material.GRAY_STAINED_GLASS_PANE) {
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    if (item.getItemMeta().getDisplayName().contains("Slotu")) {
                        isPlaceholder = true;
                    }
                }
            }
            
            if (isPlaceholder) {
                armor[i] = null;
            } else {
                armor[i] = item;
            }
        }
        config.set("armor-base64", Serializer.itemStackArrayToBase64(armor));
        config.set("armor", null);
        
        // Offhand (40)
        ItemStack offhand = inv.getItem(40);
        boolean isOffhandPlaceholder = false;
        if (offhand != null && offhand.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            if (offhand.hasItemMeta() && offhand.getItemMeta().hasDisplayName()) {
                if (offhand.getItemMeta().getDisplayName().contains("Slotu")) {
                    isOffhandPlaceholder = true;
                }
            }
        }
        
        if (isOffhandPlaceholder) {
            config.set("offhand-base64", null);
        } else {
            if (offhand != null) {
                config.set("offhand-base64", Serializer.itemStackArrayToBase64(new ItemStack[]{offhand}));
            } else {
                config.set("offhand-base64", null);
            }
        }
        config.set("offhand", null);
        
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveKit(Player p, String name) {
        File file = new File(kitsFolder, name + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        config.set("inventory-base64", Serializer.itemStackArrayToBase64(p.getInventory().getContents()));
        config.set("inventory", null);

        config.set("armor-base64", Serializer.itemStackArrayToBase64(p.getInventory().getArmorContents()));
        config.set("armor", null);

        ItemStack offhand = p.getInventory().getItemInOffHand();
        if (offhand != null) {
            config.set("offhand-base64", Serializer.itemStackArrayToBase64(new ItemStack[]{offhand}));
        } else {
            config.set("offhand-base64", null);
        }
        config.set("offhand", null);
        
        try {
            config.save(file);
            p.sendMessage("§aKit başarıyla kaydedildi: " + name);
        } catch (IOException e) {
            e.printStackTrace();
            p.sendMessage("§cKit kaydedilirken hata oluştu.");
        }
    }
    
    public void giveKit(Player p, String name) {
        File file = new File(kitsFolder, name + ".yml");
        if (!file.exists()) return;
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        // Clear first
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        for (org.bukkit.potion.PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }
        
        // Load Inventory
        ItemStack[] contents = null;
        if (config.contains("inventory-base64")) {
            try {
                contents = Serializer.itemStackArrayFromBase64(config.getString("inventory-base64"));
            } catch (IOException e) { e.printStackTrace(); }
        }
        
        if (contents == null) {
            List<ItemStack> contentList = (List<ItemStack>) config.getList("inventory");
            if (contentList != null) {
                contents = new ItemStack[36];
                for (int i = 0; i < contentList.size() && i < 36; i++) {
                    contents[i] = contentList.get(i);
                }
            }
        }
        
        if (contents != null) p.getInventory().setContents(contents);
        
        // Load Armor
        ItemStack[] armor = null;
        if (config.contains("armor-base64")) {
            try {
                armor = Serializer.itemStackArrayFromBase64(config.getString("armor-base64"));
            } catch (IOException e) { e.printStackTrace(); }
        }
        
        if (armor == null) {
            List<ItemStack> armorList = (List<ItemStack>) config.getList("armor");
            if (armorList != null) {
                armor = new ItemStack[4];
                for (int i = 0; i < armorList.size() && i < 4; i++) {
                    armor[i] = armorList.get(i);
                }
            }
        }
        
        if (armor != null) p.getInventory().setArmorContents(armor);
        
        // Load Offhand
        ItemStack offhand = null;
        if (config.contains("offhand-base64")) {
            try {
                ItemStack[] offhandArr = Serializer.itemStackArrayFromBase64(config.getString("offhand-base64"));
                if (offhandArr != null && offhandArr.length > 0) offhand = offhandArr[0];
            } catch (IOException e) { e.printStackTrace(); }
        } else {
            offhand = config.getItemStack("offhand");
        }
        
        if (offhand != null) {
            p.getInventory().setItemInOffHand(offhand);
        }
        
        p.updateInventory();
    }
    
    public boolean kitExists(String name) {
        return new File(kitsFolder, name + ".yml").exists();
    }

    public void deleteKit(String name) {
        File file = new File(kitsFolder, name + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }
    
    private void createDefaultKits() {
        // if (!getKitNames().isEmpty()) return; // Removed to allow individual kit checks/updates

        plugin.getLogger().info("Varsayılan kitler kontrol ediliyor...");

        // 1. NethPot (NetheritePOT)
        if (!kitExists("NethPot")) {
            List<ItemStack> nethPotItems = new ArrayList<>();
        nethPotItems.add(createItem(Material.NETHERITE_SWORD, "§cNetherite Sword", Enchantment.DAMAGE_ALL, 5, Enchantment.DURABILITY, 3));
        nethPotItems.add(new ItemStack(Material.TOTEM_OF_UNDYING, 1));
        nethPotItems.add(new ItemStack(Material.TOTEM_OF_UNDYING, 1));
        
        // Potions & Food
        for (int i=0; i<6; i++) nethPotItems.add(createPotion(PotionType.SPEED, false, true)); // Speed II
        for (int i=0; i<6; i++) nethPotItems.add(createPotion(PotionType.STRENGTH, false, true)); // Strength II
        nethPotItems.add(new ItemStack(Material.GOLDEN_APPLE, 64));
        
        // Fill rest with Instant Health II
        while (nethPotItems.size() < 36) {
            nethPotItems.add(createPotion(PotionType.INSTANT_HEAL, true, true));
        }

        createKit("NethPot", 
            nethPotItems.toArray(new ItemStack[0]),
            new ItemStack[]{
                createItem(Material.NETHERITE_BOOTS, "§cNetherite Boots", Enchantment.PROTECTION_ENVIRONMENTAL, 4, Enchantment.DURABILITY, 3, Enchantment.MENDING, 1),
                createItem(Material.NETHERITE_LEGGINGS, "§cNetherite Leggings", Enchantment.PROTECTION_ENVIRONMENTAL, 4, Enchantment.DURABILITY, 3, Enchantment.MENDING, 1),
                createItem(Material.NETHERITE_CHESTPLATE, "§cNetherite Chestplate", Enchantment.PROTECTION_ENVIRONMENTAL, 4, Enchantment.DURABILITY, 3, Enchantment.MENDING, 1),
                createItem(Material.NETHERITE_HELMET, "§cNetherite Helmet", Enchantment.PROTECTION_ENVIRONMENTAL, 4, Enchantment.DURABILITY, 3, Enchantment.MENDING, 1)
            },
            new ItemStack(Material.TOTEM_OF_UNDYING)
        );
        }

        // 2. Sword (Düz PvP)
        createKit("Sword", 
            new ItemStack[]{
                createItem(Material.DIAMOND_SWORD, "§bDiamond Sword", Enchantment.DAMAGE_ALL, 4, Enchantment.DURABILITY, 3),
                new ItemStack(Material.COOKED_BEEF, 64)
            },
            new ItemStack[]{
                createItem(Material.DIAMOND_BOOTS, "§bDiamond Boots", Enchantment.PROTECTION_ENVIRONMENTAL, 3),
                createItem(Material.DIAMOND_LEGGINGS, "§bDiamond Leggings", Enchantment.PROTECTION_ENVIRONMENTAL, 3),
                createItem(Material.DIAMOND_CHESTPLATE, "§bDiamond Chestplate", Enchantment.PROTECTION_ENVIRONMENTAL, 3),
                createItem(Material.DIAMOND_HELMET, "§bDiamond Helmet", Enchantment.PROTECTION_ENVIRONMENTAL, 3)
            },
            null
        );

        // 3. SMP (Klan Survival Savaşı)
        createKit("SMP", 
            new ItemStack[]{
                createItem(Material.DIAMOND_SWORD, "§aSMP Sword", Enchantment.DAMAGE_ALL, 4),
                createItem(Material.BOW, "§aSMP Bow", Enchantment.ARROW_DAMAGE, 4, Enchantment.ARROW_INFINITE, 1),
                createItem(Material.DIAMOND_PICKAXE, "§aSMP Pickaxe", Enchantment.DIG_SPEED, 4),
                createItem(Material.DIAMOND_AXE, "§aSMP Axe", Enchantment.DIG_SPEED, 4),
                new ItemStack(Material.COOKED_BEEF, 64),
                new ItemStack(Material.COBWEB, 16),
                new ItemStack(Material.ARROW, 1)
            },
            new ItemStack[]{
                createItem(Material.DIAMOND_BOOTS, "§aSMP Boots", Enchantment.PROTECTION_ENVIRONMENTAL, 3),
                createItem(Material.DIAMOND_LEGGINGS, "§aSMP Leggings", Enchantment.PROTECTION_ENVIRONMENTAL, 3),
                createItem(Material.DIAMOND_CHESTPLATE, "§aSMP Chestplate", Enchantment.PROTECTION_ENVIRONMENTAL, 3),
                createItem(Material.DIAMOND_HELMET, "§aSMP Helmet", Enchantment.PROTECTION_ENVIRONMENTAL, 3)
            },
            new ItemStack(Material.SHIELD)
        );

        // 6. UHC (Ultra Hardcore)
        createKit("UHC",
            new ItemStack[]{
                createItem(Material.IRON_SWORD, "§6UHC Sword", Enchantment.DAMAGE_ALL, 3),
                createItem(Material.BOW, "§6UHC Bow", Enchantment.ARROW_DAMAGE, 2),
                createItem(Material.FISHING_ROD, "§6UHC Rod", Enchantment.DURABILITY, 3),
                new ItemStack(Material.GOLDEN_APPLE, 6),
                new ItemStack(Material.LAVA_BUCKET),
                new ItemStack(Material.WATER_BUCKET),
                new ItemStack(Material.COBBLESTONE, 64),
                new ItemStack(Material.OAK_PLANKS, 64),
                new ItemStack(Material.COOKED_BEEF, 32),
                new ItemStack(Material.ARROW, 32),
                createItem(Material.DIAMOND_PICKAXE, "§6UHC Pickaxe", Enchantment.DIG_SPEED, 2)
            },
            new ItemStack[]{
                createItem(Material.DIAMOND_BOOTS, "§6UHC Boots", Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                createItem(Material.IRON_LEGGINGS, "§6UHC Leggings", Enchantment.PROTECTION_PROJECTILE, 2),
                createItem(Material.DIAMOND_CHESTPLATE, "§6UHC Chestplate", Enchantment.PROTECTION_ENVIRONMENTAL, 1),
                createItem(Material.IRON_HELMET, "§6UHC Helmet", Enchantment.PROTECTION_PROJECTILE, 2)
            },
            new ItemStack(Material.SHIELD)
        );



        // 12. NoDebuff (Pot PvP)
        List<ItemStack> nodebuffItems = new ArrayList<>();
        nodebuffItems.add(createItem(Material.DIAMOND_SWORD, "§bNoDebuff Sword", Enchantment.DAMAGE_ALL, 5, Enchantment.DURABILITY, 3));
        nodebuffItems.add(new ItemStack(Material.ENDER_PEARL, 16));
        nodebuffItems.add(createPotion(PotionType.SPEED, false, true));
        nodebuffItems.add(createPotion(PotionType.FIRE_RESISTANCE, false, false));
        nodebuffItems.add(new ItemStack(Material.COOKED_BEEF, 64));
        while (nodebuffItems.size() < 36) {
            nodebuffItems.add(createPotion(PotionType.INSTANT_HEAL, true, true));
        }
        createKit("NoDebuff",
            nodebuffItems.toArray(new ItemStack[0]),
            new ItemStack[]{
                createItem(Material.DIAMOND_BOOTS, "§bNoDebuff Boots", Enchantment.PROTECTION_ENVIRONMENTAL, 4, Enchantment.DURABILITY, 3),
                createItem(Material.DIAMOND_LEGGINGS, "§bNoDebuff Leggings", Enchantment.PROTECTION_ENVIRONMENTAL, 4, Enchantment.DURABILITY, 3),
                createItem(Material.DIAMOND_CHESTPLATE, "§bNoDebuff Chestplate", Enchantment.PROTECTION_ENVIRONMENTAL, 4, Enchantment.DURABILITY, 3),
                createItem(Material.DIAMOND_HELMET, "§bNoDebuff Helmet", Enchantment.PROTECTION_ENVIRONMENTAL, 4, Enchantment.DURABILITY, 3)
            },
            null
        );

        // 13. Knight (Şövalye)
        createKit("Knight",
            new ItemStack[]{
                createItem(Material.IRON_SWORD, "§fŞövalye Kılıcı", Enchantment.DAMAGE_ALL, 5, Enchantment.DURABILITY, 3),
                new ItemStack(Material.GOLDEN_CARROT, 64),
                new ItemStack(Material.GOLDEN_APPLE, 8)
            },
            new ItemStack[]{
                createItem(Material.IRON_BOOTS, "§fŞövalye Botu", Enchantment.PROTECTION_ENVIRONMENTAL, 4),
                createItem(Material.IRON_LEGGINGS, "§fŞövalye Pantolonu", Enchantment.PROTECTION_ENVIRONMENTAL, 4),
                createItem(Material.IRON_CHESTPLATE, "§fŞövalye Göğüslüğü", Enchantment.PROTECTION_ENVIRONMENTAL, 4),
                createItem(Material.IRON_HELMET, "§fŞövalye Kaskı", Enchantment.PROTECTION_ENVIRONMENTAL, 4)
            },
            new ItemStack(Material.SHIELD)
        );


    }

    private ItemStack createItem(Material mat, String name, Enchantment ench1, int lvl1, Enchantment ench2, int lvl2, Enchantment ench3, int lvl3) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (ench1 != null) meta.addEnchant(ench1, lvl1, true);
            if (ench2 != null) meta.addEnchant(ench2, lvl2, true);
            if (ench3 != null) meta.addEnchant(ench3, lvl3, true);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    // Updated helper to support multiple enchantments
    private ItemStack createItem(Material mat, String name, Enchantment ench1, int lvl1, Enchantment ench2, int lvl2) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (ench1 != null) meta.addEnchant(ench1, lvl1, true);
            if (ench2 != null) meta.addEnchant(ench2, lvl2, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void createKit(String name, ItemStack[] content, ItemStack[] armor, ItemStack offhand) {
        if (kitExists(name)) return;
        File file = new File(kitsFolder, name + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("inventory", content);
        config.set("armor", armor);
        config.set("offhand", offhand);
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
    
    private ItemStack createItem(Material mat, String name, Enchantment ench, int level) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (ench != null) meta.addEnchant(ench, level, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name, Object... enchants) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            for (int i = 0; i < enchants.length; i += 2) {
                if (i + 1 < enchants.length && enchants[i] instanceof Enchantment && enchants[i+1] instanceof Integer) {
                    meta.addEnchant((Enchantment) enchants[i], (Integer) enchants[i+1], true);
                }
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createPotion(PotionType type, boolean splash, boolean upgraded) {
        ItemStack potion = new ItemStack(splash ? Material.SPLASH_POTION : Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta != null) {
            meta.setBasePotionData(new PotionData(type, false, upgraded));
            potion.setItemMeta(meta);
        }
        return potion;
    }

    public List<String> getKitNames() {
        List<String> names = new ArrayList<>();
        File[] files = kitsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File f : files) {
                names.add(f.getName().replace(".yml", ""));
            }
        }
        return names;
    }

    public List<String> getKitPreview(String name) {
        File file = new File(kitsFolder, name + ".yml");
        if (!file.exists()) return new ArrayList<>();
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> lore = new ArrayList<>();
        
        // Armor
        List<ItemStack> armorList = (List<ItemStack>) config.getList("armor");
        if (armorList != null) {
            boolean hasArmor = false;
            for (ItemStack item : armorList) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    if (!hasArmor) {
                        lore.add("§6Zırh Seti:");
                        hasArmor = true;
                    }
                    String typeName = item.getType().name().toLowerCase().replace("_", " ");
                    lore.add("§7▪ " + capitalize(typeName));
                    addEnchantmentsToLore(item, lore);
                }
            }
        }
        
        // Main Items (Sword, Bow, etc.)
        List<ItemStack> contentList = (List<ItemStack>) config.getList("inventory");
        if (contentList != null) {
            lore.add("§6Önemli Eşyalar:");
            int count = 0;
            for (ItemStack item : contentList) {
                if (item != null && isImportant(item.getType())) {
                    String typeName = item.getType().name().toLowerCase().replace("_", " ");
                    lore.add("§7▪ " + capitalize(typeName) + " x" + item.getAmount());
                    addEnchantmentsToLore(item, lore);
                    count++;
                    if (count >= 6) {
                        lore.add("§7ve daha fazlası...");
                        break;
                    }
                }
            }
        }
        
        return lore;
    }
    
    private void addEnchantmentsToLore(ItemStack item, List<String> lore) {
        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            for (java.util.Map.Entry<Enchantment, Integer> entry : item.getItemMeta().getEnchants().entrySet()) {
                lore.add("§8  ▸ " + getEnchantmentName(entry.getKey()) + " " + entry.getValue());
            }
        }
    }

    private String getEnchantmentName(Enchantment ench) {
        if (ench.equals(Enchantment.PROTECTION_ENVIRONMENTAL)) return "Koruma";
        if (ench.equals(Enchantment.DAMAGE_ALL)) return "Keskinlik";
        if (ench.equals(Enchantment.DURABILITY)) return "Kırılmazlık";
        if (ench.equals(Enchantment.MENDING)) return "Tamir";
        if (ench.equals(Enchantment.DIG_SPEED)) return "Verimlilik";
        if (ench.equals(Enchantment.ARROW_DAMAGE)) return "Güç";
        if (ench.equals(Enchantment.ARROW_INFINITE)) return "Sonsuzluk";
        if (ench.equals(Enchantment.ARROW_FIRE)) return "Alev";
        if (ench.equals(Enchantment.FIRE_ASPECT)) return "Alevden Çehre";
        if (ench.equals(Enchantment.KNOCKBACK)) return "Savurma";
        if (ench.equals(Enchantment.PROTECTION_EXPLOSIONS)) return "Patlama Koruması";
        if (ench.equals(Enchantment.PROTECTION_PROJECTILE)) return "Ok Koruması";
        if (ench.equals(Enchantment.PROTECTION_FIRE)) return "Ateş Koruması";
        if (ench.equals(Enchantment.THORNS)) return "Dikenler";
        if (ench.equals(Enchantment.SILK_TOUCH)) return "İpeksi Dokunuş";
        if (ench.equals(Enchantment.LOOT_BONUS_BLOCKS)) return "Servet";
        if (ench.equals(Enchantment.LOOT_BONUS_MOBS)) return "Ganimet";
        if (ench.equals(Enchantment.SWEEPING_EDGE)) return "Süpürücü Kenar";
        
        return capitalize(ench.getName().replace("_", " "));
    }
    
    private boolean isImportant(org.bukkit.Material mat) {
        String name = mat.name();
        return name.contains("SWORD") || name.contains("AXE") || name.contains("BOW") || 
               name.contains("APPLE") || name.contains("POTION") || name.contains("PEARL") ||
               name.contains("TOTEM") || name.contains("CRYSTAL") || name.contains("ANCHOR");
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        StringBuilder sb = new StringBuilder();
        for (String word : str.split(" ")) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) sb.append(word.substring(1));
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }
}
