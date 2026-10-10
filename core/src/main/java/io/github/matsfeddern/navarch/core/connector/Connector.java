package io.github.matsfeddern.navarch.core.connector;

public interface Connector<T> {
    Class<T> nativeType();
    T create(DriverConfig config);
    void destroy(T instance);
}
