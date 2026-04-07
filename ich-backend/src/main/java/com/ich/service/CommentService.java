// src/main/java/com/ich/service/CommentService.java
// 在 addComment 方法中添加成就检查

package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ich.entity.Comment;
import com.ich.entity.CommentLike;
import com.ich.entity.CommentReply;
import com.ich.entity.User;
import com.ich.mapper.CommentLikeMapper;
import com.ich.mapper.CommentMapper;
import com.ich.mapper.CommentReplyMapper;
import com.ich.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService extends ServiceImpl<CommentMapper, Comment> {

    private final CommentMapper commentMapper;
    private final CommentReplyMapper commentReplyMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final UserMapper userMapper;
    private final AchievementService achievementService;

    // 添加评论（状态为待审核）
    public boolean addComment(Long heritageId, Long userId, String content) {
        Comment comment = new Comment();
        comment.setHeritageId(heritageId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setLikeCount(0);
        comment.setStatus(0);
        comment.setCreateTime(LocalDateTime.now());
        boolean success = save(comment);
        
        // 成就检查：评论数量
        if (success && userId != null && achievementService != null) {
            try {
                Long commentCount = count(new LambdaQueryWrapper<Comment>().eq(Comment::getUserId, userId));
                achievementService.checkAndGrantAchievements(userId, "comment_count", commentCount);
            } catch (Exception e) {
                System.err.println("成就检查失败: " + e.getMessage());
            }
        }
        
        return success;
    }

    // 获取评论及回复（只显示已审核通过的 status=1）
    public List<Map<String, Object>> getHeritageCommentsWithReplies(Long heritageId) {
        List<Comment> comments = commentMapper.selectList(
            new LambdaQueryWrapper<Comment>()
                .eq(Comment::getHeritageId, heritageId)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateTime)
        );
        
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> commentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
        
        List<CommentReply> allReplies = commentReplyMapper.selectList(
            new LambdaQueryWrapper<CommentReply>()
                .in(CommentReply::getCommentId, commentIds)
                .eq(CommentReply::getStatus, 1)
                .orderByAsc(CommentReply::getCreateTime)
        );
        
        Map<Long, List<CommentReply>> repliesMap = allReplies.stream()
            .collect(Collectors.groupingBy(CommentReply::getCommentId));
        
        List<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toList());
        userIds.addAll(allReplies.stream().map(CommentReply::getUserId).collect(Collectors.toList()));
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds.stream().distinct().collect(Collectors.toList()));
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment comment : comments) {
            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("id", comment.getId());
            commentMap.put("content", comment.getContent());
            commentMap.put("likeCount", comment.getLikeCount());
            commentMap.put("createTime", comment.getCreateTime());
            commentMap.put("userId", comment.getUserId());
            
            User user = userMap.get(comment.getUserId());
            commentMap.put("nickname", user != null ? user.getNickname() : "用户" + comment.getUserId());
            commentMap.put("avatar", user != null ? user.getAvatar() : null);
            
            List<CommentReply> replies = repliesMap.getOrDefault(comment.getId(), new ArrayList<>());
            List<Map<String, Object>> replyList = new ArrayList<>();
            for (CommentReply reply : replies) {
                Map<String, Object> replyMap = new HashMap<>();
                replyMap.put("id", reply.getId());
                replyMap.put("content", reply.getContent());
                replyMap.put("createTime", reply.getCreateTime());
                replyMap.put("userId", reply.getUserId());
                replyMap.put("replyToUserId", reply.getReplyToUserId());
                replyMap.put("replyToNickname", reply.getReplyToNickname());
                
                User replyUser = userMap.get(reply.getUserId());
                replyMap.put("nickname", replyUser != null ? replyUser.getNickname() : "用户" + reply.getUserId());
                replyList.add(replyMap);
            }
            commentMap.put("replies", replyList);
            result.add(commentMap);
        }
        
        return result;
    }

    // 点赞评论
    @Transactional
    public boolean likeComment(Long commentId, Long userId) {
        CommentLike existing = commentLikeMapper.selectOne(
            new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId)
        );
        if (existing != null) {
            return false;
        }
        
        CommentLike like = new CommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        like.setCreateTime(LocalDateTime.now());
        commentLikeMapper.insert(like);
        
        Comment comment = getById(commentId);
        if (comment != null) {
            comment.setLikeCount((comment.getLikeCount() == null ? 0 : comment.getLikeCount()) + 1);
            updateById(comment);
        }
        return true;
    }

    // 取消点赞
    @Transactional
    public boolean unlikeComment(Long commentId, Long userId) {
        int deleted = commentLikeMapper.delete(
            new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId)
        );
        
        if (deleted > 0) {
            Comment comment = getById(commentId);
            if (comment != null) {
                comment.setLikeCount(Math.max(0, (comment.getLikeCount() == null ? 0 : comment.getLikeCount()) - 1));
                updateById(comment);
            }
            return true;
        }
        return false;
    }

    // 添加回复
    public boolean addReply(Long commentId, Long userId, String content, Long replyToUserId, String replyToNickname) {
        CommentReply reply = new CommentReply();
        reply.setCommentId(commentId);
        reply.setUserId(userId);
        reply.setContent(content);
        reply.setReplyToUserId(replyToUserId);
        reply.setReplyToNickname(replyToNickname);
        reply.setLikeCount(0);
        reply.setStatus(1);
        reply.setCreateTime(LocalDateTime.now());
        return commentReplyMapper.insert(reply) > 0;
    }

    // 删除评论（用户删除自己的，软删除）
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        Comment comment = getById(commentId);
        if (comment == null || !comment.getUserId().equals(userId)) {
            return false;
        }
        comment.setStatus(0);
        return updateById(comment);
    }

    // 删除回复
    public boolean deleteReply(Long replyId, Long userId) {
        CommentReply reply = commentReplyMapper.selectById(replyId);
        if (reply == null || !reply.getUserId().equals(userId)) {
            return false;
        }
        reply.setStatus(0);
        return commentReplyMapper.updateById(reply) > 0;
    }

    // 管理员分页查询评论
    public Page<Comment> getAdminList(Integer pageNum, Integer pageSize, Integer status) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(Comment::getStatus, status);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        return page(page, wrapper);
    }

    // 审核评论（管理员）
    public boolean auditComment(Long id, Integer status) {
        Comment comment = getById(id);
        if (comment == null) {
            return false;
        }
        comment.setStatus(status);
        return updateById(comment);
    }
}