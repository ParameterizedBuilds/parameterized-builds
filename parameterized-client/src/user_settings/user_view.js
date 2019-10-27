import React from 'react';
import { render } from 'react-dom';
import { Provider, connect } from 'react-redux';
import { createStore } from 'redux';
import { Server } from './server'
import { serverDefinitions } from "./state-reducers";
import { Form } from "../common/aui";

let ServerList = ({ servers }) => {

    if (servers.length === 0){
        return <div>
            No Jenkins servers were found. Contact your Bitbucket Server administrator to add a Jenkins server.
        </div>
    }

    const serversByProject = servers.reduce(function(rv, x) {
        (rv[x["project_name"]] = rv[x["project_name"]] || []).push(x);
        return rv;
    }, {});

    return (
        <div>
            {Object.keys(serversByProject).map(projectName =>
                <div>
                    <h3>{projectName} Servers</h3>
                    {serversByProject[projectName].map(server => 
                        <Form>
                            <Server id={server.id} />
                        </Form>
                    )}
                    <hr />
                </div>
            )}
        </div>
    )
};
const serverListStateInjector = (state, ownProps) => {
    return {
        servers: state.servers
    }
}
ServerList = connect(serverListStateInjector)(ServerList);

document.addEventListener("DOMContentLoaded", () => {
    const baseStore = createStore(serverDefinitions);

    const servers = JSON.parse(document.getElementById('project-tokens').innerHTML);
    const bitbucketContext = document.getElementById('bitbucket-context').innerHTML;

    baseStore.dispatch({
        type: "INITIALIZE",
        baseConfig: servers,
        project: null,
        context: bitbucketContext
    });

    const app = (
        <Provider store={baseStore}>
            <ServerList />
        </Provider>
    )

    render(app, document.getElementById('jenkins-settings'))
});