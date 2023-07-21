package hellozyemlya.resourcefinder.common;

/**
 * Holds {@link BakedQuadColorProvider} instance that will be used to get color for next rendered
 * {@link net.minecraft.client.render.model.BakedQuad}. If no {@link BakedQuadColorProvider} instance provided,
 * default MC/Fabric logic will be involved to get quad color.
 */
public class BakedQuadColorOverride {
    private static BakedQuadColorProvider override = null;

    public static void setOverride(BakedQuadColorProvider getter) {
        override = getter;
    }

    public static BakedQuadColorProvider getOverride() {
        return override;
    }
}
