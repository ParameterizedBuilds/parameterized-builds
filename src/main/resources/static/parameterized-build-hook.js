(function($){

        //
        // functions
        //

        function updateTrigger (element, value, enable) {
        	var id = element.get(0).id.replace("job-", "");
			var trigger = element.find('#triggers-' + id);
			var existing = trigger.val();
			if (enable) {
				if (existing.indexOf("value") == -1) {
					existing += value
				}
			} else {
				existing = existing.replace(value, "");
			}
			trigger.val(existing);
        }
        
        //
        // event bindings
        //
        
        $(document).on('click', '#add-job', function(e) {
            e.preventDefault();
            var existingJobs = $('.parameterized-builds').children('.job').length;
            var html = com.kylenicholls.stash.parameterizedbuilds.hook.addJob({
                'count' : existingJobs,
                'jobName' : '',
                'trigger' : '',
                'token' : '',
                'branch' : '',
                'path' : '',
                'param' : '',
                'collapsed' : false
            });
            $(html).insertBefore($(this));
            $('.parameterized-builds').find('.delete-job').removeClass('hidden').addClass('inline-button');
        });

        $(document).on('click', '.toggle-job', function(e) {
            e.preventDefault();
            $currentJob = $(this).parent().find('.field-group');
            var classes = $(this).find('span').attr('class');
            if (classes.indexOf("aui-iconfont-expanded") > -1) {
            	var title = $currentJob.first().find('input').val();
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
            var $container = $('.parameterized-builds');
            var $jobs = $container.find('.job');
            if ($jobs.length == 1) {
                $jobs.children(".delete-job").removeClass('inline-button').addClass('hidden');
            }
            
            // re-number inputs
            $jobs.each(function(index, currentJob) {
            	$(currentJob).attr('id', 'job-' + index);
            	var $jobFields = $(currentJob).find('.field-group');
            	$jobFields.each(function(index2, currentField) {
            		if (index2 === 0) {
                		$(currentField).find('label').attr('for', 'jobName-' + index);
                		$(currentField).find('input').attr('id', 'jobName-' + index).attr('name', 'jobName-' + index);
                	}
                	if (index2 === 1) {
                		$(currentField).find('label').attr('for', 'trigger-buttons-' + index);
                	}
                	if (index2 === 2) {
                		$(currentField).find('label').attr('for', 'triggers-' + index);
                		$(currentField).find('input').attr('id', 'triggers-' + index).attr('name', 'triggers-' + index);
                	}
            		if (index2 === 3) {
                		$(currentField).find('label').attr('for', 'token-' + index);
                		$(currentField).find('input').attr('id', 'token-' + index).attr('name', 'token-' + index);
                	}
                	if (index2 === 4) {
                		$(currentField).find('label').attr('for', 'buildParameters-' + index);
                		$(currentField).find('textarea').attr('id', 'buildParameters-' + index).attr('name', 'buildParameters-' + index);
                	}
                	if (index2 === 5) {
                		$(currentField).find('label').attr('for', 'branchRegex-' + index);
                		$(currentField).find('input').attr('id', 'branchRegex-' + index).attr('name', 'branchRegex-' + index);
                	}
                	if (index2 === 6) {
                		$(currentField).find('label').attr('for', 'pathRegex-' + index);
                		$(currentField).find('input').attr('id', 'pathRegex-' + index).attr('name', 'pathRegex-' + index);
                	}
            	});
            });
        });
        
        $(document).on('click', '.branch-created', function(e) {
            e.preventDefault();
            var classes = $(this).find('span').attr('class');
            var id = $(this).parent().parent().get(0).id.replace("job-", "");
            if (classes.indexOf("aui-lozenge-success") > -1) {
            	updateTrigger($(this).parent().parent(), "add;", false);
				var trigger = $(this).parent().parent().find('#triggers-' + id);
				var existing = trigger.val();
            	if (existing.indexOf("push;") == -1 && existing.indexOf("delete;") == -1) {
					$(this).parent().parent().find('#branchRegex-' + id).parent().addClass('hide-branches');
            	}
            } else {
            	updateTrigger($(this).parent().parent(), "add;", true);
				$(this).parent().parent().find('#branchRegex-' + id).parent().removeClass('hide-branches');
            }
            $(this).find('span').toggleClass("aui-lozenge-success");
        });

        $(document).on('click', '.push-event', function(e) {
            e.preventDefault();
            var classes = $(this).find('span').attr('class');
            var id = $(this).parent().parent().get(0).id.replace("job-", "");
            if (classes.indexOf("aui-lozenge-success") > -1) {
            	updateTrigger($(this).parent().parent(), "push;", false);
				var trigger = $(this).parent().parent().find('#triggers-' + id);
				var existing = trigger.val();
            	if (existing.indexOf("prmerged;") == -1 && existing.indexOf("pullrequest;") == -1) {
    				$(this).parent().parent().find('#pathRegex-' + id).parent().addClass('hide-paths');
            	}
            	if (existing.indexOf("add;") == -1 && existing.indexOf("delete;") == -1) {
					$(this).parent().parent().find('#branchRegex-' + id).parent().addClass('hide-branches');
            	}
            } else {
            	updateTrigger($(this).parent().parent(), "push;", true);
				$(this).parent().parent().find('#branchRegex-' + id).parent().removeClass('hide-branches');
				$(this).parent().parent().find('#pathRegex-' + id).parent().removeClass('hide-paths');
            }
            $(this).find('span').toggleClass("aui-lozenge-success");
        });

        $(document).on('click', '.manual', function(e) {
            e.preventDefault();
            var classes = $(this).find('span').attr('class');
            if (classes.indexOf("aui-lozenge-success") > -1) {
            	updateTrigger($(this).parent().parent(), "manual;", false);
            } else {
            	updateTrigger($(this).parent().parent(), "manual;", true);
            }
            $(this).find('span').toggleClass("aui-lozenge-success");
        });

        $(document).on('click', '.branch-deleted', function(e) {
            e.preventDefault();
            var classes = $(this).find('span').attr('class');
            var id = $(this).parent().parent().get(0).id.replace("job-", "");
            if (classes.indexOf("aui-lozenge-success") > -1) {
            	updateTrigger($(this).parent().parent(), "delete;", false);
				var trigger = $(this).parent().parent().find('#triggers-' + id);
				var existing = trigger.val();
            	if (existing.indexOf("push;") == -1 && existing.indexOf("add;") == -1) {
					$(this).parent().parent().find('#branchRegex-' + id).parent().addClass('hide-branches');
            	}
            } else {
            	updateTrigger($(this).parent().parent(), "delete;", true);
				$(this).parent().parent().find('#branchRegex-' + id).parent().removeClass('hide-branches');
            }
            $(this).find('span').toggleClass("aui-lozenge-success");
        });

        $(document).on('click', '.pr-opened', function(e) {
            e.preventDefault();
            var classes = $(this).find('span').attr('class');
            var id = $(this).parent().parent().get(0).id.replace("job-", "");
            if (classes.indexOf("aui-lozenge-success") > -1) {
            	updateTrigger($(this).parent().parent(), "pullrequest;", false);
				var trigger = $(this).parent().parent().find('#triggers-' + id);
				var existing = trigger.val();
            	if (existing.indexOf("push;") == -1 && existing.indexOf("prmerged;") == -1 && existing.indexOf("prdeclined;") == -1) {
    				$(this).parent().parent().find('#pathRegex-' + id).parent().addClass('hide-paths');
            	}
            } else {
            	updateTrigger($(this).parent().parent(), "pullrequest;", true);
				$(this).parent().parent().find('#pathRegex-' + id).parent().removeClass('hide-paths');
            }
            $(this).find('span').toggleClass("aui-lozenge-success");
        });

        $(document).on('click', '.pr-merged', function(e) {
            e.preventDefault();
            var classes = $(this).find('span').attr('class');
            var id = $(this).parent().parent().get(0).id.replace("job-", "");
            if (classes.indexOf("aui-lozenge-success") > -1) {
            	updateTrigger($(this).parent().parent(), "prmerged;", false);
				var trigger = $(this).parent().parent().find('#triggers-' + id);
				var existing = trigger.val();
            	if (existing.indexOf("push;") == -1 && existing.indexOf("pullrequest;") == -1 && existing.indexOf("prdeclined;") == -1) {
    				$(this).parent().parent().find('#pathRegex-' + id).parent().addClass('hide-paths');
            	}
            } else {
            	updateTrigger($(this).parent().parent(), "prmerged;", true);
				$(this).parent().parent().find('#pathRegex-' + id).parent().removeClass('hide-paths');
            }
            $(this).find('span').toggleClass("aui-lozenge-success");
        });

        $(document).on('click', '.pr-declined', function(e) {
            e.preventDefault();
            var classes = $(this).find('span').attr('class');
            var id = $(this).parent().parent().get(0).id.replace("job-", "");
            if (classes.indexOf("aui-lozenge-success") > -1) {
            	updateTrigger($(this).parent().parent(), "prdeclined;", false);
				var trigger = $(this).parent().parent().find('#triggers-' + id);
				var existing = trigger.val();
            	if (existing.indexOf("push;") == -1 && existing.indexOf("pullrequest;") == -1 && existing.indexOf("prmerged;") == -1) {
    				$(this).parent().parent().find('#pathRegex-' + id).parent().addClass('hide-paths');
            	}
            } else {
            	updateTrigger($(this).parent().parent(), "prdeclined;", true);
				$(this).parent().parent().find('#pathRegex-' + id).parent().removeClass('hide-paths');
            }
            $(this).find('span').toggleClass("aui-lozenge-success");
        });
}(AJS.$));