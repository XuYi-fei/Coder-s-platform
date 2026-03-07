<template>
  <div v-loading="loading">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-xl font-semibold text-gray-800">技能库管理</h1>
      <div class="flex gap-2">
        <el-button type="success" @click="uploadDialogVisible = true">上传 Skill 包</el-button>
        <el-button type="primary" @click="openCreateDialog">新建技能</el-button>
      </div>
    </div>

    <!-- Table -->
    <el-table :data="skills" border stripe class="w-full">
      <el-table-column label="名称" min-width="150">
        <template #default="{ row }">
          <div class="font-medium">{{ row.displayName }}</div>
          <div class="text-xs text-gray-400 mt-0.5">{{ row.name }}</div>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="140">
        <template #default="{ row }">
          <el-tag :type="skillTypeTag(row.skillType)" size="small">{{ row.skillType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="描述" min-width="200">
        <template #default="{ row }">
          <div class="line-clamp-2 text-sm text-gray-600">{{ row.description }}</div>
        </template>
      </el-table-column>
      <el-table-column label="系统内置" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.isSystem === 1" type="warning" size="small">内置</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="info" @click="previewSkill(row)">预览</el-button>
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-tooltip v-if="row.isSystem === 1" content="系统内置技能不可删除" placement="top">
            <el-button size="small" type="danger" disabled>删除</el-button>
          </el-tooltip>
          <el-button v-else size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- ============ Upload Skill Zip Dialog ============ -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传 Skill 包（.zip）"
      width="560px"
      @close="resetUploadForm"
    >
      <div class="text-sm text-gray-500 mb-4 leading-relaxed">
        Skill 包是一个 <code>.zip</code> 文件，根目录须包含 <code>SKILL.md</code>。<br />
        <code>SKILL.md</code> 顶部使用 YAML frontmatter 声明技能元数据：
        <pre class="mt-2 p-3 bg-gray-50 rounded text-xs overflow-auto">---
name: my_skill
display_name: 我的技能
description: 技能的简短描述
---

# 技能说明正文 (Markdown)</pre>
      </div>

      <!-- Drop zone -->
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        accept=".zip"
        :limit="1"
        :on-change="onFileChange"
        :on-exceed="() => ElMessage.warning('每次只能上传一个 zip 文件')"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将 .zip 文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">仅支持 .zip 格式，文件大小不超过 20MB</div>
        </template>
      </el-upload>

      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" :disabled="!selectedFile" @click="handleUpload">
          上传并创建
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ Create/Edit Dialog ============ -->
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
          <el-input v-model="form.displayName" placeholder="如 代码审查" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="技能描述" />
        </el-form-item>
        <el-form-item label="技能类型" prop="skillType">
          <el-select v-model="form.skillType" class="w-full">
            <el-option label="PROMPT_TEMPLATE（提示词模板）" value="PROMPT_TEMPLATE" />
            <el-option label="FILE_BASED（zip 文件技能）" value="FILE_BASED" />
            <el-option label="CODE_SNIPPET" value="CODE_SNIPPET" />
            <el-option label="HTTP_PIPELINE" value="HTTP_PIPELINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="技能内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            placeholder="输入提示词模板或 Markdown 正文..."
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

    <!-- ============ Preview Dialog ============ -->
    <el-dialog v-model="previewVisible" :title="previewSkillData?.displayName" width="700px">
      <div v-if="previewSkillData">
        <div class="flex gap-2 mb-3">
          <el-tag type="info" size="small">{{ previewSkillData.name }}</el-tag>
          <el-tag :type="skillTypeTag(previewSkillData.skillType)" size="small">{{ previewSkillData.skillType }}</el-tag>
        </div>
        <p class="text-sm text-gray-500 mb-4">{{ previewSkillData.description }}</p>

        <!-- FILE_BASED: show SKILL.md -->
        <div v-if="previewSkillData.skillType === 'FILE_BASED' && previewSkillData.skillMd">
          <div class="text-xs font-semibold text-gray-400 mb-1">SKILL.md</div>
          <pre class="bg-gray-50 rounded p-3 text-xs overflow-auto max-h-96 whitespace-pre-wrap">{{ previewSkillData.skillMd }}</pre>
          <div v-if="previewSkillData.ossUrl" class="mt-2 text-xs text-gray-400">
            Zip 存储：<a :href="previewSkillData.ossUrl" target="_blank" class="text-blue-500 underline">下载</a>
          </div>
        </div>

        <!-- PROMPT_TEMPLATE / others: show content -->
        <div v-else-if="previewSkillData.content">
          <div class="text-xs font-semibold text-gray-400 mb-1">内容</div>
          <pre class="bg-gray-50 rounded p-3 text-xs overflow-auto max-h-96 whitespace-pre-wrap">{{ previewSkillData.content }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadInstance } from 'element-plus'
import { getSkills, createSkill, updateSkill, deleteSkill, uploadSkillZip } from '@/http/BackendRequests'

const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const dialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const previewVisible = ref(false)
const isEdit = ref(false)
const skills = ref<any[]>([])
const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const editingSkillId = ref<number | null>(null)
const selectedFile = ref<File | null>(null)
const previewSkillData = ref<any>(null)

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
  if (type === 'FILE_BASED') return 'success'
  if (type === 'PROMPT_TEMPLATE') return ''
  return 'info'
}

async function loadSkills() {
  loading.value = true
  try {
    const res = await getSkills<any>()
    skills.value = res?.data?.result ?? []
  } catch {
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

function previewSkill(row: any) {
  previewSkillData.value = row
  previewVisible.value = true
}

function resetForm() {
  form.value = defaultForm()
  formRef.value?.clearValidate()
}

function resetUploadForm() {
  selectedFile.value = null
  uploadRef.value?.clearFiles()
}

function onFileChange(file: any) {
  selectedFile.value = file.raw as File
}

async function handleUpload() {
  if (!selectedFile.value) return
  uploading.value = true
  try {
    await uploadSkillZip(selectedFile.value)
    ElMessage.success('Skill 包上传成功')
    uploadDialogVisible.value = false
    resetUploadForm()
    await loadSkills()
  } catch (e: any) {
    const msg = e?.response?.data?.status?.msg ?? 'Skill 上传失败'
    ElMessage.error(msg)
  } finally {
    uploading.value = false
  }
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
  } catch {
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
