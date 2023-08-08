package net.foxgenesis.property3;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public interface PropertyProvider {

	PropertyInfo registerProperty(@NotNull String category, @NotNull String key, boolean modifiable);

	Property getProperty(@NotNull String category, @NotNull String key);

	Property getProperty(@NotNull PropertyInfo info);
	
	boolean propertyExists(@NotNull String category, @NotNull String key);

	List<PropertyInfo> getPropertyList();
}
