/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.info.handlers;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.TextFileController;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.stream.Collectors;

public class InfoHelper {

    private static final Text padding = Text.of(TextColors.GOLD, "-");
    private static final Text emptyPadding = Text.of(" ");

    public static void sendInfo(TextFileController tfc, CommandSource src, String motdTitle) {
        sendInfoNT(tfc.getFileContentsAsText(), src, motdTitle);
    }

    public static List<Text> getTextFromNucleusTextTemplates(List<NucleusTextTemplate> textTemplates, CommandSource source) {
        return textTemplates.stream().map(x -> x.getForCommandSource(source)).collect(Collectors.toList());
    }

    public static void sendInfoNT(List<NucleusTextTemplate> tfc, CommandSource src, String motdTitle) {
        sendInfo(getTextFromNucleusTextTemplates(tfc, src), src, motdTitle);
    }

    private static void sendInfo(List<Text> texts, CommandSource src, String motdTitle) {

        PaginationService ps = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
        PaginationList.Builder pb = Util.getPaginationBuilder(src).contents(texts);

        if (!motdTitle.isEmpty()) {
            pb.title(TextSerializers.FORMATTING_CODE.deserialize(motdTitle)).padding(padding);
        } else {
            pb.padding(emptyPadding);
        }

        pb.sendTo(src);
    }
}