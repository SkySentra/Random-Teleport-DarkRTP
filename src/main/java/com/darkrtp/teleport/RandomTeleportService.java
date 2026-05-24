package com.darkrtp.teleport;

import java.util.concurrent.CompletableFuture;

public interface RandomTeleportService {

    CompletableFuture<TeleportResult> teleport(TeleportRequest request);

    void shutdown();
}
