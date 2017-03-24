package org.anair.stanchion.model;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

public class LoggerInfo implements Serializable {

	private static final long serialVersionUID = 5983996040518299687L;

	private String name = StringUtils.EMPTY;

	private String level = Level.OFF.toString();

	public LoggerInfo() {
	}

	public LoggerInfo(final String name, final String level) {
		this.setName(name);
		this.setLevel(level);
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = StringUtils.trimToEmpty(name);
	}

	public String getLevel() {
		return this.level;
	}

	public void setLevel(final String level) {
		this.level = Level.toLevel(level, Level.OFF).toString();
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[name=").append(this.name);
		sb.append(", level=").append(this.level).append(']');
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LoggerInfo other = (LoggerInfo) obj;
		
		return Objects.equals(name, other.name);
	}
}