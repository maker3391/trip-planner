package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.RegionRecord;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class RegionCsvLoader {

    private final List<RegionRecord> regionRecords = new ArrayList<>();

    @PostConstruct
    public void load() {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                new ClassPathResource("region_master.csv").getInputStream(),
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

                String[] tokens = line.split(",", -1);
                if (tokens.length < 7) {
                    continue;
                }

                regionRecords.add(new RegionRecord(
                        tokens[0].trim(),
                        tokens[1].trim(),
                        tokens[2].trim(),
                        tokens[3].trim(),
                        tokens[4].trim(),
                        tokens[5].trim(),
                        tokens[6].trim()
                ));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load region_master.csv", e);
        }
    }

    public List<RegionRecord> getRegionRecords() {
        return regionRecords;
    }
}