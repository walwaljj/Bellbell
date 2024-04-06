package com.overcomingroom.bellbell.lunch.repository;

import com.overcomingroom.bellbell.lunch.domain.entity.Lunch;
import com.overcomingroom.bellbell.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LunchRepository extends JpaRepository<Lunch, Long> {
    Optional<Lunch> findByMember(Member member);
}
