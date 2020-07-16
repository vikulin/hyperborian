package model.pojo.wikidata;

import java.util.Map;

public class WikiData {
	
    private Map<String, Entities> entities;

    public Map<String, Entities> getEntities ()
    {
        return entities;
    }

    public void setEntities (Map<String, Entities> entities)
    {
        this.entities = entities;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [entities = "+entities+"]";
    }

}
