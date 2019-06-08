import { getJenkinsServers } from '../common/rest'
import React from 'react';
import { render } from 'react-dom';
import { Provider, connect } from 'react-redux';
import { createStore } from 'redux';
import { Server } from './server'
import { serverDefinitions } from "./state-reducers";
import { Form } from "../common/aui";


let ServerList = ({ servers }) => {
    return (
        <Form>
            {
            servers.map((server, i) =>
                <Server id={i} />
            )
            }
        </Form>
    )
};
const serverListStateInjector = (state, ownProps) => {
    return {
        servers: state.servers
    }
}
ServerList = connect(serverListStateInjector)(ServerList);

document.addEventListener("DOMContentLoaded", () => {
    console.log("Initializing form...")
    const baseStore = createStore(serverDefinitions);

    const projectKey = document.getElementById('project-key').innerHTML;
    const bitbucketContext = document.getElementById('bitbucket-context').innerHTML;
    getJenkinsServers(bitbucketContext, projectKey).then(response => {
        const servers = response.data;
        console.log("Got servers: ", servers)
        if (servers.length == 0){
            baseStore.dispatch({
                type: "ADD_SERVER"
            });
        } else {
            baseStore.dispatch({
                type: "INITIALIZE",
                baseConfig: servers
            });
        }
    }).catch(response => {
        console.error(response);
    });

    const app = (
        <Provider store={baseStore}>
            <ServerList />
        </Provider>
    )

    render(app, document.getElementById('jenkins-settings'))
});
