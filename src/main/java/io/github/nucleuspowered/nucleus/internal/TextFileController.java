/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.spongepowered.api.asset.Asset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Handles loading and reading text files.
 */
public final class TextFileController {

    /**
     * The internal {@link Asset} that represents the default file.
     */
    private final Asset asset;

    /**
     * Holds the file location.
     */
    private final Path fileLocation;

    /**
     * Holds the file information.
     */
    private final List<String> fileContents = Lists.newArrayList();

    public TextFileController(Asset asset, Path fileLocation) throws IOException {
        this.asset = asset;
        this.fileLocation = fileLocation;
        load();
    }

    /**
     * Loads the file and refreshes the contents of the file in memory.
     *
     * @throws IOException Thown if there is an issue getting the file.
     */
    public void load() throws IOException {
        if (!Files.exists(fileLocation)) {
            // Create the file
            asset.copyToFile(fileLocation);
        }

        // Load the file into the list.
        fileContents.addAll(Files.readAllLines(fileLocation));
    }

    /**
     * Gets the contents of the file.
     *
     * @return An {@link ImmutableList} that contains the file contents.
     */
    public ImmutableList<String> getFileContents() {
        return ImmutableList.copyOf(fileContents);
    }
}
