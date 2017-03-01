/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.troubleshoot;

import static io.github.nucleuspowered.nucleus.modules.troubleshoot.TroubleshootingModule.ID;

import io.github.nucleuspowered.nucleus.internal.qsml.module.StandardModule;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = ID, name = "Troubleshooting")
public class TroubleshootingModule extends StandardModule {

    public static final String ID = "troubleshoot";
}
