import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getAgents, getAgentSessions, getAgentSession } from '@/http/BackendRequests'

export interface AgentConfig {
  agentId: number
  name: string
  description: string
  avatarUrl?: string
  executionMode: string
  status: number
  tools?: ToolConfig[]
}

export interface ToolConfig {
  toolId: number
  name: string
  displayName: string
  description: string
  toolType: string
}

export interface AgentSession {
  sessionId: number
  agentId: number
  userInput: string
  finalOutput?: string
  status: string
  startedAt: string
  completedAt?: string
}

export interface StepEvent {
  type: 'THOUGHT' | 'ACTION' | 'OBSERVATION' | 'ANSWER' | 'ERROR' | 'HEARTBEAT' | 'DONE'
  stepOrder?: number
  content?: string
  toolName?: string
  done?: boolean
  sessionId?: number
}

export const useAgentStore = defineStore('agent', () => {
  const agents = ref<AgentConfig[]>([])
  const selectedAgentId = ref<number | null>(null)
  const sessions = ref<AgentSession[]>([])
  const currentSessionId = ref<number | null>(null)
  const steps = ref<StepEvent[]>([])
  const isExecuting = ref(false)
  const finalAnswer = ref('')

  const selectedAgent = computed(() =>
    agents.value.find(a => a.agentId === selectedAgentId.value) ?? null
  )

  async function loadAgents(workspaceId = 0) {
    try {
      const res = await getAgents<any>(workspaceId)
      if (res?.data?.result) {
        agents.value = res.data.result
        // Auto-select first active agent
        if (!selectedAgentId.value && agents.value.length > 0) {
          const active = agents.value.find(a => a.status === 2)
          if (active) selectedAgentId.value = active.agentId
        }
      }
    } catch (e) {
      console.error('Failed to load agents:', e)
    }
  }

  async function loadSessions(agentId?: number) {
    try {
      const res = await getAgentSessions<any>(agentId)
      if (res?.data?.result) {
        sessions.value = res.data.result
      }
    } catch (e) {
      console.error('Failed to load sessions:', e)
    }
  }

  async function loadSessionDetail(sessionId: number) {
    try {
      const res = await getAgentSession<any>(sessionId)
      if (res?.data?.result) {
        const session = res.data.result
        // Restore steps from session detail
        if (session.steps) {
          steps.value = session.steps.map((s: any) => ({
            type: extractStepType(s.stepDesc),
            stepOrder: s.stepOrder,
            content: s.outputResult ? JSON.parse(s.outputResult)?.content : '',
            toolName: extractToolName(s.stepDesc),
          }))
        }
        finalAnswer.value = session.finalOutput || ''
      }
    } catch (e) {
      console.error('Failed to load session detail:', e)
    }
  }

  function extractStepType(stepDesc: string): StepEvent['type'] {
    if (!stepDesc) return 'THOUGHT'
    if (stepDesc.startsWith('THOUGHT')) return 'THOUGHT'
    if (stepDesc.startsWith('ACTION')) return 'ACTION'
    if (stepDesc.startsWith('OBSERVATION')) return 'OBSERVATION'
    if (stepDesc.startsWith('ANSWER')) return 'ANSWER'
    return 'THOUGHT'
  }

  function extractToolName(stepDesc: string): string | undefined {
    const match = stepDesc?.match(/^ACTION: (.+)$/)
    return match ? match[1] : undefined
  }

  function selectAgent(agentId: number) {
    selectedAgentId.value = agentId
    clearSteps()
    loadSessions(agentId)
  }

  function addStep(step: StepEvent) {
    if (step.type === 'HEARTBEAT') return
    steps.value.push(step)
    if (step.type === 'ANSWER') {
      finalAnswer.value = step.content || ''
    }
  }

  function clearSteps() {
    steps.value = []
    finalAnswer.value = ''
    currentSessionId.value = null
  }

  function setExecuting(v: boolean) {
    isExecuting.value = v
  }

  function prependSession(session: Partial<AgentSession>) {
    sessions.value.unshift(session as AgentSession)
  }

  return {
    agents,
    selectedAgentId,
    selectedAgent,
    sessions,
    currentSessionId,
    steps,
    isExecuting,
    finalAnswer,
    loadAgents,
    loadSessions,
    loadSessionDetail,
    selectAgent,
    addStep,
    clearSteps,
    setExecuting,
    prependSession,
  }
})
