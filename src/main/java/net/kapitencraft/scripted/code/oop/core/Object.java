package net.kapitencraft.scripted.code.oop.core;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class Object extends VarType<Object.ObjectInstance> {
    public Object(String name) {
        super(name, null, null, null, null, null, null);
    }

    public <T> void newMethod(String name, VarType<T> retType, List<Pair<VarType<?>, String>> params, MethodPipeline<T> pipeline) {
        this.addMethod(()-> new ObjectMethod<>(params, name, retType, pipeline));
    }

    public void setConstructor(List<Pair<VarType<?>, String>> params, MethodPipeline<Void> constructorBuilder) {
        this.setConstructor(new ObjectConstructor(params, constructorBuilder));
    }

    public class ObjectMethod<R> extends SimpleInstanceFunction<R> {
        private final VarType<R> retType;
        private final MethodPipeline<R> content;
        private final List<ParamInst<?>> params;

        public ObjectMethod(List<Pair<VarType<?>, String>> params, String name, VarType<R> retType, MethodPipeline<R> content) {
            this.params = params.stream().map(ParamInst::of)
            this.retType = retType;
            this.content = content;
        }

        @Override
        public R call(VarMap params, ObjectInstance inst) {
            params.addValue("this", new Var<>(Object.this, inst, true));
            return content.execute(params, null).getValue();
        }

        @Override
        public VarType<R> getType(IVarAnalyser analyser) {
            return retType;
        }
    }

    //TODO fix?
    public class ObjectConstructor extends SimpleConstructor {
        private final MethodPipeline<Void> builder;

        protected ObjectConstructor(List<Pair<VarType<?>, String>> params, MethodPipeline<Void> builder) {
            super(set -> set.addEntry(entry -> entry.set(params)));
            this.builder = builder;
        }

        @Override
        protected ObjectInstance call(VarMap params) {
            ObjectInstance instance = new ObjectInstance();
            builder.execute(params, null);
            VarMap self = new VarMap();
            self.addValue("this", new Var<>(Object.this, instance, true));
            instance.data.checkConstructor(self);
            return instance;
        }

        @Override
        protected VarType<ObjectInstance> getType(IVarAnalyser analyser) {
            return Object.this;
        }
    }

    public class ObjectField<J> extends Field<J> {

        public ObjectField(String name, @NotNull Supplier<VarType<J>> type) {
            super(objectInstance -> objectInstance.getFieldValue(name), (objectInstance, j) -> objectInstance.setFieldValue(name, j), type);
        }
    }

    public static class ObjectInstance {
        private final FieldData data = new FieldData();

        public <J> J getFieldValue(String name) {
            return data.getFieldValue(name);
        }

        public <J> void setFieldValue(String name, J j) {
            data.setFieldValue(name, j);
        }
    }
}
