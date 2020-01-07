import React from 'react';
import { connect } from 'react-redux';
import { saveJenkinsServer, deleteJenkinsServer, testJenkinsServer } from '../common/rest'
import { TextInput, Checkbox, ButtonGroup, Button, Message, Modal } from '../common/aui'

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
    project,
    context,
    updateServer,
    removeServer
}) => {

    const saveServer = e => {
        e.preventDefault();
        updateServer(serverData.id, "action_message", "Saving settings...");
        updateServer(serverData.id, "action_state", "LOADING");
        const newAlias = serverData.alias;
        saveJenkinsServer(context, project, serverData).then(response => {
            updateServer(serverData.id, "action_message", "Settings saved!")
            updateServer(serverData.id, "action_state", "SUCCESS")
            updateServer(serverData.id, "old_alias", newAlias)
        }).catch(error => {
            const message = `Failed to save settings:\n${error.response.data.errors.join('\n')}`
            updateServer(serverData.id, "action_message", message)
            updateServer(serverData.id, "action_state", "ERROR")
        });
    }

    const deleteServer = e => {
        e.preventDefault();
        const alias = serverData.old_alias !== "" ? serverData.old_alias : serverData.alias;
        updateServer(serverData.id, "show_clear_modal", false)
        updateServer(serverData.id, "action_message", "Removing settings...")
        updateServer(serverData.id, "action_state", "LOADING")
        deleteJenkinsServer(context, project, alias).then(response => {
            removeServer(serverData.id)
        }).catch(error => {
            updateServer(serverData.id, "action_message", "Could not remove jenkins server")
            updateServer(serverData.id, "action_state", "ERROR")
        });
    }

    const testServer = e => {
        e.preventDefault();
        updateServer(serverData.id, "action_message", "Testing settings...")
        updateServer(serverData.id, "action_state", "LOADING")
        testJenkinsServer(context, project, serverData).then(response => {
            updateServer(serverData.id, "action_message", response.data)
            updateServer(serverData.id, "action_state", "SUCCESS")
        }).catch(error => {
            updateServer(serverData.id, "action_message", error.response.data)
            updateServer(serverData.id, "action_state", "ERROR")
        });
    }

    const toggleModal = (e, newVal) => {
        e.preventDefault();
        updateServer(serverData.id, "show_clear_modal", newVal)
    }

    const tokenField = serverData.default_token === null ?
    <Button id="editToken" buttonText="Edit Token"
            onClick={() => updateServer(serverData.id, "default_token", "")}/> :
    <TextInput labelText="Default Token" id="defaultToken"
               value={serverData.default_token}
               onChange={(e) => updateServer(serverData.id, "default_token", e.target.value)}/>;

    return (
        <div>
            <TextInput labelText="Base URL" id="jenkinsUrl" 
                       required={true} value={serverData.url}
                       onChange={(e) => updateServer(serverData.id, "url", e.target.value)}/>
            <TextInput labelText="Server Nickname" id="jenkinsAlias" 
                       required={true} value={serverData.alias}
                       errorMessage={serverData.alias.includes("/") ? "Nickname cannot contain \"/\"" : "" }
                       onChange={(e) => updateServer(serverData.id, "alias", e.target.value)}/>
            <TextInput labelText="Default User" id="jenkinsUser" 
                       value={serverData.default_user}
                       onChange={(e) => updateServer(serverData.id, "default_user", e.target.value)}/>
            {tokenField}
            <Checkbox labelText="Build Token Root Plugin (uses an alternate url for triggering builds)"
                      id="jenkinsAltUrl" checked={serverData.root_token_enabled}
                      onChange={(e) => updateServer(serverData.id, "root_token_enabled", e.target.checked)} />
            <Checkbox labelText="CSRF protection" id="jenkinsCSRF"
                      checked={serverData.csrf_enabled} 
                      onChange={(e) => updateServer(serverData.id, "csrf_enabled", e.target.checked)} />
            <ButtonGroup>
                <Button id="saveButton" name="submit" buttonText="Save"
                        extraClasses={["aui-button-primary"]}
                        disabled={serverData.alias.includes("/")}
                        onClick={saveServer} />
                <Button id="testButton" name="button" buttonText="Test Jenkins Settings"
                        onClick={testServer}/>
                <Button id="clearButton" name="button" buttonText="Clear Jenkins Settings"
                        onClick={(e) => toggleModal(e, true)} disabled={serverData.old_alias === ""} />
            </ButtonGroup>
            {serverData.action_message !== "" && 
                <ActionDialog message={serverData.action_message} state={serverData.action_state}/>}
            <Modal id="confirmDelete" extraClasses={["aui-dialog2-small", "aui-dialog2-warning"]}
                   hidden={!serverData.show_clear_modal}
                   toggleFunction={(e) => toggleModal(e, false)}
                   title="Confirm Clear Settings" footerButtons={[(
                    <Button id="confirmDeleteButton" name="button" buttonText="Clear Settings"
                            onClick={deleteServer} />
                   )]}>
                <p>Are you sure you want to clear settings for {serverData.alias}? This action cannot be undone.</p>
            </Modal>
        </div>
    );
}

const mapStateToServerContainerProps = (state, ownProps) => {
    return {
        serverData: state.servers[ownProps.id],
        project: state.project,
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
        },
        removeServer: (id) => {
            dispatch({
                type: 'DELETE_SERVER',
                id: id
            })
        }
    }
}

export const Server = connect(
    mapStateToServerContainerProps, 
    mapDispatchToServerContainerProps)(ServerContainer);