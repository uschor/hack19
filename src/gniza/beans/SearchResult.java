package gniza.beans;

public class SearchResult
{
    private double Propability;
    private ReferenceDetail referenceDetail;

    public SearchResult(double propability, ReferenceDetail referenceDetail)
    {
        Propability = propability;
        this.referenceDetail = referenceDetail;
    }

    public SearchResult()
    {
    }

    public double getPropability()
    {
        return Propability;
    }

    public void setPropability(double propability)
    {
        Propability = propability;
    }

    public ReferenceDetail getReferenceDetail()
    {
        return referenceDetail;
    }

    public void setReferenceDetail(ReferenceDetail referenceDetail)
    {
        this.referenceDetail = referenceDetail;
    }
}
