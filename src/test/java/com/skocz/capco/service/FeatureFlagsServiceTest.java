package com.skocz.capco.service;

import com.skocz.capco.exception.FeatureFlagException;
import com.skocz.capco.exception.UserNotFoundException;
import com.skocz.capco.repository.GlobalFeatureFlagRepository;
import com.skocz.capco.repository.UserFeatureFlagRepository;
import com.skocz.capco.repository.UserRepository;
import com.skocz.capco.repository.entity.GlobalFeature;
import com.skocz.capco.repository.entity.User;
import com.skocz.capco.repository.entity.UserFeature;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureFlagsServiceTest {

    @InjectMocks
    private FeatureFlagsService service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GlobalFeatureFlagRepository globalFeatureFlagRepository;

    @Mock
    private UserFeatureFlagRepository userFeatureFlagRepository;


    @Nested
    class CreateFeature {

        @Test
        void givenExistingFeatureFlagName_whenCreate_thenThrowException() {
            //given
            String newFeatureName = "new";
            when(globalFeatureFlagRepository.findByName(newFeatureName))
                    .thenReturn(of(new GlobalFeature()));

            //when
            //then
            assertThrows(FeatureFlagException.class,
                    () -> service.createFeature(newFeatureName));
        }

        @Test
        void givenNotExistingFeatureFlagName_whenCreate_thenCreateOne() {
            //given
            String newFeatureName = "new";
            GlobalFeature expectedGlobalFeature = GlobalFeature.builder()
                    .name(newFeatureName)
                    .enabled(false)
                    .build();
            when(globalFeatureFlagRepository.save(any()))
                    .thenReturn(expectedGlobalFeature);

            //when
            GlobalFeature created = service.createFeature(newFeatureName);

            //then
            assertEquals(newFeatureName, created.getName());
            verify(globalFeatureFlagRepository).save(expectedGlobalFeature);
        }

    }

    @Nested
    class SwitchFeatureFlagForUser {

        @Test
        void givenNotExistingUser_whenSwitchTheFlag_thenThrowException() {
            assertThrows(UserNotFoundException.class,
                    () -> service.switchFeatureFlagForUser("", -1, false));
        }

        @Test
        void givenNotExistingFeatureFlag_whenSwitchTheFlag_thenThrowException() {
            //given
            int userId = 1;
            when(userRepository.findById(userId))
                    .thenReturn(of(new User()));

            //when
            //then
            assertThrows(FeatureFlagException.class,
                    () -> service.switchFeatureFlagForUser("", userId, false));
        }

        @Test
        void givenExistingGlobalFeatureFlag_whenSwitchTheFlag_thenCreateUserFeatureFlag() {
            //given
            User user = getUser();
            String featureFlagName = "feature-1";
            boolean isEnabled = false;
            UserFeature expectedUserFeature = getUserFeature(user, featureFlagName, isEnabled);

            when(userRepository.findById(user.getUserId()))
                    .thenReturn(of(user));
            when(globalFeatureFlagRepository.findByName(featureFlagName))
                    .thenReturn(of(new GlobalFeature()));
            when(userFeatureFlagRepository.save(any()))
                    .thenReturn(expectedUserFeature);

            //when
            UserFeature actual = service.switchFeatureFlagForUser(featureFlagName, user.getUserId(), isEnabled);

            //then
            assertEquals(expectedUserFeature, actual);
        }

        @Test
        void givenExistingGlobalFeatureFlagAndOverwritten_whenSwitchTheFlag_thenCreateUserFeatureFlag() {
            //given
            User user = getUser();
            String featureFlagName = "feature-1";
            boolean isEnabled = true;
            UserFeature expectedUserFeature = getUserFeature(user, featureFlagName, isEnabled);

            when(userRepository.findById(user.getUserId()))
                    .thenReturn(of(user));
            when(globalFeatureFlagRepository.findByName(featureFlagName))
                    .thenReturn(of(new GlobalFeature()));
            when(userFeatureFlagRepository.findByUserAndName(user, featureFlagName))
                    .thenReturn(of(getUserFeature(user, featureFlagName, !isEnabled)));
            when(userFeatureFlagRepository.save(any()))
                    .thenReturn(expectedUserFeature);

            //when
            UserFeature actual = service.switchFeatureFlagForUser(featureFlagName, user.getUserId(), isEnabled);

            //then
            assertEquals(expectedUserFeature, actual);
        }

    }

    @Nested
    class GetEnabledFeaturesForUser {

        @Test
        void givenNotExistingUser_whenGetEnabledFlags_thenThrowException() {
            assertThrows(UserNotFoundException.class,
                    () -> service.getEnabledFeaturesForUser(-1));
        }

        @Test
        void givenNoFlagsForUser_whenGetEnabledFlags_thenReturnEmptySet() {
            //given
            User user = getUser();
            when(userRepository.findById(user.getUserId()))
                    .thenReturn(of(user));

            //when
            Set<String> actual = service.getEnabledFeaturesForUser(user.getUserId());

            //then
            assertTrue(actual.isEmpty());
        }

        @Test
        void givenOneGlobalFlag_whenGetEnabledFlags_thenReturnOne() {
            //given
            User user = getUser();
            when(userRepository.findById(user.getUserId()))
                    .thenReturn(of(user));

            GlobalFeature globalFeature = getGlobalFeature("global-1", true);
            when(globalFeatureFlagRepository.findAllByEnabled(true))
                    .thenReturn(List.of(globalFeature));

            //when
            Set<String> actual = service.getEnabledFeaturesForUser(user.getUserId());

            //then
            assertEquals(1, actual.size());
            assertTrue(actual.contains(globalFeature.getName()));
        }

        @Test
        void givenGlobalFlagDisabledAndUserFlagEnabled_whenGetEnabledFlags_thenReturnOne() {
            //given
            User user = getUser();
            when(userRepository.findById(user.getUserId()))
                    .thenReturn(of(user));

            UserFeature userFeature = getUserFeature(user, "user-1", true);
            when(userFeatureFlagRepository.findAllByUser(user))
                    .thenReturn(List.of(userFeature));

            //when
            Set<String> actual = service.getEnabledFeaturesForUser(user.getUserId());

            //then
            assertEquals(1, actual.size());
            assertTrue(actual.contains(userFeature.getName()));
        }

        @Test
        void givenGlobalFlagEnabledAndUserFlagDisabled_whenGetEnabledFlags_thenReturnEmpty() {
            //given
            User user = getUser();
            when(userRepository.findById(user.getUserId()))
                    .thenReturn(of(user));

            String name = "f-1";
            GlobalFeature globalFeature = getGlobalFeature(name, true);
            when(globalFeatureFlagRepository.findAllByEnabled(true))
                    .thenReturn(List.of(globalFeature));

            UserFeature userFeature = getUserFeature(user, name, false);
            when(userFeatureFlagRepository.findAllByUser(user))
                    .thenReturn(List.of(userFeature));

            //when
            Set<String> actual = service.getEnabledFeaturesForUser(user.getUserId());

            //then
            assertTrue(actual.isEmpty());
        }

    }

    private GlobalFeature getGlobalFeature(String name, boolean enabled) {
        return GlobalFeature.builder()
                .enabled(enabled)
                .name(name)
                .build();
    }

    private UserFeature getUserFeature(User user, String featureFlagName, boolean isEnabled) {
        return UserFeature.builder()
                .user(user)
                .name(featureFlagName)
                .enabled(isEnabled)
                .build();
    }

    private User getUser() {
        return User.builder()
                .email("")
                .userId(1)
                .build();
    }


}