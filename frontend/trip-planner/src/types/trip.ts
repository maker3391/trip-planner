export interface TripScheduleRequest {
  dayNumber: number;
  title: string;
  visitOrder: number;
  startTime?: string;
  endTime?: string;
  memo?: string;
  estimatedStayMinutes?: number;

  // 장소
  placeName?: string;
  placeAddress?: string;
  latitude?: number;
  longitude?: number;
  googlePlaceId?: string;
}

export interface TripPlanRequest {
  title: string;
  destination: string;
  startDate: string;
  endDate: string;
  schedules:  TripScheduleRequest[];
}

export interface TripScheduleResponse {
  id: number;
  dayNumber: number;
  title: string;
  visitOrder: number;
  startTime: string | null;
  endTime: string | null;
  memo: string | null;
  estimatedStayMinutes: number | null;

  // 장소
  placeName: string | null;
  placeAddress: string | null;
  latitude: number | null;
  longitude: number | null;
  googlePlaceId: string | null;
}

export interface TripPlanResponse {
  id: number;
  ownerId: number;
  title: string;
  destination: string;
  startDate: string;
  endDate: string;
  status: string;
  createdAt: string;
  schedules: TripScheduleResponse[];
}