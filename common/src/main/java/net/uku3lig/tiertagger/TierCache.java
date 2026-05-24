package net.uku3lig.tiertagger;

import net.uku3lig.tiertagger.model.GameMode;
import net.uku3lig.tiertagger.model.PlayerInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class TierCache {
    private static final List<GameMode> GAMEMODES = new ArrayList<>();
    private static final Map<UUID, Optional<Map<String, PlayerInfo.Ranking>>> TIERS = new ConcurrentHashMap<>();

    public static void init() {
        GAMEMODES.clear();

        // Custom PrimeTiers gamemodes
        GAMEMODES.add(new GameMode("nodebuff", "Nodebuff"));
        GAMEMODES.add(new GameMode("uhc", "UHC"));
        GAMEMODES.add(new GameMode("sword", "Sword"));
        GAMEMODES.add(new GameMode("axe", "Axe"));
        GAMEMODES.add(new GameMode("crystal", "Crystal"));

        TierTagger.getLogger().info("Loaded PrimeTiers gamemodes!");
    }

    public static List<GameMode> getGamemodes() {
        if (GAMEMODES.isEmpty()) {
            return Collections.singletonList(GameMode.NONE);
        } else {
            return GAMEMODES;
        }
    }

    public static Optional<Map<String, PlayerInfo.Ranking>> getPlayerRankings(UUID uuid) {
        return TIERS.getOrDefault(uuid, Optional.empty());
    }

    public static CompletableFuture<PlayerInfo> searchPlayer(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Document doc = Jsoup.connect(
                        "https://primetiers.qzz.io/player/" + username)
                        .userAgent("Mozilla/5.0")
                        .get();

                Map<String, PlayerInfo.Ranking> rankings = new HashMap<>();

                // CHANGE THESE SELECTORS TO MATCH YOUR WEBSITE HTML
                Elements cards = doc.select(".ranking-card");

                for (Element card : cards) {
                    String mode = card.select(".mode-name").text();
                    String tier = card.select(".tier-value").text();

                    if (!mode.isEmpty() && !tier.isEmpty()) {
                        rankings.put(mode.toLowerCase(), parseRanking(tier));
                    }
                }

                UUID uuid = UUID.nameUUIDFromBytes(username.getBytes());

                PlayerInfo info = new PlayerInfo(
                        uuid.toString(),
                        username,
                        rankings
                );

                TIERS.put(uuid, Optional.of(rankings));

                return info;

            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch PrimeTiers player data", e);
            }
        });
    }

    private static PlayerInfo.Ranking parseRanking(String tierText) {
        // Adjust this constructor if your Ranking class is different
        return new PlayerInfo.Ranking(tierText, 0, false);
    }

    public static void clearCache() {
        TIERS.clear();
    }

    public static GameMode findNextMode(GameMode current) {
        if (GAMEMODES.isEmpty()) {
            return GameMode.NONE;
        } else {
            return GAMEMODES.get((GAMEMODES.indexOf(current) + 1) % GAMEMODES.size());
        }
    }

    public static Optional<GameMode> findMode(String id) {
        return GAMEMODES.stream().filter(m -> m.id().equalsIgnoreCase(id)).findFirst();
    }

    public static GameMode findModeOrUgly(String id) {
        return findMode(id).orElseGet(() -> new GameMode(id, id));
    }

    private static UUID parseUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (Exception e) {
            long mostSignificant = Long.parseUnsignedLong(uuid.substring(0, 16), 16);
            long leastSignificant = Long.parseUnsignedLong(uuid.substring(16), 16);
            return new UUID(mostSignificant, leastSignificant);
        }
    }

    private TierCache() {
    }
}
