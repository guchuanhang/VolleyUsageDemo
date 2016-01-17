package gu.chuan.hang.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gu.chuan.hang.bean.AudioBean;
import gu.chuan.hang.tool.AssembleUrlUtil;

/**
 * 
 这种Gson解析方式灵活而复杂； *
 */
public class AudioDeserializer implements JsonDeserializer<AudioBean> {
	@Override
	public AudioBean deserialize(final JsonElement json, final Type typeOfT,
			final JsonDeserializationContext context) {
		final JsonObject jsonObject = json.getAsJsonObject();
		AudioBean audioBean = new AudioBean();
		// ///////1.解析Id
		JsonElement idElement = jsonObject.get("RowKey");
		String id = "";
		if (idElement != null) {
			id = idElement.getAsString();
		}
		audioBean.setId(id);
		// ///////2.解析icon
		JsonElement iconElement = jsonObject.get("Icon");
		String iconUrl = "";
		if (iconElement != null) {
			// 通过解析的灵活性，在这里组装图片真正的url；
			iconUrl = AssembleUrlUtil.formURL(iconElement.getAsString());
		}
		audioBean.setIcon(iconUrl);
		// ////////3.解析name；
		JsonElement nameElement = jsonObject.get("Name");
		String name = "";
		if (nameElement != null) {
			name = nameElement.getAsString();
		}
		audioBean.setName(name);
		// ///////
		return audioBean;
	}
}