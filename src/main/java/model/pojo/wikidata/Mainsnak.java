package model.pojo.wikidata;

public class Mainsnak {
	
	private String property;
	
	private PropertyValue datavalue;

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public PropertyValue getDatavalue() {
		return datavalue;
	}

	public void setDatavalue(PropertyValue datavalue) {
		this.datavalue = datavalue;
	}

}
