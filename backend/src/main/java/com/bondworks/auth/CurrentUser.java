package com.bondworks.auth;

import java.util.UUID;

public record CurrentUser(UUID id, String email, String name, String role) {}
