package net.kapitencraft.scripted.event.custom.io;

import net.minecraftforge.eventbus.api.Event;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * used to register directories to load by the script manager
 */
public class RegisterLoadDirectoryEvent extends Event {
    private final List<File> directory = new ArrayList<>();


    public void addDirectory(String runSubPath) {
        addDirectory(new File(".", runSubPath));
    }

    public void addDirectory(File file) {
        directory.add(file);
    }
}