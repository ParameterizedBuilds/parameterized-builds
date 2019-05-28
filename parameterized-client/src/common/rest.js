import axios from 'axios';

const getJenkinsServers = (context, projectKey) => {
    const restUrl = `${context}/rest/parameterized-builds/latest/${projectKey}/servers`;
    return axios.get(restUrl, {
        timeout: 1000 * 60
    });
};
export { getJenkinsServers };