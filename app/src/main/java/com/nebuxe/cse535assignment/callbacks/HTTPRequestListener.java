package com.nebuxe.cse535assignment.callbacks;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Response;

public interface HTTPRequestListener {
    void onFailure(@NotNull IOException e);

    void onResponse(boolean success, @NotNull Object object, @NotNull Response response);
}
