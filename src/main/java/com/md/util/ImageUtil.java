package com.md.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

//ͼ������ת��������
public class ImageUtil {

	// ���ֽ�����ת���洢��һ���ļ���
	public static void bytesToFile(byte[] data, String fileName) {
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.write(data);
			fos.close();
		} catch (Exception e) {
			throw new RuntimeException("���ֽ�����浽�ļ�ʱ����" + e);
		}

	}

	// ��һ���ַ�������ʽΪ"33,22,45,..."��ת����һ���ֽ�����
	public static byte[] stringToBytes(String str) {
		try {
			String[] strs = str.split(",");
			byte[] data = new byte[strs.length];
			for (int i = 0; i < strs.length; i++) {
				data[i] = Byte.parseByte(strs[i]);
			}
			return data;
		} catch (Exception e) {
			throw new RuntimeException("ת���ַ������ֽ�����ʱ����" + e);
		}
	}

	public static byte[] stringToBytesForImage(String imageString) throws RuntimeException {
		// ����ת����Ƭ����
		try {
			byte[] data = ImageUtil.stringToBytes(imageString);
			BufferedImage img = ImageUtil.bytesToImage(data);
			if (img == null)
				throw new RuntimeException();
			return data;
		} catch (Exception e) {
			throw new RuntimeException("ת��ͼ�����ݳ���" + e.getMessage());
		}

	}

	// ��һ��PngͼƬ�ļ�ת����һ���ֽ�����
	public static byte[] pngToBytes(String pngFileName) {
		try {
			BufferedImage image = ImageIO.read(new File(pngFileName));
			return imageToBytes(image);
		} catch (Exception e) {
			throw new RuntimeException("ת��Png�ļ����ֽ�����ʱ����" + e);
		}

	}

	// ��һ��PngͼƬ�ļ�ת����һ���ֽ�����
	public static byte[] pngToBytes(File pngFile) {
		try {
			FileInputStream fileInputStream = new FileInputStream(pngFile);
			// ��ȡ�ļ���С�ֽ�
			int length = fileInputStream.available();
			// ��ȡ�ļ��ֽڵ�һ��������
			int bytesRead = 0;
			int bytesToRead = length;
			byte[] input = new byte[bytesToRead];
			while (bytesRead < bytesToRead) {
				int result = fileInputStream.read(input, bytesRead, bytesToRead - bytesRead);
				if (result == -1)
					break;
				bytesRead += result;
			}
			fileInputStream.close();
			return input;
		} catch (Exception e) {
			throw new RuntimeException("ת��Png�ļ����ֽ�����ʱ����" + e);
		}

	}

	// ��һ��ͼ�����ת�����ֽ�����
	public static byte[] imageToBytes(BufferedImage image) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			byte[] result = baos.toByteArray();
			baos.close();
			return result;
		} catch (Exception e) {
			throw new RuntimeException("ת��ͼ������ֽ�����ʱ����" + e);
		}

	}

	// ��һ��PngͼƬ�ļ������ű���ת����һ���ֽ�����
	public static byte[] pngToBytes(String pngFileName, double scale) {
		try {
			BufferedImage image = ImageIO.read(new File(pngFileName));
			// ��ȡһ��������ԭ��scale��ͼ��ʵ��
			int width = image.getWidth();
			int height = image.getHeight();
			Image zoomedImage = image.getScaledInstance((int) (width * scale), (int) (height * scale),
					Image.SCALE_DEFAULT);
			// ����ͼ��
			BufferedImage tag = new BufferedImage((int) (width * scale), (int) (height * scale),
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = tag.createGraphics();
			g.drawImage(zoomedImage, 0, 0, null); // ������С���ͼ
			g.dispose();
			return imageToBytes(tag);
		} catch (Exception e) {
			throw new RuntimeException("ת��Png�ļ����ֽ�����ʱ����" + e);
		}

	}

	// ��һ��byte[]ͼƬ�ļ������ű���ת����һ���ֽ�����
	public static byte[] zoomToBytes(byte[] imageData, double scale) {
		try {
			BufferedImage image = bytesToImage(imageData);
			// ��ȡһ��������ԭ��scale��ͼ��ʵ��
			int width = image.getWidth();
			int height = image.getHeight();
			Image zoomedImage = image.getScaledInstance((int) (width * scale), (int) (height * scale),
					Image.SCALE_DEFAULT);
			// ����ͼ��
			BufferedImage tag = new BufferedImage((int) (width * scale), (int) (height * scale),
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = tag.createGraphics();
			g.drawImage(zoomedImage, 0, 0, null); // ������С���ͼ
			g.dispose();
			return imageToBytes(tag);
		} catch (Exception e) {
			throw new RuntimeException("ת����ͼ��Сͼ���ֽ�����ʱ����" + e);
		}

	}

	// ��һ��byte[]ͼƬ�ļ����������ת����һ���ֽ�����
	public static byte[] zoomToBytes(byte[] imageData, int widthNeeded) {
		try {
			BufferedImage image = bytesToImage(imageData);
			// ��ȡһ��������ԭ��scale��ͼ��ʵ��
			int width = image.getWidth();
			int height = image.getHeight();
			// �������ű���
			double scale = widthNeeded * 1.0 / width;
			int heightNeeded = (int) (height * scale);
			Image zoomedImage = image.getScaledInstance(widthNeeded, heightNeeded, Image.SCALE_DEFAULT);
			// ����ͼ��
			BufferedImage tag = new BufferedImage(widthNeeded, heightNeeded, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = tag.createGraphics();
			g.drawImage(zoomedImage, 0, 0, null); // ������С���ͼ
			g.dispose();
			return imageToBytes(tag);
		} catch (Exception e) {
			throw new RuntimeException("ת����ͼ��Сͼ���ֽ�����ʱ����" + e);
		}

	}

	// ��һ���ֽ����鱣���һ��PngͼƬ�ļ�
	public static void bytesToPng(byte[] data, String pngFileName) {
		try {
			BufferedImage image = bytesToImage(data);
			ImageIO.write(image, "png", new File(pngFileName));
		} catch (Exception e) {
			throw new RuntimeException("ת���ֽ����鵽Png�ļ�ʱ����" + e);
		}

	}

	// ��һ���ֽ����鱣���һ��ͼ�����
	public static BufferedImage bytesToImage(byte[] data) throws IOException {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			BufferedImage image = ImageIO.read(bais);
			bais.close();
			return image;
		} catch (Exception e) {
			throw new RuntimeException("ת���ֽ����鵽ͼ�����ʱ����" + e);
		}
	}

	// ��������ͼ�񰴸�����������
	public static Image zoomImage(BufferedImage image, double zoomRation) {
		int width = (int) (image.getWidth() * zoomRation);
		int height = (int) (image.getHeight() * zoomRation);
		Image zoomedImage = image.getScaledInstance(width, height, Image.SCALE_FAST);
		return zoomedImage;
	}

	public static Object convertImage(BufferedImage image) {
		if (image == null)
			return null;
		return ImageUtil.imageToBytes(image);
	}

}
