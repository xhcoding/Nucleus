/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplate;
import org.spongepowered.api.asset.Asset;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Handles loading and reading text files.
 */
public final class TextFileController {

    private static final List<Charset> characterSetsToTest = Lists.newArrayList(
        StandardCharsets.UTF_8,
        StandardCharsets.ISO_8859_1,
        StandardCharsets.US_ASCII,
        StandardCharsets.UTF_16
    );

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

    /**
     * Holds the {@link NucleusTextTemplate} information.
     */
    private final List<NucleusTextTemplate> textTemplates = Lists.newArrayList();

    private long fileTimeStamp = 0;

    public TextFileController(Path fileLocation) throws IOException {
        this(null, fileLocation);
    }

    public TextFileController(Asset asset, Path fileLocation) throws IOException {
        this.asset = asset;
        this.fileLocation = fileLocation;
        load();
    }

    /**
     * Loads the file and refreshes the contents of the file in memory.
     *
     * @throws IOException Thrown if there is an issue getting the file.
     */
    public void load() throws IOException {
        if (asset != null && !Files.exists(fileLocation)) {
            // Create the file
            asset.copyToFile(fileLocation);
        }

        List<String> fileContents = Lists.newArrayList();

        // Load the file into the list.
        MalformedInputException exception = null;
        for (Charset charset : characterSetsToTest) {
            try {
                fileContents.addAll(Files.readAllLines(fileLocation, charset));
                exception = null;
                break;
            } catch (MalformedInputException ex) {
                exception = ex;
            }
        }

        // Rethrow exception if it doesn't work.
        if (exception != null) {
            throw exception;
        }

        this.fileTimeStamp = Files.getLastModifiedTime(fileLocation).toMillis();
        this.fileContents.clear();
        this.fileContents.addAll(fileContents);
        this.textTemplates.clear();
    }

    /**
     * Gets the contents of the file.
     *
     * @return An {@link ImmutableList} that contains the file contents.
     */
    public ImmutableList<NucleusTextTemplate> getFileContentsAsText() {
        checkFileStamp();
        if (textTemplates.isEmpty()) {
            fileContents.forEach(x -> textTemplates.add(NucleusTextTemplate.createFromAmpersandString(x)));
        }

        return ImmutableList.copyOf(textTemplates);
    }

    private void checkFileStamp() {
        try {
            if (this.fileContents.isEmpty() || Files.getLastModifiedTime(fileLocation).toMillis() > this.fileTimeStamp) {
                load();
            }
        } catch (IOException e) {
            // ignored
        }
    }
}
