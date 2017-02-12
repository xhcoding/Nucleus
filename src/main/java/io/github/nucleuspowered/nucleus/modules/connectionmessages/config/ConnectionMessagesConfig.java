/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.connectionmessages.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ConnectionMessagesConfig {

    @Setting(value = "show-first-login-message", comment = "config.connectionmessages.enablefirst")
    private boolean showFirstTimeMessage = true;

    @Setting(value = "first-login-message", comment = "config.connectionmessages.firsttime")
    private String firstTimeMessage = "&dWelcome &f{{name}} &dto the server!";

    @Setting(value = "modify-login-message", comment = "config.connectionmessages.enablelogin")
    private boolean modifyLoginMessage = false;

    @Setting(value = "modify-logout-message", comment = "config.connectionmessages.enablelogout")
    private boolean modifyLogoutMessage = false;

    @Setting(value = "login-message", comment = "config.connectionmessages.loginmessage")
    private String loginMessage = "&8[&a+&8] &f{{name}}";

    @Setting(value = "logout-message", comment = "config.connectionmessages.logoutmessage")
    private String logoutMessage = "&8[&c-&8] &f{{name}}";

    @Setting(value = "disable-with-permission", comment = "config.connectionmessages.disablepermission")
    private boolean disableWithPermission = false;

    @Setting(value = "display-name-change-if-changed", comment = "config.connectionmessages.displayprior")
    private boolean displayPriorName = true;

    @Setting(value = "changed-name-message", comment = "config.connectionmessages.displaypriormessage")
    private String priorNameMessage = "&f{{name}} &ewas previously known by a different name - they were known as &f{{previousname}}";

    public boolean isShowFirstTimeMessage() {
        return showFirstTimeMessage;
    }

    public String getFirstTimeMessage() {
        return firstTimeMessage;
    }

    public boolean isModifyLoginMessage() {
        return modifyLoginMessage;
    }

    public boolean isModifyLogoutMessage() {
        return modifyLogoutMessage;
    }

    public String getLoginMessage() {
        return loginMessage;
    }

    public String getLogoutMessage() {
        return logoutMessage;
    }

    public boolean isDisableWithPermission() {
        return disableWithPermission;
    }

    public boolean isDisplayPriorName() {
        return displayPriorName;
    }

    public String getPriorNameMessage() {
        return priorNameMessage;
    }
}
