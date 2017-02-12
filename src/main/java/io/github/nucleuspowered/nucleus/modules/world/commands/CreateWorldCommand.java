/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.DifficultyArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.ImprovedCatalogTypeArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.ImprovedGameModeArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.world.config.WorldConfigAdapter;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.CatalogTypes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SuppressWarnings("ALL")
@Permissions(prefix = "world", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"create"}, subcommandOf = WorldCommand.class)
public class CreateWorldCommand extends AbstractCommand<CommandSource> {

    private final String preset = "preset";
    private final String name = "name";
    private final String dimension = "dimension";
    private final String generator = "generator";
    private final String gamemode = "gamemode";
    private final String difficulty = "difficulty";
    private final String modifier = "modifier";
    private final String seed = "seed";

    @Inject private WorldConfigAdapter worldConfigAdapter;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags()
                .valueFlag(GenericArguments.onlyOne(new ImprovedCatalogTypeArgument(Text.of(preset), WorldArchetype.class)), "p", "-" + preset)
                .valueFlag(GenericArguments.onlyOne(new ExtendedDimensionArgument(Text.of(dimension))), "d", "-" + dimension)
                .valueFlag(GenericArguments.onlyOne(new ImprovedCatalogTypeArgument(Text.of(generator), CatalogTypes.GENERATOR_TYPE)), "g", "-" + generator)
                .valueFlag(new ImprovedCatalogTypeArgument(Text.of(modifier), CatalogTypes.WORLD_GENERATOR_MODIFIER), "m", "-" + modifier)
                .valueFlag(GenericArguments.onlyOne(GenericArguments.longNum(Text.of(seed))), "s", "-" + seed)
                .valueFlag(GenericArguments.onlyOne(new ImprovedGameModeArgument(Text.of(gamemode))), "-gm", "-" + gamemode)
                .valueFlag(GenericArguments.onlyOne(new DifficultyArgument(Text.of(difficulty))), "-di", "-" + difficulty)
                .flag("n", "-nostructures")
                .flag("i")
                .valueFlag(GenericArguments.bool(Text.of("l")), "l", "-loadonstartup")
                .valueFlag(GenericArguments.bool(Text.of("k")), "k", "-keepspawnloaded")
                .valueFlag(GenericArguments.bool(Text.of("c")), "c", "-allowcommands")
                .valueFlag(GenericArguments.bool(Text.of("b")), "b", "-bonuschest")
                .buildWith(GenericArguments.onlyOne(GenericArguments.string(Text.of(name))))
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        String nameInput = args.<String>getOne(name).get();
        Optional<DimensionType> dimensionInput = args.getOne(dimension);
        Optional<GeneratorType> generatorInput = args.getOne(generator);
        Optional<GameMode> gamemodeInput = args.getOne(gamemode);
        Optional<Difficulty> difficultyInput = args.getOne(difficulty);
        Collection<WorldGeneratorModifier> modifiers = args.getAll(modifier);
        Optional<Long> seedInput = args.getOne(seed);
        boolean genStructures = !args.hasAny("n");
        boolean loadOnStartup = !args.hasAny("l") || args.<Boolean>getOne("l").orElse(true);
        boolean keepSpawnLoaded = !args.hasAny("k") || args.<Boolean>getOne("k").orElse(true);
        boolean allowCommands = !args.hasAny("c") || args.<Boolean>getOne("c").orElse(true);
        boolean bonusChest = !args.hasAny("b") || args.<Boolean>getOne("b").orElse(true);

        if (Sponge.getServer().getAllWorldProperties().stream().anyMatch(x -> x.getWorldName().equalsIgnoreCase(nameInput))) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.world.create.exists", nameInput));
        }

        // Does the world exist?
        Path worldPath = Sponge.getGame().getGameDirectory().resolve("world");
        if (!args.hasAny("i") && Files.exists(worldPath.resolve(nameInput))) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.world.import.noexist", nameInput));
        }

        WorldArchetype.Builder worldSettingsBuilder = WorldArchetype.builder()
            .enabled(true);

        if (args.hasAny(preset)) {
            WorldArchetype preset1 = args.<WorldArchetype>getOne(preset).get();
            worldSettingsBuilder.from(preset1);
            dimensionInput.ifPresent(worldSettingsBuilder::dimension);
            generatorInput.ifPresent(worldSettingsBuilder::generator);
            gamemodeInput.ifPresent(worldSettingsBuilder::gameMode);
            difficultyInput.ifPresent(worldSettingsBuilder::difficulty);
            if (!modifiers.isEmpty()) {
                modifiers.addAll(preset1.getGeneratorModifiers());
                worldSettingsBuilder.generatorModifiers(modifiers.toArray(new WorldGeneratorModifier[modifiers.size()]));
            }
        } else {
            worldSettingsBuilder
                .dimension(dimensionInput.orElse(DimensionTypes.OVERWORLD))
                .generator(generatorInput.orElse(GeneratorTypes.DEFAULT))
                .gameMode(gamemodeInput.orElse(GameModes.SURVIVAL))
                .difficulty(difficultyInput.orElse(Difficulties.NORMAL));

                if (!modifiers.isEmpty()) {
                    worldSettingsBuilder.generatorModifiers(modifiers.toArray(new WorldGeneratorModifier[modifiers.size()]));
                }
        }

        worldSettingsBuilder.loadsOnStartup(loadOnStartup)
        .keepsSpawnLoaded(keepSpawnLoaded)
        .usesMapFeatures(genStructures)
        .commandsAllowed(allowCommands)
        .generateBonusChest(bonusChest);

        WorldArchetype wa = worldSettingsBuilder.build(nameInput.toLowerCase(), nameInput);
        seedInput.ifPresent(worldSettingsBuilder::seed);

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.create.begin", nameInput));
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.create.newparams",
                wa.getDimensionType().getName(),
                wa.getGeneratorType().getName(),
                modifierString(modifiers),
                wa.getGameMode().getName(),
                wa.getDifficulty().getName()));
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.create.newparams2",
                String.valueOf(loadOnStartup),
                String.valueOf(keepSpawnLoaded),
                String.valueOf(genStructures),
                String.valueOf(allowCommands),
                String.valueOf(bonusChest)));

        WorldProperties worldProperties = Sponge.getGame().getServer().createWorldProperties(nameInput, wa);

        worldConfigAdapter.getNodeOrDefault().getWorldBorderDefault().ifPresent(worldProperties::setWorldBorderDiameter);
        worldProperties.setDifficulty(wa.getDifficulty());

        Sponge.getServer().saveWorldProperties(worldProperties);
        Optional<World> world = Sponge.getGame().getServer().loadWorld(worldProperties);

        if (world.isPresent()) {
            world.get().getProperties().setDifficulty(wa.getDifficulty());
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.create.success", nameInput));
            return CommandResult.success();
        } else {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.world.create.worldfailedtoload", nameInput));
        }
    }

    static String modifierString(Collection<WorldGeneratorModifier> cw) {
        if (cw.isEmpty()) {
            return Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("standard.none");
        }

        StringBuilder sb = new StringBuilder();
        cw.forEach(x -> {
            if (sb.length() > 0) {
                sb.append(", ");
            }

            sb.append(x.getName());
        });

        return sb.toString();
    }

    @NonnullByDefault
    public static class ExtendedDimensionArgument extends CommandElement {

        private static HashMap<String, DimensionType> replacement = new HashMap<String, DimensionType>() {{
            put("dim0", DimensionTypes.OVERWORLD);
            put("dim-1", DimensionTypes.NETHER);
            put("dim1", DimensionTypes.THE_END);
        }};

        private ExtendedDimensionArgument(@Nullable Text key) {
            super(key);
        }

        @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            final String arg = args.next();
            if (replacement.containsKey(arg.toLowerCase())) {
                return replacement.get(arg.toLowerCase());
            }

            String arg2 = arg;
            if (!arg2.contains(":")) {
                arg2 = "minecraft:" + arg2;
            }

            return Sponge.getRegistry().getType(DimensionType.class, arg2)
                .orElseThrow(() -> args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.dimensiontype.notfound", arg)));
        }

        @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            List<String> ids = Sponge.getRegistry().getAllOf(DimensionType.class).stream().map(CatalogType::getId).collect(Collectors.toList());
            try {
                String a = args.peek();
                return ids.stream().filter(x -> x.startsWith(a)).collect(Collectors.toList());
            } catch (Exception e) {
                return ids;
            }
        }
    }
}
