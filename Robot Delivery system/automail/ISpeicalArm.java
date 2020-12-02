package automail;

public interface ISpeicalArm {

    static final int WRAPPING_TIME = 2;
    static final int UNWRAPPING_TIME = 1;

    boolean isWrapped();
    boolean isUnwrapped();

    boolean isWrapping();
    boolean isUnwrapping();

    void startWrapping();
    void startUnwrapping();

}
