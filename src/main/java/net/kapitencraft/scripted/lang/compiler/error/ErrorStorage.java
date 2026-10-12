package net.kapitencraft.scripted.lang.compiler.error;

import com.google.common.collect.ImmutableList;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.token.Token;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

public class ErrorStorage {
    public static int overallErrorCount = 0;

    private final String[] lines;
    private final String fileLoc;
    private final List<Message> messages = new ArrayList<>();
    private int errorCount;

    public ErrorStorage(String[] lines, String fileLoc) {
        this.lines = lines;
        this.fileLoc = fileLoc;
    }

    public void printAll() {
        for (Message msg : messages) {
            msg.print(lines, fileLoc);
        }
    }

    @ApiStatus.Internal
    public List<Message> getMessages() {
        return ImmutableList.copyOf(this.messages);
    }

    //region msg
    public interface Message {

        void print(String[] lines, String fileLoc);

        String msg();

        int lineIndex();
    }

    public record Error(int lineIndex, int lineStartIndex, String msg, String line) implements Message {

        @Override
        public void print(String[] lines, String fileLoc) {
            Compiler.error(lineIndex, lineStartIndex, msg, fileLoc, line);
        }
    }

    public record Warn(int lineIndex, int lineStartIndex, String msg) implements Message {

        @Override
        public void print(String[] lines, String fileLoc) {
            Compiler.warn(lineIndex, lineStartIndex, msg, fileLoc, lines[lineIndex]);
        }
    }

    public record Log(String msg) implements Message {

        @Override
        public void print(String[] lines, String fileLoc) {
            System.err.println(msg);
        }

        @Override
        public int lineIndex() {
            return -1;
        }
    }
    //endregion

    public void error(Token loc, String msg) {
        error(loc.line(), loc.lineStartIndex(), msg);
    }

    public void errorF(Token loc, String format, Object... args) {
        error(loc, String.format(format, args));
    }

    public void errorF(Expr loc, String format, Object... args) {
        errorF(Compiler.LOCATION_ANALYSER.find(loc), format, args);
    }

    public void error(int lineIndex, int lineStartIndex, String msg) {
        errorCount++;
        overallErrorCount++;
        messages.add(new Error(lineIndex, lineStartIndex, msg, lines[lineIndex - 1]));
    }

    public void error(Stmt loc, String msg) {
        error(Compiler.LOCATION_ANALYSER.find(loc), msg);
    }

    public void error(Expr loc, String msg) {
        error(Compiler.LOCATION_ANALYSER.find(loc), msg);
    }

    public void logError(String s) {
        this.messages.add(new Log(s));
    }

    public void warn(int lineIndex, int lineStartIndex, String msg) {
        this.messages.add(new Warn(lineIndex, lineStartIndex, msg));
    }

    public void warn(Token loc, String msg) {
        warn(loc.line(), loc.lineStartIndex(), msg);
    }

    public void warn(Stmt loc, String msg) {
        warn(Compiler.LOCATION_ANALYSER.find(loc), msg);
    }

    @Override
    public String toString() {
        return "ErrorStorage for '" + fileLoc + "' (errorCount: " + messages.size() + ")";
    }

    public boolean hadError() {
        return errorCount > 0;
    }
}
