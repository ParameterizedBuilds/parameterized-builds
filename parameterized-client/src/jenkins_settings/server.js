import React from 'react';
import { connect } from 'react-redux';
import { TextInput, Checkbox, ButtonGroup, Button, Message } from '../common/aui'

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
    updateServer
}) => {
    return (
        <div>
            <TextInput labelText="Base URL" id="jenkinsUrl" 
                       required={true} value={serverData.url}
                       onChange={(e) => updateServer(serverData.id, "url", e.target.value)}/>
            <TextInput labelText="Server Nickname" id="jenkinsAlias" 
                       required={true} value={serverData.alias}
                       onChange={(e) => updateServer(serverData.id, "alias", e.target.value)}/>
            <TextInput labelText="Default User" id="jenkinsUser" 
                       value={serverData.default_user}
                       onChange={(e) => updateServer(serverData.id, "default_user", e.target.value)}/>
            <Checkbox labelText="Build Token Root Plugin (uses an alternate url for triggering builds)"
                      id="jenkinsAltUrl" checked={serverData.root_token_enabled}
                      onChange={(e) => updateServer(serverData.id, "root_token_enabled", e.target.checked)} />
            <Checkbox labelText="CSRF protection" id="jenkinsCSRF"
                      checked={serverData.csrf_enabled} 
                      onChange={(e) => updateServer(serverData.id, "csrf_enabled", e.target.checked)} />
            <ButtonGroup>
                <Button id="saveButton" name="submit" buttonText="Save"
                        extraClasses={["aui-button-primary"]} />
                <Button id="testButton" name="submit" buttonText="Test Jenkins Settings" />
                <Button id="clearButton" name="submit" buttonText="Clear Jenkins Settings" />
            </ButtonGroup>
            {serverData.action_message !== "" && 
                <ActionDialog message={serverData.action_message} state={serverData.action_state}/>}
        </div>
    );
}

const mapStateToServerContainerProps = (state, ownProps) => {
    return {
        serverData: state.servers[ownProps.id]
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