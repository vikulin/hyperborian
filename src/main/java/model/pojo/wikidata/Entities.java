package model.pojo.wikidata;

import java.util.List;
import java.util.Map;

public class Entities {
	
    private String ns;

    private String modified;

    private Map<String, Sitelinks> sitelinks;

    private String id;

    private String pageid;

    private String title;

    private String type;

    private String lastrevid;
    
    private Map<String,Labels> labels;
    
    private Map<String,List<P31>> claims;

    public String getNs ()
    {
        return ns;
    }

    public void setNs (String ns)
    {
        this.ns = ns;
    }

   
    public String getModified ()
    {
        return modified;
    }

    public void setModified (String modified)
    {
        this.modified = modified;
    }

    public Map<String, Sitelinks> getSitelinks ()
    {
        return sitelinks;
    }

    public void setSitelinks (Map<String, Sitelinks> sitelinks)
    {
        this.sitelinks = sitelinks;
    }

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public String getPageid ()
    {
        return pageid;
    }

    public void setPageid (String pageid)
    {
        this.pageid = pageid;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    public String getLastrevid ()
    {
        return lastrevid;
    }

    public void setLastrevid (String lastrevid)
    {
        this.lastrevid = lastrevid;
    }

    public Map<String,Labels> getLabels ()
    {
        return labels;
    }

    public void setLabels (Map<String,Labels> labels)
    {
        this.labels = labels;
    }

	public Map<String, List<P31>> getClaims() {
		return claims;
	}

	public void setClaims(Map<String, List<P31>> claims) {
		this.claims = claims;
	}
}
