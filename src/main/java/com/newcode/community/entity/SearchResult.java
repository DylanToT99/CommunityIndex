package com.newcode.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/26 20:01
 * @description TODO
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private List<DiscussPost> list;
    private long total;
}
