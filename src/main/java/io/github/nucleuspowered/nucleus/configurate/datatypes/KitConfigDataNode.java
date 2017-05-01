/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.datatypes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.configurate.wrappers.NucleusItemStackSnapshot;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.ItemTypes;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ConfigSerializable
public class KitConfigDataNode {
    @Setting
    private Map<String, KitDataNode> kits = Maps.newHashMap();

    @Deprecated
    @Setting
    private List<NucleusItemStackSnapshot> firstKit = Lists.newArrayList();

    private boolean kitFix = false;
    private final Object locking = new Object();

    public Map<String, KitDataNode> getKits() {
        synchronized (locking) {
            if (!kitFix) {
                kitFix = true;
                // We might have multiple kits with the same name, different case, due to an error in programming.
                Map<String, Long> ksi = kits.entrySet().stream()
                    .map(x -> x.getKey().toLowerCase()).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                ksi.entrySet().removeIf(x -> x.getValue() < 2);

                // If we have duplicates, add a number at the end and lower case them all.
                if (!ksi.isEmpty()) {
                    for (String k : ksi.keySet()) {
                        if (kits.containsKey(k)) {
                            int count = 1;
                            Map<String, KitDataNode> msk = kits.entrySet().stream()
                                .filter(x -> x.getKey().equalsIgnoreCase(k) && !x.getKey().equals(k))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                            for (Map.Entry<String, KitDataNode> stringKitDataNodeEntry : msk.entrySet()) {
                                kits.remove(stringKitDataNodeEntry.getKey());
                                String kitName;
                                do {
                                    kitName = stringKitDataNodeEntry.getKey().toLowerCase() + String.valueOf(count++);
                                } while (kits.containsKey(kitName));

                                kits.put(kitName, stringKitDataNodeEntry.getValue());
                            }
                        }
                    }
                }
            }
        }

        return kits;
    }

    @SuppressWarnings("deprecation")
    public void migrate() {
        if (this.firstKit != null) {
            // Is there a "firstjoinkit"?
            if (!this.getKits().containsKey("firstjoinkit")) {
                KitDataNode kit = new KitDataNode();
                kit.setStacks(this.firstKit.stream().map(NucleusItemStackSnapshot::getSnapshot)
                        .filter(x -> x.getType() != ItemTypes.NONE)
                        .collect(Collectors.toList()))
                    .setFirstJoinKit(true);
                kits.put("firstjoinkit", kit);

                this.firstKit = null;
            }
        }
    }
}
