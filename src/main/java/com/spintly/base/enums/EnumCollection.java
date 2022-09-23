package com.spintly.base.enums;

public class EnumCollection {

    public enum CharacterTypeSets {
        ANY_CHARACTERS_SUPPORTS_MULTILINGUAL,
        ANY_CHARACTERS,
        ALPHANUMERIC,
        ALPHABETS,
        NUMERIC
    }

    public enum ResultType {
        PASSED("PASSED", 1),
        FAILED("FAILED", 3),
        WARNING("WARNING", 2),
        ERROR("ERROR", 4),
        DONE("PASSED", 0);
        public final String name;
        public final int level;
        public String toString() {
            return name;
        }
        private ResultType(String label1, int label2) {
            name = label1;
            level = label2;
        }
    }
}
