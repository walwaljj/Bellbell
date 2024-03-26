package com.overcomingroom.bellbell.basicNotification.repository;

import com.overcomingroom.bellbell.basicNotification.domain.entity.BasicNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasicNotificationRepository extends JpaRepository<BasicNotification, Long> {
}
