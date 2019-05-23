package gniza.beans;

public class ReferenceDetail
{
    public ReferenceDetail(TypeBook typeBook, String name)
    {
        this.typeBook = typeBook;
        Name = name;
    }

    public ReferenceDetail()
    {
    }

    private TypeBook typeBook=TypeBook.Other;
    private String Name;

    public TypeBook getTypeBook()
    {
        return typeBook;
    }

    public void setTypeBook(TypeBook typeBook)
    {
        this.typeBook = typeBook;
    }

    public String getName()
    {
        return Name;
    }

    public void setName(String name)
    {
        Name = name;
    }

}
