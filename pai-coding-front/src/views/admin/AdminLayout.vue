<template>
  <div class="flex h-screen bg-gray-50">
    <!-- Sidebar -->
    <aside class="w-48 border-r bg-white flex flex-col shadow-sm flex-shrink-0">
      <div class="p-4 border-b">
        <h2 class="text-base font-semibold text-gray-700">管理后台</h2>
      </div>
      <nav class="flex-1 p-3 space-y-1">
        <router-link
          to="/admin/agents"
          class="sidebar-link"
          :class="{ 'sidebar-link-active': route.path.startsWith('/admin/agents') }"
        >
          Agent 管理
        </router-link>
        <router-link
          to="/admin/tools"
          class="sidebar-link"
          :class="{ 'sidebar-link-active': route.path.startsWith('/admin/tools') }"
        >
          工具管理
        </router-link>
        <router-link
          to="/admin/models"
          class="sidebar-link"
          :class="{ 'sidebar-link-active': route.path.startsWith('/admin/models') }"
        >
          模型管理
        </router-link>
        <router-link
          to="/admin/skills"
          class="sidebar-link"
          :class="{ 'sidebar-link-active': route.path.startsWith('/admin/skills') }"
        >
          🎯 技能库
        </router-link>
      </nav>
      <div class="p-3 border-t">
        <a href="/" class="sidebar-link text-gray-400">← 返回主站</a>
      </div>
    </aside>
    <!-- Main content -->
    <main class="flex-1 overflow-auto p-6">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<style scoped>
.sidebar-link {
  display: block;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 14px;
  color: #374151;
  text-decoration: none;
  transition: background-color 0.15s;
}
.sidebar-link:hover {
  background-color: #f3f4f6;
  color: #1d4ed8;
}
.sidebar-link-active {
  background-color: #eff6ff;
  color: #1d4ed8;
  font-weight: 500;
}
</style>
