package com.fiveguys.trip_planner.response;

import com.fiveguys.trip_planner.dto.ToolCallDto;

public class ChatResponse {

    private String originalMessage;
    private boolean executable;
    private ToolCallDto toolCall;

    public ChatResponse() {
    }

    public ChatResponse(String originalMessage, boolean executable, ToolCallDto toolCall) {
        this.originalMessage = originalMessage;
        this.executable = executable;
        this.toolCall = toolCall;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public boolean isExecutable() {
        return executable;
    }

    public ToolCallDto getToolCall() {
        return toolCall;
    }

    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    public void setToolCall(ToolCallDto toolCall) {
        this.toolCall = toolCall;
    }
}