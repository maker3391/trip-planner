import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import client from "../api/client"; // Axios나 Fetch 설정이 포함된 API 클라이언트
import { TripPlanRequest } from "../../types/trip";

/* 여행 목록 데이터를 가져오는 커스텀 훅 */
export const useTrips = () => {
  return useQuery({
    // [queryKey]: 캐시를 관리하기 위한 고유 키. 
    // 이 키를 통해 데이터의 캐싱, 무효화(refetch), 공유가 이루어집니다.
    queryKey: ["trips"],
    
    // [queryFn]: 실제로 데이터를 비동기로 호출하는 함수.
    // client.get("/trips")를 통해 서버 데이터를 가져오고 응답의 data 부분을 반환합니다.
    queryFn: async () => {
      const response = await client.get("/trips");
      return response.data;
    },

    // [enabled]: 쿼리 자동 실행 여부.
    // 현재 false로 설정되어 있어 컴포넌트가 마운트될 때 자동으로 API를 호출하지 않습니다.
    // 주로 특정 버튼을 클릭했을 때(refetch() 호출 시)만 실행하고 싶을 때 사용합니다.
    enabled: false,
  });
};

// 여행 계획 생성(저장)
export const useCreateTrip = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (newTrip : TripPlanRequest) => {
      const response = await client.post("/trips", newTrip);
      return response.data;
    },

    onSuccess: () => {
      alert("여행 계획이 저장되었습니다!")
      queryClient.invalidateQueries({queryKey: ["trips"]});
    },

    onError: (error) => {
      console.error("저장 실패.", error);
      alert("여행 계획 저장이 실패하였습니다.");
    }
  });
}