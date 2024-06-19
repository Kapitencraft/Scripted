package net.kapitencraft.scripted.io;

public record StringSegment(int startIndex, int endIndex, String content) {

    public static StringSegment fromString(int start, int end, String in) {
        return new StringSegment(start, end, in.substring(start, end));
    }
}