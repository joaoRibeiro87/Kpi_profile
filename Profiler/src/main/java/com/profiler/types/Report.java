package com.profiler.types;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

public class Report {

	private  UUID id;
	private  String kpiReport; 
	private  BigInteger value;
	private  Timestamp timestamp;


	public Report(UUID id, String kpiReport, BigInteger value, Timestamp timestamp ) {
		this.id = id;
		this.kpiReport = kpiReport;
		this.value =value;
		this.timestamp =timestamp;
	}


	public UUID getId() {
		return id;
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
	
	public void setkKpiReport(String kpiReport) {
		this.kpiReport = kpiReport;
	}
	
	public void setValue(BigInteger value) {
		this.value = value;
	}

	public void getTimestamp(Timestamp timestamp) {
		 this.timestamp = timestamp;
	}
}