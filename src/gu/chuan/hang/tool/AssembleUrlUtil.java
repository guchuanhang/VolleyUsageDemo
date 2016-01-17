package gu.chuan.hang.tool;

public class AssembleUrlUtil {
	static String file_download="http://www.dingdongfm.com/EnterpriseServerREST/FileDownLoadServlet";

	/**
	 * 根据GUID获取文件的address
	 */
	public static String formURL(String guid) {
		String url = guid;
		if (!guid.contains("http")) {
			url = file_download + "?guid=" + guid
					+ "&time=99991230000000";
		}
		return url;
	}

}
