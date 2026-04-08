// src/types/map.ts

export interface PlacePoint {
  lat: number;
  lng: number;
  name: string;
  address: string;
  placeId?: string;
  photos?: string[]; // 부활시킨 사진 데이터를 위한 필드
  memo?: string;
  isMemoOpen?: boolean;
}

export interface Connection {
  from: number;
  to: number;
}