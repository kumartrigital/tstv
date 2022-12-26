package org.mifosplatform.workflow.eventaction.data;

public class ActionDetaislData {
	
	private Long id;
	private String procedureName;
	private String actionName;
	private String isSynchronous;
	private String eventName;

	public ActionDetaislData() {
	}


	public ActionDetaislData(Long id, String procedureName, String actionName, String isSynchronous,String eventName) {
            
		this.id=id;
		this.procedureName=procedureName;
		this.actionName=actionName;
		this.isSynchronous=isSynchronous;
		this.eventName=eventName;

	}


	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the procedureName
	 */
	public String getProcedureName() {
		return procedureName;
	}

	/**
	 * @return the actionType
	 */
	public String getActionName() {
		return actionName;
	}


	public String IsSynchronous() {
		return isSynchronous;
	}

	public String getEventName() {
		return eventName;
	}
	
	

}
