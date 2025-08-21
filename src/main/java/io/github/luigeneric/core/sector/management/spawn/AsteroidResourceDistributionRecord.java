package io.github.luigeneric.core.sector.management.spawn;

public record AsteroidResourceDistributionRecord(int asteroidCount, int redCount, int tylCount, int titaniumCount, int waterCount)
{

    public float tyliumPercentageOfResources()
    {
        if (countAsteroidsWithResources() == 0)
            return 0;
        return (float)tylCount / countAsteroidsWithResources();
    }
    public float titaniumPercentageOfResources()
    {
        if (countAsteroidsWithResources() == 0)
            return 0;
        return (float)titaniumCount / countAsteroidsWithResources();
    }

    public float waterPercentageOfResources()
    {
        if (countAsteroidsWithResources() == 0)
            return 0;
        return (float)waterCount / countAsteroidsWithResources();
    }
    public float redPercentage()
    {
        if (asteroidCount == 0)
            return 0;
        return (float) redCount / (float) asteroidCount;
    }
    public float countAsteroidsWithResources()
    {
        return this.asteroidCount - this.redCount;
    }

    @Override
    public String toString()
    {
        return "AsteroidResourceDistributionRecord{" +
                "asteroidCount=" + asteroidCount +
                ", redCount=" + redCount +
                ", redPercentage="+redPercentage()+
                ", tylCount=" + tylCount +
                ", tylPercentage="+tyliumPercentageOfResources()+
                ", titaniumCount=" + titaniumCount +
                ", titaniumPercentage="+titaniumPercentageOfResources()+
                ", waterCount=" + waterCount +
                ", waterPercentage="+waterPercentageOfResources()+
                '}';
    }
}
