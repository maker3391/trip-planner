import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import client from "../api/client";
import { TripPlanRequest } from "../../types/trip";
import toast from "react-hot-toast"; // 1. toast 임포트

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
      toast.success("여행 계획이 저장되었습니다! 💾"); // alert -> toast
      queryClient.invalidateQueries({ queryKey: ["trips"] });
    },

    onError: (error) => {
      console.error("저장 실패.", error);
      toast.error("여행 계획 저장이 실패하였습니다. ❌"); // alert -> toast
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

// 특정 여행 계획 수정(업데이트) 훅
export const useUpdateTrip = (tripId: number | string | null) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (updatedTrip: TripPlanRequest) => {
      const response = await client.patch(`/trips/${tripId}`, updatedTrip);
      return response.data;
    },

    onSuccess: () => {
      toast.success("여행 계획이 수정되었습니다! ✨"); // alert -> toast
      queryClient.invalidateQueries({ queryKey: ["trips"] });
      queryClient.invalidateQueries({ queryKey: ["trips", tripId] });
    },

    onError: (error) => {
      console.error("수정 실패.", error);
      toast.error("여행 계획 수정에 실패하였습니다. ❌");
    },
  });
};

// 특정 여행 계획 삭제 훅
export const useDeleteTrip = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (tripId: number) => {
      await client.delete(`/trips/${tripId}`);
    },

    onSuccess: (_data, tripId) => {
      toast.success("여행 계획이 삭제되었습니다. 🗑️"); // alert -> toast
      queryClient.invalidateQueries({ queryKey: ["trips"] });
      queryClient.removeQueries({ queryKey: ["trips", tripId] });
    },

    onError: (error) => {
      console.error("삭제 실패.", error);
      toast.error("여행 계획 삭제에 실패하였습니다. ❌"); // alert -> toast
    },
  });
};