package ch.minenox.livingpaths.debug;

public final class DebugHudState {
    private static volatile DebugHudPayload latest;

    private DebugHudState() {
    }

    public static void update(DebugHudPayload payload) {
        latest = payload;
    }

    public static DebugHudPayload latest() {
        return latest;
    }

    public static void clear() {
        latest = null;
    }
}
