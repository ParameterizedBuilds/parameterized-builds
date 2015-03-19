define('jenkins/branchlist-trigger-jenkins', [
    'jquery',
    'trigger/trigger-jenkins',
    'exports'
], function(
    $,
    branchBuild,
    exports
) {
    exports.onReady = function () {
    	branchBuild.bindToDropdownLink('.branchlist-trigger-jenkins', '.branch-list-action-dropdown', function (element) {
            return $(element).closest('[data-display-id]').attr('data-display-id');
        });
    };
});

AJS.$(document).ready(function () {
    require('jenkins/branchlist-trigger-jenkins').onReady();
});