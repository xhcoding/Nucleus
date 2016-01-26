package uk.co.drnaylor.minecraft.quickstart.internal.interfaces;

import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;

import java.time.Instant;

public interface InternalQuickStartUser extends QuickStartUser {

    void setLastLogin(Instant login);

    void setLastLogout(Instant logout);
}
