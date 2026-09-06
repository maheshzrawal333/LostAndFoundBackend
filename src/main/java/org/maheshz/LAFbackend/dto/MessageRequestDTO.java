package org.maheshz.LAFbackend.dto;
import lombok.Data;

@Data
public class MessageRequestDTO {
    private String text;
    private String attachmentUrl;
    private String attachmentType;
    private String attachmentName;
}
