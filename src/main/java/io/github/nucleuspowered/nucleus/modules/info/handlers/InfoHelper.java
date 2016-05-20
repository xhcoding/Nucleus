/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.info.handlers;

import io.github.nucleuspowered.nucleus.ChatUtil;
import io.github.nucleuspowered.nucleus.internal.TextFileController;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;

public class InfoHelper {

    public static void sendMotd(TextFileController tfc, CommandSource src, ChatUtil chatUtil, String motdTitle) {
        // Get the text.
        List<Text> textList = chatUtil.getFromStrings(tfc.getFileContents(), src);

        PaginationService ps = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
        PaginationList.Builder pb = ps.builder().contents(textList).padding(Text.of(TextColors.GOLD, "-"));

        if (!motdTitle.isEmpty()) {
            pb.title(TextSerializers.FORMATTING_CODE.deserialize(motdTitle));
        }

        if (src instanceof ConsoleSource) {
            pb.linesPerPage(-1);
        }

        pb.sendTo(src);
    }
}