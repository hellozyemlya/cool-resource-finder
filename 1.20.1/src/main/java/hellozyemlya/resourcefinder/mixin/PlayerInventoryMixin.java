package hellozyemlya.resourcefinder.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at=@At(value = "RETURN"))
    private void logInsert(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        System.out.printf("Stack %s inserted to inventory\n", stack.toString());
    }
}
