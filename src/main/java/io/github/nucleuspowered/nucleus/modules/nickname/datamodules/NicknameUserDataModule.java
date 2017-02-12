/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname.datamodules;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataKey;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataModule;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.modules.nickname.NicknameModule;
import io.github.nucleuspowered.nucleus.modules.nickname.config.NicknameConfig;
import io.github.nucleuspowered.nucleus.modules.nickname.config.NicknameConfigAdapter;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;

import javax.annotation.Nullable;

public class NicknameUserDataModule extends DataModule.ReferenceService<ModularUserService> {

    // Transient, null infers that we need to retrieve the value.
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Optional<String> prefix = null;

    static {
        Nucleus.getNucleus().registerReloadable(() -> prefix = null);
    }

    private Text nickname = null;

    @DataKey("nickname")
    @Nullable
    private String nicknameStore;

    public NicknameUserDataModule(ModularUserService modularDataService) {
        super(modularDataService);
    }

    public Optional<Text> getNicknameWithPrefix() {
        if (getNicknameAsText().isPresent()) {
            Optional<String> p = getNickPrefix();
            if (!p.isPresent() || p.get().isEmpty()) {
                return getNicknameAsText();
            }

            return Optional.of(Text.join(TextSerializers.FORMATTING_CODE.deserialize(p.get()), getNicknameAsText().get()));
        }

        return Optional.empty();
    }

    public Optional<Text> getNicknameAsText() {
        if (this.nickname != null) {
            return Optional.of(this.nickname);
        }

        Optional<String> os = getNicknameAsString();
        if (!os.isPresent()) {
            return Optional.empty();
        }

        nickname = TextSerializers.FORMATTING_CODE.deserialize(os.get());
        return Optional.of(nickname);
    }

    public Optional<String> getNicknameAsString() {
        return Optional.ofNullable(nicknameStore);
    }

    public void setNickname(String nickname) {
        this.nicknameStore = nickname;
        this.nickname = null;

        Optional<String> p = getNickPrefix();
        if (p.isPresent() && !p.get().isEmpty()) {
            nickname = p + nickname;
        }

        Text nick = TextSerializers.FORMATTING_CODE.deserialize(nickname);
        getService().getPlayer().ifPresent(x -> x.offer(Keys.DISPLAY_NAME, nick));
    }

    public void removeNickname() {
        nickname = null;
        nicknameStore = null;
        getService().getPlayer().ifPresent(x -> x.remove(Keys.DISPLAY_NAME));
    }

    private static Optional<String> getNickPrefix() {
        if (prefix == null) {
            prefix = Nucleus.getNucleus().getConfigValue(NicknameModule.ID, NicknameConfigAdapter.class, NicknameConfig::getPrefix);
        }

        return prefix;
    }
}
