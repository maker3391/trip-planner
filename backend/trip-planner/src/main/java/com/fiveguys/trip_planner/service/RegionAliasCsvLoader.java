package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.RegionAliasRecord;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class RegionAliasCsvLoader {

    private final List<RegionAliasRecord> aliasRecords = new ArrayList<>();

    @PostConstruct
    public void load() {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                new ClassPathResource("region_alias.csv").getInputStream(),
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            String line;
            boolean first = true;

            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }

                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                String[] tokens = line.split(",", -1);
                if (tokens.length < 7) {
                    continue;
                }

                aliasRecords.add(new RegionAliasRecord(
                        tokens[0].trim(),
                        tokens[1].trim(),
                        tokens[2].trim(),
                        tokens[3].trim(),
                        tokens[4].trim(),
                        tokens[5].trim(),
                        parsePriority(tokens[6].trim())
                ));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load region_alias.csv", e);
        }
    }

    private int parsePriority(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    public List<RegionAliasRecord> getAliasRecords() {
        return aliasRecords;
    }
}