import * as hook_template from './parameterized-build-hook.soy';
import $ from "jquery";

let addJob = hook_template.kylenicholls.stash.parameterizedbuilds.hook.addJob;

// create button definitions here
// button logic is automatically created for all values in triggers
const triggers =
    {
        '.branch-created':
            {
                'trigger': 'add;',
                'extraFields': ['#branchRegex-']
            },
        '.push-event':
            {
                'trigger': 'push;',
                'extraFields': ['#pathRegex-', '#branchRegex-']
            },
        '.manual':
            {
                'trigger': 'manual;',
                'extraFields': ['#requirePermission-']
            },
        '.branch-deleted':
            {
                'trigger': 'delete;',
                'extraFields': ['#branchRegex-']
            },
        '.pr-opened':
            {
                'trigger': 'pullrequest;',
                'extraFields': ['#pathRegex-', '#prDestinationRegex-']
            },
        '.pr-merged':
            {
                'trigger': 'prmerged;',
                'extraFields': ['#pathRegex-', '#prDestinationRegex-']
            },
        '.pr-auto-merged':
            {
                'trigger': 'prautomerged;',
                'extraFields': []
            },
        '.pr-declined':
            {
                'trigger': 'prdeclined;',
                'extraFields': ['#pathRegex-', '#prDestinationRegex-']
            },
        '.pr-deleted':
            {
                'trigger': 'prdeleted;',
                'extraFields': ['#pathRegex-', '#prDestinationRegex-']
            },
        '.pr-approved':
            {
                'trigger': 'prapproved;',
                'extraFields': ['#pathRegex-', '#prDestinationRegex-']
            }
    };

// used by showField to determine if the field should be hidden or not
// triggers is automatically populated by populateFields, class is the only needed value
let extraFields =
    {
        '#branchRegex-': {
            'class' : 'hide-branches',
            'triggers' : []
        },
        '#pathRegex-': {
            'class' : 'hide-paths',
            'triggers' : []
        },
        '#requirePermission-': {
            'class' : 'hide-permissions',
            'triggers' : []
        },
        '#prDestinationRegex-': {
            'class' : 'hide-pr-dest',
            'triggers' : []
        }
    };

//
// functions
//
function populateFields () {
    let trigKeys = Object.keys(triggers);
    for (let j = 0; j < trigKeys.length; j++) {
        let trigger = trigKeys[j];
        let fields = triggers[trigger]['extraFields'];
        for (let k = 0; k < fields.length; k++) {
            let field = fields[k];
            extraFields[field]['triggers'].push(triggers[trigger]['trigger'])
        }
    }
}
populateFields();

function updateTrigger (element, value, enable) {
    let id = element.get(0).id.replace("job-", "");
    let trigger = element.find('#triggers-' + id);
    let existing = trigger.val();
    if (enable) {
        if (existing.indexOf("value") == -1) {
            existing += value
        }
    } else {
        existing = existing.replace(value, "");
    }
    trigger.val(existing);
}

function showField (existing, trigger, field) {
    let relevantTriggers = extraFields[field]['triggers'];
    let shouldDisplay = false;
    for (let i = 0; i < relevantTriggers.length; i++) {
        if (existing.indexOf(relevantTriggers[i]) != -1 && relevantTriggers[i] != trigger){
            shouldDisplay = true
        }
    }
    return shouldDisplay
}

function setButton (buttonName) {
    let triggerName = triggers[buttonName]['trigger'];
    let fields = triggers[buttonName]['extraFields'];
    $(document).on('click', buttonName, function(e) {
        e.preventDefault();
        let classes = $(this).find('span').attr('class');
        let id = $(this).parent().parent().get(0).id.replace("job-", "");
        if (classes.indexOf("aui-lozenge-success") > -1) {
            updateTrigger($(this).parent().parent(), triggerName, false);
            let trigger = $(this).parent().parent().find('#triggers-' + id);
            let existing = trigger.val();
            for (let i = 0; i < fields.length; i++){
                let field = fields[i];
                if (!showField(existing, triggerName, field)){
                    $(this).parent().parent().find(field + id).parent().addClass(extraFields[field]['class']);
                }
            }
        } else {
            updateTrigger($(this).parent().parent(), triggerName, true);
            for (let j = 0; j < fields.length; j++){
                let fieldID = fields[j];
                $(this).parent().parent().find(fieldID + id).parent().removeClass(extraFields[fieldID]['class']);
            }
        }
        $(this).find('span').toggleClass("aui-lozenge-success");
    });
}

//
// event bindings
//
$(document).on('click', '#add-job', function(e) {
    e.preventDefault();
    let parameterized_builds =  $('.parameterized-builds');
    let existingJobs = parameterized_builds.children('.job').length;
    let html = addJob({
        'count' : existingJobs,
        'jobName' : '',
        'isTag' : false,
        'isPipeline': false,
        'trigger' : '',
        'token' : '',
        'branch' : '',
        'path' : '',
        'param' : '',
        'permissions' : '',
        'prDestinationRegex': '',
        'collapsed' : false
    });
    $(html).insertBefore($(this));
    parameterized_builds.find('.delete-job').removeClass('hidden').addClass('inline-button');
});

$(document).on('click', '.toggle-job', function(e) {
    e.preventDefault();
    let $currentJob = $(this).parent().find('.field-group');
    let classes = $(this).find('span').attr('class');
    if (classes.indexOf("aui-iconfont-expanded") > -1) {
        let title = $currentJob.first().find('input').val();
        $(this).find('a').append(title);
    } else {
        $(this).find('a').contents().filter(function(){ return this.nodeType == 3; }).remove();
    }
    $(this).find('span').toggleClass('aui-iconfont-expanded');
    $(this).find('span').toggleClass('aui-iconfont-collapsed');
    $currentJob.each(function(index, field) {
        $(field).toggleClass('hidden');
    });
});

$(document).on('click', '.delete-job', function (e) {
    e.preventDefault();
    $(this).parent().remove();
    let $container = $('.parameterized-builds');
    let $jobs = $container.find('.job');
    if ($jobs.length == 1) {
        $jobs.children(".delete-job").removeClass('inline-button').addClass('hidden');
    }

    // re-number inputs
    $jobs.each(function(index, currentJob) {
        $(currentJob).attr('id', 'job-' + index);
        let $jobFields = $(currentJob).find('.field-group');
        $jobFields.each(function(index2, currentField) {
            let i = 0; //avoid manual renumbering when a field is added
            if (index2 === i++) {
                $(currentField).find('label').attr('for', 'jobName-' + index);
                $(currentField).find('input').attr('id', 'jobName-' + index).attr('name', 'jobName-' + index);
            }
            if (index2 === i++) {
                $(currentField).find('label').attr('for', 'isTag-' + index);
                $(currentField).find('select').attr('id', 'isTag-' + index).attr('name', 'isTag-' + index);
            }
            if (index2 === i++) {
                $(currentField).find('label').attr('for', 'isPipeline-' + index);
                $(currentField).find('select').attr('id', 'isPipeline-' + index).attr('name', 'isPipeline-' + index);
            }
            if (index2 === i++) {
                $(currentField).find('label').attr('for', 'trigger-buttons-' + index);
            }
            if (index2 === i++) {
                $(currentField).find('label').attr('for', 'triggers-' + index);
                $(currentField).find('input').attr('id', 'triggers-' + index).attr('name', 'triggers-' + index);
            }
            if (index2 === i++) {
                $(currentField).find('label').attr('for', 'token-' + index);
                $(currentField).find('input').attr('id', 'token-' + index).attr('name', 'token-' + index);
            }
            if (index2 === i++) {
                $(currentField).find('label').attr('for', 'buildParameters-' + index);
                $(currentField).find('textarea').attr('id', 'buildParameters-' + index).attr('name', 'buildParameters-' + index);
            }
            if (index2 === i++) {
                $(currentField).find('label').attr('for', 'branchRegex-' + index);
                $(currentField).find('input').attr('id', 'branchRegex-' + index).attr('name', 'branchRegex-' + index);
            }
            if (index2 === i++) {
                $(currentField).find('label').attr('for', 'pathRegex-' + index);
                $(currentField).find('input').attr('id', 'pathRegex-' + index).attr('name', 'pathRegex-' + index);
            }
            if (index2 === i++) {
                $(currentField).find('label').attr('for', 'requirePermission-' + index);
                $(currentField).find('select').attr('id', 'requirePermission-' + index).attr('name', 'requirePermission-' + index);
            }
            if (index2 === i++) {
                $(currentField).find('label').attr('for', 'prDestinationRegex-' + index);
                $(currentField).find('select').attr('id', 'prDestinationRegex-' + index).attr('name', 'prDestinationRegex-' + index);
            }
        });
    });
});

// create event bindings for each trigger
Object.keys(triggers).forEach(setButton);

export { hook_template };