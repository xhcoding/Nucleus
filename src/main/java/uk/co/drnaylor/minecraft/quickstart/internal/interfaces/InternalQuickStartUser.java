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
}
