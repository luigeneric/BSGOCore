package io.github.luigeneric.core.dradisverification;


import io.github.luigeneric.linearalgebra.utility.Mathf;
import jakarta.enterprise.context.Dependent;

@Dependent
public class DradisData
{
    public final static float DEFAULT_TIME_SECONDS = 60f;
    private DradisUpdate current;
    private DradisUpdate last;
    public boolean updateDradis(final DradisUpdate dradisUpdate)
    {
        this.last = this.current;
        this.current = dradisUpdate;
        return this.checkIsTimeOkay();
    }

    public boolean checkIsTimeOkay()
    {
        if (this.current == null || this.last == null)
        {
            return true;
        }
        final long diff = this.current.time() - this.last.time();
        final float timeSeconds = diff * 0.001f;
        return Mathf.isInsideValues(timeSeconds, DEFAULT_TIME_SECONDS-5, DEFAULT_TIME_SECONDS+5);
    }

    public DradisUpdate getCurrent()
    {
        return current;
    }

    public DradisUpdate getLast()
    {
        return last;
    }
}