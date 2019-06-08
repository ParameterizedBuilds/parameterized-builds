import axios from 'axios';

const getRestUrl = (context) => {
    return `${context}/rest/parameterized-builds/latest`
}

const getJenkinsServers = (context, projectKey) => {
    const baseUrl = getRestUrl(context);
    let fullUrl;
    if (projectKey === undefined){
        fullUrl = `${baseUrl}/global/servers`
    } else {
        fullUrl = `${baseUrl}/projects/${projectKey}/servers`
    }

    return axios.get(fullUrl, {
        timeout: 1000 * 60
    });
};
export { getJenkinsServers };