package gu.chuan.hang.bean;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * 用于解析从服务器返回的数据
 * Gson解析的时候，如果不使用“@SerializedName（”key“）"进行映射，则变量名和网络数据中的key必须相同
 * 为了Java里面的规范（变量首字母小写），这是进行了映射；
 * 
 */
public class ResponseBean {
	@SerializedName("ErrorCode")
	private String errorCode;
	@SerializedName("ErrorString")
	private String errorString;
	@SerializedName("ResponseObject")
	private JsonObject responseObject;

	@Override
	public String toString() {
		return "ErrorCode:" + errorCode + "	ErrorString:" + errorString
				+ "	ResponseObject" + responseObject;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorString() {
		return errorString;
	}

	public void setErrorString(String errorString) {
		this.errorString = errorString;
	}

	public JsonObject getResponseObject() {
		return responseObject;
	}

	public void setResponseObject(JsonObject responseObject) {
		this.responseObject = responseObject;
	}

}
