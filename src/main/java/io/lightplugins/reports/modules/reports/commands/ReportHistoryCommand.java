package io.lightplugins.reports.modules.reports.commands;

import io.lightplugins.reports.Light;
import io.lightplugins.reports.modules.reports.LightReports;
import io.lightplugins.reports.modules.reports.enums.ReportStatus;
import io.lightplugins.reports.modules.reports.inv.HistoryInventory;
import io.lightplugins.reports.modules.reports.inv.OverviewInventory;
import io.lightplugins.reports.modules.reports.manager.ReportManager;
import io.lightplugins.reports.util.SubCommand;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReportHistoryCommand extends SubCommand {
    @Override
    public List<String> getName() {
        return List.of("history");
    }

    @Override
    public String getDescription() {
        return "Opens a GUI with closed reports.";
    }

    @Override
    public String getSyntax() {
        return "/report history";
    }

    @Override
    public int maxArgs() {
        return 1;
    }

    @Override
    public String getPermission() {
        return LightReports.instance.supporterPerm;
    }

    @Override
    public TabCompleter registerTabCompleter() {

        return (commandSender, command, s, args) -> {
            if(!commandSender.hasPermission(getPermission())) {
                return null;
            }
            return List.of("history");
        };
    }

    @Override
    public boolean performAsPlayer(Player player, String[] args) throws ExecutionException, InterruptedException {

        List<ReportManager> closedReports = LightReports.instance.getReportCache().stream()
                .filter(reportManager -> reportManager.getStatus().equals(ReportStatus.ACCEPTED)
                        || reportManager.getStatus().equals(ReportStatus.REJECTED))
                .toList();

        if(closedReports.isEmpty()) {
            Light.getMessageSender().sendPlayerMessage(LightReports.getMessageParams().reportHistoryEmpty(), player);
            return false;
        }

        HistoryInventory historyInventory = new HistoryInventory(player);
        historyInventory.openInventory();

        return false;
    }

    @Override
    public boolean performAsConsole(ConsoleCommandSender sender, String[] args) throws ExecutionException, InterruptedException {
        return false;
    }
}
