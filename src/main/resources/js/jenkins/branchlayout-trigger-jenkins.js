define('jenkins/branchlayout-trigger-jenkins', [
    'model/page-state',
    'trigger/trigger-jenkins',
    'exports'
], function(
    pageState,
    branchBuild,
    exports
) {
    exports.onReady = function () {
        branchBuild.bindToDropdownLink('.branchlayout-trigger-jenkins', '#branch-actions-menu', function () {
            return pageState.getRevisionRef().getDisplayId();
        });
    };
});

AJS.$(document).ready(function () {
    require('jenkins/branchlayout-trigger-jenkins').onReady();
});