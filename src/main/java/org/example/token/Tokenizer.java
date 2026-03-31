package org.example.token;

import java.util.stream.Stream;

public interface Tokenizer {
    Stream<String> tokenize(String text);
}
