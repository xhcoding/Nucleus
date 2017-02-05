/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.loaders;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.dataservices.Service;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import io.github.nucleuspowered.nucleus.internal.TimingsDummy;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class DataManager<I, P, S extends Service<?>> {

    private final Predicate<I> fileExists;
    private final Function<I, DataProvider<P>> dataProviderFactory;
    final Map<I, S> dataStore = new ConcurrentHashMap<>();
    final NucleusPlugin plugin;

    private Timing generalLoad = TimingsDummy.DUMMY;
    private Timing actualLoad = TimingsDummy.DUMMY;
    private Timing save = TimingsDummy.DUMMY;

    DataManager(NucleusPlugin plugin, Function<I, DataProvider<P>> dataProviderFactory, Predicate<I> fileExistsPredicate) {
        this.dataProviderFactory = dataProviderFactory;
        this.plugin = plugin;
        this.fileExists = fileExistsPredicate;

        try {
            generalLoad = Timings.of(plugin, this.getClass().getSimpleName() + " - General");
            actualLoad = Timings.of(plugin, this.getClass().getSimpleName() + " - Loading");
            save = Timings.of(plugin, this.getClass().getSimpleName() + " - Saving");
        } catch (Exception e) {
            // ignored
        }
    }

    public final boolean has(I data) {
        return this.dataStore.containsKey(data) || this.fileExists.test(data);
    }

    public final Optional<S> get(I data) {
        try {
            generalLoad.startTimingIfSync();
            if (this.dataStore.containsKey(data)) {
                return Optional.of(this.dataStore.get(data));
            }

            actualLoad.startTimingIfSync();
            DataProvider<P> d = this.dataProviderFactory.apply(data);
            if (d == null) {
                return Optional.empty();
            }

            try {
                S us = getNew(data, d).get(); // new UserService(plugin, d, user.get());
                dataStore.put(data, us);
                return Optional.of(us);
            } catch (Exception e) {
                e.printStackTrace();
                return Optional.empty();
            }
        } finally {
            generalLoad.stopTimingIfSync();
            actualLoad.stopTimingIfSync();
        }
    }

    public abstract Optional<S> getNew(I data, DataProvider<P> dataProvider) throws Exception;

    public final void saveAll() {
        try {
            save.startTimingIfSync();
            dataStore.forEach((i, s) -> {
                if (!s.save()) {
                    plugin.getLogger().error("Could not save data for " + i.toString());
                }
            });
        } finally {
            save.stopTimingIfSync();
        }
    }
}
