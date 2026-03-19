package com.huefy.models;

import java.util.Map;

public record BulkRecipient(
    String email,
    String type,
    Map<String, Object> data
) {}
