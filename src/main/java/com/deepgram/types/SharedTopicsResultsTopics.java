package com.deepgram.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/** @deprecated Use {@link SharedTopics#getSegments()}. */
@Deprecated
public final class SharedTopicsResultsTopics {
    private final Optional<List<SharedTopicsResultsTopicsSegmentsItem>> segments;
    private final Map<String, Object> additionalProperties;

    private SharedTopicsResultsTopics(
            Optional<List<SharedTopicsResultsTopicsSegmentsItem>> segments, Map<String, Object> additionalProperties) {
        this.segments = segments;
        this.additionalProperties = additionalProperties;
    }

    static SharedTopicsResultsTopics fromCurrent(List<SharedTopicsSegmentsItem> segments) {
        return new SharedTopicsResultsTopics(
                Optional.of(segments.stream()
                        .map(SharedTopicsResultsTopicsSegmentsItem::fromCurrent)
                        .collect(Collectors.toList())),
                new HashMap<>());
    }

    Optional<List<SharedTopicsSegmentsItem>> toCurrentSegments() {
        return segments.map(values -> values.stream()
                .map(SharedTopicsResultsTopicsSegmentsItem::toCurrent)
                .collect(Collectors.toList()));
    }

    public Optional<List<SharedTopicsResultsTopicsSegmentsItem>> getSegments() { return segments; }
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof SharedTopicsResultsTopics && segments.equals(((SharedTopicsResultsTopics) other).segments);
    }

    @Override
    public int hashCode() { return Objects.hash(segments); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Optional<List<SharedTopicsResultsTopicsSegmentsItem>> segments = Optional.empty();
        private final Map<String, Object> additionalProperties = new HashMap<>();

        private Builder() {}

        public Builder from(SharedTopicsResultsTopics other) { return segments(other.getSegments()); }
        public Builder segments(Optional<List<SharedTopicsResultsTopicsSegmentsItem>> segments) { this.segments = segments; return this; }
        public Builder segments(List<SharedTopicsResultsTopicsSegmentsItem> segments) { return segments(Optional.ofNullable(segments)); }
        public Builder additionalProperty(String key, Object value) { additionalProperties.put(key, value); return this; }
        public Builder additionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties.putAll(additionalProperties); return this; }

        public SharedTopicsResultsTopics build() { return new SharedTopicsResultsTopics(segments, additionalProperties); }
    }
}
