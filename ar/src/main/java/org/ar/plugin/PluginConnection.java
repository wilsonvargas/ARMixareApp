
package org.ar.plugin;

/**
 * Main interface for activity and service connections -> in package org.ar.plugin.connection
 *
 * @author A.Egal
 */
public abstract class PluginConnection {

    private PluginType pluginType;

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }

    public String getPluginName() {
        return pluginType.getActionName();
    }

    protected void storeFoundPlugin() {
        PluginLoader.getInstance().addFoundPluginToMap(pluginType.toString(), this);
    }

    protected void storeFoundPlugin(String pluginName) {
        PluginLoader.getInstance().addFoundPluginToMap(pluginName, this);
    }
}
