/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.docgen;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class CommandDoc {

    @Setting
    private String commandName;

    @Setting
    private String aliases;

    @Setting
    private String defaultLevel;

    @Setting
    private String usageString;

    @Setting
    private String oneLineDescription;

    @Setting
    private String extendedDescription;

    @Setting
    private String module;

    @Setting
    private String permissionbase;

    @Setting
    private boolean warmup;

    @Setting
    private boolean cooldown;

    @Setting
    private boolean cost;

    @Setting
    private boolean requiresMixin;

    @Setting
    private List<PermissionDoc> permissions;

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getAliases() {
        return aliases;
    }

    public void setAliases(String aliases) {
        this.aliases = aliases;
    }

    public String getDefaultLevel() {
        return defaultLevel;
    }

    public void setDefaultLevel(String defaultLevel) {
        this.defaultLevel = defaultLevel;
    }

    public String getUsageString() {
        return usageString;
    }

    public void setUsageString(String usageString) {
        this.usageString = usageString;
    }

    public String getOneLineDescription() {
        return oneLineDescription;
    }

    public void setOneLineDescription(String oneLineDescription) {
        this.oneLineDescription = oneLineDescription;
    }

    public String getExtendedDescription() {
        return extendedDescription;
    }

    public void setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getPermissionbase() {
        return permissionbase;
    }

    public void setPermissionbase(String permissionbase) {
        this.permissionbase = permissionbase.replaceAll("\\.base", "");
    }

    public boolean isWarmup() {
        return warmup;
    }

    public void setWarmup(boolean warmup) {
        this.warmup = warmup;
    }

    public boolean isCooldown() {
        return cooldown;
    }

    public void setCooldown(boolean cooldown) {
        this.cooldown = cooldown;
    }

    public boolean isCost() {
        return cost;
    }

    public void setCost(boolean cost) {
        this.cost = cost;
    }

    public List<PermissionDoc> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionDoc> permissions) {
        this.permissions = permissions;
    }

    public boolean isRequiresMixin() {
        return requiresMixin;
    }

    public void setRequiresMixin(boolean requiresMixin) {
        this.requiresMixin = requiresMixin;
    }
}
