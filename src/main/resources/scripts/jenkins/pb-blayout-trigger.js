define('jenkins/parameterized-build-layout', [
    'bitbucket/util/state',
    'trigger/build-dialog',
    'exports'
], function(
    pageState,
    branchBuild,
    exports
) {
    exports.onReady = function () {
        branchBuild.bindToDropdownLink('.parameterized-build-layout', '#branch-actions-menu', function () {
            return [pageState.getRef().id, pageState.getRef().latestCommit];
        });
    };
});

require('aui').$(document).ready(function () {
    require('jenkins/parameterized-build-layout').onReady();
});