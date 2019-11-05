package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nullable;
import javax.net.ssl.SSLException;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.item.BitbucketVariables;
import com.kylenicholls.stash.parameterizedbuilds.item.JenkinsResponse;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import com.kylenicholls.stash.parameterizedbuilds.item.JenkinsResponse.JenkinsMessage;

public class JenkinsConnection {

    private static final Logger logger = LoggerFactory.getLogger(Jenkins.class);
    private final Jenkins jenkins;

    public JenkinsConnection(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    /**
     * Returns a message object from the triggered job.
     *
     * @return a message object from the triggered job.
     * @param buildUrl
     *            the build url to trigger
     * @param joinedToken
     *            the authentication token to use in the request
     * @param csrfHeader
     *            the token to use in case cross site protection is enabled
     * @param promptUser
     *            prompt the user to link their jenkins account
     */
    public JenkinsResponse sanitizeTrigger(@Nullable String buildUrl, @Nullable String joinedToken,
            @Nullable String csrfHeader, boolean promptUser) {
        if (buildUrl == null) {
            return new JenkinsResponse.JenkinsMessage().error(true)
                    .messageText("Jenkins settings are not setup").build();
        }

        return httpPost(buildUrl.replace(" ", "%20"), joinedToken, csrfHeader, promptUser);
    }

    public JenkinsResponse triggerJob(String projectKey, ApplicationUser user, Job job, 
                                      BitbucketVariables bitbucketVariables) {
        Server jenkinsServer;
        String joinedUserToken;
        if (job.getJenkinsServer() != null){
            jenkinsServer = jenkins.getJenkinsServer(job.getJenkinsServer());
            joinedUserToken = jenkins.getJoinedUserToken(user, job.getJenkinsServer());
        } else {
            // legacy behaviour
            Server projectServer = jenkins.getJenkinsServer(projectKey);
            if (projectServer != null){
                jenkinsServer = projectServer;
                joinedUserToken = jenkins.getJoinedUserToken(user, projectKey);
            } else {
                jenkinsServer = jenkins.getJenkinsServer(null);
                joinedUserToken = jenkins.getJoinedUserToken(user, null);
            }
        }

        String buildUrl = job
                .buildUrl(jenkinsServer, bitbucketVariables, joinedUserToken != null);

        // use default user and token if the user that triggered the
        // build does not have a token set
        boolean prompt = false;
        if (joinedUserToken == null) {
            prompt = true;
            if (!jenkinsServer.getUser().isEmpty()) {
                joinedUserToken = jenkinsServer.getJoinedToken();
            }
        }

        String csrfHeader = null;
        if (jenkinsServer.getCsrfEnabled()) {
            // get a CSRF token because cross site protection is enabled
            try {
                csrfHeader = getCrumb(jenkinsServer.getBaseUrl(), joinedUserToken);
            } catch(Exception e){
                logger.warn("error getting CSRF token");
            }
        }

        return sanitizeTrigger(buildUrl, joinedUserToken, csrfHeader, prompt);
    }

    private HttpURLConnection setupConnection(String baseUrl, String userToken) throws Exception{
        URL url = new URL(baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (userToken != null && !userToken.isEmpty()) {
            byte[] authEncBytes = Base64.encodeBase64(userToken.getBytes());
            String authStringEnc = new String(authEncBytes);
            connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
        }
        connection.setReadTimeout(45000);
        connection.setInstanceFollowRedirects(true);
        connection.setDoOutput(true);
        HttpURLConnection.setFollowRedirects(true);
        return connection;
    }

    public String testConnection(Server server){
        try {
            String url = server.getBaseUrl().replaceAll("/$", "") + "/api/json";
            HttpURLConnection connection = setupConnection(url, server.getJoinedToken());
            connection.setRequestMethod("GET");
            connection.setFixedLengthStreamingMode(0);

            String csrfHeader = null;
            if (server.getCsrfEnabled()) {
                // get a CSRF token because cross site protection is enabled
                try {
                    csrfHeader = getCrumb(server.getBaseUrl(), server.getJoinedToken());
                } catch(Exception e){
                    logger.warn("error getting CSRF token");
                }
            }

            if (csrfHeader != null){
                String[] header = csrfHeader.split(":");
                connection.setRequestProperty(header[0], header[1]);
            }

            connection.connect();

            int status = connection.getResponseCode();
            if (status == 403){
                return "Error authenticating to server";
            } else if (status == 200) {
                return "Connection successful";
            } else {
                return "Failed to establish connection";
            }
        } catch (Exception e) {
            return "An error occurred trying to establish a connection";
        }
    }

    private String getCrumb(String baseUrl, String token) throws Exception{
        final String crumbPath = "/crumbIssuer/api/xml?xpath=" + 
                                 "concat(//crumbRequestField,\":\",//crumb)";
        final String crumbUrl = baseUrl  + crumbPath;
        // workaround temporary javax.net.ssl.SSLException: Received close_notify during handshake
        // retry the connection three times should be OK for temporary connection issues
        final int maxRetries = 3;
        final int sleepRetryMS = 3000;
        for( int retry = 1; retry <= maxRetries; ++retry ) {
            try {
                final HttpURLConnection connection = setupConnection(crumbUrl, token);
                connection.connect();
                final int status = connection.getResponseCode();
                if (status == 200) {
                    return new BufferedReader(new InputStreamReader(
                            (connection.getInputStream()))).readLine();
                } else {
                    logger.warn("Could not connect to " + baseUrl +
                                ", got HTTP status " + status + ".");
                    return null;
                }
            } catch(final SSLException e) {
                if( retry < maxRetries ) {
                    // log issue and try again
                    logger.warn("Could not connect to " + baseUrl +
                                ", will retry in " + sleepRetryMS + "ms", e);
                } else {
                    throw e;
                }
            }
            // wait before next retry
            Thread.sleep(sleepRetryMS);
        }
        return null;
    }

    private JenkinsResponse httpPost(String buildUrl, String token, String csrfHeader, 
                                     boolean prompt) {
        JenkinsMessage jenkinsMessage = new JenkinsResponse.JenkinsMessage().prompt(prompt);
        try {
            HttpURLConnection connection = setupConnection(buildUrl, token);
            connection.setRequestMethod("POST");
            connection.setFixedLengthStreamingMode(0);

            if (csrfHeader != null){
                String[] header = csrfHeader.split(":");
                connection.setRequestProperty(header[0], header[1]);
            }
            connection.connect();

            int status = connection.getResponseCode();
            if (status == 201) {
                return jenkinsMessage.messageText("Build triggered").build();
            }

            String message;
            String responseMessage =  connection.getResponseMessage();
            if (status == 403) {
                message = "You do not have permissions to build this job";
            } else if (status == 302 && responseMessage.equals("Found")) {
                //multibranch pipelines cause redirects on the build but work just fine
                //so if we get a redirect but it is successful, just report success
                return jenkinsMessage.messageText("Build triggered").build();
            } else if (status == 404) {
                message = "Job was not found";
                return jenkinsMessage.error(true).messageText(message).build();
            } else if (status == 500) {
                message = "Error triggering job, invalid build parameters";
            } else {
                message = responseMessage;
            }
            logger.error("Exception for parametized build: " + message);
            return jenkinsMessage.error(true).messageText(message).build();
        } catch (MalformedURLException e) {
            return jenkinsMessage.error(true).messageText("Malformed URL: " + e.getMessage())
                    .build();
        } catch (IOException e) {
            logger.error("IOException in Jenkins.httpPost: " + e.getMessage(), e);
            return jenkinsMessage.error(true).messageText("IO exception occurred: " + 
                                                          e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("Exception in Jenkins.httpPost: " + e.getMessage(), e);
            return jenkinsMessage.error(true).messageText("Something went wrong: " + e.getMessage())
                    .build();
        }
    }
}