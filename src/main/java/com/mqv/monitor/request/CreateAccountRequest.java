package com.mqv.monitor.request;

public record CreateAccountRequest(String username, String password, String firstName, String lastName) {
}
