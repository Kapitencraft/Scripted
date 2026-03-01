package net.kapitencraft.scripted.lang.run.test;

import com.google.gson.*;
import net.kapitencraft.scripted.lang.bytecode.exe.VirtualMachine;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.run.Interpreter;
import net.kapitencraft.scripted.lang.run.VarTypeManager;
import net.kapitencraft.scripted.lang.run.load.ClassLoader;
import net.minecraft.util.GsonHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class TestLoader {
    private static final Gson GSON = new GsonBuilder().create();
    private static final File TEST_CONFIG = new File("./run/test.json");

    private record TestInstance(String target, String args, String[] output) {
        public boolean run() {
            ClassReference reference = VarTypeManager.getClassForName(target);
            if (reference == null || !reference.exists()) {
                System.out.println("\u001B[31munknown class: " + target + "\u001B[0m");
                return true;
            }
            try {
                System.out.printf("\u001B[32mRunning: %s\u001B[0m\n", reference.absoluteName());
                VirtualMachine.runMainMethod(reference.get(), args, false, false);
            } catch (Exception e) {
                System.out.println("\u001B[31mprogram crashed: " + e.getMessage() + "\u001B[0m");
                return true;
            }
            return false;
        }
    }

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        try {
            JsonArray array = GSON.fromJson(new FileReader(TEST_CONFIG), JsonArray.class);
            List<TestInstance> tests = new ArrayList<>();
            for (JsonElement element : array.asList()) {
                JsonObject object = element.getAsJsonObject();
                String target = GsonHelper.getAsString(object, "target");
                String testArgs = GsonHelper.getAsString(object, "args", "");
                String[] output = GsonHelper.getAsJsonArray(object, "output").asList().stream().map(JsonElement::getAsString).toArray(String[]::new);
                tests.add(new TestInstance(target, testArgs, output));
            }
            ClassLoader.loadClasses();
            TestExecution execution = new TestExecution();
            execution.setup();
            tests.forEach(execution::runTest);
            execution.clear();
            System.out.printf("test complete. %s / %s successful\n", execution.getSucceeded(), tests.size());
        } catch (FileNotFoundException e) {
            System.out.println("file not found: " + e.getMessage() + "\u001B[0m");
        }
    }

    private static class TestExecution {
        private int succeeded = 0;
        private int outputIndex = 0;
        private boolean error = false;
        private TestInstance running;

        public void setup() {
            Interpreter.output = this::checkOutput;
        }

        @SuppressWarnings("ConstantValue")
        public void runTest(TestInstance instance) {
            this.running = instance;
            this.outputIndex = 0;
            this.error = false;
            error |= instance.run();
            if (instance.output.length > this.outputIndex) {
                System.out.printf("\u001B[31mMissing outputs. got %s but expected %s\u001B[0m\n", this.outputIndex, instance.output.length);
                error = true;
            }
            if (error) {
                System.out.println("\u001B[31mError running class '" + instance.target + "'\u001B[0m");
            } else {
                succeeded++;
                System.out.println("\u001B[32mSuccessfully tested class '" + instance.target + "'. took " + Interpreter.elapsedMillis() + "ms\u001B[0m");
            }
        }

        public void clear() {
            Interpreter.output = System.out::println;
        }

        private void checkOutput(String output) {
            if (outputIndex >= running.output.length) {
                System.out.println("\u001B[31mTest for '" + running.target + "' failed. more outputs got than expected: " + output + "\u001B[0m");
                error = true;
            } else if (!output.equals(running.output[outputIndex])) {
                System.out.printf("\u001B[31mTest for '%s' failed at index %s. Expected \"%s\", but got: \"%s\"\u001B[0m\n", running.target, outputIndex, running.output[outputIndex], output);
                error = true;
            }
            outputIndex++;
        }

        public boolean error() {
            return this.error;
        }

        public int getSucceeded() {
            return succeeded;
        }
    }
}
