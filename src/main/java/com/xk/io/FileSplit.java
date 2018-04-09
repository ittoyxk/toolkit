package com.xk.io;

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author hengxiaokang
 * 
 */
public class FileSplit {

	/**
	 * 当前目录路径
	 */
	public static String currentWorkDir = System.getProperty("user.dir") + File.separator;

	/**
	 * 左填充
	 * 
	 * @param str
	 * @param length
	 * @param ch
	 * @return
	 */
	public static String leftPad(String str, int length, char ch) {
		if (str.length() >= length)
			return str;
		char[] chs = new char[length];
		Arrays.fill(chs, ch);
		char[] src = str.toCharArray();
		System.arraycopy(src, 0, chs, length - src.length, src.length);
		return new String(chs);
	}

	/**
	 * 删除文件
	 * 
	 * @param fileName
	 *            待删除文件的完整名称
	 * @return
	 */
	public static boolean delete(String fileName) {
		boolean result = false;
		File f = new File(fileName);
		if (f.exists()) {
			result = f.delete();
		} else {
			result = true;
		}
		return result;
	}

	/**
	 * 递归获取指定目录下的所有文件（不包括文件夹）
	 * 
	 * @param dirPath
	 * @return
	 */
	public static List<File> getAllFiles(String dirPath) {
		File dir = new File(dirPath);
		List<File> files = new ArrayList<File>();
		if (dir.isDirectory()) {
			File[] fileArr = dir.listFiles();
			for (int i = 0; i < fileArr.length; i++) {
				File f = fileArr[i];
				if (f.isFile()) {
					files.add(f);
				} else {
					files.addAll(getAllFiles(f.getPath()));
				}
			}
		}
		return files;
	}

	/**
	 * 获取指定目录下的所有文件（不包括子文件夹）
	 * 
	 * @param diePath
	 * @return
	 */
	public static List<File> getDirFiles(String dirPath) {
		File dir = new File(dirPath);
		File[] fileArr = dir.listFiles();
		List<File> files = new ArrayList<File>();
		for (File file : fileArr) {
			if (file.isFile())
				files.add(file);
		}
		return files;
	}

	/**
	 * 获取指定目录下特定文件后缀名的文件 （不包括子文件夹）
	 * 
	 * @param dirPath
	 * @param suffix
	 * @return
	 */
	public static List<File> getDirFiles(String dirPath, final String suffix) {
		File path = new File(dirPath);
		File[] fileArr = path.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				String lowerName = name.toLowerCase();
				String lowerSuffix = suffix.toLowerCase();
				if (lowerName.endsWith(lowerSuffix))
					return true;
				return false;
			}
		});
		List<File> files = new ArrayList<File>();
		for (File file : fileArr) {
			if (file.isFile())
				files.add(file);
		}
		return files;
	}

	/**
	 * 读取文件内容
	 * 
	 * @param fileName
	 *            带读取的完整文件名
	 * @return 文件内容
	 * @throws IOException
	 */
	public static String read(String fileName) throws IOException {
		File f = new File(fileName);
		FileInputStream fs = new FileInputStream(f);
		String result = null;
		byte[] b = new byte[fs.available()];
		fs.read(b);
		fs.close();
		result = new String(b);
		return result;
	}

	/**
	 * 写文件
	 * 
	 * @param fileName目标文件名
	 * @param fileContent写入的内容
	 * @return
	 * @throws IOException
	 */
	public static boolean write(String fileName, String fileContent) throws IOException {
		boolean result = false;
		File f = new File(fileName);
		FileOutputStream fs = new FileOutputStream(f);
		byte[] b = fileContent.getBytes();
		fs.write(b);
		fs.flush();
		fs.close();
		result = true;
		return result;
	}

	/**
	 * 追加内容到指定文件
	 * 
	 * @param fileName
	 * @param fileContent
	 * @return
	 * @throws IOException
	 */
	public static boolean append(String fileName, String fileContent) throws IOException {
		boolean result = false;
		File f = new File(fileName);
		if (f.exists()) {
			RandomAccessFile rFile = new RandomAccessFile(f, "rw");
			byte[] b = fileContent.getBytes();
			long originLen = f.length();
			rFile.setLength(originLen + b.length);
			rFile.write(b);
			rFile.close();
		}
		result = true;
		return result;
	}

	/**
	 * 拆分文件
	 * 
	 * @param fileName
	 *            待拆分的完整文件名
	 * @param byteSize
	 *            按多少字节大小拆分
	 * @return 拆分后的文件名列表
	 * @throws IOException
	 */
	public List<String> splitBySize(String fileName, long byteSize) throws IOException {
		List<String> parts = new ArrayList<String>();
		File file = new File(fileName);
		int count = (int) Math.ceil(file.length() / (double) byteSize);
		int countLen = (count + "").length();
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(count, count * 3, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(count * 2));
		for (int i = 0; i < count; i++) {
			String partFileName = file.getName() + "." + leftPad((i + 1) + "", countLen, '0') + ".part";
			threadPoolExecutor.execute(new SplitRunnable(byteSize, i * byteSize, partFileName, file));
			parts.add(partFileName);
		}
		threadPoolExecutor.shutdown();
		return parts;
	}

	/**
	 * 合并文件
	 * 
	 * @param dirPath
	 *            拆分文件所在目录
	 * @param partFileSuffix
	 *            拆分文件后缀名
	 * @param partFileSize
	 *            拆分文件的字节大小
	 * @param mergeFileName
	 *            合并后的文件名
	 * @throws IOException
	 */
	public void mergePartFiles(String dirPath, String partFileSuffix, int partFileSize, String mergeFileName) throws IOException {
		List<File> partFiles = FileSplit.getDirFiles(dirPath, partFileSuffix);
		Collections.sort(partFiles, new FileComparator());
		RandomAccessFile randomAccessFile = new RandomAccessFile(mergeFileName, "rw");
		randomAccessFile.setLength(partFileSize * (partFiles.size() - 1) + partFiles.get(partFiles.size() - 1).length());
		randomAccessFile.close();
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(partFiles.size(), partFiles.size() * 3, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(partFiles.size() * 2));
		for (int i = 0; i < partFiles.size(); i++) {
			threadPoolExecutor.execute(new MergeRunnable(i * partFileSize, mergeFileName, partFiles.get(i)));
		}
		threadPoolExecutor.shutdown();
	}

	/**
	 * 比较文件名
	 * 
	 * @author hengxiaokang
	 * 
	 */
	private class FileComparator implements Comparator<File> {

		@Override
		public int compare(File o1, File o2) {
			// TODO Auto-generated method stub
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}

	/**
	 * 分割处理
	 * 
	 * @author hengxiaokang
	 * 
	 */
	private class SplitRunnable implements Runnable {

		long byteSize;
		String partFileName;
		File originFile;
		long startPos;

		public SplitRunnable(long bytesize, long startPos, String partFileName, File originFile) {
			this.byteSize = bytesize;
			this.startPos = startPos;
			this.partFileName = partFileName;
			this.originFile = originFile;
		}

		@Override
		public void run() {
			RandomAccessFile rFile;
			OutputStream os;
			try {
				rFile = new RandomAccessFile(originFile, "r");
				int num = 1024 * 1024 * 10;
				byte[] b = new byte[num];
				int count = (int) Math.ceil((double) byteSize / (double) num);
				rFile.seek(startPos);
				int s = 0;
				os = new FileOutputStream(partFileName);
				int i = 0;
				while ((s = rFile.read(b)) > 0) {
					os.write(b, 0, s);
					i++;
					if (i == count)
						break;
				}
				os.flush();
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 合并处理
	 * 
	 * @author hengxiaokang
	 * 
	 */
	private class MergeRunnable implements Runnable {

		long startPos;
		String mergeFileName;
		File partFile;

		public MergeRunnable(long startPos, String mergeFileName, File partFile) {
			this.startPos = startPos;
			this.mergeFileName = mergeFileName;
			this.partFile = partFile;
		}

		@Override
		public void run() {
			RandomAccessFile rFile;
			try {
				rFile = new RandomAccessFile(mergeFileName, "rw");
				rFile.seek(startPos);
				FileInputStream fs = new FileInputStream(partFile);
				int num = 1024 * 1024 * 10;
				int count = (int) Math.ceil((double) fs.available() / (double) num);
				byte[] b = null;
				if (fs.available() > num)
					b = new byte[num];
				else
					b = new byte[fs.available()];
				int i = 0;
				while (fs.read(b) > 0) {
					rFile.write(b);
					i++;
					if (i == count)
						break;
				}
				fs.close();
				rFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
