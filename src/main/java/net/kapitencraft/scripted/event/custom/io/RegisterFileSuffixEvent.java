package net.kapitencraft.scripted.event.custom.io;

import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class RegisterFileSuffixEvent extends Event {
    private final List<String> fileSuffixes;

    public RegisterFileSuffixEvent(List<String> fileSuffixes) {
        this.fileSuffixes = fileSuffixes;
    }

    public void addSuffix(String suffix) {
        fileSuffixes.add(suffix);
    }

    public List<String> getFileSuffixes() {
        return fileSuffixes;
    }
}