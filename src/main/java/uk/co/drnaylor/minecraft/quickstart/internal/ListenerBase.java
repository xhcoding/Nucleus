package uk.co.drnaylor.minecraft.quickstart.internal;

import uk.co.drnaylor.minecraft.quickstart.QuickStart;

public class ListenerBase {
    protected final QuickStart plugin;

    protected ListenerBase(QuickStart plugin) {
        this.plugin = plugin;
    }
}
