/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.config;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;

import java.util.List;

public class MessageConfigAdapter extends NucleusConfigAdapter.StandardWithSimpleDefault<MessageConfig> {

    @Override protected List<Transformation> getTransformations() {
        return Lists.newArrayList(
            // 0.18.0
            Transformation.moveTopLevel("msg-socialspy-prefix", "socialspy", "msg-prefix"),
            Transformation.moveTopLevel("socialspy-cancelled-messages", "socialspy", "show-cancelled-messages"),
            Transformation.moveTopLevel("socialspy-cancelled-tag", "socialspy", "cancelled-messages-tag"),
            Transformation.moveTopLevel("socialspy-only-players", "socialspy", "show-only-players")
        );
    }

    public MessageConfigAdapter() {
        super(MessageConfig.class);
    }
}
