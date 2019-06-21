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
    checked = false,
    onChange = () => {},
    description = "",
    errorMessage = ""
}) => (
    <fieldset className="group checkbox">
        <div className="checkbox">
            <input id={id} className="checkbox" type="checkbox" 
                    name={id} checked={checked} onChange={onChange}/>
            <label htmlFor={id}>{labelText}</label>
        </div>
        {description.length > 0 &&
        <div className={"description"}>{description}</div>}
        {errorMessage.length > 0 &&
            <div className={"error"}>{errorMessage}</div>}
    </fieldset>
)

export const Button = ({
    id,
    name,
    buttonText,
    onClick= () => {},
    type = "submit",
    extraClasses = [],
}) => (
    <input id={id} className={"aui-button " + extraClasses.join(' ')}
           name={name} value={buttonText} type={type} onClick={onClick} />
)

export const ButtonGroup = ({ children }) => (
    <div className="aui-buttons">
        {children}
    </div>
)

export const Message = ({
    messageClass,
    closeable = false,
    children
}) => (
    <div className={`aui-message ${messageClass} ${closeable ? "closeable" : ""}`}>
        {children}
    </div>
)

export const Form = ({ children }) => (
    <form className="aui prevent-double-submit" acceptCharset="UTF-8">
        {children}
    </form>
)

export const Modal = ({
    id,
    hidden=false,
    extraClasses = [],
    toggleFunction,
    title="",
    footerButtons=[],
    children, }) => (
    <section id={id} className={"aui-dialog2 aui-layer aui-dialog2-small " + extraClasses.join(' ')}
             role="dialog" aria-hidden={hidden}>
        <header className="aui-dialog2-header">
            <h2 className="aui-dialog2-header-main">{title}</h2>
            <a className="aui-dialog2-header-close" onClick={toggleFunction}>
                <span className="aui-icon aui-icon-small aui-iconfont-close-dialog">Close</span>
            </a>
        </header>
        <div className="aui-dialog2-content">
            {children}
        </div>
        <footer className="aui-dialog2-footer">
            <div className="aui-dialog2-footer-actions">
                {footerButtons}
            </div>
        </footer>
    </section>
)