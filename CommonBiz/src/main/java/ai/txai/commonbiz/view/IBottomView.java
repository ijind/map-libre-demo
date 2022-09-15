package ai.txai.commonbiz.view;

/**
 * Time: 09/03/2022
 * Author Hay
 */
public interface IBottomView {
    default void added() {
    }

    default void beforeRemove() {
    }

    default String getName() {
        return getClass().getSimpleName();
    }
}
