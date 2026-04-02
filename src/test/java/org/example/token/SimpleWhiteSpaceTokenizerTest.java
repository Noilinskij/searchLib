package org.example.token;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleWhiteSpaceTokenizerTest {

    private final SimpleWhiteSpaceTokenizer tokenizer = new SimpleWhiteSpaceTokenizer();

    @Test
    void tokenizeNullReturnsEmptyStream() {
        List<String> result = tokenizer.tokenize(null).toList();

        assertTrue(result.isEmpty());
    }

    @Test
    void tokenizeSplitsByWhitespaceAndLowercasesWords() {
        String text = "  HeLLo   Java\nWORLD  ";

        List<String> result = tokenizer.tokenize(text).toList();

        assertEquals(List.of("hello", "java", "world"), result);
    }
}
