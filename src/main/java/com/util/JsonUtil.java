package com.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.json.JsonIO;

public class JsonUtil {
	private static Random random = new Random();

	// 将一个Java对象转换成另一个目标对象，失败抛异常
	public static <T> T objectToObjectByJson(Object originObject, Class<T> elementClass) {
		try {
			if (null == originObject)
				return null;
			String str = objectToStringByJson(originObject);
			T json = stringToObjectByJson(str, elementClass);
			return json;
		} catch (Exception e) {
			throw new RuntimeException("将一个Java对象转换成另一个目标对象失败：" + e.getMessage());
		}
	}

	// 将传入的对象直接Json化，返回字符串
	public static String objectToStringByJson(Object it) {
		try {
			ObjectMapper mapper = new ObjectMapper();
//			mapper.enable(SerializationFeature.WRITE_NULL_MAP_VALUES);
			String str = mapper.writeValueAsString(it);
			return str;
		} catch (Exception e) {
			throw new RuntimeException("Json化Java对象时出错：" + e.getMessage());
		}

	}

	// 将一个字符串转换成给定的泛型对象
	public static <T> T stringToObjectByJson(String str, Class<T> elementClass) {
		try {
			if (null == str)
				return null;
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			T json = mapper.readValue(str, elementClass);
			return json;
		} catch (Exception e) {
			throw new RuntimeException("将一个字符串转换成给定的业务对象失败：" + e.getMessage());
		}
	}

	// 从一个list中选取第start——end编号中的全部元素（编号从1开始）
	public static <T> List<T> rangeList(List<T> list, int start, int end) {
		if (null == list)
			return new ArrayList<T>();
		List<T> result = new ArrayList<T>();
		start--;
		end--;
		for (int i = start; i <= end; i++)
			if (i < list.size() && i >= 0)
				result.add(list.get(i));
			else
				break;
		// System.out.println("range list size:" + result.size());
		return result;
	}

	public static <TBusinessObject, TJsonObject> List<TJsonObject> convertJsonList(
			JsonIO<TBusinessObject, TJsonObject> jsonHandler, List<TBusinessObject> list) {
		if (null == list)
			return new ArrayList<TJsonObject>();
		List<TJsonObject> resultList = new ArrayList<TJsonObject>();
		for (TBusinessObject each : list)
			resultList.add(jsonHandler.handleOutput(each));
		return resultList;
	}

	public static List stringToList(String str, Class<?> elementClasses) throws Exception {
		if (str == null)
			return new ArrayList();
		ObjectMapper mapper = new ObjectMapper();
		// 设置输入时忽略JSON字符串中存在而POJO对象实际没有的属性,或者在POJO对象中存在而在Json字符串中没有的属性。
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, elementClasses);
		List lst = mapper.readValue(str, javaType);
		return lst;
	}

	public static String blankToNull(String str) {
		if (null == str)
			return null;
		str = str.trim();
		if (str.equals(""))
			return null;
		if (str.equals("null"))
			return null;
		return str;
	}

	/**
	 * 计算resultSet中的记录总个数返回
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	public static int resultSetLength(ResultSet rs) throws Exception {
		if (null == rs)
			return 0;
		int sum = 0;
		// 遍历ResultSet中的每条数据
		while (rs.next()) {
			sum++;
		}
		System.out.println("ResultSet sum:" + sum);
		return sum;
		// return DBHelper.getRowSum(rs);
		// rs.last();
		// return rs.getRow();
	}

	/**
	 * 将resultSet转化为JSON数组
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	public static JSONArray resultSetToJsonArry(ResultSet rs) throws Exception {
		// json数组
		JSONArray array = new JSONArray();
		if (null == rs)
			return array;

		// 获取列数
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		int sum = 0;
		// 遍历ResultSet中的每条数据
		while (rs.next()) {
			JSONObject jsonObj = new JSONObject();

			// 遍历每一列
			for (int i = 1; i <= columnCount; i++) {
				String columnName = metaData.getColumnLabel(i);
				String value = rs.getString(columnName);
				System.out.print(columnName + ":" + value + ",");
				jsonObj.put(columnName, value);
			}
			array.put(jsonObj);
			System.out.println();
			sum++;
		}
		System.out.println("ResultSet sum:" + sum);
		return array;
	}

	/**
	 * 将resultSet的表头字段转化为表头列表
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	public static List<String> resultSetToHeaderList(ResultSet rs) throws Exception {
		List<String> headerList = new ArrayList<String>();
		// 获取列数
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		System.out.println("columnCount=" + columnCount);
		// 遍历ResultSet中的每个表头字段
		for (int i = 1; i <= columnCount; i++) {
			String columnName = metaData.getColumnLabel(i);
			headerList.add(columnName);
		}
		System.out.println("ResultSet header sum:" + headerList);
		return headerList;
	}

	/**
	 * 将resultSet转化为JSON数组，其中的null取值自动设置为0
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	public static JSONArray resultSetToJsonArryWithNullToZero(ResultSet rs) throws Exception {
		// json数组
		JSONArray array = new JSONArray();
		if (null == rs)
			return array;

		// 获取列数
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		int sum = 0;
		// 遍历ResultSet中的每条数据
		while (rs.next()) {
			JSONObject jsonObj = new JSONObject();

			// 遍历每一列
			for (int i = 1; i <= columnCount; i++) {
				String columnName = metaData.getColumnLabel(i);
				String value = rs.getString(columnName);
				System.out.print(columnName + ":" + value + ",");
				if (null == value)
					value = "0";
				jsonObj.put(columnName, value);
			}
			array.put(jsonObj);
			System.out.println();
			sum++;
		}
		System.out.println("ResultSet sum:" + sum);
		return array;
	}

	/**
	 * 将resultSet转化为JSON数组，其中的null取值自动设置为0
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	public static JSONArray resultSetToJsonArryWithNullToZero(ResultSet rs, int start, int end) throws Exception {
		// json数组
		JSONArray array = new JSONArray();
		if (null == rs)
			return array;

		// 获取列数
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		int sum = 0;
		int index = 0;
		start--;
		end--;
		// 遍历ResultSet中的每条数据
		while (rs.next()) {
			if (index < start || index > end) {
				index++;
				continue;
			}
			JSONObject jsonObj = new JSONObject();

			// 遍历每一列
			for (int i = 1; i <= columnCount; i++) {
				String columnName = metaData.getColumnLabel(i);
				String value = rs.getString(columnName);
				System.out.print(columnName + ":" + value + ",");
				if (null == value)
					value = "0";
				jsonObj.put(columnName, value);
			}
			array.put(jsonObj);
			System.out.println();
			sum++;
			index++;
		}
		System.out.println("ResultSet sum:" + sum);
		return array;
	}

	public static JSONArray resultSetToJsonArry(ResultSet rs, int start, int end) throws Exception {
		// json数组
		JSONArray array = new JSONArray();
		if (null == rs)
			return array;

		// 获取列数
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		int index = 0;
		start--;
		end--;
		// 遍历ResultSet中的每条数据
		while (rs.next()) {
			if (index < start || index > end) {
				index++;
				continue;
			}
			JSONObject jsonObj = new JSONObject();

			// 遍历每一列
			for (int i = 1; i <= columnCount; i++) {
				String columnName = metaData.getColumnLabel(i);
				String value = rs.getString(columnName);
				System.out.print(columnName + ":" + value + ",");
				jsonObj.put(columnName, value);
			}
			array.put(jsonObj);
			System.out.println();
			index++;
		}
		return array;
	}

	/**
	 * 将resultSet转化为JSONObject
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	public static JSONObject resultSetToJsonObject(ResultSet rs) throws Exception {
		// json对象
		JSONObject jsonObj = new JSONObject();
		// 获取列数
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		// 遍历ResultSet中的每条数据
		if (rs.next()) {
			// 遍历每一列
			for (int i = 1; i <= columnCount; i++) {
				String columnName = metaData.getColumnLabel(i);
				String value = rs.getString(columnName);
				jsonObj.put(columnName, value);
			}
		}
		return jsonObj;
	}

}
