package net.kapitencraft.scripted.lang.env.abst;

import java.util.ArrayDeque;
import java.util.function.UnaryOperator;

public class DequeStack<T> {
    private final ArrayDeque<T> stack;
    private final UnaryOperator<T> reCreator;

    /**
     * @param def default value added on creation
     */
    public DequeStack(T def, UnaryOperator<T> reCreator) {
        this.stack = new ArrayDeque<>();
        this.stack.add(def);
        this.reCreator = reCreator;
    }

    /**
     * push the stack; use pop to revert changes made after push
     */
    public void push() {
        stack.addLast(reCreator.apply(stack.getLast()));
    }

    protected T getLast() {
        return stack.getLast();
    }

    /**
     * pop the stack; removes any changes made since the last `'push' call
     */
    public void pop() {
        stack.removeLast();
        if (stack.isEmpty()) throw new IllegalStateException("leveled has been completely cleared");
    }

    protected int size() {
        return stack.size();
    }
}