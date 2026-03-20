package org.example.kidsmathapp.entity.enums;

public enum RankLevel {
    STARTER(0, "⭐", "Starter"),
    EXPLORER(100, "🌟", "Explorer"),
    CHAMPION(500, "💫", "Champion"),
    WIZARD(1000, "✨", "Wizard"),
    LEGEND(2000, "👑", "Legend");

    private final int minStars;
    private final String emoji;
    private final String displayName;

    RankLevel(int minStars, String emoji, String displayName) {
        this.minStars = minStars;
        this.emoji = emoji;
        this.displayName = displayName;
    }

    public int getMinStars() { return minStars; }
    public String getEmoji() { return emoji; }
    public String getDisplayName() { return displayName; }

    public static RankLevel fromStars(int stars) {
        RankLevel result = STARTER;
        for (RankLevel level : values()) {
            if (stars >= level.minStars) result = level;
        }
        return result;
    }

    public int getNextLevelStars() {
        RankLevel[] values = values();
        for (int i = 0; i < values.length - 1; i++) {
            if (values[i] == this) return values[i + 1].minStars;
        }
        return minStars; // already LEGEND
    }
}
