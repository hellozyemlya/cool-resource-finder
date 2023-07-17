package hellozyemlya.common;

import net.minecraft.client.util.math.MatrixStack;

@FunctionalInterface
public interface MatrixTransform {
    void transform(MatrixStack matrixStack);
}