<template>
  <div v-loading="loading">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-xl font-semibold text-gray-800">工具管理</h1>
      <el-button type="primary" @click="openCreateDialog">新建工具</el-button>
    </div>

    <!-- Table -->
    <el-table :data="tools" border stripe class="w-full">
      <el-table-column prop="name" label="工具名" min-width="120" />
      <el-table-column prop="displayName" label="显示名" min-width="120" />
      <el-table-column label="类型" width="150">
        <template #default="{ row }">
          <el-tag>{{ row.toolType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="风险级别" width="110">
        <template #default="{ row }">
          <el-tag :type="riskTagType(row.riskLevel)">{{ riskLabel(row.riskLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="timeoutMs" label="超时(ms)" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
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
      :title="isEdit ? '编辑工具' : '新建工具'"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="工具名" prop="name">
          <el-input v-model="form.name" placeholder="英文下划线命名，如 http_request" />
        </el-form-item>
        <el-form-item label="显示名">
          <el-input v-model="form.displayName" placeholder="用户可读的显示名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="工具功能描述" />
        </el-form-item>
        <el-form-item label="工具类型" prop="toolType">
          <el-select v-model="form.toolType" placeholder="请选择工具类型" class="w-full">
            <el-option label="HTTP_API" value="HTTP_API" />
            <el-option label="MCP" value="MCP" />
            <el-option label="AGENT_DELEGATE" value="AGENT_DELEGATE" />
            <el-option label="JAVA_FUNCTION" value="JAVA_FUNCTION" />
            <el-option label="SCRIPT" value="SCRIPT" />
            <el-option label="DATABASE_QUERY" value="DATABASE_QUERY" />
          </el-select>
        </el-form-item>
        <el-form-item label="入参 Schema">
          <el-input
            v-model="form.inputSchema"
            type="textarea"
            :rows="4"
            placeholder="JSON Schema 字符串，如 {&quot;type&quot;:&quot;object&quot;,...}"
          />
        </el-form-item>
        <el-form-item label="执行配置">
          <el-input
            v-model="form.config"
            type="textarea"
            :rows="3"
            placeholder="JSON 配置字符串"
          />
        </el-form-item>
        <el-form-item label="超时(ms)">
          <el-input-number v-model="form.timeoutMs" :min="0" :step="1000" />
        </el-form-item>
        <el-form-item label="重试次数">
          <el-input-number v-model="form.retryCount" :min="0" :max="10" />
        </el-form-item>
        <el-form-item label="风险级别">
          <el-radio-group v-model="form.riskLevel">
            <el-radio :value="1">READ</el-radio>
            <el-radio :value="2">MUTATION</el-radio>
            <el-radio :value="3">DANGEROUS</el-radio>
          </el-radio-group>
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
import { getAgentTools, createTool, updateTool, deleteTool } from '@/http/BackendRequests'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const tools = ref<any[]>([])
const formRef = ref<FormInstance>()
const editingToolId = ref<number | null>(null)

const defaultForm = () => ({
  name: '',
  displayName: '',
  description: '',
  toolType: 'HTTP_API',
  inputSchema: '',
  config: '',
  timeoutMs: 5000,
  retryCount: 0,
  riskLevel: 1,
  visibility: 1,
  workspaceId: 0,
})

const form = ref(defaultForm())

const rules: FormRules = {
  name: [{ required: true, message: '请输入工具名', trigger: 'blur' }],
  toolType: [{ required: true, message: '请选择工具类型', trigger: 'change' }],
}

const riskLabel = (level: number) => {
  const map: Record<number, string> = { 1: 'READ', 2: 'MUTATION', 3: 'DANGEROUS' }
  return map[level] ?? String(level)
}

const riskTagType = (level: number): '' | 'success' | 'danger' | 'info' | 'warning' => {
  const map: Record<number, '' | 'success' | 'danger' | 'info' | 'warning'> = {
    1: 'success',
    2: 'warning',
    3: 'danger',
  }
  return map[level] ?? ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAgentTools<any>()
    tools.value = res?.data?.result ?? []
  } catch (e) {
    ElMessage.error('加载工具列表失败')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  isEdit.value = false
  editingToolId.value = null
  form.value = defaultForm()
  dialogVisible.value = true
}

function openEditDialog(row: any) {
  isEdit.value = true
  editingToolId.value = row.toolId
  form.value = {
    name: row.name ?? '',
    displayName: row.displayName ?? '',
    description: row.description ?? '',
    toolType: row.toolType ?? 'HTTP_API',
    inputSchema: row.inputSchema ?? '',
    config: row.config ?? '',
    timeoutMs: row.timeoutMs ?? 5000,
    retryCount: row.retryCount ?? 0,
    riskLevel: row.riskLevel ?? 1,
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
    if (isEdit.value && editingToolId.value != null) {
      await updateTool(editingToolId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createTool(payload)
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
    await ElMessageBox.confirm(`确认删除工具「${row.displayName || row.name}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteTool(row.toolId)
    ElMessage.success('删除成功')
    await loadData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>
