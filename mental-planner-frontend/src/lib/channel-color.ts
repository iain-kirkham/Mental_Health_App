// Sunsama-style channel/tag pills: each task category gets a stable pastel color drawn from the
// Catppuccin accent palette, so the same tag always reads the same color across the app without
// needing a stored color field on the task.
const CHANNEL_COLORS = [
  { pill: 'bg-primary/15 text-primary', border: 'border-l-primary' }, // Mauve
  { pill: 'bg-chart-4/20 text-chart-4', border: 'border-l-chart-4' }, // Peach
  { pill: 'bg-sapphire/20 text-sapphire', border: 'border-l-sapphire' },
  { pill: 'bg-chart-2/20 text-chart-2', border: 'border-l-chart-2' }, // Green
  { pill: 'bg-flamingo/20 text-flamingo', border: 'border-l-flamingo' },
] as const

function hashString(value: string): number {
  let hash = 0
  for (let i = 0; i < value.length; i++) {
    hash = (hash * 31 + value.charCodeAt(i)) | 0
  }
  return Math.abs(hash)
}

function colorFor(category: string) {
  return CHANNEL_COLORS[hashString(category) % CHANNEL_COLORS.length]
}

/** Soft pastel pill classes (background + text) for a category/channel tag. */
export function channelPillClass(category: string): string {
  return colorFor(category).pill
}

/** Left-border accent classes for a category/channel, e.g. for timeline blocks. */
export function channelBorderClass(category: string): string {
  return `border-l-2 ${colorFor(category).border}`
}
