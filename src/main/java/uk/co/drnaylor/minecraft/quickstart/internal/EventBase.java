package uk.co.drnaylor.minecraft.quickstart.internal;

import uk.co.drnaylor.minecraft.quickstart.QuickStart;

public class EventBase {
    protected final QuickStart plugin;

    protected EventBase(QuickStart plugin) {
        this.plugin = plugin;
    }
}
