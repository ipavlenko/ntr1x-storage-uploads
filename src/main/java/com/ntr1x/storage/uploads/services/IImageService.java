package com.ntr1x.storage.uploads.services;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ntr1x.storage.core.model.Action;
import com.ntr1x.storage.core.model.Image;
import com.ntr1x.storage.core.model.Resource;
import com.ntr1x.storage.core.services.IScaleImageService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public interface IImageService {

	Image upload(ImageCreate create);
	Image remove(long id);

    Page<Image> query(String aspect, Pageable pageable);
    
    Image select(long id);
    Image select(UUID uuid);
	
    void createImages(Resource resource, RelatedImage[] images);
    void updateImages(Resource resource, RelatedImage[] images);
    
    @XmlRootElement
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImagePageResponse {

    	public long count;
        public int page;
        public int size;

        @XmlElement
        public List<Image> content;
	}
    
    @XmlRootElement
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedImage {
        
        public Long id;
        public Long image;
        public Action action;
    }
    
    @XmlRootElement
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageCreate {
    	
    	public Callable<File> file;
    	public long user;
    	public String original;
    	public ImageSettings settings;
    }
    
    @XmlRootElement
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageSettings {
        
        @XmlElement
        public Item[] items;
        
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Item {
            
            public String name;
            public String format;
            public Integer width;
            public Integer height;
            public IScaleImageService.Type type;
        }
    }
}
