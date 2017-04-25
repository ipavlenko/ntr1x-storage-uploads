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
import com.ntr1x.storage.core.model.Resource;
import com.ntr1x.storage.core.model.Upload;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public interface IUploadService {

    Upload upload(long scope, UploadCreate create);
    Upload remove(Long scope, long id);

    Page<Upload> query(Long scope, String aspect, Pageable pageable);
    
    Upload select(Long scope, long id);
    Upload select(Long scope, UUID uuid);
    
    void createUploads(Resource resource, RelatedUpload[] uploads);
    void updateUploads(Resource resource, RelatedUpload[] uploads);
    
    @XmlRootElement
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadPageResponse {

        public long count;
        public int page;
        public int size;

        @XmlElement
        public List<Upload> content;
    }
    
    @XmlRootElement
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedUpload {
        
        public Long id;
        public Long upload;
        public Action action;
    }
    
    @XmlRootElement
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadCreate {
        
        public Callable<File> file;
        public long user;
        public String original;
        public UploadSettings settings;
    }
    
    @XmlRootElement
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadSettings {
        
        @XmlElement
        public String[] aspects;
    }
}
