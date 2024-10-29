package io.lightplugins.reports.modules.reports.commands;

import io.lightplugins.reports.Light;
import io.lightplugins.reports.modules.reports.LightReports;
import io.lightplugins.reports.util.SubCommand;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReportReloadCommand extends SubCommand {
    @Override
    public List<String> getName() {
        return List.of("reload");
    }

    @Override
    public String getDescription() {
        return "Reload all the .yml files for the current module.";
    }

    @Override
    public String getSyntax() {
        return "/report reload";
    }

    @Override
    public int maxArgs() {
        return 1;
    }

    @Override
    public String getPermission() {
        return LightReports.instance.adminPerm;
    }

    @Override
    public TabCompleter registerTabCompleter() {

        return (commandSender, command, s, args) -> {

            if(!commandSender.hasPermission(getPermission())) {
                return null;
            }

            if(args.length == 1) {
                return getName();
            }

            return null;
        };
    }

    @Override
    public boolean performAsPlayer(Player player, String[] args) throws ExecutionException, InterruptedException {

        LightReports.instance.reload();
        Light.getMessageSender().sendPlayerMessage(LightReports.getMessageParams().moduleReload()
                .replace("#module#", Light.instance.getName()), player);

        return false;
    }

    @Override
    public boolean performAsConsole(ConsoleCommandSender sender, String[] args) throws ExecutionException, InterruptedException {

        LightReports.instance.reload();
        Light.getConsolePrinting().print("Reloaded " + LightReports.instance.getName() + " module.");

        return false;
    }
}
