define('jenkins/parameterized-build-pullrequest', [
  'aui',
  'jquery',
  'widget/notifications-center',
  'model/page-state'
], function(
   _aui,
   $,
   notificationsCenter,
   pageState
) {
	var branchName;
	var commit;
	var jobs;
	
	function getResourceUrl(resourceType){
		return _aui.contextPath() + '/rest/parameterized-builds/latest/projects/' + pageState.getProject().getKey() + '/repos/'
        + pageState.getRepository().getSlug() + '/' + resourceType;
	}
    
	function getJobs(resourceUrl){
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
	
	function triggerBuild(buildUrl){
		notificationsCenter.showNotification(AJS.I18n.getText("Build starting..."));
		$.ajax({
		  type: "POST",
		  url: buildUrl,
		  dataType: 'json',
		  async: true,
		  success: function (data){
			myjson = data;
			var buildStatus = myjson.status;
    		var buildMessage = myjson.message;
    		if (buildStatus == "prompt"){
    			var settingsPath = AJS.contextPath() + "/plugins/servlet/account/jenkins"
				AJS.messages.hint("#notifications-center", {
					body: '<p>Optional: <a href="' + settingsPath + '" target="_blank">You can now link your Jenkins account to your Stash account.</a></p>',
					fadeout: true,
					delay: 5000
				});
    		} else if (buildStatus !== "201"){
    			notificationsCenter.showNotification(AJS.I18n.getText(buildMessage));
    		}
		  }
		});
	};
	
    function getParameterArray(jobId){
    	var parameterArray = [];
		var params = jobs[jobId].parameters;
		for (var param in params){
			var val = params[param];
			parameterArray.push([param, val.split(";")]);
		}
		return parameterArray;
    };
    
	function SetupJobForm(element, jobId) {
		//Remove previous elements
		var $container = $('.job-params');
		$container.remove();
		var parameterArray = getParameterArray(jobId);
		var html = com.kylenicholls.stash.parameterizedbuilds.jenkins.branchBuild.createParams({
            'count' : parameterArray.length,
            'parameters' : parameterArray
        });
        $(html).insertAfter(element);
	};
	
	$(document).on('change', '#job', function(e) {
    	e.preventDefault();
    	SetupJobForm(this, this.value);
    });
	
    function showManualBuildDialog(buildUrl, jobIdArray) {
    	
        var dialog = _aui.dialog2(aui.dialog.dialog2({
            titleText: AJS.I18n.getText('Build with Parameters'),
            content: com.kylenicholls.stash.parameterizedbuilds.jenkins.branchBuild.buildDialog({
                jobs: jobIdArray
            }),
            footerActionContent: com.kylenicholls.stash.parameterizedbuilds.jenkins.branchBuild.buildButton(),
            removeOnHide: true
        })).show();
        
        var selects = document.getElementById("job");
        var selectedValue = selects.options[selects.selectedIndex].value;
        SetupJobForm(selects, selectedValue);
        
        dialog.$el.find('form').on('submit', function(e) { e.preventDefault(); });
        dialog.$el.find('#start-build').on('click', function() {
            _.defer(function() {
            	var $jobParameters = dialog.$el.find('.web-post-hook');
            	var jobSelect = document.getElementById("job");
                var id = selects.options[selects.selectedIndex].value;
            	buildUrl += "/" + jobs[id].id + "?";
            	$jobParameters.each(function(index, jobParam) {
            		var $curJobParam = $(jobParam);
            		var key = $curJobParam.find('label').text();
            		var value = dialog.$el.find('#build-param-value-' + index).val();
            		var type = $(dialog.$el.find('#build-param-value-' + index)[0]).attr('class');
            		if (type.indexOf("checkbox") > -1) {
            			value = dialog.$el.find('#build-param-value-' + index)[0].checked;
            		}
            		buildUrl += key + "=" + value + "&";
            	});
            	triggerBuild(buildUrl.slice(0,-1));
                dialog.hide();
            });
        }).focus().select();
    }
	
	$(".parameterized-build-pullrequest").click(function() {
		var prJSON = require('model/page-state').getPullRequest().toJSON();
		branchName = prJSON.fromRef.displayId;
		commit = prJSON.fromRef.latestChangeset;
		
		var resourceUrl = getResourceUrl("getJobs");
    	
		jobs = getJobs(resourceUrl);
    	var jobArray = [];
    	for (var i in jobs){
    		for (var param in jobs[i].parameters){
    			jobs[i].parameters[param] = jobs[i].parameters[param].replace("$BRANCH", branchName);
    			jobs[i].parameters[param] = jobs[i].parameters[param].replace("$COMMIT", commit);
    		}
    		jobArray.push(jobs[i]);
 	    }
    	
    	if (jobArray.length == 1){
    		parameters = getParameterArray("0");
        	if (parameters.length == 0){
				var buildUrl = getResourceUrl("triggerBuild/0");
        		triggerBuild(buildUrl);
        	} else {
        		var buildUrl = getResourceUrl("triggerBuild");
        		showManualBuildDialog(buildUrl, jobArray);
        	}
    	} else {
    		var buildUrl = getResourceUrl("triggerBuild");
        	showManualBuildDialog(buildUrl, jobArray);
    	}
    	
		return false;
	});

});

AJS.$(document).ready(function() {
    require('jenkins/parameterized-build-pullrequest');
});