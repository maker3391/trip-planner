import Header from "../components/layout/Header";
import "./TripListPage.css";
import { useTrips } from "../components/hooks/useTrip.ts";
import { TripPlanResponse } from "../types/trip.ts";

export default function TripListPage() {
  const { data: tripList, isLoading, isError } = useTrips();

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
            <h2 className="trip-list-section-title">내 여행 계획</h2>
            <span className="trip-list-count">
              총 {Array.isArray(tripList) ? tripList.length : 0}개
            </span>
          </div>

          <div className="trip-list-grid">
            {isLoading && <p>여행 데이터를 불러오는 중입니다... ✈️</p>}
            {isError && <p>데이터를 불러오는데 실패했습니다. 🥲</p>}

            {Array.isArray(tripList) ? (
              tripList.length === 0 ? (
                <p>아직 작성된 여행 계획이 없습니다.</p>
              ) : (
                tripList.map((trip: TripPlanResponse) => (
                  <article key={trip.id} className="trip-card">
                    <div className="trip-card-top">
                      <span className="trip-card-tag">{trip.destination}</span>
                      <span className="trip-card-status">
                        {trip.status || "계획 중"}
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

                    <button type="button" className="trip-card-button">
                      상세보기
                    </button>
                  </article>
                ))
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
        </section>
      </main>
    </div>
  );
}