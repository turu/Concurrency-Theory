package pl.agh.edu.tw.lab4;

/**
 * Author: Piotr Turek
 */
public class Resource<T> {
    private T value;
    private ResourceState state;

    public ResourceState getState() {
        return state;
    }

    public void setState(ResourceState state) {
        this.state = state;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "value=" + value +
                ", state=" + state +
                '}';
    }
}
