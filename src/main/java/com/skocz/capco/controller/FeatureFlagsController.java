package com.skocz.capco.controller;

import com.skocz.capco.controller.request.FeatureRequest;
import com.skocz.capco.repository.entity.GlobalFeature;
import com.skocz.capco.repository.entity.UserFeature;
import com.skocz.capco.service.FeatureFlagsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@Log
public class FeatureFlagsController {

    private final FeatureFlagsService featureFlagsService;

    @PostMapping("/features/create")
    GlobalFeature createFeature(@RequestBody FeatureRequest request) {
        log.info("Request to create feature: " + request);
        GlobalFeature createdFeature = featureFlagsService.createFeature(request.getName());
        log.info("Create feature: " + createdFeature);
        return createdFeature;
    }

    @GetMapping("/features/user/{userId}")
    Set<String> getUserFeatures(@PathVariable Integer userId) {
        log.info("Request to get enabled features for user with id: " + userId);
        Set<String> featuresForUser = featureFlagsService.getEnabledFeaturesForUser(userId);
        log.info("Returning features for user with id: " + userId);
        return featuresForUser;
    }

    @PutMapping("/features/user/{userId}/feature/{featureFlagName}/on")
    UserFeature enableFeatureForUser(@PathVariable Integer userId, @PathVariable String featureFlagName) {
        log.info("Request to switch on feature flag: " + featureFlagName + " for user with id: " + userId);
        UserFeature result = featureFlagsService.switchFeatureFlagForUser(featureFlagName, userId, true);
        log.info("Completed request to switch on feature flag: " + featureFlagName + " for user with id: " + userId);
        return result;
    }

    @PutMapping("/features/user/{userId}/feature/{featureFlagName}/off")
    UserFeature disableFeatureForUser(@PathVariable Integer userId, @PathVariable String featureFlagName) {
        log.info("Request to switch off feature flag: " + featureFlagName + " for user with id: " + userId);
        UserFeature result = featureFlagsService.switchFeatureFlagForUser(featureFlagName, userId, false);
        log.info("Completed request to switch off feature flag: " + featureFlagName + " for user with id: " + userId);
        return result;
    }

}
