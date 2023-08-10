package net.foxgenesis.property;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public interface PropertyProvider {

	PropertyInfo registerProperty(@NotNull String category, @NotNull String key, boolean modifiable, @NotNull PropertyType type);
	
	PropertyInfo getPropertyInfoByID(int id);

	Property getProperty(@NotNull String category, @NotNull String key);

	Property getProperty(@NotNull PropertyInfo info);
	
	boolean propertyExists(@NotNull String category, @NotNull String key);

	List<PropertyInfo> getPropertyList();
}
