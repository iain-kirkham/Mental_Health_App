import type { EnergyRating } from '@/types'

const SCORE_EMOJIS = ['😢', '😕', '😐', '😊', '🎉']

/** Seconds as a zero-padded mm:ss countdown. */
export function formatTime(seconds: number): string {
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

/** Text colour for a countdown, shifting from calm to urgent as time runs out. */
export function getTimerColorClass(timeLeft: number, totalTime: number): string {
  const percentRemaining = totalTime > 0 ? timeLeft / totalTime : 0
  if (percentRemaining > 0.66) return 'text-chart-2'
  if (percentRemaining > 0.33) return 'text-chart-3'
  return 'text-destructive'
}

export function getScoreEmoji(score: number): string {
  return SCORE_EMOJIS[score - 1] || '😐'
}

export const ENERGY_RATING_OPTIONS: {
  value: EnergyRating
  label: string
  icon: string
  colorClass: string
  badgeClass: string
  ariaLabel: string
}[] = [
  {
    value: 'ENERGIZING',
    label: 'Energizing',
    icon: '⚡',
    colorClass: 'bg-chart-2/10 hover:bg-chart-2/20 border-chart-2/40 text-chart-2',
    badgeClass: 'border-transparent bg-chart-2/15 text-chart-2',
    ariaLabel: 'Toggle energizing rating',
  },
  {
    value: 'DRAINING',
    label: 'Draining',
    icon: '🪫',
    colorClass: 'bg-chart-3/10 hover:bg-chart-3/20 border-chart-3/40 text-chart-3',
    badgeClass: 'border-transparent bg-chart-3/15 text-chart-3',
    ariaLabel: 'Toggle draining rating',
  },
]

export function getEnergyRatingOption(energyRating: EnergyRating | null | undefined) {
  return ENERGY_RATING_OPTIONS.find((option) => option.value === energyRating) ?? null
}
