package org.anair.stanchion.model;

import java.io.Serializable;
import java.util.Objects;

public class NameValue implements Serializable {

	private static final long serialVersionUID = 1717596418837449775L;

	private String key;
	private Object value;
	private String info;
	private String message;
	
	public NameValue(String key, Object value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "NameValue [" + (key != null ? "key=" + key + ", " : "")
				+ (value != null ? "value=" + value : "") + "]";
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NameValue other = (NameValue) obj;
		
		return Objects.equals(key, other.key) && Objects.equals(value, other.value);
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
	
}
