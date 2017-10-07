/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;

public abstract class AbstractService<T> implements Service {

    protected T data;
    private final DataProvider<T> dataProvider;

    AbstractService(DataProvider<T> dataProvider) throws Exception {
        this(dataProvider, true);
    }

    protected AbstractService(DataProvider<T> dataProvider, boolean loadNow) throws Exception {
        this.dataProvider = Preconditions.checkNotNull(dataProvider);
        if (loadNow) {
            data = dataProvider.load();
        }
    }

    public final void changeFile() {
        if (this.dataProvider instanceof DataProvider.FileChanging) {
            ((DataProvider.FileChanging) this.dataProvider).onChange();
        }
    }

    public final boolean isLoaded() {
        return this.data != null;
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
        if (data != null) {
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

        return true;
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
