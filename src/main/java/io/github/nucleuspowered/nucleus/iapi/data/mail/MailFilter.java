/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.iapi.data.mail;

import io.github.nucleuspowered.nucleus.api.Stable;

@Stable
public interface MailFilter<T> {

    T getSuppliedData();
}
