export interface SearchPlace {
  placeId?: string;
  name: string;
  address: string;
  lat: number;
  lng: number;
  photos?: string[];
}