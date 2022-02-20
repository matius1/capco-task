package com.skocz.capco.service;

import com.skocz.capco.exception.FeatureFlagException;
import com.skocz.capco.exception.UserNotFoundException;
import com.skocz.capco.repository.GlobalFeatureFlagRepository;
import com.skocz.capco.repository.UserFeatureFlagRepository;
import com.skocz.capco.repository.UserRepository;
import com.skocz.capco.repository.entity.GlobalFeature;
import com.skocz.capco.repository.entity.User;
import com.skocz.capco.repository.entity.UserFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Log
public class FeatureFlagsService {
    private final UserRepository userRepository;
    private final GlobalFeatureFlagRepository globalFeatureRepository;
    private final UserFeatureFlagRepository userFeatureRepository;

    public UserFeature switchFeatureFlagForUser(String featureFlagName, Integer userId, boolean isEnabled) {
        log.info("Request to change feature flag: " + featureFlagName + " for user with id: " + userId + " to enabled: " + isEnabled);

        User user = getUserById(userId);
        checkIfFeatureFlagDoNotExists(featureFlagName);

        Optional<UserFeature> existingUserFeatureFlag = userFeatureRepository.findByUserAndName(user, featureFlagName);

        return existingUserFeatureFlag.isPresent() ?
                updateUserFeatureFlag(isEnabled, existingUserFeatureFlag)
                : createUserFeatureFlag(featureFlagName, isEnabled, user);

    }

    private UserFeature createUserFeatureFlag(String featureFlagName, boolean isEnabled, User user) {
        UserFeature toCreate = UserFeature.builder()
                .user(user)
                .name(featureFlagName)
                .enabled(isEnabled)
                .build();

        UserFeature created = userFeatureRepository.save(toCreate);
        log.info("Created new feature flag for a user: " + created);
        return created;
    }

    private UserFeature updateUserFeatureFlag(boolean isEnabled, Optional<UserFeature> existingUserFeatureFlag) {
        UserFeature userFeatureFlag = existingUserFeatureFlag.get();
        userFeatureFlag.setEnabled(isEnabled);
        UserFeature updated = userFeatureRepository.save(userFeatureFlag);
        log.info("There was existing user flag. Saved updated: " + updated);
        return updated;
    }

    public GlobalFeature createFeature(String newFeatureName) {
        log.info("Create feature flag: " + newFeatureName);

        checkIfFeatureFlagExists(newFeatureName);

        return createFeatureFlag(newFeatureName);
    }

    private GlobalFeature createFeatureFlag(String newFeatureName) {
        GlobalFeature toCreate = GlobalFeature.builder()
                .name(newFeatureName)
                .enabled(false)
                .build();
        GlobalFeature savedFeature = globalFeatureRepository.save(toCreate);
        log.info("Created a feature flag: " + savedFeature);
        return savedFeature;
    }

    private void checkIfFeatureFlagExists(String newFeatureName) {
        Optional<GlobalFeature> maybe = globalFeatureRepository.findByName(newFeatureName);
        if (maybe.isPresent()) {
            log.info("Feature flag with name: " + newFeatureName + " already exists");
            throw new FeatureFlagException("Feature flag with name: " + newFeatureName + " already exists");
        }
    }

    private void checkIfFeatureFlagDoNotExists(String newFeatureName) {
        Optional<GlobalFeature> maybe = globalFeatureRepository.findByName(newFeatureName);
        if (maybe.isEmpty()) {
            log.info("Feature flag with name: " + newFeatureName + " do not exists");
            throw new FeatureFlagException("Feature flag with name: " + newFeatureName + " do not exists");
        }
    }

    public Set<String> getEnabledFeaturesForUser(Integer userId) {
        log.info("Search for enabled feature flags for user with id: " + userId);
        User user = getUserById(userId);

        List<GlobalFeature> globalEnabledFeatures = globalFeatureRepository.findAllByEnabled(true);
        List<UserFeature> userFeatures = userFeatureRepository.findAllByUser(user);

        Set<String> enabledFeatures = calculateEnabledFeatures(globalEnabledFeatures, userFeatures);

        log.info("Returning feature flags for user with id: " + userId + " - " + enabledFeatures);
        return enabledFeatures;
    }

    private Set<String> calculateEnabledFeatures(List<GlobalFeature> globalEnabledFeatures, List<UserFeature> userFeatures) {
        Set<String> enabledFeatures = new HashSet<>();

        List<String> globalEnabledFeaturesNames = globalEnabledFeatures.stream()
                .map(GlobalFeature::getName)
                .toList();
        log.info("Global enabled features: " + globalEnabledFeatures);
        enabledFeatures.addAll(globalEnabledFeaturesNames);

        Collection<String> userDisabledFeaturesNames = userFeatures.stream()
                .filter(f -> !f.isEnabled())
                .map(UserFeature::getName)
                .toList();
        log.info("User disable features: " + userDisabledFeaturesNames);
        enabledFeatures.removeAll(userDisabledFeaturesNames);

        List<String> userEnabledFeaturesNames = userFeatures.stream()
                .filter(UserFeature::isEnabled)
                .map(UserFeature::getName)
                .toList();
        log.info("User enabled features: " + userEnabledFeaturesNames);
        enabledFeatures.addAll(userEnabledFeaturesNames);

        log.info("All enabled features: " + enabledFeatures);
        return enabledFeatures;
    }

    private User getUserById(Integer userId) {
        if (userId == null) {
            throw new UserNotFoundException("Invalid user id");
        }

        Optional<User> byId = userRepository.findById(userId);
        User user = byId.orElseThrow(() -> new UserNotFoundException(userId));
        return user;
    }
}
