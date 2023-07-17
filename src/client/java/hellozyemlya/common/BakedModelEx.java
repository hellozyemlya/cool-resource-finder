package hellozyemlya.common;

import java.util.Optional;

public interface BakedModelEx {
    Optional<Integer> getColor();
    void setColor(Optional<Integer> color);
    MatrixTransform getMatrixTransform();
    void setMatrixTransform(MatrixTransform transform);
}
