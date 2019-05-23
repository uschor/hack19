package gniza.beans;

public class FileGniza{
    private String[] lines;
    private String name;

    public FileGniza(String[] lines, String name)
    {
        this.lines = lines;
        this.name = name;
    }

    public String[] getLines()
    {
        return lines;
    }

    public String getName()
    {
        return name;
    }
}
