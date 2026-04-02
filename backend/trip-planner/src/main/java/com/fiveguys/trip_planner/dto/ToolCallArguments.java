package com.fiveguys.trip_planner.dto;

public class ToolCallArguments {

    private String destination;
    private Integer days;

    public ToolCallArguments() {
    }

    public ToolCallArguments(String destination, Integer days) {
        this.destination = destination;
        this.days = days;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }
}