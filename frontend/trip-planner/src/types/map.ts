export interface PlacePoint {
  lat: number;
  lng: number;
  name: string;
  address: string;
  placeId?: string;
  photos?: string[];
  memo?: string;
  isMemoOpen?: boolean;
  customTitle?: string;
  dayNumber?: number;
  startTime?: string;
  endTime?: string;
  estimatedStayMinutes?: number;
}

export interface Connection {
  from: number;
  to: number;
}