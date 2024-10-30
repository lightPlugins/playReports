package io.lightplugins.reports.util.manager;

import io.lightplugins.reports.Light;
import io.lightplugins.reports.util.NumberFormatter;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ClickGuiStack {

    private final ConfigurationSection CATEGORY_ARGS;
    private final ConfigurationSection PLACEHOLDERS;
    private final String PATTERN_ID;
    private final Player player;
    private String item;
    private String headData;
    @Getter
    @Setter
    private String displayName;
    @Getter
    private List<String> lore = new ArrayList<>();
    private final Map<String, String> placeholders = new HashMap<>();
    private boolean playerHead = false;
    private int modelData;
    private List<String> actions = new ArrayList<>();



    public ClickGuiStack(ConfigurationSection section, Player player) {
        this.CATEGORY_ARGS = section.getConfigurationSection("args");
        this.PLACEHOLDERS = section.getConfigurationSection("args.placeholders");
        this.PATTERN_ID = section.getString("args.pattern-id");
        this.player = player;

        initActions();
        applyPlaceholders();
        guiItemContent();
        translatePlaceholders();
        translateLore();

    }

    private void applyPlaceholders() {

        if(PLACEHOLDERS == null) {
            return;
        }

        for(String placeholder : PLACEHOLDERS.getKeys(true)) {
            placeholders.put(
                    placeholder,
                    PlaceholderAPI.setPlaceholders(
                            player,
                            Objects.requireNonNull(PLACEHOLDERS.getString(placeholder))
                    ));
        }
    }

    private void translatePlaceholders() {
        for(String key : placeholders.keySet()) {
            this.displayName = displayName.replace("#" + key + "#", placeholders.get(key));
            headData = headData.replace("#" + key + "#", placeholders.get(key));
        }
    }

    private void translateLore() {
        List<String> translatedLore = new ArrayList<>();
        for(String line : lore) {
            for(String key : placeholders.keySet()) {
                line = line.replace("#" + key + "#", placeholders.get(key));
            }
            translatedLore.add(Light.instance.colorTranslation.loreLineTranslation(line, null));
        }
        this.lore = translatedLore;

    }

    private void guiItemContent() {

        this.item = CATEGORY_ARGS.getString("item");
        this.displayName = CATEGORY_ARGS.getString("display-name");
        this.lore = CATEGORY_ARGS.getStringList("lore");
        this.headData = CATEGORY_ARGS.getString("head-data");

    }

    private void initActions() {

        if(CATEGORY_ARGS.getStringList("actions").isEmpty()) {
            return;
        }

        this.actions = CATEGORY_ARGS.getStringList("actions");
    }

    public ItemStack getGuiItem() {

        ItemStack itemStack = new ItemStack(Material.STONE, 1);

        String[] splitItem = item.split(" ");
        Material material = Material.getMaterial(splitItem[0].toUpperCase());

        if(NumberFormatter.isNumber(splitItem[1])) {
            itemStack.setAmount(Integer.parseInt(splitItem[1]));
        }

        if(material != null) {
            itemStack.setType(material);

            if(material.equals(Material.PLAYER_HEAD)) {
                playerHead = true;
            }
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        if(itemMeta == null) {
            return null;
        }

        itemMeta.setDisplayName(Light.instance.colorTranslation.loreLineTranslation(displayName, player));

        for(String split : splitItem) {

            if(split.equalsIgnoreCase("model-data")) {
                String[] splitModelData = split.split(":");
                if(NumberFormatter.isNumber(splitModelData[1])) {
                    itemMeta.setCustomModelData(Integer.parseInt(splitModelData[1]));
                    this.modelData = Integer.parseInt(splitModelData[1]);
                }
            }

            if(split.equalsIgnoreCase("hide_enchants")) {
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            if(split.equalsIgnoreCase("hide_attributes")) {
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemMeta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
                itemMeta.addItemFlags(ItemFlag.HIDE_DYE);
                itemMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
                itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }
        }

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;

    }

    public boolean isPlayerHead() { return playerHead; }
    public String getHeadData() { return headData; }
    public int getModelData() { return modelData; }
    public String getPatternID() { return PATTERN_ID; }
    public List<String> getActions() { return actions; }
}
