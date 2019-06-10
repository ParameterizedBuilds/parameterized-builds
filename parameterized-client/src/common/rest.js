import axios from 'axios';

const getRestUrl = (context) => {
    return `${context}/rest/parameterized-builds/latest`
}

export const getJenkinsServers = (context, projectKey) => {
    const baseUrl = getRestUrl(context);
    let fullUrl;
    if (projectKey === undefined || projectKey === ""){
        fullUrl = `${baseUrl}/global/servers`
    } else {
        fullUrl = `${baseUrl}/projects/${projectKey}/servers`
    }

    return axios.get(fullUrl, {
        timeout: 1000 * 60
    });
};

export const saveJenkinsServer = (context, projectKey, serverData) => {
    const baseUrl = getRestUrl(context);
    const alias = serverData.alias;
    let fullUrl = projectKey === "" ?
        `${baseUrl}/global/servers/${alias}` :
        `${baseUrl}/projects/${projectKey}/servers/${alias}`;

    const data = {
        baseUrl: serverData.url,
        alias: serverData.alias,
        user: serverData.default_user,
        token: serverData.default_token,
        altUrl: serverData.root_token_enabled,
        csrfEnabled: serverData.csrf_enabled
    }

    return axios.put(fullUrl, data, {
        timeout: 1000 * 60,
        headers: {
            'content-type': 'application/json'
        }
    })
}