package org.wso2.carbon.identity.entitlement.xacml.core.dto;

/**
 * This encapsulates the attribute element data of the XACML policy
 */
public class AttributeDTO {

    private String attributeValue;

    private String attributeDataType;

    private String attributeId;

    private String attributeCategory;

    public String getAttributeDataType() {
        return attributeDataType;
    }

    public void setAttributeDataType(String attributeDataType) {
        this.attributeDataType = attributeDataType;
    }

    public String getCategory() {
        return attributeCategory;
    }

    public void setCategory(String category) {
        this.attributeCategory = category;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeDTO)) return false;

        AttributeDTO dto = (AttributeDTO) o;

        if (attributeDataType != null ? !attributeDataType.equals(dto.attributeDataType) : dto.attributeDataType != null)
            return false;
        if (attributeId != null ? !attributeId.equals(dto.attributeId) : dto.attributeId != null)
            return false;
        if (attributeCategory != null ? !attributeCategory.equals(dto.attributeCategory) : dto.attributeCategory != null)
            return false;
        if (attributeValue != null ? !attributeValue.equals(dto.attributeValue) : dto.attributeValue != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = attributeValue != null ? attributeValue.hashCode() : 0;
        result = 31 * result + (attributeDataType != null ? attributeDataType.hashCode() : 0);
        result = 31 * result + (attributeId != null ? attributeId.hashCode() : 0);
        result = 31 * result + (attributeCategory != null ? attributeCategory.hashCode() : 0);
        return result;
    }
}
