// MyTripList.tsx
import { TripPlanResponse } from "../types/trip.ts";

interface MyTripListProps {
  tripList: TripPlanResponse[];
  selectedTripId: number | null;
  handleSelectTrip: (id: number) => void;
  handleLoadTrip: (id: number) => void;
  formatStatus: (status?: string) => string;
}

export default function MyTripList({ tripList, selectedTripId, handleSelectTrip, handleLoadTrip, formatStatus }: MyTripListProps) {
  if (tripList.length === 0) {
    return <div className="trip-empty-state"><p className="trip-empty-text">아직 작성된 여행 계획이 없습니다.</p></div>;
  }

  return (
    <div className="trip-list-vertical">
      {tripList.map((trip) => (
        <article key={trip.id} className={`trip-card ${selectedTripId === trip.id ? "selected" : ""}`}>
          <div className="trip-card-main">
            <div className="trip-card-top">
              <span className="trip-card-tag">{trip.destination}</span>
              <span className="trip-card-status">{formatStatus(trip.status)}</span>
            </div>
            <h3 className="trip-card-title">{trip.title}</h3>
            <div className="trip-card-info"><p><span>여행 기간</span><strong>{trip.startDate} ~ {trip.endDate}</strong></p></div>
          </div>
          <div className="trip-card-actions">
            <button type="button" className="trip-card-button load-btn" onClick={() => handleLoadTrip(trip.id)}>계획 불러오기</button>
            <button type="button" className="trip-card-button detail-btn" onClick={() => handleSelectTrip(trip.id)}>상세보기 →</button>
          </div>
        </article>
      ))}
    </div>
  );
}