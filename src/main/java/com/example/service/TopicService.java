package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Interact;
import com.example.entity.dto.Topic;
import com.example.entity.dto.TopicType;
import com.example.entity.vo.request.AddCommentVo;
import com.example.entity.vo.request.TopicCreateVo;
import com.example.entity.vo.request.TopicUpdateVo;
import com.example.entity.vo.response.*;

import java.util.List;

public interface TopicService extends IService<Topic>{
    public List<TopicType> listTypes();

    String createTopic(int uid, TopicCreateVo vo);

    public List<TopicPreviewVo> listTopicByPage(int page,int type);

    List<TopicTopVo> listTopTopics();

    TopicDetailVo getTopic(int tid,int uid);

    void interact(Interact interact,boolean state);

    public List<TopicPreviewVo> listTopicCollects(int uid);

    String updateTopic(int uid, TopicUpdateVo vo);

    String createComment(int uid, AddCommentVo vo);

    List<CommentVo> comments(int tid,int pageNumber);

    void deleteComment(int id, int uid);

}
