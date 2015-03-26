define('jenkins/parameterized-build-layout', [
    'model/page-state',
    'trigger/trigger-jenkins',
    'exports'
], function(
    pageState,
    branchBuild,
    exports
) {
    exports.onReady = function () {
        branchBuild.bindToDropdownLink('.parameterized-build-layout', '#branch-actions-menu', function () {
            return pageState.getRevisionRef().getDisplayId();
        });
    };
});

AJS.$(document).ready(function () {
    require('jenkins/parameterized-build-layout').onReady();
});