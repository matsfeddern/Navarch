package io.github.matsfeddern.navarch.junit5;

public class TestNativeDriver {
    private final String label;

    public TestNativeDriver(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public int add(int a, int b) {
        return a + b;
    }

    public int crash() {
        throw new IllegalStateException("crash");
    }
}
