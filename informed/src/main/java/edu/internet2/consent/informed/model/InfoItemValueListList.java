package edu.internet2.consent.informed.model;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class InfoItemValueListList {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long iivllid;
	
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private List<InfoItemValueList> itemsandvalues;

	public Long getIivllid() {
		return iivllid;
	}

	public void setIivllid(Long iivllid) {
		this.iivllid = iivllid;
	}

	public List<InfoItemValueList> getItemsandvalues() {
		return itemsandvalues;
	}

	public void setItemsandvalues(List<InfoItemValueList> itemsandvalues) {
		this.itemsandvalues = itemsandvalues;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		InfoItemValueListList iivll = (InfoItemValueListList) o;
		return (iivll.getItemsandvalues().equals(this.getItemsandvalues()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getItemsandvalues());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
