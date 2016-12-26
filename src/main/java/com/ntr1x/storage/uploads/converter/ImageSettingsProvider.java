package com.ntr1x.storage.uploads.converter;


import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.JAXBContextProperties;

import com.ntr1x.storage.uploads.services.IImageService;

@Provider
public class ImageSettingsProvider implements ParamConverterProvider {
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> aClass, Type type, Annotation[] annotations) {

        if (type.equals(ImageSettingsConverter.class)) {
            return (ParamConverter<T>) new ImageSettingsConverter();
        }

        return null;
    }

    public static class ImageSettingsConverter implements ParamConverter<IImageService.ImageSettings> {

        @Override
        public IImageService.ImageSettings fromString(String string) {
            
            StringReader reader = new StringReader(string);

            Map<String, Object> jaxbProperties = new HashMap<String, Object>(2);
            jaxbProperties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
            jaxbProperties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);

            try {
                
                JAXBContext jc = JAXBContext.newInstance(new Class[] { IImageService.ImageSettings.class }, jaxbProperties);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                
                return unmarshaller.unmarshal(new StreamSource(reader), IImageService.ImageSettings.class).getValue();
                        
            } catch (JAXBException e) {
                
                return null;
            }
        }

        @Override
        public String toString(IImageService.ImageSettings settings) {

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            
            Map<String, Object> jaxbProperties = new HashMap<String, Object>(2);
            jaxbProperties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
            jaxbProperties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);

            try {
                
                JAXBContext jc = JAXBContext.newInstance(new Class[] { IImageService.ImageSettings.class }, jaxbProperties);
                Marshaller marshaller = jc.createMarshaller();
                
                marshaller.marshal(settings, stream);
                
                return new String(stream.toByteArray());
                        
            } catch (JAXBException e) {
                
                return null;
            }
        }
    }
}
