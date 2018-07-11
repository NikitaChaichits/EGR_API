package com.mkyong.rest;

public class Education {

	String fullname;
	String shortname;
	String unp;
	String renameddate;

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getUnp() {
		return unp;
	}

	public void setUnp(String unp) {
		this.unp = unp;
	}

	public String getRenameddate() {
		return renameddate;
	}

	public void setRenameddate(String renameddate) {
		this.renameddate = renameddate;
	}

	@Override
	public String toString() {
		return "Education [fullname=" + fullname + ", shortname=" + shortname + ", unp=" + unp + "]";
}

}