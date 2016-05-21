/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.interfaces;

/**
 * An interface that allows the class to be notified that there is a Nucleus reload.
 */
public interface Reloadable {

    /**
     * Reload the data associated with this {@link Reloadable}
     *
     * @throws Exception Thrown if there is an issue.
     */
    void onReload() throws Exception;
}
