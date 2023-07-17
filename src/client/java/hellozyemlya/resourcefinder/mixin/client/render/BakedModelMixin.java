package hellozyemlya.resourcefinder.mixin.client.render;

import hellozyemlya.common.BakedModelEx;
import hellozyemlya.common.MatrixTransform;
import net.minecraft.client.render.model.BasicBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(BasicBakedModel.class)
public class BakedModelMixin implements BakedModelEx {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Unique
    private Optional<Integer> color = Optional.empty();
    @Unique
    private MatrixTransform matrixTransform;

    @Override
    public Optional<Integer> getColor() {
        return color;
    }

    @Override
    public void setColor(Optional<Integer> color) {
        this.color = color;
    }

    @Override
    public MatrixTransform getMatrixTransform() {
        return matrixTransform;
    }

    @Override
    public void setMatrixTransform(MatrixTransform transform) {
        this.matrixTransform = transform;
    }
}
