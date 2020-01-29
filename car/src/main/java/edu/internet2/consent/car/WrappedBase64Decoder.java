package edu.internet2.consent.car;

import java.util.Base64;

// Convenience class to allow use of both Aapche and JDK-provided Base64 decoders

public class WrappedBase64Decoder {

	public static byte[] DecodeBase64(byte [] input) {
		return Base64.getDecoder().decode(input);
	}
	
	public static byte[] DecodeBase64(String input) {
		return Base64.getDecoder().decode(input);
	}
}
