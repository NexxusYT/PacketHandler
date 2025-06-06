package com.github.razorplay.packet_handler.util;

/**
 * Utility class providing helper methods for string transformations.
 */
public final class StringUtil {

    /**
     * Converts a camelCase or PascalCase string to snake_case.
     * <p>Each uppercase character is replaced with an underscore followed by its lowercase version.</p>
     * <pre>{@code
     * StringUtil.toSnakeCase("camelCaseString")  // returns "camel_case_string"
     * StringUtil.toSnakeCase("PascalCase")       // returns "pascal_case"
     * }</pre>
     *
     * @param str the input string in camelCase or PascalCase.
     * @return the converted string in snake_case.
     */
    public static String toSnakeCase(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else sb.append(c);
        }
        return sb.toString();
    }
}
