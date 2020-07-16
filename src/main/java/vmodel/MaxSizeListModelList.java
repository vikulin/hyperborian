package vmodel;

import java.util.Collection;

import org.apache.commons.lang.NotImplementedException;
import org.zkoss.zul.ListModelList;

public class MaxSizeListModelList extends ListModelList<Object>{

	private static final long serialVersionUID = -8673643500776844380L;
	
	private int capasity;

	public MaxSizeListModelList(int capasity) {
		this.capasity = capasity;
	}
	
	@Override
	public boolean add(Object o) {
		if(size()>capasity) {
			this.remove(0);
		}
		return super.add(o);
	}
	
	@Override
	public void add(int index, Object element) {
		if(size()>capasity) {
			this.remove(index);
		}
		super.add(index, element);
	}
	
	@Override
	public boolean addAll(Collection c) {
		throw new NotImplementedException();
	}
	
	@Override
	public boolean addAll(int index, Collection c) {
		throw new NotImplementedException();
	}
}
