package com.fiveguys.trip_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fiveguys.trip_planner.client.KakaoLocalClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AttractionDocumentCollector {

    private static final int MAX_COLLECT_SIZE = 40;

    private final KakaoLocalClient kakaoLocalClient;

    public AttractionDocumentCollector(KakaoLocalClient kakaoLocalClient) {
        this.kakaoLocalClient = kakaoLocalClient;
    }

    public List<JsonNode> collectDocuments(List<String> queries) {
        List<JsonNode> collectedDocs = new ArrayList<>();

        for (String query : queries) {
            JsonNode root = kakaoLocalClient.searchKeyword(query);
            List<JsonNode> docs = extractDocuments(root);

            if (!docs.isEmpty()) {
                collectedDocs.addAll(docs);
            }

            if (collectedDocs.size() >= MAX_COLLECT_SIZE) {
                break;
            }
        }

        return collectedDocs;
    }

    private List<JsonNode> extractDocuments(JsonNode root) {
        List<JsonNode> result = new ArrayList<>();
        JsonNode docs = root.path("documents");

        if (!docs.isArray()) {
            return result;
        }

        for (JsonNode doc : docs) {
            result.add(doc);
        }

        return result;
    }
}