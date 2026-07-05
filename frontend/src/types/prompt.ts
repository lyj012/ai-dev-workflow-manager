export interface GeneratePromptResponse {
  promptContent: string
  promptTemplateId?: number
  variables?: Record<string, string>
}
