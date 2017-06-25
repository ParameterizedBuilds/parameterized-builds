package com.kylenicholls.stash.parameterizedbuilds.conditions;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class BuildPermissionsCondition extends BaseCondition{

    private final PermissionService permissionService;
    private final AuthenticationContext authContext;

    @Autowired
    public BuildPermissionsCondition(@ComponentImport PermissionService permissionService, SettingsService service, AuthenticationContext authContext) {
        super(service);
        this.permissionService = permissionService;
        this.authContext = authContext;
    }

    public boolean checkPermissions(Job job, Repository repository, ApplicationUser user){
        Permission permissionRequired = Permission.valueOf(job.getPermissions());
        return this.permissionService.hasRepositoryPermission(user, repository, permissionRequired);
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context) {
        final Repository repository = getRepository(context);
        if (repository == null) {
            return false;
        }
        Settings settings = settingsService.getSettings(repository);
        ApplicationUser user = authContext.getCurrentUser();

        for (Job job : settingsService.getJobs(settings.asMap())) {
            if (checkPermissions(job, repository, user)) {
                return true;
            }
        }
        return false;
    }
}

