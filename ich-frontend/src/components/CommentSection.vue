<template>
  <div class="comment-section">
    <h3 class="text-xl font-bold text-ich-dark mb-6">网友评论 ({{ comments.length }})</h3>
    
    <!-- 发表评论 -->
    <div v-if="userStore.isLoggedIn" class="mb-6">
      <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="分享你的看法..." />
      <div class="flex justify-between items-center mt-3">
        <div class="text-xs text-gray-400">支持文明发言，共建和谐社区</div>
        <el-button type="danger" @click="submitComment" :loading="submitting">发表评论</el-button>
      </div>
    </div>
    <div v-else class="mb-6 text-center py-4 bg-gray-50 rounded-lg">
      <p class="text-gray-500"><el-link type="primary" @click="$router.push('/login')">登录</el-link> 后发表评论</p>
    </div>
    
    <!-- 评论列表 -->
    <div class="space-y-6">
      <div v-for="comment in comments" :key="comment.id" class="border-b border-gray-100 pb-6">
        <!-- 评论头部 -->
        <div class="flex items-center justify-between mb-2">
          <div class="flex items-center gap-2">
            <img :src="comment.avatar || getDefaultAvatar(comment.userId)" class="w-8 h-8 rounded-full object-cover" />
            <span class="font-medium">{{ comment.nickname || '用户' + comment.userId }}</span>
            <span class="text-gray-400 text-xs">{{ formatTime(comment.createTime) }}</span>
          </div>
          <div v-if="comment.userId === userStore.userInfo?.id" class="flex gap-2">
            <button @click="deleteComment(comment.id)" class="text-gray-400 hover:text-ich-red text-xs">删除</button>
          </div>
        </div>
        
        <!-- 评论内容 -->
        <p class="text-gray-700 mb-2">{{ comment.content }}</p>
        
        <!-- 评论操作栏 -->
        <div class="flex items-center gap-4 text-sm">
          <button @click="toggleLike(comment)" class="flex items-center gap-1 hover:text-ich-red transition"
            :class="comment.isLiked ? 'text-ich-red' : 'text-gray-400'">
            <el-icon><Star /></el-icon> {{ comment.likeCount || 0 }}
          </button>
          <button @click="showReplyInput(comment.id)" class="text-gray-400 hover:text-ich-red transition">
            <el-icon><ChatDotRound /></el-icon> 回复
          </button>
        </div>
        
        <!-- 回复列表 -->
        <div v-if="comment.replies && comment.replies.length" class="mt-3 pl-4 border-l-2 border-ich-gold/30">
          <div v-for="reply in comment.replies" :key="reply.id" class="mb-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="font-medium text-sm">{{ reply.nickname || '用户' + reply.userId }}</span>
                <span class="text-gray-400 text-xs">{{ formatTime(reply.createTime) }}</span>
              </div>
              <div v-if="reply.userId === userStore.userInfo?.id">
                <button @click="deleteReply(reply.id)" class="text-gray-400 hover:text-ich-red text-xs">删除</button>
              </div>
            </div>
            <p class="text-gray-600 text-sm mt-1">
              <span v-if="reply.replyToNickname" class="text-ich-gold">@{{ reply.replyToNickname }} </span>
              {{ reply.content }}
            </p>
          </div>
        </div>
        
        <!-- 回复输入框 -->
        <div v-if="replyingTo === comment.id" class="mt-3 flex gap-2">
          <el-input v-model="replyContent" size="small" placeholder="写下你的回复..." @keyup.enter="submitReply(comment.id)" />
          <el-button size="small" type="danger" @click="submitReply(comment.id)" :loading="replying">回复</el-button>
          <el-button size="small" @click="replyingTo = null">取消</el-button>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-if="comments.length === 0" class="text-center py-8 text-gray-500">
      暂无评论，快来抢沙发～
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { addComment, getComments, likeComment, unlikeComment, addReply, deleteComment, deleteReply } from '@/api/comment'

const props = defineProps({ heritageId: { type: Number, required: true } })
const emit = defineEmits(['update'])
const userStore = useUserStore()

const comments = ref([])
const commentContent = ref('')
const replyContent = ref('')
const replyingTo = ref(null)
const submitting = ref(false)
const replying = ref(false)

const getDefaultAvatar = (id) => `https://api.dicebear.com/7.x/avataaars/svg?seed=${id}`

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

const fetchComments = async () => {
  try {
    const res = await getComments(props.heritageId)
    if (res.code === 200) {
      comments.value = (res.data || []).map(c => ({ ...c, isLiked: false }))
    }
  } catch (error) {
    console.error('获取评论失败:', error)
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    await addComment(props.heritageId, userStore.userInfo.id, commentContent.value)
    ElMessage.success('评论成功')
    commentContent.value = ''
    await fetchComments()
    emit('update')
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const toggleLike = async (comment) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (comment.isLiked) {
      await unlikeComment(comment.id, userStore.userInfo.id)
      comment.likeCount = (comment.likeCount || 1) - 1
      comment.isLiked = false
    } else {
      await likeComment(comment.id, userStore.userInfo.id)
      comment.likeCount = (comment.likeCount || 0) + 1
      comment.isLiked = true
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const showReplyInput = (commentId) => {
  replyingTo.value = replyingTo.value === commentId ? null : commentId
  replyContent.value = ''
}

const submitReply = async (commentId) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replying.value = true
  try {
    await addReply({
      commentId,
      userId: userStore.userInfo.id,
      content: replyContent.value
    })
    ElMessage.success('回复成功')
    replyingTo.value = null
    replyContent.value = ''
    await fetchComments()
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    replying.value = false
  }
}

const deleteOwnComment = async (commentId) => {
  try {
    await deleteComment(commentId, userStore.userInfo.id)
    ElMessage.success('删除成功')
    await fetchComments()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const deleteOwnReply = async (replyId) => {
  try {
    await deleteReply(replyId, userStore.userInfo.id)
    ElMessage.success('删除成功')
    await fetchComments()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

fetchComments()
</script>