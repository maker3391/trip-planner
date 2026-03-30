import { useQuery } from "@tanstack/react-query";
import client from "../api/client";

export const useTrips = () => {
  return useQuery({
    queryKey: ["trips"],
    queryFn: async () => {
      const response = await client.get("/trips");
      return response.data;
    },
    enabled: false,
  });
};