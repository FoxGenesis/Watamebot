package net.foxgenesis.property.lck;

import java.sql.Blob;
import java.util.Optional;

import net.foxgenesis.property.PropertyException;
import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.PropertyResolver;

public interface LCKPropertyResolver extends PropertyResolver<Long, String, String> {
	@SuppressWarnings("exports")
	@Override
	Optional<Blob> getInternal(Long lookup, PropertyInfo info) throws PropertyException;
}
