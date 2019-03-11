package edu.internet2.consent.arpsi.model;

import java.util.Objects;
import javax.persistence.Embeddable;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class RelyingPartyId {

		private String RPtype;
		private String RPvalue;
			
		@JsonProperty("RP-type")
		public String getRPtype() {
			return RPtype;
		}
		public void setRPtype(String rPtype) {
			RPtype = rPtype;
		}
			
		@JsonProperty("RP-value")
		public String getRPvalue() {
			return RPvalue;
		}
		public void setRPvalue(String rPvalue) {
			RPvalue = rPvalue;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || o.getClass() != this.getClass()) {
				return false;
			}
			return (((RelyingPartyId) o).getRPtype().equals(this.getRPtype()) && ((RelyingPartyId) o).getRPvalue().equals(this.getRPvalue()));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(RPtype, RPvalue);
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("class RelyingPartyId { \n");
			sb.append("  RP-type: ").append(RPtype).append("\n");
			sb.append("  RP-value: ").append(RPvalue).append("\n");
			sb.append("} \n");
			return sb.toString();
		}	

}