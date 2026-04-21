// JoinedTripList.tsx
import { TripPlanResponse } from "../types/trip.ts";

interface JoinedTripListProps {
  joinedTrips: TripPlanResponse[];
  selectedTripId: number | null;
  handleSelectTrip: (id: number) => void;
  handleLoadTrip: (id: number) => void;
  formatStatus: (status?: string) => string;
}

export default function JoinedTripList({ joinedTrips, selectedTripId, handleSelectTrip, handleLoadTrip, formatStatus }: JoinedTripListProps) {
  if (joinedTrips.length === 0) {
    return (
      <div className="trip-empty-state joined-empty">
        <p className="trip-empty-text">참가한 여행 계획이 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="trip-list-vertical">
      {joinedTrips.map((trip) => (
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
            <button className="trip-card-button load-btn" onClick={() => handleLoadTrip(trip.id)}>참여 계획 보기</button>
            <button className="trip-card-button detail-btn" onClick={() => handleSelectTrip(trip.id)}>상세보기 →</button>
          </div>
        </article>
      ))}
    </div>
  );
}