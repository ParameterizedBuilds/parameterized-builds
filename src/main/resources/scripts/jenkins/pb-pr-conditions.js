define('jenkins/parameterized-condition-pullrequest', [
    'aui',
    'jquery',
    'bitbucket/internal/model/page-state',
    'bitbucket/internal/util/ajax',
    'aui/flag',
    'exports'
], function(
    _aui,
    $,
    pageState,
    ajax,
    flag,
    exports
) {
    var jobs;

    exports.prConditions = function (context) {

        var willDisplay = true;
        var prJSON = context['pullRequest'];
        var branch = prJSON.fromRef.id;
        var commit = prJSON.fromRef.latestCommit;
        var prDest = prJSON.toRef.displayId;

        willDisplay = willDisplay && prJSON.state === 'OPEN';

        var jobsUrl = getResourceUrl("getJobs") + "?branch=" + encodeURIComponent(branch) + "&commit=" + commit + "&prdestination=" + encodeURIComponent(prDest) + "&prid=" + prJSON.id;
        var localJobs = getData(jobsUrl);

        willDisplay = willDisplay && localJobs.length > 0;

        var hookUrl = getResourceUrl("getHookEnabled");
        var hookEnabled = getData(hookUrl);

        return willDisplay && hookEnabled;
    };

    function getResourceUrl(resourceType){
        return _aui.contextPath() + '/rest/parameterized-builds/latest/projects/' + pageState.getProject().getKey() + '/repos/'
            + pageState.getRepository().getSlug() + '/' + resourceType;
    }

    function getData(resourceUrl){
        var results;
        $.ajax({
            type: "GET",
            url: resourceUrl,
            dataType: 'json',
            async: false,
            success: function (data){
                results = data;
            }
        });
        return results;
    }
});