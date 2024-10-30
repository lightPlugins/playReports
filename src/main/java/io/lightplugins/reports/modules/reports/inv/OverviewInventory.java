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
import io.lightplugins.reports.modules.reports.enums.ReportStatus;
import io.lightplugins.reports.modules.reports.manager.ReportManager;
import io.lightplugins.reports.util.manager.ClickGuiStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitTask;

import java.text.SimpleDateFormat;
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

        // Background decoration
        ItemStack decoStack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta decoMeta = decoStack.getItemMeta();
        if(decoMeta == null) {
            return;
        }
        decoMeta.setDisplayName(" ");
        decoStack.setItemMeta(decoMeta);


        OutlinePane background = new OutlinePane(0, 0, 9, 6, Pane.Priority.LOWEST);
        background.addItem(new GuiItem(decoStack));
        background.setRepeat(true);
        gui.addPane(background);

        String[] patternList = conf.getStringList("pattern").toArray(new String[0]);
        Pattern pattern = new Pattern(patternList);
        PatternPane pane = new PatternPane(0, 0, 9, 6, pattern);

        for(String path : conf.getConfigurationSection("contents").getKeys(false)) {

            ClickGuiStack report = new ClickGuiStack(Objects.requireNonNull(
                    conf.getConfigurationSection("contents." + path)), player);

            ItemStack itemStack = report.getGuiItem();
            String patternID = report.getPatternID();
            List<String> actions = report.getActions();

            if (report.getGuiItem().getItemMeta() instanceof SkullMeta skullMeta) {
                PlayerProfile playerProfile = Bukkit.createPlayerProfile("LightningDesign");
                skullMeta.setOwnerProfile(playerProfile);
                report.getGuiItem().setItemMeta(skullMeta);
                itemStack.setItemMeta(skullMeta);
            }

            pane.bindItem(patternID.charAt(0), new GuiItem(itemStack, inventoryClickEvent -> {

                if(inventoryClickEvent.getAction().equals(InventoryAction.HOTBAR_SWAP)) {
                    return;
                }

                for(String action : actions) {

                    String[] actionSplit = new String[0];

                    if(action.contains(";")) {
                        actionSplit = action.split(";");
                    }

                    if(action.equalsIgnoreCase("close")) {
                        player.closeInventory();
                        return;
                    }
                }

            }));
        }

        PaginatedPane itemContents = new PaginatedPane(1, 1, 7, 3);
        PatternPane pagingPane = new PatternPane(0, 0, 9, 6, pattern);
        List<GuiItem> pageItems = new ArrayList<>();

        for(ReportManager reportManager : LightReports.instance.getReportCache()) {

            if(!reportManager.getStatus().equals(ReportStatus.OPEN)) {
                continue;
            }

            ClickGuiStack report = new ClickGuiStack(Objects.requireNonNull(
                    conf.getConfigurationSection("fill-items.click-item")), player);

            ItemStack skullItemStack = report.getGuiItem();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String createdDateStr = simpleDateFormat.format(reportManager.getCreateDate());

            OfflinePlayer reporter = Bukkit.getOfflinePlayer(reportManager.getReporter());
            OfflinePlayer reported = Bukkit.getOfflinePlayer(reportManager.getReported());

            report.getLore().forEach(line -> {
                report.getLore().set(report.getLore().indexOf(line), line
                        .replace("#reporter#", reporter.getName() == null ? "Unknown" : reporter.getName())
                        .replace("#reported#", reported.getName() == null ? "Unknown" : reported.getName())
                        .replace("#reason#", reportManager.getReason())
                        .replace("#created#", createdDateStr)
                        .replace("#status#", reportManager.getStatus().getType())
                );
            });

            report.setDisplayName(report.getDisplayName()
                    .replace("#id#", reportManager.getReportID().toString())
            );

            if (report.getGuiItem().getItemMeta() instanceof SkullMeta skullMeta) {
                PlayerProfile playerProfile = Bukkit.createPlayerProfile(reported.getName() == null ? "Gronkh" : reported.getName());
                skullMeta.setOwnerProfile(playerProfile);
                report.getGuiItem().setItemMeta(skullMeta);
                skullItemStack.setItemMeta(skullMeta);
            }

            pageItems.add(new GuiItem(skullItemStack, event -> {

                HandlingInventory handlingInventory = new HandlingInventory(player, reportManager);
                handlingInventory.openInventory();

            }));
        }

        itemContents.populateWithGuiItems(pageItems);

        gui.addPane(pane);
        gui.addPane(itemContents);
        gui.addPane(pagingPane);
        gui.show(player);
    }
}
