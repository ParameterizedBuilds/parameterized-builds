define('jenkins/parameterized-build-pullrequest', [
  'aui',
  'jquery',
  'bitbucket/internal/model/page-state',
  'bitbucket/internal/util/ajax',
  'aui/flag'
], function(
   _aui,
   $,
   pageState,
   ajax,
   flag
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
		var successFlag = flag({
            type: 'success',
            body: 'Build started',
            close: 'auto'
        });
		ajax.rest({
		  type: "POST",
		  url: buildUrl,
		  dataType: 'json',
		  async: true
		}).success(function (data) {
    		if (data.error){
    			successFlag.close();
    			flag({
                    type: 'warning',
                    body: data.messageText,
                    close: 'auto'
                });
    		} else if (data.prompt) {
    			var promptCookie = getCookie("jenkinsPrompt");
    			var settingsPath = _aui.contextPath() + "/plugins/servlet/account/jenkins";
    			if (promptCookie !== "ignore") {
	    			flag({
	                    type: 'info',
	                    body: '<p>Optional: <a href="' + settingsPath + 
	                    '" target="_blank">You can link your Jenkins account to your Bitbucket account.</a></p>' + 
	                    '<br/><label><input id="prompt-cookie" type="checkbox" /><span>Don\'t show again</span></label>'
	                });
    			}
    		}
		});
	};
	
	$(document).on('change', '#prompt-cookie', function(e) {
		var d = new Date();
	    d.setTime(d.getTime() + (365*24*60*60*1000));
	    var expires = "expires="+ d.toUTCString();
		document.cookie = "jenkinsPrompt=ignore;" + expires + ";path=/";
    });
	
	function getCookie(cname) {
	    var name = cname + "=";
	    var ca = document.cookie.split(';');
	    for(var i = 0; i <ca.length; i++) {
	        var c = ca[i];
	        while (c.charAt(0)==' ') {
	            c = c.substring(1);
	        }
	        if (c.indexOf(name) == 0) {
	            return c.substring(name.length,c.length);
	        }
	    }
	    return "";
	}
	
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
		var prJSON = require('bitbucket/internal/model/page-state').getPullRequest().toJSON();
		branchName = prJSON.fromRef.displayId;
		commit = prJSON.fromRef.latestCommit;
		
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