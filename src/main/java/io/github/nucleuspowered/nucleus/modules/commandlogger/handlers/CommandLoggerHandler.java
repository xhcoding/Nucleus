/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.commandlogger.handlers;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.logging.AbstractLoggingHandler;
import io.github.nucleuspowered.nucleus.modules.commandlogger.config.CommandLoggerConfigAdapter;

public class CommandLoggerHandler extends AbstractLoggingHandler {

    private final CommandLoggerConfigAdapter clca;

    public CommandLoggerHandler() {
        super("command", "cmds");
        this.clca = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(CommandLoggerConfigAdapter.class);
    }

    public void onReload() throws Exception {
        if (clca.getNodeOrDefault().isLogToFile() && logger == null) {
            this.createLogger();
        } else if (!clca.getNodeOrDefault().isLogToFile() && logger != null) {
            onShutdown();
        }
    }

    @Override protected boolean enabledLog() {
        return clca.getNodeOrDefault().isLogToFile();
    }
}
