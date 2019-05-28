import React from 'react';

export const TextInput = ({
    labelText,
    id,
    value,
    required = false,
    onChange = () => {},
    description = "",
    errorMessage = ""
}) => (
    <div className="field-group">
        <label htmlFor={id}>
            {labelText}
            {required && <span className="aui-icon icon-required" />}
        </label>
        <input id={id} className="text" name={id}
               type="text" onChange={onChange} value={value} />
        {description.length > 0 &&
            <div className={"description"}>{description}</div>}
        {errorMessage.length > 0 &&
            <div className={"error"}>{errorMessage}</div>}
    </div>
);

export const Checkbox = ({
    id,
    labelText,
    value = false,
    onChange = () => {},
    description = "",
    errorMessage = ""
}) => (
    <fieldset className="group checkbox">
        <div className="checkbox">
            <input id={id} className="checkbox" type="checkbox" 
                    name={id} value={value} onChange={onChange}/>
            <label htmlFor={id}>{labelText}</label>
        </div>
        {description.length > 0 &&
        <div className={"description"}>{description}</div>}
        {errorMessage.length > 0 &&
            <div className={"error"}>{errorMessage}</div>}
    </fieldset>
)

export const Form = ({ children }) => (
    <form className="aui prevent-double-submit" acceptCharset="UTF-8">
        {children}
    </form>
)