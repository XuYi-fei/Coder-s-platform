<template>
  <div v-loading="loading">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-xl font-semibold text-gray-800">模型管理</h1>
      <el-button type="primary" @click="openCreateDialog">新建模型</el-button>
    </div>

    <!-- Table -->
    <el-table :data="models" border stripe class="w-full">
      <el-table-column prop="name" label="名称" min-width="120" />
      <el-table-column prop="provider" label="提供商" width="120" />
      <el-table-column prop="modelName" label="模型ID" width="150" />
      <el-table-column prop="baseUrl" label="BaseURL" min-width="200" show-overflow-tooltip />
      <el-table-column label="API Key" width="150">
        <template #default="{ row }">
          {{ row.apiKeyMasked || '******' }}
        </template>
      </el-table-column>
      <el-table-column prop="maxTokens" label="最大Token" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑模型' : '新建模型'"
      width="580px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="form.name" placeholder="如 我的通义千问配置" />
        </el-form-item>
        <el-form-item label="提供商" prop="provider">
          <el-input v-model="form.provider" placeholder="如 Alibaba / OpenAI / Anthropic" />
        </el-form-item>
        <el-form-item label="模型ID" prop="modelName">
          <el-input v-model="form.modelName" placeholder="如 qwen-plus / gpt-4o" />
        </el-form-item>
        <el-form-item label="Base URL">
          <el-input
            v-model="form.baseUrl"
            placeholder="如 https://dashscope.aliyuncs.com/compatible-mode/v1"
          />
        </el-form-item>
        <el-form-item label="API Key">
          <el-input
            v-model="form.apiKey"
            type="password"
            show-password
            placeholder="留空则不修改"
          />
        </el-form-item>
        <el-form-item label="最大 Token">
          <el-input-number v-model="form.maxTokens" :min="256" :max="128000" :step="256" />
        </el-form-item>
        <el-form-item label="Temperature">
          <div class="w-full flex items-center gap-3">
            <el-slider
              v-model="form.temperature"
              :min="0"
              :max="2"
              :step="0.1"
              class="flex-1"
            />
            <span class="text-sm text-gray-600 w-8">{{ form.temperature }}</span>
          </div>
        </el-form-item>
        <el-form-item label="可见性">
          <el-radio-group v-model="form.visibility">
            <el-radio :value="1">PRIVATE</el-radio>
            <el-radio :value="2">WORKSPACE</el-radio>
            <el-radio :value="3">PUBLIC</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getAgentModels, createModel, updateModel, deleteModel } from '@/http/BackendRequests'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const models = ref<any[]>([])
const formRef = ref<FormInstance>()
const editingModelId = ref<number | null>(null)

const defaultForm = () => ({
  name: '',
  provider: '',
  modelName: '',
  baseUrl: '',
  apiKey: '',
  maxTokens: 4096,
  temperature: 0.7,
  visibility: 1,
  workspaceId: 0,
})

const form = ref(defaultForm())

const rules: FormRules = {
  name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  provider: [{ required: true, message: '请输入提供商', trigger: 'blur' }],
  modelName: [{ required: true, message: '请输入模型ID', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAgentModels<any>()
    models.value = res?.data?.result ?? []
  } catch (e) {
    ElMessage.error('加载模型列表失败')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  isEdit.value = false
  editingModelId.value = null
  form.value = defaultForm()
  dialogVisible.value = true
}

function openEditDialog(row: any) {
  isEdit.value = true
  editingModelId.value = row.modelConfigId
  form.value = {
    name: row.name ?? '',
    provider: row.provider ?? '',
    modelName: row.modelName ?? '',
    baseUrl: row.baseUrl ?? '',
    apiKey: '',
    maxTokens: row.maxTokens ?? 4096,
    temperature: row.temperature ?? 0.7,
    visibility: row.visibility ?? 1,
    workspaceId: row.workspaceId ?? 0,
  }
  dialogVisible.value = true
}

function resetForm() {
  form.value = defaultForm()
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload: any = { ...form.value }
    // If apiKey is empty string, omit it from payload on edit
    if (isEdit.value && !payload.apiKey) {
      delete payload.apiKey
    }
    if (isEdit.value && editingModelId.value != null) {
      await updateModel(editingModelId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createModel(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确认删除模型配置「${row.name}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteModel(row.modelConfigId)
    ElMessage.success('删除成功')
    await loadData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>
