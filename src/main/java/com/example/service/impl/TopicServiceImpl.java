package com.example.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.*;
import com.example.entity.vo.request.AddCommentVo;
import com.example.entity.vo.request.TopicCreateVo;
import com.example.entity.vo.request.TopicUpdateVo;
import com.example.entity.vo.response.CommentVo;
import com.example.entity.vo.response.TopicDetailVo;
import com.example.entity.vo.response.TopicPreviewVo;
import com.example.entity.vo.response.TopicTopVo;
import com.example.mapper.*;
import com.example.service.NotificationService;
import com.example.service.TopicService;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService{
    @Resource
    TopicTypeMapper mapper;

    @Resource
    TopicMapper topicMapper;

    @Resource
    FlowUtils flowUtils;

    @Resource
    CacheUtils cacheUtils;

    @Resource
    AccountMapper accountMapper;

    @Resource
    AccountDetailsMapper accountDetailsMapper;

    @Resource
    AccountPrivacyMapper accountPrivacyMapper;

    @Resource
    TopicCommentMapper topicCommentMapper;

    @Resource
    StringRedisTemplate template;

    @Resource
    NotificationService notificationService;

    private Set<Integer> types = null;

    @PostConstruct
    private void initTypes(){
        types = this.listTypes()
                .stream()
                .map(TopicType::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<TopicType> listTypes() {

        return mapper.selectList(null);
    }

    @Override
    public String createTopic(int uid, TopicCreateVo vo) {
        if(!textLimitCheck(vo.getContent(),20000))
            return "文章内容太多，发文失败！";
        if(!types.contains(vo.getType()))
            return "文章类型非法!";
        String key = Const.FORUM_TOPIC_CREATE_COUNTER + uid;
        if(!flowUtils.limitPeriodCounterCheck(key,3,3600))
            return "发文频繁，请稍后再试!";
        Topic topic = new Topic();
        BeanUtils.copyProperties(vo,topic);
        topic.setText(vo.getContent().toJSONString());
        topic.setUid(uid);
        topic.setTime(new Date());
        if(this.save(topic)){
            cacheUtils.deleteCachePattern(Const.FORUM_TOPIC_PREVIEW_CACHE + "*");
            return null;
        }else {
            return "内部错误，请联系管理员";
        }
    }

    @Override
    public String updateTopic(int uid, TopicUpdateVo vo) {
        if(!textLimitCheck(vo.getContent(),20000))
            return "文章内容太多，发文失败！";
        if(!types.contains(vo.getType()))
            return "文章类型非法!";
        baseMapper.update(null,Wrappers.<Topic>update()
                .eq("uid",uid)
                .eq("id",vo.getId())
                .set("title",vo.getTitle())
                .set("text",vo.getContent().toString())
                .set("type",vo.getType())
        );
        return null;
    }

    @Override
    public String createComment(int uid, AddCommentVo vo) {
        if(!textLimitCheck(JSONObject.parseObject(vo.getContent()),2000))
            return "评论内容太多，发表失败！";
        String key = Const.FORUM_TOPIC_COMMENT_COUNTER + uid;
        if(!flowUtils.limitPeriodCounterCheck(key,3,60))//一分钟只能发三条评论，防止水帖
            return "发表评论频繁，请稍后再试!";
        TopicComment comment = new TopicComment();
        comment.setUid(uid);
        BeanUtils.copyProperties(vo,comment);
        comment.setTime(new Date());
        topicCommentMapper.insert(comment);
        Topic topic = baseMapper.selectById(vo.getTid());
        Account account = accountMapper.selectById(uid);
        if(vo.getQuote() > 0){
            TopicComment com = topicCommentMapper.selectById(vo.getQuote());
            if(!Objects.equals(account.getId(),com.getUid())){
                notificationService.addNotification(
                        com.getUid(),
                        "您有新的帖子评论回复",
                        account.getUsername()+"回复了你发表的评论，快去看看吧！",
                        "success","/index/topic-detail/"+com.getTid()
                );
            }
        }else if (!Objects.equals(account.getId(),topic.getUid())){
            notificationService.addNotification(
                    topic.getUid(),
                    "您有新的帖子回复",
                    account.getUsername()+"回复了你发表的帖子：" + topic.getTitle() + "，快去看看吧！",
                    "success","/index/topic-detail/"+topic.getId()
            );
        }
        return null;
    }

    @Override
    public List<CommentVo> comments(int tid, int pageNumber) {
        Page<TopicComment> page = Page.of(pageNumber,10);
        topicCommentMapper.selectPage(page, Wrappers.<TopicComment>query().eq("tid", tid));
        return page.getRecords().stream().map(dto ->{
            CommentVo vo = new CommentVo();
            BeanUtils.copyProperties(dto,vo);
            if(dto.getQuote()>0) {
                TopicComment comment = topicCommentMapper.selectOne(Wrappers.<TopicComment>query().eq("id",
                        dto.getQuote()).orderByAsc("time"));
                if(comment != null){
                    JSONObject object = JSONObject.parseObject(comment.getContent());
                    StringBuilder builder = new StringBuilder();
                    this.shortContent(object.getJSONArray("ops"),builder, ignore -> {});
                    vo.setQuote(builder.toString());
                }else {
                    vo.setQuote("此评论已被删除");
                }
            }
            CommentVo.User user = new CommentVo.User();
            this.fillUserDetailsByPrivacy(user,dto.getUid());
            vo.setUser(user);
            return vo;
        }).toList();
    }

    @Override
    public void deleteComment(int id, int uid) {
        topicCommentMapper.delete(Wrappers.<TopicComment>query().eq("id", id).eq("uid", uid));
    }

    @Override
    public List<TopicPreviewVo> listTopicByPage(int pageNumber, int type) {
        String key =  Const.FORUM_TOPIC_PREVIEW_CACHE + pageNumber + ":" + type;
        List<TopicPreviewVo> list = cacheUtils.takeListFromCache(key, TopicPreviewVo.class);
        if(list != null) return list;
        Page<Topic> page = Page.of(pageNumber,10);
        if(type == 0)
            baseMapper.selectPage(page,Wrappers.<Topic>query().orderByDesc("time"));
        else
            baseMapper.selectPage(page,Wrappers.<Topic>query().eq("type",type).orderByDesc("time"));
        List<Topic> topics = page.getRecords();
        if(topics.isEmpty()) return null;
        list = topics.stream().map(this::resolveToPreview).toList();
        cacheUtils.saveListToCache(key,list,60);
        return list;
    }

    @Override
    public List<TopicTopVo> listTopTopics() {
        List<Topic> topics = baseMapper.selectList(Wrappers.<Topic>query()
                .select("id","title","time")
                .eq("top",1));
        return topics.stream().map(topic -> {
            TopicTopVo vo = new TopicTopVo();
            BeanUtils.copyProperties(topic,vo);
            return vo;
        }).toList();
    }

    @Override
    public TopicDetailVo getTopic(int tid, int uid) {
        TopicDetailVo vo = new TopicDetailVo();
        Topic topic = baseMapper.selectById(tid);
        BeanUtils.copyProperties(topic,vo);
        TopicDetailVo.Interact interact = new TopicDetailVo.Interact(
                hasInteract(tid,uid,"like"),
                hasInteract(tid,uid,"collect")
        );
        vo.setInteract(interact);
        TopicDetailVo.User user = new TopicDetailVo.User();
        vo.setUser(this.fillUserDetailsByPrivacy(user,topic.getUid()));
        vo .setComments(topicCommentMapper.selectCount(Wrappers.<TopicComment>query().eq("tid", tid)));
        return vo;
    }

    /**
     *由于论坛交互数据(如点赞、收藏等)更新可能会非常频繁
     *更新信息实时到MySQL不太现实，所以需要用Redis做缓冲并在合适的时机一次性入库一段时间内的全部数据
     *当数据更新到来时，会创建一个新的定时任务，此任务会在一段时间之后执行
     *将全部Redis暂时缓存的信息一次性加入到数据库，从而缓解MySQL压力，如果
     *在定时任务已经设定期间又有新的更新到来，仅更新Redis不创建新的延时任务
     */
    @Override
    public void interact(Interact interact, boolean state) {
        String type = interact.getType();
        synchronized (type.intern()){
            template.opsForHash().put(type,interact.toKey(),Boolean.toString(state));
            this.saveInteractSchedule(type);
        }
    }

    @Override
    public List<TopicPreviewVo> listTopicCollects(int uid) {
        return baseMapper.collectTopics(uid)
                .stream()
                .map(topic -> {
                    TopicPreviewVo vo = new TopicPreviewVo();
                    BeanUtils.copyProperties(topic,vo);
                    return vo;
                }).toList();
    }

    private boolean hasInteract(int tid, int uid,String type) {
        String key = tid + ":" + uid;
        if (template.opsForHash().hasKey(type, key))
            return Boolean.parseBoolean(template.opsForHash().entries(type).get(key).toString());
        return baseMapper.userInteractCount(tid, uid, type) > 0;
    }

    private final Map<String,Boolean> state = new HashMap<>();
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private void saveInteractSchedule(String type){
        if(!state.getOrDefault(type,false)){
            state.put(type,true);
            service.schedule(() -> {
                this.saveInteract(type);
                state.put(type,false);
            },3, TimeUnit.SECONDS);
        }
    }

    private void saveInteract(String type){
        synchronized (type.intern()){
            List<Interact> check = new LinkedList<>();
            List<Interact> uncheck = new LinkedList<>();
            template.opsForHash().entries(type).forEach((k,v) -> {
                if(Boolean.parseBoolean(v.toString()))
                    check.add(Interact.parseInteract(k.toString(),type));
                else
                    uncheck.add(Interact.parseInteract(k.toString(),type));
            });
            if(!check.isEmpty())
                baseMapper.addInteract(check,type);
            if(!uncheck.isEmpty())
                baseMapper.deleteInteract(uncheck,type);
            template.delete(type);
        }
    }

    private <T> T fillUserDetailsByPrivacy(T target, int uid){
        AccountDetails details = accountDetailsMapper.selectById(uid);
        Account account = accountMapper.selectById(uid);
        AccountPrivacy privacy = accountPrivacyMapper.selectById(uid);
        String[] ignores = privacy.hiddenFields();
        BeanUtils.copyProperties(account,target,ignores);
        BeanUtils.copyProperties(details,target,ignores);
        return target;
    }

    private TopicPreviewVo resolveToPreview(Topic topic){
        TopicPreviewVo vo = new TopicPreviewVo();
        BeanUtils.copyProperties(accountMapper.selectById(topic.getUid()),vo);
        BeanUtils.copyProperties(topic,vo);
        vo.setLike(baseMapper.interactCount(topic.getId(),"like"));
        vo.setCollect(baseMapper.interactCount(topic.getId(),"collect"));
        List<String> images = new ArrayList<>();
        StringBuilder previewText = new StringBuilder();
        JSONArray ops = JSONObject.parseObject(topic.getText()).getJSONArray("ops");
        this.shortContent(ops,previewText,obj -> images.add(obj.toString()));
        vo.setText(previewText.length() > 300 ? previewText.substring(0,300) : previewText.toString());
        vo.setImages(images);
        return vo;
    }

    private void shortContent(JSONArray ops, StringBuilder previewText, Consumer<Object> imageHandle){
        for(Object op : ops){
            Object insert = JSONObject.from(op).get("insert");
            if(insert instanceof String text){
                if(previewText.length() >= 300) continue;
                previewText.append(text);
            }else if(insert instanceof Map<?,?> map){
                Optional.ofNullable(map.get("image")).ifPresent(imageHandle);
            }
        }
    }


    private boolean textLimitCheck(JSONObject object,int max){
        if(object == null) return false;
        long length = 0;
        for(Object op : object.getJSONArray("ops")){
            length += JSONObject.from(op).getString("insert").length();
            if(length > max) return false;
        }
        return  true;
    }

}
