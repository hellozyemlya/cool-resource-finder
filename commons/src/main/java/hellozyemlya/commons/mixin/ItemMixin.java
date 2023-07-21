package hellozyemlya.commons.mixin;

import hellozyemlya.commons.ItemSuperPuperExtension;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ItemMixin implements ItemSuperPuperExtension {

    @Inject(method = "<init>", at=@At("TAIL"))
    private void inject(Item.Settings settings, CallbackInfo ci) {
        superPuperMethod();
    }

    @Override
    public void superPuperMethod() {
        System.out.println("Item created");
    }
}
