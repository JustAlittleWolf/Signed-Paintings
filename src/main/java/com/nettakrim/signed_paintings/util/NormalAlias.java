package com.nettakrim.signed_paintings.util;

import java.util.Locale;

public class NormalAlias extends URLAlias {
    String domain;
    String[] aliases;
    String defaultImageFormat;

    public NormalAlias(String domain, String[] aliases, String defaultImageFormat) {
        this.domain = domain;
        this.aliases = aliases;
        this.defaultImageFormat = defaultImageFormat;
    }

    @Override
    public String tryApply(String url) {
        String lowercaseUrl = url.toLowerCase(Locale.ROOT);
        for (String alias : aliases) {
            if (lowercaseUrl.startsWith(alias)) {
                String id = url.substring(alias.length());
                String newText = domain + id;
                if (!id.contains(".")) newText += defaultImageFormat;
                return newText;
            }
        }
        return url;
    }

    @Override
    public String getShortestAlias(String url) {
        String lowercaseUrl = url.toLowerCase(Locale.ROOT);
        String current = domain;
        String shortest = current;
        if (!lowercaseUrl.startsWith(domain)) {
            for (String alias : aliases) {
                if (lowercaseUrl.startsWith(alias)) {
                    current = alias;
                }
                if (alias.length() < shortest.length()) {
                    shortest = alias;
                }
            }
            if (current.equals(domain)) return url;
        }
        url = shortest+url.substring(current.length());
        return url;
    }
}
