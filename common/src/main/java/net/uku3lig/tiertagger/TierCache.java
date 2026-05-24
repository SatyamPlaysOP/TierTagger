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

public class TierCache {

    private static final List<GameMode> GAMEMODES = new ArrayList<>();
    private static final Map<UUID, Optional<Map<String, PlayerInfo.Ranking>>> TIERS = new ConcurrentHashMap<>();

    public static void init() {
        GAMEMODES.clear();

        // ✅ USE PRIME TIERS GAME MODES
        GAMEMODES.addAll(GameMode.fetchFromPrimeTiers());

        TierTagger.getLogger().info("Loaded PrimeTiers gamemodes: {}", GAMEMODES.size());
    }

    public static List<GameMode> getGamemodes() {
        return GAMEMODES.isEmpty()
                ? Collections.singletonList(GameMode.NONE)
                : GAMEMODES;
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
                        .timeout(8000)
                        .get();

                Map<String, PlayerInfo.Ranking> rankings = new HashMap<>();

                // ⚠️ YOU MUST CONFIRM SELECTORS FROM YOUR WEBSITE
                Elements cards = doc.select(".ranking-card");

                for (Element card : cards) {

                    String mode = card.select(".mode-name").text().trim().toLowerCase();
                    String tier = card.select(".tier-value").text().trim();

                    if (mode.isEmpty() || tier.isEmpty()) continue;

                    rankings.put(mode, parseRanking(tier));
                }

                UUID uuid = UUID.nameUUIDFromBytes(username.getBytes());

                PlayerInfo info = new PlayerInfo(
                        uuid.toString(),
                        username,
                        rankings,
                        "AS",
                        0,
                        0,
                        new ArrayList<>(),
                        false
                );

                TIERS.put(uuid, Optional.of(rankings));

                return info;

            } catch (Exception e) {
                throw new RuntimeException("PrimeTiers fetch failed for: " + username, e);
            }
        });
    }

    private static PlayerInfo.Ranking parseRanking(String text) {
        try {
            text = text.toUpperCase().replace("TIER", "").trim();

            int tier = 10;
            int pos = 0;

            // safer parsing (handles "T1", "1", "1.2", etc.)
            String digits = text.replaceAll("[^0-9]", "");

            if (!digits.isEmpty()) {
                tier = Character.getNumericValue(digits.charAt(0));
                if (digits.length() > 1) {
                    pos = Character.getNumericValue(digits.charAt(1));
                }
            }

            return new PlayerInfo.Ranking(
                    tier,
                    pos,
                    null,
                    null,
                    System.currentTimeMillis(),
                    false
            );

        } catch (Exception e) {
            return new PlayerInfo.Ranking(
                    10,
                    0,
                    null,
                    null,
                    System.currentTimeMillis(),
                    false
            );
        }
    }

    public static void clearCache() {
        TIERS.clear();
    }

    public static GameMode findNextMode(GameMode current) {
        if (GAMEMODES.isEmpty()) return GameMode.NONE;

        int index = GAMEMODES.indexOf(current);
        return GAMEMODES.get((index + 1) % GAMEMODES.size());
    }

    public static Optional<GameMode> findMode(String id) {
        return GAMEMODES.stream()
                .filter(m -> m.id().equalsIgnoreCase(id))
                .findFirst();
    }

    public static GameMode findModeOrUgly(String id) {
        return findMode(id).orElseGet(() -> new GameMode(id, id));
    }

    private TierCache() {}
}
