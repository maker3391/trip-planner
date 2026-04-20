import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/layout/Header";
import "./TripListPage.css";
import {
  useTrips,
  useGetTrip,
  useDeleteTrip,
} from "../components/hooks/useTrip.ts";
import { TripPlanResponse } from "../types/trip.ts";
import { useTripStore } from "../components/store/useTripStore.ts";

export default function TripListPage() {
  const [activeTab, setActiveTab] = useState<"MY" | "JOINED">("MY");
  const { data: tripList, isLoading, isError } = useTrips();
  const navigate = useNavigate();

  const {clearTripData} = useTripStore();

  const [selectedTripId, setSelectedTripId] = useState<number | null>(null);
  const deleteTripMutation = useDeleteTrip();

  const {
    data: selectedTrip,
    isLoading: isDetailLoading,
    isError: isDetailError,
  } = useGetTrip(selectedTripId);

  const handleTabChange = (tab: "MY" | "JOINED") => {
    setActiveTab(tab);
    setSelectedTripId(null);
  };

  const handleLoadTrip = (tripId: number) => {
    console.log("이동 시도, Trip ID:", tripId);
    navigate("/", { state: { tripId } });
  };

  const handleCreateTrip = () => {
    clearTripData();
    navigate("/");
  };

  const handleSelectTrip = (tripId: number) => {
    setSelectedTripId(tripId);
  };

  const handleCloseDetail = () => {
    setSelectedTripId(null);
  };

  const handleDeleteTrip = async () => {
    if (!selectedTripId) return;

    const confirmed = window.confirm("정말 이 여행 계획을 삭제하시겠습니까?");
    if (!confirmed) return;

    try {
      await deleteTripMutation.mutateAsync(selectedTripId);
      setSelectedTripId(null);
    } catch (error) {
      console.error(error);
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
        {/* 상단 소개 영역: 문구 변경 시 높이 변화를 방지하기 위해 최소 높이 유지 */}
        <section className="trip-list-intro">
          <span className="trip-list-badge">TRIP LIST</span>
          <h1 className="trip-list-title">여행 목록</h1>
          <p className="trip-list-description">
            {activeTab === "MY"
              ? "내가 만든 여행 계획들을 한눈에 확인할 수 있습니다."
              : "동료들과 함께하는 여행 계획들을 한눈에 확인할 수 있습니다."}
          </p>
        </section>

        <section className="trip-list-section">
          {/* 헤더: 탭 전환 시 상하좌우 떨림 방지 구조 */}
          <div className="trip-list-header fixed-header">
            <div className="trip-list-header-left">
              <div className="tab-group">
                <h2
                  className={`trip-tab ${activeTab === "MY" ? "active" : ""}`}
                  onClick={() => handleTabChange("MY")}
                >
                  내 여행 계획
                </h2>
                <h2
                  className={`trip-tab ${activeTab === "JOINED" ? "active" : ""}`}
                  onClick={() => handleTabChange("JOINED")}
                >
                  참가한 여행 계획
                </h2>
              </div>
              <span className="trip-list-count">
                총 {Array.isArray(tripList) ? tripList.length : 0}개
              </span>
            </div>

            <div className="trip-list-header-right">
              {activeTab === "MY" && (
                <button
                  type="button"
                  className="trip-create-button"
                  onClick={handleCreateTrip}
                >
                  + 새 여행 계획
                </button>
              )}
            </div>
          </div>

          {/* 콘텐츠 영역: 화면 전환 시 레이아웃 유지 */}
          <div className="tab-content-container">
            {activeTab === "MY" ? (
              <div className={`trip-list-layout ${selectedTripId ? "detail-open" : "detail-closed"}`}>
                <div className="trip-list-column">
                  {isLoading && <p>여행 데이터를 불러오는 중입니다... ✈️</p>}
                  {isError && <p>데이터를 불러오는데 실패했습니다. 🥲</p>}

                  {Array.isArray(tripList) ? (
                    tripList.length === 0 ? (
                      <div className="trip-empty-state">
                        <p className="trip-empty-text">아직 작성된 여행 계획이 없습니다.</p>
                      </div>
                    ) : (
                      <div className="trip-list-vertical">
                        {tripList.map((trip: TripPlanResponse) => (
                          <article
                            key={trip.id}
                            className={`trip-card ${selectedTripId === trip.id ? "selected" : ""}`}
                          >
                            <div className="trip-card-main">
                              <div className="trip-card-top">
                                <span className="trip-card-tag">{trip.destination}</span>
                                <span className="trip-card-status">{formatStatus(trip.status)}</span>
                              </div>
                              <h3 className="trip-card-title">{trip.title}</h3>
                              <div className="trip-card-info">
                                <p><span>여행 기간</span><strong>{trip.startDate} ~ {trip.endDate}</strong></p>
                              </div>
                            </div>
                            <div className="trip-card-actions">
                              <button className="trip-card-button load-btn" onClick={() => handleLoadTrip(trip.id)}>계획 불러오기</button>
                              <button className="trip-card-button detail-btn" onClick={() => handleSelectTrip(trip.id)}>상세보기 →</button>
                            </div>
                          </article>
                        ))}
                      </div>
                    )
                  ) : null}
                </div>

                {selectedTripId && (
                  <aside className="trip-detail-panel">
                    <div className="trip-detail-header-row">
                      <div className="trip-detail-header">
                        <span className="trip-detail-badge">DETAIL</span>
                        <h3 className="trip-detail-title">여행 상세보기</h3>
                      </div>
                      <div className="trip-detail-header-actions">
                        <button className="trip-detail-delete" onClick={handleDeleteTrip}>삭제</button>
                        <button className="trip-detail-close" onClick={handleCloseDetail}>닫기</button>
                      </div>
                    </div>
                    {selectedTrip && (
                      <div className="trip-detail-content">
                        <h4 className="trip-detail-name">{selectedTrip.title}</h4>
                        <div className="trip-detail-info">
                          <div className="trip-detail-item"><span>기간</span><strong>{selectedTrip.startDate} ~ {selectedTrip.endDate}</strong></div>
                        </div>
                      </div>
                    )}
                  </aside>
                )}
              </div>
            ) : (
              <div className="joined-trip-view">
                <div className="trip-empty-state joined-empty">
                  <h3>참가한 여행 계획이 없습니다.</h3>
                </div>
              </div>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}