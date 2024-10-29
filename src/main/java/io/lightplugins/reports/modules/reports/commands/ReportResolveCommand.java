package io.lightplugins.reports.modules.reports.commands;

import io.lightplugins.reports.modules.reports.LightReports;
import io.lightplugins.reports.modules.reports.inv.OverviewInventory;
import io.lightplugins.reports.util.SubCommand;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReportResolveCommand extends SubCommand {
    @Override
    public List<String> getName() {
        return List.of("resolve");
    }

    @Override
    public String getDescription() {
        return "Opens a GUI with open reports.";
    }

    @Override
    public String getSyntax() {
        return "/report resolve";
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
            return List.of("resolve");
        };
    }

    @Override
    public boolean performAsPlayer(Player player, String[] args) throws ExecutionException, InterruptedException {

        OverviewInventory overviewInventory = new OverviewInventory(player);
        overviewInventory.openInventory();

        return false;
    }

    @Override
    public boolean performAsConsole(ConsoleCommandSender sender, String[] args) throws ExecutionException, InterruptedException {
        return false;
    }
}
