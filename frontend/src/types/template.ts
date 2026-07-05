export interface TemplateCandidate {
  templateId: number
  templateName: string
  score: number
  reason: string
}

export interface MatchTemplateResponse {
  matchedTemplateId?: number
  matchedTemplateName?: string
  score?: number
  reason?: string
  autoBound: boolean
  candidates: TemplateCandidate[]
}
