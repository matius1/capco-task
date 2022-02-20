package com.skocz.capco.init;

import com.skocz.capco.repository.GlobalFeatureFlagRepository;
import com.skocz.capco.repository.UserFeatureFlagRepository;
import com.skocz.capco.repository.UserRepository;
import com.skocz.capco.repository.entity.GlobalFeature;
import com.skocz.capco.repository.entity.User;
import com.skocz.capco.repository.entity.UserFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Log
public class DataLoader {
    private final UserRepository userRepository;
    private final GlobalFeatureFlagRepository globalFeatureRepository;
    private final UserFeatureFlagRepository userFeatureRepository;

    @PostConstruct
    public void init() {

        log.info("Create initial data - start");

        User user = createUser();

        createGlobalFeature("global-1", true);
        createGlobalFeature("global-2", false);

        createUserFeature("user-1", true, user);
        createUserFeature("user-2", false, user);

        createUserFeature("global-2", true, user);


        log.info("Create initial data - end");
    }

    private void createUserFeature(String name, boolean enabled, User user) {
        UserFeature feature = UserFeature.builder()
                .name(name)
                .enabled(enabled)
                .user(user)
                .build();
        UserFeature saved = userFeatureRepository.save(feature);
        log.info("Created: " + saved);
    }

    private void createGlobalFeature(String name, boolean enabled) {
        GlobalFeature feature = GlobalFeature.builder()
                .name(name)
                .enabled(enabled)
                .build();
        GlobalFeature saved = globalFeatureRepository.save(feature);
        log.info("Created: " + saved);
    }

    private User createUser() {
        User entity = User.builder()
                .email("email")
                .firstName("first")
                .lastName("last")
                .build();
        User savedUser = userRepository.save(entity);
        log.info("Created: " + savedUser);

        return savedUser;
    }


}
