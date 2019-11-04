const createServer = (initialState = {}) => {
    return {
        id: null,
        project_name: "",
        url: "",
        alias: "",
        default_user: "",
        default_token: null,
        action_message: "",
        action_state: null,
        show_clear_modal: false,
        ...initialState
    }
};


const server = (state={}, action) => {
    switch(action.type) {
        case 'ADD_SERVER':
            return {
                ...createServer(action.serverConfig),
                id: action.id
            };
        case 'DECREMENT_ID':
            return {
                ...state,
                id: state.id - 1
            };
        case 'UPDATE_FIELD':
            let newState = {
                ...state,
            };
            newState[action.field] = action.value;
            return newState;
        default:
            return state;
    }
}


const servers = (state = [], action) => {
    switch (action.type) {
        case 'INITIALIZE':
            return action.baseConfig.map((serverConfig, index) =>
                server(state, {
                    ...action,
                    id: index,
                    serverConfig: serverConfig,
                    type: 'ADD_SERVER'
                }))
        case 'DELETE_SERVER':
            let newState = [
                ...state.slice(0, action.id),
                ...state.slice(action.id + 1).map(t =>
                    server(t, {
                        ...action,
                        type: 'DECREMENT_ID'
                    }))
            ];
            // always render an empty form
            if (newState.length == 0){
                return [
                    server(undefined, {type: 'ADD_SERVER', id: 0})
                ]
            } else {
                return newState
            }
        case 'UPDATE_FIELD':
            return [
                ...state.slice(0, action.id),
                server(state[action.id], action),
                ...state.slice(action.id + 1)
            ];
        default:
            return state
    }
};

export const serverDefinitions = (state = {servers: []}, action) => {
    switch (action.type){
        case 'INITIALIZE':
            return {
                ...state,
                context: action.context,
                servers: servers(state.servers, action)
            }
        default:
            return {
                ...state,
                servers: servers(state.servers, action)
            }
    }
};