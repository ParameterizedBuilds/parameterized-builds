import React from 'react';
import { connect } from 'react-redux';
import { TextInput, Checkbox, ButtonGroup, Button } from '../common/aui'

const ServerContainer = ({
    serverData,
    updateServer
}) => {
    return (
        <div>
            <TextInput labelText="Base URL" id="jenkinsUrl" 
                       required={true} value={serverData.url}/>
            <TextInput labelText="Server Nickname" id="jenkinsAlias" 
                       required={true} value={serverData.alias}/>
            <TextInput labelText="Default User" id="jenkinsUser" 
                       value={serverData.default_user}/>
            <Checkbox labelText="Build Token Root Plugin (uses an alternate url for triggering builds)"
                      id="jenkinsAltUrl" value={serverData.root_token_enabled} />
            <Checkbox labelText="CSRF protection" id="jenkinsCSRF"
                      value={serverData.csrf_enabled} />
            <ButtonGroup>
                <Button id="saveButton" name="submit" buttonText="Save"
                        extraClasses={["aui-button-primary"] } />
                <Button id="testButton" name="submit" buttonText="Test Jenkins Settings" />
                <Button id="clearButton" name="submit" buttonText="Clear Jenkins Settings" />
            </ButtonGroup>
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
        updateServer: (alias) => {
            dispatch({
                type: 'UPDATE_SERVER',
            });
        }
    }
}

export const Server = connect(
    mapStateToServerContainerProps, 
    mapDispatchToServerContainerProps)(ServerContainer);