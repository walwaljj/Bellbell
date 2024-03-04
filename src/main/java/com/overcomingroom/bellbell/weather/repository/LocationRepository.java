package com.overcomingroom.bellbell.weather.repository;

import com.overcomingroom.bellbell.weather.domain.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByMemberId(Long id);
}
