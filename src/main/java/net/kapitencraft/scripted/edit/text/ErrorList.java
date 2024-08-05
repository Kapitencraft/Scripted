package net.kapitencraft.scripted.edit.text;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ErrorList {
    private final Multimap<Integer, Error> lineToErrorsMap = HashMultimap.create();

    public void add(int line, String message) {

    }


    private static class Error {

    }
}
