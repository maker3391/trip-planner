package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.CommunityImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommunityImageRepository extends JpaRepository<CommunityImage, Long> {

    /**
     * 특정 게시글(community_id)에 속한 모든 이미지 리스트를 조회합니다.
     * Spring Data JPA가 메서드 이름을 분석하여
     * "SELECT * FROM community_image WHERE community_id = ?" 쿼리를 자동으로 생성합니다.
     */
    List<CommunityImage> findByCommunityId(Long communityId);
}