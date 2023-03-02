package com.newcode.community.elasticsearch;

import com.newcode.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/26 15:43
 * @description TODO
 **/
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {

}
