package gniza.logic;

import gniza.beans.ReferenceDetail;
import gniza.beans.SearchResult;

import java.util.ArrayList;
import java.util.List;

class ResultCalculator
{
    class ResultScore
    {
        public final ReferenceDetail referenceDetail;
        public final List<Double> Propabilities = new ArrayList<>();

        ResultScore(ReferenceDetail referenceDetail, double propability)
        {
            this.referenceDetail = referenceDetail;
            this.Propabilities.add(propability);
        }
    }

    List<SearchResult> CalculateResults(List<List<SearchResult>> allResults)
    {
        List<ResultScore> scores = new ArrayList<>();
        for (List<SearchResult> current : allResults) {
            for (SearchResult searchResult : current)
                AddScore(scores, searchResult);

        }
        List<SearchResult> result = Normalize(scores);
        return result;
    }


    private void AddScore(List<ResultScore> scores, SearchResult searchResult)
    {

        for (ResultScore score : scores) {
            if (ResultsAreSimilar(score.referenceDetail, searchResult.getReferenceDetail())) {
                score.Propabilities.add(searchResult.getPropability());
                return;
            }
            scores.add(new ResultScore(searchResult.getReferenceDetail(), searchResult.getPropability()));
        }
    }

    private boolean ResultsAreSimilar(ReferenceDetail first, ReferenceDetail second)
    {
        //TODO search for similarity not equals
        return first.getName().equals(second.getName()) && first.getTypeBook() == second.getTypeBook();
    }

    private List<SearchResult> Normalize(List<ResultScore> scores)
    {
        //TODO normalize correctly
        List<SearchResult> result = new ArrayList<>();
        for (ResultScore score : scores) {
            double num = 0;
            for (Double propability : score.Propabilities) {
                num += propability;
            }
            result.add(new SearchResult(num, score.referenceDetail));
        }
        return result;
    }

}
