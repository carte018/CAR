package edu.internet2.consent.informed.model;

import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class ReturnedInfoItemMetaInformation {
	
	@Id
	@JsonIgnore
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long riimiid;
	
	@JsonProperty("rhidentifier")
	@Embedded
	private RHIdentifier rhidentifier;
	
	@JsonProperty("iiidentifier")
	@OneToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private InfoItemIdentifier iiidentifier;
	
	@JsonProperty("iimode")
	@Enumerated
	private InfoItemMode iimode;
	
	@JsonProperty("displayname")
	@OneToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private InternationalizedString displayname;
	
	@JsonProperty("description")
	@OneToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private InternationalizedString description;
	
	@JsonProperty("presentationtype")
	private String presentationtype;
	
	@JsonProperty("policytype")
	private String policytype;
	
	@JsonProperty("sensitivity")
	private boolean sensitivity;
	
	@JsonProperty("asnd")
	private boolean asnd;
	
	@JsonProperty("multivalued")
	private boolean multivalued;
	
	@JsonProperty("version")
	private int version;
	
	@JsonProperty("updated")
	private long updated;

	@JsonProperty("state")
	private String state;
	
	@JsonProperty("httpHeader")
	private String httpHeader;


	public String getHttpHeader() {
		return httpHeader;
	}

	public void setHttpHeader(String httpHeader) {
		this.httpHeader = httpHeader;
	}

	public Long getRiimiid() {
		return riimiid;
	}

	public void setRiimiid(Long riimiid) {
		this.riimiid = riimiid;
	}

	public RHIdentifier getRhidentifier() {
		return rhidentifier;
	}

	public void setRhidentifier(RHIdentifier rhidentifier) {
		this.rhidentifier = rhidentifier;
	}

	public InfoItemIdentifier getIiidentifier() {
		return iiidentifier;
	}

	public void setIiidentifier(InfoItemIdentifier iiidentifier) {
		this.iiidentifier = iiidentifier;
	}

	public InfoItemMode getIimode() {
		return iimode;
	}

	public void setIimode(InfoItemMode iimode) {
		this.iimode = iimode;
	}

	public InternationalizedString getDisplayname() {
		return displayname;
	}

	public void setDisplayname(InternationalizedString displayname) {
		this.displayname = displayname;
	}

	public InternationalizedString getDescription() {
		return description;
	}

	public void setDescription(InternationalizedString description) {
		this.description = description;
	}
	
	public void setPresentationtype(String value) {
		this.presentationtype = value;
	}
	
	public String getPresentationtype() {
		return presentationtype;
	}
	
	public void setPolicytype(String value) {
		this.policytype = value;
	}
	
	public String getPolicytype() {
		return policytype;
	}
	
	public void setSensitivity(boolean value) {
		this.sensitivity = value;
	}
	
	public boolean isSensitivity() {
		return sensitivity;
	}
	
	public boolean getSensitivity() {
		return sensitivity;
	}
	
	public void setAsnd(boolean value) {
		this.asnd = value;
	}
	
	public boolean isAsnd() {
		return asnd;
	}
	
	public boolean getAsnd() {
		return asnd;
	}
	
	public void setMultivalued(boolean value) {
		this.multivalued = value;
	}
	
	public boolean isMultivalued() {
		return multivalued;
	}
	
	public boolean getMultivalued() {
		return multivalued;
	}
	
	public void setVersion(int value) {
		this.version = value;
	}
	
	public int getVersion() {
		return version;
	}
	
	public void setUpdated(long value) {
		this.updated = value;
	}
	
	public long getUpdated() {
		return updated;
	}

	public void setState(String value) {
		this.state = value;
	}
	
	public String getState() {
		return state;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ReturnedInfoItemMetaInformation riimi = (ReturnedInfoItemMetaInformation) o;
		return (this.getRhidentifier().equals(riimi.getRhidentifier()) && this.getIiidentifier().equals(riimi.getIiidentifier()) && this.getDescription().equals(riimi.getDescription()) && this.getDisplayname().equals(riimi.getDisplayname()) && this.getIimode().equals(riimi.getIimode()));
	}
	
	@Override 
	public int hashCode() {
		return Objects.hash(this.getRhidentifier(),this.getIiidentifier(),this.getDescription(),this.getDisplayname(),this.getDescription(),this.getIimode());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
