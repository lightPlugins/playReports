package io.lightplugins.reports.exceptions;

import io.lightplugins.reports.Light;
import io.lightplugins.reports.util.interfaces.LightModule;

public class ModuleNotEnabledException extends Exception {

    public ModuleNotEnabledException(LightModule module) {
        super(Light.consolePrefix + "The Module §e" + module.getName() + "§r is not enabled");
    }
}
