package hellozyemlya.resourcefinder.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ItemRenderContext {
    @Nullable
    private static ItemRenderContext CURRENT_RENDER_CONTEXT = null;

    public static void withNewContext(@NotNull ItemRenderContext context) {
        if (CURRENT_RENDER_CONTEXT != null) {
            throw new IllegalStateException("Can not put new context, while has another active context");
        }

        CURRENT_RENDER_CONTEXT = context;
    }

    public static <T extends ItemRenderContext> T requireContext() {
        if (CURRENT_RENDER_CONTEXT != null) {
            //noinspection unchecked
            return (T) CURRENT_RENDER_CONTEXT;
        }

        throw new IllegalStateException("No requested render context available");
    }

    public static <T extends ItemRenderContext> void finishContext() {
        if (CURRENT_RENDER_CONTEXT == null) {
            throw new IllegalStateException("Expected to have render context");
        }
        @SuppressWarnings({"unchecked", "unused"})
        T cast = (T) CURRENT_RENDER_CONTEXT;

        CURRENT_RENDER_CONTEXT = null;
    }
}
