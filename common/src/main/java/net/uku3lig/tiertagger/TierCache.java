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

                // Replace selectors with real website classes
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
                throw new RuntimeException("Failed to fetch PrimeTiers player data", e);
            }
        });
    }

    private static PlayerInfo.Ranking parseRanking(String tierText) {
        try {
            tierText = tierText.toUpperCase()
                    .replace("HT", "")
                    .replace("LT", "")
                    .replace("TIER", "")
                    .trim();

            int tier = 10;

            if (!tierText.isEmpty()) {
                tier = Integer.parseInt(tierText.substring(0, 1));
            }

            int pos = 0;

            if (tierText.length() > 1) {
                char c = tierText.charAt(1);

                if (Character.isDigit(c)) {
                    pos = Character.getNumericValue(c);
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
        if (GAMEMODES.isEmpty()) {
            return GameMode.NONE;
        } else {
            return GAMEMODES.get((GAMEMODES.indexOf(current) + 1) % GAMEMODES.size());
        }
    }

    public static Optional<GameMode> findMode(String id) {
        return GAMEMODES.stream()
                .filter(m -> m.id().equalsIgnoreCase(id))
                .findFirst();
    }

    public static GameMode findModeOrUgly(String id) {
        return findMode(id).orElseGet(() -> new GameMode(id, id));
    }

    private TierCache() {
    }
}
