package io.lightplugins.reports.modules.reports;

import io.lightplugins.reports.Light;
import io.lightplugins.reports.modules.reports.commands.ReportCommand;
import io.lightplugins.reports.modules.reports.commands.ReportHistoryCommand;
import io.lightplugins.reports.modules.reports.commands.ReportReloadCommand;
import io.lightplugins.reports.modules.reports.commands.ReportResolveCommand;
import io.lightplugins.reports.modules.reports.config.MessageParams;
import io.lightplugins.reports.modules.reports.config.SettingParams;
import io.lightplugins.reports.modules.reports.enums.ReportStatus;
import io.lightplugins.reports.modules.reports.manager.ReportManager;
import io.lightplugins.reports.util.SubCommand;
import io.lightplugins.reports.util.interfaces.LightModule;
import io.lightplugins.reports.util.manager.CommandManager;
import io.lightplugins.reports.util.manager.FileManager;
import lombok.Getter;
import org.bukkit.command.PluginCommand;

import java.util.*;

public class LightReports implements LightModule {

    public static LightReports instance;
    public boolean isModuleEnabled = false;

    public final String moduleName = "report";
    public final String adminPerm = "lightreports." + moduleName + ".admin";
    public final String supporterPerm = "lightreports." + moduleName + ".support";
    private final ArrayList<SubCommand> subCommands = new ArrayList<>();
    @Getter
    private final List<ReportManager> reportCache = new ArrayList<>();

    @Getter
    public static SettingParams settingParams;
    @Getter
    public static MessageParams messageParams;

    @Getter
    private FileManager settings;
    @Getter
    private FileManager language;
    @Getter
    private FileManager storage;

    @Getter
    private FileManager resolveInventory;
    @Getter
    private FileManager overviewInventory;
    @Getter
    private FileManager historyInventory;

    @Override
    public void enable() {
        Light.getDebugPrinting().print(
                "Starting " + this.moduleName + " module...");
        instance = this;
        Light.getDebugPrinting().print(
                "Creating default files for module " + this.moduleName + " module...");

        initFiles();

        settingParams = new SettingParams(this);

        Light.getDebugPrinting().print(
                "Selecting module language for module " + this.moduleName + "...");
        selectLanguage();
        messageParams = new MessageParams(language);
        Light.getDebugPrinting().print(
                "Registering subcommands for module " + this.moduleName + "...");
        initSubCommands();
        loadReportCache();
        this.isModuleEnabled = true;
        Light.getDebugPrinting().print(
                "Successfully started module " + this.moduleName + "!");
    }

    @Override
    public void disable() { }

    @Override
    public void reload() {
        Light.getConsolePrinting().print(Light.consolePrefix +
                "Reloading " + this.moduleName + " module...");
        settings.reloadConfig(moduleName + "/settings.yml");
        settingParams = new SettingParams(this);
        selectLanguage();
        language.reloadConfig(moduleName + "/language/" + settingParams.getModuleLanguage() + ".yml");
        messageParams = new MessageParams(language);
        Light.getConsolePrinting().print(Light.consolePrefix +
                "Successfully reloaded " + this.moduleName + " module!");
        storage.reloadConfig(moduleName + "/storage.yml");

        // clear and regenerate the report cache
        loadReportCache();

        resolveInventory.reloadConfig(moduleName + "/inventories/report-handle.yml");
        overviewInventory.reloadConfig(moduleName + "/inventories/report-overview.yml");
        historyInventory.reloadConfig(moduleName + "/inventories/report-history.yml");
    }

    @Override
    public boolean isEnabled() {
        return isModuleEnabled;
    }

    @Override
    public String getName() {
        return moduleName;
    }

    private void initFiles() {

        // Create the default files for the module if they do not exist.
        settings = new FileManager(
                Light.instance, moduleName + "/settings.yml", true);
        storage = new FileManager(
                Light.instance, moduleName + "/storage.yml", false);
        resolveInventory = new FileManager(
                Light.instance, moduleName + "/inventories/report-handle.yml", false);
        overviewInventory = new FileManager(
                Light.instance, moduleName + "/inventories/report-overview.yml", false);
        historyInventory = new FileManager(
                Light.instance, moduleName + "/inventories/report-history.yml", false);

    }

    private void selectLanguage() {
        language = Light.instance.selectLanguage(settingParams.getModuleLanguage(), moduleName);
    }

    private void initSubCommands() {

        PluginCommand moduleCommand = Light.instance.getCommand("report");
        subCommands.add(new ReportReloadCommand());
        subCommands.add(new ReportCommand());
        subCommands.add(new ReportResolveCommand());
        subCommands.add(new ReportHistoryCommand());
        new CommandManager(moduleCommand, subCommands);
    }

    private void loadReportCache() {
        // Load all reports from the storage file
        // Objects.requireNonNull() just used for simple check → changing in the future to a more complex check.

        // Check if the reports section is empty → skipp on load.
        if(storage.getConfig().getConfigurationSection("reports") == null) {
            Light.getConsolePrinting().print("No reports found in storage file. Skipping cache ...");
            return;
        }

        // Clear the cache if it is not empty on module reload.
        if(!getReportCache().isEmpty()) {
            getReportCache().clear();
        }

        int amount = 0;

        for(String path : Objects.requireNonNull(storage.getConfig().getConfigurationSection("reports")).getKeys(false)) {
            // create the report manager from the storage file
            ReportManager reportManager = new ReportManager(
                    UUID.fromString(path),
                    UUID.fromString(Objects.requireNonNull(storage.getConfig().getString("reports." + path + ".reporter"))),
                    UUID.fromString(Objects.requireNonNull(storage.getConfig().getString("reports." + path + ".reported"))),
                    storage.getConfig().getString("reports." + path + ".reason"),
                    Objects.equals(storage.getConfig().getString(
                            "reports." + path + ".resolvedBy"), "") ? null
                            : UUID.fromString(Objects.requireNonNull(storage.getConfig().getString("reports." + path + ".resolvedBy"))),
                    ReportStatus.valueOf(storage.getConfig().getString("reports." + path + ".status").toUpperCase())
            );
            // Set the create and solve date from the storage.yml
            reportManager.setCreateDate(new Date(storage.getConfig().getLong("reports." + path + ".created")));
            reportManager.setSolveDate(new Date(storage.getConfig().getLong("reports." + path + ".solved")));
            // Add the report to the cache
            reportCache.add(reportManager);
            amount ++;
        }

        Light.getConsolePrinting().print("Loaded " + amount + " report(s) into cache.");
    }
}
