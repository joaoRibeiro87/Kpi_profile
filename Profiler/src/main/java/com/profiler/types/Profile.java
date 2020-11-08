package com.profiler.types;

import java.math.BigInteger;
import java.util.UUID;

public class Profile {

	private  UUID id;
	private  String value;
	private  BigInteger bitrate;


	public Profile(UUID id, String value, BigInteger bitrate ) {
		this.id = id;
		this.value = value;
		this.bitrate = bitrate;
	}

	public UUID getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public BigInteger getBitrate() {
		return bitrate;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setBitrate(BigInteger bitrate) {
		this.bitrate = bitrate;
	}
}