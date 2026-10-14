package com.deepgram.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** @deprecated Use {@link SharedTopics#getSegments()}. */
@Deprecated
public final class SharedTopicsResults {
    private final Optional<SharedTopicsResultsTopics> topics;
    private final Map<String, Object> additionalProperties;

    private SharedTopicsResults(Optional<SharedTopicsResultsTopics> topics, Map<String, Object> additionalProperties) {
        this.topics = topics;
        this.additionalProperties = additionalProperties;
    }

    static SharedTopicsResults fromCurrent(List<SharedTopicsSegmentsItem> segments) {
        return new SharedTopicsResults(Optional.of(SharedTopicsResultsTopics.fromCurrent(segments)), new HashMap<>());
    }

    Optional<List<SharedTopicsSegmentsItem>> toCurrentSegments() {
        return topics.flatMap(SharedTopicsResultsTopics::toCurrentSegments);
    }

    public Optional<SharedTopicsResultsTopics> getTopics() { return topics; }
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof SharedTopicsResults && topics.equals(((SharedTopicsResults) other).topics);
    }

    @Override
    public int hashCode() { return Objects.hash(topics); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Optional<SharedTopicsResultsTopics> topics = Optional.empty();
        private final Map<String, Object> additionalProperties = new HashMap<>();

        private Builder() {}

        public Builder from(SharedTopicsResults other) { return topics(other.getTopics()); }
        public Builder topics(Optional<SharedTopicsResultsTopics> topics) { this.topics = topics; return this; }
        public Builder topics(SharedTopicsResultsTopics topics) { return topics(Optional.ofNullable(topics)); }
        public Builder additionalProperty(String key, Object value) { additionalProperties.put(key, value); return this; }
        public Builder additionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties.putAll(additionalProperties); return this; }

        public SharedTopicsResults build() { return new SharedTopicsResults(topics, additionalProperties); }
    }
}
