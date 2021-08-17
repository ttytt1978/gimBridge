package com.net;

import java.util.List;

//业务对象泛型响应器
public interface IBusinessResponseListener<T>
{
	void updateSuccess(List<T> newList);//成功得到一组业务对象
	void updateError(String errorMessage);//响应失败的消息
}
