<template>
  <div class="bg-ich-dark text-white py-12">
    <div class="max-w-7xl mx-auto px-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <div>
          <h3 class="text-xl font-bold text-ich-gold mb-4">非遗云境</h3>
          <p class="opacity-80 text-sm">以数字技术赋能非遗传承，让千年文化在云端永生</p>
        </div>
        <div>
          <h4 class="font-bold mb-4">快速链接</h4>
          <ul class="space-y-2 text-sm opacity-80">
            <li class="cursor-pointer hover:text-ich-gold" @click="$router.push('/index')">首页</li>
            <li class="cursor-pointer hover:text-ich-gold" @click="$router.push('/heritage/list')">非遗项目</li>
            <li class="cursor-pointer hover:text-ich-gold" @click="$router.push('/3d')">3D展厅</li>
            <li class="cursor-pointer hover:text-ich-gold" @click="$router.push('/market')">非遗市集</li>
          </ul>
        </div>
        <div>
          <h4 class="font-bold mb-4">关于我们</h4>
          <ul class="space-y-2 text-sm opacity-80">
            <li class="cursor-pointer hover:text-ich-gold">平台介绍</li>
            <li class="cursor-pointer hover:text-ich-gold">联系我们</li>
            <li class="cursor-pointer hover:text-ich-gold">加入我们</li>
          </ul>
        </div>
        <div>
          <h4 class="font-bold mb-4">联系方式</h4>
          <ul class="space-y-2 text-sm opacity-80">
            <li>邮箱：2971727784@qq.com</li>
            <li>电话：400-123-4567</li>
            <li>地址：青海大学</li>
          </ul>
        </div>
      </div>
      <div class="border-t border-white/20 mt-12 pt-8 text-center text-sm opacity-60">
        © 2025 非遗云境 版权所有 
      </div>
    </div>
  </div>
</template>