package com.deepgram.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** @deprecated Use {@link SharedIntents#getSegments()}. */
@Deprecated
public final class SharedIntentsResults {
    private final Optional<SharedIntentsResultsIntents> intents;
    private final Map<String, Object> additionalProperties;

    private SharedIntentsResults(Optional<SharedIntentsResultsIntents> intents, Map<String, Object> additionalProperties) {
        this.intents = intents;
        this.additionalProperties = additionalProperties;
    }

    static SharedIntentsResults fromCurrent(List<SharedIntentsSegmentsItem> segments) {
        return new SharedIntentsResults(Optional.of(SharedIntentsResultsIntents.fromCurrent(segments)), new HashMap<>());
    }

    Optional<List<SharedIntentsSegmentsItem>> toCurrentSegments() {
        return intents.flatMap(SharedIntentsResultsIntents::toCurrentSegments);
    }

    public Optional<SharedIntentsResultsIntents> getIntents() { return intents; }
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof SharedIntentsResults && intents.equals(((SharedIntentsResults) other).intents);
    }

    @Override
    public int hashCode() { return Objects.hash(intents); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Optional<SharedIntentsResultsIntents> intents = Optional.empty();
        private final Map<String, Object> additionalProperties = new HashMap<>();

        private Builder() {}

        public Builder from(SharedIntentsResults other) { return intents(other.getIntents()); }
        public Builder intents(Optional<SharedIntentsResultsIntents> intents) { this.intents = intents; return this; }
        public Builder intents(SharedIntentsResultsIntents intents) { return intents(Optional.ofNullable(intents)); }
        public Builder additionalProperty(String key, Object value) { additionalProperties.put(key, value); return this; }
        public Builder additionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties.putAll(additionalProperties); return this; }

        public SharedIntentsResults build() { return new SharedIntentsResults(intents, additionalProperties); }
    }
}
