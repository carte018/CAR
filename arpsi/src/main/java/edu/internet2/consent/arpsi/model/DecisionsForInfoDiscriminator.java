/*
 * Copyright 2015 - 2019 Duke University
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License Version 2 as published by
    the Free Software Foundation.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License Version 2
    along with this program.  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt>.

 */
package edu.internet2.consent.arpsi.model;

import java.util.Objects;

public class DecisionsForInfoDiscriminator {
	private InfoId infoId;
	private PolicyId policyId;
	private String directive;
	public InfoId getInfoId() {
		return infoId;
	}
	public void setInfoId(InfoId infoId) {
		this.infoId = infoId;
	}

	public PolicyId getPolicyId() {
		return policyId;
	}
	public void setPolicyId(PolicyId policyId) {
		this.policyId = policyId;
	}
	
	@Override 
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (this == o) {
			return true;
		}
		if (this.getClass() != o.getClass()) {
			return false;
		}
		DecisionsForInfoDiscriminator t = (DecisionsForInfoDiscriminator) o;
		if (this.getInfoId().equals(t.getInfoId()) && this.getPolicyId().equals(t.getPolicyId()) && this.getDirective().equals(t.getDirective())) {
			return true;
		} else {
			return false;
		}
	}
	public String getDirective() {
		return directive;
	}
	public void setDirective(String directive) {
		this.directive = directive;
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.getInfoId(),this.getPolicyId(),this.getDirective());
	}
}
