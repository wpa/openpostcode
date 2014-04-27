package org.eu.bitzone.openpostcode

public class OpenPostCodeUtil {

	private final static double WEST = 10.75;
	private final static double NORTH = 55.5;
	private final static double WIDE = 5.4;
	private final static double HIGH = 4.2;
	private final static String ALPHABET = "23456789CDFGHJKLMNPQRTVWX";
	private final static String BASE25 = "0123456789ABCDEFGHIJKLMNO";
	private static Envelope ingEnvelope = new Envelope(-13.4200, -5.2000, 48.1300, 56.7200);

	public static String getIrelandPostCode(double latitude, double longitude, int precision, boolean addCheckSum) {

		if (isInBngBoundary(longitude, latitude)) {

			double scale = Math.pow(5, precision);
			long x = (long) (((NORTH - Math.abs(latitude)) / HIGH) * scale);
			long y = (long) (((WEST - Math.abs(longitude)) / WIDE) * scale);
			String xBase5 = Long.toString(x, 5);
			String yBase5 = Long.toString(y, 5);
			long xBase = Long.valueOf(xBase5, 25);
			long yBase = Long.valueOf(yBase5, 25);
			String postcode = Long.toString(xBase * 5 + yBase, 25);
			return addCheckSum ? translate(postcode) + "/" + calculateCheckSum(postcode) : translate(postcode);
		} else {
			return "OUTSIDE ROI.";
		}
	}

	private static String translate(String commonEncodedPostcode) {
		StringBuffer sb = new StringBuffer();
		for (char codeElement : commonEncodedPostcode.toUpperCase().toCharArray()) {
			sb.append(ALPHABET.charAt(Integer.valueOf(codeElement + "", 25)));
		}
		return sb.toString();
	}

	private static int calculateCheckSum(String commonPostCode) {
		int index = 1;
		int sum = 0;
		for (char element : commonPostCode.toCharArray()) {
			int elementValue = Integer.valueOf(element + "", 25);
			sum = sum + (elementValue * index);
			index++;
		}
		return sum % 31;
	}

	public static boolean validatePostCode(String openPostCode) {
		String code = openPostCode.replaceAll("-", "");
		String postCode = "";
		int checkSum = Integer.MIN_VALUE;
		String[] codeWithCheckSum = code.split("/");
		if (codeWithCheckSum.length > 1) {
			postCode = codeWithCheckSum[0];
			checkSum = Integer.valueOf(codeWithCheckSum[1]);
		} else {
			throw new RuntimeException("No checksum. Validation failed");
		}

		String commonPostCode = translateToCommon(postCode);
		int calculatedCheckSum = calculateCheckSum(commonPostCode);
		if (checkSum != Integer.MIN_VALUE && checkSum != calculatedCheckSum) {
			throw new RuntimeException("Checksum error!!!");
		}

		return true;
	}

	public static double[] getPostCodeCoordinates(String openPostCode) {

		String code = openPostCode.replaceAll("-", "");
		String postCode = "";
		int checkSum = Integer.MIN_VALUE;
		String[] codeWithCheckSum = code.split("/");
		if (codeWithCheckSum.length > 1) {
			postCode = codeWithCheckSum[0];
			checkSum = Integer.valueOf(codeWithCheckSum[1]);
		} else {
			postCode = code;
		}

		double[] coordinates = new double[2];
		String commonPostCode = translateToCommon(postCode);
		int calculatedCheckSum = calculateCheckSum(commonPostCode);
		if (checkSum != Integer.MIN_VALUE && checkSum != calculatedCheckSum) {
			throw new RuntimeException("Checksum error!!!");
		}
		String zBase5 = Long.toString(Long.valueOf(commonPostCode, 25), 5);
		double scale = Math.pow(5, postCode.length());
		int i = 1;
		StringBuffer x = new StringBuffer();
		StringBuffer y = new StringBuffer();
		for (char element : zBase5.toCharArray()) {
			if (i % 2 == 0) {
				y.append(element);
			} else {
				x.append(element);
			}
			i++;
		}
		double lat = NORTH - (Long.valueOf(x.toString(), 5) + 0.5) / scale * HIGH;
		double lng = (Long.valueOf(y.toString(), 5) + 0.5) / scale * WIDE - WEST;
		coordinates[0] = lat;
		coordinates[1] = lng;
		return coordinates;
	}

	private static String translateToCommon(String postcode) {

		StringBuffer sb = new StringBuffer();
		for (char codeElement : postcode.toUpperCase().toCharArray()) {
			sb.append(BASE25.charAt(ALPHABET.indexOf(codeElement)));
		}
		return sb.toString();
	}

	public static boolean isInBngBoundary(double longitude, double latitude) {
		return ingEnvelope.covers(longitude, latitude);
	}

	static class Envelope {

		private double minx;

		private double maxx;

		private double miny;

		private double maxy;

		public Envelope(double x1, double x2, double y1, double y2) {
			init(x1, x2, y1, y2);
		}

		public void init(double x1, double x2, double y1, double y2) {
			if (x1 < x2) {
				minx = x1;
				maxx = x2;
			} else {
				minx = x2;
				maxx = x1;
			}
			if (y1 < y2) {
				miny = y1;
				maxy = y2;
			} else {
				miny = y2;
				maxy = y1;
			}
		}

		private boolean isNull() {
			return maxx < minx;
		}

		public boolean covers(double x, double y) {
			if (isNull())
				return false;
			return x >= minx && x <= maxx && y >= miny && y <= maxy;
		}
	}
}
