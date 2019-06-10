const createServer = initialState => {
    return {
        id: null,
        url: "",
        alias: "",
        default_user: "",
        default_token: null,
        root_token_enabled: false,
        csrf_enabled: false,
        action_message: "",
        action_state: null,
        ...initialState
    }
};


const server = (state=createServer(), action) => {
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
        case 'ADD_SERVER':
            return [
                ...state,
                server(undefined, {...action, id: state.length})
            ];
        case 'DELETE_SERVER':
            return [
                ...state.slice(0, action.id),
                ...state.slice(action.id + 1).map(t =>
                    server(t, {
                        ...action,
                        type: 'DECREMENT_ID'
                    }))
            ];
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

export const serverDefinitions = (state = {servers: [], project: null}, action) => {
    switch (action.type){
        case 'INITIALIZE':
            return {
                ...state,
                project: action.project,
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