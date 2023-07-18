package hellozyemlya.resourcefinder.render;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;


public class ItemRenderCallStack {
    private static final int LEAK_COUNT = 20;
    public static final ItemRenderCallStack INSTANCE = new ItemRenderCallStack();

    private final ObjectArrayList<ItemRenderCallContext> calls = new ObjectArrayList<>();

    private ItemRenderCallStack() {

    }

    public void withNewContext(@NotNull ItemRenderCallContext context) {
        if(calls.size() > LEAK_COUNT) {
            throw new RuntimeException("Most likely item render stack not properly cleared/implemented...");
        }
        calls.push(context);
    }

    @NotNull
    public ItemRenderCallContext peekTopContext() {
        return calls.top();
    }

    public void requireTopmost() {
        if (calls.size() != 1) {
            throw new IllegalStateException("Must be topmost call");
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends ItemRenderCallContext> T finishContext() {
        return (T) calls.pop();
    }
}
