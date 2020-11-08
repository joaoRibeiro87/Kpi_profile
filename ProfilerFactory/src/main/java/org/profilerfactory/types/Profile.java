package org.profilerfactory.types;

import java.util.UUID;

public class Profile {
	private  UUID id;
	private  String reportId;
	private  String value;
	private  int bitrate;


	public Profile(UUID id, String value, String reportId, int bitrate) {
		this.id = id;
		this.value = value;
		this.reportId = reportId;
		this.bitrate = bitrate;
	}

	public Profile(String value) {
		this.value = value;
	}

	public UUID getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public int getBitrate() {
		return bitrate;
	}
	public String getReportId() {
		return reportId;
	}

	public void setReportId(String reportId) {
		this.reportId = reportId;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}
}
