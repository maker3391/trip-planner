import client from "./client";
import { CommunityRequest } from "../../types/community.ts";

export const createCommunityPost = async (data: CommunityRequest) => {
    return client.post("/community/posts", data);
};