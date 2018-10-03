import { Provider, connect } from 'react-redux';
import { createStore } from 'redux';
import React from 'react';
import {jobState, Job} from "./job";

window.parameterizedbuilds = window.parameterizedbuilds || {};


const createInitialState = (config) => {
    let i = 0;
    let initialState = [];
    while (typeof config["jobName-" + i] !== 'undefined'){
        let newJob = {
            id: i,
            active: false,
            jobName: config["jobName-" + i],
            isTag: config["isTag-" + i] == 'true',
            isPipeline: config["isPipeline-" + i],
            triggers: config["triggers-" + i],
            token: config["token-" + i],
            buildParameters: config["buildParameters-" + i],
            branchRegex: config["branchRegex-" + i],
            pathRegex: config["pathRegex-" + i],
            requirePermission: config["requirePermission-" + i],
            prDestinationRegex: config["prDestinationRegex-" + i],
        };
        initialState.push(newJob);
        i++;
    }
    nextJobId = i;
    return initialState;
};

const jobs = (state = [], action) => {
    console.log("current state is:", state);
    console.log("action is", action);
    switch (action.type) {
        case 'INITIALIZE':
            return createInitialState(action.baseConfig);
        case 'ADD_JOB':
            return [
                ...state,
                jobState(undefined, action)
            ];
        case 'TOGGLE_JOB':
            return state.map(t => jobState(t, action));
        case 'DELETE_JOB':
            nextJobId--;
            return [
                ...state.slice(0, action.id),
                ...state.slice(action.id + 1).map(t =>
                    jobState(t, {
                        ...action,
                        type: 'DECREMENT_ID'
                    }))
            ];
        case 'UPDATE_TEXT_FIELD':
            return state.map(t => jobState(t, action));
        case 'UPDATE_TRIGGER_FIELD':
            return state.map(t => jobState(t, action));
        default:
            return state
    }
};

let nextJobId = 0;
const addJob = () => {
    return {
        type: 'ADD_JOB',
        id: nextJobId++,
    }
};

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


let JobList = ({jobs}) => {
    return <div>
        {
            jobs.map(job =>
                <Job id={job.id}/>
            )
        }
    </div>
};

const jobListStateInjector = state => {
    return {
        jobs: state
    }
};
JobList = connect(jobListStateInjector)(JobList);

class App extends React.Component{
    componentWillUnmount(){
        nextJobId = 0;
    }

    render(){
        return <div className={"parameterized-builds"}>
            {typeof this.props.errors !== 'undefined' && typeof this.props.errors["jenkins-admin-error"] !== 'undefined' &&
            <div className="field-group">
                <div className="error">{this.props.errors['jenkins-admin-error']}</div>
            </div>
            }
            <JobList />
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
            <App errors={errors}/>
        </Provider>

};