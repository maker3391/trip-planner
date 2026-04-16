package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.TripMemberResponse;
import com.fiveguys.trip_planner.entity.TripMember;
import com.fiveguys.trip_planner.entity.TripPlan;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.repository.TripMemberRepository;
import com.fiveguys.trip_planner.repository.TripPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripMemberService {

    private final TripPlanRepository tripPlanRepository;
    private final TripMemberRepository tripMemberRepository;

    @Transactional
    public void requestJoin(Long tripPlanId) {
        TripPlan tripPlan = getTripPlan(tripPlanId);
        User currentUser = getCurrentUser();

        if(tripPlan.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("방장은 참가 신청을 할 수 없습니다.");
        }

        if(tripMemberRepository.existsByTripPlanAndUser(tripPlan, currentUser)) {
            throw new IllegalArgumentException("이미 참가 신청을 했거나 참여 중인 여행입니다.");
        }

        TripMember tripMember = new TripMember();
        tripMember.setTripPlan(tripPlan);
        tripMember.setUser(currentUser);
        tripMember.setRole("PENDING");

        tripMemberRepository.save(tripMember);
    }

    public List<TripMemberResponse> getMembers(Long tripPlanId) {
        TripPlan tripPlan = getTripPlan(tripPlanId);

        return tripMemberRepository.findAllByTripPlan(tripPlan).stream()
                .map(TripMemberResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptJoin(Long tripPlanId, Long memberId) {
        TripPlan tripPlan = getTripPlan(tripPlanId);
        validateOwner(tripPlan);

        TripMember tripMember = tripMemberRepository.findByIdAndTripPlan(memberId, tripPlan)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 참가 신청입니다."));

        if(!"PENDING".equals(tripMember.getRole())) {
            throw new IllegalArgumentException("대기 중인 사용자만 수락할 수 있습니다.");
        }

        tripMember.setRole("MEMBER");
    }

    @Transactional
    public void removeMember(Long tripPlanId, Long memberId) {
        TripPlan tripPlan = getTripPlan(tripPlanId);
        validateOwner(tripPlan);

        TripMember tripMember = tripMemberRepository.findByIdAndTripPlan(memberId, tripPlan)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버입니다."));

        if("OWNER".equals(tripMember.getRole())) {
            throw new IllegalArgumentException("방장은 강퇴할 수 없습니다.");
        }

        tripMemberRepository.delete(tripMember);
    }

    private TripPlan getTripPlan(Long tripPlanId) {
        return tripPlanRepository.findById(tripPlanId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 여행입니다."));
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof User) {
            return (User)principal;
        }
        throw new RuntimeException("로그인 사용자 정보가 없습니다.");
    }

    private void validateOwner(TripPlan tripPlan) {
        User currentUser = getCurrentUser();
        if(!tripPlan.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("방장만 멤버를 관리할 수 있습니다.");
        }
    }
}
