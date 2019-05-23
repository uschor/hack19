package gniza.logic;

import java.util.ArrayList;
import java.util.List;

class Tokenizer
{
    List<String> Tokenize(String text, boolean reverse)
    {
        String[] rows = text.split("\n");
        return Tokenize(rows,reverse);
    }
    List<String> Tokenize(String[] rows , boolean reverse)
    {
        List<String> result = new ArrayList<>();

        if (reverse) {
            for (int i = rows.length - 1; i >= 0; i--)
                AddRow(result, rows[i]);
        } else {
            for (int i = 0; i < rows.length; i++)
                AddRow(result, rows[i]);
        }
        return result;
    }

    private void AddRow(List<String> result, String row)
    {
        String[] tokens = row.split(" ");
        for (String token : tokens) {

            token=Fix(token);
            if (WordIsValid(token))
                result.add(token);
        }
    }

    private String Fix(String token)
    {
        return token
                .replace("[מחיקה]","?")
                .replace("[/מחיקה]","?")
                .replace("[/תוספת]","?")
                .replace("[פגום]","?")
                ;
    }

    private boolean WordIsValid(String token)
    {
        //TODO words not valid
        switch (token) {
            case "[ציור]":
            case "[/תוספת]":
            case "[פגום]":
            case "?":return false;
            default:return true;
        }
    }
}
