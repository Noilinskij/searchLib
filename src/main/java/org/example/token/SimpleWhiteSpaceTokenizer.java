package org.example.token;

import java.util.Arrays;
import java.util.stream.Stream;

public class SimpleWhiteSpaceTokenizer implements Tokenizer{
    @Override
    public Stream<String> tokenize(String text) {
        if (text == null) {
            return Stream.empty();
        }

        return Arrays.stream(text.trim().split("\\s+")).map(String::toLowerCase);
    }
}
