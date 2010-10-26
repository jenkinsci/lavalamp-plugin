package com.ingenotech.lavalamp;

public class BuildState {
	private BuildStatus	status;
	private String		name;

	public BuildState(String name, BuildStatus status) {
		this.name = name;
		this.status = status;
	}

	public BuildStatus getStatus() {
		return status;
	}

	public String getName() {
		return name;
	}

	/** 
	 * Hash these on the name but not the status,
	 * so only one BuildState for each project is
	 * maintained in the Map.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BuildState other = (BuildState) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "BuildState[name=" + name + ", status=" + status + "]";
	}
}
