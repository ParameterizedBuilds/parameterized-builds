import { Provider, connect } from 'react-redux';
import { createStore } from 'redux';
import React from 'react';
import { jobs, addJob } from "./state-reducers";
import { Job } from "./job";

window.parameterizedbuilds = window.parameterizedbuilds || {};

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
    return <div>
        {
            jobs.map(job =>
                <Job id={job.id} errors={errors}/>
            )
        }
    </div>
};

const jobListStateInjector = (state, ownProps) => {
    return {
        jobs: state,
        errors: ownProps.errors
    }
};
JobList = connect(jobListStateInjector)(JobList);

class App extends React.Component{
    render(){
        return <div className={"parameterized-builds"}>
            {typeof this.props.errors["jenkins-admin-error"] !== 'undefined' &&
            <div className="errors-container">
                <div className={"aui-message aui-message-error"}>
                    <p>{this.props.errors['jenkins-admin-error']}</p>
                </div>
            </div>
            }
            <JobList errors={this.props.errors}/>
            <AddJob />
        </div>
    }
}

parameterizedbuilds.view = function({config, errors}) {
    const baseStore = createStore(jobs);

    baseStore.dispatch({
        type: "INITIALIZE",
        baseConfig: config,
    });

    return <Provider store={baseStore}>
            <App errors={errors || {}}/>
        </Provider>

};