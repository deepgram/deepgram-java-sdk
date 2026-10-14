package com.deepgram.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** @deprecated Use {@link SharedTopicsSegmentsItemTopicsItem}. */
@Deprecated
public final class SharedTopicsResultsTopicsSegmentsItemTopicsItem {
    private final Optional<String> topic;
    private final Optional<Float> confidenceScore;
    private final Map<String, Object> additionalProperties;

    private SharedTopicsResultsTopicsSegmentsItemTopicsItem(
            Optional<String> topic, Optional<Float> confidenceScore, Map<String, Object> additionalProperties) {
        this.topic = topic;
        this.confidenceScore = confidenceScore;
        this.additionalProperties = additionalProperties;
    }

    static SharedTopicsResultsTopicsSegmentsItemTopicsItem fromCurrent(SharedTopicsSegmentsItemTopicsItem value) {
        return new SharedTopicsResultsTopicsSegmentsItemTopicsItem(
                value.getTopic(), value.getConfidenceScore(), value.getAdditionalProperties());
    }

    SharedTopicsSegmentsItemTopicsItem toCurrent() {
        return SharedTopicsSegmentsItemTopicsItem.builder()
                .topic(topic)
                .confidenceScore(confidenceScore)
                .additionalProperties(additionalProperties)
                .build();
    }

    public Optional<String> getTopic() {
        return topic;
    }

    public Optional<Float> getConfidenceScore() {
        return confidenceScore;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof SharedTopicsResultsTopicsSegmentsItemTopicsItem
                && equalTo((SharedTopicsResultsTopicsSegmentsItemTopicsItem) other);
    }

    private boolean equalTo(SharedTopicsResultsTopicsSegmentsItemTopicsItem other) {
        return topic.equals(other.topic) && confidenceScore.equals(other.confidenceScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, confidenceScore);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Optional<String> topic = Optional.empty();
        private Optional<Float> confidenceScore = Optional.empty();
        private final Map<String, Object> additionalProperties = new HashMap<>();

        private Builder() {}

        public Builder from(SharedTopicsResultsTopicsSegmentsItemTopicsItem other) {
            return topic(other.getTopic()).confidenceScore(other.getConfidenceScore());
        }

        public Builder topic(Optional<String> topic) {
            this.topic = topic;
            return this;
        }

        public Builder topic(String topic) {
            return topic(Optional.ofNullable(topic));
        }

        public Builder confidenceScore(Optional<Float> confidenceScore) {
            this.confidenceScore = confidenceScore;
            return this;
        }

        public Builder confidenceScore(Float confidenceScore) {
            return confidenceScore(Optional.ofNullable(confidenceScore));
        }

        public Builder additionalProperty(String key, Object value) {
            additionalProperties.put(key, value);
            return this;
        }

        public Builder additionalProperties(Map<String, Object> additionalProperties) {
            this.additionalProperties.putAll(additionalProperties);
            return this;
        }

        public SharedTopicsResultsTopicsSegmentsItemTopicsItem build() {
            return new SharedTopicsResultsTopicsSegmentsItemTopicsItem(topic, confidenceScore, additionalProperties);
        }
    }
}
