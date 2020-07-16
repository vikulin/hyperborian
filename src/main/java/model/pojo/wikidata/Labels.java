package model.pojo.wikidata;

public class Labels {
	
	private String language;

    private String value;

    public String getLanguage ()
    {
        return language;
    }

    public void setLanguage (String language)
    {
        this.language = language;
    }

    public String getValue ()
    {
        return value;
    }

    public void setValue (String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [language = "+language+", value = "+value+"]";
    }

}
