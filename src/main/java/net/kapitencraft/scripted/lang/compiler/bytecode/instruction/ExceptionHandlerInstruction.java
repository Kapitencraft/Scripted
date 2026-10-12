package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public class ExceptionHandlerInstruction implements Instruction {
    private final int handlerStart;
    private final int handlerEnd;
    private final int handlerIP;
    private final String className;

    public ExceptionHandlerInstruction(int handlerStart, int handlerEnd, int handlerIP, String className) {
        this.handlerStart = handlerStart;
        this.handlerEnd = handlerEnd;
        this.handlerIP = handlerIP;
        this.className = className;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addExceptionHandler(
                ips.getIp(handlerStart),
                ips.getIp(handlerEnd),
                ips.getIp(handlerIP),
                builder.injectStringNoArg(className)
        );
    }

    public int getHandlerStart() {
        return handlerStart;
    }

    public int getHandlerEnd() {
        return handlerEnd;
    }

    public int getHandlerIP() {
        return handlerIP;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ExceptionHandlerInstruction ehi &&
                ehi.className.equals(this.className) &&
                ehi.handlerStart == this.handlerStart &&
                ehi.handlerEnd == this.handlerEnd &&
                ehi.handlerIP == this.handlerIP;
    }
}
