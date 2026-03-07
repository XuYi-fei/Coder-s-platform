<template>
  <div v-loading="loading">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-xl font-semibold text-gray-800">Agent 管理</h1>
      <el-button type="primary" @click="openCreateDialog">新建 Agent</el-button>
    </div>

    <!-- Table -->
    <el-table :data="agents" border stripe class="w-full">
      <el-table-column prop="name" label="名称" min-width="120" />
      <el-table-column prop="executionMode" label="执行模式" width="110">
        <template #default="{ row }">
          <el-tag>{{ row.executionMode }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="绑定模型" width="140">
        <template #default="{ row }">
          {{ row.modelName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="工具数" width="80">
        <template #default="{ row }">
          {{ row.tools ? row.tools.length : 0 }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button
            v-if="row.status === 1"
            size="small"
            type="success"
            @click="handleActivate(row)"
          >激活</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑 Agent' : '新建 Agent'"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入 Agent 名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="执行模式" prop="executionMode">
          <el-select v-model="form.executionMode" placeholder="请选择执行模式" class="w-full">
            <el-option label="REACT" value="REACT" />
            <el-option label="CHAIN" value="CHAIN" />
            <el-option label="ROUTE" value="ROUTE" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定模型">
          <el-select v-model="form.modelConfigId" placeholder="请选择模型" class="w-full" clearable>
            <el-option
              v-for="m in models"
              :key="m.modelConfigId"
              :label="`${m.name} (${m.provider})`"
              :value="m.modelConfigId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="系统提示词">
          <el-input
            v-model="form.systemPrompt"
            type="textarea"
            :rows="4"
            placeholder="请输入系统提示词"
          />
        </el-form-item>
        <el-form-item label="最大步数">
          <el-input-number v-model="form.maxSteps" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="绑定工具">
          <el-select v-model="form.toolIds" multiple placeholder="请选择工具" class="w-full">
            <el-option
              v-for="t in tools"
              :key="t.toolId"
              :label="t.displayName || t.name"
              :value="t.toolId"
            />
          </el-select>
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
import {
  getAgents,
  getAgentTools,
  getAgentModels,
  createAgent,
  updateAgent,
  deleteAgent,
  activateAgent,
} from '@/http/BackendRequests'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const agents = ref<any[]>([])
const models = ref<any[]>([])
const tools = ref<any[]>([])
const formRef = ref<FormInstance>()
const editingAgentId = ref<number | null>(null)

const defaultForm = () => ({
  name: '',
  description: '',
  executionMode: 'REACT',
  modelConfigId: null as number | null,
  systemPrompt: '',
  maxSteps: 10,
  toolIds: [] as number[],
  visibility: 1,
  workspaceId: 0,
})

const form = ref(defaultForm())

const rules: FormRules = {
  name: [{ required: true, message: '请输入 Agent 名称', trigger: 'blur' }],
  executionMode: [{ required: true, message: '请选择执行模式', trigger: 'change' }],
}

const statusLabel = (status: number) => {
  const map: Record<number, string> = { 1: 'DRAFT', 2: 'ACTIVE', 3: 'ARCHIVED' }
  return map[status] ?? String(status)
}

const statusTagType = (status: number): '' | 'success' | 'danger' | 'info' | 'warning' => {
  const map: Record<number, '' | 'success' | 'danger' | 'info' | 'warning'> = {
    1: 'info',
    2: 'success',
    3: 'danger',
  }
  return map[status] ?? ''
}

async function loadData() {
  loading.value = true
  try {
    const [agentsRes, modelsRes, toolsRes] = await Promise.all([
      getAgents<any>(),
      getAgentModels<any>(),
      getAgentTools<any>(),
    ])
    agents.value = agentsRes?.data?.result ?? []
    models.value = modelsRes?.data?.result ?? []
    tools.value = toolsRes?.data?.result ?? []
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  isEdit.value = false
  editingAgentId.value = null
  form.value = defaultForm()
  dialogVisible.value = true
}

function openEditDialog(row: any) {
  isEdit.value = true
  editingAgentId.value = row.agentId
  form.value = {
    name: row.name ?? '',
    description: row.description ?? '',
    executionMode: row.executionMode ?? 'REACT',
    modelConfigId: row.modelConfigId ?? null,
    systemPrompt: row.systemPrompt ?? '',
    maxSteps: row.maxSteps ?? 10,
    toolIds: (row.tools ?? []).map((t: any) => t.toolId),
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
    const payload = { ...form.value }
    if (isEdit.value && editingAgentId.value != null) {
      await updateAgent(editingAgentId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createAgent(payload)
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

async function handleActivate(row: any) {
  try {
    await activateAgent(row.agentId)
    ElMessage.success('激活成功')
    await loadData()
  } catch (e) {
    ElMessage.error('激活失败')
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确认删除 Agent「${row.name}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteAgent(row.agentId)
    ElMessage.success('删除成功')
    await loadData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>
