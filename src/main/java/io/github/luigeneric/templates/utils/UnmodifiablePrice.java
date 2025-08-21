package io.github.luigeneric.templates.utils;

public class UnmodifiablePrice extends Price
{

    public UnmodifiablePrice(final Price buyPrice)
    {
        super(buyPrice.getItems());
    }

    @Override
    public void addPrice(Price price, long count)
    {
        throw new UnsupportedOperationException("Unmodifiable Price!");
    }

    @Override
    public void addPrice(Price price)
    {
        throw new UnsupportedOperationException("Unmodifiable Price!");
    }

    @Override
    public void addItem(long cardGuid, long count)
    {
        throw new UnsupportedOperationException("Unmodifiable Price!");
    }
}
