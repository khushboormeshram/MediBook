export function asItems(result) {
  if (Array.isArray(result)) return result
  if (Array.isArray(result?.content)) return result.content
  return []
}
