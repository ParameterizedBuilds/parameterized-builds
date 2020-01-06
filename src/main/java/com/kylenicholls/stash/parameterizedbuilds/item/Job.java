package com.kylenicholls.stash.parameterizedbuilds.item;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Job {
    private static final Logger logger = LoggerFactory.getLogger(Job.class);
    private final int jobId;
    private final String jobName;
    private final String jenkinsServer;
    private final boolean isTag;
    private final List<Trigger> triggers;
    private final String token;
    private final List<Entry<String, Object>> buildParameters;
    private final String branchRegex;
    private final String pathRegex;
    private final String permissions;
    private final String prDestRegex;
    private final boolean isPipeline;
    private final String ignoreCommitMsg;
    private final String ignoreComitters;

    private Job(JobBuilder builder) {
        this.jobId = builder.jobId;
        this.jobName = builder.jobName;
        this.jenkinsServer = builder.jenkinsServer;
        this.isTag = builder.isTag;
        this.triggers = builder.triggers;
        this.token = builder.token;
        this.buildParameters = builder.buildParameters;
        this.branchRegex = builder.branchRegex;
        this.pathRegex = builder.pathRegex;
        this.permissions = builder.permissions;
        this.prDestRegex = builder.prDestRegex;
        this.isPipeline = builder.isPipeline;
        this.ignoreComitters = builder.ignoreComitters;
        this.ignoreCommitMsg = builder.ignoreCommitMsg;
    }

    public int getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getJenkinsServer() {
        return jenkinsServer;
    }

    public boolean getIsTag() {
        return isTag;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }

    public String getToken() {
        return token;
    }

    public List<Entry<String, Object>> getBuildParameters() {
        return buildParameters;
    }

    public String getBranchRegex() {
        return branchRegex;
    }

    public String getPathRegex() {
        return pathRegex;
    }

    public String getPermissions() {
        return permissions;
    }

    public String getPrDestRegex() {
        return prDestRegex;
    }

    public boolean getIsPipeline() { return isPipeline; }

    public String getIgnoreCommitMsg() { return ignoreCommitMsg; }

    public String getIgnoreComitters() { return ignoreComitters; }

    public Map<String, Object> asMap(BitbucketVariables bitbucketVariables) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", jobId);
        map.put("jobName", jobName);
        List<Map<String, Object>> parameterMap = new ArrayList<>();
        for (Entry<String, Object> parameter : buildParameters) {
            Object value = parameter.getValue();
            for (String variable : bitbucketVariables.getVariables().keySet()) {
                if (parameter.getValue() instanceof String && value.toString().contains(variable)
                        && bitbucketVariables.fetch(variable) != null) {
                    value = value.toString().replace(variable, bitbucketVariables.fetch(variable));
                }
            }
            Map<String, Object> mapped = new HashMap<>();
            mapped.put(parameter.getKey(), value);
            parameterMap.add(mapped);
        }
        map.put("buildParameters", parameterMap);
        return map;
    }

    public static class JobBuilder {
        private final int jobId;
        private String jobName;
        private String jenkinsServer;
        private boolean isTag;
        private List<Trigger> triggers;
        private String token;
        private List<Entry<String, Object>> buildParameters;
        private String branchRegex;
        private String pathRegex;
        private String permissions;
        private String prDestRegex;
        private boolean isPipeline;
        private String ignoreCommitMsg;
        private String ignoreComitters;

        public JobBuilder(int jobId) {
            this.jobId = jobId;
        }

        public JobBuilder jobName(String jobName) {
            this.jobName = jobName;
            return this;
        }

        public JobBuilder jenkinsServer(String jenkinsServer){
            this.jenkinsServer = jenkinsServer;
            return this;
        }

        public JobBuilder isTag(boolean isTag) {
            this.isTag = isTag;
            return this;
        }

        public JobBuilder triggers(String[] triggersAry) {
            List<Trigger> triggers = new ArrayList<>();
            for (String trig : triggersAry) {
                try {
                    triggers.add(Trigger.valueOf(trig.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    logger.error("IllegalArgumentException in Job.triggers: " + e.getMessage(), e);
                    triggers.add(Trigger.NULL);
                }
            }
            return triggers(triggers);
        }

        public JobBuilder triggers(List<Trigger> triggers) {
            this.triggers = triggers;
            return this;
        }

        public JobBuilder token(String token) {
            this.token = token;
            return this;
        }

        public JobBuilder buildParameters(String parameterString) {
            List<Entry<String, Object>> parameterList = new ArrayList<>();
            if (!parameterString.isEmpty()) {
                String[] lines = parameterString.split("\\r?\\n");
                for (String line : lines) {
                    String[] pair = line.split("=", 2);
                    String key = pair[0];
                    if (pair.length > 1) {
                        if (pair[1].split(";").length > 1) {
                            parameterList
                                    .add(new SimpleEntry<String, Object>(key, pair[1].split(";")));
                        } else {
                            if (pair[1].matches("true|false")) {
                                parameterList.add(new SimpleEntry<String, Object>(key,
                                        Boolean.parseBoolean(pair[1])));
                            } else {
                                parameterList.add(new SimpleEntry<String, Object>(key, pair[1]));
                            }
                        }
                    } else {
                        parameterList.add(new SimpleEntry<String, Object>(key, ""));
                    }
                }
            }
            return buildParameters(parameterList);
        }

        public JobBuilder buildParameters(List<Entry<String, Object>> buildParameters) {
            this.buildParameters = buildParameters;
            return this;
        }

        public JobBuilder buildParameters(Map<String, Object> buildParameters) {
            return buildParameters(buildParameters.entrySet().stream()
                    .collect(Collectors.toList()));
        }

        public JobBuilder branchRegex(String branchRegex) {
            this.branchRegex = branchRegex;
            return this;
        }

        public JobBuilder pathRegex(String pathRegex) {
            this.pathRegex = pathRegex;
            return this;
        }

        public JobBuilder permissions(String permissions) {
            this.permissions = permissions;
            return this;
        }

        public JobBuilder prDestRegex(String prDestRegex) {
            this.prDestRegex = prDestRegex;
            return this;
        }

        public JobBuilder isPipeline(boolean isPipeline){
            this.isPipeline = isPipeline;
            return this;
        }

        public JobBuilder ignoreComitters(String ignoreComitters){
            this.ignoreComitters = ignoreComitters;
            return this;
        }

        public JobBuilder ignoreCommitMsg(String ignoreCommitMsg){
            this.ignoreCommitMsg = ignoreCommitMsg;
            return this;
        }

        public Job build() {
            return new Job(this);
        }
    }

    public JobBuilder copy(){
        return new JobBuilder(jobId).jobName(jobName).jenkinsServer(jenkinsServer).isTag(isTag)
                .triggers(triggers).token(token).buildParameters(buildParameters)
                .branchRegex(branchRegex).pathRegex(pathRegex).permissions(permissions)
                .prDestRegex(prDestRegex).isPipeline(isPipeline)
                .ignoreCommitMsg(ignoreCommitMsg).ignoreComitters(ignoreComitters);
    }

    public String buildUrl(Server jenkinsServer, BitbucketVariables bitbucketVariables,
            boolean useUserToken) {
        if (jenkinsServer == null) {
            return null;
        }
        Trigger trigger =  Trigger.fromToString(bitbucketVariables.fetch("$TRIGGER"));
        URIBuilder builder = setUrlPath(jenkinsServer, useUserToken, 
                                        !this.buildParameters.isEmpty(), 
                                        trigger, bitbucketVariables);

        String buildUrl = builder.toString();

        for (String variable : bitbucketVariables.getVariables().keySet()) {

            //URIBuilder automatically encodes query params so we need to use the encoded version 
            //for substitutions
            String encodedVar;
            try {
                encodedVar = URLEncoder.encode(variable, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                encodedVar = variable;
            }

            // only try to replace a variable if it is in the params. This allows optimal use of 
            //java 8 lazy initialization
            if (buildUrl.contains(encodedVar) && bitbucketVariables.fetch(variable) != null) {
                try {
                    buildUrl = buildUrl.replace(encodedVar, 
                                    URLEncoder.encode(bitbucketVariables.fetch(variable), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    buildUrl = buildUrl.replace(encodedVar, bitbucketVariables.fetch(variable));
                }
            }
            // also try to replace unencoded variables just in case
            if (buildUrl.contains(variable) && bitbucketVariables.fetch(variable) != null) {
                try {
                    buildUrl = buildUrl.replace(variable, 
                                    URLEncoder.encode(bitbucketVariables.fetch(variable), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    buildUrl = buildUrl.replace(variable, bitbucketVariables.fetch(variable));
                }
            }
        }

        return buildUrl;
    }

    private List<String> createPipelineJobPath(String delim, Trigger trigger, 
                                               BitbucketVariables variables){
        List<String> jobSegments = new ArrayList<>(Arrays.asList(jobName.split("/")));
        if (isPipeline && !trigger.isRefChange()){
            if (delim != null){
                jobSegments.add(delim);
            }
            jobSegments.add(variables.fetch("$BRANCH"));
        }
        return jobSegments;
    }

    private URIBuilder setUrlPath(Server jenkinsServer, boolean useUserToken,
            boolean hasParameters, Trigger trigger, BitbucketVariables bitbucketVariables) {
        URIBuilder builder;
        try {
            builder = new URIBuilder(jenkinsServer.getBaseUrl());
        } catch (URISyntaxException e) {
            return new URIBuilder();
        }
        hasParameters = !isPipeline || !trigger.isRefChange() ? hasParameters : false;

        //start building the url path. Make sure to use the current context.
        StringBuilder path = new StringBuilder(builder.getPath());

        if (useUserToken || !jenkinsServer.getAltUrl()) {
            path.append("/job");
            List<String> additionalPaths =
                    createPipelineJobPath("job", trigger, bitbucketVariables);
            additionalPaths.forEach(x -> path.append("/" + x));
        } else {
            path.append("/buildByToken");

            builder.setParameter("job", String.join("/", 
                    createPipelineJobPath(null, trigger, bitbucketVariables)));
        }

        if (!useUserToken && this.token != null && !this.token.isEmpty()) {
            builder.setParameter("token", this.token);
        }

        if (hasParameters) {
            path.append("/buildWithParameters");
            appendBuildParams(builder);
        } else {
            path.append("/build");
        }

        builder.setPath(path.toString());
        return builder;
    }

    private void appendBuildParams(URIBuilder builder){
        for (Entry<String, Object> param : this.buildParameters) {
            String key = param.getKey();
            String value;
            if (param.getValue() instanceof String[]) {
                value = ((String[]) param.getValue())[0];
            } else {
                value = param.getValue().toString();
            }
            builder.setParameter(key, value);
        }
    }

    public enum Trigger {
        ADD, PUSH, PROPENED, MANUAL, DELETE, PRMERGED, PRAUTOMERGED, PRDECLINED, PRDELETED,
        PRAPPROVED, PRREOPENED, PRDESTRESCOPED, PRSOURCERESCOPED, NULL;

        @Override
        public String toString() {
            switch(this) {
                case ADD: return "REF CREATED";
                case PUSH: return "PUSH EVENT";
                case PROPENED: return "PR OPENED";
                case DELETE: return "REF DELETED";
                case PRMERGED: return "PR MERGED";
                case PRAUTOMERGED: return "AUTO MERGED";
                case PRDECLINED: return "PR DECLINED";
                case PRDELETED: return "PR DELETED";
                case PRAPPROVED: return "PR APPROVED";
                case PRSOURCERESCOPED: return "PR SOURCE RESCOPED";
                case PRDESTRESCOPED: return "PR DEST RESCOPED";
                case PRREOPENED: return "PR REOPENED";
                default: return super.toString();
            }
        }

        public Boolean isRefChange(){
            return Stream.of(ADD, PUSH, DELETE).collect(Collectors.toList()).contains(this);
        }

        public static Trigger fromToString(String toString){
            switch(toString) {
                case "REF CREATED": return ADD;
                case "PUSH EVENT": return PUSH;
                case "PR OPENED": return PROPENED;
                case "REF DELETED": return DELETE;
                case "PR MERGED": return PRMERGED;
                case "AUTO MERGED": return PRAUTOMERGED;
                case "PR DECLINED": return PRDECLINED;
                case "PR DELETED": return PRDELETED;
                case "PR APPROVED": return PRAPPROVED;
                case "PR DEST RESCOPED": return PRDESTRESCOPED;
                case "PR SOURCE RESCOPED": return PRSOURCERESCOPED;
                case "PR REOPENED": return PRREOPENED;
                default: return NULL;
            }
        }
    }
}
