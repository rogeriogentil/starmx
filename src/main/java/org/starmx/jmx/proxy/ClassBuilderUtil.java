package org.starmx.jmx.proxy;

class ClassBuilderUtil {
	
	public static String decodeType(String encodedType) {
		int arrayDim = encodedType.lastIndexOf('[');
		if (arrayDim == -1)
			return encodedType;
		char arrayType = encodedType.charAt(arrayDim + 1);
		String decodedType = null;
		switch (arrayType) {
		case 'Z':
			decodedType = "boolean";
			break;
		case 'B':
			decodedType = "byte";
			break;
		case 'C':
			decodedType = "char";
			break;
		case 'D':
			decodedType = "double";
			break;
		case 'F':
			decodedType = "float";
			break;
		case 'I':
			decodedType = "int";
			break;
		case 'J':
			decodedType = "long";
			break;
		case 'S':
			decodedType = "short";
			break;
		case 'L':
			decodedType = encodedType.substring(arrayDim + 2, encodedType
					.length() - 1);
			break;
		}
		for (int i = 0; i <= arrayDim; i++) {
			decodedType += "[]";
		}
		return decodedType;
	}

}
