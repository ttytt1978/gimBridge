package com.json;

import java.util.Date;

public class UserSessionJson {
	public int sessionId;// 会话id（主键）
	public String code;// 会话中的验证码
	public byte[] codeImage;// 验证码对应的图像数据
	public Date sessionTime;// 会话开始时刻

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public byte[] getCodeImage() {
		return codeImage;
	}

	public void setCodeImage(byte[] codeImage) {
		this.codeImage = codeImage;
	}

	public Date getSessionTime() {
		return sessionTime;
	}

	public void setSessionTime(Date sessionTime) {
		this.sessionTime = sessionTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (sessionId ^ (sessionId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserSessionJson other = (UserSessionJson) obj;
		if (sessionId != other.sessionId)
			return false;
		return true;
	}

}
