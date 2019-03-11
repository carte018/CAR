package edu.internet2.consent.icm.model;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
@Entity
public class IcmDirectiveOnValues {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long DirectiveOnValuesIdentifier;
	
	@JsonProperty("releaseDirective")
	@Enumerated(EnumType.STRING)
	private IcmReleaseDirective icmReleaseDirective;
	
	@JsonProperty("valueObjectList")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private List<ValueObject> valueObjectList;

	@JsonIgnore
	public Long getDirectiveOnValuesIdentifier() {
		return DirectiveOnValuesIdentifier;
	}

	@JsonIgnore
	public void setDirectiveOnValuesIdentifier(Long directiveOnValuesIdentifier) {
		DirectiveOnValuesIdentifier = directiveOnValuesIdentifier;
	}

	@JsonProperty("icmReleaseDirective")
	public IcmReleaseDirective getIcmReleaseDirective() {
		return icmReleaseDirective;
	}

	@JsonProperty("icmReleaseDirective")
	public void setIcmReleaseDirective(IcmReleaseDirective icmReleaseDirective) {
		this.icmReleaseDirective = icmReleaseDirective;
	}
	
	@JsonProperty("orgReleaseDirective")
	public void setOrgReleaseDirective(IcmReleaseDirective icmReleaseDirective) {
		this.icmReleaseDirective = icmReleaseDirective;
	}

	@JsonProperty("valueObjectList")
	public List<ValueObject> getValueObjectList() {
		return valueObjectList;
	}

	@JsonProperty("valueObjectList")
	public void setValueObjectList(List<ValueObject> valueObjectList) {
		this.valueObjectList = valueObjectList;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		IcmDirectiveOnValues v = (IcmDirectiveOnValues) o;
		return (v.getValueObjectList().containsAll(this.getValueObjectList()) && this.getValueObjectList().containsAll(v.getValueObjectList()) && v.getIcmReleaseDirective().equals(this.getIcmReleaseDirective()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(valueObjectList,icmReleaseDirective);
	}
	
	@Override
	public String toString() {
		return "OrgDirectiveOnValues #"+DirectiveOnValuesIdentifier;
	}
	
}
