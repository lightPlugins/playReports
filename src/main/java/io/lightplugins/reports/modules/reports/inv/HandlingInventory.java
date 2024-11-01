package io.lightplugins.reports.modules.reports.inv;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
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

import java.util.List;
import java.util.Objects;

public class HandlingInventory {

    private final ChestGui gui = new ChestGui(6, "Init");
    private final Player player;
    private final ReportManager reportManager;
    private BukkitTask bukkitTask;

    public HandlingInventory(Player player, ReportManager reportManager) {
        this.player = player;
        this.reportManager = reportManager;
    }

    public void openInventory() {
        // Open the inventory
        FileConfiguration conf = LightReports.instance.getResolveInventory().getConfig();
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

            report.setDisplayName(report.getDisplayName()
                    .replace("#name#", reportManager.getReportedName())
            );

            if (report.getGuiItem().getItemMeta() instanceof SkullMeta skullMeta) {
                OfflinePlayer reported = Bukkit.getOfflinePlayer(reportManager.getReported());
                PlayerProfile playerProfile = Bukkit.createPlayerProfile(reported.getName() == null ? "Gronkh" : reported.getName());
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

                    if(actionSplit[0].equalsIgnoreCase("report")) {

                        if(actionSplit[1].equalsIgnoreCase("accept")) {
                            Light.getMessageSender().sendPlayerMessage(LightReports.getMessageParams().reportAccepted()
                                    .replace("#player#", reportManager.getReportedName()), player);
                            reportManager.setSolved(player.getUniqueId(), ReportStatus.ACCEPTED);
                            player.closeInventory();
                            return;
                        }

                        if(actionSplit[1].equalsIgnoreCase("deny")) {
                            Light.getMessageSender().sendPlayerMessage(LightReports.getMessageParams().reportDenied()
                                    .replace("#player#", reportManager.getReportedName()), player);
                            reportManager.setSolved(player.getUniqueId(), ReportStatus.REJECTED);
                            player.closeInventory();
                            return;
                        }

                        if(actionSplit[1].equalsIgnoreCase("delete")) {
                            Light.getMessageSender().sendPlayerMessage(LightReports.getMessageParams().reportDenied()
                                    .replace("#player#", reportManager.getReportedName()), player);
                            player.closeInventory();
                            reportManager.deleteReportEntry();
                            LightReports.instance.getReportCache().remove(reportManager);
                            return;
                        }
                    }

                    if(actionSplit[0].equalsIgnoreCase("open")) {

                        if(actionSplit[1].equalsIgnoreCase("overview")) {
                            new OverviewInventory(player).openInventory();
                            return;
                        }
                    }

                    if(action.equalsIgnoreCase("close")) {
                        player.closeInventory();
                        return;
                    }
                }

            }));
        }
        gui.addPane(pane);
        gui.show(player);
    }
}
