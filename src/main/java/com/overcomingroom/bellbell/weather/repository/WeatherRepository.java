package com.overcomingroom.bellbell.weather.repository;

import com.overcomingroom.bellbell.weather.domain.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findByMemberId(Long id);
}
