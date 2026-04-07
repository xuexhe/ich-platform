package com.ich.controller;

import com.ich.service.CommentService;
import com.ich.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    public Result<?> addComment(@RequestBody Map<String, Object> params) {
        Long heritageId = Long.valueOf(params.get("heritageId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        String content = params.get("content").toString();
        boolean success = commentService.addComment(heritageId, userId, content);
        return success ? Result.success("评论成功") : Result.error("评论失败");
    }

    @GetMapping("/heritage/{heritageId}")
    public Result<?> getComments(@PathVariable Long heritageId) {
        return Result.success(commentService.getHeritageCommentsWithReplies(heritageId));
    }

    // 点赞评论
    @PostMapping("/like")
    public Result<?> likeComment(@RequestBody Map<String, Long> params) {
        Long commentId = params.get("commentId");
        Long userId = params.get("userId");
        boolean success = commentService.likeComment(commentId, userId);
        return success ? Result.success("点赞成功") : Result.error("操作失败");
    }

    // 取消点赞
    @PostMapping("/unlike")
    public Result<?> unlikeComment(@RequestBody Map<String, Long> params) {
        Long commentId = params.get("commentId");
        Long userId = params.get("userId");
        boolean success = commentService.unlikeComment(commentId, userId);
        return success ? Result.success("取消点赞") : Result.error("操作失败");
    }

    // 添加回复
    @PostMapping("/reply")
    public Result<?> addReply(@RequestBody Map<String, Object> params) {
        Long commentId = Long.valueOf(params.get("commentId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        String content = params.get("content").toString();
        Long replyToUserId = params.containsKey("replyToUserId") ? 
            Long.valueOf(params.get("replyToUserId").toString()) : null;
        String replyToNickname = (String) params.get("replyToNickname");
        
        boolean success = commentService.addReply(commentId, userId, content, replyToUserId, replyToNickname);
        return success ? Result.success("回复成功") : Result.error("回复失败");
    }

    // 删除评论（用户删除自己的）
    @DeleteMapping("/delete/{commentId}")
    public Result<?> deleteComment(@PathVariable Long commentId, @RequestParam Long userId) {
        boolean success = commentService.deleteComment(commentId, userId);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    // 删除回复
    @DeleteMapping("/reply/delete/{replyId}")
    public Result<?> deleteReply(@PathVariable Long replyId, @RequestParam Long userId) {
        boolean success = commentService.deleteReply(replyId, userId);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}