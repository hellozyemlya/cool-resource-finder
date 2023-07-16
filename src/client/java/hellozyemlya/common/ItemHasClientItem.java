package hellozyemlya.common;

import org.jetbrains.annotations.NotNull;

public interface ItemHasClientItem {
    default void setClientItem(@NotNull ClientItem clientItem) {
        throw new IllegalStateException("default impl?");
    }

    default ClientItem getClientItem() {
        throw new IllegalStateException("default impl?");
    }
}
