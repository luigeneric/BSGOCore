package io.github.luigeneric.utils;

import jakarta.enterprise.context.Dependent;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Dependent
public class BgoRandom extends Random
{

    public BgoRandom(){
        super();
    }
    public BgoRandom(final long seed)
    {
        super(seed);
    }

    /**
     * New Random number between min and max
     * @param min min inclusive bound
     * @param max max inclusive bound
     * @return the result between min and max
     */
    public int getRndBetweenInt(final int min, final int max)
    {
        if (min == max) return min;
        if (min > max)
            return getRndBetweenInt(max, min);
        return this.nextInt(max + 1 - min) + min;
    }
    public long getRndBetween(final long min, final long max)
    {
        if (min == max) return min;
        if (min > max)
            return getRndBetween(max, min);
        return this.nextLong(max+1-min)+min;
    }

    public boolean getBoolean()
    {
        return this.nextBoolean();
    }
    public float getRndNegPosOf(final float value)
    {
        return this.nextBoolean() ? value : -value;
    }

    /**
     * creates a new random float inside the bounds of min and max!
     * @param min min value
     * @param max max value
     * @return a value between min(inclusive) and max(inclusive)
     */
    public float getRndBetween(final float min, final float max)
    {
        if (min == max) return min;
        if (min > max)
            return getRndBetween(max, min);
        return this.nextFloat() * (max - min) + min;
    }
    public double getRndBetween(final double min, final double max)
    {
        if (min == max) return min;
        if (min > max)
            return getRndBetween(max, min);
        return this.nextDouble() * (max - min) + min;
    }
    public float getRndBetweenMinMax(final float value)
    {
        return getRndBetween(-value, value);
    }

    /**
     * Creates a new vector3 in array format which lies inside or at a and b bounds
     * @param a first boundary array
     * @param b second boundary array
     * @return a vector inside a and b (inclusive) as a float array of size 3
     * @throws NullPointerException if a or b is null
     * @throws IllegalArgumentException if the length of a or b is not equal to 3
     */
    public float[] getInsideVectors(final float[] a, final float[] b) throws NullPointerException, IllegalArgumentException
    {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        if (a.length != 3 || b.length != 3)
            throw new IllegalArgumentException("a or b not correct size!");

        final float[] rv = new float[3];
        for (int i = 0; i < 3; i++)
        {
            rv[i] = getRndBetween(a[i], b[i]);
        }
        return rv;
    }


    /**
     * Determines whether an action succeeds based on a given probability.
     * <p>
     * This method uses a random number to determine the success of the action.
     * A probability of 1 or higher always guarantees success, while a probability
     * of 0 always results in failure. For values in between, a random number in the range
     * of 0 to 1 is generated and compared to the specified probability.
     * </p>
     *
     * @param chance The probability of success. This should be a value between 0 and 1,
     *               inclusive. Values equal to or greater than 1 will always result in success,
     *               while values less than or equal to 0 will always result in failure.
     * @return true if the action is successful, false otherwise. Success is determined by
     *         generating a random number and checking if it is less than the specified probability.
     */
    public boolean rollChance(final float chance)
    {
        if (chance >= 1f)
            return true;
        return this.nextFloat() < chance;
    }


    /**
     * Determines whether an action succeeds based on a given probability.
     * <p>
     * This method uses a random number to determine the success of the action.
     * A probability of 1 or higher always guarantees success, while a probability
     * of 0 always results in failure. For values in between, a random number in the range
     * of 0 to 1 is generated and compared to the specified probability.
     * </p>
     *
     * @param chance The probability of success. This should be a value between 0 and 1,
     *               inclusive. Values equal to or greater than 1 will always result in success,
     *               while values less than or equal to 0 will always result in failure.
     * @return true if the action is successful, false otherwise. Success is determined by
     *         generating a random number and checking if it is less than the specified probability.
     */
    public boolean rollChance(final double chance)
    {
        if (chance >= 1.0)
            return true;
        return this.nextDouble() < chance;
    }


    /**
     * Get a random item of the given list
     * @param lst the list to use
     * @return An random item of the list
     * @param <T> list may be of any type
     * @throws NullPointerException if list param is null
     * @throws IllegalArgumentException if list size is empty
     */
    public <T> T getRandomItemOfList(final List<T> lst) throws NullPointerException, IllegalArgumentException
    {
        Objects.requireNonNull(lst, "List cannot be null!");

        if (lst.isEmpty())
            throw new IllegalArgumentException("List must contain atleast 1 item!");

        return lst.get(this.nextInt(lst.size()));
    }

    public <T> T getRandomItemOfCollection(final Collection<T> collection)
    {
        final int size = collection.size();
        final int randomPosition = nextInt(size);
        int cnt = 0;
        for (final T item : collection)
        {
            if (cnt == randomPosition)
            {
                return item;
            }
            cnt++;
        }
        throw new IllegalStateException("This should never happen");
    }

    /**
     * Variates by the given percentage value.
     * @param currentCount
     * @param percentage
     * @return the new count for the given value. minimum value is set to 1
     */
    public long variateByPercentage(final long currentCount, final float percentage)
    {
        if (percentage == 0) return currentCount;

        final float countPercent = currentCount * percentage;
        final float minCount = currentCount - countPercent;
        final float maxCount = currentCount + countPercent;
        return (long) Math.max(1f, getRndBetween(minCount, maxCount));
    }

    public <T> T getRandomArrayMember(final T[] arr)
    {
        return arr[this.nextInt(arr.length)];
    }
    public <T> T getRandomListMember(final List<T> list)
    {
        return list.get(this.nextInt(list.size()));
    }

}
