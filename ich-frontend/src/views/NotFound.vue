<template>
  <div class="min-h-screen flex items-center justify-center bg-ich-bg">
    <div class="text-center">
      <div class="text-9xl font-bold text-ich-gold">404</div>
      <div class="text-2xl text-gray-600 mt-4">页面不见了～</div>
      <p class="text-gray-400 mt-2">您访问的页面可能已被删除或暂时不可用</p>
      <el-button class="mt-8" type="danger" @click="$router.push('/index')">返回首页</el-button>
    </div>
  </div>
</template>