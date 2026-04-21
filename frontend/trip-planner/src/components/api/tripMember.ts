import client from "./client";

export interface TripMemberResponse {
  memberId: number;
  userId: number;
  nickname: string;
  name: string;
  role: string;
}

export const requestJoinTrip = async (tripId: number) => {
  const response = await client.post(`/trips/${tripId}/members/join`);
  return response.data;
};

export const getTripMembers = async (tripId: number) => {
  const response = await client.get<TripMemberResponse[]>(`/trips/${tripId}/members`);
  return response.data;
};

export const acceptTripMember = async (tripId: number, memberId: number) => {
  const response = await client.patch(`/trips/${tripId}/members/${memberId}/accept`);
  return response.data;
};

export const removeTripMember = async (tripId: number, memberId: number) => {
  const response = await client.delete(`/trips/${tripId}/members/${memberId}`);
  return response.data;
};

export const getJoinedTrips = async () => {// 참가한 여행계획
  const response = await client.get("/trip-members/joined");
  return response.data;
};

export const leaveTripMember = async (tripId: number) => {
  const response = await client.delete(`/trips/${tripId}/members/me`);
  return response.data;
};