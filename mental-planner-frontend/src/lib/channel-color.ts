// Sunsama-style channel/tag colors: each task category gets a color drawn from the Catppuccin
// accent palette. By default it's a stable hash of the category name (so the same tag always
// reads the same color without needing a stored field), but a user can override any category to
// a specific swatch via useCategoryColorStore - that override then applies everywhere the
// category shows up (pill badge, card border + fill, timeline block border + fill).
export interface ChannelColorEntry {
  name: string
  pill: string
  /** Normal (all-sides) card/block border, tinted with the category color - not a left stripe. */
  cardBorder: string
  /** Soft full-card/block background fill in the same color. */
  cardFill: string
  /** Solid swatch color, used only by the category color picker's dots. */
  dot: string
}

export const CHANNEL_COLORS: readonly ChannelColorEntry[] = [
  { name: 'Mauve', pill: 'bg-primary/15 text-primary', cardBorder: 'border-primary/40', cardFill: 'bg-primary/10', dot: 'bg-primary' },
  { name: 'Peach', pill: 'bg-chart-4/20 text-chart-4', cardBorder: 'border-chart-4/40', cardFill: 'bg-chart-4/10', dot: 'bg-chart-4' },
  { name: 'Blue', pill: 'bg-chart-1/20 text-chart-1', cardBorder: 'border-chart-1/40', cardFill: 'bg-chart-1/10', dot: 'bg-chart-1' },
  { name: 'Sapphire', pill: 'bg-sapphire/20 text-sapphire', cardBorder: 'border-sapphire/40', cardFill: 'bg-sapphire/10', dot: 'bg-sapphire' },
  { name: 'Green', pill: 'bg-chart-2/20 text-chart-2', cardBorder: 'border-chart-2/40', cardFill: 'bg-chart-2/10', dot: 'bg-chart-2' },
  { name: 'Yellow', pill: 'bg-chart-3/20 text-chart-3', cardBorder: 'border-chart-3/40', cardFill: 'bg-chart-3/10', dot: 'bg-chart-3' },
  { name: 'Teal', pill: 'bg-chart-5/20 text-chart-5', cardBorder: 'border-chart-5/40', cardFill: 'bg-chart-5/10', dot: 'bg-chart-5' },
  { name: 'Flamingo', pill: 'bg-flamingo/20 text-flamingo', cardBorder: 'border-flamingo/40', cardFill: 'bg-flamingo/10', dot: 'bg-flamingo' },
] as const

export function normalizeCategoryKey(category: string): string {
  return category.trim().toLowerCase()
}

function hashString(value: string): number {
  let hash = 0
  for (let i = 0; i < value.length; i++) {
    hash = (hash * 31 + value.charCodeAt(i)) | 0
  }
  return Math.abs(hash)
}

/** Deterministic fallback index for a category with no manual color override. */
export function defaultChannelColorIndex(category: string): number {
  return hashString(normalizeCategoryKey(category)) % CHANNEL_COLORS.length
}
