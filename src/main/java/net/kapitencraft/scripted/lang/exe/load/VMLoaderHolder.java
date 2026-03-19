package net.kapitencraft.scripted.lang.exe.load;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.ClassType;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.clazz.generated.RuntimeClass;
import net.kapitencraft.scripted.lang.oop.clazz.skeleton.SkeletonAnnotation;
import net.kapitencraft.scripted.lang.oop.clazz.skeleton.SkeletonClass;
import net.kapitencraft.scripted.lang.oop.clazz.skeleton.SkeletonInterface;
import net.minecraft.util.GsonHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class VMLoaderHolder extends ClassLoaderHolder<VMLoaderHolder> {
    private final JsonObject data;
    private final ClassType type;
    public final String name;
    final ClassReference reference;

    public VMLoaderHolder(File file, String pck) {
        super(file, pck);
        try {
            this.data = Streams.parse(new JsonReader(new FileReader(file))).getAsJsonObject();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String fileId = file.getPath().substring(12);
        String[] packages = fileId.substring(0, fileId.length() - 5).split("\\\\");
        this.name = packages[packages.length-1];
        this.reference = new ClassReference(name, pck);
        this.type = ClassType.valueOf(GsonHelper.getAsString(data, "TYPE").toUpperCase());
    }

    @Override
    public void applySkeleton()  {

        ScriptedClass skeleton = switch (type) {
            case ENUM, CLASS -> SkeletonClass.fromCache(data, pck());
            case INTERFACE -> SkeletonInterface.fromCache(data, pck());
            case ANNOTATION -> SkeletonAnnotation.fromCache(data, pck());
        };
        this.reference.setTarget(skeleton);
    }

    public ScriptedClass loadClass()  {
        ScriptedClass target;
        try {
            target = RuntimeClass.load(data, pck());
            this.reference.setTarget(target);
            return target;
        } catch (Exception e) {
            System.err.println("Error Loading Class '" + reference.absoluteName() + "': " + e.getMessage());
        }
        return null;
    }
}
