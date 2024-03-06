package com.overcomingroom.bellbell.usernotification.repository;

import com.overcomingroom.bellbell.member.domain.entity.Member;
import com.overcomingroom.bellbell.usernotification.domain.entity.UserNotification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

  Optional<List<UserNotification>> findAllByMember(Member member);

}
