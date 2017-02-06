/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.modular;

import io.github.nucleuspowered.nucleus.dataservices.AbstractService;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModularDataService<S extends ModularDataService<S>> extends AbstractService<ConfigurationNode> {

    private Map<Class<?>, DataModule<S>> cached = new HashMap<>();

    protected ModularDataService(DataProvider<ConfigurationNode> dataProvider) throws Exception {
        this(dataProvider, true);
    }

    protected ModularDataService(DataProvider<ConfigurationNode> dataProvider, boolean loadNow) throws Exception {
        super(dataProvider, loadNow);
    }

    public <T extends DataModule<S>, R> R quickGet(Class<T> module, Function<T, R> getter) {
        return getter.apply(get(module));
    }

    public <T extends DataModule<S>> void quickSet(Class<T> module, Consumer<T> setter) {
        T m = get(module);
        setter.accept(m);
        set(m);
    }

    @SuppressWarnings("unchecked")
    public <T extends DataModule<S>> T get(Class<T> module) {
        if (cached.containsKey(module)) {
            return (T)cached.get(module);
        }

        try {
            T dm = module.newInstance();
            dm.loadFrom(data);
            return dm;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public <T extends DataModule<S>> void set(T dataModule) {
        cached.put(data.getClass(), dataModule);
        dataModule.saveTo(data);
    }

    @Override public void loadInternal() throws Exception {
        super.loadInternal();
        cached.clear(); // Only clear if no exception was caught.
    }

    @Override public boolean save() {
        cached.values().forEach(x -> x.saveTo(data));
        return super.save();
    }
}
