package io.github.luigeneric.linearalgebra.utility;

public class FloatWrapper
{
    private float value;

    public FloatWrapper(final float base)
    {
        this.value = base;
    }

    public FloatWrapper()
    {
        this(0);
    }

    public float getValue()
    {
        return value;
    }

    public float incrementAndGet(final float incrementBy)
    {
        this.value += incrementBy;
        return this.value;
    }

    public float decrementAndGet(final float decrementBy)
    {
        this.value -= decrementBy;
        return this.value;
    }

    public void setValue(float value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.value);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FloatWrapper that = (FloatWrapper) o;

        return Float.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode()
    {
        return (value != 0.0f ? Float.floatToIntBits(value) : 0);
    }
}
