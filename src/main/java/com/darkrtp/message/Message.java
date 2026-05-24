package com.darkrtp.message;

public enum Message {

    PREFIX("prefix"),
    SEARCHING("searching"),
    WARMUP("warmup"),
    SUCCESS("success"),
    TELEPORTED_OTHER("teleported-other"),
    NO_LOCATION("no-location"),
    COOLDOWN("cooldown"),
    CANCELLED_MOVE("cancelled-move"),
    CANCELLED_DAMAGE("cancelled-damage"),
    ALREADY_TELEPORTING("already-teleporting"),
    WORLD_DISABLED("world-disabled"),
    NO_PERMISSION("no-permission"),
    PLAYERS_ONLY("players-only"),
    PLAYER_NOT_FOUND("player-not-found"),
    RELOADED("reloaded"),
    HELP("help");

    private final String key;

    Message(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
