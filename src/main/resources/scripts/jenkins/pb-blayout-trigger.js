define('jenkins/parameterized-build-layout', [
    'model/page-state',
    'trigger/build-dialog',
    'exports'
], function(
    pageState,
    branchBuild,
    exports
) {
    exports.onReady = function () {
        branchBuild.bindToDropdownLink('.parameterized-build-layout', '#branch-actions-menu', function () {
            return [pageState.getRevisionRef().getDisplayId(), pageState.getRevisionRef().getLatestChangeset()];
        });
    };
});

AJS.$(document).ready(function () {
    require('jenkins/parameterized-build-layout').onReady();
});