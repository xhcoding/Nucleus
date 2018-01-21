/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.loaders;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.dataservices.Service;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import io.github.nucleuspowered.nucleus.internal.TimingsDummy;
import org.spongepowered.api.Sponge;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

public abstract class DataManager<I, P, S extends Service> {

    private final Predicate<I> fileExists;
    private final BiFunction<I, Boolean, DataProvider<P>> dataProviderFactory;
    private final LoadingCache<I, S> cache;

    private Timing GENERAL_LOAD_TIMINGS = TimingsDummy.DUMMY;
    private Timing ACTUAL_LOAD_TIMINGS = TimingsDummy.DUMMY;
    private Timing SAVE_TIMINGS = TimingsDummy.DUMMY;

    DataManager(BiFunction<I, Boolean, DataProvider<P>> dataProviderFactory, Predicate<I> fileExistsPredicate) {
        this.dataProviderFactory = dataProviderFactory;
        this.fileExists = fileExistsPredicate;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .removalListener(new Removal())
                .build(new Loader());

        try {
            Nucleus plugin = Nucleus.getNucleus();
            GENERAL_LOAD_TIMINGS = Timings.of(plugin, this.getClass().getSimpleName() + " - General");
            ACTUAL_LOAD_TIMINGS = Timings.of(plugin, this.getClass().getSimpleName() + " - Loading");
            SAVE_TIMINGS = Timings.of(plugin, this.getClass().getSimpleName() + " - Saving");
        } catch (Exception e) {
            // ignored
        }
    }

    private class Removal implements RemovalListener<I, S> {

        @Override
        public void onRemoval(@Nullable I key, @Nullable S value, @Nonnull RemovalCause cause) {
            if (value != null) {
                value.save();
            }

            if (key != null && shouldNotExpire(key)) {
                DataManager.this.cache.refresh(key);
            }
        }
    }

    private class Loader implements CacheLoader<I, S> {

        @CheckForNull @Override public S load(@Nonnull I key) throws Exception {
            try {
                GENERAL_LOAD_TIMINGS.startTimingIfSync();
                ACTUAL_LOAD_TIMINGS.startTimingIfSync();
                DataProvider<P> d = dataProviderFactory.apply(key, true);
                if (d == null) {
                    return null;
                }

                return getNew(key, d).orElse(null);
            } finally {
                GENERAL_LOAD_TIMINGS.stopTimingIfSync();
                ACTUAL_LOAD_TIMINGS.stopTimingIfSync();
            }
        }
    }

    public final boolean has(I data) {
        return this.cache.getIfPresent(data) != null || this.fileExists.test(data);
    }

    public final Optional<S> get(I data) {
        return this.get(data, true);
    }

    @GuardedBy("lockingObject")
    public final Optional<S> get(I data, boolean create) {
        if (!create || has(data)) {
            return Optional.ofNullable(this.cache.get(data));
        }

        return Optional.empty();
    }

    public final Map<I, S> getAll(Collection<I> keys) {
        return this.cache.getAllPresent(keys);
    }

    protected abstract boolean shouldNotExpire(I key);

    public abstract Optional<S> getNew(I data, DataProvider<P> dataProvider) throws Exception;

    final void invalidate(I key, boolean save) {
        S value = this.cache.getIfPresent(key);
        if (value != null) {
            this.cache.invalidate(key);
            if (save) {
                value.save();
            }
        }
    }

    public final void invalidateOld(boolean save) {
        Collection<Map.Entry<I, S>> keys = this.cache.asMap().entrySet().stream().filter(x -> !this.shouldNotExpire(x.getKey())).collect(Collectors.toList());
        if (save) {
            try {
                SAVE_TIMINGS.startTimingIfSync();
                for (Map.Entry<I, S> key : keys) {
                    key.getValue().save();
                }
            } finally {
                SAVE_TIMINGS.stopTimingIfSync();
            }
        }

        this.cache.invalidateAll(keys);
    }

    public final void saveAll() {
        try {
            SAVE_TIMINGS.startTimingIfSync();
            for (S s : this.cache.asMap().values()) {
                s.save();
            }
        } finally {
            SAVE_TIMINGS.stopTimingIfSync();
        }
    }
}
