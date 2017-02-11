package com.ntr1x.storage.uploads.converter;


import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import com.ntr1x.storage.core.services.ISerializationService;
import com.ntr1x.storage.uploads.services.IImageService;

import lombok.RequiredArgsConstructor;

@Provider
public class ImageSettingsProvider implements ParamConverterProvider {
    
	@Inject
	private ISerializationService serialization;
	
    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> aClass, Type type, Annotation[] annotations) {

        if (type.equals(ImageSettingsConverter.class)) {
            return (ParamConverter<T>) new ImageSettingsConverter(serialization);
        }

        return null;
    }

    @RequiredArgsConstructor
    public static class ImageSettingsConverter implements ParamConverter<IImageService.ImageSettings> {

    	private final ISerializationService serialization;
    	
        @Override
        public IImageService.ImageSettings fromString(String string) {
        	
        	return serialization.parseJSONStringJackson(IImageService.ImageSettings.class, string);
        }

        @Override
        public String toString(IImageService.ImageSettings settings) {

        	return serialization.toJSONStringJackson(settings);
        }
    }
}
