package com.deepgram.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/** @deprecated Use {@link SharedTopicsSegmentsItem}. */
@Deprecated
public final class SharedTopicsResultsTopicsSegmentsItem {
    private final Optional<String> text;
    private final Optional<Double> startWord;
    private final Optional<Double> endWord;
    private final Optional<List<SharedTopicsResultsTopicsSegmentsItemTopicsItem>> topics;
    private final Map<String, Object> additionalProperties;

    private SharedTopicsResultsTopicsSegmentsItem(
            Optional<String> text,
            Optional<Double> startWord,
            Optional<Double> endWord,
            Optional<List<SharedTopicsResultsTopicsSegmentsItemTopicsItem>> topics,
            Map<String, Object> additionalProperties) {
        this.text = text;
        this.startWord = startWord;
        this.endWord = endWord;
        this.topics = topics;
        this.additionalProperties = additionalProperties;
    }

    static SharedTopicsResultsTopicsSegmentsItem fromCurrent(SharedTopicsSegmentsItem value) {
        return new SharedTopicsResultsTopicsSegmentsItem(
                value.getText(),
                value.getStartWord(),
                value.getEndWord(),
                value.getTopics().map(items -> items.stream()
                        .map(SharedTopicsResultsTopicsSegmentsItemTopicsItem::fromCurrent)
                        .collect(Collectors.toList())),
                value.getAdditionalProperties());
    }

    SharedTopicsSegmentsItem toCurrent() {
        return SharedTopicsSegmentsItem.builder()
                .text(text)
                .startWord(startWord)
                .endWord(endWord)
                .topics(topics.map(items -> items.stream()
                        .map(SharedTopicsResultsTopicsSegmentsItemTopicsItem::toCurrent)
                        .collect(Collectors.toList())))
                .additionalProperties(additionalProperties)
                .build();
    }

    public Optional<String> getText() { return text; }
    public Optional<Double> getStartWord() { return startWord; }
    public Optional<Double> getEndWord() { return endWord; }
    public Optional<List<SharedTopicsResultsTopicsSegmentsItemTopicsItem>> getTopics() { return topics; }
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof SharedTopicsResultsTopicsSegmentsItem
                && equalTo((SharedTopicsResultsTopicsSegmentsItem) other);
    }

    private boolean equalTo(SharedTopicsResultsTopicsSegmentsItem other) {
        return text.equals(other.text) && startWord.equals(other.startWord) && endWord.equals(other.endWord)
                && topics.equals(other.topics);
    }

    @Override
    public int hashCode() { return Objects.hash(text, startWord, endWord, topics); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Optional<String> text = Optional.empty();
        private Optional<Double> startWord = Optional.empty();
        private Optional<Double> endWord = Optional.empty();
        private Optional<List<SharedTopicsResultsTopicsSegmentsItemTopicsItem>> topics = Optional.empty();
        private final Map<String, Object> additionalProperties = new HashMap<>();

        private Builder() {}

        public Builder from(SharedTopicsResultsTopicsSegmentsItem other) {
            return text(other.getText()).startWord(other.getStartWord()).endWord(other.getEndWord()).topics(other.getTopics());
        }

        public Builder text(Optional<String> text) { this.text = text; return this; }
        public Builder text(String text) { return text(Optional.ofNullable(text)); }
        public Builder startWord(Optional<Double> startWord) { this.startWord = startWord; return this; }
        public Builder startWord(Double startWord) { return startWord(Optional.ofNullable(startWord)); }
        public Builder endWord(Optional<Double> endWord) { this.endWord = endWord; return this; }
        public Builder endWord(Double endWord) { return endWord(Optional.ofNullable(endWord)); }
        public Builder topics(Optional<List<SharedTopicsResultsTopicsSegmentsItemTopicsItem>> topics) { this.topics = topics; return this; }
        public Builder topics(List<SharedTopicsResultsTopicsSegmentsItemTopicsItem> topics) { return topics(Optional.ofNullable(topics)); }
        public Builder additionalProperty(String key, Object value) { additionalProperties.put(key, value); return this; }
        public Builder additionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties.putAll(additionalProperties); return this; }

        public SharedTopicsResultsTopicsSegmentsItem build() {
            return new SharedTopicsResultsTopicsSegmentsItem(text, startWord, endWord, topics, additionalProperties);
        }
    }
}
