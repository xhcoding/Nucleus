package uk.co.drnaylor.minecraft.quickstart.listeners;

import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.ListenerBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;

@Modules(PluginModule.MUTES)
public class MuteListener extends ListenerBase {
    public MuteListener(QuickStart plugin) {
        super(plugin);
    }
}
