package uk.co.drnaylor.minecraft.quickstart.internal.interfaces;

import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.api.data.mail.MailData;

import java.time.Instant;
import java.util.List;

public interface InternalQuickStartUser extends QuickStartUser {

    void setLastLogin(Instant login);

    void setLastLogout(Instant logout);

    List<MailData> getMail();

    void addMail(MailData mailData);

    void clearMail();

    /**
     * Determines whether QuickStart thinks the player should be flying, but does not look at the current status of the
     * player. In other words, what did the data file say?
     *
     * @return <code>true</code> if so.
     */
    boolean isFlyingSafe();

    /**
     * Determines whether QuickStart thinks the player should be invulnerable, but does not look at the current status of the
     * player. In other words, what did the data file say?
     *
     * @return <code>true</code> if so.
     */
    boolean isInvulnerableSafe();

    void setOnLogout();
}
