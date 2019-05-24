package gniza.logic;

import gniza.beans.SearchResult;
import gniza.beans.TextSearcher;
import gniza.beans.WordsSearcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextSearcherImpl implements TextSearcher
{
    private final Tokenizer tokenizer=new Tokenizer();
    private final ResultCalculator resultCalculator=new ResultCalculator();
    private final WordsSearcher wordsSearcher;
    private final int groupWordLength;

    public TextSearcherImpl(WordsSearcher wordsSearcher, int groupWordLength)
    {
        this.wordsSearcher = wordsSearcher;
        this.groupWordLength = groupWordLength;
    }

    @Override
    public List<SearchResult> Search(String text) throws IOException
    {
        List<String> tokens = tokenizer.Tokenize(text, false);
        tokens.addAll(tokenizer.Tokenize(text, true));
        List<List<SearchResult>> allResults = SearchInWords(tokens);
        List<SearchResult> result = resultCalculator.CalculateResults(allResults);
        return result;

    }
    public List<SearchResult> Search(String[] lines) throws IOException
    {
        List<String> tokens = tokenizer.Tokenize(lines, false);
        tokens.addAll(tokenizer.Tokenize(lines, true));
        List<List<SearchResult>> allResults = SearchInWords(tokens);
        List<SearchResult> result = resultCalculator.CalculateResults(allResults);
        return result;

    }
    private List<List<SearchResult>> SearchInWords(List<String> tokens) throws IOException
    {
        List<List<SearchResult>> allResults = new ArrayList<>();
        int endWords = tokens.size() - (groupWordLength - 1);
        for (int i = 0; i < endWords; i++) {
            String[] nextWords = NextWords(tokens, i);
            List<SearchResult> currentResult = wordsSearcher.Search(tokens.size(),nextWords);
            if (currentResult.size() > 0)
                allResults.add(currentResult);
        }
        return allResults;
    }


    private String[] NextWords(List<String> tokens, int indexTokex)
    {
        String[] result = new String[groupWordLength];
        for (int i = 0; i < result.length; i++) {
            result[i] = tokens.get(indexTokex + i);
        }
        return result;
    }

}

