/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;

public abstract class AbstractService<T> implements Service {

    protected T data;
    private final DataProvider<T> dataProvider;

    AbstractService(DataProvider<T> dataProvider) throws Exception {
        this(dataProvider, true);
    }

    protected AbstractService(DataProvider<T> dataProvider, boolean loadNow) throws Exception {
        this.dataProvider = dataProvider;
        if (loadNow) {
            data = dataProvider.load();
        }
    }

    @Override public boolean load() {
        try {
            loadInternal();
            return true;
        } catch (Exception e) {
            Nucleus.getNucleus().getLogger().error(e.getMessage());
            if (Nucleus.getNucleus().isDebugMode()) {
                e.printStackTrace();
            }

            return false;
        }
    }

    @Override public void loadInternal() throws Exception {
        data = dataProvider.load();
    }

    @Override public boolean save() {
        try {
            dataProvider.save(data);
            return true;
        } catch (Exception e) {
            Nucleus.getNucleus().getLogger().error(e.getMessage());
            if (Nucleus.getNucleus().isDebugMode()) {
                e.printStackTrace();
            }

            return false;
        }
    }

    @Override public boolean delete() {
        try {
            dataProvider.delete();
            return true;
        } catch (Exception e) {
            Nucleus.getNucleus().getLogger().error(e.getMessage());
            if (Nucleus.getNucleus().isDebugMode()) {
                e.printStackTrace();
            }

            return false;
        }
    }
}
