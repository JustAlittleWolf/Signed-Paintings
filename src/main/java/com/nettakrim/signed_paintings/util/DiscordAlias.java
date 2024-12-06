package com.nettakrim.signed_paintings.util;

import com.nettakrim.signed_paintings.SignedPaintingsClient;

public class DiscordAlias extends URLAlias {
    // https://media.discordapp.net/attachments/861205264719675392/1255835411624755261/20240624_085619.jpg?ex=6754265e&is=6752d4de&hm=ad82cd6d5d3b95569bb0e097ae43bf98ae616a24da24148e85e2caf4d5135165&=&format=webp&width=911&height=683
    // https://cdn.discordapp.com/attachments/861205264719675392/1255835411624755261/20240624_085619.jpg?ex=6754265e&is=6752d4de&hm=ad82cd6d5d3b95569bb0e097ae43bf98ae616a24da24148e85e2caf4d5135165

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
        SignedPaintingsClient.info(url, true);

        if (!isDiscord(url)) {
            return url;
        }

        String attachment = url.split("attachments/")[1];
        // 861205264719675392/1255835411624755261/20240624_085619.jpg?ex=6754265e&is=6752d4de&hm=ad82cd6d5d3b95569bb0e097ae43bf98ae616a24da24148e85e2caf4d5135165&=&format=webp&width=911&height=683
        String[] parts = attachment.split("\\?");
        if (parts.length != 2) return url;
        // 861205264719675392/1255835411624755261/20240624_085619.jpg
        // ex=6754265e&is=6752d4de&hm=ad82cd6d5d3b95569bb0e097ae43bf98ae616a24da24148e85e2caf4d5135165&=&format=webp&width=911&height=683

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

        SignedPaintingsClient.info("??? discord:"+parts[0]+"?"+ex+"/"+is+"/"+hm, true);

        return "discord:"+parts[0]+"?"+ex+"/"+is+"/"+hm;
    }

    public static String decode(String url) {
        if (!url.startsWith("discord:")) return url;

        // https://cdn.discordapp.com/attachments/861205264719675392/1255835411624755261/20240624_085619.jpg?ex=6754265e&is=6752d4de&hm=ad82cd6d5d3b95569bb0e097ae43bf98ae616a24da24148e85e2caf4d5135165
        // discord:861205264719675392/1255835411624755261/20240624_085619.jpg?6754265e/6752d4de/ad82cd6d5d3b95569bb0e097ae43bf98ae616a24da24148e85e2caf4d5135165

        String[] parts = url.substring(8).split("\\?");
        if (parts.length != 2) return url;
        String[] search = parts[1].split("/");
        if (search.length != 3) return  url;
        SignedPaintingsClient.info("loading "+"https://cdn.discordapp.com/attachments/"+parts[0]+"?ex="+search[0]+"&is="+search[1]+"&hm="+search[2], true);
        return "https://cdn.discordapp.com/attachments/"+parts[0]+"?ex="+search[0]+"&is="+search[1]+"&hm="+search[2];
    }

    public static boolean isDiscord(String url) {
        if (url.startsWith("https://")) url = url.substring(8);
        return url.startsWith("media.discordapp.net/attachments/") || url.startsWith("cdn.discordapp.com/attachments/");
    }
}
