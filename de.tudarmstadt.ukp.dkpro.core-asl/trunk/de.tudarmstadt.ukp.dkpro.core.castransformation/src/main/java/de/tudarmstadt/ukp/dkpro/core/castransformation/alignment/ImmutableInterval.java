package de.tudarmstadt.ukp.dkpro.core.castransformation.alignment;


public
class ImmutableInterval
extends AbstractInterval
{
    private final int	start;
    private final int	end;

    /**
     * Copy constructor.
     *
     * @param interval the original interval.
     */
    public
    ImmutableInterval(
    		final Interval interval)
    {
    	this(interval.getStart(), interval.getEnd());
    }

    /**
     * Constructor.
     *
     * @param s start offset.
     * @param e end offset.
     */
    public
    ImmutableInterval(
    		final int s,
    		final int e)
    {
		start = Math.min(s, e);
		end = Math.max(s, e);
	}

	public
    int getStart()
	{
		return start;
	}

	public
    int getEnd()
	{
		return end;
	}
}
