package io.lightplugins.reports.modules.reports.inv;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import io.lightplugins.reports.Light;
import io.lightplugins.reports.modules.reports.LightReports;
import io.lightplugins.reports.modules.reports.manager.ReportManager;
import io.lightplugins.reports.util.manager.ClickGuiStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OverviewInventory {

    private final ChestGui gui = new ChestGui(6, "Init");
    private final Player player;
    private BukkitTask bukkitTask;

    public OverviewInventory(Player player) {
        this.player = player;
    }

    public void openInventory() {

        FileConfiguration conf = LightReports.instance.getOverviewInventory().getConfig();
        String title = Light.instance.colorTranslation.loreLineTranslation(
                Objects.requireNonNull(conf.getString("gui-title")), player);

        gui.setTitle(title);
        gui.setOnGlobalClick(event -> event.setCancelled(true));


        OutlinePane background = new OutlinePane(0, 0, 9, 6, Pane.Priority.LOWEST);
        background.addItem(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
        background.setRepeat(true);
        gui.addPane(background);

        String[] patternList = conf.getStringList("pattern").toArray(new String[0]);
        Pattern pattern = new Pattern(patternList);
        PatternPane pane = new PatternPane(0, 0, 9, 6, pattern);

        for(String path : conf.getConfigurationSection("contents").getKeys(false)) {

            ClickGuiStack category = new ClickGuiStack(Objects.requireNonNull(
                    conf.getConfigurationSection("contents." + path)), player);

            ItemStack itemStack = category.getGuiItem();
            String patternID = category.getPatternID();
            List<String> actions = category.getActions();

            if (category.getGuiItem().getItemMeta() instanceof SkullMeta skullMeta) {
                PlayerProfile playerProfile = Bukkit.createPlayerProfile("LightningDesign");
                skullMeta.setOwnerProfile(playerProfile);
                category.getGuiItem().setItemMeta(skullMeta);
                itemStack.setItemMeta(skullMeta);
            }

            pane.bindItem(patternID.charAt(0), new GuiItem(itemStack, inventoryClickEvent -> {

                if(inventoryClickEvent.getAction().equals(InventoryAction.HOTBAR_SWAP)) {
                    return;
                }

                for(String action : actions) {

                    if(action.contains(";")) {
                        String[] actionSplit = action.split(";");

                        //  Unknown action aka TODO
                    }

                    if(action.equalsIgnoreCase("close")) {
                        player.closeInventory();
                        return;
                    }
                }

                player.sendMessage("§7Path §c" + path + "§7 clicked");

            }));
        }

        PaginatedPane itemContents = new PaginatedPane(1, 1, 8, 4);

        List<GuiItem> test = new ArrayList<>();

        for(ReportManager reportManager : LightReports.instance.getReportCache()) {

            test.add(new GuiItem(new ItemStack(Material.PAPER), event -> {

                player.sendMessage("§7ReportID: §c" + reportManager.getReportID());

            }));
        }

        itemContents.populateWithGuiItems(test);

        gui.addPane(pane);
        gui.addPane(itemContents);
        gui.show(player);
    }

}
