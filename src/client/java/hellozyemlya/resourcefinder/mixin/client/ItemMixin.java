package hellozyemlya.resourcefinder.mixin.client;

import hellozyemlya.common.ClientItem;
import hellozyemlya.common.ItemHasClientItem;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemHasClientItem {
    @Unique
    private ClientItem clientItem;

    @Override
    public void setClientItem(@NotNull ClientItem clientItem) {
        if(this.clientItem != null) {
            throw new IllegalStateException("Item already has ClientItem instance.");
        }

        this.clientItem = clientItem;
    }

    @Override
    public ClientItem getClientItem() {
        return clientItem;
    }
}
