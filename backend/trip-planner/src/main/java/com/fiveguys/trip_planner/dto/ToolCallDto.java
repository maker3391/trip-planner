package com.fiveguys.trip_planner.dto;

public class ToolCallDto {

    private String type;
    private String tool;
    private ToolCallArguments arguments;

    public ToolCallDto() {
    }

    public ToolCallDto(String type, String tool, ToolCallArguments arguments) {
        this.type = type;
        this.tool = tool;
        this.arguments = arguments;
    }

    public String getType() {
        return type;
    }

    public String getTool() {
        return tool;
    }

    public ToolCallArguments getArguments() {
        return arguments;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public void setArguments(ToolCallArguments arguments) {
        this.arguments = arguments;
    }
}