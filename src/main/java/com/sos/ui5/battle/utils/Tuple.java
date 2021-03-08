package com.sos.ui5.battle.utils;

import java.util.Objects;

public class Tuple<X, Y> {
	public final X x;
	public final Y y;

	public Tuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}

	public X getX() {
		return x;
	}

	public Y getY() {
		return y;
	}
	
	@Override
	public boolean equals(Object o) {
	    // self check
	    if (this == o)
	        return true;
	    // null check
	    if (o == null)
	        return false;
	    // type check and cast
	    if (getClass() != o.getClass())
	        return false;
	    @SuppressWarnings("unchecked")
			Tuple<X, Y> o2 = (Tuple<X, Y>) o;
	    // field comparison
	    return Objects.equals(x, o2.x)
	            && Objects.equals(y, o2.y);
	}
	
	@Override
	public int hashCode() {
	    return Objects.hash(x, y);
	}	
}