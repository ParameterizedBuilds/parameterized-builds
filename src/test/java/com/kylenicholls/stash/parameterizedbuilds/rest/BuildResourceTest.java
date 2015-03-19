package com.kylenicholls.stash.parameterizedbuilds.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.sun.jersey.api.client.ClientResponse.Status;

public class BuildResourceTest {
	public static final String COND_BASEURL_PREFIX = "cond-baseurl-0";
	public static final String COND_CI_PREFIX = "cond-ciserver-0";
	public static final String COND_JOB_PREFIX = "cond-jobname-0";
	public static final String COND_BRANCH_PREFIX = "cond-branch-0";
	public static final String COND_PATH_PREFIX = "cond-path-0";
	public static final String COND_PARAM_PREFIX = "cond-param-0";
	public static final String COND_TRIGGER_PREFIX = "cond-trigger-0";
	public static final String COND_USERNAME_PREFIX = "cond-username-0";
	public static final String COND_PASSWORD_PREFIX = "cond-password-0";
	private BuildResource resource;
	private I18nService i18nService;
	private SettingsService settingsService;
	private Jenkins jenkins;
	private Settings settings;
	private Repository repository;
	private String baseUrl;
	private UriInfo uriInfo;
	
	@Before
	public void setup() throws Exception {
		i18nService = mock(I18nService.class);
		settingsService = mock(SettingsService.class);
		settings = mock(Settings.class);
		jenkins = mock(Jenkins.class);
		//resource = new BuildResource(i18nService, settingsService, jenkins);
		repository = mock(Repository.class);
		baseUrl = "http://jenkins.localhost";
		uriInfo = mock(UriInfo.class);
	}
	
	@Test
	public void testGetJenkinsXml() throws ParserConfigurationException, SAXException, IOException {
		String jobUrl = "http://localhost:8080/job/test_job/config.xml";
		String token = "kyle:123";
		int status = -1;
		InputStream xml = null;
		try {
			URL url = new URL(jobUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			if (token != null && !token.equals("")) {
				byte[] authEncBytes = Base64.encodeBase64(token.getBytes());
				String authStringEnc = new String(authEncBytes);
				connection.setRequestProperty("Authorization", "Basic "
						+ authStringEnc);
			}
			connection.setRequestProperty("Accept", "application/xml");
			connection.setReadTimeout(30000);
			connection.setInstanceFollowRedirects(true);
			HttpURLConnection.setFollowRedirects(true);
			xml = connection.getInputStream();
			status = connection.getResponseCode();
		} catch (Exception e){
			
		}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(xml);
		NodeList paramDef = doc.getDocumentElement().getElementsByTagName("hudson.model.StringParameterDefinition");

		for (int i = 0; i < paramDef.getLength(); i++) {
			Element parameter = (Element) paramDef.item(i);
			String key = parameter.getElementsByTagName("name").item(0).getTextContent();
			String value = parameter.getElementsByTagName("defaultValue").item(0).getTextContent();
			System.out.println(key + "=" + value);
		}
		//Node nNode = paramDef.item(0).;
		System.out.println(paramDef.getLength());
	}

}
