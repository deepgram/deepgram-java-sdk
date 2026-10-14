package com.deepgram.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** @deprecated Use {@link SharedIntentsSegmentsItemIntentsItem}. */
@Deprecated
public final class SharedIntentsResultsIntentsSegmentsItemIntentsItem {
    private final Optional<String> intent;
    private final Optional<Float> confidenceScore;
    private final Map<String, Object> additionalProperties;

    private SharedIntentsResultsIntentsSegmentsItemIntentsItem(
            Optional<String> intent, Optional<Float> confidenceScore, Map<String, Object> additionalProperties) {
        this.intent = intent;
        this.confidenceScore = confidenceScore;
        this.additionalProperties = additionalProperties;
    }

    static SharedIntentsResultsIntentsSegmentsItemIntentsItem fromCurrent(SharedIntentsSegmentsItemIntentsItem value) {
        return new SharedIntentsResultsIntentsSegmentsItemIntentsItem(
                value.getIntent(), value.getConfidenceScore(), value.getAdditionalProperties());
    }

    SharedIntentsSegmentsItemIntentsItem toCurrent() {
        return SharedIntentsSegmentsItemIntentsItem.builder()
                .intent(intent)
                .confidenceScore(confidenceScore)
                .additionalProperties(additionalProperties)
                .build();
    }

    public Optional<String> getIntent() { return intent; }
    public Optional<Float> getConfidenceScore() { return confidenceScore; }
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof SharedIntentsResultsIntentsSegmentsItemIntentsItem
                && equalTo((SharedIntentsResultsIntentsSegmentsItemIntentsItem) other);
    }

    private boolean equalTo(SharedIntentsResultsIntentsSegmentsItemIntentsItem other) {
        return intent.equals(other.intent) && confidenceScore.equals(other.confidenceScore);
    }

    @Override
    public int hashCode() { return Objects.hash(intent, confidenceScore); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Optional<String> intent = Optional.empty();
        private Optional<Float> confidenceScore = Optional.empty();
        private final Map<String, Object> additionalProperties = new HashMap<>();

        private Builder() {}

        public Builder from(SharedIntentsResultsIntentsSegmentsItemIntentsItem other) {
            return intent(other.getIntent()).confidenceScore(other.getConfidenceScore());
        }

        public Builder intent(Optional<String> intent) { this.intent = intent; return this; }
        public Builder intent(String intent) { return intent(Optional.ofNullable(intent)); }
        public Builder confidenceScore(Optional<Float> confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public Builder confidenceScore(Float confidenceScore) { return confidenceScore(Optional.ofNullable(confidenceScore)); }
        public Builder additionalProperty(String key, Object value) { additionalProperties.put(key, value); return this; }
        public Builder additionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties.putAll(additionalProperties); return this; }

        public SharedIntentsResultsIntentsSegmentsItemIntentsItem build() {
            return new SharedIntentsResultsIntentsSegmentsItemIntentsItem(intent, confidenceScore, additionalProperties);
        }
    }
}
