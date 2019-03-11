package edu.internet2.consent.icm.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
@Entity
public class UserDirectiveOnValues {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long DirectiveIdentifier;
	
	@JsonIgnore
	public Long getDirectiveIdentifier() {
		return DirectiveIdentifier;
	}
	@JsonIgnore
	public void setDirectiveIdentifier(Long directiveIdentifier) {
		DirectiveIdentifier = directiveIdentifier;
	}
	

	@JsonProperty("releaseDirective")
	@Enumerated(EnumType.STRING)
	UserReleaseDirective userReleaseDirective;
	
	@JsonProperty("valueObjectList")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	List<ValueObject> valueObjectList;
	
	
	public UserReleaseDirective getUserReleaseDirective() {
		return userReleaseDirective;
	}
	public void setUserReleaseDirective(UserReleaseDirective userReleaseDirective) {
		this.userReleaseDirective = userReleaseDirective;
	}
	public List<ValueObject> getValueObjectList() {
		return valueObjectList;
	}
	public void setValuesList(ArrayList<ValueObject> valueList) {
		this.valueObjectList = valueList;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		return (((UserDirectiveOnValues) o).getUserReleaseDirective().equals(this.getUserReleaseDirective()) && ((UserDirectiveOnValues) o).getValueObjectList().equals(this.getValueObjectList()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(userReleaseDirective,valueObjectList);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class UserDirectiveOnValues {\n");
		sb.append("  userReleaseDirective: ").append(userReleaseDirective).append("\n");
		sb.append("  valuesList: ");
		for (ValueObject v : valueObjectList) {
			sb.append(v.getValue()).append("\n              ");
		}
		sb.append("\n}\n");
		return sb.toString();
	}
}
