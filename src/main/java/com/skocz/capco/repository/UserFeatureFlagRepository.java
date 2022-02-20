package com.skocz.capco.repository;

import com.skocz.capco.repository.entity.User;
import com.skocz.capco.repository.entity.UserFeature;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFeatureFlagRepository extends CrudRepository<UserFeature, Integer> {

    List<UserFeature> findAllByUser(User user);

    Optional<UserFeature> findByUserAndName(User user, String name);
}
