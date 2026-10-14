package com.deepgram.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/** @deprecated Use {@link SharedIntentsSegmentsItem}. */
@Deprecated
public final class SharedIntentsResultsIntentsSegmentsItem {
    private final Optional<String> text;
    private final Optional<Double> startWord;
    private final Optional<Double> endWord;
    private final Optional<List<SharedIntentsResultsIntentsSegmentsItemIntentsItem>> intents;
    private final Map<String, Object> additionalProperties;

    private SharedIntentsResultsIntentsSegmentsItem(
            Optional<String> text,
            Optional<Double> startWord,
            Optional<Double> endWord,
            Optional<List<SharedIntentsResultsIntentsSegmentsItemIntentsItem>> intents,
            Map<String, Object> additionalProperties) {
        this.text = text;
        this.startWord = startWord;
        this.endWord = endWord;
        this.intents = intents;
        this.additionalProperties = additionalProperties;
    }

    static SharedIntentsResultsIntentsSegmentsItem fromCurrent(SharedIntentsSegmentsItem value) {
        return new SharedIntentsResultsIntentsSegmentsItem(
                value.getText(),
                value.getStartWord(),
                value.getEndWord(),
                value.getIntents().map(items -> items.stream()
                        .map(SharedIntentsResultsIntentsSegmentsItemIntentsItem::fromCurrent)
                        .collect(Collectors.toList())),
                value.getAdditionalProperties());
    }

    SharedIntentsSegmentsItem toCurrent() {
        return SharedIntentsSegmentsItem.builder()
                .text(text)
                .startWord(startWord)
                .endWord(endWord)
                .intents(intents.map(items -> items.stream()
                        .map(SharedIntentsResultsIntentsSegmentsItemIntentsItem::toCurrent)
                        .collect(Collectors.toList())))
                .additionalProperties(additionalProperties)
                .build();
    }

    public Optional<String> getText() { return text; }
    public Optional<Double> getStartWord() { return startWord; }
    public Optional<Double> getEndWord() { return endWord; }
    public Optional<List<SharedIntentsResultsIntentsSegmentsItemIntentsItem>> getIntents() { return intents; }
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof SharedIntentsResultsIntentsSegmentsItem
                && equalTo((SharedIntentsResultsIntentsSegmentsItem) other);
    }

    private boolean equalTo(SharedIntentsResultsIntentsSegmentsItem other) {
        return text.equals(other.text) && startWord.equals(other.startWord) && endWord.equals(other.endWord)
                && intents.equals(other.intents);
    }

    @Override
    public int hashCode() { return Objects.hash(text, startWord, endWord, intents); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Optional<String> text = Optional.empty();
        private Optional<Double> startWord = Optional.empty();
        private Optional<Double> endWord = Optional.empty();
        private Optional<List<SharedIntentsResultsIntentsSegmentsItemIntentsItem>> intents = Optional.empty();
        private final Map<String, Object> additionalProperties = new HashMap<>();

        private Builder() {}

        public Builder from(SharedIntentsResultsIntentsSegmentsItem other) {
            return text(other.getText()).startWord(other.getStartWord()).endWord(other.getEndWord()).intents(other.getIntents());
        }

        public Builder text(Optional<String> text) { this.text = text; return this; }
        public Builder text(String text) { return text(Optional.ofNullable(text)); }
        public Builder startWord(Optional<Double> startWord) { this.startWord = startWord; return this; }
        public Builder startWord(Double startWord) { return startWord(Optional.ofNullable(startWord)); }
        public Builder endWord(Optional<Double> endWord) { this.endWord = endWord; return this; }
        public Builder endWord(Double endWord) { return endWord(Optional.ofNullable(endWord)); }
        public Builder intents(Optional<List<SharedIntentsResultsIntentsSegmentsItemIntentsItem>> intents) { this.intents = intents; return this; }
        public Builder intents(List<SharedIntentsResultsIntentsSegmentsItemIntentsItem> intents) { return intents(Optional.ofNullable(intents)); }
        public Builder additionalProperty(String key, Object value) { additionalProperties.put(key, value); return this; }
        public Builder additionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties.putAll(additionalProperties); return this; }

        public SharedIntentsResultsIntentsSegmentsItem build() {
            return new SharedIntentsResultsIntentsSegmentsItem(text, startWord, endWord, intents, additionalProperties);
        }
    }
}
