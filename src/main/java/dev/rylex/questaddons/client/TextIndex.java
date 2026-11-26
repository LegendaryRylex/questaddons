package dev.rylex.questaddons.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TextIndex {
    private static final int FORMATTING_CODE = '§';
    private final int docCount;
    private final String[] terms;
    private final int[][] postings;

    private TextIndex(int docCount, String[] terms, int[][] postings) {
        this.docCount = docCount;
        this.terms = terms;
        this.postings = postings;
    }

    public static List<String> tokens(String text) {
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            if (Character.isLetterOrDigit(codePoint)) {
                token.appendCodePoint(Character.toLowerCase(codePoint));
                continue;
            }
            if (codePoint == FORMATTING_CODE && i < text.length()) {
                i += Character.charCount(text.codePointAt(i));
            }
            if (!token.isEmpty()) {
                tokens.add(token.toString());
                token.setLength(0);
            }
        }
        if (!token.isEmpty()) {
            tokens.add(token.toString());
        }
        return tokens;
    }

    public int docCount() {
        return docCount;
    }

    public BitSet matchPrefix(String prefix) {
        BitSet docs = new BitSet(docCount);
        for (int i = lowerBound(prefix); i < terms.length && terms[i].startsWith(prefix); i++) {
            for (int doc : postings[i]) {
                docs.set(doc);
            }
        }
        return docs;
    }

    public BitSet matchAll(List<String> prefixes) {
        BitSet docs = new BitSet(docCount);
        docs.set(0, docCount);
        for (String prefix : prefixes) {
            docs.and(matchPrefix(prefix));
            if (docs.isEmpty()) {
                break;
            }
        }
        return docs;
    }

    private int lowerBound(String prefix) {
        int index = Arrays.binarySearch(terms, prefix);
        return index >= 0 ? index : -index - 1;
    }

    public static final class Builder {
        private final Map<String, int[]> postings = new HashMap<>();
        private final Map<String, Integer> sizes = new HashMap<>();
        private int docCount;

        public Builder add(int doc, String text) {
            docCount = Math.max(docCount, doc + 1);
            for (String token : tokens(text)) {
                int size = sizes.getOrDefault(token, 0);
                int[] docs = postings.get(token);
                if (docs != null && size > 0 && docs[size - 1] == doc) {
                    continue;
                }
                if (docs == null) {
                    docs = new int[2];
                } else if (size == docs.length) {
                    docs = Arrays.copyOf(docs, size * 2);
                }
                docs[size] = doc;
                postings.put(token, docs);
                sizes.put(token, size + 1);
            }
            return this;
        }

        public Builder reserve(int docs) {
            docCount = Math.max(docCount, docs);
            return this;
        }

        public TextIndex build() {
            String[] terms = postings.keySet().toArray(String[]::new);
            Arrays.sort(terms);
            int[][] lists = new int[terms.length][];
            for (int i = 0; i < terms.length; i++) {
                lists[i] = Arrays.copyOf(postings.get(terms[i]), sizes.get(terms[i]));
            }
            return new TextIndex(docCount, terms, lists);
        }
    }
}
