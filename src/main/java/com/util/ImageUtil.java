package com.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

//图像数据转换工具类
public class ImageUtil {

	//将一个文件中的数据存入字节数组
	public static  byte[] fileToBytes(File file) {
		byte[] data = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len;
			byte[] buffer = new byte[4096];
			while ((len = fis.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			data = baos.toByteArray();
			fis.close();
			baos.close();
		} catch (Exception e) {
			throw new RuntimeException("将一个文件中的数据存入字节数组错误：" + e);
		}
		return data;
	}

	// 将字节数组转换存储到一个文件里
	public static void bytesToFile(byte[] data, String fileName) {
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.write(data);
			fos.close();
		} catch (Exception e) {
			throw new RuntimeException("将字节数组存到文件时错误：" + e);
		}

	}

	// 将一个字符串（格式为"33,22,45,..."）转换成一个字节数组
	public static byte[] stringToBytes(String str) {
		try {
			String[] strs = str.split(",");
			byte[] data = new byte[strs.length];
			for (int i = 0; i < strs.length; i++) {
				data[i] = Byte.parseByte(strs[i]);
			}
			return data;
		} catch (Exception e) {
			throw new RuntimeException("转换字符串到字节数组时出错！" + e);
		}
	}

	public static String bytesToString(byte[] data) {
		try {
			if (data == null || data.length <= 0)
				return "";
			StringBuffer sb = new StringBuffer();
			sb.append(data[0]);
			for (int i = 1; i < data.length; i++) {
				byte b = data[i];
				sb.append(",");
				sb.append(b);
			}
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException("转换字节数组到字符串时出错！" + e);
		}
	}

	// 将一个Png图片文件转换成一个字节数组
	public static byte[] pngToBytes(String pngFileName) {
		try {
			BufferedImage image = ImageIO.read(new File(pngFileName));
			return imageToBytes(image);
		} catch (Exception e) {
			throw new RuntimeException("转换Png文件到字节数组时出错！" + e);
		}

	}

	// 将一个图像对象转换成字节数组
	public static byte[] imageToBytes(BufferedImage image) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			byte[] result = baos.toByteArray();
			baos.close();
			return result;
		} catch (Exception e) {
			throw new RuntimeException("转换图像对象到字节数组时出错！" + e);
		}

	}

	// 将一个Png图片文件按缩放比例转换成一个字节数组
	public static byte[] pngToBytes(String pngFileName, double scale) {
		try {
			BufferedImage image = ImageIO.read(new File(pngFileName));
			// 获取一个宽、长是原来scale的图像实例
			int width = image.getWidth();
			int height = image.getHeight();
			Image zoomedImage = image.getScaledInstance((int) (width * scale), (int) (height * scale), Image.SCALE_DEFAULT);
			// 缩放图像
			BufferedImage tag = new BufferedImage((int) (width * scale), (int) (height * scale), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = tag.createGraphics();
			g.drawImage(zoomedImage, 0, 0, null); // 绘制缩小后的图
			g.dispose();
			return imageToBytes(tag);
		} catch (Exception e) {
			throw new RuntimeException("转换Png文件到字节数组时出错！" + e);
		}

	}

	// 将一个Png图片文件按缩放比例转换成一个BufferedImage
	public static BufferedImage pngToImage(String pngFileName, double scale) {
		try {
			BufferedImage image = ImageIO.read(new File(pngFileName));
			// 获取一个宽、长是原来scale的图像实例
			int width = image.getWidth();
			int height = image.getHeight();
			Image zoomedImage = image.getScaledInstance((int) (width * scale), (int) (height * scale), Image.SCALE_DEFAULT);
			// 缩放图像
			BufferedImage tag = new BufferedImage((int) (width * scale), (int) (height * scale), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = tag.createGraphics();
			g.drawImage(zoomedImage, 0, 0, null); // 绘制缩小后的图
			g.dispose();
			return tag;
		} catch (Exception e) {
			throw new RuntimeException("转换Png文件到BufferedImage时出错！" + e);
		}

	}

	// 将一个字节数组保存成一个Png图片文件
	public static void bytesToPng(byte[] data, String pngFileName) {
		try {
			BufferedImage image = bytesToImage(data);
			ImageIO.write(image, "png", new File(pngFileName));
		} catch (Exception e) {
			throw new RuntimeException("转换字节数组到Png文件时出错！" + e);
		}

	}

	// 将一个字节数组保存成一个图像对象
	public static BufferedImage bytesToImage(byte[] data) throws IOException {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			BufferedImage image = ImageIO.read(bais);
			bais.close();
			return image;
		} catch (Exception e) {
			throw new RuntimeException("转换字节数组到图像对象时出错！" + e);
		}
	}

	// 将一个字节数组保存成一个图像对象
	public static BufferedImage bytesToImage(byte[] data, double scale) throws IOException {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			BufferedImage image = ImageIO.read(bais);
			bais.close();
			// 获取一个宽、长是原来scale的图像实例
			int width = image.getWidth();
			int height = image.getHeight();
			Image zoomedImage = image.getScaledInstance((int) (width * scale), (int) (height * scale), Image.SCALE_DEFAULT);
			// 缩放图像
			BufferedImage tag = new BufferedImage((int) (width * scale), (int) (height * scale), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = tag.createGraphics();
			g.drawImage(zoomedImage, 0, 0, null); // 绘制缩小后的图
			g.dispose();
			return tag;
		} catch (Exception e) {
			throw new RuntimeException("缩放字节数组到图像对象时出错！" + e);
		}
	}

	// 将给定的图像按给定比例缩放
	public static Image zoomImage(BufferedImage image, double zoomRation) {
		int width = (int) (image.getWidth() * zoomRation);
		int height = (int) (image.getHeight() * zoomRation);
		Image zoomedImage = image.getScaledInstance(width, height, Image.SCALE_FAST);
		return zoomedImage;
	}

	// 按给定的角度旋转图像，背景色为null则旋转后的背景透明
	public static BufferedImage rotateImg(BufferedImage image, int degree, Color bgcolor) throws IOException {

		int iw = image.getWidth();// 原始图象的宽度
		int ih = image.getHeight();// 原始图象的高度
		int w = 0;
		int h = 0;
		int x = 0;
		int y = 0;
		degree = degree % 360;
		if (degree < 0)
			degree = 360 + degree;// 将角度转换到0-360度之间
		double ang = Math.toRadians(degree);// 将角度转为弧度

		/**
		 * 确定旋转后的图象的高度和宽度
		 */

		if (degree == 180 || degree == 0 || degree == 360) {
			w = iw;
			h = ih;
		} else if (degree == 90 || degree == 270) {
			w = ih;
			h = iw;
		} else {
			int d = iw + ih;
			w = (int) (d * Math.abs(Math.cos(ang)));
			h = (int) (d * Math.abs(Math.sin(ang)));
		}

		x = (w / 2) - (iw / 2);// 确定原点坐标
		y = (h / 2) - (ih / 2);
		BufferedImage rotatedImage = new BufferedImage(w, h, image.getType());
		Graphics2D gs = (Graphics2D) rotatedImage.getGraphics();
		if (bgcolor == null) {
			rotatedImage = gs.getDeviceConfiguration().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
		} else {
			gs.setColor(bgcolor);
			gs.fillRect(0, 0, w, h);// 以给定颜色绘制旋转后图片的背景
		}

		AffineTransform at = new AffineTransform();
		at.rotate(ang, w / 2, h / 2);// 旋转图象
		at.translate(x, y);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
		op.filter(image, rotatedImage);
		image = rotatedImage;
		return rotatedImage;

	}

}
