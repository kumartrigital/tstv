/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.data;

import org.mifosplatform.infrastructure.documentmanagement.domain.StorageType;

/**
 * Immutable data object represent document being managed on platform.
 */
public class DocumentData {

   
    @SuppressWarnings("unused")
	private final Long id;
    @SuppressWarnings("unused")
	private final String parentEntityType;
    @SuppressWarnings("unused")
	private final Long parentEntityId;
    @SuppressWarnings("unused")
    private final String name;
    private final String fileName;
    @SuppressWarnings("unused")
    private final Long size;
    private final String type;
    @SuppressWarnings("unused")
    private final String description;
    private String location;
    private final Integer storageType;

    public DocumentData(final Long id, final String parentEntityType, final Long parentEntityId, final String name, final String fileName,
            final Long size, final String type, final String description, final String location,
            final Integer storageType) {
        this.id = id;
        this.parentEntityType = parentEntityType;
        this.parentEntityId = parentEntityId;
        this.name = name;
        this.fileName = fileName;
        this.size = size;
        this.type = type;
        this.description = description;
        this.location = location;
        this.storageType = storageType;
    }

    public String contentType() {
        return this.type;
    }

    public String fileName() {
        return this.fileName;
    }

    public String fileLocation() {
        return this.location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public StorageType storageType() {
    	return StorageType.fromInt(this.storageType);
    }

	public Long getId() {
		return id;
	}

	public String getParentEntityType() {
		return parentEntityType;
	}

	public Long getParentEntityId() {
		return parentEntityId;
	}

	public String getName() {
		return name;
	}

	public String getFileName() {
		return fileName;
	}

	public Long getSize() {
		return size;
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public String getLocation() {
		return location;
	}

	public Integer getStorageType() {
		return storageType;
	}

}