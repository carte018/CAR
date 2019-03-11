package edu.internet2.consent.copsu.model;

import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class ChangeOrderMetaData {
	@JsonProperty("changeOrderId")
	private String changeOrderId;  // unversioned for change orders
	@Embedded
	@JsonProperty("creator")
	private UserId creator;  // standard userId value
	@JsonProperty("createTime")
	private long createTime;
	
	public String getChangeOrderId() {
		return changeOrderId;
	}
	public void setChangeOrderId(String changeOrderId) {
		this.changeOrderId = changeOrderId;
	}
	public UserId getCreator() {
		return creator;
	}
	public void setCreator(UserId creator) {
		this.creator = creator;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ChangeOrderMetaData com = (ChangeOrderMetaData) o;
		return (com.getChangeOrderId().equals(this.getChangeOrderId()) && (com.getCreateTime() == this.getCreateTime()) && com.getCreator().equals(this.getCreator()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(changeOrderId,creator,createTime);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ChangeOrderMetaData {\n");
		sb.append("    changeOrderId: ").append(changeOrderId).append("\n");
		sb.append("    creator: ").append(creator.toString()).append("\n");
		sb.append("    createTime: ").append(createTime).append("\n");
		return sb.toString();
	}
}
