package uk.co.drnaylor.minecraft.quickstart.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import jdk.nashorn.internal.ir.annotations.Immutable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.User;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.api.data.MuteData;
import uk.co.drnaylor.minecraft.quickstart.api.data.mail.MailData;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserConfig extends AbstractConfig<ConfigurationNode, GsonConfigurationLoader> implements InternalQuickStartUser {
    private final User user;
    private MuteData muteData;
    private boolean socialSpy;
    private Instant login;
    private Instant logout;
    private boolean invulnerable;
    private List<MailData> mailDataList;

    public UserConfig(Path file, User user) throws IOException, ObjectMappingException {
        super(file);
        this.user = user;
    }

    @Override
    public void load() throws IOException, ObjectMappingException {
        node = loader.load();
        if (node.getNode("mute").isVirtual()) {
            muteData = null;
        } else {
            muteData = node.getNode("mute").getValue(TypeToken.of(MuteData.class));
        }

        socialSpy = node.getNode("socialspy").getBoolean(false);
        login = Instant.ofEpochMilli(node.getNode("timestamp", "login").getLong());
        logout = Instant.ofEpochMilli(node.getNode("timestamp", "logout").getLong());
        invulnerable = node.getNode("invulnerable").getBoolean();

        // This returned an immutable list, so we want to make it mutable.
        mailDataList = Lists.newArrayList(node.getNode("mail").getList(TypeToken.of(MailData.class)));
    }

    @Override
    public void save() throws IOException, ObjectMappingException {
        if (muteData == null) {
            node.removeChild("mute");
        } else {
            node.getNode("mute").setValue(TypeToken.of(MuteData.class), muteData);
        }

        node.getNode("socialspy").setValue(isSocialSpy());
        node.getNode("timestamp", "login").setValue(login.toEpochMilli());
        node.getNode("timestamp", "logout").setValue(logout.toEpochMilli());
        node.getNode("invulnerable").setValue(invulnerable);

        // Type erasure! Thanks to Google's Type Token, we work around it.
        node.getNode("mail").setValue(new TypeToken<List<MailData>>() {}, mailDataList);
        super.save();
    }

    @Override
    protected GsonConfigurationLoader getLoader(Path file) {
        return GsonConfigurationLoader.builder().setPath(file).build();
    }

    @Override
    protected ConfigurationNode getDefaults() {
        return SimpleConfigurationNode.root();
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Optional<MuteData> getMuteData() {
        return Optional.ofNullable(muteData);
    }

    @Override
    public void setMuteData(MuteData data) {
        this.muteData = data;
    }

    @Override
    public void removeMuteData() {
        this.muteData = null;
    }

    @Override
    public boolean isSocialSpy() {
        socialSpy = socialSpy && (user.hasPermission(QuickStart.PERMISSIONS_PREFIX + "socialspy.base") || user.hasPermission(QuickStart.PERMISSIONS_ADMIN));
        return socialSpy;
    }

    @Override
    public boolean setSocialSpy(boolean socialSpy) {
        this.socialSpy = socialSpy;

        // Permission checks! Return true if it's what we wanted.
        return isSocialSpy() == socialSpy;
    }

    @Override
    public boolean isInvulnerable() {
        return invulnerable;
    }

    @Override
    public void setInvulnerable(boolean invuln) {
        invulnerable = invuln;
    }

    @Override
    public Instant getLastLogin() {
        return login;
    }

    public void setLastLogin(Instant login) {
        this.login = login;
    }

    @Override
    public Instant getLastLogout() {
        return logout;
    }

    @Override
    public List<MailData> getMail() {
        return ImmutableList.copyOf(mailDataList);
    }

    @Override
    public void addMail(MailData mailData) {
        mailDataList.add(mailData);
    }

    @Override
    public void clearMail() {
        mailDataList.clear();
    }

    public void setLastLogout(Instant logout) {
        this.logout = logout;
    }

    @Override
    public UUID getUniqueID() {
        return user.getUniqueId();
    }
}
