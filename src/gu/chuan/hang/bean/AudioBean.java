package gu.chuan.hang.bean;

import com.google.gson.annotations.SerializedName;

public class AudioBean {
	@SerializedName("RowKey")
	private String id;
	@SerializedName("Name")
	private String name;
	@SerializedName("Icon")
	private String icon;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

}