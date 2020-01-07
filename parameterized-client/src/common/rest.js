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
    if (serverData.alias.includes("/")) {
        throw "Server nickname cannot include \"/\""
    }
    const alias = serverData.old_alias !== "" ? serverData.old_alias : serverData.alias;
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

export const deleteJenkinsServer = (context, projectKey, serverName) => {
    const baseUrl = getRestUrl(context);
    let fullUrl = projectKey === "" ?
        `${baseUrl}/global/servers/${serverName}` :
        `${baseUrl}/projects/${projectKey}/servers/${serverName}`;

    return axios.delete(fullUrl, {
        timeout: 1000 * 60,
    })
}


export const testJenkinsServer = (context, projectKey, serverData) => {
    const baseUrl = getRestUrl(context);
    let fullUrl = projectKey === "" ?
        `${baseUrl}/global/servers/validate` :
        `${baseUrl}/projects/${projectKey}/servers/validate`;

        const data = {
            baseUrl: serverData.url,
            alias: serverData.alias,
            user: serverData.default_user,
            token: serverData.default_token,
            altUrl: serverData.root_token_enabled,
            csrfEnabled: serverData.csrf_enabled
        }

    return axios.post(fullUrl, data, {
        timeout: 1000 * 60,
        headers: {
            'content-type': 'application/json'
        }
    })
}


export const updateUserToken = (context, projectKey, serverData) => {
    const baseUrl = getRestUrl(context);
    let fullUrl = projectKey === "" ?
        `${baseUrl}/global/servers/${serverData.alias}/userToken` :
        `${baseUrl}/projects/${projectKey}/servers/${serverData.alias}/userToken`;

        const data = {
            token: serverData.default_token
        }

    return axios.put(fullUrl, data, {
        timeout: 1000 * 60,
        headers: {
            'content-type': 'application/json'
        }
    })
}

export const removeUserToken = (context, projectKey, alias) => {
    const baseUrl = getRestUrl(context);
    let fullUrl = projectKey === "" ?
        `${baseUrl}/global/servers/${alias}/userToken` :
        `${baseUrl}/projects/${projectKey}/servers/${alias}/userToken`;

    return axios.delete(fullUrl, {
        timeout: 1000 * 60
    })
}