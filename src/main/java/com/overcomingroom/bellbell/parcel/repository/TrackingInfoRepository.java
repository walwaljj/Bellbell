package com.overcomingroom.bellbell.parcel.repository;

import com.overcomingroom.bellbell.parcel.domain.entity.Parcel;
import com.overcomingroom.bellbell.parcel.domain.entity.TrackingInfo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingInfoRepository extends JpaRepository<TrackingInfo, Long> {
  List<TrackingInfo> findAllByParcel(Parcel parcel);
}
