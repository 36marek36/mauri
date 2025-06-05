package com.example.mauri.controller.request;

import lombok.NonNull;

public record CreateUser(
        @NonNull
        String username,

        @NonNull
        String password
) {
}
