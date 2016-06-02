package tool;

/**
 * 字符串实用类
 * 
 * @author LiuShuai
 * 
 */
public class StringUtil {
	/**
	 * 判断字符串是否为空
	 * 
	 * @param string
	 * @return boolean(为空返回true，不为空返回false)
	 */
	public static boolean isEmpty(String string) {
		if (string == null || string.trim().equals("")) {
			return true;
		}

		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
				return false;
			}
		}
		return true;

	}

	/**
	 * 判断多个字符串是否为空
	 * 
	 * @param 多个string
	 * @return boolean(为空返回true，不为空返回false)
	 */
	public static boolean isEmpty(String... strings) {
		for (int i = 0; i < strings.length; i++) {
			if (!isEmpty(strings[i])) {
				return false;
			}
		}

		return true;

	}

}
