package com.mtheory7.legoinventoryservice.entities;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RebrickableResultDTO {
    private String set_num;
    private String name;
    private String year;
    private Integer theme_id;
    private Integer num_parts;
    private String set_img_url;
    private String set_url;
}
