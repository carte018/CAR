package edu.internet2.consent.copsu.model;

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
public class InfoReleaseStatement {
	
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
	private List<DirectiveOnValues> arrayOfDirectiveOnValues;
	@Embedded @JsonProperty("directiveAllOtherValues")
	private DirectiveAllOtherValues directiveAllOtherValues;
	
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
	public List<DirectiveOnValues> getArrayOfDirectiveOnValues() {
		return arrayOfDirectiveOnValues;
	}
	public void setArrayOfDirectiveOnValues(ArrayList<DirectiveOnValues> arrayOfDirectiveOnValues) {
		this.arrayOfDirectiveOnValues = arrayOfDirectiveOnValues;
	}
	public DirectiveAllOtherValues getDirectiveAllOtherValues() {
		return directiveAllOtherValues;
	}
	public void setDirectiveAllOtherValues(DirectiveAllOtherValues directiveAllOtherValues) {
		this.directiveAllOtherValues = directiveAllOtherValues;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		InfoReleaseStatement irs = (InfoReleaseStatement) o;
		return (irs.getInfoId().equals(this.getInfoId()) && irs.getDirectiveAllOtherValues().equals(this.getDirectiveAllOtherValues()) && irs.getArrayOfDirectiveOnValues().equals(this.getArrayOfDirectiveOnValues()) && irs.getPersistence().equals(this.getPersistence()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getArrayOfDirectiveOnValues(),this.getDirectiveAllOtherValues(),this.getInfoId(),this.getPersistence());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class InfoReleaseStatement {\n");
		sb.append("  InfoId: ").append(this.getInfoId().toString()).append("\n");
		sb.append("  Persistence: ").append(this.getPersistence()).append("\n");
		sb.append("  ArrayOfDirectiveOnValues: ");
		if (arrayOfDirectiveOnValues != null) {
			for (DirectiveOnValues d : arrayOfDirectiveOnValues) {
				sb.append(d.toString()).append("\n                          ");
			}
		}
		sb.append("\n  DirectiveAllOtherValues: ").append(this.getDirectiveAllOtherValues().toString()).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
}
