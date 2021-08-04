package org.jivesoftware.openfire.plugin.rest.controller;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusteringController {
    private static Logger LOG = LoggerFactory.getLogger(ClusteringController.class);
    public static final ClusteringController INSTANCE = new ClusteringController();

    public static ClusteringController getInstance() {
        return INSTANCE;
    }

    private static final PluginManager pluginManager = XMPPServer.getInstance().getPluginManager();
    private static final RESTServicePlugin plugin = (RESTServicePlugin) pluginManager.getPlugin("restapi");

    public static void log(String logMessage) {
        if (plugin.isServiceLoggingEnabled())
            LOG.info(logMessage);
    }

    public String getClusterStatus() {
        if (ClusterManager.isClusteringEnabled()) {
            if (ClusterManager.isClusteringStarted()) {
                if (ClusterManager.isSeniorClusterMember()) {
                    if (ClusterManager.getNodesInfo().size() == 1) {
                        return "SENIOR AND ONLY MEMBER"; //admin.clustering.only
                    } else {
                        return "Senior member"; //admin.clustering.senior
                    }
                } else {
                    return "Junior member"; //admin.clustering.junior
                }
            } else {
                return "Starting up"; //admin.clustering.starting
            }
        } else {
            return "Disabled"; //admin.clustering.disabled
        }
    }

}
