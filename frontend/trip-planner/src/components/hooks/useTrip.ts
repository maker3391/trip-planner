import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import client from "../api/client";
import { TripPlanRequest } from "../../types/trip";

/* 여행 목록 데이터를 가져오는 커스텀 훅 */
export const useTrips = () => {
  return useQuery({
    queryKey: ["trips"],
    queryFn: async () => {
      const response = await client.get("/trips");
      return response.data;
    },
  });
};

// 여행 계획 생성(저장)
export const useCreateTrip = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (newTrip: TripPlanRequest) => {
      const response = await client.post("/trips", newTrip);
      return response.data;
    },

    onSuccess: () => {
      alert("여행 계획이 저장되었습니다!");
      queryClient.invalidateQueries({ queryKey: ["trips"] });
    },

    onError: (error) => {
      console.error("저장 실패.", error);
      alert("여행 계획 저장이 실패하였습니다.");
    },
  });
};

// 특정 여행 상세 조회 훅
export const useGetTrip = (tripId: number | string | null) => {
  return useQuery({
    queryKey: ["trips", tripId],
    queryFn: async () => {
      const response = await client.get(`/trips/${tripId}`);
      return response.data;
    },
    enabled: !!tripId,
  });
};