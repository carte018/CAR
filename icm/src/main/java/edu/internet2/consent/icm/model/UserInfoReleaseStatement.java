package edu.internet2.consent.icm.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
@Entity
public class UserInfoReleaseStatement {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long InfoIdKey;
	
	@JsonIgnore
	public Long getInfoIdKey() {
		return InfoIdKey;
	}
	@JsonIgnore
	public void setInfoIdKey(Long infoIdKey) {
		InfoIdKey = infoIdKey;
	}

	@Embedded @JsonProperty("infoId")
	private InfoId infoId;
	@JsonProperty("persistence")
	private String persistence;
	@JsonProperty("arrayOfDirectiveOnValues")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private List<UserDirectiveOnValues> arrayOfDirectiveOnValues;
	@Embedded @JsonProperty("directiveAllOtherValues")
	private UserDirectiveAllOtherValues userDirectiveAllOtherValues;
	
	public InfoId getInfoId() {
		return infoId;
	}
	public void setInfoId(InfoId infoId) {
		this.infoId = infoId;
	}
	public String getPersistence() {
		if (persistence != null) {
			return persistence;
		} else return "persist";
	}
	public void setPersistence(String persistence) {
		this.persistence = persistence;
	}
	public List<UserDirectiveOnValues> getArrayOfDirectiveOnValues() {
		return arrayOfDirectiveOnValues;
	}
	public void setArrayOfDirectiveOnValues(ArrayList<UserDirectiveOnValues> arrayOfDirectiveOnValues) {
		this.arrayOfDirectiveOnValues = arrayOfDirectiveOnValues;
	}
	public UserDirectiveAllOtherValues getUserDirectiveAllOtherValues() {
		return userDirectiveAllOtherValues;
	}
	public void setUserDirectiveAllOtherValues(UserDirectiveAllOtherValues userDirectiveAllOtherValues) {
		this.userDirectiveAllOtherValues = userDirectiveAllOtherValues;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		UserInfoReleaseStatement irs = (UserInfoReleaseStatement) o;
		return (irs.getInfoId().equals(this.getInfoId()) && irs.getUserDirectiveAllOtherValues().equals(this.getUserDirectiveAllOtherValues()) && irs.getArrayOfDirectiveOnValues().equals(this.getArrayOfDirectiveOnValues()) && irs.getPersistence().equals(this.getPersistence()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getArrayOfDirectiveOnValues(),this.getUserDirectiveAllOtherValues(),this.getInfoId(),this.getPersistence());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class UserInfoReleaseStatement {\n");
		sb.append("  InfoId: ").append(this.getInfoId().toString()).append("\n");
		sb.append("  Persistence: ").append(this.getPersistence()).append("\n");
		sb.append("  ArrayOfDirectiveOnValues: ");
		if (arrayOfDirectiveOnValues != null) {
			for (UserDirectiveOnValues d : arrayOfDirectiveOnValues) {
				sb.append(d.toString()).append("\n                          ");
			}
		}
		sb.append("\n  UserDirectiveAllOtherValues: ").append(this.getUserDirectiveAllOtherValues().toString()).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
}
