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
      case "PLANNING":
        return "계획 중";
      case "COMPLETED":
        return "완료";
      case "CANCELLED":
        return "취소";
      default:
        return status;
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
            내가 만든 여행 계획들을 한눈에 확인할 수 있습니다.
          </p>
        </section>

        <section className="trip-list-section">
          <div className="trip-list-header">
            <div className="trip-list-header-left">
              <h2 className="trip-list-section-title">내 여행 계획</h2>
              <span className="trip-list-count">
                총 {Array.isArray(tripList) ? tripList.length : 0}개
              </span>
            </div>

            <button
              type="button"
              className="trip-create-button"
              onClick={handleCreateTrip}
            >
              + 새 여행 계획
            </button>
          </div>

          <div
            className={`trip-list-layout ${
              selectedTripId ? "detail-open" : "detail-closed"
            }`}
          >
            <div className="trip-list-column">
              {isLoading && <p>여행 데이터를 불러오는 중입니다... ✈️</p>}
              {isError && <p>데이터를 불러오는데 실패했습니다. 🥲</p>}

              {Array.isArray(tripList) ? (
                tripList.length === 0 ? (
                  <div className="trip-empty-state">
                    <p className="trip-empty-text">
                      아직 작성된 여행 계획이 없습니다.
                    </p>
                  </div>
                ) : (
                  <div className="trip-list-vertical">
                    {tripList.map((trip: TripPlanResponse) => (
                      <article
                        key={trip.id}
                        className={`trip-card ${
                          selectedTripId === trip.id ? "selected" : ""
                        }`}
                      >
                        <div className="trip-card-main">
                          <div className="trip-card-top">
                            <span className="trip-card-tag">
                              {trip.destination}
                            </span>
                            <span className="trip-card-status">
                              {formatStatus(trip.status)}
                            </span>
                          </div>

                          <h3 className="trip-card-title">{trip.title}</h3>

                          <div className="trip-card-info">
                            <p>
                              <span>여행 기간</span>
                              <strong>
                                {trip.startDate} ~ {trip.endDate}
                              </strong>
                            </p>
                          </div>
                        </div>

                        <div className="trip-card-actions">
                          <button
                            type="button"
                            className="trip-card-button load-btn"
                            onClick={() => handleLoadTrip(trip.id)}
                          >
                            계획 불러오기
                          </button>

                          <button
                            type="button"
                            className="trip-card-button detail-btn"
                            onClick={() => handleSelectTrip(trip.id)}
                          >
                            상세보기 <span className="detail-arrow">→</span>
                          </button>
                        </div>
                      </article>
                    ))}
                  </div>
                )
              ) : (
                !isLoading && (
                  <p style={{ color: "red" }}>
                    현재 로그인이 만료되었거나 데이터를 불러올 수 없습니다. 다시
                    로그인해 주세요.
                  </p>
                )
              )}
            </div>

            {selectedTripId && (
              <aside className="trip-detail-panel">
                <div className="trip-detail-header-row">
                  <div className="trip-detail-header">
                    <span className="trip-detail-badge">DETAIL</span>
                    <h3 className="trip-detail-title">여행 상세보기</h3>
                  </div>
                  <div className="trip-detail-header-actions">
                    <button
                      type="button"
                      className="trip-detail-delete"
                      onClick={handleDeleteTrip}
                      disabled={deleteTripMutation.isPending}
                    >
                      {deleteTripMutation.isPending ? "삭제 중..." : "삭제"}
                    </button>

                    <button
                      type="button"
                      className="trip-detail-close"
                      onClick={handleCloseDetail}
                    >
                      닫기
                    </button>
                  </div>
                </div>

                {isDetailLoading && (
                  <div className="trip-detail-empty">
                    <p>상세 정보를 불러오는 중입니다...</p>
                  </div>
                )}

                {isDetailError && (
                  <div className="trip-detail-empty">
                    <p>상세 정보를 불러오지 못했습니다.</p>
                  </div>
                )}

                {selectedTrip && !isDetailLoading && (
                  <div className="trip-detail-content">
                    <div className="trip-detail-top">
                      <span className="trip-detail-destination">
                        {selectedTrip.destination}
                      </span>
                      <span className="trip-detail-status">
                        {formatStatus(selectedTrip.status)}
                      </span>
                    </div>

                    <h4 className="trip-detail-name">{selectedTrip.title}</h4>

                    <div className="trip-detail-info">
                      <div className="trip-detail-item">
                        <span>여행 기간</span>
                        <strong>
                          {selectedTrip.startDate} ~ {selectedTrip.endDate}
                        </strong>
                      </div>

                      <div className="trip-detail-item">
                        <span>생성일</span>
                        <strong>
                          {selectedTrip.createdAt
                            ? selectedTrip.createdAt.slice(0, 10)
                            : "-"}
                        </strong>
                      </div>

                      <div className="trip-detail-item">
                        <span>일정 개수</span>
                        <strong>{selectedTrip.schedules?.length ?? 0}개</strong>
                      </div>
                    </div>

                    <div className="trip-detail-schedule-section">
                      <h5 className="trip-detail-subtitle">일정 목록</h5>

                      {selectedTrip.schedules &&
                      selectedTrip.schedules.length > 0 ? (
                        <ul className="trip-detail-schedule-list">
                          {selectedTrip.schedules.map((schedule) => (
                            <li
                              key={schedule.id}
                              className="trip-detail-schedule-item"
                            >
                              <div className="schedule-item-top">
                                <strong>
                                  DAY {schedule.dayNumber} · {schedule.title}
                                </strong>
                                <span>{schedule.visitOrder}번째 방문</span>
                              </div>

                              <div className="schedule-item-body">
                                <p>
                                  <span>장소</span>
                                  <strong>{schedule.placeName || "-"}</strong>
                                </p>
                                <p>
                                  <span>주소</span>
                                  <strong>{schedule.placeAddress || "-"}</strong>
                                </p>
                                <p>
                                  <span>시간</span>
                                  <strong>
                                    {schedule.startTime || "-"} ~{" "}
                                    {schedule.endTime || "-"}
                                  </strong>
                                </p>
                                <p>
                                  <span>메모</span>
                                  <strong>{schedule.memo || "-"}</strong>
                                </p>
                              </div>
                            </li>
                          ))}
                        </ul>
                      ) : (
                        <p className="trip-detail-no-schedule">
                          등록된 세부 일정이 없습니다.
                        </p>
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