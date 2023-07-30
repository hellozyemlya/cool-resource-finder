package hellozyemlya.resourcefinder;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class DefaultedListWithSetCallback extends DefaultedList<ItemStack> {
    protected DefaultedListWithSetCallback(List<ItemStack> delegate, @Nullable ItemStack initialElement) {
        super(delegate, initialElement);
    }

    @Override
    public ItemStack set(int index, ItemStack element) {
        ItemStack previousStack = super.set(index, element);
        System.out.printf("Replaced %s with %s\n", previousStack.toString(), element.toString());
        return previousStack;
    }

    public static DefaultedList<ItemStack> createOfSize(int size, ItemStack defaultValue) {
        Validate.notNull(defaultValue);
        ItemStack[] objects = new ItemStack[size];
        Arrays.fill(objects, defaultValue);
        return new DefaultedListWithSetCallback(Arrays.asList(objects), defaultValue);
    }
}
