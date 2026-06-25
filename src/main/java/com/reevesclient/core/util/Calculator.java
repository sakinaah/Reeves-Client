package com.reevesclient.core.util;

import java.util.OptionalDouble;

/**
 * Tiny, dependency-free math evaluator for the search/calculator bar.
 * Supports + - * / % ^, parentheses, decimals, and k/m/b/t suffixes
 * (e.g. "2.5m * 3" or "(64*9)/64"). Informational only — it never touches
 * gameplay; it just computes a number from text.
 */
public final class Calculator {

    private Calculator() {}

    /** Evaluates an expression, or empty if it isn't valid math. */
    public static OptionalDouble tryEvaluate(String input) {
        if (input == null) return OptionalDouble.empty();
        String s = input.trim();
        if (s.startsWith("=")) s = s.substring(1).trim();
        if (s.isEmpty()) return OptionalDouble.empty();
        // Must look like math (avoid treating plain item names as expressions).
        if (!s.matches("[0-9.+\\-*/%^()\\s kmbtKMBT]+")) return OptionalDouble.empty();
        if (!s.matches(".*[0-9].*")) return OptionalDouble.empty();
        try {
            Parser p = new Parser(s);
            double v = p.parseExpression();
            p.expectEnd();
            if (Double.isNaN(v) || Double.isInfinite(v)) return OptionalDouble.empty();
            return OptionalDouble.of(v);
        } catch (RuntimeException e) {
            return OptionalDouble.empty();
        }
    }

    /** Formats a result compactly: 1.25M, 340.5k, or plain. */
    public static String format(double v) {
        double abs = Math.abs(v);
        if (abs >= 1_000_000_000_000.0) return trim(v / 1_000_000_000_000.0) + "T";
        if (abs >= 1_000_000_000.0)     return trim(v / 1_000_000_000.0) + "B";
        if (abs >= 1_000_000.0)         return trim(v / 1_000_000.0) + "M";
        if (abs >= 10_000.0)            return trim(v / 1_000.0) + "k";
        return trim(v);
    }

    private static String trim(double v) {
        if (v == Math.rint(v) && !Double.isInfinite(v)) return String.format("%,d", (long) v);
        return String.format("%,.2f", v);
    }

    /** Recursive-descent parser. */
    private static final class Parser {
        private final String s;
        private int pos;

        Parser(String s) { this.s = s; }

        double parseExpression() {
            double v = parseTerm();
            while (true) {
                skipWs();
                if (consume('+')) v += parseTerm();
                else if (consume('-')) v -= parseTerm();
                else return v;
            }
        }

        private double parseTerm() {
            double v = parseFactor();
            while (true) {
                skipWs();
                if (consume('*')) v *= parseFactor();
                else if (consume('/')) v /= parseFactor();
                else if (consume('%')) v %= parseFactor();
                else return v;
            }
        }

        private double parseFactor() {
            double base = parseUnary();
            skipWs();
            if (consume('^')) return Math.pow(base, parseFactor()); // right-assoc
            return base;
        }

        private double parseUnary() {
            skipWs();
            if (consume('-')) return -parseUnary();
            if (consume('+')) return parseUnary();
            return parsePrimary();
        }

        private double parsePrimary() {
            skipWs();
            if (consume('(')) {
                double v = parseExpression();
                skipWs();
                if (!consume(')')) throw new RuntimeException("expected )");
                return v;
            }
            return parseNumber();
        }

        private double parseNumber() {
            skipWs();
            int start = pos;
            while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) pos++;
            if (pos == start) throw new RuntimeException("expected number");
            double v = Double.parseDouble(s.substring(start, pos));
            if (pos < s.length()) {
                char c = Character.toLowerCase(s.charAt(pos));
                double mult = switch (c) {
                    case 'k' -> 1_000.0;
                    case 'm' -> 1_000_000.0;
                    case 'b' -> 1_000_000_000.0;
                    case 't' -> 1_000_000_000_000.0;
                    default  -> 1.0;
                };
                if (mult != 1.0) { v *= mult; pos++; }
            }
            return v;
        }

        private void skipWs() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        private boolean consume(char c) {
            if (pos < s.length() && s.charAt(pos) == c) { pos++; return true; }
            return false;
        }

        void expectEnd() {
            skipWs();
            if (pos != s.length()) throw new RuntimeException("trailing input");
        }
    }
}
