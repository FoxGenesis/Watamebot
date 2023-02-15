package net.foxgenesis.config.fields;
@Deprecated(forRemoval = true)
public class StorageKey {
	public final String name, type;
	public final SQLDataType r;

	public StorageKey(String name, String type, SQLDataType r) {
		this.name = name;
		this.type = type;
		this.r = r;
	}

	@Override
	public String toString() {
		return String.format("Storage{name=%s,type=%s,return=%s}", this.name, this.type, this.r);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
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
		StorageKey other = (StorageKey) obj;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name))
			return false;
		return true;
	}
}
