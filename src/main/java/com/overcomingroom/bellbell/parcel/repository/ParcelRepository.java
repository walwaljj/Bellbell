package com.overcomingroom.bellbell.parcel.repository;

import com.overcomingroom.bellbell.member.domain.entity.Member;
import com.overcomingroom.bellbell.parcel.domain.entity.Parcel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
  Optional<List<Parcel>> findAllByMember(Member member);
}
