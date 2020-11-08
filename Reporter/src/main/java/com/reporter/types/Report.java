package com.reporter.types;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

public class Report {

	private  UUID id;
	private  String deviceName;
	private  String deviceType;
	private  String kpiReport; 
	private  BigInteger value;
	private  Timestamp timestamp;


	public Report(UUID id, String deviceName, String deviceType, String kpiReport, BigInteger value, Timestamp timestamp ) {
		this.id = id;
		this.deviceName = deviceName;
		this.deviceType = deviceType;
		this.kpiReport = kpiReport;
		this.value =value;
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

	public String getKpiReport() {
		return kpiReport;
	}

	public BigInteger getValue() {
		return value;
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
	
	public void setkKpiReport(String kpiReport) {
		this.kpiReport = kpiReport;
	}
	
	public void setBitrate(BigInteger value) {
		this.value = value;
	}

	public void getTimestamp(Timestamp timestamp) {
		 this.timestamp = timestamp;
	}
}