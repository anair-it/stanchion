package org.anair.stanchion.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class CacheInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name = StringUtils.EMPTY;

	private List<String> keys = new ArrayList<String>();

	private long size = 0;
	private String config;
	private long hitCount = 0;

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public long getHitCount() {
		return hitCount;
	}

	public void setHitCount(long hitCount) {
		this.hitCount = hitCount;
	}

	public CacheInfo() {
	}

	public CacheInfo(final String name, final long size) {
		this.setName(name);
		this.setSize(size);
	}
	
	public CacheInfo(final String name, final long size, String config, long hitCount) {
		this.setName(name);
		this.setSize(size);
		this.setConfig(config);
		this.setHitCount(hitCount);
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = StringUtils.trimToEmpty(name);
	}

	public long getSize() {
		return this.size;
	}

	public void setSize(final long size) {
		this.size = (0 > size ? 0 : size);
	}

	public List<String> getKeys() {
		return this.keys;
	}

	public void setKeys(final List<String> keys) {
		this.keys = (null == keys ? new ArrayList<String>() : keys);
	}

	public void addKey(final String key) {
		if (null != key) {
			this.keys.add(key);
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[name=").append(this.name);
		sb.append(", size=").append(this.size).append(']');
		return sb.toString();
	}

}
