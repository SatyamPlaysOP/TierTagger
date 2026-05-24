package net.uku3lig.tiertagger.model;

import com.google.gson.annotations.SerializedName;
import net.uku3lig.tiertagger.TierCache;
import net.uku3lig.tiertagger.TierTagger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public record PlayerInfo(
        String uuid,
        String name,
        Map<String, Ranking> rankings,
        String region,
        int points,
        int overall,
        List<Badge> badges,
        @SerializedName("combat_master") boolean combatMaster
) {

    public record Ranking(
            int tier,
            int pos,
            @Nullable @SerializedName("peak_tier") Integer peakTier,
            @Nullable @SerializedName("peak_pos") Integer peakPos,
            long attained,
            boolean retired
    ) {

        public int comparableTier() {
            return tier * 2 + pos;
        }

        public int comparablePeak() {
            if (peakTier == null || peakPos == null) {
                return Integer.MAX_VALUE;
            }
            return peakTier * 2 + peakPos;
        }

        public NamedRanking asNamed(GameMode mode) {
            return new NamedRanking(mode, this);
        }
    }

    public record NamedRanking(@Nullable GameMode mode, Ranking ranking) {}

    public record Badge(String title, String desc) {}

    private static final Map<String, Integer> REGION_COLORS = Map.of(
            "NA", 0xff6a6e,
            "EU", 0x6aff6e,
            "SA", 0xff9900,
            "AU", 0xf6b26b,
            "ME", 0xffd966,
            "AS", 0xc27ba0,
            "AF", 0x674ea7
    );

    // ----------------------------
    // ❌ OLD API METHODS REMOVED
    // ----------------------------

    public static CompletableFuture<PlayerInfo> get(HttpClient client, UUID uuid) {
        throw new UnsupportedOperationException("PrimeTiers mode does not use API get()");
    }

    public static CompletableFuture<Map<String, Ranking>> getRankings(HttpClient client, UUID uuid) {
        return CompletableFuture.completedFuture(
                TierCache.getPlayerRankings(uuid)
                        .orElse(Collections.emptyMap())
        );
    }

    public static CompletableFuture<PlayerInfo> search(HttpClient client, String query) {
        return TierCache.searchPlayer(query);
    }

    // ----------------------------

    public int getRegionColor() {
        return REGION_COLORS.getOrDefault(this.region.toUpperCase(Locale.ROOT), 0xffffff);
    }

    public static Optional<NamedRanking> getHighestRanking(Map<String, Ranking> rankings) {
        return rankings.entrySet().stream()
                .filter(e -> e.getKey() != null)
                .min(Comparator.comparingInt(e -> e.getValue().comparableTier()))
                .map(e -> e.getValue().asNamed(TierCache.findModeOrUgly(e.getKey())));
    }

    @Getter
    @AllArgsConstructor
    public enum PointInfo {
        COMBAT_GRANDMASTER("Combat Grandmaster", 0xE6C622, 0xFDE047),
        COMBAT_MASTER("Combat Master", 0xFBB03B, 0xFFD13A),
        COMBAT_ACE("Combat Ace", 0xCD285C, 0xD65474),
        COMBAT_SPECIALIST("Combat Specialist", 0xAD78D8, 0xC7A3E8),
        COMBAT_CADET("Combat Cadet", 0x9291D9, 0xADACE2),
        COMBAT_NOVICE("Combat Novice", 0x9291D9, 0xFFFFFF),
        ROOKIE("Rookie", 0x6C7178, 0x8B979C),
        UNRANKED("Unranked", 0xFFFFFF, 0xFFFFFF);

        private final String title;
        private final int color;
        private final int accentColor;
    }

    public PointInfo getPointInfo() {
        if (this.points >= 400) return PointInfo.COMBAT_GRANDMASTER;
        if (this.points >= 250) return PointInfo.COMBAT_MASTER;
        if (this.points >= 100) return PointInfo.COMBAT_ACE;
        if (this.points >= 50) return PointInfo.COMBAT_SPECIALIST;
        if (this.points >= 20) return PointInfo.COMBAT_CADET;
        if (this.points >= 10) return PointInfo.COMBAT_NOVICE;
        if (this.points >= 1) return PointInfo.ROOKIE;
        return PointInfo.UNRANKED;
    }

    public List<NamedRanking> getSortedTiers() {
        List<NamedRanking> tiers = new ArrayList<>(
                this.rankings.entrySet().stream()
                        .map(e -> e.getValue().asNamed(TierCache.findModeOrUgly(e.getKey())))
                        .toList()
        );

        tiers.sort(Comparator
                .comparing((NamedRanking a) -> a.ranking.retired, Boolean::compare)
                .thenComparingInt(a -> a.ranking.tier)
                .thenComparingInt(a -> a.ranking.pos)
        );

        return tiers;
    }
}
