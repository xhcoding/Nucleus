/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.internal.services;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.InvulnerabilityData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import uk.co.drnaylor.minecraft.quickstart.api.data.JailData;
import uk.co.drnaylor.minecraft.quickstart.api.data.MuteData;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;
import uk.co.drnaylor.minecraft.quickstart.api.data.mail.MailData;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.NoSuchWorldException;
import uk.co.drnaylor.minecraft.quickstart.commands.message.SocialSpyCommand;
import uk.co.drnaylor.minecraft.quickstart.config.serialisers.LocationNode;
import uk.co.drnaylor.minecraft.quickstart.config.serialisers.UserConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionUtil;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UserService implements InternalQuickStartUser {
    private final User user;
    private UserConfig config;
    private final GsonConfigurationLoader loader;

    public UserService(Path file, User user) throws IOException, ObjectMappingException {
        this.loader = GsonConfigurationLoader.builder().setPath(file).build();
        this.user = user;

        load();
    }

    private void load() throws IOException, ObjectMappingException {
        ConfigurationNode cn = loader.load();
        config = cn.getValue(TypeToken.of(UserConfig.class), new UserConfig());
    }

    public void save() throws IOException, ObjectMappingException {
        ConfigurationNode cn = SimpleConfigurationNode.root();
        cn.setValue(TypeToken.of(UserConfig.class), config);
        loader.save(cn);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Optional<MuteData> getMuteData() {
        return Optional.ofNullable(config.getMuteData());
    }

    @Override
    public void setMuteData(MuteData data) {
        config.setMuteData(data);
    }

    @Override
    public void removeMuteData() {
        config.setMuteData(null);
    }

    @Override
    public boolean isSocialSpy() {
        // Only a spy if they have the permission!
        return config.isSocialspy() && getPermissionUtil(SocialSpyCommand.class).getBasePermissions().stream().anyMatch(user::hasPermission);
    }

    @Override
    public boolean setSocialSpy(boolean socialSpy) {
        config.setSocialspy(socialSpy);

        // Permission checks! Return true if it's what we wanted.
        return isSocialSpy() == socialSpy;
    }

    @Override
    public boolean isInvulnerable() {
        if (user.isOnline()) {
            config.setInvulnerable(user.getPlayer().get().get(Keys.INVULNERABILITY_TICKS).orElse(0) > 0);
        }

        return config.isInvulnerable();
    }

    @Override
    public boolean setInvulnerable(boolean invuln) {
        if (user.isOnline()) {
            Player pl = user.getPlayer().get();
            Optional<InvulnerabilityData> oid = pl.get(InvulnerabilityData.class);

            if (!oid.isPresent()) {
                return false;
            }

            InvulnerabilityData id = oid.get();
            id.invulnerableTicks().set(invuln ? Integer.MAX_VALUE : 0);
            if (!pl.offer(id).isSuccessful()) {
                return false;
            }
        }

        config.setInvulnerable(invuln);
        return true;
    }

    @Override
    public boolean isFlying() {
        if (user.isOnline()) {
            config.setFly(user.getPlayer().get().get(Keys.CAN_FLY).orElse(false));
        }

        return config.isFly();
    }

    @Override
    public boolean setFlying(boolean fly) {
        if (user.isOnline()) {
            Player pl = user.getPlayer().get();
            if (!fly && !pl.offer(Keys.IS_FLYING, false).isSuccessful()) {
                return false;
            }

            if (!pl.offer(Keys.CAN_FLY, fly).isSuccessful()) {
                return false;
            }
        }

        config.setFly(fly);
        return true;
    }

    @Override
    public Optional<JailData> getJailData() {
        return Optional.ofNullable(config.getJailData());
    }

    @Override
    public Instant getLastLogin() {
        return Instant.ofEpochMilli(config.getLogin());
    }

    @Override
    public void setLastLogin(Instant login) {
        config.setLogin(login.toEpochMilli());
    }

    @Override
    public Instant getLastLogout() {
        return Instant.ofEpochMilli(config.getLogout());
    }

    @Override
    public Optional<WarpLocation> getHome(String home) {
        if (config.getHomeData() == null) {
            return Optional.empty();
        }

        LocationNode ln = config.getHomeData().get(home.toLowerCase());
        if (ln != null) {
            try {
                return Optional.of(new WarpLocation(home.toLowerCase(), ln.getLocation(), ln.getRotation()));
            } catch (NoSuchWorldException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    @Override
    public Map<String, WarpLocation> getHomes() {
        if (config.getHomeData() == null) {
            return Maps.newHashMap();
        }

        return config.getHomeData().entrySet().stream().map(x -> {
            try {
                return new WarpLocation(x.getKey(), x.getValue().getLocation(), x.getValue().getRotation());
            } catch (NoSuchWorldException e) {
                return null;
            }
        }).filter(x -> x != null).collect(Collectors.toMap(WarpLocation::getName, y -> new WarpLocation(y.getName(), y.getLocation(), y.getRotation())));
    }

    @Override
    public boolean setHome(String home, Location<World> location, Vector3d rotation) {
        final Pattern warpName = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{1,15}$");

        Map<String, LocationNode> homeData = config.getHomeData();
        if (homeData == null) {
            homeData = Maps.newHashMap();
        }

        if (homeData.containsKey(home.toLowerCase()) || !warpName.matcher(home).matches()) {
            return false;
        }

        homeData.put(home.toLowerCase(), new LocationNode(location, rotation));
        config.setHomeData(homeData);
        return true;
    }

    @Override
    public boolean deleteHome(String home) {
        Map<String, LocationNode> homeData = config.getHomeData();
        if (homeData == null) {
            return false;
        }

        if (homeData.containsKey(home.toLowerCase())) {
            homeData.remove(home.toLowerCase());
            config.setHomeData(homeData);
            return true;
        }

        return false;
    }

    @Override
    public boolean isTeleportToggled() {
        return config.isTeleportToggled();
    }

    @Override
    public void setTeleportToggled(boolean toggle) {
        config.setTeleportToggled(toggle);
    }

    @Override
    public Text getNicknameAsText() {
        return null;
    }

    @Override
    public String getNicknameAsString() {
        return null;
    }

    @Override
    public void setNickname(String nick) {

    }

    @Override
    public void removeNickname() {

    }

    @Override
    public List<MailData> getMail() {
        return ImmutableList.copyOf(config.getMailDataList());
    }

    @Override
    public void addMail(MailData mailData) {
        List<MailData> mailDataList = config.getMailDataList();
        if (mailDataList == null) {
            mailDataList = Lists.newArrayList();
        }

        mailDataList.add(mailData);
        config.setMailDataList(mailDataList);
    }

    @Override
    public void clearMail() {
        config.setMailDataList(Lists.newArrayList());
    }

    @Override
    public boolean isFlyingSafe() {
        return config.isFly();
    }

    @Override
    public boolean isInvulnerableSafe() {
        return config.isInvulnerable();
    }

    @Override
    public void setJailData(JailData data) {
        config.setJailData(data);
    }

    @Override
    public void removeJailData() {
        setJailData(null);
    }

    @Override
    public void setOnLogout() {
        setLastLogout(Instant.now());

        // Set data based toggles.
        isFlying();
        isInvulnerable();
    }

    @Override
    public Optional<Location<World>> getLocationOnLogin() {
        LocationNode ln = config.getLocationOnLogin();
        if (ln == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(ln.getLocation());
        } catch (NoSuchWorldException e) {
            return Optional.empty();
        }
    }

    @Override
    public void sendToLocationOnLogin(Location<World> worldLocation) {
        config.setLocationOnLogin(new LocationNode(worldLocation));
    }

    @Override
    public void removeLocationOnLogin() {
        config.setLocationOnLogin(null);
    }

    @Override
    public void setLastLogout(Instant logout) {
        config.setLogout(logout.toEpochMilli());
    }

    @Override
    public UUID getUniqueID() {
        return user.getUniqueId();
    }

    @Override
    public boolean jailOnNextLogin() {
        return config.isJailOffline();
    }

    @Override
    public void setJailOnNextLogin(boolean set) {
        config.setJailOffline(!user.isOnline() && set);
    }

    private PermissionUtil getPermissionUtil(Class<? extends CommandBase> c) {
        if (!util.containsKey(c)) {
            Permissions p = c.getAnnotation(Permissions.class);
            util.put(c, new PermissionUtil(p, null));
        }

        return util.get(c);
    }

    private Map<Class<? extends CommandBase>, PermissionUtil> util = new HashMap<>();
}
