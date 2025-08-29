package com.stdsolutions.deltam.options;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command line options parser.
 * Parses arguments in format "--name=value" or "--name" (empty value).
 */
public abstract class Options {

    protected final Map<String, String> map;

    /**
     * Creates Options from command line arguments.
     *
     * @param args command line arguments
     */
    protected Options(final String... args) {
        this(args != null ? Arrays.asList(args) : List.of());
    }

    /**
     * Creates Options from iterable of arguments.
     *
     * @param args iterable of arguments
     */
    protected Options(final Iterable<String> args) {
        this.map = asMap(args);
    }

    /**
     * Parses command line arguments into a map of option name to value.
     *
     * @param args the arguments to parse
     * @return map of parsed options
     * @throws IllegalArgumentException if any argument cannot be parsed
     */
    private static Map<String, String> asMap(final Iterable<String> args) {
        if (args == null) {
            return Collections.emptyMap();
        }
        final Map<String, String> map = new HashMap<>();
        final Pattern ptn = Pattern.compile("--([a-zA-Z][a-zA-Z0-9\\-_]*)(?:=(.*))?");
        for (final String arg : args) {
            if (arg == null || arg.isBlank()) {
                continue;
            }
            final Matcher matcher = ptn.matcher(arg.trim());
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                        String.format("Invalid argument format: '%s'. Expected format: --name or --name=value", arg)
                );
            }
            final String value = matcher.group(2);
            map.put(matcher.group(1).toLowerCase(), value != null ? value : "");
        }
        return map;
    }
}