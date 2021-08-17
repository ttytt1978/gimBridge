package com.listener;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.json.UserSessionJson;
import com.util.ImageUtil;
import com.util.ValidateCode;

//临时照片清理器
public class SessionManager {
	private static SessionManager instance = null;
	private long interval = 60 * 1;
	private long expire = 60 * 5;
	private Thread worker = null;
	private static Random random = new Random();
	private List<UserSessionJson> tempPhotos = new ArrayList<UserSessionJson>();

	private class CleanWork implements Runnable {

		public void run() {
			// 每隔一段时间，清理数据库中的临时照片，过期照片将被彻底删除
			System.out.println("临时验证码图像清理线程启动...");
			while (true) {
				try {
					synchronized (tempPhotos) {

						System.out.println("当前用户会话总数:" + tempPhotos.size());
						int deletedSum = 0;
						for (int i = tempPhotos.size() - 1; i >= 0; i--) {
							UserSessionJson photo = tempPhotos.get(i);
							Calendar now = Calendar.getInstance();
							now.add(Calendar.SECOND, (int) (-expire));
							Calendar uploadTime = Calendar.getInstance();
							uploadTime.setTime(photo.getSessionTime());
							if (uploadTime.compareTo(now) < 0) {
								tempPhotos.remove(photo);
								deletedSum++;
							}
						}
						System.out.println("删除的用户会话总数:" + deletedSum);

					}
					Thread.sleep(interval * 1000);
				} catch (Exception e) {
					e.printStackTrace();
					try {
						Thread.sleep(interval * 1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}

			}
		}
	}

	private SessionManager() {

	}

	public static SessionManager getInstance() {
		if (instance == null)
			instance = new SessionManager();
		return instance;
	}

	public void start() {
		if (null != worker)
			return;
		worker = new Thread(new CleanWork());
		worker.start();
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

	// 产生一个新用户会话对象
	public UserSessionJson createNewUserSession() {
		synchronized (tempPhotos) {
			UserSessionJson session = new UserSessionJson();
			session.setSessionId(random.nextInt());
			ValidateCode validateCode = new ValidateCode(189, 43, 4, 15);
			String checkcode = validateCode.getCode();
			session.setCode(checkcode);
			session.setCodeImage(ImageUtil.imageToBytes(validateCode.getBuffImg()));
			session.setSessionTime(new Date());
			tempPhotos.add(session);
			return session;
		}
	}

	// 产生一个n位的整数验证码
	private String createCheckCode(int n) {
		String checkcode = "";
		for (int i = 0; i < n; i++) {
			checkcode += random.nextInt(10);
		}
		return checkcode;
	}

	public static int r(int min, int max) {
		int num = 0;
		num = random.nextInt(max - min) + min;
		return num;
	}

	// 生成给定整数验证码的图像
	private BufferedImage createCheckCodeImage(String checkcode) {
		// 在内存中创建一副图片
		int w = 189;
		int h = 43;
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		// 在图片上画一个矩形当背景
		Graphics g = img.getGraphics();
		g.setColor(new Color(r(50, 250), r(50, 250), r(50, 250)));
		g.fillRect(0, 0, w, h);

		String str = checkcode;
		for (int i = 0; i < str.length(); i++) {
			g.setColor(new Color(r(50, 180), r(50, 180), r(50, 180)));
			g.setFont(new Font("黑体", Font.PLAIN, 40));
			char c = str.charAt(i);
			g.drawString(String.valueOf(c), 10 + i * 30, r(h - 30, h));
		}

		// 画随机线
		for (int i = 0; i < 25; i++) {
			g.setColor(new Color(r(50, 180), r(50, 180), r(50, 180)));
			g.drawLine(r(0, w), r(0, h), r(0, w), r(0, h));
		}
		return img;
	}

	// 获取给定sessionid的用户会话对象
	public UserSessionJson findUserSessionById(int sessionid) throws RuntimeException {
		synchronized (tempPhotos) {
			for (UserSessionJson session : tempPhotos)
				if (session.getSessionId() == sessionid)
					return session;
			throw new RuntimeException("验证码已经过期！");
		}
	}

}
