package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.google.common.collect.ImmutableMap;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings("serial")
public class CIServlet extends HttpServlet{

	public static final String URL_PREFIX = "jenkinsUrl-";
	public static final String USER_PREFIX = "jenkinsUser-";
	public static final String TOKEN_PREFIX = "jenkinsToken-";
    private final SoyTemplateRenderer soyTemplateRenderer;
	private final AuthenticationContext authenticationContext;
	private final NavBuilder navBuilder;
	private final Jenkins jenkins;
    
    public CIServlet(SoyTemplateRenderer soyTemplateRenderer,
    		AuthenticationContext authenticationContext, 
    		NavBuilder navBuilder,
    		Jenkins jenkins) {
        this.soyTemplateRenderer = soyTemplateRenderer;
		this.authenticationContext = authenticationContext;
		this.navBuilder = navBuilder;
		this.jenkins = jenkins;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
    	String pathInfo = req.getPathInfo();
    	if (authenticationContext.isAuthenticated()) {
    		Server server = jenkins.getSettings();
    		String baseUrl = server != null ? server.getBaseUrl() : null;
    		if (pathInfo.contains("/account/")) {
    			ApplicationUser appUser = authenticationContext.getCurrentUser();
        		String jenkinsToken = jenkins.getUserSettings(appUser);
        		if (baseUrl == null) {
        			render(resp, "jenkins.user.settings", 
        					ImmutableMap.<String, Object>of("user", appUser, "token", "", "baseUrl", "", "errors", "A Bitbucket administrator must configure the base settings for Jenkins first. These settings can be found on the admin page of Bitbucket."));
        			return;
        		}
        		if (jenkinsToken == null) {
            		render(resp, "jenkins.user.settings", ImmutableMap.<String, Object>of("user", appUser, "token", "", "baseUrl", baseUrl, "errors", ""));
        		} else {
            		render(resp, "jenkins.user.settings", ImmutableMap.<String, Object>of("user", appUser, "token", jenkinsToken, "baseUrl", baseUrl, "errors", ""));
        		}
    		} else {
        		if (server == null) {
            		render(resp, "jenkins.admin.settings", ImmutableMap.<String, Object>of("server", "", "errors", ""));
        		} else {
            		render(resp, "jenkins.admin.settings", ImmutableMap.<String, Object>of("server", server, "errors", ""));
        		}
    		}
    		
    	} else {
			resp.sendRedirect(navBuilder.login().next(req.getServletPath() + pathInfo).buildAbsolute());
		}
    }
    
    private void render(HttpServletResponse resp, String templateName, Map<String, Object> data) throws IOException, ServletException {
        resp.setContentType("text/html;charset=UTF-8");
        try {
            soyTemplateRenderer.render(resp.getWriter(),
                    "com.kylenicholls.stash.parameterized-builds:jenkins-admin-soy",
                    templateName,
                    data);
        } catch (SoyException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new ServletException(e);
        }
    }

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	String pathInfo = req.getPathInfo();
    	if (pathInfo.contains("/account/")) {
			String token = req.getParameter("jenkinsToken");
    		jenkins.setUserSettings(authenticationContext.getCurrentUser(), token);
    	} else {
    		String jenkinsUrl = req.getParameter("jenkinsUrl");
    		String jenkinsUser = req.getParameter("jenkinsUser");
    		String jenkinsToken = req.getParameter("jenkinsToken");
    		boolean jenkinsAltUrl = req.getParameter("jenkinsAltUrl") != null && req.getParameter("jenkinsAltUrl").equals("on") ? true : false;
    		String clear = req.getParameter("clear-settings");
    		if (clear != null && clear.equals("on")){
    			jenkins.setSettings("", "", "", false);
    		} else if (jenkinsUrl.isEmpty()) {
            	render(res, "jenkins.admin.settings", ImmutableMap.<String, Object>of("server", new Server(jenkinsUrl, jenkinsUser, jenkinsToken, jenkinsAltUrl), "errors", "Base URL required"));
            	return;
    		} else {
    			jenkins.setSettings(jenkinsUrl, jenkinsUser, jenkinsToken, jenkinsAltUrl);
    		}
    	}
        doGet(req, res);
    }
}