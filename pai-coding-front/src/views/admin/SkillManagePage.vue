<template>
  <div v-loading="loading">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-xl font-semibold text-gray-800">技能库管理</h1>
      <el-button type="primary" @click="openCreateDialog">新建技能</el-button>
    </div>

    <!-- Table -->
    <el-table :data="skills" border stripe class="w-full">
      <el-table-column label="名称" min-width="150">
        <template #default="{ row }">
          <div class="font-medium">{{ row.displayName }}</div>
          <div class="text-xs text-gray-400 mt-0.5">{{ row.name }}</div>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="160">
        <template #default="{ row }">
          <el-tag :type="skillTypeTag(row.skillType)" size="small">{{ row.skillType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="描述" min-width="200">
        <template #default="{ row }">
          <div class="line-clamp-2 text-sm text-gray-600">{{ row.description }}</div>
        </template>
      </el-table-column>
      <el-table-column label="系统内置" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.isSystem === 1" type="warning" size="small">系统内置</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-tooltip
            v-if="row.isSystem === 1"
            content="系统内置技能不可删除"
            placement="top"
          >
            <el-button size="small" type="danger" disabled>删除</el-button>
          </el-tooltip>
          <el-button
            v-else
            size="small"
            type="danger"
            @click="handleDelete(row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑技能' : '新建技能'"
      width="640px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="标识名称" prop="name">
          <el-input v-model="form.name" placeholder="英文下划线格式，如 code_review" />
          <div class="text-xs text-gray-400 mt-1">英文下划线格式，LLM 通过此识别技能</div>
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="form.displayName" placeholder="请输入显示名称，如 代码审查" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="请输入技能描述" />
        </el-form-item>
        <el-form-item label="技能类型" prop="skillType">
          <el-select v-model="form.skillType" placeholder="请选择技能类型" class="w-full">
            <el-option label="PROMPT_TEMPLATE" value="PROMPT_TEMPLATE" />
            <el-option label="CODE_SNIPPET" value="CODE_SNIPPET" />
            <el-option label="HTTP_PIPELINE" value="HTTP_PIPELINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="技能内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            placeholder="输入提示词模板内容..."
          />
        </el-form-item>
        <el-form-item label="可见性">
          <el-radio-group v-model="form.visibility">
            <el-radio :value="1">私有</el-radio>
            <el-radio :value="2">工作区</el-radio>
            <el-radio :value="3">公开</el-radio>
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
import { getSkills, createSkill, updateSkill, deleteSkill } from '@/http/BackendRequests'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const skills = ref<any[]>([])
const formRef = ref<FormInstance>()
const editingSkillId = ref<number | null>(null)

const defaultForm = () => ({
  name: '',
  displayName: '',
  description: '',
  skillType: 'PROMPT_TEMPLATE',
  content: '',
  visibility: 1,
  workspaceId: 0,
})

const form = ref(defaultForm())

const rules: FormRules = {
  name: [{ required: true, message: '请输入标识名称', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  skillType: [{ required: true, message: '请选择技能类型', trigger: 'change' }],
}

const skillTypeTag = (type: string): '' | 'success' | 'danger' | 'info' | 'warning' => {
  return type === 'PROMPT_TEMPLATE' ? '' : 'info'
}

async function loadSkills() {
  loading.value = true
  try {
    const res = await getSkills<any>()
    skills.value = res?.data?.result ?? []
  } catch (e) {
    ElMessage.error('加载技能列表失败')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  isEdit.value = false
  editingSkillId.value = null
  form.value = defaultForm()
  dialogVisible.value = true
}

function openEditDialog(row: any) {
  isEdit.value = true
  editingSkillId.value = row.skillId
  form.value = {
    name: row.name ?? '',
    displayName: row.displayName ?? '',
    description: row.description ?? '',
    skillType: row.skillType ?? 'PROMPT_TEMPLATE',
    content: row.content ?? '',
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
    if (isEdit.value && editingSkillId.value != null) {
      await updateSkill(editingSkillId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createSkill(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadSkills()
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确认删除技能「${row.displayName}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteSkill(row.skillId)
    ElMessage.success('删除成功')
    await loadSkills()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadSkills)
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
