package com.overcomingroom.bellbell.member.repository;

import com.overcomingroom.bellbell.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Long, Member> {

}
