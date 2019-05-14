package cn.tju.modelsearch.utils;

import net.semanticmetadata.lire.indexers.hashing.BitSampling;

import java.io.*;
import java.util.Arrays;

public class OldFeature1000 {

	public static int DEFAULT_NUMBER_OF_BINS = 3300;
	private static double[] histogram = new double[DEFAULT_NUMBER_OF_BINS];

	public static String obj2Hashcode(String objpath) {
		double[] objfeature = OldFeature1000.extract(objpath);
//		System.out.println("objfeature:"+Arrays.toString(objfeature));
//		System.out.println("hh");
		String hashcode = "";
		try {
			if(objfeature==null) {
				System.out.println("objfeature is null");
				return null;
			}
			hashcode = OldFeature1000.HashExtraction(objfeature);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return hashcode;

	}

	public  static double[] extract(String objPath) {

		String exeDir = ProjectConstant.EXEPATH+"3D_DATA/3DAlignment_v1.8";
		String exePath = ProjectConstant.EXEPATH+"3D_DATA/3DAlignment_v1.8/3DAlignment_art.exe";
		//运行前需要先创建outputDir
		String outputDir = ProjectConstant.EXEPATH+"3D_DATA/temp";
		String outputName = System.currentTimeMillis() + ".pd";

		File dir = new File(exeDir);
		String command = exePath + " " + objPath + " " + outputDir + " " + outputName;
		Runtime rt = Runtime.getRuntime();

		try {

			Process p = rt.exec(command, null, dir);
//			System.out.println(p);
			File f = new File(outputDir + "\\" + outputName);
			long t1 = System.currentTimeMillis();
			while (!f.exists()) {
				if(System.currentTimeMillis()-t1>20*1000) {
					p.destroyForcibly();
					return null;
				}

			}

			while (f.length()<1&&System.currentTimeMillis()-t1<20*1000) {
				if(System.currentTimeMillis()-t1>19*1000) {
					p.destroyForcibly();
					return null;
				}

			}


			String st1 = outputDir + "\\" + outputName;
//			System.out.println(outputDir + "\\" + outputName);

			FileInputStream inputStream = new FileInputStream(st1);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			int i = 0;
			// for (int i = 0; i < 3300; i++) {
			while ((line = bufferedReader.readLine()) != null && line.length() > 0) {
				// System.out.println(Integer.parseInt(bufferedReader.readLine()));
				histogram[i] = Integer.parseInt(line);
				// System.out.println("line:"+i+"--"+histogram[i]);
				i = i + 1;
			}
			bufferedReader.close();
		} catch (IOException e) {
			System.out.println("Error");
			e.printStackTrace();
		}
		return histogram;
	}

	public static String HashExtraction(double[] feature) throws Exception {

		BitSampling.readHashFunctions();
		double[] array1 = new double[640];
		int n = (int) Math.round(feature.length / 640 + 0.5);
		String[] tmp = new String[n];
		StringBuilder strbld = new StringBuilder();

		for (int i = 0; i < n; i++) {
			array1 = Arrays.copyOfRange(feature, i * 640, (i + 1) * 640);
			tmp[i] = arrayToString(BitSampling.generateHashes(array1));
//			System.out.println(tmp[i].length());
			strbld.append(tmp[i]);
		}
		String hashcode = strbld.toString();
		return hashcode;
	}

	public static String arrayToString(int[] array) {
		StringBuilder sb = new StringBuilder(array.length * 8);
		for (int i = 0; i < array.length; i++) {
			if (i > 0)
				sb.append(' ');
			sb.append(Integer.toHexString(array[i]));
		}
		return sb.toString();
	}

	public static String TrimStr(String str) {
		StringBuilder strbld = new StringBuilder();
		int n = str.length();
		int t = n % 4;
		strbld.append(str.substring(0, n - t));
		System.out.println(strbld.length());
		return strbld.toString();

	}
}