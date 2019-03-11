import { Provider, connect } from 'react-redux';
import { createStore } from 'redux';
import React from 'react';
import { jobDefinitions, addJob } from "./state-reducers";
import { Job } from "./job";
import axios from 'axios';

window.parameterizedbuilds = window.parameterizedbuilds || {};

const getJenkinsServers = () => {
    const urlRegex = /(.+?)(\/projects\/[\w_ -]+?\/repos\/[\w_ -]+?\/)settings.*/
    let urlParts = window.location.href.match(urlRegex);
    let restUrl = urlParts[1] + "/rest/parameterized-builds/latest" + urlParts[2] + "getJenkinsServers";
    return axios.get(restUrl, {
        timeout: 1000 * 60
    });
}

let AddJob = ({ dispatch }) => {
    return (
        <a>
            <span className="aui-icon aui-icon-small aui-iconfont-list-add" onClick={() => {
                dispatch(addJob());
            }} />
        </a>
    )
};
AddJob = connect()(AddJob);


let JobList = ({jobs, errors}) => {
    return (
        <div>
            {
                jobs.map(job =>
                    <Job id={job.id} errors={errors}/>
                )
            }
        </div>
    )
};

const jobListStateInjector = (state, ownProps) => {
    return {
        jobs: state.jobs,
        errors: ownProps.errors
    }
};
JobList = connect(jobListStateInjector)(JobList);

const App = ({errors}) => {
    return (
        <div className={"parameterized-builds"}>
            {typeof errors["jenkins-admin-error"] !== 'undefined' &&
            <div className="errors-container">
                <div className={"aui-message aui-message-error"}>
                    <p>{errors['jenkins-admin-error']}</p>
                </div>
            </div>
            }
            <JobList errors={errors}/>
            <AddJob />
        </div>
    )
};

parameterizedbuilds.view = function({config, errors}) {
    const baseStore = createStore(jobDefinitions);

    baseStore.dispatch({
        type: "INITIALIZE",
        baseConfig: config,
    });

    getJenkinsServers().then(response => {
        baseStore.dispatch({
            type: "UPDATE_JENKINS_SERVERS",
            servers: response.data,
        });
    });

    return (
        <Provider store={baseStore}>
            <App errors={errors || {}}/>
        </Provider>
    )

};