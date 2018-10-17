const getInitialState = () => {
    return {
        id: null,
        active: true,
        jobName: "",
        isTag: false,
        isPipeline: false,
        triggers: "",
        token: "",
        buildParameters: "",
        branchRegex: "",
        pathRegex: "",
        requirePermission: "REPO_READ",
        prDestinationRegex: "",
    }
};

const jobState = (state=getInitialState(), action) => {
    switch (action.type) {
        case 'ADD_JOB':
            return {
                ...getInitialState(),
                id: action.id,
            };
        case 'TOGGLE_JOB':
            return {
                ...state,
                active: action.id === state.id ? !state.active : state.active
            };
        case 'DECREMENT_ID':
            return {
                ...state,
                id: state.id - 1
            };
        case 'UPDATE_TEXT_FIELD':
            if (action.id !== state.id){
                return state;
            }
            let newState = {
                ...state,
            };
            newState[action.field] = action.value;
            return newState;
        case 'UPDATE_TRIGGER_FIELD':
            if (action.id !== state.id){
                return state;
            }
            if (state.triggers.includes(action.value)){
                return {
                    ...state,
                    triggers: state.triggers.replace(action.value, "")
                }
            } else {
                return {
                    ...state,
                    triggers: state.triggers + action.value
                }
            }
        default:
            return state
    }
};

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
    return initialState;
};

export const jobs = (state = [], action) => {
    switch (action.type) {
        case 'INITIALIZE':
            return createInitialState(action.baseConfig);
        case 'ADD_JOB':
            return [
                ...state,
                jobState(undefined, {...action, id: state.length})
            ];
        case 'TOGGLE_JOB':
            return state.map(t => jobState(t, action));
        case 'DELETE_JOB':
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

export const addJob = () => {
    return {
        type: 'ADD_JOB',
    }
};