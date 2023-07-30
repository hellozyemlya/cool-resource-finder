package hellozyemlya.resourcefinder.mixin;

import hellozyemlya.resourcefinder.DefaultedListWithSetCallback;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Redirect(method = "<init>", at=@At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;ofSize(ILjava/lang/Object;)Lnet/minecraft/util/collection/DefaultedList;"))
    private <E> DefaultedList<E>  redirectCreation(int size, E defaultValue){
        return (DefaultedList<E>)DefaultedListWithSetCallback.createOfSize(size, (ItemStack)defaultValue);
    }
}
