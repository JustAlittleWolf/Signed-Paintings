package com.nettakrim.signed_paintings.util;

public abstract class URLAlias {
    public abstract String tryApply(String url);

    public abstract String getShortestAlias(String url);
}
