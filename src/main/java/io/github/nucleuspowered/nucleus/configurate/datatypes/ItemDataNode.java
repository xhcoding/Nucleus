/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.datatypes;

import com.google.common.collect.ImmutableList;
import io.github.nucleuspowered.nucleus.configurate.annotations.ProcessSetting;
import io.github.nucleuspowered.nucleus.configurate.settingprocessor.LowercaseSetStringSettingProcessor;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ConfigSerializable
public class ItemDataNode {

    public ItemDataNode() {}

    public ItemDataNode(Set<String> aliases, int buy, int sell) {
        this.aliases = aliases;
        this.price = new PriceNode(buy, sell);
    }

    public ItemDataNode(ItemDataNode copy) {
        this(copy.aliases, copy.price.getBuy(), copy.price.getSell());
    }

    @Setting
    @ProcessSetting(LowercaseSetStringSettingProcessor.class)
    private Set<String> aliases = new HashSet<>();

    @Setting
    private PriceNode price = new PriceNode();

    public List<String> getAliases() {
        return ImmutableList.copyOf(aliases);
    }

    public void addAlias(String alias) {
        String lowerCase = alias.toLowerCase();
        if (aliases.contains(lowerCase)) {
            aliases.add(lowerCase);
        }
    }

    public void removeAlias(String alias) {
        alias = alias.toLowerCase();
        aliases.remove(alias);
    }

    public int getServerBuyPrice() {
        return price.getBuy();
    }

    public void setServerBuyPrice(int serverBuyPrice) {
        price.setBuy(serverBuyPrice);
    }

    public int getServerSellPrice() {
        return price.getSell();
    }

    public void setServerSellPrice(int serverSellPrice) {
        price.setSell(serverSellPrice);
    }

}
