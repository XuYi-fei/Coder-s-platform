<template>
  <div v-loading="loading">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-xl font-semibold text-gray-800">技能库管理</h1>
      <div class="flex gap-2">
        <el-button type="success" @click="uploadDialogVisible = true">上传 Skill 包（含资源）</el-button>
        <el-button type="primary" @click="openCreateDialog">新建 SIMPLE Skill</el-button>
      </div>
    </div>

    <!-- Table -->
    <el-table :data="skills" border stripe class="w-full">
      <el-table-column label="名称" min-width="150">
        <template #default="{ row }">
          <div class="font-medium">{{ row.displayName }}</div>
          <div class="text-xs text-gray-400 mt-0.5 font-mono">{{ row.name }}</div>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="110">
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
          <el-button size="small" type="info" @click="previewSkill(row)">SKILL.md</el-button>
          <el-button size="small" type="primary" :disabled="row.skillType === 'FOLDER'" @click="openEditDialog(row)">编辑</el-button>
          <el-tooltip v-if="row.isSystem === 1" content="系统内置技能不可删除" placement="top">
            <el-button size="small" type="danger" disabled>删除</el-button>
          </el-tooltip>
          <el-button v-else size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- ============ Upload FOLDER Skill Dialog ============ -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传 Skill 包"
      width="580px"
      @close="resetUploadForm"
    >
      <div class="text-sm text-gray-600 mb-4 leading-relaxed space-y-2">
        <p>
          Skill 是一个<strong>文件夹</strong>，核心是 <code>SKILL.md</code>。
          上传时将文件夹压缩为 <code>.zip</code> 上传。
        </p>
        <p class="text-gray-500">
          • <strong>SIMPLE</strong>：zip 只含 <code>SKILL.md</code>，系统自动识别<br/>
          • <strong>FOLDER</strong>：zip 含 <code>SKILL.md</code> + 额外资源目录（<code>scripts/</code>、<code>references/</code>、<code>assets/</code>）
        </p>
        <pre class="mt-2 p-3 bg-gray-50 rounded text-xs overflow-auto">skill-name/
├── SKILL.md          # 必须，YAML frontmatter + Markdown 指令
├── scripts/          # 可选，可执行脚本
├── references/       # 可选，参考文档
└── assets/           # 可选，模板/图标等资源</pre>
        <p class="text-xs text-gray-400 mt-2">
          <code>SKILL.md</code> 顶部 YAML frontmatter 示例：
        </p>
        <pre class="p-3 bg-gray-50 rounded text-xs overflow-auto">---
name: my_skill
display_name: 我的技能
description: 当用户需要 xxx 时触发，帮助完成 xxx 任务
---

# 技能说明正文（Markdown）
你是一位专业的...（指令内容）</pre>
      </div>

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
        <div class="el-upload__text">将 <em>.zip</em> 拖到此处，或<em>点击选择</em></div>
        <template #tip>
          <div class="el-upload__tip">仅支持 .zip，大小不超过 20MB</div>
        </template>
      </el-upload>

      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" :disabled="!selectedFile" @click="handleUpload">
          上传并创建
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ Create/Edit SIMPLE Skill Dialog ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑 Skill（SIMPLE）' : '新建 Skill（SIMPLE）'"
      width="680px"
      @close="resetForm"
    >
      <div class="text-xs text-gray-400 mb-4 leading-relaxed bg-blue-50 rounded p-3">
        <strong>SIMPLE Skill</strong> = 只有 SKILL.md，无额外资源文件。填写下方字段后，系统会自动生成标准 SKILL.md 格式存储。
        若 Skill 需要附带脚本或参考文档，请改用"上传 Skill 包"。
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="标识名称" prop="name">
          <el-input v-model="form.name" placeholder="英文下划线，如 code_review" />
          <div class="text-xs text-gray-400 mt-1">对应 SKILL.md frontmatter 中的 <code>name</code>，LLM 通过此识别技能</div>
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="form.displayName" placeholder="如 代码审查" />
        </el-form-item>
        <el-form-item label="描述（触发条件）" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="描述技能的触发场景和功能，LLM 根据此决定何时使用该技能。如：当用户提交代码需要审查时触发，帮助分析代码质量"
          />
          <div class="text-xs text-gray-400 mt-1">对应 SKILL.md frontmatter 中的 <code>description</code></div>
        </el-form-item>
        <el-form-item label="SKILL.md 正文" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="Markdown 格式的技能指令正文（frontmatter 之后的部分）。
例如：
你是一位资深软件工程师，专注于代码质量审查。
当用户提供代码时，从以下维度审查：
1. 代码规范
2. 潜在问题
..."
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

    <!-- ============ SKILL.md Preview Dialog ============ -->
    <el-dialog v-model="previewVisible" :title="`SKILL.md — ${previewSkillData?.displayName}`" width="720px">
      <div v-if="previewSkillData">
        <div class="flex gap-2 mb-3">
          <el-tag type="info" size="small" class="font-mono">{{ previewSkillData.name }}</el-tag>
          <el-tag :type="skillTypeTag(previewSkillData.skillType)" size="small">{{ previewSkillData.skillType }}</el-tag>
        </div>

        <!-- 优先展示完整 SKILL.md -->
        <div v-if="previewSkillData.skillMd">
          <div class="text-xs font-semibold text-gray-400 mb-1 uppercase tracking-wide">SKILL.md</div>
          <pre class="bg-gray-50 rounded p-4 text-xs overflow-auto max-h-[480px] whitespace-pre-wrap leading-relaxed">{{ previewSkillData.skillMd }}</pre>
          <div v-if="previewSkillData.skillType === 'FOLDER' && previewSkillData.ossUrl" class="mt-2 text-xs text-gray-400">
            Skill 包（含额外资源）：
            <a :href="previewSkillData.ossUrl" target="_blank" class="text-blue-500 underline">下载 zip</a>
          </div>
        </div>
        <!-- 降级：无 skill_md 时展示 content -->
        <div v-else-if="previewSkillData.content">
          <div class="text-xs font-semibold text-gray-400 mb-1">内容（旧格式）</div>
          <pre class="bg-gray-50 rounded p-4 text-xs overflow-auto max-h-[480px] whitespace-pre-wrap">{{ previewSkillData.content }}</pre>
        </div>
        <div v-else class="text-sm text-gray-400">暂无内容</div>
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
  content: '',
  visibility: 3,
  workspaceId: 0,
})

const form = ref(defaultForm())

const rules: FormRules = {
  name: [{ required: true, message: '请输入标识名称', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  description: [{ required: true, message: '请填写描述（触发条件）', trigger: 'blur' }],
  content: [{ required: true, message: '请输入 SKILL.md 正文', trigger: 'blur' }],
}

/** SIMPLE → primary(蓝)，FOLDER → success(绿) */
const skillTypeTag = (type: string): '' | 'success' | 'danger' | 'info' | 'warning' => {
  if (type === 'FOLDER') return 'success'
  if (type === 'SIMPLE') return ''
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
    content: row.content ?? '',
    visibility: row.visibility ?? 3,
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

/** 提交 SIMPLE skill（前端拼装 skill_md，后端作为备用也会生成） */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const { name, displayName, description, content, visibility, workspaceId } = form.value
    // 前端拼装标准 SKILL.md，与后端生成逻辑保持一致
    const skillMd = `---\nname: ${name}\ndisplay_name: ${displayName}\ndescription: ${description}\n---\n\n${content.trim()}`
    const payload = { name, displayName, description, content, skillMd, skillType: 'SIMPLE', visibility, workspaceId }

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
