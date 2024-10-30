package io.lightplugins.reports.modules.reports.commands;

import io.lightplugins.reports.Light;
import io.lightplugins.reports.modules.reports.LightReports;
import io.lightplugins.reports.modules.reports.enums.ReportStatus;
import io.lightplugins.reports.modules.reports.manager.ReportManager;
import io.lightplugins.reports.util.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class ReportCommand extends SubCommand {

    private final List<Player> onCooldown = new ArrayList<>();

    @Override
    public List<String> getName() {
        return List.of("player");
    }

    @Override
    public String getDescription() {
        return "Report a Player for a specific reason.";
    }

    @Override
    public String getSyntax() {
        return "/report player <player> <reason>";
    }

    @Override
    public int maxArgs() {
        return 3;
    }

    @Override
    public String getPermission() {
        // every Player can report someone
        return "";
    }

    @Override
    public TabCompleter registerTabCompleter() {
        return (commandSender, command, s, args) -> {

            if (args.length == 1) {
                // return the names of all online players
                return getName();
            }

            if (args.length == 2) {
                // return the names of all online players
                return Arrays.asList(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toArray(String[]::new));
            }

            if (args.length == 3) {
                // return the reasons for reporting
                return LightReports.getSettingParams().defaultWrapper().getReportReasons();
            }

            return null;
        };
    }

    @Override
    public boolean performAsPlayer(Player player, String[] args) throws ExecutionException, InterruptedException {

        // check if the player is not reporting himself
        // REMOVE “!" for public usage!!!
        if(!player.getName().equalsIgnoreCase(args[1])) {
            Light.getMessageSender().sendPlayerMessage(LightReports.getMessageParams().cannotReportSelf(), player);
            return false;
        }

        // check if reason is valid → ReportStatus
        boolean validReason = false;
        for (String reason : LightReports.getSettingParams().defaultWrapper().getReportReasons()) {
            if (reason.equalsIgnoreCase(args[2])) {
                validReason = true;
                break;
            }
        }

        if (!validReason) {
            Light.getMessageSender().sendPlayerMessage(LightReports.getMessageParams().invalidReason()
                    .replace("#reason#", args[2]), player);
            return false;
        }

        // check if the player is online
        Player target = Bukkit.getPlayer(args[1]);
        if(target == null) {
            Light.getMessageSender().sendPlayerMessage(LightReports.getMessageParams().playerNotFound()
                            .replace("#player#", args[1]), player);
            return false;
        }

        // check if the player is on cooldown
        // currently not showing the remaining time -> changing in the future
        if(onCooldown.contains(player)) {
            String waitTime = String.valueOf(LightReports.getSettingParams().defaultWrapper().getCooldownTime());
            Light.getMessageSender().sendPlayerMessage(LightReports.getMessageParams().onCooldown()
                    .replace("#time#", waitTime), player);
            return false;
        }

        // check if target is already reported with the same reason and status is OPEN
        for(ReportManager reportManager : LightReports.instance.getReportCache()) {
            if(reportManager.getReporter().equals(player.getUniqueId()) &&
                    reportManager.getReported().equals(target.getUniqueId()) &&
                    reportManager.getReason().equalsIgnoreCase(args[2]) &&
                    reportManager.getStatus().equals(ReportStatus.OPEN)) {
                Light.getMessageSender().sendPlayerMessage(LightReports.getMessageParams().alreadyReported()
                        .replace("#player#", target.getName())
                        .replace("#reason#", args[2]), player);
                return false;
            }
        }

        // create new Report
        UUID reporter = player.getUniqueId();
        UUID reported = target.getUniqueId();
        String reason = args[2];
        ReportStatus reportStatus = ReportStatus.OPEN;

        ReportManager reportManager = new ReportManager(
                UUID.randomUUID(), reporter, reported, reason, null, reportStatus);

        reportManager.createNewReportEntry();
        LightReports.instance.getReportCache().add(reportManager);

        Light.getMessageSender().sendPlayerMessage(LightReports.getMessageParams().reportSuccess()
                        .replace("#player#", target.getName())
                        .replace("#reason#", reason)
                , player);

        // add player to cooldown and remove him after 60 seconds
        onCooldown.add(player);
        int cooldownTime = LightReports.getSettingParams().defaultWrapper().getCooldownTime();
        Bukkit.getScheduler().runTaskLater(Light.instance, () -> onCooldown.remove(player), 20L * cooldownTime);

        for(Player audience : Bukkit.getOnlinePlayers()) {
            if(player.hasPermission(LightReports.instance.supporterPerm)) {
                Light.getMessageSender().sendPlayerMessage(LightReports.getMessageParams().onReportNotificationMessage()
                        .replace("#player#", player.getName())
                        .replace("#reporter#", player.getName())
                        .replace("#reported#", target.getName())
                        .replace("#reason#", reason), player);

                Light.getMessageSender().sendTitleMessage(
                        LightReports.getMessageParams().onReportNotificationTitleUpper(),
                        LightReports.getMessageParams().onReportNotificationTitleLower(), audience);

                AtomicInteger count = new AtomicInteger();

                int[] taskId = new int[1];
                taskId[0] = Bukkit.getScheduler().runTaskTimer(Light.instance, () -> {
                    count.getAndIncrement();
                    if(count.get() == 4) {
                        Bukkit.getScheduler().cancelTask(taskId[0]);
                    }
                    audience.playSound(audience.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                }, 0L, 3L).getTaskId(); // 20L is the delay between each sound (1 second)
            }
        }



        return false;
    }

    @Override
    public boolean performAsConsole(ConsoleCommandSender sender, String[] args) throws ExecutionException, InterruptedException {
        return false;
    }
}
