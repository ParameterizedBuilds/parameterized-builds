package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.atlassian.stash.user.StashUser;
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
	private final StashAuthenticationContext authenticationContext;
	private final NavBuilder navBuilder;
	private final Jenkins jenkins;
    
    public CIServlet(SoyTemplateRenderer soyTemplateRenderer,
    		StashAuthenticationContext authenticationContext, 
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
    		String baseUrl = server != null ? server.getBaseUrl() : "";
    		if (pathInfo.contains("/account/users/")) {
    			StashUser stashUser = authenticationContext.getCurrentUser();
        		String jenkinsToken = jenkins.getUserSettings(stashUser.getSlug());
        		if (jenkinsToken == null) {
            		render(resp, "jenkins.user.settings", ImmutableMap.<String, Object>of("user", stashUser, "token", "", "baseUrl", baseUrl));
        		} else {
            		render(resp, "jenkins.user.settings", ImmutableMap.<String, Object>of("user", stashUser, "token", jenkinsToken, "baseUrl", baseUrl));
        		}
    		} else {
        		if (server == null) {
            		render(resp, "jenkins.admin.settings", ImmutableMap.<String, Object>of("server", ""));
        		} else {
            		render(resp, "jenkins.admin.settings", ImmutableMap.<String, Object>of("server", server));
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
    	if (pathInfo.contains("/account/users/")) {
			String userSlug = authenticationContext.getCurrentUser().getSlug();
    		jenkins.setUserSettings(userSlug, req.getParameter("jenkinsToken"));
    	} else {
    		jenkins.setSettings(req.getParameter("jenkinsUrl"), req.getParameter("jenkinsUser"), req.getParameter("jenkinsToken"));
    	}
        doGet(req, res);
    }
}