import Header from "../components/layout/Header";
import "./TripListPage.css";

interface TripItem {
  id: number;
  title: string;
  destination: string;
  date: string;
  members: number;
  status: string;
}

export default function TripListPage() {
  const tripList: TripItem[] = [
    {
      id: 1,
      title: "부산 2박 3일 여행",
      destination: "부산",
      date: "2026.04.10 - 2026.04.12",
      members: 3,
      status: "계획 중",
    },
    {
      id: 2,
      title: "서울 당일치기",
      destination: "서울",
      date: "2026.04.18",
      members: 2,
      status: "확정",
    },
    {
      id: 3,
      title: "제주도 가족여행",
      destination: "제주",
      date: "2026.05.01 - 2026.05.04",
      members: 4,
      status: "계획 중",
    },
  ];

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
            <span className="trip-list-count">총 {tripList.length}개</span>
          </div>

          <div className="trip-list-grid">
            {tripList.map((trip) => (
              <article key={trip.id} className="trip-card">
                <div className="trip-card-top">
                  <span className="trip-card-tag">{trip.destination}</span>
                  <span className="trip-card-status">{trip.status}</span>
                </div>

                <h3 className="trip-card-title">{trip.title}</h3>

                <div className="trip-card-info">
                  <p>
                    <span>여행 기간</span>
                    <strong>{trip.date}</strong>
                  </p>
                  <p>
                    <span>인원</span>
                    <strong>{trip.members}명</strong>
                  </p>
                </div>

                <button type="button" className="trip-card-button">
                  상세보기
                </button>
              </article>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
}