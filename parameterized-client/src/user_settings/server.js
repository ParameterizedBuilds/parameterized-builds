import React from 'react';
import { connect } from 'react-redux';
import { updateUserToken, removeUserToken, testJenkinsServer } from '../common/rest'
import { PasswordInput, ButtonGroup, Button, Message } from '../common/aui'

const ActionDialog = ({
    message,
    state
}) => {
    switch(state){
        case 'LOADING':
            return (
                <Message messageClass="aui-message-info">
                    {message}
                </Message>
            );
        case 'ERROR':
            return (
                <Message messageClass="aui-message-error">
                    {message}
                </Message>
            );
        case 'SUCCESS':
            return (
                <Message messageClass="aui-message-confirmation">
                    {message}
                </Message>
            )
    }
}

const ServerContainer = ({
    serverData,
    context,
    updateServer
}) => {

    const updateToken = e => {
        e.preventDefault();
        updateServer(serverData.id, "action_message", "Saving settings...");
        updateServer(serverData.id, "action_state", "LOADING");
        updateUserToken(context, serverData.project_key, serverData).then(response => {
            updateServer(serverData.id, "action_message", "Token saved!")
            updateServer(serverData.id, "action_state", "SUCCESS")
        }).catch(error => {
            const message = `Failed to save token:\n${error.response.data.message}`
            updateServer(serverData.id, "action_message", message)
            updateServer(serverData.id, "action_state", "ERROR")
        });
    }

    const removeToken = e => {
        e.preventDefault();
        const alias = serverData.alias;
        updateServer(serverData.id, "show_clear_modal", false)
        updateServer(serverData.id, "action_message", "Removing settings...")
        updateServer(serverData.id, "action_state", "LOADING")
        removeUserToken(context, serverData.project_key, alias).then(response => {
            updateServer(serverData.id, "default_token", "")
            updateServer(serverData.id, "action_message", "Token removed!")
            updateServer(serverData.id, "action_state", "SUCCESS")
        }).catch(error => {
            updateServer(serverData.id, "action_message", "Could not remove token")
            updateServer(serverData.id, "action_state", "ERROR")
        });
    }

    const testServer = e => {
        e.preventDefault();
        updateServer(serverData.id, "action_message", "Testing settings...")
        updateServer(serverData.id, "action_state", "LOADING")
        testJenkinsServer(context, serverData.project_key, serverData).then(response => {
            updateServer(serverData.id, "action_message", response.data)
            updateServer(serverData.id, "action_state", "SUCCESS")
        }).catch(error => {
            updateServer(serverData.id, "action_message", error.response.data)
            updateServer(serverData.id, "action_state", "ERROR")
        });
    }

    const openAPITokenWindow = e => {
        e.preventDefault();
        const userSlug = document.getElementsByName("userSlug")[0].content;
        window.open(`${serverData.url}/user/${userSlug}/configure`, '_blank')
    }

    return (
        <div>
            <PasswordInput labelText={serverData.alias + " Token"} id="jenkinsToken" 
                       value={serverData.default_token}
                       onChange={(e) => updateServer(serverData.id, "default_token", e.target.value)}/>
            <ButtonGroup>
                <Button id="saveButton" name="submit" buttonText="Save"
                        extraClasses={["aui-button-primary"]}
                        onClick={updateToken} />
                <Button id="apiButton" name="button" buttonText="Get API Token"
                        onClick={openAPITokenWindow}/>
                <Button id="testButton" name="button" buttonText="Test Jenkins Settings"
                        onClick={testServer}/>
                <Button id="clearButton" name="button" buttonText="Clear Jenkins Settings"
                        onClick={(e) => removeToken(e, true)} />
            </ButtonGroup>
            {serverData.action_message !== "" && 
                <ActionDialog message={serverData.action_message} state={serverData.action_state}/>}
        </div>
    );
}

const mapStateToServerContainerProps = (state, ownProps) => {
    return {
        serverData: state.servers[ownProps.id],
        context: state.context,
    };
};

const mapDispatchToServerContainerProps = dispatch => {
    return {
        updateServer: (id, fieldName, fieldValue) => {
            dispatch({
                type: 'UPDATE_FIELD',
                id: id,
                field: fieldName,
                value: fieldValue
            });
        }
    }
}

export const Server = connect(
    mapStateToServerContainerProps,
    mapDispatchToServerContainerProps)(ServerContainer);