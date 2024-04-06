package com.overcomingroom.bellbell.lunch.repository;

import com.overcomingroom.bellbell.lunch.domain.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
