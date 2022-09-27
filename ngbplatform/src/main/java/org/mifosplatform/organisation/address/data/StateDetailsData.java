package org.mifosplatform.organisation.address.data;

public class StateDetailsData {

	private final String stateName;
	private final String stateCode;

	public StateDetailsData(final String stateName, final String stateCode) {
		this.stateName = stateName;
		this.stateCode = stateCode;
	}

	public String getStateName() {
		return stateName;
	}

	public String getStateCode() {
		return stateCode;
	}
}
