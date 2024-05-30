package net.kapitencraft.scripted.code;

import com.mojang.serialization.Codec;
import net.kapitencraft.scripted.code.method.MethodCall;
import net.kapitencraft.scripted.code.vars.Var;
import net.kapitencraft.scripted.code.vars.VarMap;

import java.util.List;

public class MethodPipeline {
    public static final Codec<MethodPipeline> CODEC = MethodCall.CODEC.listOf().xmap(
            MethodPipeline::pipeline, MethodPipeline::calls
    );
    public static final Codec<MethodPipeline> LOOP_CODEC = MethodCall.CODEC.listOf().xmap(
            MethodPipeline::loop, MethodPipeline::calls
    );

    private final boolean isLoop;
    private boolean canceled, broken, continued;
    private Var<?> returnValue;
    private VarMap vars;
    private final List<MethodCall> calls;

    public static MethodPipeline loop(List<MethodCall> calls) {
        return new MethodPipeline(true, calls);
    }

    public static MethodPipeline pipeline(List<MethodCall> calls) {
        return new MethodPipeline(false, calls);
    }

    public MethodPipeline(boolean isLoop, List<MethodCall> calls) {
        this.isLoop = isLoop;
        this.calls = calls;
    }

    private List<MethodCall> calls() {
        return calls;
    }

    public Var<?> execute(VarMap map) {
        this.vars = map;
        //Iterator<MethodCall> iterator = calls.iterator();
        //while (iterator.hasNext() && !canceled) {
        //    iterator.next().call(map, this);
        //}
        if (canceled) return returnValue;
        else return Var.EMPTY;
    }

    private boolean stopped() {
        return canceled || broken || continued;
    }

    public void setCanceled() {
        this.canceled = true;
    }

    public void setBroken() {
        assertOnLoop();
        this.broken = true;
    }

    public void setContinued() {
        assertOnLoop();
        this.continued = true;
    }

    private void assertOnLoop() {
        if (!this.isLoop) throw new IllegalStateException("tried to call break or continue outside of loop");
    }

    public boolean isBroken() {
        return broken;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public boolean isContinued() {
        return continued;
    }

    public void reset() {
        this.canceled = false;
        this.broken = false;
        this.continued = false;
    }

    public VarMap getVars() {
        return vars;
    }
}
