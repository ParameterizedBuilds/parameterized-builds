define('jenkins/parameterized-build-branchlist', [
    'jquery',
    'trigger/build-dialog',
    'exports'
], function(
    $,
    branchBuild,
    exports
) {
    exports.onReady = function () {
    	branchBuild.bindToDropdownLink('.parameterized-build-branchlist', '.branch-list-action-dropdown', function (element) {
            return [$(element).closest('[data-display-id]').attr('data-display-id'), $(element).closest('[data-latest-commit]').attr('data-latest-commit')];
        });
    };
});

AJS.$(document).ready(function () {
    require('jenkins/parameterized-build-branchlist').onReady();
});