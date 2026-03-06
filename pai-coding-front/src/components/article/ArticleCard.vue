<template>
  <div class="border-b border-gray-100 py-3 px-1 hover:bg-gray-50 transition-colors">
    <div class="flex items-start gap-3">
      <!-- Cover -->
      <img v-if="article.cover" :src="article.cover" alt="cover"
           class="w-20 h-14 object-cover rounded flex-shrink-0" />

      <div class="flex-1 min-w-0">
        <!-- Title -->
        <div class="text-sm font-medium text-gray-800 truncate mb-1">
          {{ article.title || article.shortTitle }}
        </div>
        <!-- Summary -->
        <div v-if="article.summary" class="text-xs text-gray-400 line-clamp-2 mb-1">
          {{ article.summary }}
        </div>
        <!-- Meta -->
        <div class="flex items-center gap-3 text-xs text-gray-400">
          <span>{{ article.authorName }}</span>
          <span>👁 {{ article.count?.readCount ?? 0 }}</span>
          <span>👍 {{ article.count?.praiseCount ?? 0 }}</span>
          <span>{{ formatDate(article.createTime) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'

defineProps<{ article: ArticleType }>()

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = (now.getTime() - d.getTime()) / 1000
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)} 小时前`
  return d.toLocaleDateString()
}
</script>
