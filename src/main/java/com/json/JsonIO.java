package com.json;

//展示层传入传出数据的统一处理接口：传入一个Json字符串
public interface JsonIO<TBusinessObject,TJsonObject> {

	//处理输入：将一个Json字符串转换为业务对象（以便于业务层处理）
	public abstract TBusinessObject handleInput(String jsonStr);

	//处理输出：将一个业务对象转换为另一个Json对象（以便于输出）
	public abstract TJsonObject handleOutput(TBusinessObject obj);
}
