import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import Header from "../components/layout/Header";
import "./TripListPage.css";
import { useTrips, useGetTrip, useDeleteTrip } from "../components/hooks/useTrip.ts";
import { TripPlanResponse } from "../types/trip.ts";
import { useTripStore } from "../components/store/useTripStore.ts";

import MyTripList from "./MyTripList";
import JoinedTripList from "./JoinedTripList";

export default function TripListPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { clearTripData } = useTripStore();
  const [activeTab, setActiveTab] = useState<"MY" | "JOINED">("MY");
  const [joinedTrips, setJoinedTrips] = useState<TripPlanResponse[]>([]);

  const { data: tripList, isLoading, isError } = useTrips();
  const [selectedTripId, setSelectedTripId] = useState<number | null>(null);
  
  // 삭제 뮤테이션 복구
  const deleteTripMutation = useDeleteTrip();

  const { data: selectedTrip, isLoading: isDetailLoading, isError: isDetailError } = useGetTrip(selectedTripId);

  useEffect(() => {
  // CommunityReadPage에서 보낸 joinedTrip 데이터가 있는지 확인
    if (location.state?.joinedTrip) {
      const newTrip = location.state.joinedTrip;
      setJoinedTrips((prev) => {
        // 이미 리스트에 있는 여행인지 중복 체크
        const isExisted = prev.find((t) => t.id === newTrip.id);
        if (isExisted) return prev;
        
        // 기존 참가 리스트에 새 여행 계획 추가
        return [...prev, newTrip];
      });

      // 탭을 '참가한 여행 계획'으로 즉시 전환
      setActiveTab("JOINED");

      // 주소창의 state를 비워주어 새로고침 시 중복 추가되는 현상 방지
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

  const handleTabChange = (tab: "MY" | "JOINED") => {
    setActiveTab(tab);
    setSelectedTripId(null);
  };

  const handleLoadTrip = (tripId: number) => navigate("/", { state: { tripId } });
  const handleSelectTrip = (tripId: number) => setSelectedTripId(tripId);
  const handleCloseDetail = () => setSelectedTripId(null);

  // 삭제 함수 복구
  const handleDeleteTrip = async () => {
    if (!selectedTripId) return;
    const confirmed = window.confirm("정말 이 여행 계획을 삭제하시겠습니까?");
    if (!confirmed) return;

    try {
      await deleteTripMutation.mutateAsync(selectedTripId);
      setSelectedTripId(null); // 삭제 후 상세창 닫기
    } catch (error) {
      console.error("삭제 실패:", error);
    }
  };

  const formatStatus = (status?: string) => {
    if (!status) return "계획 중";
    switch (status) {
      case "PLANNING": return "계획 중";
      case "COMPLETED": return "완료";
      case "CANCELLED": return "취소";
      default: return status;
    }
  };

  return (
    <div className="trip-list-page">
      <Header />
      <main className="trip-list-body">
        <section className="trip-list-intro">
          <span className="trip-list-badge">TRIP LIST</span>
          <h1 className="trip-list-title">여행 목록</h1>
          <p className="trip-list-description">
            {activeTab === "MY" ? "내가 만든 여행 계획들입니다." : "참가 신청한 여행 계획들입니다."}
          </p>
        </section>

        <section className="trip-list-section">
          <div className="trip-list-header fixed-header">
            <div className="tab-group">
              <h2 className={`trip-tab ${activeTab === "MY" ? "active" : ""}`} onClick={() => handleTabChange("MY")}>내 여행 계획</h2>
              <h2 className={`trip-tab ${activeTab === "JOINED" ? "active" : ""}`} onClick={() => handleTabChange("JOINED")}>참가한 여행 계획</h2>
            </div>
            <span className="trip-list-count">
              총 {activeTab === "MY" ? (tripList?.length || 0) : joinedTrips.length}개
            </span>
          </div>

          <div className={`trip-list-layout ${selectedTripId ? "detail-open" : "detail-closed"}`}>
            <div className="trip-list-column">
              {activeTab === "MY" ? (
                <MyTripList 
                  tripList={tripList || []} 
                  selectedTripId={selectedTripId} 
                  handleSelectTrip={handleSelectTrip} 
                  handleLoadTrip={handleLoadTrip} 
                  formatStatus={formatStatus} 
                />
              ) : (
                <JoinedTripList 
                  joinedTrips={joinedTrips} 
                  selectedTripId={selectedTripId} 
                  handleSelectTrip={handleSelectTrip} 
                  handleLoadTrip={handleLoadTrip} 
                  formatStatus={formatStatus} 
                />
              )}
            </div>

            {/* 상세보기 패널 및 삭제 버튼 복구 */}
            {selectedTripId && (
              <aside className="trip-detail-panel">
                <div className="trip-detail-header-row">
                  <div className="trip-detail-header">
                    <span className="trip-detail-badge">DETAIL</span>
                    <h3 className="trip-detail-title">여행 상세보기</h3>
                  </div>
                  <div className="trip-detail-header-actions">
                    {/* 내 계획 탭일 때만 삭제 버튼 노출 (참가한 계획은 삭제가 아닌 탈퇴 개념이므로) */}
                    {activeTab === "MY" && (
                      <button 
                        type="button" 
                        className="trip-detail-delete" 
                        onClick={handleDeleteTrip}
                        disabled={deleteTripMutation.isPending}
                      >
                        {deleteTripMutation.isPending ? "삭제 중..." : "삭제"}
                      </button>
                    )}
                    <button type="button" className="trip-detail-close" onClick={handleCloseDetail}>닫기</button>
                  </div>
                </div>

                {isDetailLoading && <div className="trip-detail-empty"><p>상세 정보를 불러오는 중입니다...</p></div>}
                {isDetailError && <div className="trip-detail-empty"><p>상세 정보를 불러오지 못했습니다.</p></div>}

                {selectedTrip && !isDetailLoading && (
                  <div className="trip-detail-content">
                    <div className="trip-detail-top">
                      <span className="trip-detail-destination">{selectedTrip.destination}</span>
                      <span className="trip-detail-status">{formatStatus(selectedTrip.status)}</span>
                    </div>
                    <h4 className="trip-detail-name">{selectedTrip.title}</h4>
                    <div className="trip-detail-info">
                      <div className="trip-detail-item"><span>여행 기간</span><strong>{selectedTrip.startDate} ~ {selectedTrip.endDate}</strong></div>
                      <div className="trip-detail-item"><span>생성일</span><strong>{selectedTrip.createdAt?.slice(0, 10) || "-"}</strong></div>
                      <div className="trip-detail-item"><span>일정 개수</span><strong>{selectedTrip.schedules?.length ?? 0}개</strong></div>
                    </div>

                    <div className="trip-detail-schedule-section">
                      <h5 className="trip-detail-subtitle">일정 목록</h5>
                      {selectedTrip.schedules && selectedTrip.schedules.length > 0 ? (
                        <ul className="trip-detail-schedule-list">
                          {selectedTrip.schedules.map((schedule: any) => (
                            <li key={schedule.id} className="trip-detail-schedule-item">
                              <div className="schedule-item-top">
                                <strong>DAY {schedule.dayNumber} · {schedule.title}</strong>
                                <span>{schedule.visitOrder}번째 방문</span>
                              </div>
                              <div className="schedule-item-body">
                                <p><span>장소</span><strong>{schedule.placeName || "-"}</strong></p>
                                <p><span>주소</span><strong>{schedule.placeAddress || "-"}</strong></p>
                                <p><span>시간</span><strong>{schedule.startTime || "-"} ~ {schedule.endTime || "-"}</strong></p>
                                <p><span>메모</span><strong>{schedule.memo || "-"}</strong></p>
                              </div>
                            </li>
                          ))}
                        </ul>
                      ) : (
                        <p className="trip-detail-no-schedule">등록된 세부 일정이 없습니다.</p>
                      )}
                    </div>
                  </div>
                )}
              </aside>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}