package com.nettakrim.signed_paintings.util;

public class DiscordAlias extends URLAlias {
    public DiscordAlias() {

    }

    @Override
    public String tryApply(String url) {
        return decode(url);
    }

    @Override
    public String getShortestAlias(String url) {
        return encode(url);
    }

    public static String encode(String url) {
        if (!isDiscord(url)) {
            return url;
        }

        String attachment = url.split("attachments/")[1];
        String[] parts = attachment.split("\\?");
        if (parts.length != 2) return url;

        String[] data = parts[1].split("&");

        String ex = null;
        String is = null;
        String hm = null;

        for (String search : data) {
            if (search.startsWith("ex=")) {
                ex = search.substring(3);
            }
            if (search.startsWith("is=")) {
                is = search.substring(3);
            }
            if (search.startsWith("hm=")) {
                hm = search.substring(3);
            }
        }

        if (ex == null || is == null || hm == null) {
            return url;
        }

        return "discord:"+parts[0]+"?"+ex+"/"+is+"/"+hm;
    }

    public static String decode(String url) {
        if (!url.startsWith("discord:")) return url;

        String[] parts = url.substring(8).split("\\?");
        if (parts.length != 2) return url;
        String[] search = parts[1].split("/");
        if (search.length != 3) return  url;

        return "https://cdn.discordapp.com/attachments/"+parts[0]+"?ex="+search[0]+"&is="+search[1]+"&hm="+search[2];
    }

    public static boolean isDiscord(String url) {
        if (url.startsWith("https://")) url = url.substring(8);
        return url.startsWith("media.discordapp.net/attachments/") || url.startsWith("cdn.discordapp.com/attachments/");
    }
}
