package com.overcomingroom.bellbell.weather.repository;

import com.overcomingroom.bellbell.weather.domain.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
