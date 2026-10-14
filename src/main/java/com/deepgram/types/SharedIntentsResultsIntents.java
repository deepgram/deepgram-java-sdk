package com.deepgram.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/** @deprecated Use {@link SharedIntents#getSegments()}. */
@Deprecated
public final class SharedIntentsResultsIntents {
    private final Optional<List<SharedIntentsResultsIntentsSegmentsItem>> segments;
    private final Map<String, Object> additionalProperties;

    private SharedIntentsResultsIntents(
            Optional<List<SharedIntentsResultsIntentsSegmentsItem>> segments, Map<String, Object> additionalProperties) {
        this.segments = segments;
        this.additionalProperties = additionalProperties;
    }

    static SharedIntentsResultsIntents fromCurrent(List<SharedIntentsSegmentsItem> segments) {
        return new SharedIntentsResultsIntents(
                Optional.of(segments.stream()
                        .map(SharedIntentsResultsIntentsSegmentsItem::fromCurrent)
                        .collect(Collectors.toList())),
                new HashMap<>());
    }

    Optional<List<SharedIntentsSegmentsItem>> toCurrentSegments() {
        return segments.map(values -> values.stream()
                .map(SharedIntentsResultsIntentsSegmentsItem::toCurrent)
                .collect(Collectors.toList()));
    }

    public Optional<List<SharedIntentsResultsIntentsSegmentsItem>> getSegments() { return segments; }
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof SharedIntentsResultsIntents && segments.equals(((SharedIntentsResultsIntents) other).segments);
    }

    @Override
    public int hashCode() { return Objects.hash(segments); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Optional<List<SharedIntentsResultsIntentsSegmentsItem>> segments = Optional.empty();
        private final Map<String, Object> additionalProperties = new HashMap<>();

        private Builder() {}

        public Builder from(SharedIntentsResultsIntents other) { return segments(other.getSegments()); }
        public Builder segments(Optional<List<SharedIntentsResultsIntentsSegmentsItem>> segments) { this.segments = segments; return this; }
        public Builder segments(List<SharedIntentsResultsIntentsSegmentsItem> segments) { return segments(Optional.ofNullable(segments)); }
        public Builder additionalProperty(String key, Object value) { additionalProperties.put(key, value); return this; }
        public Builder additionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties.putAll(additionalProperties); return this; }

        public SharedIntentsResultsIntents build() { return new SharedIntentsResultsIntents(segments, additionalProperties); }
    }
}
