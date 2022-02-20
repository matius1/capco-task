package com.skocz.capco.repository;

import com.skocz.capco.repository.entity.GlobalFeature;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GlobalFeatureFlagRepository extends CrudRepository<GlobalFeature, Integer> {

    List<GlobalFeature> findAllByEnabled(boolean enabled);

    Optional<GlobalFeature> findByName(String name);
}
