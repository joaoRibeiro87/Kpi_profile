package org.reporterfactory.types;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

public class Report {

	private  UUID id;
	private  String deviceName;
	private  String deviceType;
	private  BigInteger bitrate;
	private  Timestamp timestamp;


	public Report(UUID id, String deviceName, String deviceType, BigInteger bitrate, Timestamp timestamp ) {
		this.id = id;
		this.deviceName = deviceName;
		this.deviceType = deviceType;
		this.bitrate = bitrate;
		this.timestamp =timestamp;
	}

	public Report(String deviceName) {
		this.deviceName = deviceName;
	}

	public UUID getId() {
		return id;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public BigInteger getBitrate() {
		return bitrate;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public void setBitrate(BigInteger bitrate) {
		this.bitrate = bitrate;
	}

	public void getTimestamp(Timestamp timestamp) {
		 this.timestamp = timestamp;
	}
}