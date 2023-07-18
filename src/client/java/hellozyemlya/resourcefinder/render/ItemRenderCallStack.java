package hellozyemlya.resourcefinder.render;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

public class ItemRenderCallStack {
    public static final ItemRenderCallStack INSTANCE = new ItemRenderCallStack();

    private final ArrayDeque<ItemRenderCallContext> calls = new ArrayDeque<>();
    private ItemRenderCallStack() {

    }

    public void withNewContext(@NotNull ItemRenderCallContext context) {
        calls.push(context);
    }

    public ItemRenderCallContext peekTopContext() {
        return calls.peek();
    }

    public void requireTopmost() {
        if(calls.size() != 1) {
            throw new IllegalStateException("Must be topmost call");
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ItemRenderCallContext> T finishContext() {
        return (T)calls.pop();
    }
}
