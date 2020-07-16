package model.pojo.wikidata;

public class P31 {
	
	private Mainsnak mainsnak;

    private String rank;

    private String id;

    private String type;

    public Mainsnak getMainsnak ()
    {
        return mainsnak;
    }

    public void setMainsnak (Mainsnak mainsnak)
    {
        this.mainsnak = mainsnak;
    }

    public String getRank ()
    {
        return rank;
    }

    public void setRank (String rank)
    {
        this.rank = rank;
    }

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [mainsnak = "+mainsnak+", rank = "+rank+", id = "+id+", type = "+type+"]";
    }

}
