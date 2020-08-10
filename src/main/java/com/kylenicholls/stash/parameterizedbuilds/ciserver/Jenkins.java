package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.collect.Lists;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import com.kylenicholls.stash.parameterizedbuilds.item.UserToken;

public class Jenkins {
    private static final String PLUGIN_KEY = "com.kylenicholls.stash.parameterized-builds";
    private static final String JENKINS_SETTINGS = ".jenkinsSettings";
    private static final String JENKINS_SETTINGS_PROJECT = JENKINS_SETTINGS + ".";
    private static final String JENKINS_USER = ".jenkinsUser.";
    private final PluginSettings pluginSettings;

    public Jenkins(PluginSettingsFactory factory) {
        this.pluginSettings = factory.createSettingsForKey(PLUGIN_KEY);
    }

    /**
     * Saves or removes a Jenkins server for a specfic project. If the server is
     * null then the global server will be removed for the project.
     *
     * @param server
     *            the project server
     * @param projectKey
     *            the project key
     */
    public void saveJenkinsServer(@Nullable Server server, String projectKey) {
        if (projectKey == null || projectKey == ""){
            saveJenkinsServerToDB(JENKINS_SETTINGS, server);
        } else {
            saveJenkinsServerToDB(JENKINS_SETTINGS_PROJECT + projectKey, server);
        }
    }

    /**
     * Saves or removes a Jenkins server depending on the url parameter. If the
     * url parameter is null then the Jenkins server will be removed.
     *
     * @param key
     *            key to save the Jenkins server under, should be global or a
     *            project key
     * @param server
     *            the server
     */
    private void saveJenkinsServerToDB(String key, @Nullable Server server) {
        if (server != null) {
            pluginSettings.put(key, server.asMap());
        } else {
            pluginSettings.remove(key);
        }
    }

    /**
     * Saves or removes a Jenkins user token. If the token parameter is empty
     * then the Jenkins user token will be removed.
     *
     * @param userSlug
     *            the user slug to save the token under
     * @param projectKey
     *            if the projectKey is empty then the user token is saved as a
     *            global token, else the user token is saved as a project token
     * @param token
     *            the Jenkins api token, if the token is empty then the setting
     *            will be removed
     */
    public void saveUserToken(String userSlug, String projectKey, String token) {
        String appendProject = projectKey.isEmpty() ? "" : "." + projectKey;
        if (!token.isEmpty()) {
            pluginSettings.put(JENKINS_USER + userSlug + appendProject, token);
        } else {
            pluginSettings.remove(JENKINS_USER + userSlug + appendProject);
        }
    }

    /**
     * Returns the global Jenkins server.
     *
     * @return the global Jenkins server or null if there is not one
     */
    @Nullable
    private Server getJenkinsServer() {
        Object settingObj = pluginSettings.get(JENKINS_SETTINGS);
        if (settingObj != null) {
            if (settingObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> serverMap = (Map<String, Object>) settingObj;
                return new Server(serverMap);
            }
            // legacy settings
            String[] serverProps = settingObj.toString().split(";");
            boolean altUrl = serverProps.length > 3 && "true".equals(serverProps[3]) ? true : false;
            boolean csrfEnabled = true;
            return new Server(serverProps[0], null, serverProps[1], serverProps[2], altUrl, 
                    csrfEnabled);
        }
        return null;
    }

    /**
     * Returns a Jenkins server for a project.
     *
     * @return a Jenkins server for a project or null if there is not one for
     *         the specified project
     */
    @Nullable
    public Server getJenkinsServer(String projectKey, String alias) {
        if (projectKey == null || projectKey.equals("global-settings")) {
            return getJenkinsServer();
        }

        Object settingObj = pluginSettings.get(JENKINS_SETTINGS_PROJECT + projectKey);
        if (settingObj != null && settingObj instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> serverMap = (Map<String, Object>) settingObj;
            return new Server(serverMap);
        }
        return null;
    }

    /**
     * Returns jenkins server for the project with the proper user token
     * 
     * @param projectKey - Bitbucket project key
     * @param alias - alias for the target server definition
     * @param user - user to get token for
     * @return
     */
    public Server getJenkinsServer(String projectKey, String alias, ApplicationUser user){
        Server server = getJenkinsServer(projectKey, alias);
        String userToken = getUserToken(user, projectKey);
        if (userToken != null && !userToken.isEmpty()){
            server.setUser(user.getSlug());
            server.setToken(userToken);
            server.setAltUrl(false);
        }
        return server;
    }

    /**
     * Returns all Jenkins servers for a project.
     *
     * @return a list of Jenkins servers for a project or empty list if there are none for
     *         the specified project
     */
    @Nullable
    public List<Server> getJenkinsServers(String projectKey) {
        List<Server> servers = new ArrayList<>();
        if (projectKey == null || projectKey.equals("global-settings")) {
            Server server = getJenkinsServer();
            return server == null ? Lists.newArrayList() : Lists.newArrayList(server);
        }

        Object settingObj = pluginSettings.get(JENKINS_SETTINGS_PROJECT + projectKey);
        if (settingObj != null && settingObj instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> serverMap = (Map<String, Object>) settingObj;
            Server server = new Server(serverMap);
            servers.add(server);
        }
        return servers;
    }

    /**
     * Returns the user token for the specified user and project.
     *
     * @return the user token or null if the user is null or the token does not
     *         exist
     * @param user
     *            the user to get the token for, can be null if the user is
     *            anonymous
     * @param projectKey
     *            the project to get the token for, if null then this returns
     *            the global user token
     */
    @Nullable
    private String getUserToken(@Nullable ApplicationUser user, @Nullable String projectKey) {
        if (user != null) {
            if (projectKey == null) {
                Object settingObj = pluginSettings.get(JENKINS_USER + user.getSlug());
                if (settingObj != null) {
                    return settingObj.toString();
                }
            } else {
                Object settingObj = pluginSettings
                        .get(JENKINS_USER + user.getSlug() + "." + projectKey);
                if (settingObj != null) {
                    return settingObj.toString();
                }
            }
        }
        return null;
    }

    /**
     * Returns a list of all user tokens for all projects (including global)
     * that have a Jenkins server set.
     *
     * @return a list of all user tokens for all projects (including global)
     *         that have a Jenkins server set.
     * @param user
     *            the user to get the token for
     * @param projectKeys
     *            all the project keys in the bitbucket server instance
     * @param projectService
     *            used to get the project name
     */
    protected List<UserToken> getAllUserTokens(ApplicationUser user, List<String> projectKeys,
            ProjectService projectService) {
        List<UserToken> userTokens = new ArrayList<>();
        List<String> allKeys = Lists.newArrayList(projectKeys);
        allKeys.add(null);

        for (String projectKey : allKeys) {
            List<Server> servers = getJenkinsServers(projectKey);
            String userTokenString = getUserToken(user, projectKey);
            for (Server server : servers) {
                String scopeName = projectKey != null ?
                        projectService.getByKey(projectKey).getName() : "Global";
                String scopeKey = projectKey != null ? projectKey : "";
                UserToken projectUserToken = new UserToken(server.getBaseUrl(),
                        server.getAlias(), scopeKey, scopeName, user.getSlug(),
                        userTokenString);
                userTokens.add(projectUserToken);
            }
        }
        return userTokens;
    }
}
